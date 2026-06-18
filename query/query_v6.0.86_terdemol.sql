-- ============================================================================
-- v6.0.86 :: Produccion BARITINA
--             Lineas de produccion tipo BARITINA: distribucion del uso de
--             materia prima por zonas productivas (maestro zonaproductiva del
--             acopio) dentro de las ordenes de produccion (xpr_produccion).
-- ============================================================================
--
-- Contexto:
--   Se generaliza el discriminador report_template_code de xpr_linea (ya
--   existente desde v6.0.76 para ULEXITA) con un nuevo tipo BARITINA. La
--   logica de tipo de linea se resuelve por codigo (enum ProductionLineType),
--   sin hardcodear cadenas en acciones ni vistas.
--
-- Cambios (TODO ADITIVO - no altera tablas ULEXITA ni del flujo general):
--   1) CREATE xpr_produccion_baritina       : cabecera 1 a 1 con xpr_produccion
--      (uso MP y producto terminado -snapshot derivado en TN-, turnos, observacion).
--   2) CREATE xpr_produccion_baritina_zona  : N filas por produccion con el
--      porcentaje de uso por zona productiva y la cantidad calculada.
--
-- Notas:
--   * No requiere nuevos permisos: la pantalla de orden de produccion reusa
--     PRODUCTION (VIEW/CREATE/UPDATE/DELETE).
--   * No requiere semilla en 'secuencia': el generador TABLE de Hibernate crea
--     la fila del segmento en el primer uso (igual que xpr_produccion_ulexita).
--   * Las zonas productivas (SACACA/HUALLATARI/PATOYU/3 CRUCES/ANZALDO/
--     VISCACHANI) provienen del maestro existente zonaproductiva (acopio); no
--     se crean tablas de zonas.
--   * La linea de produccion BARITINA y su formulacion se dan de alta por
--     pantalla (Lineas de Produccion / Formulaciones) una vez desplegado:
--     basta crear una linea con Template de reporte = BARITINA.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 1) Cabecera de produccion BARITINA (1 a 1 con xpr_produccion)
-- ----------------------------------------------------------------------------
CREATE TABLE xpr_produccion_baritina (
    idproduccion_baritina   BIGINT        NOT NULL AUTO_INCREMENT,
    idproduccion            BIGINT        NOT NULL,
    uso_mp_tn               DECIMAL(14,4) NULL,
    pt_tn                   DECIMAL(14,4) NULL,
    turnos                  INT           NULL,
    observacion             VARCHAR(500)  NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    idcompania              BIGINT        NOT NULL,
    PRIMARY KEY (idproduccion_baritina),
    UNIQUE KEY uk_xpr_prod_baritina_prod (idproduccion),
    CONSTRAINT fk_xpr_prod_baritina_prod
        FOREIGN KEY (idproduccion) REFERENCES xpr_produccion (idproduccion)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2) Distribucion por zona productiva (N filas por produccion)
--    porcentaje 0..100 ; cantidad_tn = uso_mp_tn * porcentaje / 100
--    La suma de porcentajes de una produccion debe ser 100% (validado en Java).
-- ----------------------------------------------------------------------------
CREATE TABLE xpr_produccion_baritina_zona (
    idproduccion_baritina_zona  BIGINT        NOT NULL AUTO_INCREMENT,
    idproduccion                BIGINT        NOT NULL,
    idzonaproductiva            BIGINT        NOT NULL,
    porcentaje                  DECIMAL(8,4)  NULL,
    cantidad_tn                 DECIMAL(14,4) NULL,
    version                     BIGINT        NOT NULL DEFAULT 0,
    idcompania                  BIGINT        NOT NULL,
    PRIMARY KEY (idproduccion_baritina_zona),
    KEY ix_xpr_baritina_zona_prod (idproduccion),
    KEY ix_xpr_baritina_zona_zona (idzonaproductiva),
    CONSTRAINT fk_xpr_baritina_zona_prod
        FOREIGN KEY (idproduccion) REFERENCES xpr_produccion (idproduccion),
    CONSTRAINT fk_xpr_baritina_zona_zona
        FOREIGN KEY (idzonaproductiva) REFERENCES zonaproductiva (idzonaproductiva)
) ENGINE=InnoDB;


-- ----------------------------------------------------------------------------
-- 3) Ajuste posterior: eliminar columna despacho_tn de xpr_produccion_baritina.
--    El despacho de baritina proviene de otra fuente y se quita de la pantalla
--    y del modelo. DROP idempotente: solo elimina si la columna existe (para
--    BDs que ya corrieron la version inicial de este script con la columna).
-- ----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'xpr_produccion_baritina'
                    AND COLUMN_NAME = 'despacho_tn');
SET @sql := IF(@col_exists > 0,
               'ALTER TABLE xpr_produccion_baritina DROP COLUMN despacho_tn',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ----------------------------------------------------------------------------
-- 4) Permiso propio para "Reporte Produccion Diaria"
--    Hasta ahora el menu Produccion > Reportes > Reporte Produccion Diaria se
--    mostraba con el permiso generico PRODUCTION:VIEW (compartido con las
--    ordenes de produccion). Se le da su propia funcionalidad para poder
--    habilitarlo/ocultarlo por rol de forma independiente, igual que
--    PRODUCTION_INPUTS_REPORT (id 315).
--      idmodulo = 11 (xproduction) ; permiso bitmask = VIEW(1)
--      resourceKey = menu.xproduction.dailyProductionReport
--    Orden de columnas igual a las demas altas de funcionalidad:
--      (id, codigo, descripcion, idmodulo, permiso, nombrerecurso, habilitado)
-- ----------------------------------------------------------------------------
insert into funcionalidad
values (474, 'PRODUCTION_DAILY_REPORT', 'Reporte de Produccion Diaria (ULEXITA/BARITINA)',
        11, 1, 'menu.xproduction.dailyProductionReport', 1);
