
/** 11-12-2018 **/
UPDATE tipocuenta t SET t.`ctap_mn` = '2120110200', t.`ctap_me` = '2120130200' WHERE t.`idtipocuenta` = 1;
-- DELETE FROM arcgms;

/** 14-12-2018 **/
INSERT INTO _sequence VALUES ('CE', 0);
INSERT INTO tipodoc VALUES (12, 'CE', 'COMPROBANTE DE EGRESO');

ALTER TABLE sf_tmpdet ADD COLUMN idcredito BIGINT(20) NULL AFTER idcuenta;

/** 18-12-2018 **/

-- DEFINIR PRECIO DE VENTA EN INV_ARTICULOS
-- DEFINIR COSTO DEL ARTICULO
-- Asiganar corectamente la cuenta de almacen libretas

ALTER TABLE sf_tmpdet ADD COLUMN idsocio BIGINT(20) NULL AFTER cod_art;
ALTER TABLE sf_tmpdet ADD COLUMN cant_art BIGINT(20) NULL AFTER cod_art;
ALTER TABLE transaccioncredito ADD COLUMN id_tmpenc BIGINT(20) AFTER idcredito;
ALTER TABLE transaccioncredito ADD COLUMN VERSION BIGINT(20) NULL; 
UPDATE transaccioncredito t SET t.`version` = 0;