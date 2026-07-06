-- ============================================================================
-- v6.0.105  Orden de Venta: campo Condiciones de pago
-- ============================================================================
--  Presente en el formato oficial del documento (recuadro CONDICIONES DE PAGO).
--  hbm2ddl = validate: la columna debe existir.
-- ============================================================================

ALTER TABLE ordenventa
    ADD COLUMN condiciones_pago VARCHAR(1000) NULL AFTER cc_codigo;
