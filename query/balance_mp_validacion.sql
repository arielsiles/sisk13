-- ============================================================================
-- VALIDACION de saldos de Almacen de Materias Primas (cod_alm = 4)
--   Recalcula el saldo (cantidad) de cada articulo DESDE EL ORIGEN, replicando
--   exactamente XProductionBalanceServiceBean.computeBalances() (vista
--   Produccion > Saldos), y lo compara contra lo guardado en:
--       * inv_inventario.saldo_uni            (saldo consolidado por almacen)
--       * inv_inventario_detalle.cantidad     (sumado por centro de costo)
--   Tambien muestra saldo_mon y costo_uni actuales de inv_articulos (referencia).
--
--   Incluye al inicio UNA correccion de dato puntual (Paso 0): el acopio
--   MP-ULEX-100-24 tiene pesoprov=289070 (un digito de mas; error de carga). Se
--   corrige a 28970 (= pesobal = pesoneto), que es lo que valuo su asiento de 2024.
--   Es idempotente (solo actua si el dato sigue mal). El resto es SOLO LECTURA.
--
-- FUENTES DEL SALDO (estado <> 'ANL' en todas; igual que la vista):
--   2) Kardex   : inv_movdet  (tipo_mov 'E' suma, 'S' resta) filtrado por cod_alm
--   3) Acopio   : acopiomp.pesoneto (entrada) via metaproductoproduccion.cod_art
--   4) PT prod. : xpr_producto.cantidad (entrada)            [orden <> ANL]
--   5) Consumo  : xpr_insumo.cantidad   (salida)             [orden <> ANL]
--   6) Reproceso: xpr_produccion_ulexita (reproceso_final_tn entrada,
--                 consumo_reproceso_tn salida) sobre cod_art_reproc_final;
--                 TN -> unidad del articulo (KG = x1000).
--
-- SUPUESTOS / NOTAS:
--   * Universo: articulos VIG del almacen 4 (inv_articulos.cod_alm='4', estado='VIG').
--   * Las fuentes 3/4/5/6 no traen almacen: se atribuyen por cod_art al articulo
--     (igual que la vista). El kardex (2) si filtra cod_alm='4'.
--   * Compania: inv_* e inv_movdet se cruzan por no_cia del articulo; acopio y
--     xpr_* se cruzan solo por cod_art (asumiendo una sola compania, como en dev).
--   * inv_inventario_detalle puede tener varias filas por articulo (por centro de
--     costo): se suma cantidad.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- PASO 0: correccion de dato (idempotente). El acopio MP-ULEX-100-24 tiene
--   pesoprov=289070 (debe ser 28970). El asiento contable ya estaba bien (uso el
--   neto); esto alinea el peso de origen con el resto y con la contabilidad.
-- ----------------------------------------------------------------------------
UPDATE acopiomp
SET pesoprov = 28970.00
WHERE codigo = 'MP-ULEX-100-24' AND pesoprov = 289070.00;

SET @alm := '4';

SELECT
    p.no_cia,
    p.cod_art,
    p.descri                                   AS articulo,
    p.cod_med                                  AS unidad,

    -- ---- Saldo recalculado desde el origen (replica de la vista) ----
    ROUND(
        COALESCE((SELECT SUM(CASE m.tipo_mov WHEN 'E' THEN m.cantidad
                                             WHEN 'S' THEN -m.cantidad ELSE 0 END)
                  FROM inv_movdet m
                  WHERE m.no_cia = p.no_cia AND m.cod_alm = p.cod_alm
                    AND m.cod_art = p.cod_art AND m.estado <> 'ANL'), 0)
      + COALESCE((SELECT SUM(a.pesoneto)
                  FROM acopiomp a
                  JOIN metaproductoproduccion mp ON mp.idmetaproductoproduccion = a.idmetaproductoproduccion
                  WHERE mp.cod_art = p.cod_art AND a.estado <> 'ANL'), 0)
      + COALESCE((SELECT SUM(xpp.cantidad)
                  FROM xpr_producto xpp
                  JOIN xpr_produccion prp ON prp.idproduccion = xpp.idproduccion
                  WHERE xpp.cod_art = p.cod_art AND prp.estado <> 'ANL'), 0)
      - COALESCE((SELECT SUM(xi.cantidad)
                  FROM xpr_insumo xi
                  JOIN xpr_produccion pri ON pri.idproduccion = xi.idproduccion
                  WHERE xi.cod_art = p.cod_art AND pri.estado <> 'ANL'), 0)
      + COALESCE((SELECT SUM(
                       (CASE WHEN UPPER(TRIM(p.cod_med)) = 'KG' THEN u.reproceso_final_tn  * 1000 ELSE u.reproceso_final_tn  END)
                     - (CASE WHEN UPPER(TRIM(p.cod_med)) = 'KG' THEN u.consumo_reproceso_tn * 1000 ELSE u.consumo_reproceso_tn END))
                  FROM xpr_produccion_ulexita u
                  JOIN xpr_produccion pru ON pru.idproduccion = u.idproduccion
                  WHERE u.cod_art_reproc_final = p.cod_art AND pru.estado <> 'ANL'), 0)
    , 2)                                        AS saldo_origen,

    -- ---- Saldos guardados ----
    inv.saldo_uni                              AS saldo_inv_inventario,
    COALESCE(det.cant_det, 0)                  AS saldo_inv_detalle,

    -- ---- Diferencias (origen - guardado) ----
    ROUND(
        ( COALESCE((SELECT SUM(CASE m.tipo_mov WHEN 'E' THEN m.cantidad WHEN 'S' THEN -m.cantidad ELSE 0 END)
                    FROM inv_movdet m WHERE m.no_cia=p.no_cia AND m.cod_alm=p.cod_alm AND m.cod_art=p.cod_art AND m.estado<>'ANL'),0)
        + COALESCE((SELECT SUM(a.pesoneto) FROM acopiomp a JOIN metaproductoproduccion mp ON mp.idmetaproductoproduccion=a.idmetaproductoproduccion WHERE mp.cod_art=p.cod_art AND a.estado<>'ANL'),0)
        + COALESCE((SELECT SUM(xpp.cantidad) FROM xpr_producto xpp JOIN xpr_produccion prp ON prp.idproduccion=xpp.idproduccion WHERE xpp.cod_art=p.cod_art AND prp.estado<>'ANL'),0)
        - COALESCE((SELECT SUM(xi.cantidad) FROM xpr_insumo xi JOIN xpr_produccion pri ON pri.idproduccion=xi.idproduccion WHERE xi.cod_art=p.cod_art AND pri.estado<>'ANL'),0)
        + COALESCE((SELECT SUM((CASE WHEN UPPER(TRIM(p.cod_med))='KG' THEN u.reproceso_final_tn*1000 ELSE u.reproceso_final_tn END) - (CASE WHEN UPPER(TRIM(p.cod_med))='KG' THEN u.consumo_reproceso_tn*1000 ELSE u.consumo_reproceso_tn END)) FROM xpr_produccion_ulexita u JOIN xpr_produccion pru ON pru.idproduccion=u.idproduccion WHERE u.cod_art_reproc_final=p.cod_art AND pru.estado<>'ANL'),0)
        ) - COALESCE(inv.saldo_uni, 0)
    , 2)                                        AS dif_vs_inventario,

    -- ---- Valor monetario actual (referencia, NO recalculado aqui) ----
    p.saldo_mon                                AS saldo_mon_actual,
    p.costo_uni                                AS costo_uni_actual,
    ROUND(p.saldo_mon / NULLIF(inv.saldo_uni, 0), 6) AS costo_uni_implicito,  -- saldo_mon / saldo_uni (guardado)

    -- ---- Costo unitario CORRECTO desde el origen (promedio de entradas netas) ----
    --   = (valor neto acopio + monto vales 'E') / (pesoneto acopio + cantidad vales 'E')
    --   No es NULL aunque saldo_mon/saldo_uni guardados esten en 0.
    ROUND(
        ( COALESCE((SELECT SUM(((a.pesoprov + a.pesobal) / 2 / 1000) * a.precio
                                * (CASE WHEN a.tienefac = 1 THEN 0.87 ELSE 1 END))
                    FROM acopiomp a JOIN metaproductoproduccion mp ON mp.idmetaproductoproduccion = a.idmetaproductoproduccion
                    WHERE mp.cod_art = p.cod_art AND a.estado <> 'ANL'), 0)
        + COALESCE((SELECT SUM(m.monto) FROM inv_movdet m
                    WHERE m.no_cia = p.no_cia AND m.cod_alm = p.cod_alm AND m.cod_art = p.cod_art
                      AND m.estado <> 'ANL' AND m.tipo_mov = 'E'), 0) )
        / NULLIF(
        ( COALESCE((SELECT SUM(a.pesoneto) FROM acopiomp a JOIN metaproductoproduccion mp ON mp.idmetaproductoproduccion = a.idmetaproductoproduccion
                    WHERE mp.cod_art = p.cod_art AND a.estado <> 'ANL'), 0)
        + COALESCE((SELECT SUM(m.cantidad) FROM inv_movdet m
                    WHERE m.no_cia = p.no_cia AND m.cod_alm = p.cod_alm AND m.cod_art = p.cod_art
                      AND m.estado <> 'ANL' AND m.tipo_mov = 'E'), 0) ), 0)
    , 6)                                        AS costo_uni_origen

FROM inv_articulos p
LEFT JOIN inv_inventario inv
       ON inv.no_cia = p.no_cia AND inv.cod_alm = p.cod_alm AND inv.cod_art = p.cod_art
LEFT JOIN ( SELECT d.no_cia, d.cod_alm, d.cod_art, SUM(d.cantidad) AS cant_det
            FROM inv_inventario_detalle d
            GROUP BY d.no_cia, d.cod_alm, d.cod_art ) det
       ON det.no_cia = p.no_cia AND det.cod_alm = p.cod_alm AND det.cod_art = p.cod_art
WHERE p.cod_alm = @alm
  AND p.estado = 'VIG'
ORDER BY ABS(
        ( COALESCE((SELECT SUM(CASE m.tipo_mov WHEN 'E' THEN m.cantidad WHEN 'S' THEN -m.cantidad ELSE 0 END)
                    FROM inv_movdet m WHERE m.no_cia=p.no_cia AND m.cod_alm=p.cod_alm AND m.cod_art=p.cod_art AND m.estado<>'ANL'),0)
        + COALESCE((SELECT SUM(a.pesoneto) FROM acopiomp a JOIN metaproductoproduccion mp ON mp.idmetaproductoproduccion=a.idmetaproductoproduccion WHERE mp.cod_art=p.cod_art AND a.estado<>'ANL'),0)
        + COALESCE((SELECT SUM(xpp.cantidad) FROM xpr_producto xpp JOIN xpr_produccion prp ON prp.idproduccion=xpp.idproduccion WHERE xpp.cod_art=p.cod_art AND prp.estado<>'ANL'),0)
        - COALESCE((SELECT SUM(xi.cantidad) FROM xpr_insumo xi JOIN xpr_produccion pri ON pri.idproduccion=xi.idproduccion WHERE xi.cod_art=p.cod_art AND pri.estado<>'ANL'),0)
        + COALESCE((SELECT SUM((CASE WHEN UPPER(TRIM(p.cod_med))='KG' THEN u.reproceso_final_tn*1000 ELSE u.reproceso_final_tn END) - (CASE WHEN UPPER(TRIM(p.cod_med))='KG' THEN u.consumo_reproceso_tn*1000 ELSE u.consumo_reproceso_tn END)) FROM xpr_produccion_ulexita u JOIN xpr_produccion pru ON pru.idproduccion=u.idproduccion WHERE u.cod_art_reproc_final=p.cod_art AND pru.estado<>'ANL'),0)
        ) - COALESCE(inv.saldo_uni, 0)
    ) DESC, p.cod_art;
