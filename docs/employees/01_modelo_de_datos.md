# Modelo de datos

Todas las entidades viven en `Constants.KHIPUS_SCHEMA` y usan `@TableGenerator` sobre la
tabla `secuencia` (ver memoria *Secuencias: JPA vs funciones almacenadas*).

## Núcleo: persona → contrato → puesto

```
Employee (empleado)                 ← extiende Person (persona)
   └─1:N─ Contract (contrato)       ← model/finances
             └─1:N─ JobContract (contratopuesto)
                       ├── Job (puesto) ──┬── Charge (cargo)
                       │                  ├── JobCategory (categoriapuesto) ── Sector (sector)
                       │                  ├── Salary (sueldo) ── Currency / KindOfSalary
                       │                  └── OrganizationalUnit (unidadorganizacional)
                       │                          ├── OrganizationalLevel (jerarquía: AREA, ...)
                       │                          ├── BusinessUnit
                       │                          └── CostCenter
                       └─1:N─ HoraryBandContract (bandahorariacontrato)
                                   ├── HoraryBand (bandahoraria)
                                   ├── Tolerance (tolerancia)
                                   └── Limit (limite)
```

### `Employee` → `empleado` — banderas que deciden el cálculo

| Campo Java | Columna | Efecto |
|---|---|---|
| `controlFlag` | `flagcontrol` | Si es `false` **no se controla asistencia**: no hay atrasos ni faltas |
| `retentionFlag` | `flagret` | Habilita la retención RC-IVA del 15,5% para empleados **no** activos en planilla tributaria |
| `jubilateFlag` | `flagjubilado` | Cambia la tasa AFP: 1% si ya cumplió edad de jubilación, 2,71% si no |
| `afpFlag` | `flagafp` | Bandera de afiliación AFP |
| `paymentType` | `tipodepago` | `PAYMENT_BANK_ACCOUNT` / cheque — decide el asiento de pago |
| `markCode` | `codigomarcacion` | Clave de cruce con el biométrico (`rh_marcado.marperid`) |

### `Contract` → `contrato` — banderas de generación

| Campo | Columna | Efecto |
|---|---|---|
| `activeForPayrollGeneration` | `activogenplan` | **Filtra qué empleados entran** a la generación |
| `activeForTaxPayrollGeneration` | `activogenplanfis` | Si `true` se genera planilla tributaria + fiscal; si `false` sólo la de sueldos con RC-IVA del 15,5% |
| `occupationalBasicAmount` | `haberbasicolaboral` | Haber básico usado **sólo** en el sector `ACADEMICO` |
| `pensionFundOrganization` | `idinstfonpension` | AFP a la que se reporta (agrupa el reporte de aportes) |
| `socialSecurityOrganization` | `idinstsegsocial` | Caja de salud |

`Contract.initDate` / `endDate` definen alta y baja; de ahí sale la proporción de días
pagados (ver [03_calculos_planilla.md](03_calculos_planilla.md)).

## Generación de planillas

```
PayrollGenerationCycle (ciclogeneracionplanilla)      ← 1 por unidad de negocio/gestión/mes
   ├── tasas congeladas del mes: AFPRate x8, CNSRate, IVARate, SMNRate,
   │   DiscountRule (AFP solidario), UFV inicial/final, ExchangeRate
   ├─1:N─ ExtraHoursWorked  (horasextra)      por JobContract
   ├─1:N─ GrantedBonus      (bonoconseguido)  por JobContract
   ├─1:N─ InvoicesForm      (formfactura)     por JobContract — form. 110 RC-IVA
   ├─1:N─ TributaryPayroll  (planillatributaria)  ← consolidado del mes por empleado
   │          └─1:1─ FiscalPayroll (planillafiscal)
   └─1:N─ GestionPayroll (gestionplanilla)    ← 1 por categoría de puesto
              └─1:N─ GeneratedPayroll (planillagenerada)   ← corridas: TEST / OFFICIAL / OUTDATED
                        ├─1:N─ ManagersPayroll            (planillaadministrativos)
                        ├─1:N─ CategoryTributaryPayroll   (planillatributariaporcategoria)
                        ├─1:N─ CategoryFiscalPayroll      (planillafiscalporcategoria)
                        ├─1:N─ ChristmasPayroll           (planillaaguinaldo)
                        ├─1:N─ ControlReport              (reportecontrol)
                        ├─1:N─ GeneralPayroll             (planillageneral)        ← muerto
                        └─1:N─ FiscalProfessorPayroll     (planilladocentelaboral) ← muerto
```

La clave del diseño: **`GestionPayroll` es por categoría de puesto**, `PayrollGenerationCycle`
es por mes completo. Las planillas `Category*` son parciales (una por categoría) y se
**fusionan** en `TributaryPayroll` / `FiscalPayroll` cuando la corrida pasa a OFICIAL.

### Enums de estado

| Enum | Valores | Columna |
|---|---|---|
| `GeneratedPayrollType` | `TEST`, `OFFICIAL`, `OUTDATED` | `planillagenerada.tipoplanillagen` |
| `GestionPayrollType` | `SALARY`, `CHRISTMAS_BONUS` | `gestionplanilla.tipo` |
| `PayrollGenerationType` | `GENERATION_BY_SALARY`, `GENERATION_BY_TIME`, `GENERATION_BY_PERIODSALARY` | `categoriapuesto.tipogeneracion` |
| `MovementType` | `LOAN`, `WIN`, `ADVANCE_PAYMENT`, `OTHER_DISCOUNT`, `AFP`, `RCIVA`, `TARDINESS_MINUTES`, `DISCOUNT_OUT_OF_RETENTION`, `OTHER_INCOME`, `INCOME_OUT_OF_RETENTION` | `tipomovsueldo.tipo` |
| `BonusType` | `SUNDAYS_BONUS`, `SENIORITY_BONUS`, `PRODUCTION_BONUS`, `NIGHTWORK_BONUS`, `TRANSRETURN_BONUS`, `REFRESHMENT_BONUS`, `REGULAR_BONUS` | `bono.tipobono` |
| `SpecialDateType` | `PAID`, `UNPAID` | `fechaespecial.gocedehaber` |
| `SpecialDateTarget` | `BUSINESSUNIT`, `ORGANIZATIONALUNIT`, `EMPLOYEE` | `fechaespecial.destino` |

`PayrollGenerationType` decide **qué método de generación corre**:

| Valor | Método en `GeneratedPayrollServiceBean` | Entidad resultante | Estado |
|---|---|---|---|
| `GENERATION_BY_SALARY` | `fillManagersPayroll()` | `ManagersPayroll` | **el único vivo** |
| `GENERATION_BY_PERIODSALARY` | `fillFiscalProfessorPayroll()` | `FiscalProfessorPayroll` | académico, muerto |
| `GENERATION_BY_TIME` | `fillProffesorsPayroll()` | `GeneralPayroll` | docentes por hora, muerto |

## Tasas y parámetros (se congelan por ciclo)

`PayrollGenerationCycle` **copia** las tasas vigentes al crearse. Cambiar la tasa después
no altera planillas ya generadas — es intencional.

| Campo del ciclo | Columna | Origen | Valor en la réplica |
|---|---|---|---|
| `afpRate` | `idtasaafp` | `tasaafp` `LABOR_CONTRIBUTION` | 12,71 % |
| `laborIndividualAfpRate` | `idtasaafplabindividual` | `LABOR_INDIVIDUAL` | 10,00 % |
| `laborCommonRiskAfpRate` | `idtasaafplabriesgocomun` | `LABOR_COMMON_RISK` | 1,71 % |
| `laborSolidaryContributionAfpRate` | `idtasaafplabsolidario` | `LABOR_SOLIDARY_CONTRIBUTION` | 0,50 % |
| `laborComissionAfpRate` | `idtasaafplabcomision` | `LABOR_LABOR_COMISSION` | 0,50 % |
| `professionalRiskAfpRate` | `idtasaafpprofrisk` | `PATRONAL_..._PROFESSIONAL_RISKS` | 1,71 % |
| `proHousingAfpRate` | `idtasaafpprohous` | `PATRONAL_..._PRO_HOUSING` | 2,00 % |
| `solidaryAfpRate` | `idtasaafpsolidario` | `PATRONAL_CONTRIBUTION_SOLIDARY` | 3,00 % |
| `cnsRate` | `idtasacns` | `tasacns` | 10,00 % |
| `ivaRate` | `idtasaiva` | `tasaiva` | **0,00 %** — ver nota |
| `smnRate` | `idtasasmn` | `tasasmn` (activa) | 3.300,00 Bs |
| `nationalSolidaryAfpDiscountRule` | `idregladescuento` | `regladescuento` tipo `SOLIDARY_AFP` | 1 % sobre el exceso de 13.000 |
| `initialUfvExchangeRate` / `finalUfvExchangeRate` | `tipocambioinicialufv` / `tipocambiofinalufv` | — | UFV para el mantenimiento de valor |

> **`tasaiva = 0`** en la réplica no es un dato corrupto: el módulo tributario de RC-IVA
> quedó a medias y el RC-IVA se carga **a mano** como movimiento de sueldo tipo `RCIVA`.
> Ver [03_calculos_planilla.md](03_calculos_planilla.md).

Regla de descuento AFP solidario (`regladescuento` + `rangoregladescuento`) tal como está
configurada: `tiporango=AMOUNT`, `tipounidaddescuento=PERCENT`, `tipointervalo=OVERLAP`,
un único rango `rangoinicial=13000`, `rangofinal=NULL`, `monto=1.00` → **1 % sobre
(total ganado − 13.000)** cuando el total ganado supera 13.000 Bs.

### Bono de antigüedad

`SeniorityBonus` (`bonoantiguedad`, con vigencia por fechas) → `SeniorityBonusDetail`
(`detallebonoantiguedad`: `anioinicio`, `aniofin`, `porcentaje`, `monto`).

El monto se precalcula al guardar en
[SeniorityBonusDetailAmountCalculator](../../src/main/com/encens/khipus/util/employees/SeniorityBonusDetailAmountCalculator.java):

```java
// el comentario del código dice "MONTO = 3 x SMN x PORCENTAJE",
// pero el código multiplica además por 6:
multiplyResult = 6 * 3 * smnRate * porcentaje
monto          = multiplyResult / 100
```

Se elige el detalle cuyo rango `[anioinicio, aniofin]` contiene los años de antigüedad; si
`aniofin` es `null` es el tramo abierto superior.

## Asistencia

| Entidad | Tabla | Nota |
|---|---|---|
| `RHMark` | **vista** `vmarcado` | Lo que lee la generación de planilla |
| `RH_Mark` | `rh_marcado` | Tabla física donde escribe el biométrico |
| `HoraryBand` | `bandahoraria` | Hora inicio/fin, día inicio/fin, duración, día por medio |
| `HoraryBandContract` | `bandahorariacontrato` | Asigna banda ↔ contratopuesto con vigencia + tolerancia + límite |
| `Tolerance` | `tolerancia` | Minutos antes/después de entrada y de salida |
| `Limit` | `limite` | Límites para el descuento de medio día |
| `SpecialDate` | `fechaespecial` | Feriados y permisos por unidad de negocio / unidad organizacional / empleado |
| `ControlReport` | `reportecontrol` | Bitácora por empleado/día/banda que produce la generación |

`vmarcado` es una vista sobre `rh_marcado`:

```sql
CREATE VIEW vmarcado AS
SELECT r.idrhmarcado, r.marfecha, r.marperid,
       CONCAT(r.marperid,'') AS marreftarjeta, r.marhora, r.control, r.marippc,
       r.descripcion, '1' AS sede,
       CONCAT(p.nombres,' ',p.apellidopaterno,' ',p.apellidomaterno) AS nombre
FROM rh_marcado r
  LEFT JOIN empleado em ON r.marperid = em.codigomarcacion
  LEFT JOIN persona  p  ON em.idempleado = p.idpersona
WHERE r.marfecha >= '2020-01-01';
```

Dos consecuencias operativas:

1. **El cruce es por `empleado.codigomarcacion`**, no por id. Si el código está mal o
   duplicado, el empleado sale sin marcas y la planilla le carga faltas.
2. **La vista corta en 2020-01-01.** Regenerar una planilla anterior a esa fecha devuelve
   cero marcas.

## Contabilización y pagos

| Entidad | Tabla | Rol |
|---|---|---|
| `AccountingRecord` | (`model/finances`) | Cabecera del registro contable de la planilla |
| `SalaryMovement` | `movimientosueldo` | Ingresos/descuentos manuales por empleado y gestión planilla |
| `SalaryMovementType` | `tipomovsueldo` | Nombre + `MovementType` + cuenta contable (`cuentactb`) |
| `RotatoryFundCollection` | (finances) | Cobro de cuotas de préstamos/anticipos vía planilla |
| `PayrollGenerationInvestmentRegistration` | `regaporgenplan` | Documento por pagar de aportes por entidad de previsión |
| `JobCategory` | `categoriapuesto` | **Lleva las 13 cuentas contables** de la planilla |

Cuentas que cuelgan de `JobCategory` (todas `NOT NULL`):

| Columna | Uso |
|---|---|
| `codctactbdebe` / `codctactbhaber` | Débito / crédito del devengado en MN |
| `codctactbdebeme` / `codctactbhaberme` | Ídem en ME |
| `codctagastoaguimn` / `codctaprovaguimn` / `codctaprovaguime` | Gasto y provisión de aguinaldo |
| `codctagastoindemmn` / `codctaprevindemmn` / `codctaprevindemme` | Gasto y previsión de indemnización |
| `fonpenctapatronal` | Aporte patronal AFP |
| `segsocctapatronal` | Aporte patronal caja de salud |

Detalle del asiento en [05_contabilizacion.md](05_contabilizacion.md).
