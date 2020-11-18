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

-- 2.- 
create or replace view producciontotal as
select 	1 as ID, FECHA, COD_ART, NOMBRE,
	sum(CANTIDAD_SC) as CANTIDAD_SC,
	sum(CANTIDAD_SP) as CANTIDAD_SP,
	sum(REPROCESO_SC) as REPROCESO_SC,
	sum(REPROCESO_SP) as REPROCESO_SP,
	sum(COSTOTOTALPRODUCCION) as COSTOTOTALPRODUCCION,
	sum(CANTIDAD_SC) + sum(REPROCESO_SC) as CANT_TOTAL
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
create or replace view ventas as
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
	and ap.`cod_art` not in (192,193,194,795,195,196,188,493,693,832,833,834,835,197, 490, 521, 1078, 1090)
;

-- ----------------------------------------------------------------------
-- 5. VISTA Bajas de Productos
create or replace view inv_bajas as
select d.`no_trans`,v.fecha, m.`fecha_cre`, d.`cod_art`, a.`descri`, d.`cantidad`, d.`costounitario`, d.`preciounitcompra`, v.cod_alm
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










