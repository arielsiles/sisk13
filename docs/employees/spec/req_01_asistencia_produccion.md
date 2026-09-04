# SPEC 01 — Control de asistencia y planilla para personal de producción

> **Estado:** borrador para aprobación. Ninguna línea de código escrita a partir de este
> documento. Fuente: `tmp/rh/req_01.txt` y los documentos operativos de esa carpeta.

## 1. Objetivo

Automatizar el control de asistencia y su bajada a la planilla de sueldos para **personal
operativo de producción que trabaja en grupos con turnos rotativos de 12 horas**, hoy
llevado a mano en planillas Excel.

**Hoy armar el reporte mensual de asistencia lleva días.** Se consolida a mano, cruzando la
grilla de marcaciones con permisos, descansos, horas acumuladas y vacaciones, en varios
archivos separados. Ése es el costo que se busca eliminar.

El resultado esperado no es una pantalla más: es que ese reporte **salga de las operaciones
del mes**, no de rehacerlo cada vez.

## 2. Alcance

**Entra:**

| Sector | Situación |
|---|---|
| Producción (operativo) | Grupos, turnos rotativos día/noche, horas acumuladas |
| Administrativo financiero | Horario fijo, ya funciona; se beneficia de las reglas nuevas |

**No entra, y no se toca:**

- **El sector ACADÉMICO.** `fillProffesorsPayroll`, `fillFiscalProfessorPayroll`,
  `executeAttendanceControlProffesors` y `executeAttendanceControlFiscalProffesors` quedan
  exactamente como están. Es código viejo, sin uso, y no se considera para nada en este SPEC.
- El cálculo de aportes al SIP, RC‑IVA y planilla fiscal, ya resuelto en la v6.0.129.

## 3. Principios

### 3.1 No perder lo que ya funciona

**Esto ya está en producción en varias empresas.** Hay años de planillas generadas y
oficializadas con el motor actual. Toda regla nueva debe ser:

- **configurable**, no cableada — la lección de los carnets en `RetentionAFPCalculator`;
- **opt‑in**, de modo que un contrato que no la use calcule exactamente igual que hoy;
- **verificable** regenerando un mes cerrado en modo PRUEBA y comparándolo contra el oficial.

### 3.2 Multiempresa por diseño

**Nada de lo que se construya puede quedar atado a una empresa.** Todo lo que varíe entre
clientes va a configuración: horas semanales, tolerancias, tramos de descuento, base de
cálculo, turnos, grupos.

Esto no es un deseo genérico: es lo que se rompió antes. Los porcentajes y carnets escritos
en el código obligaron a mantener ramas distintas por cliente y a corregir planillas a mano.
La regla práctica es que **si un valor puede ser distinto en otra empresa, es dato, no
código**.

## 4. La operación real, según los documentos

### 4.1 Cronograma de grupos

De `CRONOGRAMA DE GRUPOS PRODUCCION.xlsx`, hoja *PROD ‑ JULIO 2026*. Es una grilla
**grupo × día del mes**, donde la celda lleva las horas y **el color indica el turno**:
amarillo = día, sin color = noche.

```
        13 14 15 16 17 | 18 19 | 20 21 22 23 24 | 25 26 | 27 28 29 30 31
Grupo 1  6 12 12 12  6 |  8  8 | 12 12 12 12 12 |  8  8 |  6 12 12 12  6
         └── NOCHE ──┘         └───── DÍA ─────┘        └── NOCHE ────┘
Grupo 2 12 12 12 12 12 |  8  8 |  6 12 12 12  6 |  8  8 | 12 12 12 12 12
        └─── DÍA ────┘         └──── NOCHE ────┘        └──── DÍA ────┘
```

- La **rotación es semanal** y los grupos van en contrafase.
- El turno noche produce **6 h el primer día y 6 h el último**: es el reparto de la jornada
  que cruza medianoche.
- Los totales del mes se calculan por grupo: Grupo 1 = 156 h = 19,5 días; Grupo 2 = 168 h = 21 días.
- Además se cuentan **descansos efectivos** (32 h = 4 días por grupo).
- La hoja *PERSONAL OPERATIVO (RESUMEN* contempla **hasta 4 grupos**.

### 4.2 Conversión horas → días

La planilla paga sobre 30 días, pero producción trabaja por horas. La hoja
*PERSONAL OPERATIVO OPCION 1* muestra la conversión que hoy se hace a mano:

```
TOTAL DÍAS TRABAJADOS      27
TOTAL DOMINGOS/FERIADOS  +  1
                         ────
EQUIVALENTE EN DÍAS EFECTIVOS  28
```

Y el reporte de asistencia de julio 2026 lo confirma por empleado: `272 horas trabajadas`
→ `33 días trabajados efectivos`.

### 4.3 Horas acumuladas (banco de horas)

Dos piezas:

- **Registro por evento** — formulario `PA.01.R10`: fecha, hora de inicio, hora de
  finalización, horas trabajadas, firma del trabajador, firma del responsable y VB° de RR.HH.
  Debe llegar a RR.HH. en **≤ 48 horas**.
- **Kardex por empleado** — columnas `ACUMULO` / `USO` por mes, con saldo corrido que cruza
  gestiones. Los valores están **en días**, con fracciones: 0,375 · 0,5 · 1,25 · 2,875 · 8,5.

El saldo se consume como permiso en cualquier momento.

### 4.4 Cierre mensual (procedimiento PA.01, sección 6.8)

1. El encargado de asistencia en planta cierra el mes en el registro `PA.01.R11`.
2. Envía a RR.HH. por email **con los respaldos**: permisos, descansos, compensatorios.
3. Envía el físico firmado por él y por el responsable de planta.
4. **Los ajustes se hacen entre el 1 y el 7 de cada mes.** Lo que aparezca después se
   corrige en el mes siguiente.

Formularios relacionados: `R09` movimientos de RRHH · `R10` horas extras ·
`R11` asistencia · `R12` pase de servicio · `R13` desmovilización · `R14` personal externo ·
`R15` descansos y vacaciones (48 h de antelación) · `R16` actividades fuera de planta
(salida, motivo, llegada, GPS).

## 5. Requerimientos

Numerados para poder aprobarlos, planificarlos y priorizarlos por separado.

### RF‑01 · Grupos de trabajo y cronograma de turnos

Poder definir grupos (normalmente 2 o 3, el modelo debe soportar más) y planificar, para
cada mes, qué turno le toca a cada grupo cada día.

El cronograma **se planifica dentro del sistema**, no se importa del Excel. La planificación
debe ser simple: elegir mes y grupo, y pintar el turno de cada día.

A futuro se manejará también un **cronograma de mantenimiento de maquinaria de planta**,
que igualmente se organiza por grupos. Todavía no hay requerimiento para eso, pero el
modelo de cronograma debe quedar **flexible y genérico**, no atado a "turnos de producción".

**Criterio de aceptación:** planificar en el sistema el cronograma de julio 2026 tal como
está en el Excel, y que calcule por sí sólo Grupo 1 = 156 h / 19,5 días y Grupo 2 =
168 h / 21 días, con 4 días de descanso efectivo cada uno.

### RF‑02 · Turno nocturno que cruza medianoche

El turno noche va de 19:30 a 07:30 del día siguiente.

> ⚠️ **Éste es el bloqueante técnico del requerimiento.** El motor actual **no puede**
> manejarlo: `findInitEndRHMarks()` impone el año, mes y día de la iteración a las marcas y
> las compara sólo por hora, así que una marca de salida a las 07:30 del día siguiente se
> lee como si fuera del mismo día. Está registrado en
> [08_deuda_tecnica.md](../08_deuda_tecnica.md). Sin resolver esto, no hay turno noche.

El reparto de la jornada nocturna es **6 h el primer día y 6 h el último**, y es una
convención de la empresa, no un prorrateo real: así la semana de noche totaliza
6 + 12 + 12 + 12 + 6 = **48 horas**. No se debe calcular el reparto real (4,5 h + 7,5 h).

**Criterio de aceptación:** un trabajador que marca 19:28 del lunes y 07:35 del martes
computa una jornada completa, sin falta ni atraso, y el día martes no le queda una entrada
huérfana. La semana de turno noche suma 48 horas.

### RF‑03 · Asignación de personal y cambio de horario dentro del mes

Asignar personal a un grupo de trabajo por semana, según la planificación del mes.

Y el caso que hay que cubrir sí o sí: un empleado —de producción o administrativo— tiene un
horario definido y **a mitad de mes, o a mitad de semana, cambia**, sin que eso altere al
resto de su grupo. El control de asistencia del mes debe hacerse contra **el
horario viejo hasta el día del cambio y contra el nuevo desde ese día**, en la misma planilla.

Se resuelve con la vigencia de la asignación (RF‑11). Un administrativo que el 15 de julio
pasa a producción:

```
Asignación 1   patrón "Administrativo"       vigencia 01/01 → 14/07
Asignación 2   patrón "Produccion 2 grupos"  vigencia 15/07 → abierta,  anclaje 15/07
```

| Día | Asignación vigente | Se controla contra |
|---|---|---|
| 01-14 jul | 1 | 09:00 – 17:30 |
| 15-31 jul | 2 | 07:30 – 19:30 o 19:30 – 07:30, según la posición del ciclo |

Sirve en los dos sentidos —administrativo a producción y al revés— y también para un cambio
de grupo dentro de producción, que es sólo otro anclaje. Y sirve a mitad de semana: la
vigencia es por fecha, no por semana.

**Esto encaja con lo que el motor ya hace.** `findValidHoraryBandContracts4Date()` resuelve
el horario **día por día**, comparando la fecha del recorrido contra la vigencia de cada
banda. El mecanismo existe; lo que cambia es que en vez de resolver a una lista de bandas
sueltas, resolverá a la posición del patrón vigente.

Dos bordes que hay que cubrir:

- **El anclaje, cuando el ciclo no es de 7 días.** Entrar un miércoles a un patrón semanal es
  obvio. Entrar a un patrón de 14 días exige decir **en qué posición del ciclo entra**: si le
  toca semana de día o de noche. La pantalla debe mostrarlo al asignar —"desde el 15/07 le
  corresponde turno noche"— para confirmarlo antes de guardar, no descubrirlo al generar.
- **La jornada nocturna a caballo del cambio.** Si el 14 trabajó turno noche, su marca de
  salida cae el 15 a las 07:30, ya bajo la asignación nueva. La resolución debe hacerse por
  **fecha de inicio de la jornada**, no por la fecha calendario de la marca. Si no, esa
  salida queda huérfana y genera una falta el 14 y un atraso el 15. Es la misma raíz de
  RF‑02: al resolverlo hay que razonar en jornadas, no en días.

**Criterio de aceptación:** un empleado con horario administrativo hasta el 14 de julio y
producción desde el 15 genera una sola planilla de julio, sin faltas ni atrasos espurios, con
cada día evaluado contra el horario que le regía ese día. Si el cambio cae sobre una jornada
nocturna, esa jornada se computa completa bajo el horario con el que empezó.

### RF‑04 · Horas semanales por género

Hombres 48 h/semana, mujeres 40 h/semana, según norma boliviana.

**Las horas no se cablean: son configurables por género.** La norma puede cambiar y otras
empresas pueden tener otra jornada.

**Criterio de aceptación:** cambiar el parámetro de 48 a otro valor y que el cómputo semanal
y la planificación lo respeten, sin tocar código.

### RF‑05 · Banco de horas acumuladas

Registro por evento con aprobación, y saldo corrido por empleado que sobrevive al cierre de
gestión. El saldo se consume como permiso y se descuenta.

**Criterio de aceptación:** reproducir el kardex `ACUMULO`/`USO` del informe de horas
acumuladas 2025‑2026, con los saldos en días y sus fracciones, y que usar un día de saldo
como permiso no genere falta en la planilla.

Ya existe `ExtraHoursWorked` (`horasextra`), pero es **por ciclo de generación** y sólo
guarda horas y monto pagado: no tiene saldo, ni acumulación, ni uso. Hay que revisar si se
extiende o si convive con una entidad de saldo.

### RF‑06 · Descuento por atrasos, configurable

Deben **convivir dos formas de calcular** el descuento por atrasos, y ser configurable cuál
se le asigna a cada grupo de trabajadores.

**Forma A — por minutos acumulados en el mes.** Es la que el sistema ya aplica hoy, y se
conserva:

| Minutos acumulados en el mes | Descuento |
|---|---|
| Hasta 30 | sin descuento |
| De 30 a 60 | ½ día de haber |
| De 60 a 90 | 1 día |
| De 90 a 120 | 2 días |
| Más de 120 | 3 días |

**Forma B — por cantidad de atrasos.** La nueva, para producción:

| Regla | Detalle |
|---|---|
| Atrasos acumulados | Cada **4 atrasos** en el mes = **1 día**. 14 atrasos = 3 días |
| Atraso grande | Más de 30 min y hasta 4 h = **½ día por cada atraso** |
| Atraso muy grande | Más de 4 h = **1 día** |

**Las dos reglas de la forma B no se acumulan entre sí:** un atraso que ya generó ½ día o
1 día por ser grande **no** cuenta además para los "4 atrasos". Sería descontar dos veces.

**Tolerancia, común a las dos formas.** Son **10 minutos configurables** — la empresa puede
querer más o menos. Pasada la tolerancia se descuenta el atraso **completo**: quien marca al
minuto 11 acumula 11 minutos, no 1. Así es como se van acumulando los minutos que alimentan
la forma A, y es aproximadamente lo que el sistema ya hace hoy vía `Tolerance`.

**Base del descuento, configurable:**

| Base | Uso |
|---|---|
| Salario mínimo nacional | Esta empresa |
| Total ganado del trabajador | Otras empresas — es lo que el sistema hace hoy |

**Criterio de aceptación:** un operario con 14 atrasos leves y sueldo de 5.000 Bs recibe 3
días descontados sobre el SMN. Un administrativo con la forma A y base total ganado calcula
exactamente igual que hoy, al centavo.

**Encaje con lo que existe.** `DiscountRule` de tipo `LATENESS` ya está modelado con rangos
en `MINUTE` y es asignable por gestión, unidad de negocio y categoría de puesto: eso cubre
la forma A y el "configurable su asignación". Falta agregarle a la regla la **base de
cálculo** (SMN o total ganado) y un tipo de rango por **cantidad de atrasos** para la forma B.

La tolerancia ya existe como entidad `Tolerance`, asociada a cada `HoraryBandContract` con
minutos antes y después de la entrada y de la salida. Hay que revisar si alcanza o si la
tolerancia del descuento debe vivir en la regla.

### RF‑07 · Vacaciones

15 días al cumplir un año.

**Baja por maternidad.** Se registra como una **justificación con goce de haber**, pero
identificada como tal: no genera falta, no descuenta haber y **no consume días de vacación**.
Debe quedar distinguible del resto de los permisos pagados, para poder reportarla.

**Vacaciones anticipadas.** La columna *días a cuenta de vacación* del reporte mensual son
días de vacación tomados **por adelantado**, que se descuentan del saldo de vacaciones.

El módulo existe completo (`VacationPlanning` → `VacationGestion` → `Vacation`, con
`VacationRule` por tramos de antigüedad) y ya crea una `SpecialDate` al aprobar, que es
justamente cómo una vacación llega a la planilla. Está **sin uso**: cero filas.

**Criterio de aceptación:** reactivarlo, cargar el informe de vacaciones vigente, y que
(a) una vacación aprobada no genere falta ni descuento, (b) una baja por maternidad de 30
días no reduzca el saldo de vacación, y (c) los días anticipados sí lo reduzcan.

### RF‑08 · Horarios fijos

| Horario | Rango |
|---|---|
| Administrativo | 09:00 – 17:30 |
| Operativo | 07:30 – 16:30 |
| Producción día | 07:30 – 19:30 (12 h; 10 h efectivas) |
| Producción noche | 19:30 – 07:30, lunes a jueves |
| Sereno día | 07:00 – 19:00 |
| Sereno noche | 19:00 – 07:00 |

Los serenos de resguardo sólo operan cuando la producción está suspendida o se concluyó el
objetivo de producción.

### RF‑09 · Cierre mensual asistido

Una pantalla de cierre que consolide lo que hoy se arma en Excel: horas y días efectivos por
empleado, atrasos, faltas, permisos, saldo de horas acumuladas y días a cuenta de vacación —
y que deje el resultado listo para la generación de planilla, respetando la ventana de
ajustes del 1 al 7.

**Criterio de aceptación:** reproducir las columnas finales del reporte de asistencia de
julio 2026: horas trabajadas efectivas, días trabajados efectivos, acumuladas en días,
retrasos, días pendientes por trabajar y sanciones, días a favor o en contra, horas
acumuladas y días a cuenta de vacación.

**Capacidad de reporte.** No se definen ahora los reportes finales, pero el modelo debe
**dar para construirlos**. El dato queda granular —por empleado, día y jornada, con sus
sesiones, su evaluación y sus incidencias, más el kardex mensual de acumulación y uso— de
modo que cualquier consolidado posterior sea una consulta, no un Excel.

#### La base de 240 horas

El mes objetivo son **240 horas = 30 días × 8 h**. La grilla de asistencia no cuenta sólo lo
trabajado: **los descansos, domingos y feriados se cargan como 8 horas** para que el mes
cuadre contra esas 240.

De ahí salen las dos columnas del cierre:

- **Días trabajados efectivos** = las horas acumuladas del mes llevadas a días de 8 h.
- **Días a favor o en contra** = el resultado de contrastar lo acumulado contra las 240 h.
  Por encima, días a favor; por debajo, días en contra.

> ⚠️ **La fórmula exacta hay que validarla contra un mes real antes de implementarla.**
> Aplicando `horas / 8` sobre el reporte de julio 2026, 11 de 16 empleados quedan un día por
> encima de lo que dice la planilla y 5 coinciden:
>
> | Empleado | Horas | Días en la planilla | horas/8 | Dif |
> |---|---:|---:|---:|---:|
> | ALBORTA | 240 | 30 | 30,00 | 0 |
> | CESPEDES | 226 | 28,25 | 28,25 | 0 |
> | ACHOCALLA | 272 | 33 | 34,00 | −1 |
> | ARANIBAR | 248 | 30 | 31,00 | −1 |
> | BUSTAMANTE | 262 | 31,75 | 32,75 | −1 |
>
> Los que coinciden son los que están en 240 o por debajo; los que difieren, por encima. Hay
> un ajuste adicional cuando se pasa de las 240 que no se deduce de estos datos. Como los
> *días trabajados* son el número que baja al prorrateo de la planilla, **la implementación
> de RF‑09 debe empezar reproduciendo un mes cerrado y cuadrando estas filas una por una.**

### RF‑10 · Carga masiva de marcaciones

Importar el archivo que exporta el biométrico (`Reporte_Marcaciones_export.xlsx`) en vez de
depender de que el dispositivo escriba directo en la base. **Se carga el archivo completo**,
sin filtrar por rango ni por empleado.

| Columna | Contenido | Destino |
|---|---|---|
| Id del Empleado | `1` | Cruza contra `empleado.codigomarcacion` |
| Nombres | `Alexanderzeballosrodrig` | Sólo referencia visual |
| nombre de empresa | `TERDEMOL` | — |
| Departamento | `Departamento` | — |
| Fecha | `01-07-2026` | `rh_marcado.marfecha`, formato `dd-MM-yyyy` |
| Hora | `07:40` | `rh_marcado.marhora`, `HH:mm` |
| Tipo de Marcación | `Entrada` / `Salida` | `rh_marcado.control` → **1 = Entrada, 3 = Salida** |
| Código de trabajo | `0` | — |
| Fuente de Datos | `Dispositivo` | — |

La columna de control **ya existe y ya usa esa codificación**: en la base hay 179.072 marcas
con `control = 1` y 29.516 con `control = 3`. El importador sólo tiene que traducir el texto
del archivo al número.

Requisitos del importador:

- **Cargar todo el archivo**, todas las hojas y todas las filas.
- **Deduplicar.** El dispositivo emite marcas repetidas: en el archivo de muestra las filas
  3/4, 7/8, 9/10, 11/12 y 13/14 son idénticas. Reimportar el mismo archivo no debe duplicar
  nada.
- **Reportar lo que no cruza.** Un `Id del Empleado` sin `codigomarcacion` correspondiente
  debe listarse, no perderse en silencio.
- **Previsualizar antes de confirmar**: marcas nuevas, duplicadas, sin empleado, y el rango
  de fechas que abarca.
- **Reversible por lote**, para deshacer una carga equivocada.

**Criterio de aceptación:** importar el archivo de julio 2026 completo, que las marcas queden
disponibles para la generacion de planilla con su entrada/salida correcta, y que reimportarlo
no cambie nada.

### RF‑11 · Rediseño del registro de horarios

El registro actual es inviable de mantener. Medido sobre la base en uso:

| | |
|---|---|
| Horarios realmente distintos (hora inicio-fin) | **44** |
| Filas en `bandahoraria` | **4.737** |
| Bandas que usan el rango `diainicio`-`diafin` | **0** |
| Filas en `bandahorariacontrato` | **4.683**, para **63** contratos |
| Bandas por contrato | **74 en promedio, hasta 1.333** |

La causa: se crea **una fila por cada día**. Un horario de 08:00 a 16:00 de lunes a viernes
son 5 filas; con turno de manana y tarde, 10. Y el catálogo no se reutiliza. Viene de la
parte academica, donde cada docente tiene bandas distintas cada día; para administrativos y
producción quedo pesimo.

**No se resuelve aprovechando `diainicio`/`diafin`.** Eso reduciria filas pero mantiene el
mismo modelo plano, no cubre horarios irregulares ni rotaciones, y sigue sin reutilizar nada
entre contratos.

#### El modelo: turno → patrón → asignación

Es el esquema que usan los sistemas de gestion de turnos (SAP HR lo llama *Daily Work
Schedule* -> *Period Work Schedule* -> *Work Schedule Rule*; en otros aparece como
*shift* -> *rotation pattern* -> *schedule group*). Tres niveles, cada uno reutilizable:

**1. Turno** (jornada diaria). El bloque atomico.

```
Turno "Produccion Dia"       07:30 - 19:30   cruza medianoche: no    12 h
Turno "Produccion Noche"     19:30 - 07:30   cruza medianoche: SI    12 h
Turno "Administrativo"       09:00 - 17:30   cruza medianoche: no     8,5 h
Turno "Operativo"            07:30 - 16:30   cruza medianoche: no     9 h
Turno "Sereno Noche"         19:00 - 07:00   cruza medianoche: SI    12 h
```

Lleva su tolerancia y su limite. El indicador de **cruce de medianoche** resuelve RF-02 en el
modelo, no a fuerza de casos especiales en el codigo.

**2. Patrón** (ciclo de rotacion). Una secuencia de N días donde cada posición es un turno o
un descanso. **N no tiene por que ser 7.**

```
Patron "Administrativo"      ciclo de 7 dias
  1 L  Administrativo   2 M  Administrativo   3 X  Administrativo
  4 J  Administrativo   5 V  Administrativo   6 S  descanso   7 D  descanso

Patron "Produccion 2 grupos" ciclo de 14 dias
  semana 1:  Noche Noche Noche Noche Noche  descanso descanso
  semana 2:  Dia   Dia   Dia   Dia   Dia    descanso descanso
```

**La rotacion semanal día/noche deja de ser un caso especial: es un patrón de 14 días.** El
grupo 1 y el grupo 2 usan el mismo patrón, desfasados una semana.

**3. Asignación.** El contrato -o el grupo- se asigna a un patrón, con vigencia y con una
**fecha de anclaje** que indica en que posición del ciclo arranca. Ese anclaje es lo que
permite que dos grupos compartan patrón y vayan en contrafase.

Un horario administrativo pasa a ser **1 turno + 1 patrón + 1 asignación por persona**, en
vez de 5 bandas de catálogo y 5 asignaciones por persona.

#### Inmutabilidad y cambios masivos

**Los turnos y patrones no se editan.** Editar una banda que otros contratos comparten ya
provoco cambios no advertidos en empleados ajenos al que se queria modificar.

- Un turno o patrón **usado en una planilla generada queda cerrado**. Es la misma protección
  que se aplico a las reglas de descuento en la v6.0.129.
- Para cambiar un horario se **crea la versión nueva** y se **reasigna**.
- La reasignación es **masiva y con seleccion multiple**: filtrar el personal, marcar los que
  correspondan, elegir el patrón nuevo y la fecha desde la que rige. Nunca uno por uno.
- Queda el histórico: cada asignación tiene vigencia, asi que una planilla vieja se sigue
  explicando con el patrón que regia entonces.

**Criterio de aceptación:** dar de alta el horario administrativo una sola vez; asignarlo a
40 personas en una operacion; cambiar la salida de 17:30 a 18:00 creando la versión nueva y
reasignando a esas 40 en otra operacion; y que un contrato migrado calcule su planilla
exactamente igual que antes de migrar.

**Compatibilidad.** El motor lee hoy `HoraryBandContract` via
`findValidHoraryBandContracts4Date()` y `getHoraryBandContractMapByDay()`. La migración no
puede ser un corte: hay que poder resolver el patrón a las bandas efectivas del día que el
cálculo espera, y convivir con los contratos que sigan en el modelo viejo mientras se migran.

### RF‑12 · Motor de asistencia basado en sesiones

Reemplaza el mecanismo de `Limit` + `Tolerance`, que viene dando problemas y no se puede
arreglar con parches: el defecto es conceptual.

#### Qué está mal hoy

El motor pregunta, para cada banda y cada marca, **"¿esta marca pertenece a esta banda?"**,
usando una ventana rígida (`Limit`, hoy 180 minutos en las cuatro direcciones). Si la marca
cae fuera, **se descarta**. Y si a la banda le quedan menos de dos marcas, se registra
**falta**.

De ahí sale el problema de fondo: **una falta puede originarse en un fallo de emparejamiento,
no en una ausencia real.** Alguien que trabajó 14 horas figura ausente porque su salida no
entró en la ventana. Es la peor forma de equivocarse: castiga al que más trabajó.

Y para tapar eso hubo que inflar los parámetros:

| Parámetro | Valor real | Para qué se infló |
|---|---:|---|
| `limite.despuesfin` | 180 min | Que la salida tardía no se descarte |
| `tolerancia.despuesfin` | 180 min | Que quedarse de más no cuente como atraso |

Con eso el sistema queda ciego más allá de 3 horas y, peor, **trata quedarse de más igual que
salir antes**: el código toma el valor absoluto de la diferencia contra el fin de banda, sin
mirar la dirección.

#### Lo que se implementa en su lugar

Tres etapas con responsabilidades separadas, que es como resuelven esto los sistemas de
control de tiempo y asistencia. Ninguna etapa puede provocar una falta por no encontrar pareja.

**Etapa 1 — Construir las sesiones de trabajo.** A partir de las marcas crudas del empleado,
armar pares entrada/salida. Esta etapa **no conoce el horario**.

- Si el origen de la marca trae entrada/salida confiable, se usa.
- Si no, se emparejan cronológicamente: primera con segunda, tercera con cuarta.
- Número impar de marcas → la última queda **huérfana** y genera una **incidencia**, nunca
  una falta.

> **Por qué no se puede confiar sólo en el indicador de entrada/salida.** En los últimos 12
> meses la base tiene 24.547 marcas con `control = 1` (Entrada) y **256** con `control = 3`
> (Salida): el dispositivo graba casi todo como entrada. El archivo de exportación sí viene
> balanceado (169 / 182). O sea: el dato es confiable en lo que se importe de ahora en más, y
> **no** en el histórico. El emparejamiento debe funcionar en los dos escenarios.

**Etapa 2 — Asociar cada sesión a la jornada programada.** Por **solapamiento temporal**,
eligiendo la jornada con la que más se superpone. **No hay ventana que descarte nada.**

- Sesión que no solapa con ninguna jornada → *trabajo fuera de horario*: se reporta, no se
  pierde.
- **Jornada sin ninguna sesión → eso, y sólo eso, es una falta.**

Acá desaparece `Limit`. No se reemplaza por otro parámetro: se elimina la pregunta.

**Etapa 3 — Evaluar la sesión contra la jornada, con reglas direccionales.**

| Situación | Resultado |
|---|---|
| Entrada posterior al inicio + tolerancia | **Atraso**, contando los minutos completos desde el inicio (D11) |
| Salida anterior al fin − tolerancia | **Salida anticipada** — concepto propio, no es un atraso |
| **Salida posterior al fin** | **Nunca es atraso.** Es una salida normal |
| Jornada sin sesión | Falta |

**Quedarse después de hora, sin autorización, es una salida normal.** No genera atraso, no
genera descuento, no genera incidencia y no se convierte en hora extra. El tiempo de más sí
cuenta para las *horas trabajadas efectivas* del cierre mensual (RF‑09), pero no altera la
planilla. Las horas extra son otra cosa: existen sólo si están autorizadas y registradas
(RF‑05).

#### Marca de entrada sin marca de salida

Hoy este caso da **falta de banda completa** y el atraso ni se evalúa, porque
`findInitEndRHMarks()` es todo o nada: si no encuentra las dos marcas devuelve una lista
vacía. El que trabajó todo el día y olvidó marcar la salida recibe **el mismo trato que el
que no vino** —falta completa, y con la regla del ×2, dos días descontados— y en el reporte
de control los dos se ven idénticos, con las dos marcas en `null`.

Con el modelo de sesiones:

- La sesión queda **incompleta**, no se descarta.
- Solapa con la jornada, así que **no hay falta**: la falta se reserva para la jornada sin
  ninguna sesión.
- El **atraso de entrada sí se evalúa**, porque esa marca existe y es válida.
- La sesión se **cierra en la hora de fin de la jornada**, nunca más allá.
- Se emite una **incidencia** para que RRHH actúe antes de cerrar el mes.

**El reporte de control nunca puede quedar con las dos marcas en `null` habiendo al menos una
marcación.** Si hay una marca, se guarda; lo que falta se registra como faltante, no como
inexistente. Es la diferencia entre "olvidó marcar" y "no vino", y hoy se pierde.

#### Justificación del atraso

Un atraso de entrada **se mantiene aunque la persona se quede después de hora**. Trabajar de
más no compensa haber llegado tarde: son conceptos distintos.

El atraso se quita **sólo con una justificación autorizada y registrada**. El mecanismo
existe —una `SpecialDate` con intervalo horario, que el motor consulta en
`hasPermissionForBandInterval()`— pero hay que precisarlo, porque hoy es demasiado grueso:

```java
return (banda.getInitHour() >= permiso.getStart() && banda.getInitHour() <= permiso.getEnd())
    || (banda.getEndHour()  >= permiso.getStart() && banda.getEndHour()  <= permiso.getEnd());
```

Compara el permiso contra **la hora de la banda, no contra la marca**. De ahí dos defectos:

- **Es todo o nada por jornada.** Justificar 20 minutos de atraso exime la jornada entera:
  también la salida anticipada y la falta.
- **El permiso no acota cuánto perdona.** Uno de 07:30 a 08:00 perdona igual una llegada a
  las 08:45, porque la condición mira el inicio de la banda, no la marca real.

En el motor nuevo la justificación debe **perdonar sólo los minutos que efectivamente
cubre**, comparándose contra la marca y no contra la jornada, y sin eximir el resto de la
evaluación.

La jornada queda con **tres parámetros** en lugar de los ocho de hoy: tolerancia de entrada,
tolerancia de salida anticipada, y si cruza medianoche.

#### Incidencias en vez de decisiones silenciosas

El motor deja de resolver en silencio y produce una lista de **incidencias** para que RRHH
revise antes de generar: marca huérfana, entrada sin salida, sesión que no solapa con ninguna
jornada, jornada sin sesión, sesión anormalmente larga.

Quedarse después de hora **no** es una incidencia: es normal y no requiere revisión.

Es lo que `ControlReport` debería ser y hoy no es: hoy guarda 897.121 filas de bitácora cuyos
importes salen todos en cero.

**Criterio de aceptación:**

- Alguien que sale 2 h después de su jornada no genera atraso, falta ni incidencia. Tampoco
  si sale 5 h después.
- Alguien que llega 20 min tarde y se va 2 h después **conserva su atraso**: quedarse de más
  no lo compensa.
- Ese mismo atraso desaparece si existe una justificación autorizada que cubra esos 20
  minutos, y **sólo** esos: una justificación de 20 minutos no perdona una llegada 3 h tarde.
- Alguien que olvidó marcar la salida genera una **incidencia**, no una falta automática, y
  su marca de entrada queda registrada en el reporte de control.
- Un empleado con dos jornadas en el día (mañana y tarde) empareja correctamente sus 4 marcas,
  y las marcas de la tarde no se le asignan a la jornada de la mañana.
- Una jornada nocturna de 19:30 a 07:30 se computa como **una sesión**, no como dos días con
  marcas huérfanas.
- Regenerar un mes cerrado de administrativos da **exactamente** los mismos importes que hoy.

### RF‑13 · Trazabilidad: fechas especiales y reporte de control

`SpecialDate` y `ControlReport` son, hoy, las dos piezas que de verdad sirven para rastrear,
verificar y validar lo que la planilla afirma. Hay que **conservarlas y modernizarlas**, no
reemplazarlas: son lo que permite responder "¿por qué a esta persona le descontaron este día?".

#### Reporte de control

Debe pasar de bitácora pasiva a **evidencia de la evaluación**. Por cada empleado, día y
jornada debe quedar registrado:

| Qué | Hoy | Debe |
|---|---|---|
| Marcas asociadas | `marcinicio` / `marcfin`, **ambas en `null` si no encontró las dos** | La(s) marca(s) que **sí** hubo, siempre. Nunca dos nulos habiendo una marca |
| Todas las marcas del día | texto plano `marcaciones` | Se conserva, es útil para auditar |
| Jornada esperada | implícita en la banda | Explícita: turno, hora inicio, hora fin, si cruza medianoche |
| Resultado de la evaluación | minutos de atraso y de ausencia | Atraso, salida anticipada, tiempo adicional, falta — **cada uno por separado** |
| Qué asumió el motor | — | Si cerró una sesión incompleta, si aplicó una justificación, y cuál |
| Importes en Bs | **todos en cero**: `perMinuteSalary` nunca se asigna | O se calculan de verdad, o se quitan. Una columna que siempre vale cero engaña |

**Volumen.** Ya tiene 897.121 filas y se regenera completa en cada corrida de prueba. Antes
de agregarle columnas hay que medir el impacto; puede convenir separar la bitácora de marcas
del resultado de la evaluación.

#### Incidencias

Las incidencias son el producto nuevo de esta trazabilidad, y **deben ser visibles y
accionables**: no basta con marcarlas internamente. RRHH tiene que poder ver, antes de cerrar
el mes, **quién, qué día, en qué jornada, qué pasó y qué asumió el sistema** — y actuar sobre
eso: justificar, corregir la marca, o dejarlo como está.

Tipos mínimos: marca huérfana · entrada sin salida · salida sin entrada · sesión que no
solapa con ninguna jornada · jornada sin ninguna sesión · sesión anormalmente larga.

#### Fechas especiales

`SpecialDate` es la vía por la que entran feriados, permisos, justificaciones, vacaciones
aprobadas y —nuevo— la baja por maternidad. Con 14.819 filas es la pieza más usada del
módulo. Lo que le falta:

- **Motivo explícito.** Hoy sólo distingue con goce / sin goce y a quién aplica. Se necesita
  el motivo: permiso personal, justificación de atraso, baja por maternidad, compensatorio,
  descanso. Es lo que pide RF‑07 para que la maternidad no consuma vacaciones, y lo que
  permite reportar por causa.
- **Alcance acotado.** Una justificación debe perdonar **sólo lo que cubre** (D23), no la
  jornada entera.
- **Respaldo.** Quién autorizó, cuándo, y contra qué formulario (R09, R15, R10). Hoy los
  papeles viven fuera del sistema y la planilla no puede citarlos.

**Criterio de aceptación:** ante un día con descuento, poder reconstruir desde el sistema —sin
abrir un Excel— qué marcas hubo, qué jornada le correspondía, qué evaluó el motor, qué asumió
y qué justificación se aplicó.

## 5b. Cómo queda el sistema

Un mismo código para todas las empresas. Lo único que cambia entre ellas es **configuración**.

### Las piezas

```
CONFIGURACIÓN (por empresa, se carga una vez)
  Turnos ....................... 07:30-19:30 · 19:30-07:30 (cruza) · 09:00-17:30 · ...
  Patrones ..................... ciclo de N días, cada posición un turno o descanso
  Grupos ....................... sólo si la empresa los usa
  Reglas de descuento .......... forma A o B · base SMN o total ganado · tolerancia
  Jornada semanal .............. 48 h / 40 h configurable
  Régimen de aportes SIP ....... ya implementado (v6.0.129)

        ↓ se asigna a los contratos, con vigencia

PROCESO MENSUAL (igual para todas)
  1. Marcas .................... del dispositivo, o importadas del Excel   (RF-10)
  2. Sesiones .................. emparejar marcas, sin descartar ninguna   (RF-12)
  3. Jornada esperada .......... resolver el patrón vigente ese día        (RF-03, RF-11)
  4. Evaluación ................ atraso · salida anticipada · falta        (RF-12)
  5. Incidencias ............... lo dudoso va a revisión, no se decide solo (RF-13)
  6. Cierre .................... horas, días efectivos, saldos            (RF-09)
  7. Planilla .................. lo que ya existe hoy, sin cambios
```

La regla que sostiene todo: **una falta sólo nace de una jornada sin ninguna sesión.** Nunca
de un fallo de emparejamiento.

### Las tres empresas

| | **Otras** (sin producción) | **ILVA** (admin + producción básica) | **TERDEMOL** (todo) |
|---|---|---|---|
| Turnos a definir | 1 | 2 | 5 o 6 |
| Patrones | 1 semanal | 2 semanales | semanal + **ciclo de 14 días** rotativo |
| Grupos | no usa | no usa | **2 a 4**, con cronograma mensual |
| Turno nocturno | no | no | **sí**, cruza medianoche |
| Regla de atrasos | forma A · total ganado | forma A · total ganado | **forma B · SMN** |
| Banco de horas | no | opcional | **sí**, con saldo |
| Carga de marcaciones | dispositivo | dispositivo | **importación** |
| Vacaciones | sí | sí | sí |
| Incidencias | sí | sí | sí |

**Otras empresas** configuran un turno, un patrón, y ya están: el resto queda en los valores
por defecto y el sistema se comporta como hoy, pero sin faltas por marca no emparejada.

**ILVA** suma un segundo turno y conserva su forma de cálculo actual. Gana el motor de
sesiones, las incidencias y la trazabilidad, **sin que se le mueva un importe**.

**TERDEMOL** usa todo: grupos, rotación día/noche, banco de horas, descuentos sobre el SMN e
importación de marcaciones.

### Qué NO cambia para quien no lo necesita

Una empresa que sólo tiene administrativos **no ve nada nuevo**: no configura grupos, no
configura patrones rotativos, no toca reglas de descuento. Sigue con lo suyo. Ésa es la
prueba de que el diseño es multiempresa y no un traje a medida de una sola.

## 6. Qué ya existe y se debe reutilizar

No hay que construir de cero: buena parte del modelo está y sólo está mal aprovechada.

| Necesidad | Qué hay hoy | Estado |
|---|---|---|
| Turnos y horarios | `HoraryBand`, `HoraryBandContract`, `Tolerance` | **A rediseñar** (RF‑11): 4.737 bandas para 44 horarios reales |
| Ventana de asociación de marcas | `Limit` | **Se elimina** (RF‑12): es la causa de que un fallo de emparejamiento se convierta en falta |
| Rotación | `TypeHoraryBand` + `executeAttendanceControlManagersRotation()` | Existe un esbozo; sólo toma **la primera** banda con tipo y descarta el resto |
| Descuento por atrasos configurable | `DiscountRule` tipo `LATENESS`, rangos en `MINUTE`, por gestión/unidad/categoría | Modelado y con CRUD; **sin uso** en el camino de sueldos |
| Horas extra | `ExtraHoursWorked` por ciclo de generación | Existe; sin saldo ni acumulación |
| Vacaciones | `VacationPlanning`/`VacationGestion`/`Vacation`/`VacationRule` | Completo y **sin uso** (0 filas) |
| Permisos y feriados | `SpecialDate` con destino y goce de haber | En uso intensivo (14.819 filas) |
| Bitácora de asistencia | `ControlReport` | En uso (897.121 filas); sus importes en Bs salen en cero |
| Marcado | `RH_Mark` → vista `vmarcado` → `RHMark` | En uso; cruza por `codigomarcacion` y **corta en 2020‑01‑01**. No guarda si la marca es entrada o salida |
| Carga de marcaciones | — | **No existe** (RF‑10) |
| Grupos y cronograma | — | **No existe** (RF‑01) |
| Banco de horas con saldo | — | **No existe** (RF‑05); `ExtraHoursWorked` no lleva saldo |

Detalle de cada uno en [04_asistencia_y_marcado.md](../04_asistencia_y_marcado.md) y
[01_modelo_de_datos.md](../01_modelo_de_datos.md).

## 7. Decisiones tomadas

Resueltas con el usuario el 2026‑09‑03.

| # | Decisión |
|---|---|
| **D1** | Las 48 h / 40 h por género son norma boliviana y deben ser **configurables**, no cableadas |
| **D2** | El reparto 6 h + 6 h de la jornada nocturna es **convención de la empresa** para totalizar 48 h en la semana. No se calcula el reparto real |
| **D3** | La base del descuento es **configurable**: SMN para esta empresa, total ganado para otras |
| **D4** | Las dos reglas de la forma B **no se acumulan**. Un atraso grande que ya generó ½ día no cuenta además para los "4 atrasos" |
| **D5** | La baja por maternidad se registra como **justificación con goce de haber, identificada como tal**. No consume vacaciones |
| **D6** | Los *días a cuenta de vacación* son **vacaciones anticipadas** y sí descuentan del saldo |
| **D7** | El cronograma **se planifica en el sistema**. Debe ser flexible y simple, porque a futuro se sumará el **cronograma de mantenimiento de maquinaria**, también por grupos |
| **D8** | Se conserva la forma de cálculo actual de atrasos (por minutos acumulados) y se agrega la nueva (por cantidad de atrasos). **Cuál se aplica es configurable por asignación** |
| **D9** | La ventana de ajustes del 1 al 7 es **norma y procedimiento, no se implementa** en el sistema |
| **D10** | Atraso de más de 2 h y hasta 4 h: sigue siendo **½ día**. **Más de 4 h: 1 día** |
| **D11** | La tolerancia aplica a **las dos formas** de cálculo, y sus **10 minutos son configurables** |
| **D12** | Se necesita **carga masiva de marcaciones** desde el export del biométrico, con deduplicación |
| **D13** | El registro de horarios **se rediseña**: plantilla semanal reutilizable para horario fijo, cronograma por calendario para producción |
| **D14** | Todo lo que varíe entre empresas es **configuración, no código**. Nada atado a un cliente |
| **D15** | La carga de marcaciones procesa **el archivo completo**, y mapea `Entrada`/`Salida` a `control` **1 / 3**, que es la codificación que la base ya usa |
| **D16** | Turnos y patrones **no se editan**: usados en una planilla quedan cerrados. Para cambiar se crea la versión nueva y se **reasigna en masa con selección múltiple** |
| **D17** | El modelo de horarios es **turno → patrón (ciclo de N días) → asignación con anclaje**. No se reutiliza la idea de `diainicio`/`diafin` |
| **D18** | Se **elimina `Limit`**. El control de asistencia pasa a un modelo de **sesiones**: emparejar marcas, asociar por solapamiento, evaluar con reglas direccionales |
| **D19** | **Una falta sólo puede originarse en una jornada sin ninguna sesión**, nunca en un fallo de emparejamiento |
| **D20** | **Salir después de la hora nunca genera atraso.** Es tiempo adicional, no un retraso |
| **D21** | El emparejamiento **no puede depender sólo** del indicador entrada/salida: es confiable en lo importado y no en el histórico |
| **D22** | Quedarse después de hora sin autorización es una **salida normal**: sin atraso, sin descuento, sin incidencia, y no se convierte en hora extra |
| **D23** | El **atraso de entrada se mantiene** aunque la persona se quede después. Sólo lo quita una **justificación autorizada y registrada**, que debe perdonar **sólo los minutos que cubre** |
| **D24** | Entrada sin salida: la sesión se **cierra en la hora de fin de jornada**, sin falta, con el atraso de entrada evaluado y una **incidencia** para RRHH |
| **D25** | El reporte de control **nunca queda con las dos marcas en `null` habiendo al menos una marcación** |
| **D26** | Las incidencias deben ser **visibles y accionables**: quién, qué día, qué jornada, qué pasó y qué asumió el sistema |
| **D27** | `SpecialDate` y `ControlReport` **se conservan y se modernizan**: son la base de la trazabilidad y hoy es lo que de verdad sirve para verificar |
| **D28** | El mes objetivo son **240 horas** (30 días × 8 h). Descansos, domingos y feriados se cargan como **8 h** para que el mes cuadre |
| **D29** | **Días a favor o en contra** = resultado de contrastar las horas acumuladas contra las 240 |

## 7b. Preguntas que siguen abiertas

Ninguna de concepto. Queda **un ajuste numérico por validar**: cuando las horas del mes
superan las 240, la planilla manual descuenta un día que no se deduce de los datos. Se
resuelve al implementar RF‑09, cuadrando contra un mes cerrado.

## 8. Riesgos

| Riesgo | Mitigación propuesta |
|---|---|
| Cambiar el cálculo de atrasos altera planillas de empresas que hoy funcionan | Que la regla nueva sea opt‑in por categoría de puesto o unidad de negocio; sin configurar, se calcula como hoy |
| El turno noche obliga a tocar el núcleo de `executeAttendanceControlManagers` | Aislar el cambio y validar regenerando meses cerrados de administrativos, que no deben moverse un centavo |
| No hay tests automatizados en el módulo | La verificación es regenerar en PRUEBA y comparar contra el oficial, campo por campo, como se hizo con el régimen SIP |
| El volumen de `reportecontrol` ya es de ~900 mil filas y se regenera en cada corrida | Medir el impacto antes de agregarle columnas |

## 9. Fuera de alcance

- Sector académico, en cualquier forma.
- Reescribir el motor de planillas. Se extiende, no se reemplaza.
- Migrar los históricos de asistencia previos a 2020, que la vista `vmarcado` no expone.
- Los formularios en papel (R09, R11, R12, R13, R14, R16) como flujo digital completo. Este
  SPEC sólo consume su información; digitalizarlos sería un requerimiento aparte.
- La ventana de ajustes del 1 al 7 (PA.01 §6.8): queda como norma interna, sin candado en
  el sistema.

## 10. Estado del documento

| Etapa SDD | Estado |
|---|---|
| **SPEC** | 13 requerimientos, 29 decisiones. Sin preguntas de concepto |
| PLAN | Siguiente paso |
| TASKS | No iniciado |
| IMPLEMENT | No iniciado |
