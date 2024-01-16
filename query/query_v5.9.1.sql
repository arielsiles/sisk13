CREATE TABLE `plantillacontablepredefinida` (
  `idplantillacontablepredefinida` bigint NOT NULL DEFAULT '0',
  `nombre` varchar(150) ,
  `version` bigint ,
  `idcompania` bigint ,
  PRIMARY KEY (`idplantillacontablepredefinida`)
) ;