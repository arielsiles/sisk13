-- ============================================================================
-- DIAGNOSTICO (SOLO LECTURA) de la diferencia entre:
--   * Kardex de Articulos  -> lee la CABECERA del vale (inv_vales.estado / .fecha)
--   * Saldos de Almacen     -> lee el DETALLE        (inv_movdet.estado / .fecha / .cod_alm)
--
-- Objetivo: ver, articulo por movimiento, si cada fila de inv_movdet la cuenta
-- Kardex, la cuenta Saldos, o solo una de las dos (ahi esta la diferencia).
--
-- Motor: MySQL. NO modifica datos (puros SELECT).
-- Cambiar @art para revisar otro producto (2021 BARITINA, 1255 BORO A, 1261 BORO B).
-- ============================================================================

SET @cn     := '01';            -- compania
SET @alm    := '3';             -- almacen Producto Terminado (filtro de Saldos)
SET @art    := '2021';          -- articulo a diagnosticar (BARITINA MOLIDA)
SET @cutoff := '2026-05-31';    -- fecha de corte de la pantalla Saldos

-- ----------------------------------------------------------------------------
-- 1) Detalle fila por fila. Une inv_movdet -> inv_mov (por no_cia,no_trans,ESTADO,
--    igual que el mapeo JPA) -> inv_vales (por no_cia,no_trans).
--    LEFT JOIN para no perder filas cuyo estado de detalle NO empareje con inv_mov.
--    Columnas clave:
--      est_det / fecha_det / alm_det  -> lo que mira SALDOS
--      est_vale / fecha_vale          -> lo que mira KARDEX
--    Flags:
--      en_saldos = 1 si SALDOS la cuenta (alm=@alm, est_det='APR', fecha_det<=corte)
--      en_kardex = 1 si KARDEX la cuenta (est_vale='APR', fecha_vale<=corte)
-- ----------------------------------------------------------------------------
SELECT
    d.id_inv_movdet,
    d.no_trans,
    v.no_vale,
    v.cod_doc,
    d.tipo_mov,
    d.cantidad,
    d.cod_alm  AS alm_det,
    d.estado   AS est_det,
    d.fecha    AS fecha_det,
    m.estado   AS est_mov,
    v.estado   AS est_vale,
    v.fecha    AS fecha_vale,
    IF(d.cod_alm = @alm AND d.estado = 'APR' AND d.fecha <= @cutoff, 1, 0) AS en_saldos,
    IF(v.estado = 'APR' AND v.fecha <= @cutoff, 1, 0)                       AS en_kardex,
    LEFT(m.descri, 40) AS descri
FROM inv_movdet d
LEFT JOIN inv_mov   m ON m.no_cia = d.no_cia AND m.no_trans = d.no_trans AND m.estado = d.estado
LEFT JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
WHERE d.no_cia = @cn AND d.cod_art = @art
ORDER BY v.fecha, d.id_inv_movdet;

-- ----------------------------------------------------------------------------
-- 2) Resumen: saldo segun cada criterio (E suma, S resta). La diferencia entre
--    ambos totales debe explicar el desfase Kardex vs Saldos.
-- ----------------------------------------------------------------------------
SELECT
    -- saldo con el criterio de SALDOS (detalle)
    SUM(CASE WHEN d.cod_alm = @alm AND d.estado = 'APR' AND d.fecha <= @cutoff
             THEN IF(d.tipo_mov = 'E', d.cantidad, -d.cantidad) ELSE 0 END) AS saldo_criterio_saldos,
    -- saldo con el criterio de KARDEX (cabecera) -- solo la parte inv_movdet
    SUM(CASE WHEN v.estado = 'APR' AND v.fecha <= @cutoff
             THEN IF(d.tipo_mov = 'E', d.cantidad, -d.cantidad) ELSE 0 END) AS saldo_criterio_kardex_movdet
FROM inv_movdet d
LEFT JOIN inv_mov   m ON m.no_cia = d.no_cia AND m.no_trans = d.no_trans AND m.estado = d.estado
LEFT JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
WHERE d.no_cia = @cn AND d.cod_art = @art;

-- ----------------------------------------------------------------------------
-- 3) Solo las filas DISCREPANTES (una pantalla la cuenta y la otra no): son las
--    que explican la diferencia. Mirar por que difieren est_det vs est_vale,
--    fecha_det vs fecha_vale, o alm_det <> '3'.
-- ----------------------------------------------------------------------------
SELECT
    d.id_inv_movdet, d.no_trans, v.no_vale, v.cod_doc, d.tipo_mov, d.cantidad,
    d.cod_alm AS alm_det, d.estado AS est_det, d.fecha AS fecha_det,
    v.estado AS est_vale, v.fecha AS fecha_vale, LEFT(m.descri, 40) AS descri
FROM inv_movdet d
LEFT JOIN inv_mov   m ON m.no_cia = d.no_cia AND m.no_trans = d.no_trans AND m.estado = d.estado
LEFT JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
WHERE d.no_cia = @cn AND d.cod_art = @art
  AND IF(d.cod_alm = @alm AND d.estado = 'APR' AND d.fecha <= @cutoff, 1, 0)
   <> IF(v.estado = 'APR' AND v.fecha <= @cutoff, 1, 0)
ORDER BY v.fecha, d.id_inv_movdet;
-- ============================================================================
