/** 31/08/2018 creditos **/
-- ALTER TABLE transaccioncredito MODIFY COLUMN fechatransaccion DATE NOT NULL;
-- ALTER TABLE transaccioncredito ADD COLUMN fechacreacion DATETIME NOT NULL AFTER fechatransaccion;
-- UPDATE transaccioncredito t SET t.`fechacreacion` = '2018-08-30 00:00:00';

ALTER TABLE credito ADD COLUMN cuotas INT(11) NOT NULL AFTER plazo;
UPDATE credito c SET c.`cuotas` = c.`plazo`;


CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vmarcado` AS 
SELECT  `r`.`idrhmarcado` AS `idrhmarcado`,  `r`.`marfecha` AS `marfecha`,  `r`.`marperid` AS `marperid`,  CONCAT(`r`.`marperid`,'') AS `marreftarjeta`,  `r`.`marhora` AS `marhora`,  `r`.`control` AS `control`,  `r`.`marippc` AS `marippc`,  `r`.`descripcion` AS `descripcion`,  '1' AS `sede`,  CONCAT(`p`.`nombres`,' ',`p`.`apellidopaterno`,' ',`p`.`apellidomaterno`) AS `nombre` FROM ((`rh_marcado` `r`  LEFT JOIN `empleado` `em`  ON ((`r`.`marperid` = `em`.`codigomarcacion`)))  LEFT JOIN `persona` `p`  ON ((`em`.`idempleado` = `p`.`idpersona`))) WHERE (`r`.`marfecha` >= '2018-07-01')