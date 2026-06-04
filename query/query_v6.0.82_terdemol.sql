-- ============================================================================
-- v6.0.82 :: Codigo CLIENTE / TRANSBORDO movido de Cliente al Despacho
-- ============================================================================
--
--  Correccion de enfoque respecto a v6.0.81:
--  El codigo "CLIENTE / TRANSBORDO" que se imprime en el Certificado VARIA
--  por cada despacho (no es un atributo fijo del cliente). Por lo tanto:
--
--    1) Se REVIERTE la columna agregada en v6.0.81 a personacliente
--       (codigo_transbordo + su indice unico).
--    2) Se AGREGA codigo_transbordo VARCHAR(100) a inv_valedespacho.
--
--  El campo es opcional (NULL permitido) y hasta 100 caracteres.
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- 1) Revertir columna en personacliente (introducida en v6.0.81)
-- ----------------------------------------------------------------------------
ALTER TABLE personacliente
    DROP INDEX uq_personacliente_transbordo;

ALTER TABLE personacliente
    DROP COLUMN codigo_transbordo;


-- ----------------------------------------------------------------------------
-- 2) Eliminar codigo_transbordo (texto libre) del despacho.
--    La celda CLIENTE / TRANSBORDO del certificado pasa a usar
--    client.codigo, manteniendo la FK idcliente del despacho. La columna
--    se elimina solo si existe (idempotente para BDs frescas).
-- ----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'inv_valedespacho'
                    AND COLUMN_NAME = 'codigo_transbordo');
SET @sql := IF(@col_exists > 0,
               'ALTER TABLE inv_valedespacho DROP COLUMN codigo_transbordo',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ============================================================================
-- 3) Catalogo de Tipos de Bolsa/Envase (TOTAL ENTREGADO dinamico)
-- ============================================================================
--
--  Permite generar automaticamente el texto de la columna TOTAL ENTREGADO del
--  Certificado:  {bolsas} bolsas {nombre} de {capacidad}
--    Ej. caso 1: "28 bolsas Big Bag de 1 tonelada"
--        caso 2: "56 bolsas Big Bag de 1/2 tonelada"
--
--  El numero de bolsas se toma de inv_valedespacho_det.cantidad_bolsas (ya
--  existente). El catalogo aporta el nombre del envase y la etiqueta de
--  capacidad por bolsa. capacidad_kg es opcional y se usa para la validacion
--  suave (bolsas * capacidad ~= peso de la linea).
--
--  Estado VIG/ANL (borrado logico), multi-empresa via idcompania, igual que
--  los catalogos de Conductor/Vehiculo (v6.0.80).
-- ----------------------------------------------------------------------------
CREATE TABLE inv_tipo_envase (
    idtipoenvase        BIGINT       NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(80)  NOT NULL,
    etiqueta_capacidad  VARCHAR(60)  NOT NULL,
    capacidad_kg        DECIMAL(12,3) NULL,
    estado              VARCHAR(3)   NOT NULL DEFAULT 'VIG',    -- VIG | ANL
    createdby           VARCHAR(4)   NULL,
    createddate         DATETIME     NULL,
    updatedby           VARCHAR(4)   NULL,
    updateddate         DATETIME     NULL,
    version             BIGINT       DEFAULT 0,
    idcompania          BIGINT       NOT NULL,
    PRIMARY KEY (idtipoenvase),
    UNIQUE KEY uq_tipoenvase_nombre (idcompania, nombre, etiqueta_capacidad),
    KEY ix_tipoenvase_estado (idcompania, estado),
    CONSTRAINT fk_tipoenvase_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 4) Vincular el tipo de envase a la linea del despacho (por linea)
-- ----------------------------------------------------------------------------
ALTER TABLE inv_valedespacho_det
    ADD COLUMN idtipoenvase BIGINT NULL AFTER cantidad_bolsas,
    ADD KEY ix_valedespacho_det_tipoenvase (idtipoenvase),
    ADD CONSTRAINT fk_valedespacho_det_tipoenvase
        FOREIGN KEY (idtipoenvase) REFERENCES inv_tipo_envase (idtipoenvase);


-- ----------------------------------------------------------------------------
-- 5) Permiso del catalogo (WAREHOUSEPACKAGING)
--   Bitmask: VIEW=1, CREATE=2, UPDATE=4, DELETE=8 => CRUD completo = 15.
--   idmodulo = 5 (warehouse), igual que WAREHOUSEDISPATCH*.
-- ----------------------------------------------------------------------------
insert into funcionalidad values (461, 'WAREHOUSEPACKAGING', 'Catalogo de Tipos de Bolsa/Envase', 5, 15, 'menu.warehouse.dispatch.packaging', 1);

-- Asignacion por defecto al rol Administrador (idrol=1).
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (461, 1, 15, 1, 5);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';


-- ----------------------------------------------------------------------------
-- 6) Semillas de ejemplo (idcompania = 1). Ajustar/eliminar segun la empresa.
-- ----------------------------------------------------------------------------
insert into inv_tipo_envase (nombre, etiqueta_capacidad, capacidad_kg, estado, version, idcompania)
    values ('Big Bag', '1 tonelada',   1000.000, 'VIG', 0, 1);
insert into inv_tipo_envase (nombre, etiqueta_capacidad, capacidad_kg, estado, version, idcompania)
    values ('Big Bag', '1/2 tonelada',  500.000, 'VIG', 0, 1);


-- ============================================================================
-- 9) Tipo de bolsa: peso bruto promedio por bolsa (DETALLE EN PESO observ.)
-- ============================================================================
--
--  Peso real pesado en balanza por bolsa (contenido + peso de la bolsa).
--  Es distinto de capacidad_kg (capacidad NETA de contenido). Por ejemplo:
--    Big Bag 1 tonelada: capacidad_kg=1000, peso_bruto_promedio_kg=1001
--    Big Bag 1/2 tonelada: capacidad_kg=500,  peso_bruto_promedio_kg=501
--  Si esta NULL, la OBSERVACION de la Nota de Remision sale vacia.
-- ----------------------------------------------------------------------------
ALTER TABLE inv_tipo_envase
    ADD COLUMN peso_bruto_promedio_kg DECIMAL(12,3) NULL AFTER capacidad_kg;

-- Semillas (ajustar al peso real medido por la empresa)
update inv_tipo_envase set peso_bruto_promedio_kg = 1001.000
    where nombre = 'Big Bag' and etiqueta_capacidad = '1 tonelada';
update inv_tipo_envase set peso_bruto_promedio_kg =  501.000
    where nombre = 'Big Bag' and etiqueta_capacidad = '1/2 tonelada';


-- ============================================================================
-- 7) Cliente: codigo de prefijo + ampliar codigo cliente
-- ============================================================================
--
--  Se agrega CODPREFIJO (VARCHAR 10) a personacliente y se amplia
--  CODIGOCLIENTE de VARCHAR(10) a VARCHAR(100). Ambos opcionales.
--  Por ahora no se refleja en Despachos (proxima iteracion).
-- ----------------------------------------------------------------------------
ALTER TABLE personacliente
    ADD COLUMN codprefijo VARCHAR(10) NULL AFTER codigocliente,
    MODIFY COLUMN codigocliente VARCHAR(100) NULL;


-- ============================================================================
-- 8) Permisos para ocultar campos del formulario de Cliente
-- ============================================================================
--
--  Cuatro permisos nuevos (uno por campo, los dos % Descuento agrupados).
--  Solo se usa el bit VIEW (1): si el rol tiene el permiso, el campo aparece
--  en el formulario de Cliente; si no lo tiene, queda oculto (rendered=false).
--
--  idmodulo = 1 (mismo que CLIENT, ver funcionalidad 262 en v5.0.0).
-- ----------------------------------------------------------------------------
insert into funcionalidad values (462, 'CLIENTTYPE',        'Cliente: campo Tipo Cliente',              1, 1, 'Functionality.customers.client.field.type',         1);
insert into funcionalidad values (463, 'CLIENTTERRITORY',   'Cliente: campo Territorio',                1, 1, 'Functionality.customers.client.field.territory',    1);
insert into funcionalidad values (464, 'CLIENTCATEGORY',    'Cliente: campo Categoria Cliente',         1, 1, 'Functionality.customers.client.field.category',     1);
insert into funcionalidad values (465, 'CLIENTDISCOUNT',    'Cliente: campos Porcentaje de Descuento',  1, 1, 'Functionality.customers.client.field.discount',     1);
insert into funcionalidad values (466, 'CLIENTPAYMENT',     'Cliente: campo Metodo de Pago',            1, 1, 'Functionality.customers.client.field.payment',      1);
insert into funcionalidad values (467, 'CLIENTCASHACCOUNT', 'Cliente: campo Cuenta Diferida',           1, 1, 'Functionality.customers.client.field.cashAccount',  1);

-- Asignacion por defecto al rol Administrador (idrol=1). Descomenta segun se requiera.
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (462, 1, 1, 1, 1);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (463, 1, 1, 1, 1);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (464, 1, 1, 1, 1);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (465, 1, 1, 1, 1);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (466, 1, 1, 1, 1);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (467, 1, 1, 1, 1);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';


-- ============================================================================
-- 10) Eliminar columna observacion del detalle del despacho
-- ============================================================================
--
--  La "Descripcion detallada" del reporte ahora se genera dinamicamente a
--  partir de bolsas + tipo de bolsa + producto (no se requiere texto manual
--  por linea). DROP idempotente: solo elimina si la columna existe.
-- ----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'inv_valedespacho_det'
                    AND COLUMN_NAME = 'observacion');
SET @sql := IF(@col_exists > 0,
               'ALTER TABLE inv_valedespacho_det DROP COLUMN observacion',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
