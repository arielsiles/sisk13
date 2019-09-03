/** 30.08.2019 **/
alter table tipocredito add column ipctaven varchar(20) after ictaeje;

-- FCISC
update tipocredito t set t.ipctaven = '5149930100' where t.`idtipocredito` = 4; -- Intereses Penales Cartera Vencida MV
update tipocredito t set t.ipctaeje = '5159930100' where t.`idtipocredito` = 4; -- Intereses Penales Cartera en ejec.MV

-- 
-- Añadir restriccion de integridad en:
-- sf_tmpdet (idsocio, idcuenta, idcredito)