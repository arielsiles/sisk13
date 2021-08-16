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
from inv_movdet d
left join inv_mov m   on d.`no_trans` = m.`no_trans`
left join inv_vales v on m.`no_trans` = v.`no_trans`
left join inv_articulos a on d.`cod_art` = a.`cod_art`
where v.`cod_doc` = 'BAJ'
and v.`fecha` >= '2017-01-01'
and v.`estado` in ('APR');

-- -------------------------------------------------------------------------
-- 6. revisar
create or replace view inv_devolucion as
select d.`no_trans`,v.fecha, m.`fecha_cre`, d.`cod_art`, a.`descri`, d.`cantidad`, d.`costounitario`, d.`preciounitcompra`, v.cod_alm
from inv_movdet d
left join inv_mov m   on d.`no_trans` = m.`no_trans`
left join inv_vales v on m.`no_trans` = v.`no_trans`
left join inv_articulos a on d.`cod_art` = a.`cod_art`
where v.`cod_doc` = 'DEV'
and v.`fecha` >= '2017-01-01'
and v.`estado` in ('APR')
;

-- 6.1 Reprocesos
create or replace view inv_reprocesos as
select d.`no_trans`,v.fecha, m.`fecha_cre`, d.`cod_art`, a.`descri`, d.`cantidad`, d.`costounitario`, d.`preciounitcompra`, v.cod_alm
from inv_movdet d
left join inv_mov m   on d.`no_trans` = m.`no_trans`
left join inv_vales v on m.`no_trans` = v.`no_trans`
left join inv_articulos a on d.`cod_art` = a.`cod_art`
where v.`cod_doc` = 'REP'
and v.`fecha` >= '2017-01-01'
and v.`estado` in ('APR')
;

-- 7.
-- VMARCADO
create or replace view `vmarcado` as
select  `r`.`idrhmarcado` as `idrhmarcado`,
	`r`.`marfecha` as `marfecha`,
	`r`.`marperid` as `marperid`,
	concat(`r`.`marperid`,'') as `marreftarjeta`,
	`r`.`marhora` as `marhora`,
	`r`.`control` as `control`,
	`r`.`marippc` as `marippc`,
	`r`.`descripcion` as `descripcion`,
	'1' as `sede`,
	concat(`p`.`nombres`,' ',`p`.`apellidopaterno`,' ',`p`.`apellidomaterno`) as `nombre`
from ((`rh_marcado` `r`  left join `empleado` `em`  on ((`r`.`marperid` = `em`.`codigomarcacion`)))
left join `persona` `p`  on ((`em`.`idempleado` = `p`.`idpersona`)))
where (`r`.`marfecha` >= '2020-01-01')
;










