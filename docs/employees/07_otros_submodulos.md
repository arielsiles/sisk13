# Otros submódulos

Todo lo que no es la generación de planillas.

## Maestro de empleados

**RRHH → Empleados** ([employeeList.xhtml](../../view/employees/employeeList.xhtml)) ·
permiso `EMPLOYEE` · [EmployeeAction](../../src/main/com/encens/khipus/action/employees/EmployeeAction.java)

`Employee` extiende `Person` (`persona`): nombres, apellidos, CI, extensión, país, género,
fecha de nacimiento y los datos de contacto viven en la superclase. En `empleado` sólo
están los datos laborales y las banderas (ver [01_modelo_de_datos.md](01_modelo_de_datos.md)).

Pestañas asociadas: formación académica (`EmployeeAcademicFormationAction`), experiencia,
cuentas bancarias (`BankAccountService`, permiso `BANKACCOUNT`).

## Contratos y puestos

```
Contract (contrato)          ← alta/baja, modalidad, estado, ciclo, AFP y caja de salud
   └─ JobContract (contratopuesto)   ← liga el contrato a un Job (puesto)
         └─ Job (puesto)             ← cargo + categoría + sueldo + unidad organizacional
```

| Pantalla | Action | Permiso |
|---|---|---|
| Contratos | `JobContractAction` (660 líneas, la más grande del módulo) | `JOBCONTRACT` |
| Modalidades de contrato | `ContractModeAction` | `CONTRACTMODE` |
| Cargos | `ChargeAction` | — |
| Categorías de puesto | `JobCategoryAction` | `JOBCATEGORY` |
| Sectores | `SectorAction` | `SECTOR` |
| Tipos de salario | `KindOfSalaryAction` | `KINDOFSALARY` |
| Unidades organizacionales | `OrganizationalUnitAction`, `OrganizationalUnitTreeAction` | `ORGANIZATIONALUNIT` |
| Niveles organizacionales | `OrganizationalLevelAction` | `ORGANIZATIONALLEVEL` |

Las **unidades organizacionales** son un árbol con niveles nombrados. La planilla busca el
ancestro de nivel `"AREA"` por nombre literal:

```java
OrganizationalLevel areaLevel = findOrganizationalLevelByName("AREA");
OrganizationalUnit  area      = findFather(job.getOrganizationalUnit(), areaLevel);
```

Si no existe un nivel llamado exactamente `AREA`, la columna *Área* de la planilla queda
vacía y los reportes "por área" salen sin agrupar.

### Generación de contratos desde plantilla

- `GenerateEmployeeContractAction` / `GenerateTeacherContractAction` /
  `GenerateAccidentalAssociationAction` +
  [GenerateContractServiceBean](../../src/main/com/encens/khipus/service/employees/GenerateContractServiceBean.java):
  rellenan una `Template` (documento Word) con los datos del empleado y la devuelven como
  archivo. Los dos últimos son académicos.

## Movimientos de sueldo

**RRHH → Movimientos de sueldo** · permiso `SALARYMOVEMENT` ·
`SalaryMovementAction` / `SalaryMovementTypeAction`

Un `SalaryMovement` es un ingreso o descuento puntual: empleado + gestión planilla + fecha
+ monto + moneda + tipo. El `SalaryMovementType` lleva el `MovementType` y **la cuenta
contable** con la que se acredita en el comprobante de movimientos.

Qué tipos lee efectivamente la generación: ver
[03_calculos_planilla.md](03_calculos_planilla.md). En particular, `RCIVA` es hoy la vía
real por la que se retiene el RC-IVA.

## Vacaciones

> Sin uso en la réplica: `vacacion` y `planvacacion` tienen 0 filas.

```
VacationPlanning (planvacacion)     ← por JobContract: años de antigüedad, días, usados, libres
   └─ VacationGestion (gestionvacacion)   ← una por año de antigüedad
         └─ Vacation (vacacion)           ← el descanso concreto
```

- `VacationRule` (`reglavacacion`): días de vacación por tramo de antigüedad
  (`aniosinicio` – `aniosfin` → `diasvacacion`). Permiso `VACATIONRULE`.
- `VacationGestionServiceBean.synchronizeGestionVacation(...)` recorre año por año desde el
  inicio del plan hasta los años de antigüedad, crea la `VacationGestion` que falte con los
  días de la regla, y recalcula usados/libres de las existentes. Si no hay regla para un
  año lanza `VacationRuleUndefinedYearException` (salvo que se pida ignorarlo).
- Estados de `Vacation`: `PENDING` → `APPROVED` → `ANNULLED`.
- **Al aprobar** se crea una `SpecialDate` asociada; al anular se borra. Ésa es la única
  conexión entre vacaciones y planilla: la vacación aprobada se ve como permiso con goce de
  haber y no genera falta.

Pantallas: `vacationPlanningList.xhtml` (permiso `VACATIONPLANNING`), `vacation.xhtml`,
`vacationRuleList.xhtml`. Reporte: `vacationPlanningEmployeeReportAction`.

## Retiros y finiquitos

> Sin uso en la réplica: `retiro` tiene 0 filas.

```
Dismissal (retiro)            ← por Contract: fecha de retiro, días trabajados, monto, estado
   └─ DismissalDetail (detalleretiro)   ← un concepto liquidado, con su causa y su comprobante
```

- `DismissalCause` (`causaretiro`), `DismissalRule` (`reglaretiro`, tipo `MONTHLY`).
- Estados: `Dismissal` → `PENDING` / `OPEN` / `CLOSE`; `DismissalDetail` con su propio estado
  y `motivoreversion` para revertir.
- Cada detalle guarda `notrans` (número de transacción contable), monto, moneda y tipo de
  cambio, y puede llevar un archivo adjunto (`File`).
- Existe además `DismissalPrevision` (`previsionretiro`) para la previsión de indemnización.

Pantallas: `dismissalList.xhtml` (permiso `DISMISSAL`), `dismissalCauseList.xhtml`,
`dismissalRuleList.xhtml`.

## Documentos de descargo

> Sin uso en la réplica: `documentodescargo` tiene 0 filas.

`DischargeDocument` liga un `JobContract` con una `GestionPayroll` y una entidad, con
estados `PENDING` → `APPROVED` → `NULLIFIED`
([DischargeDocumentServiceBean](../../src/main/com/encens/khipus/service/employees/DischargeDocumentServiceBean.java)).
Al aprobarse crea un documento contable vía `FinanceAccountingDocumentService`.

Su efecto en la planilla: `RetentionValidatorService.applyRetention()` **desactiva la
retención del 15,5 %** si el empleado tiene un documento de descargo para esa gestión
planilla. Como la tabla está vacía, hoy nunca se desactiva.

## Cronograma de planillas

`GestionPayrollSchedule` (`cronogramagp`) + `GestionPayrollScheduleAction` (591 líneas),
permiso `GESTIONPAYROLLSCHEDULE`. Genera en lote las `GestionPayroll` de toda una gestión a
partir de una plantilla de fechas, para no crearlas mes a mes a mano.

## Académico / evaluaciones — legado muerto

Todo esto viene de la etapa universitaria de KHIPUS y tiene 0 filas:

| Área | Entidades | Actions |
|---|---|---|
| Postulantes | `Postulant`, `PostulantCharge`, `PostulantAcademicFormation` | `PostulantAction`, `PostulantReportAction` |
| Encuestas | `PollForm`, `PollCopy`, `Question`, `Section`, `PollPunctuation` | `PollFormAction`, `PollCopyAction`, `QuestionAction` |
| Evaluaciones | `ScheduleEvaluation`, `FinalEvaluationForm`, `EvaluationCriteria`, `TeacherEvaluation` | `*ScheduleEvaluationFormAction`, `FinalEvaluationFormAction` |
| Estructura académica | `Career`, `Faculty`, `Subject`, `SubjectGroup`, `AcademicPeriod`, `Cycle` | `SubjectAction`, `CycleAction` |
| Planillas docentes | `GeneralPayroll`, `FiscalProfessorPayroll`, `AcademicGestionPayroll` | `fillProffesorsPayroll`, `fillFiscalProfessorPayroll` |

`Cycle` y `Gestion` **sí se usan**: `Gestion` es el año contable y `Cycle` es obligatorio en
`Contract` (`idciclo NOT NULL`). Lo demás se puede considerar inerte, pero **no borrarlo sin
verificar por cliente**: `GeneratedPayroll` tiene relaciones en cascada hacia
`GeneralPayroll` y `FiscalProfessorPayroll`, y el menú y los permisos los referencian.
