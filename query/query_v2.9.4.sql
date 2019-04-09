/** 08.04.2019 **/
ALTER TABLE sf_tmpdet MODIFY cant_art DECIMAL(16,2);

/** 09.04.2019 **/
ALTER TABLE configuracion ADD COLUMN cajagral1mn VARCHAR(20) NULL;
UPDATE configuracion SET cajagral1mn = '1110110100';