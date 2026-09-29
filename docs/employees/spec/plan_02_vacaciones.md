# PLAN 02 — Reactivación del submódulo de vacaciones

> Cubre [RF‑07](req_01_asistencia_produccion.md) del SPEC 01.
> Aprobado. **V1 y V2 implementados y desplegados.**

## Qué encontré

El submódulo **está completo y bien diseñado**. No hay que construirlo: hay que encenderlo.

```
VacationRule        catálogo por tramos de antigüedad: años inicio–fin → días
      ↓
VacationPlanning    por contrato de puesto: antigüedad, días, libres, usados, fecha de inicio
      ↓
VacationGestion     el detalle por año (gestión)
      ↓
Vacation            la vacación concreta: fechas, total de días, estado
                    PENDIENTE / APROBADA / ANULADA
```

Son 4 entidades, 4 servicios, 4 actions y 5 vistas, con sus validaciones propias —solapamiento
de tramos, tramo de antigüedad no definido— y una sincronización que recalcula la antigüedad y
los días acumulados contra las reglas.

**Y ya está conectado con la planilla.** `VacationServiceBean.createSpecialDate()` crea una
`SpecialDate` cuando la vacación se marca *para generación de planilla*: día completo, con goce
de haber, destino empleado, apuntando de vuelta a la vacación. Si se anula, la borra. La
planilla lee `SpecialDate` por empleado, así que **la vacación llega a la planilla por ese
camino, sin tocar el motor de cálculo.**

### Por qué está apagado

No es un defecto de diseño ni código incompleto. Es que **tres permisos nunca se registraron
en `funcionalidad`**:

| Permiso | Registrado |
|---|---|
| `VACATIONRULE` | Sí |
| `VACATIONPLANNING` | **No** |
| `VACATIONAPPROVE` | **No** |
| `VACATIONANNUL` | **No** |

`s:hasPermission` devuelve falso en silencio cuando la funcionalidad no existe, así que la
opción de menú nunca se dibujó y nadie pudo entrar. Es la misma trampa del módulo de permisos
que ya nos mordió antes.

Las cuatro tablas existen y están **vacías**, en las dos bases. Nunca se usó.

## ¿Sobra "Vacación" en fechas especiales?

**No sobra, pero no debería poder elegirse a mano.**

La `SpecialDate` que genera el módulo **es** la vacación vista desde la planilla, y necesita el
motivo `VACACIÓN` para dos cosas: reportar por causa, y que el cierre mensual (E5.3) sepa
distinguir los días a cuenta de vacación del resto de los permisos pagados.

El problema es el otro camino. Si RRHH crea a mano una fecha especial con motivo *Vacación*,
esa vacación **no descuenta del saldo**: no pasa por `VacationGestion`, así que `diasusados`
no se mueve. Queda un día pagado sin respaldo en el kardex de vacaciones, y el saldo miente sin
que nadie se entere. Es exactamente el tipo de descuadre silencioso que el motivo venía a
evitar.

**Propuesta:** `VACACIÓN` deja de ofrecerse en el combo de fechas especiales y lo escribe sólo
el módulo al generar la fecha. Sigue existiendo como valor, se sigue viendo en la lista y se
sigue pudiendo filtrar por él.

*Maternidad no entra en esta regla*: el SPEC pide explícitamente que se registre como una
justificación con goce identificada, o sea a mano, y no consume vacaciones.

## Las etapas

### V1 · Encender lo que ya existe

| # | Tarea | Riesgo |
|---|---|---|
| V1.1 | Registrar `VACATIONPLANNING`, `VACATIONAPPROVE` y `VACATIONANNUL` en `funcionalidad` | bajo |
| V1.2 | Cargar los tramos de `VacationRule`: 15 / 20 / 30 días según antigüedad | bajo |
| V1.3 | Que la `SpecialDate` generada lleve `motivo = VACACIÓN` | bajo |
| V1.4 | Quitar `VACACIÓN` del combo de alta manual de fechas especiales | bajo |
| V1.5 | Revisar el submódulo pantalla por pantalla con datos reales y anotar lo que no funcione | medio |

**Verificación:** dar de alta un plan de vacaciones para un empleado, aprobarle una vacación,
y confirmar que (a) aparece la fecha especial con motivo Vacación, (b) el saldo de días bajó,
(c) al regenerar el mes esos días **no** generan falta ni descuento, y (d) anular la vacación
borra la fecha especial y devuelve los días.

> V1.5 es una revisión, no una lista de arreglos. El módulo nunca corrió con datos: es
> probable que aparezcan defectos que no se ven leyendo el código. **Lo que salga de ahí se
> reporta y se decide, no se arregla sobre la marcha.**

### V2 · Saldo de vacaciones

> Aprobado e implementado, salvo la carga inicial.
> **Los días no caducan: se acumulan de una gestión a la siguiente.** Decidido con el usuario.

#### Lo que dice el informe real

Antes de proponer nada se leyó el `INFORME- VACACIONES DESCANSOS` vigente. Su estructura es,
por empleado: fecha de ingreso · tiempo de servicio en **año/mes/día** · días de vacación a
los que tiene derecho · **una columna por mes** con fecha de inicio, fecha de retorno y
cantidad de días usados · y al cierre **días acumulados**, **total usados** y **saldo
restante**.

Tres cosas que obligan a cambiar el modelo actual:

| Hallazgo | Consecuencia |
|---|---|
| Hay **medios días**: aparecen valores 4,5 y 0,5 en 20 celdas | `Vacation.totalDays` y los tres contadores de `VacationGestion` son `Integer`. **No sirven** |
| Una misma celda mensual lista **varias tomas** ("2/10/2025; 25/10/2025") | Un período puede tener más de un evento; el modelo ya lo permite |
| El informe **es** un libro de movimientos con acumulado y saldo | No hay que inventar el formato: ya existe y es el que RRHH sabe leer |

No hay saldos negativos en el informe, pero sí derecho otorgado antes del aniversario —alguien
con 11 meses y 21 días figura con 10 días—. Los días anticipados existen; simplemente no se
registran como saldo negativo sino adelantando el derecho a mano.

#### La propuesta: libro de movimientos, no contadores

Los sistemas de ausencias modernos —Workday, SuccessFactors, BambooHR, Personio— no guardan un
"días usados" mutable. Guardan un **libro de movimientos inmutable** y **derivan** el saldo:

| Tipo | Fecha | Días | Origen |
|---|---|---|---|
| `DEVENGO` | aniversario | +15,0 | regla de antigüedad |
| `TOMA` | día de la vacación | −4,5 | vacación #123 |
| `AJUSTE` | cuando haga falta | ±n | corrección, con motivo y usuario |

**Saldo a cualquier fecha = suma de los movimientos hasta esa fecha.** Es la misma forma que el
kardex de almacén que el sistema ya maneja, y la misma que pide el banco de horas de RF‑05:
**un solo mecanismo sirve para los dos**.

Esto resuelve tres cosas de una:

**V2.1 — saldo a fecha de corte, gratis.** Hoy `synchronizeVacationDays()` recalcula los
contadores "a hoy" y los **pisa**: el saldo de un mes cerrado es irrecuperable. Con movimientos
no se pisa nada, y el saldo al 31 de julio es una suma con un `where fecha <= ...`.

**Auditabilidad.** Ante un saldo de 7 días se puede responder *por qué* son 7, listando los
movimientos. Hoy sólo se puede responder "porque el último recálculo dio 7".

**V2.3 — maternidad, por construcción y no por omisión.** Sólo una `Vacation` genera un
movimiento `TOMA`. Una baja por maternidad es una `SpecialDate` con su motivo y **nunca toca el
libro**. Deja de ser algo que hay que verificar y pasa a ser algo que no puede pasar.

#### Días anticipados: saldo negativo acotado

El módulo hoy **bloquea** aprobar más días de los disponibles. La forma moderna no es agregar
un concepto de "anticipo" en paralelo —serían los mismos días contados dos veces— sino permitir
que el saldo baje de cero **hasta un límite configurable**, que es lo que Workday llama *negative
balance limit* y Personio *allowed overdraft*:

- Se configura por regla: 0 días —sin anticipos—, N días, o "hasta lo que devengue en el año".
- La aprobación sigue igual, pero avisa: *"esto deja el saldo en −3"*.
- El saldo se corrige solo cuando entra el siguiente devengo.
- La columna **días a cuenta de vacación** del reporte mensual es, sin más, la parte del saldo
  que quedó por debajo de cero.

#### El consumo deja de estar atado a una gestión

Cada `Vacation` colgaba de **una** `VacationGestion`, que era el balde del que se descontaba.
Con 15 días por año, una vacación de 18 días no entraba en ningún balde: la validación la
rechazaba comparándola contra los 15 de esa gestión, **aunque el empleado tuviera 45 disponibles**.
Y había que elegir el año a mano, decisión que no significa nada para quien la carga.

Tres cambios lo resuelven:

- **La validación mira el saldo del plan**, no los días de una gestión.
- **La gestión la asigna el sistema**: la más antigua con saldo. El campo sale del formulario y
  en su lugar se muestran los días disponibles.
- **El consumo se reparte entre gestiones, de la más antigua a la más nueva.** 18 días sobre
  gestiones de 15 dejan la primera en 15 usados y la segunda en 3 — que es como lo lleva el
  informe de RRHH. El saldo inicial se consume primero, por ser el más antiguo. Si sobra
  consumo —vacaciones anticipadas— queda en la última gestión con los días libres en negativo,
  que es justamente lo que hay que ver.

`VacationGestion` sigue existiendo como registro del devengo y como vista por año; la cuenta
real la lleva el libro.

#### Etapas

| # | Tarea | Riesgo |
|---|---|---|
| V2.1 | Libro de movimientos de saldo, en decimal, con `DEVENGO`/`TOMA`/`AJUSTE` | **Hecho** |
| V2.2 | Que aprobar y anular una vacación escriban y reviertan movimientos | **Hecho** |
| V2.3 | Saldo a fecha, y el libro visible en la pantalla del plan | **Hecho** |
| V2.4 | Límite de saldo negativo configurable, con aviso al aprobar | **Hecho** |
| V2.5 | Pasar días y contadores de `Integer` a decimal | **Hecho** |
| V2.6 | Carga inicial: la hace RRHH desde el sistema, no un script | **Hecho** |
| V2.7 | Proceso de actualización de devengos, lanzado a mano | **Hecho** |

V2.5 es alto porque toca entidades con columnas ya creadas. V2.6 es alto porque un saldo
inicial mal cargado se arrastra para siempre; se concilia contra el informe, fila por fila,
antes de dar por buena la carga.

#### Cómo quedó implementado

- **`movimientovacacion`** guarda cada movimiento con fecha, días con signo, gestión de origen,
  la vacación que lo causó y el usuario. Nada se modifica ni se borra.
- El **devengo** entra con el aniversario de ingreso de esa gestión, no el 1 de enero: los días
  se ganan al cumplir el año de servicio. Es idempotente, resincronizar no duplica días.
- **Anular** una vacación no borra la toma: escribe el movimiento contrario, y suma lo que esa
  vacación movió en lugar de asumir el total, por si se anuló y reactivó más de una vez.
- El **límite de anticipo** vive en `VacationRule`, por tramo. Quien no cumplió el primer año no
  cae en ningún tramo —y es justo el caso del anticipo—, así que ahí se usa el tramo más bajo
  del catálogo. Con el límite en cero el comportamiento es el de siempre.
- **Los días pasaron a decimal** en las tres entidades. `VacationRule.vacationDays` sigue entero:
  el derecho lo fija la norma en días enteros.
- La pantalla del plan muestra el **libro y el saldo**.

#### V2.6 — la carga inicial la hace RRHH, no un script

La idea de migrar el informe con un script se descartó: los datos son de **varias empresas** y
quien los tiene es cada encargado de RRHH, no el desarrollo. Así que la carga tiene que ser una
funcionalidad del sistema, no una operación de una vez.

Tres piezas, todas implementadas:

**Generación de planes en lote.** Un plan por contrato de puesto de empleado activo que no
tenga uno, con la fecha de inicio del contrato como arranque de la antigüedad —`empleado.fechaingreso`
viene vacía en la práctica—. Analizar → vista previa → confirmar, como la carga de marcaciones.
La vista previa separa los que ya tienen plan y los que quedan afuera **con el motivo**: sin
fecha de inicio, o con una fecha imposible. Ese filtro no es teórico: había un contrato con
fecha `0024-08-19`, un error de tipeo por 2024, que habría devengado dos mil gestiones.

**Saldo inicial.** Hay dos formas de dejar el saldo correcto, y se elige una por empleado:

| | |
|---|---|
| **Cargar el histórico** | Se cargan las vacaciones consumidas y el saldo sale solo, contra lo que el sistema devengó |
| **Declarar el saldo** | Se declara el saldo a una fecha y el sistema arranca de ahí |

La segunda es para las empresas que no van a cargar historia. **Declarar el saldo apaga el
devengo anterior**: se borran las gestiones y los devengos previos a la fecha, y el plan guarda
esa fecha para no volver a crearlos al sincronizar. Sin eso el número declarado se sumaría a lo
devengado y contaría dos veces el mismo derecho.

Es un tipo de movimiento propio —`OPENING`, no un ajuste genérico— para que el libro se explique
solo: se lee *"saldo inicial al 08‑08‑2026: 13,5 días"*.

**Vacaciones históricas.** Ya funcionaban; los medios días entran desde el paso a decimal. El
único orden que importa es cargar primero el saldo inicial, para que la validación de días
disponibles no las rechace.

> El componente del lote vive aparte y en scope de página: la lista de planes declara
> `end-conversation`, así que una conversación no sobrevive entre analizar y confirmar. Además
> la vista previa guarda sólo los números, no los planes: al confirmar se vuelve a analizar y
> se persiste, para no arrastrar cientos de entidades en el estado de la vista.

**El plan se crea solo al dar de alta un contrato.** Así el lote hace falta una sola vez y un
ingresante no queda sin control de vacaciones porque alguien se olvidó de generarlo. Con tres
condiciones, que son lo que lo hace seguro:

- La condición es que **no tenga plan el empleado**, no este contrato de puesto. Si fuera por
  contrato de puesto, un ascenso o un cambio de área le crearía un segundo plan con la
  antigüedad de vuelta en cero y el saldo partido en dos. A mano eso se ve; automatizado pasa
  en silencio.
- Sólo si la empresa tiene reglas de vacación cargadas. Sin catálogo el módulo no está en uso.
- **En su propia transacción y con el error atrapado.** Va al final de `JobContractAction.create()`,
  cuando el contrato ya está guardado. Sin `REQUIRES_NEW` compartiría la transacción del alta y
  un fallo la marcaría para rollback: dar de alta un contrato no puede quedar bloqueado por el
  módulo de vacaciones.

Queda de fondo que el plan cuelgue del contrato de puesto y no del empleado —observación #4 de
V1.5—. Con la regla "por empleado" el problema queda neutralizado, no resuelto: si alguna vez se
quiere un plan por puesto, hay que decidirlo **antes** de que haya saldos cargados.

#### Lo que sigue

Un reporte que reproduzca el informe —acumulados, usados y saldo por empleado a una fecha— para
que RRHH deje de mantener el Excel.

---

## V2.7 · El devengo no puede depender de que alguien abra un plan

El devengo se recalculaba sólo cuando algo tocaba el plan —crearlo, guardarlo, aprobar o anular
una vacación—. Quien cumpliera años de servicio no veía su gestión nueva hasta que alguien
abriera su plan y lo guardara. Con 238 planes eso no pasa nunca.

**No se usó el scheduler.** El Quartz que hay en el sistema quedó descartado por decisión del
usuario. En su lugar hay un **proceso que RRHH lanza a mano** desde la lista de planes.

### Cómo garantiza que no se escape nadie

El proceso **no busca "quién cumplió años"**. Eso dejaría afuera al menos tres casos reales: un
plan al que le falta una gestión porque cuando se sincronizó no existía el tramo de antigüedad,
una gestión sin su movimiento de devengo, y una antigüedad desactualizada.

En cambio busca **cualquier plan cuyo estado no coincida con lo que debería ser**. Un plan se
saltea sólo si las tres cosas cuadran: antigüedad, cantidad de gestiones y cantidad de devengos.
Ante cualquier duda el plan entra — el criterio es conservador a propósito.

Y cierra el último agujero contando los **empleados con contrato que no tienen plan**: son los
que se escaparían del proceso entero por no tener dónde devengar.

**Lo que no se puede devengar se reporta.** El proceso usa la variante que exige la regla de
antigüedad, no la que la ignora: si falta un tramo, sale listado en vez de saltearse en silencio.

### El defecto que apareció al probarlo

La primera versión **detectaba pero no reparaba**. `createMissingGestions` sólo registraba el
devengo cuando *creaba* la gestión; si la gestión ya existía sin su movimiento, pasaba de largo.
El proceso marcaba ese plan en cada corrida, informaba "1 actualizado" y no cambiaba nada: un
bucle silencioso que reportaba éxito sin trabajar.

Ahora llama a `postAccrual` también cuando la gestión ya existe. Es idempotente, así que repone
lo que falte sin duplicar. **El proceso se repara solo**, que era la intención desde el
principio.

> Lo encontró el usuario al repetir la corrida. La detección funcionaba perfecto y el síntoma
> era sólo que el número no bajaba a cero. Vale como método: **una corrida no prueba nada; la
> segunda es la que prueba.**

### Sin hilos ni tareas programadas

El avance lo maneja el propio `a4j:poll`: cada tic procesa un lote de 20 planes en su propia
transacción y devuelve el porcentaje. No hay nada corriendo por detrás que se pueda colgar ni
quedar a medias; si el usuario cierra la pantalla, lo procesado ya está guardado y lo que falta
se toma en la corrida siguiente.

La barra de progreso es propia y **sin animación**. La de RichFaces se dibuja con un rayado que
se mueve siempre, esté al 0% o al 100%, y da la sensación de que el proceso sigue corriendo
cuando ya terminó.
