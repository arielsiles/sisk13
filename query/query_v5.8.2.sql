-- 05.11.2023
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(280, 'METAPRODUCT', 6, 15, 'Functionality.production.metaProductProduction', 1);

ALTER TABLE metaproductoproduccion ADD COLUMN precio DECIMAL(12, 2) NOT NULL AFTER cod_art;

--

CREATE TABLE precioproductor (
  `idprecioproductor` BIGINT(20) NOT NULL,
  `precio` DECIMAL(12,2) NULL,
  `idproductormateriaprima`  BIGINT(20) NOT NULL,
  `idmetaproductoproduccion` BIGINT(20) NOT NULL,
  `version` BIGINT(20) DEFAULT NULL,
  `idcompania` BIGINT(20) DEFAULT NULL,
  
  PRIMARY KEY (`idprecioproductor`),
  CONSTRAINT `fk_precioproductor_productormateriaprima1`  FOREIGN KEY (`idproductormateriaprima`)  REFERENCES `productormateriaprima` (`idproductormateriaprima`),
  CONSTRAINT `fk_precioproductor_metaproductoproduccion1` FOREIGN KEY (`idmetaproductoproduccion`) REFERENCES `metaproductoproduccion` (`idmetaproductoproduccion`),
  CONSTRAINT `fk_precioproductor_compania`                FOREIGN KEY (`idcompania`) 		   REFERENCES `compania` (`idcompania`)
    
)
ENGINE = INNODB;