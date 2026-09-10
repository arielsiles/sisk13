# PLAN 05 — Horarios tipo: patrones asignables y turnos partidos

> **Aprobado, todavía sin implementar.** Es el más grande de los tres que salieron de las
> pruebas de julio 2026, y el menos urgente: hoy hay 3 personas con horario fijo cargado.

## Los dos problemas

### 1 · El horario se copia por persona

`horariocontrato` guarda, para cada persona, un turno por día de la semana. Si 25 personas
comparten horario y mañana cambia la hora de entrada, **hay que editar 25 fichas**.

Es el mismo error que la fecha de fin del contrato del [plan 04](plan_04_contrato.md): un dato
que se repite a mano termina desincronizado. Y no es una molestia teórica — el usuario lo
reportó después de cargar tres personas.

### 2 · No existe el turno partido

Un día del horario fijo admite **un solo turno**. Un turno partido —mañana 08:00–12:00 y tarde
14:00–18:00— no se puede representar: al pintar el segundo, reemplaza al primero.

Confirmado con el usuario: son muchas personas, y en otras empresas del grupo **son todas así**.

## Cómo lo resuelven los sistemas serios

Tres niveles, no dos:

| Nivel | Qué es | SAP | Odoo | UKG |
|---|---|---|---|---|
| 1 | La jornada de **un día** | Daily Work Schedule | línea del calendario | Shift |
| 2 | El **patrón semanal** con nombre | Period Work Schedule | Working Schedule | Schedule Pattern |
| 3 | La **asignación** a la persona, con vigencia | Work Schedule Rule | calendario del contrato | Pattern assignment |

El nivel 1 ya existe: es `TURNO`. Falta el 2, y convertir el horario por persona en el 3.

El modelo más parecido a este caso es el de **Odoo**: un *Working Schedule* es un nombre con una
lista de líneas `(día de la semana, hora desde, hora hasta)`, y el contrato **apunta** a ese
calendario. El turno partido no es un caso especial: son **dos líneas para el mismo día**.

> **La idea que importa: asignación por referencia, no por copia.**
> 25 personas no tienen 25 horarios iguales: tienen **el mismo horario**.

## El modelo

```
TURNO                     ya existe — la jornada de un día
   ↓ varias líneas por día → el turno partido sale solo
HORARIO TIPO              "Administrativo"  "Turno partido"  "Portería"
   ↓ asignación con vigencia
CONTRATO de la persona
```

`horariocontrato` deja de guardar *un turno por día de la persona* y pasa a guardar *a qué
horario tipo está asignada, desde cuándo*. La lógica de vigencia que ya funciona no cambia.

**La pantalla "Horario fijo" actual desaparece** y la reemplaza el catálogo de horarios tipo.

## Las tareas

| # | Tarea | Riesgo |
|---|---|---|
| T1 | Modelo: `horariotipo` + `horariotipodia` (N turnos por día) | bajo |
| T2 | Catálogo con la grilla de 7 días: clic **agrega** un turno, clic sobre uno puesto lo **saca** | bajo |
| T3 | `horariocontrato` pasa a ser la asignación; el resolutor devuelve **varias jornadas por día** | **medio** |
| T4 | Asignación masiva desde el horario tipo, igual que los integrantes de un grupo | bajo |
| T5 | Un horario ya usado en planilla oficial **no se edita: se clona** | bajo |
| T6 | La verificación muestra **una línea por jornada** (mañana y tarde por separado) | bajo |

**T3 es el corazón**: es donde el resolutor pasa de "una jornada" a "las jornadas de ese día", y
toca la firma que ya usa la pantalla de verificación y va a usar la planilla.

## Decisiones tomadas

### El turno partido son dos jornadas, no una con descanso

El usuario confirmó que **el intermedio no se paga**: dos sesiones de 4 h son 8 h. Modelarlo como
"una jornada de 08:00 a 18:00 con un descanso" implicaría que el día es una jornada continua, y
no lo es.

Como además **marcan las cuatro veces** —entrada, salida, entrada, salida—, el motor de
asistencia **no necesita ningún cambio**: `SessionScheduleMatcher` ya recibe una lista de
jornadas y asocia cada sesión a la que más se solapa.

> Se evaluó una tarea extra para el caso de quien no marca al mediodía —una sesión de
> `08:00–18:00` se asignaría solo a la mañana y la tarde quedaría como falta—. **Se descartó**
> porque marcan las cuatro veces. Si algún día dejan de hacerlo, la regla sería: asignar la
> sesión a la jornada de mayor solapamiento **y además a toda jornada que contenga por
> completo**. La regla más obvia —"toda jornada que se solape más de la mitad"— es incorrecta:
> una sesión larga de noche podría pisar dos jornadas de 12 h y contarse dos veces.

### Un solo mecanismo para los horarios particulares

Hay personas con horario propio "por la naturaleza del negocio o acuerdos de la empresa".

Se evaluaron dos caminos:

- **(A)** Quien tiene horario propio recibe **su propio horario tipo**, marcado como *personal*
  para que no ensucie la lista general. Es lo que hace Odoo.
- **(B)** Horario tipo para los generales, más un horario individual que lo pise.

**Se eligió (A).** (B) suena más flexible pero agrega una quinta capa al resolutor, y con ella la
pregunta "¿cuál gana?" en cada consulta, en cada pantalla y en cada explicación a un empleado que
reclama. Con (A) hay una sola respuesta posible: *la persona está asignada a este horario*.

El costo es que la lista crece con los casos particulares. Se resuelve con el flag *personal* y
un filtro: es un problema de pantalla, no de modelo.

### Editar un horario ya usado

Decisión del usuario: **si ya se usó, no se edita**. Se clona. Se reutiliza
`ScheduleLockService`, que ya hace exactamente esto con los turnos.

## Lo que este plan NO hace

- **El cronograma de grupos sigue con un turno por día.** Producción trabaja 12 h corridas.
- **Sin rotación de varias semanas.** SAP y UKG la tienen, pero acá ya está resuelto mejor: el
  cronograma por grupo planifica **por fecha**, que es más flexible que un patrón cíclico y es lo
  que la gente realmente hace. Si algún día hiciera falta, se agrega un número de semana a la
  línea y el modelo lo absorbe.
- **No calcula el pago de feriados ni de horas extra** (RF‑05).

## Migración

Hay **22 filas de 3 personas** cargadas. No vale la pena un script: se borran y se vuelven a
cargar creando los horarios tipo. Son cinco clics por persona.

## Volumen esperado

Entre **10 y 15 horarios tipo**. Con ese número la pantalla es una lista simple; si creciera a
40, habría que agregarle búsqueda.

## Estado

| Etapa SDD | Estado |
|---|---|
| **PLAN** | Este documento — aprobado |
| IMPLEMENT | No iniciado |
