-- ============================================================================
-- v6.0.77 :: Snapshots de calculos en xpr_produccion_ulexita
--             Persistencia de los valores calculados al momento de aprobar
--             la orden, garantizando la inmutabilidad historica del reporte
--             aunque cambien parametros (merma_factor, fórmula, articulos)
--             o ediciones post-aprobacion.
-- ============================================================================
--
-- Politica:
--   - Pendiente: cálculos en vivo desde XProductionUlexitaCalc.
--   - Aprobado: lectura desde columnas *_snap; recalculo bloqueado.
--   - Re-edicion con permiso PRODUCTION_LAB_DATA:UPDATE post-aprobacion:
--     se re-snapshotea (snap_at y snap_by se actualizan).
--   - Desaprobar: snaps se conservan; al re-aprobar se sobreescriben.
-- ----------------------------------------------------------------------------

ALTER TABLE xpr_produccion_ulexita
    ADD COLUMN merma_factor_snap     DECIMAL(10,4) NULL,
    ADD COLUMN diluyente_total_snap  DECIMAL(14,4) NULL,
    ADD COLUMN bentonita_pct_snap    DECIMAL(8,4)  NULL,
    ADD COLUMN caolin_pct_snap       DECIMAL(8,4)  NULL,
    ADD COLUMN pt_a_snap             DECIMAL(14,4) NULL,
    ADD COLUMN pt_b_snap             DECIMAL(14,4) NULL,
    ADD COLUMN pt_total_bueno_snap   DECIMAL(14,4) NULL,
    ADD COLUMN kpa_snap              DECIMAL(14,6) NULL,
    ADD COLUMN kpm_bentonita_snap    DECIMAL(14,6) NULL,
    ADD COLUMN kpm_merma_snap        DECIMAL(14,6) NULL,
    ADD COLUMN ley_mp_recalc_snap    DECIMAL(14,4) NULL,
    ADD COLUMN merma_snap            DECIMAL(14,4) NULL,
    ADD COLUMN merma_pct_snap        DECIMAL(8,4)  NULL,
    ADD COLUMN snap_at               DATETIME      NULL,
    ADD COLUMN snap_by               VARCHAR(4)    NULL;

-- ----------------------------------------------------------------------------
-- Notas:
--   ulex_disponible_snap y consumo_mp_calc_snap ya existen desde v6.0.76.
--   snap_at marca "ultimo snapshot" (al aprobar o al re-editar lab-data).
--   snap_by guarda el codigo de financesCode del usuario que disparo el snap.
--   Backfill de ordenes ya aprobadas: NO se hace automaticamente.
--     Si se necesita, ejecutar la accion "Recalcular snapshots" desde la UI
--     (a implementar como tarea admin si fuera necesario).
-- ----------------------------------------------------------------------------
