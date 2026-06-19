-- ============================================================================
-- v6.0.87 :: Correccion de snapshots ULEXITA (migracion de datos)
--             Recalcula dos snapshots de xpr_produccion_ulexita con sus
--             formulas corregidas:
--               a) ley_mp_recalc_snap  = ley_pt / kpm_merma_snap
--               b) consumo_mp_calc_snap = merma_snap + kpa_snap * pt_total_bueno_snap
--                                         - COALESCE(consumo_reproceso_tn, 0)
-- ============================================================================
--
-- Contexto:
--   Dos formulas cambiaron en el codigo:
--     a) 'Ley MP recalculada': antes ley_pt / kpm_bentonita ; ahora / kpm_merma.
--     b) 'Consumo Materia Prima (TN)': ahora descuenta tambien el reproceso.
--
--   Las ordenes PENDIENTES se recalculan en vivo (ya muestran el valor nuevo).
--   Las ordenes APROBADAS leen los snapshots, grabados con las formulas viejas,
--   por lo que pantalla y reporte mostraban valores incorrectos.
--
--   Todas las entradas del recalculo YA estan almacenadas y sus formulas NO
--   cambiaron (kpm_merma_snap, merma_snap, kpa_snap, pt_total_bueno_snap) o son
--   input (ley_pt, consumo_reproceso_tn), por lo que ambos valores se recomputan
--   de forma exacta sin re-aprobar las ordenes.
--
-- Alcance:
--   Sin filtrar por estado (cubre aprobadas y pendientes con snapshot). Cada
--   UPDATE aplica la misma guarda de nulos que el calculo en vivo.
--
-- Reversible / idempotente:
--   * Un unico respaldo previo (idempotente) guarda ambos valores originales.
--   * Los UPDATE son deterministas: re-ejecutarlos da el mismo resultado.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 1) Respaldo unico de los valores originales (idempotente).
--    Captura ambos snapshots viejos + las entradas de los dos recalculos, para
--    auditar o revertir. INSERT IGNORE: solo guarda la primera vez. Incluye las
--    filas afectadas por CUALQUIERA de los dos UPDATE.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bkp_v6087_ulexita_snap (
    idproduccion_ulexita     BIGINT        NOT NULL,
    ley_mp_recalc_snap_old   DECIMAL(14,4) NULL,
    consumo_mp_calc_snap_old DECIMAL(14,4) NULL,
    ley_pt                   DECIMAL(14,4) NULL,
    kpm_merma_snap           DECIMAL(14,6) NULL,
    kpm_bentonita_snap       DECIMAL(14,6) NULL,
    merma_snap               DECIMAL(14,4) NULL,
    kpa_snap                 DECIMAL(14,6) NULL,
    pt_total_bueno_snap      DECIMAL(14,4) NULL,
    consumo_reproceso_tn     DECIMAL(14,4) NULL,
    backed_at                TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idproduccion_ulexita)
) ENGINE=InnoDB;

INSERT IGNORE INTO bkp_v6087_ulexita_snap
    (idproduccion_ulexita, ley_mp_recalc_snap_old, consumo_mp_calc_snap_old,
     ley_pt, kpm_merma_snap, kpm_bentonita_snap,
     merma_snap, kpa_snap, pt_total_bueno_snap, consumo_reproceso_tn)
SELECT u.idproduccion_ulexita, u.ley_mp_recalc_snap, u.consumo_mp_calc_snap,
       u.ley_pt, u.kpm_merma_snap, u.kpm_bentonita_snap,
       u.merma_snap, u.kpa_snap, u.pt_total_bueno_snap, u.consumo_reproceso_tn
FROM xpr_produccion_ulexita u
WHERE (u.kpm_merma_snap IS NOT NULL AND u.kpm_merma_snap <> 0 AND u.ley_pt IS NOT NULL)
   OR (u.merma_snap IS NOT NULL AND u.kpa_snap IS NOT NULL AND u.pt_total_bueno_snap IS NOT NULL);

-- ----------------------------------------------------------------------------
-- 2) Correccion 'Ley MP recalculada':
--    ley_mp_recalc_snap = ley_pt / kpm_merma_snap   (4 decimales)
-- ----------------------------------------------------------------------------
UPDATE xpr_produccion_ulexita
SET ley_mp_recalc_snap = ROUND(ley_pt / kpm_merma_snap, 4)
WHERE kpm_merma_snap IS NOT NULL
  AND kpm_merma_snap <> 0
  AND ley_pt IS NOT NULL;

-- ----------------------------------------------------------------------------
-- 3) Correccion 'Consumo Materia Prima (TN)':
--    consumo_mp_calc_snap = merma_snap + (kpa_snap * pt_total_bueno_snap)
--                           - COALESCE(consumo_reproceso_tn, 0)   (4 decimales)
-- ----------------------------------------------------------------------------
UPDATE xpr_produccion_ulexita
SET consumo_mp_calc_snap = ROUND(
        merma_snap + (kpa_snap * pt_total_bueno_snap) - COALESCE(consumo_reproceso_tn, 0), 4)
WHERE merma_snap IS NOT NULL
  AND kpa_snap IS NOT NULL
  AND pt_total_bueno_snap IS NOT NULL;

-- ----------------------------------------------------------------------------
-- 4) Verificacion (opcional). Cada SELECT debe devolver 0 filas.
-- ----------------------------------------------------------------------------
-- a) Ley MP recalculada
-- SELECT u.idproduccion_ulexita, u.ley_pt, u.kpm_merma_snap, u.ley_mp_recalc_snap,
--        ROUND(u.ley_pt / u.kpm_merma_snap, 4) AS esperado
-- FROM xpr_produccion_ulexita u
-- WHERE u.kpm_merma_snap IS NOT NULL AND u.kpm_merma_snap <> 0 AND u.ley_pt IS NOT NULL
--   AND ABS(u.ley_mp_recalc_snap - ROUND(u.ley_pt / u.kpm_merma_snap, 4)) > 0.0001;
--
-- b) Consumo Materia Prima
-- SELECT u.idproduccion_ulexita, u.merma_snap, u.kpa_snap, u.pt_total_bueno_snap,
--        u.consumo_reproceso_tn, u.consumo_mp_calc_snap,
--        ROUND(u.merma_snap + (u.kpa_snap * u.pt_total_bueno_snap)
--              - COALESCE(u.consumo_reproceso_tn, 0), 4) AS esperado
-- FROM xpr_produccion_ulexita u
-- WHERE u.merma_snap IS NOT NULL AND u.kpa_snap IS NOT NULL AND u.pt_total_bueno_snap IS NOT NULL
--   AND ABS(u.consumo_mp_calc_snap - ROUND(u.merma_snap + (u.kpa_snap * u.pt_total_bueno_snap)
--           - COALESCE(u.consumo_reproceso_tn, 0), 4)) > 0.0001;


-- ============================================================================
-- v6.0.87 (cont.) :: Baja de columna ulex_disponible_snap (ULEXITA)
-- ============================================================================
--
-- El campo 'ULEX disponible (TN)' de la orden de produccion no se calculaba, no
-- se editaba (input disabled) y nadie lo leia: solo persistia 0. El reporte
-- calcula 'ULEX DISPONIBLE' aparte (saldo_ant + INGRESO - CONSUMO). Se elimina
-- de la UX y del modelo; aqui se baja la columna.
--
-- DROP idempotente: solo elimina si la columna existe.
-- ----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'xpr_produccion_ulexita'
                    AND COLUMN_NAME = 'ulex_disponible_snap');
SET @sql := IF(@col_exists > 0,
               'ALTER TABLE xpr_produccion_ulexita DROP COLUMN ulex_disponible_snap',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
