# Optimización del Reporte de Inventario Extendido — Caso de estudio

Caso real de optimización aplicado a [`ExtendedInventoryReportAction`](../src/main/com/encens/khipus/action/warehouse/reports/ExtendedInventoryReportAction.java) que llevó el tiempo de generación de **4m 45s → 32 segundos** (≈ 9× más rápido) sobre el mismo escenario de prueba: Almacén 2 (MATERIALES E INSUMOS), 01/01/2025–27/04/2025, sin filtro de grupo, ~1.670 artículos y ~2.975 movimientos.

Este documento sirve de referencia para diagnosticar y optimizar reportes similares (kardex, inventario, ventas por rango de fechas).

---

## Resultados medidos

| Estado | Tiempo | Mejora acumulada |
|---|---|---|
| Original | 4m 45s | — |
| + Fase 1 (filtro pre-resuelto + cache de subgrupos) | (sin cambio significativo en escenario sin filtro) | 0% |
| + Fase 2 (push-down de `warehouseCode` a SQL) | (sin cambio sensible — la DB seguía haciendo full scans) | 0% |
| + Fase 3-A (índices) | 2m 50s | 41% |
| + Fase 3-B (`JOIN FETCH`) | **32s** | **89%** |

**Lección clave**: el código Java ya estaba bien escrito (cálculos correctos, uso de `HashMap` para indexar). La lentitud venía de **dos problemas estructurales acumulados**: ausencia total de índices secundarios y N+1 lazy-loads en el bucle principal.

---

## Diagnóstico — cómo se identificó cada cuello de botella

### 1. Filtrado tardío (Fase 1)

**Síntoma**: con filtro por grupo, el reporte tardaba lo mismo que sin filtro.

**Causa**: el filtro por `group`/`subGroup` se aplicaba en memoria **después** de calcular movimientos de todo el almacén:

```java
// Antes
List<ArticleReportData> reportData = calculateExtendedData();   // procesa TODO el almacen
if (group != null) {                                            // luego descarta 80-90%
    Set<String> groupCodes = ...;
    for (ArticleReportData ard : reportData) { if (...) filtered.add(ard); }
}
```

**Fix**: resolver el `Set<String> productItemCodesFilter` ANTES, pasarlo a `calculateExtendedData(filter)` y hacer `if (filter != null && !filter.contains(code)) continue;` en cada bucle.

### 2. N+1 lazy-load sobre `productItem.subGroup` (Fase 1-C)

**Síntoma**: el bucle final que arma `ArticleReportData` accedía a `ip.getProductItem().getSubGroup().getName()` por cada artículo. `subGroup` está mapeado `FetchType.LAZY` → 1 query por artículo.

**Fix**: una sola query `select sg from SubGroup sg`, indexar a `Map<"groupCode|subGroupCode", name>`, lookup por escalares (`groupCode`, `subGroupCode` son columnas, no asociaciones). Ver [`loadSubgroupNameMap`](../src/main/com/encens/khipus/action/warehouse/reports/ExtendedInventoryReportAction.java).

### 3. Queries sin filtro de almacén (Fase 2)

**Síntoma**: las queries `findProductionByDate`, `findCashSaleDetailList`, etc. NO filtraban por almacén — traían producción/ventas/acopio de todos los almacenes.

**Causa**: las named queries originales eran genéricas y compartidas por varios reportes.

**Fix**: agregar **overloads** (no modificar las originales) que joinean a `ProductItem` y filtran por `warehouseCode`:

```jpql
select pp from ProductionProduct pp
left join pp.productionPlan plan
join pp.productItem pi
where plan.date between :startDate and :endDate
  and pi.warehouseCode = :warehouseCode
```

Aplicado en: [`ProductionOrderServiceBean`](../src/main/com/encens/khipus/service/production/ProductionOrderServiceBean.java), [`CollectMaterialServiceBean`](../src/main/com/encens/khipus/service/production/CollectMaterialServiceBean.java), [`ArticleOrderServiceBean`](../src/main/com/encens/khipus/service/customers/ArticleOrderServiceBean.java).

### 4. Cero índices secundarios (Fase 3-A) — el más grande

**Síntoma decisivo**: en MySQL puro, la query principal del reporte tardaba **0.115 segundos**. Pero el reporte tardaba 170 segundos. Diferencia 1.478×.

**Diagnóstico con `EXPLAIN`**:

```
EXPLAIN SELECT md.* FROM inv_movdet md
  JOIN inv_mov m ON m.no_trans=md.no_trans AND m.no_cia=md.no_cia
  JOIN inv_vales v ON v.no_trans=m.no_trans AND v.no_cia=m.no_cia
  WHERE md.cod_alm='2' AND v.fecha BETWEEN ... AND v.estado='APR';

inv_vales:    type=ALL, rows=18,661  ← scan completo
inv_mov:      type=ALL, rows=18,920  ← scan completo + hash join
inv_movdet:   type=ALL, rows=24,685  ← scan completo + hash join
```

**Causa**: `inv_movdet`, `inv_mov`, `inv_articulos`, `inv_inicio`, `inv_periodo` sólo tenían PRIMARY KEY. Cero índices secundarios sobre `cod_alm`, `no_trans`, `fecha`, `estado`, etc.

**Fix**: ver [`query/query_v6.0.73.sql`](../query/query_v6.0.73.sql). 10 índices, todos compuestos por columnas usadas en `WHERE` y `JOIN` del kardex.

Después de los índices: `type=ref`, ~1 fila por lookup, full scans desaparecen.

### 5. N+1 lazy-load sobre `inventoryMovement.warehouseVoucher` (Fase 3-B) — el segundo más grande

**Síntoma**: tras los índices, el SQL puro corría en 0.115s pero el reporte aún tardaba 170s.

**Causa identificada**: el bucle de "Vales de movimiento" hace:

```java
for (MovementDetail md : movementDetailList) {                              // 2.975 vueltas
    ... md.getInventoryMovement().getWarehouseVoucher().getDate(), ...      // lazy load!
        md.getInventoryMovement().getDescription()                          // lazy load!
}
```

`inventoryMovement` y `warehouseVoucher` son `FetchType.LAZY`. Hibernate dedupe por sesión, así que en vez de 2.975 disparos hace ~2.312 lookups (transacciones únicas) × 2 saltos = **~4.600 round-trips JDBC**. Cada round-trip por JBoss + Seam + Hibernate cuesta 20–40 ms aunque el SQL en sí sea sub-ms. **4.600 × 30ms ≈ 140s.** Encajaba con la diferencia observada.

**Fix**: variante `findListMovementByWarehouseAndTypeFetch` con `LEFT JOIN FETCH`:

```jpql
select distinct movementDetail from MovementDetail movementDetail
left join fetch movementDetail.inventoryMovement im
left join fetch im.warehouseVoucher wv
where movementDetail.warehouseCode = :warehouseCode
  and wv.date between :startDate and :endDate
  and wv.state = :state
```

Una sola query trae detalles + movements + vouchers. La query original queda intacta para los demás reportes que NO tocan `inventoryMovement`/`warehouseVoucher` en el bucle (y por lo tanto no sufren el N+1).

Aplicado en [`MovementDetailServiceBean`](../src/main/com/encens/khipus/service/warehouse/MovementDetailServiceBean.java).

---

## Lecciones para futuros reportes

### Lección 1 — Empujar el filtro al `WHERE` antes de procesar

Si el usuario filtra por grupo / subgrupo / categoría, **resolver el set de IDs ANTES** y pasarlo como parámetro `IN` a las queries (o como filtro en memoria al inicio de cada bucle). No procesar todo el universo y luego descartar.

### Lección 2 — Cuidado con asociaciones `LAZY` accedidas en bucles

Cualquier `entidad.getRelacion().getCampo()` dentro de un `for` de N elementos = potencial N+1.

**Patrón seguro**:
- Si la relación se usa para **filtrar/agrupar** → cargar todo en un `Map` con UNA query y hacer lookup en memoria.
- Si la relación se usa **por fila** → usar `LEFT JOIN FETCH` (con `DISTINCT` si es `to-many`).

### Lección 3 — Medir SQL puro vs tiempo total

Ante cualquier reporte lento:

```sql
SET PROFILING = 1;
-- correr la query principal del reporte
SHOW PROFILES;
```

Si el SQL en sí está bajo 1 segundo y el reporte tarda minutos → la lentitud está en la capa Hibernate/Java (típicamente N+1 o lazy-load explosivo).

Si el SQL está sobre varios segundos → falta índice o el plan es subóptimo (correr `EXPLAIN`).

### Lección 4 — Crear `overloads`, no modificar named queries compartidas

Las named queries en entidades suelen ser usadas por varios reportes. Modificar la JPQL original = riesgo de romper otros flujos. **Patrón correcto**: agregar un nuevo método en el service con sufijo descriptivo (`...ByWarehouse`, `...Fetch`, etc.) y dejar la original intacta.

#### ¿Qué es un *overload*?

En Java, **dos o más métodos pueden tener el mismo nombre** mientras tengan **firma diferente** (distinta cantidad o tipo de parámetros). Eso es un *overload* / sobrecarga. El compilador decide cuál ejecutar según los parámetros que se le pasan al llamarla.

Ejemplo del proyecto — en [`ProductionOrderService`](../src/main/com/encens/khipus/service/production/ProductionOrderService.java) ahora coexisten:

```java
// Original — sin filtro de almacen (usada por ProductInventoryReportAction y KardexProductMovementAction)
List<ProductionProduct> findProductionByDate(Date startDate, Date endDate);

// Overload nuevo — agrega un parametro warehouseCode
List<ProductionProduct> findProductionByDate(Date startDate, Date endDate, String warehouseCode);
```

Llamadas:

```java
productionOrderService.findProductionByDate(start, end);          // version vieja: 2 parametros
productionOrderService.findProductionByDate(start, end, "2");     // version nueva: 3 parametros
```

#### ¿Por qué overloads en vez de modificar la original?

| Opción | Riesgo | Impacto |
|---|---|---|
| Modificar la original y agregar `warehouseCode` como parámetro obligatorio | Hay que tocar TODOS los callers — varios archivos extra, más testing | Alto riesgo de romper otros reportes |
| **Agregar un overload nuevo** y dejar la original intacta | El método nuevo sólo se usa donde lo necesito | Cero riesgo para los demás callers |

#### Aclaración sobre el sufijo `Fetch`

En [`MovementDetailService`](../src/main/com/encens/khipus/service/warehouse/MovementDetailService.java) usé otro patrón: en vez de overload (mismo nombre, distinta firma) creé un método con **nombre distinto** (`findListMovementByWarehouseAndTypeFetch`).

**Estrictamente no es un overload de Java** porque cambió el nombre. Es la misma idea conceptual ("método paralelo, alternativa de la original") pero con sufijo descriptivo (`Fetch`) para que al leer el código quede claro que esa variante hace algo extra (`JOIN FETCH` de asociaciones). Cuando la diferencia entre las dos variantes no es de parámetros sino de **comportamiento interno**, conviene un nombre distinto antes que un overload — el lector del código no debería tener que mirar la firma para entender la diferencia.

**Regla práctica**:
- ¿La diferencia es agregar/quitar un parámetro de filtro? → **overload** (mismo nombre).
- ¿La diferencia es estrategia interna (fetch eager, projection, paginación)? → **nombre distinto con sufijo descriptivo**.

### Lección 5 — Verificar índices al diseñar consultas con joins por fecha + dimensión

Toda tabla del kardex/movimientos debe tener al menos:
- Índice por `(cod_alm)` o `(cod_alm, fecha)` — para filtros por almacén/rango.
- Índice por `(no_cia, no_trans)` — para joins con vouchers.
- Índice por `(cod_art)` o `(no_cia, cod_art)` — para reportes por artículo.

Antes de crear un nuevo reporte, correr `SHOW INDEX FROM <tabla>` y agregar lo que falte.

### Lección 6 — Algunos `@ManyToOne` JPA son EAGER por defecto

`@ManyToOne(optional = true)` **sin** especificar `fetch=` es **EAGER** según el spec JPA. Cada carga de la entidad arrastra un JOIN extra. En reportes de alto volumen revisar las entidades involucradas y considerar marcar como `LAZY` los `@ManyToOne` opcionales que no se usen siempre. Ejemplo encontrado: `WarehouseVoucher.expenseCashAccount`.

---

## Checklist para optimizar un reporte lento

1. **Medir el SQL principal puro en MySQL** con `SET PROFILING = 1`. Comparar con tiempo total del reporte.
2. **`EXPLAIN`** sobre cada query del reporte → buscar `type=ALL`, `Using join buffer`, `rows=` muy alto.
3. **Crear índices faltantes** (tablas chicas: ALTER en segundos, sin riesgo).
4. **Identificar bucles sobre listas grandes** y revisar lazy-loads encadenados (`a.getB().getC()`).
5. **Pre-cachear catálogos pequeños** (subgrupos, grupos, unidades de medida) en un `Map` con UNA query.
6. **`JOIN FETCH`** las relaciones que se usan en el bucle, en una variante nueva del query.
7. **Push-down filtros** del usuario (warehouse, group, fecha) al `WHERE` SQL — no al postprocesamiento.
8. **Reiniciar JBoss completo** para validar — `ant explode` no recarga clases con `@Name`/`@Scope` en JBoss 5.

---

## Referencias

- Action: [`ExtendedInventoryReportAction`](../src/main/com/encens/khipus/action/warehouse/reports/ExtendedInventoryReportAction.java)
- Servicios afectados:
  - [`ProductionOrderServiceBean`](../src/main/com/encens/khipus/service/production/ProductionOrderServiceBean.java) — overloads con `warehouseCode`
  - [`CollectMaterialServiceBean`](../src/main/com/encens/khipus/service/production/CollectMaterialServiceBean.java)
  - [`ArticleOrderServiceBean`](../src/main/com/encens/khipus/service/customers/ArticleOrderServiceBean.java)
  - [`MovementDetailServiceBean`](../src/main/com/encens/khipus/service/warehouse/MovementDetailServiceBean.java) — variantes `*Fetch` con `JOIN FETCH`
- Índices SQL: [`query/query_v6.0.73.sql`](../query/query_v6.0.73.sql)
- Commits relacionados:
  - `56c7958b` — Fase 1 (filtro pre-resuelto + cache subgrupos)
  - `e58e812a` — Fase 2 (push-down `warehouseCode`)
  - `a882947d` — Fase 3-A (índices, v6.0.73)
  - `bbd5a247` — Fase 3-B (`JOIN FETCH`)
