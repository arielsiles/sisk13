-- ============================================================================
-- v6.0.104  Ordenes de Venta (modulo Clientes / area comercial) - Fase 2/3
-- ============================================================================
--  Documento cabecera-detalle + notas seleccionadas + permisos del flujo.
--    - ordenventa            (cabecera; snapshot comprador, totales, workflow)
--    - ordenventadetalle     (lineas: producto terminado + centro de costo)
--    - ordenventa_nota_sel   (notas seleccionadas por orden, N a N)
--
--  Esquema: terdemol. hbm2ddl = validate: columnas DEBEN coincidir con el mapeo.
-- ============================================================================


-- ----------------------------------------------------------------------------
-- 1) Cabecera
-- ----------------------------------------------------------------------------
CREATE TABLE ordenventa (
    idordenventa            BIGINT         NOT NULL,
    no_cia                  VARCHAR(2)     NOT NULL,
    nro_orden               VARCHAR(20)    NOT NULL,
    estado                  VARCHAR(5)     NOT NULL,
    fecha                   DATETIME       NOT NULL,
    fecha_entrega           DATE           NULL,
    idpersonacliente        BIGINT         NOT NULL,
    comprador               VARCHAR(300)   NULL,
    nro_registro            VARCHAR(50)    NULL,
    direccion               VARCHAR(500)   NULL,
    telefono                VARCHAR(50)    NULL,
    fax                     VARCHAR(50)    NULL,
    email                   VARCHAR(150)   NULL,
    moneda                  VARCHAR(1)     NOT NULL,
    idincoterm              BIGINT         NULL,
    incoterm_lugar          VARCHAR(200)   NULL,
    cotizacion              VARCHAR(100)   NULL,
    sc_codigo               VARCHAR(100)   NULL,
    cc_codigo               VARCHAR(100)   NULL,
    comentarios             VARCHAR(2000)  NULL,
    observaciones_gerencia  VARCHAR(2000)  NULL,
    sub_total               DECIMAL(16,2)  NULL DEFAULT 0,
    descuento               DECIMAL(16,2)  NULL DEFAULT 0,
    recargo                 DECIMAL(16,2)  NULL DEFAULT 0,
    total                   DECIMAL(16,2)  NULL DEFAULT 0,
    created_at              DATETIME       NULL,
    created_by              VARCHAR(50)    NULL,
    fecha_revision          DATETIME       NULL,
    fecha_aprobacion        DATETIME       NULL,
    aprobado_por            VARCHAR(50)    NULL,
    fecha_anulacion         DATETIME       NULL,
    version                 BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (idordenventa),
    KEY ix_ordenventa_cliente (idpersonacliente),
    KEY ix_ordenventa_incoterm (idincoterm),
    CONSTRAINT fk_ordenventa_cliente
        FOREIGN KEY (idpersonacliente) REFERENCES personacliente (idpersonacliente),
    CONSTRAINT fk_ordenventa_incoterm
        FOREIGN KEY (idincoterm) REFERENCES incoterm (idincoterm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 2) Detalle (lineas)
--    El producto terminado se referencia por (no_cia, cod_art) hacia
--    inv_articulos, mapeado insertable=false en JPA (sin FK explicita aqui).
-- ----------------------------------------------------------------------------
CREATE TABLE ordenventadetalle (
    idordenventadetalle       BIGINT         NOT NULL,
    no_cia                    VARCHAR(2)     NOT NULL,
    idordenventa              BIGINT         NOT NULL,
    nro                       BIGINT         NOT NULL,
    cod_art                   VARCHAR(20)    NOT NULL,
    idcentrocosto_comercial   BIGINT         NULL,
    descripcion               VARCHAR(500)   NULL,
    unidad_medida             VARCHAR(50)    NULL,
    cantidad                  DECIMAL(16,2)  NOT NULL DEFAULT 0,
    precio_unitario           DECIMAL(16,2)  NOT NULL DEFAULT 0,
    total                     DECIMAL(16,2)  NOT NULL DEFAULT 0,
    version                   BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (idordenventadetalle),
    KEY ix_ordenventadet_orden (idordenventa),
    KEY ix_ordenventadet_cc (idcentrocosto_comercial),
    CONSTRAINT fk_ordenventadet_orden
        FOREIGN KEY (idordenventa) REFERENCES ordenventa (idordenventa),
    CONSTRAINT fk_ordenventadet_cc
        FOREIGN KEY (idcentrocosto_comercial) REFERENCES centrocosto_comercial (idcentrocosto_comercial)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 3) Notas seleccionadas por orden (N a N con ordenventa_nota)
-- ----------------------------------------------------------------------------
CREATE TABLE ordenventa_nota_sel (
    idordenventa       BIGINT NOT NULL,
    idordenventa_nota  BIGINT NOT NULL,
    PRIMARY KEY (idordenventa, idordenventa_nota),
    KEY ix_ovnotasel_nota (idordenventa_nota),
    CONSTRAINT fk_ovnotasel_orden
        FOREIGN KEY (idordenventa) REFERENCES ordenventa (idordenventa),
    CONSTRAINT fk_ovnotasel_nota
        FOREIGN KEY (idordenventa_nota) REFERENCES ordenventa_nota (idordenventa_nota)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 4) Semillas de secuencia (proximo id / proximo correlativo). Solo si no existe.
-- ----------------------------------------------------------------------------
INSERT INTO secuencia (tabla, valor)
    SELECT 'ordenventa', 0 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'ordenventa');

INSERT INTO secuencia (tabla, valor)
    SELECT 'ordenventadetalle', 0 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'ordenventadetalle');

-- correlativo del numero de orden por compania (defaultCompanyNumber = '01')
INSERT INTO secuencia (tabla, valor)
    SELECT 'ordenventa_01', 1 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'ordenventa_01');


-- ----------------------------------------------------------------------------
-- 5) Permisos (funcionalidad). idmodulo = 1 (customers).
--    SALESORDER: CRUD (permiso 15). Los permisos de flujo se modelan como
--    targets propios verificados con accion VIEW (patron del sistema, ej.
--    FIXEDASSETPURCHASEORDERAPPROVE).
--    Orden: (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
-- ----------------------------------------------------------------------------
insert into funcionalidad values (487, 'SALESORDER',        'Ordenes de Venta',                        1, 15, 'menu.customers.sales.salesOrder',        1);
insert into funcionalidad values (488, 'SALESORDERSEND',    'Orden de Venta: enviar a revision',       1,  1, 'menu.customers.sales.salesOrder.send',   1);
insert into funcionalidad values (489, 'SALESORDERAPPROVE', 'Orden de Venta: aprobar/rechazar',        1,  1, 'menu.customers.sales.salesOrder.approve',1);
insert into funcionalidad values (490, 'SALESORDERNULLIFY', 'Orden de Venta: anular',                  1,  1, 'menu.customers.sales.salesOrder.nullify',1);
