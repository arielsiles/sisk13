# Reporte Diario de Producción ULEXITA — Especificación Funcional y Técnica

## 1. Contexto y objetivo

El módulo `xproduction` actual gestiona órdenes de producción genéricas (insumos, materiales, productos terminados, mano de obra). La planta requiere ahora capturar y reportar información específica de la **línea ULEXITA** que hoy se lleva en una planilla Excel manual ("REPORTE DIARIO DE PRODUCCION ULEXITA"). El sistema debe:

1. Capturar los datos adicionales (laboratorio, granulado, reproceso, diluyente) en la pantalla de la orden de producción.
2. Calcular en línea los indicadores de proceso (Kpa, Kpm, MERMA, %MERMA, leyes recalculadas, consumo teórico).
3. Generar un reporte mensual en Excel que reproduzca la planilla actual.
4. Quedar preparado para extender la solución a otras líneas (ej. MOLIENDA) sin reescribir el núcleo.

## 2. Alcance

**Incluido**:

- Configuración por `ProductionLine` (artículos clave, factor de merma, plantilla de reporte).
- Entidad satélite `XProductionUlexita` para datos específicos de la línea ULEXITA.
- Capa de cálculo `XProductionUlexitaCalc` con todas las fórmulas centralizadas.
- Nueva pestaña "Datos del Proceso" en `production.xhtml`, condicional por línea.
- Vista previa de la fila Excel desde la propia pantalla de producción.
- Reporte mensual a Excel con plantilla JXLS.
- Pantalla de filtros para emitir el reporte (mes/año/línea).
- Permisos diferenciados para edición de datos de laboratorio.
- i18n en español de los nuevos labels.

**Excluido (queda preparado, no implementado)**:

- Plantilla y entidad satélite para línea MOLIENDA.
- Promedios ponderados en la fila TOTAL MES.
- Edición masiva de datos de laboratorio fuera de la orden.

## 3. Datos del reporte

### 3.1 Mapeo columnas Excel → origen

| Col | Encabezado | Naturaleza | Origen |
|-----|------------|-----------|--------|
| B | FECHA | Dato | `xproduction.initDate` (existente) |
| C | DIA | Calc | Día semana derivado de `initDate` |
| D | ULEX DISPONIBLE | Dato | `XProductionUlexita.ulexDisponibleSnap` (snapshot al aprobar; UI muestra saldo en vivo desde inventario) |
| E | CONSUMO MATERIA PRIMA ULEX (TN) — calc | Calc | `MERMA + Kpa × PT_TOTAL_BUENO`, snapshot al aprobar en `consumoMpCalcSnap` |
| — | CONSUMO MATERIA PRIMA ULEX (TN) — real | Calc | `Σ xpr_insumo.cantidad WHERE cod_art = línea.codArtMpPrincipal` (columna adicional en pantalla; el Excel mantiene solo el calculado teórico) |
| F-G | GRUPO (D/N) | Dato | `xproduction.tipoturno` (existente) |
| H | Diluyente añadido (TN) | Dato | `XProductionUlexita.diluyenteTotalTn` si está poblado, o `Σ insumos diluyentes` |
| I | Proporción bentonita % | Dato | `XProductionUlexita.bentonitaPct` si poblado, o `bent_qty / diluyente × 100` |
| J | Proporción caolín % | Calc | `100 − bentonita%` |
| K | Consumo Reproceso (TN) | Dato | `XProductionUlexita.consumoReprocesoTn` |
| L | Ley MP con bentonita | Dato (lab) | `XProductionUlexita.leyMpBentonita` |
| M | Ley MP recalculada | Calc | `Ley_PT / Kpm_bentonita` |
| N | Ley PT | Dato (lab) | `XProductionUlexita.leyPt` |
| O | PRODUCTO GRANULADO (TN) | Dato | `XProductionUlexita.productoGranuladoTn` |
| P | PRODUCTO A (TN) | Dato | `xpr_producto.cantidad WHERE cod_art = línea.codArtPtA` |
| Q | PRODUCTO B (TN) | Dato | `xpr_producto.cantidad WHERE cod_art = línea.codArtPtB` |
| R | PT TOTAL BUENO (TN) | Calc | `A + B` |
| V | REPROCESO final (TN) | Dato | `XProductionUlexita.reprocesoFinalTn` |
| W | Kpm bentonita | Calc | `Ley_PT / Ley_MP_con_bentonita` |
| X | Kpm merma | Calc | `(Kpm_bentonita − 1) − Diluyente / (PT × mermaFactor × Kpa) + 1` |
| Y | Kpa | Calc | `GRANULADO / PT_TOTAL_BUENO` |
| Z | MERMA (TN) | Calc | `(Kpm_merma − 1) × mermaFactor × Kpa × PT_TOTAL_BUENO` |
| AA | % MERMA | Calc | `MERMA / (MERMA + GRANULADO)` |
| AB | OBSERVACIONES | Dato | `xproduction.observation` (existente) |

### 3.2 Constante mermaFactor

La constante `1.03` del Excel se modela como `ProductionLine.mermaFactor` (configurable, default `1.03`). Aplica únicamente a las fórmulas de Kpm merma (X) y MERMA (Z).

### 3.3 Validaciones

- `leyMpBentonita`, `leyPt`, `productoGranuladoTn`, `consumoReprocesoTn`, `reprocesoFinalTn`, `diluyenteTotalTn` ≥ 0.
- `bentonitaPct` ∈ [0, 100].
- Al aprobar la producción se exige que `leyMpBentonita`, `leyPt` y `productoGranuladoTn` estén poblados (gating).
- Si `bentonitaPct` o `diluyenteTotalTn` se dejan vacíos, la capa de cálculo los deriva de los insumos.

## 4. Modelo de datos

### 4.1 Cambios en `xpr_linea` (ProductionLine)

```sql
ALTER TABLE xpr_linea ADD report_template_code VARCHAR(20) NULL;
ALTER TABLE xpr_linea ADD cod_art_mp_principal VARCHAR(20) NULL;
ALTER TABLE xpr_linea ADD cod_art_pt_a         VARCHAR(20) NULL;
ALTER TABLE xpr_linea ADD cod_art_pt_b         VARCHAR(20) NULL;
ALTER TABLE xpr_linea ADD cod_art_diluy_bent   VARCHAR(20) NULL;
ALTER TABLE xpr_linea ADD cod_art_diluy_caolin VARCHAR(20) NULL;
ALTER TABLE xpr_linea ADD merma_factor         DECIMAL(10,4) NOT NULL DEFAULT 1.03;
```

Valores admitidos en `report_template_code`: `ULEXITA`, `MOLIENDA` (futuro), `NULL` (línea sin reporte específico).

### 4.2 Nueva tabla `xpr_produccion_ulexita`

```sql
CREATE TABLE xpr_produccion_ulexita (
  idproduccion_ulexita    BIGINT       NOT NULL,
  idproduccion            BIGINT       NOT NULL,
  ley_mp_bentonita        DECIMAL(14,4) NULL,
  ley_pt                  DECIMAL(14,4) NULL,
  producto_granulado_tn   DECIMAL(14,4) NULL,
  consumo_reproceso_tn    DECIMAL(14,4) NULL,
  reproceso_final_tn      DECIMAL(14,4) NULL,
  diluyente_total_tn      DECIMAL(14,4) NULL,
  bentonita_pct           DECIMAL(8,4)  NULL,
  ulex_disponible_snap    DECIMAL(14,4) NULL,
  consumo_mp_calc_snap    DECIMAL(14,4) NULL,
  observacion_lab         VARCHAR(500) NULL,
  VERSION                 BIGINT       NULL,
  idcompania              VARCHAR(2)   NOT NULL,
  CONSTRAINT pk_xpr_produccion_ulexita PRIMARY KEY (idproduccion_ulexita),
  CONSTRAINT uk_xpr_produccion_ulexita UNIQUE (idproduccion),
  CONSTRAINT fk_xpr_prod_ulexita_prod
    FOREIGN KEY (idproduccion) REFERENCES xpr_produccion(idproduccion)
);
```

Snapshots (`ulex_disponible_snap`, `consumo_mp_calc_snap`) se persisten al aprobar la orden, garantizando que el reporte histórico no cambie aunque se modifique inventario o leyes posteriores.

### 4.2.1 Snapshots completos (v6.0.77)

Al aprobar la orden se snapshotea **todos** los valores calculados para inmutabilidad histórica. Migración SQL en `query/query_v6.0.77.sql`:

```sql
ALTER TABLE xpr_produccion_ulexita
    ADD COLUMN merma_factor_snap     DECIMAL(10,4),  -- factor 1.03 vigente al aprobar
    ADD COLUMN diluyente_total_snap  DECIMAL(14,4),
    ADD COLUMN bentonita_pct_snap    DECIMAL(8,4),
    ADD COLUMN caolin_pct_snap       DECIMAL(8,4),
    ADD COLUMN pt_a_snap             DECIMAL(14,4),
    ADD COLUMN pt_b_snap             DECIMAL(14,4),
    ADD COLUMN pt_total_bueno_snap   DECIMAL(14,4),
    ADD COLUMN kpa_snap              DECIMAL(14,6),
    ADD COLUMN kpm_bentonita_snap    DECIMAL(14,6),
    ADD COLUMN kpm_merma_snap        DECIMAL(14,6),
    ADD COLUMN ley_mp_recalc_snap    DECIMAL(14,4),
    ADD COLUMN merma_snap            DECIMAL(14,4),
    ADD COLUMN merma_pct_snap        DECIMAL(8,4),
    ADD COLUMN snap_at               DATETIME,       -- timestamp del ultimo snap
    ADD COLUMN snap_by               VARCHAR(4);     -- financesCode del usuario
```

**Política de lectura/escritura**:

| Estado de la orden | Cómo se obtienen los cálculos |
|--------------------|------------------------------|
| Pendiente | `XProductionUlexitaCalc` calcula en vivo desde inputs y configuración actual de la línea |
| Aprobada | Se leen los `*_snap` (los cálculos en pantalla y reporte son idénticos al momento del aprobado) |
| Aprobada + edición lab data (permiso `PRODUCTION_LAB_DATA:UPDATE`) | Se re-snapshotea automáticamente al guardar; `snap_at`/`snap_by` se actualizan |
| Desaprobada | Snapshots se conservan (en caso de re-aprobación, se sobreescriben con el nuevo cálculo) |

`XProductionUlexitaCalc` tiene un parámetro `forceLive` (constructor sobrecargado) que ignora snapshots cuando se llama desde `persistSnapshots`, evitando leer valores potencialmente desactualizados durante la propia generación del snapshot.

**Backfill de órdenes ya aprobadas previas a v6.0.77**: no automático. Si alguna orden aprobada anterior necesita snapshots históricos, ejecutar una rutina admin "Recalcular snapshots" (no implementada por defecto, agregar si surge la necesidad).

### 4.3 Diagrama relacional

```
xpr_linea (1) ── (N) xpr_produccion (1) ── (1) xpr_produccion_ulexita
                          │
                          ├── (N) xpr_insumo
                          ├── (N) xpr_producto
                          └── (N) xpr_manoobra
```

## 5. Capa de cálculo

Clase `XProductionUlexitaCalc` (POJO no persistido). Construida desde `XProduction` + `XProductionUlexita` + `ProductionLine` + listas de insumos y productos.

### 5.1 API pública

```java
class XProductionUlexitaCalc {
  XProductionUlexitaCalc(XProduction p, XProductionUlexita u, ProductionLine l,
                         List<XSupply> insumos, List<XProductionProduct> productos);

  // Formato y derivados directos
  String        getDia();                  // L/M/M/J/V/S/D según initDate
  BigDecimal    getConsumoMpReal();        // Σ insumos.cantidad WHERE cod_art = mpPrincipal
  BigDecimal    getDiluyenteTotal();       // override u.diluyenteTotalTn || Σ insumos diluyentes
  BigDecimal    getBentonitaPct();         // override u.bentonitaPct || (bent_qty / diluyente × 100)
  BigDecimal    getCaolinPct();            // 100 − bentonitaPct
  BigDecimal    getPtA();                  // qty PT clasificación A
  BigDecimal    getPtB();                  // qty PT clasificación B
  BigDecimal    getPtTotalBueno();         // A + B

  // Indicadores (devuelven null si faltan datos requeridos)
  BigDecimal    getKpa();                  // GRANULADO / PT_TOTAL_BUENO
  BigDecimal    getKpmBentonita();         // ley_pt / ley_mp_bentonita
  BigDecimal    getKpmMerma();             // (Kpm_b − 1) − diluyente / (PT × mermaFactor × Kpa) + 1
  BigDecimal    getMerma();                // (Kpm_m − 1) × mermaFactor × Kpa × PT
  BigDecimal    getMermaPct();             // MERMA / (MERMA + GRANULADO)
  BigDecimal    getLeyMpRecalc();          // ley_pt / Kpm_bentonita
  BigDecimal    getConsumoMpCalc();        // MERMA + Kpa × PT
}
```

### 5.2 Manejo de divisiones por cero

Cualquier denominador cero o nulo retorna `null` en el getter respectivo. La UI y el reporte muestran celda vacía. Nunca se persisten `null` propagados como cero.

### 5.3 Pruebas unitarias

Verificar las fórmulas contra los valores reales del Excel original (filas 9–12). Tolerancia `1e−6`.

## 6. Cambios en la pantalla `production.xhtml`

### 6.1 Nuevo tab "Datos del Proceso"

Renderizado solo si `productionLine.reportTemplateCode == 'ULEXITA'`. Tres secciones:

**Sección 1 — Producción del día**
- ULEX disponible (read-only, query inventario; botón refrescar).
- PRODUCTO GRANULADO (TN) [input numérico].
- Consumo Reproceso (TN) [input numérico].
- Reproceso final (TN) [input numérico].
- Diluyente total (TN) [input opcional, placeholder muestra valor derivado de insumos].
- Bentonita % [input numérico, validación 0–100; placeholder con valor derivado].

**Sección 2 — Datos de Laboratorio** (resaltada en color amarillo claro como en el Excel)
- Ley MP con bentonita [input numérico].
- Ley PT [input numérico].

**Sección 3 — Cálculos en vivo** (read-only, fondo gris claro)
- Caolín %, Kpa, Kpm bentonita, Kpm merma, MERMA (TN), % MERMA, Ley MP recalculada, PT Total Bueno, Consumo MP teórico, Consumo MP real.
- Cada celda con tooltip de la fórmula en texto.
- Color rojo claro de fondo si un input dependiente está vacío; gris si todos los inputs requeridos existen.

Cada input dispara `a4j:support event="onblur" reRender="ulexitaCalcPanel"` para refresco inmediato sin perder estado.

### 6.2 Botón "Vista previa Excel"

Botón en la barra de acciones del tab. Abre `rich:modalPanel` mostrando la fila tal como saldría en el reporte mensual, con los valores actuales del formulario. Usa la misma `XProductionUlexitaCalc`.

### 6.3 Compatibilidad

Los tabs existentes (Insumo, Material, Productos Terminados, Mano de Obra) no se modifican. La estructura del `rich:tabPanel` se extiende con el nuevo tab al final.

### 6.4 Permisos

- `PRODUCTION:UPDATE` permite editar datos del proceso mientras la orden está pendiente.
- Nuevo permiso `PRODUCTION_LAB_DATA:UPDATE` permite editar `leyMpBentonita`, `leyPt` y `observacionLab` incluso después de aprobada (uso típico: laboratorio cierra leyes con retraso).

## 7. Pantalla maestra `productionLine.xhtml`

Agregar campos:

- Combo `Template de reporte` (`—`, `ULEXITA`, `MOLIENDA`).
- Suggestion `Materia prima principal` (cod_art).
- Suggestion `PT clasificación A` (cod_art).
- Suggestion `PT clasificación B` (cod_art).
- Suggestion `Diluyente bentonita` (cod_art).
- Suggestion `Diluyente caolín` (cod_art).
- Input numérico `Factor de merma` (default 1.03, 4 decimales).

Visibilidad condicional: los campos específicos aparecen cuando `Template de reporte != —`. Para `ULEXITA` se muestran todos los campos descritos.

## 8. Reporte mensual a Excel

### 8.1 Tecnología

**Apache POI (HSSF) cargando una plantilla `.xls` precargada**. Misma librería que ya usa `ProductInventoryReportAction.exportarExcel()` (`HSSFWorkbook`/`HSSFSheet`/`HSSFCellStyle`). El JAR `poi-3.5-FINAL-20090928.jar` ya está en `lib/`; no se introducen dependencias nuevas.

Patrón de **plantilla** en lugar de construir todo en código: el `.xls` se carga desde disco con cabecera, colores, merges y fórmulas ya definidos en Excel; el código solo escribe las filas de datos reusando los `HSSFCellStyle` capturados de una fila modelo.

La plantilla original del usuario (`.xlsx`) se guarda como `.xls` desde Excel — formato binario clásico que preserva todo el styling visual (colores, merges, fórmulas, anchos, fuentes). Para 31 filas/mes está muy por debajo del límite HSSF (65 535 filas).

**Único formato soportado**: XLS. No se ofrece PDF (decisión confirmada por el usuario). Si en el futuro se requiere XLSX nativo, se agrega `poi-ooxml` + `xmlbeans` y se cambia `HSSFWorkbook` por `HSSFWorkbook` (el resto del código se mantiene).

### 8.2 Plantilla

Archivo `view/xproduction/reports/dailyProductionUlexita.xls`, versionado en git. Es una copia del Excel original "REPORTE DIARIO DE PRODUCCION ULEXITA" guardada en formato `.xls` con:

- Cabecera multinivel (filas 1-7) intacta: títulos, "Insumo / insumos", "Cálculo", "Ingreso producción", "Dato laboratorio", "GRUPO D/N", "PRODUCTO TN A/B".
- Colores por tipo de columna: azul claro = calculados, amarillo claro = dato laboratorio, naranja = ULEX disponible y MERMA, etc.
- Merged cells respetadas.
- Anchos de columna y altura de filas.
- Fila modelo (fila 9) con todos los `CellStyle` aplicados a cada columna del cuerpo (formato numérico con decimales, alineación, bordes). Esta fila se usa como template de estilo.
- Fila SALDO ANTERIOR (fila 8) opcional según diseño final.
- Fila TOTAL MES al final con fórmulas `=SUM(...)` que el código reescribe al rango real generado.

El usuario funcional puede abrir esta plantilla en Excel y modificar colores, anchos, agregar columnas, sin tocar código Java. Solo se requiere mantener la posición de columnas cuyos índices están referenciados en el código.

### 8.3 Acción y pantalla

- Pantalla nueva `view/xproduction/dailyProductionReport.xhtml` con filtros: año, mes, línea de producción, opción "Mostrar días sin producción".
- Acción `UlexitaDailyReportAction` (Seam, scope PAGE) con método `generateReport()`.
- Botón "Exportar Excel".

### 8.4 Algoritmo de generación

```java
public void generateReport() {
    // 1. Cargar producciones del mes
    List<XProduction> producciones = xproductionService.findByMonth(productionLine, year, month);

    // 2. Construir filas de datos
    List<UlexitaReportRow> rows = new ArrayList<UlexitaReportRow>();
    for (XProduction p : producciones) {
        XProductionUlexita u = xproductionUlexitaService.findByProduction(p);
        XProductionUlexitaCalc calc = new XProductionUlexitaCalc(p, u, productionLine,
                                          p.getSupplyList(), p.getProductionProductList());
        rows.add(buildRow(p, u, calc));
    }
    if (showEmptyDays) {
        rows = padWithEmptyDays(rows, year, month);
    }

    // 3. Cargar plantilla
    InputStream is = new FileInputStream(JSFUtil.getRealPath(
        "/xproduction/reports/dailyProductionUlexita.xls"));
    HSSFWorkbook wb = new HSSFWorkbook(is);
    HSSFSheet sheet = wb.getSheetAt(0);

    // 4. Capturar estilos de la fila modelo (fila 9 en la plantilla)
    HSSFRow templateRow = sheet.getRow(TEMPLATE_ROW_INDEX);
    Map<Integer, HSSFCellStyle> styles = captureStyles(templateRow);

    // 5. Escribir N filas reales reusando estilos
    int rowIdx = DATA_START_ROW;
    for (UlexitaReportRow r : rows) {
        HSSFRow row = sheet.createRow(rowIdx++);
        writeCell(row, COL_FECHA, r.getFecha(), styles.get(COL_FECHA));
        writeCell(row, COL_DIA, r.getDia(), styles.get(COL_DIA));
        writeCell(row, COL_ULEX_DISP, r.getUlexDisp(), styles.get(COL_ULEX_DISP));
        writeCell(row, COL_CONSUMO_CALC, r.getConsumoMpCalc(), styles.get(COL_CONSUMO_CALC));
        // ... resto de columnas
    }

    // 6. Escribir fila TOTAL MES con fórmulas SUM
    HSSFRow totalRow = sheet.createRow(rowIdx);
    writeFormulaCell(totalRow, COL_ULEX_DISP, "SUM(D9:D" + rowIdx + ")");
    writeFormulaCell(totalRow, COL_CONSUMO_CALC, "SUM(E9:E" + rowIdx + ")");
    // ... resto de columnas físicas (H, K, O, P, Q, R, V, Z)
    // Columnas calculadas (Kpa, Kpm bent/merma, %MERMA, Leyes, Caolín%) → en blanco

    // 7. Forzar recálculo de fórmulas al abrir
    wb.setForceFormulaRecalculation(true);

    // 8. Stream al response
    HttpServletResponse resp = (HttpServletResponse) FacesContext.getCurrentInstance()
        .getExternalContext().getResponse();
    resp.setContentType("application/vnd.ms-excel");
    resp.setHeader("Content-Disposition",
        "attachment; filename=\"reporte_diario_ulexita_" + year + "_" + month + ".xls\"");
    wb.write(resp.getOutputStream());
    FacesContext.getCurrentInstance().responseComplete();
}
```

### 8.5 Reglas de escritura de celdas

- `BigDecimal` no nulo → `cell.setCellValue(value.doubleValue())` con estilo numérico.
- `BigDecimal` nulo o calculado con división por cero → celda en blanco (sin `setCellValue`).
- Strings → `setCellValue(string)`.
- Fechas → `setCellValue(date)` con estilo de fecha (`dd-MMM`).
- GRUPO (D/N) → escribir "X" en columna D si turno=DIA, "X" en columna N si NOCHE; la otra queda vacía.

### 8.6 Snapshots vs. live

- ULEX DISPONIBLE (col D) y CONSUMO MP CALC (col E): si la producción está aprobada, se usan los valores guardados en `XProductionUlexita.ulexDisponibleSnap` y `consumoMpCalcSnap`. Para producciones pendientes el reporte recalcula en vivo.
- Esto garantiza que un reporte histórico no cambie aunque se modifique el inventario o las leyes después.

### 8.7 Constantes de columnas

Definidas como constantes en `UlexitaDailyReportAction` (deben coincidir con los índices reales de la plantilla):

```java
private static final int TEMPLATE_ROW_INDEX = 8;   // fila 9 en Excel (modelo de estilos)
private static final int DATA_START_ROW     = 8;   // empieza a escribir aquí
private static final int COL_FECHA          = 1;   // B
private static final int COL_DIA            = 2;   // C
private static final int COL_ULEX_DISP      = 3;   // D
private static final int COL_CONSUMO_CALC   = 4;   // E
private static final int COL_GRUPO_D        = 5;   // F
private static final int COL_GRUPO_N        = 6;   // G
private static final int COL_DILUYENTE      = 7;   // H
private static final int COL_BENT_PCT       = 8;   // I
private static final int COL_CAOL_PCT       = 9;   // J
private static final int COL_REPROC_IN      = 10;  // K
private static final int COL_LEY_MP_BENT    = 11;  // L
private static final int COL_LEY_RECALC     = 12;  // M
private static final int COL_LEY_PT         = 13;  // N
private static final int COL_GRANULADO      = 14;  // O
private static final int COL_PT_A           = 15;  // P
private static final int COL_PT_B           = 16;  // Q
private static final int COL_PT_BUENO       = 17;  // R
private static final int COL_REPROC_OUT     = 21;  // V (hay columnas vacías de separación visual)
private static final int COL_KPM_BENT       = 22;  // W
private static final int COL_KPM_MERMA      = 23;  // X
private static final int COL_KPA            = 24;  // Y
private static final int COL_MERMA          = 25;  // Z
private static final int COL_MERMA_PCT      = 26;  // AA
private static final int COL_OBS            = 27;  // AB
```

## 9. Pantalla de configuración de inventario

No requiere cambios. La capa de cálculo consume el saldo actual de inventario para mostrar `ulexDisponible` en pantalla; el módulo `warehouse` ya expone esa información.

## 10. Internacionalización

Agregar a `messages_es.properties`:

```
XProduction.tab.ulexitaData=Datos del Proceso
XProduction.ulexita.section.production=Producción del día
XProduction.ulexita.section.lab=Datos de Laboratorio
XProduction.ulexita.section.calc=Cálculos
XProduction.ulexita.ulexAvailable=ULEX disponible (TN)
XProduction.ulexita.granulated=Producto granulado (TN)
XProduction.ulexita.reprocessIn=Consumo reproceso (TN)
XProduction.ulexita.reprocessOut=Reproceso final (TN)
XProduction.ulexita.diluentTotal=Diluyente total (TN)
XProduction.ulexita.bentonitePct=Proporción bentonita (%)
XProduction.ulexita.kaolinPct=Proporción caolín (%)
XProduction.ulexita.leyMpBent=Ley MP con bentonita
XProduction.ulexita.leyPt=Ley PT
XProduction.ulexita.leyMpRecalc=Ley MP recalculada
XProduction.ulexita.kpa=Kpa
XProduction.ulexita.kpmBent=Kpm bentonita
XProduction.ulexita.kpmMerma=Kpm merma
XProduction.ulexita.merma=MERMA (TN)
XProduction.ulexita.mermaPct=% MERMA
XProduction.ulexita.ptTotalGood=PT total bueno (TN)
XProduction.ulexita.consumoMpReal=Consumo MP real (TN)
XProduction.ulexita.consumoMpCalc=Consumo MP teórico (TN)
XProduction.ulexita.previewExcel=Vista previa Excel
ProductionLine.reportTemplateCode=Template de reporte
ProductionLine.codArtMpPrincipal=Materia prima principal
ProductionLine.codArtPtA=PT clasificación A
ProductionLine.codArtPtB=PT clasificación B
ProductionLine.codArtDiluyBent=Diluyente bentonita
ProductionLine.codArtDiluyCaolin=Diluyente caolín
ProductionLine.mermaFactor=Factor de merma
DailyProductionReport.title=Reporte Diario de Producción
DailyProductionReport.month=Mes
DailyProductionReport.year=Año
DailyProductionReport.line=Línea de producción
DailyProductionReport.showEmptyDays=Mostrar días sin producción
```

## 11. Plan de entrega (orden de implementación)

1. Migración SQL: alteración de `xpr_linea` y creación de `xpr_produccion_ulexita`.
2. Entidad JPA `XProductionUlexita` y ampliación de `ProductionLine`.
3. Capa de cálculo `XProductionUlexitaCalc` con suite de pruebas unitarias contra el Excel original.
4. Pantalla `productionLine.xhtml` ampliada con los nuevos campos.
5. Tab "Datos del Proceso" en `production.xhtml`; carga, persistencia y refresh en `XProductionAction`.
6. Modal de vista previa Excel (reusa la capa de cálculo).
7. Plantilla JXLS `dailyProductionUlexita.xls`, `UlexitaDailyReportAction` y pantalla de filtros.
8. Permisos `PRODUCTION_LAB_DATA:UPDATE` y gating en aprobación.
9. i18n `messages_es.properties`.
10. Pruebas integradas en QA con datos reales de noviembre 2024 (o el mes que provea el usuario).

## 12. Riesgos y mitigaciones

| Riesgo | Mitigación |
|--------|-----------|
| Cambio futuro de fórmulas | Centralización en `XProductionUlexitaCalc`; un único punto de modificación. |
| Inconsistencia entre planilla manual y sistema | Vista previa Excel en pantalla antes de aprobar; validación de campos requeridos al aprobar. |
| Saldo de inventario cambia tras aprobar | Snapshot persistido en `ulex_disponible_snap` y `consumo_mp_calc_snap` al aprobar. |
| División por cero en cálculos | Getters retornan `null`; UI y reporte muestran celda vacía. |
| Extensión a MOLIENDA con campos distintos | Nueva entidad satélite `XProductionMolienda` y nueva calc class; sin cambios al núcleo. |
| Datos de laboratorio se conocen tarde | Permiso separado `PRODUCTION_LAB_DATA:UPDATE` permite edición post-aprobación. |

## 13. Criterios de aceptación

1. Configurada la línea ULEXITA con los seis cod_art y `mermaFactor=1.03`, una orden de producción muestra el tab "Datos del Proceso".
2. Capturando los 6 valores de la fila 1 del Excel original (01-abr), los cálculos en vivo coinciden con el Excel con tolerancia `1e−4`.
3. Aprobada la orden, los snapshots `ulex_disponible_snap` y `consumo_mp_calc_snap` quedan poblados.
4. La vista previa Excel muestra la fila idéntica a la del reporte mensual.
5. El reporte mensual genera el Excel con la fila 01-abr coincidente con el Excel original (mismas celdas pobladas, mismos valores).
6. Múltiples producciones del mismo día aparecen como filas consecutivas en el reporte.
7. Fila TOTAL MES suma correctamente las 10 columnas físicas y deja vacías las calculadas.
8. Una línea distinta (sin `reportTemplateCode`) no muestra el tab nuevo y opera igual que antes.

## 14. Fuera del alcance / trabajos futuros

- Plantilla y entidad para línea MOLIENDA.
- Promedios ponderados por PT en la fila TOTAL MES.
- Gráficos históricos de Kpa / %MERMA.
- Exportación PDF.
- Dashboard de producción en tiempo real.
- Migración de datos históricos previos a la implementación (no se requiere).

---

## 15. Bitácora de implementación y decisiones (v6.0.76 + v6.0.77)

Este apartado documenta todo lo implementado, los problemas encontrados, las decisiones tomadas y los patrones reutilizables para futuras líneas (MOLIENDA, etc.). Es la **referencia operativa** para mantenimiento y extensión.

### 15.1 Estructura final de archivos

| Capa | Archivo | Propósito |
|------|---------|-----------|
| Migración SQL | [query/query_v6.0.76.sql](../query/query_v6.0.76.sql) | ALTER `xpr_linea` (7 cols) + CREATE `xpr_produccion_ulexita` + permisos `PRODUCTION_LAB_DATA`, `PRODUCTION_LABOR` |
| Migración SQL | [query/query_v6.0.77.sql](../query/query_v6.0.77.sql) | ALTER `xpr_produccion_ulexita` con 15 columnas snapshot |
| Entidad | [src/main/com/encens/khipus/model/xproduction/ProductionLine.java](../src/main/com/encens/khipus/model/xproduction/ProductionLine.java) | Campos de configuración por línea + helper `isUlexitaTemplate()` |
| Entidad | [src/main/com/encens/khipus/model/xproduction/XProductionUlexita.java](../src/main/com/encens/khipus/model/xproduction/XProductionUlexita.java) | Datos de proceso/laboratorio + 15 snapshots + `hasSnapshots()` |
| Servicio | [src/main/com/encens/khipus/service/xproduction/XProductionUlexitaService.java](../src/main/com/encens/khipus/service/xproduction/XProductionUlexitaService.java) | API: `findByProduction`, `save`, `findProductionsByLineAndMonth`, `persistSnapshots` |
| Servicio | [src/main/com/encens/khipus/service/xproduction/XProductionUlexitaServiceBean.java](../src/main/com/encens/khipus/service/xproduction/XProductionUlexitaServiceBean.java) | Implementación. `persistSnapshots` usa `forceLive=true` en el calc |
| Cálculos | [src/main/com/encens/khipus/service/xproduction/XProductionUlexitaCalc.java](../src/main/com/encens/khipus/service/xproduction/XProductionUlexitaCalc.java) | Capa pura de cálculo, snapshot-aware, conversión KG→TN automática |
| Acción | [src/main/com/encens/khipus/action/xproduction/XProductionAction.java](../src/main/com/encens/khipus/action/xproduction/XProductionAction.java) | Cargar/persistir ULEXITA, agregar PT directo, snapshot al aprobar y al re-editar |
| Acción reporte | [src/main/com/encens/khipus/action/xproduction/UlexitaDailyReportAction.java](../src/main/com/encens/khipus/action/xproduction/UlexitaDailyReportAction.java) | Genera Excel mensual con Apache POI HSSF (sin plantilla externa) |
| Vista | [view/xproduction/production.xhtml](../view/xproduction/production.xhtml) | Layout completo: header con datos calculados + 4 tabs (Insumo, Material, Datos del Proceso, Productos Terminados / Mano de Obra) |
| Vista | [view/xproduction/productionLine.xhtml](../view/xproduction/productionLine.xhtml) | CRUD línea con 6 campos de configuración condicional (visible si template = ULEXITA) |
| Vista | [view/xproduction/dailyProductionReportUlexita.xhtml](../view/xproduction/dailyProductionReportUlexita.xhtml) | Pantalla de filtros (línea, año, mes, mostrar días vacíos) |
| Menú | [view/layout/menu.xhtml](../view/layout/menu.xhtml) | Entrada "Reporte Producción Diaria ULEXITA" en módulo Producción → Reportes |
| i18n | [resources/messages_app.properties](../resources/messages_app.properties) | ~50 claves nuevas con prefijo `XProduction.ulexita.*`, `ProductionLine.*`, `DailyProductionReport.*`, `Production.addFinishedProduct` |

### 15.2 Migración SQL — orden de ejecución

```bash
# Ejecutar en orden, después de mergear la rama
mysql -u root -p khipus < query/query_v6.0.76.sql
mysql -u root -p khipus < query/query_v6.0.77.sql
```

`v6.0.76` debe correr ANTES de `v6.0.77` porque éste último depende de la tabla creada en aquél.

### 15.3 Configuración inicial post-deploy

Una vez aplicada la migración, configurar la línea ULEXITA en pantalla `productionLine.xhtml`:

| Campo | Valor para planta actual |
|-------|--------------------------|
| Template de reporte | `ULEXITA` |
| Cod. artículo MP principal | `5` (ULEXITA) |
| Cod. artículo PT clasificación A | `1255` (BORO 10 - A) |
| Cod. artículo PT clasificación B | `1261` (BORO 10 - B) |
| Cod. artículo diluyente bentonita | `7` (BENTONITA) |
| Cod. artículo diluyente caolín | (vacío si no aplica) |
| Factor de merma | `1.0300` |

Asignar permisos a roles:
- `PRODUCTION_LAB_DATA:UPDATE` → rol Laboratorio (para editar leyes post-aprobación).
- `PRODUCTION_LABOR:VIEW/UPDATE/DELETE` → rol RRHH si quieren la pestaña Mano de Obra; sino dejar sin asignar para esconderla.

### 15.4 Patrón de extensión a otras líneas (ej. MOLIENDA)

Cuando llegue MOLIENDA, replicar este patrón:

1. **Migración SQL nueva** (ej. `v6.X.Y.sql`):
   - Si MOLIENDA tiene campos de proceso/lab distintos: nueva tabla `xpr_produccion_molienda` con sus columnas + 15 columnas `_snap` + `snap_at` + `snap_by`.
   - Si comparte la mayoría de los campos con ULEXITA: reusar `xpr_produccion_ulexita` agregando solo lo nuevo (no recomendado por nombres confusos).
2. **Entidad** `XProductionMolienda.java` análoga a `XProductionUlexita`.
3. **Capa de cálculo** `XProductionMoliendaCalc.java` con sus fórmulas específicas. Heredar el patrón `useSnapshots()`/`forceLive` para inmutabilidad.
4. **Servicio** `XProductionMoliendaService(Bean).java` con `findByProduction`, `save`, `persistSnapshots`.
5. **Constantes en línea**: agregar (si necesita) más cods de artículo a `xpr_linea` o reusar los existentes.
6. **`reportTemplateCode = 'MOLIENDA'`** en `ProductionLine` + helper `isMoliendaTemplate()`.
7. **Vista**: en `production.xhtml` agregar bloques condicionales `rendered="#{xproductionAction.moliendaTemplate}"` con sus inputs.
8. **Acción `XProductionAction`**: agregar `moliendaData`, `loadMoliendaData()`, `persistMoliendaData()`, llamar a `persistSnapshots` correspondiente en `approve()` y `update()`.
9. **Reporte**: nueva clase `MoliendaDailyReportAction` (puede reusar mucho de `UlexitaDailyReportAction`; considerar extraer un padre abstracto `AbstractDailyReportAction`).
10. **Permisos**: reusar `PRODUCTION_LAB_DATA` o crear `PRODUCTION_LAB_DATA_MOLIENDA` si se necesita granularidad por línea.

### 15.5 Patrones técnicos reutilizables

#### 15.5.1 Conversión KG → TN automática

Los artículos en `inv_articulos` están en KG (o la unidad que tengan). Los cálculos del reporte trabajan en TN. La capa `XProductionUlexitaCalc` convierte automáticamente leyendo `productItem.usageMeasureCode`:

```java
private static BigDecimal convertToTn(BigDecimal value, String unit) {
    if (value == null) return null;
    if (UNIT_KG.equalsIgnoreCase(unit)) {
        return value.divide(ONE_THOUSAND, SCALE, RoundingMode.HALF_UP);
    }
    return value;
}
```

Aplica a `getConsumoMpReal`, `getDiluyenteTotal` (cuando deriva de insumos), `getPtA`, `getPtB`. Si el artículo está en TN, no convierte. Pattern extensible para otras unidades (KG ↔ TN ↔ G).

#### 15.5.2 Snapshot-aware getters

Cada getter de cálculo retorna snap si la orden está aprobada y el snap está populado:

```java
public BigDecimal getKpa() {
    if (useSnapshots()) return ulexita.getKpaSnap();
    // ... live calc
}

private boolean useSnapshots() {
    if (forceLive) return false;
    if (ulexita == null || !ulexita.hasSnapshots()) return false;
    return production != null && production.isApproved();
}
```

`forceLive=true` solo se usa dentro de `persistSnapshots` para evitar leer snaps al generarlos.

#### 15.5.3 Persistencia inmediata de PT con cantidad 0 + edición diferida de cantidad

`addFinishedProductsDirect()` persiste cada PT inmediatamente (`em.persist`) con `quantity=0`. Esto resuelve el problema de "lazy collection re-init" al pasar la entidad al EJB stateless (Hibernate descartaba in-memory adds). Las cantidades editadas inline se persisten al "Guardar" vía el loop `em.merge` existente en `updateProduction`.

#### 15.5.4 Borrado físico en `removeProductionProduct`

Cambiado de `setProduction(null) + em.merge` (desvincular) a `em.remove(em.merge(product))` (borrado físico). Esto refleja el modelo nuevo donde "una fila = un PT real" sin la dualidad plan/asignación. El panel viejo "Asignar Producto Terminado" quedó comentado en `production.xhtml` (no eliminado por trazabilidad).

#### 15.5.5 Default de fecha inicio

`select()` setea `initDate = productionPlan.date 00:00:00` si está nulo. Garantiza que el campo siempre tenga un valor razonable cuando el operador entra a una orden vieja.

### 15.6 Permisos creados

| Código | idmodulo | bitmask | Descripción |
|--------|----------|---------|-------------|
| `PRODUCTION_LAB_DATA` | 11 | 4 (UPDATE) | Edita leyes MP/PT y observación lab incluso después de aprobar la orden |
| `PRODUCTION_LABOR` | 11 | 13 (VIEW + UPDATE + DELETE) | Habilita el botón "+ Mano Obra" y la pestaña "Mano de Obra" |

Asignación: vía pantalla Administración → Roles. Permisos no asignados a ningún rol = funcionalidad oculta para todos.

### 15.7 Lecciones aprendidas / gotchas para futuras implementaciones

#### 15.7.1 NUNCA usar `c:if` envolviendo componentes JSF/RichFaces

`c:if` (JSTL) muta el árbol JSF en build-time. JSF guarda estado de los componentes con autoIDs. Si el árbol cambia entre requests, los autoIDs fallan a resolverse → error `AjaxUpdate component not found for id: :form:j_idXXXX`.

**Bien**: `rendered="#{...}"` en el componente JSF.
**Mal**: `<c:if test="..."><componente jsf/></c:if>`.

Si necesitás envolver, usá `<a4j:outputPanel layout="none" rendered="...">` — pero **cuidado**: cambia el padre del componente envuelto, lo que rompe `a4j:support` (que se enlaza al padre directo del input).

#### 15.7.2 `a4j:support` debe ser hijo directo del input que escucha

Si lo envolvés en cualquier otro componente (incluido `a4j:outputPanel`), el handler se enlaza al wrapper, no al input → no dispara.

**Solución**: usar `rendered` directamente en `a4j:support`:

```xml
<h:inputText ...>
    <app:realNumberConverter forId="..."/>
    <a4j:support event="onblur" ajaxSingle="true"
                 rendered="#{!xproductionAction.approved}"
                 reRender="..."/>
</h:inputText>
```

#### 15.7.3 `reRender` debe incluir el panel que contiene los inputs si querés que se re-formateen

JSF + custom converters: al disparar blur ajax, el converter formatea el valor en el modelo, pero el OTRO input solo se re-renderiza si está en la lista de `reRender`. Si solo re-renderás el panel de cálculos derivados, los inputs editados quedan con el texto crudo del usuario hasta el próximo full submit.

**Patrón de Insumos/Materiales** (correcto):
```xml
reRender="ingredientPanel, totalCostField, totalRawMaterialField"
```
`ingredientPanel` contiene los inputs → al re-renderizarlo, todos se reformatean.

#### 15.7.4 `RealNumberConverter` requiere `forId` para retornar `BigDecimal`

Sin `forId`, el converter retorna `Double` o `Long` → `IllegalArgumentException: argument type mismatch` al setear en field BigDecimal. Patrón obligatorio:

```xml
<h:inputText id="qtyInput" value="#{bean.bigDecimalField}">
    <app:realNumberConverter pattern="..." forId="qtyInput"/>
</h:inputText>
```

#### 15.7.5 Locale español en converter — separador decimal = coma

Los inputs aceptan `2,1` no `2.1`. Si el usuario escribe punto, el converter lo rechaza con error y JSF conserva los valores submitted en TODOS los inputs hasta que se corrijan los inválidos. Es comportamiento estándar JSF, no bug del módulo. Coherente con todo SISK13 (Costo Total muestra `554.372,52`).

#### 15.7.6 EJB stateless + lazy collections = peligro

Si pasás una entidad JPA con lazy collections a un EJB stateless, Hibernate puede re-inicializar la collection al primer acceso dentro del EJB y descartar adiciones en memoria del action. **Solución**: persistir nuevos elementos inmediatamente vía service method (no diferir a un `updateProduction` posterior que itera la collection).

#### 15.7.7 `idcompania` es BIGINT, NO VARCHAR

`Company.id` es `Long`. Tablas que tienen FK a `compania` usan `idcompania BIGINT`. **No confundir con `no_cia VARCHAR(2)`** que es el código de compañía usado en otras tablas legacy. Hibernate falla con `Wrong column type ... Found: varchar, expected: bigint` si la migración usa varchar.

#### 15.7.8 Snapshots completos al aprobar — patrón empresarial

Calcular y persistir TODOS los valores derivados al aprobar (no solo algunos) garantiza inmutabilidad histórica. Permite:
- Cambiar parámetros de configuración (`merma_factor`, articulos PT) sin afectar historia.
- Refactorizar fórmulas en código sin reescribir reportes pasados.
- Auditoría regulatoria: el reporte de cualquier mes es exactamente el que se reportó entonces.
- Comparabilidad entre períodos.

Costo: ~16 columnas extra por tabla satélite. Beneficio: invariancia total. **Recomendado para todas las líneas futuras**.

### 15.8 UX final — distribución del formulario

```
┌────────────────────────────────── 05/05/2026 ──────────────────────────────────┐
│ Estado          Pendiente               ┌─ Datos Calculados ──────────────┐    │
│ Línea Prod*     [L.PROD.ULEXITA ▾]      │ Consumo MP teórico  41.99       │    │
│ Formulación*    [FORM.ULEXITA 01 ▾]     │ Caolín %             0.00       │    │
│ Grupo*          [Grupo 1 ▾]             │ Ley MP recalculada  29.20       │    │
│ Turno*          [Turno Noche ▾]         │ PT total bueno      29.00       │    │
│ Costo Total     554.372,52              │ Kpm bentonita        1.11       │    │
│ M.P. Usada      41.900,00 KG            │ Kpm merma            1.10       │    │
│ Inicio*         [05/05/2026 00:00] 📅   │ Kpa                  1.31       │    │
│ Fin*            [05/05/2026 12:00] 📅   │ MERMA (TN)           3.99       │    │
│                                         │ % MERMA              0.10       │    │
│                                         └─────────────────────────────────┘    │
│                                                                                │
│                                         Observaciones [_________________]      │
└────────────────────────────────────────────────────────────────────────────────┘
[ Insumo | Material | Datos del Proceso | Productos Terminados | Mano de Obra ]
```

- Lado izquierdo: campos maestros + fechas + costos.
- Lado derecho: panel de cálculos siempre visible (fondo gris), 2 columnas (5 izq + 4 der).
- Observaciones: textarea debajo del panel de cálculos, label izquierdo + textarea ancho completo.
- Tab "Datos del Proceso": 2 secciones (Datos de Producción a la izquierda + Datos de Laboratorio a la derecha), labels alineados a 50/50, inputs al 45% del ancho de su columna.

### 15.9 Comportamiento cuando la orden está aprobada

| Componente | Comportamiento |
|------------|----------------|
| Selects (Grupo, Turno) | `disabled="true"` |
| Calendarios (Inicio, Fin) | `disabled="true"` + `readonly="true"` |
| Inputs numéricos (cantidades, leyes, granulado, etc.) | `readonly="true"` |
| Textareas (observaciones) | `readonly="true"`, excepción lab data si tiene permiso `PRODUCTION_LAB_DATA:UPDATE` |
| `a4j:support onblur` | `rendered="false"` → no genera handler, no hay "cargando" al perder foco |
| Botones (+ Material, + Insumo, + PT, + Mano Obra, Recalcular, Aprobar) | `rendered="false"` |
| Botón Desaprobar | `rendered="true"` |
| Cálculos | Leen de snapshots `*_snap` |

Edición de leyes lab post-aprobación: si el usuario tiene `PRODUCTION_LAB_DATA:UPDATE`, los inputs `leyMpBentonita`, `leyPt`, `observacionLab` quedan editables. El botón "Guardar" sigue oculto pero como las inputs aún tienen `value="#{...}"`, el form lo procesa. **Limitación actual**: no hay un botón "Guardar leyes" dedicado. Si esto se vuelve crítico, agregar uno con `rendered="#{xproductionAction.approved and s:hasPermission('PRODUCTION_LAB_DATA','UPDATE')}"` que llame `update()` (que ya re-snapshota).

### 15.10 Verificación rápida post-deploy

```sql
-- 1. Verificar tablas
DESC xpr_produccion_ulexita;
DESC xpr_linea;

-- 2. Verificar permisos
SELECT codigo, descripcion, idmodulo, permiso
  FROM funcionalidad
 WHERE codigo IN ('PRODUCTION_LAB_DATA', 'PRODUCTION_LABOR');

-- 3. Verificar configuración línea ULEXITA
SELECT idlinea, codigo, nombre,
       report_template_code, cod_art_mp_principal,
       cod_art_pt_a, cod_art_pt_b,
       cod_art_diluy_bent, cod_art_diluy_caolin,
       merma_factor
  FROM xpr_linea
 WHERE codigo LIKE '%ULEX%';

-- 4. Verificar snapshots de orden aprobada (después de aprobar una)
SELECT idproduccion, ulex_disponible_snap, kpa_snap, merma_snap, merma_pct_snap,
       snap_at, snap_by
  FROM xpr_produccion_ulexita
 WHERE snap_at IS NOT NULL
 ORDER BY snap_at DESC LIMIT 5;
```

### 15.11 Integración con el reporte mensual Excel

`UlexitaDailyReportAction.generateReport()`:
1. Cargar producciones del mes/año/línea ordenadas por `initDate, id`.
2. Por cada producción crear `XProductionUlexitaCalc`. Si la orden está aprobada, los getters retornan automáticamente los snaps (sin código adicional en el report action).
3. Construir el .xls con Apache POI HSSF (sin plantilla externa) — cabecera multinivel, colores, merges programáticos.
4. Fila TOTAL MES suma columnas físicas; calculadas en blanco.
5. Stream al response con `application/vnd.ms-excel`.

Si querés migrar a plantilla `.xls` externa (más fácil de mantener visualmente), solo cambiar `generateReport()` para cargar la plantilla con `new HSSFWorkbook(new FileInputStream(...))` y reusar las celdas modelo. Ver sección 8.4 del documento para el patrón.

### 15.12 Trabajos futuros sugeridos (consecuencia de esta implementación)

1. **Backfill de snapshots históricos**: si surgen órdenes aprobadas pre-v6.0.77 sin snaps, agregar pantalla admin con botón "Recalcular snapshots" que itere y dispare `persistSnapshots`.
2. **Botón "Guardar leyes" para lab post-aprobación**: visible solo con `PRODUCTION_LAB_DATA:UPDATE`, llama a `update()`.
3. **Vista previa Excel inline**: modal con la fila tal como saldría en el reporte, antes de aprobar.
4. **Saldo de inventario en vivo**: campo "ULEX disponible" debería autocompletarse desde el módulo `warehouse` con el saldo a la fecha de la producción. Hoy es input manual.
5. **Validación gating al aprobar**: spec 3.3 menciona requerir `leyMpBentonita`, `leyPt`, `productoGranuladoTn` no nulos al aprobar. No está implementado para no bloquear pruebas.
6. **Línea MOLIENDA**: aplicar el patrón de la sección 15.4.
