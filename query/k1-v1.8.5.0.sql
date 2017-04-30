INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (218, 'CREDIT', NULL, 1, 15, 'menu.customers.configuration.credit', 1);

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (219, 'DISCOUNTPOLICY', NULL, 1, 15, 'menu.customers.configuration.discountPolicy', 1);

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (220, 'CUSTOMERDISCOUNTRULE', NULL, 1, 15, 'menu.customers.configuration.customerDiscountRule', 1);

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (221, 'CUSTOMERCATEGORY', NULL, 1, 15, 'menu.customers.configuration.customerCategory', 1);

INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
VALUES (222, 'PARTNERCLUBCATEGORY', NULL, 1, 15, 'menu.customers.configuration.partnerClubCategory', 1);

-- 
-- 13.12.2016 PARTNER table
CREATE TABLE `socio` (
  `idsocio` BIGINT(20) NOT NULL,
  `nosocio` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`idsocio`)
) ENGINE=INNODB DEFAULT CHARSET=utf8


ALTER TABLE socio ADD COLUMN idcompania BIGINT NOT NULL;
ALTER TABLE socio ADD FOREIGN KEY (idcompania) REFERENCES compania (idcompania);


ALTER TABLE credito ADD COLUMN idsocio BIGINT NOT NULL;
ALTER TABLE credito ADD FOREIGN KEY (idsocio) REFERENCES socio (idsocio);

ALTER TABLE credito ADD COLUMN codigo VARCHAR(25) NOT NULL AFTER idcredito;

ALTER TABLE credito ADD COLUMN plazo INT(11) NOT NULL AFTER importe;

ALTER TABLE credito ADD COLUMN amortiza INT(11) NOT NULL AFTER plazo;

ALTER TABLE credito ADD COLUMN fechaconcesion DATE NOT NULL AFTER amortiza;

ALTER TABLE credito ADD COLUMN fechapago DATE NOT NULL AFTER fechaconcesion;

ALTER TABLE credito ADD COLUMN anual INT(11) NOT NULL AFTER importe;

ALTER TABLE credito ADD COLUMN cuota DECIMAL(13,2) NOT NULL AFTER fechapago;

ALTER TABLE credito MODIFY COLUMN importe DECIMAL(13,2) NOT NULL;

ALTER TABLE credito MODIFY COLUMN idcredito BIGINT NOT NULL;

ALTER TABLE credito ADD PRIMARY KEY(idcredito);

