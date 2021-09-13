/**  30.08.2021  **/
-- revisar en ILVA
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(428, 'CREDITSALE_INVOICING', 1, 1, 'Functionality.customers.orderInvoicing', 1);

-- 13.09.2021
ALTER TABLE productormateriaprima ADD COLUMN activo INT(1) AFTER esresponsable;
UPDATE productormateriaprima p SET p.`activo` = 1;

UPDATE productormateriaprima p SET p.`activo` = 0
WHERE p.`numerocuenta` IS NULL
;

-- --