# XProduction — Órdenes de producción: líneas, templates y cálculos

Documento de referencia funcional/técnica de las órdenes de producción del módulo
**XProduction** (`xpr_produccion`). Cubre los tres comportamientos según el **tipo de
línea de producción**: **ULEXITA**, **BARITINA** y **General** (otras líneas), la
mecánica de **templates**, y **todas las fórmulas de cálculo**.

> Alcance: pantalla de orden de producción (`view/xproduction/production.xhtml`),
> acción `XProductionAction`, capa de cálculo `XProductionUlexitaCalc` y servicios
> `XProductionUlexitaService(Bean)` / `XProductionBaritinaService(Bean)`.
> No cubre el plan de producción ni la generación de los reportes diarios (ver
> `docs/xproduction_ulexita_daily_report_spec.md` para el reporte ULEXITA).

---

## 1. Arquitectura: líneas de producción y templates

### 1.1 Discriminador `report_template_code`

Cada **línea de producción** (`xpr_linea`, entidad `ProductionLine`) tiene una columna
discriminadora `report_template_code` que define su **plantilla especializada**:

| `report_template_code` | Tipo (`ProductionLineType`) | Comportamiento |
|---|---|---|
| `ULEXITA` | `ProductionLineType.ULEXITA` | Flujo común + panel ULEXITA (proceso/laboratorio) + cálculos propios + snapshots |
| `BARITINA` | `ProductionLineType.BARITINA` | Flujo común + distribución de MP por zonas productivas |
| `NULL` / desconocido | `null` → **General** | Solo el flujo común (Formulación, Insumos, Materiales, Productos Terminados) |

El tipo se resuelve **por código**, no por cadenas hardcodeadas, mediante el enum
[`ProductionLineType`](../src/main/com/encens/khipus/model/xproduction/ProductionLineType.java):

```java
public ProductionLineType getLineType()    { return ProductionLineType.fromCode(reportTemplateCode); }
public boolean isUlexitaTemplate()         { return ProductionLineType.ULEXITA == getLineType(); }
public boolean isBaritinaTemplate()        { return ProductionLineType.BARITINA == getLineType(); }
```

`fromCode(null)` o un código desconocido retorna `null` ⇒ la línea es **General**.

### 1.2 Configuración por línea (campos de `ProductionLine`)

Solo la línea **ULEXITA** usa los campos de configuración de artículos; el resto los deja nulos:

| Campo | Uso |
|---|---|
| `codArtMpPrincipal` | cod_art de la materia prima principal (ULEXITA) |
| `codArtPtA` / `codArtPtB` | cod_art de los productos terminados clasificación A / B |
| `codArtDiluyBent` | cod_art del diluyente bentonita |
| `codArtDiluyCaolin` | cod_art del diluyente caolín |
| `mermaFactor` | factor de merma (default `1.03` si nulo o ≤ 0) |

### 1.3 Cómo activa la UI cada template

La vista muestra/oculta paneles y pestañas según el tipo
(`xproductionAction.ulexitaTemplate` / `baritinaTemplate`):

- **General**: Insumos, Materiales, Productos Terminados, Mano de Obra.
- **ULEXITA**: lo anterior + panel "Datos Calculados" + pestaña "Datos del Proceso"
  (Datos de Producción y Datos de Laboratorio).
- **BARITINA**: lo anterior + "Resumen Baritina" + pestaña de distribución por zonas.

### 1.4 Tablas satélite (1‑a‑1 con `xpr_produccion`)

| Tabla | Para | Contenido |
|---|---|---|
| `xpr_produccion_ulexita` | ULEXITA | inputs de proceso/lab + snapshots de los cálculos al aprobar |
| `xpr_produccion_baritina` | BARITINA | uso MP (TN), PT (TN), turnos, observación |
| `xpr_produccion_baritina_zona` | BARITINA | N filas: zona productiva, porcentaje, cantidad (TN) |

Una línea **General** no tiene tabla satélite.

---

## 2. Flujo común (aplica a TODAS las líneas)

Toda orden — sea ULEXITA, BARITINA o General — comparte el costeo base de
`XProductionAction`. Convención: importes con escala interna 6, redondeo final 2.

### 2.1 Costo unitario de un insumo — `getSupplyUnitCost`

```
si el insumo tiene fórmula y esa fórmula tiene "segunda fórmula" (insumo compuesto):
    unitCost = calculateCost_compoundSupply(insumo)
si no:
    unitCost = productItem.unitCost   (costo promedio del artículo en inventario)
```

### 2.2 Insumo compuesto — `calculateCost_compoundSupply`

Para un insumo que a su vez se produce con una **segunda formulación**, su costo
unitario se deriva de los ingredientes de esa fórmula:

```
para cada ingrediente i de la segunda fórmula:
    cantidad_i = cantidad_insumo * qtyFormulacion_i / formulacion.totalEquivalent
    costo_i    = cantidad_i * unitCost_i
costoTotal = Σ costo_i
costo unitario = costoTotal / cantidad_insumo
```

### 2.3 Costo total de la orden — `calculateTotalCost`

```
costoTotal = Σ(insumos:  cantidad * unitCost) + Σ(materiales: cantidad * unitCost)
```
Redondeo a 2 decimales. Es el valor mostrado como **"Costo Total"**.

### 2.4 Materia prima usada — `calculateRawMaterial`

```
materiaPrima = Σ cantidad de insumos con fórmula marcados inputDefault
             + Σ materia prima (leche cruda, ID_ART_RAW_MILK) de insumos compuestos
```
Redondeo a 2 decimales. Es el valor **"M.P. Usada"**.
(En ULEXITA, esta es la cantidad del insumo de MP principal; ver §3.5 syncMpFromConsumo.)

### 2.5 Recalcular insumos según fórmula — `recalculateSupplies`

Botón "Recalcular". Ajusta la cantidad de los insumos **no default** en proporción a
la cantidad del insumo **default**, según las proporciones de la formulación:

```
para cada insumo no-default con fórmula:
    nuevaCantidad = qtyForm[insumo] * cantidad[default] / qtyForm[default]
```
Luego recalcula Costo Total y M.P. Usada.

### 2.6 Costeo por producto al APROBAR — `approve`

Al aprobar, el costo se distribuye entre los productos terminados en tres componentes:

**a) Costo A — asignación directa** (insumo/material asignado a un producto puntual,
`productionProduct != null`):
```
costA(producto) = Σ (cantidad * unitCost) de insumos/materiales asignados a ese producto
```

**b) Costo B — distribución por volumen** (insumo/material sin asignación,
`productionProduct == null`):
```
remainingCost = Σ (cantidad * unitCost) de insumos/materiales SIN asignar
volumenProducto = cantidad_producto * basicQuantity
volumenTotal    = Σ volumenProducto                         (calculateTotalVolume)
costB(producto) = remainingCost * (volumenProducto / volumenTotal)
```

**c) Costo C**: componente adicional preexistente (no se calcula en `approve`).

**Costo y costo unitario del producto** — `updateUnitCostProducts`:
```
cost(producto)     = costA + costB + costC
unitCost(producto) = cost / cantidad_producto
```

> Nota: desde la UI ya **no** se asigna a un producto puntual (columna "Afecta"
> retirada): todos los insumos/materiales quedan en `productionProduct == null`, por lo
> que el costo se reparte 100% por la vía **Costo B** (proporcional al volumen).

---

## 3. Línea ULEXITA

Capa de cálculo: [`XProductionUlexitaCalc`](../src/main/com/encens/khipus/service/xproduction/XProductionUlexitaCalc.java).
Tabla satélite de inputs/snapshots: `xpr_produccion_ulexita`.

### 3.1 Datos de entrada

**Laboratorio** (carga manual): `leyMpBentonita` (Ley MP con bentonita), `leyPt` (Ley PT).
**Proceso** (carga manual): `productoGranuladoTn`, `consumoReprocesoTn` (Consumo reproceso),
`reprocesoFinalTn` (Reproceso final), y overrides opcionales `diluyenteTotalTn`, `bentonitaPct`.
**Inventario de la orden**: insumos y productos terminados de la orden.
**Configuración de línea**: `codArtMpPrincipal`, `codArtPtA`, `codArtPtB`,
`codArtDiluyBent`, `codArtDiluyCaolin`, `mermaFactor` (default 1.03).

> Conversión de unidades: las cantidades en **KG** se convierten a **TN** dividiendo
> entre 1000 (constante `UNIT_KG`). Si la unidad ya es TN, no se convierte.

### 3.2 Derivados directos

| Magnitud | Fórmula |
|---|---|
| Consumo MP real | Σ insumos con `cod_art = codArtMpPrincipal` → TN |
| Diluyente total | override `diluyenteTotalTn`, o (bentonita + caolín insumos) → TN |
| Bentonita (qty) | Σ insumos con `cod_art = codArtDiluyBent` → TN |
| **Bentonita %** | override `bentonitaPct`, o `bentonitaQty / diluyenteTotal * 100` |
| **Caolín %** | `100 − bentonita%` |
| PT A | Σ productos con `cod_art = codArtPtA` → TN |
| PT B | Σ productos con `cod_art = codArtPtB` → TN |
| **PT total bueno** | `PtA + PtB` |

### 3.3 Indicadores

| Indicador | Fórmula | Excel |
|---|---|---|
| **Kpa** | `productoGranuladoTn / PtTotalBueno` | Y |
| **Kpm bentonita** | `leyPt / leyMpBentonita` | W |
| **Kpm merma** | `(Kpm_bentonita − 1) − diluyenteTotal / (PtTotalBueno · mermaFactor · Kpa) + 1` | X |
| **MERMA (TN)** | `(Kpm_merma − 1) · mermaFactor · Kpa · PtTotalBueno` | Z |
| **% MERMA** | `MERMA / (MERMA + productoGranulado)` | AA |
| **Ley MP recalculada** | `leyPt / Kpm_merma` | M |
| **Consumo MP (TN)** | `MERMA + Kpa · PtTotalBueno − consumoReproceso` | E |

> Nota histórica: "Ley MP recalculada" usaba antes `leyPt / Kpm_bentonita` y "Consumo MP"
> no restaba el reproceso. Ambas fórmulas fueron corregidas; los snapshots de órdenes ya
> aprobadas se migran con `query/query_v6.0.87_terdemol.sql`. La v6.0.87 solo corrigió el
> snapshot; el insumo MP y el costeo de las órdenes aprobadas se realinean al snapshot
> corregido con `query/query_v6.0.90_terdemol.sql` (cantidad de `5-ULEXITA`,
> `xpr_produccion.costototal`/`totalmp` y `costo`/`costouni` de cada PT).

Cualquier división por cero o input requerido nulo hace que el getter retorne `null`
(la UI/reporte lo interpretan como celda vacía).

### 3.4 Snapshots (inmutabilidad histórica)

- **Orden PENDIENTE**: todos los indicadores se **calculan en vivo** desde los inputs.
- **Orden APROBADA**: los indicadores se **leen de los snapshots** congelados en
  `xpr_produccion_ulexita` (columnas `*_snap`), grabados al aprobar por
  `persistSnapshots` (`useSnapshots() == true` cuando hay snapshot y la orden está aprobada).

Esto garantiza que el reporte histórico no cambie aunque luego se modifique inventario,
leyes o configuración. Re-guardar/re-aprobar regenera los snapshots.

### 3.5 Volcado al insumo de MP — `syncMpFromConsumo`

Al editar los datos de proceso/laboratorio o la cantidad de un PT (y mientras la orden
esté pendiente), el **Consumo MP (TN)** calculado se vuelca a la cantidad del **insumo de
materia prima por defecto** (el ingrediente marcado `inputDefault` en la formulación),
**respetando su unidad**:

```
consumoMostrado = round(ConsumoMpCalc, 2)                 // el valor visible (2 decimales)
si la unidad del insumo es KG:  cantidad = consumoMostrado * 1000
si no (TN):                     cantidad = consumoMostrado
```

Se ejecuta también en `create`, `update` y `approve` antes de persistir, para que la
cantidad guardada (y por ende Costo Total / M.P. Usada / snapshots) refleje el consumo.

---

## 4. Línea BARITINA

Tablas satélite: `xpr_produccion_baritina` (cabecera) y `xpr_produccion_baritina_zona`
(distribución). Lógica en `XProductionAction` (métodos `baritina*`).

### 4.1 Cálculo de Materia Prima desde el PT — `syncBaritinaMpFromPt`

Al ingresar la cantidad del **Producto Terminado principal** de la línea (el de
`cod_art = ProductionLine.codArtPtPrincipal`, configurado en el catálogo de Líneas), la
**Materia Prima por defecto** (insumo `inputDefault` de la formulación) se calcula como:

```
MP = PT · factor          (factor = ProductionLine.factorPtMp, configurable por línea)
```

El resultado **respeta la unidad** del insumo (la cantidad del PT se convierte a la unidad
de la MP: KG↔TN) y se redondea a **2 decimales** (el valor mostrado).

Ejemplo: PT = 24.000,00 KG · factor 1,02 → MP = 24.480,00 KG.

Es **no-op** si: la orden está aprobada, la línea no tiene `factorPtMp` (nulo o ≤ 0), o no
hay PT principal cargado (en ese caso se respeta la MP ingresada manualmente).

Se dispara en vivo al editar la cantidad del PT (vía `recalcOnPtChange`, que además recalcula
la distribución por zonas) y en `create` / `update` / `approve` antes de persistir.

Configuración (catálogo de Líneas de Producción → columnas en `xpr_linea`):
`cod_art_pt_principal` y `factor_pt_mp` (SQL `query/query_v6.0.88_terdemol.sql`).

### 4.2 Resumen Baritina

| Magnitud | Fórmula |
|---|---|
| **Uso MP Baritina (TN)** | Σ cantidad (KG) de insumos marcados `inputDefault` / 1000 |
| **Baritina PT (TN)** | Σ cantidad (KG) de los productos terminados de la orden / 1000 |
| **Suma de proporciones (%)** | Σ porcentajes de todas las zonas (debe ser 100) |

### 4.3 Distribución por zona productiva

Cada fila asigna un porcentaje del uso de MP a una **zona productiva** (maestro
`zonaproductiva` del módulo de acopio: SACACA, HUALLATARI, PATOYU, 3 CRUCES, ANZALDO,
VISCACHANI, etc.). Recalculo (`recalcBaritinaZonas`):

```
cantidad_zona (TN) = UsoMpBaritina · porcentaje / 100
```

Se dispara al editar la cantidad del insumo, el porcentaje de una zona o al agregar/quitar
una zona.

### 4.4 Validación al guardar/aprobar — `validateBaritina`

- Cada fila debe tener **zona seleccionada**.
- La **suma de porcentajes** debe ser **100%** (tolerancia 0.01).
- Sin zonas registradas no bloquea (la orden puede ser parcial).

La cabecera además guarda **turnos producidos** (`turnos`) y **observación**.

---

## 5. Línea General (otras)

No tiene tabla satélite ni cálculos propios: usa **solo el flujo común** de la §2
(Formulación → Insumos/Materiales → Costo Total / M.P. Usada → costeo por producto al
aprobar). Ejemplo: línea `LP-MOL` (molienda con formulación RUMIFOS).

---

## 6. Resumen por tipo de línea

| Aspecto | General | ULEXITA | BARITINA |
|---|---|---|---|
| `report_template_code` | NULL | `ULEXITA` | `BARITINA` |
| Tabla satélite | — | `xpr_produccion_ulexita` | `xpr_produccion_baritina(_zona)` |
| Paneles extra | — | Datos Calculados + Proceso/Lab | Resumen + zonas |
| Cálculos propios | — | Kpa, Kpm, MERMA, leyes, Consumo MP | Uso MP, PT, distribución por zona |
| Snapshots históricos | — | Sí (al aprobar) | — |
| Costeo base (§2) | Sí | Sí | Sí |

---

## 7. Cómo agregar una nueva línea especializada

1. Declarar un valor nuevo en el enum `ProductionLineType` (código + resourceKey).
2. Crear su tabla satélite (1‑a‑1 con `xpr_produccion`) vía SQL aditivo, si necesita
   datos propios.
3. Agregar su panel/pestaña en `production.xhtml` condicionado al nuevo
   `xproductionAction.<tipo>Template`.
4. Implementar su capa de cálculo (análoga a `XProductionUlexitaCalc`) y los métodos de
   carga/persistencia en `XProductionAction`.
5. Dar de alta la línea por pantalla (Líneas de Producción) con su Template de reporte.

No se hardcodean cadenas de tipo en acciones ni vistas: todo se resuelve por
`report_template_code` → `ProductionLineType`.

---

## 8. Referencias de código

| Tema | Archivo |
|---|---|
| Tipo de línea / template | `model/xproduction/ProductionLineType.java`, `ProductionLine.java` |
| Costeo común / orden | `action/xproduction/XProductionAction.java` |
| Cálculos ULEXITA | `service/xproduction/XProductionUlexitaCalc.java` |
| Snapshots ULEXITA | `service/xproduction/XProductionUlexitaServiceBean.java` |
| Distribución BARITINA | `XProductionAction.java` (métodos `baritina*`), `XProductionBaritinaServiceBean.java` |
| Vista de la orden | `view/xproduction/production.xhtml` |
| Reporte diario ULEXITA | `docs/xproduction_ulexita_daily_report_spec.md` |
| Migraciones recientes | `query/query_v6.0.76/77/86/87/90_terdemol.sql` |
