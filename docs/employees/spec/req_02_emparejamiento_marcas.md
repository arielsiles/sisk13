# REQ 02 — El emparejamiento de marcas se desfasa cuando faltan marcas

> Detectado el 07/09/2026 probando la verificación de asistencia con julio 2026.
> **Produce descuentos grandes y equivocados.** Hay que corregirlo antes de conectar el motor
> a la planilla (E2.9 del [plan 01](plan_01_asistencia_produccion.md)).

## Los dos casos reales

### JUAN CARLOS ARUQUIPA — 1.898 minutos de atraso inventados

Sus marcas de la primera semana de julio, y lo que el motor concluyó:

| Día | Marcas | El motor leyó | Resultado |
|---|---|---|---|
| MIÉ 01/07 | `17:09` | entrada 17:09 → salida 07:45 *(del día 2)* | Atraso **479 min** |
| JUE 02/07 | `07:45` `17:15` | entrada 17:15 → salida 07:43 *(del día 3)* | Atraso **485 min** |
| VIE 03/07 | `07:43` `17:16` | entrada 17:16 → sin salida | Atraso **486 min** |

Su horario es 09:00–17:30. La lectura correcta salta a la vista: el `17:09` del día 1 es una
**salida** —le falta la entrada de ese día— y a partir de ahí **todo quedó corrido un lugar**.

El resumen del mes daba **19 días trabajados, 3 faltas y 1.898 minutos de atraso**. Ninguno de
esos números es real.

### YOSELIN CHOQUE — "salió 454 minutos antes"

| Día | Marcas | El motor leyó | Resultado |
|---|---|---|---|
| VIE 03/07 | `09:21` `09:51` `17:31` | entrada 09:21 → salida 09:51 | Salió **454 min** antes |

Número impar de marcas: emparejó la primera con la segunda y dejó el `17:31` huérfano. La lectura
razonable es que entró 09:21, el `09:51` es espurio y salió 17:31.

## Las causas — son dos

### Causa A · Una sesión que cruza la medianoche entra en una jornada que no cruza

El caso del 01/07 es más grave de lo que parece, y **no depende del emparejamiento**:

> Su jornada es 09:00–17:30 y **no cruza la medianoche**. Una sesión que empieza a las 17:09 y
> termina a las 07:45 del día siguiente **no puede pertenecerle**, con cualquier emparejamiento.

`SessionScheduleMatcher` asocia por solapamiento y toma la jornada con más minutos en común. Esa
sesión de 14,6 h se solapa 21 minutos con la jornada —de 17:09 a 17:30— y con eso gana, porque no
hay ninguna otra que compita.

Falta una condición de **plausibilidad**: una sesión que cruza la medianoche solo puede
pertenecer a una jornada que cruza la medianoche. El cruce es normal en el turno de noche de
producción; en un horario fijo de oficina es imposible.

Esta causa se corrige sola y es independiente de la otra.

### Causa B · El emparejamiento cronológico se desfasa

El motor arma sesiones en dos pasos: primero empareja marcas, después asocia cada sesión a la
jornada que le corresponde. El emparejamiento usa el indicador de entrada/salida del biométrico
y, cuando no es confiable, **empareja cronológicamente: primera con segunda, tercera con cuarta**.

Verificado en los datos: **el indicador es basura**. En una sola semana de ARUQUIPA:

| Marca | Indicador dice | Realidad |
|---|---|---|
| 03/07 `07:43` | salida | entrada |
| 06/07 `07:43` | salida | entrada |
| 06/07 `17:03` | entrada | salida |
| 07/07 `17:09` | entrada | salida |
| 08/07 `07:59` | salida | entrada |

El motor **detecta bien** que no es confiable y cae al emparejamiento cronológico. Ahí está el
segundo defecto:

> **El emparejamiento cronológico asume que la primera marca es una entrada.**

Cuando falta una marca —la primera entrada del período, o una del medio— todo lo que sigue queda
corrido, y cada día produce un atraso enorme y falso. No es un error de un día: **contamina el
resto del período**.

## Por qué importa

Estos dos casos aparecieron al mirar **dos personas**. El error no es cosmético: son minutos de
atraso y faltas que se descuentan del sueldo. Si el motor se conectara hoy a la planilla, se
pagaría mal.

Y el modo de falla es el peor posible: **silencioso y verosímil**. Un atraso de 479 minutos se
nota, pero uno de 40 pasaría sin que nadie lo mire.

## El indicador no se puede usar como solución

Dato de campo del usuario, y coincide con lo medido: **el indicador acierta en un 60% o menos**.
Las causas son operativas y no se van a arreglar solas — la gente se equivoca al marcar, corrige
marcando de nuevo, marca dos veces, o el equipo lo registra mal.

Esto descarta de entrada la salida fácil de "confiar más en el indicador" o "afinar el umbral de
confiabilidad". **Cualquier solución tiene que funcionar sin él.**

## Lo que hay que cambiar, en una frase

El horario tiene que **guiar la interpretación** de las marcas, y no al revés.

Hoy el motor empareja a ciegas y después busca a qué jornada pertenece cada sesión. Sabiendo que
a esa persona le tocaba 09:00–17:30, una marca a las 17:09 no puede ser una entrada.

**Para la causa A**, la regla es concreta y chica: una sesión que cruza la medianoche solo se
puede asociar a una jornada que cruza la medianoche.

**Para la causa B**, direcciones evaluadas sin decidir todavía:

1. **Emparejar dentro de la jornada.** Para cada jornada, tomar las marcas cercanas y decidir
   entrada/salida por cercanía al inicio y al fin. Es el cambio más grande y el más robusto, y el
   único que funciona bien sin indicador.
2. **Anclar el primer par.** Decidir si la primera marca es entrada o salida según a qué extremo
   de su jornada está más cerca. Cambio chico, resuelve el desfase inicial pero no el número
   impar del medio.
3. **Emparejar por ventana de jornada** en lugar de por todo el período, para que un día roto no
   contamine a los siguientes.

La 1 y la 3 se pueden combinar. La 2 sola es un parche.

Conviene arreglar **A primero**: es chica, independiente, y por sí sola ya elimina el atraso de
479 minutos del 01/07.

## Alcance

- La causa A toca `SessionScheduleMatcher`; la causa B, `WorkSessionBuilder`. Las dos son el
  corazón del motor de asistencia.
- **No** toca el cronograma, los grupos, los horarios ni los feriados.
- La pantalla de verificación no cambia: es la que dejó ver el problema, y va a ser la que
  confirme el arreglo.

## Verificación exigida

Reproducir los dos casos de arriba y que den la lectura correcta, **y** volver a verificar julio
2026 completo para las personas ya cargadas confirmando que ningún día que hoy está bien se
rompe. Los datos ya están: 4.537 marcas de 72 personas.

## Estado

| Etapa SDD | Estado |
|---|---|
| **SPEC** | Este documento |
| PLAN | Pendiente |
| IMPLEMENT | No iniciado |
