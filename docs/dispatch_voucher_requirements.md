# Requerimientos — Vale de Egreso por Despacho de Productos Terminados

**Identificador:** DESPACHO-PT
**Módulo:** Warehouse (Almacenes)
**Origen funcional:** Documento "CERTIFICADO DE RECEPCION DE CARGUIO / ORDEN DE ENTREGA" — TERDEMOL S.R.L., Terminal de Molienda.
**Rama de desarrollo:** `dev_dispatch_voucher` (a partir de `dev_terdemol`).

---

## 1. Objetivo

Permitir el registro y aprobación de **despachos** de productos terminados (resultado del módulo XProduction) desde el Almacén de Productos Terminados hacia clientes del sistema, manteniendo control de:

- Datos del cliente, transportista y conductor.
- Datos del carguío (pesos balanza, bolsas, horario, lote, turno).
- Lugares de origen y destino del envío.
- Trazabilidad respecto al inventario (descuento atómico al aprobar).
- Emisión del certificado físico de recepción de carguío.

La aprobación de un despacho debe **descontar el stock** del almacén seleccionado, generar el **asiento contable** correspondiente al egreso y dejar el documento **inmutable** y reimprimible.

---

## 2. Alcance funcional

### 2.1 Incluido (v1)

- ABM de catálogo **Lugar de Despacho/Entrega** (plano: código + descripción + dirección).
- Creación / edición / eliminación de Despachos en estado **BORRADOR**.
- Listado y búsqueda de Despachos.
- Aprobación con **doble confirmación** (dos paneles modales secuenciales con detalle explicativo).
- Anulación de Despacho aprobado (con permiso) que revierte stock y asiento — apoyándose en el flujo existente de anulación de vales ([warehouse_reversal_implementation_plan.md](warehouse_reversal_implementation_plan.md)).
- Reporte PDF del Certificado de Carguío (Jasper).
- Nuevos permisos dedicados.
- Numeración correlativa por empresa vía `gensecuencia`.

### 2.2 Excluido (v1, posibles iteraciones futuras)

- Vinculación con factura o nota de venta del módulo Customers (en v1 los campos "N° de Factura" y "Código de Lote-Venta" son texto libre).
- Alta de Provider/Client desde el formulario de despacho (deben existir previamente).
- Catálogo separado de Conductores y Vehículos por Proveedor (en v1 son campos de texto digitados en cada despacho).
- Cálculo automático de peso neto a partir de bolsas; el peso neto se persiste a partir de la diferencia `bruto - tara`.
- Integración con GPS / trackeo del viaje.
- Firma electrónica del receptor.

---

## 3. Roles y permisos

Se crean **cinco** nuevos permisos en la tabla de permisos (módulo Warehouse, igual estructura que `WAREHOUSEVOUCHER`):

| Código permiso | Operación | Quién |
|---|---|---|
| `WAREHOUSEDISPATCH` | VIEW | Logística, Almacén, Producción |
| `WAREHOUSEDISPATCH` | CREATE | Logística, Almacén |
| `WAREHOUSEDISPATCH` | UPDATE | Logística, Almacén (solo en BORRADOR) |
| `WAREHOUSEDISPATCH` | DELETE | Logística (solo en BORRADOR) |
| `WAREHOUSEDISPATCHAPPROVAL` | VIEW | Jefatura de Almacén / Logística |
| `WAREHOUSEDISPATCHREVERSE` | VIEW | Contabilidad / Jefatura |
| `WAREHOUSEDISPATCHPLACE` | VIEW/CREATE/UPDATE/DELETE | Administración (catálogo) |

Toda acción está sujeta a los filtros existentes de **unidad ejecutora** y **empresa** (multi-company).

---

## 4. Modelo de datos requerido

### 4.1 Entidad `WarehouseVoucherDispatch` (cabecera del despacho)

| Campo | Tipo | Obligatorio | Origen / Validación |
|---|---|---|---|
| `id` | Long | sí (PK + companyNumber) | Generado |
| `companyNumber` | Integer | sí | Empresa actual |
| `state` | enum `DispatchState` | sí | BORRADOR \| APROBADO \| ANULADO |
| `deliveryOrderNumber` | Long | sí (al pasar a APROBADO) | `sequenceGeneratorService.nextValue("DISPATCH_ORDER_NUMBER")` |
| `dispatchDate` | Date | sí | Fecha del carguío. Default = hoy. Debe estar dentro del mes contable abierto al aprobar. |
| `deliverySeller` | `JobContract` (FK) | sí | Empleado vendedor de entrega (selectPopUp con `jobContractDataModel`) |
| `salesLotCode` | String(80) | sí | Texto libre (ej. `BAR-03-26`) |
| `bagCount` | Integer | sí | > 0; debe coincidir con `bagsToNumber - bagsFromNumber + 1` |
| `invoiceNumber` | String(50) | no | Texto libre |
| `transportCompany` | `Provider` (FK) | sí | selectPopUp con providerDataModel |
| `client` | `Client` (FK) | sí | selectPopUp con clientDataModel |
| `productionTurn` | `ProductionGroup` (FK) | sí | selectOneMenu con productionGroupList |
| `originPlace` | `DispatchPlace` (FK) | sí | selectOneMenu (catálogo nuevo) |
| `destinationPlace` | `DispatchPlace` (FK) | sí | selectOneMenu (catálogo nuevo) |
| `driverName` | String(120) | sí | Texto libre |
| `driverLicense` | String(30) | sí | Texto libre |
| `driverPhone` | String(30) | no | Texto libre |
| `vehiclePlate` | String(20) | sí | Texto libre |
| `vehicleBrand` | String(50) | no | Texto libre |
| `vehicleColor` | String(30) | no | Texto libre |
| `loadingStartTime` | Date (timestamp) | sí | Hora inicio carguío |
| `loadingEndTime` | Date (timestamp) | sí | Hora fin carguío; > `loadingStartTime` |
| `truckDispatchNumber` | Integer | sí | N° secuencial del camión en el turno (1..N) |
| `weighingTicketNumber` | String(30) | sí | N° de boleta balanza |
| `tareWeightKg` | BigDecimal(12,3) | sí | ≥ 0 |
| `grossWeightKg` | BigDecimal(12,3) | sí | > `tareWeightKg` |
| `netWeightKg` | BigDecimal(12,3) | sí (calculado) | `grossWeightKg - tareWeightKg`; persistido |
| `bagsFromNumber` | Integer | sí | ≥ 1 |
| `bagsToNumber` | Integer | sí | ≥ `bagsFromNumber` |
| `warehouse` | `Warehouse` (FK) | sí | Almacén de Productos Terminados (selectPopUp con `warehouseSearchDataModel`, filtrado por unidad ejecutora). Determina qué productos terminados se listan en el detalle. |
| `responsible` | `Employee` (FK) | sí | Auto-asignado al seleccionar `warehouse` (mismo patrón que vales existentes) |
| `executorUnit` | `BusinessUnit` (FK) | sí | Auto-asignado a partir de `warehouse.executorUnit` |
| `costCenter` | `CostCenter` (FK) | sí | Centro de costo del despacho |
| `warehouseVoucher` | `WarehouseVoucher` (FK) | no | NULL hasta aprobar. Al aprobar guarda referencia al vale de egreso generado. |
| `observation` | String(500) | no | Texto libre |
| `createdBy` / `createdDate` / `updatedBy` / `updatedDate` / `version` | auditoría estándar | sí | Igual que el resto de entidades del proyecto |

### 4.2 Entidad `WarehouseVoucherDispatchDetail` (líneas del despacho)

Una línea por cada producto terminado incluido.

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `id` | Long (PK + companyNumber) | sí | Generado |
| `companyNumber` | Integer | sí | |
| `dispatch` | `WarehouseVoucherDispatch` (FK, cascade) | sí | Cabecera |
| `productItem` | `ProductItem` (FK) | sí | Producto terminado del almacén seleccionado |
| `measureUnit` | `MeasureUnit` (FK) | sí | Auto desde `productItem.usageMeasureCode` |
| `quantity` | BigDecimal(14,6) | sí | > 0; ≤ stock disponible al aprobar |
| `unitCost` | BigDecimal(14,6) | sí (snapshot) | Tomado del costo promedio del item al aprobar |
| `amount` | BigDecimal(14,6) | sí (snapshot) | `quantity * unitCost` |
| `bagsCount` | Integer | no | Opcional por línea (para casos multi-producto) |
| `observation` | String(250) | no | Texto libre |

### 4.3 Entidad `DispatchPlace` (catálogo plano)

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `id` | Long (PK + companyNumber) | sí | Generado |
| `companyNumber` | Integer | sí | |
| `code` | String(20) | sí, único por empresa | Ej. `ORIG-SANTIVANEZ` |
| `description` | String(150) | sí | Ej. `TERDEMOL S.R.L. - PARQUE INDUSTRIAL SANTIVAÑEZ` |
| `address` | String(250) | no | Dirección completa |
| `kind` | enum `DispatchPlaceKind` | sí | `ORIGEN` \| `DESTINO` \| `AMBOS` |
| `active` | boolean | sí (default true) | Permite ocultar lugares en desuso |

### 4.4 Enums nuevos

- `DispatchState`: `BORRADOR`, `APROBADO`, `ANULADO`.
- `DispatchPlaceKind`: `ORIGEN`, `DESTINO`, `AMBOS`.

### 4.5 Sequence nuevo

- Nombre: `DISPATCH_ORDER_NUMBER`.
- Gestión: vía `sequenceGeneratorService.nextValue(...)` que internamente lee/incrementa la tabla `gensecuencia` (entidad `com.encens.khipus.model.common.Sequence`).
- Alcance: por empresa (la tabla `gensecuencia` ya está filtrada por `Company`).
- Asignación: al pasar a APROBADO. Persistido en `deliveryOrderNumber`.

---

## 5. Reglas de negocio y validaciones

### 5.1 Validaciones en BORRADOR (al guardar)

V01. Todos los campos marcados "Obligatorio" en sección 4 deben tener valor.
V02. `loadingEndTime > loadingStartTime`.
V03. `grossWeightKg > tareWeightKg`.
V04. `bagsToNumber ≥ bagsFromNumber` y `bagCount = bagsToNumber - bagsFromNumber + 1`.
V05. Debe existir al menos un `WarehouseVoucherDispatchDetail`.
V06. `dispatchDate` no puede ser anterior al inicio del mes contable abierto ni posterior a la fecha del sistema.
V07. `warehouse` debe pertenecer a `executorUnit` seleccionada y estar en estado `VIG`.
V08. Cada línea: `quantity > 0` y `productItem.warehouse == cabecera.warehouse`.

### 5.2 Validaciones en APROBADO (al confirmar la segunda pantalla)

V09. Se re-ejecutan V01-V08 (defensa en profundidad).
V10. **Stock disponible**: para cada línea, `inventoryService.findUnitaryBalanceByProductItemAndArticle(warehouse, productItem) >= quantity`. Si falla, se aborta toda la aprobación y se muestra el detalle de las líneas problemáticas (mismo patrón de `addInventoryMessages`).
V11. El mes contable de `dispatchDate` debe seguir abierto al momento de aprobar.
V12. El usuario debe tener permiso `WAREHOUSEDISPATCHAPPROVAL:VIEW`.

### 5.3 Reglas de estado

- **BORRADOR → APROBADO** vía botón "Aprobar Despacho" + doble confirmación.
- **APROBADO → ANULADO** vía botón "Anular" (permiso `WAREHOUSEDISPATCHREVERSE`). Llama al servicio existente de reversión de vales para revertir stock y generar contra-asiento.
- **BORRADOR → (borrado)** vía botón "Eliminar" (permiso DELETE). Elimina cabecera y líneas; **no** afecta inventario.
- Desde **APROBADO** o **ANULADO** no se pueden editar campos. Solo lectura + reimpresión del certificado.
- No se permite revertir un despacho cuyo mes contable esté cerrado (mismo criterio que vales).

### 5.4 Doble confirmación de aprobación

Flujo UX al hacer clic en "Aprobar Despacho":

1. **Panel 1 — Revisión de datos**: modal con resumen de cabecera (cliente, transportista, conductor, vehículo, pesos, lugares, lote, turno). Texto explicativo:
   > "Está por aprobar el Despacho N° [pendiente]. Verifique que los datos del cliente, transportista, conductor y pesos sean correctos. Al continuar pasará a la confirmación del impacto en inventario."

   Botones: `[Cancelar]` `[Continuar a confirmación de inventario]`.

2. **Panel 2 — Confirmación de impacto en inventario**: modal con tabla de productos a descontar (item, cantidad, saldo actual, saldo resultante) y texto explicativo:
   > "Al confirmar, se descontarán definitivamente las cantidades del Almacén [nombre], se generará el vale de egreso N° [se asignará al confirmar] y el asiento contable correspondiente. **Esta acción solo puede revertirse mediante anulación con permiso especial.** ¿Confirma la aprobación?"

   Botones: `[Volver]` `[Confirmar aprobación definitivamente]`.

3. Al hacer clic en "Confirmar aprobación definitivamente":
   - Se ejecuta V09-V12.
   - Se asigna `deliveryOrderNumber` desde el sequence.
   - Se construye `WarehouseVoucher` de tipo egreso + `InventoryMovement` + `MovementDetail` por cada línea (snapshot `unitCost`, `amount`).
   - Se persiste y aprueba mediante `warehouseService.saveWarehouseVoucher(...)` + `approvalWarehouseVoucherService.approveWarehouseVoucher(...)` (mismo patrón que `createTransfer` en [WarehouseVoucherCreateAction.java:226](src/main/com/encens/khipus/action/warehouse/WarehouseVoucherCreateAction.java#L226)).
   - Se vincula el vale al despacho (`dispatch.warehouseVoucher = vale`) y se cambia estado a APROBADO.
   - Toda la operación dentro de una sola transacción (`REQUIRES_NEW`); si algo falla, rollback completo y el despacho queda en BORRADOR.

---

## 6. Vista y experiencia de usuario

### 6.1 Listado `dispatchVoucherList.xhtml`

Columnas: N° Orden Entrega, Fecha, Cliente, Transportista, Placa, Lote-Venta, Bolsas, Estado, Acciones (Ver/Editar/Imprimir).
Filtros: rango de fecha, cliente, transportista, estado, lote.

### 6.2 Formulario de creación/edición `dispatchVoucherCreate.xhtml`

Layout en dos columnas siguiendo el patrón del proyecto (igual `panelGrid columns=2`):

**Columna izquierda — Datos del despacho:**
- Vendedor de entrega (selectPopUp JobContract).
- Código de lote-venta.
- N° de factura.
- Fecha de carguío / despacho.
- Transportadora (selectPopUp Provider).
- Cliente (selectPopUp Client).
- Turno / Grupo (selectOneMenu ProductionGroup).
- Lugar de despacho origen (selectOneMenu DispatchPlace filtrado por kind ∈ {ORIGEN, AMBOS}).
- Lugar de entrega destino (selectOneMenu DispatchPlace filtrado por kind ∈ {DESTINO, AMBOS}).

**Columna derecha — Carguío y vehículo:**
- N° de camión despacho.
- Hora inicial / final de carguío.
- Datos del conductor (nombre, licencia, celular).
- Datos del vehículo (placa, marca, color).
- N° boleta pesaje balanza.
- Peso tara / Peso bruto / Peso neto (calculado read-only en tiempo real con `a4j:support onblur`).
- Numeración de bolsas: desde / hasta / total (campo "total" read-only calculado).

**Sección inferior — Almacén y productos:**
- Selector de Almacén (selectPopUp `warehouseSearchDataModel`).
- Botón "Agregar producto terminado" → modal `productItemsByWarehouseListModalPanel` filtrado por almacén y por items que sean productos terminados (criterio: `productItemType.code = "PT"` o el flag que utilice XProduction).
- Tabla de líneas: Producto, U/M, Cantidad, Bolsas (opcional), Observación, Acción (eliminar).
- Observación general.

**Botones (footer):**
- `Guardar` (BORRADOR).
- `Aprobar Despacho` (dispara doble confirmación) — solo si estado=BORRADOR y permiso `WAREHOUSEDISPATCHAPPROVAL`.
- `Cancelar`.

### 6.3 Vista de detalle `dispatchVoucherUpdate.xhtml`

Misma estructura que creación pero con todos los campos en modo **lectura** si el estado es APROBADO o ANULADO. Agrega:
- N° Orden de Entrega asignado.
- Fecha y usuario de aprobación.
- Botón `Imprimir Certificado` (Jasper).
- Botón `Anular` si APROBADO y permiso `WAREHOUSEDISPATCHREVERSE`.

### 6.4 Catálogo `dispatchPlaceList.xhtml` + `Create.xhtml` + `Update.xhtml`

CRUD estándar. Columnas: código, descripción, tipo, activo.

---

## 7. Reporte — Certificado de Recepción de Carguío

Replica fiel del layout izquierdo del PDF de referencia. JasperReport (`dispatchCertificate.jrxml`) con:

- Cabecera: logo, datos de la empresa, título "CERTIFICADO DE RECEPCION DE CARGUIO", N° de Orden de Entrega.
- Bloque "Datos de Vendedor de Entrega".
- Tabla Planta/Producto/Destino/Fecha/Hora.
- Tabla Cliente / N° Lote / Cedula / Placa / Empresa Transporte / Marca / Conductor / Color.
- Tabla principal con columnas: CANTIDAD, PRODUCTO/SERVICIO, DESCRIPCIÓN DETALLADA, NUMERACION BOLSAS, TOTAL ENTREGADO (subreport).
- Bloque MEDIDOR DE PESO (Tara/Bruto/Neto/Peso Neto Carga/Pesado en/N° de Boleta).
- Bloque "Datos del Recepcionista de la Orden" con firmas (Entregue Conforme / Recibí Conforme).
- Disclaimer del artículo 956 del código de transporte (texto fijo).

Invocación: `dispatchCertificateReportAction.generateCertificate(dispatch)` desde el botón "Imprimir Certificado", siguiendo el patrón de `voucherDocumentReportAction.generateReport`.

---

## 8. Integraciones

| Integración | Cómo |
|---|---|
| Inventario | `warehouseService.saveWarehouseVoucher(...)` + `approvalWarehouseVoucherService.approveWarehouseVoucher(...)` |
| Asiento contable | Disparado por `approvalWarehouseVoucherService` (no se reimplementa) |
| Anulación / Reversión | `ReverseInventoryService` y `annulVoucher` ([warehouse_reversal_implementation_plan.md](warehouse_reversal_implementation_plan.md)) |
| Mes contable | `monthProcessService.getMothProcessDate` y `closeCurrentProcessMonth` |
| Secuencia | `sequenceGeneratorService.nextValue("DISPATCH_ORDER_NUMBER")` |
| XProduction | Lectura de `ProductionGroup` para selector de turno |
| Customers | Lectura de `Client` (selectPopUp) |
| Finances | Lectura de `Provider` (selectPopUp) |
| Employees | Lectura de `JobContract` (selectPopUp del vendedor) |

---

## 9. Internacionalización

Todas las cadenas visibles vía `messages_es.properties`. Prefijo de claves: `WarehouseDispatch.*` y `DispatchPlace.*`. Ejemplos:

```
WarehouseDispatch.title=Despacho de Productos Terminados
WarehouseDispatch.new=Nuevo Despacho
WarehouseDispatch.edit=Editar Despacho
WarehouseDispatch.deliveryOrderNumber=N° de Orden de Entrega
WarehouseDispatch.deliverySeller=Vendedor de Entrega
WarehouseDispatch.salesLotCode=Código de Lote-Venta
WarehouseDispatch.bagCount=Número de Bolsas
WarehouseDispatch.dispatchDate=Fecha de Carguío/Despacho
WarehouseDispatch.invoiceNumber=N° de Factura
WarehouseDispatch.transportCompany=Empresa Transportadora
WarehouseDispatch.client=Cliente
WarehouseDispatch.productionTurn=Grupo / Turno
WarehouseDispatch.originPlace=Lugar de Despacho Origen
WarehouseDispatch.destinationPlace=Lugar de Entrega Destino
WarehouseDispatch.driver.name=Nombre del Conductor
WarehouseDispatch.driver.license=Número de Licencia
WarehouseDispatch.driver.phone=Celular del Conductor
WarehouseDispatch.vehicle.plate=Placa de Vehículo
WarehouseDispatch.vehicle.brand=Marca
WarehouseDispatch.vehicle.color=Color
WarehouseDispatch.loadingStart=Hora Inicial de Carguío
WarehouseDispatch.loadingEnd=Hora Final de Carguío
WarehouseDispatch.truckDispatchNumber=N° de Camión Despacho
WarehouseDispatch.weighingTicket=N° Boleta Pesaje Balanza
WarehouseDispatch.weight.tare=Peso Tara (Kg)
WarehouseDispatch.weight.gross=Peso Bruto (Kg)
WarehouseDispatch.weight.net=Peso Neto (Kg)
WarehouseDispatch.bags.from=Numeración de Bolsas (desde)
WarehouseDispatch.bags.to=Numeración de Bolsas (hasta)
WarehouseDispatch.button.approve=Aprobar Despacho
WarehouseDispatch.approve.confirm1.title=Revisión de datos del despacho
WarehouseDispatch.approve.confirm2.title=Confirmación del impacto en inventario
WarehouseDispatch.approve.confirm2.message=Al confirmar se descontará definitivamente del Almacén y se generará el vale de egreso.
WarehouseDispatch.error.insufficientStock=Stock insuficiente para el producto {0}. Disponible: {1}; requerido: {2}.
WarehouseDispatch.error.bagsMismatch=El número de bolsas ({0}) no coincide con el rango {1}-{2}.
WarehouseDispatch.state.BORRADOR=Borrador
WarehouseDispatch.state.APROBADO=Aprobado
WarehouseDispatch.state.ANULADO=Anulado
DispatchPlace.title=Lugares de Despacho/Entrega
DispatchPlace.code=Código
DispatchPlace.description=Descripción
DispatchPlace.address=Dirección
DispatchPlace.kind=Tipo
DispatchPlace.kind.ORIGEN=Origen
DispatchPlace.kind.DESTINO=Destino
DispatchPlace.kind.AMBOS=Ambos
```

---

## 10. Criterios de aceptación (definition of done)

| # | Criterio | Cómo se verifica |
|---|---|---|
| AC01 | Se puede crear un despacho en BORRADOR con todos los campos requeridos | Test manual en dev |
| AC02 | No se puede guardar sin pasar validaciones V01-V08 | Cada validación con mensaje localizado |
| AC03 | Aprobación con doble confirmación funciona y muestra impacto en inventario | Test manual mostrando saldos antes/después |
| AC04 | Tras aprobar: stock descontado, vale generado, asiento contable correcto, despacho en estado APROBADO con número de orden | Consulta directa a `inv_articulo`, vales, asiento |
| AC05 | No se puede aprobar si algún item no tiene stock suficiente; el despacho queda en BORRADOR sin cambios laterales | Test manual |
| AC06 | Anulación revierte stock y genera contra-asiento; despacho queda ANULADO | Test manual + revisión asientos |
| AC07 | Reporte PDF se genera y coincide visualmente con el formato del PDF de referencia | Comparación lado a lado |
| AC08 | Usuario sin permiso no puede ver / crear / aprobar / anular según corresponda | Test con dos usuarios |
| AC09 | El catálogo `DispatchPlace` permite ABM completo y se respeta el filtro por `kind` en los combos del despacho | Test manual |
| AC10 | Multi-company: dos empresas con sequence independiente, datos aislados | Test en dev con dos compañías |
| AC11 | Build con JDK 1.8 pasa (`ant clean explode`) | CI/local |
| AC12 | Numeración de Orden de Entrega es correlativa y única por empresa | Crear varios despachos consecutivos |

---

## 11. Dependencias y suposiciones

- Existe un mecanismo para identificar productos terminados (producto resultado de XProduction). Si no existe un flag específico en `ProductItem`, se asumirá que **cualquier item disponible en el almacén seleccionado** es elegible, dejando al operador la responsabilidad de elegir el almacén correcto. Confirmar con el usuario antes de implementar el filtro.
- El permiso `WAREHOUSEDISPATCHAPPROVAL` puede compartir titulares con `WAREHOUSEVOUCHERAPPROVAL` pero es un permiso independiente para granularidad futura.
- La estructura de `Provider` ya tiene los campos necesarios para identificar la transportadora (razón social, NIT). No se modificará Provider en v1.
- No hay requerimiento de exportar a Excel en v1 (solo PDF Jasper).
- El despacho **no** genera factura ni nota de venta; eso queda en módulos de Customers/Sales (fuera de alcance).

---

## 12. Referencias

- PDF de especificación: `REQ DESPACHO.pdf` (compartido por el cliente).
- Flujo actual de vales: [warehouse_order_voucher_flow.md](warehouse_order_voucher_flow.md).
- Reversión de vales (base reutilizable): [warehouse_reversal_implementation_plan.md](warehouse_reversal_implementation_plan.md).
- Plan de implementación: [dispatch_voucher_implementation_plan.md](dispatch_voucher_implementation_plan.md).
