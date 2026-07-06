# XProduction — Saldos de Almacén (recalculados desde el origen)

Documento de referencia funcional/técnica de la vista **Produccion > Saldos** del módulo
**XProduction**. Muestra el **saldo actual** de los artículos de un almacén de **Materia
Prima (MP)** o **Producto Terminado (PT)**, **recalculado desde el origen** de todos los
movimientos hasta el momento de la consulta.

> **Premisa clave:** el saldo **NO se lee** de tablas de inventario consolidado
> (`inv_inventario`). Se **recalcula** sumando/restando cada movimiento desde su tabla de
> origen. Más adelante se implementará un cierre mensual que aporte saldos iniciales; por
> ahora se recorre todo el histórico (estado `ANL`/anulado excluido).

> **Alcance:** vista [`view/xproduction/warehouseBalance.xhtml`](../view/xproduction/warehouseBalance.xhtml),
> acción [`XProductionBalanceAction`](../src/main/com/encens/khipus/action/xproduction/XProductionBalanceAction.java),
> servicio [`XProductionBalanceService(Bean)`](../src/main/com/encens/khipus/service/xproduction/XProductionBalanceServiceBean.java)
> y los DTO `WarehouseBalanceRow` / `BalanceGroup`.

---

## 1. Ubicación, acceso y navegación

- **Menú:** pestaña **XProduction** → enlace **"Saldos"**, ubicado después de *Procesos*
  (`view/layout/menu.xhtml`).
- **Permiso propio:** funcionalidad **`XPRODUCTION_BALANCE`** (opción `VIEW`). El enlace de
  menú se renderiza con `s:hasPermission('XPRODUCTION_BALANCE','VIEW')`.
  - `nombrerecurso` en menú: `menu.xproduction.balance` → **"Saldos"**.
  - `nombrerecurso` en panel de permisos: `Functionality.xproduction.balance` → **"Saldos MP/PT"**.
- **Vista:** `/xproduction/warehouseBalance.xhtml`, ancho de cuerpo 60% centrado
  (`bodyWidth=60%`, `bodyAlign=center`), pestaña activa `xproduction`.

---

## 2. Flujo de la vista

1. **Panel de filtro:** un `selectOneMenu` con los almacenes elegibles (solo MP y PT) y un
   botón **"Actualizar"** (`a4j:commandButton`, ajax) que ejecuta `refresh()` y re-renderiza
   `balancePanel`.
2. **Tabla de resultados:** lista los productos del almacén seleccionado **agrupados por
   Subgrupo**, ordenados por subgrupo y luego por nombre (A-Z). Columnas: **Código (15%)**,
   **Producto (55%)**, **Unidad (12%)** y **Saldo (18%, derecha)**.
3. La tabla usa los componentes estándar del sistema: `rich:dataTable` (var=`group`) con un
   `rich:column colspan="4"` como encabezado de grupo (subgrupo) y un `rich:subTable`
   (var=`row`) para las filas de productos. Hover por fila vía CSS
   `.balanceTable .rich-subtable-row:hover td`.

### Almacenes elegibles — `findBalanceWarehouses()`

```sql
select w from Warehouse w
where w.warehouseType in (RAW_MATERIAL, FINISHED_GOODS) and w.state <> BLO
order by w.name
```

Solo almacenes de tipo **Materia Prima** o **Producto Terminado**, no bloqueados.

---

## 3. Cálculo del saldo — `computeBalances(companyNumber, warehouseCode, date)`

> **Corte por fecha:** el saldo se calcula **hasta `date`** (inclusive). El filtro se
> abre en el panel con un `rich:calendar` (`XProductionBalanceAction.balanceDate`, por
> defecto la **fecha actual**) y cada fuente cuenta solo los movimientos con fecha `<=` al
> **fin del día** seleccionado (`endOfDay`, para incluir fechas guardadas con hora). Fechas
> por fuente: `MovementDetail.movementDetailDate` (2), `CollectMaterial.date` (3) y, para
> producción (4/5/6), `productionPlan.date` (la **fecha del plan** de producción, mismo
> criterio que el reporte de insumos `findProductionInputsByDates`); las órdenes sin plan
> se cuentan siempre para no alterar los totales actuales.


El saldo de cada artículo se arma en memoria a partir de **un query de productos** + **cinco
queries de agregación (`GROUP BY`)** que se aplican sobre las filas. Esto evita recorrer
movimiento por movimiento por producto.

### 3.1 Universo de productos (paso 1)

```sql
select p, sg from ProductItem p left join p.subGroup sg
where p.warehouseCode = :wc and p.companyNumber = :cn and p.state = VIG
order by sg.name, p.name
```

- **Solo artículos activos** (`ProductItemState.VIG`); se excluyen los **bloqueados** (`BLO`).
- `LEFT JOIN` al subgrupo para incluir artículos sin subgrupo (`"(Sin subgrupo)"`).
- Cada producto entra al `LinkedHashMap` con **saldo 0** (se muestran aunque no tengan
  movimientos), preservando el orden subgrupo+nombre.

### 3.2 Las 6 fuentes de movimiento

Todas excluyen registros **anulados** (estado `<> ANL`). Las cantidades se atribuyen al
artículo por su código; los códigos ajenos al almacén consultado se ignoran.

| # | Fuente | Entidad / tabla | Efecto | Aplica a |
|---|---|---|---|---|
| 2 | **Kardex (vales E/S + despachos)** | `MovementDetail` (`inv_movdet`) | `E` = entrada (+), `S` = salida (−), por tipo | MP y PT |
| 3 | **Acopio de Materia Prima** | `CollectMaterial` (`acopiomp`/`acopio`) | entrada (+) `netWeight` | MP |
| 4 | **Producción: PT producido** | `XProductionProduct` (`xpr_producto`) | entrada (+) `quantity` | PT |
| 5 | **Producción: consumo de insumos** | `XSupply` (`xpr_insumo`) | salida (−) `quantity` | MP y PT* |
| 6 | **Reproceso ULEXITA** | `XProductionUlexita` (`xpr_produccion_ulexita`) | `Reproceso final` entrada (+), `Consumo reproceso` salida (−) | artículo configurado |

\* La fuente 5 incluye **PT usado como insumo**: un producto terminado puede consumirse en
otra orden; sigue viviendo en el almacén de PT pero se descuenta al usarse (transparente).

> **Despachos no se doble-cuentan:** los despachos generan `MovementDetail` (ver
> `DispatchVoucherServiceBean`), por lo que ya están contemplados en la fuente 2 y **no** se
> suman desde la tabla de despachos. Acopio (fuente 3) y órdenes de producción (4/5/6)
> **no** escriben en `inv_movdet`, por eso son fuentes independientes.

### 3.3 Fuente 6 en detalle — Reproceso ULEXITA

```sql
select u.codArtReprocFinal, sum(u.reprocesoFinalTn), sum(u.consumoReprocesoTn)
from XProductionUlexita u
where u.codArtReprocFinal is not null and u.production.state <> ANL
group by u.codArtReprocFinal
```

- El artículo destino es el **configurado en la orden** (`cod_art_reproc_final`, copiado
  desde la línea de producción — ver [[project_xproduction_line_types]]).
- **`Reproceso final (TN)`** → **entrada** al inventario del artículo configurado.
- **`Consumo reproceso (TN)`** → **salida** del mismo artículo (permite ver entradas y
  salidas al sacar un detallado del artículo).
- **Conversión de unidad:** los valores están en **TN**; si la unidad del artículo es
  **KG** se multiplican por **1000** (`tnToUnit`), de lo contrario se asumen en TN.
- Esta fuente nació de un caso real (artículo `2074 - ULEXITA PROCESADA` salía con saldo 0)
  porque el reproceso vive en la tabla satélite y no aparecía en las otras 5 fuentes.

---

## 4. Componentes

| Componente | Tipo | Rol |
|---|---|---|
| `XProductionBalanceAction` | `@Name @Scope(CONVERSATION)` | Filtro (`selectedWarehouse`), `refresh()`, `getGroups()` (agrupa la lista por subgrupo) |
| `XProductionBalanceService(Bean)` | `@Stateless @Name @AutoCreate` | `findBalanceWarehouses()` y `computeBalances()` |
| `WarehouseBalanceRow` | DTO (no entidad) | fila de saldo en memoria: code/name/measureCode/subGroup + `balance` con `add()`/`subtract()` |
| `BalanceGroup` | DTO (no entidad) | grupo de filas por subgrupo, para el encabezado de la tabla |

`getGroups()` recorre la lista ya ordenada y corta un nuevo `BalanceGroup` cada vez que
cambia el `subGroupCode` (agrupación por subgrupos consecutivos, sin reordenar).

---

## 5. Notas y supuestos

- **Cada artículo vive en un solo almacén** (`ProductItem.warehouseCode`); las fuentes que
  no traen almacén (acopio, producción, reproceso) se atribuyen al almacén del artículo y los
  códigos que no estén en el almacén consultado se descartan.
- **Solo se excluye `ANL`** (anulado); cualquier otro estado de documento se cuenta.
- **Acopio es independiente** y no genera vales.
- El saldo es un cálculo **al vuelo**; no se persiste. Un futuro **cierre mensual** aportará
  saldos iniciales para acotar el rango de recálculo.

---

## 6. Cómo extender (agregar una nueva fuente)

1. Identificar la entidad/tabla de origen y su campo de estado (excluir `ANL`).
2. Agregar un `GROUP BY` por `productItemCode` en `computeBalances()`.
3. Aplicar con `applySums(rows, data, add)` (entrada `true` / salida `false`), o lógica
   propia si requiere conversión de unidad (ver fuente 6 / `tnToUnit`).
4. Documentar la fuente en la tabla de la sección 3.2.
