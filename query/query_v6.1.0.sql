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


-- 11) Codigo de marcacion de los empleados -------------------------------------
-- La base tenia el CI donde el dispositivo de marcado usa un id correlativo, por eso
-- ninguna marcacion cruzaba. Cruzado por nombre contra Reporte_Marcaciones_export.xlsx
-- (72 codigos); el generador es scripts/rh/map_mark_codes.py.
-- "VERIFICAR" marca los casos donde el nombre difiere en algunas letras entre el
-- dispositivo y la base (typos de un lado o del otro); estan revisados uno por uno.

-- ALEXANDER ALBERT ZEBALLOS RODRIGUEZ | archivo "Alexanderzeballosrodrig" | 597101 -> 1
UPDATE empleado SET codigomarcacion = '1' WHERE idempleado = 125;
-- NAYRA ANDREA MERCADO LEYVA | archivo "Nayramercadoleyva" | 8766753 -> 2
UPDATE empleado SET codigomarcacion = '2' WHERE idempleado = 45;
-- YOSELIN CHOQUE ROCHA | archivo "Yoselionchoqueroche" | 9352453 -> 5   <-- parecido 0.94, VERIFICAR
UPDATE empleado SET codigomarcacion = '5' WHERE idempleado = 49;
-- JOSE LUIS MENDOZA MAMANI | archivo "Josemendozamamani" | 8022544 -> 9
UPDATE empleado SET codigomarcacion = '9' WHERE idempleado = 9;
-- ADRIANA ALBORTA CRUZ | archivo "Adrianaalbortacruz" | 13160911 -> 13
UPDATE empleado SET codigomarcacion = '13' WHERE idempleado = 266;
-- MILTON SORIA CALVETI | archivo "Miltonsoriacalveti" | 985316 -> 14
UPDATE empleado SET codigomarcacion = '14' WHERE idempleado = 120;
-- PABLO FELIPE TUSCO . | archivo "Felipetusco" | 4390283 -> 16
UPDATE empleado SET codigomarcacion = '16' WHERE idempleado = 18;
-- DAVID ROSALES TORREZ | archivo "Davidrosales" | 13098059 -> 17
UPDATE empleado SET codigomarcacion = '17' WHERE idempleado = 19;
-- CESAR AUGUSTO TASTACA . | archivo "Cesartastaca" | 12490679 -> 18
UPDATE empleado SET codigomarcacion = '18' WHERE idempleado = 17;
-- SANDY MOLINA GRAGEDA | archivo "Sandymolinagrageda" | 3619885 -> 23
UPDATE empleado SET codigomarcacion = '23' WHERE idempleado = 12;
-- EDWIN VEIZAGA FLORES | archivo "Edwinveizagaflores" | 7468356 -> 24
UPDATE empleado SET codigomarcacion = '24' WHERE idempleado = 64;
-- ALEX CESPEDES LEON | archivo "Alexcespedesleon" | 8773887 -> 25
UPDATE empleado SET codigomarcacion = '25' WHERE idempleado = 15;
-- OSCAR PINTO LOPEZ | archivo "Oscarpintolopez" | 597103 -> 27
UPDATE empleado SET codigomarcacion = '27' WHERE idempleado = 127;
-- JUAN CARLOS PEREZ ROMERO | archivo "Juancarlosperezromero" | 336699 -> 28
UPDATE empleado SET codigomarcacion = '28' WHERE idempleado = 87;
-- HENRY MENDIETA FLORES | archivo "Henrymendietaflores" | 5756692 -> 29
UPDATE empleado SET codigomarcacion = '29' WHERE idempleado = 11;
-- OMAR SIMON LOPEZ | archivo "Omarsimonlopez" | 11092024 -> 30
UPDATE empleado SET codigomarcacion = '30' WHERE idempleado = 152;
-- VICTOR HUGO MOJICA JUSTINIANO | archivo "Victormojicajustiniano" | 653245 -> 32
UPDATE empleado SET codigomarcacion = '32' WHERE idempleado = 117;
-- CARLOS GUSTAVO PIZZA MAMANI | archivo "Gustavopizzamamani" | 9866975 -> 34
UPDATE empleado SET codigomarcacion = '34' WHERE idempleado = 30;
-- JUAN CARLOS ARUQUIPA CALLISAYA | archivo "Juanaraquipacallisaya" | 9873814 -> 35   <-- parecido 0.95, VERIFICAR
UPDATE empleado SET codigomarcacion = '35' WHERE idempleado = 164;
-- JUAN CARLOS TAPIA ADRIAN | archivo "Juancarlostapiaadrian" | 9448092 -> 36
UPDATE empleado SET codigomarcacion = '36' WHERE idempleado = 10;
-- VALERY JHOSSELINE BUSTAMANTE PAREDES | archivo "Valerybustamanteparedes" | 8796804 -> 37
UPDATE empleado SET codigomarcacion = '37' WHERE idempleado = 161;
-- FLORENCIO SACAICO MAIRE | archivo "Florenciosacaicomaire" | 8022789 -> 38
UPDATE empleado SET codigomarcacion = '38' WHERE idempleado = 66;
-- ALFONSO FLORES AVILA | archivo "Alfonsofloresavila" | 7918449 -> 40
UPDATE empleado SET codigomarcacion = '40' WHERE idempleado = 26;
-- SONIA RIVERA GONZALES | archivo "Soniariveragonzales" | 0 -> 42
UPDATE empleado SET codigomarcacion = '42' WHERE idempleado = 63;
-- DERICK GALVEZ MOLINA | archivo "Derickgalvezmolina" | 13098059 -> 44
UPDATE empleado SET codigomarcacion = '44' WHERE idempleado = 33;
-- JUAN CONDORI AYAVIRI | archivo "Juancondoriayaviri" | 7976949 -> 46
UPDATE empleado SET codigomarcacion = '46' WHERE idempleado = 67;
-- WILSON HEREDIA JUSTINIANO | archivo "Wilsonheriajustiniano" | 9275156 -> 48   <-- parecido 0.90, VERIFICAR
UPDATE empleado SET codigomarcacion = '48' WHERE idempleado = 20;
-- VICTOR COLQUE JORA | archivo "Victorcolquejora" | 9465721 -> 52
UPDATE empleado SET codigomarcacion = '52' WHERE idempleado = 82;
-- ELVIS OCTAVIO BUSTAMANTE MEJIA | archivo "Elvisbustamantemejia" | 9304252 -> 55
UPDATE empleado SET codigomarcacion = '55' WHERE idempleado = 177;
-- MANUEL JESUS MENDOZA SITA | archivo "Manuelmendozasita" | 634598 -> 57
UPDATE empleado SET codigomarcacion = '57' WHERE idempleado = 136;
-- DANIEL - DIAZ | archivo "Danieldiaz" | 5103940 -> 59
UPDATE empleado SET codigomarcacion = '59' WHERE idempleado = 221;
-- ORLANDO SALAZAR COTIMBO | archivo "Orlandosalazar" | 0 -> 64
UPDATE empleado SET codigomarcacion = '64' WHERE idempleado = 47;
-- ROGER CACERES CANO | archivo "Rogercacerescano" | 19092024 -> 65
UPDATE empleado SET codigomarcacion = '65' WHERE idempleado = 150;
-- JHON BRAYAN ACHOCALLA SARABIA | archivo "Jhonachocallasarabia" | 14722232 -> 69
UPDATE empleado SET codigomarcacion = '69' WHERE idempleado = 169;
-- NAYRA ESTEFANY SALGUERO PEREZ | archivo "Nayraestefanysalguerop" | 12400795 -> 71
UPDATE empleado SET codigomarcacion = '71' WHERE idempleado = 180;
-- FEDERICO DIONICIO TORREZ ALFARO | archivo "Federicotorrezalfaro" | 8795344 -> 74
UPDATE empleado SET codigomarcacion = '74' WHERE idempleado = 226;
-- KATHERINE ESTHER MERCADO CISNEROS | archivo "Katherinemercadocisn" | 5921959 -> 80
UPDATE empleado SET codigomarcacion = '80' WHERE idempleado = 224;
-- ADAN ALANIS YBANEZ | archivo "Adanalanisybanez" | 14382433 -> 84
UPDATE empleado SET codigomarcacion = '84' WHERE idempleado = 190;
-- MILTON QUIROZ JAILLITA | archivo "Miltonquirozjaillita" | 8855098 -> 85
UPDATE empleado SET codigomarcacion = '85' WHERE idempleado = 189;
-- ALVARO CORIA HERRERA | archivo "Alvarocoria" | 93299979 -> 86
UPDATE empleado SET codigomarcacion = '86' WHERE idempleado = 188;
-- JHONY ANTONIO MARCA MENECES | archivo "Jhonymarcameneces" | 12682638 -> 90
UPDATE empleado SET codigomarcacion = '90' WHERE idempleado = 192;
-- ELVIS NAVARRO SILOS | archivo "Elvisnavarrosilos" | 10546517 -> 93
UPDATE empleado SET codigomarcacion = '93' WHERE idempleado = 210;
-- INDALECIO ARANIBAR MAMANI | archivo "Indalecioaranibarma" | 4456932 -> 118
UPDATE empleado SET codigomarcacion = '118' WHERE idempleado = 244;
-- PABLO ANDRES SILVESTRE JORGE | archivo "Pablosilvestrejorge" | 13798744 -> 119
UPDATE empleado SET codigomarcacion = '119' WHERE idempleado = 248;
-- DANILEY SOLIS MIRANDA | archivo "Daanileysolism" | 8852738 -> 120   <-- parecido 0.93, VERIFICAR
UPDATE empleado SET codigomarcacion = '120' WHERE idempleado = 238;
-- NOELIA JUAN FERNADEZ | archivo "Noeliajuanfernandez" | 13128323 -> 121   <-- parecido 0.94, VERIFICAR
UPDATE empleado SET codigomarcacion = '121' WHERE idempleado = 246;
-- JHOSELYN QUELCA HERGUERO | archivo "Jhoselynquelcahelguero" | 8842160 -> 125   <-- parecido 0.95, VERIFICAR
UPDATE empleado SET codigomarcacion = '125' WHERE idempleado = 237;
-- ELVIS BRANEZ GARCIA | archivo "Elvisbranezgarcia" | 12998075 -> 128
UPDATE empleado SET codigomarcacion = '128' WHERE idempleado = 240;
-- DIONICIO INTURIAS GARCIA | archivo "Dioniciointuriasgarcia" | 8692324 -> 129
UPDATE empleado SET codigomarcacion = '129' WHERE idempleado = 242;
-- JOSE CARLOS LUIZAGA PENARRIETA | archivo "Joseluzagapenarieta" | 9306595 -> 131   <-- parecido 0.89, VERIFICAR
UPDATE empleado SET codigomarcacion = '131' WHERE idempleado = 260;
-- CRISTIAN CASTRO MENDEZ | archivo "Cristiancastromendez" | 9509229 -> 133
UPDATE empleado SET codigomarcacion = '133' WHERE idempleado = 259;
-- RUTH MARCELA PACO TERRAZAS | archivo "Ruthpacoterrazas" | 8738276 -> 135
UPDATE empleado SET codigomarcacion = '135' WHERE idempleado = 253;
-- GUILLERMO DARIO HUALLPA POMA | archivo "Guillermohuallpapoma" | 8818695  -> 136
UPDATE empleado SET codigomarcacion = '136' WHERE idempleado = 257;
-- MARCOS AQUINO LIMACHI | archivo "Marcosaquinolimachi" | 8507080 -> 137
UPDATE empleado SET codigomarcacion = '137' WHERE idempleado = 258;
-- MARY LIZETH MONTANO ENCINAS | archivo "Marymontanoencinas" | 9346346 -> 138
UPDATE empleado SET codigomarcacion = '138' WHERE idempleado = 255;
-- MARIELA RAMIREZ ROMAN | archivo "Marielaramirezroman" | 9388341 -> 139
UPDATE empleado SET codigomarcacion = '139' WHERE idempleado = 262;
-- MARIBEL ESPINOZA USNAYA | archivo "Maribelespiozausnaya" | 9468926 -> 140   <-- parecido 0.95, VERIFICAR
UPDATE empleado SET codigomarcacion = '140' WHERE idempleado = 256;
-- MIJHAEL ROSBERT LEDEZMA BLANCO | archivo "Rossbertledezmablanco" | 9345083 -> 141   <-- parecido 0.95, VERIFICAR
UPDATE empleado SET codigomarcacion = '141' WHERE idempleado = 265;
-- JORGE FELIPE PEREZ PAY | archivo "Jorgeperezpay" | 4828232 -> 142
UPDATE empleado SET codigomarcacion = '142' WHERE idempleado = 264;
-- JUAN EDINALDO FERNANDEZ MOLLO | archivo "Juanfernandezmollo" | 9333324 -> 143
UPDATE empleado SET codigomarcacion = '143' WHERE idempleado = 267;
-- ERNESTO ORTIZ DURAN | archivo "Ernestoortzduran" | 14437430 -> 144   <-- parecido 0.94, VERIFICAR
UPDATE empleado SET codigomarcacion = '144' WHERE idempleado = 273;
-- JUAN RAUL DURAN ORTIZ | archivo "Raulduranortiz" | 14861746 -> 145
UPDATE empleado SET codigomarcacion = '145' WHERE idempleado = 274;
-- ENRIQUE JHONNY MAMANI COLQUE | archivo "Enriquejhonnymamani" | 9534803 -> 146
UPDATE empleado SET codigomarcacion = '146' WHERE idempleado = 278;
-- JHIMY ONOFRE GUZMAN | archivo "Jimyonofreguzman" | 8671889 -> 147   <-- parecido 0.94, VERIFICAR
UPDATE empleado SET codigomarcacion = '147' WHERE idempleado = 272;
-- JHOANNA NICOLE TAPIA RIVAS | archivo "Nicoletapiarivas" | 8058846 -> 148
UPDATE empleado SET codigomarcacion = '148' WHERE idempleado = 275;
-- MARCO ANTONIO FLORES ARIAS | archivo "Marcofloresarias" | 7882689 -> 149
UPDATE empleado SET codigomarcacion = '149' WHERE idempleado = 276;
-- WILBER CALICHO CUBA | archivo "Wilbercalichocuba" | 13532411 -> 150
UPDATE empleado SET codigomarcacion = '150' WHERE idempleado = 277;
-- BENJAMIN - REVOLLO | archivo "Benjaminrevollo" | 553399 -> 151
UPDATE empleado SET codigomarcacion = '151' WHERE idempleado = 94;

-- Resueltos a mano:
-- IVAN CARLOS MAMANI CHOQUE cruza con dos registros con el mismo CI (8740168); el
-- id 165 esta rotulado "REPETIDO - NO USAR" en la propia base, asi que va al 163.
UPDATE empleado SET codigomarcacion = '61' WHERE idempleado = 163;
-- JESSICA MONTANO URIONA | archivo "Jesicamontanooriana" | 14149841 -> 39
-- (parecido 0.84: JESICA/JESSICA y ORIANA/URIONA. Es la unica MONTANO que puede ser.)
UPDATE empleado SET codigomarcacion = '39' WHERE idempleado = 162;

-- Quedan sin asignar: los codigos 152 "Jhonatanarnezbutron" y 153 "Weimarcallequispe"
-- no existen en `empleado` (no hay ningun BUTRON ni ningun WEIMAR). Hay que darlos de
-- alta. Sus marcaciones igual se importan y se vinculan solas cuando se los cree.


-- 12) Correccion de fecha de contrato -----------------------------------------
-- LUIS ALEJANDRO ROSELIO COLQUE tenia 0024-08-19 en lugar de 2024-08-19. Con esa fecha la
-- antiguedad daba dos mil anios y el plan de vacaciones le habria devengado un dia por anio.
-- Solo terdemol
UPDATE contrato SET fechainicio = '2024-08-19' WHERE idcontrato = 217;
