-- -----------------------------------------------------------------
-- VISTAS PARA REPORTE DE PRODUCCIO DE PRODUCTOS
-- 1.- 
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

-- ----------------------------------------------------------------------
-- 5. VISTA Bajas de Productos
-- CREATE VIEW inv_bajas AS
SELECT d.`cod_art`, a.`descri`, SUM(d.`cantidad`) AS cantidad
FROM inv_movdet d
LEFT JOIN inv_mov m   ON d.`no_trans` = m.`no_trans`
LEFT JOIN inv_vales v ON m.`no_trans` = v.`no_trans`
LEFT JOIN inv_articulos a ON d.`cod_art` = a.`cod_art`
WHERE v.`cod_doc` = 'BAJ'
AND v.`fecha` BETWEEN '2017-01-01' AND '2017-12-31'
AND v.`estado` IN ('APR', 'PEN')
GROUP BY d.`cod_art`, a.`descri`;

--
-- CREATE OR REPLACE VIEW inv_bajas AS
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
-- CREATE OR REPLACE VIEW inv_devolucion AS
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
-- CREATE OR REPLACE VIEW inv_reprocesos AS
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
	CONCAT(`r`.`marperid`,'') AS `marreftarjeta`,
	`r`.`marhora` AS `marhora`,
	`r`.`control` AS `control`,
	`r`.`marippc` AS `marippc`,
	`r`.`descripcion` AS `descripcion`,
	'1' AS `sede`,
	CONCAT(`p`.`nombres`,' ',`p`.`apellidopaterno`,' ',`p`.`apellidomaterno`) AS `nombre`
FROM ((`rh_marcado` `r`  LEFT JOIN `empleado` `em`  ON ((`r`.`marperid` = `em`.`codigomarcacion`)))
LEFT JOIN `persona` `p`  ON ((`em`.`idempleado` = `p`.`idpersona`)))
WHERE (`r`.`marfecha` >= '2018-08-01')
;










