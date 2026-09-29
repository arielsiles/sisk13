# PLAN 12 — La planilla toma los números del motor

> Es **E2.9** del [plan 01](plan_01_asistencia_produccion.md), con nombre propio porque es la
> etapa que convierte todo lo anterior en algo que se paga. Incluye **F3, F4 y F5** del
> [plan 11](plan_11_marca_incompleta.md), que se dejaron explícitamente para acá.

Hoy hay dos verdades sobre la asistencia de una persona y **no se hablan**: la pantalla de
verificación, que corre el motor nuevo, y la planilla, que sigue con el modelo de bandas
horarias. Este plan deja una sola.

## Lo que encontré antes de escribir el plan

Lo primero fue mirar la base, no el código. Cambia el plan entero:

| Dato | Valor |
|---|---|
| Empleados | **245** |
| Empleados con `flagcontrol = 1` (Control de asistencia) | **0** — los 245 están en 0 |
| Filas en `bandahorariacontrato` | **10** |
| Marcas cargadas | 4.538, **solo julio 2026**, 70 empleados |
| Códigos de marcado sin empleado | 2 |
| Planillas generadas | 3 TEST de **agosto 2026**, 45 personas · ninguna de julio |
| De esas 45, con marcas | **27** |
| Fechas especiales cargadas | **1** — un feriado nacional |
| Categoría de puesto | TERDEMOL CENTRAL, `GENERATION_BY_SALARY` |

De ahí salen tres conclusiones que hay que decir antes de cualquier tarea:

**1. Hoy la planilla de terdemol ignora la asistencia por completo.** Con `flagcontrol = 0` el
generador no exige bandas *y tampoco controla nada*: cae al camino "contrato sin bandas", fija
`workedDays` por prorrateo de contrato y sigue de largo. Todos cobran 30 días, cero faltas, cero
atrasos. Las 10 bandas que quedan son residuo de la carga vieja.

**2. La verificación que pedía el plan 01 no aplica acá.** Decía *"regenerar tres meses cerrados y
confirmar que no se mueve un centavo"*. Eso se escribió pensando en una empresa que **ya** usaba
bandas. En terdemol no hay nada que preservar: hay que verificar lo contrario, que **cada número
nuevo se explica**. Lo dejo anotado porque es exactamente el tipo de criterio que se arrastra sin
revisar y después justifica un error.

**3. El agujero más peligroso todavía no se ve en los datos.** El motor nuevo solo conoce
`SpecialDate` con motivo **HOLIDAY** y día completo. No conoce permisos, vacaciones, maternidad,
compensatorios ni descansos. Como en la base hay **una sola** fecha especial —un feriado—, hoy
esto no se nota. El día que RRHH cargue la primera vacación, el motor le va a poner **falta a cada
día de vacaciones**. No es un riesgo futuro: es un defecto que ya está escrito y que la
verificación de julio **no puede detectar**. Por eso es tarea de este plan y no de después.

## Las decisiones

### D1 · Quién entra al control: manda la jornada, no el flag

`flagcontrol` nació con las bandas y hoy no lleva información: los 245 están en 0, que es el valor
por omisión, no una decisión de nadie.

**Decide la jornada.** Si las cuatro capas no devuelven ninguna jornada para el período, no hay
asistencia que controlar y el contrato se paga como hoy. Es una regla sola, verificable en la
pantalla y sin un segundo interruptor que contradiga al primero.

`flagcontrol` **se mantiene como exención explícita**: en 0 la persona no se controla aunque tenga
jornadas. Sirve para el gerente que figura en un horario de papel. Pero como hoy los 245 están en
0, hay que corregir el dato antes de que el motor sirva de algo → **P7**, con SQL que aplica el
usuario.

> La alternativa era sacar el flag del camino nuevo. No la elegí porque quitaría la única manera
> de eximir a alguien que sí tiene horario, y eso se va a necesitar.

### D2 · Los días excusados entran al motor, no a la planilla

Una fecha especial de **día completo con goce de haber** —feriado, permiso, vacación, maternidad,
compensatorio, descanso— **excusa la jornada**: ni se trabaja ni se pierde. Sale del cálculo antes
de que exista la falta.

Tiene que vivir en el motor y no en la planilla por la misma razón de siempre: **la pantalla de
verificación tiene que mostrar lo mismo que se va a pagar**. Si el perdón se aplicara en la
planilla, la pantalla diría "falta" y el recibo diría "pagado", y nadie podría explicar cuál de
los dos miente.

`HolidayService` ya sabe resolver los tres destinos —empresa, unidad organizacional, persona— por
los dos caminos de unidad de negocio. Se generaliza de "feriados" a **"días excusados"** y se le
saca el filtro por motivo. El feriado conserva además su efecto propio: suprime la jornada del
horario fijo, no la del cronograma de grupo.

Y la de **día completo sin goce de haber** hace lo contrario: pierde la jornada, pero **no se
sanciona** (ver D3).

De paso esto elimina el borde documentado en
[03_calculos_planilla.md](../03_calculos_planilla.md): hoy la planilla resta `unpaid.size()` sin
comprobar que el control haya generado esa ausencia, y `dayAbsences` puede quedar **negativo** y
pagar más de 30 días. Con el día sin goce resuelto dentro del motor, la resta desaparece.

### D3 · Tres clases de falta, no una

Es **F4** del plan 11, y al recorrerlo aparece una tercera:

| Clase | Qué pasó | Costo |
|---|---|---|
| **Ausencia** | no vino y no tiene excusa | 1 día · **×2** |
| **Registro** | vino y falta una punta | 1 día o ½ · **simple** |
| **Sin goce** | licencia sin goce de haber | 1 día · **simple** |

El ×2 es una sanción y es para quien no vino. Quien marcó mal cometió un error de registro; quien
tiene licencia aprobada sin goce no cometió ninguno: simplemente ese día no se le paga.

```
descuento = ausencia × 2  +  registro  +  sinGoce
salario   = basico / 30 × (workedDays − descuento)
```

### D4 · El atraso de un día perdido no se cobra además

Es **F3**, y **ya está implementado** en el motor: `getLatenessMinutes()`, `getEarlyExitMinutes()`
y `getExtraMinutes()` devuelven cero cuando la jornada está perdida. La planilla no tiene que
hacer nada salvo consumir esos números en vez de recalcularlos.

Se pudo adelantar porque vive en el motor. Queda anotado acá para que al revisar F3 no se busque
código que no existe.

### D5 · La escala del descuento por atrasos no se toca

Los tramos —0/30/60/90/120 minutos → 0, ½, 1, 2, 3 días sobre el total ganado— son una regla de
**cómo se le pone precio al atraso**, no de cómo se mide. Cambia de dónde salen los minutos, no
qué se hace con ellos.

### D6 · El rastro por día: `reportecontrol` sin banda

La planilla escribe una fila de `ControlReport` por día y persona: es la evidencia de por qué
alguien perdió dos días. **Una planilla tiene que congelar su evidencia**; recalcularla después
con la pantalla no sirve, porque el cronograma pudo cambiar.

Problema: `reportecontrol.idbandahorariac` es **NOT NULL** y con el motor nuevo no hay banda.

**Se hace nullable y se agregan las columnas que el motor tiene y la banda no**: de dónde salió la
jornada, jornadas del día, jornadas perdidas y clase de falta. Preferí esto a una tabla nueva
porque el reporte ya existe y RRHH ya lo conoce; una tabla nueva es también un reporte nuevo.

**La condición es que el reporte sirva.** Guardar el rastro y no poder leerlo es lo mismo que no
guardarlo: tiene que haber un **reporte de control de la planilla generada** donde se vea, persona
por persona y día por día, todo lo que la planilla decidió —qué jornada le tocaba, qué marcó, si
perdió el día y por cuál de las tres causas, cuántos minutos de atraso— con los totales del mes
cuadrando contra el recibo. Es lo que permite contestar un reclamo sin recalcular nada.

Entra acá la limpieza que el plan 01 dejó pendiente: *"los importes en cero del reporte de control
se calculan de verdad o se quitan"*. Una columna que siempre vale cero engaña, y en un reporte que
se usa para defender un descuento, engaña el doble.

### D7 · Nunca más de 30 días, y que no se pueda

Que una planilla pague **más de 30 días** no es un detalle: es la clase de número que hace que
nadie vuelva a creerle al sistema. Hoy puede pasar por dos caminos distintos, y hay que cerrar
los dos **y** poner una red debajo.

**Causa 1 — la resta de los días sin goce.** `dayAbsences = dayAbsences × 2 − unpaid.size()`.
Si la persona tiene tres días de licencia sin goce y el control no generó ausencia por ellos,
`dayAbsences` queda en **−3** y se le pagan **33 días**. Desaparece con D2: el día sin goce pasa a
ser una clase de falta dentro del motor y la resta ya no existe.

**Causa 2 — `workedDays` se pisa.** Dentro del bucle de contratos hay `workedDays = contractDays;`
sin acumular: con dos contratos en el mes, el último que se itera borra al anterior. Desaparece con
D8 y D9: paga un solo contrato, así que no hay bucle que pise nada.

**La red: no se recorta, se rechaza.** Al cerrar cada persona se comprueba la invariante

```
0  <=  dias pagados  <=  dias de contrato en el mes  <=  30
```

Si no se cumple, la generación **no produce esa planilla**: acumula a la persona y la reporta,
igual que hoy se hace con quien no tiene contrato. Un `min(30, ...)` silencioso sería peor que el
error, porque lo esconde: el número quedaría plausible y la causa seguiría viva.

### D8 · A la planilla va el contrato principal

`employeeValidContractsList.get(0)` es la respuesta actual a todo. Y no es siquiera un contrato al
azar: la consulta **ordena por `fechainicio`**, así que `get(0)` es sistemáticamente el **más
viejo** de los que tocan el mes —en una recontratación, el que ya terminó—. De ahí sale el sueldo.

No es un caso de laboratorio. El empleado 114 —PABLO, el de las pruebas— tiene cinco contratos, y
para septiembre 2026 la consulta devuelve cuatro:

| Contrato | Desde | Hasta | Estado | Principal |
|---|---|---|---|---|
| 336 | 01/09/2026 | 30/09/2026 | INACTIVO | sí |
| 339 | 04/09/2026 | — | ACTIVO | no |
| 338 | 05/09/2026 | — | ACTIVO | no |
| 337 | 10/09/2026 | — | ACTIVO | no |

Hoy cobraría por el **336**, que está dado de baja.

**La regla es una sola: a la planilla va el contrato principal, activo para generación y vigente en
el período.** De él salen el sueldo, el puesto, el centro de costo y las cuentas; sobre él se
controla la asistencia; y sus fechas dan los días.

El filtro `activogenplan` **ya existe** en la consulta. Lo que se agrega es el principal, y lo que
se saca es el `get(0)`.

Si no hay exactamente uno, **no se genera y se reporta**, junto con los demás casos, para que RRHH
los resuelva de una pasada:

```
ninguno principal   ->  RH tiene que designarlo   (es el caso de PABLO)
mas de uno          ->  contradice la invariante 6, hay datos rotos
```

No es una limitación: es la decisión del [plan 08](plan_08_fecha_salida.md) dicha en voz alta
—*"RH decide y cambiará manualmente cuál es el contrato principal"*—. El sistema no adivina cuál
de tres sueldos cobra alguien.

**Los secundarios no entran.** Son trabajo eventual con fecha de inicio y fin: no llevan AFP ni
vacaciones —plan 08— y tampoco generan faltas contra el sueldo mensual. Si algún día se les pone
precio, será otro cálculo.

### D9 · Los días del mes siguen siendo 30

Esto **no cambia y no estaba en discusión**: el mes vale **30 días** para el cálculo, tenga 28, 30
o 31. El código incluso fuerza `lastDayOfMonth.setDate(30)`. El divisor es 30 y el mes completo son
30 días.

Lo único que baja de 30 es que **el contrato no cubra el mes entero**, y eso ya lo resuelve
`getContractDays4Month()`: alta el 16 → 15 días; baja el 10 → 10 días; mes completo → 30.

Con D8 esto se vuelve trivial, porque hay **un solo contrato** que paga:

```
workedDays = getContractDays4Month(contrato principal)
```

Y desaparece el defecto de hoy, que era `workedDays = contractDays` **dentro del bucle de
contratos**, sin acumular: con dos contratos en el mes, el último que se iteraba borraba al
anterior.

> **El medio mes de una recontratación no se pierde: no es de la planilla.** Si alguien tiene baja
> el 15 y contrato nuevo desde el 16, la planilla paga los 15 días del contrato principal nuevo;
> del 1 al 15 responde el **finiquito**, que es otro proceso y hoy no se hace. La planilla no
> inventa esa mitad ni la paga dos veces.

### D10 · Los dos motores conviven, y la decisión es una fecha

Este plan se escribió mirando terdemol, que arranca de cero. Pero `fillManagersPayroll` **no es
suyo**: es el camino de toda planilla `GENERATION_BY_SALARY`, y hay un cliente usándolo hace más
de cinco años con bandas horarias.

| Lo que hay en esa base | |
|---|---|
| Bandas horarias | 4.683 |
| Filas de `reportecontrol` | 903.131 |
| Filas de `planillaadministrativos` | 31.563 |
| Planillas **OFICIALES** | 93 |

Reemplazar el bloque sin más le habría apagado el control de asistencia **en silencio**: sin
turnos ni cronograma cargados el resolutor devuelve cero jornadas, y la planilla habría pagado
treinta días a todos sin dar ningún error.

**La decisión es una fecha de corte por categoría de puesto, no un interruptor.** Con un
interruptor, cambiar en marzo y regenerar enero daría números distintos a la planilla oficial de
enero: un interruptor no tiene memoria. Con `categoriapuesto.jornadasdesde`, cada período resuelve
siempre igual y **regenerar un mes viejo reproduce el mes viejo**.

La granularidad es la **categoría** porque es exactamente la unidad de una corrida: una planilla
se genera por unidad de negocio, categoría y mes. Así cada planilla tiene **un motor y una sola
explicación**, y una empresa migra una categoría por vez —la más chica primero, se compara un mes
entero, después la siguiente—.

El motor usado **se sella en la planilla generada**. No decide nada —eso ya lo decidió la fecha—
sino que deja escrito con qué criterio se pagó, para no tener que recalcular la regla dos años
después.

Y el día que se apague el motor viejo, la señal es **mecánica y no una opinión**: ninguna categoría
sin fecha, y ninguna planilla sellada `BANDS` sujeta a regeneración.

> **Lo que la transición NO hereda:** la tolerancia. En bandas cuelga de `categoriapuesto` —una
> para toda la categoría— y en jornadas de cada `turno`. Al migrar hay que llevarla a los turnos:
> no se arrastra sola.

## Las tareas

| # | Tarea | Riesgo |
|---|---|---|
| P1 | `AttendancePeriodSummary`: los números del período, armados desde la misma lista de días que usa la pantalla | bajo |
| P2 | Días excusados con y sin goce dentro del motor (D2) | **medio** |
| P3 | Las tres clases de falta en `DayAbsence` y en el resumen (D3) | bajo |
| P4 | **La elección del contrato**: solo el principal activo y vigente (D8/D9), con el rechazo cuando no hay exactamente uno | **alto** |
| P5 | Reemplazar el bloque de asistencia de `fillManagersPayroll` | **alto** |
| P6 | La invariante de los 30 días, con rechazo y reporte (D7) | bajo |
| P7 | `ControlReport` sin banda (D6) **y el reporte que lo muestra** | medio |
| P8 | F5: la lista de días a revisar antes de oficializar | bajo |
| P9 | El flag y el dato: SQL de `flagcontrol` — **ya escrito**, sección 23 | bajo |
| P10 | **La convivencia**: fecha de corte, sello, y el motor viejo detrás del `if` (D10) | **alto** |
| P11 | Verificación de julio 2026 contra la pantalla, persona por persona | **alto** |

**P1 es la que sostiene todo.** Un solo objeto con los totales del período —días trabajados, las
tres clases de falta, minutos de atraso, minutos programados, minutos trabajados— construido a
partir del `List<AttendanceDay>` que ya devuelve `AttendanceCheckService`. La pantalla deja de
sumar por su cuenta y lee el mismo objeto. Con dos sumadoras terminan diciendo cosas distintas, y
ese es justamente el error que este plan viene a cerrar.

**P4 y P5 son las de riesgo alto**, y van juntas: el bloque que se reemplaza —de la línea ~1035 a
la ~1155— calcula cinco cosas entrelazadas: `workedDays`, las faltas, los atrasos, los minutos de
banda para `pricePerMinute` y el `jobContract` de pago. Se sustituye entero, no por partes, porque
separarlas es justamente lo que hoy no está hecho.

**P6 es barata y es la que sostiene la confianza.** Quince líneas: comprobar la invariante y, si
falla, no generar. Va después de P4 y P5 aunque las cubra, porque tiene que seguir ahí el día que
alguien toque otra cosa.

## Lo que este plan NO hace

- **No toca las otras planillas.** `fillProffesorsPayroll`, `fillFiscalProfessorPayroll` y la de
  aguinaldo siguen con bandas. terdemol usa `GENERATION_BY_SALARY`, que va a `fillManagersPayroll`.
  Migrar las otras sin una empresa que las use sería escribir código sin forma de probarlo.
- **No borra el modelo de bandas.** Queda vivo para las empresas que lo usan. Se apaga el día que
  no quede ninguna.
- **No toca la justificación acotada** (E2.6): un permiso por horas que perdona solo los minutos
  que cubre. Hoy el motor no la tiene y el modelo viejo sí. Es una tarea aparte, y hasta que
  exista, un permiso por horas no perdona atraso.
- **No cambia fórmulas de haber básico, AFP, RC-IVA ni aguinaldo.**
- **No calcula horas extra ni pago de feriados** (RF-05).

## Riesgos

**El número del primer mes.** Julio tiene **272 días-persona con una sola hora marcada** sobre
1.447 con marcas. Una parte son la marca de las 07:30 de un turno de noche, que sí cierra la
jornada anterior, pero aun así quedan decenas de faltas por marca incompleta. Va a ser trabajo
real para RRHH y conviene avisarlo **antes** de que vean la planilla, no cuando la vean.

**El salto es de cero a algo.** No es "los números cambian un poco": hoy nadie tiene faltas ni
atrasos y mañana los va a tener. Cada persona con diferencia necesita explicación, y van a ser
muchas.

**Los días excusados no se pueden verificar con estos datos.** Hay una sola fecha especial en toda
la base. P2 se escribe a ciegas respecto de los datos reales; hay que probarla cargando permisos y
una vacación a mano.

**Dos códigos de marcado sin empleado.** Marcas que no le pertenecen a nadie. No afectan el
cálculo, pero conviene resolverlas antes de cerrar un mes.

**El rechazo de D8 puede frenar una generación.** Quien no tenga exactamente un contrato principal
activo y vigente no genera hasta que RRHH lo designe. Es deliberado —mejor eso que pagarle el
sueldo equivocado— pero conviene saberlo antes del cierre, no durante. Hoy hay **una** persona
así: el empleado 114, que son datos de prueba.

**Hay que mirar los principales antes de generar.** La regla nueva convierte "falta el principal"
en un bloqueo, así que conviene correr la invariante 6 antes del cierre y no descubrirlo de a uno.
Para julio ya está corrida y da limpio: de los 245, **74 tienen contrato vigente y los 74 tienen
exactamente un principal**; los otros 171 no tienen contrato en el mes. Cero bloqueos.

## Cómo se verifica

Julio 2026 es el único mes con marcas, así que es el mes.

1. **Antes de tocar nada**, generar la planilla de julio con el código actual y guardarla. Es la
   línea base: faltas y atrasos en cero para todos.
2. Generar julio con el motor conectado.
3. **Persona por persona, contra la pantalla de verificación**: los días trabajados, las faltas de
   cada clase y los minutos de atraso de la planilla tienen que ser **idénticos** a los que muestra
   la pantalla para el mismo rango. Cualquier diferencia es un defecto de la conexión, no del
   motor.
4. Rehacer la aritmética completa de **ALANIS** y **BRAÑEZ**, que ya están verificados día por día
   —14 y 10 días trabajados, 0,00 y 4,00 faltas, 66 y 173 minutos de atraso—.
5. **Cargar a mano** un permiso de día completo con goce, uno sin goce y una vacación, y confirmar
   que el primero no genera falta, el segundo genera una simple y la tercera tampoco genera falta.
6. Un día con **falta por marca incompleta** no descuenta además el atraso, y descuenta **simple**.
7. **Nadie cobra más de 30 días, y no por recorte.** Se fuerza a mano el caso: una persona con
   tres días de licencia **sin goce** y sin ninguna falta. Antes daba 33; ahora tiene que dar 27,
   con tres faltas simples y no con una resta.
8. **La elección del contrato, con el empleado 114.** Con sus cuatro contratos de septiembre y
   ninguno principal vigente, la generación **tiene que rechazarlo y nombrarlo**. Designando uno
   como principal, tiene que generar y cobrar **por ese** —no por el 336, que está dado de baja—.
9. **Los 30 días**: un mes de 28 y uno de 31 dan los mismos 30 días a quien tiene contrato
   completo. Alta el 16 da 15; baja el 10 da 10.
10. **El reporte de control cuadra con el recibo**: para tres personas al azar, los días y las
    faltas del reporte suman exactamente lo que descontó la planilla.
11. Las seis invariantes de siempre sobre contratos y planes de vacaciones siguen dando lo mismo:
    la planilla no las toca, y si se movieron, algo se tocó de más.

## SQL

En el script de la versión vigente y **aplicados por el usuario**.

**Sección 23 — ya escrita.** `empleado.flagcontrol = 1` para los 245, porque el 0 de hoy es el
valor por omisión y no una decisión; dejarlo así apagaría el motor para todos. Debajo queda la
plantilla comentada para eximir a gerentes y personal de confianza por `codigoempleado`, y la
consulta de verificación.

**Pendiente hasta la implementación:** `reportecontrol.idbandahorariac` pasa a aceptar nulos, más
las columnas nuevas del rastro (D6). No se escribe todavía porque las columnas exactas salen de
P7, y una DDL adivinada se aplica una sola vez.

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | [req_01](req_01_asistencia_produccion.md) y [plan_11](plan_11_marca_incompleta.md) F3-F5 |
| PLAN | Aprobado |
| **IMPLEMENT** | **P1-P10 escritos y compilando · sin verificar contra datos**. Falta P11 |

**Hay SQL que aplicar antes de desplegar**: secciones 23, 24 y 25.

### Lo que quedó escrito

`AttendancePeriodSummary` es la sumadora única. La pantalla de verificación dejó de sumar por su
cuenta y la planilla lee lo mismo, así que una diferencia entre el recibo y la pantalla es un
defecto y no un criterio distinto.

`DayAbsence` pasó de un número a tres: ausencia, registro y sin goce. `getChargedDays()` aplica el
×2 solo a la primera, y es lo único que la planilla consume.

El bloque de asistencia de `fillManagersPayroll` se partió en dos métodos —`resolveByJourneys`
y `resolveByBands`— detrás de una sola forma, `AttendanceOutcome`. El resto de la planilla —más
de mil líneas— **no sabe cuál corrió**.

El código de bandas se movió tal cual, sin tocarle el criterio: quien genere con bandas tiene que
obtener exactamente los mismos números, o el cambio de motor no sería una elección sino una
sorpresa. Lo único que se movió de lugar es el ×2 y la resta de los días sin goce, que ahora
viven **dentro** de ese método: son parte de su criterio, no del cálculo del sueldo.

> **El motor viejo no cambia en NADA, ni siquiera en sus errores.** La invariante de los 30 días
> corre solo con jornadas. El de bandas puede seguir llegando a un número negativo por su propia
> resta de los días sin goce, y ahí la red frenaría una generación que hoy sale. Quien lleva años
> con ese motor tiene que obtener exactamente lo mismo hasta que decida pasarse: **corregirlo es
> parte de pasarse, no un efecto colateral de actualizar el sistema.**

La equivalencia se comprobó comparando el método nuevo contra el original línea por línea,
ignorando sangría y comentarios. Están todas las líneas de cálculo, en el mismo orden. Las únicas
diferencias son las esperadas: el guard acumula en `blockers` y devuelve `null` en vez de
`continue`, las declaraciones que vivían en el método de arriba bajaron a este, y se sacaron dos
`System.out.println` de depuración.

### Cuatro defectos que aparecieron al recorrerlo

Ninguno estaba en el plan; los tres primeros habrían costado plata en silencio.

- **Un código de marcado ilegible costaba el mes entero.** No devuelve ninguna marca, y sin marcas
  cada jornada se pierde. Hay dos personas con el código mal cargado -ninguna con contrato en
  julio-. Ahora frena la generación en vez de descontar treinta días sin avisar.
- **El puesto salía de `jobContractList.get(0)`.** Es el mismo error que se corrigió un nivel más
  arriba con el contrato: funciona mientras cada contrato tenga un solo puesto -hoy los 242 lo
  tienen- y falla callado el día que alguno tenga dos. Ahora se elige por categoría.
- **El horario fijo cerrado desaparecía.** `resolve()` cargaba solo los horarios con fecha de fin
  nula, así que al regenerar un mes viejo de alguien que después cambió de horario, sus días
  quedaban sin jornada y por lo tanto sin falta ni atraso. Ahora se cargan los que alcanzan el
  período.
- **La verificación resolvía cada día dos veces.** `sourceOf` y `suppressedByHoliday` cuestan tres
  o cuatro consultas cada uno y se llamaban dentro del bucle de días. Con una persona son
  doscientas consultas y no se nota; con 245 son decenas de miles y la generación se vuelve
  inusable. `resolvePeriod` devuelve todo en una pasada.

### Dos cosas que crecen

- **El rastro pasa de 168 filas a ~7.400 por planilla** -una por persona y día con jornada-. Es el
  precio de poder contestar un reclamo sin recalcular nada.
- **El rastro viejo gana el contrato.** Las 903.131 filas históricas cuelgan de la banda, y sin
  banda la fila quedaba huérfana: la pantalla mostraba los nombres en blanco. Se rellena
  `idcontrato` desde la banda —verificado: **899.022 de 903.131 son derivables**— y las filas
  conservan su banda. Quedan **4.109** cuya banda no resuelve: referencias huérfanas viejas, que
  hay que mirar aparte antes de decidir.
- **`fechaespecial` empieza a importar de verdad.** Antes ninguna vacación ni permiso llegaba al
  cálculo; ahora un permiso mal cargado cambia un recibo.
