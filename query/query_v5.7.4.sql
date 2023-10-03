-- 25.09.2023
/* OJO */
/* Inserts and updates for Terdemol */

ALTER TABLE inv_tipodocs MODIFY COLUMN tipo_vale VARCHAR(2) NULL;
UPDATE inv_tipodocs SET `tipo_vale` = 'RT' WHERE cod_doc = 'RPT';

ALTER TABLE inv_vales add column iddestino bigint(20);

ALTER TABLE inv_vales ADD FOREIGN KEY (iddestino) REFERENCES inv_destino (iddestino);

/* Inserts for Terdemol */
-- insert into `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) values('1','CP-1','CILO PULMON','0','1');
-- insert into `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) values('2','CD-1','CONO DISTRIBUIDOR ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('3','AM-1','ALMACEN','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('4','CH-1','MOLINO CHANCADOR','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('5','CT-1','CINTA TRANSPORTADORA','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('6','CT-2','CINTA TRANSPORTADORA HACIA EL RAYMOND','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('7','CT-3','CINTA TRANSPORTADORA HACIA LA TOLVA','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('8','CT-4','CINTA TRANSPORTADORA ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('9','CT-5','CINTA TRANSPORTADORA ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('10','CT-6','CINTA TRANSPORTADORA ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('11','CT-7','CINTA TRANSPORTADORA ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('12','CT-8','CINTA TRANSPORTADORA ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('13','EC-0','ELEVADOR DE CANJILONES','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('14','EC-1','ELEVADOR DE CANJILONES','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('15','EC-2','ELEVADOR DE CANJILONES','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('16','FC-0','FARMACEUTICO','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('17','FS-1','FOSA1','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('18','FS-2','FOSA2','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('19','GO-1','GRADERIA OFICINA','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('20','GR-1','GRUA MOVILIDAD','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('21','MC-1','MESCLADOR','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('22','MM-1','MOLINO DE MARTILLOS','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('23','MR-1','MOLINO RAYMOND','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('24','OF-1','OFICINA','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('25','PC-1','PALA CARGADORA','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('26','PG-1','PLATO GRANULADOR','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('27','PM-1','PORTON MOVIL ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('28','PM-2','PORTON MOVIL ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('29','PM-3','PORTON MOVIL ','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('30','SB-1','SUBESTACION','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('31','SCH-1','SERCHA 10M','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('32','SCH-2','SERCHA 7M','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('33','SCH-3','SERCHA 5M (LABORATORIO','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('34','SCH-4','SERCHA (PALOMERO)','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('35','SL-0','SILOS','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('36','SR-1','SECADOR ROTATIVO','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('37','TA-1','TOLVA PRINCIPAR','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('38','TA-1','TOLVA DE ALIMENTACION HACIA LOS SILOS','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('39','TC-1','TROMEL CLASIFICADOR','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('40','TM-1','TAMIZADOR','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('41','TT-1','TORNILLO TRANSPORTADOR','0','1');
INSERT INTO `inv_destino` (`iddestino`, `codigo`, `nombre`, `version`, `idcompania`) VALUES('42','TT-2','TORNILLO TRANSPORTADOR','0','1');

UPDATE secuencia SET valor=(SELECT MAX(e.iddestino)+1 FROM inv_destino e) WHERE tabla='inv_destino';


UPDATE `lugarrecepcion` SET idunidadnegocio = 2;

UPDATE `lugarrecepcion` SET nombre = 'TERDEMOL PLANTA' WHERE idlugarrecepcion = 1;
DELETE FROM lugarrecepcion WHERE idlugarrecepcion IN (2, 3);


