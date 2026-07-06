-- ============================================================================
-- v6.0.106  Orden de Venta: catalogo de Terminos de cabecera (seleccion unica)
-- ============================================================================
--  Catalogo de textos (activar/inactivar). En la orden se elige UNO y su texto
--  se COPIA a ordenventa.termino_cabecera (snapshot, SIN tabla de relacion).
--  hbm2ddl = validate: las columnas/tablas deben existir.
-- ============================================================================


-- ----------------------------------------------------------------------------
-- 1) Catalogo de terminos de cabecera
-- ----------------------------------------------------------------------------
CREATE TABLE ordenventa_termino (
    idordenventa_termino  BIGINT         NOT NULL,
    texto                 VARCHAR(1000)  NOT NULL,
    orden                 INT            NULL DEFAULT 0,
    activo                INT            NOT NULL DEFAULT 1,
    idcompania            BIGINT         NOT NULL,
    version               BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (idordenventa_termino)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------------------------------------------------------
-- 2) Campo (snapshot) en la orden: el texto elegido se copia aqui (sin relacion)
-- ----------------------------------------------------------------------------
ALTER TABLE ordenventa
    ADD COLUMN termino_cabecera VARCHAR(1000) NULL AFTER condiciones_pago;


-- ----------------------------------------------------------------------------
-- 3) Semilla: el texto legal que estaba fijo en el reporte queda como 1 termino
--    (compania 1). Se puede editar/inactivar desde el ABM.
-- ----------------------------------------------------------------------------
INSERT INTO ordenventa_termino (idordenventa_termino, texto, orden, activo, idcompania, version) VALUES
    (1, '48 HOURS AFTER RECEIVED THIS ORDER IT WILL BE CONSIDERED ACCEPTED, THE SUPPLIER MUST FOLLOW THE COMPANY BUYING TERMS.', 1, 1, 1, 0);


-- ----------------------------------------------------------------------------
-- 4) Semilla de secuencia (proximo id = 2, ya que se sembro el id 1)
-- ----------------------------------------------------------------------------
INSERT INTO secuencia (tabla, valor)
    SELECT 'ordenventa_termino', 2 FROM dual
    WHERE NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'ordenventa_termino');


-- ----------------------------------------------------------------------------
-- 5) Permiso (funcionalidad). permiso = 15 (VIEW+CREATE+UPDATE+DELETE).
--    idmodulo = 1 (customers).
-- ----------------------------------------------------------------------------
insert into funcionalidad values (491, 'SALESORDERTERM', 'Catalogo de Terminos de cabecera (Orden de Venta)', 1, 15, 'menu.customers.configuration.salesOrderTerm', 1);
