# Generación de planillas

El proceso mensual completo, de la creación del ciclo hasta el asiento contable.

## Vista de pájaro

```
1. Ciclo de generación de planillas (mes)      PayrollGenerationCycle
      ↓ congela tasas AFP/CNS/IVA/SMN, UFV, tipo de cambio
2. Insumos del mes (dentro del ciclo)
      · Horas extra          ExtraHoursWorked   por contratopuesto
      · Bonos otorgados      GrantedBonus       por contratopuesto
      · Formularios 110      InvoicesForm       por contratopuesto
      · Movimientos sueldo   SalaryMovement     por empleado + gestión planilla
      ↓
3. Gestión planilla por categoría de puesto    GestionPayroll
      ↓ "Generar planilla"  (N veces, tantas pruebas como haga falta)
4. Planilla generada TEST                      GeneratedPayroll
      · ManagersPayroll             (planilla de sueldos)
      · CategoryTributaryPayroll    (tributaria por categoría)
      · CategoryFiscalPayroll       (fiscal por categoría)
      · ControlReport               (bitácora de asistencia)
      · RotatoryFundCollection      (cuotas descontadas)
      ↓ cambiar tipo a OFICIAL
5. Merge → TributaryPayroll + FiscalPayroll    (consolidado del ciclo)
      ↓
6. Registro contable                           AccountingRecord → Vouchers
```

## 1. Ciclo de generación

Pantalla: **RRHH → Ciclo de generación de planillas**
([payrollGenerationCycleList.xhtml](../../view/employees/payrollGenerationCycleList.xhtml) →
[payrollGenerationCycle.xhtml](../../view/employees/payrollGenerationCycle.xhtml)) ·
Permiso `PAYROLLGENERATIONCYCLE` ·
[PayrollGenerationCycleAction](../../src/main/com/encens/khipus/action/employees/PayrollGenerationCycleAction.java)

Único por **(unidad de negocio, gestión, mes)** y por nombre; ambas cosas se validan en
`validateDefaultValues()`. Al crearse copia las tasas activas del momento.

Fechas del ciclo:

| Campo | Significado |
|---|---|
| `generationInitDate` / `generationEndDate` | Período que se liquida |
| `startDate` / `endDate` (`fechainiciofiscal`/`fechafinfiscal`) | Período fiscal declarado al Ministerio |
| `generationBeginning` / `generationDeadline` | Ventana en la que se **permite generar** |
| `officialPayrollDeadline` | Fecha tope para marcar una corrida como OFICIAL |

`getReadOnly()` bloquea la edición cuando el ciclo ya tiene planillas oficiales
(`payrollGenerationCycleService.isReadOnly()`).

## 2. Insumos del mes

Se cargan desde botones de la propia pantalla del ciclo, y sólo mientras
`not payrollGenerationCycleAction.hasAllPayrollsAsOfficial`:

| Botón | Entidad | Acción |
|---|---|---|
| Agregar gestión planilla | `GestionPayroll` | `gestionPayrollAction.newInstance` |
| Agregar formulario de facturas | `InvoicesForm` | `invoicesFormAction.newInstance` |
| Agregar horas extra | `ExtraHoursWorked` | `extraHoursWorkedAction.newInstance` |
| Agregar bono otorgado | `GrantedBonus` | `grantedBonusCreateAction.addGrantedBonus` |

Los **movimientos de sueldo** (`SalaryMovement`) se cargan aparte, desde
**RRHH → Movimientos de sueldo**, y se asocian a una `GestionPayroll` concreta.

## 3. Gestión planilla

Pantalla: **RRHH → Gestión de planillas** ·
[GestionPayrollAction](../../src/main/com/encens/khipus/action/employees/GestionPayrollAction.java) ·
permiso `GESTIONPAYROLL`.

Una `GestionPayroll` = (gestión, mes, unidad de negocio, **categoría de puesto**, tipo).
El nombre (`gestionName`) se arma automáticamente concatenando organización, año, mes,
categoría y el rango de fechas (`updateName()`).

`putStartAndEndDates()` fija `initDate` = primer día del mes y `endDate` = último día real
del mes. **Ojo:** para el prorrateo la generación usa siempre un mes de **30 días**
(ver [03_calculos_planilla.md](03_calculos_planilla.md)).

Validación de unicidad (`validate()`):
- tipo `CHRISTMAS_BONUS` → único por (gestión, unidad de negocio, categoría, tipo)
- tipo `SALARY` con `GENERATION_BY_SALARY` → único por (unidad de negocio, gestión, mes, categoría, tipo)

## 4. Generar la planilla

`GestionPayrollAction.generatePayroll(instance)` abre la conversación y prepara un
`GeneratedPayroll` en blanco: nombre correlativo
(`gestionPayrollService.getNextGeneratedPayrollName`), tipo de cambio copiado de la gestión
planilla y `payrollGenerationCycle` del ciclo en curso.

`GestionPayrollAction.generatePayroll()` (sin argumentos) es el que ejecuta. Valida, en orden:

1. `hasValidGenerationDateRange` — hoy dentro de `[generationBeginning, generationDeadline]`
2. nombre no repetido
3. no existe ya una corrida **OFICIAL** para esa gestión planilla
4. si es aguinaldo: deben existir planillas oficiales de **septiembre, octubre y noviembre**

y luego llama según el tipo:

- `gestionPayroll.isSalaryType()` → `generatedPayrollService.fillPayroll(generatedPayroll)`
- si no → `generatedPayrollService.fillChristmasPayroll(generatedPayroll)`

La corrida siempre nace como `TEST`.

### `fillPayroll` — el motor

[GeneratedPayrollServiceBean.fillPayroll()](../../src/main/com/encens/khipus/service/employees/GeneratedPayrollServiceBean.java)
(línea ~577). Transacción manejada a mano (`@TransactionManagement(BEAN)` + `UserTransaction`).

```
persistir GeneratedPayroll (commit propio, para tener id)
cargar fechas especiales del mes por unidad de negocio
cargar cobros de fondos rotatorios existentes de la gestión planilla
mientras queden empleados:                       ← paginado de 50 en 50
    abrir transacción (timeout 120 s × pageSize)
    employeeList = empleados con contrato activo para generación en el rango
    despachar por PayrollGenerationType:
        BY_PERIODSALARY → fillFiscalProfessorPayroll(...)
        BY_SALARY       → fillManagersPayroll(...)
        BY_TIME         → fillProffesorsPayroll(...)
    commit
comparar cobros nuevos vs existentes:
    si difieren  → borrar los viejos, marcar OUTDATED las demás corridas, crear los nuevos
si el resultado != SUCCESS → borrar la GeneratedPayroll recién creada
```

**Recreación de cobros y OUTDATED.** Si el conjunto de cuotas descontadas cambió respecto
de la corrida anterior (`recreateIsNeed`), `setTestToOutdatedGeneratedPayrollButCurrentByGestionPayroll`
marca **todas las demás corridas de esa gestión planilla como `OUTDATED`**. Es la
protección contra tener dos pruebas con cobros incoherentes.

### Qué empleados entran

`Employee.findEmployeesForPayrollGenerationByLastDayOfMonth`:

```sql
select distinct jobContract.contract.employee
from JobContract jobContract
where jobContract.contract.activeForPayrollGeneration = true
  and ( (contract.initDate <= :endDate and contract.endDate is null)
     or (contract.initDate <= :initDate and contract.endDate  >= :endDate)
     or (contract.initDate >= :initDate and contract.initDate <= :endDate)
     or (contract.initDate <= :lastDayOfMonth and contract.initDate >= :endDate)
     or (contract.endDate  >= :initDate and contract.endDate  <= :endDate) )
  and jobContract.job.organizationalUnit.businessUnit = :businessUnit
  and jobContract.job.jobCategory = :jobCategory
```

`:lastDayOfMonth` se fuerza al **día 30** en
[EmployeeServiceBean:200](../../src/main/com/encens/khipus/service/employees/EmployeeServiceBean.java).

### Resultados posibles

[PayrollGenerationResult](../../src/main/com/encens/khipus/util/employees/PayrollGenerationResult.java):

| Resultado | Mensaje al usuario | Causa |
|---|---|---|
| `SUCCESS` | generación exitosa | — |
| `WITHOUT_CONTRACTS` | `GeneratedPayroll.error.withoutContracts` | el empleado entró por la consulta pero no tiene contrato válido en el rango |
| `WITHOUT_BANDS` | `GeneratedPayroll.error.withoutBands` | el empleado no tiene **ninguna banda horaria** vigente → aborta toda la corrida |
| `FAIL` | `GeneratedPayroll.error.generationAborted` | excepción |

> Cualquiera distinto de `SUCCESS` **borra la corrida entera**. Un solo empleado sin banda
> horaria tumba la planilla de toda la categoría — es la causa más frecuente de "no me
> genera la planilla".

## 5. Pasar a OFICIAL y fusionar

Pantalla: lista de planillas generadas
([generatedPayrollListBody.xhtml](../../view/employees/generatedPayrollListBody.xhtml)) →
*Editar* → cambiar **Tipo** a Oficial ·
[GeneratedPayrollAction.update()](../../src/main/com/encens/khipus/action/employees/GeneratedPayrollAction.java)
→ [GeneratedPayrollServiceBean.update()](../../src/main/com/encens/khipus/service/employees/GeneratedPayrollServiceBean.java) (línea 125).

Transiciones permitidas:

```
TEST ──────► OFFICIAL          sí (con validaciones)
TEST ──────► OUTDATED          NO  (CannotChangeToOutdatedGeneratedPayrollTypeException)
OFFICIAL ──► TEST              NO  (CannotChangeFromOfficialToTestGeneratedPayrollTypeException)
OUTDATED ──► cualquiera        NO  (CannotChangeFromOutdatedGeneratedPayrollTypeException)
```

El estado `OUTDATED` **sólo lo pone el sistema** al recrear cobros de fondos rotatorios.

Validaciones de TEST → OFICIAL, en orden:

| # | Comprobación | Excepción / mensaje |
|---|---|---|
| 1 | ningún líquido negativo | `GeneratedPayrollHasNegativeAmountException` → `GeneratedPayroll.error.negativeAmount` |
| 2 | hoy ≤ `officialPayrollDeadline` | `GestionPayrollOfficialPayrollDeadlineException` |
| 3 | todos los empleados con cuenta bancaria | `EmployeeMissingBankAccountException` |
| 4 | no existe otra OFICIAL para los mismos parámetros | `AlreadyExistsAnOfficialGeneratedPayrollException` |
| 5 | las cuotas de fondos rotatorios siguen vigentes | `QuotaInfoOutdatedException` |

Superadas, ejecuta **el merge**:

```java
GeneratedPayroll generatedPayroll4Operations = load(generatedPayroll);
payrollGenerationCycleMergeService.merge(generatedPayroll4Operations);
```

### El merge

[PayrollGenerationCycleMergeServiceBean.merge()](../../src/main/com/encens/khipus/service/employees/PayrollGenerationCycleMergeServiceBean.java)

Consolida las planillas **por categoría** en las planillas **del ciclo**, acumulando por
empleado. Un empleado con dos categorías en el mismo mes suma sus dos aportes en una sola
fila de `TributaryPayroll` / `FiscalPayroll`:

```
por cada CategoryTributaryPayroll de la corrida:
    tributaryPayroll = mergedMap.get(empleado)          ← null la primera vez
    tributaryPayroll = TributaryPayrollMergeProcessor(ciclo, tributaryPayroll, categoría).merge()
    persist o merge; si es nuevo asigna número correlativo
    fiscalPayroll    = FiscalPayrollMergeProcessor(ciclo, tributaryPayroll.fiscalPayroll,
                                                   categoría.categoryFiscalPayroll).merge()
    persist o merge, colgándolo del tributaryPayroll
```

El *cómo* se acumula cada columna lo declara cada `MergeProcessor` con
`BasicMergeCalculator.sum / avg / max / min` — reflexión sobre el nombre de la propiedad:

- `sum` para todos los importes (básico, bonos, AFP, CNS, impuestos, saldos, total ganado…)
- `max` para `seniorityYears`
- `avg` para `workedDays`, `paidDays`, `hourDayPayment` (sólo en el fiscal)

> `salaryMovementService.matchGeneratedSalaryMovement(...)` está **comentado** desde el
> 23/08/2018 "para obviar error". Los movimientos de sueldo no se marcan como consumidos
> al oficializar.

## 6. Registro contable

Pantalla: **RRHH → Generación de planillas → Registro contable**
([accountingRecord.xhtml](../../view/employees/accountingRecord.xhtml)) ·
permiso `ACCOUNTINGRECORD`. Ver [05_contabilizacion.md](05_contabilizacion.md).

## Aguinaldo (`fillChristmasPayroll`)

Gestión planilla de tipo `CHRISTMAS_BONUS`. No recorre asistencia: se alimenta de las
planillas **oficiales de septiembre, octubre y noviembre** de la misma gestión, unidad de
negocio y categoría.

```
empleados = intersección de los que aparecen en las 3 planillas oficiales
por cada empleado:
    initContractDate = la MENOR fecha de inicio de contrato entre los 3 meses
    salary           = el sueldo del ÚLTIMO mes iterado (noviembre)
    workedDays       = días entre initContractDate y el 31/12 de la gestión, tope 365
    totalIncome(mes) = si companyConfiguration.basicBasedChristmasPayroll
                          → salario + otros ingresos + ingreso fuera de IVA
                       si no
                          → total ganado + ingreso fuera de IVA
    promedio         = (sep + oct + nov) / 3
    ganancia base    = workedDays >= 365 → promedio
                       workedDays >  90  → promedio × workedDays / 365
                       si no             → 0        ← menos de 90 días: sin aguinaldo
```

`basicBasedChristmasPayroll` sale de la configuración de compañía y decide si el promedio
se calcula sobre el **básico** o sobre el **total ganado**.

Si falta la planilla oficial de alguno de los tres meses, la generación se rechaza con
`GeneratedPayroll.error.missingOfficialPayroll`.

## Widget de tablero

[OfficialPayrollGenerationWidgetAction](../../src/main/com/encens/khipus/action/employees/OfficialPayrollGenerationWidgetAction.java)
(widget XML id `9`) muestra un medidor *generadas / total* de planillas oficiales del mes
en curso, vía `generatedPayrollService.calculateGeneratedPayrolls(year, month, executorUnitId)`.
