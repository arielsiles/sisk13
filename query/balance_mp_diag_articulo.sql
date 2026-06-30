-- ============================================================================
-- DIAGNOSTICO de costo unitario de UN articulo del almacen MP (cod_alm=4).
--   Desglosa entradas/salidas por FUENTE para entender por que el costo
--   implicito (saldo_mon/saldo_uni guardado) difiere del costo desde el origen.
--   SOLO LECTURA.
-- ----------------------------------------------------------------------------

-- Fijar el articulo (por descripcion o por cod_art directo):
SET @art := (SELECT cod_art FROM inv_articulos WHERE descri = 'ULEXITA' AND cod_alm = '4' LIMIT 1);
SET @cia := (SELECT no_cia  FROM inv_articulos WHERE cod_art = @art LIMIT 1);
SET @med := (SELECT cod_med FROM inv_articulos WHERE cod_art = @art LIMIT 1);

-- ---------------------------------------------------------------------------
-- A) Desglose por fuente: cantidad, valor (si aplica) y costo implicito.
-- ---------------------------------------------------------------------------
SELECT * FROM (
    -- Acopio (ENTRADA valuada). valor = neto del acopio.
    SELECT 1 AS ord, 'ACOPIO (E)' AS fuente,
           COUNT(*) AS n_movs,
           ROUND(SUM(a.pesoneto), 2) AS cantidad,
           ROUND(SUM(((a.pesoprov + a.pesobal)/2/1000) * a.precio
                     * (CASE WHEN a.tienefac = 1 THEN 0.87 ELSE 1 END)), 2) AS valor
    FROM acopiomp a
    JOIN metaproductoproduccion mp ON mp.idmetaproductoproduccion = a.idmetaproductoproduccion
    WHERE mp.cod_art = @art AND a.estado <> 'ANL'

    UNION ALL
    -- Kardex 'E' (ENTRADA valuada). valor = monto.
    SELECT 2, 'KARDEX E', COUNT(*),
           ROUND(SUM(m.cantidad), 2), ROUND(SUM(m.monto), 2)
    FROM inv_movdet m
    WHERE m.no_cia = @cia AND m.cod_alm = '4' AND m.cod_art = @art
      AND m.estado <> 'ANL' AND m.tipo_mov = 'E'

    UNION ALL
    -- Kardex 'E' SIN valor (monto 0/NULL) -> estas DILUYEN el costo desde el origen.
    SELECT 3, 'KARDEX E sin monto', COUNT(*),
           ROUND(SUM(m.cantidad), 2), ROUND(SUM(COALESCE(m.monto,0)), 2)
    FROM inv_movdet m
    WHERE m.no_cia = @cia AND m.cod_alm = '4' AND m.cod_art = @art
      AND m.estado <> 'ANL' AND m.tipo_mov = 'E'
      AND (m.monto IS NULL OR m.monto = 0)

    UNION ALL
    -- Kardex 'S' (SALIDA).
    SELECT 4, 'KARDEX S', COUNT(*),
           ROUND(SUM(m.cantidad), 2), ROUND(SUM(m.monto), 2)
    FROM inv_movdet m
    WHERE m.no_cia = @cia AND m.cod_alm = '4' AND m.cod_art = @art
      AND m.estado <> 'ANL' AND m.tipo_mov = 'S'

    UNION ALL
    -- Consumo de insumos en produccion (SALIDA).
    SELECT 5, 'CONSUMO PRODUCCION (S)', COUNT(*),
           ROUND(SUM(xi.cantidad), 2), NULL
    FROM xpr_insumo xi
    JOIN xpr_produccion pr ON pr.idproduccion = xi.idproduccion
    WHERE xi.cod_art = @art AND pr.estado <> 'ANL'

    UNION ALL
    -- PT producido que entra a este codigo (ENTRADA sin costo de compra).
    SELECT 6, 'PT PRODUCIDO (E)', COUNT(*),
           ROUND(SUM(xpp.cantidad), 2), NULL
    FROM xpr_producto xpp
    JOIN xpr_produccion pr ON pr.idproduccion = xpp.idproduccion
    WHERE xpp.cod_art = @art AND pr.estado <> 'ANL'

    UNION ALL
    -- Reproceso ULEXITA final (E) y consumo (S), TN -> unidad.
    SELECT 7, 'REPROCESO FINAL (E)', COUNT(*),
           ROUND(SUM(CASE WHEN UPPER(TRIM(@med))='KG' THEN u.reproceso_final_tn*1000 ELSE u.reproceso_final_tn END), 2), NULL
    FROM xpr_produccion_ulexita u
    JOIN xpr_produccion pr ON pr.idproduccion = u.idproduccion
    WHERE u.cod_art_reproc_final = @art AND pr.estado <> 'ANL'

    UNION ALL
    SELECT 8, 'CONSUMO REPROCESO (S)', COUNT(*),
           ROUND(SUM(CASE WHEN UPPER(TRIM(@med))='KG' THEN u.consumo_reproceso_tn*1000 ELSE u.consumo_reproceso_tn END), 2), NULL
    FROM xpr_produccion_ulexita u
    JOIN xpr_produccion pr ON pr.idproduccion = u.idproduccion
    WHERE u.cod_art_reproc_final = @art AND pr.estado <> 'ANL'
) x
ORDER BY ord;

-- Costo implicito de las ENTRADAS valuadas (acopio + kardex E):
--   si KARDEX E aporta mucha cantidad con poco/cero monto, el costo origen baja.

-- ---------------------------------------------------------------------------
-- B) Las 30 entradas 'E' mas grandes en cantidad (para ver cuales no tienen
--    monto, o tienen un costo unitario raro).
-- ---------------------------------------------------------------------------
SELECT m.fecha, m.no_trans, m.tipo_mov, m.cantidad, m.monto,
       ROUND(m.monto / NULLIF(m.cantidad,0), 6) AS costo_mov,
       m.costounitario, m.estado
FROM inv_movdet m
WHERE m.no_cia = @cia AND m.cod_alm = '4' AND m.cod_art = @art
  AND m.estado <> 'ANL' AND m.tipo_mov = 'E'
ORDER BY m.cantidad DESC
LIMIT 30;

-- ---------------------------------------------------------------------------
-- C) Valores guardados (referencia).
-- ---------------------------------------------------------------------------
SELECT p.cod_art, p.descri, p.cod_med,
       inv.saldo_uni                                   AS saldo_guardado,
       p.saldo_mon                                     AS saldo_mon_guardado,
       p.costo_uni                                     AS costo_uni_guardado,
       ROUND(p.saldo_mon / NULLIF(inv.saldo_uni,0), 6) AS costo_implicito
FROM inv_articulos p
LEFT JOIN inv_inventario inv ON inv.no_cia = p.no_cia AND inv.cod_alm = '4' AND inv.cod_art = p.cod_art
WHERE p.cod_art = @art;
