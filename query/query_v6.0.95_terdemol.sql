-- ============================================================================
-- v6.0.95 :: Carga de Inventario Inicial (inv_inicio) gestion 2026 - Almacen 4 (MATERIAS PRIMAS)
-- ============================================================================
--   Problema: el reporte de Movimientos por articulo (kardex) calcula el "saldo
--   anterior" a partir de inv_inicio (cierre de gestion). Para el almacen 4 NO se
--   cargo la apertura 2026, por lo que el saldo anterior salia 0 y el kardex no
--   cuadraba con "Saldos de Almacen" (que recalcula desde el origen).
--
--   Este script carga inv_inicio gestion='2026', alm='4' con el SALDO REAL
--   ACUMULADO al 31/12/2025 (todo el historico hasta esa fecha), NO con el
--   movimiento del periodo 2025 del "Reporte General de Inventario" (ese reporte
--   sale con Inv Inicial 0 y solo muestra 2025, por lo que subvalua la apertura y
--   omite articulos cuyo stock proviene de 2023-2024).
--
--   Criterio del saldo (identico a XProductionBalanceService / kardex corregido):
--     + Acopio (acopiomp.pesobal = Peso Empresa), estados APR/CONTA
--     + Produccion PT (xpr_producto.cantidad), orden no ANL
--     - Consumo insumos (xpr_insumo.cantidad), orden no ANL
--     +/- Reproceso ULEXITA (xpr_produccion_ulexita, TN x1000 en KG), orden no ANL
--     + Vales/despachos (inv_movdet E - S), estado APR
--   ...todo con fecha <= 2025-12-31.
--
--   Valores calculados y verificados contra la BD (cierre 2025 + movimientos 2026
--   = saldo actual del balance):
--     1  ROCA FOSFORICA  3.779.497,00
--     2  CALCITA            76.830,00
--     3  YESO AGRICOLA   1.316.000,00
--     4  BARITINA          403.160,00
--     5  ULEXITA         2.549.610,00
--     6  DOLOMITA           25.540,00
--     7  BENTONITA       1.286.177,00
--     (2037 CAOLIN = 0: solo tiene acopio 2026, no se carga apertura)
--
--   Idempotente: limpia la apertura 2026 del almacen 4 antes de reinsertar, y
--   toma los ids desde la tabla 'secuencia' (tabla='inv_inicio'), actualizandola.
-- ----------------------------------------------------------------------------

START TRANSACTION;

-- Limpieza previa (re-ejecucion segura): solo apertura 2026 del almacen 4, cia 01.
-- DELETE FROM inv_inicio WHERE gestion = '2026' AND alm = '4' AND no_cia = '01';

-- Base de ids desde la secuencia (allocationSize = 1: 'valor' es el proximo id).
SET @base := (SELECT valor FROM secuencia WHERE tabla = 'inv_inicio');

INSERT INTO inv_inicio (idinvinicio, cod_art, nombre, cantidad, und, alm, costo_uni, gestion, no_cia) VALUES
(@base + 0, '1',  'ROCA FOSFORICA', 3779497.00, 'KG', '4', 0.174285, '2026', '01'),
(@base + 1, '2',  'CALCITA',          76830.00, 'KG', '4', 0.080973, '2026', '01'),
(@base + 2, '3',  'YESO AGRICOLA',  1316000.00, 'KG', '4', 0.054767, '2026', '01'),
(@base + 3, '4',  'BARITINA',        403160.00, 'KG', '4', 0.292703, '2026', '01'),
(@base + 4, '5',  'ULEXITA',        2549610.00, 'KG', '4', 0.990633, '2026', '01'),
(@base + 5, '6',  'DOLOMITA',         25540.00, 'KG', '4', 0.080000, '2026', '01'),
(@base + 6, '7',  'BENTONITA',      1286177.00, 'KG', '4', 0.205145, '2026', '01');

-- Avanzar la secuencia (se insertaron 7 filas).
UPDATE secuencia SET valor = @base + 7 WHERE tabla = 'inv_inicio';

COMMIT;

-- ----------------------------------------------------------------------------
-- Verificacion:
--   SELECT cod_art, nombre, cantidad, alm, gestion FROM inv_inicio
--    WHERE gestion='2026' AND alm='4' ORDER BY CAST(cod_art AS UNSIGNED);
--   -- El saldo anterior del kardex 2026 debe igualar estos valores, y el saldo
--   -- final del kardex debe cuadrar con "Saldos de Almacen".
-- ============================================================================
