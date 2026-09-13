# PLAN 10 — La jornada guía el emparejamiento de las marcas

> Resuelve [req_02](req_02_emparejamiento_marcas.md). Es lo que bloquea **E2.9**: conectar el
> motor de asistencia a la planilla.

## El diagnóstico, en términos del código

El motor tiene dos etapas y el problema está en la frontera entre las dos:

```
WorkSessionBuilder      marcas   → sesiones     NO conoce el horario
SessionScheduleMatcher  sesiones → jornadas     recién acá aparece el horario
```

`WorkSessionBuilder` lo dice en su propio comentario: *"la única que no conoce el horario"*. Eso
parecía una virtud —una etapa simple, sin dependencias— y es la causa de los dos errores.

Empareja a ciegas: primera con segunda, tercera con cuarta. Y **asume que la primera marca es una
entrada**. Cuando falta una, todo lo que sigue queda corrido.

El límite de 16 horas que ya existe no alcanza: la sesión falsa de JUAN CARLOS —17:09 del día 1 a
07:45 del día 2— dura **14,6 horas** y pasa por debajo.

## La corrección, en una frase

> **La jornada guía el emparejamiento.** Sabiendo que a esa persona le tocaba 09:00–17:30, una
> marca a las 17:09 no puede ser una entrada.

## Una corrección al orden que proponía el SPEC

El SPEC decía *"conviene arreglar A primero: por sí sola ya elimina el atraso de 479 minutos"*.
**Es cierto y es insuficiente.**

Con solo la regla de medianoche, la sesión 17:09→07:45 deja de poder asignarse a una jornada que
no cruza la medianoche. Pero entonces no se asigna a **ninguna**, y la jornada del 01/07 queda sin
sesión. Y una jornada sin sesión es **falta**.

El atraso inventado de 479 minutos se convertiría en una **falta inventada**. Deja de ser un
número absurdo y pasa a ser uno verosímil, que es peor.

La regla de medianoche es un **blindaje**, no el arreglo. Va, pero junto con el arreglo.

## Lo que el motor ya hace bien y no hay que romper

Verificado leyendo `JourneyEvaluation`: **no calcula atraso si no hay entrada**, ni salida
anticipada si la salida es supuesta. El motor nunca inventó un castigo sin dato.

El `17:09` le llegó etiquetado como *entrada*, y esa etiqueta se puso una etapa antes. **La falla
es de atribución, no de evaluación.** Este plan toca solo la atribución.

## Una jornada a la vez

Confirmado con el usuario, y es la restricción que más simplifica el diseño:

> Una persona **no tiene dos jornadas simultáneas**. El caso más parecido es el préstamo entre
> grupos: la persona queda alcanzada por el cronograma de dos grupos, pero **trabaja en uno solo**
> —el que le corresponde ese día— y marca una sola vez.

El resolutor de cuatro capas ya lo garantiza: prefiere el préstamo sobre la pertenencia base y
devuelve **una** jornada por día.

Eso deja fuera del alcance los casos que en otros sistemas obligan a modelos complicados:
jornadas que se solapan de verdad —programado y de guardia a la vez—, o dobletes donde una misma
marca es salida de una jornada y entrada de la siguiente. Si algún día aparecen, se resuelven
entonces; hoy agregar esa maquinaria sería complejidad sin caso.

### Pero el préstamo sí importa para este plan

Porque **la misma persona cambia de patrón a mitad del período**: dos semanas de día en su grupo,
diez días de noche en el grupo al que se la prestó, y de vuelta.

Esto descarta de entrada cualquier solución basada en el hábito de la persona —*"esta persona
suele entrar 07:30"*—: el préstamo la rompe, y los préstamos son normales en producción.

El emparejamiento guiado por la jornada es **inmune** a eso, porque lee el horario que
corresponde a ese día y no una costumbre. Es un argumento a favor del diseño elegido, no una
complicación.

## Las etapas

### E1 · Una sesión que cruza la medianoche solo entra en una jornada que cruza

En `SessionScheduleMatcher`, al elegir la jornada con más solapamiento, se descarta la
combinación imposible.

`ScheduledJourney.crossesMidnight()` **ya existe**, así que solo hay que agregar el equivalente en
`WorkSession` y una condición en el bucle.

La asimetría importa: una sesión que **no** cruza la medianoche sí puede pertenecer a una jornada
que sí cruza —quien entra 19:30 y sale 23:00 en un turno de noche—. Lo imposible es al revés.

No aplica a sesiones incompletas: el tramo que se les presta para medir el solapamiento es
artificial y no significa que la persona haya cruzado la noche.

### E2 · Las marcas se reparten entre las jornadas, y ahí se emparejan

Es el cambio de fondo. En lugar de emparejar a ciegas y después preguntar de quién es cada
sesión, **primero se reparte y después se empareja**:

```
1. Cada jornada abre una VENTANA:
        [ inicio − margen antes ,  fin + margen después ]
   recortada por el PUNTO MEDIO hacia la jornada anterior y la siguiente.

2. Cada marca entra en la ventana que la contiene.
   Si no cae en ninguna, es trabajo fuera de horario.

3. Dentro de cada jornada:
        la marca más temprana  →  entrada
        la marca más tardía    →  salida
        una sola marca         →  entrada o salida, según el borde más cercano
        las del medio          →  sobrantes, se reportan
```

### El recorte por punto medio, que es lo que hace funcionar el turno partido

Sin él, un turno partido de 08:00–12:00 y 14:00–18:00 con los márgenes por defecto abre ventanas
que **se pisan cuatro horas**: la del primer bloque llegaría hasta las 16:00, o sea adentro del
segundo. Y a las 13:00 en punto una marca queda a 60 minutos de las dos, sin forma de decidir.

Con el recorte, el punto medio es las 13:00 y las ventanas quedan `06:00–13:00` y `13:00–22:00`.
Sin solape y sin empate, para cualquier combinación:

| Turno partido | Punto medio | Bloque 1 | Bloque 2 |
|---|---|---|---|
| 08:00–12:00 · 14:00–18:00 | 13:00 | 06:00 – 13:00 | 13:00 – 22:00 |
| 07:00–12:00 · 14:00–17:00 | 13:00 | 05:00 – 13:00 | 13:00 – 21:00 |
| 08:00–12:00 · 13:00–17:00 | 12:30 | 06:00 – 12:30 | 12:30 – 21:00 |

**Y solo actúa cuando la jornada vecina está cerca.** Entre días consecutivos el punto medio cae
más lejos que el margen, así que no recorta nada y **queda hueco**: con el turno de 12 horas, la
ventana cierra 23:30 y la del día siguiente abre 05:30. Ese hueco es lo que permite que una marca
pueda ser *trabajo fuera de horario*; si las ventanas se tocaran siempre, ninguna podría serlo.

| Situación | Quién decide la frontera |
|---|---|
| Jornadas lejos — días consecutivos | **el margen**, y queda hueco |
| Jornadas cerca — turno partido | **el punto medio**, sin hueco ni solape |

Es una sola regla que se comporta distinto según el caso, en lugar de dos reglas que hay que
saber cuándo aplicar.

**En el empate exacto gana la jornada anterior**: ya está abierta y necesita su salida, mientras
que la siguiente todavía no empezó. Aplica en el punto medio justo y cuando el fin de una coincide
con el inicio de la otra —lo que pasa el día en que termina un préstamo—.

Aplicado a los casos del SPEC:

| Caso | Marcas | Jornada | Lectura nueva |
|---|---|---|---|
| JUAN CARLOS 01/07 | `17:09` | 09:00–17:30 | **salida** sin entrada — a 21 min del fin, a 8 h del inicio |
| JUAN CARLOS 02/07 | `07:45` `17:15` | 09:00–17:30 | entrada 07:45, salida 17:15 |
| YOSELIN 03/07 | `09:21` `09:51` `17:31` | 09:00–17:30 | entrada 09:21, salida 17:31, **sobra** el 09:51 |

El desfase desaparece por construcción: **cada jornada se resuelve sola**. Un día roto ya no
contamina a los siguientes porque no hay un hilo cronológico que se corra.

Y el indicador del biométrico **deja de sostener el resultado**. Sigue usándose cuando es
confiable —no cuesta nada— pero con 60% de acierto, que no dependa de él es la diferencia entre
funcionar y no.

### E3 · La ventana se define en el turno, con valor propio

Dos campos en `TURNO`: **margen antes** y **margen después**. Obligatorios, y **precargados** al
crear el turno: cada turno queda dueño de sus números, sin heredar nada en tiempo de ejecución.

**Por qué propios y no heredados de una constante.** Con herencia, el valor real vive en el código
y es invisible: dentro de seis meses, cuando alguien cuestione un atraso de marzo, no hay forma de
saber qué ventana aplicó. Y cambiar la constante movería **los veinte turnos a la vez**, en
silencio. Con valor propio, se abre el turno y se ve.

No contradice el *"por referencia, no por copia"* del [plan 05](plan_05_horarios_tipo.md): ahí lo
compartido es el **horario**, que muchas personas usan de verdad. Acá el margen es una propiedad
del turno, como su hora de inicio. Nadie espera que veinte turnos compartan su hora de inicio.

**Por qué en el turno y no en el horario tipo** —que sería más elegante—: los turnos de producción
se asignan por **cronograma de grupo**, no por horario tipo. Si el margen viviera ahí, la mitad de
la gente no tendría de dónde heredarlo.

#### La precarga mira la duración del turno

La ventana siempre mide *duración + margen antes + margen después*. Con 2 y 4 fijos:

```
ventana ≤ 2 × duración     ⟺     duración ≥ 6 horas
```

No es un criterio inventado, sale de la aritmética: **los turnos de 6 horas o más funcionan con
2/4; los de menos, no**. Verificado sobre dieciséis tipos de turno —producción de 12 h, rotativos
de 8 h, administrativos, medios turnos de 3, 4 y 5 horas—: los once largos quedan entre 1,5× y
1,75×, y los cinco cortos entre 2,2× y 3×.

Por eso la precarga no es un número fijo:

| Duración del turno | Precarga |
|---|---|
| 6 h o más | 2 h antes · 4 h después |
| menos de 6 h | 1 h antes · 2 h después |

Con eso, **ninguno de los dieciséis casos necesita que nadie intervenga**. Sigue siendo precarga
—visible y editable—, no una fórmula escondida: la diferencia es que arranca bien en vez de
arrancar mal en cinco de dieciséis.

#### La ventana se muestra en pantalla

Debajo de los dos campos, la pantalla de turnos muestra **el resultado**, recalculado al escribir:

```
Turno            [ 08:00 ]  a  [ 12:00 ]
Margen antes     [ 1 ] h
Margen después   [ 2 ] h

  Ventana:  07:00 – 14:00
```

Es lo que hace que dos números abstractos se entiendan sin explicación: quien crea un turno corto
ve al instante si la ventana quedó desproporcionada. **La persona es la parte adaptativa**, y es
más confiable que una fórmula que haya que explicar.

Con una aclaración en la misma pantalla: **la ventana que se muestra es la del turno solo.** Si el
turno forma parte de un partido, la frontera entre bloques la decide el punto medio.

#### No se confunden con las tolerancias que ya existen

`toleranciaentrada` y `toleranciasalida` —10 y 5 minutos— deciden **si está atrasado**. El margen
decide **de qué jornada es la marca**. Usar las primeras para lo segundo dejaría al `17:09` de
JUAN CARLOS sin jornada.

### E4 · Tres validaciones al guardar el turno

Con configuración viene el riesgo de que alguien escriba un número que arruine la atribución. No
se prohíbe: **se avisa**, porque puede haber un caso legítimo detrás.

| | Cuándo | Qué dice |
|---|---|---|
| 1 | La ventana toca o pisa la del mismo turno al día siguiente | *No se podrá distinguir un día del otro* |
| 2 | Margen antes menor a 30 minutos | *Una marca apenas anticipada quedaría fuera y la jornada sin entrada* |
| 3 | La ventana dura más del **doble** que el turno | *Muy amplia para la duración de este turno* |

La **1** atrapa el margen enorme: con 10 h después en el turno de 12 h, la ventana cierra 05:30 y
la del día siguiente abre 05:30. Se tocan, y una marca de madrugada deja de poder atribuirse.

La **2** atrapa el caso traicionero, porque *parece* prolijo: con margen 0, quien marque 07:58
para un turno de 08:00 queda fuera y su jornada se queda sin entrada.

La **3** es la que atrapa el turno corto mal configurado, y dispara exactamente donde corresponde
por la aritmética de más arriba: solo en turnos de menos de 6 horas con la precarga larga.

Con los turnos reales y su precarga, ninguna dispara. El de 12 horas cierra 23:30 y el del día
siguiente abre 05:30, con **seis horas de hueco**. Ese hueco es lo que permite que una marca pueda
ser *fuera de horario*; si las ventanas se tocaran, ninguna marca podría serlo nunca.

### E5 · Sin jornada, el día se corta a las 03:00

Quien no tiene horario asignado no tiene contra qué repartir. Ahí se conserva el emparejamiento
cronológico, **pero encerrado dentro de cada día**, con el corte a las 03:00 —el *day divide* de
la industria— en lugar de a medianoche.

Es un cambio chico con un efecto grande: **un día roto deja de contaminar a los siguientes**. Es
la mitad del problema de JUAN CARLOS resuelta sin depender de ningún horario.

### E6 · Nada se descarta

Se mantiene la regla que el motor ya tiene: lo que no encaja **sale como incidencia, nunca como
falta**. Las marcas sobrantes de E2 son un tipo nuevo —*marca sin par*— y las que no caen en
ninguna ventana siguen siendo *trabajo fuera de horario*.

Es lo que permite que RRHH vea que el `09:51` de YOSELIN existió, en vez de que el sistema lo
borre en silencio.

## La verificación: el período completo, no dos casos

Esta parte no se puede saltear.

Hay **4.537 marcas de 72 personas** de julio 2026 cargadas. La verificación es correr el motor
**antes y después** sobre todas y comparar día por día:

| Qué mirar | Qué se espera |
|---|---|
| Días que cambian | Los conocidos, y ninguno más sin explicación |
| Días que hoy están **bien** | **Ninguno se rompe.** Es la condición que manda |
| Atrasos totales del mes | Bajan, y la baja se explica caso por caso |
| Faltas | No aparecen faltas nuevas |

Dos casos ya validados a mano sirven de piedra de toque: **ALANIS y BRAÑEZ** cerraron julio en
**168 h**, coincidiendo con la planilla manual. Si después del cambio siguen dando 168 h, no se
rompió lo que funcionaba.

Y **al menos una persona con préstamo** en el período, para verificar que el cambio de patrón a
mitad de mes se lee bien en los dos tramos.

Sin esa comparación no hay forma de saber si se arregló un caso y se rompieron veinte: el motor
hoy acierta en la enorme mayoría de los días, y ese es el activo a proteger.

## La precondición de E2.9, que no es parte de este plan

Este plan deja de inventar atrasos. **No alcanza para conectar la planilla.**

Falta la regla que tienen todos los sistemas serios: **un período con marcas faltantes sin
resolver no se cierra**. Alguien las justifica o las corrige, y recién entonces se paga.

Sin eso, el día que falte una marca la planilla no va a inventar un atraso —eso lo arregla este
plan— pero va a pagar el día completo como si nada hubiera pasado. Silencioso otra vez, en la
otra dirección.

Va en **E2.9**, y se anota acá porque es la razón por la que el plan 10 solo no habilita el paso
siguiente.

## Alcance

| Archivo | Qué recibe |
|---|---|
| `WorkSession.java` | si la sesión cruza la medianoche |
| `SessionScheduleMatcher.java` | E1: la combinación imposible se descarta |
| `WorkSessionBuilder.java` | E2 y E5: recibe las jornadas, reparte y después empareja |
| `ScheduledJourney.java` | la ventana, calculada del turno |
| `WorkShift.java` + SQL + `workShift.xhtml` | E3: los dos márgenes y la ventana a la vista |
| `WorkShiftAction.java` | E3: la precarga según duración · E4: las tres validaciones |
| `AttendanceIncidenceType.java` | E6: el tipo *marca sin par* |
| `AttendanceCheckServiceBean.java` | pasa las jornadas al constructor |

**No** toca el cronograma, los grupos, los préstamos, las excepciones ni los feriados. La
resolución de qué jornada le tocaba a cada uno ya funciona y este plan la consume, no la cambia.

La pantalla de verificación **no cambia**: es la que dejó ver el problema y la que va a confirmar
el arreglo.

## Riesgos

**Es el corazón del motor.** De acá salen los minutos de atraso y las faltas que se descuentan del
sueldo. Un error acá se paga, literalmente.

**Se invierte una dependencia.** `WorkSessionBuilder` pasa a conocer el horario. Es lo correcto
—el horario es lo que le da sentido a una marca— pero deja de ser una clase que se pueda razonar
sola.

**El turno partido no está probado con datos.** El recorte por punto medio se diseñó contra los
casos que planteó el usuario —08:00–12:00 con 14:00–18:00, y 07:00–12:00 con 14:00–17:00— pero en
julio 2026 no hay ninguno cargado. Cuando el [plan 05](plan_05_horarios_tipo.md) los habilite, la
verificación tiene que repetirse con un turno partido real.

**Puede haber días hoy correctos por compensación**: dos errores que se cancelan. Al arreglar uno
aparece el otro. Es exactamente lo que la comparación completa tiene que dejar ver, y por eso no
alcanza con probar los dos casos conocidos.

**El día en que termina un préstamo.** Si el último turno de noche termina 07:30 y ese mismo día
el grupo base tiene turno de día desde 07:30, el cronograma le asigna a la persona dos jornadas
consecutivas sin descanso. No es un problema de emparejamiento —la marca de las 07:30 cierra la
jornada que ya estaba abierta— pero **la jornada de día quedaría como falta**. Es un problema de
planificación, no de asistencia, y hay que mirarlo cuando se pruebe un préstamo real.

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | [req_02](req_02_emparejamiento_marcas.md) |
| PLAN | Este documento |
| **IMPLEMENT** | **E1 a E6 escritos y compilando · sin verificar contra datos** |

**Hay SQL que aplicar antes de desplegar** —se agregan dos columnas y el sistema valida el
esquema al arrancar—: sección **22** de `query_v6.1.0_terdemol.sql`, que crea `margenantes` y
`margendespues` y los siembra según la duración de cada turno.

La siembra se probó contra los cuatro turnos reales: los cuatro dan 6 horas o más —el de noche
calcula bien sus 720 minutos cruzando la medianoche— así que los cuatro quedan en 120/240.

### Lo que falta, y es lo que decide si esto sirve

**La comparación del período completo.** Nada de esto está verificado contra las 4.537 marcas.
Trazar el caso de JUAN CARLOS a mano sobre el código dice que el `17:09` ahora se lee como salida
—está a 21 minutos del fin y a 489 del inicio— pero un trazado a mano no es una verificación.

Hasta que esa comparación se corra, el plan está **escrito, no probado**.

### Notas de implementación

**La sesión recuerda su jornada.** Cuando el emparejamiento fue guiado por el horario, la sesión
guarda a qué jornada pertenece y `SessionScheduleMatcher` **respeta esa decisión** en lugar de
volver a deducirla por solapamiento. Calcular una cosa y después recalcularla distinto es como se
cuelan las inconsistencias.

**Lo que no cae en ninguna ventana no se descarta.** Se empareja como se pueda —por indicador o
cronológicamente, acotado por el corte de las 03:00— para que la etapa siguiente lo reporte como
trabajo fuera de horario.

**Las incidencias todavía no llegan a la pantalla.** `MARK_WITHOUT_PAIR` se registra pero no se
muestra: la verificación de asistencia no dibuja incidencias hoy. Es lo que hay que agregar para
que RRHH pueda ver que el `09:51` de YOSELIN existió.
