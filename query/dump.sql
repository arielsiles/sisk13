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
/*Table structure for table `_sequence` */

CREATE TABLE `_sequence` (
  `seq_name` varchar(50) NOT NULL,
  `seq_val` int(10) unsigned NOT NULL,
  PRIMARY KEY (`seq_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `acopiomateriaprima` */

CREATE TABLE `acopiomateriaprima` (
  `idacopiomateriaprima` bigint(20) NOT NULL,
  `cantidad` decimal(16,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idsesionacopio` bigint(20) DEFAULT NULL,
  `idproductormateriaprima` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idacopiomateriaprima`),
  KEY `fk_companiaacopiomateriaprima` (`idcompania`),
  KEY `fk_productormateriaprimaacopiomateriaprima` (`idproductormateriaprima`),
  KEY `fk_sesionacopioacopiomateriaprima` (`idsesionacopio`),
  CONSTRAINT `fk_companiaacopiomateriaprima` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_productormateriaprimaacopiomateriaprima` FOREIGN KEY (`idproductormateriaprima`) REFERENCES `productormateriaprima` (`idproductormateriaprima`),
  CONSTRAINT `fk_sesionacopioacopiomateriaprima` FOREIGN KEY (`idsesionacopio`) REFERENCES `sesionacopio` (`idsesionacopio`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `acreedor` */

CREATE TABLE `acreedor` (
  `idacreedor` bigint(20) DEFAULT NULL,
  `nombre` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `actividad` */

CREATE TABLE `actividad` (
  `idactividad` bigint(20) DEFAULT NULL,
  `codigo` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacreacion` date DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechamodificacion` date DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idprograma` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuariocreador` bigint(20) DEFAULT NULL,
  `idusuarioeditor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `af_activos` */

CREATE TABLE `af_activos` (
  `idactivo` bigint(20) DEFAULT NULL,
  `dep_acu_vo` decimal(12,2) DEFAULT NULL,
  `fch_ajuste` date DEFAULT NULL,
  `codbarras` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `vobs` decimal(12,2) DEFAULT NULL,
  `tasabssus` decimal(16,6) DEFAULT NULL,
  `tasabsufv` decimal(16,6) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `dep_vo` decimal(12,2) DEFAULT NULL,
  `tasa_dep` decimal(7,2) DEFAULT NULL,
  `descri` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `detalle` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `duracion` int(11) DEFAULT NULL,
  `fch_baja` date DEFAULT NULL,
  `cod_acti` bigint(20) DEFAULT NULL,
  `grupo` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `subgrupo` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `mej` decimal(12,2) DEFAULT NULL,
  `ulttasabssus` decimal(16,6) DEFAULT NULL,
  `ulttasabsufv` decimal(16,6) DEFAULT NULL,
  `medida` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `modelo` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `mesesgarantia` int(11) DEFAULT NULL,
  `id_com_encoc` bigint(20) DEFAULT NULL,
  `fch_alta` date DEFAULT NULL,
  `desecho` decimal(12,2) DEFAULT NULL,
  `serie` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `vosus` decimal(12,2) DEFAULT NULL,
  `marca` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `voufv` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `idaflocalizacion` bigint(20) DEFAULT NULL,
  `idfoto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `af_grupos` */

CREATE TABLE `af_grupos` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `grupo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `af_hdepre` */

CREATE TABLE `af_hdepre` (
  `idhdepre` bigint(20) DEFAULT NULL,
  `dep_acu_vo` decimal(12,2) DEFAULT NULL,
  `dep_acu_vo_bs` decimal(12,2) DEFAULT NULL,
  `dep_vo_bs` decimal(12,2) DEFAULT NULL,
  `tasabssus` decimal(16,6) DEFAULT NULL,
  `tasabsufv` decimal(16,6) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `dep_vo` decimal(12,2) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `tasa_dep` decimal(7,2) DEFAULT NULL,
  `fecha_en_proceso` date DEFAULT NULL,
  `val_tot` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `custodio` bigint(20) DEFAULT NULL,
  `idactivo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `af_movs` */

CREATE TABLE `af_movs` (
  `id_af_movs` bigint(20) DEFAULT NULL,
  `monto` decimal(12,2) DEFAULT NULL,
  `montobs` decimal(12,2) DEFAULT NULL,
  `tasabssus` decimal(16,6) DEFAULT NULL,
  `tasabsufv` decimal(16,6) DEFAULT NULL,
  `motivo` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha_cre` date DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `dep_ini` decimal(12,2) DEFAULT NULL,
  `cod_cc_ant` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tasabssusmesant` decimal(16,6) DEFAULT NULL,
  `tasabsufvmesant` decimal(16,6) DEFAULT NULL,
  `cod_mov` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `no_mov` bigint(20) DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `montoufv` decimal(12,2) DEFAULT NULL,
  `no_usr` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `no_compro` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `custodio` bigint(20) DEFAULT NULL,
  `idactivo` bigint(20) DEFAULT NULL,
  `idpago` bigint(20) DEFAULT NULL,
  `idafvale` bigint(20) DEFAULT NULL,
  `idunidadnegocioant` bigint(20) DEFAULT NULL,
  `custodio_ant` bigint(20) DEFAULT NULL,
  `idaflocalizacion_ant` bigint(20) DEFAULT NULL,
  `idaflocalizacion_nvo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `af_pago` */

CREATE TABLE `af_pago` (
  `idpago` bigint(20) DEFAULT NULL,
  `cuentabanco` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombrebeneficiario` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuentacaja` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fechacreacion` date DEFAULT NULL,
  `descripcion` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipocambio` decimal(16,6) DEFAULT NULL,
  `tipobeneficiario` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `montopago` decimal(12,2) DEFAULT NULL,
  `monedapago` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipopago` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `montoorigen` decimal(12,2) DEFAULT NULL,
  `monedaorigen` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `notrans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idsededestinocheque` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `af_subgrupos` */

CREATE TABLE `af_subgrupos` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `grupo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `subgrupo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctadavo` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tasa_dep` decimal(6,2) DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `detalle` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `duracion` int(11) DEFAULT NULL,
  `ctagavo` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_acti` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctamej` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cta_vo` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `requierepartes` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `desecho` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `cta_alm` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `af_tipomovs` */

CREATE TABLE `af_tipomovs` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_mov` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_mov` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `aflocalizacion` */

CREATE TABLE `aflocalizacion` (
  `idaflocalizacion` bigint(20) DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `afvale` */

CREATE TABLE `afvale` (
  `idafvale` bigint(20) DEFAULT NULL,
  `montobs` decimal(12,2) DEFAULT NULL,
  `tasabssus` decimal(16,6) DEFAULT NULL,
  `tasabsufv` decimal(16,6) DEFAULT NULL,
  `motivo` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `creadopor` bigint(20) DEFAULT NULL,
  `fechacreacion` date DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `montosus` decimal(12,2) DEFAULT NULL,
  `no_trans` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `montoufv` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `codigo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipovale` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `cpcustodio` bigint(20) DEFAULT NULL,
  `idaflocalizacion` bigint(20) DEFAULT NULL,
  `idpago` bigint(20) DEFAULT NULL,
  `idordencompra` bigint(20) DEFAULT NULL,
  `actualizadopor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ambientedeposito` */

CREATE TABLE `ambientedeposito` (
  `idambientedeposito` bigint(20) DEFAULT NULL,
  `codigo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `iddepositoproductoterminado` bigint(20) DEFAULT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `arcgcc` */

CREATE TABLE `arcgcc` (
  `clase_cambio` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descripcion` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `arcgms` */

CREATE TABLE `arcgms` (
  `cuenta` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cta_raiz` varchar(20) DEFAULT NULL,
  `cta_niv3` varchar(20) DEFAULT NULL,
  `est` varchar(3) DEFAULT NULL,
  `cn_ana` varchar(1) DEFAULT NULL,
  `cn_nivel` int(11) DEFAULT NULL,
  `cn_dv` int(11) DEFAULT NULL,
  `cn_tip` varchar(1) DEFAULT NULL,
  `cn_act` int(11) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `clase` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activa` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `permite_iva` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ind_presup` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `creditos` decimal(14,2) DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `debitos` decimal(14,2) DEFAULT NULL,
  `saldo_mes_ant_dol` decimal(20,6) DEFAULT NULL,
  `saldo_per_ant_dol` decimal(20,6) DEFAULT NULL,
  `creditos_dol` decimal(20,6) DEFAULT NULL,
  `debitos_dol` decimal(20,6) DEFAULT NULL,
  `gru_cta` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `permiso_con` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `exije_cc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `permiso_afijo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `permiso_cxp` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `permiso_cxc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `permiso_che` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `permiso_inv` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `f_inactiva` date DEFAULT NULL,
  `ind_mov` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `saldo_mes_ant` decimal(15,2) DEFAULT NULL,
  `saldo_per_ant` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`cuenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `arcgtc` */

CREATE TABLE `arcgtc` (
  `fecha` date DEFAULT NULL,
  `clase_cambio` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_cambio` decimal(10,6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `archivo` */

CREATE TABLE `archivo` (
  `tipo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idarchivo` bigint(20) DEFAULT NULL,
  `tipocontenido` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tamanio` bigint(20) DEFAULT NULL,
  `valor` longblob,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `articulo_por_proveedor` */

CREATE TABLE `articulo_por_proveedor` (
  `id_articulo_por_proveedor` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `entrega` int(11) DEFAULT NULL,
  `precio_grupo` decimal(12,6) DEFAULT NULL,
  `cod_med_may` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_art` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_prov` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `articulos_pedido` */

CREATE TABLE `articulos_pedido` (
  `IDARTICULOSPEDIDO` bigint(20) NOT NULL,
  `CAJA` bigint(20) DEFAULT NULL,
  `CANTIDAD` int(11) DEFAULT NULL,
  `COD_ALM` varchar(6) DEFAULT NULL,
  `cod_art` varchar(6) DEFAULT NULL,
  `no_cia` varchar(2) DEFAULT NULL,
  `PRECIO` double DEFAULT NULL,
  `PRECIO_INV` double DEFAULT NULL,
  `PROMOCION` int(11) DEFAULT NULL,
  `REPOSICION` int(11) DEFAULT NULL,
  `TOTAL` int(11) DEFAULT NULL,
  `descuento` double DEFAULT NULL,
  `TOTAL_INV` int(11) DEFAULT NULL,
  `IMPORTE` double NOT NULL,
  `costo_uni` decimal(16,6) DEFAULT NULL,
  `cu` decimal(16,6) DEFAULT NULL,
  `IDPEDIDOS` bigint(20) DEFAULT NULL,
  `estado` varchar(20) DEFAULT NULL,
  `POR_REPONER` int(11) DEFAULT NULL,
  `tipo` varchar(30) DEFAULT NULL,
  `IDVENTADIRECTA` bigint(20) DEFAULT NULL,
  `cod_art2` varchar(6) DEFAULT NULL,
  PRIMARY KEY (`IDARTICULOSPEDIDO`),
  KEY `FK_ARTICULOS_PEDIDO_PEDIDOS` (`IDPEDIDOS`),
  CONSTRAINT `FK_ARTICULOS_PEDIDO_PEDIDOS` FOREIGN KEY (`IDPEDIDOS`) REFERENCES `pedidos` (`IDPEDIDOS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `articulospromocion` */

CREATE TABLE `articulospromocion` (
  `IDARTICULOSPROMOCION` bigint(20) NOT NULL,
  `CANTIDAD` int(11) DEFAULT NULL,
  `IDPROMOCION` bigint(20) NOT NULL,
  `IDVENTAARTICULO` bigint(20) NOT NULL,
  PRIMARY KEY (`IDARTICULOSPROMOCION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `asignatura` */

CREATE TABLE `asignatura` (
  `idasignatura` bigint(20) DEFAULT NULL,
  `codigo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idreferencia` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcarrera` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `asignaturas` */

CREATE TABLE `asignaturas` (
  `asignatura` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sigla` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `unidad_acad_adm` int(11) DEFAULT NULL,
  `tipo_asignatura` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `costo` int(11) DEFAULT NULL,
  `fecha_creacion` date DEFAULT NULL,
  `creditos` int(11) DEFAULT NULL,
  `area_de_conocimiento` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `carga_hpractica` int(11) DEFAULT NULL,
  `cuota` int(11) DEFAULT NULL,
  `carga_horaria` int(11) DEFAULT NULL,
  `desc_corta` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `carga_hteorica` int(11) DEFAULT NULL,
  `unidad` int(11) DEFAULT NULL,
  `universidad` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `auxinv` */

CREATE TABLE `auxinv` (
  `cod_art` varchar(100) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `inicial` decimal(16,2) DEFAULT NULL,
  `cu` decimal(16,6) DEFAULT NULL,
  `cantidad` decimal(16,2) DEFAULT NULL,
  `und` varchar(100) DEFAULT NULL,
  `alm` varchar(100) DEFAULT NULL,
  `costo_uni` decimal(16,6) DEFAULT NULL,
  `costo_prom` decimal(16,6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `banco` */

CREATE TABLE `banco` (
  `fechaactivacion` date DEFAULT NULL,
  `numeroadenda` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechabaja` date DEFAULT NULL,
  `fechafincontrato` date DEFAULT NULL,
  `numerocontrato` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(15) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechaestado` date DEFAULT NULL,
  `idbanco` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `bandahoraria` */

CREATE TABLE `bandahoraria` (
  `idbandahoraria` bigint(20) DEFAULT NULL,
  `duracion` int(11) DEFAULT NULL,
  `diafin` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `horafin` time DEFAULT NULL,
  `diapormedio` int(11) DEFAULT NULL,
  `diainicio` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `horainicio` time DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipobandahoraria` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `bandahorariacontrato` */

CREATE TABLE `bandahorariacontrato` (
  `idbandahorariacontrato` bigint(20) DEFAULT NULL,
  `horarioacademico` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `edificio` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `ambiente` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `pivotecosto` int(11) DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `grupoasignatura` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `nombreasignatura` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `precioperiodo` decimal(16,6) DEFAULT NULL,
  `compartido` int(11) DEFAULT NULL,
  `asignatura` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipohora` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idbandahoraria` bigint(20) DEFAULT NULL,
  `idlimite` bigint(20) DEFAULT NULL,
  `idtolerancia` bigint(20) DEFAULT NULL,
  `idtipobandahoraria` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `barrio` */

CREATE TABLE `barrio` (
  `idbarrio` bigint(20) NOT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idzona` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idbarrio`),
  KEY `fk_companiabarrio` (`idcompania`),
  KEY `fk_zonabarrio` (`idzona`),
  CONSTRAINT `fk_companiabarrio` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_zonabarrio` FOREIGN KEY (`idzona`) REFERENCES `zona` (`idzona`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `bono` */

CREATE TABLE `bono` (
  `idbono` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `tipobono` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `iddescripcion` bigint(20) DEFAULT NULL,
  `idtasasmn` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `bonoantiguedad` */

CREATE TABLE `bonoantiguedad` (
  `fechafin` date DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `idbonoantiguedad` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `bonoconseguido` */

CREATE TABLE `bonoconseguido` (
  `idbonoconseguido` bigint(20) NOT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idbono` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idciclogeneracionplanilla` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idbonoconseguido`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `caja` */

CREATE TABLE `caja` (
  `idcaja` bigint(20) NOT NULL DEFAULT '0',
  `fechacreacion` date DEFAULT NULL,
  `descripcion` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `autorizacionrequerida` int(11) DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechaestado` date DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipocaja` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcaja`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cajausuario` */

CREATE TABLE `cajausuario` (
  `idcaja` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL,
  `fechacierre` date DEFAULT NULL,
  `fechaapertura` date DEFAULT NULL,
  `estado` varchar(15) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `validar` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  KEY `idcaja` (`idcaja`),
  KEY `idusuario` (`idusuario`),
  CONSTRAINT `cajausuario_ibfk_1` FOREIGN KEY (`idcaja`) REFERENCES `caja` (`idcaja`),
  CONSTRAINT `cajausuario_ibfk_2` FOREIGN KEY (`idusuario`) REFERENCES `usuario` (`idusuario`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `calle` */

CREATE TABLE `calle` (
  `idcalle` bigint(20) NOT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idbarrio` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcalle`),
  KEY `fk_barriocalle` (`idbarrio`),
  KEY `fk_companiacalle` (`idcompania`),
  CONSTRAINT `fk_barriocalle` FOREIGN KEY (`idbarrio`) REFERENCES `barrio` (`idbarrio`),
  CONSTRAINT `fk_companiacalle` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cambiobandahorario` */

CREATE TABLE `cambiobandahorario` (
  `idcambiohorario` bigint(20) DEFAULT NULL,
  `carrera` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idcarrera` int(11) DEFAULT NULL,
  `fechacambio` date DEFAULT NULL,
  `ciactual` int(11) DEFAULT NULL,
  `diaactual` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `hfinactual` time DEFAULT NULL,
  `hinicioactual` time DEFAULT NULL,
  `grupo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sede` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `materno` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombres` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cinuevo` int(11) DEFAULT NULL,
  `dianuevo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `hfinnuevo` time DEFAULT NULL,
  `maternonuevo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombresnuevo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `paternonuevo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `hinicionuevo` time DEFAULT NULL,
  `paterno` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `asignatura` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cargo` */

CREATE TABLE `cargo` (
  `idcargo` bigint(20) NOT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcargo`),
  KEY `fk_companiacargo` (`idcompania`),
  CONSTRAINT `fk_companiacargo` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `carrera` */

CREATE TABLE `carrera` (
  `idcarrera` bigint(20) DEFAULT NULL,
  `codigo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idreferencia` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idfacultad` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `categoriacliente` */

CREATE TABLE `categoriacliente` (
  `idcategoriacliente` bigint(20) NOT NULL DEFAULT '0',
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(64) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcategoriacliente`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `categoriaclubsocio` */

CREATE TABLE `categoriaclubsocio` (
  `idcategoriaclubsocio` bigint(20) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `categoriapuesto` */

CREATE TABLE `categoriapuesto` (
  `idcategoriapuesto` bigint(20) NOT NULL,
  `sigla` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctaprovaguime` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctaprevindemme` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctactbhaberme` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctactbdebeme` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctagastoaguimn` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctaprovaguimn` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctagastoindemmn` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctaprevindemmn` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctactbhaber` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codctactbdebe` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipogeneracion` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fonpenctapatronal` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `posicion` int(11) DEFAULT NULL,
  `idsector` bigint(20) DEFAULT NULL,
  `segsocctapatronal` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idlimite` bigint(20) DEFAULT NULL,
  `idtolerancia` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcategoriapuesto`),
  KEY `fk_companiacategoriapuesto` (`idcompania`),
  KEY `fk_limitecategoriapuesto` (`idlimite`),
  KEY `fk_sectorcategoriapuesto` (`idsector`),
  KEY `fk_toleranciacategoriapuesto` (`idtolerancia`),
  CONSTRAINT `fk_companiacategoriapuesto` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_limitecategoriapuesto` FOREIGN KEY (`idlimite`) REFERENCES `limite` (`idlimite`),
  CONSTRAINT `fk_sectorcategoriapuesto` FOREIGN KEY (`idsector`) REFERENCES `sector` (`idsector`),
  CONSTRAINT `fk_toleranciacategoriapuesto` FOREIGN KEY (`idtolerancia`) REFERENCES `tolerancia` (`idtolerancia`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `categorias` */

CREATE TABLE `categorias` (
  `cod` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descripcion` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `causaretiro` */

CREATE TABLE `causaretiro` (
  `idcausaretiro` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `permitepagos` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cg_cencos` */

CREATE TABLE `cg_cencos` (
  `cod_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cons_excl` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `gru_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ind_mov` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_detplanti` */

CREATE TABLE `cg_detplanti` (
  `id_cg_detplanti` bigint(20) DEFAULT NULL,
  `haber` decimal(16,2) DEFAULT NULL,
  `debe` decimal(16,2) DEFAULT NULL,
  `ref` varchar(60) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_planti` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_uni` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_grucc` */

CREATE TABLE `cg_grucc` (
  `gru_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_moneda` */

CREATE TABLE `cg_moneda` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_mon` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `abrev` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `clase_cambio` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_movdet` */

CREATE TABLE `cg_movdet` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_linea` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `monto_mn` decimal(16,2) DEFAULT NULL,
  `cod_cc` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tc` decimal(10,6) DEFAULT NULL,
  `cod_uni` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `referencia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_mov` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_compro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_movmae` */

CREATE TABLE `cg_movmae` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_compro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `monto_total_mn` decimal(16,2) DEFAULT NULL,
  `fecha_cre` date DEFAULT NULL,
  `glosa` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `max_no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_imp` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `registrado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `origen` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_usr` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_plantillas` */

CREATE TABLE `cg_plantillas` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_planti` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `obs` varchar(600) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_unidades` */

CREATE TABLE `cg_unidades` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_uej` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ciclo` */

CREATE TABLE `ciclo` (
  `idciclo` bigint(20) NOT NULL,
  `activo` int(11) DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `tipocambiome` decimal(15,2) DEFAULT NULL,
  `diaslaborales` int(11) DEFAULT NULL,
  `semanaslaborales` decimal(15,2) DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipociclo` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `cicloraiz` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idciclo`),
  KEY `fk_companiaciclo` (`idcompania`),
  CONSTRAINT `fk_companiaciclo` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ciclogeneracionplanilla` */

CREATE TABLE `ciclogeneracionplanilla` (
  `idciclogeneracionplanilla` bigint(20) NOT NULL DEFAULT '0',
  `fechacreacion` date DEFAULT NULL,
  `fechafinfiscal` date DEFAULT NULL,
  `tipocambiofinalufv` decimal(16,6) DEFAULT NULL,
  `fechaaperturagen` date DEFAULT NULL,
  `fechalimitegen` date DEFAULT NULL,
  `fechafingen` date DEFAULT NULL,
  `fechainiciogen` date DEFAULT NULL,
  `tipocambioinicialufv` decimal(16,6) DEFAULT NULL,
  `mes` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechaplanoficial` date DEFAULT NULL,
  `fechainiciofiscal` date DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idtasaafp` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idtasacns` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipocambio` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `idtasaiva` bigint(20) DEFAULT NULL,
  `idregladescuento` bigint(20) DEFAULT NULL,
  `idtasaafpprohous` bigint(20) DEFAULT NULL,
  `idtasaafpprofrisk` bigint(20) DEFAULT NULL,
  `idtasasmn` bigint(20) DEFAULT NULL,
  `idtasaafpsolidario` bigint(20) DEFAULT NULL,
  `idtasaafplabindividual` bigint(20) DEFAULT NULL,
  `idtasaafplabriesgocomun` bigint(20) DEFAULT NULL,
  `idtasaafplabsolidario` bigint(20) DEFAULT NULL,
  `idtasaafplabcomision` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idciclogeneracionplanilla`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ciudad` */

CREATE TABLE `ciudad` (
  `idciudad` bigint(20) NOT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `iddepartamento` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idciudad`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ck_bancos` */

CREATE TABLE `ck_bancos` (
  `cod_bco` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ck_ctas_bco` */

CREATE TABLE `ck_ctas_bco` (
  `cta_bco` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta` varchar(31) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_bco` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ck_docus` */

CREATE TABLE `ck_docus` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `monto` decimal(16,4) DEFAULT NULL,
  `cta_bco` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_benef` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `beneficiario` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta` varchar(31) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_conci` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `entregadopor` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fechaentrega` date DEFAULT NULL,
  `obsentrega` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_doc` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_doc` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tc` decimal(10,6) DEFAULT NULL,
  `mod_aut` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `entregado` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `procedencia` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `origen` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_compro` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ck_movs` */

CREATE TABLE `ck_movs` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha_cre` date DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `descri` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_mov` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_usr` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_compro` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ck_tipodocs` */

CREATE TABLE `ck_tipodocs` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_doc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `dosificador` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_mov` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `clasifcuenta` */

CREATE TABLE `clasifcuenta` (
  `idclasifcuenta` bigint(20) DEFAULT NULL,
  `codigocuenta` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idclasificador` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `clasificador` */

CREATE TABLE `clasificador` (
  `idclasificador` bigint(20) DEFAULT NULL,
  `codigo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cliente` */

CREATE TABLE `cliente` (
  `idcliente` bigint(20) DEFAULT NULL,
  `fechaprimeracompra` date DEFAULT NULL,
  `fechaultimacompra` date DEFAULT NULL,
  `nocliente` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `totalimporteadquirido` decimal(13,2) DEFAULT NULL,
  `totalarticulosadquiridos` int(11) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cobrofondorota` */

CREATE TABLE `cobrofondorota` (
  `idcobro` bigint(20) DEFAULT NULL,
  `fechaaprobacion` date DEFAULT NULL,
  `cuentabanco` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerodeposito` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombrebeneficiario` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipobeneficiario` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentaajuste` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentacaja` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo` int(11) DEFAULT NULL,
  `montocobro` decimal(12,2) DEFAULT NULL,
  `monedacobro` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacobro` date DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacreacion` date DEFAULT NULL,
  `no_trans_dep` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocambio` decimal(12,2) DEFAULT NULL,
  `observacion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `saldocuota` decimal(13,2) DEFAULT NULL,
  `tipocobro` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `montoorigen` decimal(12,2) DEFAULT NULL,
  `monedaorigen` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notrans` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `anuladopor` bigint(20) DEFAULT NULL,
  `aprobadopor` bigint(20) DEFAULT NULL,
  `iddocumentocobro` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idgestionplanilla` bigint(20) DEFAULT NULL,
  `idpagoordencompra` bigint(20) DEFAULT NULL,
  `idcuota` bigint(20) DEFAULT NULL,
  `entregadoa` bigint(20) DEFAULT NULL,
  `creadopor` bigint(20) DEFAULT NULL,
  `idfondorotatorio` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `codigopedidosecuencia` */

CREATE TABLE `codigopedidosecuencia` (
  `secuencia` bigint(20) NOT NULL,
  PRIMARY KEY (`secuencia`),
  UNIQUE KEY `unique_secuencia` (`secuencia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `com_af_detoc` */

CREATE TABLE `com_af_detoc` (
  `id_com_af_detoc` bigint(20) DEFAULT NULL,
  `tasabssus` decimal(16,6) DEFAULT NULL,
  `total_bs` decimal(16,2) DEFAULT NULL,
  `tasabsufv` decimal(16,6) DEFAULT NULL,
  `vobs` decimal(16,2) DEFAULT NULL,
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `detalle` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nro` bigint(20) DEFAULT NULL,
  `grupo` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `subgrupo` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `medida` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `modelo` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `mesesgarantia` int(11) DEFAULT NULL,
  `duracion_neta` int(11) DEFAULT NULL,
  `no_orden` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cant_sol` int(11) DEFAULT NULL,
  `desecho` decimal(12,2) DEFAULT NULL,
  `total_sus` decimal(16,2) DEFAULT NULL,
  `vosus` decimal(16,2) DEFAULT NULL,
  `duracion_total` int(11) DEFAULT NULL,
  `marca` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `total_ufv` decimal(16,2) DEFAULT NULL,
  `voufv` decimal(16,2) DEFAULT NULL,
  `duracion_usada` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idmodelo` bigint(20) DEFAULT NULL,
  `id_com_encoc` bigint(20) DEFAULT NULL,
  `idmarca` bigint(20) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `com_detoc` */

CREATE TABLE `com_detoc` (
  `id_com_detoc` bigint(20) DEFAULT NULL,
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nro` bigint(20) DEFAULT NULL,
  `no_orden` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_art` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_med` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cant_sol` decimal(16,2) DEFAULT NULL,
  `total` decimal(16,6) DEFAULT NULL,
  `costo_uni` decimal(16,6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `advertencia` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idhistorialarticuloprov` bigint(20) DEFAULT NULL,
  `id_com_encoc` bigint(20) DEFAULT NULL,
  KEY `id_com_encoc` (`id_com_encoc`),
  CONSTRAINT `com_detoc_ibfk_1` FOREIGN KEY (`id_com_encoc`) REFERENCES `com_encoc` (`id_com_encoc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `com_encoc` */

CREATE TABLE `com_encoc` (
  `id_com_encoc` bigint(20) NOT NULL DEFAULT '0',
  `montosaldo` decimal(16,2) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `mes_consumo` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `descuento` decimal(16,2) DEFAULT NULL,
  `porc_desc` decimal(7,4) DEFAULT NULL,
  `tipodoccompra` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `glosa` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `numero_factura` varchar(150) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_orden` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo` varchar(25) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estadopago` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_prov` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_recepcion` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha_recepcion` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sub_total` decimal(16,2) DEFAULT NULL,
  `total` decimal(16,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `cod_alm` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `confactura` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcondicionpago` bigint(20) DEFAULT NULL,
  `idcontratopuestosol` bigint(20) DEFAULT NULL,
  `idmotivoordenc` bigint(20) DEFAULT NULL,
  `idlugarrecepcion` bigint(20) DEFAULT NULL,
  `id_responsable` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_com_encoc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `comentariodesc` */

CREATE TABLE `comentariodesc` (
  `idcomentariodesc` bigint(20) DEFAULT NULL,
  `motivo` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `tipo` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechamodificacion` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `creadopor` bigint(20) DEFAULT NULL,
  `idafvale` bigint(20) DEFAULT NULL,
  `id_com_encoc` bigint(20) DEFAULT NULL,
  `idfondorotatorio` bigint(20) DEFAULT NULL,
  `actualizadopor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `compania` */

CREATE TABLE `compania` (
  `idcompania` bigint(20) NOT NULL,
  `codigo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  PRIMARY KEY (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `componentepanel` */

CREATE TABLE `componentepanel` (
  `idcomponentepanel` bigint(20) DEFAULT NULL,
  `area` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombrecomponente` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `funcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `modulo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `titulo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `unidad` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `verificacion` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `xmlid` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idresponsablenacional` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `composicionproducto` */

CREATE TABLE `composicionproducto` (
  `idcomposicionproducto` bigint(20) NOT NULL,
  `activo` int(11) DEFAULT NULL,
  `pesocontenedor` decimal(24,0) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cantidadproducir` decimal(24,0) DEFAULT NULL,
  `teoricoobtenido` decimal(24,0) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idproductoprocesado` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcomposicionproducto`),
  KEY `idproductoprocesado` (`idproductoprocesado`),
  CONSTRAINT `composicionproducto_ibfk_1` FOREIGN KEY (`idproductoprocesado`) REFERENCES `productoprocesado` (`idproductoprocesado`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `condicionpago` */

CREATE TABLE `condicionpago` (
  `idcondicionpago` bigint(20) DEFAULT NULL,
  `codigo` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `configrupo` */

CREATE TABLE `configrupo` (
  `idconfigrupo` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_gru` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `configuracion` */

CREATE TABLE `configuracion` (
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `titulo` varchar(100) DEFAULT NULL,
  `subtitulo` varchar(100) DEFAULT NULL,
  `compania` varchar(100) DEFAULT NULL,
  `sistema` varchar(100) DEFAULT NULL,
  `lugar` varchar(100) DEFAULT NULL,
  `leyenda_offline` varchar(300) DEFAULT NULL,
  `leyenda_online` varchar(300) DEFAULT NULL,
  `leyenda_uno` varchar(300) DEFAULT NULL,
  `annul_date` date DEFAULT NULL,
  `url_measure_units` varchar(300) DEFAULT NULL,
  `url_products_services` varchar(300) DEFAULT NULL,
  `url_activities` varchar(300) DEFAULT NULL,
  `url_nit_verification` varchar(300) DEFAULT NULL,
  `url_validate_offline_bill_packages` varchar(300) DEFAULT NULL,
  `url_process_offline_bill_packages` varchar(300) DEFAULT NULL,
  `url_prepare_offline_bill_packages` varchar(300) DEFAULT NULL,
  `url_set_online_mode` varchar(300) DEFAULT NULL,
  `url_set_offline_mode` varchar(300) DEFAULT NULL,
  `url_significant_event` varchar(300) DEFAULT NULL,
  `url_online_offline_mode` varchar(300) DEFAULT NULL,
  `url_createbill` varchar(300) DEFAULT NULL,
  `url_cancelbill` varchar(300) DEFAULT NULL,
  `url_qr` varchar(300) DEFAULT NULL,
  `url_ping` varchar(300) DEFAULT NULL,
  `ctaaitb` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctaantprovme` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctaantprovmn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `urlredirevalprogae` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctadiftipcam` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `agui_basico` int(11) DEFAULT NULL,
  `urlredirevalprogjc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_doc_caja` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `automodifcontrato` int(11) DEFAULT NULL,
  `codmodifcontrato` int(11) DEFAULT NULL,
  `cod_cc` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_usr_rpagos` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `anio_gen_rpagos` int(11) DEFAULT NULL,
  `no_usr_sis` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctadeptrame` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctadeptramn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctaafet` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctaivacrefime` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `hrsdialaboral` decimal(10,2) DEFAULT NULL,
  `it` decimal(3,2) DEFAULT NULL,
  `iue` decimal(3,2) DEFAULT NULL,
  `iva_tax` decimal(5,2) DEFAULT NULL,
  `net_val` decimal(5,2) DEFAULT NULL,
  `it_tax` decimal(5,2) DEFAULT NULL,
  `ctaivacrefimn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctaivacrefitrmn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activoautdoc_cxp` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctaprovobu` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `occodifactiva` int(11) DEFAULT NULL,
  `retencionprestamoanti` int(11) DEFAULT NULL,
  `urlredirevalprog` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `urlredirevalprogest` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `urlredirevalprogdoc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activoautdoc_teso` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `preciounitarioleche` decimal(5,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `ctaalmme` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctatransalmme` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctaalmmn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctatransalm1mn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctatransalm2mn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctatransalmmn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `no_usr_conta` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_usr_produccion` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `iddeftipodoc` bigint(20) DEFAULT NULL,
  `no_usr_pagar` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idcargodoc` bigint(20) DEFAULT NULL,
  `iddefsalhom` bigint(20) DEFAULT NULL,
  `iddefsalmuj` bigint(20) DEFAULT NULL,
  `no_usr_teso` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `pagoctabcome` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idcategoriapuestodlh` bigint(20) DEFAULT NULL,
  `idcategoriapuestodth` bigint(20) DEFAULT NULL,
  `idtiposueldodlh` bigint(20) DEFAULT NULL,
  `idtiposueldodth` bigint(20) DEFAULT NULL,
  `pagoctabcomn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `iue_ret` varchar(20) DEFAULT NULL,
  `it_ret` varchar(20) DEFAULT NULL,
  `ctaCostPT` varchar(20) DEFAULT NULL,
  `ctaAlmPT` varchar(20) DEFAULT NULL,
  `ctaAlmPTAG` varchar(20) DEFAULT NULL,
  `ctaCostPV` varchar(20) DEFAULT NULL,
  `ctaAlmPV` varchar(20) DEFAULT NULL,
  `ctaMerma` varchar(20) DEFAULT NULL,
  `ctaProm` varchar(20) DEFAULT NULL,
  `ctaMermaBaj` varchar(20) DEFAULT NULL,
  `ctaReproc` varchar(20) DEFAULT NULL,
  `ct_cajaahorro` varchar(20) DEFAULT NULL,
  `ct_cajaveter` varchar(20) DEFAULT NULL,
  `cajagral1mn` varchar(20) DEFAULT NULL,
  `i_pvig_pf_mn` varchar(20) DEFAULT NULL,
  `ctaprovaf` varchar(20) DEFAULT NULL,
  `ctaG_it` varchar(20) DEFAULT NULL,
  `ctaP_debFisIva` varchar(20) DEFAULT NULL,
  `ctaP_itxpagar` varchar(20) DEFAULT NULL,
  `ctaI_ventapri` varchar(20) DEFAULT NULL,
  `ctaI_ventasec` varchar(20) DEFAULT NULL,
  `distparam` decimal(5,2) DEFAULT NULL,
  `ctacomision` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `confplanillafiscal` */

CREATE TABLE `confplanillafiscal` (
  `idconfplanillafiscal` bigint(20) DEFAULT NULL,
  `fechacreacion` date DEFAULT NULL,
  `descripcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `tipocambiofinalufv` decimal(16,6) DEFAULT NULL,
  `tipocambioinicialufv` decimal(16,6) DEFAULT NULL,
  `mes` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idtasaafp` bigint(20) DEFAULT NULL,
  `idtasaafpprohous` bigint(20) DEFAULT NULL,
  `idtasaafpprofrisk` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idtasacns` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `idtasaiva` bigint(20) DEFAULT NULL,
  `idtasasmn` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `contrato` */

CREATE TABLE `contrato` (
  `idcontrato` bigint(20) DEFAULT NULL,
  `academico` int(11) DEFAULT NULL,
  `activogenplan` int(11) DEFAULT NULL,
  `activogenplanfis` int(11) DEFAULT NULL,
  `activofonpension` int(11) DEFAULT NULL,
  `controlasistencia` int(11) DEFAULT NULL,
  `respaldo` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `automodifcontrato` int(11) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `codmodifcontrato` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocontrato` int(11) DEFAULT NULL,
  `haberbasicolaboral` decimal(16,6) DEFAULT NULL,
  `montogloballaboral` decimal(16,6) DEFAULT NULL,
  `codregfonpension` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codregsegsocial` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `especial` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmodalidadcontrato` bigint(20) DEFAULT NULL,
  `idestadocontrato` bigint(20) DEFAULT NULL,
  `idciclo` bigint(20) DEFAULT NULL,
  `idinstfonpension` bigint(20) DEFAULT NULL,
  `idinstsegsocial` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `contratopuesto` */

CREATE TABLE `contratopuesto` (
  `idcontratopuesto` bigint(20) NOT NULL DEFAULT '0',
  `asignatura` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `edificio` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `ambiente` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcontrato` bigint(20) DEFAULT NULL,
  `plan_estudio` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `tipo_grupo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idpuesto` bigint(20) DEFAULT NULL,
  `montolaboral` decimal(16,6) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `grupo_asignatura` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `sistema` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idbandahorariacontrato` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcontratopuesto`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `costosindirectos` */

CREATE TABLE `costosindirectos` (
  `idcostosindirectos` bigint(20) NOT NULL,
  `montobs` decimal(16,2) DEFAULT NULL,
  `nombre` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcostosindirectosconf` bigint(20) DEFAULT NULL,
  `idperiodocostoindirecto` bigint(20) DEFAULT NULL,
  `idordenproduccion` bigint(20) DEFAULT NULL,
  `idproductosimple` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcostosindirectos`),
  KEY `idperiodocostoindirecto` (`idperiodocostoindirecto`),
  CONSTRAINT `costosindirectos_ibfk_1` FOREIGN KEY (`idperiodocostoindirecto`) REFERENCES `periodocostoindirecto` (`idperiodocostoindirecto`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `costosindirectosconf` */

CREATE TABLE `costosindirectosconf` (
  `idcostosindirectosconf` bigint(20) NOT NULL,
  `cuenta` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_gru` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `predefinido` int(11) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcostosindirectosconf`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `credito` */

CREATE TABLE `credito` (
  `idcredito` bigint(20) NOT NULL,
  `estado` varchar(3) DEFAULT NULL,
  `ue` varchar(3) DEFAULT NULL,
  `codigo` varchar(25) NOT NULL,
  `codigoant` varchar(15) DEFAULT NULL,
  `importe` decimal(13,2) NOT NULL,
  `montodif` decimal(13,2) NOT NULL DEFAULT '0.00',
  `anual` int(11) NOT NULL,
  `ipenal` decimal(5,2) DEFAULT NULL,
  `plazo` int(11) NOT NULL,
  `cuotas` int(11) NOT NULL,
  `amortiza` int(11) NOT NULL,
  `fechaconcesion` date NOT NULL,
  `fechapago` date NOT NULL,
  `cuota` decimal(13,2) NOT NULL,
  `cuotadif` decimal(13,2) NOT NULL DEFAULT '0.00',
  `saldo` decimal(13,2) NOT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `ultimopago` date DEFAULT NULL,
  `fechavence` date DEFAULT NULL,
  `entregado` int(11) NOT NULL,
  `traspaso` int(11) NOT NULL DEFAULT '0',
  `idcreditoorig` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idsocio` bigint(20) NOT NULL,
  `idtipocredito` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcredito`),
  KEY `idsocio` (`idsocio`),
  KEY `idtipocredito` (`idtipocredito`),
  KEY `idcreditoorig` (`idcreditoorig`),
  CONSTRAINT `credito_ibfk_1` FOREIGN KEY (`idsocio`) REFERENCES `socio` (`idsocio`),
  CONSTRAINT `credito_ibfk_2` FOREIGN KEY (`idtipocredito`) REFERENCES `tipocredito` (`idtipocredito`),
  CONSTRAINT `credito_ibfk_3` FOREIGN KEY (`idcreditoorig`) REFERENCES `credito` (`idcredito`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `criterioevaluacion` */

CREATE TABLE `criterioevaluacion` (
  `idcriterioevaluacion` bigint(20) DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cronogramagp` */

CREATE TABLE `cronogramagp` (
  `idcronogramagp` bigint(20) DEFAULT NULL,
  `fechacreacion` date DEFAULT NULL,
  `nombre` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cuenta` */

CREATE TABLE `cuenta` (
  `idcuenta` bigint(20) NOT NULL,
  `fechaapertura` date DEFAULT NULL,
  `fechavence` date DEFAULT NULL,
  `nocuenta` varchar(20) NOT NULL,
  `codigo` varchar(20) DEFAULT NULL,
  `moneda` varchar(20) NOT NULL,
  `capital` decimal(13,2) DEFAULT NULL,
  `estado` varchar(32) NOT NULL,
  `saldo` decimal(13,2) NOT NULL,
  `beneficio1` varchar(100) DEFAULT NULL,
  `beneficio2` varchar(100) DEFAULT NULL,
  `ret` int(11) DEFAULT NULL,
  `emp` int(11) DEFAULT NULL,
  `idsocio` bigint(20) NOT NULL,
  `idtipocuenta` bigint(20) NOT NULL,
  `VERSION` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idcuenta`),
  KEY `idsocio` (`idsocio`),
  KEY `idcompania` (`idcompania`),
  KEY `idtipocuenta` (`idtipocuenta`),
  CONSTRAINT `cuenta_ibfk_1` FOREIGN KEY (`idsocio`) REFERENCES `socio` (`idsocio`),
  CONSTRAINT `cuenta_ibfk_2` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `cuenta_ibfk_3` FOREIGN KEY (`idtipocuenta`) REFERENCES `tipocuenta` (`idtipocuenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cuentabancaria` */

CREATE TABLE `cuentabancaria` (
  `idcuentabancaria` bigint(20) NOT NULL DEFAULT '0',
  `numerocuenta` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocliente` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentapordefecto` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `identidadbancaria` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmoneda` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idcuentabancaria`),
  UNIQUE KEY `empcuenta` (`identidadbancaria`,`idempleado`,`numerocuenta`),
  KEY `idempleado` (`idempleado`),
  CONSTRAINT `cuentabancaria_ibfk_1` FOREIGN KEY (`idempleado`) REFERENCES `empleado` (`idempleado`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cuentas` */

CREATE TABLE `cuentas` (
  `id` int(11) DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cuentas_art_wise` */

CREATE TABLE `cuentas_art_wise` (
  `cod_art` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `id_cuenta` int(11) DEFAULT NULL,
  `cod_alm` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cuota` */

CREATE TABLE `cuota` (
  `idcuota` bigint(20) DEFAULT NULL,
  `cantidad` decimal(13,2) DEFAULT NULL,
  `moneda` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `porplanilla` int(11) DEFAULT NULL,
  `tipocambio` decimal(16,6) DEFAULT NULL,
  `fechavencimiento` date DEFAULT NULL,
  `saldo` decimal(13,2) DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idfondorotatorio` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `curriculas` */

CREATE TABLE `curriculas` (
  `asignatura` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `plan_estudio` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `sistema` int(11) DEFAULT NULL,
  `activo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `costo` decimal(19,2) DEFAULT NULL,
  `creditos` int(11) DEFAULT NULL,
  `cuota` int(11) DEFAULT NULL,
  `escala` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nivel_asignatura` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cxp_claseprov` */

CREATE TABLE `cxp_claseprov` (
  `clase` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cxp_docrel` */

CREATE TABLE `cxp_docrel` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_conci` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `monto` decimal(16,2) DEFAULT NULL,
  `tc` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cxp_docus` */

CREATE TABLE `cxp_docus` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `mod_aut` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `monto` decimal(16,2) DEFAULT NULL,
  `beneficiario` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_doc` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_doc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_enti` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tc` decimal(10,6) DEFAULT NULL,
  `fecha_ven` date DEFAULT NULL,
  `no_factura` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `ctaxpagar` varchar(31) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_prov` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha_emis` date DEFAULT NULL,
  `pendiente_registro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_compro_regfac` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `saldo_regfac` decimal(16,2) DEFAULT NULL,
  `tipo_compro_regfac` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `saldo` decimal(16,2) DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_compro` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cxp_lcompras` */

CREATE TABLE `cxp_lcompras` (
  `no_auto` varchar(150) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_enti` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_fact` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `importe` decimal(16,2) DEFAULT NULL,
  `cod_control` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `exento` decimal(16,2) DEFAULT NULL,
  `ice` decimal(16,2) DEFAULT NULL,
  `nit` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `razon_social` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `impuesto` decimal(16,2) DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cxp_movs` */

CREATE TABLE `cxp_movs` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha_cre` date DEFAULT NULL,
  `descri` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `tipo_mov` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_prov` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_usr` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_compro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cxp_proveedores` */

CREATE TABLE `cxp_proveedores` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_prov` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctaxpagar` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `clase` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `aux` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cxp_tipodocs` */

CREATE TABLE `cxp_tipodocs` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_doc` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `clase_doc` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `modulo` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_mov` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `registro_requerido` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `departamento` */

CREATE TABLE `departamento` (
  `iddepartamento` bigint(20) NOT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpais` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`iddepartamento`),
  KEY `fk_companiadepartamento` (`idcompania`),
  KEY `fk_paisdepartamento` (`idpais`),
  CONSTRAINT `fk_companiadepartamento` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_paisdepartamento` FOREIGN KEY (`idpais`) REFERENCES `pais` (`idpais`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `depositoproductoterminado` */

CREATE TABLE `depositoproductoterminado` (
  `iddepositoproductoterminado` bigint(20) DEFAULT NULL,
  `codigo` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(1500) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `derechoacceso` */

CREATE TABLE `derechoacceso` (
  `idfuncionalidad` bigint(20) DEFAULT NULL,
  `idrol` bigint(20) DEFAULT NULL,
  `permiso` tinyint(4) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmodulo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `descuentocliente` */

CREATE TABLE `descuentocliente` (
  `idcliente` bigint(20) DEFAULT NULL,
  `iddescuentocliente` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `descuentoempleado` */

CREATE TABLE `descuentoempleado` (
  `iddescuentoempleado` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `descuentofactura` */

CREATE TABLE `descuentofactura` (
  `idfactura` bigint(20) DEFAULT NULL,
  `iddescuentocliente` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `descuentofacturadetalle` */

CREATE TABLE `descuentofacturadetalle` (
  `idfacturadetalle` bigint(20) DEFAULT NULL,
  `iddescuentoproducto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `descuentoproducto` */

CREATE TABLE `descuentoproducto` (
  `iddescuentoproducto` bigint(20) DEFAULT NULL,
  `idproducto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `descuentoproductor` */

CREATE TABLE `descuentoproductor` (
  `iddescuentoproductor` bigint(20) DEFAULT NULL,
  `montototalme` decimal(16,2) DEFAULT NULL,
  `montototalmn` decimal(16,2) DEFAULT NULL,
  `promedioleche` decimal(16,2) DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `reserva` decimal(8,5) DEFAULT NULL,
  `reservaquicenta` decimal(8,5) DEFAULT NULL,
  `fechaini` date DEFAULT NULL,
  `estado` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tc` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `descuentoreserva` */

CREATE TABLE `descuentoreserva` (
  `iddescuentoreserva` bigint(20) DEFAULT NULL,
  `monto` decimal(16,2) DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `fechaini` date DEFAULT NULL,
  `iddescuentoproductor` bigint(20) DEFAULT NULL,
  `idproductormateriaprima` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `descuentproductmateriaprima` */

CREATE TABLE `descuentproductmateriaprima` (
  `iddescuentproductmateriaprima` bigint(20) NOT NULL,
  `alcohol` decimal(16,2) DEFAULT NULL,
  `tachos` decimal(16,2) DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `concentrados` decimal(16,2) DEFAULT NULL,
  `credito` decimal(16,2) DEFAULT NULL,
  `otrosdescuentos` decimal(16,2) DEFAULT NULL,
  `comision` decimal(16,2) DEFAULT NULL,
  `otrosingresos` decimal(16,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `veterinario` decimal(16,2) DEFAULT NULL,
  `retencion` decimal(16,2) DEFAULT NULL,
  `yogurt` decimal(16,2) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idproductormateriaprima` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`iddescuentproductmateriaprima`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `detallebonoantiguedad` */

CREATE TABLE `detallebonoantiguedad` (
  `iddetallebonoantiguedad` bigint(20) DEFAULT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `aniofin` int(11) DEFAULT NULL,
  `porcentaje` decimal(13,2) DEFAULT NULL,
  `anioinicio` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idbonoantiguedad` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `detalleretiro` */

CREATE TABLE `detalleretiro` (
  `iddetalleretiro` bigint(20) DEFAULT NULL,
  `monto` decimal(16,6) DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `moneda` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocambio` decimal(16,6) DEFAULT NULL,
  `motivoreversion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notrans` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcausaretiro` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idretiro` bigint(20) DEFAULT NULL,
  `idarchivo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `detplanilladocentelaboral` */

CREATE TABLE `detplanilladocentelaboral` (
  `iddetplanilladocentelaboral` bigint(20) DEFAULT NULL,
  `descuentoporminutosausencia` decimal(13,2) DEFAULT NULL,
  `area` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `minutosausenciabandas` int(11) DEFAULT NULL,
  `basicoganado` decimal(13,2) DEFAULT NULL,
  `sueldobasico` decimal(13,2) DEFAULT NULL,
  `categoria` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechafincontrato` date DEFAULT NULL,
  `fechainiciocontrato` date DEFAULT NULL,
  `modalidadcontratacion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `diferencia` decimal(13,2) DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  `ingresofueraiva` decimal(13,2) DEFAULT NULL,
  `puesto` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipoempleado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `liquidopagable` decimal(13,2) DEFAULT NULL,
  `numerominutosausenciabandas` int(11) DEFAULT NULL,
  `otrosingresos` decimal(13,2) DEFAULT NULL,
  `atrasos` decimal(13,2) DEFAULT NULL,
  `minutosatraso` int(11) DEFAULT NULL,
  `descuentoporminutosatraso` decimal(13,2) DEFAULT NULL,
  `totaldescuento` decimal(13,2) DEFAULT NULL,
  `totalganado` decimal(13,2) DEFAULT NULL,
  `unidad` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcargo` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idbandahorariacontrato` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `detregcontable` */

CREATE TABLE `detregcontable` (
  `iddetregcontable` bigint(20) DEFAULT NULL,
  `numctabanaria` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocompania` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idregistrocontable` bigint(20) DEFAULT NULL,
  `idplanillaaguinaldo` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idplanilladocentelaboral` bigint(20) DEFAULT NULL,
  `idplanillageneral` bigint(20) DEFAULT NULL,
  `idplanillaadministrativos` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `direccion` */

CREATE TABLE `direccion` (
  `iddireccion` bigint(20) NOT NULL,
  `numero` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcalle` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`iddireccion`),
  KEY `fk_calledireccion` (`idcalle`),
  KEY `fk_companiadireccion` (`idcompania`),
  CONSTRAINT `fk_calledireccion` FOREIGN KEY (`idcalle`) REFERENCES `calle` (`idcalle`),
  CONSTRAINT `fk_companiadireccion` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `distmesciclo` */

CREATE TABLE `distmesciclo` (
  `iddistmesciclo` bigint(20) DEFAULT NULL,
  `diaslaborales` int(11) DEFAULT NULL,
  `mes` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idciclo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `distpresdet` */

CREATE TABLE `distpresdet` (
  `iddistpresdet` bigint(20) DEFAULT NULL,
  `mes` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `porcentajedist` decimal(5,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `iddistpresupuesto` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `distpresupuesto` */

CREATE TABLE `distpresupuesto` (
  `iddistpresupuesto` bigint(20) DEFAULT NULL,
  `tipodistribucion` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `porcentajedist` decimal(5,2) DEFAULT NULL,
  `tipo` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `distribuciongasto` */

CREATE TABLE `distribuciongasto` (
  `iddistribuciongasto` bigint(20) DEFAULT NULL,
  `cuenta` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `monto` decimal(12,2) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `porcentaje` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idfondorotatorio` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `distribuciongastocobrofon` */

CREATE TABLE `distribuciongastocobrofon` (
  `iddistribuciongastocobrofon` bigint(20) DEFAULT NULL,
  `cuenta` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `monto` decimal(12,2) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `porcentaje` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcobro` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `distribuidor` */

CREATE TABLE `distribuidor` (
  `iddistribuidor` bigint(20) NOT NULL,
  `descripcion` varchar(100) DEFAULT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`iddistribuidor`),
  KEY `idcompania` (`idcompania`),
  CONSTRAINT `distribuidor_ibfk_1` FOREIGN KEY (`iddistribuidor`) REFERENCES `persona` (`idpersona`),
  CONSTRAINT `distribuidor_ibfk_2` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `docentesporcarrera` */

CREATE TABLE `docentesporcarrera` (
  `iddocentesporcarrera` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `unidad_acad_adm` int(11) DEFAULT NULL,
  `carrera` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `sede` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `cant_docentes` int(11) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `plan_estudio` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `documentocobro` */

CREATE TABLE `documentocobro` (
  `tipodocumentocobro` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `iddocumentocobro` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `identidad` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `documentocompra` */

CREATE TABLE `documentocompra` (
  `cuentaajuste` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocambio` decimal(16,6) DEFAULT NULL,
  `idordencompra` bigint(20) DEFAULT NULL,
  `estado` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `iddocumentocompra` bigint(20) NOT NULL DEFAULT '0',
  `idcompania` bigint(20) DEFAULT NULL,
  `identidad` bigint(20) DEFAULT NULL,
  `idtmpenc` bigint(20) DEFAULT NULL,
  `id_tmpdet` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`iddocumentocompra`),
  KEY `idtmpenc` (`idtmpenc`),
  KEY `idordencompra` (`idordencompra`),
  KEY `id_tmpdet` (`id_tmpdet`),
  CONSTRAINT `documentocompra_ibfk_1` FOREIGN KEY (`idtmpenc`) REFERENCES `sf_tmpenc` (`id_tmpenc`),
  CONSTRAINT `documentocompra_ibfk_2` FOREIGN KEY (`idordencompra`) REFERENCES `com_encoc` (`id_com_encoc`),
  CONSTRAINT `documentocompra_ibfk_3` FOREIGN KEY (`iddocumentocompra`) REFERENCES `documentocontable` (`iddocumentocontable`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `documentocontable` */

CREATE TABLE `documentocontable` (
  `iddocumentocontable` bigint(20) NOT NULL DEFAULT '0',
  `direccion` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `importe` decimal(12,2) DEFAULT NULL,
  `numeroautorizacion` varchar(150) DEFAULT NULL,
  `codigocontrol` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `tasas` decimal(12,2) DEFAULT NULL,
  `nocf` decimal(12,2) DEFAULT NULL,
  `exento` decimal(12,2) DEFAULT NULL,
  `regcompro` int(11) DEFAULT NULL,
  `ice` decimal(12,2) DEFAULT NULL,
  `iva` decimal(12,2) DEFAULT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `descuentos` decimal(12,2) DEFAULT NULL,
  `importeneto` decimal(12,2) DEFAULT NULL,
  `nit` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numero` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerotransaccion` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`iddocumentocontable`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `documentodescargo` */

CREATE TABLE `documentodescargo` (
  `moneda` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocambio` decimal(16,6) DEFAULT NULL,
  `estado` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `iddocumentodescargo` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `identidad` bigint(20) DEFAULT NULL,
  `idgestionplanilla` bigint(20) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `documentogarante` */

CREATE TABLE `documentogarante` (
  `iddocumentogarante` bigint(20) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `idgarante` bigint(20) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`iddocumentogarante`),
  KEY `idgarante` (`idgarante`),
  KEY `idcompania` (`idcompania`),
  CONSTRAINT `documentogarante_ibfk_1` FOREIGN KEY (`idgarante`) REFERENCES `garante` (`idgarante`),
  CONSTRAINT `documentogarante_ibfk_2` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `dosificacion` */

CREATE TABLE `dosificacion` (
  `IDDOSIFICACION` bigint(20) NOT NULL,
  `NROAUTORIZACION` bigint(20) DEFAULT NULL,
  `FECHAVENCIMIENTO` date DEFAULT NULL,
  `EST_COD` varchar(30) DEFAULT NULL,
  `LLAVE` varchar(100) DEFAULT NULL,
  `ESTADO` varchar(15) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `FACTURADEL` bigint(20) DEFAULT NULL,
  `FACTURAAL` bigint(20) DEFAULT NULL,
  `NUMEROACTUAL` bigint(20) DEFAULT NULL,
  `NITEMPRESA` varchar(20) DEFAULT NULL,
  `ETIQUETAEMPRESA` varchar(100) DEFAULT NULL,
  `FECHAINICIO` date NOT NULL,
  `FECHACONTROL` date NOT NULL,
  `IDSUCURSAL` bigint(20) DEFAULT NULL,
  `ETIQUETALEY` varchar(255) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`IDDOSIFICACION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `dosificaciones` */

CREATE TABLE `dosificaciones` (
  `id` bigint(20) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `est_cod` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `facturadel` decimal(10,0) DEFAULT NULL,
  `facturaal` decimal(10,0) DEFAULT NULL,
  `llave` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nroautorizacion` decimal(15,0) DEFAULT NULL,
  `nro_actual` decimal(10,0) DEFAULT NULL,
  `activo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ejemplarencuesta` */

CREATE TABLE `ejemplarencuesta` (
  `idejemplarencuesta` bigint(20) DEFAULT NULL,
  `fecharevision` date DEFAULT NULL,
  `numerorevision` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idperiodocad` bigint(20) DEFAULT NULL,
  `idcarrera` bigint(20) DEFAULT NULL,
  `idcomentario` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idciclo` bigint(20) DEFAULT NULL,
  `idevaluador` bigint(20) DEFAULT NULL,
  `idfacultad` bigint(20) DEFAULT NULL,
  `idpersona` bigint(20) DEFAULT NULL,
  `idformularioencuesta` bigint(20) DEFAULT NULL,
  `idasignatura` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `emp` */

CREATE TABLE `emp` (
  `identidad` bigint(20) DEFAULT NULL,
  `ci` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidopaterno` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidomaterno` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombres` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `empleado` */

CREATE TABLE `empleado` (
  `flagafp` int(11) DEFAULT NULL,
  `flagcontrol` int(11) DEFAULT NULL,
  `codigoempleado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechaingreso` date DEFAULT NULL,
  `flagjubilado` int(11) DEFAULT NULL,
  `codigomarcacion` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipodepago` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `flagret` int(11) DEFAULT NULL,
  `fechasalida` date DEFAULT NULL,
  `salario` decimal(13,2) DEFAULT NULL,
  `idempleado` bigint(20) NOT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idempleado`),
  KEY `fk_companiaempleado` (`idcompania`),
  KEY `fk_unidadnegocioempleado` (`idunidadnegocio`),
  CONSTRAINT `empleado_ibfk_1` FOREIGN KEY (`idempleado`) REFERENCES `persona` (`idpersona`),
  CONSTRAINT `fk_companiaempleado` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_unidadnegocioempleado` FOREIGN KEY (`idunidadnegocio`) REFERENCES `unidadnegocio` (`idunidadnegocio`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `empleadosporcarr` */

CREATE TABLE `empleadosporcarr` (
  `idempleadosporcarr` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcarrera` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombrecarrera` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigoempleado` int(11) DEFAULT NULL,
  `nombresemp` varchar(40) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `generoemp` varchar(1) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `identificacionemp` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipodocumentoemp` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidopaternoemp` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidomaternoemp` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idfacultad` int(11) DEFAULT NULL,
  `nombrefac` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `idsede` int(11) DEFAULT NULL,
  `nombresede` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `entidad` */

CREATE TABLE `entidad` (
  `identidad` bigint(20) NOT NULL,
  `noidentificacion` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `iddireccion` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipodocumento` bigint(20) DEFAULT NULL,
  `idexttipodocumento` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`identidad`),
  KEY `fk_companiaentidad` (`idcompania`),
  KEY `fk_direccionentidad` (`iddireccion`),
  KEY `fk_exttipodocumentoentidad` (`idexttipodocumento`),
  KEY `fk_tipodocumentoentidad` (`idtipodocumento`),
  CONSTRAINT `fk_companiaentidad` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_direccionentidad` FOREIGN KEY (`iddireccion`) REFERENCES `direccion` (`iddireccion`),
  CONSTRAINT `fk_exttipodocumentoentidad` FOREIGN KEY (`idexttipodocumento`) REFERENCES `exttipodocumento` (`idexttipodocumento`),
  CONSTRAINT `fk_tipodocumentoentidad` FOREIGN KEY (`idtipodocumento`) REFERENCES `tipodocumento` (`idtipodocumento`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `entidadbancaria` */

CREATE TABLE `entidadbancaria` (
  `identidadbancaria` bigint(20) DEFAULT NULL,
  `codigo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `entidadbensocial` */

CREATE TABLE `entidadbensocial` (
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_prov` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `identidadbensocial` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `entradaordenproduccion` */

CREATE TABLE `entradaordenproduccion` (
  `identradaordenproduccion` bigint(20) DEFAULT NULL,
  `cantidadentregada` decimal(24,0) DEFAULT NULL,
  `observacionentrega` varchar(1500) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `observacionrecepcion` varchar(1500) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idinventarioproductoterminado` bigint(20) DEFAULT NULL,
  `idregistrotransferenciaproduct` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `entregaarticulo` */

CREATE TABLE `entregaarticulo` (
  `identarticulo` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_fact` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `estadoahorro` */

CREATE TABLE `estadoahorro` (
  `id` bigint(20) NOT NULL,
  `cuenta` varchar(10) DEFAULT NULL,
  `descr` varchar(100) DEFAULT NULL,
  `gabsocio` varchar(100) DEFAULT NULL,
  `ci` varchar(100) DEFAULT NULL,
  `nombrecomp` varchar(100) DEFAULT NULL,
  `nombres` varchar(100) DEFAULT NULL,
  `ap` varchar(100) DEFAULT NULL,
  `am` varchar(100) DEFAULT NULL,
  `saldocuenta` decimal(16,2) DEFAULT NULL,
  `interes` decimal(16,2) DEFAULT NULL,
  `saldoaporte` decimal(16,2) DEFAULT NULL,
  `unidad` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `estadoarticulo` */

CREATE TABLE `estadoarticulo` (
  `idestadoarticulo` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_art` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estadobandahoraria` */

CREATE TABLE `estadobandahoraria` (
  `idestadobandahoraria` bigint(20) DEFAULT NULL,
  `despuesfin` int(11) DEFAULT NULL,
  `despuesinicio` int(11) DEFAULT NULL,
  `antesfin` int(11) DEFAULT NULL,
  `antesinicio` int(11) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_cc` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `duracion` int(11) DEFAULT NULL,
  `horafin` time DEFAULT NULL,
  `horainicio` time DEFAULT NULL,
  `mindescuento` int(11) DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idbandahoraria` bigint(20) DEFAULT NULL,
  `idbandahorariac` bigint(20) DEFAULT NULL,
  `idunidadorganizacional` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estadocartera` */

CREATE TABLE `estadocartera` (
  `id` bigint(20) NOT NULL,
  `cuenta` varchar(10) DEFAULT NULL,
  `descr` varchar(100) DEFAULT NULL,
  `ngab` varchar(10) DEFAULT NULL,
  `nrocred` varchar(10) DEFAULT NULL,
  `Moneda` varchar(10) DEFAULT NULL,
  `ci` varchar(10) DEFAULT NULL,
  `gabsocio` varchar(10) DEFAULT NULL,
  `nombrecomp` varchar(100) DEFAULT NULL,
  `nombres` varchar(100) DEFAULT NULL,
  `ap` varchar(100) DEFAULT NULL,
  `am` varchar(100) DEFAULT NULL,
  `fapertura` date DEFAULT NULL,
  `ianual` decimal(16,2) DEFAULT NULL,
  `garantia` varchar(10) DEFAULT NULL,
  `objetivo` varchar(20) DEFAULT NULL,
  `saldoactual` decimal(16,2) DEFAULT NULL,
  `ultimopago` date DEFAULT NULL,
  `diasmora` int(11) DEFAULT NULL,
  `interes` decimal(16,2) DEFAULT NULL,
  `dias` int(11) DEFAULT NULL,
  `unidad` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `estadocivil` */

CREATE TABLE `estadocivil` (
  `idestadocivil` bigint(20) NOT NULL,
  `codigo` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idestadocivil`),
  KEY `fk_companiaestadocivil` (`idcompania`),
  CONSTRAINT `fk_companiaestadocivil` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estadocontrato` */

CREATE TABLE `estadocontrato` (
  `idestadocontrato` bigint(20) NOT NULL,
  `nombre` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idestadocontrato`),
  KEY `fk_companiaestadocontrato` (`idcompania`),
  CONSTRAINT `fk_companiaestadocontrato` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estadomarcado` */

CREATE TABLE `estadomarcado` (
  `idestadomarcado` bigint(20) DEFAULT NULL,
  `identificado` int(11) DEFAULT NULL,
  `marfecha` date DEFAULT NULL,
  `marhora` time DEFAULT NULL,
  `codigomarcacion` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idrhmarcado` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estadomarestadobh` */

CREATE TABLE `estadomarestadobh` (
  `idestadomarestadobh` bigint(20) DEFAULT NULL,
  `mindescuento` int(11) DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idestadobandahoraria` bigint(20) DEFAULT NULL,
  `idestadomarcado` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estadoproducto` */

CREATE TABLE `estadoproducto` (
  `idestadoproducto` bigint(20) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estadorecepcion` */

CREATE TABLE `estadorecepcion` (
  `idestadorecepcion` bigint(20) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estructuras` */

CREATE TABLE `estructuras` (
  `codigo` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descripcion` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `estudiante` */

CREATE TABLE `estudiante` (
  `codigoestudiante` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idestudiante` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `estudiantesporasig` */

CREATE TABLE `estudiantesporasig` (
  `idestudientesporasig` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcarrera` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombrecarrera` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigoempleado` int(11) DEFAULT NULL,
  `nombresemp` varchar(40) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `generoemp` varchar(1) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `identificacionemp` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipodocumentoemp` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidopaternoemp` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidomaternoemp` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idfacultad` int(11) DEFAULT NULL,
  `nombrefac` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `idsede` int(11) DEFAULT NULL,
  `nombresede` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigoestudiante` int(11) DEFAULT NULL,
  `nombresest` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `generoest` varchar(1) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `identificacionest` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipoidentificacionest` varchar(5) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidopaternoest` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidomaternoest` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idasignatura` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombreasig` varchar(300) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `evaluaciondocente` */

CREATE TABLE `evaluaciondocente` (
  `idevaluaciondocente` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `unidadacadadm` int(11) DEFAULT NULL,
  `sede` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codempleado` bigint(20) DEFAULT NULL,
  `nombres` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `documento` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellido_paterno` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellido_materno` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cant_carreras` int(11) DEFAULT NULL,
  `cant_estudiantes` int(11) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `evaluacionprog` */

CREATE TABLE `evaluacionprog` (
  `idevaluacionprog` bigint(20) DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idciclo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `experiencia` */

CREATE TABLE `experiencia` (
  `idexperiencia` bigint(20) DEFAULT NULL,
  `duracion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `lugar` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cargo` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpostulante` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `exttipodocumento` */

CREATE TABLE `exttipodocumento` (
  `idexttipodocumento` bigint(20) NOT NULL,
  `extension` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipodocumento` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idexttipodocumento`),
  KEY `fk_companiaexttipodocumento` (`idcompania`),
  KEY `fk_tipodocumentoexttipodoc` (`idtipodocumento`),
  CONSTRAINT `fk_companiaexttipodocumento` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_tipodocumentoexttipodoc` FOREIGN KEY (`idtipodocumento`) REFERENCES `tipodocumento` (`idtipodocumento`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `factura` */

CREATE TABLE `factura` (
  `idfactura` bigint(20) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `nombres` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidopaterno` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidomaterno` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerofactura` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `razonsocial` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `formapago` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nodoctributario` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipodoctibutario` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `importetotal` decimal(13,2) DEFAULT NULL,
  `totaldescuento` decimal(13,2) DEFAULT NULL,
  `idcaja` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcliente` bigint(20) DEFAULT NULL,
  `idreglatributaria` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `facturadetalle` */

CREATE TABLE `facturadetalle` (
  `idfacturadetalle` bigint(20) DEFAULT NULL,
  `descuento` decimal(13,2) DEFAULT NULL,
  `porcentajedescuento` decimal(13,2) DEFAULT NULL,
  `preciounitario` decimal(13,2) DEFAULT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `importe` decimal(13,2) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idproducto` bigint(20) DEFAULT NULL,
  `idfactura` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `facultad` */

CREATE TABLE `facultad` (
  `idfacultad` bigint(20) DEFAULT NULL,
  `codigo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idreferencia` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idsede` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `fechaespecial` */

CREATE TABLE `fechaespecial` (
  `idfechaespecial` bigint(20) DEFAULT NULL,
  `allday` int(11) DEFAULT NULL,
  `gocedehaber` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `dia` int(11) DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `horafin` time DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `mes` int(11) DEFAULT NULL,
  `moveralunes` int(11) DEFAULT NULL,
  `ocurrencia` int(11) DEFAULT NULL,
  `tiporol` varchar(15) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `destino` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `horainicio` time DEFAULT NULL,
  `titulo` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcontrato` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idunidadorganizacional` bigint(20) DEFAULT NULL,
  `idvacacion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `filtrocomppnl` */

CREATE TABLE `filtrocomppnl` (
  `idfiltrocomppnl` bigint(20) DEFAULT NULL,
  `color` int(11) DEFAULT NULL,
  `descripcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `indice` int(11) DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcomponentepanel` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `fondorotatorio` */

CREATE TABLE `fondorotatorio` (
  `idfondorotatorio` bigint(20) DEFAULT NULL,
  `monto` decimal(12,2) DEFAULT NULL,
  `cuentactb` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo` int(11) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_cc` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `porplanilla` int(11) DEFAULT NULL,
  `tipocambio` decimal(16,6) DEFAULT NULL,
  `fechavencimiento` date DEFAULT NULL,
  `monedapago` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `residuoporpagar` decimal(12,2) DEFAULT NULL,
  `cuotas` int(11) DEFAULT NULL,
  `cod_prov` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `residuoporcobrar` decimal(12,2) DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `anuladopor` bigint(20) DEFAULT NULL,
  `aprobadopor` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipodocfondorota` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `creadopor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `formacionacademica` */

CREATE TABLE `formacionacademica` (
  `idformacionacademica` bigint(20) DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `universidad` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `formacionacademp` */

CREATE TABLE `formacionacademp` (
  `idformacionacademp` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `formacionacadpost` */

CREATE TABLE `formacionacadpost` (
  `idformacionacadpost` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpostulante` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `formevalfinal` */

CREATE TABLE `formevalfinal` (
  `idformevalfinal` bigint(20) DEFAULT NULL,
  `fechaaprob` date DEFAULT NULL,
  `codigo` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `subtitulo` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `titulo` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idciclo` bigint(20) DEFAULT NULL,
  `idmetodologia` bigint(20) DEFAULT NULL,
  `idobjetivo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `formevaluacionprog` */

CREATE TABLE `formevaluacionprog` (
  `idformevaluacionprog` bigint(20) DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `muestra` int(11) DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `tipo` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idformularioencuesta` bigint(20) DEFAULT NULL,
  `idevaluacionprog` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `formfactura` */

CREATE TABLE `formfactura` (
  `idformfactura` bigint(20) DEFAULT NULL,
  `creditofiscal` int(11) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `fechapresentacion` date DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idciclogeneracionplanilla` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `formularioencuesta` */

CREATE TABLE `formularioencuesta` (
  `idformularioencuesta` bigint(20) DEFAULT NULL,
  `restricperiodaca` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechaaprobacion` date DEFAULT NULL,
  `codigo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `restricciclo` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `porcentajeequiva` int(11) DEFAULT NULL,
  `restricagrupa` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `restricpersona` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipoagrupacion` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `porcentajemuestra` int(11) DEFAULT NULL,
  `subtitulo` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `titulo` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idciclo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `funcionalidad` */

CREATE TABLE `funcionalidad` (
  `idfuncionalidad` bigint(20) NOT NULL DEFAULT '0',
  `codigo` varchar(40) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `idmodulo` bigint(20) DEFAULT NULL,
  `permiso` tinyint(4) DEFAULT NULL,
  `nombrerecurso` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idfuncionalidad`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `garante` */

CREATE TABLE `garante` (
  `idgarante` bigint(20) NOT NULL,
  `idsocio` bigint(20) NOT NULL,
  `idcredito` bigint(20) NOT NULL,
  `descripcion` varchar(100) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idgarante`),
  KEY `idcompania` (`idcompania`),
  KEY `idsocio` (`idsocio`),
  KEY `idcredito` (`idcredito`),
  CONSTRAINT `garante_ibfk_1` FOREIGN KEY (`idsocio`) REFERENCES `socio` (`idsocio`),
  CONSTRAINT `garante_ibfk_2` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `garante_ibfk_3` FOREIGN KEY (`idcredito`) REFERENCES `credito` (`idcredito`),
  CONSTRAINT `garante_ibfk_4` FOREIGN KEY (`idcredito`) REFERENCES `credito` (`idcredito`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `gensecuencia` */

CREATE TABLE `gensecuencia` (
  `idgensecuencia` bigint(20) DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `valor` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `gestion` */

CREATE TABLE `gestion` (
  `idgestion` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `anio` int(11) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `gestionimpuesto` */

CREATE TABLE `gestionimpuesto` (
  `idgestionimpuesto` bigint(20) NOT NULL,
  `fechafin` date DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  PRIMARY KEY (`idgestionimpuesto`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `gestionplanilla` */

CREATE TABLE `gestionplanilla` (
  `idgestionplanilla` bigint(20) NOT NULL DEFAULT '0',
  `fechafin` date DEFAULT NULL,
  `fechaaperturagen` date DEFAULT NULL,
  `fechalimitegen` date DEFAULT NULL,
  `nombregestion` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `mes` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechaplanoficial` date DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipocambio` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL,
  `idciclogeneracionplanilla` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idgestionplanilla`),
  KEY `idciclogeneracionplanilla` (`idciclogeneracionplanilla`),
  KEY `idcategoriapuesto` (`idcategoriapuesto`),
  CONSTRAINT `gestionplanilla_ibfk_1` FOREIGN KEY (`idciclogeneracionplanilla`) REFERENCES `ciclogeneracionplanilla` (`idciclogeneracionplanilla`),
  CONSTRAINT `gestionplanilla_ibfk_2` FOREIGN KEY (`idcategoriapuesto`) REFERENCES `categoriapuesto` (`idcategoriapuesto`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `gestionplanillaadm` */

CREATE TABLE `gestionplanillaadm` (
  `idgestionplanillaadm` bigint(20) DEFAULT NULL,
  `idgestionplanilla` bigint(20) DEFAULT NULL,
  `idconfplanillafiscal` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `gestionvacacion` */

CREATE TABLE `gestionvacacion` (
  `idgestionvacacion` bigint(20) DEFAULT NULL,
  `diaslibres` int(11) DEFAULT NULL,
  `diasusados` int(11) DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `diasvacacion` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idplanvacacion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `grupoasignatura` */

CREATE TABLE `grupoasignatura` (
  `idgrupoasignatura` bigint(20) DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idunidadorganizacional` bigint(20) DEFAULT NULL,
  `idasignatura` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `grupoctactb` */

CREATE TABLE `grupoctactb` (
  `idgrupoctactb` bigint(20) DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `grupos_asignaturas` */

CREATE TABLE `grupos_asignaturas` (
  `asignatura` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `plan_estudio` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `tipo_grupo` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `grupo_asignatura` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sistema` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `historialarticuloprov` */

CREATE TABLE `historialarticuloprov` (
  `idhistorialarticuloprov` bigint(20) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `costo_uni` decimal(16,6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `id_articulo_por_proveedor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `historialcaja` */

CREATE TABLE `historialcaja` (
  `idcaja` bigint(20) DEFAULT NULL,
  `fechaestado` date DEFAULT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `descripcion` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `autorizacionrequerida` int(11) DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipocaja` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `historialestadosolmant` */

CREATE TABLE `historialestadosolmant` (
  `idhisestsolmant` bigint(20) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `iddescripcion` bigint(20) DEFAULT NULL,
  `idsolmant` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `historialsueldos` */

CREATE TABLE `historialsueldos` (
  `idhistorialsueldos` bigint(20) DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `estado` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `horadisponible` */

CREATE TABLE `horadisponible` (
  `idhoradisponible` bigint(20) DEFAULT NULL,
  `dia` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `horafin` time DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `horainicio` time DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpostulante` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `horarios` */

CREATE TABLE `horarios` (
  `gestion` int(11) DEFAULT NULL,
  `horario` bigint(20) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `plan_estudio` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `horasextra` */

CREATE TABLE `horasextra` (
  `idhorasextra` bigint(20) DEFAULT NULL,
  `horasextra` decimal(13,2) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `totalpagado` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idciclogeneracionplanilla` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `id_gen` */

CREATE TABLE `id_gen` (
  `GEN_NAME` varchar(30) NOT NULL,
  `GEN_VAL` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`GEN_NAME`),
  UNIQUE KEY `GEN_NAME_UNIQUE` (`GEN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ilva_movimientos` */

CREATE TABLE `ilva_movimientos` (
  `id` bigint(20) DEFAULT NULL,
  `cuen_id` bigint(20) DEFAULT NULL,
  `casoespecial` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `codigocontrol` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `fecha_registro` date DEFAULT NULL,
  `desc_pedido` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descreal` decimal(10,2) DEFAULT NULL,
  `descuento` decimal(10,0) DEFAULT NULL,
  `est_cod` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `glosa` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `en_un_recibo` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `motivo` varchar(300) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cant` int(11) DEFAULT NULL,
  `montocust` decimal(10,2) DEFAULT NULL,
  `montodesc` decimal(10,2) DEFAULT NULL,
  `montoneto` decimal(10,2) DEFAULT NULL,
  `montoteso` decimal(10,2) DEFAULT NULL,
  `nit` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nro_creditos` decimal(22,0) DEFAULT NULL,
  `nrofactura` varchar(12) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nropreimpreso` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nrorecibo` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `observacion` varchar(300) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `pi_id` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `solidaridad` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `plan_estudio` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `total_factura` decimal(10,2) DEFAULT NULL,
  `estado` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_cambio` decimal(10,2) DEFAULT NULL,
  `tipo_pago` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `unidad_acad_adm` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `usua_id` bigint(20) DEFAULT NULL,
  `pedido` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `dosi_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ilva_reimpresiones` */

CREATE TABLE `ilva_reimpresiones` (
  `id` bigint(20) DEFAULT NULL,
  `fecha_emision` date DEFAULT NULL,
  `fecha_reimp` date DEFAULT NULL,
  `glosa` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `usua_id_emision` bigint(20) DEFAULT NULL,
  `usua_id_reimp` bigint(20) DEFAULT NULL,
  `nit` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nrofactura` varchar(12) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `numero_reimp` int(11) DEFAULT NULL,
  `pi_id` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `plan_estudio` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `motivo` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `est_cod` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `unidad_acad_adm` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `pedido` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `dosi_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `impresionfactura` */

CREATE TABLE `impresionfactura` (
  `IDIMPRECIONFACTURA` bigint(20) NOT NULL,
  `FECHAIMPRESION` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TIPO` varchar(10) DEFAULT NULL,
  `IDMOVIMIENTO` bigint(20) NOT NULL,
  `IDUSUARIO` bigint(20) NOT NULL,
  `IDDOSIFICACION` bigint(20) DEFAULT NULL,
  `NROFACTURA` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`IDIMPRECIONFACTURA`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `impuestoproductor` */

CREATE TABLE `impuestoproductor` (
  `idimpuestoproductor` bigint(20) NOT NULL,
  `numeroformulario` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idgestionimpuesto` bigint(20) DEFAULT NULL,
  `idproductormateriaprima` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idimpuestoproductor`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ingredienteproduccion` */

CREATE TABLE `ingredienteproduccion` (
  `idingredienteproduccion` bigint(20) NOT NULL,
  `ingredienteverificable` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `formulamatematica` varchar(500) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
  `idcomposicionproducto` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idingredienteproduccion`),
  KEY `idcomposicionproducto` (`idcomposicionproducto`),
  KEY `idmetaproductoproduccion` (`idmetaproductoproduccion`),
  CONSTRAINT `ingredienteproduccion_ibfk_1` FOREIGN KEY (`idcomposicionproducto`) REFERENCES `composicionproducto` (`idcomposicionproducto`),
  CONSTRAINT `ingredienteproduccion_ibfk_2` FOREIGN KEY (`idmetaproductoproduccion`) REFERENCES `metaproductoproduccion` (`idmetaproductoproduccion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `institucion` */

CREATE TABLE `institucion` (
  `aniversario` date DEFAULT NULL,
  `razonsocial` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idinstitucion` bigint(20) NOT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idinstitucion`),
  KEY `fk_companiainstitucion` (`idcompania`),
  CONSTRAINT `fk_companiainstitucion` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `instituciones` */

CREATE TABLE `instituciones` (
  `pi_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nro_doc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `razon_soc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `insumoproduccion` */

CREATE TABLE `insumoproduccion` (
  `idinsumoproduccion` bigint(20) NOT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idinsumoproduccion`),
  CONSTRAINT `insumoproduccion_ibfk_1` FOREIGN KEY (`idinsumoproduccion`) REFERENCES `metaproductoproduccion` (`idmetaproductoproduccion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `intervalocomppnl` */

CREATE TABLE `intervalocomppnl` (
  `valormax` int(11) DEFAULT NULL,
  `valormin` int(11) DEFAULT NULL,
  `idintervalocomppnl` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `inv_almacenes` */

CREATE TABLE `inv_almacenes` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `cod_alm` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `cuenta` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctacosto` varchar(20) DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo` varchar(20) DEFAULT NULL,
  `id_responsable` bigint(20) DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `cod_est` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `inv_egr` int(11) DEFAULT NULL,
  PRIMARY KEY (`no_cia`,`cod_alm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_articulos` */

CREATE TABLE `inv_articulos` (
  `no_cia` varchar(2) NOT NULL,
  `cod_art` varchar(6) NOT NULL,
  `codact` varchar(50) DEFAULT NULL,
  `codsin` int(11) DEFAULT NULL,
  `codeq` varchar(6) DEFAULT NULL,
  `cod_alm` varchar(255) DEFAULT NULL,
  `descri` varchar(100) DEFAULT NULL,
  `control_valorado` varchar(255) DEFAULT NULL,
  `cantiad_equi` decimal(10,2) DEFAULT NULL,
  `cod_gru` varchar(3) DEFAULT NULL,
  `cod_med_may` varchar(6) DEFAULT NULL,
  `saldo_mon` decimal(20,6) DEFAULT NULL,
  `stockmaximo` decimal(16,6) DEFAULT NULL,
  `stockminimo` decimal(16,6) DEFAULT NULL,
  `nombrecorto` varchar(14) DEFAULT NULL,
  `cuenta_art` varchar(31) DEFAULT NULL,
  `vendible` varchar(255) DEFAULT NULL,
  `fix` int(11) DEFAULT NULL,
  `sigla` varchar(14) DEFAULT NULL,
  `pos` int(11) DEFAULT NULL,
  `estado` varchar(3) DEFAULT NULL,
  `cod_sub` varchar(3) DEFAULT NULL,
  `costo_uni` decimal(16,6) DEFAULT NULL,
  `cu` decimal(16,6) DEFAULT NULL,
  `ct` decimal(20,6) DEFAULT NULL,
  `cod_med` varchar(6) DEFAULT NULL,
  `cod_meds` int(11) DEFAULT NULL,
  `uni_meds` varchar(50) DEFAULT NULL,
  `med_pr` varchar(6) DEFAULT NULL,
  `cant_pr` decimal(16,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `precio_venta` decimal(10,2) DEFAULT NULL,
  `control_s` varchar(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`no_cia`,`cod_art`),
  UNIQUE KEY `cod_art` (`cod_art`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_grupos` */

CREATE TABLE `inv_grupos` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_gru` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta_inv` varchar(31) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `tipo` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta_ingreso` varchar(31) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_inicio` */

CREATE TABLE `inv_inicio` (
  `idinvinicio` bigint(20) NOT NULL,
  `cod_art` varchar(100) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `cantidad` decimal(16,2) DEFAULT NULL,
  `und` varchar(100) DEFAULT NULL,
  `alm` varchar(100) DEFAULT NULL,
  `costo_uni` decimal(16,6) DEFAULT NULL,
  `gestion` varchar(6) DEFAULT NULL,
  `no_cia` varchar(2) DEFAULT NULL,
  `cantx` decimal(16,2) DEFAULT NULL,
  PRIMARY KEY (`idinvinicio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_inventario` */

CREATE TABLE `inv_inventario` (
  `cod_art` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_alm` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `saldo_uni` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_inventario_detalle` */

CREATE TABLE `inv_inventario_detalle` (
  `id_inv_det` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_art` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cantidad` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `cod_alm` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_invmes` */

CREATE TABLE `inv_invmes` (
  `cod_art` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `mes` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_alm` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `monto_ent` decimal(16,6) DEFAULT NULL,
  `unidad_ent` decimal(12,2) DEFAULT NULL,
  `monto_sal` decimal(16,6) DEFAULT NULL,
  `unidad_sal` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_mov` */

CREATE TABLE `inv_mov` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha_cre` date DEFAULT NULL,
  `descri` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha_mov` date DEFAULT NULL,
  `no_usr` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `no_compro` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_compro` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_movdet` */

CREATE TABLE `inv_movdet` (
  `id_inv_movdet` bigint(20) NOT NULL DEFAULT '0',
  `monto` decimal(16,6) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_med` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `tipo_mov` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta_art` varchar(31) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_art` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `preciocompra` decimal(16,6) DEFAULT NULL,
  `cantidad` decimal(19,2) DEFAULT NULL,
  `residuo` decimal(12,2) DEFAULT NULL,
  `id_fuente` bigint(20) DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `costounitario` decimal(16,6) DEFAULT NULL,
  `preciounitcompra` decimal(16,6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `cod_alm` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `advertencia` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `id_inv_movdet_raiz` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_inv_movdet`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_periodo` */

CREATE TABLE `inv_periodo` (
  `id_inv_periodo` bigint(20) NOT NULL,
  `cod_art` varchar(6) NOT NULL,
  `saldofis` decimal(12,2) NOT NULL,
  `saldoval` decimal(12,2) NOT NULL,
  `costouni` decimal(16,6) NOT NULL,
  `mes` int(11) NOT NULL,
  `gestion` int(11) NOT NULL,
  `cod_alm` varchar(6) NOT NULL,
  `no_cia` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`id_inv_periodo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_product` */

CREATE TABLE `inv_product` (
  `id_inv_prod` bigint(20) NOT NULL DEFAULT '0',
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_art` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cantidad` decimal(12,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `cod_alm` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_inv_prod`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_product_register` */

CREATE TABLE `inv_product_register` (
  `idregistroinvproducto` bigint(20) NOT NULL,
  `fecha` datetime DEFAULT NULL,
  `cod_art` varchar(6) DEFAULT NULL,
  `cantidad` decimal(12,2) DEFAULT NULL,
  `tipo` varchar(64) DEFAULT NULL,
  `tipomovimiento` varchar(64) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `id_inv_prod` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idregistroinvproducto`),
  KEY `FK_INV_PRODUCT` (`id_inv_prod`),
  CONSTRAINT `FK_INV_PRODUCT` FOREIGN KEY (`id_inv_prod`) REFERENCES `inv_product` (`id_inv_prod`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_subgrupos` */

CREATE TABLE `inv_subgrupos` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_gru` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_sub` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_tipodocs` */

CREATE TABLE `inv_tipodocs` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_doc` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ctracuentamn` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `desc_def` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `restriccioncampo` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `tipo_vale` varchar(1) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_vales` */

CREATE TABLE `inv_vales` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans` varchar(10) NOT NULL,
  `contracuenta` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `cod_doc` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_vale` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `id_com_encoc` bigint(20) DEFAULT NULL,
  `no_trans_rec` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `oper` varchar(6) DEFAULT NULL,
  `dest` varchar(10) DEFAULT NULL,
  `orig` varchar(10) DEFAULT NULL,
  `dest_cod_cc` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_alm_dest` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `cod_alm` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tiporecepcion` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `no_cia_raiz` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans_raiz` varchar(10) DEFAULT NULL,
  `idcontratopuestosol` bigint(20) DEFAULT NULL,
  `id_responsable` bigint(20) DEFAULT NULL,
  `idunidadnegocio_dest` bigint(20) DEFAULT NULL,
  `id_responsable_dest` bigint(20) DEFAULT NULL,
  `idordenproduccion` bigint(20) DEFAULT NULL,
  `idproductobase` bigint(20) DEFAULT NULL,
  `idtmpenc` bigint(20) DEFAULT NULL,
  `idpedidostr` bigint(20) DEFAULT NULL,
  UNIQUE KEY `no_trans_UNIQUE` (`no_trans`),
  KEY `id_com_encoc` (`id_com_encoc`),
  KEY `idtmpenc` (`idtmpenc`),
  KEY `idpedidostr` (`idpedidostr`),
  CONSTRAINT `inv_vales_ibfk_1` FOREIGN KEY (`id_com_encoc`) REFERENCES `com_encoc` (`id_com_encoc`),
  CONSTRAINT `inv_vales_ibfk_2` FOREIGN KEY (`idtmpenc`) REFERENCES `sf_tmpenc` (`id_tmpenc`),
  CONSTRAINT `inv_vales_ibfk_3` FOREIGN KEY (`idpedidostr`) REFERENCES `pedidos` (`IDPEDIDOS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inv_ventart` */

CREATE TABLE `inv_ventart` (
  `id_mov` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_dosi` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `apellidopaterno` varchar(65) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_fact` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombres` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_vale` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `pedido` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_per` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nodoc_per` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_art` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cantidad` decimal(12,2) DEFAULT NULL,
  `apellidomaterno` varchar(65) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `cod_alm` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_est` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `identarticulo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inventario_detalle_log` */

CREATE TABLE `inventario_detalle_log` (
  `id_inventario_detalle_log` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `descripcion` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cantidad` decimal(12,2) DEFAULT NULL,
  `id_inv_det_origen` bigint(20) DEFAULT NULL,
  `id_inv_det_destino` bigint(20) DEFAULT NULL,
  `no_usr` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `inventarioproductoterminado` */

CREATE TABLE `inventarioproductoterminado` (
  `idinventarioproductoterminado` bigint(20) DEFAULT NULL,
  `cantidad` decimal(24,0) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idambientedeposito` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `jefesporcarr` */

CREATE TABLE `jefesporcarr` (
  `idjefesporcarr` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcarrera` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombrecarrera` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigoempleado` int(11) DEFAULT NULL,
  `nombresemp` varchar(40) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `generoemp` varchar(1) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `identificacionemp` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipodocumentoemp` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidopaternoemp` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidomaternoemp` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idfacultad` int(11) DEFAULT NULL,
  `nombrefac` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `idsede` int(11) DEFAULT NULL,
  `nombresede` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `libroventas` */

CREATE TABLE `libroventas` (
  `idlibroventa` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `total_factura` decimal(16,2) DEFAULT NULL,
  `nro_de_autorizacion` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo_de_control` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `importes_exentos` decimal(16,2) DEFAULT NULL,
  `total_ice` decimal(16,2) DEFAULT NULL,
  `nro_de_factura` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `importe_neto` decimal(16,2) DEFAULT NULL,
  `nro_nit_cliente` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre_razon_social_cliente` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `debito_fiscal` decimal(16,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `limite` */

CREATE TABLE `limite` (
  `idlimite` bigint(20) NOT NULL,
  `despuesfin` int(11) DEFAULT NULL,
  `despuesinicio` int(11) DEFAULT NULL,
  `antesfin` int(11) DEFAULT NULL,
  `antesinicio` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idlimite`),
  KEY `fk_companialimite` (`idcompania`),
  CONSTRAINT `fk_companialimite` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `logzonaproductiva` */

CREATE TABLE `logzonaproductiva` (
  `idlogzonaproductiva` bigint(20) NOT NULL,
  `fecha` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idzonaproductiva` bigint(20) DEFAULT NULL,
  `idproductormateriaprima` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idlogzonaproductiva`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `lugarrecepcion` */

CREATE TABLE `lugarrecepcion` (
  `idlugarrecepcion` bigint(20) DEFAULT NULL,
  `codigo` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `mantenimiento` */

CREATE TABLE `mantenimiento` (
  `idmant` bigint(20) DEFAULT NULL,
  `monto` decimal(16,6) DEFAULT NULL,
  `fechaentrega` date DEFAULT NULL,
  `fechaestimadarecepcion` date DEFAULT NULL,
  `fecharecepcion` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmoneda` bigint(20) DEFAULT NULL,
  `iddescripentrega` bigint(20) DEFAULT NULL,
  `iddescriprecepcion` bigint(20) DEFAULT NULL,
  `idestadorecepcion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `marca` */

CREATE TABLE `marca` (
  `idmarca` bigint(20) DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `materialproduccion` */

CREATE TABLE `materialproduccion` (
  `idmaterialproduccion` bigint(20) NOT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idmaterialproduccion`),
  CONSTRAINT `materialproduccion_ibfk_1` FOREIGN KEY (`idmaterialproduccion`) REFERENCES `metaproductoproduccion` (`idmetaproductoproduccion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `metaproductoproduccion` */

CREATE TABLE `metaproductoproduccion` (
  `idmetaproductoproduccion` bigint(20) NOT NULL,
  `codigo` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `esacopiable` int(11) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(500) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_art` varchar(6) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idunidadmedidaproduccion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idmetaproductoproduccion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `modalidadcontrato` */

CREATE TABLE `modalidadcontrato` (
  `idmodalidadcontrato` bigint(20) NOT NULL,
  `duraciondias` int(11) DEFAULT NULL,
  `definition` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idmodalidadcontrato`),
  KEY `fk_companiamodalidadcontrato` (`idcompania`),
  CONSTRAINT `fk_companiamodalidadcontrato` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `modelo` */

CREATE TABLE `modelo` (
  `idmodelo` bigint(20) DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `modulo` */

CREATE TABLE `modulo` (
  `idmodulo` bigint(20) DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `nombrerecurso` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `modulocompania` */

CREATE TABLE `modulocompania` (
  `idcompania` bigint(20) DEFAULT NULL,
  `idmodulo` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `moduloprov` */

CREATE TABLE `moduloprov` (
  `idmoduloprov` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `modulo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_prov` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `moneda` */

CREATE TABLE `moneda` (
  `idmoneda` bigint(20) NOT NULL DEFAULT '0',
  `codigomoneda` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `simbolo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idmoneda`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `motivoocestadoaf` */

CREATE TABLE `motivoocestadoaf` (
  `idmotivoocestadoaf` bigint(20) DEFAULT NULL,
  `estadoaf` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmotivoordenc` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `motivoordencomp` */

CREATE TABLE `motivoordencomp` (
  `idmotivoordenc` bigint(20) DEFAULT NULL,
  `codigo` int(11) DEFAULT NULL,
  `descripcion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `requiereactivos` int(11) DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `movfondorota` */

CREATE TABLE `movfondorota` (
  `idmovimiento` bigint(20) DEFAULT NULL,
  `pagodocbannodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `pagodocbantipodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobroajtctactbcuenta` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobroajtctactbnombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobroctacajanombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobroctacajacuenta` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `pagocajanombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `pagocajacuenta` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `pagocajanodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo` int(11) DEFAULT NULL,
  `montocobro` decimal(12,2) DEFAULT NULL,
  `monedacobro` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `cobroajtdepnodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobroajtdeptipodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobrodocbannodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobrodocbantipodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobrodocnodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobrodoctipodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocambio` decimal(12,2) DEFAULT NULL,
  `clasemov` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipomov` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `observacion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `montopago` decimal(12,2) DEFAULT NULL,
  `monedapago` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobroplanombreges` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cobroocnoorden` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notrans` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacompro` date DEFAULT NULL,
  `nocompro` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocompro` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idfondorotatorio` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `movimiento` */

CREATE TABLE `movimiento` (
  `IDMOVIMIENTO` bigint(20) NOT NULL,
  `FECHA_FACTURA` date DEFAULT NULL,
  `GLOSA` varchar(300) DEFAULT NULL,
  `ESTADO` varchar(15) DEFAULT NULL,
  `TIPOEMISION` varchar(100) DEFAULT NULL,
  `NROFACTURA` int(11) DEFAULT NULL,
  `CUF` varchar(255) DEFAULT NULL,
  `FECHASIN` varchar(100) DEFAULT NULL,
  `NIT_CLIENTE` varchar(50) DEFAULT NULL,
  `RAZON_SOCIAL` varchar(200) DEFAULT NULL,
  `IMPORTE_TOTAL` decimal(10,2) DEFAULT NULL,
  `IMPORTE_ICE_IEHD_TASAS` decimal(10,2) DEFAULT NULL,
  `EXPORT_EXENTAS` decimal(10,2) DEFAULT NULL,
  `VENTAS_GRAB_TASACERO` decimal(10,2) DEFAULT NULL,
  `SUBTOTAL` decimal(10,2) DEFAULT NULL,
  `DESCUENTOS` decimal(10,2) DEFAULT NULL,
  `IMPORTE_PARA_DEBITO_FISCAL` decimal(10,2) DEFAULT NULL,
  `DEBITO_FISCAL` decimal(10,2) DEFAULT NULL,
  `CODIGOCONTROL` varchar(30) DEFAULT NULL,
  `TIPOPAGO` varchar(10) DEFAULT NULL,
  `TIPOCAMBIO` double DEFAULT NULL,
  `FECHAREGISTRO` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CODIGO_QR` varchar(150) NOT NULL,
  `NRO_AUTORIZACION` varchar(15) NOT NULL,
  `LEYENDA` varchar(300) DEFAULT NULL,
  `DESCRI` varchar(50) DEFAULT NULL,
  `CODESTADO` varchar(10) DEFAULT NULL,
  `CODIGOREC` varchar(100) DEFAULT NULL,
  `FACTURA` longtext,
  `id_tmpdet` bigint(20) DEFAULT NULL,
  `IDPEDIDOS` bigint(20) DEFAULT NULL,
  `IDVENTADIRECTA` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`IDMOVIMIENTO`),
  KEY `IDPEDIDOS` (`IDPEDIDOS`),
  KEY `IDVENTADIRECTA` (`IDVENTADIRECTA`),
  KEY `id_tmpdet` (`id_tmpdet`),
  CONSTRAINT `movimiento_ibfk_1` FOREIGN KEY (`IDPEDIDOS`) REFERENCES `pedidos` (`IDPEDIDOS`),
  CONSTRAINT `movimiento_ibfk_2` FOREIGN KEY (`IDVENTADIRECTA`) REFERENCES `ventadirecta` (`IDVENTADIRECTA`),
  CONSTRAINT `movimiento_ibfk_3` FOREIGN KEY (`id_tmpdet`) REFERENCES `sf_tmpdet` (`id_tmpdet`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `movimientosalariogab` */

CREATE TABLE `movimientosalariogab` (
  `idmovimientosalariogab` bigint(20) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `descripcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `valor` decimal(16,2) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idzonaproductiva` bigint(20) DEFAULT NULL,
  `idtipomovimientogab` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `movimientosalarioproductor` */

CREATE TABLE `movimientosalarioproductor` (
  `idmovimientosalarioproductor` bigint(20) NOT NULL,
  `fecha` date DEFAULT NULL,
  `descripcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `valor` decimal(16,2) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idzonaproductiva` bigint(20) DEFAULT NULL,
  `idproductormateriaprima` bigint(20) DEFAULT NULL,
  `idtipomovimientoproductor` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idmovimientosalarioproductor`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `movimientosueldo` */

CREATE TABLE `movimientosueldo` (
  `idmovimientosueldo` bigint(20) DEFAULT NULL,
  `cantidad` decimal(13,2) DEFAULT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `descripcion` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechamodificacion` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuariocreador` bigint(20) DEFAULT NULL,
  `idmoneda` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idgestionplanilla` bigint(20) DEFAULT NULL,
  `idtipomovimientosueldo` bigint(20) DEFAULT NULL,
  `idusuarioeditor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `niveles_asignatura` */

CREATE TABLE `niveles_asignatura` (
  `nivel_asignatura` varchar(5) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descrip_niv_asig` varchar(40) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `unidad` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `grado` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `nivelorganizacional` */

CREATE TABLE `nivelorganizacional` (
  `idnivelorganizacional` bigint(20) NOT NULL,
  `sigla` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `nivelorganizacionalraiz` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idnivelorganizacional`),
  KEY `fk_companianivelorganizacional` (`idcompania`),
  KEY `fk_nivelorganizacionalnivelorg` (`nivelorganizacionalraiz`),
  CONSTRAINT `fk_companianivelorganizacional` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_nivelorganizacionalnivelorg` FOREIGN KEY (`nivelorganizacionalraiz`) REFERENCES `nivelorganizacional` (`idnivelorganizacional`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `notarechazomateriaprima` */

CREATE TABLE `notarechazomateriaprima` (
  `idnotarechazomateriaprima` bigint(20) NOT NULL,
  `acida` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `calostro` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `sucia` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tachomalestado` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `observaciones` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `otros` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cantidadrechazada` decimal(15,0) DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `aguadabajosng` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
  `idproductormateriaprima` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idnotarechazomateriaprima`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `operaciones` */

CREATE TABLE `operaciones` (
  `IDOPERACIONES` int(11) NOT NULL,
  `descripcion` varchar(100) DEFAULT NULL,
  `NOMBRE` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`IDOPERACIONES`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ordencomactivo` */

CREATE TABLE `ordencomactivo` (
  `idordencomactivo` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idactivo` bigint(20) DEFAULT NULL,
  `idordencompra` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ordeninsumo` */

CREATE TABLE `ordeninsumo` (
  `idordeninsumo` bigint(20) NOT NULL,
  `cantidad` decimal(16,6) DEFAULT NULL,
  `cantidadstock` decimal(24,0) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `costototal` decimal(16,6) DEFAULT NULL,
  `costounitario` decimal(16,6) DEFAULT NULL,
  `formulamatematica` varchar(500) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_art` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idproductobase` bigint(20) DEFAULT NULL,
  `idordenproduccion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idordeninsumo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ordenmaterial` */

CREATE TABLE `ordenmaterial` (
  `idordenmaterial` bigint(20) NOT NULL,
  `cantidadpesosolicitada` decimal(16,2) DEFAULT NULL,
  `cantidadunidadsolicitada` decimal(16,2) DEFAULT NULL,
  `cantidadpesoretornada` decimal(16,2) DEFAULT NULL,
  `cantidadpesousada` decimal(16,2) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `costototal` decimal(16,6) DEFAULT NULL,
  `costounitario` decimal(16,6) DEFAULT NULL,
  `cod_art` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idordenproduccion` bigint(20) DEFAULT NULL,
  `idproductosimple` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idordenmaterial`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ordenproduccion` */

CREATE TABLE `ordenproduccion` (
  `idordenproduccion` bigint(20) NOT NULL,
  `codigo` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `pesocontenedor` decimal(24,0) DEFAULT NULL,
  `estadoorden` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cantidadesperada` decimal(24,0) DEFAULT NULL,
  `porcentajegrasa` decimal(16,2) DEFAULT NULL,
  `id_tmpenc` bigint(20) DEFAULT NULL,
  `no_trans` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `no_vale` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cantidadproducida` decimal(24,0) DEFAULT NULL,
  `cantidadproducidaresponsable` decimal(24,0) DEFAULT NULL,
  `costinsumoprincipal` decimal(16,2) DEFAULT NULL,
  `costotoalproduccion` decimal(16,2) DEFAULT NULL,
  `totalcostoindirecto` decimal(16,2) DEFAULT NULL,
  `preciototalinsumo` decimal(16,2) DEFAULT NULL,
  `preciototalmanoobra` decimal(16,2) DEFAULT NULL,
  `preciototalmaterial` decimal(16,2) DEFAULT NULL,
  `costounitario` decimal(16,6) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcomposicionproducto` bigint(20) DEFAULT NULL,
  `productopadre` bigint(20) DEFAULT NULL,
  `idplanificacionproduccion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idordenproduccion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `pago` */

CREATE TABLE `pago` (
  `id_pago` bigint(20) NOT NULL,
  `fecha` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pago` double DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `IDPERSONACLIENTE` bigint(20) NOT NULL,
  `idusuario` bigint(20) NOT NULL,
  `id_tmpenc` bigint(20) DEFAULT NULL,
  `IDSUCURSAL` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_pago`),
  KEY `pago_persona_FK` (`IDPERSONACLIENTE`),
  KEY `pago_usuario_FK` (`idusuario`),
  KEY `pago_sf_tmpenc_FK` (`id_tmpenc`),
  CONSTRAINT `pago_persona_FK` FOREIGN KEY (`IDPERSONACLIENTE`) REFERENCES `personacliente` (`IDPERSONACLIENTE`),
  CONSTRAINT `pago_sf_tmpenc_FK` FOREIGN KEY (`id_tmpenc`) REFERENCES `sf_tmpenc` (`id_tmpenc`),
  CONSTRAINT `pago_usuario_FK` FOREIGN KEY (`idusuario`) REFERENCES `usuario` (`idusuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pagofondorota` */

CREATE TABLE `pagofondorota` (
  `idpago` bigint(20) DEFAULT NULL,
  `fechaaprobacion` date DEFAULT NULL,
  `cuentabanco` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombrebeneficiario` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipobeneficiario` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentaajuste` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentacaja` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo` int(11) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacreacion` date DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocambio` decimal(12,2) DEFAULT NULL,
  `montopago` decimal(12,2) DEFAULT NULL,
  `monedapago` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechapago` date DEFAULT NULL,
  `motivoreversion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipopago` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `montoorigen` decimal(12,2) DEFAULT NULL,
  `monedaorigen` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notrans` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `anuladopor` bigint(20) DEFAULT NULL,
  `aprobadopor` bigint(20) DEFAULT NULL,
  `idsededestinocheque` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `creadopor` bigint(20) DEFAULT NULL,
  `idfondorotatorio` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `pagoordencompra` */

CREATE TABLE `pagoordencompra` (
  `idpagoordencompra` bigint(20) DEFAULT NULL,
  `fechaaprobacion` date DEFAULT NULL,
  `cuentabanco` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombrebeneficiario` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipobeneficiario` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuentacaja` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fechacreacion` date DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipocambio` decimal(12,2) DEFAULT NULL,
  `montopago` decimal(12,2) DEFAULT NULL,
  `monedapago` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipopago` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `idordencompra` bigint(20) DEFAULT NULL,
  `clasepago` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `montoorigen` decimal(12,2) DEFAULT NULL,
  `monedaorigen` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `notrans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `aprobadopor` bigint(20) DEFAULT NULL,
  `idsededestinocheque` bigint(20) DEFAULT NULL,
  `creadopor` bigint(20) DEFAULT NULL,
  `idfondorotatorio` bigint(20) DEFAULT NULL,
  `id_tmpenc` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pais` */

CREATE TABLE `pais` (
  `idpais` bigint(20) NOT NULL,
  `codigoarea` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `prefijo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idpais`),
  KEY `fk_compania` (`idcompania`),
  CONSTRAINT `fk_compania` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `parteactfijo` */

CREATE TABLE `parteactfijo` (
  `idparteactfijo` bigint(20) DEFAULT NULL,
  `descripcion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numero` bigint(20) DEFAULT NULL,
  `serie` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `preciouni` decimal(16,6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idactivofijo` bigint(20) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `unidadmedida` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `parteafoc` */

CREATE TABLE `parteafoc` (
  `idparteafoc` bigint(20) DEFAULT NULL,
  `descripcion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `serie` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `preciouni` decimal(16,6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idactivofijo` bigint(20) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `unidadmedida` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `id_com_encoc` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `partedetoc` */

CREATE TABLE `partedetoc` (
  `idpartedetoc` bigint(20) DEFAULT NULL,
  `descripcion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numero` bigint(20) DEFAULT NULL,
  `total` decimal(16,6) DEFAULT NULL,
  `preciouni` decimal(16,6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `iddetalleafoc` bigint(20) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `unidadmedida` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `pedidos` */

CREATE TABLE `pedidos` (
  `IDPEDIDOS` bigint(20) NOT NULL,
  `DESCRIPCION` varchar(255) DEFAULT NULL,
  `FACTURA` varchar(255) DEFAULT NULL,
  `FECHA_A_PAGAR` date DEFAULT NULL,
  `FECHA_ENTREGA` date DEFAULT NULL,
  `FECHA_PEDIDO` date DEFAULT NULL,
  `CODIGO` bigint(20) DEFAULT NULL,
  `IDDIRECCION` bigint(20) DEFAULT NULL,
  `OBSERVACION` varchar(255) DEFAULT NULL,
  `PORCENTAJECOMISION` double DEFAULT NULL,
  `PORCENTAJEGARANTIA` double DEFAULT NULL,
  `SUPERVISOR` bigint(20) DEFAULT NULL,
  `TOTAL` double DEFAULT NULL,
  `ESTADO_PEDIDO` bigint(20) DEFAULT NULL,
  `IDCLIENTE` bigint(20) DEFAULT NULL,
  `iddistribuidor` bigint(20) DEFAULT NULL,
  `tipoventa` varchar(100) DEFAULT NULL,
  `idtipoventa` bigint(20) DEFAULT NULL,
  `IDTIPOPEDIDO` bigint(20) DEFAULT NULL,
  `ESTADO` varchar(15) DEFAULT NULL,
  `TOTALIMPORTE` double NOT NULL,
  `DESCUENTOADICIONAL` decimal(16,2) DEFAULT NULL,
  `DESCUENTOPRODUCTO` decimal(16,2) DEFAULT NULL,
  `MONTODIST` decimal(16,2) DEFAULT NULL,
  `IDUSUARIO` bigint(20) DEFAULT NULL,
  `VALORCOMISION` double DEFAULT '0',
  `VALORGARANTIA` double DEFAULT NULL,
  `CON_REPOSICION` int(11) DEFAULT '0',
  `IDMOVIMIENTO` bigint(20) DEFAULT NULL,
  `PEDIDOSREPOSICION` bigint(20) DEFAULT NULL,
  `IMPUESTO` double DEFAULT NULL,
  `TIENEFACTURA` tinyint(1) DEFAULT '1',
  `id_tmpenc` bigint(20) DEFAULT NULL,
  `flagct` int(11) DEFAULT '0',
  `CONTABILIZADO` tinyint(1) DEFAULT NULL,
  `IDSUCURSAL` bigint(20) DEFAULT NULL,
  `flagstock` int(11) DEFAULT '0',
  `id_tmpenc_cv` bigint(20) DEFAULT NULL,
  `CV` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`IDPEDIDOS`),
  KEY `pedidos_sf_tmpenc_FK` (`id_tmpenc`),
  KEY `IDCLIENTE` (`IDCLIENTE`),
  KEY `IDTIPOPEDIDO` (`IDTIPOPEDIDO`),
  KEY `IDUSUARIO` (`IDUSUARIO`),
  KEY `IDMOVIMIENTO` (`IDMOVIMIENTO`),
  KEY `idtipoventa` (`idtipoventa`),
  KEY `iddistribuidor` (`iddistribuidor`),
  CONSTRAINT `pedidos_ibfk_1` FOREIGN KEY (`IDCLIENTE`) REFERENCES `personacliente` (`IDPERSONACLIENTE`),
  CONSTRAINT `pedidos_ibfk_2` FOREIGN KEY (`IDTIPOPEDIDO`) REFERENCES `tipopedido` (`IDTIPOPEDIDO`),
  CONSTRAINT `pedidos_ibfk_3` FOREIGN KEY (`IDUSUARIO`) REFERENCES `usuario` (`idusuario`),
  CONSTRAINT `pedidos_ibfk_4` FOREIGN KEY (`IDMOVIMIENTO`) REFERENCES `movimiento` (`IDMOVIMIENTO`),
  CONSTRAINT `pedidos_ibfk_5` FOREIGN KEY (`id_tmpenc`) REFERENCES `sf_tmpenc` (`id_tmpenc`),
  CONSTRAINT `pedidos_ibfk_6` FOREIGN KEY (`idtipoventa`) REFERENCES `tipoventa` (`idtipoventa`),
  CONSTRAINT `pedidos_ibfk_7` FOREIGN KEY (`iddistribuidor`) REFERENCES `distribuidor` (`iddistribuidor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `per_insts` */

CREATE TABLE `per_insts` (
  `id` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `hst` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `factura` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `mail` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nro_doc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tel_ref` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `supervisor` varchar(255) DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tdo_cod` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `periodoacademico` */

CREATE TABLE `periodoacademico` (
  `idperiodocad` bigint(20) DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idreferencia` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `indice` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `periodocostoindirecto` */

CREATE TABLE `periodocostoindirecto` (
  `idperiodocostoindirecto` bigint(20) NOT NULL,
  `mes` int(11) DEFAULT NULL,
  `procesado` int(11) DEFAULT NULL,
  `contab` int(11) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idperiodocostoindirecto`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `periodos` */

CREATE TABLE `periodos` (
  `periodo` int(11) DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `persona` */

CREATE TABLE `persona` (
  `fechanacimiento` date DEFAULT NULL,
  `nombres` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `genero` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `domicilio` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `apellidopaterno` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `apellidomaterno` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `profesion` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `telcelular` varchar(20) DEFAULT NULL,
  `idpersona` bigint(20) NOT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpais` bigint(20) DEFAULT NULL,
  `idestadocivil` bigint(20) DEFAULT NULL,
  `idsaludo` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idpersona`),
  KEY `fk_companiapersona` (`idcompania`),
  KEY `fk_estadocivilpersona` (`idestadocivil`),
  KEY `fk_paispersona` (`idpais`),
  KEY `fk_saludopersona` (`idsaludo`),
  CONSTRAINT `fk_companiapersona` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_estadocivilpersona` FOREIGN KEY (`idestadocivil`) REFERENCES `estadocivil` (`idestadocivil`),
  CONSTRAINT `fk_paispersona` FOREIGN KEY (`idpais`) REFERENCES `pais` (`idpais`),
  CONSTRAINT `fk_saludopersona` FOREIGN KEY (`idsaludo`) REFERENCES `saludo` (`idsaludo`),
  CONSTRAINT `persona_ibfk_1` FOREIGN KEY (`idpersona`) REFERENCES `entidad` (`identidad`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `personacliente` */

CREATE TABLE `personacliente` (
  `IDPERSONACLIENTE` bigint(20) NOT NULL,
  `AM` varchar(255) DEFAULT NULL,
  `AP` varchar(255) DEFAULT NULL,
  `CEM_COD` varchar(255) DEFAULT NULL,
  `EST_CIVIL` varchar(255) DEFAULT NULL,
  `FECHA_NAC` date DEFAULT NULL,
  `NOM` varchar(255) DEFAULT NULL,
  `NRO_DOC` varchar(255) DEFAULT NULL,
  `COMP` varchar(10) DEFAULT NULL,
  `OCU_COD` varchar(255) DEFAULT NULL,
  `SEXO` varchar(255) DEFAULT NULL,
  `SIS_COD` varchar(255) DEFAULT NULL,
  `TDO_COD` varchar(255) DEFAULT NULL,
  `TIPO_PERSONA` varchar(45) DEFAULT NULL,
  `ctaregula` varchar(20) DEFAULT NULL,
  `DIRECCION` varchar(100) DEFAULT NULL,
  `TELEFONO` int(11) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `idtipodocsin` bigint(20) DEFAULT NULL,
  `NIT` varchar(20) DEFAULT NULL,
  `CODIGOCLIENTE` varchar(10) DEFAULT NULL,
  `RAZONSOCIAL` varchar(100) DEFAULT NULL,
  `CODMETODOPAGOSIN` int(11) DEFAULT NULL,
  `OBSERVACION` varchar(100) DEFAULT NULL,
  `IDRETENCION` bigint(20) DEFAULT NULL,
  `IDTIPOCLIENTE` bigint(20) DEFAULT NULL,
  `idcategoriacliente` bigint(20) DEFAULT NULL,
  `IDDEPARTAMENTO` bigint(20) DEFAULT NULL,
  `DESCUENTO` decimal(10,2) DEFAULT NULL,
  `DESCUENTOPROD` decimal(10,2) DEFAULT NULL,
  `PORCENTAJECOMISION` double DEFAULT NULL,
  `PORCENTAJEGARANTIA` double DEFAULT NULL,
  `IDTERRITORIOTRABAJO` bigint(20) DEFAULT NULL,
  `con_factura` int(11) NOT NULL DEFAULT '1',
  `ESPERSONA` int(11) DEFAULT NULL,
  `deuda` double DEFAULT NULL,
  PRIMARY KEY (`IDPERSONACLIENTE`),
  KEY `idcategoriacliente` (`idcategoriacliente`),
  CONSTRAINT `personacliente_ibfk_1` FOREIGN KEY (`idcategoriacliente`) REFERENCES `categoriacliente` (`idcategoriacliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `personas` */

CREATE TABLE `personas` (
  `pi_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nro_doc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ap` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `am` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nom` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `planes_estudios` */

CREATE TABLE `planes_estudios` (
  `plan_estudio` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `unidad_acad_adm` int(11) DEFAULT NULL,
  `unidad` int(11) DEFAULT NULL,
  `desc_plan` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `planificacionproduccion` */

CREATE TABLE `planificacionproduccion` (
  `idplanificacionproduccion` bigint(20) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `observaciones` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `totallecheproducida` decimal(9,0) DEFAULT NULL,
  `totallechequeso` decimal(9,0) DEFAULT NULL,
  `totallechereproceso` decimal(9,0) DEFAULT NULL,
  `totallecheuht` decimal(9,0) DEFAULT NULL,
  `totallecheyogurt` decimal(9,0) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillaacopio` */

CREATE TABLE `planillaacopio` (
  `idplanillaacopio` bigint(20) NOT NULL,
  `fecha` date DEFAULT NULL,
  `porcentajegrasa` decimal(16,2) DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `totalpesado` decimal(16,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillaacopio`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillaadministrativos` */

CREATE TABLE `planillaadministrativos` (
  `idplanillaadministrativos` bigint(20) NOT NULL DEFAULT '0',
  `descuentoporminutosausencia` decimal(13,2) DEFAULT NULL,
  `activogenplanfis` int(11) DEFAULT NULL,
  `anticipo` decimal(13,2) DEFAULT NULL,
  `afps` decimal(13,2) DEFAULT NULL,
  `area` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `minutosausenciabandas` int(11) DEFAULT NULL,
  `basicoganado` decimal(13,2) DEFAULT NULL,
  `categoria` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechafincontrato` date DEFAULT NULL,
  `fechainiciocontrato` date DEFAULT NULL,
  `modalidadcontratacion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `diferencia` decimal(13,2) DEFAULT NULL,
  `descuentossinretencion` decimal(13,2) DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  `registrocontable` int(11) DEFAULT NULL,
  `pagoactivo` int(11) DEFAULT NULL,
  `ingresofueraiva` decimal(13,2) DEFAULT NULL,
  `seguro` decimal(13,2) DEFAULT NULL,
  `saldoiva` decimal(13,2) DEFAULT NULL,
  `retencioniva` decimal(13,2) DEFAULT NULL,
  `puesto` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipoempleado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `totallaboral` decimal(13,2) DEFAULT NULL,
  `saldoivaanterior` decimal(13,2) DEFAULT NULL,
  `liquidopagable` decimal(13,2) DEFAULT NULL,
  `prestamo` decimal(13,2) DEFAULT NULL,
  `numerominutosausenciabandas` int(11) DEFAULT NULL,
  `otrosdescuentos` decimal(13,2) DEFAULT NULL,
  `otrosingresos` decimal(13,2) DEFAULT NULL,
  `totalpatronal` decimal(13,2) DEFAULT NULL,
  `provivienda` decimal(13,2) DEFAULT NULL,
  `rciva` decimal(13,2) DEFAULT NULL,
  `sueldo` decimal(13,2) DEFAULT NULL,
  `atrasos` decimal(13,2) DEFAULT NULL,
  `minutosatraso` int(11) DEFAULT NULL,
  `descuentoporminutosatraso` decimal(13,2) DEFAULT NULL,
  `totaldescuento` decimal(13,2) DEFAULT NULL,
  `totalganado` decimal(13,2) DEFAULT NULL,
  `unidad` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `win` decimal(13,2) DEFAULT NULL,
  `diastrabajados` decimal(13,2) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcargo` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillaadministrativos`),
  KEY `idempleado` (`idempleado`),
  KEY `idcategoriapuesto` (`idcategoriapuesto`),
  KEY `idcompania` (`idcompania`),
  KEY `idcargo` (`idcargo`),
  KEY `idunidadnegocio` (`idunidadnegocio`),
  KEY `idplanillagenerada` (`idplanillagenerada`),
  CONSTRAINT `planillaadministrativos_ibfk_1` FOREIGN KEY (`idempleado`) REFERENCES `empleado` (`idempleado`),
  CONSTRAINT `planillaadministrativos_ibfk_2` FOREIGN KEY (`idcategoriapuesto`) REFERENCES `categoriapuesto` (`idcategoriapuesto`),
  CONSTRAINT `planillaadministrativos_ibfk_3` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `planillaadministrativos_ibfk_4` FOREIGN KEY (`idcargo`) REFERENCES `cargo` (`idcargo`),
  CONSTRAINT `planillaadministrativos_ibfk_5` FOREIGN KEY (`idunidadnegocio`) REFERENCES `unidadnegocio` (`idunidadnegocio`),
  CONSTRAINT `planillaadministrativos_ibfk_6` FOREIGN KEY (`idplanillagenerada`) REFERENCES `planillagenerada` (`idplanillagenerada`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillaaguinaldo` */

CREATE TABLE `planillaaguinaldo` (
  `idplanillaaguinaldo` bigint(20) NOT NULL AUTO_INCREMENT,
  `activogenplanfis` int(11) DEFAULT NULL,
  `area` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `sueldopromedio` decimal(13,2) DEFAULT NULL,
  `cuentabancaria` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `monedactabancaria` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocliente` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechafincontrato` date DEFAULT NULL,
  `fechainiciocontrato` date DEFAULT NULL,
  `sueldocotizable` decimal(13,2) DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  `registrocontable` int(11) DEFAULT NULL,
  `pagoactivo` int(11) DEFAULT NULL,
  `liquidopagable` decimal(13,2) DEFAULT NULL,
  `totalganadonoviembre` decimal(13,2) DEFAULT NULL,
  `totalganadooctubre` decimal(13,2) DEFAULT NULL,
  `sueldo` decimal(13,2) DEFAULT NULL,
  `totalganadoseptiembre` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `diastrabajados` decimal(13,2) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcargo` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillaaguinaldo`)
) ENGINE=InnoDB AUTO_INCREMENT=238 DEFAULT CHARSET=latin1;

/*Table structure for table `planilladocentelaboral` */

CREATE TABLE `planilladocentelaboral` (
  `idplanilladocentelaboral` bigint(20) DEFAULT NULL,
  `descuentoporminutosausencia` decimal(13,2) DEFAULT NULL,
  `numerocuenta` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `activogenplanfis` int(11) DEFAULT NULL,
  `anticipo` decimal(13,2) DEFAULT NULL,
  `afps` decimal(13,2) DEFAULT NULL,
  `area` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `minutosausenciabandas` int(11) DEFAULT NULL,
  `basicoganado` decimal(13,2) DEFAULT NULL,
  `sueldobasico` decimal(13,2) DEFAULT NULL,
  `categoria` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocliente` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechafincontrato` date DEFAULT NULL,
  `fechainiciocontrato` date DEFAULT NULL,
  `modalidadcontratacion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `diferencia` decimal(13,2) DEFAULT NULL,
  `descuentossinretencion` decimal(13,2) DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  `registrocontable` int(11) DEFAULT NULL,
  `pagoactivo` int(11) DEFAULT NULL,
  `ingresofueraiva` decimal(13,2) DEFAULT NULL,
  `seguro` decimal(13,2) DEFAULT NULL,
  `saldoiva` decimal(13,2) DEFAULT NULL,
  `retencioniva` decimal(13,2) DEFAULT NULL,
  `puesto` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipoempleado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `totallaboral` decimal(13,2) DEFAULT NULL,
  `saldoivaanterior` decimal(13,2) DEFAULT NULL,
  `liquidopagable` decimal(13,2) DEFAULT NULL,
  `prestamo` decimal(13,2) DEFAULT NULL,
  `numerominutosausenciabandas` int(11) DEFAULT NULL,
  `otrosdescuentos` decimal(13,2) DEFAULT NULL,
  `otrosingresos` decimal(13,2) DEFAULT NULL,
  `totalpatronal` decimal(13,2) DEFAULT NULL,
  `tipodepago` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `provivienda` decimal(13,2) DEFAULT NULL,
  `rciva` decimal(13,2) DEFAULT NULL,
  `atrasos` decimal(13,2) DEFAULT NULL,
  `minutosatraso` int(11) DEFAULT NULL,
  `descuentoporminutosatraso` decimal(13,2) DEFAULT NULL,
  `totaldescuento` decimal(13,2) DEFAULT NULL,
  `totalganado` decimal(13,2) DEFAULT NULL,
  `unidad` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `win` decimal(13,2) DEFAULT NULL,
  `diastrabajados` decimal(13,2) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcargo` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmonedacuenta` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillafiscal` */

CREATE TABLE `planillafiscal` (
  `idplanillafiscal` bigint(20) NOT NULL DEFAULT '0',
  `descuentoporminutosausencia` decimal(13,2) DEFAULT NULL,
  `anticipo` decimal(13,2) DEFAULT NULL,
  `haberbasico` decimal(13,2) DEFAULT NULL,
  `fechanacimiento` date DEFAULT NULL,
  `fechaingreso` date DEFAULT NULL,
  `horasextra` decimal(13,2) DEFAULT NULL,
  `costohorasextra` decimal(13,2) DEFAULT NULL,
  `genero` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `horasdiapagado` decimal(13,2) DEFAULT NULL,
  `liquidopagable` decimal(13,2) DEFAULT NULL,
  `prestamo` decimal(13,2) DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nacionalidad` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `novedad` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numero` bigint(20) DEFAULT NULL,
  `ocupacion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `otrosbonos` decimal(13,2) DEFAULT NULL,
  `otrosdescuentos` decimal(13,2) DEFAULT NULL,
  `otrosingresos` decimal(13,2) DEFAULT NULL,
  `otrosdescuentosmovsal` decimal(13,2) DEFAULT NULL,
  `diaspagados` decimal(13,2) DEFAULT NULL,
  `ci` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `bonoproduccion` decimal(13,2) DEFAULT NULL,
  `retencionafp` decimal(13,2) DEFAULT NULL,
  `liquidacionretencion` decimal(13,2) DEFAULT NULL,
  `bonoantiguedad` decimal(13,2) DEFAULT NULL,
  `aniosantiguedad` int(11) DEFAULT NULL,
  `bonodominical` decimal(13,2) DEFAULT NULL,
  `descuentoporminutosatraso` decimal(13,2) DEFAULT NULL,
  `totaldescuentos` decimal(13,2) DEFAULT NULL,
  `totalganado` decimal(13,2) DEFAULT NULL,
  `win` decimal(13,2) DEFAULT NULL,
  `diastrabajados` decimal(13,2) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `idciclogeneracionplanilla` bigint(20) DEFAULT NULL,
  `idplanillatributaria` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillafiscal`),
  KEY `idciclogeneracionplanilla` (`idciclogeneracionplanilla`),
  KEY `idempleado` (`idempleado`),
  KEY `idcontratopuesto` (`idcontratopuesto`),
  KEY `idplanillatributaria` (`idplanillatributaria`),
  CONSTRAINT `planillafiscal_ibfk_1` FOREIGN KEY (`idciclogeneracionplanilla`) REFERENCES `ciclogeneracionplanilla` (`idciclogeneracionplanilla`),
  CONSTRAINT `planillafiscal_ibfk_2` FOREIGN KEY (`idempleado`) REFERENCES `empleado` (`idempleado`),
  CONSTRAINT `planillafiscal_ibfk_3` FOREIGN KEY (`idcontratopuesto`) REFERENCES `contratopuesto` (`idcontratopuesto`),
  CONSTRAINT `planillafiscal_ibfk_4` FOREIGN KEY (`idplanillatributaria`) REFERENCES `planillatributaria` (`idplanillatributaria`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillafiscalgenerada` */

CREATE TABLE `planillafiscalgenerada` (
  `idplanillafiscalgenerada` bigint(20) DEFAULT NULL,
  `estadovalidacion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechageneracion` datetime DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idconfplanillafiscal` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillafiscalporcategoria` */

CREATE TABLE `planillafiscalporcategoria` (
  `idplanillafiscalporcategoria` bigint(20) NOT NULL DEFAULT '0',
  `descuentoporminutosausencia` decimal(13,2) DEFAULT NULL,
  `anticipo` decimal(13,2) DEFAULT NULL,
  `haberbasico` decimal(13,2) DEFAULT NULL,
  `fechanacimiento` date DEFAULT NULL,
  `fechaingreso` date DEFAULT NULL,
  `horasextra` decimal(13,2) DEFAULT NULL,
  `costohorasextra` decimal(13,2) DEFAULT NULL,
  `genero` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `horasdiapagado` decimal(13,2) DEFAULT NULL,
  `liquidopagable` decimal(13,2) DEFAULT NULL,
  `prestamo` decimal(13,2) DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nacionalidad` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `novedad` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numero` bigint(20) DEFAULT NULL,
  `ocupacion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `otrosbonos` decimal(13,2) DEFAULT NULL,
  `otrosdescuentos` decimal(13,2) DEFAULT NULL,
  `otrosingresos` decimal(13,2) DEFAULT NULL,
  `otrosdescuentosmovsal` decimal(13,2) DEFAULT NULL,
  `diaspagados` decimal(13,2) DEFAULT NULL,
  `ci` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `ext` varchar(6) DEFAULT NULL,
  `bonoproduccion` decimal(13,2) DEFAULT NULL,
  `retencionafp` decimal(13,2) DEFAULT NULL,
  `afplab_individual` decimal(13,2) DEFAULT NULL,
  `afplab_riesgocomun` decimal(13,2) DEFAULT NULL,
  `afplab_solidario` decimal(13,2) DEFAULT NULL,
  `afplab_comision` decimal(13,2) DEFAULT NULL,
  `afpsolidario` decimal(13,2) DEFAULT NULL,
  `liquidacionretencion` decimal(13,2) DEFAULT NULL,
  `bonoantiguedad` decimal(13,2) DEFAULT NULL,
  `aniosantiguedad` int(11) DEFAULT NULL,
  `bonodominical` decimal(13,2) DEFAULT NULL,
  `bononocturno` decimal(13,2) DEFAULT NULL,
  `bonopasaje` decimal(13,2) DEFAULT NULL,
  `bonorefrigerio` decimal(13,2) DEFAULT NULL,
  `descuentoporminutosatraso` decimal(13,2) DEFAULT NULL,
  `totaldescuentos` decimal(13,2) DEFAULT NULL,
  `totalganado` decimal(13,2) DEFAULT NULL,
  `win` decimal(13,2) DEFAULT NULL,
  `diastrabajados` decimal(13,2) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idplanillatributariaporcat` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillafiscalporcategoria`),
  KEY `idplanillagenerada` (`idplanillagenerada`),
  KEY `idcontratopuesto` (`idcontratopuesto`),
  KEY `idempleado` (`idempleado`),
  KEY `idplanillatributariaporcat` (`idplanillatributariaporcat`),
  CONSTRAINT `planillafiscalporcategoria_ibfk_1` FOREIGN KEY (`idplanillagenerada`) REFERENCES `planillagenerada` (`idplanillagenerada`),
  CONSTRAINT `planillafiscalporcategoria_ibfk_2` FOREIGN KEY (`idcontratopuesto`) REFERENCES `contratopuesto` (`idcontratopuesto`),
  CONSTRAINT `planillafiscalporcategoria_ibfk_3` FOREIGN KEY (`idempleado`) REFERENCES `empleado` (`idempleado`),
  CONSTRAINT `planillafiscalporcategoria_ibfk_4` FOREIGN KEY (`idplanillatributariaporcat`) REFERENCES `planillatributariaporcategoria` (`idplanillatributariaporcat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillagenerada` */

CREATE TABLE `planillagenerada` (
  `idplanillagenerada` bigint(20) NOT NULL DEFAULT '0',
  `tipoplanillagen` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechageneracion` datetime DEFAULT NULL,
  `idgestionplanilla` bigint(20) DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipocambio` bigint(20) DEFAULT NULL,
  `idciclogeneracionplanilla` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillagenerada`),
  KEY `idciclogeneracionplanilla` (`idciclogeneracionplanilla`),
  CONSTRAINT `planillagenerada_ibfk_1` FOREIGN KEY (`idciclogeneracionplanilla`) REFERENCES `ciclogeneracionplanilla` (`idciclogeneracionplanilla`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillageneral` */

CREATE TABLE `planillageneral` (
  `idplanillageneral` bigint(20) DEFAULT NULL,
  `minutosausencia` int(11) DEFAULT NULL,
  `descuentoporminutosausencia` decimal(13,2) DEFAULT NULL,
  `descuentototalporausencias` decimal(13,2) DEFAULT NULL,
  `descuentoausencia` decimal(13,2) DEFAULT NULL,
  `minausencia` int(11) DEFAULT NULL,
  `activogenplanfis` int(11) DEFAULT NULL,
  `afps` decimal(13,2) DEFAULT NULL,
  `area` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `modalidadcontratacion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocontrol` int(11) DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `diferencia` decimal(13,2) DEFAULT NULL,
  `descuentossinretencion` decimal(13,2) DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  `registrocontable` int(11) DEFAULT NULL,
  `pagoactivo` int(11) DEFAULT NULL,
  `ingresofueraiva` decimal(13,2) DEFAULT NULL,
  `seguro` decimal(13,2) DEFAULT NULL,
  `saldoiva` decimal(13,2) DEFAULT NULL,
  `retencioniva` decimal(13,2) DEFAULT NULL,
  `puesto` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipoempleado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `totallaboral` decimal(13,2) DEFAULT NULL,
  `saldoivaanterior` decimal(13,2) DEFAULT NULL,
  `liquidopagable` decimal(13,2) DEFAULT NULL,
  `otrosdescuentos` decimal(13,2) DEFAULT NULL,
  `otrosingresos` decimal(13,2) DEFAULT NULL,
  `totalpatrimonial` decimal(13,2) DEFAULT NULL,
  `provivienda` decimal(13,2) DEFAULT NULL,
  `sueldo` decimal(13,2) DEFAULT NULL,
  `atrasos` decimal(13,2) DEFAULT NULL,
  `totaldescuentos` decimal(13,2) DEFAULT NULL,
  `totalganado` decimal(13,2) DEFAULT NULL,
  `totalperiodosganado` decimal(13,2) DEFAULT NULL,
  `totalperiodostrabajado` decimal(13,2) DEFAULT NULL,
  `unidad` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `diastrabajados` int(11) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcargo` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillapagomateriaprima` */

CREATE TABLE `planillapagomateriaprima` (
  `idplanillapagomateriaprima` bigint(20) NOT NULL,
  `fechafin` date DEFAULT NULL,
  `it` decimal(3,2) DEFAULT NULL,
  `iue` decimal(3,2) DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `estado` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tasaimpuesto` decimal(3,2) DEFAULT NULL,
  `totalajustexgab` decimal(16,2) DEFAULT NULL,
  `totalalcoholxgab` decimal(16,2) DEFAULT NULL,
  `totalpesadoxgab` decimal(16,2) DEFAULT NULL,
  `totalconcentradosxgab` decimal(16,2) DEFAULT NULL,
  `totalcreditoxgab` decimal(16,2) DEFAULT NULL,
  `totadescuentosxgab` decimal(16,2) DEFAULT NULL,
  `totalcomision` decimal(16,2) DEFAULT NULL,
  `totaliquidoxgab` decimal(16,2) DEFAULT NULL,
  `totalmontoacopioadoxgab` decimal(16,2) DEFAULT NULL,
  `totalotrosdecuentosxgab` decimal(16,2) DEFAULT NULL,
  `totalotrosingresosxgab` decimal(16,2) DEFAULT NULL,
  `totaltachosxgab` decimal(16,2) DEFAULT NULL,
  `totaldescuentoreserva` decimal(16,2) DEFAULT NULL,
  `totalga` decimal(16,2) DEFAULT NULL,
  `totalretencionesxgab` decimal(16,2) DEFAULT NULL,
  `totalveterinarioxgab` decimal(16,2) DEFAULT NULL,
  `totalacopiadoxgab` decimal(16,2) DEFAULT NULL,
  `totalyogurdxgab` decimal(16,2) DEFAULT NULL,
  `preciounitario` decimal(9,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
  `idzonaproductiva` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillapagomateriaprima`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillatributaria` */

CREATE TABLE `planillatributaria` (
  `idplanillatributaria` bigint(20) NOT NULL DEFAULT '0',
  `haberbasico` decimal(13,2) DEFAULT NULL,
  `cns` decimal(13,2) DEFAULT NULL,
  `codigo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `saldodependiente` decimal(13,2) DEFAULT NULL,
  `saldodependientemessgute` decimal(13,2) DEFAULT NULL,
  `saldototaldependiente` decimal(13,2) DEFAULT NULL,
  `fechaingreso` date DEFAULT NULL,
  `horasextra` decimal(13,2) DEFAULT NULL,
  `costohorasextra` decimal(13,2) DEFAULT NULL,
  `creditofiscal` decimal(13,2) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `saldoanterioractualizado` decimal(13,2) DEFAULT NULL,
  `saldomesanterior` decimal(13,2) DEFAULT NULL,
  `mantenimientovalor` decimal(13,2) DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `sueldoneto` decimal(13,2) DEFAULT NULL,
  `numero` bigint(20) DEFAULT NULL,
  `otrosbonos` decimal(13,2) DEFAULT NULL,
  `otrosingresos` decimal(13,2) DEFAULT NULL,
  `retpatrproviviendaafp` decimal(13,2) DEFAULT NULL,
  `retpatrriesgoprofafp` decimal(13,2) DEFAULT NULL,
  `retencionpatronalafp` decimal(13,2) DEFAULT NULL,
  `retpatrsolidarioafp` decimal(13,2) DEFAULT NULL,
  `saldofisco` decimal(13,2) DEFAULT NULL,
  `bonoproduccion` decimal(13,2) DEFAULT NULL,
  `retencionafp` decimal(13,2) DEFAULT NULL,
  `liquidacionretencion` decimal(13,2) DEFAULT NULL,
  `sueldonoimpdossmn` decimal(13,2) DEFAULT NULL,
  `bonoantiguedad` decimal(13,2) DEFAULT NULL,
  `aniosantiguedad` int(11) DEFAULT NULL,
  `afpsolidario` decimal(13,2) DEFAULT NULL,
  `bonodominical` decimal(13,2) DEFAULT NULL,
  `impuesto` decimal(13,2) DEFAULT NULL,
  `impsobredossmn` decimal(13,2) DEFAULT NULL,
  `totalganado` decimal(13,2) DEFAULT NULL,
  `totalotrosingresos` decimal(13,2) DEFAULT NULL,
  `difsujetaimpuesto` decimal(13,2) DEFAULT NULL,
  `saldoutilizado` decimal(13,2) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idciclogeneracionplanilla` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillatributaria`),
  KEY `idciclogeneracionplanilla` (`idciclogeneracionplanilla`),
  KEY `idempleado` (`idempleado`),
  CONSTRAINT `planillatributaria_ibfk_1` FOREIGN KEY (`idciclogeneracionplanilla`) REFERENCES `ciclogeneracionplanilla` (`idciclogeneracionplanilla`),
  CONSTRAINT `planillatributaria_ibfk_2` FOREIGN KEY (`idempleado`) REFERENCES `empleado` (`idempleado`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planillatributariaporcategoria` */

CREATE TABLE `planillatributariaporcategoria` (
  `idplanillatributariaporcat` bigint(20) NOT NULL DEFAULT '0',
  `haberbasico` decimal(13,2) DEFAULT NULL,
  `cns` decimal(13,2) DEFAULT NULL,
  `codigo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `saldodependiente` decimal(13,2) DEFAULT NULL,
  `saldodependientemessgute` decimal(13,2) DEFAULT NULL,
  `saldototaldependiente` decimal(13,2) DEFAULT NULL,
  `fechaingreso` date DEFAULT NULL,
  `horasextra` decimal(13,2) DEFAULT NULL,
  `costohorasextra` decimal(13,2) DEFAULT NULL,
  `creditofiscal` decimal(13,2) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `saldoanterioractualizado` decimal(13,2) DEFAULT NULL,
  `saldomesanterior` decimal(13,2) DEFAULT NULL,
  `mantenimientovalor` decimal(13,2) DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `sueldoneto` decimal(13,2) DEFAULT NULL,
  `numero` bigint(20) DEFAULT NULL,
  `otrosbonos` decimal(13,2) DEFAULT NULL,
  `otrosingresos` decimal(13,2) DEFAULT NULL,
  `retpatrproviviendaafp` decimal(13,2) DEFAULT NULL,
  `retpatrriesgoprofafp` decimal(13,2) DEFAULT NULL,
  `retencionpatronalafp` decimal(13,2) DEFAULT NULL,
  `retpatrsolidarioafp` decimal(13,2) DEFAULT NULL,
  `saldofisco` decimal(13,2) DEFAULT NULL,
  `bonoproduccion` decimal(13,2) DEFAULT NULL,
  `retencionafp` decimal(13,2) DEFAULT NULL,
  `afplab_individual` decimal(13,2) DEFAULT NULL,
  `afplab_riesgocomun` decimal(13,2) DEFAULT NULL,
  `afplab_solidario` decimal(13,2) DEFAULT NULL,
  `afplab_comision` decimal(13,2) DEFAULT NULL,
  `liquidacionretencion` decimal(13,2) DEFAULT NULL,
  `sueldonoimpdossmn` decimal(13,2) DEFAULT NULL,
  `bonoantiguedad` decimal(13,2) DEFAULT NULL,
  `aniosantiguedad` int(11) DEFAULT NULL,
  `afpsolidario` decimal(13,2) DEFAULT NULL,
  `bonodominical` decimal(13,2) DEFAULT NULL,
  `bononocturno` decimal(13,2) DEFAULT NULL,
  `bonopasaje` decimal(13,2) DEFAULT NULL,
  `bonorefrigerio` decimal(13,2) DEFAULT NULL,
  `impuesto` decimal(13,2) DEFAULT NULL,
  `impsobredossmn` decimal(13,2) DEFAULT NULL,
  `totalganado` decimal(13,2) DEFAULT NULL,
  `totalotrosingresos` decimal(13,2) DEFAULT NULL,
  `difsujetaimpuesto` decimal(13,2) DEFAULT NULL,
  `saldoutilizado` decimal(13,2) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idplanillatributariaporcat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `plantilla` */

CREATE TABLE `plantilla` (
  `idplantilla` bigint(20) DEFAULT NULL,
  `formato` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idarchivo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `planvacacion` */

CREATE TABLE `planvacacion` (
  `idplanvacacion` bigint(20) DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `diaslibres` int(11) DEFAULT NULL,
  `diasusados` int(11) DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `aniosantiguedad` int(11) DEFAULT NULL,
  `diasvacacion` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcontractopuesto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `politicadescuento` */

CREATE TABLE `politicadescuento` (
  `idpoliticadescuento` bigint(20) DEFAULT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idacreedor` bigint(20) DEFAULT NULL,
  `idtipopoliticadescuento` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `porcentajetributario` */

CREATE TABLE `porcentajetributario` (
  `idporcentajetributario` bigint(20) DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `porcentaje` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipoporcentajetrib` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `postulante` */

CREATE TABLE `postulante` (
  `idpostulante` bigint(20) DEFAULT NULL,
  `fechanacimiento` date DEFAULT NULL,
  `lugarnacimiento` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `celular` int(11) DEFAULT NULL,
  `email` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `lugarextension` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombres` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `genero` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `noidentificacion` int(11) DEFAULT NULL,
  `apellidopaterno` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellidomaterno` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `telefono` int(11) DEFAULT NULL,
  `tipo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecharegistro` date DEFAULT NULL,
  `salariotiempocompleto` decimal(13,2) DEFAULT NULL,
  `salariomediotiempo` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idlibros` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idarticulosinternacional` bigint(20) DEFAULT NULL,
  `idpremiosinternacionales` bigint(20) DEFAULT NULL,
  `idarticulosnacional` bigint(20) DEFAULT NULL,
  `idpremiosnacionales` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `postulanteasigna` */

CREATE TABLE `postulanteasigna` (
  `idpostulante` bigint(20) DEFAULT NULL,
  `idasignatura` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `postulantecargo` */

CREATE TABLE `postulantecargo` (
  `idpostulantecargo` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcargo` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpostulante` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `pr_categoria` */

CREATE TABLE `pr_categoria` (
  `idcategoria` bigint(20) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `VERSION` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idcategoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_formula` */

CREATE TABLE `pr_formula` (
  `idformula` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `estado` varchar(3) DEFAULT NULL,
  `totaleq` decimal(16,2) NOT NULL,
  `capacidad` decimal(16,2) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `idcategoria` bigint(20) DEFAULT NULL,
  `VERSION` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idformula`),
  KEY `idcategoria` (`idcategoria`),
  CONSTRAINT `pr_formula_ibfk_1` FOREIGN KEY (`idcategoria`) REFERENCES `pr_categoria` (`idcategoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_insumo` */

CREATE TABLE `pr_insumo` (
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
  CONSTRAINT `pr_insumo_ibfk_1` FOREIGN KEY (`idproduccion`) REFERENCES `pr_produccion` (`idproduccion`),
  CONSTRAINT `pr_insumo_ibfk_2` FOREIGN KEY (`idinsumoformula`) REFERENCES `pr_insumoformula` (`idinsumoformula`),
  CONSTRAINT `pr_insumo_ibfk_3` FOREIGN KEY (`idproducto`) REFERENCES `pr_producto` (`idproducto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_insumoformula` */

CREATE TABLE `pr_insumoformula` (
  `idinsumoformula` bigint(20) NOT NULL,
  `cantidad` decimal(20,6) NOT NULL,
  `cod_art` varchar(6) NOT NULL,
  `defecto` int(11) DEFAULT NULL,
  `idformula` bigint(20) NOT NULL,
  `idform` bigint(20) DEFAULT NULL,
  `VERSION` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idinsumoformula`),
  KEY `idformula` (`idformula`),
  KEY `idform` (`idform`),
  CONSTRAINT `pr_insumoformula_ibfk_1` FOREIGN KEY (`idformula`) REFERENCES `pr_formula` (`idformula`),
  CONSTRAINT `pr_insumoformula_ibfk_2` FOREIGN KEY (`idform`) REFERENCES `pr_formula` (`idformula`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_material` */

CREATE TABLE `pr_material` (
  `idmaterial` bigint(20) NOT NULL,
  `cod_art` varchar(6) NOT NULL,
  `cod_art_mat` varchar(6) NOT NULL,
  `descri` varchar(100) DEFAULT NULL,
  `flag_cant` int(11) DEFAULT NULL,
  `tipo` varchar(15) DEFAULT NULL,
  `vol1` decimal(10,2) DEFAULT NULL,
  `peso1` decimal(10,2) DEFAULT NULL,
  `VERSION` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idmaterial`),
  KEY `cod_art` (`cod_art`),
  KEY `cod_art_mat` (`cod_art_mat`),
  CONSTRAINT `pr_material_ibfk_1` FOREIGN KEY (`cod_art`) REFERENCES `inv_articulos` (`cod_art`),
  CONSTRAINT `pr_material_ibfk_2` FOREIGN KEY (`cod_art_mat`) REFERENCES `inv_articulos` (`cod_art`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_plan` */

CREATE TABLE `pr_plan` (
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

CREATE TABLE `pr_prodafectado` (
  `idprodafectado` bigint(20) NOT NULL,
  `idinsumo` bigint(20) NOT NULL,
  `idproducto` bigint(20) NOT NULL,
  `VERSION` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idprodafectado`),
  KEY `idinsumo` (`idinsumo`),
  KEY `idproducto` (`idproducto`),
  KEY `idcompania` (`idcompania`),
  CONSTRAINT `pr_prodafectado_ibfk_1` FOREIGN KEY (`idinsumo`) REFERENCES `pr_insumo` (`idinsumo`),
  CONSTRAINT `pr_prodafectado_ibfk_2` FOREIGN KEY (`idproducto`) REFERENCES `pr_producto` (`idproducto`),
  CONSTRAINT `pr_prodafectado_ibfk_3` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_produccion` */

CREATE TABLE `pr_produccion` (
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
  CONSTRAINT `pr_produccion_ibfk_1` FOREIGN KEY (`idformula`) REFERENCES `pr_formula` (`idformula`),
  CONSTRAINT `pr_produccion_ibfk_2` FOREIGN KEY (`idtanque`) REFERENCES `pr_tanque` (`idtanque`),
  CONSTRAINT `pr_produccion_ibfk_3` FOREIGN KEY (`idplan`) REFERENCES `pr_plan` (`idplan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_producto` */

CREATE TABLE `pr_producto` (
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
  CONSTRAINT `pr_producto_ibfk_1` FOREIGN KEY (`idproduccion`) REFERENCES `pr_produccion` (`idproduccion`),
  CONSTRAINT `pr_producto_ibfk_2` FOREIGN KEY (`idproduccion`) REFERENCES `pr_produccion` (`idproduccion`),
  CONSTRAINT `pr_producto_ibfk_3` FOREIGN KEY (`idplan`) REFERENCES `pr_plan` (`idplan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pr_tanque` */

CREATE TABLE `pr_tanque` (
  `idtanque` bigint(20) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `capacidad` decimal(16,2) NOT NULL,
  `codmed` varchar(6) DEFAULT NULL,
  `VERSION` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idtanque`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `precioarticulo` */

CREATE TABLE `precioarticulo` (
  `idprecioarticulo` bigint(20) NOT NULL,
  `cod_art` varchar(6) NOT NULL,
  `precio` decimal(12,2) NOT NULL,
  `idcategoriacliente` bigint(20) NOT NULL,
  `no_cia` varchar(2) NOT NULL,
  `version` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idprecioarticulo`),
  KEY `idcategoriacliente` (`idcategoriacliente`),
  KEY `idcompania` (`idcompania`),
  CONSTRAINT `precioarticulo_ibfk_1` FOREIGN KEY (`idcategoriacliente`) REFERENCES `categoriacliente` (`idcategoriacliente`),
  CONSTRAINT `precioarticulo_ibfk_2` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `pregunta` */

CREATE TABLE `pregunta` (
  `idpregunta` bigint(20) DEFAULT NULL,
  `contenido` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `indice` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcriterioevaluacion` bigint(20) DEFAULT NULL,
  `idseccion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `presupuestogasto` */

CREATE TABLE `presupuestogasto` (
  `idpresupuesto` bigint(20) DEFAULT NULL,
  `importe` decimal(13,2) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `editable` int(11) DEFAULT NULL,
  `estado` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechamodificacion` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idactividad` bigint(20) DEFAULT NULL,
  `iddistpresupuesto` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idclasificador` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuariocreador` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `idusuarioeditor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `presupuestoingreso` */

CREATE TABLE `presupuestoingreso` (
  `idpresupuestoingreso` bigint(20) DEFAULT NULL,
  `importe` decimal(13,2) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `editable` int(11) DEFAULT NULL,
  `estado` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechamodificacion` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `iddistpresupuesto` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idclasificador` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuariocreador` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `idusuarioeditor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `previsionretiro` */

CREATE TABLE `previsionretiro` (
  `idprevisionretiro` bigint(20) DEFAULT NULL,
  `monto` decimal(16,6) DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `moneda` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipocambio` decimal(16,6) DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notrans` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocompania` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idretiro` bigint(20) DEFAULT NULL,
  `idgestionplanilla` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `producto` */

CREATE TABLE `producto` (
  `idproducto` bigint(20) DEFAULT NULL,
  `cuentaactivo` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `preciocompra` decimal(13,2) DEFAULT NULL,
  `codigoproducto` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `cuentapasivo` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombreproducto` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `precioventa` decimal(13,2) DEFAULT NULL,
  `fechaestado` date DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipoproducto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `productobase` */

CREATE TABLE `productobase` (
  `idproductobase` bigint(20) NOT NULL,
  `codigo` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `id_tmpenc` bigint(20) DEFAULT NULL,
  `no_trans` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `no_vale` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `costototalinsumos` decimal(16,6) DEFAULT NULL,
  `unidades` int(11) DEFAULT NULL,
  `volumen` decimal(8,2) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idplanificacionproduccion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idproductobase`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `productoorden` */

CREATE TABLE `productoorden` (
  `idproductoorden` bigint(20) NOT NULL,
  `nombreproducto` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idproductoprocesado` bigint(20) DEFAULT NULL,
  `idordenproduccion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idproductoorden`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `productoprocesado` */

CREATE TABLE `productoprocesado` (
  `cantidad` decimal(7,2) DEFAULT NULL,
  `unidadmedidate` varchar(4) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idproductoprocesado` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  KEY `idproductoprocesado` (`idproductoprocesado`),
  CONSTRAINT `productoprocesado_ibfk_1` FOREIGN KEY (`idproductoprocesado`) REFERENCES `metaproductoproduccion` (`idmetaproductoproduccion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `productoreprocesado` */

CREATE TABLE `productoreprocesado` (
  `idproductoreprocesado` bigint(20) DEFAULT NULL,
  `unidades` int(11) DEFAULT NULL,
  `volumen` decimal(8,2) DEFAULT NULL,
  `idproductobase` bigint(20) DEFAULT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `productormateriaprima` */

CREATE TABLE `productormateriaprima` (
  `idproductormateriaprima` bigint(20) NOT NULL,
  `licenciaimpuestos` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `licenciaimpuestos2011` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechaexpiralicenciaimpuesto` date DEFAULT NULL,
  `fechafinimpuesto2011` date DEFAULT NULL,
  `esresponsable` int(11) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `numerocuenta` varchar(50) DEFAULT NULL,
  `fechainicialicenciaimpuesto` date DEFAULT NULL,
  `fechainiimpuesto2011` date DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idzonaproductiva` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idproductormateriaprima`),
  KEY `idzonaproductiva` (`idzonaproductiva`),
  CONSTRAINT `productormateriaprima_ibfk_1` FOREIGN KEY (`idproductormateriaprima`) REFERENCES `persona` (`idpersona`),
  CONSTRAINT `productormateriaprima_ibfk_2` FOREIGN KEY (`idzonaproductiva`) REFERENCES `zonaproductiva` (`idzonaproductiva`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `productosimple` */

CREATE TABLE `productosimple` (
  `idproductosimple` bigint(20) NOT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `cantidadproducidaresponsable` int(11) DEFAULT NULL,
  `costototalmanoobra` decimal(16,6) DEFAULT NULL,
  `porcentajegrasa` decimal(16,6) DEFAULT NULL,
  `no_trans` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `costototalproduccion` decimal(16,6) DEFAULT NULL,
  `costototalindirecto` decimal(16,6) DEFAULT NULL,
  `costototalinsumos` decimal(16,6) DEFAULT NULL,
  `costototalmateriales` decimal(16,6) DEFAULT NULL,
  `costounitario` decimal(16,6) DEFAULT NULL,
  `idproductobase` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idproductosimple`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `productosimpleprocesado` */

CREATE TABLE `productosimpleprocesado` (
  `idproductosimpleprocesado` bigint(20) NOT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
  `idproductosimple` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idproductosimpleprocesado`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `programa` */

CREATE TABLE `programa` (
  `idprograma` bigint(20) DEFAULT NULL,
  `codigo` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechamodificacion` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuariocreador` bigint(20) DEFAULT NULL,
  `idusuarioeditor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `promocion` */

CREATE TABLE `promocion` (
  `IDPROMOCION` bigint(20) NOT NULL,
  `NOMBRE` varchar(50) DEFAULT NULL,
  `FECHAINICIO` date DEFAULT NULL,
  `FECHAFIN` date DEFAULT NULL,
  `ESTADO` varchar(15) DEFAULT NULL,
  `TOTAL` double DEFAULT NULL,
  `no_cia` varchar(2) DEFAULT NULL,
  `cod_art` varchar(6) DEFAULT NULL,
  `PRECIOVENTA` double NOT NULL,
  PRIMARY KEY (`IDPROMOCION`),
  KEY `fk_promocion_articulos` (`no_cia`,`cod_art`),
  CONSTRAINT `fk_promocion_articulos` FOREIGN KEY (`no_cia`, `cod_art`) REFERENCES `inv_articulos` (`no_cia`, `cod_art`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `puesto` */

CREATE TABLE `puesto` (
  `idpuesto` bigint(20) DEFAULT NULL,
  `idcargo` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL,
  `idunidadorganizacional` bigint(20) DEFAULT NULL,
  `idsueldo` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `puntuacion` */

CREATE TABLE `puntuacion` (
  `idpuntuacion` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idvalorcriterioevaluacion` bigint(20) DEFAULT NULL,
  `idejemplarencuesta` bigint(20) DEFAULT NULL,
  `idpregunta` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `rangopuntua` */

CREATE TABLE `rangopuntua` (
  `idrangopuntua` bigint(20) DEFAULT NULL,
  `finrango` int(11) DEFAULT NULL,
  `interpretacion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `posicion` int(11) DEFAULT NULL,
  `iniciorango` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idformevalfinal` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `rangoregladescuento` */

CREATE TABLE `rangoregladescuento` (
  `idrangoregladescuento` bigint(20) DEFAULT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `rangofinal` int(11) DEFAULT NULL,
  `rangoinicial` int(11) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `secuencia` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idregladescuento` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `rdescuentocliente` */

CREATE TABLE `rdescuentocliente` (
  `iddescuentocliente` bigint(20) DEFAULT NULL,
  `fechaactivacion` datetime DEFAULT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `estadoregladescuento` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notas` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `fechaestado` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpoliticadescuento` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `rdescuentoempleado` */

CREATE TABLE `rdescuentoempleado` (
  `iddescuentoempleado` bigint(20) DEFAULT NULL,
  `fechaactivacion` datetime DEFAULT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `estadoregladescuento` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notas` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `cantidad` int(11) DEFAULT NULL,
  `fechaestado` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpoliticadescuento` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `rdescuentoproducto` */

CREATE TABLE `rdescuentoproducto` (
  `iddescuentoproducto` bigint(20) DEFAULT NULL,
  `fechaactivacion` datetime DEFAULT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `estadoregladescuento` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notas` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `fechaestado` datetime DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idpoliticadescuento` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `reembolso` */

CREATE TABLE `reembolso` (
  `idreembolso` bigint(20) DEFAULT NULL,
  `fecha` datetime DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idtiporeembolso` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `regaporgenplan` */

CREATE TABLE `regaporgenplan` (
  `idregaporgenplan` bigint(20) DEFAULT NULL,
  `monto` decimal(18,2) DEFAULT NULL,
  `nocia` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipodoc` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `notrans` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmoneda` bigint(20) DEFAULT NULL,
  `iddescripcion` bigint(20) DEFAULT NULL,
  `idciclogeneracionplanilla` bigint(20) DEFAULT NULL,
  `identidadbensocial` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `registroacopio` */

CREATE TABLE `registroacopio` (
  `idregistroacopio` bigint(20) NOT NULL,
  `cantidadrecibida` decimal(16,2) DEFAULT NULL,
  `cantidadrechazada` decimal(16,2) DEFAULT NULL,
  `cantidadpesada` decimal(16,2) DEFAULT NULL,
  `idplanillaacopio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idzonaproductiva` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idregistroacopio`),
  KEY `idplanillaacopio` (`idplanillaacopio`),
  KEY `idzonaproductiva` (`idzonaproductiva`),
  CONSTRAINT `registroacopio_ibfk_1` FOREIGN KEY (`idplanillaacopio`) REFERENCES `planillaacopio` (`idplanillaacopio`),
  CONSTRAINT `registroacopio_ibfk_2` FOREIGN KEY (`idzonaproductiva`) REFERENCES `zonaproductiva` (`idzonaproductiva`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `registrocontable` */

CREATE TABLE `registrocontable` (
  `idregistrocontable` bigint(20) DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipodocumentocxp` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `impmonextbanco` decimal(13,2) DEFAULT NULL,
  `impmonextcheque` decimal(13,2) DEFAULT NULL,
  `tipo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `montomaxino` decimal(13,2) DEFAULT NULL,
  `noidentificacion` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `montominimo` decimal(13,2) DEFAULT NULL,
  `mes` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `impmonnacbanco` decimal(13,2) DEFAULT NULL,
  `impmonnaccheque` decimal(13,2) DEFAULT NULL,
  `cuentaxpagar` varchar(31) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigoproveedor` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecharegistro` datetime DEFAULT NULL,
  `notrans` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `iddescripcion` bigint(20) DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `registropagomateriaprima` */

CREATE TABLE `registropagomateriaprima` (
  `idregistropagomateriaprima` bigint(20) NOT NULL,
  `descuentoreserva` decimal(16,2) DEFAULT NULL,
  `ga` decimal(16,2) DEFAULT NULL,
  `totalganado` decimal(16,2) DEFAULT NULL,
  `fechaexpiralicenciaimpuesto` date DEFAULT NULL,
  `liquidopagable` decimal(16,2) DEFAULT NULL,
  `ajustezonaproductiva` decimal(16,2) DEFAULT NULL,
  `fechainicialicenciaimpuesto` date DEFAULT NULL,
  `licenciaimpuestos` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cantidadtotal` decimal(24,2) DEFAULT NULL,
  `totalpagoacopio` decimal(16,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idplanillapagomateriaprima` bigint(20) DEFAULT NULL,
  `iddescuentproductmateriaprima` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idregistropagomateriaprima`),
  KEY `idplanillapagomateriaprima` (`idplanillapagomateriaprima`),
  KEY `iddescuentproductmateriaprima` (`iddescuentproductmateriaprima`),
  CONSTRAINT `registropagomateriaprima_ibfk_1` FOREIGN KEY (`idplanillapagomateriaprima`) REFERENCES `planillapagomateriaprima` (`idplanillapagomateriaprima`),
  CONSTRAINT `registropagomateriaprima_ibfk_2` FOREIGN KEY (`iddescuentproductmateriaprima`) REFERENCES `descuentproductmateriaprima` (`iddescuentproductmateriaprima`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `registrotransferenciaproduct` */

CREATE TABLE `registrotransferenciaproduct` (
  `idregistrotransferenciaproduct` bigint(20) DEFAULT NULL,
  `fechaentrega` date DEFAULT NULL,
  `fecharecepcion` date DEFAULT NULL,
  `estado` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `regladescuento` */

CREATE TABLE `regladescuento` (
  `idregladescuento` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tiporango` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipodescuento` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipounidaddescuento` varchar(30) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipointervalo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmoneda` bigint(20) DEFAULT NULL,
  `idgestion` bigint(20) DEFAULT NULL,
  `idcategoriapuesto` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `reglaretiro` */

CREATE TABLE `reglaretiro` (
  `idreglaretiro` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `monto` decimal(13,2) DEFAULT NULL,
  `tipomonto` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `ocurrencia` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `reglatributaria` */

CREATE TABLE `reglatributaria` (
  `idreglatributaria` bigint(20) DEFAULT NULL,
  `cantidadasignada` int(11) DEFAULT NULL,
  `numerofacturaactual` bigint(20) DEFAULT NULL,
  `fechadosificacion` datetime DEFAULT NULL,
  `fechapedido` datetime DEFAULT NULL,
  `numerofacturafin` bigint(20) DEFAULT NULL,
  `cantidadminima` int(11) DEFAULT NULL,
  `notas` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `numeroorden` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `periododosificacion` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numeroserie` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerofacturainicio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idtipodosificacion` bigint(20) DEFAULT NULL,
  `idporcentajetributario` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `reglavacacion` */

CREATE TABLE `reglavacacion` (
  `idreglavacacion` bigint(20) DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `aniosinicio` int(11) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `aniosfin` int(11) DEFAULT NULL,
  `diasvacacion` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `reltransregctb` */

CREATE TABLE `reltransregctb` (
  `idreltransregctb` bigint(20) DEFAULT NULL,
  `notrans` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idregistrocontable` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `reportecontrol` */

CREATE TABLE `reportecontrol` (
  `idreportecontrol` bigint(20) DEFAULT NULL,
  `faltabanda` int(11) DEFAULT NULL,
  `descuentofaltabanda` decimal(13,2) DEFAULT NULL,
  `tipocontrol` int(11) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `importedescuento` decimal(13,2) DEFAULT NULL,
  `marcfin` time DEFAULT NULL,
  `marcinicio` time DEFAULT NULL,
  `marcaciones` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `mindescuento` int(11) DEFAULT NULL,
  `numerofaltabandas` int(11) DEFAULT NULL,
  `sueldoporbanda` decimal(13,2) DEFAULT NULL,
  `sueldoporfin` decimal(13,2) DEFAULT NULL,
  `importeminutostrabajo` decimal(13,2) DEFAULT NULL,
  `minutostrabajados` int(11) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idplanillagenerada` bigint(20) DEFAULT NULL,
  `idbandahorariac` bigint(20) DEFAULT NULL,
  KEY `idplanillagenerada` (`idplanillagenerada`),
  CONSTRAINT `reportecontrol_ibfk_1` FOREIGN KEY (`idplanillagenerada`) REFERENCES `planillagenerada` (`idplanillagenerada`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `reportemarcacion` */

CREATE TABLE `reportemarcacion` (
  `idreportemarcacion` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idbandahorariacontrato` bigint(20) DEFAULT NULL,
  `idtipomarcacion` bigint(20) DEFAULT NULL,
  `idrhmarcado` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `responsablecomppnl` */

CREATE TABLE `responsablecomppnl` (
  `idresponsablecomppnl` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcontratopuesto` bigint(20) DEFAULT NULL,
  `idcomponentepanel` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `resumenplanificacionacademica` */

CREATE TABLE `resumenplanificacionacademica` (
  `idresumenplanacad` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `sigla` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `unidad_acad_adm` int(11) DEFAULT NULL,
  `cod_asignatura` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `grupo_asignatura` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre_asignatura` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `carrera` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `sede` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codempleado` bigint(20) DEFAULT NULL,
  `nombres` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `gestion` int(11) DEFAULT NULL,
  `tipo_grupo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `documento` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellido_paterno` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `apellido_materno` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cant_estudiantes` int(11) DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `carga_practica` int(11) DEFAULT NULL,
  `carga_horaria` int(11) DEFAULT NULL,
  `semestre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `plan_estudio` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `carga_teorica` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `retiro` */

CREATE TABLE `retiro` (
  `idretiro` bigint(20) DEFAULT NULL,
  `monto` decimal(16,6) DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fecharetiro` date DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `diastrabajados` int(11) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcontrato` bigint(20) DEFAULT NULL,
  `idarchivo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `revisionentidad` */

CREATE TABLE `revisionentidad` (
  `idreventidad` bigint(20) DEFAULT NULL,
  `nrorevision` bigint(20) DEFAULT NULL,
  `almacenadoen` datetime DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `almacenadopor` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `rh_marcado` */

CREATE TABLE `rh_marcado` (
  `idrhmarcado` bigint(20) NOT NULL AUTO_INCREMENT,
  `control` int(11) DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `marfecha` date DEFAULT NULL,
  `marippc` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `marperid` int(11) DEFAULT NULL,
  `marhora` time DEFAULT NULL,
  PRIMARY KEY (`idrhmarcado`)
) ENGINE=InnoDB AUTO_INCREMENT=126496 DEFAULT CHARSET=utf8;

/*Table structure for table `rhmarcado` */

CREATE TABLE `rhmarcado` (
  `idrhmarcado` bigint(20) NOT NULL,
  `control` int(11) DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `marfecha` date DEFAULT NULL,
  `marippc` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `marperid` int(11) DEFAULT NULL,
  `marreftarjeta` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `marhora` time DEFAULT NULL,
  `sede` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idrhmarcado`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `rol` */

CREATE TABLE `rol` (
  `idrol` bigint(20) DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `rubros` */

CREATE TABLE `rubros` (
  `cod` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descr` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `saludo` */

CREATE TABLE `saludo` (
  `idsaludo` bigint(20) NOT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idsaludo`),
  KEY `fk_companiasaludo` (`idcompania`),
  CONSTRAINT `fk_companiasaludo` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `seccion` */

CREATE TABLE `seccion` (
  `idseccion` bigint(20) DEFAULT NULL,
  `indice` int(11) DEFAULT NULL,
  `titulo` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idformularioencuesta` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `sector` */

CREATE TABLE `sector` (
  `idsector` bigint(20) NOT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idsector`),
  KEY `fk_companiasector` (`idcompania`),
  CONSTRAINT `fk_companiasector` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `secuencia` */

CREATE TABLE `secuencia` (
  `tabla` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `valor` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `sede` */

CREATE TABLE `sede` (
  `idsede` bigint(20) DEFAULT NULL,
  `codigo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idreferencia` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `sesionacopio` */

CREATE TABLE `sesionacopio` (
  `idsesionacopio` bigint(20) NOT NULL,
  `fecha` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
  `idzonaproductiva` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idsesionacopio`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `sf_confdet` */

CREATE TABLE `sf_confdet` (
  `id_sf_confdet` bigint(20) NOT NULL,
  `cuenta` varchar(31) DEFAULT NULL,
  `tipomovimiento` varchar(20) DEFAULT NULL,
  `id_sf_confenc` bigint(20) NOT NULL,
  PRIMARY KEY (`id_sf_confdet`),
  KEY `sf_confdet_sf_confenc_FK` (`id_sf_confenc`),
  CONSTRAINT `sf_confdet_sf_confenc_FK` FOREIGN KEY (`id_sf_confenc`) REFERENCES `sf_confenc` (`id_sf_confenc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sf_confenc` */

CREATE TABLE `sf_confenc` (
  `id_sf_confenc` bigint(20) NOT NULL,
  `cuenta` varchar(31) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL,
  `tipo_doc` varchar(10) DEFAULT NULL,
  `glosa` varchar(150) DEFAULT NULL,
  `operacion` varchar(30) DEFAULT NULL,
  `idusuario` bigint(20) NOT NULL,
  PRIMARY KEY (`id_sf_confenc`),
  UNIQUE KEY `unique_OPERACION` (`operacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sf_entidades` */

CREATE TABLE `sf_entidades` (
  `cod_enti` bigint(20) NOT NULL,
  `razon_social` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `motivo_bloqueo` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fax` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `ci` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `direccion` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nit` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `obs` varchar(240) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `telefono` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `lugar` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `casilla` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `responsable` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `direccion1` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`cod_enti`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sf_modulos` */

CREATE TABLE `sf_modulos` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `modulo` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `mes_proce` date DEFAULT NULL,
  `descri` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_doc_ext` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sf_tmpdet` */

CREATE TABLE `sf_tmpdet` (
  `id_tmpdet` bigint(20) NOT NULL,
  `timemillis` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta` varchar(31) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_uni` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_cc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `debe` decimal(16,2) DEFAULT NULL,
  `haber` decimal(16,2) DEFAULT NULL,
  `debeme` decimal(16,2) DEFAULT NULL,
  `haberme` decimal(16,2) DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tc` decimal(10,6) DEFAULT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `id_tmpenc` bigint(20) NOT NULL,
  `idmovimiento` bigint(20) DEFAULT NULL,
  `glosa` varchar(1000) DEFAULT NULL,
  `idpersonacliente` bigint(20) DEFAULT NULL,
  `cod_prov` varchar(6) DEFAULT NULL,
  `cod_art` varchar(6) DEFAULT NULL,
  `cant_art` decimal(16,2) DEFAULT NULL,
  `idsocio` bigint(20) DEFAULT NULL,
  `idcuenta` bigint(20) DEFAULT NULL,
  `idcredito` bigint(20) DEFAULT NULL,
  `iddocumentocompra` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_tmpdet`),
  KEY `FK_TMPENC` (`id_tmpenc`),
  KEY `idpersonacliente` (`idpersonacliente`),
  KEY `idsocio` (`idsocio`),
  KEY `idcuenta` (`idcuenta`),
  KEY `idcredito` (`idcredito`),
  KEY `idmovimiento` (`idmovimiento`),
  KEY `iddocumentocompra` (`iddocumentocompra`),
  CONSTRAINT `FK_TMPENC` FOREIGN KEY (`id_tmpenc`) REFERENCES `sf_tmpenc` (`id_tmpenc`),
  CONSTRAINT `sf_tmpdet_ibfk_1` FOREIGN KEY (`idpersonacliente`) REFERENCES `personacliente` (`IDPERSONACLIENTE`),
  CONSTRAINT `sf_tmpdet_ibfk_2` FOREIGN KEY (`idsocio`) REFERENCES `socio` (`idsocio`),
  CONSTRAINT `sf_tmpdet_ibfk_3` FOREIGN KEY (`idcuenta`) REFERENCES `cuenta` (`idcuenta`),
  CONSTRAINT `sf_tmpdet_ibfk_4` FOREIGN KEY (`idcredito`) REFERENCES `credito` (`idcredito`),
  CONSTRAINT `sf_tmpdet_ibfk_5` FOREIGN KEY (`idmovimiento`) REFERENCES `movimiento` (`IDMOVIMIENTO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sf_tmpenc` */

CREATE TABLE `sf_tmpenc` (
  `id_tmpenc` bigint(20) NOT NULL,
  `no_trans` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `monto` decimal(16,2) DEFAULT NULL,
  `cta_bco` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cuenta` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_cia` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `idmovimiento` bigint(20) DEFAULT NULL,
  `fac` int(11) DEFAULT NULL,
  `no_doc` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tipo_doc` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `estado` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `glosa` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descri` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `beneficiario` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `tc` decimal(10,6) DEFAULT NULL,
  `fecha_ven` date DEFAULT NULL,
  `observacion` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sede_pago_chq` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `formulario` varchar(30) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `pendiente_registro` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `agregar_cta_prov` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_prov` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `entregado_a` varchar(660) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_trans_rel` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `procedencia` varchar(3) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `no_usr` varchar(4) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_enti` bigint(20) DEFAULT NULL,
  `NOMBRECLIENTE` varchar(100) DEFAULT NULL,
  `DEBE` double DEFAULT NULL,
  `HABER` double DEFAULT NULL,
  `IDPERSONACLIENTE` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL,
  `ndoc` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id_tmpenc`),
  UNIQUE KEY `id_tmpenc_UNIQUE` (`id_tmpenc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sin_actividad` */

CREATE TABLE `sin_actividad` (
  `idactividad` bigint(20) NOT NULL,
  `codigocaeb` varchar(50) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL,
  `tipoactividad` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sin_eventosignificativo` */

CREATE TABLE `sin_eventosignificativo` (
  `codigo` int(11) NOT NULL DEFAULT '0',
  `descripcion` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sin_metodopago` */

CREATE TABLE `sin_metodopago` (
  `idmetodopago` bigint(20) NOT NULL,
  `codigo` int(11) DEFAULT NULL,
  `descripcion` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`idmetodopago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sin_motivoanulacion` */

CREATE TABLE `sin_motivoanulacion` (
  `idmotivoanulacion` bigint(20) NOT NULL,
  `codigo` int(11) DEFAULT NULL,
  `descripcion` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`idmotivoanulacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sin_produtoservicio` */

CREATE TABLE `sin_produtoservicio` (
  `idprodutoservicio` bigint(20) NOT NULL,
  `codactividad` varchar(20) DEFAULT NULL,
  `codproducto` int(11) DEFAULT NULL,
  `descproducto` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`idprodutoservicio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sin_unidadmedida` */

CREATE TABLE `sin_unidadmedida` (
  `idunidadmedida` bigint(20) NOT NULL,
  `codigo` int(11) DEFAULT NULL,
  `descripcion` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`idunidadmedida`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `socio` */

CREATE TABLE `socio` (
  `idsocio` bigint(20) NOT NULL,
  `idpersona` bigint(20) DEFAULT NULL,
  `noidentificacion` varchar(100) DEFAULT NULL,
  `nit` varchar(100) DEFAULT NULL,
  `idexttipodocumento` bigint(20) DEFAULT NULL,
  `idtipodocumento` bigint(20) DEFAULT NULL,
  `apellidopaterno` varchar(200) DEFAULT NULL,
  `apellidomaterno` varchar(200) DEFAULT NULL,
  `nombres` varchar(255) DEFAULT NULL,
  `fechanacimiento` date DEFAULT NULL,
  `profesion` varchar(100) DEFAULT NULL,
  `idsaludo` bigint(20) DEFAULT NULL,
  `genero` varchar(100) DEFAULT NULL,
  `idestadocivil` bigint(20) DEFAULT NULL,
  `domicilio` varchar(500) DEFAULT NULL,
  `nosocio` varchar(100) NOT NULL,
  `nsocio` varchar(10) DEFAULT NULL,
  `estado` varchar(3) DEFAULT NULL,
  `nocred` int(11) NOT NULL,
  `nrohijos` int(11) DEFAULT NULL,
  `contacto` varchar(100) DEFAULT NULL,
  `telcontacto` varchar(20) DEFAULT NULL,
  `telfijo` varchar(20) DEFAULT NULL,
  `telcelular` varchar(20) DEFAULT NULL,
  `conyuge` varchar(100) DEFAULT NULL,
  `idzonaproductiva` bigint(20) DEFAULT NULL,
  `iddepartamento` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idsocio`),
  KEY `idcompania` (`idcompania`),
  KEY `idzonaproductiva` (`idzonaproductiva`),
  KEY `iddepartamento` (`iddepartamento`),
  KEY `idexttipodocumento` (`idexttipodocumento`),
  KEY `idtipodocumento` (`idtipodocumento`),
  KEY `idsaludo` (`idsaludo`),
  KEY `idestadocivil` (`idestadocivil`),
  KEY `idpersona` (`idpersona`),
  CONSTRAINT `socio_ibfk_1` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `socio_ibfk_3` FOREIGN KEY (`idzonaproductiva`) REFERENCES `zonaproductiva` (`idzonaproductiva`),
  CONSTRAINT `socio_ibfk_4` FOREIGN KEY (`iddepartamento`) REFERENCES `departamento` (`iddepartamento`),
  CONSTRAINT `socio_ibfk_5` FOREIGN KEY (`idexttipodocumento`) REFERENCES `exttipodocumento` (`idexttipodocumento`),
  CONSTRAINT `socio_ibfk_6` FOREIGN KEY (`idtipodocumento`) REFERENCES `tipodocumento` (`idtipodocumento`),
  CONSTRAINT `socio_ibfk_7` FOREIGN KEY (`idsaludo`) REFERENCES `saludo` (`idsaludo`),
  CONSTRAINT `socio_ibfk_8` FOREIGN KEY (`idestadocivil`) REFERENCES `estadocivil` (`idestadocivil`),
  CONSTRAINT `socio_ibfk_9` FOREIGN KEY (`idpersona`) REFERENCES `persona` (`idpersona`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `solicitudmantenimiento` */

CREATE TABLE `solicitudmantenimiento` (
  `idsolmant` bigint(20) DEFAULT NULL,
  `codigo` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechasolmant` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerocompania` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idunidadejecutora` bigint(20) DEFAULT NULL,
  `idmantenimiento` bigint(20) DEFAULT NULL,
  `idmotivosolicitud` bigint(20) DEFAULT NULL,
  `idsolicitante` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `solmantactivofijo` */

CREATE TABLE `solmantactivofijo` (
  `idsolmant` bigint(20) DEFAULT NULL,
  `idactivo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `sucursal` */

CREATE TABLE `sucursal` (
  `IDSUCURSAL` bigint(20) NOT NULL,
  `NOMBRE` varchar(150) DEFAULT NULL,
  `descripcion` varchar(30) DEFAULT NULL,
  `codsuc` int(11) DEFAULT NULL,
  `codpos` int(11) DEFAULT NULL,
  `docsector` int(11) DEFAULT NULL,
  `ACTIVIDAD` varchar(150) DEFAULT NULL,
  `NOMBRE_EMPRESA` varchar(150) DEFAULT NULL,
  `NOMBRE_SUCURSAL` varchar(150) DEFAULT NULL,
  `NOMBRE_POS` varchar(150) DEFAULT NULL,
  `TELEFONOS` varchar(150) DEFAULT NULL,
  `LUGAR` varchar(150) DEFAULT NULL,
  `DIRECCION` varchar(150) DEFAULT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`IDSUCURSAL`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sueldo` */

CREATE TABLE `sueldo` (
  `idsueldo` bigint(20) DEFAULT NULL,
  `cantidad` decimal(15,2) DEFAULT NULL,
  `haberbasico` decimal(15,2) DEFAULT NULL,
  `descripcion` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmoneda` bigint(20) DEFAULT NULL,
  `idtiposueldo` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tarjetatiempoempleado` */

CREATE TABLE `tarjetatiempoempleado` (
  `idtarjetatiempoempleado` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `costoporhora` decimal(16,2) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `finjornada` datetime DEFAULT NULL,
  `horafin` datetime DEFAULT NULL,
  `cod_gru` varchar(3) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `horainicio` datetime DEFAULT NULL,
  `cod_sub` varchar(3) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idempleado` bigint(20) DEFAULT NULL,
  `idtipotareaprod` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tasaafp` */

CREATE TABLE `tasaafp` (
  `idtasaafp` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `tipotasaafp` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tasa` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tasacns` */

CREATE TABLE `tasacns` (
  `idtasacns` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `tasa` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tasaiva` */

CREATE TABLE `tasaiva` (
  `idtasaiva` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `tasa` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tasasmn` */

CREATE TABLE `tasasmn` (
  `idtasasmn` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `tasa` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `territoriotrabajo` */

CREATE TABLE `territoriotrabajo` (
  `IDTERRITORIOTRABAJO` bigint(20) NOT NULL,
  `NOMBRE` varchar(100) DEFAULT NULL,
  `PAIS` varchar(50) DEFAULT NULL,
  `DESCRIPCION` varchar(50) DEFAULT NULL,
  `IDDISTRIBUIDOR` bigint(20) DEFAULT NULL,
  `IDDEPARTAMENTO` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`IDTERRITORIOTRABAJO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `texto` */

CREATE TABLE `texto` (
  `idtexto` bigint(20) DEFAULT NULL,
  `valor` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipobandahoraria` */

CREATE TABLE `tipobandahoraria` (
  `idtipobandahoraria` bigint(20) DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipocaja` */

CREATE TABLE `tipocaja` (
  `idtipocaja` bigint(20) NOT NULL DEFAULT '0',
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentacaja` varchar(20) DEFAULT NULL,
  `cuentaingreso` varchar(20) DEFAULT NULL,
  `cuentaxcobrar` varchar(20) DEFAULT NULL,
  `cuentacontingencia` varchar(20) DEFAULT NULL,
  `no_cia` varchar(2) DEFAULT NULL,
  `idcategoriacliente` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtipocaja`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipocambio` */

CREATE TABLE `tipocambio` (
  `idtipocambio` bigint(20) DEFAULT NULL,
  `fecha` datetime DEFAULT NULL,
  `compra` decimal(13,2) DEFAULT NULL,
  `tasa` decimal(13,2) DEFAULT NULL,
  `venta` decimal(13,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipociclo` */

CREATE TABLE `tipociclo` (
  `idtipociclo` bigint(20) NOT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `periodo` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idsector` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtipociclo`),
  KEY `fk_companiatipociclo` (`idcompania`),
  KEY `fk_sectortipociclo` (`idsector`),
  CONSTRAINT `fk_companiatipociclo` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_sectortipociclo` FOREIGN KEY (`idsector`) REFERENCES `sector` (`idsector`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipocliente` */

CREATE TABLE `tipocliente` (
  `IDTIPOCLIENTE` bigint(20) NOT NULL,
  `NOMBRE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`IDTIPOCLIENTE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tipocredito` */

CREATE TABLE `tipocredito` (
  `idtipocredito` bigint(20) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `moneda` varchar(20) DEFAULT NULL,
  `ctavig` varchar(20) DEFAULT NULL,
  `ctaven` varchar(20) DEFAULT NULL,
  `ctaeje` varchar(20) DEFAULT NULL,
  `ictavig` varchar(20) DEFAULT NULL,
  `ictaven` varchar(20) DEFAULT NULL,
  `ictaeje` varchar(20) DEFAULT NULL,
  `ipctavig` varchar(20) DEFAULT NULL,
  `ipctaven` varchar(20) DEFAULT NULL,
  `ipctaeje` varchar(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idmoneda` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtipocredito`),
  KEY `idmoneda` (`idmoneda`),
  CONSTRAINT `tipocredito_ibfk_1` FOREIGN KEY (`idmoneda`) REFERENCES `moneda` (`idmoneda`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tipocuenta` */

CREATE TABLE `tipocuenta` (
  `idtipocuenta` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `posicion` int(11) DEFAULT NULL,
  `dias` int(11) DEFAULT NULL,
  `inta` decimal(4,2) DEFAULT NULL,
  `intb` decimal(4,2) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `tipo` varchar(10) DEFAULT NULL,
  `ctap_mn` varchar(20) NOT NULL,
  `ctap_me` varchar(20) DEFAULT NULL,
  `ctap_mv` varchar(20) DEFAULT NULL,
  `ctacf_mn` varchar(20) DEFAULT NULL,
  `ctacf_me` varchar(20) DEFAULT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtipocuenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tipodoc` */

CREATE TABLE `tipodoc` (
  `IDTIPODOC` bigint(20) NOT NULL DEFAULT '0',
  `NOMBRE` varchar(5) NOT NULL,
  `DESCRIPCION` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`IDTIPODOC`),
  UNIQUE KEY `NOMBRE_UNIQUE` (`NOMBRE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tipodocfondorota` */

CREATE TABLE `tipodocfondorota` (
  `idtipodocfondorota` bigint(20) DEFAULT NULL,
  `activo` int(11) DEFAULT NULL,
  `cuentactbajme` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentactbajmn` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codigo` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` varchar(1000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `restriccioncampo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentactbme` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cuentactbmn` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `columnaplanilla` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipofondorotatorio` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `no_usr` varchar(4) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipodocumento` */

CREATE TABLE `tipodocumento` (
  `idtipodocumento` bigint(20) NOT NULL,
  `aplicablea` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `codsin` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtipodocumento`),
  KEY `fk_companiatipodocumento` (`idcompania`),
  CONSTRAINT `fk_companiatipodocumento` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipodosificacion` */

CREATE TABLE `tipodosificacion` (
  `idtipodosificacion` bigint(20) DEFAULT NULL,
  `nombretipo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipoestadocaja` */

CREATE TABLE `tipoestadocaja` (
  `idtipoestadocaja` bigint(20) DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipoestadocontabilidad` */

CREATE TABLE `tipoestadocontabilidad` (
  `idtipoestadocontabilidad` bigint(20) DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipomarcacion` */

CREATE TABLE `tipomarcacion` (
  `idtipomarcacion` bigint(20) DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipomovimientogab` */

CREATE TABLE `tipomovimientogab` (
  `idtipomovimientogab` bigint(20) NOT NULL,
  `moneda` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipomovimiento` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtipomovimientogab`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipomovimientoproductor` */

CREATE TABLE `tipomovimientoproductor` (
  `idtipomovimientoproductor` bigint(20) DEFAULT NULL,
  `moneda` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(100) DEFAULT NULL,
  `tipomovimiento` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipomovsueldo` */

CREATE TABLE `tipomovsueldo` (
  `idtipomovsueldo` bigint(20) DEFAULT NULL,
  `pordefecto` int(11) DEFAULT NULL,
  `cuentactb` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipopedido` */

CREATE TABLE `tipopedido` (
  `IDTIPOPEDIDO` bigint(20) NOT NULL,
  `NOMBRE` varchar(255) DEFAULT NULL,
  `tipo` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`IDTIPOPEDIDO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tipoperiodopreasiento` */

CREATE TABLE `tipoperiodopreasiento` (
  `idtiporeglapreasiento` bigint(20) DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipopoliticadescuento` */

CREATE TABLE `tipopoliticadescuento` (
  `idtipopoliticadescuento` bigint(20) DEFAULT NULL,
  `medicion` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `recurso` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `destino` varchar(15) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipoporcentajetributario` */

CREATE TABLE `tipoporcentajetributario` (
  `idtipoporcentajetrib` bigint(20) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipoproducto` */

CREATE TABLE `tipoproducto` (
  `idtipoproducto` bigint(20) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tiporeembolso` */

CREATE TABLE `tiporeembolso` (
  `idtiporeembolso` bigint(20) DEFAULT NULL,
  `detalle` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `cantidad` int(11) DEFAULT NULL,
  `preciototal` decimal(13,2) DEFAULT NULL,
  `preciounitario` decimal(13,2) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipos_documento_ingreso` */

CREATE TABLE `tipos_documento_ingreso` (
  `tipo_documento_ingreso` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `sigla` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descripcion` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `obligado` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tiposueldo` */

CREATE TABLE `tiposueldo` (
  `idtiposueldo` bigint(20) NOT NULL,
  `descripcion` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `tipo` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idsector` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtiposueldo`),
  KEY `fk_companiatiposueldo` (`idcompania`),
  KEY `fk_sectortiposueldo` (`idsector`),
  CONSTRAINT `fk_companiatiposueldo` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_sectortiposueldo` FOREIGN KEY (`idsector`) REFERENCES `sector` (`idsector`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipotareaprod` */

CREATE TABLE `tipotareaprod` (
  `idtipotareaprod` bigint(20) DEFAULT NULL,
  `no_cia` varchar(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cod_gru` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipounidadnegocio` */

CREATE TABLE `tipounidadnegocio` (
  `idtipounidadnegocio` bigint(20) NOT NULL,
  `principal` int(11) DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtipounidadnegocio`),
  KEY `fk_companiatipounidadnegocio` (`idcompania`),
  CONSTRAINT `fk_companiatipounidadnegocio` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tipoventa` */

CREATE TABLE `tipoventa` (
  `idtipoventa` bigint(20) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `tipo` varchar(100) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtipoventa`),
  KEY `idcompania` (`idcompania`),
  CONSTRAINT `tipoventa_ibfk_1` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `titulo` */

CREATE TABLE `titulo` (
  `idtitulo` bigint(20) DEFAULT NULL,
  `nombre` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `tolerancia` */

CREATE TABLE `tolerancia` (
  `idtolerancia` bigint(20) NOT NULL,
  `despuesfin` int(11) DEFAULT NULL,
  `despuesinicio` int(11) DEFAULT NULL,
  `antesfin` int(11) DEFAULT NULL,
  `antesinicio` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtolerancia`),
  KEY `fk_companiatolerancia` (`idcompania`),
  CONSTRAINT `fk_companiatolerancia` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `transaccioncaja` */

CREATE TABLE `transaccioncaja` (
  `idtransaccion` bigint(20) DEFAULT NULL,
  `fechacierre` datetime DEFAULT NULL,
  `importefalla` decimal(13,2) DEFAULT NULL,
  `descripcionfalla` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `importemodificado` decimal(13,2) DEFAULT NULL,
  `fechaapertura` datetime DEFAULT NULL,
  `importetotal` decimal(13,2) DEFAULT NULL,
  `idcaja` bigint(20) DEFAULT NULL,
  `idusuariocaja` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuariocontrol` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `transaccioncredito` */

CREATE TABLE `transaccioncredito` (
  `idtransaccioncredito` bigint(20) NOT NULL DEFAULT '0',
  `capital` decimal(13,2) NOT NULL,
  `interes` decimal(13,2) NOT NULL,
  `cuotadif` decimal(13,2) DEFAULT '0.00',
  `interespenal` decimal(13,2) DEFAULT NULL,
  `importe` decimal(13,2) DEFAULT NULL,
  `diff` decimal(13,2) DEFAULT NULL,
  `glosa` varchar(255) DEFAULT NULL,
  `fechatransaccion` date NOT NULL,
  `fechacreacion` datetime NOT NULL,
  `dias` int(11) NOT NULL,
  `saldo` decimal(13,2) NOT NULL,
  `tipo` varchar(3) DEFAULT NULL,
  `traspaso` int(11) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcredito` bigint(20) NOT NULL,
  `id_tmpenc` bigint(20) DEFAULT NULL,
  `VERSION` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idtransaccioncredito`),
  KEY `idcredito` (`idcredito`),
  CONSTRAINT `transaccioncredito_ibfk_1` FOREIGN KEY (`idcredito`) REFERENCES `credito` (`idcredito`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `transaccioncuenta` */

CREATE TABLE `transaccioncuenta` (
  `idtransaccioncuenta` bigint(20) NOT NULL,
  `importe` decimal(13,2) NOT NULL,
  `glosa` varchar(255) NOT NULL,
  `fechatransaccion` datetime NOT NULL,
  `tipotrans` varchar(32) NOT NULL,
  `idcuenta` bigint(20) NOT NULL,
  `VERSION` bigint(20) NOT NULL,
  `idcompania` bigint(20) NOT NULL,
  PRIMARY KEY (`idtransaccioncuenta`),
  KEY `idcuenta` (`idcuenta`),
  KEY `idcompania` (`idcompania`),
  CONSTRAINT `transaccioncuenta_ibfk_1` FOREIGN KEY (`idcuenta`) REFERENCES `cuenta` (`idcuenta`),
  CONSTRAINT `transaccioncuenta_ibfk_2` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `unidades` */

CREATE TABLE `unidades` (
  `unidad` int(11) DEFAULT NULL,
  `sigla` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activa` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descripcion` varchar(60) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `unidad_acad_adm` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `unidades_acad_adm` */

CREATE TABLE `unidades_acad_adm` (
  `unidad_acad_adm` int(11) DEFAULT NULL,
  `sigla` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `activa` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `universidad` int(11) DEFAULT NULL,
  `descripcion` varchar(60) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `posicion` int(11) DEFAULT NULL,
  `tipo_unidad_acad_adm` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `unidadmedida` */

CREATE TABLE `unidadmedida` (
  `no_cia` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `cod_med` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descripcion` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombre` varchar(150) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `unidadmedidaproduccion` */

CREATE TABLE `unidadmedidaproduccion` (
  `idunidadmedidaproduccion` bigint(20) DEFAULT NULL,
  `descripcion` varchar(500) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `unidadnegocio` */

CREATE TABLE `unidadnegocio` (
  `idunidadnegocio` bigint(20) NOT NULL,
  `sigla` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `direccion` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nomatricomercial` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `descripcion` longtext CHARACTER SET latin1 COLLATE latin1_bin,
  `codunidadejecutora` varchar(10) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `posicion` int(11) DEFAULT NULL,
  `publicidad` varchar(80) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cantidaddepartamentos` int(11) DEFAULT NULL,
  `cantidadempleados` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idtipounidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idresponsablerh` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idunidadnegocio`),
  KEY `fk_companiaunidadnegocio` (`idcompania`),
  KEY `fk_empleadounidadnegocio` (`idresponsablerh`),
  KEY `fk_tipounidadnegociounidadneg` (`idtipounidadnegocio`),
  CONSTRAINT `fk_companiaunidadnegocio` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_empleadounidadnegocio` FOREIGN KEY (`idresponsablerh`) REFERENCES `empleado` (`idempleado`),
  CONSTRAINT `fk_tipounidadnegociounidadneg` FOREIGN KEY (`idtipounidadnegocio`) REFERENCES `tipounidadnegocio` (`idtipounidadnegocio`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `unidadorganizacional` */

CREATE TABLE `unidadorganizacional` (
  `idunidadorganizacional` bigint(20) NOT NULL,
  `sigla` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `planestudio` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `numerocompania` varchar(2) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `codigocencos` varchar(6) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `descripcion` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idnivelorganizacional` bigint(20) DEFAULT NULL,
  `unidadorganizacionalraiz` bigint(20) DEFAULT NULL,
  `idsector` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idunidadorganizacional`),
  KEY `fk_companiaunidadorg` (`idcompania`),
  KEY `fk_nivelorganizacionaluo` (`idnivelorganizacional`),
  KEY `fk_sectorunidadorg` (`idsector`),
  KEY `fk_unidadorganizacionaluo` (`unidadorganizacionalraiz`),
  CONSTRAINT `fk_companiaunidadorg` FOREIGN KEY (`idcompania`) REFERENCES `compania` (`idcompania`),
  CONSTRAINT `fk_nivelorganizacionaluo` FOREIGN KEY (`idnivelorganizacional`) REFERENCES `nivelorganizacional` (`idnivelorganizacional`),
  CONSTRAINT `fk_sectorunidadorg` FOREIGN KEY (`idsector`) REFERENCES `sector` (`idsector`),
  CONSTRAINT `fk_unidadorganizacionaluo` FOREIGN KEY (`unidadorganizacionalraiz`) REFERENCES `unidadorganizacional` (`idunidadorganizacional`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `usuario` */

CREATE TABLE `usuario` (
  `idusuario` bigint(20) NOT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `email` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numerousuario` varchar(4) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `usuariofinanzas` int(11) DEFAULT NULL,
  `clave` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `usuario` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `IDSUCURSAL` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idusuario`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `usuariorol` */

CREATE TABLE `usuariorol` (
  `idrol` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `usuarios` */

CREATE TABLE `usuarios` (
  `no_usr` varchar(4) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(50) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `orauser` varchar(25) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `usuariounidadnegocio` */

CREATE TABLE `usuariounidadnegocio` (
  `idusuariounidadnegocio` bigint(20) DEFAULT NULL,
  `idunidadnegocio` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuario` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `vacacion` */

CREATE TABLE `vacacion` (
  `idvacacion` bigint(20) DEFAULT NULL,
  `fechacreacion` datetime DEFAULT NULL,
  `diaslibres` int(11) DEFAULT NULL,
  `descripcion` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `fechafin` date DEFAULT NULL,
  `fechainicio` date DEFAULT NULL,
  `estado` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `totaldias` int(11) DEFAULT NULL,
  `fechamodificacion` datetime DEFAULT NULL,
  `usarparagenplan` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idusuariocreador` bigint(20) DEFAULT NULL,
  `idusuarioeditor` bigint(20) DEFAULT NULL,
  `idgestionvacacion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `valeinsumosrequeridos` */

CREATE TABLE `valeinsumosrequeridos` (
  `idvaleinsumosrequeridos` bigint(20) DEFAULT NULL,
  `cantidad` decimal(24,0) DEFAULT NULL,
  `preciocostototal` decimal(16,2) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idmetaproductoproduccion` bigint(20) DEFAULT NULL,
  `idordenproduccion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `valeproductoterminado` */

CREATE TABLE `valeproductoterminado` (
  `idvaleproductoterminado` bigint(20) DEFAULT NULL,
  `observaciones` varchar(1500) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `cantidadproducida` decimal(24,0) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `identradaordenproduccion` bigint(20) DEFAULT NULL,
  `idproductoprocesado` bigint(20) DEFAULT NULL,
  `idordenproduccion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `valorcriterioevaluacion` */

CREATE TABLE `valorcriterioevaluacion` (
  `idvalorcriterioevaluacion` bigint(20) DEFAULT NULL,
  `indice` int(11) DEFAULT NULL,
  `titulo` varchar(250) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `valor` int(11) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  `idcriterioevaluacion` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ventaarticulo` */

CREATE TABLE `ventaarticulo` (
  `IDVENTAARTICULO` bigint(20) NOT NULL AUTO_INCREMENT,
  `PRECIO` double DEFAULT NULL,
  `no_cia` varchar(2) DEFAULT NULL,
  `cod_art` varchar(6) DEFAULT NULL,
  `IDPROMOCION` bigint(20) DEFAULT NULL,
  `TIPO` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`IDVENTAARTICULO`),
  KEY `FK_ventaarticulo_promocion_idx` (`IDPROMOCION`),
  KEY `fk_ventart_invart` (`no_cia`,`cod_art`),
  CONSTRAINT `FK_ventaarticulo_promocion` FOREIGN KEY (`IDPROMOCION`) REFERENCES `promocion` (`IDPROMOCION`),
  CONSTRAINT `fk_ventart_invart` FOREIGN KEY (`no_cia`, `cod_art`) REFERENCES `inv_articulos` (`no_cia`, `cod_art`)
) ENGINE=InnoDB AUTO_INCREMENT=2235 DEFAULT CHARSET=utf8;

/*Table structure for table `ventacliente` */

CREATE TABLE `ventacliente` (
  `IDVENTACLIENTE` bigint(20) NOT NULL,
  `PRECIOESPECIAL` double DEFAULT NULL,
  `IDCLIENTE` bigint(20) NOT NULL,
  `no_cia` varchar(2) DEFAULT NULL,
  `cod_art` varchar(6) DEFAULT NULL,
  PRIMARY KEY (`IDVENTACLIENTE`),
  KEY `VentaCliente_CLIENTE_FK` (`IDCLIENTE`),
  KEY `VentaCliente_INVARFT_FK` (`no_cia`,`cod_art`),
  CONSTRAINT `VentaCliente_CLIENTE_FK` FOREIGN KEY (`IDCLIENTE`) REFERENCES `personacliente` (`IDPERSONACLIENTE`),
  CONSTRAINT `VentaCliente_INVARFT_FK` FOREIGN KEY (`no_cia`, `cod_art`) REFERENCES `inv_articulos` (`no_cia`, `cod_art`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `ventadirecta` */

CREATE TABLE `ventadirecta` (
  `IDVENTADIRECTA` bigint(20) NOT NULL,
  `DESCRIPCION` varchar(255) DEFAULT NULL,
  `FECHA_PEDIDO` date DEFAULT NULL,
  `OBSERVACION` varchar(255) DEFAULT NULL,
  `TOTAL` double DEFAULT NULL,
  `IDCLIENTE` bigint(20) DEFAULT NULL,
  `ESTADO` varchar(15) DEFAULT NULL,
  `TOTALIMPORTE` double NOT NULL,
  `IMPUESTO` double NOT NULL,
  `IDUSUARIO` bigint(20) DEFAULT NULL,
  `CODIGO` bigint(20) DEFAULT NULL,
  `IDMOVIMIENTO` bigint(20) DEFAULT NULL,
  `PAGO` double NOT NULL,
  `CAMBIO` double NOT NULL,
  `documento` longblob,
  `id_tmpenc` bigint(20) DEFAULT NULL,
  `IDSUCURSAL` bigint(20) DEFAULT NULL,
  `flagstock` int(11) DEFAULT '0',
  `id_tmpenc_cv` bigint(20) DEFAULT NULL,
  `CV` int(11) NOT NULL DEFAULT '0',
  `flagct` int(11) DEFAULT '0',
  `tipoventa` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`IDVENTADIRECTA`),
  KEY `fk_ventdir_movimiento` (`IDMOVIMIENTO`),
  KEY `fk_ventdir_usuario` (`IDUSUARIO`),
  KEY `fk_ventdir_cliente` (`IDCLIENTE`),
  KEY `id_tmpenc` (`id_tmpenc`),
  CONSTRAINT `fk_ventdir_cliente` FOREIGN KEY (`IDCLIENTE`) REFERENCES `personacliente` (`IDPERSONACLIENTE`),
  CONSTRAINT `fk_ventdir_movimiento` FOREIGN KEY (`IDMOVIMIENTO`) REFERENCES `movimiento` (`IDMOVIMIENTO`),
  CONSTRAINT `fk_ventdir_usuario` FOREIGN KEY (`IDUSUARIO`) REFERENCES `usuario` (`idusuario`),
  CONSTRAINT `ventadirecta_ibfk_1` FOREIGN KEY (`id_tmpenc`) REFERENCES `sf_tmpenc` (`id_tmpenc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `zona` */

CREATE TABLE `zona` (
  `idzona` bigint(20) NOT NULL,
  `nombre` varchar(150) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idciudad` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idzona`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `zonaproductiva` */

CREATE TABLE `zonaproductiva` (
  `idzonaproductiva` bigint(20) NOT NULL,
  `grupo` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `nombre` varchar(200) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `numero` varchar(20) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `idcompania` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idzonaproductiva`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

