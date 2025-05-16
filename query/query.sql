-- Reporte de produccion diaria
select xp.fecha, pr.codigo, pr.tipoturno, g.codigo, p.cod_art, a.descri, p.cantidad, pr.totalmp
from xpr_producto p
join xpr_produccion pr on p.idproduccion = pr.idproduccion
join xpr_plan xp       on pr.idplan      = xp.idplan
join inv_articulos a   on p.cod_art      = a.cod_art
join xpr_grupo g       on pr.idgrupo     = g.idgrupo
where xp.fecha between '2025-01-01' and '2025-12-31'
;

-- Reporte de Gastos (EGR), vales
select v.no_trans, v.fecha, v.no_vale, v.estado, v.tipo_gasto, v.iddestino, d.nombre, a.descri, de.cantidad, de.costounitario, de.monto,  v.idproceso, v.cod_prod
from inv_vales v
         join inv_destino d   on v.iddestino = d.iddestino
         join inv_mov m       on v.no_trans = m.no_trans
         join inv_movdet de   on m.no_trans = de.no_trans
         join inv_articulos a on de.cod_art = a.cod_art
where v.fecha between '2025-04-01' and '2025-12-31'
  and v.cod_doc = 'EGR'
-- and v.idproceso is null
;

-- Resumen de Gastos (EGR), vales
select v.iddestino, d.nombre, sum(de.cantidad) as cantidad, sum(de.monto) as monto
from inv_vales v
join inv_destino d   on v.iddestino = d.iddestino
join inv_mov m       on v.no_trans = m.no_trans
join inv_movdet de   on m.no_trans = de.no_trans
join inv_articulos a on de.cod_art = a.cod_art
where v.fecha between '2025-04-01' and '2025-04-30'
  and v.cod_doc = 'EGR'
  and v.estado = 'APR'
group by v.iddestino, d.nombre
;

-- Detalle de Gastos (EGR), vales
select a.cod_art, a.descri, sum(de.cantidad) as cantidad, sum(de.monto) as monto
from inv_vales v
         join inv_destino d   on v.iddestino = d.iddestino
         join inv_mov m       on v.no_trans = m.no_trans
         join inv_movdet de   on m.no_trans = de.no_trans
         join inv_articulos a on de.cod_art = a.cod_art
where v.fecha between '2025-04-01' and '2025-04-30'
  and v.cod_doc = 'EGR'
  and v.estado = 'APR'
group by a.cod_art, a.descri
;

-- PARA REPORTE DE GASTOS, CON ANALITICA
select v.no_trans, v.fecha, v.no_vale, d.nombre as area, da.nombre as ana_nombre, de.cod_art, de.cantidad, de.monto
from inv_movdet de
    join inv_mov m           on de.no_trans = m.no_trans
    join inv_vales v         on m.no_trans = v.no_trans
    join detalleanalitica da on v.iddetalleanalitica = da.iddetalleanalitica
    join inv_destino d       on v.iddestino = d.iddestino
where v.fecha between '2025-04-01' and '2025-04-30'
and v.cod_doc = 'EGR'
and v.estado = 'APR'
;

select d.nombre as area, da.nombre as ana_nombre, count(v.no_trans) as no_vales, sum(de.monto) as monto
from inv_movdet de
    join inv_mov m           on de.no_trans = m.no_trans
    join inv_vales v         on m.no_trans = v.no_trans
    join detalleanalitica da on v.iddetalleanalitica = da.iddetalleanalitica
    join inv_destino d       on v.iddestino = d.iddestino
where v.fecha between '2025-04-01' and '2025-04-30'
and v.cod_doc = 'EGR'
and v.estado = 'APR'
group by d.nombre, da.nombre
;
