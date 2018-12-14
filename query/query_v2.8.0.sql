
/** 11-12-2018 **/
UPDATE tipocuenta t SET t.`ctap_mn` = '2120110200', t.`ctap_me` = '2120130200' WHERE t.`idtipocuenta` = 1;
-- DELETE FROM arcgms;

/** 14-12-2018 **/
INSERT INTO _sequence VALUES ('CE', 0);
INSERT INTO tipodoc VALUES (12, 'CE', 'COMPROBANTE DE EGRESO');

ALTER TABLE sf_tmpdet ADD COLUMN idcredito BIGINT(20) NULL AFTER idcuenta;