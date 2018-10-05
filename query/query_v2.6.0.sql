/** 05-10-2018 **/
/** Modificando tabla SOCIO **/
ALTER TABLE socio ADD COLUMN noidentificacion VARCHAR(100) AFTER idsocio;
ALTER TABLE socio ADD COLUMN idexttipodocumento BIGINT(20) AFTER noidentificacion;
ALTER TABLE socio ADD COLUMN idtipodocumento BIGINT(20) AFTER idexttipodocumento;
ALTER TABLE socio ADD COLUMN apellidopaterno VARCHAR(200) AFTER idtipodocumento;
ALTER TABLE socio ADD COLUMN apellidomaterno VARCHAR(200) AFTER apellidopaterno;
ALTER TABLE socio ADD COLUMN nombres VARCHAR(255) AFTER apellidomaterno;
ALTER TABLE socio ADD COLUMN fechanacimiento DATE AFTER nombres;
ALTER TABLE socio ADD COLUMN profesion VARCHAR(100) AFTER fechanacimiento;
ALTER TABLE socio ADD COLUMN idsaludo BIGINT(20) AFTER profession;
ALTER TABLE socio ADD COLUMN genero VARCHAR(100) AFTER idsaludo;
ALTER TABLE socio ADD COLUMN idestadocivil BIGINT(20) AFTER genero;
ALTER TABLE socio ADD COLUMN domicilio VARCHAR(500) AFTER idestadocivil;

--

UPDATE socio s
JOIN persona p ON s.`idsocio` = p.`idpersona`
JOIN entidad e ON p.`idpersona` = e.`identidad`
SET 	s.`noidentificacion` = e.noidentificacion,
	s.`idexttipodocumento` = e.idexttipodocumento,
	s.`idtipodocumento` = e.idtipodocumento,
	s.`apellidopaterno` = p.`apellidopaterno`,
	s.`apellidomaterno` = p.`apellidomaterno`,
	s.`nombres` = p.`nombres`,
	s.`fechanacimiento` = p.`fechanacimiento`,
	s.`profesion` = p.`profesion`,
	s.`idsaludo` = p.`idsaludo`,
	s.`genero` = p.`genero`,
	s.`idestadocivil` = p.`idestadocivil`,
	s.`domicilio` = p.`domicilio`
;

INSERT INTO secuencia VALUES ('socio', 1);
UPDATE SECUENCIA SET VALOR=(SELECT MAX(e.idsocio)+1 FROM socio e) WHERE TABLA='socio';
ALTER TABLE socio DROP FOREIGN KEY socio_ibfk_2;

-- eliminar identidad 500 / en acopio reemplazar id 500 por el id 512
UPDATE acopiomateriaprima a 		SET a.`idproductormateriaprima` = 512 WHERE a.`idproductormateriaprima` = 500;
UPDATE descuentoreserva d 		SET d.`idproductormateriaprima` = 512 WHERE d.`idproductormateriaprima` = 500;
UPDATE descuentproductmateriaprima d 	SET d.`idproductormateriaprima` = 512 WHERE d.`idproductormateriaprima` = 500;

DELETE FROM productormateriaprima WHERE idproductormateriaprima = 500;
DELETE FROM persona WHERE idpersona = 500;
DELETE FROM entidad WHERE identidad = 500;

-- eliminar idpersona 510, 516 si no tiene acopio
DELETE FROM contratopuesto WHERE idcontrato = 60;
DELETE FROM contratopuesto WHERE idcontrato = 7;

DELETE FROM contrato WHERE idcontrato = 60;
DELETE FROM contrato WHERE idcontrato = 7;

DELETE FROM empleado WHERE idempleado = 510;
DELETE FROM persona WHERE idpersona = 510;
DELETE FROM entidad WHERE identidad = 510;

DELETE FROM empleado WHERE idempleado = 516;
DELETE FROM persona WHERE idpersona = 516;
DELETE FROM entidad WHERE identidad = 516;

-- 23 por el 579 ok
INSERT INTO `productormateriaprima` (`idproductormateriaprima`, `licenciaimpuestos`, `licenciaimpuestos2011`, `fechaexpiralicenciaimpuesto`, `fechafinimpuesto2011`, `esresponsable`, `fechainicialicenciaimpuesto`, `fechainiimpuesto2011`, `idcompania`, `idzonaproductiva`) 
VALUES('579',NULL,NULL,NULL,NULL,'0',NULL,NULL,'1','65');

UPDATE acopiomateriaprima a 		SET a.`idproductormateriaprima` = 579 WHERE a.`idproductormateriaprima` = 23;
UPDATE descuentoreserva d 		SET d.`idproductormateriaprima` = 579 WHERE d.`idproductormateriaprima` = 23;
UPDATE descuentproductmateriaprima d 	SET d.`idproductormateriaprima` = 579 WHERE d.`idproductormateriaprima` = 23;
UPDATE movimientosalarioproductor d 	SET d.`idproductormateriaprima` = 579 WHERE d.`idproductormateriaprima` = 23;
UPDATE descuentproductmateriaprima d 	SET d.`idproductormateriaprima` = 579 WHERE d.`idproductormateriaprima` = 23;

DELETE FROM productormateriaprima WHERE idproductormateriaprima = 23;
DELETE FROM persona WHERE idpersona = 23;
DELETE FROM entidad WHERE identidad = 23;

-- 356 por el 537

UPDATE acopiomateriaprima a 		SET a.`idproductormateriaprima` = 537 WHERE a.`idproductormateriaprima` = 356;
UPDATE descuentoreserva d 		SET d.`idproductormateriaprima` = 579 WHERE d.`idproductormateriaprima` = 356;
UPDATE descuentproductmateriaprima d 	SET d.`idproductormateriaprima` = 579 WHERE d.`idproductormateriaprima` = 356;
UPDATE movimientosalarioproductor d 	SET d.`idproductormateriaprima` = 579 WHERE d.`idproductormateriaprima` = 356;
UPDATE descuentproductmateriaprima d 	SET d.`idproductormateriaprima` = 579 WHERE d.`idproductormateriaprima` = 356;

DELETE FROM productormateriaprima WHERE idproductormateriaprima = 356;
DELETE FROM persona WHERE idpersona = 356;
DELETE FROM entidad WHERE identidad = 356;

-- ----------
-- 399 eliminar ELENA TORRICO SILES, - 1260 
-- 72 ELENA TORRICO FLORES ok - 1259

DELETE FROM ventacliente WHERE IDCLIENTE = 1260;
DELETE FROM personacliente WHERE IDPERSONACLIENTE = 1260;

DELETE FROM productormateriaprima WHERE idproductormateriaprima = 399;
DELETE FROM persona WHERE idpersona = 399;
DELETE FROM entidad WHERE identidad = 399;

-- ---------------------
-- idcliente 1268 JUSTINO , idpersona 292
-- idcliente 1269 JUANA	  , idpersona 314

UPDATE personacliente p SET p.`NRO_DOC` = '100001' WHERE p.`IDPERSONACLIENTE` = 1268;
UPDATE personacliente p SET p.`NRO_DOC` = '100002' WHERE p.`IDPERSONACLIENTE` = 1269;

UPDATE entidad e SET e.`noidentificacion` = '100001' WHERE e.`identidad` = 292;
UPDATE entidad e SET e.`noidentificacion` = '100002' WHERE e.`identidad` = 314;

-- ---------------------
-- idcliente 1292 MODESTA, idpersona 305
-- idcliente 1291 JULIANA, idpersona 285

UPDATE personacliente p SET p.`NRO_DOC` = '100003' WHERE p.`IDPERSONACLIENTE` = 1292;
UPDATE personacliente p SET p.`NRO_DOC` = '100004' WHERE p.`IDPERSONACLIENTE` = 1291;

UPDATE entidad e SET e.`noidentificacion` = '100003' WHERE e.`identidad` = 305;
UPDATE entidad e SET e.`noidentificacion` = '100004' WHERE e.`identidad` = 285;


SELECT * FROM pedidos p WHERE p.`IDCLIENTE` IN (1291, 1292);
SELECT * FROM empleado e WHERE e.`idempleado` IN (305, 285);
SELECT * FROM productormateriaprima p WHERE p.`idproductormateriaprima` IN (168);
SELECT * FROM acopiomateriaprima p WHERE p.`idproductormateriaprima` IN (168);

-- -----

ALTER TABLE socio ADD FOREIGN KEY (idexttipodocumento) REFERENCES exttipodocumento(idexttipodocumento);
ALTER TABLE socio ADD FOREIGN KEY (idtipodocumento) REFERENCES tipodocumento(idtipodocumento);
ALTER TABLE socio ADD FOREIGN KEY (idsaludo) REFERENCES saludo(idsaludo);
ALTER TABLE socio ADD FOREIGN KEY (idestadocivil) REFERENCES estadocivil(idestadocivil);

ALTER TABLE productormateriaprima ADD FOREIGN KEY (idproductormateriaprima) REFERENCES persona(idpersona);

DELETE FROM persona WHERE idpersona IN ( SELECT s.`idsocio` FROM socio s);
DELETE FROM entidad WHERE identidad IN ( SELECT s.`idsocio` FROM socio s);


-- --------------------------------------

SELECT e.`noidentificacion`, COUNT(e.`noidentificacion`)
FROM entidad e
GROUP BY e.`noidentificacion`
;

SELECT p.`idpersona`, p.`nombres`, p.`apellidopaterno`, p.`apellidomaterno`, p.`genero`, p.`domicilio`, e.`noidentificacion`
FROM persona p
JOIN entidad e ON p.`idpersona` = e.`identidad`
WHERE p.`idpersona` IN (
	SELECT e.`identidad`
	FROM entidad e
	WHERE e.`noidentificacion` IN (78787,3013295,4504863,6537359,65656,7875416)
	);






