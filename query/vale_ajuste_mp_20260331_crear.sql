-- ============================================================================
-- CREACION de un VALE DE SALIDA de AJUSTE (Materia Prima, cod_alm = 4), APROBADO,
-- con fecha 2026-03-31, para que la vista "Saldos de Almacen" (que RECALCULA el
-- saldo desde inv_movdet, no desde inv_inventario) coincida con el INVENTARIO
-- FISICO al 31/03/2026.
--
-- Motor: MySQL.  Ejecutar en el schema de PRODUCCION (terdemol).
-- Script STANDALONE: NO forma parte de las migraciones versionadas (query_v6.0.xx).
--
-- Que hace:
--   1) Inserta 1 cabecera en inv_vales (estado APR).
--   2) Inserta 1 movimiento en inv_mov  (estado APR).
--   3) Inserta 1 detalle por articulo en inv_movdet (estado APR, tipo_mov 'S').
--   4) Avanza las secuencias:  _sequence('VALE')  y  secuencia('inv_movdet').
--
-- Que NO hace (a proposito):
--   - NO toca inv_inventario / inv_inventario_detalle / inv_articulos. La vista
--     "Saldos de Almacen" NO los lee; recalcula desde inv_movdet. Si ademas se
--     quiere corregir el inventario clasico, ese es otro script
--     (query/balance_mp_actualizacion.sql).
--
-- Cantidad de salida por articulo = (saldo actual del sistema) - (fisico), en KG:
--     fisico en TN * 1000. Valores tomados de la pantalla al 31/03/2026.
--
--     cod_art  articulo          sistema(KG)   fisico(KG)     SALIDA(KG)
--     -------  ----------------  -----------   ----------   ------------
--        1     ROCA FOSFORICA      3.779.497       60.000      3.719.497
--        3     YESO AGRICOLA       1.316.000       30.820      1.285.180
--        4     BARITINA              830.410      151.520        678.890
--        5     ULEXITA             2.829.610            0      2.829.610   (fisico "-", se deja en 0)
--        7     BENTONITA           1.286.177      125.630      1.160.547
--
--   Excluidos: CALCITA (2), DOLOMITA (6), CAOLIN (2037) -> sin saldo fisico.
--              PIEDRA CALIZA -> fuera por indicacion.
--
-- Para RE-EJECUTAR: correr antes query/vale_ajuste_mp_20260331_eliminar.sql
--   (borra el vale por su marca no_vale) y volver a correr este.
-- ============================================================================

START TRANSACTION;

-- ----------------------------------------------------------------------------
-- Parametros
-- ----------------------------------------------------------------------------
SET @alm     := '4';                                  -- almacen Materia Prima
SET @fecha   := '2026-03-31';                         -- fecha del vale y de los movimientos
SET @no_vale := 'AJ-MP-20260331';                     -- MARCA para poder ubicar/eliminar el vale
SET @gloss   := 'Ajuste a inventario fisico MP al 31/03/2026';

-- Valores fijos (por indicacion):
SET @cia     := '01';        -- compania
SET @cod_doc := '0111';      -- tipo de documento de SALIDA (tipo_vale = 'S')
SET @idun    := 2;           -- unidad de negocio (idunidadnegocio)
SET @no_usr  := 'ADM';       -- inv_mov.no_usr (codigo de usuario)
SET @creado_por := 'admin';  -- inv_vales.created_by (login del usuario)

-- Centro de costo: se REUTILIZA de un vale de MP reciente. Fijarlo a mano si se
-- prefiere (SET @cod_cc := '...';).
SET @cod_cc := (SELECT v.cod_cc FROM inv_vales v
                WHERE v.cod_alm = @alm AND v.cod_cc IS NOT NULL
                ORDER BY CAST(v.no_trans AS UNSIGNED) DESC LIMIT 1);

-- ----------------------------------------------------------------------------
-- Guarda anti-duplicado: @crear = 1 solo si el vale (por su marca) NO existe.
-- Todos los INSERT/UPDATE de abajo estan condicionados a @crear = 1, asi que
-- re-ejecutar sin haber borrado NO crea un segundo vale (no hace nada).
-- ----------------------------------------------------------------------------
SET @crear := (SELECT IF(COUNT(*) = 0, 1, 0) FROM inv_vales WHERE no_vale = @no_vale);
SELECT IF(@crear = 1,
          'OK: el vale no existe, se procede a crearlo',
          'AVISO: el vale ya existe. No se hara nada. Corra primero el script de ELIMINAR.') AS guarda;

-- ----------------------------------------------------------------------------
-- Items a ajustar (cod_art -> cantidad de SALIDA en KG)
-- ----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_ajuste_mp;
CREATE TEMPORARY TABLE tmp_ajuste_mp (
    cod_art VARCHAR(6)     NOT NULL PRIMARY KEY,
    salida  DECIMAL(19,2)  NOT NULL
);
INSERT INTO tmp_ajuste_mp (cod_art, salida) VALUES
    ('1', 3719497.00),   -- ROCA FOSFORICA
    ('3', 1285180.00),   -- YESO AGRICOLA
    ('4',  678890.00),   -- BARITINA
    ('5', 2829610.00),   -- ULEXITA  (fisico "-", se deja en 0)
    ('7', 1160547.00);   -- BENTONITA

-- ----------------------------------------------------------------------------
-- Numero de transaccion (no_trans): siguiente de la secuencia 'VALE',
-- garantizando que sea mayor que el MAX(no_trans) existente.
-- ----------------------------------------------------------------------------
SET @no_trans := (SELECT GREATEST(
        COALESCE((SELECT seq_val FROM _sequence WHERE seq_name = 'VALE'), 0),
        COALESCE((SELECT MAX(CAST(no_trans AS UNSIGNED)) FROM inv_vales), 0)
    ) + 1);

-- ----------------------------------------------------------------------------
-- Base de id_inv_movdet: convencion Hibernate (secuencia.valor = proximo id a
-- asignar). Se toma el mayor entre la secuencia y MAX(id)+1 para no colisionar.
-- ----------------------------------------------------------------------------
SET @movdet_next := (SELECT GREATEST(
        COALESCE((SELECT valor FROM secuencia WHERE tabla = 'inv_movdet'), 1),
        COALESCE((SELECT MAX(id_inv_movdet) FROM inv_movdet), 0) + 1
    ));
SET @N := (SELECT COUNT(*) FROM tmp_ajuste_mp);

-- ----------------------------------------------------------------------------
-- 1) Cabecera del vale (inv_vales), APROBADO.  (solo si @crear = 1)
-- ----------------------------------------------------------------------------
INSERT INTO inv_vales
    (no_cia, no_trans, cod_doc, no_vale, fecha, estado, cod_cc, cod_alm,
     idunidadnegocio, baja, fechacreacion, created_at, created_by, version)
SELECT @cia, @no_trans, @cod_doc, @no_vale, @fecha, 'APR', @cod_cc, @alm,
       @idun, 0, NOW(), NOW(), @creado_por, 0
FROM (SELECT 1) x
WHERE @crear = 1;

-- ----------------------------------------------------------------------------
-- 2) Movimiento (inv_mov), APROBADO. PK = (no_cia, no_trans, estado).
-- ----------------------------------------------------------------------------
INSERT INTO inv_mov
    (no_cia, no_trans, estado, fecha_mov, fecha_cre, descri, no_usr, tipo_compro, version)
SELECT @cia, @no_trans, 'APR', @fecha, @fecha, @gloss, @no_usr, 'S', 0
FROM (SELECT 1) x
WHERE @crear = 1;

-- ----------------------------------------------------------------------------
-- 3) Detalles (inv_movdet), APROBADOS, tipo_mov 'S'. Un id correlativo por fila.
--    cod_med, cuenta_art y costo se toman del articulo (inv_articulos).
--    monto es informativo (la vista de saldos solo suma 'cantidad').
-- ----------------------------------------------------------------------------
SET @rn := 0;
INSERT INTO inv_movdet
    (id_inv_movdet, no_cia, no_trans, estado, cod_alm, cod_art, tipo_mov,
     cantidad, cod_med, cuenta_art, costounitario, monto, fecha, idunidadnegocio, version)
SELECT
    @movdet_next + (@rn := @rn + 1) - 1,
    @cia, @no_trans, 'APR', @alm, t.cod_art, 'S',
    t.salida, a.cod_med, a.cuenta_art,
    COALESCE(a.costo_uni, 0),
    ROUND(t.salida * COALESCE(a.costo_uni, 0), 6),
    @fecha, @idun, 0
FROM tmp_ajuste_mp t
JOIN inv_articulos a ON a.no_cia = @cia AND a.cod_art = t.cod_art
WHERE @crear = 1
ORDER BY CAST(t.cod_art AS UNSIGNED);

-- ----------------------------------------------------------------------------
-- 4) Avanzar secuencias.  (solo si @crear = 1)
-- ----------------------------------------------------------------------------
-- 4.a  _sequence('VALE') -> ultimo no_trans emitido.
UPDATE _sequence SET seq_val = @no_trans WHERE seq_name = 'VALE' AND @crear = 1;
INSERT INTO _sequence (seq_name, seq_val)
SELECT 'VALE', @no_trans
WHERE @crear = 1 AND NOT EXISTS (SELECT 1 FROM _sequence WHERE seq_name = 'VALE');

-- 4.b  secuencia('inv_movdet') -> proximo id a asignar por Hibernate.
UPDATE secuencia SET valor = @movdet_next + @N WHERE tabla = 'inv_movdet' AND @crear = 1;
INSERT INTO secuencia (tabla, valor)
SELECT 'inv_movdet', @movdet_next + @N
WHERE @crear = 1 AND NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'inv_movdet');

-- ----------------------------------------------------------------------------
-- Revision antes de confirmar
-- ----------------------------------------------------------------------------
SELECT @cia AS no_cia, @no_trans AS no_trans, @cod_doc AS cod_doc,
       @cod_cc AS cod_cc, @idun AS idunidadnegocio, @no_usr AS no_usr;

SELECT d.cod_art, a.descri AS articulo, d.tipo_mov, d.cantidad, d.cod_med, d.estado, d.fecha
FROM inv_movdet d
JOIN inv_articulos a ON a.no_cia = d.no_cia AND a.cod_art = d.cod_art
WHERE d.no_cia = @cia AND d.no_trans = @no_trans
ORDER BY CAST(d.cod_art AS UNSIGNED);

DROP TEMPORARY TABLE IF EXISTS tmp_ajuste_mp;

-- Revisar los SELECT de arriba. Si todo esta correcto:
COMMIT;
-- Si algo esta mal:  ROLLBACK;
-- ============================================================================
