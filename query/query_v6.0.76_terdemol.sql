-- ============================================================================
-- v6.0.76 :: Reporte Diario de Produccion ULEXITA
--             Configuracion por linea + tabla satelite de datos especificos
--             para la pantalla "Datos del Proceso" en xpr_produccion.
-- ============================================================================
--
-- Cambios:
--   1) ALTER xpr_linea: nuevos campos para configurar la linea (cod_art de MP,
--      PT clasificacion A/B, diluyente bentonita/caolin, factor de merma,
--      template de reporte).
--   2) CREATE xpr_produccion_ulexita: datos especificos de cada produccion
--      ULEXITA - laboratorio (Ley MP, Ley PT), granulado, reproceso, snapshots.
--   3) Permiso PRODUCTION_LAB_DATA para edicion de datos de laboratorio
--      independiente del estado de la produccion.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 1) Configuracion extendida de la linea de produccion
-- ----------------------------------------------------------------------------
ALTER TABLE xpr_linea
    ADD COLUMN report_template_code   VARCHAR(20)   NULL,
    ADD COLUMN cod_art_mp_principal   VARCHAR(20)   NULL,
    ADD COLUMN cod_art_pt_a           VARCHAR(20)   NULL,
    ADD COLUMN cod_art_pt_b           VARCHAR(20)   NULL,
    ADD COLUMN cod_art_diluy_bent     VARCHAR(20)   NULL,
    ADD COLUMN cod_art_diluy_caolin   VARCHAR(20)   NULL,
    ADD COLUMN merma_factor           DECIMAL(10,4) NOT NULL DEFAULT 1.0300;

-- ----------------------------------------------------------------------------
-- 2) Datos especificos de produccion ULEXITA (1 a 1 con xpr_produccion)
-- ----------------------------------------------------------------------------
CREATE TABLE xpr_produccion_ulexita (
    idproduccion_ulexita    BIGINT        NOT NULL AUTO_INCREMENT,
    idproduccion            BIGINT        NOT NULL,
    ley_mp_bentonita        DECIMAL(14,4) NULL,
    ley_pt                  DECIMAL(14,4) NULL,
    producto_granulado_tn   DECIMAL(14,4) NULL,
    consumo_reproceso_tn    DECIMAL(14,4) NULL,
    reproceso_final_tn      DECIMAL(14,4) NULL,
    diluyente_total_tn      DECIMAL(14,4) NULL,
    bentonita_pct           DECIMAL(8,4)  NULL,
    ulex_disponible_snap    DECIMAL(14,4) NULL,
    consumo_mp_calc_snap    DECIMAL(14,4) NULL,
    observacion_lab         VARCHAR(500)  NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    idcompania              BIGINT        NOT NULL,
    PRIMARY KEY (idproduccion_ulexita),
    UNIQUE KEY uk_xpr_prod_ulexita_prod (idproduccion),
    CONSTRAINT fk_xpr_prod_ulexita_prod
        FOREIGN KEY (idproduccion) REFERENCES xpr_produccion (idproduccion)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3) Permiso PRODUCTION_LAB_DATA - edicion de datos de laboratorio
--    permite editar leyes MP/PT y observacion_lab incluso despues de aprobar.
--    idmodulo = 11 (xproduction, alineado con PRODUCTION_INPUTS_REPORT existente)
--    permiso bitmask = UPDATE(4) = 4
-- ----------------------------------------------------------------------------
insert into funcionalidad
values (453, 'PRODUCTION_LAB_DATA',
        'Edicion de datos de laboratorio en produccion (leyes MP/PT) post-aprobacion',
        11, 4, 'menu.xproduction.production', 1);

-- ----------------------------------------------------------------------------
-- 4) Permiso PRODUCTION_LABOR - registro de mano de obra en orden de produccion
--    Permite mostrar/ocultar el boton "+ Mano Obra" y la pestaña "Mano de Obra"
--    de forma independiente de PRODUCTION:UPDATE.
--    idmodulo = 11 (xproduction)
--    permiso bitmask = VIEW(1) + UPDATE(4) + DELETE(8) = 13
-- ----------------------------------------------------------------------------
insert into funcionalidad
values (454, 'PRODUCTION_LABOR',
        'Registro de mano de obra en orden de produccion',
        11, 13, 'menu.xproduction.production', 1);
