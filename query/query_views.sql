-- -----------------------------------------------------------------
-- VISTAS PARA REPORTE DE PRODUCCIO DE PRODUCTOS
-- 1.- 
CREATE OR REPLACE VIEW produccionreproceso AS
SELECT pl.fecha, m.codigo AS cod_art, m.nombre, o.codigo AS cod_ord, o.`cantidadproducida` AS cantidad_sc, o.cantidadproducidaresponsable AS cantidad_sp, 0 AS reproceso_sc, 0 AS reproceso_sp, o.costotoalproduccion AS costototalproduccion
FROM ordenproduccion o
LEFT JOIN planificacionproduccion pl    ON o.idplanificacionproduccion  = pl.idplanificacionproduccion
LEFT JOIN composicionproducto c         ON o.idcomposicionproducto      = c.idcomposicionproducto
LEFT JOIN productoprocesado p           ON c.idproductoprocesado        = p.idproductoprocesado
LEFT JOIN metaproductoproduccion m      ON p.idproductoprocesado        = m.idmetaproductoproduccion
UNION
SELECT pp.`fecha`, mp.codigo AS cod_art, mp.`nombre`, pb.`codigo` AS cod_ord, 0 AS cantidad_sc, 0 AS cantidad_sp, ps.`cantidad` AS reproceso_sc, ps.`cantidadproducidaresponsable` AS reproceso_sp, ps.costototalproduccion
FROM productobase pb
LEFT JOIN planificacionproduccion pp 	ON pb.`idplanificacionproduccion` = pp.`idplanificacionproduccion`
LEFT JOIN productosimple ps		ON pb.`idproductobase` = ps.`idproductobase`
LEFT JOIN productosimpleprocesado psp	ON ps.`idproductosimple` = psp.`idproductosimple`
LEFT JOIN metaproductoproduccion mp 	ON psp.`idmetaproductoproduccion` = mp.`idmetaproductoproduccion`
UNION
SELECT pl.`fecha`, p.`cod_art`, a.`descri` AS nombre, '', p.`cantidad` AS cantidad_sc, 0, 0, 0, p.`costo`
FROM pr_producto p
LEFT JOIN pr_plan pl 	  ON p.`idplan` = pl.`idplan`
LEFT JOIN inv_articulos a ON p.`cod_art` = a.`cod_art`
;

-- 2.- 
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
-- -----------------------------------------------------------------
-- 3.- Vista para Reporte COSTO DE PRODUCCION
CREATE OR REPLACE VIEW costoproduccion AS
SELECT e.`id_tmpenc`, d.`id_tmpdet`, e.`fecha`, e.`tipo_doc`, e.`no_doc`, e.`no_trans`, o.`codigo`, d.`cuenta`, a.`descri`, d.`debe`, d.`haber`
FROM sf_tmpenc e
JOIN ordenproduccion o ON e.`id_tmpenc` = o.`id_tmpenc`
JOIN sf_tmpdet d ON e.`id_tmpenc` = d.`id_tmpenc`
JOIN arcgms a    ON d.`cuenta` = a.`cuenta`
WHERE e.estado <> 'ANL'
UNION
SELECT e.`id_tmpenc`, d.`id_tmpdet`, e.`fecha`, e.`tipo_doc`, e.`no_doc`, e.`no_trans`, b.`codigo`, d.`cuenta`, a.`descri`, d.`debe`, d.`haber`
FROM sf_tmpenc e
JOIN productobase b    ON e.`id_tmpenc` = b.`id_tmpenc`
JOIN sf_tmpdet d ON e.`id_tmpenc` = d.`id_tmpenc`
JOIN arcgms a    ON d.`cuenta` = a.`cuenta`
WHERE e.estado <> 'ANL'
;
-- -----------------------------------------------------------------

-- 4. VISTA VENTAS union al contado y a credito (lacteos U veterinarios)
/*create or replace view ventas as
	select p.`FECHA_ENTREGA` as FECHA, p.tipoventa, ap.`IDARTICULOSPEDIDO`, ap.`cod_art`, ap.`CANTIDAD`, ap.PROMOCION, ap.`REPOSICION`, ap.`TOTAL`, ap.importe, p.idcliente, ap.`IDPEDIDOS`, ap.`IDVENTADIRECTA`, p.idusuario, p.idtipopedido
	from articulos_pedido ap
	join pedidos p 	 on ap.`IDPEDIDOS` = p.`IDPEDIDOS`
	and p.estado <> 'ANULADO'
	and ap.cod_art not in (192,193,194,795,195,196,188,493,693,832,833,834,835,197, 490, 521, 1078, 1090,
	480, 481) -- LAM
	union
	select v.`FECHA_PEDIDO` as FECHA, v.tipoventa, ap.`IDARTICULOSPEDIDO`, ap.`cod_art`, ap.`CANTIDAD`, 0, ap.`REPOSICION`, ap.`TOTAL`, ap.importe, v.idcliente, ap.`IDPEDIDOS`, ap.`IDVENTADIRECTA`, v.idusuario, 1
	from articulos_pedido ap
	join ventadirecta v on ap.`IDVENTADIRECTA` = v.`IDVENTADIRECTA`
	and v.estado <> 'ANULADO'
	and ap.`cod_art` not in (192,193,194,795,195,196,188,493,693,832,833,834,835,197, 490, 521, 1078, 1090); */
CREATE OR REPLACE VIEW ventas AS
	SELECT p.`FECHA_ENTREGA` AS FECHA, p.tipoventa, ap.`IDARTICULOSPEDIDO`, a.`cod_alm`, ap.`cod_art`, a.`descri`,
	ap.`CANTIDAD`, ap.PROMOCION, ap.`REPOSICION`, ap.`TOTAL`, ap.importe, p.idcliente, ap.`IDPEDIDOS`, ap.`IDVENTADIRECTA`, p.idusuario, p.idtipopedido
	FROM articulos_pedido ap
	JOIN pedidos p 	     ON ap.`IDPEDIDOS` = p.`IDPEDIDOS`
	JOIN inv_articulos a ON ap.`cod_art` = a.`cod_art`
	AND p.`FECHA_ENTREGA` >= '2021-01-01'
	AND p.estado <> 'ANULADO'
	AND ap.cod_art NOT IN (192,193,194,795,195,196,188,493,693,832,833,834,835,197, 490, 521, 1078, 1090, 480, 481)
	UNION
	SELECT v.`FECHA_PEDIDO` AS FECHA, v.tipoventa, ap.`IDARTICULOSPEDIDO`, a.`cod_alm`, ap.`cod_art`, a.`descri`,
	ap.`CANTIDAD`, 0 AS PROMOCION, ap.`REPOSICION`, ap.`TOTAL`, ap.importe, v.idcliente, ap.`IDPEDIDOS`, ap.`IDVENTADIRECTA`, v.idusuario, 1 AS idtipopedido
	FROM articulos_pedido ap
	JOIN ventadirecta v ON ap.`IDVENTADIRECTA` = v.`IDVENTADIRECTA`
	JOIN inv_articulos a ON ap.`cod_art` = a.`cod_art`
	AND v.`FECHA_PEDIDO` >= '2021-01-01'
	AND v.estado <> 'ANULADO'
	AND ap.`cod_art` NOT IN (192,193,194,795,195,196,188,493,693,832,833,834,835,197, 490, 521, 1078, 1090)
;

-- ----------------------------------------------------------------------
-- 5. VISTA Bajas de Productos
CREATE OR REPLACE VIEW inv_bajas AS
SELECT d.`no_trans`,v.fecha, m.`fecha_cre`, d.`cod_art`, a.`descri`, d.`cantidad`, d.`costounitario`, d.`preciounitcompra`, v.cod_alm
FROM inv_movdet d
LEFT JOIN inv_mov m   ON d.`no_trans` = m.`no_trans`
LEFT JOIN inv_vales v ON m.`no_trans` = v.`no_trans`
LEFT JOIN inv_articulos a ON d.`cod_art` = a.`cod_art`
WHERE v.`cod_doc` = 'BAJ'
AND v.`fecha` >= '2017-01-01'
AND v.`estado` IN ('APR');

-- -------------------------------------------------------------------------
-- 6. revisar
CREATE OR REPLACE VIEW inv_devolucion AS
SELECT d.`no_trans`,v.fecha, m.`fecha_cre`, d.`cod_art`, a.`descri`, d.`cantidad`, d.`costounitario`, d.`preciounitcompra`, v.cod_alm
FROM inv_movdet d
LEFT JOIN inv_mov m   ON d.`no_trans` = m.`no_trans`
LEFT JOIN inv_vales v ON m.`no_trans` = v.`no_trans`
LEFT JOIN inv_articulos a ON d.`cod_art` = a.`cod_art`
WHERE v.`cod_doc` = 'DEV'
AND v.`fecha` >= '2017-01-01'
AND v.`estado` IN ('APR')
;

-- 6.1 Reprocesos
CREATE OR REPLACE VIEW inv_reprocesos AS
SELECT d.`no_trans`,v.fecha, m.`fecha_cre`, d.`cod_art`, a.`descri`, d.`cantidad`, d.`costounitario`, d.`preciounitcompra`, v.cod_alm
FROM inv_movdet d
LEFT JOIN inv_mov m   ON d.`no_trans` = m.`no_trans`
LEFT JOIN inv_vales v ON m.`no_trans` = v.`no_trans`
LEFT JOIN inv_articulos a ON d.`cod_art` = a.`cod_art`
WHERE v.`cod_doc` = 'REP'
AND v.`fecha` >= '2017-01-01'
AND v.`estado` IN ('APR')
;

-- 7.
-- VMARCADO
CREATE OR REPLACE VIEW `vmarcado` AS
SELECT  `r`.`idrhmarcado` AS `idrhmarcado`,
	`r`.`marfecha` AS `marfecha`,
	`r`.`marperid` AS `marperid`,
	concat(`r`.`marperid`,'') AS `marreftarjeta`,
	`r`.`marhora` AS `marhora`,
	`r`.`control` AS `control`,
	`r`.`marippc` AS `marippc`,
	`r`.`descripcion` AS `descripcion`,
	'1' AS `sede`,
	concat(`p`.`nombres`,' ',`p`.`apellidopaterno`,' ',`p`.`apellidomaterno`) AS `nombre`
FROM ((`rh_marcado` `r`  LEFT JOIN `empleado` `em`  ON ((`r`.`marperid` = `em`.`codigomarcacion`)))
LEFT JOIN `persona` `p`  ON ((`em`.`idempleado` = `p`.`idpersona`)))
WHERE (`r`.`marfecha` >= '2020-01-01')
;










