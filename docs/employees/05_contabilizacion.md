# Contabilización de la planilla

Pantalla: **RRHH → Generación de planillas → Registro contable**
([accountingRecord.xhtml](../../view/employees/accountingRecord.xhtml)) ·
permiso `ACCOUNTINGRECORD` ·
[AccountingRecordAction](../../src/main/com/encens/khipus/action/employees/AccountingRecordAction.java) →
[AccountingRecordServiceBean](../../src/main/com/encens/khipus/service/finances/AccountingRecordServiceBean.java)

Convierte una planilla **oficial** en comprobantes contables y en el pago a los empleados.

## Lo que el usuario elige

1. **Gestión planilla** y la planilla generada oficial (buscador `gestionPayRollSearchModalPanel`).
2. **Proveedor** (`AccountingRecord.provider`) — la cuenta por pagar del devengado.
3. **Tipo de documento** por pagar.
4. Las **cuentas bancarias de origen** del pago y el reparto de importes en cuatro baldes:
   `nationalAmountForBank`, `foreignAmountForBank`, `nationalAmountForCheck`,
   `foreignAmountForCheck`, cada uno con su tipo de cambio.
5. Los empleados a pagar (checkbox por fila del `*PayrollGenerationDataModel`).

## Validaciones antes de contabilizar

| Comprobación | Mensaje |
|---|---|
| Hay al menos un empleado seleccionado | `AccountingRecord.error.emptyPayroll` |
| La moneda de la cuenta por pagar del proveedor coincide con la de la planilla | mismatch de moneda |
| Algún importe distinto de cero | `AccountingRecord.error.amounts` |
| Ninguno de los seleccionados ya está contabilizado ni tiene pago inactivo | `GeneratedPayroll.error.hasAccountingRecordOrHasInactivePayment` |
| Las cuotas de fondos rotatorios siguen vigentes (sólo planillas de sueldo) | error de cuotas |
| Cada empleado tiene **centro de costo** | `AccountingRecord.error.withoutCostCenter` |
| La planilla es OFICIAL | `AccountingRecord.error.withoutOfficialGeneration` |

La moneda base es `FinancesCurrencyType.P` (Bs) salvo para `GeneralPayroll` (docentes por
hora, muerto), que usa `D`.

## Comprobantes que genera

Todos comparten un `mainTransactionNumber` y quedan enlazados por
`AccountingRecordRelatedTransaction`:

| # | Tipo | Formulario | Glosa (clave i18n) | Cuándo |
|---|---|---|---|---|
| 1 | `PAYABLE_DOCUMENT` | documento por pagar | `AccountingRecord.voucherGlossForPayrollGeneration` | siempre, salvo aguinaldo |
| 2 | `SALARY_MOVEMENT` | `RRHH` | `AccountingRecord.voucherGlossForSalaryMovement` | si hay movimientos de sueldo o cobros de fondos rotatorios |
| 3 | `CHRISTMAS_PROVISION` | `RRHH` | `AccountingRecord.voucherGlossForChristmasProvision` | si hay detalle de provisión |
| 4 | `COMPENSATION_PREVISION` | `RRHH` | `AccountingRecord.voucherGlossForCompensationPrevision` | si hay detalle de previsión |
| 5 | `BANK_PAYMENT` | banco | `AccountingRecord.voucherGlossForPayment` | pagos por cuenta bancaria |
| 6 | `CHECK_PAYMENT` | `TESO_CON_DOSIF_EMP` | `AccountingRecord.voucherGlossForPayment` | pagos con cheque |

### 1. Documento por pagar (devengado)

```
DEBE   por cada (unidad de negocio, centro de costo):
         cuenta = jobCategory.nationalCurrencyDebitAccount   (o la ME si la moneda base es D)
         importe = Σ líquidos de los empleados de ese centro de costo
HABER  proveedor.payableAccount   por el total
```

El número lo da `payableDocumentService.nextPayableDocumentNumberForVoucher(HHRR)` y el
usuario contable es `companyConfiguration.getDefaultPayableFinanceUser()`.

Si la moneda base es ME, la diferencia de conversión se cierra contra
`companyConfiguration.requireBalanceExchangeRateAccount()` en el centro de costo
`exchangeRateBalanceCostCenter`.

### 2. Movimientos de sueldo y fondos rotatorios

```
DEBE   por (unidad de negocio, centro de costo): cuenta de débito de la categoría
       + débitos que arrastra el cobro de cuotas (RotatoryFundMigrationValue)
HABER  la cuenta contable de cada SalaryMovementType (tipomovsueldo.cuentactb)
       + créditos del cobro de cuotas
```

`rotatoryFundCollectionService.approveRotatoryFundCollectionsByPayroll(...)` **aprueba** los
cobros de cuotas en este momento: es cuando el descuento de préstamo/anticipo se hace firme.

Se crea sólo si el comprobante quedó con detalles.

### 3 y 4. Provisión de aguinaldo y previsión de indemnización

Ambas se calculan sobre el **total ganado** por la constante

```java
Constants.PREVISION_PROPORTION = 0.0833     // ≈ 1/12
```

| | Débito | Crédito |
|---|---|---|
| Provisión aguinaldo | `codctagastoaguimn` | `codctaprovaguimn` |
| Previsión indemnización | `codctagastoindemmn` | `codctaprevindemmn` |

(ambas cuentas viven en `JobCategory`, ver [01_modelo_de_datos.md](01_modelo_de_datos.md))

### 5 y 6. Pago

Se arma un comprobante por cuenta bancaria de origen. El destino depende de
`employee.paymentType`:

- `PAYMENT_BANK_ACCOUNT` → comprobante de banco, agrupado por cuenta.
- `PAYMENT_WITH_CHECK` → comprobante de cheque por empleado, formulario `TESO_CON_DOSIF_EMP`.

La moneda de cada empleado sale de
`employeeService.getEmployeesCurrencyByPaymentType(...)` y se guarda en
`currencyMapPaymentByEmployee`.

## Aportes a entidades de previsión

Aparte del asiento de planilla, desde la pantalla del **ciclo** se genera el documento por
pagar de los aportes (AFP y caja de salud) con
[PayrollGenerationInvestmentRegistrationCreateAction](../../src/main/com/encens/khipus/action/employees/PayrollGenerationInvestmentRegistrationCreateAction.java)
(botón *Registro de aportes*, permiso `PAYROLLGENERATIONINVESTMENTREGISTRATION`).

Sólo aparece cuando **todas** las planillas de gerentes y docentes fiscales del ciclo están
oficiales (`hasAllManagersAndFiscalProfessorsPayrollsAsOfficial`). Guarda una fila en
`regaporgenplan` por `SocialWelfareEntity` con el monto que el usuario captura, apoyándose
en el reporte de aportes (`workContributionReportAction`) para los importes.

## Bloqueo posterior

Una vez contabilizada, cada fila de planilla queda con `registrocontable` marcado y
`generatedPayrollService.validateHasAccountingRecord(...)` impide volver a contabilizarla o
cambiar su bandera de pago activo (`GeneratedPayroll.error.hasAccountingRecord`).
