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
dayAbsences       = jornadas: ausencia × 2 + registro + sinGoce     ← ver "Faltas: dos motores"
                    bandas:   (faltas del control) × 2 − (días sin goce de haber)
mensualTotalSalary= basicSalary / 30 × (workedDays − dayAbsences)          ← "básico ganado"

horasExtras       = `horasextra` del ciclo: lo que escribió el pago del banco de horas

otrosIngresos     = si activeForTaxPayrollGeneration → categoryTributaryPayroll.totalOtherIncomes
                    si no                            → Σ SalaryMovement de tipo OTHER_INCOME
                    en los dos casos se le RESTAN el bono de antigüedad y las horas extras,
                    que tienen columna propia (ver abajo)

totalIncome       = mensualTotalSalary + otrosIngresos                     ← "total ganado"

descuentos        = RC-IVA + AFP
                  + descuento por atrasos
                  + WIN + otros descuentos (movimientos de sueldo)
                  + cuotas de préstamos / anticipos (fondos rotatorios)

liquido           = mensualTotalSalary + otrosIngresos + ingresosFueraIVA − descuentos
```

### Descuento por atrasos — dos decisiones, las dos de la empresa

```
descuento = valor del día × días que la política descuenta
```

**Sobre qué base vale el día** lo dice *Preferencias de compañía → Recursos Humanos*:

| Preferencia | El día vale |
|---|---|
| **Total ganado** (por defecto) | `totalIncome / 30` — básico ganado + bono de antigüedad + otros ingresos |
| **Sueldo básico** | `basicSalary / 30` — el sueldo del contrato, el mes completo |

La norma boliviana habla del *haber*, que es el sueldo; varias empresas lo tienen escrito sobre el
total ganado. Por defecto queda en total ganado, que es lo que se venía haciendo.

**Con qué política se convierte el atraso en días**, en
[LatenessPolicy](../../src/main/com/encens/khipus/util/employees/payroll/LatenessPolicy.java):

*Por minutos acumulados del mes* — la de siempre, para todos salvo que se configure otra cosa:

| Minutos acumulados | Descuento |
|---|---|
| 0 – 30 | 0 |
| 31 – 60 | ½ día |
| 61 – 90 | 1 día |
| 91 – 120 | 2 días |
| ≥ 121 | 3 días |

*Por evento* — para las áreas marcadas con **Atrasos por evento** en la unidad organizacional:

| Atraso | Descuento |
|---|---|
| cada **4** atrasos de hasta 30 min | 1 día (8 son 2 días; el resto no se arrastra al mes siguiente) |
| uno de 31 a 120 min | ½ día, **por cada uno** |
| uno de 121 min o más | ½ día **+ memorándum**, por cada uno |

Las tres se suman. Ejemplo: 4 atrasos chicos + 2 de 45 min + 1 de 130 min = 1 + 1 + 0,5 = **2,5
días**, y un memorándum.

**Tres condiciones** para que corra la política por evento, y tienen que darse las tres: la empresa
la tiene encendida **desde una fecha** —regenerar un mes anterior reproduce lo que se pagó—, el
área del puesto está marcada —o alguna que la contenga—, y corre el **motor de jornadas**, porque
la política necesita el atraso día por día y el motor de bandas no lo entrega.

**El tope** son los días que se están pagando: por atrasos no se puede descontar más de lo ganado.

Los **memorándums** se cuentan y se reportan en la columna MEMORANDUMS de la planilla de sueldos
(`planillaadministrativos.memorandumsatraso`). El sistema **no emite** el documento: avisa a quién
le corresponde.

El importe se guarda en `ManagersPayroll.tardinessMinutesDiscount` (`descuentoporminutosatraso`),
**sí** entra al total de descuentos, y la planilla fiscal lo copia de ahí.

### Faltas: dos motores conviviendo

Desde la 6.1.0 hay **dos motores de asistencia** y cada planilla se calcula con uno solo.
Cuál se usa lo decide una **fecha de corte por categoría de puesto**
(`categoriapuesto.jornadasdesde`): si el período empieza antes de esa fecha, bandas; desde esa
fecha, jornadas. Si la categoría no tiene fecha, bandas.

La decisión es una fecha y no un interruptor **a propósito**: un interruptor no tiene memoria, y
regenerar enero en marzo daría números distintos a los que se pagaron. El motor que se usó queda
**sellado** en `planillagenerada.motorasistencia`, para poder leer dentro de dos años con qué se
pagó sin recalcular nada. Ver [AttendanceEngine](../../src/main/com/encens/khipus/util/employees/AttendanceEngine.java).

En terdemol la fecha es **2026-07-01**; ILVA sigue entero en bandas.

---

### Motor de jornadas — las tres clases de falta

El día perdido no siempre cuesta lo mismo, y la planilla lo necesita **separado**:

| Clase | Qué pasó | Cuesta |
|---|---|---|
| **Ausencia** | no vino: ninguna marca en la jornada | 1 día **× 2** |
| **Registro** | vino y marcó **una sola punta** | 1 día, simple |
| **Sin goce** | licencia aprobada sin goce de haber | 1 día, simple |

```
díasDescontados = ausencia × 2 + registro + sinGoce
básicoGanado    = basicSalary / 30 × (workedDays − díasDescontados)
```

El **× 2 es una sanción y es solo para quien no vino**. Quien marcó mal cometió un error de
registro, que se corrige; quien tiene licencia sin goce no cometió ninguno: simplemente ese día no
se le paga. El motor viejo cobraba las tres igual.

**Lo que no cuesta nada:**

| Situación del día | Días |
|---|---|
| Permiso de día completo, vacación o maternidad **con goce** | 0 |
| Feriado | 0 — suspende el horario fijo del contrato, **no** el cronograma del grupo |
| Día sin jornada: descanso, o sin turno asignado | no se evalúa |
| Jornada cumplida con al menos una sesión completa | 0 |

**Fracciones.** Si el día tiene dos jornadas —turno partido— y se pierde una, es **medio día**. Y
un **permiso por horas** reduce la falta en la parte que cubre: en un turno de 12 h con permiso de
07:30 a 13:30 y sin marcas, se pierde **medio día**, no uno. Si el permiso cubre la jornada entera,
no se pierde nada. Ver [DayAbsence](../../src/main/com/encens/khipus/util/employees/attendance/DayAbsence.java).

**La red de los 30 días.** Antes de pagar se verifica que los días no sean absurdos —negativos, o
más de 30—. Si lo son, esa persona **no se genera** y se reporta; un recorte silencioso dejaría el
número plausible y la causa viva. La red corre **solo** para el motor de jornadas.

### Motor de bandas — como estaba, y así sigue

**No cambió nada**, a propósito: quien lleva años con él tiene que seguir obteniendo lo mismo,
errores incluidos, hasta que decida pasarse.

El control produce `dayAbsences` en fracciones de día:

- falta una banda de varias del día → `+0,5`
- faltan todas las bandas del día → `+1` (si es una única banda, `+1` solo si dura ≥ 8 h para
  varones o ≥ 7 h para mujeres; si no, `+0,5`)

Y después:

```java
dayAbsences = dayAbsences * 2;                                  // sanción: todo al doble
List<Date> unpaid = specialDateService.getSpecialDateRangeUnpaid(employee, init, end);
dayAbsences = dayAbsences - unpaid.size();                      // licencia sin goce: sólo 1 día
mensualTotalSalary = basicSalary / 30 * (workedDays - dayAbsences);
```

La clave: `getSpecialDateRange(employee, …)` **filtra `credit = PAID`**
([SpecialDateServiceBean:59](../../src/main/com/encens/khipus/service/employees/SpecialDateServiceBean.java)).

| Situación del día | ¿Genera ausencia? | Días descontados |
|---|---|---|
| Permiso / feriado **con** goce de haber (`PAID`) | no | 0 |
| Falta injustificada, día completo | sí (`+1`) | **2** |
| Falta injustificada, una banda | sí (`+0,5`) | **1** |
| Licencia **sin** goce de haber (`UNPAID`) | sí → `+1` → ×2 = 2 | 2 − 1 = **1** |

> **Borde conocido:** la resta es de 1 por cada día `UNPAID` del rango, sin verificar que el
> control haya generado esa ausencia. Si la persona marcó normalmente en un día declarado sin goce,
> `dayAbsences` puede quedar **negativo** y pagarle **más de 30 días**. En el motor de jornadas esto
> no pasa: el día sin goce se resuelve adentro y la red de los 30 días lo frena.

### El descuento por ausencias NO se suma dos veces

```java
double absenceDiscount = dayAbsences * basicSalary / 30;
managersPayroll.setAbsenceMinutesDiscount(BigDecimalUtil.toBigDecimal(absenceDiscount));
```

`absenceDiscount` se **guarda para el reporte** (`descuentoporminutosausencia`) pero **no** se
agrega a `totalSumOfDiscounts`: la falta ya se descontó al calcular `mensualTotalSalary`. Sumarlo
sería doble castigo. Al tocar esta parte, no "corregir" ese aparente olvido. Vale para los dos
motores.

### Atrasos: cómo se cuentan los minutos

La escala de arriba se aplica a los minutos **acumulados en el mes**. Cómo se llega a ellos, en el
motor de jornadas:

Cada turno tiene **cuatro números configurados** (`turno`), y hacen cosas distintas:

| Campo | En terdemol | Para qué |
|---|---|---|
| `toleranciaentrada` | 10 min | hasta cuánto se puede llegar tarde sin que cuente |
| `toleranciasalida` | 5 min | hasta cuánto se puede salir antes sin que cuente |
| `margenantes` | 120 min | desde cuándo una marca ya es de esa jornada |
| `margendespues` | 240 min | hasta cuándo una marca sigue siendo de esa jornada |

Los **márgenes no perdonan nada**: definen la ventana con la que la marca se asocia a su jornada.
Con 120 antes y 240 después, un turno de 07:30 a 19:30 recoge marcas desde las 05:30 hasta las
23:30. Cuando dos ventanas se pisan —el turno de noche y el del día siguiente— se recortan en el
punto medio, así ninguna marca queda en dos jornadas.

- **La tolerancia es un umbral, no un descuento.** Con 10 minutos de tolerancia, llegar 8 minutos
  tarde no cuenta; llegar 11 cuenta **11**, no 1. Es el mismo criterio del motor viejo.
- La **salida anticipada** se mide igual, con su propia tolerancia de 5 minutos: salir 19:26 en un
  turno que termina 19:30 no cuenta; salir 19:23 cuenta **7** minutos.
- Un **permiso por horas** se resta **antes** de mirar la tolerancia. Al revés, media hora de
  atraso con veinte minutos de permiso seguiría pasándose de la tolerancia y se cobraría entera.
- **No acumulan atraso**: la jornada perdida —ya cuesta el día, cobrar además el atraso sería
  descontar dos veces—, el día perdonado con goce —la persona ni siquiera tenía que venir— y el
  día sin goce.
- La **salida sin marcar** no se castiga como salida anticipada: no se sabe hasta qué hora se
  quedó. El día queda como marca incompleta, que es clase *registro*.

> **La salida anticipada se calcula y se muestra, pero hoy la planilla NO la descuenta.** Al
> cálculo le llegan solo los minutos de atraso de entrada
> ([GeneratedPayrollServiceBean:2512](../../src/main/com/encens/khipus/service/employees/GeneratedPayrollServiceBean.java)).
> Si alguna vez debe descontarse, es una regla nueva y hay que pedirla.

### Horas extras pagadas

El pago de horas del **banco de horas** escribe `horasextra` del ciclo —la tabla de siempre— y la
planilla lo muestra en su propia columna, **HORAS EXTRAS**, entre el bono de antigüedad y otros
ingresos.

Quién lo cobra y por qué vía:

| | Cómo llega | Qué hace la planilla de sueldos |
|---|---|---|
| **Activo para planilla fiscal** | ya viene dentro de `totalOtherIncomes` de la cadena tributaria | lo muestra en su columna y lo **resta** de OTROS INGRESOS |
| **No activo** | no pasa por la cadena tributaria | lo **suma** a los ingresos y lo muestra en su columna |

El segundo caso era un agujero: las horas salían del banco y el importe **no aparecía en ninguna
planilla**. El primero es la trampa opuesta, pagar dos veces, y por eso la resta.

La fila cuadra en los dos casos:

```
básico ganado + bono antigüedad + horas extras + otros ingresos = total ganado
```

**AFP y RC-IVA**: quien está en la planilla fiscal los paga sobre el total ganado, que incluye las
horas extras. Quien no está no pasa por esa cadena y cobra el importe completo, igual que el resto
de sus ingresos.

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
