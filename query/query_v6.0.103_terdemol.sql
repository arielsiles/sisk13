-- ============================================================================
-- v6.0.103  Ordenes de Venta (modulo Clientes / area comercial) - Fase 1
-- ============================================================================
--  Catalogos base + campo fax en el cliente + permisos.
--    - personacliente.fax          (snapshot del fax en la Orden de Venta)
--    - incoterm                    (catalogo Incoterms 2020, configurable)
--    - centrocosto_comercial       (centros de costo del area comercial)
--    - ordenventa_nota             (notas predefinidas para el recuadro final)
--
--  Esquema: terdemol (Constants.KHIPUS_SCHEMA). hbm2ddl = validate: las tablas
--  y columnas DEBEN existir y coincidir con el mapeo JPA.
-- ============================================================================


-- ----------------------------------------------------------------------------
-- 1) Fax del cliente (snapshot del comprador en la Orden de Venta)
-- ----------------------------------------------------------------------------
ALTER TABLE personacliente
    ADD COLUMN fax VARCHAR(255) NULL AFTER email;


-- ----------------------------------------------------------------------------
-- 2) Catalogo Incoterm (Camara de Comercio Internacional)
-- ----------------------------------------------------------------------------
CREATE TABLE incoterm (
    idincoterm   BIGINT        NOT NULL,
    codigo       VARCHAR(10)   NOT NULL,
    nombre       VARCHAR(150)  NOT NULL,
    descripcion  VARCHAR(500)  NULL,
    activo       INT           NOT NULL DEFAULT 1,
    idcompania   BIGINT        NOT NULL,
    version      BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (idincoterm),
    UNIQUE KEY uq_incoterm_cia_cod (idcompania, codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 3) Catalogo Centro de Costo Comercial (independiente de cg_cencos)
-- ----------------------------------------------------------------------------
CREATE TABLE centrocosto_comercial (
    idcentrocosto_comercial  BIGINT        NOT NULL,
    codigo                   VARCHAR(20)   NOT NULL,
    descripcion              VARCHAR(150)  NOT NULL,
    activo                   INT           NOT NULL DEFAULT 1,
    idcompania               BIGINT        NOT NULL,
    version                  BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (idcentrocosto_comercial),
    UNIQUE KEY uq_cencoscom_cia_cod (idcompania, codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 4) Catalogo de Notas de Orden de Venta (recuadro final, texto sin relacion)
-- ----------------------------------------------------------------------------
CREATE TABLE ordenventa_nota (
    idordenventa_nota  BIGINT         NOT NULL,
    texto              VARCHAR(1000)  NOT NULL,
    orden              INT            NULL DEFAULT 0,
    activo             INT            NOT NULL DEFAULT 1,
    idcompania         BIGINT         NOT NULL,
    version            BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (idordenventa_nota)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 5) Semilla: 11 Incoterms 2020 (compania 1)
-- ----------------------------------------------------------------------------
INSERT INTO incoterm (idincoterm, codigo, nombre, descripcion, activo, idcompania, version) VALUES
    (1,  'EXW', 'En Fabrica',                          'Ex Works',                          1, 1, 0),
    (2,  'FCA', 'Franco Transportista',                'Free Carrier',                      1, 1, 0),
    (3,  'FAS', 'Franco al Costado del Buque',         'Free Alongside Ship',               1, 1, 0),
    (4,  'FOB', 'Franco a Bordo',                      'Free On Board',                     1, 1, 0),
    (5,  'CFR', 'Costo y Flete',                       'Cost and Freight',                  1, 1, 0),
    (6,  'CIF', 'Costo, Seguro y Flete',               'Cost, Insurance and Freight',       1, 1, 0),
    (7,  'CPT', 'Transporte Pagado Hasta',             'Carriage Paid To',                  1, 1, 0),
    (8,  'CIP', 'Transporte y Seguro Pagados Hasta',   'Carriage and Insurance Paid To',    1, 1, 0),
    (9,  'DAP', 'Entregado en Lugar',                  'Delivered At Place',                1, 1, 0),
    (10, 'DPU', 'Entregado en Lugar Descargado',       'Delivered at Place Unloaded',       1, 1, 0),
    (11, 'DDP', 'Entregado con Derechos Pagados',      'Delivered Duty Paid',               1, 1, 0);


-- ----------------------------------------------------------------------------
-- 6) Semillas de secuencia (GenerationType.TABLE, allocationSize = 1:
--    'valor' es el PROXIMO id a asignar). Solo inserta si no existe.
-- ----------------------------------------------------------------------------
INSERT INTO secuencia (tabla, valor)
    SELECT 'incoterm', 12 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'incoterm');

INSERT INTO secuencia (tabla, valor)
    SELECT 'centrocosto_comercial', 0 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'centrocosto_comercial');

INSERT INTO secuencia (tabla, valor)
    SELECT 'ordenventa_nota', 0 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'ordenventa_nota');


-- ----------------------------------------------------------------------------
-- 7) Permisos (funcionalidad). permiso = 15 (VIEW+CREATE+UPDATE+DELETE).
--    idmodulo = 1 (customers). nombrerecurso = clave de messages_app.properties.
--    La asignacion de accesos por rol se hace por el panel de permisos.
--    Orden de columnas: (idfuncionalidad, codigo, descripcion, idmodulo,
--                        permiso, nombrerecurso, idcompania)
-- ----------------------------------------------------------------------------
insert into funcionalidad values (484, 'INCOTERM',            'Catalogo de Incoterms',                1, 15, 'menu.customers.configuration.incoterm',            1);
insert into funcionalidad values (485, 'COMMERCIALCOSTCENTER','Catalogo de Centros de Costo Comercial',1, 15, 'menu.customers.configuration.commercialCostCenter',1);
insert into funcionalidad values (486, 'SALESORDERNOTE',      'Catalogo de Notas de Orden de Venta',   1, 15, 'menu.customers.configuration.salesOrderNote',      1);
