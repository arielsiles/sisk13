# Órdenes de Venta (módulo Clientes / Comercial) — Plan de implementación

Documento de referencia para la feature **Órdenes de Venta**. El formato del
documento impreso replica la Orden (Order Nº) del proveedor TERDEMOL S.R.L.

## Decisiones de diseño (aprobadas)

| Tema | Decisión |
|---|---|
| Datos del comprador | **Snapshot** (copia) en la orden + FK al `Client`. Se agrega campo `fax` al `Client`. |
| Centro de costo | Catálogo **nuevo e independiente** comercial (`CommercialCostCenter`), por línea, con CRUD. |
| Incoterm | Catálogo CRUD sembrado con los **11 Incoterms 2020** + campo "lugar designado" en la orden. |
| Estados | `BORRADOR → REVISIÓN → APROBADO → ANULADA`. En REVISIÓN Gerencia agrega observaciones. |
| Numeración | Correlativo por **compañía**, 6 dígitos (`000099`), asignado al crear el borrador. |
| Totales | Subtotal + Descuento + Recargo + Total. |
| Ítems | Desde **Productos Terminados** (`ProductItem` filtrado por almacén `FINISHED_GOODS`). |
| Moneda | `FinancesCurrencyType` (P=Bs, D=$us), seleccionable al crear. |
| Ubicación | Paquetes/vistas nuevos `sales/`. Esquema BD `khipus`. |

Patrón base: espejo de **Orden de Compra** (`PurchaseOrder`/`PurchaseOrderDetail`
+ `GenericAction` + `SequenceGeneratorService` + `GenericReportAction`/JasperReports).

## Ciclo de vida y permisos

| Transición | Permiso | Regla |
|---|---|---|
| Crear / editar | `SALESORDER_CREATE` / `SALESORDER_UPDATE` (comercial) | Solo en **BORRADOR** |
| Enviar a revisión | `SALESORDER_SEND` (comercial) | BOR → REV (bloquea edición) |
| Aprobar | `SALESORDER_APPROVE` (gerencia) | REV → APR |
| Rechazar (+observaciones) | `SALESORDER_APPROVE` (gerencia) | REV → BOR |
| Anular | `SALESORDER_NULLIFY` | cualquier estado → ANL (no borra) |
| Imprimir PDF | `SALESORDER_VIEW` | cualquier estado |

En **REVISIÓN**, Gerencia puede escribir `managementObservations`. Al rechazar,
la orden vuelve a BORRADOR conservando las observaciones para que comercial corrija.

## Modelo de datos

### `SalesOrder` (cabecera) — `khipus.ordenventa`
- `id` PK (`@TableGenerator` sobre `secuencia`, pkValue `ordenventa`)
- `orderNumber` correlativo 6 díg. por compañía (negocio, no PK)
- `state` enum `SalesOrderState` (BOR/REV/APR/ANL)
- `date` fecha creación (auto), `deliveryDate` fecha de entrega
- `client` FK `Client`
- Snapshot comprador: `buyerName, buyerRegNumber, buyerAddress, buyerPhone, buyerFax, buyerEmail`
- `currency` `FinancesCurrencyType`
- `incoterm` FK `Incoterm`, `incotermPlace` texto
- `quotation, scCode, ccCode` textos (COTIZACIÓN, S/C, C/C)
- `comments` texto multilínea (COMMENTS)
- `managementObservations` observaciones de Gerencia (REVISIÓN)
- Totales: `subTotalAmount, discountAmount, rechargeAmount, totalAmount`
- Auditoría: `createdBy/At, sentAt, approvedBy/At, nullifiedAt`, `@Version`, unidad de negocio + `CompanyNumberListener`
- `detailList` `@OneToMany(cascade=ALL, @OrderBy detailNumber)`
- `selectedNotes` notas seleccionadas (NOTAS)

### `SalesOrderDetail` (línea) — `khipus.ordenventadetalle`
`id`, `detailNumber` (ITEM #), `salesOrder` FK, `productItem` FK + `productItemCode`,
`costCenter` FK `CommercialCostCenter`, `description` (snapshot editable), `quantity`,
`measureUnit` (del producto, editable), `unitPrice`, `totalAmount`, `@Version`.

### `SalesOrderState` (enum)
`BOR, REV, APR, ANL` con `resourceKey`.

### Catálogos (cada uno con CRUD List+Edit)
- **`Incoterm`** (`khipus.incoterm`): `code, name, description, active`. Sembrado 2020: EXW, FCA, CPT, CIP, DAP, DPU, DDP, FAS, FOB, CFR, CIF.
- **`CommercialCostCenter`** (`khipus.centrocosto_comercial`): `code, description, active`, company-scoped.
- **`SalesOrderNote`** (`khipus.ordenventa_nota`): `text` (multilínea), `orden`, `active` — notas predefinidas del recuadro final.

## Numeración
`SalesOrderNumberGeneratorServiceBean` usando `SequenceGeneratorService.nextValue("ordenventa_"+compañía)` → formato `%06d`.

## Acciones / Servicios (Seam)
- `SalesOrderAction extends GenericAction<SalesOrder>`: `select/create/update`; copia snapshot al elegir cliente; recálculo de totales; transiciones `sendToReview()`, `approve()`, `reject()`, `nullify()`.
- `SalesOrderDetailListCreateAction` (conversation): add/remove líneas en memoria; total línea = `cantidad × precio`; subtotal.
- `SalesOrderDataModel extends QueryDataModel<Long,SalesOrder>`: lista + filtros (Nº, cliente, estado, rango fechas).
- Selección de productos: reuso de `ProductItemsForSalesDataModel` (FINISHED_GOODS) en modal `selectPopUp`.
- `IncotermAction/DataModel/Service`, `CommercialCostCenterAction/…`, `SalesOrderNoteAction/…`.
- `SalesOrderServiceBean`: `create` (Nº/fecha/estado BOR, snapshot, líneas, totales), `update`, `updateTotalAmountFields`, transiciones validadas.

## Vistas (`view/sales/`)
- `salesOrderList.xhtml` — lista + filtros + badges de estado + botones por permiso.
- `salesOrder.xhtml` + `salesOrderForm.xhtml` — cabecera: cliente (`selectPopUp` autocompleta snapshot), fecha entrega, moneda, incoterm + lugar, cotización/SC/CC, comments, notas (multi-select), totales (descuento/recargo), sección observaciones Gerencia (render en REVISIÓN).
- `salesOrderDetailListCreateForm.xhtml` — grilla de líneas con add-modal y remove.
- Pares CRUD: `incotermList/incoterm`, `commercialCostCenterList/commercialCostCenter`, `salesOrderNoteList/salesOrderNote`.
- Todo el texto vía `messages_app.properties` (sin literales en xhtml).

## Reporte PDF (formato de la imagen)
- `view/sales/reports/salesOrderReport.jrxml` (cabecera: logo, `ORDER Nº`, recuadro comprador, fecha/registro/dirección/tel/fax/email, condiciones de pago, fecha entrega, INCOTERM) + subreporte `salesOrderDetailSubReport.jrxml` (tabla ITEM/CODE/COST CENTER/DESCRIPTION/QTY/MEASURE/UNIT PRICE/TOTAL), COMMENTS + Incoterm derivado, totales, COTIZACIÓN/S-C/C-C, y NOTAS al pie.
- `SalesOrderReportAction extends GenericReportAction`.

## Navegación / menú / build
- `resources/WEB-INF/sales/pages.xml` (pares list↔edit espejo del bloque `client`).
- Entrada en el menú Clientes/Comercial.
- Permisos nuevos sembrados en `funcionalidad` (SALESORDER*, INCOTERM, COMMERCIALCOSTCENTER, SALESORDERNOTE).
- Compilar con **JDK 1.8** (`ant explode`).

## Fases
1. **Catálogos + base**: `Incoterm` (sembrado), `CommercialCostCenter`, `SalesOrderNote` con CRUD; campo `fax` en `Client`; permisos.
2. **Orden (núcleo)**: entidades + secuencia + servicio + acción + editor de líneas + list/edit + snapshot + totales (crear/editar en BORRADOR).
3. **Workflow**: estados, observaciones REVISIÓN, aprobar/rechazar/anular, bloqueo y permisos.
4. **Reporte PDF** con el formato exacto de la imagen.
5. **Integración**: menú, i18n, pruebas.
