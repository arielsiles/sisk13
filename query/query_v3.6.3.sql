/** 22.08.2019 **/
alter table tipocredito add column ipctaeje varchar(20) after ictaeje;

-- FCISC
update tipocredito t set t.ipctaeje = '5159930100' where t.`idtipocredito` = 1; -- Intereses Penales Cartera en ejec.MV
-- F. Lech
-- update tipocredito t set t.ipctaeje = '5159910100' where t.`idtipocredito` = 1; -- Intereses Penales Cartera Ejec.CIS M.N
-- verifivar ????


-- 
-- Añadir restriccion de integridad en:
-- sf_tmpdet (idsocio, idcuenta, idcredito)