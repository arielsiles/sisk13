# Análisis: Generación de Planillas de Acopio de Leche — v2 (histórico)

> ⚠️ **SUPERSEDIDO POR v3.** Para el estado actual del feature (planilla única, excedentes por
> cupo, precios con vigencia, 3 reportes y resumen por bloques) leer
> [`analisis-planilla-acopio-leche-v3.md`](analisis-planilla-acopio-leche-v3.md).
> Este v2 se conserva como referencia del **motor de cálculo por-productor** (fórmulas, prorrateos,
> redondeos), que v3 sigue usando y referencia.
>
> **Versión 2** — documenta la generación previa (single-planilla, con filtro de días,
> pre-cálculos batch `@Claude OPT-1..7`, nuevo `calculateLiquidPayable` con `BigDecimal`).
> La v1 (`analisis-planilla-acopio-leche-v1.md`) describe la generación anterior y se
> conserva como referencia histórica.
>
> Propósito: dar contexto completo y exacto para hacer **cambios en cálculos y generación**.
> Fecha: 2026-07-08.

## Índice

1. [Diferencias clave vs. v1](#1-diferencias-clave-vs-v1)
2. [Punto de entrada (UI)](#2-punto-de-entrada-ui)
3. [Flujo de generación (Action)](#3-flujo-de-generación-action)
4. [Motor de cálculo (ServiceBean.generatePayroll)](#4-motor-de-cálculo-servicebeangeneratepayroll)
5. [Cálculos detallados (fórmulas verbatim)](#5-cálculos-detallados-fórmulas-verbatim)
6. [El filtro de días (sinDomingos / soloDomingos)](#6-el-filtro-de-días-sindomingos--solodomingos)
7. [Redondeo: SYMMETRIC vs HALF_UP](#7-redondeo-symmetric-vs-half_up)
8. [Entidades y tablas](#8-entidades-y-tablas)
9. [Servicios involucrados](#9-servicios-involucrados)
10. [Optimizaciones @Claude OPT-1..7](#10-optimizaciones-claude-opt-17)
11. [Flujo de aprobación](#11-flujo-de-aprobación)
12. [Queries clave](#12-queries-clave)
13. [⚠️ Puntos de riesgo para cambios de cálculo](#13-️-puntos-de-riesgo-para-cambios-de-cálculo)
14. [Archivos fuente](#14-archivos-fuente)

---

## 1. Diferencias clave vs. v1

| Tema | v1 (anterior) | v2 (actual) |
|------|---------------|-------------|
| Firma `generatePayroll` | `(payRoll, discountProducer)` | `(payRoll, discountProducer, totalWeightFortnight, producerTaxCache, dayFilter)` |
| Peso total quincenal | se recalculaba dentro | **pre-calculado 1 sola vez** en el Action y pasado como parámetro (`@Claude OPT-6`) |
| Licencias fiscales | lazy loading por productor (N+1) | **pre-cargadas** en `preloadProducerTaxes()` → `Map<Long,ProducerTax>` (`OPT-2/3`) |
| Descuentos individuales | 1 query por productor (`prepareDiscount`) | **1 query batch por zona** (`prepareDiscountsBatch`) (`OPT-4`) |
| Prorrateos (alcohol %, ajuste, reserva) | 3 iteraciones separadas | **1 sola iteración** `applyProrations()` (`OPT-7`) |
| `DiscountReserve` | cascade vía producer | `persist()` directo en `applyProrations` (`OPT-5`) |
| **Filtro de días** | no existía | **`sinDomingos` / `soloDomingos`** (dayFilter 0/1/2) |
| **Líquido pagable** | `RoundUtil` SYMMETRIC; sin comisión ni GA en la fórmula | **`BigDecimal` HALF_UP**; incluye **comisión** y resta **descuento GA** |
| Productores sin acopio | se generaba registro igual | **se excluyen** (`collectedAmount <= 0`) (regla R7) |
| Líquido negativo | silencioso | **aviso por productor** en pantalla |
| Selección de zonas | `findAllThatDoNotHavePayRollOnDate` | `findAllThatDoNotHaveCollectionForm` |
| Precio unitario | fijo | **editable en UI** (`editPriceMilk`) |
| Redefinir planilla | — | botón **Redefinir** limpia registros y totales |

---

## 2. Punto de entrada (UI)

**Archivo:** `view/production/rawMaterialPayRoll.xhtml`

### Parámetros de entrada

| Parámetro | EL | Notas |
|-----------|-----|-------|
| `metaProduct` | `rawMaterialPayRoll.metaProduct` | Producto de acopio (leche). Sólo editable al crear |
| `gestion` | `rawMaterialPayRollAction.gestion` | Año fiscal |
| `month` | `rawMaterialPayRollAction.month` | Enum `Month` |
| `periodo` | `rawMaterialPayRollAction.periodo` | `FIRSTPERIODO`(1-15) / `SECONDPERIODO`(16-fin) |
| `unitPrice` | `rawMaterialPayRoll.unitPrice` | **Editable** con lápiz (`editPriceMilk`); default `Constants.PRICE_UNIT_MILK = 4.50` |
| `sinDomingos` | `rawMaterialPayRollAction.sinDomingos` | Excluye domingos (dayFilter=1) |
| `soloDomingos` | `rawMaterialPayRollAction.soloDomingos` | Sólo domingos (dayFilter=2) |
| `taxRate` | `rawMaterialPayRoll.taxRate` | Sólo lectura; = `it + iue` |

`sinDomingos` y `soloDomingos` son **mutuamente excluyentes** (al marcar uno se desmarca el otro — Action líneas 534-546).

### Botones
- **Generar** (`generate`) — visible si `!managed && !readonly`.
- **Redefinir** (`redefine`) — visible si `!managed && readonly`; limpia la lista de registros y los totales (Action líneas 377-393) y vuelve a `readonly=false`.
- **Eliminar todas** (`deleteAll`) — borra todas las planillas del periodo (si ninguna está `APPROVED`).

`initRawMaterialPayRoll()` (Action 78-103): si `unitPrice == 0`, setea `it=0.3`, `iue=0.5`, `unitPrice=Constants.PRICE_UNIT_MILK`, `taxRate=it+iue=0.8`.

> **Nota:** hoy la vista **no** muestra selección de zona productiva (GAB); la generación es siempre "todas las zonas ILVA". El campo `productiveZone` del Action existe pero no se usa en `generate()`.

---

## 3. Flujo de generación (Action)

**Método `generate()`** — `RawMaterialPayRollAction` líneas 206-289:

```
1. Calcula fechas del periodo:
   startDate = (gestion.year, month.value, periodo.initDay)
   endDate   = (gestion.year, month.value, periodo.getEndDay(month.value+1, gestion.year))
   (se normalizan a yyyy/MM/dd)
2. Valida startDate <= endDate.
3. Busca DiscountProducer vigente para endDate (findDiscountsProducerByDate):
   - si hay >1 → error "fechas duplicadas".
   - si null o reserve==0 → info "sin reserva".
4. productiveZones = productiveZoneService.findAllThatDoNotHaveCollectionForm(startDate, endDate)
5. @Claude OPT-6: totalWeightFortnight = collectedRawMaterialCalculatorService
        .calculateCollectedAmountBetweenDates(startDate, endDate, metaProduct, getDayFilter())
   (peso total de TODAS las zonas, filtrado por días)
6. @Claude OPT-3: producerTaxCache = rawMaterialPayRollService.preloadProducerTaxes(startDate, endDate)
7. Para cada zone con group == "ILVA":
   a. Crea payRoll y copia (fechas, company, metaProduct, unitPrice, taxRate, zone, it, iue)
   b. validate(payRoll)  -> no cruce con planilla previa / fecha mínima
   c. generatePayroll(payRoll, discountProducer, totalWeightFortnight, producerTaxCache, getDayFilter())
   d. createAll(payRoll)  -> persiste
   e. por cada record con liquidPayable < 0 -> mensaje ERROR con nombre del productor
8. Mensaje de éxito.
```

`getDayFilter()` (Action 548-552): `sinDomingos→1`, `soloDomingos→2`, ninguno→`0`.

`validate()` (ServiceBean 79-109):
- `CROSS_WITH_ANOTHER_PAYROLL` si `startDate <= última endDate` de esa zona+producto.
- `MINIMUM_START_DATE` si existe una sesión de acopio anterior a `startDate` sin planilla.

---

## 4. Motor de cálculo (ServiceBean.generatePayroll)

**`RawMaterialPayRollServiceBean.generatePayroll(...)`** líneas 154-302. Pasos:

```
PASO 1 — RESERVA POR GAB (sólo si discountProducer != null y dayFilter != 2)   [155-161]
   totalWeightFortnightGAB = calculateCollectedAmountBetweenDates(start,end,meta,ZONE,dayFilter)
   percentageReserveGAB    = ((totalWeightFortnightGAB*100)/totalWeightFortnight)/100
   totalReservaGAB = round(totalWeightFortnight * discountProducer.reserve, 2, SYMMETRIC)
                     * unitPrice * percentageReserveGAB

PASO 2 — DIFERENCIAS DE PESO POR DÍA   [163, 719-737]
   createMapOfDifferencesWeights(payRoll, dayFilter):
     por cada día (filtrado por shouldIncludeDate):
       diff = weightedAmount*unitPrice - receivedAmount*unitPrice

PASO 3 — MAPA DE PRODUCTORES   [165, 894-949]
   createMapOfProducers(...): acumula por productor (Aux) y aplica prorrateos.

PASO 4 — ALCOHOL POR GAB   [166]
   alcoholByGAB = (dayFilter==2) ? 0 : salaryMovementGABService.getAlcoholBayGAB(zone,start,end)

PASO 5 — DESCUENTOS BATCH POR ZONA   [168-171] (@Claude OPT-4)
   discountsBatch = (dayFilter==2) ? {} : prepareDiscountsBatch(start,end,zone)

PASO 6 — CONSTRUCCIÓN DE REGISTROS (por cada Aux)   [194-261]
   record.totalAmount             = round(collectedAmount,2,SYMMETRIC)
   record.productiveZoneAdjustment= round(adjustmentAmount,2,SYMMETRIC)
   record.earnedMoney             = round(earnedMoney,2,SYMMETRIC)
   record.totalPayCollected       = round(unitPrice*collectedAmount,2,SYMMETRIC)
   licencia fiscal (desde producerTaxCache)  -> taxLicense/fechas si isValidLicence
   discount = discountsBatch[producerId]  (o ceros si no hay)
   discount.alcohol       = round(alcoholByGAB * aux.procentaje, 2, SYMMETRIC)
   discount.withholdingTax= round(aux.withholdingTax, 2, SYMMETRIC)
   record.discountReserve = aux.reserveDiscount
   record.discountGA      = round(aux.discountGA, 2, SYMMETRIC)
   (se acumulan todos los totales de la planilla)

PASO 7 — AJUSTE DE REDONDEO DE ALCOHOL   [231, 262-263, 280-283]
   alcoholDiff = Σ(alcoholByGAB*% - round(alcoholByGAB*%))
   se suma alcoholDiff al alcohol del PRIMER registro.

PASO 8 — LÍQUIDO PAGABLE   [285, 1312-1338]
   calculateLiquidPayable(payRoll)

PASO 9 — TOTALES DE LA PLANILLA   [286-300]
   set de todos los totalXxxByGAB (redondeados SYMMETRIC en pasos previos).
```

---

## 5. Cálculos detallados (fórmulas verbatim)

### 5.1 Constantes y tasas
```
Constants.PRICE_UNIT_MILK = 4.50    (util/Constants.java:252)
Constants.DISCOUNT_GA     = 0.0     (util/Constants.java:262)  -> descuento GA deshabilitado
it = 0.3, iue = 0.5  (hardcoded en Action.initRawMaterialPayRoll, NO desde config)
taxRate = it + iue = 0.8            (RawMaterialPayRoll.getTaxRate = it+iue)
taxRateUsado = taxRate / 100 = 0.008   (createMapOfProducers:895)
```
> `Constants.IT_RETENTION=0.03` y `IUE_RETENTION=0.05` **existen pero NO se usan** en este flujo.

### 5.2 Fechas del periodo (`Periodo`)
`getEndDay(mount, year)` — **`mount` es 1-based** (`month.getValue()+1`):
- `FIRSTPERIODO`: initDay=1, endDay=**15**.
- `SECONDPERIODO`: initDay=16; endDay = 30 base, **+1 (=31)** en meses de 31 días (1,3,5,7,8,10,12); **febrero = 29** (bisiesto) o **28**.
- Bisiesto: `year%4==0 && (year%100!=0 || year%400==0)`.

### 5.3 Peso total quincenal (base de la reserva)
`calculateCollectedAmountBetweenDates(...)` suma **`CollectionRecord.weightedAmount`** (columna balanza `registroacopio.cantidadpesada`), **no** el acopio recibido por productor. Sin redondeo.
- Total (todas las zonas): sin `productiveZone` → `totalWeightFortnight`.
- Por GAB: con `productiveZone` → `totalWeightFortnightGAB`.
- Con `dayFilter != 0`: agrupa por día y suma sólo los días que pasan `shouldIncludeDate`.

### 5.4 Reserva por GAB
```
percentageReserveGAB = (totalWeightFortnightGAB*100 / totalWeightFortnight) / 100
totalReservaGAB = round(totalWeightFortnight * discountProducer.reserve, 2, SYMMETRIC)
                  * unitPrice * percentageReserveGAB
```
Sólo si `discountProducer != null` **y** `dayFilter != 2`.

### 5.5 Diferencias de peso (ajuste zona productiva)  [719-737]
```
diff(día) = weightedAmount(balanza) * unitPrice - receivedAmount(recibido) * unitPrice
totalDifference = Σ diff(día que pasa el filtro)
```
Prorrateo a productor en `applyProrations` [1005-1039]:
```
porcentage = (aux.earnedMoney*100 / totalMoneyCollected) / 100        [1009-1011]
proration  = round(totalDifference * porcentage, 2, SYMMETRIC)         [1019]
aux.adjustmentAmount = proration
aux.earnedMoney     += proration
```

### 5.6 Por productor — acumulación (`createMapOfProducers`)  [894-949]
Por cada fila (productor, día, cantidad) que pasa el filtro de días:
```
earned      = amount * unitPrice
hasLic      = licencia fiscal válida (cache)  [OPT-2]
withholding = (dayFilter==2) ? 0 : (hasLic ? 0 : earned * taxRate)     taxRate=0.008
aux.collectedAmount   += amount
aux.earnedMoney       += earned
aux.collectedTotalMoney += earned
aux.withholdingTax    += withholding
aux.discountGA        += (dayFilter==2) ? 0 : amount * Constants.DISCOUNT_GA   (=0)
```
**R7 [940-946]:** al final se **eliminan** los `Aux` con `collectedAmount <= 0` (no generan registro).

### 5.7 Retención fiscal / licencia  [992-998, 1182-1188]
`hasLicenseFromTax(producerTax)` = `producerTax != null && isValidLicence(...)`.
`isValidLicence(license, start, end)`: false si `end==null`, `start==null`, `start>end`, o `license` en blanco.
Licencias pre-cargadas: `preloadProducerTaxes(start,end)` [975-989] trae los `ProducerTax` cuyo `gestionTax` cubre `[start,end]`, indexados por `producer.id`.

### 5.8 Descuentos individuales (`prepareDiscountsBatch`)  [SalaryMavementProducerServiceBean:398]
1 query por zona (`SalaryMovementProducer.getDiscountByZone`); mapea por **`typeMovementProducer.name`** (String, `.equals`):

| `name` | Campo `RawMaterialProducerDiscount` |
|--------|--------------------------------------|
| `CONCENTRADOS` | concentrated |
| `COMISION BANCO` | commission |
| `YOGURT` | yogurt |
| `VETERINARIO` | veterinary |
| `TACHOS` | cans |
| `OTROS EGRESOS` | otherDiscount |
| `OTROS INGRESOS` | otherIncoming |
| `CREDITO` | credit |

Nombres fuera de la lista se **ignoran** (no hay `else`). Cada campo se redondea `SYMMETRIC` 2 dec. El **alcohol NO** viene de aquí (es a nivel GAB).

### 5.9 Alcohol por GAB (`getAlcoholBayGAB`)  [SalaryMavementGABServiceBean:30]
```
alcoholByGAB = Σ SalaryMovementGAB.valor   (zona + periodo)
```
Se prorratea por productor: `discount.alcohol = round(alcoholByGAB * aux.procentaje, 2, SYMMETRIC)`.
El residuo de redondeo (`alcoholDiff`) se suma al **primer** registro de la lista.

> ⚠️ La query **no filtra por tipo de movimiento**: suma TODOS los `SalaryMovementGAB` de la zona/periodo asumiendo que todos son alcohol. Ver §13.

### 5.10 Líquido pagable (`calculateLiquidPayable`)  [1312-1338]  — **BigDecimal HALF_UP**
```
totalDiscount = alcohol + concentrated + withholdingTax + cans + credit
              + veterinary + yogurt + otherDiscount + commission
liquidPayable = earnedMoney - totalDiscount + otherIncoming - discountGA
liquidPayable = HALF_UP(liquidPayable, 2)
totalLiquidByGAB = HALF_UP(Σ liquidPayable, 2)
```
> Diferencias vs v1: (a) usa `BigDecimal` **HALF_UP** (no SYMMETRIC); (b) **incluye `commission`** en `totalDiscount`; (c) **resta `discountGA`**. `earnedMoney` ya trae el ajuste de peso sumado y la reserva restada.

### 5.11 Fórmula completa por productor
```
LÍQUIDO = (litros*precio + ajustePeso - reserva)          <- earnedMoney
        - retención - alcohol - concentrados - comisión
        - yogurt - veterinario - tachos - crédito
        - otrosDescuentos - descuentoGA(=0)
        + otrosIngresos
```

---

## 6. El filtro de días (sinDomingos / soloDomingos)

`dayFilter`: `0`=todos, `1`=sin domingos, `2`=sólo domingos. Regla común
`shouldIncludeDate` (ServiceBean 951-959 y calculator 89-97):
```java
if (dayFilter == 0) return true;
boolean isSunday = (cal.get(DAY_OF_WEEK) == SUNDAY);
if (dayFilter == 1) return !isSunday;   // sin domingos
if (dayFilter == 2) return isSunday;    // sólo domingos
```

**Efecto de `dayFilter == 2` (sólo domingos)** — genera una planilla "limpia" de sólo leche:
- reserva `totalReservaGAB = 0` (paso 1 no entra),
- alcohol `alcoholByGAB = 0` (paso 4),
- descuentos batch `= {}` (paso 5),
- retención `withholding = 0` (5.6),
- descuento GA `= 0` (5.6).

`dayFilter == 1` (sin domingos) sí aplica todos los descuentos, pero calculados sobre los días no-domingo.

Filtro aplicado en: peso total (calculator), diferencias de peso (`createMapOfDifferencesWeights`), y acumulación por productor (`createMapOfProducers`).

---

## 7. Redondeo: SYMMETRIC vs HALF_UP

⚠️ El flujo **mezcla dos modos de redondeo**:
- **`RoundUtil.RoundMode.SYMMETRIC`** (2 dec): en todos los cálculos intermedios
  (reserva, ajuste, alcohol, retención, descuentos, totales de la planilla).
- **`BigDecimal ... HALF_UP`** (2 dec): sólo en `calculateLiquidPayable` (líquido por
  productor y `totalLiquidByGAB`).

Al cambiar cálculos, mantené la coherencia o unificá conscientemente (afecta a los centavos del líquido vs. la suma de descuentos).

---

## 8. Entidades y tablas

### 8.1 `RawMaterialPayRoll` → `planillapagomateriaprima` (cabecera)
Campos relevantes (fieldName → columna): `startDate→fechainicio`, `endDate→fechafin`,
`unitPrice→preciounitario`, `taxRate→tasaimpuesto`, `iue→iue`, `it→it`, `state→estado`
(enum `StatePayRoll`), `productiveZone→idzonaproductiva`, `metaProduct→idmetaproductoproduccion`.

Totales: `totalCollectedByGAB→totalpesadoxgab`, `totalMountCollectdByGAB→totalmontoacopioadoxgab`,
`totalRetentionGAB→totalretencionesxgab`, `totalCreditByGAB→totalcreditoxgab`,
`totalVeterinaryByGAB→totalveterinarioxgab`, `totalAlcoholByGAB→totalalcoholxgab`,
`totalConcentratedByGAB→totalconcentradosxgab`, `totalYogourdByGAB→totalyogurdxgab`,
`totalRecipByGAB→totaltachosxgab`, `totalDiscountByGAB→totadescuentosxgab`,
`totalOtherDiscountByGAB→totalotrosdecuentosxgab`, `totalAdjustmentByGAB→totalajustexgab`,
`totalOtherIncomeByGAB→totalotrosingresosxgab`, `totalLiquidByGAB→totaliquidoxgab`,
`totalReserveDicount→totaldescuentoreserva`, `totalCommission→totalcomision` **(nuevo)**,
`totalGA→totalga` **(nuevo)**.

> ⚠️ **Semántica de columnas invertida** (TODO en el código, línea 262): el campo
> `totalWeighedByGAB` mapea a `totalacopiadoxgab` y `totalCollectedByGAB` a `totalpesadoxgab`.
> Es decir "acopiado" y "pesado" están cruzados respecto al nombre de columna.

### 8.2 `RawMaterialPayRecord` → `registropagomateriaprima` (detalle por productor)
`totalAmount→cantidadtotal` (litros), `productiveZoneAdjustment→ajustezonaproductiva`,
`discountReserve→descuentoreserva`, `discountGA→ga`, `taxLicense→licenciaimpuestos`,
`expirationDateTaxLicence→fechaexpiralicenciaimpuesto`, `startDateTaxLicence→fechainicialicenciaimpuesto`,
`earnedMoney→totalganado`, `totalPayCollected→totalpagoacopio`, `liquidPayable→liquidopagable`,
`rawMaterialProducerDiscount→iddescuentproductmateriaprima`, `rawMaterialPayRoll→idplanillapagomateriaprima`.

### 8.3 `RawMaterialProducerDiscount` → `descuentproductmateriaprima`
`yogurt`, `veterinary→veterinario`, `credit→credito`, `cans→tachos`,
`otherDiscount→otrosdescuentos`, `otherIncoming→otrosingresos`, `withholdingTax→retencion`,
`alcohol`, `concentrated→concentrados`, `commission→comision`, `code→codigo`,
`rawMaterialProducer→idproductormateriaprima`.
Unique: `(codigo, idproductormateriaprima, idcompania)`.

### 8.4 Lectura (acopio y descuentos)
`registroacopio` (`CollectionRecord`: `cantidadpesada`=weighted, `cantidadrecibida`=received),
`planillaacopio` (`CollectionForm`), `acopiomateriaprima` (`CollectedRawMaterial`),
`sesionacopio` (`RawMaterialCollectionSession`), `movimientosalarioproductor`
(`SalaryMovementProducer`), `movimientosalariogab` (`SalaryMovementGAB`),
`descuentoproductor` (`DiscountProducer`: `reserva`), `descuentoreserva` (`DiscountReserve`),
`zonaproductiva` (`ProductiveZone`: `grupo` = 'ILVA'/…).

---

## 9. Servicios involucrados

| Servicio | Responsabilidad en la generación |
|----------|----------------------------------|
| `RawMaterialPayRollServiceBean` | Orquesta `generatePayroll`, `createMapOfProducers`, `applyProrations`, `calculateLiquidPayable`, `validate`, aprobación |
| `CollectedRawMaterialCalculatorServiceBean` | `calculateCollectedAmountBetweenDates` (peso total y por GAB, con dayFilter) — lee `CollectionRecord.weightedAmount` |
| `SalaryMavementProducerServiceBean` | `prepareDiscountsBatch` (descuentos por zona) — legacy `prepareDiscount` por productor |
| `SalaryMavementGABServiceBean` | `getAlcoholBayGAB` (suma movimientos GAB de la zona) |
| `ProductiveZoneService` | `findAllThatDoNotHaveCollectionForm` (zonas sin planilla en el periodo) |
| `RawMaterialProducerDiscountService` | descuentos de productores |

Clase interna `Aux` (ServiceBean 1246-1261): `producer, collectedAmount, adjustmentAmount,
collectedTotalMoney, earnedMoney, withholdingTax, procentaje, reserveDiscount, discountGA`.

---

## 10. Optimizaciones @Claude OPT-1..7

> Al modificar cálculos, **no reintroducir** los N+1 que estas optimizaciones eliminaron.

| OPT | Qué hace | Dónde |
|-----|----------|-------|
| OPT-1 | Eliminó query muerta `getRawMaterialCollected` (resultado no usado) | `applyProrations` |
| OPT-2 | Cache de licencia fiscal por productor (`licenseCache`) | `createMapOfProducers` |
| OPT-3 | `ProducerTax` pre-cargado (`preloadProducerTaxes`) en vez de lazy load | Action + ServiceBean |
| OPT-4 | Descuentos batch por zona (`prepareDiscountsBatch`) en vez de 1 query/productor | `generatePayroll` |
| OPT-5 | `DiscountReserve` persistido directo con `persist()` | `applyProrations` |
| OPT-6 | `totalWeightFortnight` pre-calculado 1 vez en el Action | Action.generate |
| OPT-7 | Consolida alcohol% + ajuste% + reserva% en una sola iteración | `applyProrations` |

---

## 11. Flujo de aprobación

`approvedPayRoll()` (Action 296-318) valida que haya planillas del periodo y luego, vía ServiceBean:
1. `approvedSession` → `RawMaterialCollectionSession` = APPROVED
2. `approvedNoteRejection` → `RawMaterialRejectionNote` = APPROVED
3. `approvedDiscounts` → `SalaryMovementProducer` = APPROVED
4. `approvedDiscountsGAB` → `SalaryMovementGAB` = APPROVED
5. `approvedRawMaterialPayRoll` → `RawMaterialPayRoll` = APPROVED
6. `approvedReservProductor` → `DiscountProducer` = APPROVED

---

## 12. Queries clave

- `RawMaterialPayRoll.findCollectedAmountByMetaProductBetweenDates` — (fecha, productor, cantidad) de acopio por productor.
- `RawMaterialPayRoll.differenceRawMaterialBetweenDates` — (fecha, recibido, pesado) para diferencias (usada en `createMapOfDifferencesWeights`; **ojo:** `findDifferencesWeights` ignora su parámetro `namedQuery` y usa esta fija).
- `CollectionForm.calculateWeightedAmountBetweebDateByMetaProduct[AndGAB]` — suma `weightedAmount` (total / por zona).
- `CollectionForm.weightedAmountPerDayByMetaProduct[AndGAB]` — por día (para dayFilter).
- `SalaryMovementProducer.getDiscountByZone` — descuentos batch por zona.
- `SalaryMovementGAB.getDiscount` — movimientos GAB (alcohol).
- `RawMaterialPayRoll.findLasEndDateByMetaProductAndProductiveZone` — validación de cruce.

---

## 13. ⚠️ Puntos de riesgo para cambios de cálculo

1. **Alcohol sin filtro de tipo:** `getAlcoholBayGAB` suma **todos** los `SalaryMovementGAB`
   de la zona/periodo (no filtra por nombre "ALCOHOL"). Si se agregan otros tipos de
   movimiento GAB, se sumarían como alcohol. La columna `typeMovementGAB.name` existe pero no se usa.
2. **Mezcla de redondeos** (§7): intermedios `SYMMETRIC`, líquido `HALF_UP`. Puede haber
   descuadres de centavos entre `Σ descuentos` y el `liquidPayable`.
3. **Base de la reserva = peso de balanza** (`weightedAmount`), no el acopio recibido por
   productores. Cambiar esto altera `percentageReserveGAB` y el monto de reserva.
4. **`dayFilter == 2` (sólo domingos)** desactiva reserva, alcohol, descuentos, retención y GA.
   Es intencional (planilla de sólo leche dominical), pero cualquier cálculo nuevo debe
   respetar esa condición o se filtrará indebidamente.
5. **`taxRate` hardcoded** (`it=0.3, iue=0.5`) en `initRawMaterialPayRoll` — **no** lee la
   configuración de empresa (el bloque `companyConfigurationService` está comentado).
6. **Columnas invertidas** `totalacopiadoxgab` / `totalpesadoxgab` (§8.1): cuidado al leer/escribir SQL directo.
7. **`alcoholDiff` al primer registro:** el residuo de redondeo del alcohol se acumula en el
   primer productor de la lista (arbitrario según orden del `Map`).
8. **`DISCOUNT_GA = 0`**: el descuento GA está deshabilitado; `discountGA` siempre 0 salvo que se cambie la constante.
9. **Productores sin acopio excluidos (R7):** si un productor tiene sólo descuentos pero 0
   litros en el periodo, **no** se genera registro (no se le cobra el descuento).
10. **`prepareDiscountsBatch` case-sensitive por `name`:** los tipos de movimiento se
    matchean por string exacto (`"CONCENTRADOS"`, `"COMISION BANCO"`, …). Un typo en el
    catálogo `tipomovimientoproductor` hace que el descuento se ignore silenciosamente.

---

## 14. Archivos fuente

| Archivo | Ruta |
|---------|------|
| Vista | `view/production/rawMaterialPayRoll.xhtml` |
| Action | `src/main/com/encens/khipus/action/production/RawMaterialPayRollAction.java` |
| Servicio principal | `src/main/com/encens/khipus/service/production/RawMaterialPayRollServiceBean.java` |
| Interfaz servicio | `src/main/com/encens/khipus/service/production/RawMaterialPayRollService.java` |
| Calculadora de acopio | `.../service/production/CollectedRawMaterialCalculatorServiceBean.java` |
| Descuentos productor | `.../service/production/SalaryMavementProducerServiceBean.java` |
| Descuentos GAB (alcohol) | `.../service/production/SalaryMavementGABServiceBean.java` |
| Entidad planilla | `.../model/production/RawMaterialPayRoll.java` |
| Entidad registro pago | `.../model/production/RawMaterialPayRecord.java` |
| Entidad descuentos | `.../model/production/RawMaterialProducerDiscount.java` |
| Enum Periodo | `.../model/production/Periodo.java` |
| Constantes | `.../util/Constants.java` |
| Named queries acopio | `.../model/production/CollectionForm.java`, `CollectionRecord.java` |

---

*Documento v2 — generación actual. Referencia para cambios en cálculos y generación de planillas de acopio de leche.*
