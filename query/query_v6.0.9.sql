/** **/
DROP TABLE IF EXISTS inspeccionzona;
CREATE TABLE `inspeccionzona`(
                                 `idinspeccionzona` BIGINT(20) NOT NULL,
                                 `fecha` DATE,
                                 `horasalida` TIME,
                                 `horallegada` TIME,
                                 `idproductor` BIGINT(20),
                                 `idzonaproductiva` BIGINT(20),
                                 `version` BIGINT(20),
                                 `idcompania` BIGINT(20),
                                 PRIMARY KEY (`idinspeccionzona`),
                                 CONSTRAINT `inspeccionzona_ibfk_1` FOREIGN KEY (`idproductor`) REFERENCES `productormateriaprima`(`idproductormateriaprima`),
                                 CONSTRAINT `inspeccionzona_ibfk_2` FOREIGN KEY (`idzonaproductiva`) REFERENCES `zonaproductiva`(`idzonaproductiva`),
                                 CONSTRAINT `inspeccionzona_ibfk_3` FOREIGN KEY (`idcompania`) REFERENCES `compania`(`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS materiasprimaszona;
CREATE TABLE `materiasprimaszona` (
                                      `idmateriasprimaszona` bigint(20) NOT NULL,
                                      `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
                                      `precioorigen` decimal(12,2) DEFAULT NULL,
                                      `preciodestino` decimal(12,2) DEFAULT NULL,
                                      `observacion` varchar(255) DEFAULT NULL,
                                      `idinspeccionzona` bigint(20) DEFAULT NULL,
                                      `version` BIGINT(20),
                                      `idcompania` BIGINT(20),
                                      PRIMARY KEY (`idmateriasprimaszona`),
                                      KEY `materiasprimaszona_ibfk_1` (`idmetaproductoproduccion`),
                                      KEY `materiasprimaszona_ibfk_2` (`idinspeccionzona`),
                                      CONSTRAINT `materiasprimaszona_ibfk_1` FOREIGN KEY (`idmetaproductoproduccion`) REFERENCES `metaproductoproduccion` (`idmetaproductoproduccion`),
                                      CONSTRAINT `materiasprimaszona_ibfk_2` FOREIGN KEY (`idinspeccionzona`) REFERENCES `inspeccionzona` (`idinspeccionzona`),
                                      CONSTRAINT `materiasprimaszona_ibfk_3` FOREIGN KEY (`idcompania`) REFERENCES `compania`(`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


alter table archivo add primary key (idarchivo);

DROP TABLE IF EXISTS resultadolab;
CREATE TABLE `resultadolab` (
                                `idresultadolab` bigint(20) NOT NULL,
                                `fecha` date DEFAULT NULL,
                                `codigo` varchar(100) DEFAULT NULL,
                                `caracteristica` varchar(255) DEFAULT NULL,
                                `fechamuestra` date DEFAULT NULL,
                                `fecharecepcion` date DEFAULT NULL,
                                `fechainicio` date DEFAULT NULL,
                                `fechafin` date DEFAULT NULL,
                                `idinspeccionzona` bigint(20) DEFAULT NULL,
                                `idarchivo` bigint(20) DEFAULT NULL,
                                `version` BIGINT(20),
                                `idcompania` BIGINT(20),
                                PRIMARY KEY (`idresultadolab`),
                                KEY `informelab_ibfk_1` (`idinspeccionzona`),
                                CONSTRAINT `resultadolab_ibfk_1` FOREIGN KEY (`idinspeccionzona`) REFERENCES `inspeccionzona` (`idinspeccionzona`),
                                CONSTRAINT `resultadolab_ibfk_2` FOREIGN KEY (`idcompania`) REFERENCES `compania`(`idcompania`),
                                CONSTRAINT `resultadolab_ibfk_3` FOREIGN KEY (`idarchivo`) REFERENCES `archivo` (`idarchivo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/** **/
-- 27.03.2024
INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(301, 'ZONEINSPECTION', 6, 15, 'menu.production.configuration.zoneInspection', 1);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(302, 'LABORATORYRESULT', 6, 15, 'Functionality.production.zoneInspection.laboratoryResult', 1);

INSERT INTO funcionalidad(idfuncionalidad, codigo, idmodulo, permiso, nombrerecurso, idcompania)
VALUES(303, 'LOCALRAWMATERIAL', 6, 15, 'Functionality.production.zoneInspection.localRawMaterail', 1);

-- 06.04.2024
alter table inv_grupos add column cta_gasto varchar(31) after cta_costo;

update inv_grupos set cta_gasto = '53001400' where cod_gru = '9';
update inv_grupos set cta_gasto = '53001700' where cod_gru = '12';
update inv_grupos set cta_gasto = '53001100' where cod_gru = '6';
update inv_grupos set cta_gasto = '53001200' where cod_gru = '7';
update inv_grupos set cta_gasto = '53001600' where cod_gru = '11';
update inv_grupos set cta_gasto = '53001500' where cod_gru = '10';
update inv_grupos set cta_gasto = '53001300' where cod_gru = '8';
update inv_grupos set cta_gasto = '51000100' where cod_gru = '1';
update inv_grupos set cta_gasto = '53001000' where cod_gru = '5';
update inv_grupos set cta_gasto = '51000000' where cod_gru = '14';
update inv_grupos set cta_gasto = '53000900' where cod_gru = '3';
update inv_grupos set cta_gasto = '53000800' where cod_gru = '13';
update inv_grupos set cta_gasto = '51000711' where cod_gru = '4';
update inv_grupos set cta_gasto = '51000701' where cod_gru = '2';

--
alter table inv_vales add column tipo_gasto varchar(100) after estado;