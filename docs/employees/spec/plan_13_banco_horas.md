# PLAN 13 — El banco de horas

> *"Necesitamos registrar y acumular por mes, luego se consume como permiso."*
> *"Lo importante al final será poder emitir un reporte."*

## El circuito real

```
Jefe de area  ->  lista semanal de horas extra AUTORIZADAS
RRHH          ->  valida y registra
                  |
                  +-- se compensa con permiso   -> baja el saldo
                  +-- se paga                   -> baja el saldo y va a la planilla
```

Esto decide lo más importante del plan: **al banco solo entra lo autorizado**. No hace falta un
mínimo de minutos ni un filtro de ruido, porque nada entra solo.

## Lo que hay hoy, y por qué no alcanza

**`horasextra` paga; el banco compensa.** La tabla que existe guarda, por ciclo y persona, las
horas y **el importe pagado**, tipeados a mano. Alimenta la planilla fiscal y el importe entra a
los otros ingresos. En terdemol tiene **0 filas**: nunca se usó.

Lo que hace falta lo muestra la planilla que RRHH lleva a mano:

| | Qué tiene |
|---|---|
| Por mes | una columna **ACUMULO** y una **USO** |
| Saldo | arrastrado, cruzando gestiones — hay hoja 2025 y hoja 2026 |
| Unidad | **días y horas**, a razón de 1 día = 8 horas |
| Signo | **puede ser negativo**: hay quien está en −2,00 días / −16,00 horas |

El 1 = 8 está verificado contra la planilla (3,00 → 24,00 · 5,75 → 46,00 · 0,37 → 2,96) y coincide
con `jornadasemanal.horasdia`, que ya está cargado.

## La forma: un libro mayor, no un campo

El saldo es **la suma de sus movimientos**, nunca un número guardado que alguien pisa.

```
+ ACUMULA   lo autorizado por el jefe de area, registrado por RRHH
+ APERTURA  el saldo que viene de la planilla a mano
- USA       permiso compensatorio
- PAGA      se cobra en vez de tomarse
= SALDO     en horas; los dias salen de dividir por jornadasemanal.horasdia
```

Es el mismo patrón de [vacaciones](plan_02_vacaciones.md), y por la misma razón: con un campo, el
día que alguien corrija un mes viejo el saldo queda mintiendo y nadie se entera. Con movimientos,
el saldo no puede desincronizarse de su historia porque **es** su historia.

Más simple que vacaciones en un punto: el banco **no tiene período**. No se cierra ni se reabre;
corre continuo y cruza gestiones, como la planilla que llevan a mano.

**Los tres destinos conviven.** Una empresa que solo compensa no usa `PAGA`; una que solo paga no
usa `USA`; una que hace las dos usa las dos. No hay que configurar nada: es el mismo saldo con
tres formas de bajarlo.

### Una sola pantalla para el banco, y la vieja queda donde está

El registro de horas extra del ciclo de planilla **no se toca**: el cliente que lleva años con
bandas lo usa, y no maneja compensatorio. Sigue escribiendo `horasextra` como siempre.

Lo nuevo vive en el banco, y **la acción `PAGA` escribe la misma tabla `horasextra`** del ciclo.
Asi la planilla fiscal no se entera de nada: lee lo de siempre, del mismo lugar. Lo unico que
cambia es quien lo escribe.

Es la misma convivencia que los dos motores de asistencia: **una empresa usa una pantalla o la
otra, no las dos**. Usar las dos dejaria horas pagadas que el saldo no vio, y alguien podria
tomarlas despues como permiso —cobradas y compensadas—.

### El saldo cuelga del contrato principal

Como todo el módulo. Al registrar se resuelve el principal de la persona —la misma regla que usa
la planilla desde el [plan 12](plan_12_planilla_motor.md)—, así que un contrato secundario no
tiene banco propio.

### La unidad se guarda en horas

Se **almacena en horas** y se **muestra en días y horas**. Las horas son lo que da el reloj; los
días son una conversión que depende de `jornadasemanal.horasdia` y puede diferir por empresa.

Guardar días obliga a números como **0,37**, que es lo que hoy aparece en la planilla a mano. En
horas ese mismo saldo es 2,96 y no hay que explicar de dónde salió el 0,37.

### El saldo puede ser negativo

Se permite y **se muestra en rojo**. Es un hecho: alguien tomó tiempo que no había acumulado.
Prohibirlo obligaría a RRHH a inventar un movimiento para cuadrar, que es como se arruina un libro
mayor.

### El permiso compensatorio deja de cargarse a mano

No es una decisión nueva: es la que ya tomó el módulo para las vacaciones, escrita en el código:

> *"Cargada a mano quedaría un día pagado sin respaldo en el kardex y el saldo mentiría en
> silencio."*

Por eso `VACACION` no aparece entre los motivos elegibles de la pantalla de fechas especiales.
**`COMPENSATORIO` pasa a estar en la misma situación**: se carga desde el banco, que crea la fecha
especial *y* el movimiento. En la lista se sigue viendo y se puede filtrar.

## Consumir por horas: el motor nuevo perdió algo que el viejo tiene

El permiso se consume **en horas**, no solo en días completos. Y eso destapa algo que no es del
banco de horas: **el motor nuevo no lee los permisos parciales**.

Una fecha especial de **día completo** le dice al motor "este día no se evalúa": ni pide marcas ni
genera falta. Es todo o nada, y es lo único que el motor nuevo entiende hoy.

Si alguien se va cuatro horas antes con permiso, el día **sí** se evalúa: marcó entrada y salida, y
el motor ve una **salida anticipada de cuatro horas**. El permiso existe y el motor no se entera.

**El motor viejo sí los lee** —`hasPermissionForBandInterval`— y no es un caso de borde: en la
base del cliente que lleva cinco años con bandas, **8.398 de sus 14.819 fechas especiales son
parciales**. La mayoría de sus permisos son por horas.

| | Motor viejo | Motor nuevo |
|---|---|---|
| Lee permisos parciales | sí | **no** |
| Qué perdona | **la banda entera** | nada |

Y el criterio viejo es grueso: si el permiso *toca* la banda por un extremo, perdona la banda
completa. Un permiso de diez minutos borra una jornada de ocho horas.

**H6 es enseñarle al motor nuevo a leerlos, con el criterio correcto: perdonar solo los minutos que
el permiso cubre.**

```
jornada 08:00-18:00,  permiso 14:00-18:00
  se fue 14:00  ->  perdona las 4 horas, no cuenta salida anticipada
  se fue 13:30  ->  perdona las 4 horas y cuenta 30 minutos
```

Es **E2.6** del [plan 01](plan_01_asistencia_produccion.md), la justificación acotada, que estaba
pendiente. **Hay que hacerlo con banco de horas o sin él**: sin esto no se puede migrar al cliente
de bandas, porque el día que pase al motor nuevo sus 8.398 permisos por horas dejan de contar y le
aparecen atrasos y salidas anticipadas a media empresa.

**No hay modelo nuevo ni migración.** `fechaespecial` ya tiene `horainicio`, `horafin` y `allday`,
y la pantalla ya deja cargarlos. Lo que falta es que el motor los mire: los días excusados del
[plan 12](plan_12_planilla_motor.md) solo miran los de día completo.

## El reporte

Es el entregable, y lo que decide si el banco sirve. Dos niveles:

**Saldos** — una línea por persona: acumulado, usado, pagado y **saldo**, en días y horas, para el
período elegido. Es la vista que reemplaza a la planilla a mano.

**Movimientos** — el detalle de una persona: fecha, tipo, horas, motivo, quién lo registró y
cuándo. Es lo que permite contestar *"¿por qué tengo este saldo?"* sin recalcular nada.

Con filtro por persona y por rango de fechas, y exportable.

## Lo que se resolvió al probarlo (2026-09-17)

Tres decisiones que no estaban en el plan original y que salieron de probar la pantalla con datos
reales.

### El permiso por horas también reduce la falta, en proporción

Hasta ahora el permiso por horas solo perdonaba atrasos y salidas anticipadas. Si nadie marcaba,
el día se cobraba entero **aunque hubiera permiso**, que era la contradicción más grande del
motor: el permiso valía para llegar tarde pero no para no venir.

La regla no cambia —cada jornada del día vale su parte, perderlas todas cuesta el día— y se le
agrega una sola cosa: **una jornada se pierde solo en la parte que el permiso no cubre**.

| Caso, en un turno de 07:30 a 19:30 | Cuesta |
|---|---|
| Sin permiso y sin marcas | 1 día |
| Permiso 07:30–19:30, sin marcas | 0 |
| Permiso 07:30–13:30, sin marcas | medio día |
| Permiso 07:30–13:30 y marcas de 13:30 a 19:30 | 0 |

### El permiso de día completo consume las horas del turno de ese día

No una jornada promedio: los turnos son de 4, 7, 8 y 12 horas, y quien no trabaja un turno de 12
deja de trabajar 12. Las horas salen del mismo resolutor que usan la asistencia y la planilla, así
que un cambio de cronograma no deja al banco calculando por su cuenta.

Un día sin jornada programada **no admite permiso**: no hay nada que compensar.

La conversión del **saldo** a días sigue siendo la de `jornadasemanal` —8 h—, porque es una
referencia de lectura y no puede cambiar de persona a persona en la misma columna.

### El permiso que cruza la medianoche es un solo registro

Se carga con una fecha y dos horas. Si la hora de fin es menor que la de inicio, el tramo termina
al día siguiente: 23:30 a 03:30 no puede significar otra cosa. **No hay un check** que lo declare,
porque sería un dato que el sistema ya sabe deducir y que alguien puede olvidar de marcar.

Lo que protege del error de tipeo es otra cosa: **un permiso no puede cubrir más que la jornada de
ese día** —13:30 a 07:30 en un turno de 12 h da 18 h y se rechaza— y la pantalla muestra el
resultado antes de guardar.

### Corregir, sin estados

Un circuito *Pendiente / Aprobado* significaría que alguien aprueba lo que él mismo cargó: la
autorización real ya ocurre afuera, cuando el jefe de área manda la lista. Lo que hacía falta era
**corregir el error de tipeo**, y eso se resuelve con el lápiz de cada fila, el mismo formulario y
el rastro de quién corrigió y cuándo.

La única excepción: **un pago cuya planilla ya se generó no se toca**. Esas horas ya se pagaron, y
corregirlas por atrás dejaría el banco diciendo una cosa y la planilla otra.

## Las tareas

| # | Tarea | Riesgo |
|---|---|---|
| H1 | `HourBankMovement`: contrato, fecha, tipo, horas, motivo, quién y cuándo | bajo |
| H2 | El saldo como suma de movimientos, y la conversión a días | bajo |
| H3 | Registro de lo autorizado, por persona y fecha | bajo |
| H4 | El permiso compensatorio desde el banco: crea la fecha especial y el movimiento | **medio** |
| H5 | `COMPENSATORIO` fuera de los motivos cargables a mano, como ya está `VACACION` | bajo |
| H6 | **E2.6**: el motor lee los permisos parciales y perdona solo los minutos que cubren | **alto** |
| H7 | El pago: baja el saldo y escribe `horasextra` del ciclo | **medio** |
| H8 | Reporte de saldos y de movimientos | bajo |
| H9 | El permiso reduce la falta en proporción; día completo = horas del turno; tramo que cruza la medianoche | **alto** |
| H10 | Corregir y borrar un movimiento, con rastro; el pago cerrado por planilla generada | bajo |

**H4 y H7 son las de riesgo por lo mismo**: son dos escrituras que tienen que ir juntas. Si se crea
la fecha especial y falla el movimiento, el día queda excusado sin descontar del saldo —justo el
agujero que describe el comentario de vacaciones—. Y al borrar hay que revertir las dos.

**H6 es la más cara**, y es la única del plan que sirve para algo más que el banco: sin ella el
motor nuevo no puede reemplazar al viejo en ninguna empresa que use permisos por horas.

## Lo que este plan NO hace

- **No detecta la acumulación sola.** El motor ya calcula los minutos que alguien se queda después
  de su jornada —en julio son **95 días con exceso, unas 63 horas, en 9 personas**— pero al banco
  entra lo que autorizó el jefe de área, no lo que vio el reloj. El dato del motor sirve para otra
  cosa: **contrastar** la lista autorizada contra lo que realmente se marcó.
- **No calcula recargo legal.** En Bolivia la hora extra tiene tope de 2 h diarias y recargo del
  100 %. Si el pago debe salir con recargo, es una regla del cálculo y va aparte.

## Riesgos

**El saldo inicial son 100 y pico de movimientos de apertura.** La planilla a mano trae saldos de
2025 y 2026 —hay gente con 11,50 días—. Se cargan como movimiento de apertura, y conviene hacerlo
con un script a partir de la planilla, no a mano.

**El saldo sigue al contrato principal.** Una baja y recontratación **no lo arrastra**: el contrato
nuevo empieza en cero. En la planilla a mano el saldo es de la persona. Si se quiere que sobreviva
al reingreso, hay que decirlo ahora.

**H6 cambia números en cualquier empresa que ya use permisos por horas.** Hoy el motor nuevo los
ignora, así que al empezar a leerlos van a desaparecer atrasos y salidas anticipadas que hasta
ayer se contaban. En terdemol no pasa —tiene una sola fecha especial, un feriado— pero en el
otro cliente hay 8.398, y ahí la diferencia va a ser grande y hay que explicarla.

## Cómo se verifica

1. Registrar 8 horas autorizadas y ver saldo **1 día / 8 horas**.
2. Permiso compensatorio de un día completo: saldo **0**, y ese día no genera falta en la
   verificación de asistencia.
3. Borrar el permiso: el saldo vuelve a 1 día y la fecha especial desaparece.
4. Permiso de 4 horas: el saldo baja 4 horas y **ese día no cuenta salida anticipada** por esas 4.
5. Pagar 4 horas: el saldo baja y el importe aparece en la planilla del ciclo.
6. Consumir más de lo acumulado: saldo **negativo**, visible y en rojo.
7. `COMPENSATORIO` ya no aparece entre los motivos de la pantalla de fechas especiales.
8. El saldo de cada persona es igual a la suma de sus movimientos, comprobado con una consulta
   sobre todos, no a ojo sobre uno.

## Qué está probado y qué no (al 2026-09-17)

### Probado con datos reales y verificado contra la base

Con ACHOCALLA, contrato 246, julio 2026, turnos de 12 h del GRUPO 1:

- **Registrar** horas autorizadas, **corregirlas** con el lápiz y el rastro *corregido por*.
- **Permiso por tramo** (20/07, 07:30–19:00, 11,50 h): cubre los 28 minutos de atraso de ese día.
- **Permiso de día completo** (22/07): consume **12 h**, las del turno, no una jornada promedio.
- **Permiso que cruza la medianoche** (30/07, 19:30–07:30): un solo registro, 12 h.
- **El tope de la jornada**: 13:30–07:30 en un turno de 12 h se rechaza.
- **Día sin jornada**: avisa y no deja registrar, sin pantalla de error.
- **El efecto en la planilla**, generaciones 006 → 007 → 008 de julio:

| | 006 | 007 | 008 |
|---|---|---|---|
| Minutos de atraso | 99 | 71 | **55** |
| Descuento por atraso | 155,56 | 77,78 | **38,89** |
| Líquido | 2.177,77 | 2.255,55 | **2.294,44** |

  La 007 comprueba el permiso por horas; la 008, que un día perdonado ya no acumula atraso.
  Comparadas las 74 filas de la 007 contra la 008, la única que cambia es la de esta persona.

### Falta probar

- **Borrar** un movimiento con el modal: cancelar, borrar, y que el permiso se lleve su fecha
  especial y el día vuelva a evaluarse.
- **Pagar horas**: que baje el saldo y que escriba `horasextra` del ciclo, y que un pago cuya
  planilla ya se generó quede sin lápiz ni tacho.
- **Saldo negativo**: consumir más de lo acumulado y ver la fila en rojo.
- **El reporte de saldos** por rango, con varias personas y no una sola.
- **La falta que desaparece**: un permiso de día completo el **29/07** —el día sin marcas— tiene
  que dejar la planilla en 29 días, 0 faltas y 0 minutos perdidos.
- **`COMPENSATORIO` fuera de los motivos** de la pantalla de fechas especiales (H5).
- **La carga de los saldos iniciales** desde la planilla de Excel: el script todavía no existe.
- **El reporte impreso** del banco de horas.

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | Este documento lo incluye |
| PLAN | Aprobado |
| **IMPLEMENT** | **H1–H10 hechas y desplegadas; probado lo de arriba, pendiente lo de la lista** |
