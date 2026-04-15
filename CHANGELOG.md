# Changelog

Todos los cambios notables de este proyecto se documentan aqui.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es/1.0.0/).

## [6.0.46] - 2026-04-14

### Agregado
- **Toast notifications global**: Mensajes del sistema como notificaciones flotantes (top-right) con boton X para cerrar. No empuja contenido. Auto-renderizado en cada request AJAX (`ajaxRendered=true`). Estilos diferenciados por tipo: error (rojo), warn (naranja), info (verde).
- **Iconos de estado en listado de pedidos**: `pending.png` (PENDIENTE), `file-check.png` (CONTABILIZADO), `anulado.png` (ANULADO). Columna Estado movida a posicion 2.
- **Indicador ventas incompletas**: Icono `warn.png` en ventas al contado sin asiento contable con tooltip "Asiento contable pendiente".
- **Indicador facturacion pendiente**: Icono `pending.png` en columna Estado SIN cuando factura existe pero sin estado.

### Corregido
- **Fix error handling en registro de ventas** (T01-T03):
  - `checkMinimumValues()` ahora retorna boolean y detiene ejecucion. Validacion antes de abrir modales.
  - Proteccion doble-click en botones de registro (onclick disable/oncomplete enable).
  - Try-catch en los 4 metodos de registro con mensajes diferenciados por tipo de fallo.
  - `markRollback()` para manejo limpio de TX ABORT_ONLY en Seam.
  - `safeClearAll()` en paths de exito para evitar fallos de EJB post-error.
- **Fix NPE billing API deshabilitada**: `checkBillingMode()` retornaba `null` → NPE por unboxing `Boolean` a `boolean` → `RollbackInterceptor` de Seam marcaba TX como ABORT_ONLY. Ahora retorna `false` (modo offline).
- **Fix `generateInvoiceOnline()`**: Catch `IOException` cambiado a `Exception` + `markRollback()` como safety net.
- **Fix `registerCashSale()` inner catch**: Faltaba `return` despues de `markRollback()`, causando cascada al outer catch con mensaje incorrecto.
- **Mensaje billing sin conexion**: Cambiado de INFO "Facturacion SIN CONEXION, -1" a WARN "Facturacion pendiente, sin conexion".
- **Secuencia protegida**: Generacion de secuencia movida dentro de `createSaleWithInventory()` como parte de la transaccion atomica.

---

## [6.0.45] - 2026-04-13

### Corregido
- **Fix concurrencia ventas a credito**: Error `TransactionRequiredException: no transaction is in progress` cuando dos o mas usuarios registraban ventas simultaneamente sobre el mismo producto.
  - **Causa raiz**: La funcion MySQL `getNextSeq()` retenia un row lock en la tabla `_sequence` durante toda la transaccion de Seam (persist pedido + actualizacion inventario + costos). Si la transaccion duraba mas de `innodb_lock_wait_timeout` (50s), MySQL hacia rollback de la segunda transaccion.
  - **Nuevo `SaleSequenceService`**: Generador de secuencias con `@TransactionAttribute(REQUIRES_NEW)` y `@PersistenceContext`. Micro-transaccion independiente que libera el lock en ~1ms. Reemplaza llamada a `getNextSeq()` en ventas.
  - **Nuevo `SaleTransactionService`**: Operacion atomica de venta (persist pedido + actualizacion inventario + costos ProductItem) en una sola transaccion `REQUIRES_NEW`. Usa `LockModeType.WRITE` en Inventory para serializar accesos concurrentes al mismo producto.
  - **Atomicidad**: Si falla la actualizacion de inventario, se revierte el pedido completo (antes quedaban pedidos huerfanos sin descuento de inventario).
  - **Codigo legacy intacto**: `SaleServiceBean`, `InventoryServiceBean`, `FinancesPkGeneratorServiceBean` sin cambios. Solo se modifica `SalesAction` para usar los nuevos servicios.
  - Archivos nuevos: `SaleSequenceService.java`, `SaleSequenceServiceBean.java`, `SaleTransactionService.java`, `SaleTransactionServiceBean.java`
  - Archivo modificado: `SalesAction.java`
  - Tests: 15 tests de concurrencia (secuencias, inventario, atomicidad, anotaciones)

---

## [6.0.43]

### 2026-04-12

#### Agregado
- **Reporte Recepcion de Pedidos**: Migrado desde khipus2 al listado de pedidos (`customerOrderList.xhtml`).
  - Modal con filtros: Fecha de entrega, Almacen (default DAIRY), Territorio (multi-select dinamico por fecha y almacen).
  - Reporte Jasper (crosstab) con columnas: Cliente, Nota (codigo pedido), productos como columnas dinamicas.
  - Cabecera: titulo y compania desde tabla configuracion, cantidad y monto calculados.
  - Compatibilidad iReport 5.6.0: sanitizador automatico de atributos incompatibles con JasperReports 3.7.4 (uuid, bucket class, measureExpression class, textFieldExpression class).
  - Filtros: tipoventa=CREDIT, estado<>ANULADO, almacen del producto.
  - Modal panel separado en `recepcionPedidosModalPanel.xhtml` (patron existente con `s:decorate`).
  - UX: loading "Cargando territorios..." al cambiar fecha/almacen, reset de filtros al abrir modal, calendar solo seleccion.
  - Cada pedido del mismo cliente se muestra en fila independiente (no celdas combinadas).
  - Columna Nota con fondo gris claro para diferenciacion visual.
  - Archivos: `RecepcionPedidosReportAction.java`, `recepcionPedidos.jrxml`, `customerOrderList.xhtml`, `recepcionPedidosModalPanel.xhtml`

---

## [6.0.40]

### 2026-03-15

#### Modificado
- **Importacion de descuentos desde Excel**: Simplificada la asignacion de zona productiva en `importFromExcel()`.
  - Eliminada validacion que comparaba IDZONAPRODUCTIVA del Excel (col 9) con la zona del productor encontrado por CI. Ya no genera error si la zona del Excel no coincide.
  - En la creacion del registro, se usa directamente `producer.getProductiveZone()` en lugar de leer la zona desde el Excel con `em.getReference()`.
  - Archivos: `SalaryMovementProducerAction.java`

### 2026-03-14

#### Modificado
- **Reorganizacion de pantalla Generar Planillas** (`rawMaterialPayRoll.xhtml`):
  - Panel reorganizado de 3 columnas a 2 columnas. Campos `Materia prima` y `Zona productiva` movidos al inicio de la primera columna.
  - Botones `Generar planilla de pago`, `Redefinir planilla` y `Borrar Todos` movidos dentro del panel en fila independiente al 100% con alineacion derecha y `styleClass="button"`.
  - Eliminados los dos botones `Guardar` (superior e inferior) y la tabla completa de resultados (Productor, Acopiado, Descuentos, Totales).
- **Precio unitario - Botones de icono**: Reemplazado boton de texto `Editar` por dos iconos alternados segun estado:
  - Icono lapiz (`edit3.png`) para activar edicion, icono check verde (`active.gif`) para confirmar y bloquear.
  - Campo con `disabled` por defecto (apariencia gris/inactiva igual que `Tasa impuesto`), se activa al hacer click en editar.
  - Envuelto en `<a4j:region>` para que el boton confirmar procese correctamente el valor editado sin afectar otros campos del formulario.
- **Formato decimal**: Campos `Precio unitario` y `Tasa impuesto` ahora muestran valores con 2 decimales usando `f:convertNumber` con patron `patterns.decimalNumber` (`#,##0.00`).
- Eliminado campo duplicado de `Tasa impuesto(%)` que aparecia dos veces.

### 2026-03-13

#### Agregado
- **R4 - Columna FIRMA en Planilla General**: Columna vacia "FIRMA" en el reporte Planilla General de Pago a Productores para que los productores firmen. Filas de doble alto (30px) para dar espacio a la firma.
- **R5 - Precio unitario en Planilla General**: El periodo del reporte Planilla General muestra el precio unitario de la leche, ej: `1RA QUINCENA FEBRERO 2026    Precio: 4.00`.

#### Modificado
- `rawMaterialGeneralPayRollReport.jrxml` - Columna FIRMA, filas de 30px, redistribucion de anchos de columnas
- `RawMaterialGeneralPayRollReportAction.java` - Precio unitario anexado al texto del periodo

### 2026-03-12

#### Agregado
- **R0 - Precio Unitario editable**: Campo `Precio Unitario` visible y editable en la pantalla de generacion de planillas de acopio de leche. Permite ajustar el precio antes de generar.
- **R1 - Filtro "Sin domingos"**: Checkbox `Sin domingos` en generacion de planillas de acopio. Al activarse, excluye los registros de acopio correspondientes a dias domingo del calculo de la planilla.
- **R2 - Filtro "Solo domingos"**: Checkbox `Solo domingos` en generacion de planillas de acopio. Al activarse, genera la planilla considerando unicamente los registros de acopio de dias domingo.
- **R3 - Solo domingos sin descuentos**: Cuando se genera planilla con filtro "Solo domingos" (`dayFilter=2`), se anulan todos los descuentos (retencion impositiva, alcohol, concentrados, credito, veterinario, yogurt, tachos, otros egresos, comision, reserva, descuento GA). Se mantiene el ajuste por diferencias de peso en zona productiva (`productiveZoneAdjustment`).

#### Optimizado
- **Generacion de planillas de acopio de leche**: Reduccion de ~2000+ queries a ~50-70 en el proceso `generateAll()`.
  Tiempo estimado de 5+ min a <30 seg para ~100 productores en ~20 zonas.
  - OPT-1: Eliminada query muerta en `addProrationAlcohol()` (~100 queries)
  - OPT-2: Cache de `hasLicense()` por productor (~1400 lazy loads)
  - OPT-3: Pre-carga batch de `ProducerTax` con JOIN FETCH (~200 queries)
  - OPT-4: Batch `prepareDiscount()` por zona con nueva NamedQuery (~100 queries)
  - OPT-5: Eliminado `findById()+update()` redundante, persist directo de DiscountReserve (~200 queries)
  - OPT-6: Calculo global `totalWeightFortnight` movido fuera del loop de zonas (~19 queries)
  - OPT-7: Consolidacion de 3 iteraciones del mapa de productores en `applyProrations()` (mejora CPU)

#### Modificado
- `RawMaterialPayRollServiceBean.java` - Nuevos metodos: `preloadProducerTaxes()`, `hasLicenseFromTax()`, `applyProrations()`
- `RawMaterialPayRollService.java` - Firma de `generatePayroll()` con parametro `totalWeightFortnight`
- `RawMaterialPayRollAction.java` - Inyeccion de `CollectedRawMaterialCalculatorService`, pre-calculo de peso quincenal
- `SalaryMavementProducerServiceBean.java` - Nuevo metodo `prepareDiscountsBatch()`
- `SalaryMovementProducerService.java` - Nueva firma `prepareDiscountsBatch()`
- `SalaryMovementProducer.java` - Nueva NamedQuery `SalaryMovementProducer.getDiscountByZone`

---

## [6.0.30] - (fecha anterior)

(Releases anteriores no documentados en este formato)
