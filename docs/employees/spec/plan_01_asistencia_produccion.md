# PLAN 01 — Control de asistencia y planilla de producción

> Deriva de [req_01_asistencia_produccion.md](req_01_asistencia_produccion.md).
> **Ninguna línea de código escrita a partir de este documento.** Esperando aprobación.

## Criterio que ordena todo el plan

Cada etapa termina con el sistema **funcionando y verificado**, no a medias. La verificación
es siempre la misma y es la única red que hay, porque el módulo no tiene tests: **regenerar un
mes cerrado en modo PRUEBA y comparar contra el oficial, campo por campo**. Es lo que se hizo
con el régimen SIP y detectó las tres desviaciones antes de que llegaran a producción.

De ahí sale el orden: **primero lo que no puede mover un importe, después lo que sí.**

## Las cinco etapas

```
E1  Fundaciones          no toca el cálculo    ── configuración y carga
E2  Motor de sesiones    reemplaza el núcleo   ── mismo resultado, otra mecánica
E3  Turnos y patrones    modelo de horarios    ── migración con convivencia
E4  Producción           lo nuevo de verdad    ── grupos, noche, banco de horas
E5  Cierre y reportes    lo que ahorra días    ── consolidado mensual
```

Cada una es desplegable por sí sola. Si el proyecto se detiene en E2, lo entregado ya vale.

---

## E1 · Fundaciones

**Qué entra:** RF‑10 (carga de marcaciones), RF‑13 parcial (motivo en fechas especiales),
RF‑04 (jornada semanal configurable).

**Por qué primero:** nada de esto toca el cálculo de planilla. Se puede desplegar y usar
mientras el resto se desarrolla, y la carga de marcaciones ya ahorra trabajo por sí sola.

| # | Tarea | Riesgo |
|---|---|---|
| E1.1 | Importador del export del biométrico: previsualización, deduplicación, reporte de no cruzados, reversible por lote | bajo |
| E1.2 | Mapear `Entrada`/`Salida` a `control` 1/3 al importar | bajo |
| E1.3 | Motivo explícito en `SpecialDate` (permiso, justificación, maternidad, compensatorio, descanso) | bajo |
| E1.4 | Jornada semanal configurable por género | bajo |

**Verificación:** importar julio 2026 completo; reimportar y confirmar que no cambia nada;
regenerar un mes cerrado y confirmar que **ningún importe se mueve**.

---

## E2 · Motor de asistencia basado en sesiones

**Qué entra:** RF‑12 completo, RF‑02 (cruce de medianoche), RF‑13 (reporte de control e
incidencias).

**Por qué acá:** es el corazón del cambio y de él dependen E4 y E5. Se hace **antes** de tocar
el modelo de horarios, para poder validarlo contra los horarios actuales y aislar la causa de
cualquier diferencia.

| # | Tarea | Riesgo |
|---|---|---|
| E2.1 | Construcción de sesiones: emparejar marcas, con fallback cronológico cuando el indicador no es confiable | medio |
| E2.2 | Asociación sesión↔jornada por solapamiento. **Eliminar `Limit`** | alto |
| E2.3 | Evaluación direccional: atraso, salida anticipada, tiempo adicional, falta | alto |
| E2.4 | Sesión que cruza medianoche, razonando en jornadas y no en días | alto |
| E2.5 | Entrada sin salida: cerrar en fin de jornada + incidencia | medio |
| E2.6 | Justificación acotada: perdona sólo los minutos que cubre | medio |
| E2.7 | Reporte de control: nunca dos marcas en null habiendo una; resultados separados | medio |
| E2.8 | Incidencias visibles y accionables | bajo |

**Verificación, la más exigente del plan:** regenerar **tres meses cerrados** de
administrativos y confirmar que **no se mueve un centavo**. Esta etapa cambia la mecánica sin
cambiar el resultado; toda diferencia es un defecto hasta que se demuestre lo contrario.

> Los importes en cero del reporte de control se resuelven acá: se calculan de verdad o se
> quitan. Una columna que siempre vale cero engaña.

---

## E3 · Turnos, patrones y asignación

**Qué entra:** RF‑11 (rediseño), RF‑08 (horarios fijos), RF‑03 (cambio de horario en el mes).

**Por qué después de E2:** el motor nuevo ya sabe resolver "qué jornada le tocaba ese día".
Acá cambia **de dónde sale** esa jornada, no cómo se evalúa.

| # | Tarea | Riesgo |
|---|---|---|
| E3.1 | Catálogo de **turnos**: hora inicio/fin, cruza medianoche, tolerancias | bajo |
| E3.2 | **Patrones**: ciclo de N días, cada posición un turno o descanso | medio |
| E3.3 | **Asignación** contrato↔patrón con vigencia y fecha de anclaje | medio |
| E3.4 | Resolver el patrón a la jornada del día, conviviendo con el modelo viejo | **alto** |
| E3.5 | Inmutabilidad: turno o patrón usado en planilla queda cerrado | bajo |
| E3.6 | Reasignación masiva con selección múltiple | medio |
| E3.7 | Migración de las 4.683 asignaciones actuales a patrones | **alto** |

**La convivencia es obligatoria.** No puede haber un corte: mientras haya contratos en el
modelo viejo, el motor tiene que resolver los dos. La migración se hace por empresa y por
lote, verificando cada uno.

**Verificación:** por cada lote migrado, regenerar el último mes cerrado y confirmar que da
idéntico. Un contrato migrado y uno sin migrar en la misma planilla deben convivir sin
diferencias.

---

## E4 · Producción

**Qué entra:** RF‑01 (grupos y cronograma), RF‑05 (banco de horas), RF‑06 (descuentos
configurables).

**Por qué acá:** todo esto se apoya en el patrón de E3 y en el motor de E2. Antes no se puede.

| # | Tarea | Riesgo |
|---|---|---|
| E4.1 | Grupos de trabajo | bajo |
| E4.2 | Cronograma mensual por grupo, genérico —sirve para el de mantenimiento futuro— | medio |
| E4.3 | Asignación de personal a grupo por semana | medio |
| E4.4 | Banco de horas: registro por evento con aprobación | medio |
| E4.5 | Kardex de saldo, acumulación y uso, cruzando gestiones | medio |
| E4.6 | Consumo del saldo como permiso | medio |
| E4.7 | Base de descuento configurable: SMN o total ganado | **alto** |
| E4.8 | Forma B de atrasos: por cantidad, con sus tramos | **alto** |
| E4.9 | Tolerancia configurable, común a las dos formas | medio |

**Lo de E4.7 y E4.8 cambia importes por diseño.** Por eso son opt‑in: una empresa que no
configure la forma B sigue con la A y la base actual, y **no se le mueve nada**.

**Verificación:** reproducir el cronograma de julio 2026 y que dé Grupo 1 = 156 h / 19,5 días
y Grupo 2 = 168 h / 21 días. Regenerar meses cerrados de empresas que no adopten la forma B y
confirmar cero diferencias.

---

## E5 · Cierre mensual y reportes

**Qué entra:** RF‑09.

| # | Tarea | Riesgo |
|---|---|---|
| E5.1 | Consolidado por empleado: horas y días efectivos, atrasos, faltas, permisos, saldos | medio |
| E5.2 | Base de 240 h y días a favor o en contra | **alto** |
| E5.3 | Días a cuenta de vacación (anticipadas) | medio |
| E5.4 | Pantalla de cierre con las incidencias pendientes de resolver | medio |

> **E5.2 arranca cuadrando, no programando.** El primer paso es reproducir un mes cerrado y
> conciliar fila por fila contra la planilla manual. Con la regla de las 240 h aplicada
> literalmente, los empleados por encima de 240 quedan un día por encima de lo que dice la
> planilla y los que están en 240 o por debajo coinciden. Hay un ajuste que no se deduce de
> los datos: **si no aparece al cuadrar, se pregunta con los casos en la mano antes de
> escribir la fórmula.**

**Verificación:** el reporte generado reproduce el de julio 2026, columna por columna.

---

## Riesgos del plan

| Riesgo | Mitigación |
|---|---|
| E2 cambia el núcleo del cálculo | Se valida contra tres meses cerrados; toda diferencia es defecto hasta demostrar lo contrario |
| La migración de horarios toca 4.683 filas | Convivencia obligatoria, migración por lote, verificación por lote |
| No hay tests automatizados | La regeneración comparada es el sustituto; conviene dejarla como script reutilizable desde E1 |
| `ControlReport` ya tiene 897.121 filas y se regenera completo | Medir antes de agregar columnas en E2.7; evaluar separar bitácora de evaluación |
| El alcance es grande y puede detenerse a mitad | Cada etapa es desplegable sola y deja valor |

## Lo que este plan NO hace

- No toca el sector académico, en ninguna etapa.
- No reescribe la generación de planilla: la alimenta con mejores datos.
- No digitaliza los formularios en papel.
- No implementa la ventana de ajustes del 1 al 7 (D9).

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | Completo y commiteado |
| **PLAN** | Este documento — esperando aprobación |
| TASKS | No iniciado |
| IMPLEMENT | No iniciado |
