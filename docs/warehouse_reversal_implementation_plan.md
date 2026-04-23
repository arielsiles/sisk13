# Plan de implementación — Anulación y reversión de Órdenes de Compra y Vales

Plan por fases y tareas específicas para implementar la funcionalidad de **anular/revertir** Órdenes de Compra y Vales de Almacén, con reversión completa de inventario (Costo Promedio), contra-asiento contable y auditoría.

Basado en [warehouse_order_voucher_flow.md](warehouse_order_voucher_flow.md). Cada tarea es autónoma y debe aprobarse antes de implementar.

---

## Alcance

**Dos flujos de anulación**:

1. **Anular Orden de Compra** (con todos sus registros asociados): revierte la OC y su vale de RECEPCION auto-generado, su asiento de ingreso, y (si estaba LIQ) su asiento de liquidación.
2. **Anular Vale independiente** (sin OC): revierte cualquier vale cuyo `operation` sea null y no venga de otro módulo.

**Decisiones adoptadas** (respuestas del usuario):

| # | Decisión | Valor |
|---|---|---|
| 1 | Estados anulables de OC | APR, FIN, **LIQ** |
| 2 | Estrategia contable | **Opción B** — contra-asiento nuevo |
| 3 | Valorización del movimiento físico | **Costo actual** (`ProductItem.unitCost` al momento de la anulación) |
| 4 | Período cerrado | **Permitir** |
| 5 | Permisos | **Nuevos** (`WAREHOUSEPURCHASEORDERREVERSE`, `WAREHOUSEVOUCHERREVERSE`) |
| 6 | Auditoría | **Sí** (`nullifyDate`, `nullifyUser`, `nullifyReason`) |

**Respuestas finales del usuario (ronda 2):**

| # | Decisión | Valor |
|---|---|---|
| A7 | Asientos en OC LIQ | **IA** se revierte con contra-asiento (cuentas de inventario). **CP** solo se marca `state=ANL` y en la glosa se agrega el motivo entre asteriscos. |
| A8 | Pagos/anticipos cobrados | **Solo anular** (`state=ANL`) y actualizar glosa con motivo entre asteriscos — no revierten asiento |
| A10 | Cheque emitido en OC CONTADO | **Anular el cheque** (no bloquear la OC) |
| B1 | Estados anulables del Vale | **APR y PAR** (misma lógica) |
| B2 | Tipos de vale permitidos | RECEPCION, EGRESO, CONSUMO, DEVOLUCION, BAJA, REPROCESO, TRANSFERENCIAS. **Bloquear**: `operation != null` + almacén de leche con RECEPCION |
| C1 | Cuenta de ajuste | **Pendiente / no se usa** por ahora → el plan se ajusta a **reverso simétrico** (ver abajo) |

---

## Estrategia técnica clave

### Reversión simétrica (sin cuenta de ajuste)

Como se descartó la cuenta de ajuste (respuesta C1), la reversión es **simétrica al movimiento original**:

Al anular un movimiento de **entrada** (recepción):
```
qty_revert    = movementDetail.quantity
amount_revert = movementDetail.amount          ← monto original del movimiento (no costo actual)

Inventory.unitaryBalance          -= qty_revert
InventoryDetail.quantity          -= qty_revert
ProductItem.investmentAmount      -= amount_revert
ProductItem.ct                    -= movementDetail.purchasePrice
unitCost se recalcula             = investmentAmount / sum(unitaryBalance)
```

Al anular un movimiento de **salida**: los signos se invierten (suma en lugar de resta).

**Implicancia**: el `unitCost` actual puede cambiar tras la anulación (no se preserva), pero el reverso es limpio contablemente y no requiere cuenta de ajuste.

**Validaciones**:
- Si al restar el stock quedaría negativo → bloquear (pedir revertir movimientos posteriores primero).
- Si `ProductItem.controlValued == false` → solo ajustar cantidad, no costo monetario.

### Contra-asiento contable para el asiento IA (Ingreso Almacén)

Aplica al asiento generado en la finalización de OC o en la aprobación de Vale:

- Nuevo `Voucher` con DEBE/HABER **invertidos** línea por línea respecto al original.
- Montos = **idénticos** al asiento original (simétrico, no recalculados a costo actual).
- `relatedTransactionNumber` = `originalVoucher.transactionNumber`.
- `state = PEN` (se aprueba después vía stored procedure).
- `gloss` = `"ANULACION - " + originalGloss + " [Motivo: <razón>]"`.
- Tipo documento: `IA` → `SA` (intercambiados).
- El `Voucher` original queda **intacto** (no se modifica su estado); el contra-asiento lo compensa.

### Anulación directa del asiento CP (Liquidación/Pago) y pagos

Aplica al asiento generado en la liquidación de OC (CP), a cheques, a `PurchaseOrderPayment` y `AdvancePayment`:

- **No** se genera contra-asiento.
- Se modifica el registro original:
  - `state = ANL`
  - `gloss = "*" + motivo + "* " + glosaOriginal` (motivo entre asteriscos al inicio).
- Para cheques: invocar el flujo existente de anulación de cheque del módulo de tesorería (a localizar en Fase 3).

Esta decisión respeta la semántica contable existente: los IA afectan inventario (requieren compensación física), los CP afectan sólo dinero/pasivos (basta marcarlos como nulos).

---

## Fase 0 — Preparación de esquema y configuración

Cambios transversales que habilitan todas las fases siguientes.

### Tarea 0.1 — Columnas de auditoría en `com_encoc`
Agregar a `PurchaseOrder`:
- `nullify_date` (Date)
- `nullify_user` (String, 4 chars — FK a usuario de finanzas)
- `nullify_reason` (String, 250)
- `reversal_voucher_id` (Long, FK a `sf_tmpenc`)

Generar SQL migration: `query/v6.0.70/add_purchase_order_nullify_columns.sql`.

### Tarea 0.2 — Columnas de auditoría en `inv_vales`
Agregar a `WarehouseVoucher`:
- `nullify_date`, `nullify_user`, `nullify_reason`
- `reversed_warehouse_voucher_id` (Long, auto-referencia al vale que lo originó o que lo anula).

SQL migration: `query/v6.0.70/add_warehouse_voucher_nullify_columns.sql`.

### Tarea 0.3 — Cuenta de ajuste en `CompanyConfiguration` — **POSTERGADA**
Originalmente planificada, pero descartada por respuesta C1 = "no se usa". El reverso es simétrico (mismos montos del asiento original), por lo que no se requiere cuenta de ajuste. Si en el futuro se necesita volver a valorización a costo actual, esta tarea se reactivará.

### Tarea 0.4 — Permisos y datos semilla
- Agregar permisos `WAREHOUSEPURCHASEORDERREVERSE` y `WAREHOUSEVOUCHERREVERSE` en el módulo de permisos (datos semilla / `resources/import-scripts/`).
- Asignar por defecto al rol administrador.

### Tarea 0.5 — Mensajes i18n
- Agregar claves en `messages_es.properties` y `messages_en.properties`:
  - `WarehousePurchaseOrder.reverse.confirm`, `WarehousePurchaseOrder.reverse.reasonRequired`, `WarehousePurchaseOrder.reverse.success`, `WarehousePurchaseOrder.reverse.errorHasAdvancePayments`, etc.
  - Equivalentes para `WarehouseVoucher.reverse.*`.

---

## Fase 1 — Servicios base de reversión (reutilizables por Fases 2 y 3)

Servicios utilitarios que encapsulan la lógica de reversión. Sin cambios de UI aún.

### Tarea 1.1 — `ReverseInventoryService`
Nuevo servicio (interfaz + bean) en `src/main/com/encens/khipus/service/warehouse/`:

```java
public interface ReverseInventoryService {
    // Revierte cantidad física en inv_inventario + inv_inventario_detalle
    void reverseInventory(WarehouseVoucher warehouseVoucher,
                          Warehouse warehouse,
                          MovementDetail movementDetail) throws ...;

    // Revierte ProductItem (unitCost, investmentAmount, ct) a costo actual
    void reverseProductItemCost(WarehouseVoucher warehouseVoucher,
                                MovementDetail movementDetail);

    // Revierte acumulados de inv_invmes
    void reverseInventoryHistory(MovementDetail movementDetail);
}
```

**Reglas del bean**:
- Si el movimiento original fue `E` (entrada): invocar la lógica opuesta de `removeFromInventory` usando `quantity` original, valorizando a `unitCost` actual.
- Si fue `S` (salida): inverso de `addAtInventory`.
- Si al restar el stock quedaría negativo → lanzar `InventoryReversalException` con mensaje claro (no permitir).
- Si el `ProductItem` no tiene `controlValued=true` → solo ajustar cantidad, no costo.

### Tarea 1.2 — Método `createReverseAccountEntry` en `WarehouseAccountEntryServiceBean`
Método que recibe el `Voucher` original y el `WarehouseVoucher` de origen, y genera un nuevo `Voucher`:

```java
public Voucher createReverseAccountEntry(WarehouseVoucher sourceWarehouseVoucher,
                                          Voucher originalVoucher,
                                          String reason) throws ...;
```

**Reglas (reverso simétrico)**:
- Cada `VoucherDetail` del original se replica invirtiendo DEBE/HABER.
- Monto de cada línea = **idéntico** al original.
- `gloss` = `Constants.ANNULMENT_PREFIX + originalVoucher.gloss + " [Motivo: " + reason + "]"`.
- `relatedTransactionNumber` = `originalVoucher.transactionNumber`.
- Tipo documento: `IA` ↔ `SA` (intercambiados).
- Persistir con `voucherAccoutingService.saveVoucher(...)`.

### Tarea 1.2.b — Método `annulPaymentVoucher` en `WarehouseAccountEntryServiceBean` (o servicio contable)
Para asientos CP y pagos:

```java
public void annulVoucher(Voucher voucher, String reason);
```

**Reglas**:
- `voucher.state = VoucherState.ANL`
- `voucher.gloss = "*" + reason + "* " + voucher.gloss` (motivo entre asteriscos al inicio)
- No genera contra-asiento. No afecta inventario.
- Uso: asientos CP de liquidación de OC, pagos, anticipos.

### Tarea 1.3 — Método `reverseMovementDetails` en `ApprovalWarehouseVoucherServiceBean` (o servicio nuevo)
Orquestador que, dado un `WarehouseVoucher` aprobado:
1. Itera sus `MovementDetail` del `InventoryMovement` aprobado.
2. Para cada uno: `reverseInventoryService.reverseInventory(...)` + `reverseProductItemCost(...)` + `reverseInventoryHistory(...)`.
3. Crea un nuevo `InventoryMovement` con descripción "ANULACION - …" para auditoría.
4. Marca los detalles originales con `state=ANL` (sin borrarlos).

### Tarea 1.4 — Agregar constantes y utilidades
- `Constants.ANNULMENT_PREFIX = "ANULACION - "`
- Validadores: `ReversalValidator.canReverseWarehouseVoucher(warehouseVoucher)` — chequea estado, operation, tipo almacén.

---

## Fase 2 — Anulación de Vale independiente

El caso más simple. Reusa Fase 1.

### Tarea 2.1 — Servicio `ReverseWarehouseVoucherServiceBean`
Nuevo bean en `src/main/com/encens/khipus/service/warehouse/`:

```java
public interface ReverseWarehouseVoucherService {
    void reverseWarehouseVoucher(WarehouseVoucherPK id,
                                  String reason,
                                  String userCode) throws ReverseNotAllowedException, ...;
}
```

**Flujo**:
1. Validar:
   - `state ∈ {APR, PAR}`.
   - `operation == null` (no viene de otro módulo).
   - No es vale del almacén de leche (`COD_WAREHUOSE_MILK_COLLECTED`) + RECEPCION.
   - No fue ya anulado.
2. Según tipo:
   - **RECEPCION / EGRESO / CONSUMO / DEVOLUCION / BAJA / REPROCESO**: invocar `reverseMovementDetails(...)` + `createReverseAccountEntry(...)`.
   - **TRANSFERENCIA entre almacenes (T)**: invocar `reverseMovementDetails(...)` para ambos (salida origen + entrada destino). **No crear asiento** (no existe original).
   - **TRANSFERENCIA entre U.Ejec.**: invocar dos veces `createReverseAccountEntry` (origen + destino).
3. Setear:
   - `warehouseVoucher.state = ANL`
   - `warehouseVoucher.nullifyDate = new Date()`
   - `warehouseVoucher.nullifyUser = userCode`
   - `warehouseVoucher.nullifyReason = reason`
4. Merge/flush.

### Tarea 2.2 — Acción UI `WarehouseVoucherUpdateAction.annul()`
Agregar método:
```java
@Restrict("#{s:hasPermission('WAREHOUSEVOUCHERREVERSE','VIEW')}")
public String annul() {
    // Validar reason no vacío
    // Invocar reverseWarehouseVoucherService.reverseWarehouseVoucher(...)
    // Mensajes de éxito/error
}
```

### Tarea 2.3 — UI del vale (`view/warehouse/warehouseVoucher.xhtml` o modal en update)
- Botón "Anular" visible cuando `state=APR`, `operation=null` y usuario tiene permiso.
- Modal de confirmación con input obligatorio "Motivo de Anulación" (`warehouseVoucherUpdateAction.nullifyReason`).
- Llama a `annul()`.

### Tarea 2.4 — Lista de vales (`warehouseVoucherList.xhtml`)
- Mostrar estado ANL con estilo distintivo.
- Agregar filtro de estado incluyendo ANL.
- Tooltip con `nullifyReason` al pasar sobre el vale anulado.

### Tarea 2.5 — Tests manuales
Casos de prueba:
1. Vale RECEPCION simple → anular → inventario, costo y asiento revertidos.
2. Vale EGRESO → anular → stock vuelve, asiento invertido.
3. Vale TRANSFERENCIA tipo T → anular → ambos almacenes revertidos.
4. Vale con `operation != null` → no debe permitirse.
5. Vale en estado PEN → no debe permitirse.
6. Stock insuficiente para reversión → error claro.
7. Sin cuenta de ajuste configurada → error claro.

---

## Fase 3 — Anulación de Orden de Compra

Reusa Fase 1 y Fase 2 para revertir el vale asociado. Agrega lógica específica de la OC.

### Tarea 3.1 — Servicio `ReversePurchaseOrderServiceBean`
Nuevo bean. Método:
```java
public void reversePurchaseOrder(PurchaseOrder purchaseOrder,
                                  String reason,
                                  String userCode) throws ReverseNotAllowedException, ...;
```

**Flujo**:
1. Validaciones:
   - `state ∈ {APR, FIN, LIQ}`.
   - No fue ya anulada.
2. Según estado actual:
   - **APR**: solo cambiar estado a ANL + auditoría (nada físico que revertir, igual que el flujo `nullifyWarehousePurchaseOrder` actual).
   - **FIN**: buscar el `WarehouseVoucher` de RECEPCION (`findWarehouseVoucherByPurchaseOrder`) → invocar `ReverseWarehouseVoucherService.reverseWarehouseVoucher(...)` (Fase 2). Esto revertirá inventario + **contra-asiento IA** (reverso simétrico).
   - **LIQ**: lo del FIN + **anulación directa** del asiento CP (llamar a `annulVoucher(cpVoucher, reason)` de Tarea 1.2.b).
3. Para cada `PurchaseDocument` asociado: `state=ANL`, `hasVoucher=false` (reutilizar o extender `nullifyInvoicesPurchaseOrder`).
4. Para cada `PurchaseOrderPayment` asociado: llamar a `annulVoucher` sobre su asiento (si tiene) y marcar el pago como ANL con motivo en la glosa.
5. Si hubo **cheque emitido**: invocar el flujo existente de anulación de cheques del módulo de tesorería (a localizar al inicio de la tarea — posible servicio `CheckService` o similar). Pasar `reason` a la anulación del cheque.
6. Para cada **anticipo** (`AdvancePayment`) cobrado: `annulVoucher` sobre su asiento + marcar ANL con glosa de motivo.
7. Setear:
   - `purchaseOrder.state = ANL`
   - `purchaseOrder.nullifyDate`, `nullifyUser`, `nullifyReason`
   - `purchaseOrder.balanceAmount` restaurado al valor previo (si aplica)
8. Merge/flush.

### Tarea 3.2 — Localizar flujo de anulación de cheques
Investigar y documentar cómo se anula un cheque en el módulo de tesorería (revisar `Check`, `CheckService`, `PurchaseOrderPayment.getCheck()`). Definir el punto de entrada exacto que invocará `reversePurchaseOrder` cuando detecte un pago con cheque.

### Tarea 3.3 — Acción UI `WarehousePurchaseOrderAction.reverse()`
Nueva acción:
```java
@BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
@End
@Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDERREVERSE','VIEW')}")
public String reverseWarehousePurchaseOrder() {
    // validar motivo
    // invocar reversePurchaseOrderService
    // mensajes
}
```

### Tarea 3.4 — UI de la OC (`view/warehouse/warehousePurchaseOrder.xhtml`)
- Botón "Revertir/Anular" visible cuando `state ∈ {APR, FIN, LIQ}` y usuario tiene permiso. Distinguir del actual botón "Anular" (que solo permite PEN/APR sin reversión).
- Modal de confirmación con motivo obligatorio.
- Mensaje de advertencia claro sobre lo que se va a revertir (inventario, asientos, pagos).

### Tarea 3.5 — Lista de OCs (`warehousePurchaseOrderList.xhtml`)
- Mostrar estado ANL.
- Tooltip con `nullifyReason`.
- Filtro de búsqueda por estado ANL.

### Tarea 3.6 — Tests manuales
Casos:
1. OC en APR → anular → solo cambia estado (sin vale, sin asiento, sin stock).
2. OC en FIN (CRÉDITO) → anular → revierte vale, asiento de ingreso, stock y costo. Proveedor queda con saldo 0 de la OC.
3. OC en FIN (CONTADO) → anular → igual que crédito (el pago ya se hizo en finalización).
4. OC en LIQ → anular → revierte asiento de liquidación + asiento de ingreso + vale + stock.
5. OC en LIQ con cheque emitido → debe bloquear.
6. OC con anticipos cobrados → debe bloquear (o revertir si decidimos).
7. Diferencia de costo mayor → ajuste a cuenta `annulmentAdjustmentAccount` registrada.
8. Permiso faltante → no aparece botón.

---

## Fase 4 — Reportes y visibilidad (opcional pero recomendado)

### Tarea 4.1 — Reporte de anulaciones
- Nuevo reporte en `reports/` listando OCs y vales anulados en un rango de fechas, con motivo, usuario, montos revertidos.

### Tarea 4.2 — Referencia cruzada en UI
- En el detalle de una OC o vale anulado: link al contra-asiento generado y al vale de ajuste.
- En el contra-asiento: link al vale/OC original.

---

## Fase 5 — Validación final

### Tarea 5.1 — Revisión de impacto en reportes existentes
Verificar que reportes que suman montos/stock ignoren correctamente los estados ANL:
- Mayor Contable (ya filtra `state <> 'ANL'`).
- Inventario físico valorado.
- Reportes de compras.
- Kardex de artículos.

### Tarea 5.2 — Revisión de cierre mensual
El cierre mensual (`MonthProcessServiceBean`) no debe permitir anular transacciones de meses ya cerrados si no se acordó (o debe permitirlo explícitamente — definir).

### Tarea 5.3 — Documentación de usuario
Manual breve de uso: cuándo se puede/no se puede anular, qué se revierte, implicancias contables.

---

## Orden sugerido de implementación y aprobación

| Fase | Dependencias | Puede validarse independientemente |
|---|---|---|
| 0 | — | Sí (migraciones + permisos) |
| 1 | 0 | Parcial (tests unitarios de servicios) |
| 2 | 0, 1 | **Sí (casos de uso completos de Vale independiente)** |
| 3 | 0, 1, 2 | Sí (casos de uso completos de OC) |
| 4 | 2, 3 | Sí |
| 5 | 2, 3 | Sí |

Se recomienda **implementar y validar Fase 2 antes de comenzar Fase 3**: el vale independiente es más simple y sirve como prueba de la infraestructura de la Fase 1.

---

## Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Pérdida de integridad contable al anular asientos ya procesados por `WISE.APROBAR_ASIENTOS.GEN_COMPRO` | Usar siempre contra-asiento (opción B), nunca borrado físico. |
| Stock negativo por anulación de RECEPCION cuya mercadería ya se consumió | Validación previa: bloquear y exigir revertir consumos primero. |
| Diferencia grande entre costo original y actual que infla `annulmentAdjustmentAccount` | Documentar y mostrar al usuario el delta en el modal de confirmación. |
| Anulación en período cerrado rompe cuadres contables | Advertir visualmente; mantener fecha del asiento reverso en fecha actual (no en el mes original). |
| Concurrent modification entre usuarios | Lock optimista en `WarehouseVoucher` y `PurchaseOrder` vía `@Version` (revisar si ya lo tienen). |

---

## Abierto / a definir con el usuario

- Cierre mensual: permitir anular en mes cerrado ya está decidido (permitir). El asiento reverso se fecha en la fecha actual, no en la del asiento original.
- Flujo exacto de anulación de cheque (a investigar en Fase 3 / Tarea 3.2).
- Caso particular de ventas al contado: el usuario mencionó que "en ventas al contado no siempre" se revierte igual — revisar en Fase 3 si hay casuística CONTADO que deba diferenciarse.
- Valorización a costo actual (con cuenta de ajuste): se deja **pendiente** hasta nueva decisión; por ahora el plan va con reverso simétrico.
