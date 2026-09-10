# PLAN 09 — Cierre del plan de vacaciones y reingreso

> Nace de una pantalla que muestra un número imposible: PABLO GUIZADA reingresó el 01/09/2026
> con un contrato nuevo y *Planificación de vacaciones* le sigue mostrando **2 años de antigüedad
> y 30 días**, que son los del contrato que terminó el 31/12/2024.

## Lo que se ve y lo que hay detrás

| | |
|---|---|
| Plan **código 2** | atado al contrato de puesto **200**, el del contrato cerrado el 31/12/2024 |
| Su fecha de inicio | **19/07/2024** |
| Contrato **336** (01/09/2026) | **no tiene plan** |

Los 2 años y los 30 días son correctos **para el contrato que terminó**. El problema es otro: ese
plan no terminó nunca, y el contrato nuevo no arrancó el suyo.

## Los dos huecos

### 1 · Un plan de vacaciones no se puede cerrar

`planvacacion` tiene fecha de inicio y **no tiene fecha de fin**. No hay forma de decir *"este
plan corresponde a un período que terminó"*.

Y el devengo calcula la antigüedad como *"años entre la fecha de inicio del plan y **hoy**"*, sin
mirar el contrato. La consecuencia es literal: **el plan de alguien que se fue en 2024 va a seguir
sumando años en 2027, en 2030 y para siempre**. No es que muestre mal el dato del reingreso: es
que sigue devengando días para una relación laboral que no existe.

Hoy no se nota porque nadie se había ido y vuelto. En cuanto pasa, se ve.

### 2 · El reingreso no genera plan nuevo

Al dar de alta un contrato, el plan se crea solo si la persona **no tiene ninguno**. Esa regla
existe por una razón buena —que un cambio de puesto no le duplique el plan y le parta la
antigüedad— pero no distingue un cambio de puesto de un reingreso, y en el reingreso bloquea
justo el plan que corresponde.

Resultado: la persona vuelve, empieza a generar derecho a vacaciones, y el sistema le sigue
mostrando el saldo del período anterior.

## Cómo lo resuelven los sistemas profesionales

Los tres grandes hacen lo mismo, con nombres distintos:

| Sistema | Cómo lo modela |
|---|---|
| **Oracle HCM** | Los planes de ausencia se **inscriben y se dan de baja**: la baja del contrato cierra la inscripción y el devengo se detiene ahí. El reingreso crea una **inscripción nueva** |
| **Workday** | El *Time Off Plan* está atado al *employment record*. La baja lo cierra; el *rehire* abre uno nuevo. El saldo anterior no se arrastra salvo una regla explícita de servicio continuo |
| **SAP HCM** | La generación de cuotas se hace **por período de vinculación**. Terminado el período, no se generan más |

Las tres reglas que comparten:

1. **El devengo se detiene el día de la baja**, no el día que alguien se acuerda.
2. **El saldo que quedaba no desaparece.** Se liquida o se pierde, pero queda **registrado como
   un hecho**, no como una fila congelada que nadie sabe qué significa.
3. **El reingreso arranca su propio período**, desde cero, salvo que alguien reconozca servicio
   anterior de forma explícita.

## Lo que ya tenemos a favor

El [plan 02](plan_02_vacaciones.md) construyó las vacaciones como **libro mayor de movimientos**:
el saldo no se guarda, se deriva sumando `OPENING`, `ACCRUAL`, `TAKING` y `ADJUSTMENT` hasta una
fecha. Los movimientos no se editan ni se borran.

Eso es exactamente la estructura que la regla 2 necesita. **Cerrar un plan no es poner una
bandera: es escribir el movimiento que lo cierra.** No hay que inventar un modelo nuevo.

## La propuesta

### V1 · El plan tiene fecha de cierre

Una columna en `planvacacion`. Vacía mientras el período está abierto.

El devengo pasa de contar hasta **hoy** a contar hasta **la menor entre hoy y la fecha de
cierre**. Con eso el plan de un período terminado deja de crecer, y lo hace **sin ningún proceso
que haya que correr**: el mismo cálculo da el resultado correcto para siempre.

### V2 · La baja del contrato cierra su plan, y lo deja al día

Cuando *Dar de baja* cierra un contrato, el plan de ese contrato de puesto recibe como fecha de
cierre **el último día de trabajo**. Es el mismo acto, no un paso aparte que haya que recordar.

**El orden importa**: primero se escribe la fecha de cierre y **recién después** se sincroniza.
Como el devengo ya cuenta contra esa fecha, el plan queda cerrado **y al día a ese día** en una
sola operación. Al revés quedaría cerrado con el saldo de otro momento, que es peor que no
cerrarlo: un número que nadie puede explicar.

Va aislado del resto de la baja: si el módulo de vacaciones falla, la baja del contrato se
registra igual. Cerrar el plan no puede bloquear que alguien se vaya.

### V3 · El reingreso crea su propio plan

La condición cambia de *"si la persona no tiene ningún plan"* a **"si no tiene ningún plan
abierto"**.

Con eso las dos situaciones quedan bien y siguen siendo distinguibles sin adivinar:

| Situación | Qué pasa |
|---|---|
| Cambio de puesto, mismo contrato | tiene plan abierto → **no** se crea otro. Igual que hoy |
| Reingreso con contrato nuevo | no tiene plan abierto → **se crea**, desde su fecha de inicio, en 0 años y 0 días |

PABLO quedaría con **dos planes**: el de 2024 cerrado en 2 años, y el nuevo en cero. Que es lo
que pasó de verdad — trabajó dos períodos distintos.

### V4 · El saldo que quedó abierto, a la vista

Al cerrar el plan quedan 30 días que la persona no usó. **No se pagan ni se borran acá**: el
libro los conserva y el plan cerrado los muestra como **saldo pendiente de liquidación**.

Es deliberado. Pagarlos es finiquito, y el finiquito es otro tema —está anotado como la nota 5
del script de datos y tiene su propio módulo sin usar—. Lo que este plan garantiza es que ese
saldo **no se pierda de vista**: hoy queda en una fila que sigue creciendo y que nadie sabe leer.

### V5 · La pantalla distingue lo abierto de lo cerrado

*Planificación de vacaciones* pasa a mostrar por defecto los planes **abiertos**, con un filtro
para ver los cerrados. Sin eso, una persona con dos períodos aparece dos veces y no se sabe cuál
es el vigente.

En el plan cerrado se muestra su rango —*"19/07/2024 al 31/12/2024"*— para que se lea como lo que
es: un período terminado.

## Lo que no se hace

**No se paga ni se descuenta nada.** Ningún cálculo de dinero. El finiquito queda afuera.

**No se toca la antigüedad del bono.** La antigüedad que paga la planilla se calcula desde
`contrato.fechainicio` por su propio camino, y este plan no la roza. Son dos cosas con el mismo
nombre y distinto dueño, y ya casi las mezclamos una vez.

**No se decide si el reingreso conserva antigüedad.** Acá el criterio es: **contrato nuevo, plan
nuevo, cero**. Si alguna vez hace falta reconocer servicio anterior, es una decisión aparte y con
la planilla en la mesa desde el principio.

**No se cierran los planes viejos a mano.** Los 167 contratos ya inactivos tienen planes que
habría que cerrar con la fecha de fin de su contrato. Va en el script, en una sola pasada.

## Alcance

| Archivo | Qué recibe |
|---|---|
| `VacationPlanning.java` + SQL | la fecha de cierre |
| `VacationAccrualServiceBean.java` | el devengo se detiene en la fecha de cierre |
| `VacationPlanningServiceBean.java` | V3: la condición pasa a *"sin plan abierto"* |
| `ContractConditionServiceBean.java` | V2: la baja cierra el plan |
| `vacationPlanningList.xhtml` + `VacationPlanningDataModel.java` | V5: el filtro y el rango |
| `query_v6.1.0_terdemol_updates.sql` | cerrar los planes de los contratos ya inactivos |

## Riesgos

**Toca el devengo, que decide días de vacaciones.** No es plata directa, pero es un derecho. La
verificación tiene que incluir que a **nadie con contrato vigente** le cambie el saldo: si las
dos fechas no aplican, el cálculo tiene que dar exactamente lo de hoy.

**Los planes de contratos ya cerrados vienen inflados.** Llevan devengando desde que su contrato
terminó. Al ponerles fecha de cierre, su antigüedad **baja**. Eso es la corrección, no una
pérdida —esos días nunca se generaron— pero hay que decirlo antes de correr el script, no
después.

**El saldo pendiente no tiene dueño.** V4 lo deja visible y sin liquidar. Es honesto, pero es una
deuda que alguien va a tener que cerrar cuando se retome el finiquito.

## Cómo se verifica

1. Una persona con contrato vigente: su plan **no cambia en nada**. Mismos años, mismos días.
2. PABLO: el plan de 2024 queda cerrado al **31/12/2024**, con **2 años** — no 3 el año que
   viene.
3. PABLO tiene un **segundo plan** desde 01/09/2026, **0 años, 0 días**.
4. La pantalla muestra por defecto solo el segundo; con el filtro aparecen los dos.
5. Los 30 días del plan viejo **siguen estando**, marcados como pendientes de liquidación.
6. Un cambio de puesto sobre un contrato vigente **no** crea un plan nuevo. Igual que hoy.

## Estado

**V1 a V5 implementados y probados.** Compila con JDK 1.8.

Probado de punta a punta con un reingreso real: baja del contrato → el plan se cierra al
último día de trabajo y queda al día; contrato nuevo → plan nuevo en cero; contrato
secundario → no genera plan. Las seis invariantes verificadas contra los 245 empleados.

**Hay SQL que aplicar antes de desplegar** —se agrega una columna y el sistema valida el esquema
al arrancar—: sección **21** de `query_v6.1.0_terdemol.sql`, que crea `fechacierre` y cierra los
planes de los contratos ya inactivos.

Después de aplicarla hay que correr **Actualizar devengos** una vez: los planes recién cerrados
conservan la antigüedad inflada hasta que el proceso los recalcule contra su fecha de cierre.

### Detalles de la implementación

**El límite del devengo vive en la entidad**, en un solo método: *hoy si el plan sigue abierto, el
día del cierre si terminó*. Todo lo demás lo consulta de ahí, así que la pantalla y el proceso no
pueden discrepar.

**El filtro de período no compara contra nulo.** El buscador convierte cada expresión en un
parámetro, no en texto, así que no hay forma de meterle un *"is null"*. Se compara contra una
fecha tope que ningún cierre real alcanza: *"cerrado antes del año 9999"* equivale a *"tiene
fecha de cierre"*, y cada condición se apaga sola cuando su valor es nulo, que es como se ven
todos los períodos juntos.
