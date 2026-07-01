-- ============================================================================
-- v6.0.94 :: Formulacion - columna 'sigla' (producto corto para el calendario)
-- ============================================================================
--   Agrega xpr_formula.sigla (VARCHAR 10): sigla corta del producto de la
--   formulacion (ULEX, BAR, RFOS, RUMI...) que se muestra en el calendario del
--   Plan de Produccion. Se configura en el catalogo de Formulaciones.
--   Aditivo y no destructivo. El ADD es idempotente (solo si no existe).
-- ----------------------------------------------------------------------------

SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'xpr_formula'
                      AND COLUMN_NAME = 'sigla');
SET @sql := IF(@col_exists = 0,
               'ALTER TABLE xpr_formula ADD COLUMN sigla VARCHAR(10) NULL AFTER nombre',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------------------
-- Siglas iniciales (solo donde este vacio; no pisa lo que configures a mano).
-- Ajustar/agregar segun tus formulaciones reales.
-- ----------------------------------------------------------------------------
UPDATE xpr_formula SET sigla = 'ULEX' WHERE (sigla IS NULL OR sigla = '') AND nombre LIKE 'FORM. ULEXITA%';
UPDATE xpr_formula SET sigla = 'BAR'  WHERE (sigla IS NULL OR sigla = '') AND nombre LIKE 'FORM. BARITINA%';
UPDATE xpr_formula SET sigla = 'RUMI' WHERE (sigla IS NULL OR sigla = '') AND nombre LIKE 'FORM. RUMIFOS%';
UPDATE xpr_formula SET sigla = 'RFOS' WHERE (sigla IS NULL OR sigla = '') AND nombre LIKE 'FORM. RFOS%';

-- Verificacion:
-- SELECT idformula, nombre, estado, sigla FROM xpr_formula ORDER BY nombre;
-- ============================================================================
