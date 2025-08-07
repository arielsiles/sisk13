/*
`com_encoc`
`com_detoc`
`inv_articulos`
*/

select e.`id_com_encoc`, e.`fecha`, g.descri as grupo, s.descri as subrupo, d.`cod_art`, d.`cant_sol`, d.`costo_uni`, d.`total` as total_Bs
from com_detoc d
    join com_encoc e     on d.`id_com_encoc` = e.`id_com_encoc`
    join inv_articulos i on d.`cod_art` = i.`cod_art`
    join inv_subgrupos s on i.cod_sub = s.cod_sub
    join inv_grupos g    on s.cod_gru = g.cod_gru
where i.cod_gru = s.cod_gru
;
