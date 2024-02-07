--nuevo modulo de produccion de terdemol
INSERT INTO ilva.modulo (idmodulo, descripcion, nombrerecurso, idcompania)
VALUES (11, 'modulo de produccion de TERDEMOL', 'productionx', 1);
---clonando
create table xproductobase
(
    idproductobase            bigint(20)    not null
        primary key,
    codigo                    varchar(50)   null,
    id_tmpenc                 bigint        null,
    no_trans                  varchar(255)  null,
    no_vale                   varchar(255)  null,
    estado                    varchar(20)   null,
    costototalinsumos         decimal(16, 6)  null,
    unidades                  int            null,
    volumen                   decimal(8, 2) null,
    idcompania                bigint(20)    null,
    idplanificacionproduccion bigint(20)    null
);

------------------

--
-- Table structure for table `xpr_insumoformula`
--

DROP TABLE IF EXISTS `xpr_insumoformula`;

CREATE TABLE `xpr_insumoformula` (
                                     `idinsumoformula` bigint NOT NULL,
                                     `cantidad` decimal(20,6) NOT NULL,
                                     `cod_art` varchar(6) NOT NULL,
                                     `defecto` int DEFAULT NULL,
                                     `idformula` bigint NOT NULL,
                                     `idform` bigint DEFAULT NULL,
                                     `VERSION` bigint NOT NULL,
                                     `idcompania` bigint NOT NULL,
                                     PRIMARY KEY (`idinsumoformula`),
                                     KEY `idformula` (`idformula`),
                                     KEY `idform` (`idform`)

);

--
-- Table structure for table `xpr_categoria`
--

DROP TABLE IF EXISTS `xpr_categoria`;

CREATE TABLE `xpr_categoria` (
                                 `idcategoria` bigint NOT NULL,
                                 `nombre` varchar(100) DEFAULT NULL,
                                 `VERSION` bigint NOT NULL,
                                 `idcompania` bigint NOT NULL,
                                 PRIMARY KEY (`idcategoria`)
) ;


--
-- Table structure for table `xpr_produccion`
--

DROP TABLE IF EXISTS `xpr_produccion`;

CREATE TABLE `xpr_produccion` (
                                  `idproduccion` bigint NOT NULL,
                                  `codigo` int DEFAULT NULL,
                                  `estado` varchar(5) DEFAULT NULL,
                                  `costototal` decimal(16,2) DEFAULT '0.00',
                                  `totalmp` decimal(16,2) DEFAULT '0.00',
                                  `descripcion` varchar(255) DEFAULT NULL,
                                  `idformula` bigint DEFAULT NULL,
                                  `idtanque` bigint DEFAULT NULL,
                                  `idplan` bigint DEFAULT NULL,
                                  `id_tmpenc` bigint DEFAULT NULL,
                                  `version` bigint NOT NULL,
                                  `idcompania` bigint NOT NULL,
                                  PRIMARY KEY (`idproduccion`),
                                  KEY `idformula` (`idformula`),
                                  KEY `idtanque` (`idtanque`),
                                  KEY `idplan` (`idplan`)
);

--
-- Table structure for table `xpr_producto`
--

DROP TABLE IF EXISTS `xpr_producto`;

CREATE TABLE `xpr_producto` (
                                `idproducto` bigint NOT NULL,
                                `cod_art` varchar(6) DEFAULT NULL,
                                `cantidad` decimal(16,2) DEFAULT NULL,
                                `costo` decimal(16,2) DEFAULT '0.00',
                                `costouni` decimal(16,2) DEFAULT '0.00',
                                `costo_a` decimal(16,2) DEFAULT '0.00',
                                `costo_b` decimal(16,2) DEFAULT '0.00',
                                `costo_c` decimal(16,2) DEFAULT '0.00',
                                `idproduccion` bigint DEFAULT NULL,
                                `idplan` bigint DEFAULT NULL,
                                `VERSION` bigint NOT NULL,
                                `idcompania` bigint NOT NULL,
                                PRIMARY KEY (`idproducto`),
                                KEY `idproduccion` (`idproduccion`),
                                KEY `idplan` (`idplan`)
);

--
-- Table structure for table `xpr_tanque`
--

--
-- Table structure for table `xpr_formula`
--

DROP TABLE IF EXISTS `xpr_formula`;

CREATE TABLE `xpr_formula` (
                               `idformula` bigint NOT NULL,
                               `nombre` varchar(100) NOT NULL,
                               `estado` varchar(3) DEFAULT NULL,
                               `totaleq` decimal(16,2) NOT NULL,
                               `capacidad` decimal(16,2) DEFAULT NULL,
                               `activo` int DEFAULT NULL,
                               `idcategoria` bigint DEFAULT NULL,
                               `VERSION` bigint NOT NULL,
                               `idcompania` bigint NOT NULL,
                               PRIMARY KEY (`idformula`),
                               KEY `idcategoria` (`idcategoria`)
);

--
-- Table structure for table `xpr_insumo`
--

DROP TABLE IF EXISTS `xpr_insumo`;

CREATE TABLE `xpr_insumo` (
                              `idinsumo` bigint NOT NULL,
                              `cod_art` varchar(6) DEFAULT NULL,
                              `cantidad` decimal(16,6) NOT NULL DEFAULT '0.000000',
                              `costouni` decimal(16,6) DEFAULT NULL,
                              `tipo` varchar(15) DEFAULT NULL,
                              `idinsumoformula` bigint DEFAULT NULL,
                              `idproduccion` bigint NOT NULL,
                              `idproducto` bigint DEFAULT NULL,
                              `VERSION` bigint NOT NULL,
                              `idcompania` bigint NOT NULL,
                              PRIMARY KEY (`idinsumo`),
                              KEY `idproduccion` (`idproduccion`),
                              KEY `idinsumoformula` (`idinsumoformula`),
                              KEY `idproducto` (`idproducto`)
) ;

--
-- Table structure for table `xpr_material`
--

DROP TABLE IF EXISTS `xpr_material`;

CREATE TABLE `xpr_material` (
                                `idmaterial` bigint NOT NULL,
                                `cod_art` varchar(6) NOT NULL,
                                `cod_art_mat` varchar(6) NOT NULL,
                                `descri` varchar(100) DEFAULT NULL,
                                `flag_cant` int DEFAULT NULL,
                                `tipo` varchar(15) DEFAULT NULL,
                                `vol1` decimal(10,2) DEFAULT NULL,
                                `peso1` decimal(10,2) DEFAULT NULL,
                                `VERSION` bigint NOT NULL,
                                `idcompania` bigint NOT NULL,
                                PRIMARY KEY (`idmaterial`),
                                KEY `cod_art` (`cod_art`),
                                KEY `cod_art_mat` (`cod_art_mat`)
);

--
-- Table structure for table `xpr_plan`
--

DROP TABLE IF EXISTS `xpr_plan`;

CREATE TABLE `xpr_plan` (
                            `idplan` bigint NOT NULL,
                            `nombre` varchar(100) DEFAULT NULL,
                            `fecha` date NOT NULL,
                            `estado` varchar(5) DEFAULT NULL,
                            `VERSION` bigint NOT NULL,
                            `idcompania` bigint NOT NULL,
                            PRIMARY KEY (`idplan`),
                            UNIQUE KEY `fecha` (`fecha`)
);
--
-- Table structure for table `xpr_prodafectado`
--

DROP TABLE IF EXISTS `xpr_prodafectado`;

CREATE TABLE `xpr_prodafectado` (
                                    `idprodafectado` bigint NOT NULL,
                                    `idinsumo` bigint NOT NULL,
                                    `idproducto` bigint NOT NULL,
                                    `VERSION` bigint NOT NULL,
                                    `idcompania` bigint NOT NULL,
                                    PRIMARY KEY (`idprodafectado`),
                                    KEY `idinsumo` (`idinsumo`),
                                    KEY `idproducto` (`idproducto`),
                                    KEY `idcompania` (`idcompania`)
);


DROP TABLE IF EXISTS `xpr_tanque`;

CREATE TABLE `xpr_tanque` (
                              `idtanque` bigint NOT NULL,
                              `nombre` varchar(255) NOT NULL,
                              `capacidad` decimal(16,2) NOT NULL,
                              `codmed` varchar(6) DEFAULT NULL,
                              `VERSION` bigint NOT NULL,
                              `idcompania` bigint NOT NULL,
                              PRIMARY KEY (`idtanque`)
);
