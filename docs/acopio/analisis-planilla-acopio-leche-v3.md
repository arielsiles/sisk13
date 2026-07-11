# Análisis: Planillas de Acopio de Leche — v3 (planilla única + excedentes + precios con vigencia)

> **Versión 3 (autocontenida).** Reemplaza a v2 como documento de referencia.
> Para retomar el feature en el futuro **basta leer este v3**.
> v2 (`analisis-planilla-acopio-leche-v2.md`) y v1 se conservan como histórico del motor
> de cálculo previo (single-planilla, con checkboxes de días).
>
> Propósito: contexto completo y exacto para seguir haciendo cambios en generación,
> cálculos, reportes y configuración de precios del acopio de leche.
> Fecha de esta versión: 2026-07-09.

## Índice
1. [Qué cambió vs v2 (visión general)](#1-qué-cambió-vs-v2-visión-general)
2. [Concepto de negocio](#2-concepto-de-negocio)
3. [Configuración de precios (con vigencia)](#3-configuración-de-precios-con-vigencia)
4. [Restricción de acopio por productor (cupo + override)](#4-restricción-de-acopio-por-productor-cupo--override)
5. [Modelo de datos (columnas nuevas)](#5-modelo-de-datos-columnas-nuevas)
6. [Generación única (Action.generate)](#6-generación-única-actiongenerate)
7. [Motor de cálculo](#7-motor-de-cálculo)
8. [Excedentes (Modelo A)](#8-excedentes-modelo-a)
9. [Pureza de domingos y excedentes](#9-pureza-de-domingos-y-excedentes)
10. [Reportes por tipo de planilla](#10-reportes-por-tipo-de-planilla)
11. [Boleta de pago](#11-boleta-de-pago)
12. [Resumen General de Pago](#12-resumen-general-de-pago)
13. [SQL y despliegue](#13-sql-y-despliegue)
14. [i18n: "hábil" → "quincena"](#14-i18n-hábil--quincena)
15. [Pendientes y puntos de riesgo](#15-pendientes-y-puntos-de-riesgo)
16. [Archivos fuente](#16-archivos-fuente)

---

## 1. Qué cambió vs v2 (visión general)

| Tema | v2 (anterior) | v3 (actual) |
|------|---------------|-------------|
| Generación | 1 planilla por corrida; el operador corría **3 veces** (sin domingos / solo domingos) cambiando el precio a mano y borrando entre corridas | **1 solo botón "Generar"** que crea **hasta 4 planillas por zona** de una vez |
| Precio | `unitPrice` tecleado a mano en la UI (default `PRICE_UNIT_MILK`) | **Configuración global con vigencia** (`MilkPriceConfig`); la UI ya no pide precio |
| Discriminación de planillas | ninguna | 2 columnas nuevas: `tipoplanilla` (`NORMAL`/`EXCEDENTE`) + `tipodia` (`HABIL`/`DOMINGO`/`NINGUNO`) |
| Excedentes por cupo | no existía | leche sobre el **cupo diario** del productor se paga aparte a precio de excedente (planillas `EXCEDENTE`) |
| Checkboxes sin/solo domingos | en la UI | **eliminados**; el motor arma hábiles y domingos internamente |
| Reportes de planilla | 1 (con checkbox "solo domingos" que sólo cambiaba el título) | **3 botones**: Planilla Quincena, Planilla Domingos, Planilla Excedentes |
| Resumen General | agregaba 1 tipo de planilla | **por bloques**: Quincena / Domingos / Excedentes + Líquido Pagable Total |
| Boleta de pago | todas las NORMAL | sólo `NORMAL/HABIL` |

**El motor de cálculo por-productor NO cambió de fondo** (fórmulas, prorrateos, redondeos de v2 siguen vigentes). Lo nuevo es: (a) se invoca varias veces por tipo de día, (b) el precio viene de config, (c) se derivan los excedentes por cupo.

---

## 2. Concepto de negocio

- El acopio de una quincena se paga en **tres conceptos**, cada uno en su(s) planilla(s):
  - **Quincena (días hábiles)** — leche de lunes a sábado, precio hábil (ej. 5,00 Bs/L). Lleva
    diferencia de pesaje, descuentos, retención (IT/IUE), reserva y GA.
  - **Domingos** — leche de domingos, precio domingo (ej. 4,50 Bs/L). Planilla **pura** (sólo litros × precio).
  - **Excedentes** — para productores con **cupo diario** (ej. 80 L/día): la leche que supera el
    cupo se paga a **precio de excedente** (ej. 4,00 Bs/L), separada en excedente hábil y excedente domingo.
- **Modelo A** (decisión de diseño): `acopiomateriaprima` guarda el **total real** acopiado; el
  excedente se **deriva** en la generación (no hay columna "excedente" en el acopio).
- Los precios **cambian seguido** → se configuran con **vigencia** (global) y el productor puede
  **negociar** su propio precio de **excedente** (override).

---

## 3. Configuración de precios (con vigencia)

**Entidad nueva `MilkPriceConfig`** → tabla `precio_acopio_leche` (configuración **propia**, no
reutiliza `CompanyConfiguration`). Campos:

| Campo | Columna | Notas |
|-------|---------|-------|
| `priceWeekday` | `preciohabil` | precio quincena/hábiles (default 5.0) |
| `priceSunday` | `preciodomingo` | precio domingos (default 4.5) |
| `excessPriceWeekday` | `precioexcedentehabil` | excedente hábil global (default 4.0) |
| `excessPriceSunday` | `precioexcedentedomingo` | excedente domingo global (default 4.0) |
| `startDate`/`endDate` | `fechaini`/`fechafin` | vigencia |
| `state` | `estado` | `ENABLE`/`DISABLE` |

- Todos los precios son `double` primitivo (evita el bug de conversión Long/Double del conversor JSF).
- Servicio `MilkPriceConfigService`:
  - `findVigente(startDate, endDate)` → config `ENABLE` cuyo rango **cubre** el periodo
    (`startDate<=:startDate AND endDate>=:endDate`), la más reciente. `null` si no hay.
  - `findOverlapping(startDate, endDate, excludeId)` → validación de solapamiento.
- CRUD: `MilkPriceConfigAction` (+ `@Factory("milkPriceConfig")`, `validate()`, `getState/setState`),
  `MilkPriceConfigDataModel`, vistas `milkPriceConfig(List).xhtml`.
- Permiso `MILKPRICECONFIG`; menú Producción → **Precios de Acopio de Leche**.
- Si **no hay config vigente** para el periodo, la generación **avisa y no genera**
  (`MilkPriceConfig.error.notFound`).

---

## 4. Restricción de acopio por productor (cupo + override)

**Entidad `ProducerCollectionRestriction`** → tabla `restriccion_acopio_productor`. Define, por
productor y con vigencia:

| Campo | Columna | Notas |
|-------|---------|-------|
| `rawMaterialProducer` | `idproductormateriaprima` | FK productor |
| `maxLitersPerDay` | `cupolitrosdia` | **cupo diario** (default 80) |
| `excessPriceWeekday` | `precioexcedentehabil` | override excedente hábil (**0 = usar global**) |
| `excessPriceSunday` | `precioexcedentedomingo` | override excedente domingo (**0 = usar global**) |
| `startDate`/`endDate` | `fechaini`/`fechafin` | vigencia |
| `state` | `estado` | `ENABLE`/`DISABLE` |

- Regla de precio de excedente: **`override > 0 ? override : precio global`**.
- Sólo los productores **con restricción vigente** tienen cupo y por lo tanto pueden generar excedente.
- Servicio `ProducerCollectionRestrictionService`: `preloadRestrictions(start,end)` (Map por
  `producer.id`, patrón `@Claude OPT` batch) + `findOverlapping(...)`.
- CRUD: `ProducerCollectionRestrictionAction/DataModel`, vistas
  `producerCollectionRestriction(List).xhtml`, permiso `PRODUCERCOLLECTIONRESTRICTION`.
- **v1 del feature soporta override sólo de EXCEDENTE.** El precio NORMAL negociado por productor
  NO está soportado (requeriría precio por-registro; el `unitPrice` es por-planilla).

---

## 5. Modelo de datos (columnas nuevas)

Sobre lo de v2 (§8 de v2 sigue vigente para el detalle de columnas), se agregó:

### `RawMaterialPayRoll` → `planillapagomateriaprima`
- `type` → `tipoplanilla` VARCHAR(20), enum **`PayRollType { NORMAL, EXCEDENTE }`** (default NORMAL).
- `dayType` → `tipodia` VARCHAR(10), enum **`DayType { HABIL, DOMINGO, NINGUNO }`** (default NINGUNO).
- La combinación `(tipoplanilla, tipodia)` da las **4 planillas** por zona:
  `NORMAL/HABIL`, `NORMAL/DOMINGO`, `EXCEDENTE/HABIL`, `EXCEDENTE/DOMINGO`.
- Planillas **históricas** (pre-migración) quedan `NORMAL` + `NINGUNO` → **no** aparecen en los
  reportes por tipo de día ni en la boleta hasta regenerarlas.

> El detalle por productor (`RawMaterialPayRecord` / `registropagomateriaprima`) y descuentos
> (`RawMaterialProducerDiscount`) **no cambiaron de estructura**.

---

## 6. Generación única (Action.generate)

**`RawMaterialPayRollAction.generate()`** — UI `view/production/rawMaterialPayRoll.xhtml`:

1. Calcula `startDate`/`endDate` del periodo (gestión, mes, periodo) — igual que v2.
2. Busca `DiscountProducer` vigente (reserva) para `endDate`.
3. **`priceConfig = milkPriceConfigService.findVigente(startDate, endDate)`** → si `null`, error y aborta.
4. `producerTaxCache = preloadProducerTaxes(...)`, `restrictionCache = preloadRestrictions(...)`.
5. `totalWeightHabil = calculateCollectedAmountBetweenDates(..., dayFilter=1)` (base de reserva, sólo hábiles).
6. Por cada `zone` con `group == "ILVA"`, arma hasta 4 planillas con `buildBasePayRoll(source, zone, precio)`:
   - **NORMAL/HABIL**: `validate(habil)` (⚠ **una sola vez por zona**), `generatePayroll(habil, discount, totalWeightHabil, taxCache, restrictionCache, dayFilter=1)`, `createAll`, `warnNegativeLiquid`. `unitPrice = priceConfig.priceWeekday`.
   - **NORMAL/DOMINGO**: `generatePayroll(domingo, discount, 0.0, taxCache, restrictionCache, dayFilter=2)`; sólo `createAll` si tiene registros. `unitPrice = priceConfig.priceSunday`.
   - **EXCEDENTE/HABIL** (si `restrictionCache` no vacío): `generateExcessPayroll(excH, restrictionCache, dayFilter=1, priceConfig.excessPriceWeekday)`; `createAll` si tiene registros.
   - **EXCEDENTE/DOMINGO**: `generateExcessPayroll(excD, restrictionCache, dayFilter=2, priceConfig.excessPriceSunday)`; `createAll` si tiene registros.

> **`validate()` sólo se llama para la planilla hábil de cada zona.** Valida contra `MAX(endDate)`
> de esa zona/producto; si se validara la 2ª planilla de la misma quincena, detectaría la recién
> creada como cruce (`CROSS_WITH_ANOTHER_PAYROLL`).

**UI del panel de generación:**
- Ya **no** hay campo de precio manual ni checkboxes de domingos.
- Muestra **"Precios vigentes"** (bloque read-only): `getCurrentPriceConfig()` resuelve la config
  vigente para el periodo seleccionado (memoizado por gestión/mes/periodo) y se refresca por AJAX al
  cambiar los combos. Si no hay config, avisa en rojo.
- ⚠ Para resolver bien en el primer render: `getGestion()` arranca con la **última gestión** por
  defecto (sino queda `null` hasta un postback); y `getCurrentPriceConfig()` usa los **campos**
  `month`/`periodo` (no `getMonth()/getPeriodo()`, que recalculan al mes/periodo **actual** e
  ignoran la selección).
- `deleteAll` borra **todas** las planillas del rango (las 4 tipos) → un solo borrado limpia todo.

---

## 7. Motor de cálculo

El cálculo por-productor es el de **v2** (ver v2 §4–§7 para fórmulas verbatim, redondeos y prorrateos).
Aquí sólo el delta relevante:

- `generatePayroll(payRoll, discountProducer, totalWeightFortnight, producerTaxCache, restrictionCache, dayFilter)`
  - Ahora recibe `restrictionCache`. Construye `CappedCollection capped = buildCappedCollection(payRoll, restrictionCache, dayFilter)`.
  - La reserva usa `totalWeightFortnightGAB = calculateCollectedAmountBetweenDates(...) - capped.totalExcess` (resta el excedente de la base).
  - `createMapOfProducers(...)` itera **`capped.normalByProducer`** (paga sólo la leche ≤ cupo).
  - **Ajuste de pesaje = PESADA − ACOPIO** (no `pesada − recibida`). El ajuste correcto es la
    diferencia entre la **balanza** (`registroacopio.cantidadpesada`, vía `createMapOfDifferencesWeights`
    que ahora acumula la pesada por día) y el **acopio por productor** (`acopiomateriaprima`, = `capped`
    normal + excedente). Se calcula en `createMapOfProducers`:
    `totalDifference = Σ pesada − (Σ normalByProducer + totalExcess) × precio`, y se prorratea por la
    participación **normal** del productor.
    - **Sin domingos**: se usa `dayFilter=1` (la pesada y el acopio son solo hábiles).
    - **Sin excedentes**: el excedente está en la pesada **y** en el acopio, por lo que **se cancela**
      (`(pesada−exc) − (acopio−exc) = pesada − acopio`). No se escala ni se resta aparte.
    - La `recibida` (`registroacopio.cantidadrecibida`) **no** es el acopio; usarla inflaba el ajuste.
      En domingos `recibida = acopio`, por eso el ajuste de domingos **no cambia**; solo cambia el hábil.
- `dayFilter`: `1` = sin domingos (hábiles), `2` = sólo domingos. (El `0`=todos ya no se usa en la
  generación; `shouldIncludeDate` sigue igual.)

### Fórmula por productor (recordatorio, sin cambios)
```
LÍQUIDO = (litros_normal*precio + ajustePeso - reserva)      <- earnedMoney
        - retención - alcohol - concentrados - comisión
        - yogurt - veterinario - tachos - crédito
        - otrosDescuentos - descuentoGA(=0) + otrosIngresos
```
Redondeos: intermedios `RoundUtil.SYMMETRIC`; líquido `BigDecimal HALF_UP` (v2 §7 — mantener coherencia).

---

## 8. Excedentes (Modelo A)

**`buildCappedCollection(payRoll, restrictionCache, dayFilter)`** (ServiceBean):
1. Trae acopio por `(productor, día)` (`findCollectedAmountByMetaProductBetweenDates`), filtrado por `dayFilter`.
2. Por productor con restricción vigente y cupo `cap`:
   - por día: `normal = min(dailyTotal, cap)`, `excess = max(dailyTotal - cap, 0)`.
   - acumula `normalByProducer`, `excessByProducer`, `excessByDay`, `totalExcess`.
3. Productores **sin** restricción → todo va a `normal` (sin excedente).

**`generateExcessPayroll(payRoll, restrictionCache, dayFilter, globalExcessPrice)`**:
- `setType(EXCEDENTE)`; el `dayType` lo fija el Action.
- Por productor con excedente > 0:
  - `precio = (restriction.override > 0) ? override : globalExcessPrice` (según `dayFilter`: hábil/domingo).
  - `earned = excess * precio`; registro **puro**: `liquidPayable = earned`, todo descuento/retención/reserva/GA = 0.
- Sólo se persiste si hay al menos un registro (algún productor superó su cupo).

---

## 9. Pureza de domingos y excedentes

En `createMapOfProducers`, para `dayFilter == 2` (domingos):
```
withholding = 0        // retención 0
discountGA  = 0
```
y en `generatePayroll` con `dayFilter == 2`: reserva 0, alcohol 0, `discountsBatch = {}`.
→ **La planilla NORMAL/DOMINGO es pura** (sólo litros × precio domingo).
→ **Las planillas EXCEDENTE son puras** por construcción (`generateExcessPayroll`).

**Sólo `NORMAL/HABIL`** lleva diferencia de pesaje, descuentos, retención, IT/IUE, reserva y GA.
Este invariante es la base del **Resumen General** (§12) y de la **boleta** (§11).

---

## 10. Reportes por tipo de planilla

**`RawMaterialGeneralPayRollReportAction`** + `view/production/reports/rawMaterialGeneralPayRollReport.xhtml`.
Tres botones (extiende `GenericReportAction`, mecanismo EJBQL `getEjbql()` + `getRestrictions()`):

| Botón | Método | Filtro | jrxml |
|-------|--------|--------|-------|
| Planilla Quincena | `generateReport` | `type=NORMAL AND dayType=HABIL` | `rawMaterialGeneralPayRollReport.jrxml` |
| Planilla Domingos | `generateSundayReport` | `type=NORMAL AND dayType=DOMINGO` | mismo (general) |
| Planilla Excedentes | `generateExcessReport` | `type=EXCEDENTE` (hábil+domingo) | `rawMaterialExcessPayRollReport.jrxml` (dedicado) |

- `getRestrictions()` agrega `type = #{...reportType}` y, si no es excedente, `dayType = #{...reportDayType}`
  (Seam exige **exactamente un** `#{}` por restricción → binding, no literal enum).
- **Excedentes**: `getEjbql()` devuelve un **pivote** por productor con `SUM(CASE WHEN dayType=HABIL...)`
  y `SUM(CASE WHEN dayType=DOMINGO...)` (litros y total por tipo de día) + `groupByProperty`. El
  precio se deriva en el jrxml (`total/litros`). El jrxml mapea columnas **por posición**
  (`fieldDescription = COLUMN_1..N`), no por alias.
- Columnas del reporte de excedentes: CI, Productor, Litros/Precio/Total **Quincena** y **Domingo**,
  Total a Pagar, Firma.

---

## 11. Boleta de pago

`RawMaterialPayRollServiceBean.consultaBoletaDePago(...)` y `...GA(...)` filtran
**`type=NORMAL AND dayType=HABIL`**. Es decir la boleta paga **sólo la quincena hábil**; los
domingos se pagan por la planilla de domingos (con firma) y los excedentes por la de excedentes.
(Estas queries son `createQuery` directas, no restricciones Seam → el literal enum es válido.)

---

## 12. Resumen General de Pago

`RawMaterialPaySummaryReportAction` + `view/production/reports/rawMaterialPaySummaryReport.jrxml`.
Es el **resumen de todas las planillas** del periodo. Rediseñado **por bloques** (los montos deben
conciliar exacto — es dinero):

### Cabecera (igual a la boleta de pago)
Dos líneas de compañía desde `CompanyConfiguration`: `getTitle()` (COOPERATIVA…) y `getCompanyName()`
(INDUSTRIAS…), luego **"RESUMEN GENERAL DE PAGO A PRODUCTORES LECHEROS"** y **"PERIODO DEL … AL …"**
(centrados). Params: `empresaLinea1`, `empresaLinea2`, `tituloReporte`, `periodoTexto`.

### Bloques
| Bloque | Filtro | Contenido |
|--------|--------|-----------|
| **QUINCENA** | `NORMAL/HABIL` | Acopio Productor, Diferencia, Pesaje de Acopio, Otros Ingresos, **Total**, Descuentos (Alcohol…GA), Total Descuentos, **Líquido Quincena**. Reproduce **exacto** el resumen viejo. |
| **DOMINGOS** | `NORMAL/DOMINGO` | Acopio Domingos (litros × precio), **Líquido Domingos** (puro). |
| **EXCEDENTES** | `EXCEDENTE` | Excedentes Quincena + Excedentes Domingos, **Líquido Excedentes**. |
| **LÍQUIDO PAGABLE TOTAL** | — | = Líquido Quincena + Domingos + Exc.Quincena + Exc.Domingos. |

### Cálculo (a prueba de errores)
- Cada bloque se obtiene con **`getDiscounts(startDate, endDate, metaProduct, PayRollType, DayType)`**
  (versión filtrada por tipo+tipodia; comparte `buildDiscounts(datas)` con la versión sin filtrar).
- La "Diferencia" del bloque hábil usa `getSumAdjustmentFromRecords(..., NORMAL, HABIL)`.
- El líquido de cada bloque = **Σ `totalLiquidByGAB`** de esas planillas → el total concilia con la
  suma del líquido de **todas** las planillas generadas. Sin restas cruzadas entre bloques.
- El ratio IT/IUE (`iue/taxRate`) es constante entre tipos (todas heredan `it/iue/taxRate`), así que
  `getTotalsRawMaterialPayRoll(...)` sigue devolviéndolo correcto.

### Contabilización (`accountingPeriod`, botón "Contabilizar")
Genera el comprobante contable de la quincena, **por bloques** (mismas cuentas, líneas separadas):
- **QUINCENA (hábiles)**: estructura completa —
  Debe `LECHE CRUDA` (`ACCOUNT_LECHECRUDA`) = bruto (mount+dif); Haber `Acreedores por bienes y
  servicios` (`ACCOUNT_ACREEDORES_BIENESSERVICIOS`, provider `PRODUCTORES`) = líquido; `IT/IUE Retenido`;
  `Fondos en custodia` (comisión, crédito, alcohol, concentrados, tachos, otros, reserva, GA);
  `Clientes Productores` (veterinario) y `Clientes` (yogurt) por productor.
- **DOMINGOS** (puro): Debe `LECHE CRUDA` + Haber `Acreedores Productores` = líquido domingos (glosa "ACOPIO/LIQUIDO DOMINGOS").
- **EXCEDENTES** (puro): Debe `LECHE CRUDA` + Haber `Acreedores Productores` = líquido excedentes (hábil+domingo) (glosa "ACOPIO/LIQUIDO EXCEDENTES").
- Cada bloque se calcula con `getDiscounts(...,tipo,tipodia)`; domingos/excedentes **sin retención ni
  descuentos** (pagos puros) → agregan **debe = haber**, no alteran el cuadre de hábiles.
- Se usa el campo `VoucherDetail.gloss` para distinguir las líneas (misma cuenta, líneas separadas).

---

## 13. SQL y despliegue

**`query/query_v6.0.47.sql`** (idempotente donde se pudo) crea:
- `restriccion_acopio_productor` + secuencia + funcionalidad `PRODUCERCOLLECTIONRESTRICTION`.
- Columna `tipoplanilla` (default `NORMAL`) y `tipodia` (default `NINGUNO`) en `planillapagomateriaprima`.
- `precio_acopio_leche` + secuencia + funcionalidad `MILKPRICECONFIG`.
- El INSERT de `funcionalidad` usa **variables de sesión** (`SET @idmod/@newid/@existe`) para no
  leer la tabla destino dentro del INSERT (evita error 1093 de MySQL).

`hibernate.hbm2ddl.auto=validate` → **correr el SQL antes de desplegar**. Compilar con **JDK 1.8**
(el terminal por defecto es Java 21); JDK 1.8 en `C:/Program Files/Java/jdk1.8.0_311`; build `ant clean explode`.

**Checklist de despliegue:**
1. Correr `query_v6.0.47.sql`.
2. Dar permisos `MILKPRICECONFIG` y `PRODUCERCOLLECTIONRESTRICTION` a los roles.
3. **Crear una configuración de precios vigente** (menú → Precios de Acopio de Leche) que cubra la
   quincena; sin ella no se puede generar.
4. Cargar restricciones (cupo/override) a los productores que correspondan.
5. Regenerar planillas: las históricas (`tipodia=NINGUNO`) no salen en reportes por tipo de día.

---

## 14. i18n: "hábil" → "quincena"

En textos **visibles** se usa "Quincena" en vez de "Hábil": etiquetas de `MilkPriceConfig.*`,
`ProducerCollectionRestriction.excessPriceWeekday`, botón `RawMaterialPayRoll.weekdayReport`
("Planilla Quincena"), encabezados del jrxml de excedentes (LITROS/PRECIO/TOTAL QUINCENA) y del
resumen. **Interno sin cambios**: enum `DayType.HABIL`, columna `tipodia='HABIL'`, campos jrxml
(`litrosHabil`, etc.).

---

## 15. Pendientes y puntos de riesgo

**Pendientes conocidos:**
1. **Override de precio NORMAL por productor**: no soportado (sólo excedente). Requeriría precio
   por-registro.
2. jrxml de excedentes / resumen: revisar anchos/posiciones si al imprimir queda apretado.
3. La contabilización asume que domingos/excedentes se pagan **sin retención** (como el motor y las
   boletas). Si en el futuro se decidiera retener IT/IUE sobre ellos, sería un cambio de motor + asiento.

**Puntos de riesgo heredados de v2 (siguen vigentes):** alcohol sin filtro de tipo, mezcla de
redondeos SYMMETRIC/HALF_UP, base de reserva = peso de balanza, `taxRate` hardcoded (`it=0.3,iue=0.5`),
columnas `totalacopiadoxgab`/`totalpesadoxgab` invertidas, `alcoholDiff` al primer registro,
`DISCOUNT_GA=0`, productores sin acopio excluidos (R7), `prepareDiscountsBatch` case-sensitive. Ver v2 §13.

**Nuevos a tener en cuenta:**
- Varias planillas `NORMAL` por (fecha, zona) ahora coexisten → cuidado con queries que asumían una sola
  (revisadas: boleta y totales; `getDiscounts`/`getTotalsRawMaterialPayRoll` agrupan por `unitPrice`).
- `findVigente` exige que la config **cubra** el periodo completo (`fechaini ≤ inicio` y `fechafin ≥ fin`).

---

## 16. Archivos fuente

### Nuevos (feature v3)
| Archivo | Ruta |
|---------|------|
| Config precios (entidad) | `.../model/production/MilkPriceConfig.java` |
| Config precios (service/bean) | `.../service/production/MilkPriceConfigService[Bean].java` |
| Config precios (action/dataModel) | `.../action/production/MilkPriceConfigAction.java`, `MilkPriceConfigDataModel.java` |
| Config precios (vistas) | `view/production/milkPriceConfig.xhtml`, `milkPriceConfigList.xhtml` |
| Restricción productor (entidad) | `.../model/production/ProducerCollectionRestriction.java` |
| Restricción (service/action/dataModel/vistas) | `...ProducerCollectionRestriction*`, `view/production/producerCollectionRestriction[List].xhtml` |
| Enums | `.../model/production/PayRollType.java`, `DayType.java` |
| Reporte excedentes (jrxml) | `view/production/reports/rawMaterialExcessPayRollReport.jrxml` |
| SQL | `query/query_v6.0.47.sql` |

### Modificados
| Archivo | Cambio |
|---------|--------|
| `.../model/production/RawMaterialPayRoll.java` | `+type (tipoplanilla)`, `+dayType (tipodia)` |
| `.../action/production/RawMaterialPayRollAction.java` | `generate()` reescrito (4 planillas); precios de config; panel "precios vigentes"; `getGestion` default; helpers `buildBasePayRoll`/`warnNegativeLiquid`; se quitaron checkboxes/precio manual |
| `.../service/production/RawMaterialPayRollService[Bean].java` | `buildCappedCollection`, `generateExcessPayroll(...,globalExcessPrice)`, `getDiscounts(...,type,dayType)` + `buildDiscounts`, `getSumAdjustmentFromRecords(...,type,dayType)`, boleta filtra `HABIL` |
| `.../action/production/reports/RawMaterialGeneralPayRollReportAction.java` | 3 botones; `getEjbql` pivote excedentes; `getRestrictions` por tipo/tipodia |
| `.../action/production/reports/RawMaterialPaySummaryReportAction.java` | resumen por bloques; cabecera de compañía; params nuevos |
| `view/production/rawMaterialPayRoll.xhtml` | panel un botón + precios vigentes |
| `view/production/reports/rawMaterialGeneralPayRollReport.xhtml` | 3 botones |
| `view/production/reports/rawMaterialPaySummaryReport.jrxml` | cabecera + bloques Domingos/Excedentes + total |
| `resources/messages_app.properties` | claves `MilkPriceConfig.*`, botones, "quincena" |
| `resources/WEB-INF/production/pages.xml`, `view/layout/menu.xhtml` | navegación/menú de los catálogos nuevos |

---

*Documento v3 — planilla única + excedentes + precios con vigencia. Referencia autocontenida para
continuar el feature de acopio de leche.*
