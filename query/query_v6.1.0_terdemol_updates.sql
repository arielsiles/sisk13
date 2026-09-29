-- ============================================================================
-- query_v6.1.0_terdemol_updates.sql
-- ============================================================================
-- Actualizaciones de DATOS de esta empresa. Nada de estructura ni configuracion:
-- eso va en query_v6.1.0_terdemol.sql, que debe aplicarse primero.
--
-- Las secciones van en el orden en que se fueron necesitando.
-- ----------------------------------------------------------------------------


-- 1) Codigo de marcacion de los empleados --------------------------------------
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


-- 2) Correccion de fecha de contrato ------------------------------------------
-- LUIS ALEJANDRO ROSELIO COLQUE tenia 0024-08-19 en lugar de 2024-08-19. Con esa fecha la
-- antiguedad daba dos mil anios y el plan de vacaciones le habria devengado un dia por anio.
-- Solo terdemol
UPDATE contrato SET fechainicio = '2024-08-19' WHERE idcontrato = 217;

--
--


-- 3) Datos maestros desde la planilla oficial de julio 2026 -------------------
-- V1_PLANILLA GENERAL_07-2026_08-08-2026.xlsx, hoja 'V.1-PLLA GRAL COMPLETA- JULIO'.
-- La planilla es el dato oficial y gana ante lo que hay en la base.
-- El generador es scripts/rh/payroll_master_data.py.
--
-- La planilla trae el CI 5103940 repetido en dos personas. El de COLQUE JORA VICTOR
-- es 9465721 -ya correcto en la base- y el 5103940 queda para DIAZ DANIEL.

-- Cruzados 71 de la planilla | con AFP 12 | sin AFP 59 | sin cruzar 2

-- CI ---------------------------------------------------------------------
-- ALANIS YBAÑEZ ADAN: 14382433 -> 13382433
UPDATE entidad SET noidentificacion = '13382433' WHERE identidad = 190;
-- ALBORTA CRUZ ADRIANA: 13160911 -> 131660911
UPDATE entidad SET noidentificacion = '131660911' WHERE identidad = 266;
-- CONDORI  AYAVIRI  JUAN: 7976949 -> 7976343
UPDATE entidad SET noidentificacion = '7976343' WHERE identidad = 67;
-- GALVEZ GOMEZ JOSE LUIS: 54465 -> 2877001
UPDATE entidad SET noidentificacion = '2877001' WHERE identidad = 31;
-- GALVEZ MOLINA DERECK JOEL: 654654564 -> 4506990
UPDATE entidad SET noidentificacion = '4506990' WHERE identidad = 33;
-- REVOLLO  BENJAMIN: 16233066 -> 16232366
UPDATE entidad SET noidentificacion = '16232366' WHERE identidad = 94;
-- RIVERA  GONZALES SONIA: 0 -> 13418751
UPDATE entidad SET noidentificacion = '13418751' WHERE identidad = 63;
-- SALAZAR COTIMBO ORLANDO: 0 -> 5287397
UPDATE entidad SET noidentificacion = '5287397' WHERE identidad = 47;
-- VEIZAGA FLORES EDWIN: 7468356 -> 7468456
UPDATE entidad SET noidentificacion = '7468456' WHERE identidad = 64;

-- Fecha de nacimiento ----------------------------------------------------
UPDATE persona SET fechanacimiento = '2000-09-23' WHERE idpersona = 277;  -- CALICHO  CUBA  WILBER
UPDATE persona SET fechanacimiento = '1996-01-29' WHERE idpersona = 259;  -- CASTRO  MENDEZ  CRISTIAN
UPDATE persona SET fechanacimiento = '1986-04-11' WHERE idpersona = 15;  -- CESPEDES LEON ALEX
UPDATE persona SET fechanacimiento = '1996-02-05' WHERE idpersona = 67;  -- CONDORI  AYAVIRI  JUAN
UPDATE persona SET fechanacimiento = '1994-03-19' WHERE idpersona = 26;  -- FLORES AVILA ALFONSO
UPDATE persona SET fechanacimiento = '1959-05-13' WHERE idpersona = 31;  -- GALVEZ GOMEZ JOSE LUIS
UPDATE persona SET fechanacimiento = '1992-02-27' WHERE idpersona = 33;  -- GALVEZ MOLINA DERECK JOEL
UPDATE persona SET fechanacimiento = '1991-02-11' WHERE idpersona = 257;  -- HUALLPA  POMA  GUILLERMO DARIO
UPDATE persona SET fechanacimiento = '2003-09-05' WHERE idpersona = 242;  -- INTURIAS  GARCIA  DIONICIO
UPDATE persona SET fechanacimiento = '2026-03-22' WHERE idpersona = 224;  -- MERCADO CISNEROS KATHERINE ESTHER
UPDATE persona SET fechanacimiento = '1977-02-09' WHERE idpersona = 117;  -- MOJICA JUSTINIANO VICTOR HUGO
UPDATE persona SET fechanacimiento = '2025-10-25' WHERE idpersona = 162;  -- MONTAÑO URIONA JESSICA
UPDATE persona SET fechanacimiento = '2025-10-25' WHERE idpersona = 210;  -- NAVARRO  SILOS ELVIS
UPDATE persona SET fechanacimiento = '2000-02-11' WHERE idpersona = 189;  -- QUIROZ  JAILLITA  MILTON
UPDATE persona SET fechanacimiento = '1996-05-15' WHERE idpersona = 63;  -- RIVERA  GONZALES SONIA
UPDATE persona SET fechanacimiento = '1982-02-18' WHERE idpersona = 47;  -- SALAZAR COTIMBO ORLANDO
UPDATE persona SET fechanacimiento = '2003-01-08' WHERE idpersona = 152;  -- SIMON  LOPEZ  OMAR
UPDATE persona SET fechanacimiento = '1970-09-20' WHERE idpersona = 226;  -- TORREZ  ALFARO  FEDERICO DIONICIO
UPDATE persona SET fechanacimiento = '1983-06-24' WHERE idpersona = 64;  -- VEIZAGA FLORES EDWIN

-- Fecha de ingreso y sueldo basico, en el contrato -----------------------
UPDATE contrato SET fechainicio = '2026-04-01', haberbasicolaboral = 3300.00 WHERE idcontrato = 246;  -- ACHOCAYA  SARABIA  JHON BRAYAN
UPDATE contrato SET fechainicio = '2026-04-01', haberbasicolaboral = 3600.00 WHERE idcontrato = 267;  -- ALANIS YBAÑEZ ADAN
UPDATE contrato SET fechainicio = '2026-06-29', haberbasicolaboral = 750.00 WHERE idcontrato = 327;  -- ALBORTA CRUZ ADRIANA
UPDATE contrato SET fechainicio = '2026-05-20', haberbasicolaboral = 3300.00 WHERE idcontrato = 320;  -- AQUINO LIMACHI MARCOS
UPDATE contrato SET fechainicio = '2024-09-10', haberbasicolaboral = 4200.00 WHERE idcontrato = 244;  -- ARUQUIPA CALLISAYA JUAN CARLOS
UPDATE contrato SET fechainicio = '2026-05-11', haberbasicolaboral = 3300.00 WHERE idcontrato = 302;  -- BRAÑEZ  GARCIA  ELVIS
UPDATE contrato SET fechainicio = '2026-05-18', haberbasicolaboral = 3300.00 WHERE idcontrato = 254;  -- BUSTAMANTE  MEJIA  ELVIS OCTAVIO
UPDATE contrato SET fechainicio = '2025-02-03', haberbasicolaboral = 4200.00 WHERE idcontrato = 241;  -- BUSTAMANTE  PAREDES VALERY JHOSELINE
UPDATE contrato SET fechainicio = '2026-05-19', haberbasicolaboral = 3300.00 WHERE idcontrato = 234;  -- CACERES  CANO  ROGER
UPDATE contrato SET fechainicio = '2026-07-21', haberbasicolaboral = 3300.00 WHERE idcontrato = 334;  -- CALICHO  CUBA  WILBER
UPDATE contrato SET fechainicio = '2026-05-20', haberbasicolaboral = 1000.00 WHERE idcontrato = 321;  -- CASTRO  MENDEZ  CRISTIAN
UPDATE contrato SET fechainicio = '2023-08-01', haberbasicolaboral = 4350.00 WHERE idcontrato = 104;  -- CESPEDES LEON ALEX
UPDATE contrato SET fechainicio = '2024-01-26', haberbasicolaboral = 3750.00 WHERE idcontrato = 134;  -- CHOQUE ROCHA YOSELIN
UPDATE contrato SET fechainicio = '2026-05-08', haberbasicolaboral = 4250.00 WHERE idcontrato = 168;  -- COLQUE JORA VICTOR
UPDATE contrato SET fechainicio = '2026-05-11', haberbasicolaboral = 3500.00 WHERE idcontrato = 152;  -- CONDORI  AYAVIRI  JUAN
UPDATE contrato SET fechainicio = '2026-05-09', haberbasicolaboral = 3300.00 WHERE idcontrato = 265;  -- CORIA HERRERA ALVARO
UPDATE contrato SET fechainicio = '2025-09-16', haberbasicolaboral = 3300.00 WHERE idcontrato = 290;  -- DIAZ DANIEL
UPDATE contrato SET fechainicio = '2026-07-06', haberbasicolaboral = 3300.00 WHERE idcontrato = 331;  -- DURAN  ORTIZ  JUAN RAUL
UPDATE contrato SET fechainicio = '2026-05-20', haberbasicolaboral = 3300.00 WHERE idcontrato = 318;  -- ESPINOZA  USNAYA MARIBEL
UPDATE contrato SET fechainicio = '2026-07-01', haberbasicolaboral = 3300.00 WHERE idcontrato = 328;  -- FERNANDEZ  MOLLO  JUAN EDINALDO
UPDATE contrato SET fechainicio = '2026-07-21', haberbasicolaboral = 3300.00 WHERE idcontrato = 333;  -- FLORES  ARIAS  MARCO ANTONIO
UPDATE contrato SET fechainicio = '2023-10-13', haberbasicolaboral = 3910.00 WHERE idcontrato = 113;  -- FLORES AVILA ALFONSO
UPDATE contrato SET fechainicio = '2021-09-25', haberbasicolaboral = 18000.00 WHERE idcontrato = 123;  -- GALVEZ GOMEZ JOSE LUIS
UPDATE contrato SET fechainicio = '2021-09-25', haberbasicolaboral = 18000.00 WHERE idcontrato = 125;  -- GALVEZ MOLINA DERECK JOEL
UPDATE contrato SET fechainicio = '2024-01-01', haberbasicolaboral = 4250.00 WHERE idcontrato = 109;  -- HEREDIA JUSTINIANO WILSON
UPDATE contrato SET fechainicio = '2026-05-20', haberbasicolaboral = 3300.00 WHERE idcontrato = 319;  -- HUALLPA  POMA  GUILLERMO DARIO
UPDATE contrato SET fechainicio = '2026-05-11', haberbasicolaboral = 3300.00 WHERE idcontrato = 304;  -- INTURIAS  GARCIA  DIONICIO
UPDATE contrato SET fechainicio = '2026-04-13', haberbasicolaboral = 750.00 WHERE idcontrato = 308;  -- JUAN  FERNANDEZ NOELIA
UPDATE contrato SET fechainicio = '2026-06-01', haberbasicolaboral = 750.00 WHERE idcontrato = 326;  -- LEDEZMA BLANCO MIJHAEL ROSBERT
UPDATE contrato SET fechainicio = '2026-05-19', haberbasicolaboral = 3300.00 WHERE idcontrato = 322;  -- LUIZAGA PEÑARRIETA JOSE CARLOS
UPDATE contrato SET fechainicio = '2025-02-13', haberbasicolaboral = 3300.00 WHERE idcontrato = 243;  -- MAMANI CHOQUE IVAN CARLOS
UPDATE contrato SET fechainicio = '2026-07-09', haberbasicolaboral = 3300.00 WHERE idcontrato = 335;  -- MAMANI COLQUE ENRIQUE JHONNY
UPDATE contrato SET fechainicio = '2026-03-11', haberbasicolaboral = 3900.00 WHERE idcontrato = 269;  -- MARCA  MENECES JHONY ANTONIO
UPDATE contrato SET fechainicio = '2023-09-01', haberbasicolaboral = 3910.00 WHERE idcontrato = 111;  -- MENDIETA FLORES HENRY
UPDATE contrato SET fechainicio = '2026-05-09', haberbasicolaboral = 3300.00 WHERE idcontrato = 97;  -- MENDOZA MAMANI JOSE LUIS
UPDATE contrato SET fechainicio = '2024-06-07', haberbasicolaboral = 3300.00 WHERE idcontrato = 216;  -- MENDOZA SITA MANUEL JESUS
UPDATE contrato SET fechainicio = '2026-03-10', haberbasicolaboral = 3300.00 WHERE idcontrato = 292;  -- MERCADO CISNEROS KATHERINE ESTHER
UPDATE contrato SET fechainicio = '2024-01-18', haberbasicolaboral = 4990.00 WHERE idcontrato = 131;  -- MERCADO LEYVA NAYRA ANDREA
UPDATE contrato SET fechainicio = '2024-07-17', haberbasicolaboral = 3750.00 WHERE idcontrato = 189;  -- MIRANDA APAZA ALEJANDRA KATYA
UPDATE contrato SET fechainicio = '2024-07-25', haberbasicolaboral = 3300.00 WHERE idcontrato = 203;  -- MOJICA JUSTINIANO VICTOR HUGO
UPDATE contrato SET fechainicio = '2023-09-08', haberbasicolaboral = 3910.00 WHERE idcontrato = 112;  -- MOLINA GRAGEDA SANDY
UPDATE contrato SET fechainicio = '2026-05-20', haberbasicolaboral = 3300.00 WHERE idcontrato = 317;  -- MONTAÑO ENCINAS MARY LISETH
UPDATE contrato SET fechainicio = '2026-03-30', haberbasicolaboral = 3500.00 WHERE idcontrato = 242;  -- MONTAÑO URIONA JESSICA
UPDATE contrato SET fechainicio = '2025-07-20', haberbasicolaboral = 3550.00 WHERE idcontrato = 282;  -- NAVARRO  SILOS ELVIS
UPDATE contrato SET fechainicio = '2026-07-15', haberbasicolaboral = 750.00 WHERE idcontrato = 329;  -- ONOFRE  GUZMAN  JHIMY
UPDATE contrato SET fechainicio = '2026-07-06', haberbasicolaboral = 3300.00 WHERE idcontrato = 330;  -- ORTIZ  DURAN  ERNESTO
UPDATE contrato SET fechainicio = '2026-05-21', haberbasicolaboral = 3300.00 WHERE idcontrato = 315;  -- PACO TERRAZAS RUTH MARCELA
UPDATE contrato SET fechainicio = '2026-06-09', haberbasicolaboral = 4000.00 WHERE idcontrato = 325;  -- PEREZ PAY JORGE FELIPE
UPDATE contrato SET fechainicio = '2024-06-11', haberbasicolaboral = 4200.00 WHERE idcontrato = 173;  -- PEREZ ROMERO JUAN CARLOS
UPDATE contrato SET fechainicio = '2024-09-19', haberbasicolaboral = 3800.00 WHERE idcontrato = 212;  -- PINTO LOPEZ OSCAR
UPDATE contrato SET fechainicio = '2023-10-18', haberbasicolaboral = 5100.00 WHERE idcontrato = 119;  -- PIZZA MAMANI CARLOS GUSTAVO
UPDATE contrato SET fechainicio = '2026-04-24', haberbasicolaboral = 750.00 WHERE idcontrato = 301;  -- QUELCA HELGUERO JHOSELYN
UPDATE contrato SET fechainicio = '2026-05-09', haberbasicolaboral = 3300.00 WHERE idcontrato = 266;  -- QUIROZ  JAILLITA  MILTON
UPDATE contrato SET fechainicio = '2026-05-21', haberbasicolaboral = 750.00 WHERE idcontrato = 324;  -- RAMIREZ ROMAN MARIELA
UPDATE contrato SET fechainicio = '2026-07-29', haberbasicolaboral = 3300.00 WHERE idcontrato = 180;  -- REVOLLO  BENJAMIN
UPDATE contrato SET fechainicio = '2024-05-01', haberbasicolaboral = 3300.00 WHERE idcontrato = 148;  -- RIVERA  GONZALES SONIA
UPDATE contrato SET fechainicio = '2024-06-05', haberbasicolaboral = 3700.00 WHERE idcontrato = 108;  -- ROSALES TORREZ DAVID
UPDATE contrato SET fechainicio = '2024-04-20', haberbasicolaboral = 3750.00 WHERE idcontrato = 151;  -- SACAICO MAIRE FLORENCIO
UPDATE contrato SET fechainicio = '2025-09-01', haberbasicolaboral = 5000.00 WHERE idcontrato = 133;  -- SALAZAR COTIMBO ORLANDO
UPDATE contrato SET fechainicio = '2026-03-02', haberbasicolaboral = 3650.00 WHERE idcontrato = 257;  -- SALGUERO PEREZ NAYRA ESTEFANY
UPDATE contrato SET fechainicio = '2026-03-24', haberbasicolaboral = 3919.43 WHERE idcontrato = 310;  -- SILVESTRE JORGE PABLO ANDRES
UPDATE contrato SET fechainicio = '2026-06-26', haberbasicolaboral = 3300.00 WHERE idcontrato = 236;  -- SIMON  LOPEZ  OMAR
UPDATE contrato SET fechainicio = '2026-04-07', haberbasicolaboral = 750.00 WHERE idcontrato = 307;  -- SOLIS MIRANDA DANILEY
UPDATE contrato SET fechainicio = '2024-08-24', haberbasicolaboral = 3800.00 WHERE idcontrato = 206;  -- SORIA CALVETI MILTON
UPDATE contrato SET fechainicio = '2026-07-21', haberbasicolaboral = 750.00 WHERE idcontrato = 332;  -- TAPIA  RIVAS  JHOANNA NICOLE
UPDATE contrato SET fechainicio = '2023-05-02', haberbasicolaboral = 4225.00 WHERE idcontrato = 100;  -- TAPIA ADRIAN JUAN CARLOS
UPDATE contrato SET fechainicio = '2023-05-30', haberbasicolaboral = 4750.00 WHERE idcontrato = 106;  -- TASTACA CESAR AUGUSTO
UPDATE contrato SET fechainicio = '2026-05-18', haberbasicolaboral = 3300.00 WHERE idcontrato = 294;  -- TORREZ  ALFARO  FEDERICO DIONICIO
UPDATE contrato SET fechainicio = '2023-08-01', haberbasicolaboral = 3700.00 WHERE idcontrato = 107;  -- TUSCO PABLO FELIPE
UPDATE contrato SET fechainicio = '2024-04-14', haberbasicolaboral = 4250.00 WHERE idcontrato = 149;  -- VEIZAGA FLORES EDWIN
UPDATE contrato SET fechainicio = '2024-09-20', haberbasicolaboral = 5849.53 WHERE idcontrato = 210;  -- ZEBALLOS RODRIGUEZ ALEXANDER ALBERT

-- Regimen de aportes -----------------------------------------------------
-- Con AFP (12): aportan trabajador y empleador.
UPDATE contrato SET idregimenaportesip = (SELECT idregimenaportesip FROM regimenaportesip WHERE pordefecto = 1)
 WHERE idcontrato IN (104, 134, 131, 112, 119, 108, 310, 206, 100, 107, 106, 210);
-- Sin AFP (59): no aporta nadie.
UPDATE contrato SET idregimenaportesip = (SELECT idregimenaportesip FROM regimenaportesip WHERE nombre = 'Sin AFP')
 WHERE idcontrato IN (246, 267, 327, 320, 244, 302, 254, 241, 234, 334, 321, 168, 152, 265, 290, 331, 318, 328, 333, 113, 123, 125, 109, 319, 304, 308, 326, 322, 335, 243, 269, 111, 97, 216, 292, 189, 203, 317, 242, 282, 329, 330, 315, 325, 173, 212, 301, 266, 324, 180, 148, 151, 133, 257, 236, 307, 332, 294, 149);


-- Duplicados: se marca el segundo apellido del que NO tiene contrato -------
-- No se borran: eliminarlos exige revisar antes las referencias de otros modulos
-- -asientos, ordenes de compra, vales, usuarios- y reasignarlas al que sobrevive.
-- IVAN CARLOS MAMANI CHOQUE: manda el id 163, se marca el id 165
UPDATE persona SET apellidomaterno = CONCAT(IFNULL(apellidomaterno, ''), ' DUPLICA')
 WHERE idpersona = 165 AND apellidomaterno NOT LIKE '%DUPLICA';
-- JUAN LUIS COCA COLQUE: manda el id 191, se marca el id 201
UPDATE persona SET apellidomaterno = CONCAT(IFNULL(apellidomaterno, ''), ' DUPLICA')
 WHERE idpersona = 201 AND apellidomaterno NOT LIKE '%DUPLICA';
-- ELIANA TRUJILLO: manda el id 228, se marca el id 227
UPDATE persona SET apellidomaterno = CONCAT(IFNULL(apellidomaterno, ''), ' DUPLICA')
 WHERE idpersona = 227 AND apellidomaterno NOT LIKE '%DUPLICA';
-- JOSE LUIS GALVEZ GOMEZ: manda el id 31, se marca el id 4
UPDATE persona SET apellidomaterno = CONCAT(IFNULL(apellidomaterno, ''), ' DUPLICA')
 WHERE idpersona = 4 AND apellidomaterno NOT LIKE '%DUPLICA';
-- LUIS GALVEZ MOLINA: manda el id 35, se marca el id 32
UPDATE persona SET apellidomaterno = CONCAT(IFNULL(apellidomaterno, ''), ' DUPLICA')
 WHERE idpersona = 32 AND apellidomaterno NOT LIKE '%DUPLICA';


-- 4) Condicion de los contratos (plan 04) -------------------------------------
-- Antes: 234 contratos, NINGUNO sin fecha de fin, y 148 que decian ACTIVO y vencido
-- a la vez. La fecha se usaba como vencimiento administrativo que nadie renovaba.
--
-- Regla del usuario: manda la planilla oficial de julio 2026. Los 71 que estan en
-- ella quedan activos; con AFP son LABORAL y el resto EVENTUAL; TODOS indefinidos y
-- sin fecha de fin. Los 115 activos que no estan en esa planilla se inactivan.
--
-- DEPENDE de la seccion 17 de query_v6.1.0_terdemol.sql, que crea contrato.tipoduracion.
-- Si falta, lo de abajo corta con un error que dice que hacer, en lugar de fallar a la
-- mitad y dejar los datos migrados por partes.
SET @falta_columna = (SELECT COUNT(*) = 0 FROM information_schema.COLUMNS
                       WHERE TABLE_SCHEMA = DATABASE()
                         AND TABLE_NAME = 'contrato'
                         AND COLUMN_NAME = 'tipoduracion');
SET @chequeo = IF(@falta_columna,
  'SELECT * FROM `EJECUTE_PRIMERO_LA_SECCION_17_DE_query_v6_1_0_terdemol_sql`',
  'SELECT 1');
PREPARE verificacion FROM @chequeo;
EXECUTE verificacion;
DEALLOCATE PREPARE verificacion;

-- Los 71 contratos de la planilla oficial de julio 2026.
SET @planilla_julio = '97,100,104,106,107,108,109,111,112,113,119,123,125,131,133,134,148,149,151,152,168,173,180,189,203,206,210,212,216,234,236,241,242,243,244,246,254,257,265,266,267,269,282,290,292,294,301,302,304,307,308,310,315,317,318,319,320,321,322,324,325,326,327,328,329,330,331,332,333,334,335';

-- 4.1) Todos los de la planilla: indefinidos y sin fecha de fin.
UPDATE contrato
   SET tipoduracion = 'INDEFINITE', fechafin = NULL
 WHERE FIND_IN_SET(idcontrato, @planilla_julio);

-- 4.2) Con AFP -regimen SIP- son LABORAL. Son 12, e incluye a SILVESTRE JORGE PABLO
--      ANDRES (contrato 310), el unico EVENTUAL que aportaba.
UPDATE contrato
   SET idmodalidadcontrato = (SELECT idmodalidadcontrato FROM modalidadcontrato WHERE nombre = 'LABORAL')
 WHERE FIND_IN_SET(idcontrato, @planilla_julio)
   AND idregimenaportesip IS NOT NULL;

-- 4.3) El resto de la planilla es EVENTUAL. Son 59; 17 vienen de LABORAL.
UPDATE contrato
   SET idmodalidadcontrato = (SELECT idmodalidadcontrato FROM modalidadcontrato WHERE nombre = 'EVENTUAL')
 WHERE FIND_IN_SET(idcontrato, @planilla_julio)
   AND idregimenaportesip IS NULL;

-- 4.4) Los de la planilla quedan activos y dentro de la generacion de planillas.
UPDATE contrato
   SET idestadocontrato = (SELECT idestadocontrato FROM estadocontrato WHERE nombre = 'ACTIVO'),
       activogenplan = 1
 WHERE FIND_IN_SET(idcontrato, @planilla_julio);

-- 4.5) Los 115 activos que NO estan en la planilla: fuera. Ninguno tiene AFP.
--      Se les apaga activogenplan en lugar de escribirles una fecha de fin, y es la
--      unica vez que se hace asi: no sabemos cuando se fueron, y una fecha de salida
--      inventada es peor que ninguna. Quien conozca la real registra la baja.
UPDATE contrato c
   JOIN estadocontrato e ON e.idestadocontrato = c.idestadocontrato
   SET c.idestadocontrato = (SELECT idestadocontrato FROM estadocontrato WHERE nombre = 'INACTIVO'),
       c.activogenplan = 0,
       c.tipoduracion = 'INDEFINITE',
       c.fechafin = NULL
 WHERE e.nombre = 'ACTIVO'
   AND NOT FIND_IN_SET(c.idcontrato, @planilla_julio);

-- 4.6) Verificacion. Esperado: 12 LABORAL y 59 EVENTUAL activos, 0 con fecha de fin.
-- SELECT m.nombre, COUNT(*) n, SUM(c.fechafin IS NOT NULL) con_fecha_fin
--   FROM contrato c
--   JOIN modalidadcontrato m ON m.idmodalidadcontrato = c.idmodalidadcontrato
--   JOIN estadocontrato e ON e.idestadocontrato = c.idestadocontrato
--  WHERE e.nombre = 'ACTIVO' GROUP BY m.nombre;


-- ===========================================================================
-- PENDIENTES - no se tocan, quedan para decidir
-- ===========================================================================
--
-- 1) En la planilla oficial pero NO existen en el sistema. Hay que darlos de alta:
--    CI 8806374 CB     ARNEZ  BUTRON  JHONATAN NELSON
--    CI 8020013 CB     CALLE  QUISPE  WEIMAR JOEL
--
-- 2) Un mismo CI en dos personas DISTINTAS. No son duplicados: el CI esta mal
--    cargado en alguno de los dos. Ambos tienen contrato.
--    CI 7951447    id 5    FRANZ LUIS CHANEZ HUALLATA       | id 13   JORGE ERICK ALMANZA QUIROZ
--    CI 6550221    id 23   BENJAMIN PELAEZ FLORES           | id 24   NAHUEL EINAR FERNANDEZ VARGAS
--    CI 9304252    id 156  ELMER BUSTAMANTE MEJIA           | id 177  ELVIS OCTAVIO BUSTAMANTE MEJIA
--    ALMANZA QUIROZ y ELVIS OCTAVIO BUSTAMANTE si estan en la planilla y su CI se
--    corrige mas arriba; los otros cuatro quedan compartiendo el CI.
--
-- 3) `empleado.flagafp` y `contrato.activofonpension` quedan como estan. Se
--    verifico que NINGUN calculo los lee: el aporte al SIP se resuelve solo por el
--    regimen del contrato. Revisarlos y quitarlos si no cumplen otra funcion.
--
-- 4) Los marcados con DUPLICA siguen existiendo. Para eliminarlos hay que revisar
--    sus referencias en los demas modulos y reasignarlas al que sobrevive.
--
-- 5) Finiquito al dar de baja. El modulo de bajas existe con sus reglas
--    (`DismissalRule`) pero tiene 0 registros y nadie lo usa. La baja del plan 04
--    registra la salida y escribe la fecha de fin, pero NO calcula finiquito.
--    Revisar ese modulo aparte y definir si se integra, se reescribe o se descarta.
--
-- 6) PENDIENTE. Los 115 contratos que la seccion 4.5 inactiva no tienen fecha de fin
--    real: no se sabe cuando se fueron. La seccion 5 los cierra con su misma fecha de
--    inicio para que dejen de figurar abiertos, pero esa fecha es de relleno, no un
--    dato. Quien conozca la salida real de cada uno la corrige registrando la baja
--    desde Planificacion > Condicion de contratos.


-- ===========================================================================
-- SECCION 5 - Cierre de los contratos inactivos sin fecha de fin
-- ===========================================================================

-- 5.1) Cuantos son. Esperado antes de correr 5.2: 115.
-- SELECT COUNT(*) FROM contrato c
--   JOIN estadocontrato e ON e.idestadocontrato = c.idestadocontrato
--  WHERE e.nombre = 'INACTIVO' AND c.fechafin IS NULL;

-- 5.2) Sin fecha de fin el contrato figura abierto: el filtro "Vigente en" los cuenta hoy.
UPDATE contrato c
   JOIN estadocontrato e ON e.idestadocontrato = c.idestadocontrato
   SET c.fechafin = c.fechainicio
 WHERE e.nombre = 'INACTIVO'
   AND c.fechafin IS NULL;

-- 5.3) La fecha de salida de la persona, con la misma regla que aplica el sistema de aca en
--      mas: se fue el que no tiene NINGUN contrato abierto, y la fecha es la mayor fecha de
--      fin. Quien tenga alguno abierto queda sin fecha, aunque la tuviera puesta.
--      Idempotente: se puede volver a correr. A quien no tiene contratos no se lo toca.
UPDATE empleado e
   JOIN (SELECT c.idempleado,
                SUM(CASE WHEN UPPER(ec.nombre) = 'INACTIVO' THEN 0 ELSE 1 END) abiertos,
                MAX(c.fechafin) ultimofin
           FROM contrato c
           JOIN estadocontrato ec ON ec.idestadocontrato = c.idestadocontrato
          GROUP BY c.idempleado) x ON x.idempleado = e.idempleado
   SET e.fechasalida = CASE WHEN x.abiertos > 0 THEN NULL ELSE x.ultimofin END;

-- 5.4) Verificacion. Esperado: 0 inactivos sin fecha de fin, y los 70 activos sin ella intactos.
-- SELECT e.nombre, COUNT(*) n, SUM(c.fechafin IS NULL) sin_fecha_fin
--   FROM contrato c
--   JOIN estadocontrato e ON e.idestadocontrato = c.idestadocontrato
--  GROUP BY e.nombre;
-- SELECT COUNT(*) FROM empleado WHERE fechasalida IS NULL;  -- esperado: 78
