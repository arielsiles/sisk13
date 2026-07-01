-- ============================================================================
-- LIMPIEZA DE ORDENES DE PRODUCCION VACIAS  (XProduction)
-- ============================================================================
--
--  Objetivo:
--    Eliminar ordenes de produccion (xpr_produccion) que quedaron VACIAS:
--      - sin materia prima consumida (todos los insumos en cantidad 0), y
--      - sin productos terminados,
--    y luego eliminar el Plan de Produccion (xpr_plan) que quede SIN ordenes.
--
--    Ejemplo tipico: ordenes "SIN PRODUCCION. Motivo: Bloqueo de caminos." con
--    el insumo de MP por defecto en 0.00 y la grilla de Productos Terminados vacia.
--
--  >>> HACER RESPALDO ANTES. Es destructivo e irreversible. <<<
--  >>> Ejecutar PRIMERO la PARTE 1 (PREVIEW, solo lectura) y revisar la lista. <<<
--  >>> Probar en LOCAL antes que en PRODUCCION. <<<
--
--  GARANTIA DE SEGURIDAD (no se borran ordenes con datos reales). Una orden solo
--  es candidata si cumple TODAS estas condiciones:
--    1. NO esta contabilizada  -> id_tmpenc IS NULL  (sin comprobante/asiento).
--    2. NO tiene productos terminados -> no hay filas en xpr_producto ligadas a
--       la orden (xpr_producto.idproduccion = orden).
--    3. NO consumio materia prima -> ningun insumo con cantidad > 0
--       (xpr_insumo.cantidad). Esto cubre "MP por defecto en cero" y ademas
--       exige que NINGUN insumo tenga cantidad, evitando borrar ordenes que si
--       consumieron algo. Es intencionalmente conservador: ante la duda, NO borra.
--
--  Tablas involucradas (hijas de una orden xpr_produccion):
--    xpr_insumo                    - insumos y materiales (FK idproduccion)
--    xpr_manoobra                  - mano de obra          (FK idproduccion)
--    xpr_produccion_ulexita        - satelite ULEXITA 1-1  (FK idproduccion)
--    xpr_produccion_baritina       - satelite BARITINA 1-1 (FK idproduccion)
--    xpr_produccion_baritina_zona  - zonas BARITINA N-1    (FK idproduccion)
--    xpr_producto                  - productos terminados  (FK idproduccion)
--                                    y productos PROGRAMADOS del plan (FK idplan)
--    xpr_produccion                - la orden               (FK idplan -> xpr_plan)
--    xpr_plan                      - el plan de produccion
--
--  Nota de inventario: al agregar productos programados el sistema ajusta
--  saldos (ProductItem / inv). Borrar por SQL NO revierte esos ajustes. En estas
--  ordenes vacias (MP=0, sin PT) el impacto es 0; si dudas, revisa saldos aparte.
-- ============================================================================


-- ############################################################################
-- PARTE 1 -- PREVIEW (SOLO LECTURA, no modifica nada). Ejecutar y revisar.
-- ############################################################################

-- 1.A) Ordenes que se ELIMINARIAN (con su plan, fecha, estado y observacion).
SELECT  p.idproduccion,
        p.codigo,
        p.estado,
        p.idplan,
        pl.fecha                                            AS fecha_plan,
        p.costototal,
        (SELECT COUNT(*) FROM xpr_insumo   s  WHERE s.idproduccion  = p.idproduccion) AS num_insumos,
        (SELECT COALESCE(SUM(s.cantidad),0) FROM xpr_insumo s WHERE s.idproduccion = p.idproduccion) AS suma_cant_insumos,
        (SELECT COUNT(*) FROM xpr_producto pt WHERE pt.idproduccion = p.idproduccion) AS num_prod_terminados,
        p.observacion
FROM    xpr_produccion p
JOIN    xpr_plan pl ON pl.idplan = p.idplan
WHERE   p.id_tmpenc IS NULL
  AND   NOT EXISTS (SELECT 1 FROM xpr_producto pt WHERE pt.idproduccion = p.idproduccion)
  AND   NOT EXISTS (SELECT 1 FROM xpr_insumo   s  WHERE s.idproduccion  = p.idproduccion
                                                    AND COALESCE(s.cantidad,0) > 0)
ORDER BY pl.fecha, p.codigo;

-- 1.B) Planes que quedarian SIN ordenes (solo por efecto de esta limpieza) y por
--      lo tanto se eliminarian tambien. Muestra sus productos programados.
SELECT  pl.idplan,
        pl.fecha,
        pl.estado,
        (SELECT COUNT(*) FROM xpr_producto pt WHERE pt.idplan = pl.idplan) AS num_prod_programados,
        (SELECT COALESCE(SUM(pt.cantidad),0) FROM xpr_producto pt WHERE pt.idplan = pl.idplan) AS suma_cant_programada
FROM    xpr_plan pl
WHERE   EXISTS (
            -- el plan tiene al menos una orden candidata a borrar...
            SELECT 1 FROM xpr_produccion p
            WHERE p.idplan = pl.idplan
              AND p.id_tmpenc IS NULL
              AND NOT EXISTS (SELECT 1 FROM xpr_producto pt WHERE pt.idproduccion = p.idproduccion)
              AND NOT EXISTS (SELECT 1 FROM xpr_insumo   s  WHERE s.idproduccion  = p.idproduccion
                                                              AND COALESCE(s.cantidad,0) > 0)
        )
  AND   NOT EXISTS (
            -- ...y NO tiene ninguna orden que NO sea candidata (es decir, todas sus ordenes se borran)
            SELECT 1 FROM xpr_produccion p
            WHERE p.idplan = pl.idplan
              AND NOT (
                    p.id_tmpenc IS NULL
                AND NOT EXISTS (SELECT 1 FROM xpr_producto pt WHERE pt.idproduccion = p.idproduccion)
                AND NOT EXISTS (SELECT 1 FROM xpr_insumo   s  WHERE s.idproduccion  = p.idproduccion
                                                                AND COALESCE(s.cantidad,0) > 0)
              )
        )
ORDER BY pl.fecha;


-- ############################################################################
-- PARTE 2 -- BORRADO (DESTRUCTIVO). Ejecutar solo si la PARTE 1 es correcta.
-- ############################################################################
-- Sugerencia: correr todo el bloque; revisar los conteos del paso 8; si algo
-- no cuadra, ejecutar  ROLLBACK;  en vez de  COMMIT;

START TRANSACTION;

-- 1) Congelar las ordenes candidatas (id + su plan) en una tabla temporal.
--    Se usa el MISMO criterio de la PARTE 1.
CREATE TEMPORARY TABLE tmp_ord_vacias AS
SELECT p.idproduccion, p.idplan
FROM   xpr_produccion p
WHERE  p.id_tmpenc IS NULL
  AND  NOT EXISTS (SELECT 1 FROM xpr_producto pt WHERE pt.idproduccion = p.idproduccion)
  AND  NOT EXISTS (SELECT 1 FROM xpr_insumo   s  WHERE s.idproduccion  = p.idproduccion
                                                   AND COALESCE(s.cantidad,0) > 0);

-- 2) Borrar hijos de esas ordenes.
DELETE s  FROM xpr_insumo                   s  JOIN tmp_ord_vacias t ON t.idproduccion = s.idproduccion;
DELETE mo FROM xpr_manoobra                 mo JOIN tmp_ord_vacias t ON t.idproduccion = mo.idproduccion;
DELETE u  FROM xpr_produccion_ulexita       u  JOIN tmp_ord_vacias t ON t.idproduccion = u.idproduccion;
DELETE z  FROM xpr_produccion_baritina_zona z  JOIN tmp_ord_vacias t ON t.idproduccion = z.idproduccion;
DELETE b  FROM xpr_produccion_baritina      b  JOIN tmp_ord_vacias t ON t.idproduccion = b.idproduccion;
-- (por si hubiera alguna fila de producto terminado con cantidad 0 ligada a la orden)
DELETE pt FROM xpr_producto                 pt JOIN tmp_ord_vacias t ON t.idproduccion = pt.idproduccion;

-- 3) Borrar las ordenes.
DELETE p  FROM xpr_produccion               p  JOIN tmp_ord_vacias t ON t.idproduccion = p.idproduccion;

-- 4) Determinar los planes que quedaron SIN ninguna orden (de entre los afectados).
CREATE TEMPORARY TABLE tmp_planes_vacios AS
SELECT DISTINCT t.idplan
FROM   tmp_ord_vacias t
WHERE  NOT EXISTS (SELECT 1 FROM xpr_produccion p WHERE p.idplan = t.idplan);

-- 5) Borrar los productos PROGRAMADOS de esos planes (xpr_producto ligado al plan).
DELETE pt FROM xpr_producto pt JOIN tmp_planes_vacios t ON t.idplan = pt.idplan;

-- 6) Borrar los planes vacios.
DELETE pl FROM xpr_plan pl JOIN tmp_planes_vacios t ON t.idplan = pl.idplan;

-- 7) Limpieza de temporales.
--    (se hara despues del control; MySQL las descarta al cerrar la sesion igual)

-- 8) Control: cuantas ordenes y planes se eliminaron.
SELECT (SELECT COUNT(*) FROM tmp_ord_vacias)     AS ordenes_eliminadas,
       (SELECT COUNT(*) FROM tmp_planes_vacios)  AS planes_eliminados;

DROP TEMPORARY TABLE IF EXISTS tmp_ord_vacias;
DROP TEMPORARY TABLE IF EXISTS tmp_planes_vacios;

-- 9) Confirmar o revertir:
COMMIT;
-- ROLLBACK;


-- ############################################################################
-- PARTE 3 -- PLANES SIN NINGUNA ORDEN (preexistentes / abandonados)
-- ############################################################################
--
--  El paso anterior solo borra planes que quedan vacios AL borrar sus ordenes.
--  Un plan que NUNCA tuvo ordenes (se ve como "0 ordenes" en el calendario) no
--  lo alcanza. Esta parte lo limpia.
--
--  Criterio RECOMENDADO (realmente vacio, = regla isEmptyPlan del sistema):
--    - el plan NO tiene ordenes            (NOT EXISTS en xpr_produccion), y
--    - sus productos programados suman 0   (o no tiene productos programados).
--  Asi NUNCA se borra un plan con cantidades programadas reales.

-- ---- 3.A) PREVIEW (solo lectura): planes que se eliminarian ----------------
SELECT  pl.idplan,
        pl.fecha,
        pl.estado,
        (SELECT COUNT(*) FROM xpr_producto pt WHERE pt.idplan = pl.idplan)                 AS num_prod_programados,
        (SELECT COALESCE(SUM(pt.cantidad),0) FROM xpr_producto pt WHERE pt.idplan = pl.idplan) AS suma_cant_programada
FROM    xpr_plan pl
WHERE   NOT EXISTS (SELECT 1 FROM xpr_produccion p WHERE p.idplan = pl.idplan)
  AND   COALESCE((SELECT SUM(pt.cantidad) FROM xpr_producto pt WHERE pt.idplan = pl.idplan),0) = 0
ORDER BY pl.fecha;

-- ---- 3.B) BORRADO (destructivo). Ejecutar solo si 3.A es correcto. ---------
START TRANSACTION;

CREATE TEMPORARY TABLE tmp_planes_solos AS
SELECT pl.idplan
FROM   xpr_plan pl
WHERE  NOT EXISTS (SELECT 1 FROM xpr_produccion p WHERE p.idplan = pl.idplan)
  AND  COALESCE((SELECT SUM(pt.cantidad) FROM xpr_producto pt WHERE pt.idplan = pl.idplan),0) = 0;

-- productos programados en 0 (si los hubiera) y luego el plan
DELETE pt FROM xpr_producto pt JOIN tmp_planes_solos t ON t.idplan = pt.idplan;
DELETE pl FROM xpr_plan      pl JOIN tmp_planes_solos t ON t.idplan = pl.idplan;

SELECT COUNT(*) AS planes_sin_orden_eliminados FROM tmp_planes_solos;

DROP TEMPORARY TABLE IF EXISTS tmp_planes_solos;

COMMIT;
-- ROLLBACK;

-- ----------------------------------------------------------------------------
-- VARIANTE MAS AGRESIVA (opcional): borrar CUALQUIER plan sin ordenes, aunque
-- tenga cantidades programadas. Descomentar solo si es lo que quieres; revisa
-- antes el preview quitando la condicion de suma = 0.
-- ----------------------------------------------------------------------------
-- START TRANSACTION;
-- CREATE TEMPORARY TABLE tmp_planes_solos2 AS
--   SELECT pl.idplan FROM xpr_plan pl
--   WHERE NOT EXISTS (SELECT 1 FROM xpr_produccion p WHERE p.idplan = pl.idplan);
-- DELETE pt FROM xpr_producto pt JOIN tmp_planes_solos2 t ON t.idplan = pt.idplan;
-- DELETE pl FROM xpr_plan      pl JOIN tmp_planes_solos2 t ON t.idplan = pl.idplan;
-- DROP TEMPORARY TABLE IF EXISTS tmp_planes_solos2;
-- COMMIT;
