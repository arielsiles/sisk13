# Requerimiento: Excedentes de acopio por cupo (pago diferenciado)

> Spec de implementación. Complementa `analisis-planilla-acopio-leche-v2.md` (motor actual).
> Estado: **aprobado para implementar**. Fecha: 2026-07-08.

## 1. Regla de negocio

Algunos productores tienen un **cupo acordado de acopio** (hoy **80 L/día**). La leche
hasta el cupo se paga al **precio normal**; el **excedente** (lo que pasa del cupo) se paga
a un **precio menor**.

Precios (todos configurables):
- Normal día hábil: **5.00 Bs/L**  · Normal domingo: **4.50 Bs/L** (= `unitPrice` de la planilla, por corrida)
- Excedente día hábil: **4.00 Bs/L**  · Excedente domingo: **4.00 Bs/L** (config por productor)

El cupo aplica **por día**. Si en domingo se excede el cupo, el excedente también se paga a 4.00.

## 2. Decisiones confirmadas

| Tema | Decisión |
|------|----------|
| Dónde va el excedente | **Modelo A (Opción 2 pura):** `acopiomateriaprima` guarda el **total real** (sin topar). El excedente se **deriva** en la generación. Sin columnas nuevas en acopio. |
| Config | Nueva entidad **por productor** con vigencia: cupo, precio excedente hábil, precio excedente domingo. |
| Estructura de planillas | **2 planillas de excedente** (hábiles/domingos) reusando `dayFilter` + campo `tipo` NORMAL/EXCEDENTE en `RawMaterialPayRoll`. |
| Planilla de excedente | **Pura:** `excedente × precio`, sin ajuste, reserva, alcohol, descuentos ni retención. |
| Regla del ajuste | **"leche normal = total − excedente"**: el excedente se resta de todo lo que use el pesado/balanza dentro de la planilla normal (ajuste y reserva). |
| Cupo | Por día (hoy 1 sesión/día por productor; agregación por (productor, día) queda robusta igual). |

## 3. Por qué se infla el ajuste (contexto clave)

`registroacopio.cantidadrecibida` (**recibido**) **NO se teclea**: se calcula como la **suma
del acopio por persona** (`CollectionFormServiceBean.populateWithTotalsOfCollectedAmount` →
`CollectionForm.calculateCollectedAmountOnDateByMetaProduct`). El **pesado**
(`cantidadpesada`) es la balanza (manual).

Ajuste = `(pesado − recibido) × precio`, prorrateado por el % de `earnedMoney`.

Si se registra al productor **topado en 80** pero la balanza pesa **120**, entonces
recibido=80 y pesado=120 → el ajuste "ve" 40 L de diferencia que **no son merma, son
excedente** → **ajuste inflado**. En **Modelo A** se registra el total real (120), con lo
que recibido=pesado y el ajuste deja de inflarse; y para la planilla normal se resta el
excedente de ambos lados para reconciliar **solo la leche normal**.

## 4. Ejemplo numérico (una GAB, un día)

Productores: **Olga** (con cupo 80) entrega 120 L; **Juan** (sin cupo) entrega 50 L.
Balanza (pesado) = 170 L (sin merma). Precio normal hábil = 5.00; excedente = 4.00.

```
Total real registrado (Modelo A): Olga 120, Juan 50  -> recibido = 170 ; pesado = 170
Excedente Olga = max(120-80,0) = 40 ; Excedente Juan = 0 ; excedenteGAB = 40

PLANILLA NORMAL (hábil, 5.00):
  normalQty: Olga 80, Juan 50
  ajuste: pesadoNormal = 170-40 = 130 ; recibidoNormal = 170-40 = 130 -> diff = 0 (solo merma real)
  earnedMoney: Olga 80*5=400, Juan 50*5=250   (sin excedente)
  boleta: Olga figura con 80 L, Juan 50 L      (nada de excedente)

PLANILLA EXCEDENTE (hábil, 4.00, pura):
  Olga: 40 * 4.00 = 160 ; liquidPayable = 160 (sin descuentos/retención/ajuste)
  Juan: no aparece (excedente 0)
```

## 5. Diseño de datos

### 5.1 Entidad nueva `ProducerCollectionRestriction`
Tabla `restriccion_acopio_productor`:

| Columna | Tipo | Notas |
|---------|------|-------|
| id | BIGINT PK | |
| idproductormateriaprima | BIGINT FK | productor |
| cupolitrosdia | DECIMAL(16,2) | default 80 |
| precioexcedentehabil | DECIMAL(9,2) | default 4.00 |
| precioexcedentedomingo | DECIMAL(9,2) | default 4.00 |
| fechaini | DATE | vigencia |
| fechafin | DATE | vigencia |
| estado | VARCHAR | ENABLE/APPROVED (patrón `DiscountProducer`) |
| idcompania | BIGINT FK | |
| version | BIGINT | |

Servicio `ProducerCollectionRestrictionService.preloadRestrictions(startDate, endDate)` →
`Map<Long, ProducerCollectionRestriction>` por `producerId` (patrón `preloadProducerTaxes`).

### 5.2 Campo nuevo en `RawMaterialPayRoll`
`tipo` (enum `PayRollType { NORMAL, EXCEDENTE }`) → columna `tipoplanilla` **default `NORMAL`**
(las planillas existentes quedan NORMAL; sin config, la generación es idéntica a hoy).

## 6. Cambios en el motor (`RawMaterialPayRollServiceBean`)

### 6.1 `createMapOfProducers` — tope + derivación de excedente
Reestructurar a acumulación **por (productor, día)**:
```
dailyTotal(prod,día) = Σ amount de ese día
si restringido y dailyTotal > cupo:  normalQty = cupo ; excessQty = dailyTotal - cupo
si no:                               normalQty = dailyTotal ; excessQty = 0
earnedMoney / collectedAmount / withholding  ->  sobre normalQty
excessByDay(día) += excessQty            (para 6.2 y 6.3)
excessByProducer(prod, día) = excessQty  (para la planilla de excedente)
```

### 6.2 Ajuste — restar excedente de ambos lados
```
diff(día) = (pesado(día) - excessByDay(día))*precio - (recibido(día) - excessByDay(día))*precio
```

### 6.3 Reserva — base sin excedente
Restar el excedente del `totalWeightFortnight` (todas las zonas, pre-calculado en el Action) y
del `weightedGAB`, para que nada del excedente entre en lo normal.

### 6.4 Planilla de excedente — `generateExcessPayroll(payRoll, restrictionCache, dayFilter)`
```
por productor restringido con excedente (según el tipo de día del dayFilter):
   excessQty = Σ (días del tipo) max(dailyTotal - cupo, 0)
   precio    = dayFilter==1 ? precioExcedenteHabil : precioExcedenteDomingo   (por productor)
   record.totalAmount = excessQty ; earnedMoney = excessQty*precio ; liquidPayable = earnedMoney
   (retención/reserva/alcohol/descuentos/ajuste = 0)
payRoll.tipo = EXCEDENTE
totales: totalCollected = Σ excessQty ; totalLiquid = Σ liquidPayable ; resto 0
```
> El precio de excedente es **por productor** → cada registro lleva su propio precio; la
> planilla de excedente **no** tiene un `unitPrice` único.

### 6.5 Disparo — `RawMaterialPayRollAction.generate()`
Tras crear la planilla NORMAL de cada zona ILVA, crear la planilla EXCEDENTE del **mismo
`dayFilter`**. Por quincena/GAB: 4 planillas (normal hábil, normal domingo, excedente hábil,
excedente domingo).

## 7. Reportes / boletas (crítico)
Las consultas actuales (`consultaBoletaDePago`, `getDiscounts`, `getTotalsRawMaterialPayRoll`,
listados) deben filtrar **`tipo = NORMAL`** para que el excedente **no figure** en lo normal.
Agregar boleta/reporte para `tipo = EXCEDENTE`.

## 8. UI
- Catálogo CRUD **"Restricción de Acopio por Productor"** (patrón Seam) + permiso propio + menú Producción.
- Listado de planillas: columna/filtro `tipo`.
- Boleta de excedentes (reporte nuevo).

## 9. SQL versionado (`query_v6.0.x_terdemol.sql`)
- `CREATE TABLE restriccion_acopio_productor` + `secuencia`.
- `ALTER TABLE planillapagomateriaprima ADD tipoplanilla VARCHAR ... DEFAULT 'NORMAL'`.
- `funcionalidad` + permiso del catálogo nuevo.

## 10. Fases de implementación
1. **Fase 1** — Modelo + config (entidad, servicio, CRUD, SQL, permiso). No toca generación.
2. **Fase 2** — Planilla normal con tope + ajuste/reserva sin excedente.
3. **Fase 3** — Planilla de excedente (`tipo`) + disparo + reportes (filtro `tipo=NORMAL` + boleta excedentes).

## 11. Riesgos / notas
- `dayFilter` obligatorio para el split de precio (hábil=1 / domingo=2). Con `dayFilter=0` no hay cómo elegir precio de excedente por día.
- Sin config de restricción vigente → comportamiento **idéntico al actual** (tope solo a restringidos).
- Migración: planillas viejas = `tipo NORMAL`.
- Confirmado: 1 sesión/día por productor hoy; la agregación por (productor, día) lo cubre igual si cambia.
- Retención en excedente = 0 (planilla pura). Si a futuro el excedente debiera retener IT/IUE, es un cambio acotado en `generateExcessPayroll`.

---

*Spec para implementación del pago de excedentes de acopio por cupo. Implementación en rama nueva a partir de `dev_ilva`.*
