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
12. [ProductItemCostUnitReportAction.java](../src/main/com/encens/khipus/action/warehouse/reports/ProductItemCostUnitReportAction.java) — línea 59 incluye `productItem.cu` en el SELECT de una JPQL. Quitar del select y del reporte.

### UI / Reportes
13. [inventory.xhtml](../view/warehouse/inventory.xhtml) — línea 174 renderiza `#{inventoryDetailItem.productItem.cu}`. Remover esa celda/columna.
14. `view/warehouse/reports/*.jrxml` — revisar los reportes que apuntan a `ProductItemCostUnitReportAction`. Si el `.jrxml` declara `<field name="cu">`, quitarlo (el agente de exploración no encontró coincidencia directa, pero conviene verificar durante la fase).

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

### Fase B — Limpieza Java (lecturas)

Empezamos por los callers que **leen** `cu`/`ct`, reemplazando por el valor correcto (`unitCost` donde aplica). Así, cuando eliminemos la entidad, no quedan compile errors en lugares que leían.

- **B.1** — `WarehouseVoucherCreateAction`, `WarehousePurchaseOrderDetailListCreateAction`, `WarehousePurchaseOrderDetailAction`: reemplazar `productItem.getCu()` por `productItem.getUnitCost()`. Verificar caso por caso que tenga sentido funcional.
- **B.2** — `ProductItemCostUnitReportAction`: quitar `productItem.cu` del SELECT JPQL. Ajustar el DTO/Object[] de destino. Actualizar el `.jrxml` si lo referenciaba.
- **B.3** — `inventory.xhtml`: remover la columna/celda que mostraba `productItem.cu`.
- **B.4** — Compilar → debería quedar todo lo que **solo lee** cu/ct sin refs.

### Fase C — Limpieza Java (escrituras)

- **C.1** — `ApprovalWarehouseVoucherServiceBean.updateProductItemInformationForInputs`: eliminar cálculo de `newCU`, `newCTAmount`; eliminar `setCu` y `setCt`. La rama `sumUnitaryBalances == 0` mantiene el reset de `investmentAmount` (sin tocar cu/ct).
- **C.2** — `ApprovalWarehouseVoucherServiceBean.updateProductItemInformationForOutputs`: quitar la actualización de `ct` en el caso `isConsumption()`.
- **C.3** — `ReverseInventoryServiceBean.reverseProductItemCost`: eliminar manipulación de `ct` y recálculo de `cu`. Mantener la lógica sobre `investmentAmount` y `unitCost`.
- **C.4** — `ProductItemServiceBean`: quitar `setCu(ZERO)` y `setCt(ZERO)` en creación.
- **C.5** — `InventoryServiceBean.increaseProductItemAmount`: simplificar, mantener `investmentAmount` + `unitCost`.
- **C.6** — `XProductionPlanServiceBean`, `ProductionPlanServiceBean`, `SaleServiceBean`: quitar actualizaciones de `ct` (eran acumuladores paralelos que no impactaban otra cosa — validar antes de suprimir).
- **C.7** — Compilar → no debería quedar ningún `getCu/setCu/getCt/setCt` sobre `ProductItem`.

### Fase D — Entidad

- **D.1** — `ProductItem.java`: eliminar campos `cu`, `ct`, sus `@Column`, getters y setters.
- **D.2** — Compilar → 0 errores. Si aparece alguna referencia olvidada, corregirla aquí.

### Fase E — Migración DB

- **E.1** — Crear `query/query_v6.0.71.sql`:
  ```sql
  -- v6.0.71 :: eliminar columnas cu y ct de inv_articulos
  -- Campos acumuladores desalineados con saldo_mon/costo_uni, reemplazados por los oficiales.
  alter table inv_articulos drop column cu;
  alter table inv_articulos drop column ct;
  ```
- **E.2** — Ejecutar en DB dev y verificar que `SHOW COLUMNS FROM inv_articulos;` ya no los trae.

### Fase F — Validación funcional

Probar los flujos que tocan costo:

- **F.1** — Crear un vale de RECEPCION, aprobarlo. Verificar `saldo_mon` y `costo_uni` actualizados correctamente.
- **F.2** — Crear un vale de EGRESO, aprobar. Verificar que `costo_uni` se preserva y `saldo_mon` baja.
- **F.3** — Anular un vale aprobado. Verificar valores como hicimos en Fase 2/3 del plan de anulación.
- **F.4** — Anular una OC en estado FIN. Verificar reversión completa del costo.
- **F.5** — Ejecutar el reporte "Costo Unitario" (`ProductItemCostUnitReportAction`) y ver que funciona sin la columna `cu`.
- **F.6** — Pantalla de inventario (`inventory.xhtml`) — verificar que carga sin errores.
- **F.7** — Registrar una venta / producción — verificar que los flujos afectados (Sale, ProductionPlan, XProductionPlan) siguen funcionando.

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
