-- ACTUALIZACION DE CTA_NIV3 - PLAN DE CUENTAS CONTABLES
-- ======================================================
-- Fecha: 2025-08-12 11:02:00
-- Archivo origen: plan_ctas/plan_cuentas.xlsx
-- Tabla destino: arcgms
-- Total de cuentas: 861
-- Cambios necesarios: 14

-- ACTUALIZACIONES NECESARIAS:
-- --------------------------------------------------

-- Cuenta: 3210000000 (Nivel 3) | Actual: 3110000000 -> 3210000000
UPDATE arcgms SET cta_niv3 = 3210000000 WHERE cuenta = 3210000000;

-- Cuenta: 3310000000 (Nivel 3) | Actual: 3110000000 -> 3310000000
UPDATE arcgms SET cta_niv3 = 3310000000 WHERE cuenta = 3310000000;

-- Cuenta: 3410000000 (Nivel 3) | Actual: 3110000000 -> 3410000000
UPDATE arcgms SET cta_niv3 = 3410000000 WHERE cuenta = 3410000000;

-- Cuenta: 3510000000 (Nivel 3) | Actual: 3110000000 -> 3510000000
UPDATE arcgms SET cta_niv3 = 3510000000 WHERE cuenta = 3510000000;

-- Cuenta: 3610000000 (Nivel 3) | Actual: 3110000000 -> 3610000000
UPDATE arcgms SET cta_niv3 = 3610000000 WHERE cuenta = 3610000000;

-- Cuenta: 3710000000 (Nivel 3) | Actual: 3110000000 -> 3710000000
UPDATE arcgms SET cta_niv3 = 3710000000 WHERE cuenta = 3710000000;

-- Cuenta: 5210060000 (Nivel 4) | Actual: VACIO -> 5210000000
UPDATE arcgms SET cta_niv3 = 5210000000 WHERE cuenta = 5210060000;

-- Cuenta: 5350150000 (Nivel 4) | Actual: VACIO -> 5350000000
UPDATE arcgms SET cta_niv3 = 5350000000 WHERE cuenta = 5350150000;

-- Cuenta: 5350150100 (Nivel 5) | Actual: VACIO -> 5350000000
UPDATE arcgms SET cta_niv3 = 5350000000 WHERE cuenta = 5350150100;

-- Cuenta: 5350160000 (Nivel 4) | Actual: VACIO -> 5350000000
UPDATE arcgms SET cta_niv3 = 5350000000 WHERE cuenta = 5350160000;

-- Cuenta: 5350160100 (Nivel 5) | Actual: VACIO -> 5350000000
UPDATE arcgms SET cta_niv3 = 5350000000 WHERE cuenta = 5350160100;

-- Cuenta: 5350160200 (Nivel 5) | Actual: VACIO -> 5350000000
UPDATE arcgms SET cta_niv3 = 5350000000 WHERE cuenta = 5350160200;

-- Cuenta: 5350170000 (Nivel 4) | Actual: VACIO -> 5350000000
UPDATE arcgms SET cta_niv3 = 5350000000 WHERE cuenta = 5350170000;

-- Cuenta: 5350170100 (Nivel 5) | Actual: VACIO -> 5350000000
UPDATE arcgms SET cta_niv3 = 5350000000 WHERE cuenta = 5350170100;

-- TOTAL DE ACTUALIZACIONES: 14
