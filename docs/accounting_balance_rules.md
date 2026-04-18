# Reglas de cálculo de saldo por naturaleza de cuenta

Reglas contables aplicadas al **Mayor Contable** (`MajorAccountingReportAction`) que deben reutilizarse en cualquier reporte nuevo que calcule saldos por cuenta.

## Naturaleza de la cuenta

Cada `CashAccount` tiene un `accountType` (enum `CashAccountType`) que determina la naturaleza contable:

| Tipo | Código | Naturaleza | Aumenta por | Fórmula de saldo |
|---|---|---|---|---|
| Activo | `A` | Deudora | Debe | `saldo + debe - haber` |
| Egreso / Gasto | `E` | Deudora | Debe | `saldo + debe - haber` |
| Pasivo | `P` | Acreedora | Haber | `saldo + haber - debe` |
| Capital / Patrimonio | `C` | Acreedora | Haber | `saldo + haber - debe` |
| Ingreso | `I` | Acreedora | Haber | `saldo + haber - debe` |

Valores del enum no mapeados (`OD`, `OA`, `Z`): el helper los trata como deudora por defecto (+1). Si se necesitan otras reglas, agregarlas explícitamente.

## Excepción: cuentas regularizadoras

Algunas cuentas **invierten la naturaleza de su grupo**:

- **Pérdidas Acumuladas** (Capital): naturaleza deudora, no acreedora.
- **Depreciaciones Acumuladas** (Activo): naturaleza acreedora, no deudora.
- **Previsiones** (Activo): naturaleza acreedora, no deudora.

Estas se marcan con el campo `regulating` (columna `ind_regulariz` en `arcgms`, valores `'S'`/`'N'`). El flag invierte la naturaleza inferida del `accountType`.

## Implementación de referencia

### Helper `natureSign` — [MajorAccountingReportAction.java](../src/main/com/encens/khipus/action/accounting/reports/MajorAccountingReportAction.java)

```java
private int natureSign(CashAccount ca) {
    if (ca == null || ca.getAccountType() == null) return +1;
    CashAccountType t = ca.getAccountType();
    boolean debtor = (t == CashAccountType.A || t == CashAccountType.E);
    if (Boolean.TRUE.equals(ca.getRegulating())) debtor = !debtor;
    return debtor ? +1 : -1;
}
```

Retorna `+1` (deudora) o `-1` (acreedora).

### Aplicación del signo

Dado `sign = natureSign(cuenta)`:

```java
// Saldo inicial con signo natural
BigDecimal initialBalance = rawBalance.multiply(BigDecimal.valueOf(sign));

// Running balance fila a fila
BigDecimal delta = debit.subtract(credit);
if (sign == -1) delta = delta.negate();
running = running.add(delta);
```

Fórmula equivalente: `saldo += sign * (debe − haber)`.

## Regla de oro: qué se transforma y qué no

| Dato mostrado | ¿Aplica signo? |
|---|---|
| Columna "Debe" por fila | **No.** Valor crudo del asiento. |
| Columna "Haber" por fila | **No.** Valor crudo del asiento. |
| Suma TOTALES "Debe" | **No.** Suma cruda de los movimientos. |
| Suma TOTALES "Haber" | **No.** Suma cruda de los movimientos. |
| Saldo inicial | **Sí.** Multiplicado por `sign`. |
| Saldo corriente (running) | **Sí.** Se acumula `delta * sign`. |

El usuario siempre ve los movimientos contables originales. Sólo el **saldo** (acumulado o inicial) refleja la convención de naturaleza.

## Origen de los datos

- `voucherAccoutingService.getBalance(startDate, code)` / `getBalancesByAccountCodes(...)` → retornan **saldo crudo** `sum(debit) - sum(credit)` pre-inicio del periodo. El reporte aplica el signo después.
- `voucherService.getTransactionMajorAccounting(...)` / `getTransactionsByAccountCodes(...)` → devuelven movimientos crudos (debit, credit positivos).

**No modificar los servicios** para aplicar el signo. Otros consumidores dependen del valor crudo. La inversión de signo es responsabilidad de la capa `action`.

## UI: marcar cuenta como regularizadora

Formulario [cashAccount.xhtml](../view/finances/cashAccount.xhtml): checkbox "Cta. Regularizadora" en la sección "Datos de la Cuenta", debajo de "Cta. Movimiento". Marcar en las cuentas conocidas (Pérdidas Acumuladas, Depreciaciones, Previsiones, etc.).

Alternativa SQL directa:

```sql
UPDATE arcgms SET ind_regulariz = 'S' WHERE cuenta IN ('3830100100', '1290100100', ...);
```

## Nota sobre `StringBooleanUserType`

La columna `ind_regulariz` usa `VARCHAR(1)` con valores `'S'`/`'N'` (no `'1'`/`'0'`). Esto se debe al `@TypeDef` global en [package-info.java](../src/main/com/encens/khipus/model/package-info.java) que registra `StringBoolean` con parámetros `ACRONYM_TRUE_VALUE='S'` / `ACRONYM_FALSE_VALUE='N'`.

Cualquier migración SQL que cree columnas booleanas en entidades mapeadas con `@Type(StringBooleanUserType.NAME)` debe usar:

```sql
varchar(1) NOT NULL DEFAULT 'N'
```
