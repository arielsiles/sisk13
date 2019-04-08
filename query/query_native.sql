/** 06.04.2019 **/
-- Para calcular el COSTO UNITARIO de Productos Terminados
-- select z.cod_art, sum(z.debe), sum(z.haber), sum(z.cant_d), sum(z.cant_h), (SUM(z.debe)-SUM(z.haber)) as monto, (SUM(z.cant_d)-SUM(z.cant_h)) as cantidad, (SUM(z.debe)-SUM(z.haber)) / (SUM(z.cant_d)-SUM(z.cant_h)) as costo_uni
SELECT z.cod_art, (SUM(z.debe)-SUM(z.haber)) / (SUM(z.cant_d)-SUM(z.cant_h)) AS costo_uni
FROM (
	-- Inventario inicio de periodo
	SELECT p.cod_art, (p.saldofis * p.costouni) AS debe, 0 AS haber, p.saldofis AS cant_d, 0 AS cant_h
	FROM inv_periodo p
	WHERE p.cod_alm = 2
	AND p.mes = 1
	AND p.gestion = 2019
	AND p.saldofis > 0
	UNION
	-- Compras
	SELECT d.cod_art, SUM(d.monto) AS debe, 0 AS haber, SUM(d.cantidad) AS cant_d, 0 AS cant_h
	FROM inv_movdet d
	LEFT JOIN inv_vales v ON d.no_trans = v.no_trans
	WHERE v.fecha BETWEEN '2019-01-01' AND '2019-01-31'
	AND v.cod_alm = 2 AND d.tipo_mov = 'E' AND v.id_com_encoc IS NOT NULL
	GROUP BY d.cod_art
	UNION
	-- Entradas de produccion
	SELECT d.cod_art, SUM(d.monto) AS debe, 0 AS haber, SUM(d.cantidad) cant_d, 0 AS cant_h
	FROM inv_vales i
	JOIN inv_movdet d ON i.no_trans = d.no_trans
	WHERE i.fecha BETWEEN '2019-01-01' AND '2019-01-31'
	AND i.cod_alm = 2
	AND (i.idordenproduccion IS NOT NULL OR i.idproductobase IS NOT NULL)
	GROUP BY d.cod_art
	UNION
	-- Transferencias, Bajas, Devoluciones
	SELECT d.`cod_art`, SUM(t.`debe`) AS debe, SUM(t.`haber`) AS haber, SUM(IF(t.debe>0, d.cantidad, 0)) AS cant_d, SUM(IF(t.haber>0, d.cantidad, 0)) AS cant_h
	FROM inv_movdet d
	LEFT JOIN inv_vales v ON d.`no_trans` = v.`no_trans`
	LEFT JOIN sf_tmpdet t ON v.`idtmpenc` = t.`id_tmpenc`
	WHERE v.`fecha` BETWEEN '2019-01-01' AND '2019-01-31'
	AND v.`oper` IS NOT NULL
	AND d.`cod_art` = t.`cod_art`
	GROUP BY d.`cod_art`
) z
GROUP BY z.cod_art
;


-- PARA CALCULAR COSTO UNITARIO DE PRODUCTOS VETERINARIOS
SELECT z.cod_art, SUM(z.monto) / SUM(z.cantidad) FROM (
	SELECT p.cod_art, (p.saldofis * p.costouni) AS monto, p.saldofis AS cantidad
	FROM inv_periodo p
	WHERE p.cod_alm = 5
	AND p.mes = 1
	AND p.gestion = 2019
	AND p.saldofis > 0
	UNION
	SELECT d.cod_art, d.monto, d.cantidad
	FROM inv_movdet d
	LEFT JOIN inv_vales v ON d.no_trans = v.no_trans
	WHERE v.fecha BETWEEN '2019-01-01' AND '2019-01-31'
	AND v.cod_alm = 5 AND d.tipo_mov = 'E' AND v.id_com_encoc IS NOT NULL
) z
GROUP BY z.cod_art
;



