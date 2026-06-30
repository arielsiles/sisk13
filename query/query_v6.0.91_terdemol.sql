-- ============================================================================
-- v6.0.91 :: ULEXITA - Bajar el COSTO UNITARIO corregido de ULEXITA (cod_art 5)
--             a las ordenes de produccion aprobadas, con COSTO PROMEDIO MOVIL
--             A LA FECHA de cada orden, y recalcular el costeo.
-- ============================================================================
--
-- CONTEXTO
--   El costo del acopio se valuaba mal (precio/100 y /0.87) -> inflaba el
--   costo_uni de ULEXITA en inv_articulos ~13x. Cada orden de produccion, al
--   aprobarse, copio ese costo inflado al insumo (xpr_insumo.costouni de
--   '5-ULEXITA'). Hoy las 264 ordenes APR tienen costouni entre 1,29 y 13,44.
--
--   Ya se corrigio el costo en inventario (balance_mp_*) y el codigo del acopio.
--   Ahora hay que bajar a cada orden el costo CORRECTO de ULEXITA *a la fecha en
--   que esa orden se produjo* (costo promedio movil del kardex de ULEXITA), no un
--   valor unico, y recalcular el costeo de la orden.
--
-- CRITERIO (acordado)
--   * Costo = PROMEDIO MOVIL PONDERADO de ULEXITA recorrido en orden cronologico
--     (acopio neto entra; consumo/vale sale al promedio vigente). En cada CONSUMO
--     de una orden se toma el costo vigente en esa fecha.
--   * Si el saldo corriente se hace negativo (estacionalidad feb-may 2025), el
--     costo NO se pone en 0: se ARRASTRA el ultimo costo valido (carry-forward).
--   * Solo se corrige el insumo ULEXITA (cod_art 5, inputDefault). El resto de
--     insumos de la orden no se tocan.
--
-- VALOR NETO de la entrada de acopio (igual que la contabilizacion / balance):
--     valor = (avg(pesoprov, pesobal)/1000) * precio * (tienefac=1 ? 0.87 : 1)
--     cantidad = pesoneto      (precio = Bs/Tonelada; 0.87 = 1 - VAT)
--
-- ALCANCE: ordenes APR de linea ULEXITA. Recalcula, por orden:
--     xpr_insumo.costouni (solo cod_art 5) -> costo movil a la fecha
--     xpr_produccion.costototal / totalmp
--     xpr_producto.costo_b / costo / costouni
--
-- IDEMPOTENTE / REVERSIBLE: tablas de respaldo bkp_v6091_*; los recalculos son
--   deterministas. El movimiento de ULEXITA se recorre desde wrk_ulex_mov.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 1) Tablas de trabajo (se recrean en cada corrida).
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS wrk_ulex_mov;
CREATE TABLE wrk_ulex_mov (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha        DATE          NOT NULL,
    ord          INT           NOT NULL,          -- 1 = entrada, 2 = salida (mismo dia)
    clase        CHAR(2)       NOT NULL,          -- 'EV' entrada valuada, 'S' salida
    cantidad     DECIMAL(20,4) NOT NULL,
    valor_in     DECIMAL(20,6) NULL,              -- valor neto (solo 'EV')
    idproduccion BIGINT        NULL,              -- orden, solo en consumos de produccion
    KEY ix_ord (fecha, ord, id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS wrk_ulex_costo;
CREATE TABLE wrk_ulex_costo (
    idproduccion    BIGINT        NOT NULL,
    costo_uni_fecha DECIMAL(16,6) NULL,           -- costo movil de ULEXITA a la fecha del consumo
    PRIMARY KEY (idproduccion)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2) Poblar el flujo de movimientos de ULEXITA (cod_art 5).
-- ----------------------------------------------------------------------------

-- 2.a Acopio (ENTRADA valuada): valor neto, cantidad = pesoneto.
INSERT INTO wrk_ulex_mov (fecha, ord, clase, cantidad, valor_in, idproduccion)
SELECT COALESCE(a.fecha, '1900-01-01'), 1, 'EV', COALESCE(a.pesoneto, 0),
       ROUND(((a.pesoprov + a.pesobal)/2/1000) * a.precio
             * (CASE WHEN a.tienefac = 1 THEN 0.87 ELSE 1 END), 6), NULL
FROM acopiomp a
JOIN metaproductoproduccion mp ON mp.idmetaproductoproduccion = a.idmetaproductoproduccion
WHERE mp.cod_art = '5' AND a.estado <> 'ANL';

-- 2.b Kardex 'E' (ENTRADA valuada): valor = monto.
INSERT INTO wrk_ulex_mov (fecha, ord, clase, cantidad, valor_in, idproduccion)
SELECT COALESCE(m.fecha, '1900-01-01'), 1, 'EV', COALESCE(m.cantidad, 0), COALESCE(m.monto, 0), NULL
FROM inv_movdet m
WHERE m.cod_alm = '4' AND m.cod_art = '5' AND m.estado <> 'ANL' AND m.tipo_mov = 'E';

-- 2.c Kardex 'S' (SALIDA, al costo vigente). Sin orden asociada.
INSERT INTO wrk_ulex_mov (fecha, ord, clase, cantidad, valor_in, idproduccion)
SELECT COALESCE(m.fecha, '1900-01-01'), 2, 'S', COALESCE(m.cantidad, 0), NULL, NULL
FROM inv_movdet m
WHERE m.cod_alm = '4' AND m.cod_art = '5' AND m.estado <> 'ANL' AND m.tipo_mov = 'S';

-- 2.d Consumo de ULEXITA en produccion (SALIDA). Lleva idproduccion para
--     registrar el costo vigente de la orden. Todas las ordenes no anuladas
--     (PEN+APR) para que el promedio refleje el consumo real del stock.
INSERT INTO wrk_ulex_mov (fecha, ord, clase, cantidad, valor_in, idproduccion)
SELECT COALESCE(DATE(pr.fechainicio), '1900-01-01'), 2, 'S', COALESCE(xi.cantidad, 0), NULL, xi.idproduccion
FROM xpr_insumo xi
JOIN xpr_produccion pr ON pr.idproduccion = xi.idproduccion
WHERE xi.cod_art = '5' AND pr.estado <> 'ANL';

-- ----------------------------------------------------------------------------
-- 3) Procedimiento: recorre el kardex de ULEXITA y registra el costo movil
--    vigente en cada consumo de orden (carry-forward del costo si saldo <= 0).
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_ulex_costo_fecha;
DELIMITER $$
CREATE PROCEDURE sp_ulex_costo_fecha()
BEGIN
    DECLARE v_done  INT DEFAULT 0;
    DECLARE v_clase CHAR(2);
    DECLARE v_cant  DECIMAL(20,4);
    DECLARE v_valor DECIMAL(20,6);
    DECLARE v_idprod BIGINT;

    DECLARE r_qty  DECIMAL(24,6) DEFAULT 0;
    DECLARE r_val  DECIMAL(24,6) DEFAULT 0;
    DECLARE r_cost DECIMAL(28,10) DEFAULT 0;

    DECLARE cur CURSOR FOR
        SELECT clase, cantidad, valor_in, idproduccion
        FROM wrk_ulex_mov
        ORDER BY fecha, ord, id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    OPEN cur;
    rl: LOOP
        FETCH cur INTO v_clase, v_cant, v_valor, v_idprod;
        IF v_done = 1 THEN LEAVE rl; END IF;

        IF v_clase = 'EV' THEN
            SET r_qty = r_qty + v_cant;
            SET r_val = r_val + COALESCE(v_valor, 0);
            IF r_qty > 0 THEN SET r_cost = r_val / r_qty; END IF;   -- si <=0, arrastra
        ELSE  -- 'S'
            -- costo vigente para la orden que consume en esta fecha
            IF v_idprod IS NOT NULL THEN
                INSERT INTO wrk_ulex_costo (idproduccion, costo_uni_fecha)
                VALUES (v_idprod, ROUND(r_cost, 6))
                ON DUPLICATE KEY UPDATE costo_uni_fecha = ROUND(r_cost, 6);
            END IF;
            SET r_val = r_val - v_cant * r_cost;
            SET r_qty = r_qty - v_cant;
            IF r_qty > 0 THEN SET r_cost = r_val / r_qty; END IF;   -- si <=0, arrastra
        END IF;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;
CALL sp_ulex_costo_fecha();

-- ----------------------------------------------------------------------------
-- 4) REVISION (correr y revisar). Costo movil que recibira cada orden APR ULEXITA
--    vs el costouni actual del insumo. Ordenado por fecha.
-- ----------------------------------------------------------------------------
SELECT p.idproduccion, p.codigo, DATE(p.fechainicio) AS fecha,
       i.costouni AS costouni_actual,
       w.costo_uni_fecha AS costo_movil_fecha,
       i.cantidad AS cantidad_mp
FROM xpr_produccion p
JOIN xpr_linea l   ON l.idlinea = p.idlinea
JOIN xpr_insumo i  ON i.idproduccion = p.idproduccion AND i.cod_art = '5'
JOIN xpr_insumoformula f ON f.idinsumoformula = i.idinsumoformula AND f.defecto = 1
JOIN wrk_ulex_costo w ON w.idproduccion = p.idproduccion
WHERE p.estado = 'APR' AND l.report_template_code = 'ULEXITA'
ORDER BY p.fechainicio, p.idproduccion;

-- ----------------------------------------------------------------------------
-- 5) Respaldos (idempotentes).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bkp_v6091_orden (
    idproduccion   BIGINT        NOT NULL,
    costototal_old DECIMAL(16,2) NULL,
    totalmp_old    DECIMAL(16,2) NULL,
    backed_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idproduccion)
) ENGINE=InnoDB;

INSERT INTO bkp_v6091_orden (idproduccion, costototal_old, totalmp_old)
SELECT p.idproduccion, p.costototal, p.totalmp
FROM xpr_produccion p
JOIN xpr_linea l ON l.idlinea = p.idlinea
JOIN wrk_ulex_costo w ON w.idproduccion = p.idproduccion
WHERE p.estado = 'APR' AND l.report_template_code = 'ULEXITA'
  AND NOT EXISTS (SELECT 1 FROM bkp_v6091_orden b WHERE b.idproduccion = p.idproduccion);

CREATE TABLE IF NOT EXISTS bkp_v6091_insumo (
    idinsumo     BIGINT        NOT NULL,
    idproduccion BIGINT        NULL,
    cod_art      VARCHAR(6)    NULL,
    costouni_old DECIMAL(16,6) NULL,
    costouni_new DECIMAL(16,6) NULL,
    backed_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idinsumo)
) ENGINE=InnoDB;

INSERT INTO bkp_v6091_insumo (idinsumo, idproduccion, cod_art, costouni_old, costouni_new)
SELECT i.idinsumo, i.idproduccion, i.cod_art, i.costouni, w.costo_uni_fecha
FROM xpr_insumo i
JOIN xpr_produccion p ON p.idproduccion = i.idproduccion
JOIN xpr_linea l ON l.idlinea = p.idlinea
JOIN xpr_insumoformula f ON f.idinsumoformula = i.idinsumoformula AND f.defecto = 1
JOIN wrk_ulex_costo w ON w.idproduccion = i.idproduccion
WHERE i.cod_art = '5' AND p.estado = 'APR' AND l.report_template_code = 'ULEXITA'
  AND NOT EXISTS (SELECT 1 FROM bkp_v6091_insumo b WHERE b.idinsumo = i.idinsumo);

CREATE TABLE IF NOT EXISTS bkp_v6091_producto (
    idproducto   BIGINT        NOT NULL,
    idproduccion BIGINT        NULL,
    cantidad     DECIMAL(14,4) NULL,
    costo_old    DECIMAL(16,2) NULL,
    costouni_old DECIMAL(16,2) NULL,
    costo_a      DECIMAL(16,2) NULL,
    costo_b_old  DECIMAL(16,2) NULL,
    costo_c      DECIMAL(16,2) NULL,
    backed_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idproducto)
) ENGINE=InnoDB;

INSERT INTO bkp_v6091_producto (idproducto, idproduccion, cantidad, costo_old, costouni_old, costo_a, costo_b_old, costo_c)
SELECT pr.idproducto, pr.idproduccion, pr.cantidad, pr.costo, pr.costouni, pr.costo_a, pr.costo_b, pr.costo_c
FROM xpr_producto pr
JOIN bkp_v6091_orden b ON b.idproduccion = pr.idproduccion
WHERE NOT EXISTS (SELECT 1 FROM bkp_v6091_producto x WHERE x.idproducto = pr.idproducto);

-- ----------------------------------------------------------------------------
-- 6) Actualizar el COSTO UNITARIO del insumo ULEXITA (solo cod_art 5) al costo
--    movil de la fecha de la orden.
-- ----------------------------------------------------------------------------
UPDATE xpr_insumo i
JOIN bkp_v6091_insumo b ON b.idinsumo = i.idinsumo
SET i.costouni = b.costouni_new;

-- ----------------------------------------------------------------------------
-- 7) Recalcular COSTO TOTAL de la orden (y totalmp) con el costouni ya corregido.
--      costototal = ROUND( SUM(ROUND(cantidad*costouni,6)), 2 )
--      totalmp    = ROUND( SUM(cantidad), 2 )   [insumos inputDefault]
-- ----------------------------------------------------------------------------
UPDATE xpr_produccion p
JOIN bkp_v6091_orden b ON b.idproduccion = p.idproduccion
SET
    p.costototal = (
        SELECT ROUND(SUM(ROUND(i.cantidad * i.costouni, 6)), 2)
        FROM xpr_insumo i WHERE i.idproduccion = p.idproduccion),
    p.totalmp = (
        SELECT ROUND(SUM(i.cantidad), 2)
        FROM xpr_insumo i
        JOIN xpr_insumoformula f ON f.idinsumoformula = i.idinsumoformula AND f.defecto = 1
        WHERE i.idproduccion = p.idproduccion);

-- ----------------------------------------------------------------------------
-- 8) Recalcular COSTO B, COSTO y COSTO UNITARIO de cada producto terminado.
--    (mismo costeo que XProductionAction.approve / v6.0.90)
-- ----------------------------------------------------------------------------
UPDATE xpr_producto pr
JOIN bkp_v6091_orden b ON b.idproduccion = pr.idproduccion
JOIN ( SELECT idproduccion, rc FROM (
           SELECT i.idproduccion AS idproduccion,
                  ROUND(SUM(ROUND(i.cantidad * i.costouni, 6)), 2) AS rc
           FROM xpr_insumo i WHERE i.idproducto IS NULL
           GROUP BY i.idproduccion
       ) zr ) rc ON rc.idproduccion = pr.idproduccion
JOIN ( SELECT idproduccion, tv FROM (
           SELECT pr2.idproduccion AS idproduccion,
                  SUM(ROUND(pr2.cantidad *
                       (SELECT a2.cant_pr FROM inv_articulos a2 WHERE a2.cod_art = pr2.cod_art LIMIT 1), 2)) AS tv
           FROM xpr_producto pr2 GROUP BY pr2.idproduccion
       ) zv ) tv ON tv.idproduccion = pr.idproduccion
SET pr.costo_b = ROUND(
        rc.rc * ROUND(
                  ROUND(
                    ROUND(
                      ROUND(pr.cantidad *
                        (SELECT a.cant_pr FROM inv_articulos a WHERE a.cod_art = pr.cod_art LIMIT 1), 2)
                      * 100, 2)
                    / tv.tv, 2)
                  / 100, 6),
        2)
WHERE pr.cantidad > 0 AND tv.tv > 0;

UPDATE xpr_producto pr
JOIN bkp_v6091_orden b ON b.idproduccion = pr.idproduccion
SET pr.costo    = ROUND(ROUND(pr.costo_a + pr.costo_b, 2) + pr.costo_c, 2),
    pr.costouni = ROUND(ROUND(ROUND(pr.costo_a + pr.costo_b, 2) + pr.costo_c, 2) / pr.cantidad, 2)
WHERE pr.cantidad > 0;

-- ----------------------------------------------------------------------------
-- 9) VERIFICACION (opcional).
-- ----------------------------------------------------------------------------
-- a) Insumo ULEXITA con el costo movil aplicado (0 filas):
-- SELECT i.idinsumo, i.costouni, b.costouni_new FROM xpr_insumo i
--   JOIN bkp_v6091_insumo b ON b.idinsumo=i.idinsumo
--   WHERE ABS(i.costouni - b.costouni_new) > 0.000001;
-- b) Costo Total = suma de insumos (0 filas):
-- SELECT p.idproduccion, p.costototal FROM xpr_produccion p JOIN bkp_v6091_orden b ON b.idproduccion=p.idproduccion
--   WHERE ABS(p.costototal - ROUND((SELECT SUM(ROUND(i.cantidad*i.costouni,6)) FROM xpr_insumo i WHERE i.idproduccion=p.idproduccion),2)) > 0.01;
-- c) PT coherente (0 filas):
-- SELECT pr.idproducto FROM xpr_producto pr JOIN bkp_v6091_orden b ON b.idproduccion=pr.idproduccion
--   WHERE pr.cantidad>0 AND ( ABS(pr.costo - ROUND(ROUND(pr.costo_a+pr.costo_b,2)+pr.costo_c,2))>0.01
--      OR ABS(pr.costouni - ROUND(pr.costo/pr.cantidad,2))>0.01 );
-- d) Resumen viejo vs nuevo del costouni de ULEXITA:
-- SELECT MIN(costouni_old) o_min, MAX(costouni_old) o_max, MIN(costouni_new) n_min, MAX(costouni_new) n_max FROM bkp_v6091_insumo;

-- ----------------------------------------------------------------------------
-- 10) LIMPIEZA (opcional, tras validar):
-- DROP PROCEDURE IF EXISTS sp_ulex_costo_fecha;
-- DROP TABLE IF EXISTS wrk_ulex_mov;
-- DROP TABLE IF EXISTS wrk_ulex_costo;
-- DROP TABLE IF EXISTS bkp_v6091_producto;
-- DROP TABLE IF EXISTS bkp_v6091_insumo;
-- DROP TABLE IF EXISTS bkp_v6091_orden;
-- ============================================================================
