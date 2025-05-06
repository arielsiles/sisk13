select xp.fecha, pr.codigo, pr.tipoturno, g.codigo, p.cod_art, a.descri, p.cantidad
from xpr_producto p
join xpr_produccion pr on p.idproduccion = pr.idproduccion
join xpr_plan xp       on pr.idplan      = xp.idplan
join inv_articulos a   on p.cod_art      = a.cod_art
join xpr_grupo g       on pr.idgrupo     = g.idgrupo
where xp.fecha between '2025-01-01' and '2025-12-31'
;