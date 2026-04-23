# Órdenes de Compra y Vales de Almacén — Flujo completo

Documento de referencia para entender cómo se registran, aprueban, finalizan y liquidan las **Órdenes de Compra (OC)** y cómo se crean, aprueban y contabilizan los **Vales de Almacén** (RECEPCION, EGRESO, TRANSFERENCIA, etc.). Incluye el detalle de los cálculos de inventario por **Costo Promedio** y la generación de asientos contables en `sf_tmpenc` / `sf_tmpdet`.

Este documento es la base para implementar la funcionalidad de **anular / revertir** OCs y Vales ya aprobados/finalizados.

---

## 1. Tablas involucradas y su mapeo JPA

| Tabla (DB) | Entidad JPA | Propósito |
|---|---|---|
| `com_encoc` | [PurchaseOrder](../src/main/com/encens/khipus/model/purchases/PurchaseOrder.java) | Encabezado de OC |
| `com_detoc` | [PurchaseOrderDetail](../src/main/com/encens/khipus/model/purchases/PurchaseOrderDetail.java) | Detalle de OC (línea por producto) |
| `inv_vales` | [WarehouseVoucher](../src/main/com/encens/khipus/model/warehouse/WarehouseVoucher.java) | Encabezado del vale |
| `inv_mov` | [InventoryMovement](../src/main/com/encens/khipus/model/warehouse/InventoryMovement.java) | Movimiento de inventario (hay uno pendiente + uno aprobado por vale) |
| `inv_movdet` | [MovementDetail](../src/main/com/encens/khipus/model/warehouse/MovementDetail.java) | Línea de movimiento (por producto) |
| `inv_articulos` | [ProductItem](../src/main/com/encens/khipus/model/warehouse/ProductItem.java) | Catálogo de artículos (mantiene costo promedio global) |
| `inv_inventario` | [Inventory](../src/main/com/encens/khipus/model/warehouse/Inventory.java) | Stock por (producto, almacén) |
| `inv_inventario_detalle` | [InventoryDetail](../src/main/com/encens/khipus/model/warehouse/InventoryDetail.java) | Stock por (producto, almacén, U.Ejecutora, C.Costo) |
| `sf_tmpenc` | [Voucher](../src/main/com/encens/khipus/model/finances/Voucher.java) | Asiento contable (cabecera) |
| `sf_tmpdet` | [VoucherDetail](../src/main/com/encens/khipus/model/finances/VoucherDetail.java) | Línea del asiento (debe/haber por cuenta) |
| `inv_invmes` | [InventoryHistory](../src/main/com/encens/khipus/model/warehouse/InventoryHistory.java) | Historial mensual (entradas/salidas, montos) |
| `inventario_detalle_log` | [InventoryDetailLog](../src/main/com/encens/khipus/model/warehouse/InventoryDetailLog.java) | Bitácora de transferencias entre centros de costo |

### Catálogos secundarios relevantes
- `WarehouseDocumentType` (`inv_tipovales`) — tipo de vale: RECEPCION (R), EGRESO/SALIDA (S), TRANSFERENCIA (T), DEVOLUCION (D), CONSUMO (C), BAJA (B), REPROCESO (W), etc. Cada tipo puede traer una cuenta contra-parte por defecto.
- `Warehouse` — almacén; mantiene `cashAccount` (cuenta contable del almacén).
- `ProductItem` — tiene `cashAccount` (`inv_articulos.cta_gasto` o similar), `subGroup.group.costCashAccount` y `subGroup.group.lowCashAccount` (cuentas para costo/gasto y bajas).
- `CompanyConfiguration` — cuentas globales: `warehouseNationalCurrencyTransientAccount`, `lowAccount`, `reworkAccount`, `balanceExchangeRateAccount`, `advancePaymentNationalCurrencyAccount`, VAT.
- `Provider.payableAccount` — cuenta de cuenta por pagar del proveedor.

---

## 2. Enums de estado

### [PurchaseOrderState](../src/main/com/encens/khipus/model/purchases/PurchaseOrderState.java)

| Código | Significado | Descripción |
|---|---|---|
| `PEN` | Pendiente | Registrada, aún no aprobada |
| `APR` | Aprobada | Aprobada, lista para finalizar (recepcionar) |
| `FIN` | Finalizada | Mercadería recibida, inventario y asiento generados |
| `LIQ` | Liquidada | OC totalmente pagada |
| `ANL` | Anulada | Cancelada (sólo permitido desde PEN o APR — **actualmente no revierte inventario ni asientos**) |

`getInactiveStates()` = `{PEN, ANL}`. Los estados activos para operaciones son `APR`, `FIN`, `LIQ`.

### [WarehouseVoucherState](../src/main/com/encens/khipus/model/warehouse/WarehouseVoucherState.java)

| Código | Significado | Descripción |
|---|---|---|
| `PEN` | Pendiente | Creado, aún no aprobado (no afecta inventario) |
| `APR` | Aprobado | Aprobado, inventario y asiento generados |
| `PAR` | Parcial | Recepción parcial (uso limitado) |
| `ANL` | Anulado | Definido en el enum pero **nunca se setea en el código actual** |

`getValidStates()` = `{APR, PAR}`.

### [MovementDetailType](../src/main/com/encens/khipus/model/warehouse/MovementDetailType.java)

| Código | Significado |
|---|---|
| `E` | Entrada (incrementa inventario) |
| `S` | Salida (decrementa inventario) |

### [PurchaseOrderReceivedType](../src/main/com/encens/khipus/model/purchases/PurchaseOrderReceivedType.java)

| Código | Significado |
|---|---|
| `RP` | Recepción parcial |
| `RT` | Recepción total (se fija siempre al finalizar — ver sección 4) |

### [VoucherState](../src/main/com/encens/khipus/model/finances/VoucherState.java) (asiento contable)

| Código | Significado |
|---|---|
| `PEN` | Pendiente de aprobación contable |
| `APR` | Aprobado (pasa a asiento definitivo vía procedure) |
| `ANL` | Anulado (se excluye en queries con `state <> 'ANL'`) |

### [VoucherOperation](../src/main/com/encens/khipus/model/warehouse/VoucherOperation.java)

Marca el origen del vale cuando viene de otro módulo:
- `OP` Orden de Producción, `OC` Orden de Compra, `BA` Baja de producto, `BV` Salida por venta, `DE` Devolución, `TP` Transferencia de producto.
- Si `warehouseVoucher.getOperation() != null` → el asiento contable se crea en el flujo del módulo origen y NO se genera el asiento genérico en la aprobación del vale (ver [ApprovalWarehouseVoucherServiceBean.java:224](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L224)).

---

## 3. Flujo 1: Orden de Compra (OC)

### UI de entrada
- Lista: [view/warehouse/warehousePurchaseOrderList.xhtml](../view/warehouse/warehousePurchaseOrderList.xhtml)
- Detalle/crear: `view/warehouse/warehousePurchaseOrder.xhtml`

### 3.1 Ciclo de vida (estados y transiciones)

```
(nueva) ──► PEN ──approve──► APR ──finalize──► FIN ──liquidate──► LIQ
             │                │
             └─nullify─────── └─nullify──► ANL (sólo desde PEN/APR)
```

| Transición | Action (UI) | Service | Efecto físico |
|---|---|---|---|
| → PEN | `WarehousePurchaseOrderAction.create` | `WarehousePurchaseOrderServiceBean.create*PurchaseOrder` | Inserta `com_encoc` + `com_detoc` |
| PEN → APR | [WarehousePurchaseOrderAction.approveWarehousePurchaseOrder():378](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java#L378) | [WarehousePurchaseOrderServiceBean.approveWarehousePurchaseOrder():330](../src/main/com/encens/khipus/service/warehouse/WarehousePurchaseOrderServiceBean.java#L330) | Sólo cambia estado |
| APR → FIN | [WarehousePurchaseOrderAction.finalizeWarehousePurchaseOrder():436](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java#L436) | [WarehousePurchaseOrderServiceBean.finalizePurchaseOrder():398](../src/main/com/encens/khipus/service/warehouse/WarehousePurchaseOrderServiceBean.java#L398) | Crea vale RECEPCION, aprueba vale, actualiza inventario y genera asiento contable |
| FIN → LIQ | [WarehousePurchaseOrderAction.liquidateWarehousePurchaseOrder():480](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java#L480) | `WarehousePurchaseOrderServiceBean.onlyLiquidatePurchaseOrder(...)` / `liquidatePurchaseOrder(...)` | Genera asiento contable de liquidación (pago al proveedor) |
| PEN/APR → ANL | [WarehousePurchaseOrderAction.nullifyWarehousePurchaseOrder():347](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java#L347) | `service.nullifyPurchaseOrder()` + `service.nullifyInvoicesPurchaseOrder()` | Sólo cambia estado — no revierte nada (porque desde APR aún no se afectó inventario). **No permitido desde FIN/LIQ**. |

### 3.2 Paso a paso — al pulsar "Finalizar"

La acción [finalizeWarehousePurchaseOrder()](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java#L436) orquesta:

```java
// WarehousePurchaseOrderAction.java:448-456
service.finalizePurchaseOrder(getInstance());        // (A) crea el vale PEN
select(getInstance());
addPurchaseOrderFinalizedMessage();

/** Aprueba el vale despues de finalizar la O.C. **/
WarehouseVoucher warehouseVoucher =
    warehouseVoucherService.findWarehouseVoucherByPurchaseOrder(getInstance());
warehouseVoucherUpdateAction.putWarehouseVoucher(warehouseVoucher.getId());
warehouseVoucherUpdateAction.approve();              // (B) aprueba el vale y genera asiento
```

#### (A) `WarehousePurchaseOrderServiceBean.finalizePurchaseOrder` ([línea 398](../src/main/com/encens/khipus/service/warehouse/WarehousePurchaseOrderServiceBean.java#L398))
```java
// 1) Validación: no finalizada ya
if (isPurchaseOrderFinalized(entity)) throw new PurchaseOrderFinalizedException(...);

// 2) Busca el tipo de vale RECEPCION (Warehouse Document Type con tipo R)
WarehouseDocumentType warehouseDocumentType = getFirstReceptionType();

// 3) Cambia estado y tipo de recepción (siempre total)
entity.setState(PurchaseOrderState.FIN);
entity.setReceivedType(PurchaseOrderReceivedType.RT);
getEntityManager().merge(entity); getEntityManager().flush();

// 4) Crea el vale PEN con sus MovementDetail a partir del detalle de la OC
createWarehouseVoucher(entity, purchaseOrderDetails, responsible, warehouseDocumentType);
```

#### `createWarehouseVoucher` ([línea 822](../src/main/com/encens/khipus/service/warehouse/WarehousePurchaseOrderServiceBean.java#L822))

Campos copiados de la OC a `inv_vales`:
- `documentType` = tipo RECEPCION (R)
- `warehouse` = almacén de la OC
- `date` = fecha del mes-proceso actual (`monthProcessService.getMothProcessDate(new Date())`)
- `state` = `PEN`
- `purchaseOrder` = referencia a la OC (foreign key — clave para saber que el vale nace de una OC)
- `executorUnit`, `costCenterCode`, `responsible` (responsable del almacén).

Para cada `PurchaseOrderDetail` se construye un `MovementDetail` (tipo `E`):
```java
// WarehousePurchaseOrderServiceBean.java:871-898
movementDetailTemp.setProductItem(productItem);
movementDetailTemp.setProductItemAccount(productItem.getProductItemAccount());
movementDetailTemp.setQuantity(requestedQuantity);      // convertido a unidad de uso si aplica

if (CollectionDocumentType.INVOICE.equals(entity.getDocumentType())) {
    // Con factura: descuenta IVA (monto_sin_IVA = total * 0.87 aprox. via VAT_COMPLEMENT)
    BigDecimal discountValue = BigDecimalUtil.multiply(purchaseOrderDetail.getTotalAmount(), percentVal);
    BigDecimal totalAmount   = BigDecimalUtil.subtract(purchaseOrderDetail.getTotalAmount(), discountValue);
    movementDetailTemp.setAmount(BigDecimalUtil.multiply(totalAmount, Constants.VAT_COMPLEMENT, 6));
} else {
    movementDetailTemp.setAmount(purchaseOrderDetail.getTotalAmount());
}

movementDetailTemp.setUnitCost(BigDecimalUtil.divide(amount, requestedQuantity, 6));
movementDetailTemp.setUnitPurchasePrice(purchaseOrderDetail.getUnitCost());
movementDetailTemp.setPurchasePrice(qty * purchaseOrderDetail.getUnitCost());
movementDetailTemp.setMovementType(MovementDetailType.E);
```

#### (B) `WarehouseVoucherUpdateAction.approve()` ([línea 166](../src/main/com/encens/khipus/action/warehouse/WarehouseVoucherUpdateAction.java#L166))

Cuando el vale **tiene OC** (`hasPurchaseOrder()`) y forma de pago es CONTADO o CRÉDITO:

```java
// WarehouseVoucherUpdateAction.java:174-190
if (warehouseVoucher.hasPurchaseOrder() && (CASH || CREDIT)) {
    // 1) Genera el ASIENTO CONTABLE sf_tmpenc / sf_tmpdet
    voucher = warehouseAccountEntryService.createEntryAccountForPurchaseOrder(warehouseVoucher);
    voucherAccoutingService.updatePurchaseDocumentIfExist(voucher);

    // 2) Aprueba el vale y actualiza inventario (costo promedio)
    approvalWarehouseVoucherService.approveWarehouseVoucherForPurchaseOrder(
        warehouseVoucher.getId(), getGlossMessage(), ...);

    // 3) Actualiza la OC con el voucher generado
    warehousePurchaseOrderService.updatePurchaseOrder(voucher, warehouseVoucher.getPurchaseOrder());

    // 4) Si es CONTADO, liquida inmediatamente (FIN → LIQ)
    if (CASH) warehousePurchaseOrderService.liquidateCashPurchaseOrder(...);
}
```

> **Importante:** el `approveWarehouseVoucherForPurchaseOrder` de [línea 237](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L237) **no crea asiento contable** (lo hace el paso 1 de arriba). El path genérico `approveWarehouseVoucher()` sí lo crearía, pero está protegido por `if (!warehouseVoucher.hasPurchaseOrder())` en [línea 225](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L225).

### 3.3 Asiento contable por finalización de OC — `createEntryAccountForPurchaseOrder`

[WarehouseAccountEntryServiceBean.java:509](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L509)

Estructura del asiento (cabecera + detalles):

**DEBE** (una línea por cada `MovementDetail` del vale):
- Cuenta: `detail.getProductItem().getWarehouse().getCashAccount()` — cuenta contable del almacén del producto.
- Monto: `detail.getAmount()` (ya sin IVA si factura).

**DEBE** (si documento = FACTURA y `withBill`): una línea adicional por cada `PurchaseDocument` aprobado:
- Cuenta: `companyConfiguration.getNationalCurrencyVATFiscalCreditAccount()` (Crédito Fiscal IVA)
- Monto: `purchaseDocument.getIva()`

**HABER**:
- Si CRÉDITO → cuenta por pagar del proveedor: `purchaseOrder.getProvider().getPayableAccount()` por `totalAmount`.
- Si CONTADO → cuenta de caja/banco configurada (`defaultAccount` o seleccionada en el pago).

Atributos adicionales del `Voucher`:
- `voucher.form` = `Constants.WAREHOUSE_VOUCHER_FORM`
- `voucher.documentType` = `Constants.IA_VOUCHER_DOCTYPE` (Ingreso Almacén)
- `voucher.userNumber` = usuario contable por defecto
- `voucher.providerCode` = proveedor de la OC
- `voucher.state` = `PEN` (se aprueba después vía `WISE.APROBAR_ASIENTOS.GEN_COMPRO` — stored procedure Oracle)
- `voucher.gloss` = generado por `glossGeneratorService.generatePurchaseOrderGloss(...)`

El `Voucher` se **enlaza** al `WarehouseVoucher` vía `warehouseVoucher.setVoucher(voucher)` → columna en `inv_vales`. Esto es la referencia clave para anular después.

### 3.4 Paso a paso — al pulsar "Liquidar"

Solo aplica para OCs al CRÉDITO que aún no están pagadas (las de CONTADO se liquidan automáticamente al finalizar).

[liquidateWarehousePurchaseOrder():480](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java#L480) invoca uno de:
- `service.onlyLiquidatePurchaseOrder(...)` — liquidación simple con `liquidationPayment`.
- `service.liquidatePurchaseOrderWithCashBox(...)` — liquidación desde caja.

Todas terminan llamando al método común [liquidatePurchaseOrder():430](../src/main/com/encens/khipus/service/warehouse/WarehousePurchaseOrderServiceBean.java#L430):
```java
Voucher voucher = warehouseAccountEntryService.createEntryAccountForValidatePurchaseOrder(
    purchaseOrder, defaultExchangeRate);
purchaseOrder.setBalanceAmount(BigDecimal.ZERO);
purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);
```

Asiento de liquidación — [createEntryAccountForValidatePurchaseOrder():370](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L370):

**DEBE**:
- Cuenta: `companyConfiguration.warehouseNationalCurrencyTransientAccount` (transitoria de almacén)
- Monto: `totalAmount * VAT_COMPLEMENT` (sin IVA)
- Si factura: cuenta `nationalCurrencyVATFiscalCreditAccount` (si `withBill=S`) o `nationalCurrencyVATFiscalCreditTransientAccount` (si `withBill=N`), por el IVA.

**HABER**:
- Anticipos registrados (si hay): cuenta `advancePaymentNationalCurrencyAccount` por `sumAdvancePaymentAmount`.
- Pagos de liquidación: cuenta `provider.payableAccount` por `sumLiquidationPaymentAmount` (o `totalAmount` si no hay pago parcial).
- Diferencias de cambio (si aplica): `companyConfiguration.balanceExchangeRateAccount`.

### 3.5 Anular OC (`nullifyWarehousePurchaseOrder`)

Actualmente en [línea 347](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java#L347). Permitido desde **PEN y APR** solamente (lanza `PurchaseOrderFinalizedException` o `PurchaseOrderLiquidatedException` si ya pasó).

Efecto actual: sólo marca estado `ANL` en `com_encoc` y anula facturas asociadas (`nullifyInvoicesPurchaseOrder`). **No revierte inventario ni asientos** porque desde APR aún no se afectó.

---

## 4. Flujo 2: Vale de Almacén (independiente de OC)

### UI de entrada
- Lista: [view/warehouse/warehouseVoucherList.xhtml](../view/warehouse/warehouseVoucherList.xhtml)
- Crear: `view/warehouse/warehouseVoucherCreate.xhtml`

### 4.1 Ciclo de vida

```
(nuevo) ──► PEN ──approve──► APR
                              │
                              └─► (PAR en flujos de recepción parcial)
```

No existe en el código un camino `APR → ANL` (el valor ANL del enum nunca se asigna — ver sección 8).

### 4.2 Relación entre entidades

- **1 WarehouseVoucher** (`inv_vales`) tiene **N InventoryMovement** (`inv_mov`).
- Al crear un vale se inserta 1 `InventoryMovement` con estado `PEN`. Al aprobar se crea un segundo `InventoryMovement` con estado `APR` y se elimina el pendiente ([ApprovalWarehouseVoucherServiceBean.java:181](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L181) `delete(pendantInventoryMovement)`).
- **1 InventoryMovement** tiene **N MovementDetail** (`inv_movdet`) — uno por producto.
- Cada `MovementDetail` referencia `ProductItem` y `Warehouse`.
- Para vales de **transferencia** (tipo T): el mismo vale tiene `MovementDetail` tipo `S` (salida del origen) y tipo `E` (entrada al destino).

### 4.3 Paso a paso — al pulsar "Aprobar" vale independiente

La acción [WarehouseVoucherUpdateAction.approve():193-203](../src/main/com/encens/khipus/action/warehouse/WarehouseVoucherUpdateAction.java#L193) llama a:
```java
approvalWarehouseVoucherService.approveWarehouseVoucher(
    warehouseVoucher.getId(), getGlossMessage(),
    movementDetailUnderMinimalStockMap, ..., movementDetailWithoutWarnings);
```

Método [ApprovalWarehouseVoucherServiceBean.approveWarehouseVoucher():94](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L94):

```
1. Validaciones
   - Existe en DB (WarehouseVoucherNotFoundException)
   - Tiene detalles (WarehouseVoucherEmptyException)
   - No está aprobado (WarehouseVoucherApprovedException)

2. Carga WarehouseVoucher + InventoryMovement pendiente

3. Enrutamiento por tipo de vale:
   - isTransfer() || isExecutorUnitTransfer() → approveOutputDetails + approveInputDetails
   - tipo movimiento E (entrada) → validateInputDetails + approveInputDetails
   - tipo movimiento S (salida)  → validateOutputDetails + approveOutputDetails

4. updateWarehouseVoucher(warehouseVoucher):
   - state = APR
   - number = generateWarehouseVoucherNumber()   // número correlativo
   - merge + flush

5. createApprovedInventoryMovement() → nuevo inv_mov con estado APR
   (se conserva como bitácora; el PEN se borra al final)

6. approveInputDetails / approveOutputDetails:
   - Para cada MovementDetail pendiente:
     * detail.state = APR, detail.movementDetailDate = fecha
     * addAtInventory (entradas) o removeFromInventory (salidas)   ← AFECTA STOCK
     * inventoryHistoryService.updateInventoryHistory(detail)      ← acumula mensual (solo salidas; entradas comentadas)
     * updateProductItemInformationForInputs / ForOutputs         ← RECALCULA COSTO PROMEDIO
     * merge(detail)

7. Reemplazo de placeholder de glosa:
   gloss[i] = gloss[i].replaceAll(WAREHOUSEVOUCHER_NUMBER_PARAM, warehouseVoucher.getNumber())

8. Generación de asiento contable (condicionada):
   if (almacén MILK_COLLECTED y tipo RECEPCION)
       warehouseAccountEntryService.createAccountEntryFromCollection(...)
   else if (operation == null y documentCode != "RPT")
       if (!warehouseVoucher.hasPurchaseOrder())
           warehouseAccountEntryService.createAccountEntry(warehouseVoucher, gloss)

9. delete(pendantInventoryMovement)
```

### 4.4 Cuentas usadas por tipo de vale

Método selector [createAccountEntry():1335](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L1335). Resumen:

| Tipo | Método | DEBE | HABER |
|---|---|---|---|
| **RECEPCION** (R) | `createAccountEntryForReception` ([1608](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L1608)) | `ProductItem.warehouse.cashAccount` (por cada detalle) | `ProductItem.subGroup.group.costCashAccount` o `WarehouseVoucher.expenseCashAccount` |
| **TRANSFERENCIA U.Ejec.** | `createAccountEntryForExecutorUnitTransfer` ([1790](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L1790)) | 2 asientos (origen y destino) con `warehouseNationalCurrencyTransientAccount` | Cuentas de almacén |
| **TRANSFERENCIA entre almacenes** (tipo T) | — | **No se genera asiento** ([1342-1345](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L1342)) | — |
| **ENTRADA con contra-cuenta** | `createAccountEntryForInputsAndContraAccount` ([2036](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L2036)) | `Warehouse.cashAccount` | `WarehouseDocumentType.contraAccount` o `WarehouseVoucher.contraAccount` |
| **SALIDA con contra-cuenta** | `createAccountEntryForOutputsAndContraAccount` ([2073](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L2073)) | Contra-cuenta | `Warehouse.cashAccount` |
| **ENTRADA simple** | `createAccountEntryForInputs` ([1844](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L1844)) | `Warehouse.cashAccount` (total) | `ProductItem.cashAccount` (por detalle) |
| **SALIDA/EGRESO** | `createAccountEntryForOutputs` ([1899](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java#L1899)) | `ProductItem.cashAccount` (o cuenta especial según tipo) | `Warehouse.cashAccount` |

Casos especiales del DEBE en SALIDAS:
- Tipo `B` (Baja): `companyConfiguration.lowAccount`.
- `warehouseVoucher.lowFlag=true`: `ProductItem.subGroup.group.lowCashAccount`.
- Tipo `W` (Reproceso): `companyConfiguration.reworkAccount`.

En todos los casos el `Voucher` creado queda en estado `PEN`, se graba con `voucherAccoutingService.saveVoucher(voucher)` y se enlaza al vale con `warehouseVoucher.setVoucher(voucher)`.

---

## 5. Método de Costo Promedio (Costo Promedio Ponderado)

Implementado en [ApprovalWarehouseVoucherServiceBean.java:1085-1183](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L1085).

### 5.1 Campos clave

| Entidad | Campo | Columna | Escala | Qué almacena |
|---|---|---|---|---|
| `ProductItem` | `unitCost` | `costo_uni` | 6 | Costo unitario promedio **global** (no por almacén) |
| `ProductItem` | `cu` | `cu` | 6 | Costo unitario alterno (precio compra unitario acumulado) |
| `ProductItem` | `investmentAmount` | `saldo_mon` | 6 | Saldo monetario total (qty * unitCost aprox.) |
| `ProductItem` | `ct` | `ct` | 6 | Acumulador de costo total (precio compra total acumulado) |
| `Inventory` | `unitaryBalance` | `saldo_uni` | 2 | Cantidad en stock **por (producto, almacén)** |
| `InventoryDetail` | `quantity` | `cantidad` | 2 | Cantidad por (producto, almacén, U.Ejec., C.Costo) — **NO almacena costo** |
| `MovementDetail` | `quantity` | `cantidad` | 2 | Cantidad movida |
| `MovementDetail` | `unitCost` | `costounitario` | 6 | Costo unitario **al momento del movimiento** (snapshot) |
| `MovementDetail` | `amount` | `monto` | 6 | `quantity * unitCost` (para contabilidad) |
| `MovementDetail` | `purchasePrice` | `preciocompra` | 6 | Precio compra total |
| `MovementDetail` | `unitPurchasePrice` | `preciounitcompra` | 6 | Precio compra unitario |

> Importante: el **costo promedio se mantiene a nivel de `ProductItem` (global por empresa)**, mientras la **cantidad física se mantiene por almacén** en `Inventory`. Ver query `Inventory.sumUnitaryBalancesByArticleCode` usada como divisor.

### 5.2 Fórmula en ENTRADAS (recepción, entrada)

[updateProductItemInformationForInputs:1085-1141](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L1085):

```java
// sumUnitaryBalances = SUM(Inventory.unitaryBalance) por productItem en la empresa
BigDecimal sumUnitaryBalances = (BigDecimal) em.createNamedQuery(
    "Inventory.sumUnitaryBalancesByArticleCode")
    .setParameter("articleNumber", productItem.getId().getProductItemCode())
    .setParameter("companyNumber", movementDetail.getCompanyNumber())
    .getSingleResult();

if ((warehouseVoucher.isReception() || warehouseVoucher.isInput()) && productItem.getControlValued()) {
    BigDecimal newInvestmentAmount = BigDecimalUtil.sum(
        productItem.getInvestmentAmount(), movementDetail.getAmount(), 6);
    BigDecimal newCTAmount = BigDecimalUtil.sum(
        productItem.getCt(), movementDetail.getPurchasePrice(), 6);

    if (sumUnitaryBalances.doubleValue() > 0) {
        BigDecimal newUnitCost = BigDecimalUtil.divide(newInvestmentAmount, sumUnitaryBalances, 6);
        BigDecimal newCU       = BigDecimalUtil.divide(newCTAmount, sumUnitaryBalances, 6);

        productItem.setUnitCost(newUnitCost);        // ← COSTO PROMEDIO
        productItem.setCu(newCU);
        productItem.setInvestmentAmount(newInvestmentAmount);
        productItem.setCt(newCTAmount);
    } else {
        productItem.setInvestmentAmount(BigDecimal.ZERO);
        productItem.setCt(BigDecimal.ZERO);
    }
}
```

**Nota sutil**: la suma `sumUnitaryBalances` YA incluye la cantidad recién agregada al inventario (porque `addAtInventory` corre ANTES que `updateProductItemInformationForInputs` — ver secuencia en [approveInputDetails](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java) donde el orden es: set state, `addAtInventory`, `inventoryHistoryService.updateInventoryHistory`, `updateProductItemInformationForInputs`).

Por lo tanto la fórmula efectiva es:
```
newUnitCost = (investmentAmount_prev + amount_in) / (qty_total_post_entrada)
```
que equivale al costo promedio ponderado clásico.

### 5.3 Fórmula en SALIDAS (consumo, egreso)

[updateProductItemInformationForOutputs:1143-1183](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L1143):

**Caso `isConsumption()`** (tipo `C`): recalcula `investmentAmount` al costo actual y resta `purchasePrice`:
```java
BigDecimal newInvestmentAmount = BigDecimalUtil.multiply(
    sumUnitaryBalances, productItem.getUnitCost(), 6);
productItem.setInvestmentAmount(newInvestmentAmount);
productItem.setCt(BigDecimalUtil.subtract(productItem.getCt(), movementDetail.getPurchasePrice()));
```

**Caso `isOutput()`** (salida genérica, incluye BV, B, W):
```java
BigDecimal newInvestmentAmount = BigDecimalUtil.subtract(
    productItem.getInvestmentAmount(), movementDetail.getAmount(), 6);
if (BigDecimal.ZERO.compareTo(newInvestmentAmount) == 1)
    throw new ProductItemAmountException(...);  // no permite ir a negativo
productItem.setInvestmentAmount(newInvestmentAmount);
if (sumUnitaryBalances > 0 && newInvestmentAmount > 0) {
    productItem.setUnitCost(BigDecimalUtil.divide(newInvestmentAmount, sumUnitaryBalances, 6));
}
```

En salida, `unitCost` puede cambiar levemente si el `amount` del detalle no coincide exactamente con `qty_salida * unitCost_actual` (por redondeos). El costo se **preserva esencialmente** (no se revalúa como en FIFO/LIFO).

### 5.4 Actualización de stock físico

[addAtInventory:1230](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L1230) y [removeFromInventory:1185](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java#L1185):

- Actualiza `Inventory.unitaryBalance` (total por almacén).
- Actualiza `InventoryDetail.quantity` (por U.Ejecutora + C.Costo). En salida selecciona primero por (U.Ejecutora + C.Costo del vale); si no hay o es 0, hace fallback al **C.Costo Público** (`getInventoryDetailWithPublicCostCenter`). **No es FIFO ni hay selección manual por el usuario.**
- Si `Inventory` no existía para el par (producto, almacén), lo crea.
- Lanza `InventoryUnitaryBalanceException` si el resultado sería negativo.

### 5.5 Historial mensual (`inv_invmes`)

[InventoryHistoryServiceBean](../src/main/com/encens/khipus/service/warehouse/InventoryHistoryServiceBean.java) acumula por `(companyNumber, productItemCode, warehouseCode, año, mes)`:
- `incomingQuantity` / `incomingAmount` (entradas)
- `outgoingQuantity` / `outgoingAmount` (salidas)

Llamado en `approveOutputDetails` (entradas están comentadas en el código actual — posible inconsistencia).

---

## 6. Resumen — qué se registra al aprobar/finalizar

### 6.1 Finalizar OC (CRÉDITO o CONTADO)

| Tabla | Operación |
|---|---|
| `com_encoc` | `state=FIN`, `receivedType=RT` |
| `inv_vales` | INSERT (state=PEN → APR, tipo RECEPCION, link a OC) + `number` generado |
| `inv_mov` | 1 INSERT pendiente + 1 INSERT aprobado; DELETE pendiente al final |
| `inv_movdet` | 1 INSERT por cada detalle de OC (tipo E, con unitCost y amount) |
| `inv_inventario` | UPDATE `unitaryBalance += qty` (o INSERT si no existe) |
| `inv_inventario_detalle` | UPDATE o INSERT de slot por (U.Ejec., C.Costo) |
| `inv_articulos` | UPDATE `unitCost`, `cu`, `investmentAmount`, `ct` (costo promedio) |
| `sf_tmpenc` | 1 INSERT (state=PEN, doctype=IA, linkeado al vale) |
| `sf_tmpdet` | N INSERT: DEBE almacén por cada item + DEBE IVA (si factura) + HABER proveedor/caja |
| `com_encoc` | Si CONTADO: `state=LIQ`, `balanceAmount=0`, `paymentStatus=FULLY_PAID` |

### 6.2 Liquidar OC (sólo CRÉDITO post-finalizada)

| Tabla | Operación |
|---|---|
| `com_encoc` | `state=LIQ`, `balanceAmount=0`, `paymentStatus=FULLY_PAID` |
| `sf_tmpenc` | 1 INSERT adicional (asiento de liquidación) |
| `sf_tmpdet` | N INSERT: DEBE transitoria almacén + IVA (si factura) / HABER proveedor / anticipos / dif. cambio |
| Pagos | Actualización de `PurchaseOrderPayment` asociados |

### 6.3 Aprobar Vale independiente (RECEPCION / EGRESO / …)

| Tabla | Operación |
|---|---|
| `inv_vales` | `state=APR`, `number` generado |
| `inv_mov` | 1 INSERT aprobado; DELETE pendiente |
| `inv_movdet` | UPDATE `state=APR` en cada detalle |
| `inv_inventario` | UPDATE `unitaryBalance` |
| `inv_inventario_detalle` | UPDATE/INSERT slot |
| `inv_articulos` | UPDATE costo promedio (entrada) o baja saldos (salida) |
| `sf_tmpenc` | 1 INSERT (salvo tipos T, vales con `operation != null` o `RPT`) |
| `sf_tmpdet` | N INSERT (estructura según tabla de la sección 4.4) |
| `inv_invmes` | UPDATE acumulado mensual (sólo en salidas en el código actual) |

---

## 7. Relación OC ↔ Vale ↔ Asiento (para trazabilidad de anulaciones)

```
PurchaseOrder (com_encoc)
   │ purchaseOrder_id
   ▼
WarehouseVoucher (inv_vales) ── voucher_id ──► Voucher (sf_tmpenc) ── 1:N ─► VoucherDetail (sf_tmpdet)
   │ 1:N
   ▼
InventoryMovement (inv_mov) — 1:N → MovementDetail (inv_movdet)
                                         │
                                         ▼
                              Inventory (inv_inventario) + InventoryDetail (inv_inventario_detalle)
                              + ProductItem.unitCost/ct/investmentAmount
                              + InventoryHistory (inv_invmes)
```

Relaciones útiles para revertir:
- `WarehouseVoucher.purchaseOrder` (FK directo): `warehouseVoucherService.findWarehouseVoucherByPurchaseOrder(po)`.
- `WarehouseVoucher.voucher` (FK directo): navega al asiento generado.
- `MovementDetail.warehouseVoucher` vía `InventoryMovement`: lista de items a desafectar.
- Para OCs liquidadas hay un **segundo Voucher** (asiento de liquidación) referenciado desde `PurchaseOrder` / `PurchaseOrderPayment`.

---

## 8. Mecanismos existentes de anulación/reversión

Búsqueda exhaustiva en el módulo warehouse (`anular`, `reverse`, `cancel`, `undo`, `rollback`, `revert`, `nullify`):

| Mecanismo | Ubicación | Alcance |
|---|---|---|
| `PurchaseOrderState.ANL` + `nullifyWarehousePurchaseOrder` | [WarehousePurchaseOrderAction.java:347](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java#L347) | Sólo desde PEN/APR. Cambia estado y anula facturas. **No revierte inventario ni asientos** (no hay nada que revertir en esos estados). |
| `WarehouseVoucherState.ANL` | Definido en [enum](../src/main/com/encens/khipus/model/warehouse/WarehouseVoucherState.java#L14) | **Nunca se asigna en el código actual** — no existe flujo de anulación de vale. |
| `VoucherState.ANL` | [VoucherState.java](../src/main/com/encens/khipus/model/finances/VoucherState.java) | Se filtra en queries (`voucher.state <> 'ANL'`), pero no hay método que lo asigne sobre vouchers ya aprobados. |
| `VoucherServiceBean.deleteVoucher(Voucher)` | [VoucherServiceBean.java:104](../src/main/com/encens/khipus/service/finances/VoucherServiceBean.java#L104) | `em.remove(voucher)` directo; sin chequeo de estado. Usado para rollback en `catch` del `approve()`. Riesgoso si el voucher ya fue aprobado por el stored procedure. |
| `Voucher.approvedAllVoucherEntries` | `VoucherServiceBean` | Llama a `WISE.APROBAR_ASIENTOS.GEN_COMPRO()` (Oracle) para pasar PEN → APR. No hay un inverso. |
| `PartialWarehouseVoucherAction.cancel()` | [línea 264](../src/main/com/encens/khipus/action/warehouse/PartialWarehouseVoucherAction.java#L264) | Sólo descarta cambios en memoria (override trivial). No es anulación. |

**Conclusión**: el sistema **no cuenta hoy con reversión funcional para OC finalizadas/liquidadas ni para Vales aprobados**. Lo único existente es el cambio de estado `ANL` para OCs aún no finalizadas.

---

## 9. Consideraciones para implementar Anulación/Reversión

Para diseñar la nueva funcionalidad, se deben revertir exactamente los efectos listados en la sección 6. Puntos críticos:

### 9.1 Orden de reversión (inverso al de aplicación)

1. Marcar asiento contable generado como `ANL` (o crear asiento contrario) — ver sección 9.3.
2. Revertir stock:
   - En `inv_inventario`: restar (si entrada) o sumar (si salida) `quantity`.
   - En `inv_inventario_detalle`: mismo ajuste sobre el slot correspondiente.
3. **Recalcular costo promedio** en `inv_articulos`:
   - Restar `movementDetail.amount` del `investmentAmount` (entrada) o sumarlo (salida).
   - Restar `movementDetail.purchasePrice` del `ct` (entrada) o sumarlo (salida).
   - Dividir por el **nuevo `sumUnitaryBalances`** para obtener el nuevo `unitCost`.
   - Manejar división por cero: si queda stock total = 0 → setear `investmentAmount=0`, `ct=0` (mantener último `unitCost`).
4. Revertir `inv_invmes` (restar al acumulado del mes).
5. Setear `inv_mov` y `inv_movdet` a estado `ANL` (o borrar físicamente — preferible marcar para auditoría).
6. Setear `inv_vales.state = ANL`.
7. Si viene de OC: setear `com_encoc.state` al estado previo (`APR`) y `receivedType = null`.

### 9.2 Riesgo del Costo Promedio

El **costo promedio NO es reversible matemáticamente** en un caso general:

> Si después de la OC se hicieron más recepciones/egresos, revertir sólo esa OC no restaura el `unitCost` original.

Alternativas de diseño:
- **A. Reversión contable pero congelando el costo actual**: dejar `unitCost` como está; sólo ajustar cantidades e `investmentAmount` restando `amount` del movimiento. El costo quedará "desviado" pero consistente con el stock monetario restante.
- **B. Bloquear anulación si hay movimientos posteriores sobre el mismo producto** — más seguro pero muy restrictivo.
- **C. Generar movimiento contrario (ajuste)** en vez de anular — preferido en sistemas ERP porque preserva trazabilidad y deja el costo promedio evolucionando naturalmente (se valora al costo actual, no al costo original).

Se recomienda **opción C** (crear un vale/asiento contrario) salvo que el usuario requiera explícitamente "deshacer" literal.

### 9.3 Reversión del asiento contable

Dos estrategias:
- **Anular lógicamente** (`Voucher.state = ANL`): sencillo, queda en `sf_tmpenc` pero excluido de balances. Riesgoso si el stored procedure `WISE.APROBAR_ASIENTOS.GEN_COMPRO()` ya lo pasó a tablas definitivas.
- **Generar asiento contrario** (contra-asiento): nuevo `Voucher` con DEBE/HABER invertidos y `relatedTransactionNumber` apuntando al original. Opción más limpia contablemente y compatible con asientos ya procesados.

Reglas a mantener:
- Glosa: prefijo o sufijo indicando anulación, conservando el `WAREHOUSEVOUCHER_NUMBER_PARAM` reemplazado.
- Tipo documento: usar contrapartida (`IA` → `SA` y viceversa) o un tipo específico de ajuste si existe.
- `userNumber` = usuario contable por defecto.
- Moneda y tipo de cambio: replicar del asiento original.

### 9.4 Casos especiales

- **OC al CONTADO ya LIQuidada**: revertir también la liquidación (ver asiento de `createEntryAccountForValidatePurchaseOrder` — incluye anticipos y tipo de cambio). Si hubo movimientos de caja o cheques emitidos, coordinar con esos módulos.
- **OC con CRÉDITO + facturas con IVA (`withBill=S`)**: el asiento incluye la cuenta de Crédito Fiscal IVA; la reversión debe incluir la línea de IVA.
- **Vales con `hasOperation()`**: el asiento se crea en el módulo origen (OP, OC, BA, BV, DE, TP). Coordinar reversión con ese módulo.
- **Vales del almacén `COD_WAREHUOSE_MILK_COLLECTED` con tipo RECEPCION**: usan `createAccountEntryFromCollection` — revisar ese flujo por separado.
- **Vales de transferencia entre almacenes** (tipo T): no generan asiento contable, sólo revertir inventario en ambos almacenes.
- **Vales de transferencia entre Unidades Ejecutoras**: generan 2 asientos (origen y destino). Revertir ambos.
- **Período cerrado**: si el mes de proceso está cerrado (`MonthProcessService`), la anulación puede requerir bloqueo o reapertura por administrador.

### 9.5 Permisos y auditoría

- Definir permiso nuevo (p. ej. `WAREHOUSEPURCHASEORDERREVERSE`, `WAREHOUSEVOUCHERREVERSE`).
- Dejar traza en un campo `nullifiedDate` / `nullifiedBy` / `nullifyReason` en las entidades (requiere agregar columnas).
- No borrar registros físicamente — marcar `ANL` y conservar datos.

---

## 10. Puntos de entrada recomendados al implementar

| Funcionalidad | Action propuesta | Service propuesta |
|---|---|---|
| Anular OC (cualquier estado ≥ FIN) | Nuevo método en `WarehousePurchaseOrderAction` (p.ej. `reversePurchaseOrder()`) | Nuevo método en `WarehousePurchaseOrderServiceBean` que orqueste reversión de vale + asiento |
| Anular Vale (independiente) | Nuevo método en `WarehouseVoucherUpdateAction` (p.ej. `annul()`) | Nuevo servicio (p.ej. `ReverseWarehouseVoucherServiceBean`) que reutilice `addAtInventory`/`removeFromInventory` invertidos + recalcule costo promedio |
| Generar contra-asiento | Nuevo método en `WarehouseAccountEntryServiceBean` (p.ej. `createReverseAccountEntry(warehouseVoucher, originalVoucher)`) | — |

Al construir estos métodos, **reusar las mismas reglas de cuentas** documentadas en la sección 4.4 para que el asiento reverso use las mismas cuentas pero con signo invertido.

---

## 11. Referencias cruzadas rápidas

- Acción OC: [WarehousePurchaseOrderAction.java](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderAction.java)
- Servicio OC: [WarehousePurchaseOrderServiceBean.java](../src/main/com/encens/khipus/service/warehouse/WarehousePurchaseOrderServiceBean.java)
- Acción Vale: [WarehouseVoucherUpdateAction.java](../src/main/com/encens/khipus/action/warehouse/WarehouseVoucherUpdateAction.java) / [WarehouseVoucherCreateAction.java](../src/main/com/encens/khipus/action/warehouse/WarehouseVoucherCreateAction.java)
- Servicio aprobación vale: [ApprovalWarehouseVoucherServiceBean.java](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java)
- Servicio asiento contable: [WarehouseAccountEntryServiceBean.java](../src/main/com/encens/khipus/service/warehouse/WarehouseAccountEntryServiceBean.java)
- Servicio voucher genérico: [VoucherServiceBean.java](../src/main/com/encens/khipus/service/finances/VoucherServiceBean.java)
- Servicio contabilidad: [VoucherAccoutingServiceBean.java](../src/main/com/encens/khipus/service/accouting/VoucherAccoutingServiceBean.java)
- Constantes de cuentas/config: `CompanyConfiguration`, `Constants.VAT`, `Constants.VAT_COMPLEMENT`, `Constants.WAREHOUSE_VOUCHER_FORM`, `Constants.IA_VOUCHER_DOCTYPE`, `Constants.SA_VOUCHER_DOCTYPE`, `Constants.WAREHOUSEVOUCHER_NUMBER_PARAM`, `Constants.COD_WAREHUOSE_MILK_COLLECTED`.
