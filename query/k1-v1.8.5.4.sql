/** 190420017 Partner **/
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (223, 'PARTNER', NULL, 1, 15, 'Functionality.customers.partner', 1);

-- UPDATE exttipodocumento e SET e.`idtipodocumento` = 2 WHERE idexttipodocumento = 1;

INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (2, 'CB', 0, 1, 1);
INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (3, 'LP', 0, 1, 1);
INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (4, 'SC', 0, 1, 1);
INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (5, 'OR', 0, 1, 1);
INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (6, 'SU', 0, 1, 1);
INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (7, 'PT', 0, 1, 1);
INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (8, 'BE', 0, 1, 1);
INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (9, 'PA', 0, 1, 1);
INSERT INTO exttipodocumento (idexttipodocumento, extension, VERSION, idcompania, idtipodocumento) VALUES (10,'TA', 0, 1, 1);

/** 20042017 **/
ALTER TABLE socio ADD COLUMN estado VARCHAR(3) NULL AFTER nosocio;

ALTER TABLE socio ADD COLUMN idzonaproductiva BIGINT(20) NULL AFTER estado;
ALTER TABLE socio ADD FOREIGN KEY (idzonaproductiva) REFERENCES zonaproductiva (idzonaproductiva);

ALTER TABLE socio ADD COLUMN telfijo VARCHAR(20) NULL AFTER estado;
ALTER TABLE socio ADD COLUMN telcelular VARCHAR(20) NULL AFTER telfijo;
ALTER TABLE socio ADD COLUMN nrohijos INT NULL AFTER estado;

ALTER TABLE socio ADD COLUMN conyuge VARCHAR(100) NULL AFTER telcelular;

ALTER TABLE socio ADD COLUMN contacto VARCHAR(100) NULL AFTER nrohijos;

ALTER TABLE socio ADD COLUMN telcontacto VARCHAR(20) NULL AFTER contacto;

ALTER TABLE socio ADD COLUMN iddepartamento BIGINT(20) NULL AFTER idzonaproductiva;
ALTER TABLE socio ADD FOREIGN KEY (iddepartamento) REFERENCES departamento (iddepartamento);

/** 21042017 **/
ALTER TABLE credito ADD COLUMN estado VARCHAR(3) NULL AFTER idcredito;

/** 23042017 **/
ALTER TABLE socio ADD COLUMN nocred INT NOT NULL AFTER estado;

/** 27042017 **/
ALTER TABLE credito ADD COLUMN saldo DECIMAL(13,2) NOT NULL AFTER cuota;
ALTER TABLE transaccioncredito ADD COLUMN capital DECIMAL(13,2) NOT NULL AFTER idtransaccioncredito;
ALTER TABLE transaccioncredito ADD COLUMN interes DECIMAL(13,2) NOT NULL AFTER capital;
ALTER TABLE transaccioncredito ADD COLUMN dias INT NOT NULL AFTER fechatransaccion;

/** 28042017 **/
ALTER TABLE transaccioncredito ADD COLUMN saldo DECIMAL(13,2) NOT NULL AFTER dias;
ALTER TABLE transaccioncredito DROP COLUMN idfactura;
ALTER TABLE credito DROP COLUMN identidad;
ALTER TABLE transaccioncredito ADD COLUMN tipo VARCHAR(3) NULL AFTER saldo;

ALTER TABLE credito ADD COLUMN entregado INT(11) NOT NULL AFTER fechacreacion;


