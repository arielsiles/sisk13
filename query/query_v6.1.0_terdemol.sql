-- ============================================================================
-- query_v6.1.0.sql
-- ============================================================================
-- Etapa E1 del PLAN de control de asistencia: carga masiva de marcaciones.
-- Aplica a todas las empresas. Ver docs/employees/spec/.
--
-- APLICAR ANTES DE DESPLEGAR: persistence usa hbm2ddl.auto=validate.
-- ----------------------------------------------------------------------------


-- 1) Lote de importacion de marcaciones ---------------------------------------

CREATE TABLE IF NOT EXISTS loteimportmarcado (
  idloteimportmarcado BIGINT       NOT NULL,
  nombrearchivo       VARCHAR(250) NOT NULL,
  fechacarga          DATETIME     NOT NULL,
  idusuario           BIGINT           NULL,
  fechadesde          DATE             NULL,
  fechahasta          DATE             NULL,
  filasleidas         INT          NOT NULL DEFAULT 0,
  marcasnuevas        INT          NOT NULL DEFAULT 0,
  marcasduplicadas    INT          NOT NULL DEFAULT 0,
  marcassinempleado   INT          NOT NULL DEFAULT 0,
  codigossinempleado  VARCHAR(2000)    NULL,
  estado              VARCHAR(20)  NOT NULL,
  idcompania          BIGINT       NOT NULL,
  version             BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (idloteimportmarcado),
  KEY fk_loteimportmarcado_compania (idcompania),
  CONSTRAINT fk_loteimportmarcado_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- 2) Marca -> lote ------------------------------------------------------------
-- Nulo significa que la escribio el dispositivo directo, no una importacion.

ALTER TABLE rh_marcado ADD COLUMN idloteimportmarcado BIGINT NULL;
ALTER TABLE rh_marcado ADD CONSTRAINT fk_rhmarcado_loteimport
  FOREIGN KEY (idloteimportmarcado) REFERENCES loteimportmarcado (idloteimportmarcado);


-- 3) Deduplicacion ------------------------------------------------------------
-- El dispositivo emite marcas repetidas y el archivo las trae duplicadas. El indice
-- acelera la verificacion de existencia que hace el importador fila por fila.

CREATE INDEX ix_rhmarcado_persona_fecha_hora ON rh_marcado (marperid, marfecha, marhora);


-- 4) Secuencia del @TableGenerator --------------------------------------------
-- `valor` es el PROXIMO id a entregar, no el ultimo entregado.

INSERT INTO secuencia (tabla, valor)
SELECT 'loteimportmarcado', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'loteimportmarcado');


-- 5) Permiso ------------------------------------------------------------------
-- Bitmask: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. idmodulo = 4 (employees).
-- VIEW para consultar los lotes, CREATE para importar, DELETE para anular un lote.

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'MARKIMPORT', 'Carga masiva de marcaciones', 4, 11,
       'Functionality.employees.markImport', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'MARKIMPORT');

-- 6) Motivo en fechas especiales (E1.3) ----------------------------------------
-- Hasta ahora una fecha especial solo decia si era con goce de haber y a quien aplicaba.
-- Sin motivo no hay forma de reportar por causa ni de que la maternidad no consuma
-- vacaciones. Queda nulo en las filas existentes: inferirlo hacia atras seria adivinar.

ALTER TABLE fechaespecial ADD COLUMN motivo VARCHAR(30) NULL;


-- 7) Jornada semanal por genero (E1.4) -----------------------------------------
-- 48 h hombres / 40 h mujeres es la norma boliviana, pero el numero no se cablea: puede
-- cambiar y otras empresas tienen otra jornada.

CREATE TABLE IF NOT EXISTS jornadasemanal (
  idjornadasemanal BIGINT       NOT NULL,
  genero           VARCHAR(10)  NOT NULL,
  horassemana      DECIMAL(5,2) NOT NULL,
  horasdia         DECIMAL(5,2) NOT NULL,
  activo           INT          NOT NULL DEFAULT 1,
  descripcion      VARCHAR(200)     NULL,
  idcompania       BIGINT       NOT NULL,
  version          BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (idjornadasemanal),
  KEY fk_jornadasemanal_compania (idcompania),
  CONSTRAINT fk_jornadasemanal_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Valores de la norma boliviana como punto de partida. Cada empresa los ajusta por pantalla.
INSERT INTO jornadasemanal (idjornadasemanal, genero, horassemana, horasdia, activo, descripcion, idcompania, version)
SELECT 1, 'MAN', 48.00, 8.00, 1, 'Norma boliviana', 1, 0
  FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM jornadasemanal WHERE genero = 'MAN');

INSERT INTO jornadasemanal (idjornadasemanal, genero, horassemana, horasdia, activo, descripcion, idcompania, version)
SELECT 2, 'WOMAN', 40.00, 8.00, 1, 'Norma boliviana', 1, 0
  FROM (SELECT 1) t WHERE NOT EXISTS (SELECT 1 FROM jornadasemanal WHERE genero = 'WOMAN');

-- `valor` es el PROXIMO id a entregar, no el ultimo entregado.
INSERT INTO secuencia (tabla, valor)
SELECT 'jornadasemanal', (SELECT COALESCE(MAX(idjornadasemanal), 0) + 1 FROM jornadasemanal)
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'jornadasemanal');


-- Permiso. Bitmask: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. idmodulo = 4 (employees).
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'WEEKLYWORKLOAD', 'Jornada semanal por género', 4, 15,
       'Functionality.employees.weeklyWorkload', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'WEEKLYWORKLOAD');



-- 8) Vacaciones: activar el submodulo (V1) ------------------------------------
-- El submodulo esta completo pero nunca se pudo usar: faltaban estos tres permisos.
-- hasPermission devuelve falso en silencio si la funcionalidad no existe, asi que la
-- opcion de menu nunca se dibujo. VACATIONRULE ya estaba registrado.

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'VACATIONPLANNING', 'Planificación de vacaciones', 4, 15,
       'Functionality.employees.vacationPlanning', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'VACATIONPLANNING');

-- Aprobar y anular una vacacion se separan del alta a proposito: son actos distintos.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'VACATIONAPPROVE', 'Aprobar vacaciones', 4, 1,
       'Functionality.employees.vacationApprove', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'VACATIONAPPROVE');

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'VACATIONANNUL', 'Anular vacaciones', 4, 1,
       'Functionality.employees.vacationAnnul', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'VACATIONANNUL');


-- Secuencias de las cuatro tablas del submodulo. `valor` es el PROXIMO id a entregar.
INSERT INTO secuencia (tabla, valor)
SELECT 'reglavacacion', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'reglavacacion');
INSERT INTO secuencia (tabla, valor)
SELECT 'planvacacion', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'planvacacion');
INSERT INTO secuencia (tabla, valor)
SELECT 'gestionvacacion', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'gestionvacacion');
INSERT INTO secuencia (tabla, valor)
SELECT 'vacacion', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'vacacion');


-- Tramos de la Ley General del Trabajo. Se cargan por SQL para arrancar; despues se
-- mantienen por pantalla (Configuracion > Reglas de vacacion). `aniosfin` nulo = sin tope.
-- OJO: los tramos NO se pueden solapar. La consulta que busca el tramo de un anio usa
-- limites inclusivos de los dos lados y espera un unico resultado, asi que un 1-5 seguido
-- de un 5-10 revienta al sincronizar a alguien con 5 anios exactos.
INSERT INTO reglavacacion (idreglavacacion, codigo, nombre, aniosinicio, aniosfin, diasvacacion, idcompania, version)
SELECT 1, 1, 'DE 1 A 4 ANIOS', 1, 4, 15, 1, 0 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM reglavacacion WHERE aniosinicio = 1);
INSERT INTO reglavacacion (idreglavacacion, codigo, nombre, aniosinicio, aniosfin, diasvacacion, idcompania, version)
SELECT 2, 2, 'DE 5 A 9 ANIOS', 5, 9, 20, 1, 0 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM reglavacacion WHERE aniosinicio = 5);
INSERT INTO reglavacacion (idreglavacacion, codigo, nombre, aniosinicio, aniosfin, diasvacacion, idcompania, version)
SELECT 3, 3, 'DE 10 ANIOS EN ADELANTE', 10, NULL, 30, 1, 0 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM reglavacacion WHERE aniosinicio = 10);

UPDATE secuencia SET valor = (SELECT MAX(idreglavacacion) + 1 FROM reglavacacion)
 WHERE tabla = 'reglavacacion';

-- El codigo lo entrega otra secuencia, con nombre propio y no por tabla. Ojo: esta guarda
-- el ULTIMO valor entregado, al reves que las de arriba (incrementa y despues devuelve).
INSERT INTO secuencia (tabla, valor)
SELECT 'VACATIONRULE_CODE_SEQUENCE', (SELECT MAX(codigo) FROM reglavacacion) FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'VACATIONRULE_CODE_SEQUENCE');


-- 9) Vacaciones: saldo como libro de movimientos (V2) -------------------------
-- El saldo dejaba de existir cuando se recalculaba: los contadores se pisaban con el valor
-- "a hoy" y no habia forma de saber el saldo de un mes cerrado ni de explicar de donde salia.
-- Ahora se guardan los movimientos y el saldo es la suma. Los dias no caducan: se acumulan.

-- Las cuatro tablas del submodulo se crearon sin clave primaria ni indices: nunca se usaron.
-- Sin clave primaria en planvacacion y vacacion, MySQL no deja crear las FK del libro.
ALTER TABLE reglavacacion   MODIFY idreglavacacion   BIGINT NOT NULL;
ALTER TABLE reglavacacion   ADD PRIMARY KEY (idreglavacacion);
ALTER TABLE planvacacion    MODIFY idplanvacacion    BIGINT NOT NULL;
ALTER TABLE planvacacion    ADD PRIMARY KEY (idplanvacacion);
ALTER TABLE gestionvacacion MODIFY idgestionvacacion BIGINT NOT NULL;
ALTER TABLE gestionvacacion ADD PRIMARY KEY (idgestionvacacion);
ALTER TABLE vacacion        MODIFY idvacacion        BIGINT NOT NULL;
ALTER TABLE vacacion        ADD PRIMARY KEY (idvacacion);

-- Las columnas por las que el modulo une en cada pantalla.
CREATE INDEX ix_planvacacion_contratopuesto ON planvacacion (idcontractopuesto);
CREATE INDEX ix_gestionvacacion_plan ON gestionvacacion (idplanvacacion);
CREATE INDEX ix_vacacion_gestion ON vacacion (idgestionvacacion);


CREATE TABLE IF NOT EXISTS movimientovacacion (
  idmovimientovacacion BIGINT       NOT NULL,
  idplanvacacion       BIGINT       NOT NULL,
  tipo                 VARCHAR(15)  NOT NULL,
  fecha                DATE         NOT NULL,
  dias                 DECIMAL(7,2) NOT NULL,
  gestion              INT              NULL,
  idvacacion           BIGINT           NULL,
  descripcion          VARCHAR(250)     NULL,
  idusuario            BIGINT           NULL,
  fechacreacion        DATETIME     NOT NULL,
  idcompania           BIGINT       NOT NULL,
  version              BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (idmovimientovacacion),
  KEY ix_movvacacion_plan_fecha (idplanvacacion, fecha),
  KEY fk_movvacacion_vacacion (idvacacion),
  KEY fk_movvacacion_compania (idcompania),
  CONSTRAINT fk_movvacacion_plan FOREIGN KEY (idplanvacacion) REFERENCES planvacacion (idplanvacacion),
  CONSTRAINT fk_movvacacion_vacacion FOREIGN KEY (idvacacion) REFERENCES vacacion (idvacacion),
  CONSTRAINT fk_movvacacion_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO secuencia (tabla, valor)
SELECT 'movimientovacacion', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'movimientovacacion');


-- Medios dias: el informe de vacaciones usa 4,5 y 0,5. Con INT no entran.
ALTER TABLE vacacion        MODIFY totaldias    DECIMAL(7,2) NOT NULL DEFAULT 0;
ALTER TABLE vacacion        MODIFY diaslibres   DECIMAL(7,2) NOT NULL DEFAULT 0;
ALTER TABLE gestionvacacion MODIFY diasvacacion DECIMAL(7,2) NOT NULL DEFAULT 0;
ALTER TABLE gestionvacacion MODIFY diasusados   DECIMAL(7,2) NOT NULL DEFAULT 0;
ALTER TABLE gestionvacacion MODIFY diaslibres   DECIMAL(7,2) NOT NULL DEFAULT 0;
ALTER TABLE planvacacion    MODIFY diasvacacion DECIMAL(7,2) NOT NULL DEFAULT 0;
ALTER TABLE planvacacion    MODIFY diasusados   DECIMAL(7,2) NOT NULL DEFAULT 0;
ALTER TABLE planvacacion    MODIFY diaslibres   DECIMAL(7,2) NOT NULL DEFAULT 0;


-- Vacaciones anticipadas: hasta cuantos dias puede quedar el saldo por debajo de cero.
-- Cero = no se permiten, que es como se venia comportando.
ALTER TABLE reglavacacion ADD COLUMN diasanticipomaximo INT NOT NULL DEFAULT 0;


-- 10) Saldo inicial: desde cuando devenga el sistema ---------------------------
-- Nula = el sistema devenga desde el inicio del contrato y el saldo sale de cargar las
-- vacaciones consumidas. Con fecha = se declaro el saldo a esa fecha y el sistema no
-- devenga nada anterior, porque ese numero ya lo contiene.
ALTER TABLE planvacacion ADD COLUMN fechasaldoinicial DATE NULL;


-- 11) Regimen de aportes para quien no esta en el SIP --------------------------
-- Los cuatro regimenes existentes aportan al menos algo. Hace falta uno donde no
-- aporte nadie: ni el trabajador ni el empleador.

SET @nuevo_regimen = (SELECT MAX(idregimenaportesip) + 1 FROM regimenaportesip);
INSERT INTO regimenaportesip (idregimenaportesip, nombre, descripcion,
       aportacuentaindividual, aportariesgocomun, aportasolidario, aportacomision,
       aportanacsolidario, aportapatronal, aportacns, pordefecto, activo, idcompania, version)
SELECT @nuevo_regimen, 'Sin AFP',
       'No esta afiliado al Sistema Integral de Pensiones: no aporta el trabajador ni el empleador.',
       0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM regimenaportesip WHERE nombre = 'Sin AFP');
