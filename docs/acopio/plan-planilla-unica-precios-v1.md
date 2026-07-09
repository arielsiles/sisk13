# Plan: Planilla única de acopio + precios con vigencia + excedentes

> Continuación de [requerimiento-excedentes-acopio.md](requerimiento-excedentes-acopio.md).
> Objetivo de esta iteración: **un solo proceso de generación** por quincena que
> produzca y **registre** hábiles, domingos y excedentes; precios desde
> **configuración con vigencia** (global + override por productor); y **3 reportes**.

## 1. Problema actual

Para emitir la quincena hoy el operador hace un ciclo manual y frágil:

1. Generar `Sin domingos` → emitir planilla → **borrar**.
2. Generar `Solo domingos` → emitir → **borrar**.
3. En **cada** generación debe **cambiar el precio a mano** (5.00 hábil, 4.50 domingo).

Es lento y muy susceptible a errores (precio equivocado, olvidar borrar, etc.).

## 2. Objetivo

Un solo botón **Generar** (periodo + producto) que arme y **persista**:

| Planilla        | tipoplanilla | tipodia | Precio             |
|-----------------|--------------|---------|--------------------|
| Quincena hábiles | NORMAL       | HABIL   | precio hábil (5.00) |
| Domingos        | NORMAL       | DOMINGO | precio domingo (4.50) |
| Excedente hábil | EXCEDENTE    | HABIL   | precio exced. hábil |
| Excedente dom.  | EXCEDENTE    | DOMINGO | precio exced. dom.  |

Luego **3 botones de reporte**: Hábiles, Domingos, Excedentes (formato multicolumna).
Todos los cálculos quedan **registrados** por tipo de día (auditoría/estadística).

## 3. Decisiones confirmadas (con el usuario)

- **Precios**: configuración **global propia con vigencia** (entidad/tabla dedicada,
  **NO** reutilizar `CompanyConfiguration`), con los 4 precios. Además **override por
  productor** en la restricción existente (que "funcionó bien"): si el override es `0`,
  se usa el precio global. → *Manejar ambos.*
- **Auditoría**: **por tipo de día** (consolidado por productor) → 4 planillas por zona.
- **Generación**: **un solo botón**; precios desde config; se quita el precio manual y
  el checkbox de domingos.

## 4. Modelo de datos

### 4.1 Columna nueva `tipodia` en `planillapagomateriaprima`
- `DayType { HABIL, DOMINGO, NINGUNO }` (enum String, default `NINGUNO`).
- Junto con `tipoplanilla` (NORMAL/EXCEDENTE, ya existe) da las 4 combinaciones.
- `ALTER TABLE planillapagomateriaprima ADD tipodia VARCHAR(10) NOT NULL DEFAULT 'NINGUNO'`.

### 4.2 Config global de precios — entidad nueva `MilkPriceConfig`
- Tabla `precio_acopio_leche` (dedicada, con su `secuencia`).
- Campos: `fechaini`, `fechafin`, `estado` (ENABLE/DISABLE),
  `preciohabil`, `preciodomingo`, `precioexcedentehabil`, `precioexcedentedomingo`
  (todos `double`, DECIMAL(9,2)).
- CRUD (list + form) + permiso `MILKPRICECONFIG` + ítem de menú (Producción → Configuración).
- Resolver `preloadMilkPrice(startDate, endDate)` → config vigente (misma lógica de
  vigencia/solapamiento que `ProducerCollectionRestriction` / `DiscountProducer`).

### 4.3 Restricción por productor (existente, se mantiene)
- `cupolitrosdia` + `precioexcedentehabil` + `precioexcedentedomingo`.
- Regla de resolución de precio de excedente: `override > 0 ? override : global`.
- (Se mantiene `double` primitivo — evita el bug de conversión Long/Double ya resuelto.)

## 5. Motor de generación (`RawMaterialPayRollServiceBean` + acción)

`generate()` en `RawMaterialPayRollAction`, por cada zona `ILVA`, arma **4 planillas**:

1. **NORMAL/HABIL** — `generatePayroll(dayFilter=1)`, `unitPrice = preciohabil`.
2. **NORMAL/DOMINGO** — `generatePayroll(dayFilter=2)`, `unitPrice = preciodomingo`.
3. **EXCEDENTE/HABIL** — `generateExcessPayroll(dayFilter=1)`.
4. **EXCEDENTE/DOMINGO** — `generateExcessPayroll(dayFilter=2)`.

- **Sin cambios de fondo en el cálculo**: la reserva/descuentos siguen aplicándose sólo a
  hábiles (el motor ya los omite cuando `dayFilter==2`). Domingos = litros × precio (puro).
- `generateExcessPayroll` ya recibe `dayFilter` y elige el precio de excedente; sólo se
  ajusta para tomar `override>0 ? override : globalExcedente(habil/domingo)`.
- Cada planilla se marca con su `type` + `dayType` antes de `createAll`.
- Sólo se persiste el excedente si hay registros (algún productor superó su cupo).

## 6. Formulario de generación (`rawMaterialPayRoll` xhtml + acción)

- **Quitar** el campo de precio manual y el checkbox `soloDomingos`/`sinDomingos`.
- Un botón **Generar** → lee precios de la config vigente y arma las 4 planillas.
- `deleteAll`: sin cambios (borra por rango) — **un solo borrar limpia las 4**. Simplifica
  el ciclo generar/borrar del operador.

## 7. Reportes (`rawMaterialGeneralPayRollReport`)

3 botones:

1. **Planilla Hábiles** — `type=NORMAL AND tipodia=HABIL`.
2. **Planilla Domingos** — `type=NORMAL AND tipodia=DOMINGO`.
3. **Planilla Excedentes** — `type=EXCEDENTE`, formato multicolumna.

- `getRestrictions()`: agregar filtro por `tipodia` (además de `type`) para hábiles/domingos.
- **Excedentes**: query dedicada que **une HABIL + DOMINGO por productor** →
  columnas: `CI, Productor, Litros Háb, Precio Háb, Total Háb, Litros Dom, Precio Dom,
  Total Dom, Total a Pagar, Firma`. **jrxml dedicado** (ya no se reutiliza el general).

## 8. Impactos a revisar al implementar (riesgo)

Con el split HABIL/DOMINGO ahora hay **varias planillas NORMAL** por (fecha, zona), donde
antes había una. Revisar que no rompan:

- **Boleta de pago** (`consultaBoletaDePago` / `...GA`): hoy asume 1 NORMAL por periodo y
  agrupa por `unitPrice`; con dos precios el productor aparece dos veces. Decidir si la
  boleta **suma hábil+domingo** (y muestra excedente aparte) o se deja.
- **Aprobación** (`approvedPayRoll`) y named queries `getMaterialPayRollInDates`,
  `getPayRollInDatesAndGAB`, `getSumaryTotal`, `getDiscounts`: filtran por (fecha, zona);
  verificar comportamiento con múltiples planillas.

### Punto abierto a confirmar
- El **override de precio NORMAL por productor** no es representable con un `unitPrice`
  único por planilla (requeriría precio por-registro = cambio mayor). Propongo: en v1 el
  override por productor aplica **sólo a excedente** (como hoy); el precio NORMAL negociado
  por productor queda para una iteración posterior. **¿De acuerdo?**

## 9. SQL

Extender `query/query_v6.0.47.sql` (o nuevo `.48` si ya desplegaron .47):
- `ALTER TABLE planillapagomateriaprima ADD tipodia VARCHAR(10) NOT NULL DEFAULT 'NINGUNO'`.
- `CREATE TABLE precio_acopio_leche` + fila en `secuencia`.
- `funcionalidad` + `permiso` para `MILKPRICECONFIG`.
- (`tipoplanilla` ya se creó en la fase previa.)

## 10. Fases de implementación

1. **Config global de precios**: entidad + CRUD + permiso + menú + resolver + SQL.
2. **Columna `tipodia`** + motor genera 4 planillas + formulario a un solo botón.
3. **Reportes**: 3 botones + query/jrxml de excedentes multicolumna.
4. **Revisión** de boleta/aprobación (impactos §8).

Compilar con **JDK 1.8**. `hbm2ddl=validate` → correr el SQL antes de desplegar.

---

## 11. Estado de implementacion (hecho)

Todas las fases implementadas y compiladas con JDK 1.8.

**Fase 1 — Config global de precios**
- `MilkPriceConfig` (tabla `precio_acopio_leche`) + `MilkPriceConfigService(Bean)`
  (`findVigente`, `findOverlapping`) + `MilkPriceConfigAction` + `MilkPriceConfigDataModel`.
- Vistas `milkPriceConfigList.xhtml` / `milkPriceConfig.xhtml`; pages.xml; menu.xhtml
  (permiso `MILKPRICECONFIG`); claves i18n `MilkPriceConfig.*`.

**Fase 2 — Motor / generacion unica**
- Enum `DayType {HABIL, DOMINGO, NINGUNO}`; columna `tipodia` en `RawMaterialPayRoll`.
- `RawMaterialPayRollAction.generate()` reescrito: por zona genera hasta 4 planillas
  (NORMAL/HABIL, NORMAL/DOMINGO, EXCEDENTE/HABIL, EXCEDENTE/DOMINGO) con precios de la
  config vigente; `validate()` una sola vez por zona. Helpers `buildBasePayRoll` /
  `warnNegativeLiquid`.
- `generateExcessPayroll(..., double globalExcessPrice)`: precio = override productor (&gt;0)
  o global.
- Formulario `rawMaterialPayRoll.xhtml`: se quito precio manual y checkbox de domingos
  (precio solo lectura al ver una planilla existente).

**Fase 3 — Reportes (3 botones)**
- `RawMaterialGeneralPayRollReportAction`: `generateReport` (habiles), `generateSundayReport`
  (domingos), `generateExcessReport` (excedentes). `getEjbql`/`getRestrictions` ramifican por
  `excess` y `reportDayType`; el excedente usa un EJBQL pivote (CASE por tipo de dia) + GROUP BY.
- jrxml dedicado `rawMaterialExcessPayRollReport.jrxml` (CI, Productor, Litros/Precio/Total
  habil y domingo, Total a pagar, Firma). Habiles/domingos reutilizan el jrxml general.

**Fase 4 — Boleta**
- Boleta de pago (`consultaBoletaDePago` y `...GA`) filtra `type=NORMAL AND dayType=HABIL`
  (los domingos se pagan por la planilla de domingos; el excedente por la de excedentes).

## 12. Notas de despliegue

1. Correr `query/query_v6.0.47.sql` (crea `precio_acopio_leche`, columna `tipodia`,
   funcionalidad `MILKPRICECONFIG`) antes de desplegar (hbm2ddl=validate).
2. Dar el permiso `MILKPRICECONFIG` al rol correspondiente.
3. **Crear una configuracion de precios vigente** (menu Produccion → Precios de Acopio de
   Leche) que cubra la quincena, o descomentar la semilla del SQL. Sin config vigente, la
   generacion avisa y no genera.
4. Las planillas historicas quedan con `tipodia=NINGUNO`: no apareceran en los reportes
   por tipo de dia ni en la boleta hasta regenerarlas con el proceso nuevo.
