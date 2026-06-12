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


-- ============================================================================
-- 11) Numeracion de bolsas: mover de cabecera al detalle del despacho
-- ============================================================================
--
--  Un despacho puede tener varios productos (detalles) con distintos tipos de
--  bolsa y rangos de numeracion. La numeracion (desde/hasta) y la cantidad
--  total de bolsas se mueven del header al detalle.
--  cantidad_bolsas del header pasa a ser derivado en Java (sum de detalles).
-- ----------------------------------------------------------------------------
ALTER TABLE inv_valedespacho
    DROP COLUMN bolsa_desde,
    DROP COLUMN bolsa_hasta,
    DROP COLUMN cantidad_bolsas;

ALTER TABLE inv_valedespacho_det
    ADD COLUMN bolsa_desde INT NULL AFTER cantidad_bolsas,
    ADD COLUMN bolsa_hasta INT NULL AFTER bolsa_desde;


-- ============================================================================
-- 12) Tipo de bolsa: texto por defecto del DETALLE DE ENVASE
-- ============================================================================
--
--  Pre-llena la columna DETALLE DE ENVASE del nuevo reporte
--  "Detalle de Envases Carguio". Texto editable bolsa por bolsa despues.
-- ----------------------------------------------------------------------------
ALTER TABLE inv_tipo_envase
    ADD COLUMN detalle_envase_default VARCHAR(255) NULL AFTER peso_bruto_promedio_kg;

UPDATE inv_tipo_envase
   SET detalle_envase_default = 'BOLSA BIG BAG SELLADA SIN INPERFECCIONES CON PORTADOCUMENTO'
 WHERE nombre = 'Big Bag';


-- ============================================================================
-- 13) Tabla de envases del despacho (filas del reporte Carguio)
-- ============================================================================
--
--  Una fila por bolsa fisica del despacho. Se generan al APROBAR el despacho:
--  por cada detalle, se crean detail.bagsCount filas con correlativos
--  consecutivos arrancando en detail.bolsa_desde.
--    codigo_identificacion = salesLotCode + "/" + lpad(correlativo, 3, '0')
--      ej. "BAR-03-26/057"
--    detalle_envase        = packaging.detalle_envase_default (editable)
--    peso_neto_aprox_kg    = packaging.capacidad_kg          (editable)
--
--  En estado APROBADO el operador puede editar texto y peso. En FINALIZADO
--  los envases quedan inmutables (solo se imprime el reporte).
-- ----------------------------------------------------------------------------
CREATE TABLE inv_valedespacho_envase (
    idenvase             BIGINT       NOT NULL AUTO_INCREMENT,
    idvaledespacho       BIGINT       NOT NULL,
    iddetalledespacho    BIGINT       NOT NULL,
    numero_correlativo   INT          NOT NULL,
    codigo_identificacion VARCHAR(120) NOT NULL,
    detalle_envase       VARCHAR(255) NULL,
    peso_neto_aprox_kg   DECIMAL(12,3) NULL,
    createdby            VARCHAR(4)   NULL,
    createddate          DATETIME     NULL,
    updatedby            VARCHAR(4)   NULL,
    updateddate          DATETIME     NULL,
    version              BIGINT       DEFAULT 0,
    idcompania           BIGINT       NOT NULL,
    PRIMARY KEY (idenvase),
    KEY ix_envase_dispatch (idvaledespacho),
    KEY ix_envase_detail (iddetalledespacho),
    CONSTRAINT fk_envase_dispatch
        FOREIGN KEY (idvaledespacho) REFERENCES inv_valedespacho (idvaledespacho),
    CONSTRAINT fk_envase_detail
        FOREIGN KEY (iddetalledespacho) REFERENCES inv_valedespacho_det (iddetalledespacho),
    CONSTRAINT fk_envase_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ============================================================================
-- 14) Permisos FINALIZAR y DESFINALIZAR el despacho (separados)
-- ============================================================================
--
--  Bitmask: solo VIEW (1) se usa como flag de habilitacion del boton.
--  idmodulo = 5 (warehouse).
--
--  FINALIZAR: operacion frecuente (operador de almacen). Bloquea edicion
--             de envases del despacho aprobado.
--  DESFINALIZAR: operacion correctiva/excepcional (supervisor/admin).
--                Revierte FIN -> APR para permitir correccion de envases.
-- ----------------------------------------------------------------------------
insert into funcionalidad values (468, 'WAREHOUSEDISPATCHFINALIZE',   'Finalizar Despacho',    5, 1, 'menu.warehouse.dispatch.finalize',   1);
insert into funcionalidad values (469, 'WAREHOUSEDISPATCHUNFINALIZE', 'Desfinalizar Despacho', 5, 1, 'menu.warehouse.dispatch.unfinalize', 1);

-- Asignacion por defecto al rol Administrador (idrol=1).
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (468, 1, 1, 1, 5);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (469, 1, 1, 1, 5);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';


-- ============================================================================
-- 15) HOJA DE RUTA: catalogos ProductDescription + DispatchRoute, FKs en
--     despacho/detalle, vigencia y permisos
-- ============================================================================
--
--  Cambios introducidos:
--    a) Tabla inv_descripcion_producto  - descripciones tecnicas reutilizables
--                                          por producto, con estado
--                                          BORRADOR/APROBADO/INACTIVO.
--    b) Tabla inv_ruta_despacho         - catalogo de rutas con paradas e
--                                          imagen de mapa, mismo ciclo de
--                                          estados. Una imagen por ruta,
--                                          reutilizada por N despachos.
--    c) ALTER inv_valedespacho_det      - FK iddescripcion_producto.
--    d) ALTER inv_valedespacho          - FK idruta + vigencia_dias.
--    e) Permisos
--         470 PRODUCTDESCRIPTION          (CRUD bitmask=15)
--         471 WAREHOUSEDISPATCHROUTE      (CRUD bitmask=15)
--         472 WAREHOUSEDISPATCHROUTESHEET (VIEW  bitmask=1, boton de imprimir)
--
--  Ciclo de estados de los catalogos (en codigo: enum CatalogApprovalState):
--    BORRADOR -> APROBADO -> INACTIVO     (sin vuelta atras)
--    Solo APROBADO se ofrece en selectPopUp del despacho.
--    APROBADO/INACTIVO son inmutables (no se editan).
-- ----------------------------------------------------------------------------

-- 15.a) Catalogo de descripciones tecnicas por producto -----------------------
CREATE TABLE inv_descripcion_producto (
    iddescripcion_producto BIGINT       NOT NULL AUTO_INCREMENT,
    no_cia_art             VARCHAR(2)   NOT NULL,
    cod_art                VARCHAR(6)   NOT NULL,
    descripcion            LONGTEXT     NOT NULL,
    estado                 VARCHAR(15)  NOT NULL DEFAULT 'BORRADOR',
    createdby              VARCHAR(4)   NULL,
    createddate            DATETIME     NULL,
    updatedby              VARCHAR(4)   NULL,
    updateddate            DATETIME     NULL,
    version                BIGINT       DEFAULT 0,
    idcompania             BIGINT       NOT NULL,
    PRIMARY KEY (iddescripcion_producto),
    KEY ix_descprod_art (no_cia_art, cod_art),
    KEY ix_descprod_estado (estado),
    CONSTRAINT fk_descprod_articulo
        FOREIGN KEY (no_cia_art, cod_art) REFERENCES inv_articulos (no_cia, cod_art),
    CONSTRAINT fk_descprod_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- 15.b) Catalogo de rutas de despacho ---------------------------------------
--   imagen_mapa LONGBLOB - se aplica resize+JPEG calidad 85 en el setter del
--                          action (typical ~50-150 KB por ruta). Una imagen
--                          por ruta, NUNCA replicada al despacho.
CREATE TABLE inv_ruta_despacho (
    idruta                    BIGINT       NOT NULL AUTO_INCREMENT,
    nombre                    VARCHAR(120) NOT NULL,
    origen_texto              VARCHAR(200) NULL,
    destino_texto             VARCHAR(200) NULL,
    paradas                   LONGTEXT     NOT NULL,
    imagen_mapa               LONGBLOB     NULL,
    imagen_mapa_content_type  VARCHAR(50)  NULL,
    distancia_km              DECIMAL(8,2) NULL,
    duracion_horas            DECIMAL(6,2) NULL,
    estado                    VARCHAR(15)  NOT NULL DEFAULT 'BORRADOR',
    createdby                 VARCHAR(4)   NULL,
    createddate               DATETIME     NULL,
    updatedby                 VARCHAR(4)   NULL,
    updateddate               DATETIME     NULL,
    version                   BIGINT       DEFAULT 0,
    idcompania                BIGINT       NOT NULL,
    PRIMARY KEY (idruta),
    UNIQUE KEY uq_ruta_nombre (idcompania, nombre),
    KEY ix_ruta_estado (estado),
    CONSTRAINT fk_ruta_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- 15.c) FK descripcion en detalle del despacho ------------------------------
ALTER TABLE inv_valedespacho_det
    ADD COLUMN iddescripcion_producto BIGINT NULL AFTER cod_med;
ALTER TABLE inv_valedespacho_det
    ADD CONSTRAINT fk_valedespdet_descripcion
        FOREIGN KEY (iddescripcion_producto) REFERENCES inv_descripcion_producto (iddescripcion_producto);


-- 15.d) FK ruta + vigencia en cabecera del despacho -------------------------
ALTER TABLE inv_valedespacho
    ADD COLUMN idruta        BIGINT NULL,
    ADD COLUMN vigencia_dias INT    NULL;
ALTER TABLE inv_valedespacho
    ADD CONSTRAINT fk_valedespacho_ruta
        FOREIGN KEY (idruta) REFERENCES inv_ruta_despacho (idruta);


-- 15.e) Permisos --------------------------------------------------------------
--   idmodulo = 5 (warehouse) para los tres.
--   PRODUCTDESCRIPTION y WAREHOUSEDISPATCHROUTE: CRUD completo (bitmask=15).
--   WAREHOUSEDISPATCHROUTESHEET: solo VIEW (bitmask=1) - flag del boton.
insert into funcionalidad values (470, 'PRODUCTDESCRIPTION',          'Catalogo de Descripciones Tecnicas de Producto', 5, 15, 'menu.warehouse.dispatch.productDescription', 1);
insert into funcionalidad values (471, 'WAREHOUSEDISPATCHROUTE',      'Catalogo de Rutas de Despacho',                  5, 15, 'menu.warehouse.dispatch.route',              1);
insert into funcionalidad values (472, 'WAREHOUSEDISPATCHROUTESHEET', 'Imprimir Hoja de Ruta',                          5, 1,  'menu.warehouse.dispatch.routeSheet',         1);

-- Asignacion por defecto al rol Administrador (idrol=1). Comentadas - decide el usuario.
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (470, 1, 15, 1, 5);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (471, 1, 15, 1, 5);
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (472, 1, 1,  1, 5);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';


-- ============================================================================
-- 16) Hoja de Ruta: vigencia maxima en dias
-- ============================================================================
--
--  Se agrega vigencia_maxima_dias en inv_valedespacho. Es un dato editable
--  por el operador en estado BORRADOR junto con vigencia_dias. Se imprime
--  en la Hoja de Ruta como "(Maximo X Dias)" junto al campo de vigencia.
-- ----------------------------------------------------------------------------
ALTER TABLE inv_valedespacho
    ADD COLUMN vigencia_max_dias INT NULL AFTER vigencia_dias;


-- ============================================================================
-- 17) Quitar Centro de Costo del despacho
-- ============================================================================
--
--  Campo antiguo del despacho que no se usa en la operativa. Al APROBAR el
--  despacho, el WarehouseVoucher generado toma el centro de costo desde la
--  constante DispatchVoucherServiceBean.DEFAULT_COST_CENTER_CODE (por
--  defecto '0111').
--
--  Pasos: 1) drop FK constraint (idempotente, busca por nombre real),
--         2) drop column cod_cc (idempotente, solo si existe).
-- ----------------------------------------------------------------------------

-- 17.a) Drop FK que apunta a cod_cc (si existe). El nombre del constraint
--       puede variar entre BDs (se autogenera), por eso lo buscamos.
SET @fk_name := (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inv_valedespacho'
      AND COLUMN_NAME = 'cod_cc'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql := IF(@fk_name IS NOT NULL,
               CONCAT('ALTER TABLE inv_valedespacho DROP FOREIGN KEY ', @fk_name),
               'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 17.b) Drop column cod_cc (idempotente)
SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inv_valedespacho'
      AND COLUMN_NAME = 'cod_cc'
);
SET @sql := IF(@col_exists > 0,
               'ALTER TABLE inv_valedespacho DROP COLUMN cod_cc',
               'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================================
-- 18) Permiso DESAPROBAR despacho
-- ============================================================================
--
--  Permite revertir el estado APROBADO -> BORRADOR. Reversa el WarehouseVoucher
--  generado (devuelve stock) y opcionalmente elimina el detalle de envases
--  (decision del operador en el dialogo de confirmacion).
--
--  Operacion correctiva: NO se debe asignar a operadores comunes, solo a
--  supervisores o administradores.
--  idmodulo = 5 (warehouse). Bitmask 1 = solo VIEW como flag del boton.
-- ----------------------------------------------------------------------------
insert into funcionalidad values (473, 'WAREHOUSEDISPATCHUNAPPROVE', 'Desaprobar Despacho', 5, 1, 'menu.warehouse.dispatch.unapprove', 1);

-- Asignacion por defecto al rol Administrador (idrol=1). Comentada - decide el usuario.
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (473, 1, 1, 1, 5);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';
