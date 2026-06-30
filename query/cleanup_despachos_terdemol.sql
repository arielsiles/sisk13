-- ============================================================================
-- LIMPIEZA DE DESPACHOS (solo DEV / PRUEBAS)
-- ============================================================================
--
--  Borra TODOS los despachos (cabeceras, detalles, envases) y reinicia el
--  N de Orden de Entrega para que el proximo despacho empiece en 1.
--
--  >>> HACER RESPALDO ANTES. Es destructivo e irreversible. <<<
--
--  Tablas involucradas:
--    inv_valedespacho         - cabecera del despacho
--    inv_valedespacho_det     - detalle (FK -> cabecera, ON DELETE CASCADE)
--    inv_valedespacho_envase  - envases (FK -> cabecera y detalle)
--    inv_vales / inv_mov / inv_movdet - vale de egreso generado al aprobar
--    gensecuencia             - secuencia 'DISPATCH_ORDER_NUMBER' (N de Orden)
--
--  ELEGIR UNA de las dos opciones (NO correr ambas):
--    OPCION A: solo despachos + reinicio de orden.
--    OPCION B: despachos + vale de egreso generado (reset limpio). RECOMENDADA.
--
--  Notas:
--   - Al APROBAR, cada despacho crea un vale de egreso (inv_vales + inv_mov +
--     inv_movdet). Si solo se borran los despachos (Opcion A), esos vales
--     quedan huerfanos y siguen apareciendo en el modulo de Vales. La Opcion B
--     los elimina (solo los del despacho, sin tocar compras/produccion/transf.).
--   - Borrar inv_mov/inv_movdet NO restaura saldos de inv_inventario /
--     inv_articulos. En carga retroactiva con stock en 0 es indiferente; si
--     hubo aprobaciones en modo ON que movieron stock, reajustar saldos aparte.
--   - En la configuracion actual el despacho NO genera asiento contable, asi
--     que no hay comprobantes que limpiar.
-- ============================================================================


-- ============================================================================
-- OPCION A -- Solo despachos + reinicio del N de Orden de Entrega
-- ============================================================================
-- START TRANSACTION;
--
-- DELETE FROM inv_valedespacho_envase;   -- envases
-- DELETE FROM inv_valedespacho_det;      -- detalles
-- DELETE FROM inv_valedespacho;          -- cabeceras (despachos)
--
-- DELETE FROM gensecuencia WHERE nombre = 'DISPATCH_ORDER_NUMBER';
--   -- (si solo una empresa:  ... AND idcompania = <id>)
--
-- COMMIT;
--
-- -- Opcional: que los IDs internos tambien arranquen en 1
-- ALTER TABLE inv_valedespacho_envase AUTO_INCREMENT = 1;
-- ALTER TABLE inv_valedespacho_det    AUTO_INCREMENT = 1;
-- ALTER TABLE inv_valedespacho        AUTO_INCREMENT = 1;


-- ============================================================================
-- OPCION B -- Despachos + vale de egreso generado (RESET LIMPIO, recomendada)
-- ============================================================================
START TRANSACTION;

-- 1) Capturar las claves de los vales de egreso generados por los despachos
CREATE TEMPORARY TABLE tmp_vales_desp AS
SELECT DISTINCT no_cia_vale AS no_cia, no_trans_vale AS no_trans
FROM inv_valedespacho
WHERE no_trans_vale IS NOT NULL;

-- 2) Borrar el despacho (envases -> detalles -> cabeceras)
DELETE FROM inv_valedespacho_envase;
DELETE FROM inv_valedespacho_det;
DELETE FROM inv_valedespacho;

-- 3) Borrar movimientos y vales de egreso SOLO de esos despachos
DELETE md FROM inv_movdet md
    JOIN tmp_vales_desp t ON md.no_cia = t.no_cia AND md.no_trans = t.no_trans;
DELETE m FROM inv_mov m
    JOIN tmp_vales_desp t ON m.no_cia = t.no_cia AND m.no_trans = t.no_trans;
DELETE v FROM inv_vales v
    JOIN tmp_vales_desp t ON v.no_cia = t.no_cia AND v.no_trans = t.no_trans;

DROP TEMPORARY TABLE tmp_vales_desp;

-- 4) Reiniciar el N de Orden de Entrega (proximo despacho vuelve a 1)
DELETE FROM gensecuencia WHERE nombre = 'DISPATCH_ORDER_NUMBER';
--   (si solo una empresa:  ... AND idcompania = <id>)

COMMIT;

-- Opcional: que los IDs internos tambien arranquen en 1
ALTER TABLE inv_valedespacho_envase AUTO_INCREMENT = 1;
ALTER TABLE inv_valedespacho_det    AUTO_INCREMENT = 1;
ALTER TABLE inv_valedespacho        AUTO_INCREMENT = 1;
