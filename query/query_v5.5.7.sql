-- 10.02.2022
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(444, 'VALIDATE_SALE', 1, 1, 'Functionality.customers.validateSale', 1);

ALTER TABLE cajausuario ADD COLUMN validar INT(1) NULL AFTER estado;
UPDATE cajausuario c SET c.`validar` = 1;