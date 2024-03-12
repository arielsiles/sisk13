-- 10.03.2024
alter table acopiomp add foreign key (idzonaproductiva) references zonaproductiva (idzonaproductiva);

alter table zonaproductiva add column idciudad bigint(20) after numero;
alter table zonaproductiva add foreign key (idciudad) references ciudad (idciudad);

--

CREATE TABLE `inspeccionzona`(
    `idinspeccion` BIGINT(20) NOT NULL,
    `fecha` DATE,
    `horasalida` TIME,
    `horallegada` TIME,
    `idproductor` BIGINT(20),
    `idciudad` BIGINT(20),
    `idzonaproductiva` BIGINT(20),
    PRIMARY KEY (`idinspeccion`),
    CONSTRAINT `inspeccionzona_ibfk_1` FOREIGN KEY (`idproductor`) REFERENCES `terdemol`.`productormateriaprima`(`idproductormateriaprima`),
    CONSTRAINT `inspeccionzona_ibfk_2` FOREIGN KEY (`idciudad`) REFERENCES `terdemol`.`ciudad`(`idciudad`),
    CONSTRAINT `inspeccionzona_ibfk_3` FOREIGN KEY (`idzonaproductiva`) REFERENCES `terdemol`.`zonaproductiva`(`idzonaproductiva`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `materiasprimaszona` (
    `idmateriasprimaszona` bigint(20) NOT NULL,
    `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
    `precioorigen` decimal(12,2) DEFAULT NULL,
    `preciodestino` decimal(12,2) DEFAULT NULL,
    `observacion` varchar(255) DEFAULT NULL,
    `idinspeccion` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`idmateriasprimaszona`),
    KEY `materiasprimaszona_ibfk_1` (`idmetaproductoproduccion`),
    KEY `materiasprimaszona_ibfk_2` (`idinspeccion`),
    CONSTRAINT `materiasprimaszona_ibfk_1` FOREIGN KEY (`idmetaproductoproduccion`) REFERENCES `metaproductoproduccion` (`idmetaproductoproduccion`),
    CONSTRAINT `materiasprimaszona_ibfk_2` FOREIGN KEY (`idinspeccion`) REFERENCES `inspeccionzona` (`idinspeccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `informelab` (
    `idinformelab` bigint(20) NOT NULL,
    `fecha` date DEFAULT NULL,
    `codigoinforme` varchar(100) DEFAULT NULL,
    `caracteristica` varchar(255) DEFAULT NULL,
    `fechamuestra` date DEFAULT NULL,
    `fecharecepcion` date DEFAULT NULL,
    `fechainicio` date DEFAULT NULL,
    `fechafin` date DEFAULT NULL,
    `idinspeccion` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`idinformelab`),
    KEY `informelab_ibfk_1` (`idinspeccion`),
    CONSTRAINT `informelab_ibfk_1` FOREIGN KEY (`idinspeccion`) REFERENCES `inspeccionzona` (`idinspeccion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

