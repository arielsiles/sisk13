-- ============================================================================
-- query_v6.0.129_terdemol.sql
-- ============================================================================
-- Regimen de aportes al Sistema Integral de Pensiones.
--
-- Reemplaza los numeros de carnet que estaban escritos en RetentionAFPCalculator y en
-- PatronalAFPRetentionCalculator para eximir del aporte a la cuenta individual y/o de la
-- prima de riesgo comun. Ahora es configuracion: RRHH > Generacion de planillas >
-- Configuracion > Regimenes de aportes SIP, y se asigna en el contrato.
--
-- APLICAR ANTES DE DESPLEGAR: persistence usa hbm2ddl.auto=validate; si la tabla o la
-- columna no existen, el despliegue falla al arrancar.
-- ----------------------------------------------------------------------------


-- 1) Catalogo -----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS regimenaportesip (
  idregimenaportesip     BIGINT       NOT NULL,
  nombre                 VARCHAR(100) NOT NULL,
  descripcion            VARCHAR(500)     NULL,
  aportacuentaindividual INT          NOT NULL DEFAULT 1,
  aportariesgocomun      INT          NOT NULL DEFAULT 1,
  aportasolidario        INT          NOT NULL DEFAULT 1,
  aportacomision         INT          NOT NULL DEFAULT 1,
  aportanacsolidario     INT          NOT NULL DEFAULT 1,
  aportapatronal         INT          NOT NULL DEFAULT 1,
  aportacns              INT          NOT NULL DEFAULT 1,
  pordefecto             INT          NOT NULL DEFAULT 0,
  activo                 INT          NOT NULL DEFAULT 1,
  idcompania             BIGINT       NOT NULL,
  version                BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (idregimenaportesip),
  UNIQUE KEY uk_regimenaportesip_nombre (nombre),
  KEY fk_regimenaportesip_compania (idcompania),
  CONSTRAINT fk_regimenaportesip_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- 2) FK en contrato -----------------------------------------------------------
-- Nulo = "usar el regimen por defecto". Asi los contratos existentes siguen calculando
-- exactamente igual que antes de este cambio.

ALTER TABLE contrato ADD COLUMN idregimenaportesip BIGINT NULL;
ALTER TABLE contrato ADD CONSTRAINT fk_contrato_regimenaportesip
  FOREIGN KEY (idregimenaportesip) REFERENCES regimenaportesip (idregimenaportesip);


-- 3) Los cuatro regimenes -----------------------------------------------------
-- Descomposicion del aporte del asegurado sobre el total ganado:
--   cuenta individual 10% + riesgo comun 1,71% + solidario 0,5% + comision 0,5% = 12,71%
-- Los porcentajes 12,71 / 2,71 / 1,00 son los que ya estaban en el codigo antes del
-- refactor que perdio la regla del jubilado (AGE_COMPLIANT_PERCENTAGE = 1.0 y
-- NOT_AGE_COMPLIANT_PERCENTAGE = 2.71 en RetentionAFPCalculator).
--
-- El aporte nacional solidario y los aportes del empleador (riesgo profesional, pro
-- vivienda, solidario patronal y CNS) se cobran en los cuatro regimenes: son
-- redistributivos o cubren riesgos que el jubilado sigue corriendo mientras trabaja.

INSERT INTO regimenaportesip
  (idregimenaportesip, nombre, descripcion,
   aportacuentaindividual, aportariesgocomun, aportasolidario, aportacomision,
   aportanacsolidario, aportapatronal, aportacns, pordefecto, activo, idcompania, version)
SELECT 1, 'Asegurado activo',
       'Aporte completo del asegurado: 10% cuenta individual + 1,71% riesgo comun + 0,5% solidario + 0,5% comision = 12,71%.',
       1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM regimenaportesip WHERE idregimenaportesip = 1);

INSERT INTO regimenaportesip
  (idregimenaportesip, nombre, descripcion,
   aportacuentaindividual, aportariesgocomun, aportasolidario, aportacomision,
   aportanacsolidario, aportapatronal, aportacns, pordefecto, activo, idcompania, version)
SELECT 2, 'Jubilado con edad cumplida',
       'Percibe pension y continua trabajando, con la edad de jubilacion cumplida: no aporta a la cuenta individual ni paga la prima de riesgo comun. Aporta 0,5% + 0,5% = 1,00%.',
       0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM regimenaportesip WHERE idregimenaportesip = 2);

INSERT INTO regimenaportesip
  (idregimenaportesip, nombre, descripcion,
   aportacuentaindividual, aportariesgocomun, aportasolidario, aportacomision,
   aportanacsolidario, aportapatronal, aportacns, pordefecto, activo, idcompania, version)
SELECT 3, 'Jubilado sin edad cumplida',
       'Percibe pension y continua trabajando, sin la edad de jubilacion cumplida: no aporta a la cuenta individual pero sigue pagando la prima de riesgo comun. Aporta 1,71% + 0,5% + 0,5% = 2,71%.',
       0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM regimenaportesip WHERE idregimenaportesip = 3);

INSERT INTO regimenaportesip
  (idregimenaportesip, nombre, descripcion,
   aportacuentaindividual, aportariesgocomun, aportasolidario, aportacomision,
   aportanacsolidario, aportapatronal, aportacns, pordefecto, activo, idcompania, version)
SELECT 4, 'Edad de jubilacion cumplida',
       'Cumplio la edad de jubilacion pero no se jubilo: sigue aportando a su cuenta individual y deja de pagar la prima de riesgo comun. Aporta 10% + 0,5% + 0,5% = 11,00%.',
       1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM regimenaportesip WHERE idregimenaportesip = 4);


-- 4) Secuencia del @TableGenerator --------------------------------------------
-- `valor` es el PROXIMO id a entregar, no el ultimo entregado.

INSERT INTO secuencia (tabla, valor)
SELECT 'regimenaportesip', (SELECT COALESCE(MAX(idregimenaportesip), 0) + 1 FROM regimenaportesip)
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'regimenaportesip');


-- 5) Migracion de los carnets que estaban en el codigo ------------------------
-- Se asignan por numero de identificacion, no por id: los ids difieren entre bases.
-- Si un carnet no existe en esta base el UPDATE no toca nada, que es lo correcto.
--
--   2862262 JUANA DE DIOS POZO   -> individual 0 y riesgo comun 0  -> regimen 2
--   2868139 ELISEO CAMACHO       -> individual 0 y riesgo comun 0  -> regimen 2
--    815059 LUIS FERRUFINO       -> solo riesgo comun 0            -> regimen 4
--    921886 SIMON CALUCHO        -> solo riesgo comun 0            -> regimen 4

UPDATE contrato c
   JOIN empleado e ON e.idempleado = c.idempleado
   JOIN entidad  n ON n.identidad  = e.idempleado
   SET c.idregimenaportesip = 2
 WHERE n.noidentificacion IN ('2862262', '2868139')
   AND c.idregimenaportesip IS NULL;

UPDATE contrato c
   JOIN empleado e ON e.idempleado = c.idempleado
   JOIN entidad  n ON n.identidad  = e.idempleado
   SET c.idregimenaportesip = 4
 WHERE n.noidentificacion IN ('815059', '921886')
   AND c.idregimenaportesip IS NULL;


-- 6) Permiso ------------------------------------------------------------------
-- Bitmask: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. 15 = CRUD completo.
-- idmodulo = 4 (employees). idfuncionalidad NO es auto_increment: se calcula MAX+1.
-- El grant a los roles se hace desde Administracion > Roles; hasta entonces la opcion
-- queda oculta para todos.

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'SIPCONTRIBUTIONREGIME', 'Regimenes de aportes al SIP (regimenaportesip)', 4, 15,
       'Functionality.employees.sipContributionRegime', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'SIPCONTRIBUTIONREGIME');


-- Sin control de asistencia no se exigen bandas horarias para generar planilla.
UPDATE empleado SET flagcontrol = 0;

-- solo terdemol
UPDATE regladescuento SET idunidadnegocio = 2 WHERE idregladescuento = 1;