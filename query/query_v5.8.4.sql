-- 13.11.2023
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(281, 'COLLECTMATERIAL', 6, 1, 'Functionality.production.collectMaterial', 1);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(282, 'MATERIALCOLLECTIONRECORD', 6, 15, 'Functionality.production.collectionRecord', 1);

--
ALTER TABLE acopiomp ADD COLUMN idzonaproductiva BIGINT(20) NOT NULL AFTER estado;

-- 23.11.2023
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(283, 'PRODUCERPRICE', 6, 15, 'Functionality.production.producerPrice', 1);

-- 25.11.2023
ALTER TABLE precioproductor ADD CONSTRAINT acopio_precioproductor UNIQUE (`idproductormateriaprima`, `idmetaproductoproduccion`);

CREATE TABLE `pagoacopiomp` (
  `idpagoacopiomp` BIGINT(20) NOT NULL,
  `fecha` DATE NULL,
  `fechaaprobacion` DATE NULL,
  `beneficiario` VARCHAR(200) NULL,
  `tipopago` VARCHAR(100) NULL,
  `montopago` DECIMAL(12,2) NULL,
  `estado` VARCHAR(40) NULL,
  `glosa` VARCHAR(1000) NULL,
  `id_tmpenc` BIGINT(20) NULL,
  `version` BIGINT(20) DEFAULT NULL,
  idproductormateriaprima BIGINT(20) DEFAULT NULL,
  `idcompania` BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`idpagoacopiomp`),
  CONSTRAINT `fk_pagoacopiomp_productormateriaprima` FOREIGN KEY (`idproductormateriaprima`) REFERENCES `productormateriaprima` (`idproductormateriaprima`),
  CONSTRAINT `fk_pagoacopiomp_compania` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
)
ENGINE = INNODB;

CREATE TABLE `detallepagoacopiomp` (
  `iddetallepagoacopiomp` BIGINT(20) NOT NULL,
  `peso` DECIMAL(12,2) NULL,
  `precio` DECIMAL(12,2) NULL,
  `monto` DECIMAL(12,2) NULL,
  `idpagoacopiomp` BIGINT(20) NOT NULL,
  `idacopiomp` BIGINT(20) NOT NULL,
  `version` BIGINT(20) DEFAULT NULL,
  `idcompania` BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`iddetallepagoacopiomp`),
  CONSTRAINT `fk_detallepagoacopiomp_pagoacopiomp` FOREIGN KEY (`idpagoacopiomp`) REFERENCES `pagoacopiomp` (`idpagoacopiomp`),
  CONSTRAINT `fk_detallepagoacopiomp_acopiomp`     FOREIGN KEY (`idacopiomp`) REFERENCES `acopiomp` (`idacopiomp`),
  CONSTRAINT `fk_detallepagoacopiomp_compania` 	   FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
)
ENGINE = INNODB;

--
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(284, 'RAWMATERIALPAYMENTREQUEST', 6, 15, 'Functionality.production.rawMaterialPaymentRequest', 1);



