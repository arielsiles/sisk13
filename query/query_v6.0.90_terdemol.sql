-- ============================================================================
-- v6.0.90 :: ULEXITA - Sincronizar Consumo MP corregido con el insumo 5-ULEXITA,
--             el Costo Total de la orden y el costo unitario de los PT.
-- ============================================================================
--
-- CONTEXTO
--   El 'Consumo Materia Prima (TN)' de la linea ULEXITA se calcula como
--       Consumo MP = MERMA + Kpa * PT_TOTAL_BUENO - CONSUMO_REPROCESO
--   (XProductionUlexitaCalc.getConsumoMpCalc; antes NO restaba el reproceso).
--
--   Ese valor se vuelca a la cantidad del insumo de Materia Prima por defecto
--   ('5-ULEXITA') como  cantidad = ROUND(Consumo,2) * 1000  (KG)
--   (XProductionAction.syncMpFromConsumo), PERO solo mientras la orden esta
--   PENDIENTE. Las ordenes ya APROBADAS NO se vuelven a sincronizar.
--
--   La v6.0.87 corrigio el snapshot 'consumo_mp_calc_snap' de las aprobadas, pero
--   NO toco el insumo ni el costeo. Por eso en las ordenes aprobadas quedaron
--   desincronizados:
--     * xpr_insumo.cantidad  del insumo MP (5-ULEXITA)
--     * xpr_produccion.costototal (y totalmp)  de la orden
--     * xpr_producto.costo / costouni / costo_b  de cada producto terminado
--
--   Este script realinea esos valores a partir del 'consumo_mp_calc_snap' ya
--   corregido, replicando exactamente el costeo de XProductionAction.
--
-- DECISIONES (acordadas)
--   1) El costo unitario del insumo (xpr_insumo.costouni) NO se modifica: se
--      mantiene el guardado, igual que hace approve() (que usa el costouni
--      almacenado y nunca lo recalcula). El 'costo del insumo MP' se corrige
--      solo por el cambio de cantidad (costo = cantidad * costouni, no es columna).
--   2) Alcance: SOLO ordenes APROBADAS de linea ULEXITA con snapshot
--      (snap_at y consumo_mp_calc_snap no nulos). Las PENDIENTES se auto-corrigen
--      en vivo al guardar/aprobar (syncMpFromConsumo), no necesitan migracion.
--
-- FORMULAS REPLICADAS (todas HALF_UP == MySQL ROUND para valores positivos)
--   cantidad_MP (KG)  = ROUND( ROUND(consumo_mp_calc_snap, 2) * 1000, 4 )   [si cod_med = KG]
--                     = ROUND( consumo_mp_calc_snap, 2 )                     [si TN]
--   costototal        = ROUND( SUM( ROUND(cantidad*costouni, 6) ), 2 )      [todos los insumos]
--   remainingCost     = ROUND( SUM( ROUND(cantidad*costouni, 6) ), 2 )      [insumos sin asignar]
--   totalVolume       = SUM( ROUND(cantidad_PT * cant_pr, 2) )
--   pct(PT)           = ROUND( ROUND( ROUND( ROUND(cant*cant_pr,2)*100, 2) / totalVolume, 2) / 100, 6)
--   costo_b(PT)       = ROUND( remainingCost * pct, 2 )
--   costo(PT)         = ROUND( ROUND(costo_a + costo_b, 2) + costo_c, 2 )   [costo_a/costo_c se conservan]
--   costouni(PT)      = ROUND( costo / cantidad, 2 )
--
-- IDEMPOTENTE / REVERSIBLE
--   * Una tabla de trabajo (bkp_v6090_ulexita_fix) congela el conjunto de ordenes
--     a corregir + valores originales del insumo MP y del costototal. Se llena una
--     sola vez (NOT EXISTS); en re-ejecuciones no agrega filas.
--   * bkp_v6090_ulexita_producto respalda costo/costouni/costo_b/a/c de los PT.
--   * Todos los UPDATE se hacen JOIN a la tabla de trabajo y son deterministas:
--     re-ejecutarlos da el mismo resultado.
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
-- 1) CONSULTA DE IDENTIFICACION (solo lectura; ejecutar antes de migrar).
--    Lista las ordenes ULEXITA aprobadas cuyo insumo MP (5-ULEXITA) NO coincide
--    con el Consumo MP corregido. Muestra cantidad actual vs esperada (KG) y el
--    Costo Total actual vs recalculado.
-- ----------------------------------------------------------------------------
SELECT
    p.idproduccion,
    p.codigo                                                   AS orden,
    mi.idinsumo                                                AS idinsumo_mp,
    mi.cod_art                                                 AS cod_art_mp,
    (SELECT a.cod_med FROM inv_articulos a WHERE a.cod_art = mi.cod_art LIMIT 1) AS unidad_mp,
    u.consumo_mp_calc_snap                                     AS consumo_mp_tn,
    mi.cantidad                                                AS cantidad_mp_actual,
    CASE WHEN UPPER(TRIM((SELECT a.cod_med FROM inv_articulos a WHERE a.cod_art = mi.cod_art LIMIT 1))) = 'KG'
         THEN ROUND(ROUND(u.consumo_mp_calc_snap, 2) * 1000, 4)
         ELSE ROUND(u.consumo_mp_calc_snap, 2)
    END                                                        AS cantidad_mp_esperada,
    p.costototal                                               AS costototal_actual,
    ROUND((SELECT SUM(ROUND(i2.cantidad * i2.costouni, 6))
           FROM xpr_insumo i2 WHERE i2.idproduccion = p.idproduccion), 2) AS costototal_recalc_con_qty_actual
FROM xpr_produccion p
JOIN xpr_linea l                ON l.idlinea = p.idlinea
JOIN xpr_produccion_ulexita u   ON u.idproduccion = p.idproduccion
JOIN xpr_insumo mi              ON mi.idinsumo = (
        SELECT MIN(i2.idinsumo)
        FROM xpr_insumo i2
        JOIN xpr_insumoformula f2 ON f2.idinsumoformula = i2.idinsumoformula AND f2.defecto = 1
        WHERE i2.idproduccion = p.idproduccion)
WHERE p.estado = 'APR'
  AND l.report_template_code = 'ULEXITA'
  AND u.snap_at IS NOT NULL
  AND u.consumo_mp_calc_snap IS NOT NULL
  AND ABS(mi.cantidad -
          CASE WHEN UPPER(TRIM((SELECT a.cod_med FROM inv_articulos a WHERE a.cod_art = mi.cod_art LIMIT 1))) = 'KG'
               THEN ROUND(ROUND(u.consumo_mp_calc_snap, 2) * 1000, 4)
               ELSE ROUND(u.consumo_mp_calc_snap, 2)
          END) > 0.0001
ORDER BY p.idproduccion;


-- ----------------------------------------------------------------------------
-- 2) TABLA DE TRABAJO / RESPALDO: congela el conjunto a corregir y los valores
--    originales del insumo MP + costototal. Se llena una sola vez (NOT EXISTS).
--    cantidad_mp_new = cantidad esperada del insumo MP (KG o TN segun unidad).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bkp_v6090_ulexita_fix (
    idproduccion      BIGINT        NOT NULL,
    idinsumo_mp       BIGINT        NOT NULL,
    cod_med_mp        VARCHAR(6)    NULL,
    consumo_mp_calc_snap DECIMAL(14,4) NULL,
    cantidad_mp_old   DECIMAL(14,4) NULL,
    cantidad_mp_new   DECIMAL(14,4) NULL,
    costototal_old    DECIMAL(16,2) NULL,
    totalmp_old       DECIMAL(16,2) NULL,
    backed_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idproduccion)
) ENGINE=InnoDB;

INSERT INTO bkp_v6090_ulexita_fix
    (idproduccion, idinsumo_mp, cod_med_mp, consumo_mp_calc_snap,
     cantidad_mp_old, cantidad_mp_new, costototal_old, totalmp_old)
SELECT
    p.idproduccion,
    mi.idinsumo,
    (SELECT a.cod_med FROM inv_articulos a WHERE a.cod_art = mi.cod_art LIMIT 1),
    u.consumo_mp_calc_snap,
    mi.cantidad,
    CASE WHEN UPPER(TRIM((SELECT a.cod_med FROM inv_articulos a WHERE a.cod_art = mi.cod_art LIMIT 1))) = 'KG'
         THEN ROUND(ROUND(u.consumo_mp_calc_snap, 2) * 1000, 4)
         ELSE ROUND(u.consumo_mp_calc_snap, 2)
    END,
    p.costototal,
    p.totalmp
FROM xpr_produccion p
JOIN xpr_linea l                ON l.idlinea = p.idlinea
JOIN xpr_produccion_ulexita u   ON u.idproduccion = p.idproduccion
JOIN xpr_insumo mi              ON mi.idinsumo = (
        SELECT MIN(i2.idinsumo)
        FROM xpr_insumo i2
        JOIN xpr_insumoformula f2 ON f2.idinsumoformula = i2.idinsumoformula AND f2.defecto = 1
        WHERE i2.idproduccion = p.idproduccion)
WHERE p.estado = 'APR'
  AND l.report_template_code = 'ULEXITA'
  AND u.snap_at IS NOT NULL
  AND u.consumo_mp_calc_snap IS NOT NULL
  AND ABS(mi.cantidad -
          CASE WHEN UPPER(TRIM((SELECT a.cod_med FROM inv_articulos a WHERE a.cod_art = mi.cod_art LIMIT 1))) = 'KG'
               THEN ROUND(ROUND(u.consumo_mp_calc_snap, 2) * 1000, 4)
               ELSE ROUND(u.consumo_mp_calc_snap, 2)
          END) > 0.0001
  AND NOT EXISTS (SELECT 1 FROM bkp_v6090_ulexita_fix b WHERE b.idproduccion = p.idproduccion);


-- ----------------------------------------------------------------------------
-- 3) RESPALDO de los productos terminados de las ordenes a corregir.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bkp_v6090_ulexita_producto (
    idproducto    BIGINT        NOT NULL,
    idproduccion  BIGINT        NULL,
    cantidad      DECIMAL(14,4) NULL,
    costo_old     DECIMAL(16,2) NULL,
    costouni_old  DECIMAL(16,2) NULL,
    costo_a       DECIMAL(16,2) NULL,
    costo_b_old   DECIMAL(16,2) NULL,
    costo_c       DECIMAL(16,2) NULL,
    backed_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idproducto)
) ENGINE=InnoDB;

INSERT INTO bkp_v6090_ulexita_producto
    (idproducto, idproduccion, cantidad, costo_old, costouni_old, costo_a, costo_b_old, costo_c)
SELECT pr.idproducto, pr.idproduccion, pr.cantidad, pr.costo, pr.costouni,
       pr.costo_a, pr.costo_b, pr.costo_c
FROM xpr_producto pr
JOIN bkp_v6090_ulexita_fix b ON b.idproduccion = pr.idproduccion
WHERE NOT EXISTS (SELECT 1 FROM bkp_v6090_ulexita_producto x WHERE x.idproducto = pr.idproducto);


-- ----------------------------------------------------------------------------
-- 4) Actualizar la CANTIDAD del insumo MP (5-ULEXITA) al valor esperado (KG/TN).
--    (El costouni del insumo NO se toca; ver decision 1.)
-- ----------------------------------------------------------------------------
UPDATE xpr_insumo mi
JOIN bkp_v6090_ulexita_fix b ON b.idinsumo_mp = mi.idinsumo
SET mi.cantidad = b.cantidad_mp_new;


-- ----------------------------------------------------------------------------
-- 5) Recalcular el COSTO TOTAL de la orden (y totalmp / M.P. Usada) con las
--    cantidades ya corregidas.
--      costototal = ROUND( SUM(ROUND(cantidad*costouni,6)), 2 )   [todos los insumos]
--      totalmp    = ROUND( SUM(cantidad), 2 )                     [insumos inputDefault]
-- ----------------------------------------------------------------------------
UPDATE xpr_produccion p
JOIN bkp_v6090_ulexita_fix b ON b.idproduccion = p.idproduccion
SET
    p.costototal = (
        SELECT ROUND(SUM(ROUND(i.cantidad * i.costouni, 6)), 2)
        FROM xpr_insumo i
        WHERE i.idproduccion = p.idproduccion),
    p.totalmp = (
        SELECT ROUND(SUM(i.cantidad), 2)
        FROM xpr_insumo i
        JOIN xpr_insumoformula f ON f.idinsumoformula = i.idinsumoformula AND f.defecto = 1
        WHERE i.idproduccion = p.idproduccion);


-- ----------------------------------------------------------------------------
-- 6) Recalcular COSTO B (distribucion por volumen) de cada producto terminado.
--    remainingCost (insumos sin asignar) se reparte proporcional al volumen de PT.
--    Como en la UI ya no se asigna insumo a un PT puntual, remainingCost == costototal.
--    Las subconsultas que leen xpr_producto van doblemente anidadas para forzar
--    materializacion (evita "target table in FROM" al actualizar xpr_producto).
-- ----------------------------------------------------------------------------
UPDATE xpr_producto pr
JOIN bkp_v6090_ulexita_fix b ON b.idproduccion = pr.idproduccion
JOIN ( SELECT idproduccion, rc FROM (
           SELECT i.idproduccion AS idproduccion,
                  ROUND(SUM(ROUND(i.cantidad * i.costouni, 6)), 2) AS rc
           FROM xpr_insumo i
           WHERE i.idproducto IS NULL
           GROUP BY i.idproduccion
       ) zr ) rc ON rc.idproduccion = pr.idproduccion
JOIN ( SELECT idproduccion, tv FROM (
           SELECT pr2.idproduccion AS idproduccion,
                  SUM(ROUND(pr2.cantidad *
                       (SELECT a2.cant_pr FROM inv_articulos a2 WHERE a2.cod_art = pr2.cod_art LIMIT 1), 2)) AS tv
           FROM xpr_producto pr2
           GROUP BY pr2.idproduccion
       ) zv ) tv ON tv.idproduccion = pr.idproduccion
SET pr.costo_b = ROUND(
        rc.rc * ROUND(
                  ROUND(
                    ROUND(
                      ROUND(pr.cantidad *
                        (SELECT a.cant_pr FROM inv_articulos a WHERE a.cod_art = pr.cod_art LIMIT 1), 2)
                      * 100, 2)
                    / tv.tv, 2)
                  / 100, 6),
        2)
WHERE pr.cantidad > 0
  AND tv.tv > 0;


-- ----------------------------------------------------------------------------
-- 7) Recalcular COSTO y COSTO UNITARIO de cada producto terminado.
--      costo    = ROUND( ROUND(costo_a + costo_b, 2) + costo_c, 2 )
--      costouni = ROUND( costo / cantidad, 2 )
--    OJO: este es un UPDATE multi-tabla (tiene JOIN). En MySQL el orden de
--    evaluacion de los SET en updates multi-tabla NO esta garantizado, por lo que
--    costouni NO debe leer la columna pr.costo recien asignada (podria tomar el
--    valor viejo). Se calcula costouni con la MISMA expresion (costo_a/b/c), que
--    son columnas estables -> resultado independiente del orden de asignacion.
-- ----------------------------------------------------------------------------
UPDATE xpr_producto pr
JOIN bkp_v6090_ulexita_fix b ON b.idproduccion = pr.idproduccion
SET pr.costo    = ROUND(ROUND(pr.costo_a + pr.costo_b, 2) + pr.costo_c, 2),
    pr.costouni = ROUND(ROUND(ROUND(pr.costo_a + pr.costo_b, 2) + pr.costo_c, 2) / pr.cantidad, 2)
WHERE pr.cantidad > 0;


-- ----------------------------------------------------------------------------
-- 8) VERIFICACION (opcional). Cada SELECT deberia devolver 0 filas.
-- ----------------------------------------------------------------------------
-- a) Insumo MP ya sincronizado con la cantidad esperada:
-- SELECT mi.idinsumo, mi.cantidad, b.cantidad_mp_new
-- FROM xpr_insumo mi
-- JOIN bkp_v6090_ulexita_fix b ON b.idinsumo_mp = mi.idinsumo
-- WHERE ABS(mi.cantidad - b.cantidad_mp_new) > 0.0001;
--
-- b) Costo Total de la orden = suma de insumos:
-- SELECT p.idproduccion, p.costototal,
--        ROUND((SELECT SUM(ROUND(i.cantidad*i.costouni,6)) FROM xpr_insumo i WHERE i.idproduccion=p.idproduccion),2) AS esperado
-- FROM xpr_produccion p
-- JOIN bkp_v6090_ulexita_fix b ON b.idproduccion = p.idproduccion
-- WHERE ABS(p.costototal - ROUND((SELECT SUM(ROUND(i.cantidad*i.costouni,6)) FROM xpr_insumo i WHERE i.idproduccion=p.idproduccion),2)) > 0.01;
--
-- c) Costo / costo unitario de PT consistentes (costo = a+b+c, costouni = costo/cant):
-- SELECT pr.idproducto, pr.costo, pr.costouni
-- FROM xpr_producto pr
-- JOIN bkp_v6090_ulexita_fix b ON b.idproduccion = pr.idproduccion
-- WHERE pr.cantidad > 0
--   AND ( ABS(pr.costo - ROUND(ROUND(pr.costo_a+pr.costo_b,2)+pr.costo_c,2)) > 0.01
--      OR ABS(pr.costouni - ROUND(pr.costo/pr.cantidad,2)) > 0.01 );

-- ----------------------------------------------------------------------------
-- 9) LIMPIEZA (opcional, ejecutar solo tras validar):
-- DROP TABLE IF EXISTS bkp_v6090_ulexita_producto;
-- DROP TABLE IF EXISTS bkp_v6090_ulexita_fix;
-- ============================================================================

-- ============================================================================
-- ============================================================================

