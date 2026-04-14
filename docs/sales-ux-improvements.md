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
- **Archivos:** `SalesAction.java`

### Prioridad Media (UX y calidad)

#### T04 - Indicador de carga AJAX (spinner/loading)
- **Estado:** Pendiente
- **Problema:** No hay `a4j:status` ni spinner visible. El usuario no sabe si la venta se esta procesando, especialmente en ventas con facturacion que toman mas tiempo.
- **Solucion:** Agregar un `a4j:status` global con un spinner/overlay que se muestre durante las operaciones AJAX.
- **Archivos:** `salesBox.xhtml`

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
| T01 | Fix checkMinimumValues() | Alta | Pendiente |
| T02 | Proteccion doble-click | Alta | Pendiente |
| T03 | Try-catch en registro | Alta | Pendiente |
| T04 | Spinner/loading AJAX | Media | Pendiente |
| T05 | Columna Existencia | Media | Pendiente |
| T06 | Logging Seam | Media | Pendiente |
| T07 | Migrar processBillingSpecial | Media | Pendiente |
| T08 | Limpiar codigo comentado | Baja | Pendiente |
| T09 | Extraer logica contable | Baja | Pendiente |
| T10 | Strings a messages.properties | Baja | Pendiente |
