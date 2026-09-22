-- ============================================================================
-- query_v6.1.1_terdemol.sql
-- ============================================================================
-- Cambios posteriores al despliegue de la 6.1.0. Ver docs/employees/spec/.
--
-- APLICAR ANTES DE DESPLEGAR: persistence usa hbm2ddl.auto=validate.
-- ----------------------------------------------------------------------------


-- 1) Atrasos: base del descuento y politica por evento (plan 14).
-- basedescuentoatraso: NULL o TOTAL_INCOME = como hasta ahora; BASIC_SALARY = sobre el sueldo.
-- atrasoporeventodesde: NULL = politica de siempre para todos; con fecha, las areas marcadas
-- cobran cada atraso por separado desde ese periodo.
ALTER TABLE configuracion
  ADD COLUMN basedescuentoatraso  VARCHAR(20) NULL,
  ADD COLUMN atrasoporeventodesde DATE        NULL;

-- 1.1) El area que cobra por evento. En terdemol, PRODUCCION.
ALTER TABLE unidadorganizacional
  ADD COLUMN atrasoporevento TINYINT(1) NULL;

-- 1.2) Cuantos atrasos del mes ameritan memorandum. Se reporta, no se emite.
ALTER TABLE planillaadministrativos
  ADD COLUMN memorandumsatraso INT NULL;
