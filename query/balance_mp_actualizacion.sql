-- ============================================================================
-- ACTUALIZACION de saldos y valor del Almacen de Materias Primas (cod_alm = 4)
--   con COSTO PROMEDIO PONDERADO MOVIL (perpetuo), procesando los movimientos
--   en ORDEN CRONOLOGICO:
--     - En cada ENTRADA con valor propio (acopio / vale 'E'):
--           saldo  += cantidad
--           valor  += valor_neto_entrada
--           costo   = valor / saldo            <- nuevo costo promedio
--     - En cada SALIDA (vale 'S', consumo de produccion, consumo reproceso):
--           valor  -= cantidad * costo_vigente <- sale al promedio del momento
--           saldo  -= cantidad
--           costo   = se mantiene (valor y saldo bajan en proporcion)
--     - Entradas SIN costo de compra (PT producido, reproceso final): entran al
--       costo promedio vigente (suben saldo y valor, no cambian el costo).
--
--   Al final por articulo:
--       inv_inventario.saldo_uni        = saldo final
--       inv_inventario_detalle.cantidad = saldo final (solo si 1 fila de detalle)
--       inv_articulos.costo_uni         = costo promedio final
--       inv_articulos.saldo_mon         = valor final (neto)
--
--   NO se toman totales y se dividen: el costo se recalcula movimiento a
--   movimiento via un procedimiento con cursor.
--
-- ORDEN: PRIMERO validar cantidades (query/balance_mp_validacion.sql). Luego
--   correr ESTE script en DEV, revisar la Seccion 6, y recien aplicar Seccion 7.
--   Probar en DEV antes de produccion.
--
-- VALOR NETO de la ENTRADA de acopio (igual que la contabilizacion del acopio):
--     valor = (avg(pesoprov, pesobal)/1000) * precio * (tienefac=1 ? 0.87 : 1)
--     cantidad = pesoneto         (0.87 = 1 - VAT(0.13); precio = Bs/Tonelada)
--   Vale 'E': valor = inv_movdet.monto ; cantidad = inv_movdet.cantidad
--
-- FECHAS de ordenamiento:
--     acopio  -> acopiomp.fecha ;  kardex -> inv_movdet.fecha
--     produccion / reproceso -> xpr_produccion.fechainicio
--   Misma fecha: ENTRADAS antes que SALIDAS (para poder valorar la salida).
--
-- SUPUESTOS: una sola compania; inv_movdet.monto es valor neto de la entrada;
--   si un articulo tiene >1 fila en inv_inventario_detalle no se reparte (se
--   reporta). Articulos sin entradas valuadas quedan en costo 0.
-- ----------------------------------------------------------------------------

SET @alm := '4';

-- ----------------------------------------------------------------------------
-- 1) Tablas de trabajo (se recrean en cada corrida).
--    wrk_mov_mp  : flujo unificado de movimientos (a recorrer en orden).
--    wrk_costo_mp: resultado por articulo (saldo, valor y costo finales).
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS wrk_mov_mp;
CREATE TABLE wrk_mov_mp (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    no_cia    VARCHAR(2)    NOT NULL,
    cod_art   VARCHAR(6)    NOT NULL,
    fecha     DATE          NOT NULL,
    ord       INT           NOT NULL,          -- 1 = entrada, 2 = salida (mismo dia)
    clase     CHAR(2)       NOT NULL,          -- 'EV' entrada valuada, 'EC' entrada a costo, 'S' salida
    cantidad  DECIMAL(20,4) NOT NULL,
    valor_in  DECIMAL(20,6) NULL,              -- valor neto (solo 'EV')
    KEY ix_art (no_cia, cod_art, fecha, ord, id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS wrk_costo_mp;
CREATE TABLE wrk_costo_mp (
    no_cia        VARCHAR(2)    NOT NULL,
    cod_art       VARCHAR(6)    NOT NULL,
    saldo_new     DECIMAL(14,2) NULL,
    saldo_mon_new DECIMAL(20,2) NULL,
    costo_uni_new DECIMAL(16,6) NULL,
    detalle_rows  INT           NULL,
    neg_balance   INT           NULL,          -- 1 = el saldo corriente fue negativo (costo NO confiable)
    PRIMARY KEY (no_cia, cod_art)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2) Poblar el flujo de movimientos para los articulos del almacen 4 (VIG).
--    Universo de articulos: inv_articulos cod_alm=4, estado VIG.
-- ----------------------------------------------------------------------------

-- 2.a Acopio (ENTRADA valuada). cantidad = pesoneto ; valor = neto del acopio.
INSERT INTO wrk_mov_mp (no_cia, cod_art, fecha, ord, clase, cantidad, valor_in)
SELECT p.no_cia, p.cod_art, COALESCE(a.fecha, '1900-01-01'), 1, 'EV',
       COALESCE(a.pesoneto, 0),
       ROUND(((a.pesoprov + a.pesobal) / 2 / 1000) * a.precio
             * (CASE WHEN a.tienefac = 1 THEN 0.87 ELSE 1 END), 6)
FROM inv_articulos p
JOIN metaproductoproduccion mp ON mp.cod_art = p.cod_art
JOIN acopiomp a ON a.idmetaproductoproduccion = mp.idmetaproductoproduccion
WHERE p.cod_alm = @alm AND p.estado = 'VIG' AND a.estado <> 'ANL';

-- 2.b Kardex 'E' (ENTRADA valuada). valor = monto.
INSERT INTO wrk_mov_mp (no_cia, cod_art, fecha, ord, clase, cantidad, valor_in)
SELECT m.no_cia, m.cod_art, COALESCE(m.fecha, '1900-01-01'), 1, 'EV',
       COALESCE(m.cantidad, 0), COALESCE(m.monto, 0)
FROM inv_movdet m
JOIN inv_articulos p ON p.no_cia = m.no_cia AND p.cod_art = m.cod_art
WHERE p.cod_alm = @alm AND p.estado = 'VIG'
  AND m.cod_alm = @alm AND m.estado <> 'ANL' AND m.tipo_mov = 'E';

-- 2.c Kardex 'S' (SALIDA, al costo vigente).
INSERT INTO wrk_mov_mp (no_cia, cod_art, fecha, ord, clase, cantidad, valor_in)
SELECT m.no_cia, m.cod_art, COALESCE(m.fecha, '1900-01-01'), 2, 'S',
       COALESCE(m.cantidad, 0), NULL
FROM inv_movdet m
JOIN inv_articulos p ON p.no_cia = m.no_cia AND p.cod_art = m.cod_art
WHERE p.cod_alm = @alm AND p.estado = 'VIG'
  AND m.cod_alm = @alm AND m.estado <> 'ANL' AND m.tipo_mov = 'S';

-- 2.d Consumo de insumos en produccion (SALIDA, al costo vigente).
INSERT INTO wrk_mov_mp (no_cia, cod_art, fecha, ord, clase, cantidad, valor_in)
SELECT p.no_cia, p.cod_art, COALESCE(DATE(pr.fechainicio), '1900-01-01'), 2, 'S',
       COALESCE(xi.cantidad, 0), NULL
FROM inv_articulos p
JOIN xpr_insumo xi ON xi.cod_art = p.cod_art
JOIN xpr_produccion pr ON pr.idproduccion = xi.idproduccion
WHERE p.cod_alm = @alm AND p.estado = 'VIG' AND pr.estado <> 'ANL';

-- 2.e PT producido (ENTRADA a costo vigente). Para MP normalmente vacio.
INSERT INTO wrk_mov_mp (no_cia, cod_art, fecha, ord, clase, cantidad, valor_in)
SELECT p.no_cia, p.cod_art, COALESCE(DATE(pr.fechainicio), '1900-01-01'), 1, 'EC',
       COALESCE(xpp.cantidad, 0), NULL
FROM inv_articulos p
JOIN xpr_producto xpp ON xpp.cod_art = p.cod_art
JOIN xpr_produccion pr ON pr.idproduccion = xpp.idproduccion
WHERE p.cod_alm = @alm AND p.estado = 'VIG' AND pr.estado <> 'ANL';

-- 2.f Reproceso ULEXITA: final = ENTRADA a costo vigente ; consumo = SALIDA.
--     TN -> unidad del articulo (KG = x1000).
INSERT INTO wrk_mov_mp (no_cia, cod_art, fecha, ord, clase, cantidad, valor_in)
SELECT p.no_cia, p.cod_art, COALESCE(DATE(pr.fechainicio), '1900-01-01'), 1, 'EC',
       (CASE WHEN UPPER(TRIM(p.cod_med)) = 'KG' THEN COALESCE(u.reproceso_final_tn,0) * 1000
             ELSE COALESCE(u.reproceso_final_tn,0) END), NULL
FROM inv_articulos p
JOIN xpr_produccion_ulexita u ON u.cod_art_reproc_final = p.cod_art
JOIN xpr_produccion pr ON pr.idproduccion = u.idproduccion
WHERE p.cod_alm = @alm AND p.estado = 'VIG' AND pr.estado <> 'ANL'
  AND COALESCE(u.reproceso_final_tn,0) <> 0;

INSERT INTO wrk_mov_mp (no_cia, cod_art, fecha, ord, clase, cantidad, valor_in)
SELECT p.no_cia, p.cod_art, COALESCE(DATE(pr.fechainicio), '1900-01-01'), 2, 'S',
       (CASE WHEN UPPER(TRIM(p.cod_med)) = 'KG' THEN COALESCE(u.consumo_reproceso_tn,0) * 1000
             ELSE COALESCE(u.consumo_reproceso_tn,0) END), NULL
FROM inv_articulos p
JOIN xpr_produccion_ulexita u ON u.cod_art_reproc_final = p.cod_art
JOIN xpr_produccion pr ON pr.idproduccion = u.idproduccion
WHERE p.cod_alm = @alm AND p.estado = 'VIG' AND pr.estado <> 'ANL'
  AND COALESCE(u.consumo_reproceso_tn,0) <> 0;

-- ----------------------------------------------------------------------------
-- 3) Procedimiento: recorre wrk_mov_mp en orden y calcula el promedio movil.
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_costo_promedio_movil_mp;
DELIMITER $$
CREATE PROCEDURE sp_costo_promedio_movil_mp()
BEGIN
    DECLARE v_done   INT DEFAULT 0;
    DECLARE v_no_cia VARCHAR(2);
    DECLARE v_cod    VARCHAR(6);
    DECLARE v_clase  CHAR(2);
    DECLARE v_cant   DECIMAL(20,4);
    DECLARE v_valor  DECIMAL(20,6);

    DECLARE p_no_cia VARCHAR(2) DEFAULT NULL;
    DECLARE p_cod    VARCHAR(6) DEFAULT NULL;
    DECLARE r_qty    DECIMAL(24,6) DEFAULT 0;
    DECLARE r_val    DECIMAL(24,6) DEFAULT 0;
    DECLARE r_cost   DECIMAL(28,10) DEFAULT 0;
    DECLARE v_neg    INT DEFAULT 0;            -- marca si el saldo corriente fue negativo

    DECLARE cur CURSOR FOR
        SELECT no_cia, cod_art, clase, cantidad, valor_in
        FROM wrk_mov_mp
        ORDER BY no_cia, cod_art, fecha, ord, id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_no_cia, v_cod, v_clase, v_cant, v_valor;

        IF v_done = 1 THEN
            IF p_cod IS NOT NULL THEN
                INSERT INTO wrk_costo_mp (no_cia, cod_art, saldo_new, saldo_mon_new, costo_uni_new, neg_balance)
                VALUES (p_no_cia, p_cod, ROUND(r_qty,2), ROUND(r_val,2), ROUND(r_cost,6), v_neg);
            END IF;
            LEAVE read_loop;
        END IF;

        -- cambio de articulo: graba el anterior y reinicia acumuladores
        IF p_cod IS NULL OR v_cod <> p_cod OR v_no_cia <> p_no_cia THEN
            IF p_cod IS NOT NULL THEN
                INSERT INTO wrk_costo_mp (no_cia, cod_art, saldo_new, saldo_mon_new, costo_uni_new, neg_balance)
                VALUES (p_no_cia, p_cod, ROUND(r_qty,2), ROUND(r_val,2), ROUND(r_cost,6), v_neg);
            END IF;
            SET p_no_cia = v_no_cia, p_cod = v_cod, r_qty = 0, r_val = 0, r_cost = 0, v_neg = 0;
        END IF;

        -- aplica el movimiento
        IF v_clase = 'EV' THEN
            SET r_qty = r_qty + v_cant;
            SET r_val = r_val + COALESCE(v_valor, 0);
        ELSEIF v_clase = 'EC' THEN
            SET r_val = r_val + v_cant * r_cost;
            SET r_qty = r_qty + v_cant;
        ELSE  -- 'S'
            SET r_val = r_val - v_cant * r_cost;
            SET r_qty = r_qty - v_cant;
        END IF;

        -- recalcula costo promedio vigente
        IF r_qty > 0 THEN
            SET r_cost = r_val / r_qty;
        ELSE
            SET r_val = 0;
            SET r_cost = 0;
            IF r_qty < 0 THEN SET v_neg = 1; END IF;   -- sobreventa: costo no confiable
        END IF;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

-- ----------------------------------------------------------------------------
-- 4) Ejecutar el calculo.
-- ----------------------------------------------------------------------------
CALL sp_costo_promedio_movil_mp();

-- 4.b Cantidad de filas de detalle por articulo (para saber si se puede fijar
--     inv_inventario_detalle sin repartir).
UPDATE wrk_costo_mp w
LEFT JOIN ( SELECT no_cia, cod_alm, cod_art, COUNT(*) AS n
            FROM inv_inventario_detalle WHERE cod_alm = @alm
            GROUP BY no_cia, cod_alm, cod_art ) d
       ON d.no_cia = w.no_cia AND d.cod_art = w.cod_art
SET w.detalle_rows = COALESCE(d.n, 0);

-- ----------------------------------------------------------------------------
-- 5) Respaldo de valores ORIGINALES (idempotente, una sola vez).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bkp_v6090_balance_mp (
    no_cia        VARCHAR(2)    NOT NULL,
    cod_art       VARCHAR(6)    NOT NULL,
    saldo_old     DECIMAL(14,2) NULL,
    saldo_det_old DECIMAL(14,2) NULL,
    detalle_rows  INT           NULL,
    saldo_mon_old DECIMAL(20,6) NULL,
    costo_uni_old DECIMAL(16,6) NULL,
    saldo_new     DECIMAL(14,2) NULL,
    saldo_mon_new DECIMAL(20,2) NULL,
    costo_uni_new DECIMAL(16,6) NULL,
    neg_balance   INT           NULL,
    backed_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (no_cia, cod_art)
) ENGINE=InnoDB;

INSERT INTO bkp_v6090_balance_mp
    (no_cia, cod_art, saldo_old, saldo_det_old, detalle_rows,
     saldo_mon_old, costo_uni_old, saldo_new, saldo_mon_new, costo_uni_new, neg_balance)
SELECT w.no_cia, w.cod_art,
       inv.saldo_uni,
       COALESCE(det.cant_det, 0),
       w.detalle_rows,
       p.saldo_mon, p.costo_uni,
       w.saldo_new, w.saldo_mon_new, w.costo_uni_new, w.neg_balance
FROM wrk_costo_mp w
JOIN inv_articulos p ON p.no_cia = w.no_cia AND p.cod_art = w.cod_art
LEFT JOIN inv_inventario inv ON inv.no_cia = w.no_cia AND inv.cod_alm = @alm AND inv.cod_art = w.cod_art
LEFT JOIN ( SELECT no_cia, cod_alm, cod_art, SUM(cantidad) AS cant_det
            FROM inv_inventario_detalle WHERE cod_alm = @alm
            GROUP BY no_cia, cod_alm, cod_art ) det
       ON det.no_cia = w.no_cia AND det.cod_art = w.cod_art
WHERE NOT EXISTS (SELECT 1 FROM bkp_v6090_balance_mp b
                  WHERE b.no_cia = w.no_cia AND b.cod_art = w.cod_art);

-- ----------------------------------------------------------------------------
-- 6) REVISION (correr y revisar ANTES de aplicar la Seccion 7).
--    Compara viejo vs nuevo por articulo.
-- ----------------------------------------------------------------------------
SELECT b.cod_art,
       b.saldo_old, b.saldo_new,
       b.costo_uni_old, b.costo_uni_new,
       b.saldo_mon_old, b.saldo_mon_new,
       b.detalle_rows,
       b.neg_balance              -- 1 = saldo corriente fue negativo => costo NO confiable, revisar a mano
FROM bkp_v6090_balance_mp b
ORDER BY b.neg_balance DESC, ABS(COALESCE(b.saldo_mon_new,0) - COALESCE(b.saldo_mon_old,0)) DESC, b.cod_art;

-- ----------------------------------------------------------------------------
-- 7) APLICAR (tras revisar la Seccion 6).
-- ----------------------------------------------------------------------------

-- 7.a Saldo (cantidad) en inv_inventario.
UPDATE inv_inventario inv
JOIN wrk_costo_mp w ON w.no_cia = inv.no_cia AND w.cod_art = inv.cod_art
SET inv.saldo_uni = w.saldo_new
WHERE inv.cod_alm = @alm;

-- 7.b Cantidad en inv_inventario_detalle (solo articulos con 1 fila de detalle).
UPDATE inv_inventario_detalle d
JOIN wrk_costo_mp w ON w.no_cia = d.no_cia AND w.cod_art = d.cod_art
SET d.cantidad = w.saldo_new
WHERE d.cod_alm = @alm AND w.detalle_rows = 1;

-- 7.c costo_uni y saldo_mon (neto) en inv_articulos.
UPDATE inv_articulos p
JOIN wrk_costo_mp w ON w.no_cia = p.no_cia AND w.cod_art = p.cod_art
SET p.costo_uni = w.costo_uni_new,
    p.saldo_mon = w.saldo_mon_new
WHERE p.cod_alm = @alm;

-- ----------------------------------------------------------------------------
-- 8) VERIFICACION (opcional, 0 filas salvo la (d)).
-- ----------------------------------------------------------------------------
-- a) inv_inventario sincronizado:
-- SELECT inv.cod_art, inv.saldo_uni, w.saldo_new FROM inv_inventario inv
--   JOIN wrk_costo_mp w ON w.no_cia=inv.no_cia AND w.cod_art=inv.cod_art
--   WHERE inv.cod_alm=@alm AND ABS(inv.saldo_uni - w.saldo_new) > 0.01;
-- b) inv_articulos sincronizado:
-- SELECT p.cod_art, p.costo_uni, w.costo_uni_new, p.saldo_mon, w.saldo_mon_new
--   FROM inv_articulos p JOIN wrk_costo_mp w ON w.no_cia=p.no_cia AND w.cod_art=p.cod_art
--   WHERE p.cod_alm=@alm AND (ABS(p.costo_uni-w.costo_uni_new)>0.000001 OR ABS(p.saldo_mon-w.saldo_mon_new)>0.01);
-- c) saldo_mon = saldo * costo_uni (coherencia interna):
-- SELECT cod_art, saldo_new, costo_uni_new, saldo_mon_new,
--        ROUND(saldo_new*costo_uni_new,2) AS chk FROM wrk_costo_mp
--   WHERE ABS(saldo_mon_new - ROUND(saldo_new*costo_uni_new,2)) > 0.02;
-- d) Articulos con varias filas de detalle (NO actualizados, revisar a mano):
-- SELECT cod_art, detalle_rows, saldo_new FROM wrk_costo_mp WHERE detalle_rows <> 1;
-- e) Articulos con saldo corriente negativo en algun punto (costo NO confiable):
-- SELECT cod_art, saldo_new, costo_uni_new FROM wrk_costo_mp WHERE neg_balance = 1;

-- ----------------------------------------------------------------------------
-- 9) LIMPIEZA (opcional, tras validar):
-- DROP PROCEDURE IF EXISTS sp_costo_promedio_movil_mp;
-- DROP TABLE IF EXISTS wrk_mov_mp;
-- DROP TABLE IF EXISTS wrk_costo_mp;
-- DROP TABLE IF EXISTS bkp_v6090_balance_mp;   -- conserva el respaldo si dudas
-- ============================================================================
