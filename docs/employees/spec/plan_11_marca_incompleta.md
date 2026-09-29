# PLAN 11 — La jornada con una sola punta marcada

> Nace de una pregunta durante las pruebas del [plan 10](plan_10_emparejamiento_marcas.md):
> *"un período que tiene registrada solo entrada o solo salida, ¿cómo se contabiliza en la
> planilla: atraso, falta?"*.

## El problema: los dos motores se van a extremos opuestos

Un día en que la persona marcó **una sola punta** —entró y no marcó la salida, o al revés— no es
una falta: hay evidencia de que estuvo. Pero tampoco es un día normal: falta la mitad del dato.

Los dos motores lo resuelven, y de las dos maneras equivocadas:

| | Qué hace | Consecuencia |
|---|---|---|
| **Motor viejo** de bandas | `si marcas del día < 2 → FALTA DE BANDA` | Pierde el día entero **y se le descuenta doble** por la regla del ×2 |
| **Motor nuevo** | La jornada tiene una sesión, aunque incompleta → no es falta, y sin entrada no hay atraso | **Se le paga el día completo** |

La documentación del motor viejo lo dice con todas las letras: *"con una sola marca en todo el
día no se asocia nada → falta de banda completa, **aunque la persona haya trabajado**"*.

Y el motor nuevo hace lo contrario a propósito: la falta se reservó para la jornada **sin ninguna
sesión**, porque el error que veníamos a matar era el peor de todos —alguien que trabajó catorce
horas figurando ausente—.

**El dato real es "no se sabe".** Un sistema que elige un extremo está inventando: el viejo
castiga a quien trabajó, el nuevo paga a quien quizás no vino.

## La regla

Una jornada **se pierde** cuando no tiene asistencia válida, y eso pasa de dos formas:

```
sin ninguna marca             ->  jornada perdida   (la falta de siempre)
con una sola punta marcada    ->  jornada perdida   (lo que este plan agrega)
```

Y el día se cuenta así:

```
pierde TODAS las jornadas del dia    ->  1 dia
pierde ALGUNA pero no todas          ->  1/2 dia
```

Sin umbral de horas, sin duración, sin género.

| Caso | Jornadas del día | Pierde | Falta |
|---|---|---|---|
| Mujer, sábado 07:00–12:00 (5 h) | 1 | la única | **1 día** |
| Mujer, martes 07:00–14:00 (7 h) | 1 | la única | 1 día |
| Producción, turno de 12 h | 1 | la única | 1 día |
| Turno partido 4 h + 4 h | 2 | la mañana | **½ día** |
| Turno partido | 2 | las dos | 1 día |

### Por qué no hay umbral por duración

El motor viejo compara la duración de la banda contra la jornada legal —8 h varón, 7 h mujer— y
con eso decide entre 1 día y ½. Esa comparación **es una adivinanza**: con una sola banda corta no
sabía si era el día completo de esa persona o media jornada, y apostaba.

Nosotros no tenemos que adivinar: **el cronograma dice cuántas jornadas tenía ese día**.

Y cualquier número fijo se rompe con un caso real: en una empresa del grupo la mujer trabaja
07:00–14:00 de lunes a viernes y **07:00–12:00 los sábados**. Ese sábado de 5 horas no es medio
día: es su día completo. Con un umbral de 7 h daría media falta, y estaría mal.

La regla nueva es la del motor viejo **sin la adivinanza**. No se agrega nada: se saca lo que
sobraba. Y de paso **desaparece la pregunta del género** para este cálculo.

### Lo que la regla asume

Que **perder el día propio cuesta un día**, sea de 5 horas o de 12. Es lo coherente con un sueldo
mensual: el día vale `sueldo/30` para todos, y quien trabaja 5 horas diarias ya tiene eso
reflejado en su sueldo.

Si alguna vez se quisiera valorizar distinto una jornada corta, eso no es una regla de asistencia
sino de **cómo se le pone precio a la falta**, y vive en el cálculo de planilla. Por eso el motor
entrega **dos números y no uno**: jornadas perdidas y **minutos programados perdidos**. Con el
segundo, el día que haga falta valorizar distinto, el dato ya está.

## Las tareas

### F1 · La evaluación reporta la jornada perdida

`JourneyEvaluation` gana dos cosas:

- `isLost()` — la jornada no tiene asistencia válida: ninguna marca, o una sola punta.
- `getLostMinutes()` — los minutos que esa jornada exigía.

La falta de siempre pasa a ser un caso de `isLost()`, no un concepto aparte. **Una sola
definición**, en el único lugar que la pantalla y la planilla consultan.

### F2 · El día se arma con las jornadas del día

Un contador por día: cuántas jornadas tenía y cuántas perdió.

```
perdidas == 0            ->  0
perdidas == total        ->  1 dia
0 < perdidas < total     ->  1/2 dia
```

Vive junto al resto del motor, no en la pantalla ni en la planilla.

### F3 · No se cobra dos veces

Si el día se convirtió en falta por marca incompleta, **el atraso de ese día no se cobra además**.
Alguien que llegó 08:12 y no marcó la salida perdería el día *y* los 42 minutos.

No es criterio nuestro: es el que ya usa el [SPEC](req_01_asistencia_produccion.md) para la forma
B —*"un atraso que ya generó ½ día o 1 día no cuenta además para los 4 atrasos. Sería descontar
dos veces"*—.

Con eso la escala queda coherente:

| Situación | Costo |
|---|---|
| No vino | 1 día · **puede ser ×2** |
| Vino, falta una marca | 1 día o ½ · **simple**, y sin atraso encima |
| Vino y marcó las dos | solo atraso o salida anticipada |

### F4 · La falta por marca incompleta no entra en el ×2

La planilla hace `dayAbsences = faltas × 2` como sanción. Esa sanción es para quien **no vino**.

Quien vino y no marcó cometió un error de registro, no una ausencia. Se le descuenta el día
—porque no hay prueba de que trabajó— pero **no se lo sanciona al doble**.

Eso obliga a que la planilla reciba las faltas **separadas en dos**: las de ausencia, que van al
×2, y las de marca incompleta, que van simples.

### F5 · La lista antes de oficializar

Pasar una planilla a **OFICIAL** es decisión de RRHH y este plan no se la quita. Lo que agrega es
**la lista adelante**, antes del paso irreversible:

```
Generar planilla  ->  "23 jornadas con una sola marca se van a cobrar como falta"
                      persona · dia · que falta · 1/2 o 1 dia
                  ->  RRHH corrige la marca, justifica, o acepta
                  ->  OFICIAL
```

Informativa y no bloqueante: el sistema ya no dice *"no sé"*, dice *"esto va a costar tanto,
revisalo antes de firmar"*.

El camino de corrección **ya existe**: la pantalla de marcas permite agregar o arreglar una. Sin
ese camino la regla sería injusta, porque una falla del lector costaría un día sin remedio.

## Alcance

| Archivo | Qué recibe |
|---|---|
| `JourneyEvaluation.java` | F1: `isLost()` y `getLostMinutes()` |
| `AttendanceDay.java` + `attendanceCheck.xhtml` | el estado nuevo a la vista |
| el agregador por día | F2: jornadas del día y perdidas |
| `GeneratedPayrollServiceBean.java` | F3 y F4, al conectar el motor |
| la pantalla de generación | F5: la lista |

**F1 y F2 se pueden hacer ya** y se ven en la verificación de asistencia. **F3, F4 y F5 son parte
de E2.9**, cuando la planilla consuma este motor: hoy la planilla todavía usa el modelo viejo de
bandas y tocarla ahora sería trabajar dos veces.

## Riesgos

**El volumen del primer mes.** En julio hay **272 días-persona con una sola hora marcada** sobre
1.447 con marcas. El número real es bastante menor —muchos son la marca de las 07:30 de un turno
de noche, que **sí cierra** la jornada del día anterior— pero aun así hablamos de decenas de días
por mes. Es el objetivo de la regla —obligar a marcar bien— y a la vez trabajo real para RRHH el
primer mes. Conviene avisarlo antes, no cuando vean la planilla.

**Se penaliza también la falla del lector.** Si el biométrico falla, la persona pierde un día. La
regla solo es justa si corregir la marca es fácil y rápido. Es la razón por la que F5 no es un
adorno.

**Cambia el resultado respecto del motor viejo en las dos direcciones.** Quien hoy pierde el día
*y el doble* va a perder el día simple; quien hoy cobra completo va a perder el día. Al conectar
E2.9 hay que comparar un mes entero contra la planilla vieja y explicar cada diferencia.

## Cómo se verifica

1. **BRAÑEZ 28/07** —solo salida 19:28, turno de 12 h— pasa a **falta de 1 día**, sin atraso
   encima.
2. **BRAÑEZ 31/07** —solo entrada, después de juntar la doble pasada— pasa a falta de 1 día.
3. Un día con **las dos marcas** no cambia en nada.
4. Un turno partido con un bloque sin marcar da **½ día**.
5. El sábado de 5 h de una mujer, sin marcar la salida, da **1 día** y no ½.
6. El total de faltas del mes de ALANIS y BRAÑEZ se explica día por día.

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | Este documento lo incluye: nació de una pregunta, no de un requerimiento aparte |
| PLAN | Aprobado |
| **IMPLEMENT** | **F1 y F2 escritos y compilando · sin verificar contra datos**. F3, F4 y F5 van con E2.9 |

**No hay SQL que aplicar.** Es solo cálculo: no se agregó ninguna columna.

### Lo que quedó escrito

`JourneyEvaluation.isLost()` y `getLostMinutes()` son la definición única, y `DayAbsence` aplica
la regla del día. La pantalla y la planilla consultan las mismas dos clases.

`DayAbsence` recibe **las jornadas de un día** aunque hoy siempre sea una: cuando el
[plan 05](plan_05_horarios_tipo.md) habilite los turnos partidos, el medio día sale solo sin
tocar nada.

### Tres arrastres que aparecieron al recorrer el motor

Al agregar el estado hubo que revisar **todos los lugares que enumeran estados**, que es donde se
escapan siempre:

- **El atraso y la salida anticipada** de una jornada perdida pasan a cero. Es F3, y se pudo
  adelantar porque vive en el motor y no en la planilla.
- **El tiempo adicional** también: acreditar horas extra de un día que se cobra como falta es una
  contradicción que nadie habría mirado hasta que alguien cobrara de más.
- **Los días trabajados** del resumen dejan de contar las jornadas perdidas. Antes solo excluían
  las que no tenían ninguna marca.

### Lo que se ve en pantalla

Un día con una sola punta se pinta como falta y lleva dos etiquetas: **qué falta** —*"Sin marca de
entrada"*— y **cuánto cuesta** —*"Falta por marca incompleta"*—. Son distintas de *"Falta"* a
propósito: acá la persona estuvo, y el día se puede corregir.

El total de faltas del mes admite medios días, así que puede leerse **4,5**.
