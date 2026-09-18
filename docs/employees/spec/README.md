# SPEC y planes de RRHH

Trabajo dirigido por especificación sobre el módulo `employees`: primero el **SPEC** (qué hace
falta y por qué), después el **PLAN** (cómo, con las tareas y sus riesgos), y recién ahí la
implementación.

Cada plan se escribe **antes** de tocar código y se actualiza al terminar con lo que de verdad
quedó y por qué. La documentación de referencia del módulo —cómo funciona hoy— está un nivel más
arriba, en [../README.md](../README.md).

## Dónde estamos

| Documento | Qué cubre | Estado |
|---|---|---|
| [req_01_asistencia_produccion.md](req_01_asistencia_produccion.md) | SPEC: el requerimiento completo de asistencia y planillas para la nueva empresa | Cerrado |
| [req_02_emparejamiento_marcas.md](req_02_emparejamiento_marcas.md) | SPEC: el emparejamiento de marcas se desfasa cuando faltan marcas | **Abierto — bloquea E2.9** |
| [plan_01_asistencia_produccion.md](plan_01_asistencia_produccion.md) | Carga de marcaciones, motor de asistencia, planillas | **E1 y E2.1–E2.5 hechos** · falta **E2.9**: conectar el motor a la planilla |
| [plan_02_vacaciones.md](plan_02_vacaciones.md) | Vacaciones como libro mayor de movimientos | **Implementado y probado** |
| [plan_03_horarios.md](plan_03_horarios.md) | Turnos, grupos, cronograma y las cuatro capas de resolución | **H1–H10 implementados** · probado con julio 2026 |
| [plan_04_contrato.md](plan_04_contrato.md) | Duración del contrato, cambios de condición y baja | **C1–C6 implementados** · limpieza de datos aplicada y verificada |
| [plan_05_horarios_tipo.md](plan_05_horarios_tipo.md) | Horarios tipo asignables y turnos partidos | Aprobado — **no iniciado** |
| [plan_06_movimientos_grupo.md](plan_06_movimientos_grupo.md) | Préstamos entre grupos y reorganización | **M1–M5 implementados** · falta probar |
| [plan_07_busqueda_contratos.md](plan_07_busqueda_contratos.md) | Los filtros de Condición de contratos y la búsqueda histórica | **B1–B6 implementados** · probado |
| [plan_08_fecha_salida.md](plan_08_fecha_salida.md) | Fecha de salida derivada y contratos simultáneos con principal | **A y B implementados y probados** con reingreso, baja y contrato secundario |
| [plan_09_vacaciones_cierre_reingreso.md](plan_09_vacaciones_cierre_reingreso.md) | El plan de vacaciones no se cierra nunca y el reingreso no genera uno nuevo | **V1–V5 implementados y probados**: cierre al dar de baja, plan nuevo al reingresar |
| [plan_10_emparejamiento_marcas.md](plan_10_emparejamiento_marcas.md) | La jornada guía el emparejamiento de las marcas | **E1–E6 implementados y probados** con julio 2026 |
| [plan_11_marca_incompleta.md](plan_11_marca_incompleta.md) | La jornada con una sola punta marcada: ni falta entera ni día pagado | **F1 y F2 implementados** · F3–F5 van con E2.9 |
| [plan_12_planilla_motor.md](plan_12_planilla_motor.md) | E2.9: la planilla toma los números del motor, y las tres clases de falta | **P1–P10 implementados**, con los dos motores conviviendo · falta verificar julio 2026 |
| [plan_13_banco_horas.md](plan_13_banco_horas.md) | El banco de horas: acumular por mes y consumir como permiso | **Pendiente de aprobación** |

## El orden en que salieron

Los planes 04, 05 y 06 no estaban previstos: **salieron de probar el plan 03 con datos reales**.

- El **04** nació de un bloqueo concreto: al asignar gente a un grupo, el sistema rechazaba a
  personas que sí trabajan, porque su contrato figuraba vencido. Detrás había 148 contratos que
  decían ACTIVO y vencido al mismo tiempo.
- El **05** y el **06** nacieron de dos preguntas del usuario mientras cargaba datos: "¿esto
  contempla los turnos partidos?" y "¿contempla que muevan gente de un grupo a otro?".
- El **07** salió de leer la pantalla del 04 con los datos cargados: el filtro de vencimientos
  devolvía 49 avisos y ninguno era trabajo pendiente.
- El **08** salió de probar la baja: dar de baja y volver a contratar dejaba a la persona marcada
  como ida para siempre.
- El **09** salió de probar el 08: al reingresar, vacaciones seguía mostrando el saldo del
  período anterior, porque un plan no se podía cerrar.

Vale la pena registrarlo porque es el argumento a favor de probar con datos reales temprano:
todas se habrían descubierto igual, pero en producción.

## Una lección que costó varias vueltas

Los errores del 08 y el 09 fueron **el mismo error tres veces**: una regla puesta en un camino y
no en los otros. El plan de vacaciones se creaba con las validaciones completas desde el alta de
contrato, con la mitad desde la generación masiva y **sin ninguna** desde el botón *Nuevo*.

Se terminaron cuando la regla dejó de estar en cada camino y pasó a vivir en el único punto por
el que pasan todos. La forma de encontrarlos no fue arreglar el caso que fallaba, sino **enumerar
todos los caminos** que crean un plan, lo cierran o recalculan la fecha de salida.

Las invariantes que quedaron, y que conviene verificar contra la base después de cada cambio:

1. Una persona no puede tener **dos planes abiertos** a la vez.
2. Un contrato **secundario** no genera plan.
3. Un contrato **cerrado** no tiene plan abierto.
4. La **fecha de salida** coincide con lo que dicen los contratos.
5. La **antigüedad** de cada plan coincide con sus fechas.
6. Nadie tiene **más de un contrato principal** abierto.

## Las piezas que quedaron

Cuatro capas resuelven qué jornada le tocaba a una persona un día, en este orden:

```
1. Excepción de esa persona ese día
2. Cronograma del grupo al que pertenecía —o al que estaba prestada— esa semana
3. Horario fijo del contrato, salvo que sea feriado
4. Nada  →  no se evalúa asistencia
```

`JourneyResolverService` es el único punto que las conoce. El motor de asistencia recibe una
lista de jornadas y no se entera de dónde salieron; por eso una empresa sin producción puede usar
solo la capa 3 y el cálculo es el mismo.

## Lo que falta para cerrar el ciclo

El motor está construido y validado contra julio 2026, pero **todavía no alimenta la planilla**:
`GeneratedPayrollServiceBean` sigue calculando faltas y atrasos con el modelo viejo de bandas
horarias. Ese reemplazo es **E2.9** del [plan 01](plan_01_asistencia_produccion.md) y es el paso
que queda para que todo esto sirva de verdad.

La evaluación ya está lista para eso: `JourneyEvaluation` calcula atraso, salida anticipada,
falta y tiempo adicional, y es la **misma clase** que usa la pantalla de verificación.

**Pero antes hay que resolver [req_02](req_02_emparejamiento_marcas.md).** Probando julio con dos
personas aparecieron atrasos inventados de cientos de minutos: cuando falta una marca, el
emparejamiento cronológico se corre y contamina el resto del período. Conectar el motor a la
planilla con ese defecto sería pagar mal.
