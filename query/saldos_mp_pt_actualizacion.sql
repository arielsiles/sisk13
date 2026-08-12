-- Recalcula el saldo de los almacenes MATERIAS PRIMAS y PRODUCTOS TERMINADOS con el criterio de la vista Produccion > Saldos y actualiza inv_inventario, inv_inventario_detalle e inv_articulos.saldo_mon (= saldo * costo_uni).
-- Se ejecuta completo de una sola vez, igual en local que en produccion (sin schema escrito: conectar el cliente al schema o usar USE <schema>).
-- Cruce por cod_alm + cod_art (sin centro de costo ni compania); los articulos con 0 o mas de 1 fila destino no se tocan.
-- Las tablas de trabajo se crean con CREATE TABLE ... SELECT para heredar el juego de caracteres de cada entorno y se eliminan al final: no queda ninguna tabla nueva en la base.

-- Necesario porque hay UPDATE sin WHERE sobre las tablas de trabajo; solo afecta a esta sesion.
SET SQL_SAFE_UPDATES = 0;

-- Fecha de corte (por defecto hoy); @corte es el limite superior exclusivo.
SET @fecha := CURDATE();
SET @corte := DATE_ADD(@fecha, INTERVAL 1 DAY);

-- Almacenes objetivo, resueltos por tipo porque el cod_alm cambia entre entornos.
DROP TABLE IF EXISTS wrk_alm_mp_pt;
CREATE TABLE wrk_alm_mp_pt AS
SELECT a.cod_alm
FROM inv_almacenes a
WHERE a.tipo IN ('RAW_MATERIAL', 'FINISHED_GOODS')
  AND (a.estado IS NULL OR a.estado <> 'BLO');
ALTER TABLE wrk_alm_mp_pt ADD PRIMARY KEY (cod_alm);

-- Universo: articulos VIG de esos almacenes, incluidos los de saldo cero.
DROP TABLE IF EXISTS wrk_saldo_mp_pt;
CREATE TABLE wrk_saldo_mp_pt AS
SELECT p.cod_alm,
       p.cod_art,
       p.cod_med,
       CAST(0 AS DECIMAL(24,6)) AS saldo,
       CAST(0 AS SIGNED)        AS inv_filas,
       CAST(0 AS SIGNED)        AS det_filas
FROM inv_articulos p
         JOIN wrk_alm_mp_pt a ON a.cod_alm = p.cod_alm
WHERE p.estado = 'VIG';
ALTER TABLE wrk_saldo_mp_pt ADD PRIMARY KEY (cod_alm, cod_art), ADD KEY ix_art (cod_art);

-- Kardex inv_movdet: vales y despachos APROBADOS del almacen del articulo.
UPDATE wrk_saldo_mp_pt s
    JOIN ( SELECT m.cod_alm, m.cod_art,
                  SUM(CASE m.tipo_mov WHEN 'E' THEN m.cantidad
                                      WHEN 'S' THEN -m.cantidad
                                      ELSE 0 END) AS delta
           FROM inv_movdet m
                    JOIN wrk_alm_mp_pt a ON a.cod_alm = m.cod_alm
           WHERE m.estado = 'APR'
             AND m.fecha < @corte
           GROUP BY m.cod_alm, m.cod_art ) k
    ON k.cod_art = s.cod_art AND k.cod_alm = s.cod_alm
SET s.saldo = s.saldo + COALESCE(k.delta, 0);

-- Acopio de materia prima (entrada) por Peso Empresa, estados APR y CONTA.
UPDATE wrk_saldo_mp_pt s
    JOIN ( SELECT mp.cod_art, SUM(ac.pesobal) AS delta
           FROM acopiomp ac
                    JOIN metaproductoproduccion mp
                         ON mp.idmetaproductoproduccion = ac.idmetaproductoproduccion
           WHERE ac.estado IN ('APR', 'CONTA')
             AND ac.fecha < @corte
           GROUP BY mp.cod_art ) c
    ON c.cod_art = s.cod_art
SET s.saldo = s.saldo + COALESCE(c.delta, 0);

-- Produccion, producto obtenido (entrada): orden <> ANL y plan hasta el corte (sin plan cuenta siempre).
UPDATE wrk_saldo_mp_pt s
    JOIN ( SELECT pp.cod_art, SUM(pp.cantidad) AS delta
           FROM xpr_producto pp
                    JOIN xpr_produccion pr ON pr.idproduccion = pp.idproduccion
                    LEFT JOIN xpr_plan pl  ON pl.idplan = pr.idplan
           WHERE pr.estado <> 'ANL'
             AND (pl.fecha IS NULL OR pl.fecha < @corte)
           GROUP BY pp.cod_art ) x
    ON x.cod_art = s.cod_art
SET s.saldo = s.saldo + COALESCE(x.delta, 0);

-- Produccion, consumo de insumos (salida); incluye producto terminado usado como insumo.
UPDATE wrk_saldo_mp_pt s
    JOIN ( SELECT xi.cod_art, SUM(xi.cantidad) AS delta
           FROM xpr_insumo xi
                    JOIN xpr_produccion pr ON pr.idproduccion = xi.idproduccion
                    LEFT JOIN xpr_plan pl  ON pl.idplan = pr.idplan
           WHERE pr.estado <> 'ANL'
             AND (pl.fecha IS NULL OR pl.fecha < @corte)
           GROUP BY xi.cod_art ) x
    ON x.cod_art = s.cod_art
SET s.saldo = s.saldo - COALESCE(x.delta, 0);

-- Reproceso ULEXITA sobre cod_art_reproc_final: final entra, consumo sale, en TN convertidas a la unidad del articulo.
UPDATE wrk_saldo_mp_pt s
    JOIN ( SELECT u.cod_art_reproc_final AS cod_art,
                  SUM(COALESCE(u.reproceso_final_tn, 0))   AS entrada_tn,
                  SUM(COALESCE(u.consumo_reproceso_tn, 0)) AS salida_tn
           FROM xpr_produccion_ulexita u
                    JOIN xpr_produccion pr ON pr.idproduccion = u.idproduccion
                    LEFT JOIN xpr_plan pl  ON pl.idplan = pr.idplan
           WHERE u.cod_art_reproc_final IS NOT NULL
             AND pr.estado <> 'ANL'
             AND (pl.fecha IS NULL OR pl.fecha < @corte)
           GROUP BY u.cod_art_reproc_final ) r
    ON r.cod_art = s.cod_art
SET s.saldo = s.saldo
    + (r.entrada_tn - r.salida_tn) * (CASE WHEN UPPER(TRIM(s.cod_med)) = 'KG' THEN 1000 ELSE 1 END);

-- Precision final de saldo_uni y cantidad.
UPDATE wrk_saldo_mp_pt SET saldo = ROUND(saldo, 2);

-- Filas destino existentes: solo se actualiza lo que tiene exactamente una.
UPDATE wrk_saldo_mp_pt s
    LEFT JOIN ( SELECT cod_alm, cod_art, COUNT(*) AS n
                FROM inv_inventario GROUP BY cod_alm, cod_art ) i
    ON i.cod_alm = s.cod_alm AND i.cod_art = s.cod_art
SET s.inv_filas = COALESCE(i.n, 0);

UPDATE wrk_saldo_mp_pt s
    LEFT JOIN ( SELECT cod_alm, cod_art, COUNT(*) AS n
                FROM inv_inventario_detalle GROUP BY cod_alm, cod_art ) d
    ON d.cod_alm = s.cod_alm AND d.cod_art = s.cod_art
SET s.det_filas = COALESCE(d.n, 0);

START TRANSACTION;

-- Cantidad en inv_inventario.
UPDATE inv_inventario inv
    JOIN wrk_saldo_mp_pt s
        ON s.cod_alm = inv.cod_alm AND s.cod_art = inv.cod_art
SET inv.saldo_uni = s.saldo
WHERE s.inv_filas = 1;

-- Cantidad en inv_inventario_detalle, equivalente a inv_inventario.
UPDATE inv_inventario_detalle d
    JOIN wrk_saldo_mp_pt s
        ON s.cod_alm = d.cod_alm AND s.cod_art = d.cod_art
SET d.cantidad = s.saldo
WHERE s.det_filas = 1;

-- Valor: saldo_mon = cantidad * costo_uni; costo_uni no se modifica.
UPDATE inv_articulos p
    JOIN wrk_saldo_mp_pt s
        ON s.cod_alm = p.cod_alm AND s.cod_art = p.cod_art
SET p.saldo_mon = ROUND(s.saldo * COALESCE(p.costo_uni, 0), 2)
WHERE s.inv_filas = 1 AND s.det_filas = 1;

COMMIT;

-- Limpieza de las tablas de trabajo.
DROP TABLE IF EXISTS wrk_saldo_mp_pt;
DROP TABLE IF EXISTS wrk_alm_mp_pt;

