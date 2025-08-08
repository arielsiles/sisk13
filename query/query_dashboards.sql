/*
`com_encoc`
`com_detoc`
`inv_articulos`
*/

select e.`id_com_encoc`, e.`fecha`, g.cod_gru, g.descri as grupo, s.descri as subrupo, d.`cod_art`, d.`cant_sol`, d.`costo_uni`, d.`total` as total_Bs
from com_detoc d
    join com_encoc e     on d.`id_com_encoc` = e.`id_com_encoc`
    join inv_articulos i on d.`cod_art` = i.`cod_art`
    join inv_subgrupos s on i.cod_sub = s.cod_sub
    join inv_grupos g    on s.cod_gru = g.cod_gru
where i.cod_gru = s.cod_gru
and e.`fecha` between '2025-01-01' and '2025-07-08'
and g.cod_gru = 9
;


update inv_articulos i set i.cod_sub = 5
where i.cod_art = 321;