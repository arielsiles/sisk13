-- ============================================================================
-- CREACION de DOS VALES DE AJUSTE (Producto Terminado, cod_alm = 3), APROBADOS,
-- fecha 2026-03-31, para que la vista "Saldos de Almacen" (que RECALCULA el
-- saldo desde inv_movdet, no desde inv_inventario) coincida con el saldo
-- deseado al 31/03/2026.
--
--   1 VALE DE SALIDA  (tipo_mov 'S')  -> baja saldos
--   1 VALE DE ENTRADA (tipo_mov 'E')  -> sube saldos
--
-- Motor: MySQL.  Ejecutar en el schema de PRODUCCION (terdemol).
-- Script STANDALONE: NO forma parte de las migraciones versionadas (query_v6.0.xx).
--
-- Que hace:
--   1) Inserta 2 cabeceras en inv_vales (estado APR): una S y una E.
--   2) Inserta 2 movimientos en inv_mov (estado APR): tipo_compro 'S' y 'E'.
--   3) Inserta 3 detalles en inv_movdet (estado APR): 1 de salida + 2 de entrada.
--   4) Avanza las secuencias:  _sequence('VALE')  y  secuencia('inv_movdet').
--
-- Que NO hace (a proposito):
--   - NO toca inv_inventario / inv_inventario_detalle / inv_articulos. La vista
--     "Saldos de Almacen" NO los lee; recalcula desde inv_movdet.
--
-- Ajuste por articulo (unidad KG; TN * 1000). Saldos tomados de la pantalla
-- "Saldos de Almacen" al 31/03/2026:
--
--   vale  cod_art  articulo          sistema(KG)   deseado      MOV   cantidad(KG)
--   ----  -------  ---------------   -----------   ---------   ----   ------------
--    S     1255    BORO 10 - A        8.482.000     0 TN       'S'      8.482.000
--    E     2021    BARITINA MOLIDA            0     140 TN     'E'        140.000
--    E     1261    BORO 10 - B                0     26 TN      'E'         26.000
--
-- Para RE-EJECUTAR: correr antes query/vale_ajuste_pt_20260331_eliminar.sql
--   (borra ambos vales por sus marcas no_vale) y volver a correr este.
-- ============================================================================

START TRANSACTION;

-- ----------------------------------------------------------------------------
-- Parametros
-- ----------------------------------------------------------------------------
SET @alm      := '3';                                 -- almacen Producto Terminado
SET @fecha    := '2026-03-31';                        -- fecha de los vales y movimientos
SET @no_vale_s := 'AJ-PT-S-20260331';                 -- MARCA del vale de SALIDA
SET @no_vale_e := 'AJ-PT-E-20260331';                 -- MARCA del vale de ENTRADA
SET @gloss_s  := 'Ajuste de saldo PT (salida) al 31/03/2026';
SET @gloss_e  := 'Ajuste de saldo PT (entrada) al 31/03/2026';

-- Valores fijos (por indicacion, mismos que el ajuste MP):
SET @cia      := '01';        -- compania
SET @idun     := 2;           -- unidad de negocio (idunidadnegocio)
SET @no_usr   := 'ADM';       -- inv_mov.no_usr (codigo de usuario)
SET @creado_por := 'admin';   -- inv_vales.created_by (login del usuario)

-- Tipo de documento (cod_doc, FK a inv_tipodocs):
--   SALIDA  -> 'EGR' (EGRESO)
--   ENTRADA -> 'REC' (RECEPCION)
SET @cod_doc_s := 'EGR';
SET @cod_doc_e := 'REC';

-- Centro de costo: se REUTILIZA de un vale reciente del almacen PT.
SET @cod_cc := (SELECT v.cod_cc FROM inv_vales v
                WHERE v.cod_alm = @alm AND v.cod_cc IS NOT NULL
                ORDER BY CAST(v.no_trans AS UNSIGNED) DESC LIMIT 1);

-- ----------------------------------------------------------------------------
-- Guarda anti-duplicado: @crear = 1 solo si NINGUNA de las dos marcas existe.
-- Re-ejecutar sin haber borrado NO crea vales duplicados (no hace nada).
-- ----------------------------------------------------------------------------
SET @crear := (SELECT IF(COUNT(*) = 0, 1, 0) FROM inv_vales WHERE no_vale IN (@no_vale_s, @no_vale_e));
SELECT IF(@crear = 1,
          'OK: los vales no existen, se procede a crearlos',
          'AVISO: ya existe alguna marca. No se hara nada. Corra primero el script de ELIMINAR.') AS guarda;

-- ----------------------------------------------------------------------------
-- Numeros de transaccion (no_trans): dos siguientes de la secuencia 'VALE',
-- garantizando que sean mayores que el MAX(no_trans) existente.
-- ----------------------------------------------------------------------------
SET @no_trans_s := (SELECT GREATEST(
        COALESCE((SELECT seq_val FROM _sequence WHERE seq_name = 'VALE'), 0),
        COALESCE((SELECT MAX(CAST(no_trans AS UNSIGNED)) FROM inv_vales), 0)
    ) + 1);
SET @no_trans_e := @no_trans_s + 1;

-- ----------------------------------------------------------------------------
-- Base de id_inv_movdet: convencion Hibernate (secuencia.valor = proximo id a
-- asignar). Se toma el mayor entre la secuencia y MAX(id)+1 para no colisionar.
-- Se crean 3 detalles (@N = 3).
-- ----------------------------------------------------------------------------
SET @movdet_next := (SELECT GREATEST(
        COALESCE((SELECT valor FROM secuencia WHERE tabla = 'inv_movdet'), 1),
        COALESCE((SELECT MAX(id_inv_movdet) FROM inv_movdet), 0) + 1
    ));
SET @N := 3;

-- ----------------------------------------------------------------------------
-- Detalles a crear: (seq -> orden de id, no_trans, tipo_mov, cod_art, cantidad)
-- ----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_ajuste_pt;
CREATE TEMPORARY TABLE tmp_ajuste_pt (
    seq      INT           NOT NULL PRIMARY KEY,
    no_trans BIGINT        NOT NULL,
    tipo_mov VARCHAR(1)    NOT NULL,
    cod_art  VARCHAR(6)    NOT NULL,
    cantidad DECIMAL(19,2) NOT NULL
);
INSERT INTO tmp_ajuste_pt (seq, no_trans, tipo_mov, cod_art, cantidad) VALUES
    (1, @no_trans_s, 'S', '1255', 8482000.00),   -- BORO 10 - A    (salida)
    (2, @no_trans_e, 'E', '2021',  140000.00),   -- BARITINA MOLIDA (entrada)
    (3, @no_trans_e, 'E', '1261',   26000.00);   -- BORO 10 - B    (entrada)

-- ----------------------------------------------------------------------------
-- 1) Cabeceras de los vales (inv_vales), APROBADAS.  (solo si @crear = 1)
--    Un INSERT por vale con referencia directa a las variables (evita que un
--    UNION de variables de usuario corrompa el tipo de no_trans -> VARCHAR(10)).
-- ----------------------------------------------------------------------------
INSERT INTO inv_vales
    (no_cia, no_trans, cod_doc, no_vale, fecha, estado, cod_cc, cod_alm,
     idunidadnegocio, baja, fechacreacion, created_at, created_by, version)
SELECT @cia, @no_trans_s, @cod_doc_s, @no_vale_s, @fecha, 'APR', @cod_cc, @alm,
       @idun, 0, NOW(), NOW(), @creado_por, 0
FROM (SELECT 1) x
WHERE @crear = 1;

INSERT INTO inv_vales
    (no_cia, no_trans, cod_doc, no_vale, fecha, estado, cod_cc, cod_alm,
     idunidadnegocio, baja, fechacreacion, created_at, created_by, version)
SELECT @cia, @no_trans_e, @cod_doc_e, @no_vale_e, @fecha, 'APR', @cod_cc, @alm,
       @idun, 0, NOW(), NOW(), @creado_por, 0
FROM (SELECT 1) x
WHERE @crear = 1;

-- ----------------------------------------------------------------------------
-- 2) Movimientos (inv_mov), APROBADOS. PK = (no_cia, no_trans, estado).
-- ----------------------------------------------------------------------------
INSERT INTO inv_mov
    (no_cia, no_trans, estado, fecha_mov, fecha_cre, descri, no_usr, tipo_compro, version)
SELECT @cia, @no_trans_s, 'APR', @fecha, @fecha, @gloss_s, @no_usr, 'S', 0
FROM (SELECT 1) x
WHERE @crear = 1;

INSERT INTO inv_mov
    (no_cia, no_trans, estado, fecha_mov, fecha_cre, descri, no_usr, tipo_compro, version)
SELECT @cia, @no_trans_e, 'APR', @fecha, @fecha, @gloss_e, @no_usr, 'E', 0
FROM (SELECT 1) x
WHERE @crear = 1;

-- ----------------------------------------------------------------------------
-- 3) Detalles (inv_movdet), APROBADOS. Un id correlativo por fila (@movdet_next
--    + seq - 1). cod_med, cuenta_art y costo se toman del articulo. monto es
--    informativo (la vista de saldos solo suma 'cantidad').
-- ----------------------------------------------------------------------------
INSERT INTO inv_movdet
    (id_inv_movdet, no_cia, no_trans, estado, cod_alm, cod_art, tipo_mov,
     cantidad, cod_med, cuenta_art, costounitario, monto, fecha, idunidadnegocio, version)
SELECT
    @movdet_next + t.seq - 1,
    @cia, t.no_trans, 'APR', @alm, t.cod_art, t.tipo_mov,
    t.cantidad, a.cod_med, a.cuenta_art,
    COALESCE(a.costo_uni, 0),
    ROUND(t.cantidad * COALESCE(a.costo_uni, 0), 6),
    @fecha, @idun, 0
FROM tmp_ajuste_pt t
JOIN inv_articulos a ON a.no_cia = @cia AND a.cod_art = t.cod_art
WHERE @crear = 1
ORDER BY t.seq;

-- ----------------------------------------------------------------------------
-- 4) Avanzar secuencias.  (solo si @crear = 1)
-- ----------------------------------------------------------------------------
-- 4.a  _sequence('VALE') -> ultimo no_trans emitido (@no_trans_e).
UPDATE _sequence SET seq_val = @no_trans_e WHERE seq_name = 'VALE' AND @crear = 1;
INSERT INTO _sequence (seq_name, seq_val)
SELECT 'VALE', @no_trans_e
WHERE @crear = 1 AND NOT EXISTS (SELECT 1 FROM _sequence WHERE seq_name = 'VALE');

-- 4.b  secuencia('inv_movdet') -> proximo id a asignar por Hibernate.
UPDATE secuencia SET valor = @movdet_next + @N WHERE tabla = 'inv_movdet' AND @crear = 1;
INSERT INTO secuencia (tabla, valor)
SELECT 'inv_movdet', @movdet_next + @N
WHERE @crear = 1 AND NOT EXISTS (SELECT 1 FROM secuencia WHERE tabla = 'inv_movdet');

-- ----------------------------------------------------------------------------
-- Revision antes de confirmar
-- ----------------------------------------------------------------------------
SELECT @cia AS no_cia, @no_trans_s AS no_trans_salida, @cod_doc_s AS cod_doc_s,
       @no_trans_e AS no_trans_entrada, @cod_doc_e AS cod_doc_e,
       @cod_cc AS cod_cc, @idun AS idunidadnegocio, @no_usr AS no_usr;

SELECT d.no_trans, d.cod_art, a.descri AS articulo, d.tipo_mov, d.cantidad,
       d.cod_med, d.estado, d.fecha
FROM inv_movdet d
JOIN inv_articulos a ON a.no_cia = d.no_cia AND a.cod_art = d.cod_art
WHERE d.no_cia = @cia AND d.no_trans IN (@no_trans_s, @no_trans_e)
ORDER BY d.no_trans, CAST(d.cod_art AS UNSIGNED);

DROP TEMPORARY TABLE IF EXISTS tmp_ajuste_pt;

-- Revisar los SELECT de arriba. Si todo esta correcto:
COMMIT;
-- Si algo esta mal:  ROLLBACK;
-- ============================================================================


-- ============================================================================
-- CORRECCION GENERAL: fecha de los DESPACHOS en inv_movdet
-- ----------------------------------------------------------------------------
-- PROBLEMA: los despachos (inv_vales.cod_doc = 'DSP') guardan en inv_movdet.fecha
-- (movementDetailDate) la fecha en que se APROBARON, no la fecha real del despacho
-- (inv_vales.fecha). Por eso las dos pantallas no cuadran:
--     * Kardex de Articulos  filtra por inv_vales.fecha  (fecha real)  -> correcto
--     * Saldos de Almacen    filtra por inv_movdet.fecha (fecha aprob.) -> a un corte
--       anterior a la fecha de aprobacion NO descuenta el despacho (saldo inflado).
--
-- SOLUCION: alinear inv_movdet.fecha = inv_vales.fecha en TODOS los despachos.
--
-- Alcance: GENERAL. Toma todos los 'DSP' sin filtro de fecha ni de articulo, de
--   cualquier compania y estado. Reutilizable si aparecen nuevos despachos con el
--   mismo desfase. Idempotente: solo actualiza las filas donde la fecha difiere;
--   re-ejecutar cuando ya estan alineadas no cambia nada.
--
-- NO afecta a los vales de ajuste creados arriba (su fecha de detalle ya coincide
--   con la del vale) ni a otros tipos de documento (p.ej. EGR).
--
-- Motor: MySQL. Ejecutar en el schema de PRODUCCION (terdemol).
-- ============================================================================

START TRANSACTION;

-- Previsualizacion: cuantas filas se van a corregir (por estado del detalle).
SELECT d.estado, COUNT(*) AS filas_a_corregir
FROM inv_movdet d
JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
WHERE v.cod_doc = 'DSP' AND d.fecha <> v.fecha
GROUP BY d.estado;

-- Correccion.
UPDATE inv_movdet d
JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
SET d.fecha = v.fecha
WHERE v.cod_doc = 'DSP' AND d.fecha <> v.fecha;

-- Verificacion: debe quedar 0 (ningun despacho con fecha de detalle desalineada).
SELECT COUNT(*) AS pendientes_despues
FROM inv_movdet d
JOIN inv_vales v ON v.no_cia = d.no_cia AND v.no_trans = d.no_trans
WHERE v.cod_doc = 'DSP' AND d.fecha <> v.fecha;

-- Revisar: 'pendientes_despues' = 0. Si todo esta correcto:
COMMIT;
-- Si algo esta mal:  ROLLBACK;
-- ============================================================================


-- ============================================================================
-- Vale de ajuste REC (recepcion/entrada) BENTONITA PROCESADA (1573), almacen 3, 24/03/2026. Simple, sin temporales; idempotente por no_vale.
START TRANSACTION;

SET @cia:='01', @alm:='3', @fecha:='2026-03-24', @cod_art:='1573', @cant:=72633.00, @costo:=0.205148,
    @no_vale:='AJ-PT-E-20260324', @gloss:='Ajuste recepcion BENTONITA PROCESADA 24/03/2026',
    @idun:=2, @no_usr:='ADM', @creado_por:='admin';
SET @cod_cc := (SELECT cod_cc FROM inv_vales WHERE cod_alm=@alm AND cod_cc IS NOT NULL ORDER BY CAST(no_trans AS UNSIGNED) DESC LIMIT 1);
SET @crear := (SELECT IF(COUNT(*)=0,1,0) FROM inv_vales WHERE no_vale=@no_vale);
SET @no_trans := (SELECT GREATEST(COALESCE((SELECT seq_val FROM _sequence WHERE seq_name='VALE'),0), COALESCE((SELECT MAX(CAST(no_trans AS UNSIGNED)) FROM inv_vales),0))+1);
SET @movdet_next := (SELECT GREATEST(COALESCE((SELECT valor FROM secuencia WHERE tabla='inv_movdet'),1), COALESCE((SELECT MAX(id_inv_movdet) FROM inv_movdet),0)+1));

INSERT INTO inv_vales (no_cia,no_trans,cod_doc,no_vale,fecha,estado,cod_cc,cod_alm,idunidadnegocio,baja,fechacreacion,created_at,created_by,version)
SELECT @cia,@no_trans,'REC',@no_vale,@fecha,'APR',@cod_cc,@alm,@idun,0,NOW(),NOW(),@creado_por,0 FROM (SELECT 1) x WHERE @crear=1;

INSERT INTO inv_mov (no_cia,no_trans,estado,fecha_mov,fecha_cre,descri,no_usr,tipo_compro,version)
SELECT @cia,@no_trans,'APR',@fecha,@fecha,@gloss,@no_usr,'E',0 FROM (SELECT 1) x WHERE @crear=1;

INSERT INTO inv_movdet (id_inv_movdet,no_cia,no_trans,estado,cod_alm,cod_art,tipo_mov,cantidad,cod_med,cuenta_art,costounitario,monto,fecha,idunidadnegocio,version)
SELECT @movdet_next,@cia,@no_trans,'APR',@alm,@cod_art,'E',@cant,a.cod_med,a.cuenta_art,@costo,ROUND(@cant*@costo,6),@fecha,@idun,0
FROM inv_articulos a WHERE a.no_cia=@cia AND a.cod_art=@cod_art AND @crear=1;

UPDATE _sequence SET seq_val=@no_trans WHERE seq_name='VALE' AND @crear=1;
UPDATE secuencia SET valor=@movdet_next+1 WHERE tabla='inv_movdet' AND @crear=1;

SELECT d.no_trans,d.cod_art,a.descri,d.tipo_mov,d.cantidad,d.costounitario,d.monto,d.estado,d.fecha
FROM inv_movdet d JOIN inv_articulos a ON a.no_cia=d.no_cia AND a.cod_art=d.cod_art WHERE d.no_cia=@cia AND d.no_trans=@no_trans;

COMMIT;
-- ROLLBACK si algo esta mal.
-- ============================================================================