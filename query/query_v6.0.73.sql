-- ============================================================================
-- v6.0.73 :: Indices para optimizar el Reporte de Inventario Extendido
--             y, en general, todas las consultas de kardex/inventario por
--             almacen y rango de fechas.
-- ============================================================================
--
-- Diagnostico: las tablas inv_movdet, inv_mov, inv_vales, inv_articulos,
-- inv_inicio e inv_periodo no tienen indices secundarios en las columnas
-- usadas por los WHERE/JOIN del reporte. EXPLAIN sobre la consulta principal
-- mostraba type=ALL en las tres tablas (full scan + hash join), y los
-- accesos por PK desde Hibernate (lazy load de InventoryMovement) tambien
-- caian en full scan porque inv_mov no tenia indice secundario alguno.
--
-- Estos indices son seguros (no cambian semantica), ocupan poco espacio
-- y se aplican rapido en tablas chicas (~25K filas).
-- ----------------------------------------------------------------------------

-- inv_movdet: filtros por almacen y joins por (no_cia, no_trans)
ALTER TABLE inv_movdet ADD INDEX idx_movdet_alm (cod_alm);
ALTER TABLE inv_movdet ADD INDEX idx_movdet_notrans (no_cia, no_trans);
ALTER TABLE inv_movdet ADD INDEX idx_movdet_codart (no_cia, cod_art);

-- inv_mov: lookup por (no_cia, no_trans) tanto desde joins como desde lazy load
ALTER TABLE inv_mov ADD INDEX idx_mov_notrans (no_cia, no_trans);

-- inv_vales: filtro por almacen + fecha (kardex), y por estado
ALTER TABLE inv_vales ADD INDEX idx_vales_alm_fecha (cod_alm, fecha);
ALTER TABLE inv_vales ADD INDEX idx_vales_estado (estado);

-- inv_articulos: findByWarehouseCode, findByGroupCode, findBySubGroupCode
ALTER TABLE inv_articulos ADD INDEX idx_articulos_alm (cod_alm);
ALTER TABLE inv_articulos ADD INDEX idx_articulos_grusub (no_cia, cod_gru, cod_sub);

-- inv_inicio: saldo inicial de gestion por almacen
ALTER TABLE inv_inicio ADD INDEX idx_inicio_alm_gest (alm, gestion);

-- inv_periodo: getInventoryPeriodInitialList (cod_alm + gestion + mes)
ALTER TABLE inv_periodo ADD INDEX idx_periodo_alm_gest (cod_alm, gestion, mes);
