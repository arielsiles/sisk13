-- ============================================================================
-- v6.0.88 :: BARITINA - calculo PT -> Materia Prima por factor
-- ============================================================================
--
-- Para las lineas de produccion tipo BARITINA, al ingresar la cantidad del
-- Producto Terminado principal se calcula la Materia Prima por defecto como
--   MP = PT * factor
-- y se vuelca en el insumo marcado 'inputDefault' de la formulacion.
--
-- Se agregan dos columnas de configuracion a xpr_linea (ProductionLine):
--   * cod_art_pt_principal : cod_art del PT que dispara el calculo (no se
--     hardcodea; columna dedicada, distinta de cod_art_pt_a de ULEXITA).
--   * factor_pt_mp         : factor de conversion PT -> MP (ej. 1.02). Si es
--     nulo o <= 0, el calculo no toca la MP (se respeta lo ingresado).
--
-- TODO ADITIVO. ADD COLUMN idempotente: solo agrega si la columna no existe.
-- ----------------------------------------------------------------------------

-- cod_art_pt_principal -------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'xpr_linea'
                    AND COLUMN_NAME = 'cod_art_pt_principal');
SET @sql := IF(@col_exists = 0,
               'ALTER TABLE xpr_linea ADD COLUMN cod_art_pt_principal VARCHAR(20) NULL',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- factor_pt_mp ---------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'xpr_linea'
                    AND COLUMN_NAME = 'factor_pt_mp');
SET @sql := IF(@col_exists = 0,
               'ALTER TABLE xpr_linea ADD COLUMN factor_pt_mp DECIMAL(10,4) NULL',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- ULEXITA - articulo destino del Reproceso final (TN)
-- ============================================================================
--
-- Para las lineas de produccion tipo ULEXITA se configura el articulo donde,
-- al aprobar la orden, se acumulara el valor 'Reproceso final (TN)' (ej. el
-- articulo 'Ulexita Procesada'). El articulo NO se hardcodea: se almacena su
-- cod_art en una columna dedicada de xpr_linea (ProductionLine).
--
--   * cod_art_reproc_final : cod_art del articulo que acumula el Reproceso
--     final. Si es nulo, no se acumula nada al aprobar.
--
-- TODO ADITIVO. ADD COLUMN idempotente: solo agrega si la columna no existe.
-- ----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'xpr_linea'
                    AND COLUMN_NAME = 'cod_art_reproc_final');
SET @sql := IF(@col_exists = 0,
               'ALTER TABLE xpr_linea ADD COLUMN cod_art_reproc_final VARCHAR(20) NULL',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------------------
-- La ORDEN guarda tambien el articulo destino del Reproceso final: se copia
-- desde la config de la linea al guardar, para que la orden conserve con que
-- articulo se haran los movimientos de inventario (junto al valor ya guardado
-- en reproceso_final_tn). Columna en la tabla satelite xpr_produccion_ulexita.
-- ADD COLUMN idempotente: solo agrega si la columna no existe.
-- ----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'xpr_produccion_ulexita'
                    AND COLUMN_NAME = 'cod_art_reproc_final');
SET @sql := IF(@col_exists = 0,
               'ALTER TABLE xpr_produccion_ulexita ADD COLUMN cod_art_reproc_final VARCHAR(20) NULL',
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- Permiso propio para DESAPROBAR una orden de produccion
-- ============================================================================
--
-- Hasta ahora el boton "Desaprobar" se mostraba solo por el estado (aprobada),
-- sin permiso propio. Se le da su funcionalidad para habilitarlo/ocultarlo por
-- rol de forma independiente de PRODUCTION:UPDATE.
--   idmodulo = 11 (xproduction) ; permiso bitmask = UPDATE(4)
--   resourceKey = menu.xproduction.production.disapprove (propio, traducible)
-- Orden de columnas: (id, codigo, descripcion, idmodulo, permiso, nombrerecurso, habilitado)
--
-- NOTA: solo se registra la funcionalidad. La asignacion de accesos
-- (derechoacceso) se hace por el panel de permisos del sistema.
-- ----------------------------------------------------------------------------
insert into funcionalidad
values (475, 'PRODUCTION_DISAPPROVE', 'Desaprobar orden de produccion',
        11, 4, 'menu.xproduction.production.disapprove', 1);

-- ----------------------------------------------------------------------------
-- Correccion: la funcionalidad PRODUCTION_DISAPPROVE se habia registrado con el
-- nombrerecurso generico 'menu.xproduction.production' (sin traduccion). Se le
-- asigna su propia clave para que el panel de permisos muestre el texto correcto.
-- UPDATE idempotente por codigo (corrige las BDs que ya ejecutaron el insert).
-- ----------------------------------------------------------------------------
update funcionalidad
   set nombrerecurso = 'menu.xproduction.production.disapprove'
 where codigo = 'PRODUCTION_DISAPPROVE';

-- ----------------------------------------------------------------------------
-- Normalizacion de claves de PRODUCTION_LAB_DATA / PRODUCTION_LABOR.
-- En v6.0.76 se insertaron con el nombrerecurso generico
-- 'menu.xproduction.production' (sin traduccion). Se les asigna su clave propia
-- para que el panel de permisos muestre el texto correcto tambien en BDs nuevas.
-- UPDATE idempotente por codigo.
-- ----------------------------------------------------------------------------
update funcionalidad
   set nombrerecurso = 'menu.xproduction.production.labData'
 where codigo = 'PRODUCTION_LAB_DATA';

update funcionalidad
   set nombrerecurso = 'menu.xproduction.production.labor'
 where codigo = 'PRODUCTION_LABOR';

-- ============================================================================
-- Permiso propio para la vista "Saldos" (Produccion > Saldos)
-- ============================================================================
--
-- Muestra los saldos de almacen (Materia Prima / Producto Terminado) recalculados
-- desde el origen de los movimientos. Funcionalidad propia para habilitar/ocultar
-- por rol el menu y la vista.
--   idmodulo = 11 (xproduction) ; permiso bitmask = VIEW(1)
--   resourceKey = Functionality.xproduction.balance  (panel de permisos: "Saldos MP/PT")
--   (el menu usa otra clave 'menu.xproduction.balance' = "Saldos")
-- Orden de columnas: (id, codigo, descripcion, idmodulo, permiso, nombrerecurso, habilitado)
--
-- NOTA: solo se registra la funcionalidad. La asignacion de accesos
-- (derechoacceso) se hace por el panel de permisos del sistema.
-- ----------------------------------------------------------------------------
insert into funcionalidad
values (476, 'XPRODUCTION_BALANCE', 'Saldos de almacen (Materia Prima / Producto Terminado)',
        11, 1, 'Functionality.xproduction.balance', 1);
