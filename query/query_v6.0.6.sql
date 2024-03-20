-- 10.03.2024
alter table acopiomp add foreign key (idzonaproductiva) references zonaproductiva (idzonaproductiva);

alter table zonaproductiva add column idciudad bigint(20) after numero;
alter table zonaproductiva add foreign key (idciudad) references ciudad (idciudad);
--
/*
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
*/
-- 12.03.2024
alter table metaproductoproduccion add column regalia decimal(12,2) after precio;

alter table configuracion add column cxp_iva varchar(20);
alter table configuracion add column cxp_regalia varchar(20);
alter table configuracion add column cxp_cns varchar(20);
alter table configuracion add column ret_cns decimal(5, 3);


update configuracion set cxp_iva        = '11400101' where no_cia = '01';
update configuracion set cxp_regalia    = '21300704' where no_cia = '01';
update configuracion set cxp_cns        = '21300705' where no_cia = '01';
update configuracion set ret_cns        = 0.018 where no_cia = '01';

alter table zonaproductiva add column tienecns int(1) after numero;
update zonaproductiva set tienecns = 0 where tienecns is null;

alter table acopiomp add column tienefac int(1) after boleta;
update acopiomp set tienefac = 0 where tienefac is null;

alter table acopiomp drop column tieneiva;

update acopiomp a set a.estado = 'CONTA' where a.estado = 'APR';
update zonaproductiva set idciudad = 4 where idciudad is null;

update zonaproductiva set tienecns = 1 where nombre = 'CHAUPISUYU';

-- AUX
select *
from acopiomp a where a.fecha between '2023-09-01' and '2023-10-10'
and a.idproductormateriaprima = 41; -- Pedro Cuba

update acopiomp a set a.estado = 'APR'
where a.fecha between '2023-09-01' and '2023-10-10';

update acopiomp a set a.tienefac = 0
where a.fecha between '2023-09-01' and '2023-10-10'
and a.idproductormateriaprima = 39; -- Feliciano

update acopiomp a set a.tienefac = 0
where a.fecha between '2023-09-01' and '2023-10-10'
and a.idproductormateriaprima = 41; -- Pedro Cuba


-- FELICIANO LIZARAZU REQUE - CHAUPISUYU 13, 15, 16, 17 Sep
-- PEDRO CUBA PEREZ - TAPACARI, LUYULUYUNI, CHAGUERY