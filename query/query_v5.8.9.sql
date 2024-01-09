-- 08.01.2023
ALTER TABLE arcgms ADD COLUMN util VARCHAR(2) AFTER activa;
ALTER TABLE arcgms ADD COLUMN nomutil VARCHAR(50) AFTER util;

UPDATE arcgms a SET a.util = 'N' WHERE a.util IS NULL;

-- Para ILVA/CISC fijar Cuenta Credito Fisal -> columna permite_iva = 'S'
UPDATE arcgms a SET a.permite_iva = 'N' WHERE a.permite_iva IS NULL;
UPDATE arcgms a SET a.permite_iva = 'S' WHERE a.cuenta = '11400101'; /* Para Terdemol */

ALTER TABLE configuracion ADD COLUMN doc_oc_pago VARCHAR(5) AFTER ctacomision;
UPDATE configuracion a SET a.`doc_oc_pago` = 'CE';