-- ============================================================================
-- v6.0.78 :: Vale de Egreso por Despacho de Productos Terminados
--             Fase 0 - Schema, secuencia, permisos.
--             Plan: docs/dispatch_voucher_implementation_plan.md
--             Requerimientos: docs/dispatch_voucher_requirements.md
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0.1 Catalogo Lugar de Despacho/Entrega (origen/destino)
-- ----------------------------------------------------------------------------
CREATE TABLE inv_lugardespacho (
    idlugardespacho   BIGINT       NOT NULL AUTO_INCREMENT,
    codigo            VARCHAR(20)  NOT NULL,
    descripcion       VARCHAR(150) NOT NULL,
    direccion         VARCHAR(250),
    tipo              VARCHAR(20)  NOT NULL,    -- ORIGEN | DESTINO | AMBOS
    activo            TINYINT(1)   NOT NULL DEFAULT 1,
    version           BIGINT       DEFAULT 0,
    idcompania        BIGINT       NOT NULL,
    PRIMARY KEY (idlugardespacho),
    UNIQUE KEY uq_lugardespacho_cod (idcompania, codigo),
    KEY ix_lugardespacho_tipo (idcompania, tipo, activo),
    CONSTRAINT fk_lugardespacho_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ----------------------------------------------------------------------------
-- 0.2 Cabecera del Vale de Despacho
-- ----------------------------------------------------------------------------
--   estado:        BORRADOR | APROBADO | ANULADO
--   no_orden_entrega: secuencia 'DISPATCH_ORDER_NUMBER' por compania
--                  (la fila en gensecuencia se autocrea en la 1ra llamada)
--   no_trans_vale + no_cia_vale: FK al WarehouseVoucher generado al aprobar
--                  (NULL hasta que la aprobacion sea exitosa)
-- ----------------------------------------------------------------------------
CREATE TABLE inv_valedespacho (
    idvaledespacho       BIGINT        NOT NULL AUTO_INCREMENT,
    estado               VARCHAR(15)   NOT NULL,
    no_orden_entrega     BIGINT        NULL,
    fecha_despacho       DATETIME      NOT NULL,

    -- Vendedor de entrega (JobContract)
    idcontratopuestovend BIGINT        NULL,

    -- Datos comerciales
    codigo_lote_venta    VARCHAR(80)   NOT NULL,
    cantidad_bolsas      INT           NOT NULL,
    numero_factura       VARCHAR(50)   NULL,
    -- Transportadora: cxp_proveedores PK es compuesta (no_cia, cod_prov).
    --   no_cia se comparte con la fila (mas abajo), aqui solo se almacena cod_prov.
    cod_prov             VARCHAR(6)    NULL,
    idcliente            BIGINT        NULL,    -- personacliente.idpersonacliente
    idturno              BIGINT        NULL,    -- xpr_grupo.idgrupo
    idlugar_origen       BIGINT        NULL,
    idlugar_destino      BIGINT        NULL,

    -- Conductor / vehiculo (texto libre por despacho)
    conductor_nombre     VARCHAR(120)  NOT NULL,
    conductor_licencia   VARCHAR(30)   NOT NULL,
    conductor_celular    VARCHAR(30)   NULL,
    vehiculo_placa       VARCHAR(20)   NOT NULL,
    vehiculo_marca       VARCHAR(50)   NULL,
    vehiculo_color       VARCHAR(30)   NULL,

    -- Carguio
    hora_inicio          DATETIME      NOT NULL,
    hora_fin             DATETIME      NOT NULL,
    no_camion            INT           NOT NULL,

    -- Pesaje en balanza
    no_boleta_balanza    VARCHAR(30)   NOT NULL,
    peso_tara_kg         DECIMAL(12,3) NOT NULL,
    peso_bruto_kg        DECIMAL(12,3) NOT NULL,
    peso_neto_kg         DECIMAL(12,3) NOT NULL,

    -- Numeracion de bolsas (rango)
    bolsa_desde          INT           NOT NULL,
    bolsa_hasta          INT           NOT NULL,

    -- Almacen origen / responsable / unidad / centro de costo
    -- Warehouse PK es compuesta (no_cia, cod_alm); CostCenter PK es compuesta
    -- (no_cia, cod_cc). Se replica el patron usado en inv_vales.
    no_cia               VARCHAR(2)    NULL,
    cod_alm              VARCHAR(6)    NULL,
    cod_cc               VARCHAR(8)    NULL,
    idresponsable        BIGINT        NULL,
    idunidadnegocio      BIGINT        NULL,

    -- Enlace al WarehouseVoucher generado al aprobar
    no_trans_vale        VARCHAR(10)   NULL,
    no_cia_vale          VARCHAR(2)    NULL,

    -- Datos varios
    observacion          VARCHAR(500)  NULL,

    -- Auditoria
    fecha_anul           DATETIME      NULL,
    usuario_anul         VARCHAR(4)    NULL,
    motivo_anul          VARCHAR(250)  NULL,
    createdby            VARCHAR(4)    NULL,
    createddate          DATETIME      NULL,
    updatedby            VARCHAR(4)    NULL,
    updateddate          DATETIME      NULL,
    version              BIGINT        DEFAULT 0,
    idcompania           BIGINT        NOT NULL,

    PRIMARY KEY (idvaledespacho),
    KEY ix_valedespacho_estado (idcompania, estado),
    KEY ix_valedespacho_fecha  (idcompania, fecha_despacho),
    KEY ix_valedespacho_orden  (idcompania, no_orden_entrega),
    KEY ix_valedespacho_alm    (no_cia, cod_alm),
    KEY ix_valedespacho_vale   (no_cia_vale, no_trans_vale),
    KEY ix_valedespacho_prov   (no_cia, cod_prov),
    CONSTRAINT fk_valedespacho_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania),
    CONSTRAINT fk_valedespacho_almacen
        FOREIGN KEY (no_cia, cod_alm) REFERENCES inv_almacenes (no_cia, cod_alm),
    CONSTRAINT fk_valedespacho_proveedor
        FOREIGN KEY (no_cia, cod_prov) REFERENCES cxp_proveedores (no_cia, cod_prov),
    CONSTRAINT fk_valedespacho_lugarorigen
        FOREIGN KEY (idlugar_origen) REFERENCES inv_lugardespacho (idlugardespacho),
    CONSTRAINT fk_valedespacho_lugardestino
        FOREIGN KEY (idlugar_destino) REFERENCES inv_lugardespacho (idlugardespacho)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ----------------------------------------------------------------------------
-- 0.3 Detalle del Vale de Despacho (productos terminados)
-- ----------------------------------------------------------------------------
--   ProductItem PK es compuesta (no_cia, cod_art). Se mantiene el patron.
--   costo_unitario y monto son snapshot al aprobar (no cambian luego).
-- ----------------------------------------------------------------------------
CREATE TABLE inv_valedespacho_det (
    iddetalledespacho    BIGINT        NOT NULL AUTO_INCREMENT,
    idvaledespacho       BIGINT        NOT NULL,
    no_cia_art           VARCHAR(2)    NOT NULL,
    cod_art              VARCHAR(6)    NOT NULL,
    cod_med              VARCHAR(6)    NOT NULL,    -- MeasureUnit code (FK: no_cia_art + cod_med)
    cantidad             DECIMAL(14,6) NOT NULL,
    costo_unitario       DECIMAL(14,6) NOT NULL DEFAULT 0,
    monto                DECIMAL(14,6) NOT NULL DEFAULT 0,
    cantidad_bolsas      INT           NULL,
    observacion          VARCHAR(250)  NULL,
    version              BIGINT        DEFAULT 0,
    idcompania           BIGINT        NOT NULL,
    PRIMARY KEY (iddetalledespacho),
    KEY ix_detdespacho_vale (idvaledespacho),
    KEY ix_detdespacho_art (no_cia_art, cod_art),
    CONSTRAINT fk_detdespacho_vale
        FOREIGN KEY (idvaledespacho) REFERENCES inv_valedespacho (idvaledespacho)
        ON DELETE CASCADE,
    CONSTRAINT fk_detdespacho_compania
        FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ----------------------------------------------------------------------------
-- 0.4 Permisos (funcionalidad + derechoacceso)
-- ----------------------------------------------------------------------------
--   Bitmask de permiso: VIEW=1, CREATE=2, UPDATE=4, DELETE=8.
--   Para CRUD completo se usa 15. Para acciones puntuales (approve, reverse),
--   solo VIEW=1 - la accion en si misma se renderiza si tiene VIEW del permiso
--   especifico (mismo patron de WAREHOUSEVOUCHERREVERSE y WAREHOUSEVOUCHERAPPROVAL).
--   idmodulo = 5 (warehouse) para alinearse con el resto de funcionalidades
--   WAREHOUSE* (WAREHOUSEVOUCHER, WAREHOUSEVOUCHERREVERSE, INVENTORYRECONCILIATION).
--   Columnas funcionalidad: (idfuncionalidad, codigo, descripcion, idmodulo,
--                            permiso, nombrerecurso, idcompania).
-- ----------------------------------------------------------------------------
insert into funcionalidad values (453, 'WAREHOUSEDISPATCH',         'Despacho de Productos Terminados',          5, 15, 'menu.warehouse.dispatch',         1);
insert into funcionalidad values (454, 'WAREHOUSEDISPATCHAPPROVAL', 'Aprobacion de Despacho',                    5,  1, 'menu.warehouse.dispatch.approve', 1);
insert into funcionalidad values (455, 'WAREHOUSEDISPATCHREVERSE',  'Anulacion / Reversion de Despacho',         5,  1, 'menu.warehouse.dispatch.reverse', 1);
insert into funcionalidad values (456, 'WAREHOUSEDISPATCHPLACE',    'Catalogo Lugares de Despacho/Entrega',      5, 15, 'menu.warehouse.dispatch.place',   1);

-- Asignacion por defecto al rol Administrador (idrol=1). Mismo idmodulo que
-- en funcionalidad (requisito de AccessRight.findByUser).
insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (453, 1, 15, 1, 5);
insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (454, 1,  1, 1, 5);
insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (455, 1,  1, 1, 5);
insert into derechoacceso (idfuncionalidad, idrol, permiso, idcompania, idmodulo) values (456, 1, 15, 1, 5);

-- Actualizar secuencia interna de funcionalidad
update secuencia set valor = (select max(e.idfuncionalidad)+1 from funcionalidad e) where tabla = 'funcionalidad';

-- ----------------------------------------------------------------------------
-- 0.5 Notas
-- ----------------------------------------------------------------------------
--   * La fila en gensecuencia para 'DISPATCH_ORDER_NUMBER' NO se siembra
--     manualmente: SequenceServiceBean.createOrUpdateNextSequenceValue
--     crea la fila en la primera invocacion (em.persist).
--   * El DocumentType (cod_doc) y el valor enum WarehouseVoucherType.DESP
--     que se usaran al generar el WarehouseVoucher de egreso desde la
--     aprobacion se sembraran en Fase 5 cuando este implementada esa logica.
--   * Las claves i18n se agregan en el commit aparte que toca
--     resources/messages_app.properties.
-- ----------------------------------------------------------------------------
