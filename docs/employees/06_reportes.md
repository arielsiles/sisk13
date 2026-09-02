# Reportes

68 clases en
[action/employees/reports/](../../src/main/com/encens/khipus/action/employees/reports/) y
67 `.jrxml` en [view/employees/reports/](../../view/employees/reports/).

## Cómo está armado un reporte

Todos extienden `GenericReportAction`:

```java
public void generateReport(GeneratedPayroll gp) {
    setGeneratedPayroll(gp);                       // filtro que usarán las restrictions
    Map params = getReportParamsInfo(cycle);       // parámetros para el .jrxml
    setReportFormat(ReportFormat.XLS);             // o PDF
    super.generateReport("nombre", "/employees/reports/x.jrxml", titulo, params);
}

@Override protected String getEjbql() { return "SELECT ... FROM ..."; }

@Create public void init() {
    restrictions = new String[]{ "x.company=#{currentCompany}",
                                 "generatedPayroll = #{xAction.generatedPayroll}" };
    sortProperty = "...";
    groupByProperty = "...";     // opcional
}
```

La consulta es **EJBQL**, no SQL: se resuelve contra el modelo JPA. Los `.jrxml` reciben los
campos en el orden del `SELECT`.

Ojo con el caché del contexto de persistencia: ver la memoria *Reportes con snapshot viejo*
— si un reporte no cuadra con lo que se ve en pantalla, sospechar del `EntityManager` de
conversación.

## Desde la lista de planillas generadas

Menú *Generar* en [generatedPayrollListBody.xhtml](../../view/employees/generatedPayrollListBody.xhtml).
Todos reciben la `GeneratedPayroll` de la fila.

| Reporte | Action | Fuente | Fmt |
|---|---|---|---|
| Planilla a RRHH | `managersPayrollReportAction` | `ManagersPayroll` | XLS |
| Planilla a RRHH por centro de costo | `managersPayrollByCostCenterReportAction` | `ManagersPayroll` | XLS |
| Planilla por área | `managersPayrollByAreaReportAction` | `ManagersPayroll` | XLS |
| Planilla a presidencia (extendida) | `managersPayrollExtendedReportAction` | `ManagersPayroll` | XLS |
| Planilla tributaria por categoría | `categoryTributaryPayrollReportAction` | `CategoryTributaryPayroll` | XLS |
| Planilla fiscal por categoría | `categoryFiscalPayrollReportAction` | `CategoryFiscalPayroll` | XLS |
| **Boleta de pago por categoría** | `categoryPaymentSlipReportAction` | `CategoryFiscalPayroll` | PDF |
| Reporte de control | `controlReportReportAction` | `ControlReport` | XLS |
| Planilla banco | `payrollBankReportAction` | — | XLS |
| **Archivo UNISUELDO** | `payrollBankUnisueldoReportAction` | — | XLS |
| **Archivo UNISUELDO v2** | `payrollBankUnisueldo2ReportAction` | `CategoryFiscalPayroll` | XLS |
| Planilla aguinaldo / por área / por centro de costo | `christmasBonusPayroll*ReportAction` | `ChristmasPayroll` | XLS |
| Boleta de aguinaldo | `christmasPaymentSlipReportAction` | `ChristmasPayroll` | PDF |
| Planilla general / por centro de costo *(muerto)* | `generalPayroll*ReportAction` | `GeneralPayroll` | XLS |
| Planilla docente laboral *(muerto)* | `laboralProfessorPayrollReportAction` | `FiscalProfessorPayroll` | XLS |

### Archivo UNISUELDO

Formato de carga masiva del banco. La v2
([PayrollBankUnisueldo2ReportAction](../../src/main/com/encens/khipus/action/employees/reports/PayrollBankUnisueldo2ReportAction.java))
arma la cabecera con el NIT de la compañía, fecha `yyyyMMdd`, número de carga, servicio
`UNISUELDO`, cuenta de débito (`companyConfiguration.foreignBankAccountForPayment`),
moneda, mes y año. **El email destino sale de `companyConfiguration.getUnisueldoEmail()`**,
configurable en *Preferencias de compañía → Recursos humanos* desde la v6.0.110
(commit `a9164389`); antes estaba hardcodeado en la clase.

## Desde la pantalla del ciclo

[payrollGenerationCycle.xhtml](../../view/employees/payrollGenerationCycle.xhtml), sobre los
consolidados del mes.

| Reporte | Action | Fuente | Fmt |
|---|---|---|---|
| Ver planilla tributaria | `payrollGenerationCycleAction.viewTributaryPayroll` | `TributaryPayroll` | — |
| Exportar planilla tributaria | `tributaryPayrollReportAction` | `TributaryPayroll` | XLS |
| **Formato Da Vinci** | `payrollDavinciFormatReportAction` | `TributaryPayroll` | XLS |
| Ver planilla fiscal | `payrollGenerationCycleAction.viewFiscalPayroll` | `FiscalPayroll` | — |
| Exportar planilla fiscal | `fiscalPayrollReportAction` | `FiscalPayroll` | XLS |
| **Boleta de pago** | `paymentSlipReportAction` | `FiscalPayroll` | PDF |
| **Aportes laborales/patronales** | `workContributionReportAction` | `FiscalPayroll` + `TributaryPayroll` | XLS |
| Aportes por centro de costo | `workContributionByCostCenterReportAction` | ídem | XLS |

El reporte de aportes agrupa por **entidad de fondo de pensiones**
(`contract.pensionFundOrganization`) y suma:

```sql
SUM(tributaryPayroll.cns),
SUM(fiscalPayroll.retentionAFP),
SUM(tributaryPayroll.patronalProffesionalRiskRetentionAFP),
SUM(tributaryPayroll.patronalProHomeRetentionAFP),
SUM(tributaryPayroll.patronalSolidaryRetentionAFP)
```

Un contrato **sin AFP asignada** cae en un grupo nulo y descuadra la planilla de aportes.

## Reportes del menú

| Menú | Reporte | Permiso |
|---|---|---|
| Reportes | Resumen de planilla por forma de pago | `SUMMARYPAYROLLBYPAYMENTMETHODREPORT` |
| Reportes | Resumen nacional de planilla de gerentes | `MANAGERSNATIONALSUMMARYPAYROLLREPORT` |
| Reportes | Resumen nacional de aguinaldos | `CHRISTMASNATIONALSUMMARYREPORT` |
| Reportes | Fechas especiales | `SPECIALDATEREPORT` |
| Reportes | Contratos puestos | `JOBCONTRACTREPORT` |
| Reportes | Movimientos de sueldo | `SALARYMOVEMENTREPORT` |
| Reportes | Documentos de descargo | `DISCHARGEDOCUMENTSREPORT` |
| Reportes | Registro de formularios | `FORMRECORDREPORT` |
| — | Estados de marcado | `markStateReportAction` |
| — | Personal por área | `PERSONBYAREAREPORT` |
| — | Plan de vacaciones por empleado | `vacationPlanningEmployeeReportAction` |
| Reportes *(académico, muerto)* | Resumen planilla académica, docentes laborales, encuestas, evaluación docente, postulantes | varios |

Los reportes con filtros (fechas, unidad de negocio, categoría, empleado…) declaran esos
filtros como propiedades del propio action y los referencian desde `restrictions` con EL:
`#{salaryMovementReportAction.initDate}`. Algunos usan además `framework:entity-query`
declaradas en [action/employees/components.xml](../../src/main/com/encens/khipus/action/employees/components.xml)
para poblar los combos (por ejemplo `gestionPayrollToManagersSummaryList`).

## Scriptlets

Los reportes con subtotales calculados en Java usan scriptlets de JasperReports, en el
mismo paquete: `GeneralPayrollReportScriptlet`, `ManagersPayrollReportScriptlet`,
`ManagersPayrollSummaryReportScriptlet`, `ManagersPayrollExtendedReportScriptlet`,
`PayrollSummaryByCurrencySubReportScriptlet`, `PayrollSummaryBySedeSubReportScriptlet`,
`WorkContributionByCostCenterReportScriptlet`, `MarkStateReportScriptlet`,
`ChristmasNationalSummaryReportScriptlet` y los académicos.

Acumulan en mapas con claves compuestas — ver
[WorkContributionReportUtil](../../src/main/com/encens/khipus/action/employees/reports/WorkContributionReportUtil.java),
que centraliza el armado de esas claves (`CNS_<cencos>`, `AFP_<cencos>_<afp>`,
`PROHOME_<cencos>_<afp>`, …). **Al agregar una columna a uno de estos reportes hay que
tocar el scriptlet, no sólo el `.jrxml`.**

## Al agregar un reporte nuevo

1. Clase `XReportAction extends GenericReportAction` en `action/employees/reports/`.
2. `.jrxml` en `view/employees/reports/` (se copia al war con el `ant explode`).
3. Título y etiquetas en `messages_app.properties` — **nunca literales en el `.xhtml`**.
4. Enlace en la vista correspondiente con `rendered="#{s:hasPermission('X','VIEW')}"`.
5. Si es una opción de menú nueva: alta de la funcionalidad y del `derechoacceso`
   (ver la memoria *Permisos: por qué no aparece una opción* — `hasPermission` falla en
   silencio y `derechoacceso.idmodulo NULL` ignora el grant).
