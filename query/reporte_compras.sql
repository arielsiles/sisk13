-- =============================================================================
-- Reportes de compras y asientos contables generados (Abril 2025 - Marzo 2026)
-- Base: terdemol (KHIPUS_SCHEMA = FINANCES_SCHEMA = terdemol)
--
-- Rutas de join (todo confluye en sf_tmpenc = asiento contable):
--   com_encoc.idtmpenc ------------------> sf_tmpenc   (asiento de la OC)
--   inv_vales.idtmpenc ------------------> sf_tmpenc   (ingreso a almacen / IA)
--   documentocompra.idtmpenc ------------> sf_tmpenc   (factura/recibo CD/CE)
--   Detalle de articulos:
--     inv_vales -(no_cia,no_trans,estado)- inv_mov - inv_movdet -(no_cia,cod_art)- inv_articulos
--
-- Tipo de asiento (IA, CD, CE...) = sf_tmpenc.tipo_doc
-- Filtros: excluir anulados (estado <> 'ANL'); compras pendientes incluidas
--          (LEFT JOIN al asiento -> asiento vacio si aun no se genero).
-- Rango fecha: >= '2025-04-01' AND < '2026-04-01'
-- =============================================================================


-- =============================================================================
-- REPORTE 2 -- Lista de compras (sin detalle de articulos)
-- Una fila por compra; los asientos se concatenan en la fila.
-- =============================================================================
SELECT
    e.fecha                                                   AS fecha,
    e.no_orden                                                AS nro_orden,
    e.cod_prov                                                AS cod_proveedor,
    CAST(COALESCE(GROUP_CONCAT(DISTINCT ac.tipo_doc
             ORDER BY ac.tipo_doc SEPARATOR ', '), '') AS CHAR)  AS asientos, -- IA, CD, CE... (CAST evita BLOB en export)
    e.total                                                   AS monto,
    e.numero_factura                                          AS nro_recibo_factura,
    CASE e.tipodoccompra
         WHEN 'INVOICE'    THEN 'FACTURA'
         WHEN 'RECEIPT'    THEN 'RECIBO'
         WHEN 'ADJUSTMENT' THEN 'AJUSTE'
         ELSE e.tipodoccompra
    END                                                       AS con_factura_o_recibo,
    e.confactura                                              AS flag_confactura,
    e.estado                                                  AS estado_oc,
    e.glosa                                                   AS glosa
FROM com_encoc e
LEFT JOIN (
        -- Todos los asientos que genera cada compra (3 origenes)
        SELECT e2.id_com_encoc AS id_com_encoc, v.tipo_doc
          FROM com_encoc e2
          JOIN sf_tmpenc v ON v.id_tmpenc = e2.idtmpenc AND v.estado <> 'ANL'
        UNION
        SELECT iv.id_com_encoc, v.tipo_doc
          FROM inv_vales iv
          JOIN sf_tmpenc v ON v.id_tmpenc = iv.idtmpenc AND v.estado <> 'ANL'
         WHERE iv.id_com_encoc IS NOT NULL
        UNION
        SELECT dc.idordencompra, v.tipo_doc
          FROM documentocompra dc
          JOIN sf_tmpenc v ON v.id_tmpenc = dc.idtmpenc AND v.estado <> 'ANL'
         WHERE dc.idordencompra IS NOT NULL
) ac ON ac.id_com_encoc = e.id_com_encoc
WHERE e.fecha >= '2025-04-01' AND e.fecha < '2026-04-01'
  AND e.estado <> 'ANL'
GROUP BY e.id_com_encoc
ORDER BY e.fecha, e.no_orden;


-- =============================================================================
-- REPORTE 1 -- Compras con detalle de articulos
-- VARIANTE A (recomendada): una fila por articulo; el asiento de la fila es
-- el del ingreso a almacen (normalmente IA). No infla montos.
-- =============================================================================
SELECT
    COALESCE(v.fecha, mov.fecha_mov, e.fecha)                 AS fecha,
    v.tipo_doc                                                AS asiento,        -- IA, etc.
    md.cod_art                                                AS cod_articulo,
    a.descri                                                  AS articulo,
    md.cantidad                                               AS cantidad,
    md.costounitario                                          AS precio,
    md.monto                                                  AS monto,
    e.numero_factura                                          AS nro_recibo_factura,
    CASE e.tipodoccompra
         WHEN 'INVOICE'    THEN 'FACTURA'
         WHEN 'RECEIPT'    THEN 'RECIBO'
         WHEN 'ADJUSTMENT' THEN 'AJUSTE'
         ELSE e.tipodoccompra
    END                                                       AS con_factura_o_recibo,
    e.no_orden                                                AS nro_orden,
    CAST(COALESCE(v.descri, mov.descri, e.glosa) AS CHAR)     AS glosa   -- CAST evita BLOB en export
FROM com_encoc e
JOIN inv_vales  iv  ON iv.id_com_encoc = e.id_com_encoc AND iv.estado <> 'ANL'
JOIN inv_mov    mov ON mov.no_cia = iv.no_cia AND mov.no_trans = iv.no_trans AND mov.estado = iv.estado
JOIN inv_movdet md  ON md.no_cia  = mov.no_cia AND md.no_trans = mov.no_trans AND md.estado = mov.estado
JOIN inv_articulos a ON a.no_cia = md.no_cia AND a.cod_art = md.cod_art
LEFT JOIN sf_tmpenc v ON v.id_tmpenc = iv.idtmpenc AND v.estado <> 'ANL'
WHERE e.fecha >= '2025-04-01' AND e.fecha < '2026-04-01'
  AND e.estado <> 'ANL'
ORDER BY e.fecha, e.no_orden, md.cod_art;


-- =============================================================================
-- REPORTE 1 -- Compras con detalle de articulos
-- VARIANTE B: cada articulo se repite por cada tipo de asiento de la compra
-- (IA, CD, CE...).  ADVERTENCIA: duplica 'monto' por fila -> usar para listar,
-- NO para sumar montos.
-- =============================================================================
SELECT
    ac.fecha                                                  AS fecha,
    ac.tipo_doc                                               AS asiento,        -- IA, CD, CE
    md.cod_art                                                AS cod_articulo,
    a.descri                                                  AS articulo,
    md.cantidad                                               AS cantidad,
    md.costounitario                                          AS precio,
    md.monto                                                  AS monto,
    e.numero_factura                                          AS nro_recibo_factura,
    CASE e.tipodoccompra
         WHEN 'INVOICE'    THEN 'FACTURA'
         WHEN 'RECEIPT'    THEN 'RECIBO'
         WHEN 'ADJUSTMENT' THEN 'AJUSTE'
         ELSE e.tipodoccompra
    END                                                       AS con_factura_o_recibo,
    e.no_orden                                                AS nro_orden,
    CAST(COALESCE(ac.glosa, e.glosa) AS CHAR)                 AS glosa   -- CAST evita BLOB en export
FROM com_encoc e
JOIN inv_vales  iv  ON iv.id_com_encoc = e.id_com_encoc AND iv.estado <> 'ANL'
JOIN inv_mov    mov ON mov.no_cia = iv.no_cia AND mov.no_trans = iv.no_trans AND mov.estado = iv.estado
JOIN inv_movdet md  ON md.no_cia  = mov.no_cia AND md.no_trans = mov.no_trans AND md.estado = mov.estado
JOIN inv_articulos a ON a.no_cia = md.no_cia AND a.cod_art = md.cod_art
JOIN (
        SELECT e2.id_com_encoc AS id_com_encoc, v.tipo_doc, v.fecha, v.descri AS glosa
          FROM com_encoc e2 JOIN sf_tmpenc v ON v.id_tmpenc = e2.idtmpenc AND v.estado <> 'ANL'
        UNION
        SELECT iv2.id_com_encoc, v.tipo_doc, v.fecha, v.descri
          FROM inv_vales iv2 JOIN sf_tmpenc v ON v.id_tmpenc = iv2.idtmpenc AND v.estado <> 'ANL'
         WHERE iv2.id_com_encoc IS NOT NULL
        UNION
        SELECT dc.idordencompra, v.tipo_doc, v.fecha, v.descri
          FROM documentocompra dc JOIN sf_tmpenc v ON v.id_tmpenc = dc.idtmpenc AND v.estado <> 'ANL'
         WHERE dc.idordencompra IS NOT NULL
) ac ON ac.id_com_encoc = e.id_com_encoc
WHERE e.fecha >= '2025-04-01' AND e.fecha < '2026-04-01'
  AND e.estado <> 'ANL'
ORDER BY e.fecha, e.no_orden, md.cod_art, ac.tipo_doc;
