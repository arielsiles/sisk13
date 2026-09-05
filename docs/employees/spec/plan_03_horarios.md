# PLAN 03 — Turnos, cronogramas y asignación de horarios

> Reemplaza la etapa **E3** de [plan_01](plan_01_asistencia_produccion.md) y cubre RF‑11, RF‑08 y RF‑03.
> **Ninguna línea de código escrita a partir de este documento.** Esperando aprobación.

## Por qué se reescribe en vez de reutilizar

Decisión del usuario: el modelo actual —`HoraryBand` + `Tolerance` + `Limit`— no se extiende, se
reemplaza. Y los datos le dan la razón:

| | |
|---|---|
| Bandas horarias cargadas | 10, todas 08:00–16:00 |
| Asignaciones a contratos | 10, para **2 empleados** |
| Asignaciones **vigentes hoy** | **0** — vencieron el 31/12/2024 |

**No hay nada que migrar.** Eso elimina las dos tareas de mayor riesgo del plan 01: la
convivencia entre modelos (E3.4) y la migración de asignaciones (E3.7). Las 4.683 asignaciones
que menciona el SPEC son de la otra base, no de esta.

## Cómo planifican hoy

El cronograma real es una planilla pintada a mano, una fila por grupo y una columna por día:

```
GRUPO      13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 ...
Grupo 1     6 12 12 12  6  8  8 12 12 12 12 12  8  8  6      ← semana noche, semana día
Grupo 2    12 12 12 12 12  8  8  6 12 12 12  6  8  8 12      ← al revés
```

La celda guarda **horas**; el **color** guarda el concepto:

| Color | Qué es | Horas |
|---|---|---|
| Amarillo | Turno día 07:30–19:30 | 12, o **6** lunes y viernes por el cruce de días |
| Azul | Turno noche 19:30–07:30 | ídem |
| Verde | Descanso pagado | 8 |
| Rojo | Domingo o feriado, pagado | 8 |

**La rotación es cíclica** —cada grupo hace una semana de noche y una de día— pero eso **no
alcanza para generarla sola**. En julio el Grupo 1 arranca recién el día 13, las horas van entre
6, 8 y 12 según el día, y los grupos entran y salen a mitad de mes. Un cronograma generado
automáticamente saldría parecido pero no igual, y **encontrar y corregir lo que quedó mal cuesta
más que planificarlo de una vez**.

**Verde y rojo son sólo del reporte.** Confirmado con el usuario: existen para explicar cómo se
llega a las 240 horas del mes en la planilla manual, y **no llegan a la planilla de sueldos**.
El motor no los evalúa: son días sin jornada.

## El modelo

**1 · Turno.** Hora de inicio, hora de fin, si cruza medianoche, tolerancia de entrada y
tolerancia de salida anticipada. Nada más — las ocho tolerancias de hoy sobran. Ya está resuelto
en `ScheduledJourney` (E2.2); acá se persiste como catálogo.

**2 · Grupo.** Un conjunto de personas que rota junto. La persona pertenece a un grupo y **se
mueve entre grupos según planificación**.

**3 · Cronograma por grupo.** Lo **planifica** producción o RRHH en una grilla —una fila por
grupo, una columna por día—, marcando en cada celda turno día, turno noche, descanso o no
laborable. Se carga por semana y tiene que estar completo antes de fin de mes.

**Nada se genera solo.** Lo que el sistema aporta son atajos que el planificador dispara cuando
quiere:

| Atajo | Para qué |
|---|---|
| Pintar arrastrando | Marcar un rango de días de un grupo de una pasada, como en la planilla |
| **Copiar la semana anterior** | La semana 3 es igual a la 1: un clic en vez de cinco celdas |
| **Copiar otro grupo invirtiendo turnos** | El Grupo 2 es el Grupo 1 con día y noche cambiados |
| Totales en vivo | Horas y días del grupo mientras se planifica, contra el objetivo del mes |

Esos atajos cubren la rotación cíclica sin imponerla: hacen rápido lo repetitivo y no dejan nada
que haya que ir a buscar y corregir.

**4 · Horario fijo del contrato.** Para administrativos, que no llevan cronograma.

**5 · Excepción por persona y día.** Un cambio puntual que gana sobre todo lo demás.

### Cómo se resuelve qué jornada le tocaba a alguien un día

```
1. Excepción de esa persona ese día
2. Cronograma del grupo al que pertenecía esa semana      producción
3. Horario fijo del contrato                              administrativos y mantenimiento
4. Sin nada  →  no se evalúa asistencia
```

Una empresa sin producción usa sólo la capa 3. Una con turnos usa las cuatro. **El motor de E2
no se entera**: recibe una lista de jornadas y no le importa de dónde salieron.

Cuando mantenimiento arme sus grupos, pasa de la capa 3 a la 2 sin tocar código.

### Horas extra

Una sola regla para todas las áreas: **es extra lo que excede la jornada programada**. Para
administración la jornada es de 8 h, así que 9 h son una extra; para producción la jornada es de
12 h, así que 12 h no son extra. No hace falta una regla por área.

El registro y la aprobación de las extras son RF‑05, no entran acá.

## Las tareas

| # | Tarea | Riesgo |
|---|---|---|
| H1 | Catálogo de **turnos** con sus dos tolerancias | bajo |
| H2 | **Grupos** y pertenencia de la persona al grupo, con vigencia | medio |
| H3 | **Cronograma por grupo**: la grilla de planificación | medio |
| H4 | Los atajos: pintar, copiar semana, copiar grupo invirtiendo turnos | medio |
| H5 | Totales en vivo mientras se planifica | bajo |
| H6 | Resolver la jornada de una persona en una fecha, según el orden de arriba | **alto** |
| H7 | Horario fijo del contrato para administrativos | bajo |
| H8 | Excepción por persona y día | bajo |
| H9 | Asignación masiva de personas a grupos, con selección múltiple | medio |
| H10 | Inmutabilidad: turno o cronograma usado en una planilla oficial queda cerrado | bajo |

**H6 es el corazón.** Es la función que E2 consume y la única que conoce las cuatro capas.

**H3 y H4 son la cara visible.** Si planificar en el sistema no es más rápido que en la
planilla, nadie lo va a usar y el cronograma va a llegar tarde o incompleto.

**Verificación:** reproducir el cronograma de julio 2026 y que dé Grupo 1 = 156 h / 19,5 días y
Grupo 2 = 168 h / 21 días, que son los totales de la planilla real.

## Lo que este plan NO hace

- No toca el motor de cálculo de planilla. Eso es el último paso de E2.
- No migra el modelo viejo: no hay nada vigente que migrar. `HoraryBand` queda como está hasta
  que se retire el motor viejo.
- No implementa el registro de horas extra (RF‑05).
- No implementa el banco de horas ni el cierre mensual de las 240 h (RF‑09).

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | RF‑11, RF‑08 y RF‑03 del SPEC 01 |
| **PLAN** | Este documento — esperando aprobación |
| IMPLEMENT | No iniciado |
