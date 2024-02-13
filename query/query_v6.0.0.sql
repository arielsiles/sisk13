-- nuevo modulo de produccion de terdemol
INSERT INTO modulo (idmodulo, descripcion, nombrerecurso, idcompania)
VALUES (11, 'modulo de produccion de TERDEMOL', 'productionx', 1);
-- clonando

-- ----------------

-- drop table `xpr_categoria`;
-- drop table `xpr_formula`;
-- drop table `xpr_insumo`;
-- drop table `xpr_insumoformula`;
-- drop table `xpr_maquina`;
-- drop table `xpr_material`;
-- drop table `xpr_plan`;
-- drop table `xpr_proceso`;
-- drop table `xpr_prodafectado`;
-- drop table `xpr_produccion`;
-- drop table `xpr_produccionmaquina`;
-- drop table `xpr_producto`;
-- drop table `xpr_tanque`;

/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.6.10 : Database - khipus
*********************************************************************
*/


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
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `xpr_maquina`
--

DROP TABLE IF EXISTS `xpr_maquina`;

CREATE TABLE `xpr_maquina` (
                               `idmaquina` bigint(20) NOT NULL,
                               `nombre` varchar(255) DEFAULT NULL,
                               `VERSION` bigint(20) DEFAULT NULL,
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
                               `VERSION` bigint(20) DEFAULT NULL,
                               `idcompania` bigint(20) DEFAULT NULL,
                               PRIMARY KEY (`idproceso`)
);


--
-- Table structure for table `xpr_procesomaquina`
--

DROP TABLE IF EXISTS `xpr_procesomaquina`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;

CREATE TABLE `xpr_procesomaquina` (
                                      `idprocesomaquina` bigint(20) NOT NULL,
                                      `idmaquina` bigint(20) DEFAULT NULL,
                                      `idproceso` bigint(20) DEFAULT NULL,
                                      `VERSION` bigint(20) DEFAULT NULL,
                                      `idcompania` bigint(20) DEFAULT NULL,
                                      PRIMARY KEY (`idprocesomaquina`),
                                      KEY `xpr_procesomaqina_xpr_proceso_idproceso_fk` (`idproceso`),
                                      KEY `xpr_procesomaqina_xpr_maquina_idmaquina_fk` (`idmaquina`),
                                      CONSTRAINT `xpr_procesomaqina_xpr_maquina_idmaquina_fk` FOREIGN KEY (`idmaquina`) REFERENCES `xpr_maquina` (`idmaquina`),
                                      CONSTRAINT `xpr_procesomaqina_xpr_proceso_idproceso_fk` FOREIGN KEY (`idproceso`) REFERENCES `xpr_proceso` (`idproceso`)
);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-02-12 19:48:46