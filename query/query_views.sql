-- -----------------------------------------------------------------
-- VISTAS PARA REPORTE DE PRODUCCIO DE PRODUCTOS
-- 1.- 
create or replace view produccionreproceso as
select PL.FECHA, M.CODIGO as COD_ART, M.NOMBRE, O.CODIGO as COD_ORD, o.`cantidadproducida` as CANTIDAD_SC, O.CANTIDADPRODUCIDARESPONSABLE as CANTIDAD_SP, 0 as REPROCESO_SC, 0 as REPROCESO_SP, o.costotoalproduccion as COSTOTOTALPRODUCCION
from ORDENPRODUCCION O
left join PLANIFICACIONPRODUCCION PL    on O.IDPLANIFICACIONPRODUCCION  = PL.IDPLANIFICACIONPRODUCCION
left join COMPOSICIONPRODUCTO C         on O.IDCOMPOSICIONPRODUCTO      = C.IDCOMPOSICIONPRODUCTO
left join PRODUCTOPROCESADO P           on C.IDPRODUCTOPROCESADO        = P.IDPRODUCTOPROCESADO
left join METAPRODUCTOPRODUCCION M      on P.IDPRODUCTOPROCESADO        = M.IDMETAPRODUCTOPRODUCCION
union
select pp.`FECHA`, mp.codigo as COD_ART, mp.`NOMBRE`, pb.`CODIGO` as COD_ORD, 0 as CANTIDAD_SC, 0 as CANTIDAD_SP, ps.`cantidad` as REPROCESO_SC, ps.`cantidadproducidaresponsable` as REPROCESO_SP, ps.COSTOTOTALPRODUCCION
from productobase pb
left join planificacionproduccion pp 	on pb.`idplanificacionproduccion` = pp.`idplanificacionproduccion`
left join productosimple ps		on pb.`idproductobase` = ps.`idproductobase`
left join productosimpleprocesado psp	on ps.`idproductosimple` = psp.`idproductosimple`
left join metaproductoproduccion mp 	on psp.`idmetaproductoproduccion` = mp.`idmetaproductoproduccion`
union
select pl.`fecha`, p.`cod_art`, a.`descri` as nombre, '', p.`cantidad` as cantidad_sc, 0, 0, 0, p.`costo`
from pr_producto p
left join pr_plan pl 	  on p.`idplan` = pl.`idplan`
left join inv_articulos a on p.`cod_art` = a.`cod_art`
;


select pl.`fecha`, p.`cod_art`, a.`descri` as nombre, '', p.`cantidad` as cantidad_sc, 0, 0, 0, p.`costo`
from pr_producto p
left join pr_plan pl 	  on p.`idplan` = pl.`idplan`
left join inv_articulos a on p.`cod_art` = a.`cod_art`
;


-- 2.- 
create or replace view producciontotal as
select 	1 as ID, FECHA, COD_ART, NOMBRE,
	SUM(CANTIDAD_SC) as CANTIDAD_SC,
	SUM(CANTIDAD_SP) as CANTIDAD_SP,
	SUM(REPROCESO_SC) as REPROCESO_SC,
	SUM(REPROCESO_SP) as REPROCESO_SP,
	SUM(COSTOTOTALPRODUCCION) as COSTOTOTALPRODUCCION,
	SUM(CANTIDAD_SC) + SUM(REPROCESO_SC) as CANT_TOTAL
from produccionreproceso
group by ID, FECHA, COD_ART, NOMBRE
;
-- -----------------------------------------------------------------
-- 3.- Vista para Reporte COSTO DE PRODUCCION
create or replace view costoproduccion as
select e.`id_tmpenc`, d.`id_tmpdet`, e.`fecha`, e.`tipo_doc`, e.`no_doc`, e.`no_trans`, o.`codigo`, d.`cuenta`, a.`descri`, d.`debe`, d.`haber`
from sf_tmpenc e
join ordenproduccion o on e.`id_tmpenc` = o.`id_tmpenc`
join sf_tmpdet d on e.`id_tmpenc` = d.`id_tmpenc`
join arcgms a    on d.`cuenta` = a.`cuenta`
where e.estado <> 'ANL'
union
select e.`id_tmpenc`, d.`id_tmpdet`, e.`fecha`, e.`tipo_doc`, e.`no_doc`, e.`no_trans`, b.`codigo`, d.`cuenta`, a.`descri`, d.`debe`, d.`haber`
from sf_tmpenc e
join productobase b    on e.`id_tmpenc` = b.`id_tmpenc`
join sf_tmpdet d on e.`id_tmpenc` = d.`id_tmpenc`
join arcgms a    on d.`cuenta` = a.`cuenta`
where e.estado <> 'ANL'
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










