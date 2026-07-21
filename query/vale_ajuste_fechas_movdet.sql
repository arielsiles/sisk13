-- ============================================================================
-- CORRECCION GENERAL: alinear inv_movdet.fecha (detalle) con inv_vales.fecha (cabecera)
-- ----------------------------------------------------------------------------
-- PROBLEMA: en algunos movimientos la fecha del DETALLE (inv_movdet.fecha, que a
-- veces queda estampada en la aprobacion/cierre) NO coincide con la fecha del VALE
-- (inv_vales.fecha, la fecha real). Como los dos reportes leen columnas distintas:
--     * Kardex de Articulos  -> inv_vales.fecha  (cabecera)
--     * Saldos de Almacen     -> inv_movdet.fecha (detalle)
-- ese desfase hace que ambos NO cuadren en cortes entre las dos fechas.
--
-- SOLUCION: alinear inv_movdet.fecha = inv_vales.fecha en TODOS los movimientos
-- donde la FECHA (dia) difiera. General (cualquier tipo de documento y almacen),
-- sin filtro de periodo ni articulo. Idempotente: solo toca filas desalineadas;
-- re-ejecutar cuando ya estan alineadas no cambia nada.
--
-- Alcance detectado: los despachos (cod_doc='DSP') ya se corrigieron aparte
-- (query/vale_ajuste_pt_20260331_crear.sql); aqui ademas quedan los egresos
-- (cod_doc='EGR'). Este script cubre TODOS los tipos de una sola vez.
--
-- Motor: MySQL. Ejecutar en el schema de PRODUCCION (terdemol).
-- Script STANDALONE (no versionado). NO altera cantidades ni estados, solo fechas.
-- ============================================================================

START TRANSACTION;

-- ----------------------------------------------------------------------------
-- 1) DIAGNOSTICO: que es lo que NO cuadra (detalle vs vale). Incluye no_vale y cod_alm.
-- ----------------------------------------------------------------------------
SELECT
    v.cod_doc,
    v.no_vale,
    d.cod_alm,
    d.cod_art,
    d.tipo_mov,
    d.estado,
    d.cantidad,
    DATE(d.fecha) AS fecha_detalle_SALDOS,
    DATE(v.fecha) AS fecha_vale_KARDEX,
    DATEDIFF(DATE(d.fecha), DATE(v.fecha)) AS dias_desfase
FROM inv_movdet d
JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
WHERE DATE(d.fecha) <> DATE(v.fecha)
ORDER BY v.cod_doc, v.fecha, v.no_vale;

-- 1b) Resumen por tipo de documento y estado (cuantas filas se corregiran).
SELECT v.cod_doc, d.estado, COUNT(*) AS filas
FROM inv_movdet d
JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
WHERE DATE(d.fecha) <> DATE(v.fecha)
GROUP BY v.cod_doc, d.estado
ORDER BY v.cod_doc, d.estado;

-- ----------------------------------------------------------------------------
-- 2) CORRECCION: alinear la fecha del detalle a la del vale (general).
-- ----------------------------------------------------------------------------
UPDATE inv_movdet d
JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
SET d.fecha = v.fecha
WHERE DATE(d.fecha) <> DATE(v.fecha);

-- ----------------------------------------------------------------------------
-- 3) VERIFICACION: debe quedar 0 (ninguna fila con fecha de detalle desalineada).
-- ----------------------------------------------------------------------------
SELECT COUNT(*) AS pendientes_despues
FROM inv_movdet d
JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
WHERE DATE(d.fecha) <> DATE(v.fecha);

-- Revisar el SELECT (1) y que 'pendientes_despues' = 0. Si todo esta correcto:
COMMIT;
-- Si algo esta mal:  ROLLBACK;
-- ============================================================================
