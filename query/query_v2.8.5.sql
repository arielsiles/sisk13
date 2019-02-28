/** 24.02.2019 **/
-- inta
-- intb
ALTER TABLE tipocuenta ADD COLUMN intb DECIMAL(4,2) AFTER interes;
ALTER TABLE tipocuenta ADD COLUMN inta DECIMAL(4,2) AFTER interes;

ALTER TABLE tipocuenta DROP COLUMN interes;

/* Para fondo CISC */
UPDATE tipocuenta t SET t.`inta` = 2 WHERE t.`idtipocuenta` IN (1, 2);
UPDATE tipocuenta t SET t.`inta` = 5 WHERE t.`idtipocuenta` IN (3);
UPDATE tipocuenta t SET t.`intb` = 1.5 WHERE t.`idtipocuenta` IN (1, 2);
UPDATE tipocuenta t SET t.`intb` = 0 WHERE t.`idtipocuenta` IN (3);

/* Para fondo Lechero */
-- todo

/* Para fondo CISC */
ALTER TABLE tipocuenta ADD  COLUMN tipo VARCHAR(10) AFTER activo;
UPDATE tipocuenta t SET t.`tipo` = 'AHO';
UPDATE tipocuenta t SET t.`tipo` = 'DPF' WHERE t.`idtipocuenta` = 3;

/** Para ambos **/
-- DELETE FROM arcgms  WHERE cuenta IN (4110000000, 4110100000, 4110110000);
-- upload csv

UPDATE sf_tmpdet d SET d.`cod_prov` = 62 WHERE d.`id_tmpdet` = 410263;
UPDATE sf_tmpdet d SET d.`cod_prov` = 521 WHERE d.`id_tmpdet` = 437823;
UPDATE sf_tmpdet d SET d.`cod_prov` = 74 WHERE d.`id_tmpdet` = 468218;
UPDATE sf_tmpdet d SET d.`cod_prov` = 521 WHERE d.`id_tmpdet` = 478967;
UPDATE sf_tmpdet d SET d.`cod_prov` = 67 WHERE d.`id_tmpdet` = 479875;
UPDATE sf_tmpdet d SET d.`cod_prov` = 448 WHERE d.`id_tmpdet` IN (520831,520832,520899,520900);

UPDATE inv_articulos i SET i.`cuenta_art` = '4460310208' WHERE i.`cod_art` = 600;
UPDATE sf_tmpdet d SET d.`cuenta` = '4460114700' WHERE d.`id_tmpdet` = 408394;
UPDATE sf_tmpdet d SET d.`cuenta` = '4460114700' WHERE d.`id_tmpdet` = 410800;

UPDATE sf_tmpdet d SET d.`cod_prov` = 89 WHERE d.`id_tmpdet` = 428987;

UPDATE sf_tmpdet d SET d.`cuenta` = '4460114700' 
WHERE d.`id_tmpdet` IN (412222,412418,412574,412766,412874,413037,414380,414554,414669,414834);



