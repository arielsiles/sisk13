# Mejoras UX: Modulo de Ventas (salesBox)

**Rama:** `feature/sales-ux-improvements` (desde `dev_ilva`)
**Fecha:** 2026-04-13
**Archivo principal:** `view/customers/salesBox.xhtml`
**Action bean:** `action/customers/SalesAction.java`

---

## Contexto

La UX de ventas (`salesBox.xhtml`) es una interfaz POS (punto de venta) single-page que permite
registrar ventas a credito y al contado de forma continua sin recargar la pagina.
Funciona con AJAX (a4j/RichFaces) y Seam conversation scope.

URL: `/khipus/customers/salesBox?actionMethod=home.xhtml%3AsalesAction.openSale()&conversationPropagation=begin`

---

## Evaluacion actual

### Positivo
- Single-page AJAX sin recarga de pagina
- Conversation scope mantiene estado durante el flujo completo
- Botones de producto rapido por categoria (un click para agregar)
- SuggestionBox con autocompletado para busqueda de productos
- `clearAll()` despues de cada venta para repetir el ciclo
- Modales de confirmacion en archivos separados (patron del proyecto)

### Negativo (detalle en tareas abajo)
- Bugs potenciales en validacion y doble-click
- Sin manejo de errores visible al usuario
- Sin indicadores de carga AJAX
- Codigo excesivamente largo y mezclado (SRP)
- Debug con `System.out.println` en produccion
- Funcionalidades deshabilitadas (stock, inventario)
- Strings hardcodeados

---

## Tareas de mejora

### Prioridad Alta (bugs potenciales)

#### T01 - Fix checkMinimumValues() que no detiene ejecucion
- **Estado:** Pendiente
- **Problema:** `checkMinimumValues()` muestra mensajes de error pero su `return` solo sale del metodo, no del caller. Si no hay cliente seleccionado, `createSale()` sigue ejecutandose y lanza NPE en `client.getProductDiscount()`.
- **Solucion:** Cambiar `checkMinimumValues()` para que retorne `boolean`. Los callers (`registerSale`, `registerSaleAndInvoice`, `registerCashSale`, `registerCashSaleNoInvoice`) deben hacer `if (!checkMinimumValues()) return;`.
- **Archivos:** `SalesAction.java`
- **Lineas:** 431-446 (checkMinimumValues), 448-462 (registerSale), 465-485 (registerSaleAndInvoice), 488-517 (registerCashSale), 572-586 (registerCashSaleNoInvoice)

#### T02 - Proteccion contra doble-click en botones de registro
- **Estado:** Pendiente
- **Problema:** Los botones "Aceptar", "Cobrar SF", "Aceptar y facturar" no se deshabilitan durante el procesamiento AJAX. Un doble-click rapido puede crear ventas duplicadas.
- **Solucion:** Agregar `onclick="this.disabled=true"` y `oncomplete="this.disabled=false"` en los `a4j:commandButton` de los modales de confirmacion.
- **Archivos:** `confirmCreditSaleModalPanel.xhtml` (botones confirmRegisterButton, confirmRegisterInvoiceButton), `confirmCashSaleModalPanel.xhtml` (botones confirmRegisterButton, confirmRegisterNoInvoiceButton)

#### T03 - Try-catch con mensajes de error limpios en metodos de registro
- **Estado:** Pendiente
- **Problema:** `registerSale()`, `registerCashSale()`, `registerCashSaleNoInvoice()`, `registerSaleAndInvoice()` no tienen try-catch. Si falla `createInvoice()`, `accountingCashSale()`, o el servicio de transaccion, el usuario ve una excepcion cruda en pantalla.
- **Solucion:** Envolver el cuerpo de cada metodo en try-catch, mostrar `facesMessages.addFromResourceBundle(ERROR, "mensaje")` y hacer log del error. Limpiar el formulario solo si la venta fue exitosa.
- **Secuencia protegida:** Generacion de secuencia movida dentro de `createSaleWithInventory()` para que sea parte de la misma transaccion atomica. Si falla el persist, la secuencia se revierte.
- **Estado parcial:** Si la venta se registra pero falla factura o asiento, se muestra WARN con numero de venta y se limpia el formulario. La factura se puede generar posteriormente (funcionalidad existente: `processBilling`).
- **TODO pendiente:** Implementar generacion de asiento contable posterior para ventas que quedaron sin asiento.
- **Archivos:** `SalesAction.java`, `SaleTransactionService.java`, `SaleTransactionServiceBean.java`

### Prioridad Media (UX y calidad)

#### T04 - Toast notifications y mejoras visuales listado pedidos
- **Estado:** Completado
- **Problema:** Los mensajes de Seam (`facesMessages`) se renderizaban como un bloque que empujaba todo el contenido hacia abajo, desplazando la UX de ventas.
- **Solucion:**
  - Mensajes globales convertidos a toast flotante (`position:fixed; top:20px; right:20px`) con boton X para cerrar
  - `a4j:outputPanel ajaxRendered="true"` para auto-renderizar en cada request AJAX sin modificar reRender de botones
  - Estilos toast: box-shadow, border-radius, colores diferenciados por tipo (error rojo, warn naranja, info verde)
  - Fix `checkBillingMode()` retornaba `null` → NPE unboxing → TX ABORT_ONLY. Ahora retorna `false` (offline)
  - Fix `generateInvoiceOnline()` catch `IOException` → `Exception` + `markRollback()`
  - Fix `registerCashSale()` inner catch sin `return` → cascada a outer catch
  - Reemplazar `clearAll()/assignCustomerOrderTypeDefault()` por `safeClearAll()` en todos los register methods
  - Mensaje billing sin conexion: WARN "Facturacion pendiente, sin conexion"
  - Iconos estado en customerOrderList: `pending.png` (PEN), `file-check.png` (CONTA), `anulado.png` (ANL)
  - Columna Estado movida a posicion 2 (despues de checkbox)
  - Icono `warn.png` para ventas al contado sin asiento contable
  - Icono `pending.png` en columna Estado SIN cuando factura existe pero sin estado
- **Archivos:** `messages.xhtml`, `theme.css`, `SalesAction.java`, `BillControllerAction.java`, `customerOrderList.xhtml`

#### T05 - Rehabilitar columna Existencia (stock disponible)
- **Estado:** Pendiente
- **Problema:** La columna "Existencia" en la tabla de productos siempre muestra vacio (linea 404: `<h:outputText value="" />`). El metodo `getUnitaryBalance()` esta comentado. El vendedor no puede ver el stock disponible antes de vender.
- **Solucion:** Rehabilitar la llamada a `getUnitaryBalance(articleItem)` o mostrar el `articleItem.unitaryBalance` que ya se carga en `addProduct()` (linea 293).
- **Archivos:** `salesBox.xhtml` (linea 404)

#### T06 - Reemplazar System.out.println por logging Seam
- **Estado:** Pendiente
- **Problema:** ~40 `System.out.println` de debug en `SalesAction.java`. Genera ruido en consola de produccion, no se puede filtrar por nivel, impacto en performance por concatenacion de strings.
- **Solucion:** Reemplazar por `@Logger Log log` de Seam con niveles apropiados (`log.debug()` para trazas, `log.info()` para eventos de negocio, `log.error()` para errores). Eliminar prints que no aportan valor.
- **Archivos:** `SalesAction.java`

#### T07 - Migrar processBillingSpecial() al nuevo SaleSequenceService
- **Estado:** Pendiente
- **Problema:** `processBillingSpecial()` (linea 1023) aun usa `financesPkGeneratorService.getNextNoTransByDocumentType()` que tiene el bug de concurrencia original (lock de secuencia retenido durante toda la transaccion).
- **Solucion:** Cambiar a `saleSequenceService.getNextValue()`. Tambien evaluar si `saleService.createSale()` en linea 1041 deberia usar `saleTransactionService.createSaleWithInventory()`.
- **Archivos:** `SalesAction.java` (linea 1023, 1041)

### Prioridad Baja (deuda tecnica)

#### T08 - Limpiar codigo comentado
- **Estado:** Pendiente
- **Problema:** Multiples bloques de codigo comentado: `isThereInventory` check (lineas 254-257, 583-589), `getUnitaryBalance` (lineas 402-403), subsidio dropdown (lineas 170-183 xhtml), `productsSelected` lista (linea 94), etc. Reduce legibilidad.
- **Solucion:** Eliminar bloques comentados que no se van a rehabilitar. Los que son funcionalidad pendiente, documentar con un `// TODO: [descripcion]` de una linea.
- **Archivos:** `SalesAction.java`, `salesBox.xhtml`

#### T09 - Extraer logica contable a servicio separado
- **Estado:** Pendiente
- **Problema:** `SalesAction` (~1400 lineas) mezcla responsabilidades: ventas, facturacion, asientos contables (`accountingCreditSale`, `accountingCashSale`, `accountingCashSaleNoInvoice`), generacion XML, cambios de modo de facturacion, procesamiento offline. Viola Single Responsibility Principle.
- **Solucion:** Extraer metodos contables a un nuevo servicio `SaleAccountingService` con metodos: `createCreditSaleVoucher()`, `createCashSaleVoucher()`, `createCashSaleNoInvoiceVoucher()`. SalesAction solo orquesta.
- **Archivos nuevos:** `SaleAccountingService.java`, `SaleAccountingServiceBean.java`
- **Archivo modificado:** `SalesAction.java`

#### T10 - Mover strings hardcodeados a messages.properties
- **Estado:** Pendiente
- **Problema:** Mensajes de error como "Monto total incorrecto para facturar, revise el % descuento.", "No se puede realizar la venta, monto incorrecto.", "Inventario Insuficiente..." estan hardcodeados en Java.
- **Solucion:** Crear keys en `messages.properties` y usar `facesMessages.addFromResourceBundle()` con las keys.
- **Archivos:** `SalesAction.java`, `messages.properties`

---

## Estado de avance

| Tarea | Descripcion | Prioridad | Estado |
|-------|-------------|-----------|--------|
| T01 | Fix checkMinimumValues() | Alta | **Completado** |
| T02 | Proteccion doble-click | Alta | **Completado** |
| T03 | Try-catch en registro + secuencia protegida | Alta | **Completado** |
| T04 | Toast notifications + iconos listado | Media | **Completado** |
| T05 | Columna Existencia | Media | Pendiente |
| T06 | Logging Seam | Media | Pendiente |
| T07 | Migrar processBillingSpecial | Media | Pendiente |
| T08 | Limpiar codigo comentado | Baja | Pendiente |
| T09 | Extraer logica contable | Baja | Pendiente |
| T10 | Strings a messages.properties | Baja | Pendiente |
