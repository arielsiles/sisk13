# Fix: Concurrencia en Ventas a Credito

**Rama:** `bugfix/concurrent-credit-sale` (creada desde `dev_ilva`)
**Fecha:** 2026-04-13
**Estado:** Pendiente de implementacion

---

## Error reportado

Cuando dos o mas usuarios realizan una venta a credito sobre un mismo producto simultaneamente, uno de los usuarios recibe:

```
javax.ejb.EJBTransactionRolledbackException: no transaction is in progress

at com.encens.khipus.action.customers.SalesAction.createSale(SalesAction.java:666)
at com.encens.khipus.action.customers.SalesAction.registerSale(SalesAction.java:449)

Caused by: javax.persistence.TransactionRequiredException: no transaction is in progress
at org.hibernate.ejb.AbstractEntityManagerImpl.flush(AbstractEntityManagerImpl.java:301)
at com.encens.khipus.service.customers.SaleServiceBean.createSale(SaleServiceBean.java:40)
```

---

## Analisis de causa raiz

### Flujo actual de una venta a credito

```
Seam inicia JTA Transaction
  +-- SalesAction.registerSale()
       +-- SalesAction.createSale()                        <-- metodo local (no EJB)
       |    +-- financesPkGeneratorService
       |    |     .getNextNoTransByDocumentType()           <-- EJB, REQUIRED (sin REQUIRES_NEW)
       |    |     +-- MySQL: SELECT getNextSeq('CREDIT')   <-- BLOQUEA fila en tabla _sequence
       |    |        (lock retenido hasta commit de la transaccion JTA)
       |    |
       |    +-- saleService.createSale(customerOrder)       <-- EJB, REQUIRED
       |          +-- em.persist() + em.flush()             <-- inserta en tabla pedidos
       |
       +-- inventoryService.updateInventoryForSales()       <-- EJB, REQUIRED
            +-- lee Inventory, resta cantidad, merge, flush
            +-- actualiza InventoryDetail, merge, flush
            +-- saleService.updateArticleForOutputs()       <-- actualiza ProductItem (CT, saldo_mon)
Seam hace commit de JTA Transaction                        <-- AQUI recien se libera el lock de secuencia
```

### Escenario concurrente (2 usuarios)

| Tiempo | Usuario A | Usuario B |
|--------|-----------|-----------|
| T0 | Seam inicia TX-A | Seam inicia TX-B |
| T1 | `getNextSeq('CREDIT')` -> **bloquea fila secuencia** -> obtiene codigo 100 | |
| T2 | `saleService.createSale()` -> persist + flush | `getNextSeq('CREDIT')` -> **BLOQUEADO esperando TX-A** |
| T3 | `inventoryService.updateInventoryForSales()` -> actualiza inventario | **sigue bloqueado...** |
| T4 | `updateArticleForOutputs()` -> actualiza ProductItem | **sigue bloqueado...** |
| T5 | Seam commit TX-A -> **libera lock secuencia** | |
| T5+ | | Obtiene codigo 101, continua... |

**Si TX-A tarda mas de `innodb_lock_wait_timeout` (50s default MySQL):**
- MySQL hace rollback de TX-B automaticamente
- Cuando `saleService.createSale()` de usuario B llama `em.flush()` -> **"no transaction is in progress"**

### 3 problemas identificados

1. **Lock de secuencia retenido demasiado tiempo:** `getNextNoTransByDocumentType()` no tiene `@TransactionAttribute(REQUIRES_NEW)`, participa en la transaccion de Seam que no hace commit hasta que termina todo `registerSale()` (persist + inventario + costos).

2. **Race condition en inventario:** `updateInventoryForSales()` hace read-modify-write sin locking (`SELECT` sin `FOR UPDATE`, sin `@Version`). Dos ventas del mismo producto leen el mismo saldo y se sobrescriben.

3. **Operacion no atomica:** `saleService.createSale()` y `inventoryService.updateInventoryForSales()` son llamadas EJB separadas. Si la primera funciona pero la segunda falla, queda un pedido persistido sin descuento de inventario.

---

## Plan de solucion

### Principio: Codigo legacy intacto, implementacion en archivos nuevos

| Archivo existente | Se modifica? | Razon |
|---|---|---|
| `FinancesPkGeneratorServiceBean.java` | **NO** | Lo usan ~8 modulos (warehouse, vouchers, purchases, payable, accounting, discharge) |
| `SaleServiceBean.java` / `SaleService.java` | **NO** | Lo usa inventoryService, otros flujos de venta |
| `InventoryServiceBean.java` / `InventoryService.java` | **NO** | Lo usan produccion, almacen, anulaciones |
| `SalesAction.java` | **SI** | Controller JSF especifico de ventas, no compartido por otros modulos |

### Parte 1: SequenceGeneratorService (nuevo, reutilizable)

**Archivos nuevos:**
- `src/main/com/encens/khipus/service/finances/SequenceGeneratorService.java` (interfaz)
- `src/main/com/encens/khipus/service/finances/SequenceGeneratorServiceBean.java` (implementacion)

**Caracteristicas:**
- `@Stateless`, `@TransactionAttribute(REQUIRES_NEW)`
- Metodo: `long getNextValue(String sequenceName)`
- Logica interna (queries parametrizadas, sin SQL injection):
  ```sql
  UPDATE _sequence SET seq_val = seq_val + 1 WHERE seq_name = :name
  SELECT seq_val FROM _sequence WHERE seq_name = :name
  -- Si no existe la fila: INSERT con valor 1
  ```
- Micro-transaccion propia: lock liberado en ~1ms
- Misma tabla `_sequence`, sin migracion de schema
- Reutilizable: cualquier modulo puede inyectar `sequenceGeneratorService` cuando quiera migrar del viejo `getNextSeq()`. No obliga a nadie a migrar ahora.

### Parte 2: SaleTransactionService (nuevo, atomico)

**Archivos nuevos:**
- `src/main/com/encens/khipus/service/customers/SaleTransactionService.java` (interfaz)
- `src/main/com/encens/khipus/service/customers/SaleTransactionServiceBean.java` (implementacion)

**Caracteristicas:**
- `@Stateless`, `@TransactionAttribute(REQUIRES_NEW)`
- Metodo: `void createSaleWithInventory(CustomerOrder customerOrder)`
- Usa un solo `EntityManager` (elimina inconsistencia `entityManager` vs `listEntityManager`)
- Operaciones en UNA transaccion atomica:
  1. Persist `ArticleOrder`s + `CustomerOrder` + flush
  2. Para cada articulo:
     - `SELECT ... FOR UPDATE` sobre `Inventory` (`LockModeType.PESSIMISTIC_WRITE`)
     - Resta cantidad, merge, flush
     - Actualiza `InventoryDetail` con nuevo saldo
     - Actualiza `ProductItem` (CT, saldo_mon) - misma logica que `updateArticleForOutputs()`
  3. Si cualquier paso falla -> rollback completo

### Parte 3: Modificar SalesAction.java

**Unico archivo existente que se modifica.**

Cambios:

1. **Inyectar** los nuevos servicios:
   ```java
   @In private SequenceGeneratorService sequenceGeneratorService;
   @In private SaleTransactionService saleTransactionService;
   ```

2. **`createSale()`** (linea 598) - cambiar generacion de secuencia:
   ```java
   // Antes:
   Long saleCode = new Long(financesPkGeneratorService.getNextNoTransByDocumentType(saleType.getSequenceName()));
   // Ahora:
   Long saleCode = sequenceGeneratorService.getNextValue(saleType.getSequenceName());
   ```
   Y **quitar** la llamada a `saleService.createSale()` (linea 666). El metodo solo CONSTRUYE el CustomerOrder, ya no persiste.

3. **`registerSale()`** (linea 442):
   ```java
   // Antes:
   CustomerOrder co = createSale();                      // construye + persiste
   inventoryService.updateInventoryForSales(co);         // inventario separado

   // Ahora:
   CustomerOrder co = createSale();                      // solo construye
   saleTransactionService.createSaleWithInventory(co);   // todo atomico
   ```

4. **Aplicar el mismo cambio a los demas metodos de registro:**
   - `registerSaleAndInvoice()` - usar nuevo servicio para sale+inventory, invoice sigue igual
   - `registerCashSale()` - idem
   - `registerCashSaleNoInvoice()` - idem

---

## Resumen de archivos

| # | Archivo | Accion | Riesgo |
|---|---------|--------|--------|
| 1 | `service/finances/SequenceGeneratorService.java` | **Nuevo** | Ninguno |
| 2 | `service/finances/SequenceGeneratorServiceBean.java` | **Nuevo** | Ninguno |
| 3 | `service/customers/SaleTransactionService.java` | **Nuevo** | Ninguno |
| 4 | `service/customers/SaleTransactionServiceBean.java` | **Nuevo** | Ninguno |
| 5 | `action/customers/SalesAction.java` | **Modificar** | Bajo - solo cambia a que servicio llama |

**Codigo legacy intacto (0 cambios):** `FinancesPkGeneratorServiceBean`, `SaleServiceBean`, `SaleService`, `InventoryServiceBean`, `InventoryService`, todos los demas servicios y actions.

---

## Migracion futura (opcional, no parte de este fix)

Cuando otros modulos quieran migrar del viejo `getNextSeq()`:
- Solo necesitan inyectar `sequenceGeneratorService` y reemplazar la llamada
- Sin cambios en BD, sin romper nada
- Puede hacerse modulo por modulo, sin prisa

### Callers actuales de getNextSeq (para referencia futura)

| Caller | Metodo | Sequence name |
|--------|--------|---------------|
| `SalesAction` | `getNextNoTransByDocumentType()` | `SECUENCIAPEDIDO`, `VENTADIRECTA` |
| `VoucherAccoutingServiceBean` | `getNextNoTransByDocumentType()` | `voucher.getDocumentType()` (dinamico) |
| `VoucherAccoutingServiceBean` | `getNextNoTransTmpenc()` | `ASIENTO` |
| `WarehouseAccountEntryServiceBean` | `getNextNoTransByDocumentType()` | `TR` |
| `WarehouseAccountEntryServiceBean` | `getNextNoTransTmpenc()` | `ASIENTO` |
| `WarehouseServiceBean` | `getNextPK()` | `VALE` |
| `DischargeDocumentServiceBean` | `getNextPK()` | `VALE` |
| `AccountingRecordServiceBean` | `getNextPK()` | `VALE` |
| `PurchaseDocumentServiceBean` | `getNextPK()` | `VALE` |
| `PayableDocumentServiceBean` | `executeFunction()` | `VALE`, conciliacion |

---

## Verificacion

1. `ant clean explode` con `JAVA_HOME="C:/Program Files/Java/jdk1.8.0_311"`
2. Abrir ventas, dos sesiones de navegador con usuarios diferentes
3. Agregar el mismo producto en ambas sesiones
4. Click "Venta credito" -> "Aceptar" en ambas simultaneamente
5. Verificar: ambas ventas se registran sin error, inventario descontado correctamente
6. Verificar: flujos existentes (venta contado, venta con factura) siguen funcionando
