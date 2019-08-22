/** 22.08.2019 **/
ALTER TABLE tipocredito ADD COLUMN ipctaeje VARCHAR(20) AFTER ictaeje;

-- FCISC
UPDATE tipocredito t SET t.ipctaeje = '5159910100' WHERE t.`idtipocredito` = 1; -- Intereses Penales Cartera Ejec.CIS M.N
-- verifivar ????
UPDATE tipocredito t SET t.ipctaeje = '5159930100' WHERE t.`idtipocredito` = 1; -- Intereses Penales Cartera en ejec.MV
