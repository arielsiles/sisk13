# Cálculos de la planilla

De dónde sale cada número. Todo lo de este archivo corre en
[GeneratedPayrollServiceBean.fillManagersPayroll()](../../src/main/com/encens/khipus/service/employees/GeneratedPayrollServiceBean.java)
(línea ~917) y en las calculadoras de `util/employees/payroll/`.

> **Mes = 30 días, siempre.** El divisor es la constante 30, no los días reales del mes.
> `EmployeeServiceBean` y `ContractServiceBean` incluso fuerzan `lastDayOfMonth.setDate(30)`.

## Días de contrato en el mes

`getContractDays4Month(contract, gestionPayroll)` (línea 497). Devuelve `periodDuration`,
por defecto **30**:

| Caso | Días |
|---|---|
| Alta **y** baja dentro del mes | `endDate − initDate` |
| Alta dentro del mes | `min(30, día30 − initDate)` |
| Baja dentro del mes | `min(30, endDate − primerDía)` salvo que el resultado supere 30 → 30 |
| Contrato normal (cubre todo el mes) | 30 |

Ese valor es el `workedDays` con que se prorratea el básico.

## Cadena de la planilla de sueldos (`ManagersPayroll`)

```
basicSalary       = job.salary.amount                       ← si la moneda es $US: × tipoCambio.venta
dayAbsences       = (faltas del control de asistencia) × 2 − (días sin goce de haber)
mensualTotalSalary= basicSalary / 30 × (workedDays − dayAbsences)          ← "básico ganado"

otrosIngresos     = si activeForTaxPayrollGeneration → categoryTributaryPayroll.totalOtherIncomes
                    si no                            → Σ SalaryMovement de tipo OTHER_INCOME

totalIncome       = mensualTotalSalary + otrosIngresos                     ← "total ganado"

descuentos        = RC-IVA + AFP
                  + descuento por atrasos
                  + WIN + otros descuentos (movimientos de sueldo)
                  + cuotas de préstamos / anticipos (fondos rotatorios)

liquido           = mensualTotalSalary + otrosIngresos + ingresosFueraIVA − descuentos
```

### Descuento por atrasos acumulados en el mes

Escalonado sobre el **total ganado**, no sobre el básico (se cambió; el código viejo sobre
`basicSalary` quedó comentado):

| Minutos de atraso acumulados | Descuento |
|---|---|
| 0 – 30 | 0 |
| 31 – 60 | `totalIncome / 30 / 2` (½ día) |
| 61 – 90 | `totalIncome / 30` (1 día) |
| 91 – 120 | `totalIncome / 30 × 2` (2 días) |
| ≥ 121 | `totalIncome / 30 × 3` (3 días) |

Se guarda en `ManagersPayroll.tardinessMinutesDiscount` (`descuentoporminutosatraso`) y
**sí** entra al total de descuentos.

### Descuento por faltas — la regla del ×2

Ésta es la parte menos evidente del módulo. El control de asistencia produce
`dayAbsences` en **fracciones de día**:

- falta una banda de varias del día → `+0,5`
- faltan todas las bandas del día → `+1` (si es una única banda, `+1` sólo si dura ≥8 h
  para varones o ≥7 h para mujeres; si no, `+0,5`)

Luego, en `fillManagersPayroll`:

```java
dayAbsences = dayAbsences * 2;                                  // sanción: la falta se paga doble
List<Date> unpaid = specialDateService.getSpecialDateRangeUnpaid(employee, init, end);
dayAbsences = dayAbsences - unpaid.size();                      // licencia sin goce: sólo 1 día
mensualTotalSalary = basicSalary / 30 * (workedDays - dayAbsences);
```

La clave para entenderlo: `getSpecialDateRange(employee, …)` **filtra `credit = PAID`**
([SpecialDateServiceBean:59](../../src/main/com/encens/khipus/service/employees/SpecialDateServiceBean.java)).
O sea:

| Situación del día | ¿Genera ausencia? | Días descontados |
|---|---|---|
| Permiso / feriado **con** goce de haber (`PAID`) | no | 0 |
| Falta injustificada, día completo | sí (`+1`) | **2** |
| Falta injustificada, una banda | sí (`+0,5`) | **1** |
| Licencia **sin** goce de haber (`UNPAID`) | sí (no cuenta como permiso) → `+1` → ×2 = 2 | 2 − 1 = **1** |

> **Borde conocido:** la resta es de 1 por cada día `UNPAID` en el rango, sin verificar que
> el control de asistencia haya generado efectivamente la ausencia de ese día. Si el
> empleado marcó normalmente en un día declarado sin goce de haber, `dayAbsences` puede
> quedar negativo y pagarle **más** de 30 días.

### El descuento por ausencias NO se suma dos veces

```java
double absenceDiscount = dayAbsences * basicSalary / 30;
managersPayroll.setAbsenceMinutesDiscount(BigDecimalUtil.toBigDecimal(absenceDiscount));
```

`absenceDiscount` se **guarda para el reporte** (`descuentoporminutosausencia`) pero **no**
se agrega a `totalSumOfDiscounts`: la falta ya se descontó al calcular `mensualTotalSalary`.
Sumarlo sería doble castigo. Al tocar esta parte, no "corregir" ese aparente olvido.

### Movimientos de sueldo

`SalaryMovement` filtrados por empleado + gestión planilla. Se convierten a Bs con el tipo
de cambio venta si están en $US.

| `MovementType` | Efecto | Condición |
|---|---|---|
| `OTHER_INCOME` | suma a otros ingresos | **sólo si** `activeForTaxPayrollGeneration = false` |
| `OTHER_DISCOUNT` | suma a otros descuentos | siempre |
| `WIN` | suma a descuento WIN | siempre |
| `RCIVA` | suma al RC-IVA a retener | siempre — **es la vía real de carga del RC-IVA** |

Los demás tipos del enum (`LOAN`, `ADVANCE_PAYMENT`, `AFP`, `TARDINESS_MINUTES`,
`DISCOUNT_OUT_OF_RETENTION`, `INCOME_OUT_OF_RETENTION`) **no se leen** en esta rutina.

### Préstamos y anticipos (fondos rotatorios)

Sólo si el líquido preliminar es ≥ 0. Se cobran cuotas hasta agotar el líquido disponible:

```
amountToPay = quotaService.sumResidueToCollectByPayrollEmployeeAndJobCategory(...)
maxDiscount = líquido preliminar
por cada cuota mientras maxDiscount >= 0:
    monto = residuo de la cuota          (× tipo de cambio venta si no es Bs)
    descuento = min(maxDiscount, monto)
    clasifica según RotatoryFundType:
        ADVANCE           → anticipo
        LOAN              → préstamo
        OTHER_RECEIVABLES → WIN u otros descuentos según PayrollColumnType
    crea un RotatoryFundCollection
```

## Planilla tributaria por categoría (`CategoryTributaryPayroll`)

Se genera **sólo si** `contract.activeForTaxPayrollGeneration = true`, con
[TributaryPayrollGenerator](../../src/main/com/encens/khipus/util/employees/payroll/tributary/TributaryPayrollGenerator.java).

Arquitectura: `PayrollGenerator` mantiene una `LinkedList<PayrollColumn>`; cada columna
envuelve un `Calculator<T>` que muta la instancia. **El orden importa** — cada calculadora
lee lo que dejaron las anteriores. Agregar una columna al medio cambia resultados.

### Orden de ejecución

| # | Calculadora | Produce |
|---|---|---|
| 1 | `CodeCalculator` | `code` = `empleado.idNumber` |
| 2 | `NameCalculator` | `name` = `"Paterno Materno, Nombres"` |
| 3 | `GeneralTributaryCalculator` | `basicAmount`, `entranceDate`, `jobContract`, `businessUnit`, `employee` |
| 4 | `SeniorityYearsCalculator` | `seniorityYears` |
| 5 | `SeniorityBonusCalculator` | `seniorityBonus` |
| 6 | `ExtraHourCalculator` | `extraHour`, `extraHourCost` |
| 7 | `GeneralBonusCalculator` | `sundayBonus`, `productionBonus`, `nightWorkBonus`, `transReturnBonus`, `refreshmentBonus`, `otherBonus` |
| 8 | `OtherIncomesCalculator` | `otherIncomes`, `totalOtherIncomes` |
| 9 | `TotalGrainedCalculator` | `totalGrained` |
| 10 | `RetentionAFPCalculator` | AFP laboral + solidario, `retentionAFP` |
| 11 | `PatronalAFPRetentionCalculator` | AFP patronal (riesgo profesional, pro vivienda, solidario) |
| 12 | `PatronalOtherRetentionCalculator` | `cns` |
| 13 | `NetSalaryCalculator` | `netSalary` |
| 14 | `SalaryNotTaxableTwoSMNCalculator` | `salaryNotTaxableTwoSMN` |
| 15 | `UnlikeTaxableCalculator` | `unlikeTaxable` |
| 16 | `TaxCalculator` | `tax` |
| 17 | `FiscalCreditCalculator` | `fiscalCredit` |
| 18 | `TaxForTwoSMNCalculator` | `taxForTwoSMN` |
| 19 | `PhysicalBalanceCalculator` | `physicalBalance` |
| 20 | `DependentBalanceCalculator` | `dependentBalance` |
| 21 | `LastMonthBalanceCalculator` | `lastMonthBalance` |
| 22 | `MaintenanceOfValueCalculator` | `maintenanceOfValue` |
| 23 | `LastBalanceUpdatedCalculator` | `lastBalanceUpdated` |
| 24 | `DependentTotalBalanceCalculator` | `dependentTotalBalance` |
| 25 | `UsedBalanceCalculator` | `usedBalance` |
| 26 | `RetentionClearanceCalculator` | `retentionClearance` |
| 27 | `DependentBalanceToNextMonthCalculator` | `dependentBalanceToNextMonth` |

### Haber básico

```
si jobCategory.sector.name != "ACADEMICO":
    basicAmount = job.salary.amount     ( × tipoCambio.venta si la moneda es $US, escala 6 )
si no:
    basicAmount = contract.occupationalBasicAmount   (0 si es null)
```

### Antigüedad

```java
hireDate      = contract.initDate + 1 día      // para que la antigüedad cuente el mes SIGUIENTE
seniorityDays = díasEntre(hireDate, gestionPayroll.endDate)
años = 0; while (seniorityDays > 365) { años++; seniorityDays -= 365; }
```

Con el `>` estricto (no `>=`) y el `+1 día`, un empleado con exactamente un año cumplido el
último día del mes **todavía no** cobra el tramo siguiente.

El monto sale del tramo de `SeniorityBonusDetail` que contiene esos años.

### Ingresos

```
totalOtherIncomes = otherIncomes            ← movimientos de sueldo OTHER_INCOME
                  + seniorityBonus
                  + extraHourCost
                  + productionBonus
                  + sundayBonus
                  + nightWorkBonus
                  + transReturnBonus
                  + refreshmentBonus
                  + otherBonus
                  + otherIncomes            ← ¡otra vez! ver deuda técnica

totalGrained      = basicAmount / 30 × workedDays  +  totalOtherIncomes
```

> **`otherIncomes` se suma dos veces** en
> [OtherIncomesCalculator](../../src/main/com/encens/khipus/util/employees/payroll/tributary/OtherIncomesCalculator.java)
> (el propio código lo marca con `//???`). En la práctica no se nota porque, cuando
> `activeForTaxPayrollGeneration = true`, los movimientos `OTHER_INCOME` **no se leen** y
> `otherIncomes` vale 0. Cambiar esa condición sin arreglar esto duplicaría los ingresos.

### Aportes AFP

**Laboral** (lo que se le descuenta al trabajador), todos sobre `totalGrained`:

```
laborIndividualAFP           = totalGrained × 10,00 %
laborCommonRiskAFP           = totalGrained ×  1,71 %
laborSolidaryContributionAFP = totalGrained ×  0,50 %
laborComissionAFP            = totalGrained ×  0,50 %
solidaryAFP                  = 1 % × (totalGrained − 13.000)    si totalGrained > 13.000
retentionAFP                 = suma de los cinco
```

`solidaryAFP` sale de recorrer los rangos de `nationalSolidaryAFPDiscountRule`:
si `DiscountUnitType.CURRENCY` toma el monto del rango tal cual (× tipo de cambio si es
$US); si es porcentaje aplica `(totalGrained − rangoInicial) × monto / 100`.

Si `employee.jubilateFlag = true`, `afpRate` se reemplaza por 1 % (edad de jubilación
cumplida) o 2,71 % (no cumplida) — pero **ese valor ya no se usa** para `retentionAFP`
desde el refactor "revisión": el cálculo viejo quedó comentado.

**Patronal** (costo de la empresa), también sobre `totalGrained`:

```
patronalProffesionalRiskRetentionAFP = totalGrained × 1,71 %
patronalProHomeRetentionAFP          = totalGrained × 2,00 %
patronalSolidaryRetentionAFP         = totalGrained × 3,00 %
patronalRetentionAFP                 = totalGrained × (1,71 + 2,00 + 3,00) %
cns                                  = totalGrained × 10,00 %
```

### Qué componentes se cobran: el régimen de aportes

Desde la v6.0.129 los cinco conceptos de arriba, los tres patronales y el CNS se cobran o
se eximen segun el **régimen de aportes al SIP** del contrato
([SIPContributionRegime](../../src/main/com/encens/khipus/model/employees/SIPContributionRegime.java),
tabla `regimenaportesip`). `contrato.idregimenaportesip` nulo significa "el régimen marcado
por defecto"; si el catálogo está vacío se cobra todo, que es el comportamiento histórico.

| Régimen | Ind. 10% | R.Común 1,71% | Solid. 0,5% | Comis. 0,5% | Tasa efectiva |
|---|:---:|:---:|:---:|:---:|---|
| Asegurado activo *(por defecto)* | ✓ | ✓ | ✓ | ✓ | 12,71 % |
| Jubilado con edad cumplida | — | — | ✓ | ✓ | 1,00 % |
| Jubilado sin edad cumplida | — | ✓ | ✓ | ✓ | 2,71 % |
| Edad de jubilación cumplida | ✓ | — | ✓ | ✓ | 11,00 % |

El Aporte Nacional Solidario y los aportes del empleador (riesgo profesional, pro vivienda,
solidario patronal y CNS) se cobran en los cuatro regímenes: son redistributivos o cubren
riesgos que el jubilado sigue corriendo mientras trabaja. Las banderas existen igual, por si
la norma cambia.

Se administra en **RRHH → Generación de planillas → Configuración → Regímenes de aportes SIP**
(permiso `SIPCONTRIBUTIONREGIME`) y se asigna en el fieldset *Contrato* de la pantalla de
contratos puestos.

> Hasta la v6.0.128 esto eran **cuatro números de carnet escritos en el código**
> (`815059`, `2862262`, `2868139`, `921886`), agregados en el commit `b4eb44d1` para cerrar
> una planilla. Los porcentajes 1,00 % y 2,71 % ya existían como constantes en
> `RetentionAFPCalculator` desde antes, pero el refactor que separó el aporte en cuatro
> componentes dejó de usarlos y la regla del jubilado se perdió en silencio.

### RC-IVA

Cadena completa (todos los montos en Bs):

```
netSalary              = totalGrained − retentionAFP
salaryNotTaxableTwoSMN = min(netSalary, 2 × SMN)
unlikeTaxable          = netSalary − salaryNotTaxableTwoSMN
tax                    = unlikeTaxable × ivaRate                      ← 13 % normalmente
fiscalCredit           = InvoicesForm.fiscalCredit del mes            ← form. 110
taxForTwoSMN           = min(tax, salaryNotTaxableTwoSMN × ivaRate)

physicalBalance   = max(0, tax − taxForTwoSMN − fiscalCredit)         ← a favor del fisco
dependentBalance  = max(0, taxForTwoSMN + fiscalCredit − tax)         ← a favor del dependiente

lastMonthBalance     = dependentBalanceToNextMonth del mes anterior
maintenanceOfValue   = lastMonthBalance × (UFVfinal / UFVinicial) − lastMonthBalance
lastBalanceUpdated   = lastMonthBalance + maintenanceOfValue
dependentTotalBalance= dependentBalance + lastBalanceUpdated

usedBalance = dependentTotalBalance      si dependentTotalBalance ≥ 1 y physicalBalance > dependentTotalBalance
            = physicalBalance            si dependentTotalBalance ≥ physicalBalance
            = 0                          en otro caso

retentionClearance          = (physicalBalance − usedBalance) + RC-IVA de movimientos de sueldo
dependentBalanceToNextMonth = dependentBalance − usedBalance   si usedBalance < dependentTotalBalance
                            = 0                                en otro caso
```

Los umbrales usan `compareTo(BigDecimal.ONE) >= 0`, es decir **se ignoran saldos < 1 Bs**.

> **Cómo funciona esto hoy en la práctica.** Con `tasaiva = 0` y `formfactura` vacía:
> `tax = 0` → `physicalBalance = 0` → `retentionClearance = 0 + RC-IVA cargado a mano`.
> El módulo tributario está efectivamente **puenteado**: el RC-IVA que se retiene es el
> que RRHH carga como movimiento de sueldo tipo `RCIVA`. El propio código lo dice:
> `/** Revisar, nuevo provisional hasta corregir modulo Tributario RCIVA **/`.
> Antes de "arreglar" el módulo tributario hay que confirmar con el cliente si quiere
> volver al cálculo automático.

### Retención del 15,5 % (empleados fuera de planilla tributaria)

Si `activeForTaxPayrollGeneration = false` y `employee.retentionFlag = true`:

```java
BigDecimal amount = mensualTotalSalary + totalSumOfIncomesBeforeIva;
if (retentionValidatorService.applyRetention(employee, gestionPayroll, amount)) {
    totalRCIvaDiscount = (mensualTotalSalary + totalOtherIncomes) * 0.155;
}
```

`applyRetention` devuelve `false` (o sea, **no** retiene) si el empleado presentó un
`DischargeDocument` para esa gestión planilla. Como `documentodescargo` está vacía, hoy
siempre retiene.

## Planilla fiscal por categoría (`CategoryFiscalPayroll`)

Es la planilla del **Ministerio de Trabajo**. Se genera después de la tributaria, con
[FiscalPayrollGenerator](../../src/main/com/encens/khipus/util/employees/payroll/fiscal/FiscalPayrollGenerator.java),
y en su mayor parte **copia** valores ya calculados.

| # | Calculadora | Qué hace |
|---|---|---|
| 1 | `FiscalPayrollGeneralCalculator` | datos personales, cargo, ingreso, y copia bonos y descuentos desde `ManagersPayroll` + `CategoryTributaryPayroll` |
| 2 | `OccupationCalculator` | `occupation` = nombre del cargo |
| 3 | `WorkedDaysCalculator` | `workedDays` desde `ManagersPayroll` |
| 4 | `NewnessCalculator` | `newnessType` = `I` (ingreso) / `R` (retiro) |
| 5 | `PaidDaysCalculator` | `paidDays` = `workedDays` del contrato |
| 6 | `BasicAmountCalculator` | copia `basicAmount` |
| 7 | `TotalGrainedCalculator` | copia `totalGrained` |
| 8 | `RetentionAFPCalculator` | copia los 5 componentes AFP |
| 9 | `RetentionClearanceCalculator` | copia `retentionClearance` |
| 10 | `OtherDiscountsCalculator` | `otherDiscount` |
| 11 | `TotalDiscountCalculator` | `totalDiscount` |
| 12 | `LiquidPaymentCalculator` | `liquidPayment = totalGrained − totalDiscount` |

`NewnessCalculator`:

```
si entranceDate > ciclo.startDate           → I   (ingreso en el período)
si contract.endDate < ciclo.endDate         → R   (retiro en el período)
```

`OtherDiscountsCalculator` está **recortado**: hoy `otherDiscount` es sólo
`otherSalaryMovementDiscount`. La versión original (comentada) sumaba también ausencias,
atrasos, préstamos, anticipos y WIN; esos conceptos ahora se suman directamente en
`TotalDiscountCalculator`:

```
totalDiscount = absenceMinutesDiscount + tardinessMinutesDiscount
              + loanDiscount + advanceDiscount + winDiscount
              + laborIndividualAFP + laborCommonRiskAFP
              + laborSolidaryContributionAFP + laborComissionAFP + solidaryAFP
              + retentionClearance
              + otherDiscount
```

> Nótese que aquí **sí** entra `absenceMinutesDiscount`, mientras que en `ManagersPayroll`
> no. Los dos documentos usan criterios distintos: la planilla de sueldos descuenta la
> falta dentro del básico ganado; la fiscal la muestra como descuento explícito.
> Por eso `fillManagersPayroll` reconcilia al final:
> ```java
> if (Math.abs(managersPayroll.getLiquid() - categoryFiscalPayroll.getLiquidPayment()) <= 0.05)
>     managersPayroll.setLiquid(categoryFiscalPayroll.getLiquidPayment());
> ```
> Una diferencia mayor a 5 centavos **no se corrige y no se avisa**: las dos planillas
> quedan descuadradas en silencio.

`hourDayPayment` viene de `companyConfiguration.getHrsWorkingDay()`.

## Consolidación por ciclo

Al oficializar, los `Category*Payroll` se acumulan en `TributaryPayroll` / `FiscalPayroll`
(ver [02_generacion_de_planillas.md](02_generacion_de_planillas.md)):
todo se **suma**, salvo `seniorityYears` que se toma el **máximo** y
`workedDays` / `paidDays` / `hourDayPayment` que se **promedian**.
