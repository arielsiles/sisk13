-- ============================================================================
-- v6.0.93 :: Observacion del Despacho configurable (catalogo + estado + permiso)
-- ============================================================================
--
--  La "Observacion" de la cabecera del Despacho deja de ser texto libre y pasa
--  a SELECCIONARSE de un catalogo configurable. Cada entrada del catalogo tiene:
--    - NOMBRE descriptivo (lo que el operador elige en el dropdown)
--    - TEXTO de la observacion (lo que queda registrado en el despacho)
--    - ESTADO BORRADOR/APROBADO/INACTIVO (mismo ciclo que ProductDescription /
--      DispatchRoute, enum CatalogApprovalState). Solo APROBADO se ofrece en el
--      dropdown del despacho.
--
--  El despacho NO cambia su esquema: el TEXTO de la observacion seleccionada se
--  sigue guardando en la columna existente inv_valedespacho.observacion, tal
--  como antes. NO se agrega columna ni FK; el catalogo solo alimenta el dropdown.
--
--  Ciclo de estados (en codigo: enum CatalogApprovalState):
--    BORRADOR -> APROBADO -> INACTIVO     (sin vuelta atras)
--    APROBADO/INACTIVO son inmutables (no se editan).
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- 1) Catalogo de Observaciones de Despacho
-- ----------------------------------------------------------------------------
CREATE TABLE inv_observacion_despacho (
    idobservacion   BIGINT        NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(120)  NOT NULL,
    observacion     VARCHAR(1000) NOT NULL,    -- alineado a inv_valedespacho.observacion
    estado          VARCHAR(15)   NOT NULL DEFAULT 'BORRADOR',
    createdby       VARCHAR(4)    NULL,
    createddate     DATETIME      NULL,
    updatedby       VARCHAR(4)    NULL,
    updateddate     DATETIME      NULL,
    version         BIGINT        DEFAULT 0,
    idcompania      BIGINT        NOT NULL,
    PRIMARY KEY (idobservacion),
    UNIQUE KEY uq_obsdespacho_nombre (idcompania, nombre),
    KEY ix_obsdespacho_estado (estado),
    CONSTRAINT fk_obsdespacho_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 2) Permiso del catalogo (WAREHOUSEDISPATCHOBSERVATION)
--   Bitmask: VIEW=1, CREATE=2, UPDATE=4, DELETE=8 => CRUD completo = 15.
--   idmodulo = 5 (warehouse), igual que los demas catalogos del despacho.
-- ----------------------------------------------------------------------------
insert into funcionalidad values (478, 'WAREHOUSEDISPATCHOBSERVATION', 'Catalogo de Observaciones de Despacho', 5, 15, 'menu.warehouse.dispatch.observation', 1);

-- Asignacion por defecto al rol Administrador (idrol=1). Descomenta segun se requiera.
-- insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (478, 1, 15, 1, 5);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';


-- ----------------------------------------------------------------------------
-- 3) Semillas de ejemplo (idcompania = 1). Ajustar/eliminar segun la empresa.
--    Quedan en estado APROBADO para que aparezcan de inmediato en el dropdown.
-- ----------------------------------------------------------------------------
insert into inv_observacion_despacho (nombre, observacion, estado, version, idcompania)
    values ('Sin observacion', 'Despacho sin observaciones adicionales.', 'APROBADO', 0, 1);
insert into inv_observacion_despacho (nombre, observacion, estado, version, idcompania)
    values ('Carga sellada', 'Carga sellada en origen, verificar precintos en destino.', 'APROBADO', 0, 1);


-- ============================================================================
-- 4) Descripcion Tecnica de Producto: nombre descriptivo (solo UX)
-- ============================================================================
--
--  Se agrega un NOMBRE descriptivo corto a inv_descripcion_producto. Es lo que
--  se muestra en el dropdown "Descripcion (Hoja de Ruta)" del despacho, en vez
--  del texto largo. NO altera funcionamiento ni reportes: la descripcion (texto)
--  que consumen los reportes queda intacta; el nombre es solo etiqueta de UX.
--
--  Nullable: los registros existentes quedan sin nombre y el codigo hace
--  fallback al resumen de la descripcion (ProductDescription.getDisplayName).
-- ----------------------------------------------------------------------------
ALTER TABLE inv_descripcion_producto
    ADD COLUMN nombre VARCHAR(120) NULL AFTER cod_art;


-- ============================================================================
-- 5) Despacho: interruptor para DESACTIVAR el control de stock al aprobar
-- ============================================================================
--
--  Permite registrar despachos de meses atras aunque no haya stock suficiente
--  a la fecha. Interruptor por empresa en la tabla configuracion:
--    desp_controla_inventario = 1  -> control de stock ACTIVO (comportamiento
--                                     normal de hoy: rechaza si no hay stock).
--    desp_controla_inventario = 0  -> al APROBAR un despacho NO se valida stock
--                                     suficiente; el inventario igual se mueve
--                                     (puede quedar negativo), costos y asiento
--                                     contable se generan como siempre.
--
--  SOLO afecta a los vales de Despacho (documento DSP). Compras, produccion y
--  transferencias NO se ven afectados. Default 1 = sin cambios respecto a hoy.
--  Cuando se regularice el inventario, volver a poner en 1 con un UPDATE.
--
--  IMPORTANTE: el tipo debe ser INT (no TINYINT(1)). El mapeo del entity usa
--  IntegerBooleanUserType y el validador de Hibernate exige columna 'integer';
--  TINYINT(1) lo reporta el driver MySQL como 'bit' y el despliegue falla con
--  "Wrong column type ... Found: bit, expected: integer". Las demas columnas
--  booleanas de configuracion (occodifactiva, etc.) son int(11).
-- ----------------------------------------------------------------------------
ALTER TABLE configuracion
    ADD COLUMN desp_controla_inventario INT NOT NULL DEFAULT 1;

-- Si la columna YA fue creada como TINYINT(1) (despliegue previo fallido),
-- corregir el tipo a INT con:
--   ALTER TABLE configuracion MODIFY COLUMN desp_controla_inventario INT NOT NULL DEFAULT 1;

-- Para activar la etapa de carga retroactiva (desactivar control de stock):
   UPDATE configuracion SET desp_controla_inventario = 0 WHERE no_cia = '01';
-- Para reactivar el control cuando el inventario este al dia:
--   UPDATE configuracion SET desp_controla_inventario = 1 WHERE no_cia = '01';
