-- ============================================================
-- Reproducción del SALDO del "Reporte General de Inventario"
-- Almacen=2 (MATERIALES E INSUMOS), 2026-01-01..2026-05-02
-- vs inv_inventario.saldo_uni (cod_alm=2)
-- ============================================================
SET @cod_alm := '2';
SET @sd      := '2026-01-01';
SET @ed      := '2026-05-02';
SET @gest    := '2026';

DROP TEMPORARY TABLE IF EXISTS tmp_rep_saldo;

CREATE TEMPORARY TABLE tmp_rep_saldo AS
SELECT
    a.cod_art,
    a.descri AS nombre,
    -- 1) Saldo inicial gestion (inv_inicio)
    COALESCE(ii.cantidad, 0)                                       AS inicial_inv_inicio,
    -- 2) Entradas en periodo via inv_movdet (vales APR)
    COALESCE(ent.tot, 0)                                           AS entradas_movdet,
    -- 3) Salidas en periodo via inv_movdet (vales APR)
    COALESCE(sal.tot, 0)                                           AS salidas_movdet,
    -- SALDO_REP = inicial + entradas - salidas
    (COALESCE(ii.cantidad,0) + COALESCE(ent.tot,0) - COALESCE(sal.tot,0)) AS saldo_reporte,
    -- 4) saldo_uni de inv_inventario
    COALESCE(inv.saldo_uni, 0)                                     AS saldo_inventario
FROM inv_articulos a
LEFT JOIN inv_inicio ii
       ON ii.cod_art = a.cod_art
      AND ii.alm     = @cod_alm
      AND ii.gestion = @gest
LEFT JOIN inv_inventario inv
       ON inv.cod_art = a.cod_art
      AND inv.cod_alm = @cod_alm
LEFT JOIN (
    SELECT md.cod_art, SUM(md.cantidad) AS tot
    FROM inv_movdet md
    JOIN inv_vales  v ON v.no_trans = md.no_trans AND v.no_cia = md.no_cia
    WHERE md.cod_alm = @cod_alm
      AND md.tipo_mov = 'E'
      AND v.estado    = 'APR'
      AND v.fecha BETWEEN @sd AND @ed
    GROUP BY md.cod_art
) ent ON ent.cod_art = a.cod_art
LEFT JOIN (
    SELECT md.cod_art, SUM(md.cantidad) AS tot
    FROM inv_movdet md
    JOIN inv_vales  v ON v.no_trans = md.no_trans AND v.no_cia = md.no_cia
    WHERE md.cod_alm = @cod_alm
      AND md.tipo_mov = 'S'
      AND v.estado    = 'APR'
      AND v.fecha BETWEEN @sd AND @ed
    GROUP BY md.cod_art
) sal ON sal.cod_art = a.cod_art
WHERE a.cod_alm = @cod_alm;

-- 1) Resumen general
SELECT
    COUNT(*)                                          AS total_articulos,
    SUM(saldo_reporte <> saldo_inventario)            AS articulos_con_diferencia,
    ROUND(SUM(saldo_reporte - saldo_inventario), 2)   AS diferencia_neta_total
FROM tmp_rep_saldo;

-- 2) Top 50 articulos con diferencia (ordenado por |diff| desc)
SELECT
    cod_art,
    LEFT(nombre, 50)                          AS articulo,
    inicial_inv_inicio                        AS ini,
    entradas_movdet                           AS ent,
    salidas_movdet                            AS sal,
    saldo_reporte                             AS s_rep,
    saldo_inventario                          AS s_inv,
    ROUND(saldo_reporte - saldo_inventario,2) AS diff
FROM tmp_rep_saldo
WHERE ROUND(saldo_reporte - saldo_inventario, 2) <> 0
ORDER BY ABS(saldo_reporte - saldo_inventario) DESC
LIMIT 50;
