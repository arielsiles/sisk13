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

-- 6) Codigo de marcacion de los empleados -------------------------------------
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


-- 7) Motivo en fechas especiales (E1.3) ----------------------------------------
-- Hasta ahora una fecha especial solo decia si era con goce de haber y a quien aplicaba.
-- Sin motivo no hay forma de reportar por causa ni de que la maternidad no consuma
-- vacaciones. Queda nulo en las filas existentes: inferirlo hacia atras seria adivinar.

ALTER TABLE fechaespecial ADD COLUMN motivo VARCHAR(30) NULL;


-- 8) Jornada semanal por genero (E1.4) -----------------------------------------
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

-- `descripcion` lleva el mismo texto que muestra el sistema (el valor de la clave en
-- messages_app.properties), para poder ubicar el permiso por ese nombre. MARKIMPORT ya se
-- aplico antes con otro texto, se corrige.
UPDATE funcionalidad SET descripcion = 'Carga masiva de marcaciones' WHERE codigo = 'MARKIMPORT';
