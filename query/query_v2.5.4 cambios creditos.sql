/** 31/08/2018 creditos **/
-- ALTER TABLE transaccioncredito MODIFY COLUMN fechatransaccion DATE NOT NULL;
-- ALTER TABLE transaccioncredito ADD COLUMN fechacreacion DATETIME NOT NULL AFTER fechatransaccion;
-- UPDATE transaccioncredito t SET t.`fechacreacion` = '2018-08-30 00:00:00';

ALTER TABLE credito ADD COLUMN cuotas INT(11) NOT NULL AFTER fechapago;
UPDATE credito c SET c.`cuotas` = c.`plazo`;