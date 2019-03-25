-- /** 25.03.2019 cambios para generar costo de ventas **/
-- VISTAS PARA REPORTE DE PRODUCCIO DE PRODUCTOS
-- 1.- modify
CREATE OR REPLACE VIEW produccionreproceso AS
SELECT PL.FECHA, M.CODIGO AS COD_ART, M.NOMBRE, O.CODIGO AS COD_ORD, o.`cantidadproducida` AS CANTIDAD_SC, O.CANTIDADPRODUCIDARESPONSABLE AS CANTIDAD_SP, 0 AS REPROCESO_SC, 0 AS REPROCESO_SP, o.costotoalproduccion AS COSTOTOTALPRODUCCION
FROM ORDENPRODUCCION O
LEFT JOIN PLANIFICACIONPRODUCCION PL    ON O.IDPLANIFICACIONPRODUCCION  = PL.IDPLANIFICACIONPRODUCCION
LEFT JOIN COMPOSICIONPRODUCTO C         ON O.IDCOMPOSICIONPRODUCTO      = C.IDCOMPOSICIONPRODUCTO
LEFT JOIN PRODUCTOPROCESADO P           ON C.IDPRODUCTOPROCESADO        = P.IDPRODUCTOPROCESADO
LEFT JOIN METAPRODUCTOPRODUCCION M      ON P.IDPRODUCTOPROCESADO        = M.IDMETAPRODUCTOPRODUCCION
UNION
SELECT pp.`FECHA`, mp.codigo AS COD_ART, mp.`NOMBRE`, pb.`CODIGO` AS COD_ORD, 0 AS CANTIDAD_SC, 0 AS CANTIDAD_SP, ps.`cantidad` AS REPROCESO_SC, ps.`cantidadproducidaresponsable` AS REPROCESO_SP, ps.COSTOTOTALPRODUCCION
FROM productobase pb
LEFT JOIN planificacionproduccion pp 	ON pb.`idplanificacionproduccion` = pp.`idplanificacionproduccion`
LEFT JOIN productosimple ps		ON pb.`idproductobase` = ps.`idproductobase`
LEFT JOIN productosimpleprocesado psp	ON ps.`idproductosimple` = psp.`idproductosimple`
LEFT JOIN metaproductoproduccion mp 	ON psp.`idmetaproductoproduccion` = mp.`idmetaproductoproduccion`
UNION
SELECT STR_TO_DATE(CONCAT(i.`gestion`, '-01-01'),'%Y-%m-%d') AS fecha, i.`cod_art`, i.`nombre`, '0000-0000', i.`cantidad`, 0, 0, 0, (i.`cantidad` * i.`costo_uni`) AS COSTOTOTALPRODUCCION
FROM inv_inicio i  WHERE i.`alm` = 2 AND i.`cantidad` > 0
;

-- 2.- modify
CREATE OR REPLACE VIEW producciontotal AS
SELECT 	1 AS ID, FECHA, COD_ART, NOMBRE,
	SUM(CANTIDAD_SC) AS CANTIDAD_SC,
	SUM(CANTIDAD_SP) AS CANTIDAD_SP,
	SUM(REPROCESO_SC) AS REPROCESO_SC,
	SUM(REPROCESO_SP) AS REPROCESO_SP,
	SUM(COSTOTOTALPRODUCCION) AS COSTOTOTALPRODUCCION,
	SUM(CANTIDAD_SC) + SUM(REPROCESO_SC) AS CANT_TOTAL
FROM produccionreproceso
GROUP BY ID, FECHA, COD_ART, NOMBRE
;

-- 4. VISTA VENTAS union al contado y a credito (lacteos y veterinarios)
CREATE OR REPLACE VIEW ventas AS
	SELECT p.`FECHA_ENTREGA` AS FECHA, ap.`IDARTICULOSPEDIDO`, ap.`cod_art`, ap.`CANTIDAD`, ap.PROMOCION, ap.`REPOSICION`, ap.`TOTAL`, ap.`IDPEDIDOS`, ap.`IDVENTADIRECTA`, p.idusuario, p.idtipopedido
	FROM articulos_pedido ap
	JOIN pedidos p 	 ON ap.`IDPEDIDOS` = p.`IDPEDIDOS`
	AND p.estado <> 'ANULADO'
	UNION
	SELECT v.`FECHA_PEDIDO` AS FECHA, ap.`IDARTICULOSPEDIDO`, ap.`cod_art`, ap.`CANTIDAD`, 0, ap.`REPOSICION`, ap.`TOTAL`, ap.`IDPEDIDOS`, ap.`IDVENTADIRECTA`, v.idusuario, 1
	FROM articulos_pedido ap
	JOIN ventadirecta v ON ap.`IDVENTADIRECTA` = v.`IDVENTADIRECTA`
	AND v.estado <> 'ANULADO'
;
--







