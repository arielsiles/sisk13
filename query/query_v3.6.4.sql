/** 30.08.2019 **/
ALTER TABLE tipocredito ADD COLUMN ipctaven VARCHAR(20) AFTER ictaeje;

-- FCISC
UPDATE tipocredito t SET t.ipctaven = '5149930100' WHERE t.`idtipocredito` = 4; -- Intereses Penales Cartera Vencida MV



-- 
-- Añadir restriccion de integridad en:
-- sf_tmpdet (idsocio, idcuenta, idcredito)