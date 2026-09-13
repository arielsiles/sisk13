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


-- 12) Turnos (H1 del plan 03) --------------------------------------------------
-- Reemplaza a bandahoraria, que guardaba una fila por dia de la semana. Un turno es
-- uno solo y se usa el dia que haga falta. Que cruce la medianoche no se guarda: se
-- deduce de que la hora de fin no sea posterior a la de inicio.

CREATE TABLE IF NOT EXISTS turno (
  idturno            BIGINT       NOT NULL,
  nombre             VARCHAR(100) NOT NULL,
  horainicio         TIME         NOT NULL,
  horafin            TIME         NOT NULL,
  toleranciaentrada  INT          NOT NULL DEFAULT 0,
  toleranciasalida   INT          NOT NULL DEFAULT 0,
  activo             INT          NOT NULL DEFAULT 1,
  idcompania         BIGINT       NOT NULL,
  version            BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (idturno),
  KEY fk_turno_compania (idcompania),
  CONSTRAINT fk_turno_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO secuencia (tabla, valor)
SELECT 'turno', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'turno');

-- Permiso. Bitmask: VIEW=1, CREATE=2, UPDATE=4, DELETE=8. idmodulo = 4 (employees).
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'WORKSHIFT', 'Turnos de trabajo', 4, 15, 'Functionality.employees.workShift', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'WORKSHIFT');


-- 13) Grupos de trabajo (H2 del plan 03) ---------------------------------------
-- El cronograma se planifica por grupo, no por persona: dentro de un grupo todos
-- hacen el mismo turno. La pertenencia lleva vigencia porque para evaluar un dia
-- pasado hay que saber en que grupo estaba ESE dia, no en cual esta hoy.

CREATE TABLE IF NOT EXISTS grupotrabajo (
  idgrupotrabajo BIGINT       NOT NULL,
  nombre         VARCHAR(100) NOT NULL,
  area           VARCHAR(100)     NULL,
  activo         INT          NOT NULL DEFAULT 1,
  idcompania     BIGINT       NOT NULL,
  version        BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (idgrupotrabajo),
  KEY fk_grupotrabajo_compania (idcompania),
  CONSTRAINT fk_grupotrabajo_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS grupotrabajomiembro (
  idgrupotrabajomiembro BIGINT NOT NULL,
  idgrupotrabajo        BIGINT NOT NULL,
  idcontrato            BIGINT NOT NULL,
  fechainicio           DATE   NOT NULL,
  fechafin              DATE       NULL,
  idcompania            BIGINT NOT NULL,
  version               BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (idgrupotrabajomiembro),
  KEY ix_grupomiembro_contrato_fecha (idcontrato, fechainicio),
  KEY fk_grupomiembro_grupo (idgrupotrabajo),
  KEY fk_grupomiembro_compania (idcompania),
  CONSTRAINT fk_grupomiembro_grupo FOREIGN KEY (idgrupotrabajo) REFERENCES grupotrabajo (idgrupotrabajo),
  CONSTRAINT fk_grupomiembro_contrato FOREIGN KEY (idcontrato) REFERENCES contrato (idcontrato),
  CONSTRAINT fk_grupomiembro_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO secuencia (tabla, valor)
SELECT 'grupotrabajo', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'grupotrabajo');
INSERT INTO secuencia (tabla, valor)
SELECT 'grupotrabajomiembro', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'grupotrabajomiembro');

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'WORKGROUP', 'Grupos de trabajo', 4, 15, 'Functionality.employees.workGroup', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'WORKGROUP');


-- 14) Cronograma de turnos por grupo (H3 del plan 03) --------------------------
-- Una fila por grupo y dia. idturno nulo = ese dia no hay jornada. El estado vive
-- en el dia y no en una cabecera de semana: asi no existe una semana publicada a
-- medias. El motor de asistencia solo lee las filas PUBLISHED.

CREATE TABLE IF NOT EXISTS cronogramagrupodia (
  idcronogramagrupodia BIGINT      NOT NULL,
  idgrupotrabajo       BIGINT      NOT NULL,
  dia                  DATE        NOT NULL,
  idturno              BIGINT          NULL,
  estado               VARCHAR(15) NOT NULL DEFAULT 'DRAFT',
  idcompania           BIGINT      NOT NULL,
  version              BIGINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (idcronogramagrupodia),
  UNIQUE KEY uk_cronogramagrupodia (idgrupotrabajo, dia),
  KEY ix_cronogramagrupodia_dia (dia),
  KEY fk_cronogramadia_turno (idturno),
  KEY fk_cronogramadia_compania (idcompania),
  CONSTRAINT fk_cronogramadia_grupo FOREIGN KEY (idgrupotrabajo) REFERENCES grupotrabajo (idgrupotrabajo),
  CONSTRAINT fk_cronogramadia_turno FOREIGN KEY (idturno) REFERENCES turno (idturno),
  CONSTRAINT fk_cronogramadia_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO secuencia (tabla, valor)
SELECT 'cronogramagrupodia', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'cronogramagrupodia');

-- permiso = 5: solo VIEW (1) y UPDATE (4). No hay alta ni baja de celdas: se pintan.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'GROUPSCHEDULE', 'Cronograma de turnos', 4, 5, 'Functionality.employees.groupSchedule', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'GROUPSCHEDULE');


-- 15) Horario fijo del contrato (H7 del plan 03) -------------------------------
-- La semana tipo de quien no rota: administrativos y mantenimiento. Se guarda por
-- dia de la semana, no por fecha, porque no hay nada que planificar. idturno nulo
-- = ese dia de la semana no se trabaja. Lleva vigencia: cambiar el horario cierra
-- el anterior en lugar de pisarlo, para poder reproducir como se evaluo el pasado.

CREATE TABLE IF NOT EXISTS horariocontrato (
  idhorariocontrato BIGINT NOT NULL,
  idcontrato        BIGINT NOT NULL,
  diasemana         INT    NOT NULL,
  idturno           BIGINT     NULL,
  fechainicio       DATE   NOT NULL,
  fechafin          DATE       NULL,
  idcompania        BIGINT NOT NULL,
  version           BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (idhorariocontrato),
  KEY ix_horariocontrato_contrato_dia (idcontrato, diasemana, fechainicio),
  KEY fk_horariocontrato_turno (idturno),
  KEY fk_horariocontrato_compania (idcompania),
  CONSTRAINT fk_horariocontrato_contrato FOREIGN KEY (idcontrato) REFERENCES contrato (idcontrato),
  CONSTRAINT fk_horariocontrato_turno FOREIGN KEY (idturno) REFERENCES turno (idturno),
  CONSTRAINT fk_horariocontrato_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO secuencia (tabla, valor)
SELECT 'horariocontrato', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'horariocontrato');

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'CONTRACTSCHEDULE', 'Horario fijo', 4, 5, 'Functionality.employees.contractSchedule', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'CONTRACTSCHEDULE');


-- 16) Excepciones de horario (H8 del plan 03) ----------------------------------
-- A esta persona, este dia, le tocaba otra cosa. Gana sobre el cronograma del grupo
-- y sobre el horario fijo. idturno nulo = ese dia no se le evalua asistencia. El
-- motivo es obligatorio: una excepcion sin explicacion es indefendible.

CREATE TABLE IF NOT EXISTS excepcionhorario (
  idexcepcionhorario BIGINT       NOT NULL,
  idcontrato         BIGINT       NOT NULL,
  fecha              DATE         NOT NULL,
  idturno            BIGINT           NULL,
  motivo             VARCHAR(250) NOT NULL,
  idcompania         BIGINT       NOT NULL,
  version            BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (idexcepcionhorario),
  UNIQUE KEY uk_excepcionhorario (idcontrato, fecha),
  KEY ix_excepcionhorario_fecha (fecha),
  KEY fk_excepcionhorario_turno (idturno),
  KEY fk_excepcionhorario_compania (idcompania),
  CONSTRAINT fk_excepcionhorario_contrato FOREIGN KEY (idcontrato) REFERENCES contrato (idcontrato),
  CONSTRAINT fk_excepcionhorario_turno FOREIGN KEY (idturno) REFERENCES turno (idturno),
  CONSTRAINT fk_excepcionhorario_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO secuencia (tabla, valor)
SELECT 'excepcionhorario', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'excepcionhorario');

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'SCHEDULEEXCEPTION', 'Excepciones de horario', 4, 15, 'Functionality.employees.scheduleException', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'SCHEDULEEXCEPTION');


-- 17) Condicion del contrato: duracion y movimientos (plan 04) -----------------
-- La fecha de fin dejaba de ser un vencimiento administrativo que nadie renueva y
-- pasa a registrar cuando termino la relacion. La duracion es un eje aparte de la
-- modalidad: hay eventuales indefinidos y eventuales a plazo fijo.

ALTER TABLE contrato
  ADD COLUMN tipoduracion VARCHAR(15) NOT NULL DEFAULT 'INDEFINITE' AFTER fechafin;

-- Historial: sin esto, pasar de eventual a laboral seria una edicion silenciosa y
-- nadie podria decir desde cuando ni quien lo decidio.
CREATE TABLE IF NOT EXISTS movimientocontrato (
  idmovimientocontrato BIGINT       NOT NULL,
  idcontrato           BIGINT       NOT NULL,
  tipo                 VARCHAR(20)  NOT NULL,
  fecha                DATE         NOT NULL,
  idmodalidadanterior  BIGINT           NULL,
  idmodalidadnueva     BIGINT           NULL,
  duracionanterior     VARCHAR(15)      NULL,
  duracionnueva        VARCHAR(15)      NULL,
  fechafinanterior     DATE             NULL,
  fechafinnueva        DATE             NULL,
  motivo               VARCHAR(250) NOT NULL,
  idusuario            BIGINT           NULL,
  fecharegistro        DATETIME     NOT NULL,
  idcompania           BIGINT       NOT NULL,
  version              BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (idmovimientocontrato),
  KEY ix_movcontrato_contrato (idcontrato, fecha),
  KEY fk_movcontrato_modant (idmodalidadanterior),
  KEY fk_movcontrato_modnue (idmodalidadnueva),
  KEY fk_movcontrato_usuario (idusuario),
  KEY fk_movcontrato_compania (idcompania),
  CONSTRAINT fk_movcontrato_contrato FOREIGN KEY (idcontrato) REFERENCES contrato (idcontrato),
  CONSTRAINT fk_movcontrato_modant FOREIGN KEY (idmodalidadanterior) REFERENCES modalidadcontrato (idmodalidadcontrato),
  CONSTRAINT fk_movcontrato_modnue FOREIGN KEY (idmodalidadnueva) REFERENCES modalidadcontrato (idmodalidadcontrato),
  CONSTRAINT fk_movcontrato_usuario FOREIGN KEY (idusuario) REFERENCES usuario (idusuario),
  CONSTRAINT fk_movcontrato_compania FOREIGN KEY (idcompania) REFERENCES compania (idcompania)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO secuencia (tabla, valor)
SELECT 'movimientocontrato', 1 FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM secuencia s WHERE s.tabla = 'movimientocontrato');

-- La planilla congela la duracion junto a la modalidad: reimprimir un mes muestra la
-- condicion de ESE mes.
ALTER TABLE planillageneral
  ADD COLUMN duracioncontrato VARCHAR(255) NULL AFTER modalidadcontratacion;

SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'CONTRACTCONDITION', 'Condicion de contratos', 4, 5, 'Functionality.employees.contractCondition', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'CONTRACTCONDITION');


-- 18) Verificacion del control de asistencia ----------------------------------
-- Solo lectura: corre el mismo motor que la planilla pero no calcula ni guarda.
-- permiso = 1: solo VIEW.
SET @nuevo_id = (SELECT MAX(idfuncionalidad) + 1 FROM funcionalidad);
INSERT INTO funcionalidad (idfuncionalidad, codigo, descripcion, idmodulo, permiso, nombrerecurso, idcompania)
SELECT @nuevo_id, 'ATTENDANCECHECK', 'Verificacion de asistencia', 4, 1, 'Functionality.employees.attendanceCheck', 1
  FROM (SELECT 1) t
 WHERE NOT EXISTS (SELECT 1 FROM funcionalidad WHERE codigo = 'ATTENDANCECHECK');


-- 19) Movimientos entre grupos: prestamos y reorganizacion (plan 06) -----------
-- Un prestamo pisa a la pertenencia base sin borrarla y EXIGE fecha de fin: vencido
-- el plazo la persona vuelve a su grupo sola. Dos prestamos no pueden pisarse entre
-- si, pero un prestamo y la base si: esa es su razon de ser.

ALTER TABLE grupotrabajomiembro
  ADD COLUMN tipo VARCHAR(10) NOT NULL DEFAULT 'BASE' AFTER fechafin,
  ADD COLUMN motivo VARCHAR(250) NULL AFTER tipo;


-- 20) Contrato principal (plan 08) ---------------------------------------------
-- Una persona puede tener varios contratos a la vez: el principal lleva AFP y vacaciones,
-- los secundarios son trabajo eventual acotado y no arrastran ninguna de las dos cosas.
-- Entre los contratos abiertos puede haber uno principal O NINGUNO: quedarse solo con
-- eventuales es valido y el sistema no designa por su cuenta.

ALTER TABLE contrato
  ADD COLUMN principal INT NOT NULL DEFAULT 0 AFTER tipoduracion;

-- 20.1) Siembra. Hoy hay un contrato por persona, asi que todos son principales.
UPDATE contrato SET principal = 1;

-- 20.2) Verificacion. Esperado: ninguna persona con mas de un principal abierto.
-- SELECT c.idempleado, COUNT(*) principales
--   FROM contrato c
--   JOIN estadocontrato e ON e.idestadocontrato = c.idestadocontrato
--  WHERE c.principal = 1 AND UPPER(e.nombre) <> 'INACTIVO'
--  GROUP BY c.idempleado HAVING COUNT(*) > 1;


-- 21) Cierre del plan de vacaciones (plan 09) ----------------------------------
-- Un plan sin fecha de cierre devenga contra el dia de hoy y no mira el contrato: el de
-- alguien que se fue en 2024 seguiria sumando anios en 2030. La fecha de cierre es el
-- ultimo dia de trabajo, y el devengo cuenta hasta ahi.

ALTER TABLE planvacacion
  ADD COLUMN fechacierre DATE NULL AFTER fechainicio;

-- 21.1) Cierra los planes de contratos ya inactivos con la fecha de fin de su contrato.
--       Quedan con la antiguedad inflada hasta que el proceso de devengos los recalcule:
--       correr despues "Actualizar devengos" desde Planificacion de vacaciones.
UPDATE planvacacion p
   JOIN contratopuesto cp ON cp.idcontratopuesto = p.idcontractopuesto
   JOIN contrato c        ON c.idcontrato = cp.idcontrato
   JOIN estadocontrato ec ON ec.idestadocontrato = c.idestadocontrato
   SET p.fechacierre = c.fechafin
 WHERE p.fechacierre IS NULL
   AND UPPER(ec.nombre) = 'INACTIVO'
   AND c.fechafin IS NOT NULL;

-- 21.2) Verificacion. Esperado: ningun plan abierto de un contrato inactivo.
-- SELECT COUNT(*) FROM planvacacion p
--   JOIN contratopuesto cp ON cp.idcontratopuesto = p.idcontractopuesto
--   JOIN contrato c ON c.idcontrato = cp.idcontrato
--   JOIN estadocontrato ec ON ec.idestadocontrato = c.idestadocontrato
--  WHERE p.fechacierre IS NULL AND UPPER(ec.nombre) = 'INACTIVO';


-- 22) Ventana de atribucion del turno (plan 10) --------------------------------
-- Decide DE QUE JORNADA es una marca, que es otra cosa que las tolerancias: esas deciden
-- si la persona esta atrasada. Con los 10 minutos de tolerancia, una salida marcada 17:09
-- para un turno que termina 17:30 no perteneceria a ninguna jornada.
-- La ventana mide `duracion + antes + despues`, asi que con 2 y 4 horas queda por debajo
-- del doble del turno solo si el turno dura 6 horas o mas. Por eso la siembra mira la
-- duracion: los cortos llevan la mitad.

ALTER TABLE turno
  ADD COLUMN margenantes   INT NOT NULL DEFAULT 120 AFTER toleranciasalida,
  ADD COLUMN margendespues INT NOT NULL DEFAULT 240 AFTER margenantes;

-- 22.1) Siembra. Turnos de 6 horas o mas: 2 h y 4 h. Los cortos: 1 h y 2 h.
--       La duracion sale de las horas, contando el cruce de medianoche.
UPDATE turno
   SET margenantes = 60, margendespues = 120
 WHERE (CASE WHEN horafin > horainicio
             THEN TIMESTAMPDIFF(MINUTE, horainicio, horafin)
             ELSE TIMESTAMPDIFF(MINUTE, horainicio, horafin) + 24 * 60 END) < 6 * 60;

-- 22.2) Verificacion. Esperado: ninguna ventana de 24 h o mas, ninguna del doble del turno.
-- SELECT t.nombre, t.horainicio, t.horafin, t.margenantes, t.margendespues,
--        (CASE WHEN t.horafin > t.horainicio
--              THEN TIMESTAMPDIFF(MINUTE, t.horainicio, t.horafin)
--              ELSE TIMESTAMPDIFF(MINUTE, t.horainicio, t.horafin) + 24*60 END) duracion
--   FROM turno t;
