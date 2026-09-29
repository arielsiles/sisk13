# PLAN 04 — Condición del contrato: duración, cambios y baja

> Nace de un problema concreto: al asignar gente a un grupo de trabajo, el sistema rechazaba a
> personas que sí trabajan, porque su contrato figuraba vencido.

## El problema

`contrato.fechafin` se venía usando como **vencimiento administrativo** —"hay que renovar"— y no
como lo que debería ser: el registro de **cuándo terminó la relación laboral**.

La diferencia no es semántica. Un vencimiento administrativo hay que renovarlo a mano todos los
años, para cientos de personas, con el error que eso implica. Como nadie lo renueva, el dato
queda mintiendo, y el sistema termina teniendo dos verdades que se contradicen.

### Lo que decían los datos

De 234 contratos, **ninguno** tenía la fecha de fin vacía:

| Modalidad | Estado | Total | Ya vencidos | Vigentes |
|---|---|---|---|---|
| LABORAL | ACTIVO | 78 | **76** | 2 |
| EVENTUAL | ACTIVO | 108 | **72** | 36 |

**148 contratos decían ACTIVO y vencido al mismo tiempo.**

Y lo llamativo: el sistema **ya soportaba** contratos sin fecha de fin. La columna es nullable,
la entidad no tiene `@NotNull`, y 30 de las consultas del contrato ya contemplan
`endDate is null`. Lo único que obligaba a inventar una fecha era **una línea del formulario**:
`required="true"`.

Las fechas venían en tandas —17 el `01/09/2023`, 16 el `24/04/2024`—, o sea cargas masivas, no
decisiones caso por caso.

## Lo que ya estaba bien

**El histórico de planillas es inmutable y no hay que tocarlo.** `planillageneral` no guarda una
referencia al contrato: guarda una **copia** de `area`, `unidad`, `puesto`, `sueldo`, días
trabajados y **`modalidadcontratacion` como texto**, escrita al generar
(`GeneratedPayrollServiceBean:1746`).

Por eso, si alguien pasa de EVENTUAL a LABORAL en septiembre, la planilla de julio reimpresa
dentro de un año lo sigue mostrando como EVENTUAL. **La modalidad no necesita vigencia con
fechas**: el mes ya quedó congelado. Solo falta que el snapshot congele también la duración.

## El modelo

Modalidad y duración son **dos ejes independientes**, y los dos cambian con el tiempo:

| | Al ingresar | A los 3 meses | Más adelante |
|---|---|---|---|
| **Modalidad** | EVENTUAL | EVENTUAL | LABORAL |
| **Duración** | plazo fijo, 3 meses | indefinido | indefinido |

Por eso la duración no puede colgar de la modalidad: es un campo del contrato.

| Duración | Fecha de fin |
|---|---|
| INDEFINIDO | **vacía**, campo deshabilitado |
| PLAZO FIJO | **obligatoria** |

**El contrato es uno solo y no se corta.** Cambiar de condición no crea un contrato nuevo ni
cierra el anterior: la fecha de inicio sigue siendo la de ingreso real. Si cada cambio creara un
contrato nuevo, la gente perdería su antigüedad por un trámite administrativo, y con ella las
vacaciones, que cuelgan de ahí.

## La trampa de `activogenplan`

`contrato.activogenplan` es el campo por el que **todas** las consultas de planilla filtran. Y
**ningún punto del código lo modifica**: solo se pone desde una casilla del formulario. El módulo
de bajas existe, tiene 0 registros y no lo toca.

Si la baja apagara esa casilla el día que la persona se va, **le haría perder su última
planilla**: se va el 20 de julio, la planilla se genera en agosto, ya no aparece, y se queda sin
cobrar sus 20 días.

Por eso **la baja escribe la fecha de fin y no toca `activogenplan`**. El filtro por rango de
fechas ya la incluye en julio (`fechafin 20/07 >= 01/07`) y la excluye de agosto solo.
`activogenplan` queda para lo que de verdad es: excluir a alguien por otro motivo.

## Las tareas

| # | Tarea | Riesgo |
|---|---|---|
| C1 | **Duración** (`INDEFINIDO` / `PLAZO FIJO`) como campo del contrato | bajo |
| C2 | **Cambiar condición**: acto con fecha y motivo, sobre el mismo contrato | medio |
| C3 | **Dar de baja**: escribe fin y salida, no toca `activogenplan` | medio |
| C4 | **Aviso de vencimiento** de los plazo fijo, con salidas explícitas | bajo |
| C5 | Congelar la **duración** en el snapshot de la planilla | bajo |
| C6 | **Limpieza** de los 186 contratos activos | bajo, pero de alto impacto |

C2, C3 y C4 viven en una **pantalla nueva** —*Condición de contratos*— y no dentro del formulario
del contrato. Es a propósito: cambiar de condición o dar de baja son actos que dejan rastro, no
ediciones de campos, y así no se toca el alta de contratos que ya funciona.

## C6 — la limpieza

Regla del usuario: los de la planilla oficial de julio 2026 quedan activos; **con AFP ⇒ LABORAL**,
el resto EVENTUAL; **todos indefinidos y sin fecha de fin**; los que no están en esa planilla se
inactivan.

**Los 71 de la planilla de julio** — activos, `INDEFINIDO`, sin fecha de fin:

| | Antes | Después |
|---|---|---|
| LABORAL | 28 | **12** (los que tienen AFP) |
| EVENTUAL | 43 | **59** |

17 pasan de LABORAL a EVENTUAL y 1 de EVENTUAL a LABORAL — `SILVESTRE JORGE PABLO ANDRES`,
contrato 310, que era el único eventual con AFP.

**Los 115 activos que no están en la planilla** → INACTIVO y fuera de generación de planilla.
De esos 115, **ninguno tiene AFP**: inactivarlos no toca a ningún aportante.

A estos se les apaga `activogenplan` en lugar de escribirles una fecha de fin, y **es la única
vez que se hace así**. El motivo: no sabemos cuándo se fueron, y una fecha de salida inventada es
peor que ninguna. Quien conozca la fecha real registra la baja después, con C3.

## Pendiente a definir

**Finiquito al dar de baja.** Existe el módulo de bajas con sus reglas (`DismissalRule`), hoy sin
uso: 0 registros. C3 registra la salida pero **no calcula finiquito**. Hay que revisar ese módulo
aparte y definir si se integra, se reescribe o se descarta. Decisión del usuario (06/09/2026):
queda fuera de este plan.

## Lo que este plan NO hace

- No toca el cálculo de planillas ni ninguna fórmula.
- No le da vigencia con fechas a la modalidad: el snapshot de la planilla ya congela el mes.
- No toca las 30 consultas que ya manejan `endDate is null` — funcionan por sí solas cuando el
  dato deja de mentir.
- No calcula finiquito.

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | Nace de un bloqueo del plan 03 |
| **PLAN** | Este documento — aprobado |
| IMPLEMENT | C1 a C6 implementados. **C6 aplicado y verificado** en la base: 12 LABORAL y 59 EVENTUAL activos, todos indefinidos y sin fecha de fin |

### Dónde quedó cada cosa

| # | Archivos |
|---|---|
| C1 | `ContractDuration`, `Contract.duration`, `contractFragment.xhtml` |
| C2 y C3 | `ContractMovement`, `ContractConditionService`, `ContractConditionAction`, `contractConditionList.xhtml` y sus dos modales |
| C4 | filtro *Solo los que vencen* en `ContractConditionDataModel` |
| C5 | `GeneralPayroll.contractDuration`, `GeneratedPayrollServiceBean` |
| C6 | sección 4 de `query_v6.1.0_terdemol_updates.sql` |
| esquema | sección 17 de `query_v6.1.0_terdemol.sql` |

El SQL de C6 se verificó contra la base antes de escribirlo: los filtros devuelven
exactamente 71 / 12 / 59 / 115.
