-- ============================================================================
-- v6.0.79 :: Vale de Egreso por Despacho de Productos Terminados
--             Fase 5 - Semilla del WarehouseDocumentType (cod_doc='DSP')
-- ============================================================================
--
-- Crea un tipo de documento de inventario "DSP" (Despacho de Productos
-- Terminados) que se usara al generar el WarehouseVoucher de egreso al
-- aprobar un Vale de Despacho.
--
-- Reusa el enum WarehouseVoucherType.S (output) para evitar tener que
-- agregar un nuevo valor al enum y todas las ramas que dependan de el.
-- La diferenciacion en listados/reportes se hace por cod_doc='DSP'.
--
-- Si se necesita registrar otra compania, replicar este INSERT cambiando
-- el no_cia correspondiente.
-- ----------------------------------------------------------------------------

INSERT INTO inv_tipodocs
  (no_cia, cod_doc, descri, tipo_vale, estado, desc_def, version)
VALUES
  ('01', 'DSP', 'Despacho de Productos Terminados', 'S', 'VIG',
   'Despacho de productos terminados a cliente', 0);
