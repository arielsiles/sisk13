-- nuevo modulo de produccion de terdemol
INSERT INTO modulo (idmodulo, descripcion, nombrerecurso, idcompania)
VALUES (11, 'Produccion TERDEMOL', 'productionx', 1);
-- clonando

-- ----------------

/*!40101 SET NAMES utf8 */;
/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `xpr_categoria` */

CREATE TABLE `xpr_categoria` (
                                 `idcategoria` bigint(20) NOT NULL,
                                 `nombre` varchar(100) DEFAULT NULL,
                                 `VERSION` bigint(20) NOT NULL,
                                 `idcompania` bigint(20) NOT NULL,
                                 PRIMARY KEY (`idcategoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_formula` */

CREATE TABLE `xpr_formula` (
                               `idformula` bigint(20) NOT NULL,
                               `nombre` varchar(100) NOT NULL,
                               `estado` varchar(3) DEFAULT NULL,
                               `totaleq` decimal(16,2) NOT NULL,
                               `capacidad` decimal(16,2) DEFAULT NULL,
                               `activo` int(1) DEFAULT NULL,
                               `idcategoria` bigint(20) DEFAULT NULL,
                               `VERSION` bigint(20) NOT NULL,
                               `idcompania` bigint(20) NOT NULL,
                               PRIMARY KEY (`idformula`),
                               KEY `idcategoria` (`idcategoria`),
                               CONSTRAINT `xpr_formula_ibfk_1` FOREIGN KEY (`idcategoria`) REFERENCES `xpr_categoria` (`idcategoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_insumo` */

CREATE TABLE `xpr_insumo` (
                              `idinsumo` bigint(20) NOT NULL,
                              `cod_art` varchar(6) DEFAULT NULL,
                              `cantidad` decimal(16,6) NOT NULL DEFAULT '0.000000',
                              `costouni` decimal(16,6) DEFAULT NULL,
                              `tipo` varchar(15) DEFAULT NULL,
                              `idinsumoformula` bigint(20) DEFAULT NULL,
                              `idproduccion` bigint(20) NOT NULL,
                              `idproducto` bigint(20) DEFAULT NULL,
                              `VERSION` bigint(20) NOT NULL,
                              `idcompania` bigint(20) NOT NULL,
                              PRIMARY KEY (`idinsumo`),
                              KEY `idproduccion` (`idproduccion`),
                              KEY `idinsumoformula` (`idinsumoformula`),
                              KEY `idproducto` (`idproducto`),
                              CONSTRAINT `xpr_insumo_ibfk_1` FOREIGN KEY (`idproduccion`) REFERENCES `xpr_produccion` (`idproduccion`),
                              CONSTRAINT `xpr_insumo_ibfk_2` FOREIGN KEY (`idinsumoformula`) REFERENCES `xpr_insumoformula` (`idinsumoformula`),
                              CONSTRAINT `xpr_insumo_ibfk_3` FOREIGN KEY (`idproducto`) REFERENCES `xpr_producto` (`idproducto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xpr_insumoformula` */

CREATE TABLE `xpr_insumoformula` (
                                     `idinsumoformula` bigint(20) NOT NULL,
                                     `cantidad` decimal(20,6) NOT NULL,
                                     `cod_art` varchar(6) NOT NULL,
                                     `defecto` int(1) DEFAULT NULL,
                                     `idformula` bigint(20) NOT NULL,
                                     `idform` bigint(20) DEFAULT NULL,
                                     `VERSION` bigint(20) NOT NULL,
                                     `idcompania` bigint(20) NOT NULL,
                                     PRIMARY KEY (`idinsumoformula`),
                                     KEY `idformula` (`idformula`),
                                     KEY `idform` (`idform`),
                                     CONSTRAINT `xpr_insumoformula_ibfk_1` FOREIGN KEY (`idformula`) REFERENCES `xpr_formula` (`idformula`),
                                     CONSTRAINT `xpr_insumoformula_ibfk_2` FOREIGN KEY (`idform`) REFERENCES `xpr_formula` (`idformula`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_material` */

CREATE TABLE `xpr_material` (
                                `idmaterial` bigint(20) NOT NULL,
                                `cod_art` varchar(6) NOT NULL,
                                `cod_art_mat` varchar(6) NOT NULL,
                                `descri` varchar(100) DEFAULT NULL,
                                `flag_cant` int(1) DEFAULT NULL,
                                `tipo` varchar(15) DEFAULT NULL,
                                `vol1` decimal(10,2) DEFAULT NULL,
                                `peso1` decimal(10,2) DEFAULT NULL,
                                `VERSION` bigint(20) NOT NULL,
                                `idcompania` bigint(20) NOT NULL,
                                PRIMARY KEY (`idmaterial`),
                                KEY `cod_art` (`cod_art`),
                                KEY `cod_art_mat` (`cod_art_mat`),
                                CONSTRAINT `xpr_material_ibfk_1` FOREIGN KEY (`cod_art`) REFERENCES `inv_articulos` (`cod_art`),
                                CONSTRAINT `xpr_material_ibfk_2` FOREIGN KEY (`cod_art_mat`) REFERENCES `inv_articulos` (`cod_art`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_plan` */

CREATE TABLE `xpr_plan` (
                            `idplan` bigint(20) NOT NULL,
                            `nombre` varchar(100) DEFAULT NULL,
                            `fecha` date NOT NULL,
                            `estado` varchar(5) DEFAULT NULL,
                            `VERSION` bigint(20) NOT NULL,
                            `idcompania` bigint(20) NOT NULL,
                            PRIMARY KEY (`idplan`),
                            UNIQUE KEY `fecha` (`fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_prodafectado` */

CREATE TABLE `xpr_prodafectado` (
                                    `idprodafectado` bigint(20) NOT NULL,
                                    `idinsumo` bigint(20) NOT NULL,
                                    `idproducto` bigint(20) NOT NULL,
                                    `VERSION` bigint(20) NOT NULL,
                                    `idcompania` bigint(20) NOT NULL,
                                    PRIMARY KEY (`idprodafectado`),
                                    KEY `idinsumo` (`idinsumo`),
                                    KEY `idproducto` (`idproducto`),
                                    KEY `idcompania` (`idcompania`),
                                    CONSTRAINT `xpr_prodafectado_ibfk_1` FOREIGN KEY (`idinsumo`) REFERENCES `xpr_insumo` (`idinsumo`),
                                    CONSTRAINT `xpr_prodafectado_ibfk_2` FOREIGN KEY (`idproducto`) REFERENCES `xpr_producto` (`idproducto`),
                                    CONSTRAINT `xpr_prodafectado_ibfk_3` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_produccion` */

CREATE TABLE `xpr_produccion` (
                                  `idproduccion` bigint(20) NOT NULL,
                                  `codigo` int(11) DEFAULT NULL,
                                  `estado` varchar(5) DEFAULT NULL,
                                  `costototal` decimal(16,2) DEFAULT '0.00',
                                  `totalmp` decimal(16,2) DEFAULT '0.00',
                                  `descripcion` varchar(255) DEFAULT NULL,
                                  `idformula` bigint(20) DEFAULT NULL,
                                  `idtanque` bigint(20) DEFAULT NULL,
                                  `idplan` bigint(20) DEFAULT NULL,
                                  `id_tmpenc` bigint(20) DEFAULT NULL,
                                  `version` bigint(20) NOT NULL,
                                  `idcompania` bigint(20) NOT NULL,
                                  PRIMARY KEY (`idproduccion`),
                                  KEY `idformula` (`idformula`),
                                  KEY `idtanque` (`idtanque`),
                                  KEY `idplan` (`idplan`),
                                  CONSTRAINT `xpr_produccion_ibfk_1` FOREIGN KEY (`idformula`) REFERENCES `xpr_formula` (`idformula`),
                                  CONSTRAINT `xpr_produccion_ibfk_2` FOREIGN KEY (`idtanque`) REFERENCES `xpr_tanque` (`idtanque`),
                                  CONSTRAINT `xpr_produccion_ibfk_3` FOREIGN KEY (`idplan`) REFERENCES `xpr_plan` (`idplan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xpr_producto` */

CREATE TABLE `xpr_producto` (
                                `idproducto` bigint(20) NOT NULL,
                                `cod_art` varchar(6) DEFAULT NULL,
                                `cantidad` decimal(16,2) DEFAULT NULL,
                                `costo` decimal(16,2) DEFAULT '0.00',
                                `costouni` decimal(16,2) DEFAULT '0.00',
                                `costo_a` decimal(16,2) DEFAULT '0.00',
                                `costo_b` decimal(16,2) DEFAULT '0.00',
                                `costo_c` decimal(16,2) DEFAULT '0.00',
                                `idproduccion` bigint(20) DEFAULT NULL,
                                `idplan` bigint(20) DEFAULT NULL,
                                `VERSION` bigint(20) NOT NULL,
                                `idcompania` bigint(20) NOT NULL,
                                PRIMARY KEY (`idproducto`),
                                KEY `idproduccion` (`idproduccion`),
                                KEY `idplan` (`idplan`),
                                CONSTRAINT `xpr_producto_ibfk_1` FOREIGN KEY (`idproduccion`) REFERENCES `xpr_produccion` (`idproduccion`),
                                CONSTRAINT `xpr_producto_ibfk_2` FOREIGN KEY (`idproduccion`) REFERENCES `xpr_produccion` (`idproduccion`),
                                CONSTRAINT `xpr_producto_ibfk_3` FOREIGN KEY (`idplan`) REFERENCES `xpr_plan` (`idplan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xpr_tanque` */

CREATE TABLE `xpr_tanque` (
                              `idtanque` bigint(20) NOT NULL,
                              `nombre` varchar(255) NOT NULL,
                              `capacidad` decimal(16,2) NOT NULL,
                              `codmed` varchar(6) DEFAULT NULL,
                              `VERSION` bigint(20) NOT NULL,
                              `idcompania` bigint(20) NOT NULL,
                              PRIMARY KEY (`idtanque`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


/* **************procesos**********************/

--
-- Table structure for table `xpr_maquina`
--

DROP TABLE IF EXISTS `xpr_maquina`;

CREATE TABLE `xpr_maquina` (
                               `idmaquina` bigint(20) NOT NULL,
                               `nombre` varchar(255) DEFAULT NULL,
                               `version` bigint(20) DEFAULT NULL,
                               `idcompania` bigint(20) DEFAULT NULL,
                               PRIMARY KEY (`idmaquina`)
) ;


--
-- Table structure for table `xpr_proceso`
--

DROP TABLE IF EXISTS `xpr_proceso`;


CREATE TABLE `xpr_proceso` (
                               `idproceso` bigint(20) NOT NULL,
                               `nombre` varchar(255) NOT NULL,
                               `version` bigint(20) DEFAULT NULL,
                               `idcompania` bigint(20) DEFAULT NULL,
                               PRIMARY KEY (`idproceso`)
);


--
-- Table structure for table `xpr_procesomaquina`
--

DROP TABLE IF EXISTS `xpr_procesomaquina`;

CREATE TABLE `xpr_procesomaquina` (
                                      `idprocesomaquina` bigint(20) NOT NULL,
                                      `idmaquina` bigint(20) DEFAULT NULL,
                                      `idproceso` bigint(20) DEFAULT NULL,
                                      `version` bigint(20) DEFAULT NULL,
                                      `idcompania` bigint(20) DEFAULT NULL,
                                      PRIMARY KEY (`idprocesomaquina`),
                                      KEY `xpr_procesomaqina_xpr_proceso_idproceso_fk` (`idproceso`),
                                      KEY `xpr_procesomaqina_xpr_maquina_idmaquina_fk` (`idmaquina`),
                                      CONSTRAINT `xpr_procesomaqina_xpr_maquina_idmaquina_fk` FOREIGN KEY (`idmaquina`) REFERENCES `xpr_maquina` (`idmaquina`),
                                      CONSTRAINT `xpr_procesomaqina_xpr_proceso_idproceso_fk` FOREIGN KEY (`idproceso`) REFERENCES `xpr_proceso` (`idproceso`)
);

-- 12.02.2024
alter table xpr_produccion add column idproceso bigint(20) after idplan;
alter table xpr_produccion add foreign key (idproceso) references xpr_proceso (idproceso);

insert into xpr_proceso values (1, 'Proceso de Produccion Rumifos', 0, 1);
--
alter table acopiomp add column tieneiva int(1) after conta;
update acopiomp set tieneiva = 0 where tieneiva is null;

-- 14.02.2024
alter table configuracion add column cta_pat01 varchar(20);
alter table configuracion add column cta_pat02 varchar(20);
alter table configuracion add column cta_pat03 varchar(20);
alter table configuracion add column cta_pat04 varchar(20);
alter table configuracion add column cta_pat05 varchar(20);

-- For ILVA
update configuracion set cta_pat01 = '3100000000' where no_cia = '01';
update configuracion set cta_pat02 = '3200000000' where no_cia = '01';
update configuracion set cta_pat03 = '3300000000' where no_cia = '01';
update configuracion set cta_pat04 = '3400000000' where no_cia = '01';
update configuracion set cta_pat05 = '3500000000' where no_cia = '01';

-- For Terdemol

delete from arcgms where cuenta = '32000300';
delete from arcgms where cuenta = '32000301';

update configuracion set cta_pat01 = '31000000' where no_cia = '01';
update configuracion set cta_pat02 = '32000000' where no_cia = '01';
update configuracion set cta_pat03 = '33000000' where no_cia = '01';
update configuracion set cta_pat04 = '34000000' where no_cia = '01';
update configuracion set cta_pat05 = '35000000' where no_cia = '01';

--
update arcgms set cuenta = '35000000', cta_raiz = '35000000' where cuenta = '33000000';
update arcgms set cuenta = '35000100', cta_raiz = '35000000' where cuenta = '33000100';
update arcgms set cuenta = '35000101', cta_raiz = '35000000' where cuenta = '33000101';
update arcgms set cuenta = '35000200', cta_raiz = '35000000' where cuenta = '33000200';
update arcgms set cuenta = '35000201', cta_raiz = '35000000' where cuenta = '33000201';

update arcgms set cuenta = '34000000', cta_raiz = '34000000' where cuenta = '32000000';
update arcgms set cuenta = '34000100', cta_raiz = '34000000' where cuenta = '32000100';
update arcgms set cuenta = '34000101', cta_raiz = '34000000' where cuenta = '32000101';
update arcgms set cuenta = '34000200', cta_raiz = '34000000' where cuenta = '32000200';
update arcgms set cuenta = '34000201', cta_raiz = '34000000' where cuenta = '32000201';

insert into `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `est`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `util`, `nomutil`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`)
values('32000000','APORTES NO CAPITALIZABLES','32000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'01',NULL,'C','S','N',NULL,'N',NULL,NULL,'P',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,'S',NULL,NULL);
insert into `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `est`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `util`, `nomutil`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`)
values('32000100','APORTES NO CAPITALIZABLES','32000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'01',NULL,'C','S','N',NULL,'N',NULL,NULL,'P',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,'S',NULL,NULL);

insert into `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `est`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `util`, `nomutil`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`)
values('33000000','AJUSTES AL PATRIMONIO','33000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'01',NULL,'C','S','N',NULL,'N',NULL,NULL,'P',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,'S',NULL,NULL);
insert into `arcgms` (`cuenta`, `descri`, `cta_raiz`, `cta_niv3`, `est`, `cn_ana`, `cn_nivel`, `cn_dv`, `cn_tip`, `cn_act`, `no_cia`, `clase`, `tipo`, `activa`, `util`, `nomutil`, `permite_iva`, `ind_presup`, `creditos`, `moneda`, `debitos`, `saldo_mes_ant_dol`, `saldo_per_ant_dol`, `creditos_dol`, `debitos_dol`, `gru_cta`, `permiso_con`, `exije_cc`, `permiso_afijo`, `permiso_cxp`, `permiso_cxc`, `permiso_che`, `permiso_inv`, `f_inactiva`, `ind_mov`, `saldo_mes_ant`, `saldo_per_ant`)
values('33000100','AJUSTES AL PATRIMONIO','33000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'01',NULL,'C','S','N',NULL,'N',NULL,NULL,'P',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,'S',NULL,NULL);
