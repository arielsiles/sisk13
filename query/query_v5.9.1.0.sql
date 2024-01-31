-- 13.01.2023
ALTER TABLE af_activos ADD COLUMN codigo VARCHAR(255) AFTER codbarras;

ALTER TABLE af_activos MODIFY serie VARCHAR(255);
ALTER TABLE af_activos MODIFY marca VARCHAR(255);
ALTER TABLE af_activos MODIFY modelo VARCHAR(255);

INSERT INTO `aflocalizacion` (`idaflocalizacion`, `nombre`, `version`, `idcompania`) VALUES('5','AMINISTRACION CS','0','1');
INSERT INTO `aflocalizacion` (`idaflocalizacion`, `nombre`, `version`, `idcompania`) VALUES('6','AMINISTRACION CS PB','0','1');
INSERT INTO `aflocalizacion` (`idaflocalizacion`, `nombre`, `version`, `idcompania`) VALUES('7','AMINISTRACION CS 1P','0','1');
INSERT INTO `aflocalizacion` (`idaflocalizacion`, `nombre`, `version`, `idcompania`) VALUES('8','AMINISTRACION CS 2P','0','1');

UPDATE secuencia SET valor=(SELECT MAX(e.idaflocalizacion)+1 FROM aflocalizacion e) WHERE tabla='aflocalizacion';
UPDATE secuencia SET valor=(SELECT MAX(e.`idactivo`)+1 FROM af_activos e) WHERE tabla='af_activos';

ALTER TABLE af_activos ADD COLUMN revaluo INT(1) AFTER tasa_dep;

UPDATE af_activos a SET a.revaluo = 0 WHERE a.revaluo IS NULL;
