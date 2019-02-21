SELECT a.`descri`, a.`cuenta`, SUBSTRING(a.`cuenta`, 6, 1)
FROM arcgms a
WHERE SUBSTRING(a.`cuenta`, 6, 1) = '3'
;

/** 19.02.2019 Actualizar moneda en Cuentas Contables **/
UPDATE arcgms a SET a.`moneda` = 'D' WHERE SUBSTRING(a.`cuenta`, 6, 1) = '2';
UPDATE arcgms a SET a.`moneda` = 'D' WHERE SUBSTRING(a.`cuenta`, 6, 1) = '3';
/** 20.02.2019 **/
