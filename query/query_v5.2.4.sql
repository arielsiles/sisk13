/** 16/07/2021 **/
ALTER TABLE inv_almacenes ADD COLUMN tipo VARCHAR(20) AFTER descri;
UPDATE inv_almacenes a SET a.`tipo` = 'INPUTS' WHERE a.`cod_alm` = 1;
UPDATE inv_almacenes a SET a.`tipo` = 'DAIRY' WHERE a.`cod_alm` = 2;
UPDATE inv_almacenes a SET a.`tipo` = 'MATERIAL' WHERE a.`cod_alm` = 3;
UPDATE inv_almacenes a SET a.`tipo` = 'SERVICE' WHERE a.`cod_alm` = 4;
UPDATE inv_almacenes a SET a.`tipo` = 'VETERINARY' WHERE a.`cod_alm` = 5;
UPDATE inv_almacenes a SET a.`tipo` = 'AUXILIARY' WHERE a.`cod_alm` = 6;
UPDATE inv_almacenes a SET a.`tipo` = 'REPLACEMENT' WHERE a.`cod_alm` = 7;
UPDATE inv_almacenes a SET a.`tipo` = 'AGENCY' WHERE a.`cod_alm` = 8;
