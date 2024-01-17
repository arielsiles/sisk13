-- 16.01.2023
-- Ejecutado en Terdemol Prod
alter table inv_grupos add column cta_costo varchar(31) after descri;
update inv_grupos g set g.cta_costo = '51000707' where g.cod_gru = 9;
update inv_grupos g set g.cta_costo = '51000710' where g.cod_gru = 12;
update inv_grupos g set g.cta_costo = '51000704' where g.cod_gru = 6;
update inv_grupos g set g.cta_costo = '51000705' where g.cod_gru = 7;
update inv_grupos g set g.cta_costo = '51000709' where g.cod_gru = 11;
update inv_grupos g set g.cta_costo = '51000708' where g.cod_gru = 10;
update inv_grupos g set g.cta_costo = '51000706' where g.cod_gru = 8;
update inv_grupos g set g.cta_costo = '51000100' where g.cod_gru = 1;
update inv_grupos g set g.cta_costo = '51000703' where g.cod_gru = 5;
update inv_grupos g set g.cta_costo = '51000702' where g.cod_gru = 3;
update inv_grupos g set g.cta_costo = '51000701' where g.cod_gru = 13;
update inv_grupos g set g.cta_costo = '51000711' where g.cod_gru = 4;
update inv_grupos g set g.cta_costo = '51000701' where g.cod_gru = 2;

-- Actualizar Cta de Costo (Grupos) a Articulos
-- Ejecutado en Terdemol Prod
update inv_articulos a
LEFT JOIN inv_grupos g    ON a.`cod_gru` = g.`cod_gru`
set a.cuenta_art = g.cta_costo
where a.cod_gru = g.cod_gru
;
