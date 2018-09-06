/** 31/08/2018 creditos **/
ALTER TABLE credito ADD COLUMN cuotas INT(11) NOT NULL AFTER plazo;
UPDATE credito c SET c.`cuotas` = c.`plazo`;