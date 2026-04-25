# Plan — Eliminar columnas `cu` y `ct` de `inv_articulos`

Rama: `feature/drop-cu-ct` (desde `feature/anulacion-oc-vales`).

## Contexto

En `inv_articulos` (entidad [ProductItem](../src/main/com/encens/khipus/model/warehouse/ProductItem.java)) conviven dos pares de columnas de costo:

| Par | Uso | Estado |
|---|---|---|
| `saldo_mon` + `costo_uni` | Saldo monetario y costo unitario promedio — correctos y coherentes | ✅ Conservar |
| `ct` + `cu` | Acumuladores paralelos con fórmulas distintas | ❌ Eliminar — producen valores inconsistentes/negativos |

Problemas conocidos de `cu` / `ct`:
- `ct` solo se actualiza en entradas y en consumos (tipo C), **no se descuenta en salidas genéricas** → queda inflado crecientemente.
- `cu = ct / stock` reproduce esa inflación y diverge del precio real de reposición.
- Varios flujos (reverso de OC, producción, ventas) tocan estos campos con reglas distintas, agravando la distorsión.
- Datos actuales ya tienen valores negativos y desalineados con `saldo_mon` / `costo_uni`.

**Objetivo**: eliminar ambas columnas y todas sus referencias, sin romper ningún flujo. Los cálculos correctos se siguen haciendo con `saldo_mon` (=`investmentAmount`) y `costo_uni` (=`unitCost`).

**Alcance**: solo `inv_articulos.cu` e `inv_articulos.ct`. La columna `cu` en `articulos_pedido` (entidad `ArticleOrder`) queda **fuera de scope** — decisión separada.

---

## Archivos afectados

### Java — Entidad
1. [ProductItem.java](../src/main/com/encens/khipus/model/warehouse/ProductItem.java) — quitar campos `cu` y `ct`, sus `@Column`, getters y setters.

### Java — Servicios (escritura / lectura de cu/ct)
2. [ApprovalWarehouseVoucherServiceBean.java](../src/main/com/encens/khipus/service/warehouse/ApprovalWarehouseVoucherServiceBean.java) — `updateProductItemInformationForInputs` (líneas ~1101-1125) y `updateProductItemInformationForOutputs` (~1143-1183): quitar todo `set/getCu` y `set/getCt`, conservar la lógica sobre `investmentAmount` y `unitCost`.
3. [ReverseInventoryServiceBean.java](../src/main/com/encens/khipus/service/warehouse/ReverseInventoryServiceBean.java) — `reverseProductItemCost` (líneas ~110-160): quitar recálculo de `cu` y actualización de `ct`.
4. [ProductItemServiceBean.java](../src/main/com/encens/khipus/service/warehouse/ProductItemServiceBean.java) — remover los `setCu(ZERO)` / `setCt(ZERO)` en creación de nuevo producto (~líneas 62, 64).
5. [InventoryServiceBean.java](../src/main/com/encens/khipus/service/warehouse/InventoryServiceBean.java) — `increaseProductItemAmount` (~líneas 146-148): quitar lógica de `cu`/`ct`, mantener la de `investmentAmount`.
6. [XProductionPlanServiceBean.java](../src/main/com/encens/khipus/service/xproduction/XProductionPlanServiceBean.java) — `addProductQuantity` / `removeProductQuantity` (~116-138): quitar cálculo de `ct`, evaluar si se usaba `cu` para algo crítico.
7. [ProductionPlanServiceBean.java](../src/main/com/encens/khipus/service/production/ProductionPlanServiceBean.java) — mismo patrón que XProduction.
8. [SaleServiceBean.java](../src/main/com/encens/khipus/service/customers/SaleServiceBean.java) — `addArticleQuantity` / `removeArticleQuantity` (~85-109): quitar actualizaciones de `ct`.

### Java — Acciones (lectura de cu)
9. [WarehouseVoucherCreateAction.java](../src/main/com/encens/khipus/action/warehouse/WarehouseVoucherCreateAction.java) — líneas 160-161 usan `productItemFrom.getCu()` para setear `MovementDetail`. **Reemplazo**: usar `productItemFrom.getUnitCost()` (o el campo correcto según contexto — se valida caso por caso).
10. [WarehousePurchaseOrderDetailListCreateAction.java](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderDetailListCreateAction.java) — línea 94, misma sustitución.
11. [WarehousePurchaseOrderDetailAction.java](../src/main/com/encens/khipus/action/warehouse/WarehousePurchaseOrderDetailAction.java) — línea 358, misma sustitución.

### UI
12. [inventory.xhtml](../view/warehouse/inventory.xhtml) — línea 174 renderiza `#{inventoryDetailItem.productItem.cu}`. Remover esa celda/columna.

### Reporte "Costo Unitario" — eliminación completa (Opción 2)

El reporte ya está comentado/oculto en el menú. Se elimina en vez de mantenerlo zombie.

13. **Borrar** [ProductItemCostUnitReportAction.java](../src/main/com/encens/khipus/action/warehouse/reports/ProductItemCostUnitReportAction.java).
14. **Borrar** [view/warehouse/productItemCostUnitReport.xhtml](../view/warehouse/productItemCostUnitReport.xhtml).
15. **Borrar** [view/warehouse/reports/productItemCostUnitReport.jrxml](../view/warehouse/reports/productItemCostUnitReport.jrxml) y [productItemCostUnitReportNeto.jrxml](../view/warehouse/reports/productItemCostUnitReportNeto.jrxml).
16. **Migrar las 5 referencias cruzadas** de `productItemCostUnitReportAction.executorUnit` en otros reportes — cada uno tiene su propio action de reporte:
    - [generateCostOfSalesByWarehouse.xhtml](../view/accounting/generateCostOfSalesByWarehouse.xhtml)
    - [extendedInventoryReport.xhtml](../view/warehouse/extendedInventoryReport.xhtml)
    - [itemDestinationReport.xhtml](../view/warehouse/itemDestinationReport.xhtml)
    - [productInventoryGroupedReport.xhtml](../view/warehouse/productInventoryGroupedReport.xhtml)
    - [productInventoryReport.xhtml](../view/warehouse/productInventoryReport.xhtml)
17. **Limpiar referencias al permiso `PRODUCTITEMCOSTUNITREPORT`**:
    - Líneas comentadas en [menu.xhtml](../view/layout/menu.xhtml) (líneas ~361-368 y ~2117-2124).
    - Otras coincidencias (línea ~1992 de `menu.xhtml`).
    - Clave i18n `Functionality.reports.finances.productItemCostUnitReport` en `messages_app.properties`.
    - Fila en tabla `funcionalidad` (si existe en DB) — se limpia con una sentencia SQL opcional en la migración (no crítico, el permiso queda huérfano sin uso pero no rompe nada).

### SQL
15. **Migración nueva** `query/query_v6.0.71.sql`: `ALTER TABLE inv_articulos DROP COLUMN cu, DROP COLUMN ct;`

---

## Secuencia de implementación (un commit por fase)

### Fase A — Preparación y auditoría de datos

- **A.1** — Commit del plan (este documento).
- **A.2** — Query diagnóstica antes de la migración para documentar qué tan desalineados están los datos hoy:
  ```sql
  SELECT
    SUM(CASE WHEN cu  < 0 THEN 1 ELSE 0 END) AS cu_negativos,
    SUM(CASE WHEN ct  < 0 THEN 1 ELSE 0 END) AS ct_negativos,
    SUM(CASE WHEN cu IS NULL THEN 1 ELSE 0 END) AS cu_nulos,
    SUM(CASE WHEN ct IS NULL THEN 1 ELSE 0 END) AS ct_nulos,
    COUNT(*) AS total
  FROM inv_articulos;
  ```
  Sólo informativo; no se guarda nada.

### Fase B — Limpieza Java (lectores de cu)

Empezamos por los callers que **leen** `cu`, reemplazando por el valor correcto (`unitCost`). Así, cuando eliminemos la entidad, no quedan compile errors en lugares que leían.

- **B.1** — `WarehouseVoucherCreateAction`, `WarehousePurchaseOrderDetailListCreateAction`, `WarehousePurchaseOrderDetailAction`: reemplazar `productItem.getCu()` por `productItem.getUnitCost()`. Verificar caso por caso que tenga sentido funcional.
- **B.2** — `inventory.xhtml`: remover la columna/celda que mostraba `productItem.cu`.
- **B.3** — Compilar → los lectores restantes solo son el reporte muerto (se borra en Fase C).

### Fase C — Eliminar reporte "Costo Unitario" (muerto)

El reporte ya está comentado/oculto en el menú principal. Opción 2 del usuario: borrarlo completo.

- **C.1** — Migrar las 5 referencias cruzadas de `productItemCostUnitReportAction.executorUnit`. Cada uno de los 5 xhtml tiene su propio action del reporte; reemplazamos la referencia por el action propio de cada uno.
- **C.2** — Borrar `ProductItemCostUnitReportAction.java`, `productItemCostUnitReport.xhtml`, `productItemCostUnitReport.jrxml`, `productItemCostUnitReportNeto.jrxml`.
- **C.3** — Quitar del `menu.xhtml` las líneas comentadas (~361-368 y ~2117-2124) y la línea 1992 con `s:hasPermission('PRODUCTITEMCOSTUNITREPORT','VIEW')`.
- **C.4** — Quitar la clave i18n `Functionality.reports.finances.productItemCostUnitReport` de `messages_app.properties`.
- **C.5** — Agregar a la migración SQL un `DELETE` de la funcionalidad y su `derechoacceso` (si existe en DB dev).
- **C.6** — Compilar → 0 errores.

### Fase D — Limpieza Java (escritores de cu/ct)

- **D.1** — `ApprovalWarehouseVoucherServiceBean.updateProductItemInformationForInputs`: eliminar cálculo de `newCU`, `newCTAmount`; eliminar `setCu` y `setCt`. La rama `sumUnitaryBalances == 0` mantiene el reset de `investmentAmount` (sin tocar cu/ct).
- **D.2** — `ApprovalWarehouseVoucherServiceBean.updateProductItemInformationForOutputs`: quitar la actualización de `ct` en el caso `isConsumption()`.
- **D.3** — `ReverseInventoryServiceBean.reverseProductItemCost`: eliminar manipulación de `ct` y recálculo de `cu`. Mantener la lógica sobre `investmentAmount` y `unitCost`.
- **D.4** — `ProductItemServiceBean`: quitar `setCu(ZERO)` y `setCt(ZERO)` en creación.
- **D.5** — `InventoryServiceBean.increaseProductItemAmount`: simplificar, mantener `investmentAmount` + `unitCost`.
- **D.6** — `XProductionPlanServiceBean`, `ProductionPlanServiceBean`, `SaleServiceBean`: quitar actualizaciones de `ct` (eran acumuladores paralelos — validar antes de suprimir).
- **D.7** — Compilar → no debería quedar ningún `getCu/setCu/getCt/setCt` sobre `ProductItem`.

### Fase E — Entidad

- **E.1** — `ProductItem.java`: eliminar campos `cu`, `ct`, sus `@Column`, getters y setters.
- **E.2** — Compilar → 0 errores. Si aparece alguna referencia olvidada, corregirla aquí.

### Fase F — Migración DB

- **F.1** — (Opcional) Backup preventivo antes del DROP:
  ```sql
  CREATE TABLE inv_articulos_cu_ct_backup AS
    SELECT cod_art, cu, ct FROM inv_articulos;
  ```
- **F.2** — Crear `query/query_v6.0.71.sql`:
  ```sql
  -- v6.0.71 :: eliminar columnas cu y ct de inv_articulos y limpiar
  --            el reporte muerto productItemCostUnitReport.
  alter table inv_articulos drop column cu;
  alter table inv_articulos drop column ct;

  -- Limpieza del permiso del reporte eliminado (si existe en la DB)
  delete from derechoacceso where idfuncionalidad
      in (select idfuncionalidad from funcionalidad
          where codigo = 'PRODUCTITEMCOSTUNITREPORT');
  delete from funcionalidad where codigo = 'PRODUCTITEMCOSTUNITREPORT';
  ```
- **F.3** — Ejecutar en DB dev y verificar que `SHOW COLUMNS FROM inv_articulos;` ya no los trae.

### Fase G — Validación funcional

Probar los flujos que tocan costo:

- **G.1** — Crear un vale de RECEPCION, aprobarlo. Verificar `saldo_mon` y `costo_uni` actualizados correctamente.
- **G.2** — Crear un vale de EGRESO, aprobar. Verificar que `costo_uni` se preserva y `saldo_mon` baja.
- **G.3** — Anular un vale aprobado. Verificar valores como hicimos en Fase 2/3 del plan de anulación.
- **G.4** — Anular una OC en estado FIN o LIQ. Verificar reversión completa del costo.
- **G.5** — Pantalla de inventario (`inventory.xhtml`) — verificar que carga sin errores.
- **G.6** — Abrir los 5 reportes que antes referenciaban `productItemCostUnitReportAction.executorUnit` (extendedInventoryReport, itemDestinationReport, productInventoryReport, productInventoryGroupedReport, generateCostOfSalesByWarehouse) y confirmar que el filtro de unidad ejecutora sigue funcionando.
- **G.7** — Registrar una venta / producción — verificar que los flujos afectados (Sale, ProductionPlan, XProductionPlan) siguen funcionando.

---

## Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Algún flujo desconocido lee `productItem.cu` / `ct` | Compilación con JDK 1.8 después de quitar la entidad obliga a que todo el código compile; si falta refactor, el compilador lo avisa. |
| Reportes Jasper con `<field name="cu">` que no detectamos | Validar en Fase F.5 y ajustar `.jrxml` si aplica. |
| Consumidores externos (procesos programados, otros sistemas) que leen la DB directo | Baja probabilidad en este setup; si existieran, se revisan con el usuario antes de la migración. |
| `ct` tenía un rol en producción/ventas que no entendemos del todo | Revisar commits de `XProductionPlanServiceBean.addProductQuantity` y equivalentes antes de suprimir. Si no impacta cálculos visibles, quitar sin miedo. Si impacta, proponer reemplazo con `investmentAmount`. |
| Rollback necesario | La migración solo hace DROP; si hay que restaurar, se recompone desde `saldo_mon` / `costo_uni` + recálculo histórico. Para mayor seguridad, hacer backup antes de ejecutar E.1. |

---

## Abierto para decisión

- **¿Eliminamos también la `cu` de `articulos_pedido` (entidad `ArticleOrder`)?** Está fuera de este plan, pero presenta los mismos síntomas. Si querés incluirla, se agrega una Fase G con el mismo patrón (Java refs, migración).
- **¿Backup de datos de cu/ct antes de la migración?** Una vez droppeadas no hay vuelta atrás; si querés poder recomponer manualmente, hacer un `CREATE TABLE inv_articulos_cu_ct_backup AS SELECT cod_art, cu, ct FROM inv_articulos;` antes del DROP.
