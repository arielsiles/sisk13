package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.XProduction;
import com.encens.khipus.model.xproduction.XProductionUlexita;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.xproduction.XProductionUlexitaCalc;
import com.encens.khipus.service.xproduction.XProductionUlexitaService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Reporte mensual diario de produccion ULEXITA.
 *
 * Genera un Excel (.xls) con una fila por orden de produccion del mes,
 * replicando el formato de la planilla manual "REPORTE DIARIO DE PRODUCCION ULEXITA".
 *
 * Usa Apache POI HSSF (jar ya presente en lib/). Construye la cabecera multinivel,
 * colores y merges programaticamente; las fechas sin produccion se omiten salvo
 * que se active showEmptyDays.
 */
@Name("ulexitaDailyReportAction")
@Scope(ScopeType.PAGE)
public class UlexitaDailyReportAction {

    @Logger
    private Log log;

    @In
    private XProductionUlexitaService xproductionUlexitaService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    private Integer year;
    private Integer month;
    private ProductionLine productionLine;
    private boolean showEmptyDays = false;

    // Indices de columnas en el Excel (0-based)
    // Columnas del Excel generado. Sin huecos: cada indice es una columna usada.
    // Las columnas S, T, U del Excel manual original eran solo separadores
    // visuales; aqui se eliminan para compactar el reporte.
    private static final int COL_FECHA       = 1;   // B
    private static final int COL_DIA         = 2;   // C
    private static final int COL_ULEX_DISP   = 3;   // D
    private static final int COL_CONSUMO     = 4;   // E
    private static final int COL_GRUPO_D     = 5;   // F
    private static final int COL_GRUPO_N     = 6;   // G
    private static final int COL_DILUYENTE   = 7;   // H
    private static final int COL_BENT_PCT    = 8;   // I
    private static final int COL_CAOL_PCT    = 9;   // J
    private static final int COL_REPROC_IN   = 10;  // K
    private static final int COL_LEY_MP_BENT = 11;  // L
    private static final int COL_LEY_RECALC  = 12;  // M
    private static final int COL_LEY_PT      = 13;  // N
    private static final int COL_GRANULADO   = 14;  // O
    private static final int COL_PT_A        = 15;  // P
    private static final int COL_PT_B        = 16;  // Q
    private static final int COL_PT_BUENO    = 17;  // R
    private static final int COL_REPROC_OUT  = 18;  // S
    private static final int COL_KPM_BENT    = 19;  // T
    private static final int COL_KPM_MERMA   = 20;  // U
    private static final int COL_KPA         = 21;  // V
    private static final int COL_MERMA       = 22;  // W
    private static final int COL_MERMA_PCT   = 23;  // X
    private static final int COL_OBS         = 24;  // Y

    private static final int LAST_COL        = 24;
    // Layout de filas:
    //   fila 0 (Excel 1): titulo (centrado, mergeado todas las cols)
    //   fila 1 (Excel 2): periodo (desde col B)
    //   fila 2 (Excel 3): blank
    //   fila 3 (Excel 4): cabecera principal — para F/G mergea horizontal con
    //                     "GRUPO"; para P/Q mergea horizontal con "PRODUCTO (TN)";
    //                     resto de columnas mergea vertical hasta fila 4
    //   fila 4 (Excel 5): subcabecera D/N en F/G y A/B en P/Q; resto vacio
    //                     (parte de las celdas mergeadas verticalmente)
    //   fila 5+ (Excel 6+): datos
    private static final int HEADER_ROW1     = 3;
    private static final int HEADER_ROW2     = 4;
    private static final int SALDO_ROW       = 5;   // fila "SALDO ANT."
    private static final int DATA_START_ROW  = 6;

    @Create
    public void init() {
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH) + 1;
    }

    public void generateReport() {
        if (productionLine == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DailyProductionReport.error.lineRequired");
            return;
        }
        if (year == null || month == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DailyProductionReport.error.periodRequired");
            return;
        }
        try {
            CompanyConfiguration cc = companyConfigurationService.findCompanyConfiguration();
            List<XProduction> producciones =
                    xproductionUlexitaService.findProductionsByLineAndMonth(productionLine, year, month);

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("ULEXITA " + month + "-" + year);
            sheet.setDisplayGridlines(false);

            Styles styles = buildStyles(wb);
            buildHeader(sheet, styles, cc);

            int rowIdx = DATA_START_ROW;
            BigDecimal[] totals = new BigDecimal[LAST_COL + 1];

            for (XProduction p : producciones) {
                XProductionUlexita u = xproductionUlexitaService.findByProduction(p);
                XProductionUlexitaCalc calc = new XProductionUlexitaCalc(
                        p, u, p.getProductionLine(), p.getSupplyList(), p.getProductionProductList());
                writeDataRow(sheet, rowIdx++, p, u, calc, styles);
                accumulate(totals, p, u, calc);
            }

            writeTotalsRow(sheet, rowIdx, totals, styles);

            for (int i = 0; i <= LAST_COL; i++) {
                sheet.setColumnWidth(i, columnWidthFor(i));
            }

            sendResponse(wb);
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "CompanyConfiguration.notFound");
        } catch (Exception e) {
            log.error("Error generando reporte ULEXITA", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DailyProductionReport.error.generic");
        }
    }

    // ------------------------------------------------------------------ cabecera

    private void buildHeader(HSSFSheet sheet, Styles s, CompanyConfiguration cc) {
        // Fila 0: titulo principal (mergeado de B en adelante)
        HSSFRow r0 = sheet.createRow(0);
        HSSFCell cTitle = r0.createCell(COL_FECHA);
        cTitle.setCellValue("REPORTE DIARIO DE PRODUCCION \"" + safeUpper(productionLine.getName()) + "\"");
        cTitle.setCellStyle(s.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, COL_FECHA, LAST_COL));

        // Fila 1: periodo (desde col B = COL_FECHA)
        HSSFRow r1 = sheet.createRow(1);
        r1.createCell(COL_FECHA).setCellValue("Periodo: " + monthName(month) + " " + year);

        // Fila 2: blank (espacio visual)

        // Fila 3 + 4: cabecera de dos filas con merges.
        //   - F/G: row1 "GRUPO" mergeado horizontal; row2 "D"/"N"
        //   - P/Q: row1 "PRODUCTO (TN)" mergeado horizontal; row2 "A"/"B"
        //   - Resto de columnas: row1 con el nombre, mergeado verticalmente con row2
        HSSFRow rh1 = sheet.createRow(HEADER_ROW1);
        HSSFRow rh2 = sheet.createRow(HEADER_ROW2);
        // Alto de cabecera: 28 puntos (~37 px), igual al Excel manual original
        rh1.setHeightInPoints(28f);
        rh2.setHeightInPoints(28f);

        // Columnas con merge vertical (rh1 + rh2 misma columna)
        putHeader(sheet, rh1, rh2, COL_FECHA,       "FECHA",                          s.header);
        putHeader(sheet, rh1, rh2, COL_DIA,         "DIA",                            s.header);
        putHeader(sheet, rh1, rh2, COL_ULEX_DISP,   "ULEX DISPONIBLE",                s.header);
        putHeader(sheet, rh1, rh2, COL_CONSUMO,     "CONSUMO MATERIA PRIMA ULEX (TN)",s.header);
        putHeader(sheet, rh1, rh2, COL_DILUYENTE,   "Diluyente añadido (TN)",         s.header);
        putHeader(sheet, rh1, rh2, COL_BENT_PCT,    "Proporcion bentonita en el diluyente %", s.header);
        putHeader(sheet, rh1, rh2, COL_CAOL_PCT,    "Proporcion caolin en el diluyente %", s.header);
        putHeader(sheet, rh1, rh2, COL_REPROC_IN,   "Consumo Reproceso (TN)",         s.header);
        putHeader(sheet, rh1, rh2, COL_LEY_MP_BENT, "Ley MP con bentonita",           s.header);
        putHeader(sheet, rh1, rh2, COL_LEY_RECALC,  "Ley MP recalculada",             s.header);
        putHeader(sheet, rh1, rh2, COL_LEY_PT,      "Ley PT",                         s.header);
        putHeader(sheet, rh1, rh2, COL_GRANULADO,   "PRODUCTO GRANULADO (TN)",        s.header);
        putHeader(sheet, rh1, rh2, COL_PT_BUENO,    "PT TOTAL BUENO (TN)",            s.header);
        putHeader(sheet, rh1, rh2, COL_REPROC_OUT,  "REPROCESO final (TN)",           s.header);
        putHeader(sheet, rh1, rh2, COL_KPM_BENT,    "Kpm bentonita",                  s.header);
        putHeader(sheet, rh1, rh2, COL_KPM_MERMA,   "Kpm merma",                      s.header);
        putHeader(sheet, rh1, rh2, COL_KPA,         "Kpa",                            s.header);
        putHeader(sheet, rh1, rh2, COL_MERMA,       "MERMA (TN)",                     s.header);
        putHeader(sheet, rh1, rh2, COL_MERMA_PCT,   "% MERMA",                        s.header);
        putHeader(sheet, rh1, rh2, COL_OBS,         "OBSERVACIONES",                  s.header);

        // GRUPO: row1 mergeado F-G, row2 separadas D/N
        setText(rh1, COL_GRUPO_D, "GRUPO", s.header);
        setText(rh1, COL_GRUPO_N, null, s.header);
        sheet.addMergedRegion(new CellRangeAddress(HEADER_ROW1, HEADER_ROW1, COL_GRUPO_D, COL_GRUPO_N));
        setText(rh2, COL_GRUPO_D, "D", s.header);
        setText(rh2, COL_GRUPO_N, "N", s.header);

        // PRODUCTO (TN): row1 mergeado P-Q, row2 separadas A/B
        setText(rh1, COL_PT_A, "PRODUCTO (TN)", s.header);
        setText(rh1, COL_PT_B, null, s.header);
        sheet.addMergedRegion(new CellRangeAddress(HEADER_ROW1, HEADER_ROW1, COL_PT_A, COL_PT_B));
        setText(rh2, COL_PT_A, "A", s.header);
        setText(rh2, COL_PT_B, "B", s.header);

        // Fila SALDO: etiqueta en col B, resto vacio pero con borde en toda la fila
        HSSFRow rs = sheet.createRow(SALDO_ROW);
        setText(rs, COL_FECHA, "SALDO", s.header);
        for (int col = COL_DIA; col <= LAST_COL; col++) {
            setText(rs, col, null, s.body);
        }

        // Pin del encabezado al hacer scroll vertical
        sheet.createFreezePane(0, DATA_START_ROW);
    }

    /** Escribe un header con merge vertical (rh1+rh2 misma columna). */
    private static void putHeader(HSSFSheet sheet, HSSFRow rh1, HSSFRow rh2, int col,
                                  String value, HSSFCellStyle style) {
        setText(rh1, col, value, style);
        setText(rh2, col, null, style);
        sheet.addMergedRegion(new CellRangeAddress(HEADER_ROW1, HEADER_ROW2, col, col));
    }

    // ------------------------------------------------------------------ filas de datos

    private void writeDataRow(HSSFSheet sheet, int rowIdx, XProduction p, XProductionUlexita u,
                              XProductionUlexitaCalc calc, Styles s) {
        HSSFRow row = sheet.createRow(rowIdx);
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM");
        setText(row, COL_FECHA, fmt.format(p.getInitDate()), s.body);
        setText(row, COL_DIA, calc.getDia(), s.bodyCenter);

        BigDecimal ulexDisp = (p.isApproved() && u != null) ? u.getUlexDisponibleSnap() : null;
        setNumber(row, COL_ULEX_DISP, ulexDisp, s.body);

        BigDecimal consumoCalc = (p.isApproved() && u != null && u.getConsumoMpCalcSnap() != null)
                ? u.getConsumoMpCalcSnap() : calc.getConsumoMpCalc();
        setNumber(row, COL_CONSUMO, consumoCalc, s.body);

        setText(row, COL_GRUPO_D, calc.isShiftDay() ? "X" : "", s.bodyCenter);
        setText(row, COL_GRUPO_N, calc.isShiftNight() ? "X" : "", s.bodyCenter);

        setNumber(row, COL_DILUYENTE,   calc.getDiluyenteTotal(),  s.body);
        setNumber(row, COL_BENT_PCT,    calc.getBentonitaPct(),    s.body);
        setNumber(row, COL_CAOL_PCT,    calc.getCaolinPct(),       s.body);
        setNumber(row, COL_REPROC_IN,   u != null ? u.getConsumoReprocesoTn() : null, s.body);
        setNumber(row, COL_LEY_MP_BENT, u != null ? u.getLeyMpBentonita() : null,     s.body);
        setNumber(row, COL_LEY_RECALC,  calc.getLeyMpRecalc(),     s.body);
        setNumber(row, COL_LEY_PT,      u != null ? u.getLeyPt() : null,              s.body);
        setNumber(row, COL_GRANULADO,   u != null ? u.getProductoGranuladoTn() : null,s.body);
        setNumber(row, COL_PT_A,        calc.getPtA(),             s.body);
        setNumber(row, COL_PT_B,        calc.getPtB(),             s.body);
        setNumber(row, COL_PT_BUENO,    calc.getPtTotalBueno(),    s.body);
        setNumber(row, COL_REPROC_OUT,  u != null ? u.getReprocesoFinalTn() : null,   s.body);
        setNumber(row, COL_KPM_BENT,    calc.getKpmBentonita(),    s.body);
        setNumber(row, COL_KPM_MERMA,   calc.getKpmMerma(),        s.body);
        setNumber(row, COL_KPA,         calc.getKpa(),             s.body);
        setNumber(row, COL_MERMA,       calc.getMerma(),           s.body);
        setNumber(row, COL_MERMA_PCT,   calc.getMermaPct(),        s.bodyPct);

        if (p.getObservation() != null) {
            setText(row, COL_OBS, p.getObservation(), s.body);
        }
    }

    private void accumulate(BigDecimal[] totals, XProduction p, XProductionUlexita u, XProductionUlexitaCalc calc) {
        addToTotals(totals, COL_ULEX_DISP, (p.isApproved() && u != null) ? u.getUlexDisponibleSnap() : null);
        addToTotals(totals, COL_CONSUMO,
                (p.isApproved() && u != null && u.getConsumoMpCalcSnap() != null)
                        ? u.getConsumoMpCalcSnap() : calc.getConsumoMpCalc());
        addToTotals(totals, COL_DILUYENTE,  calc.getDiluyenteTotal());
        addToTotals(totals, COL_REPROC_IN,  u != null ? u.getConsumoReprocesoTn() : null);
        addToTotals(totals, COL_GRANULADO,  u != null ? u.getProductoGranuladoTn() : null);
        addToTotals(totals, COL_PT_A,       calc.getPtA());
        addToTotals(totals, COL_PT_B,       calc.getPtB());
        addToTotals(totals, COL_PT_BUENO,   calc.getPtTotalBueno());
        addToTotals(totals, COL_REPROC_OUT, u != null ? u.getReprocesoFinalTn() : null);
        addToTotals(totals, COL_MERMA,      calc.getMerma());
    }

    private void writeTotalsRow(HSSFSheet sheet, int rowIdx, BigDecimal[] totals, Styles s) {
        HSSFRow row = sheet.createRow(rowIdx);
        // Etiqueta "TOTAL MES:" mergeada en B-C
        setText(row, COL_FECHA, "TOTAL MES:", s.totalsLabel);
        setText(row, COL_DIA, null, s.totalsLabel);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, COL_FECHA, COL_DIA));
        // Recorrer todas las columnas para asegurar que cada celda tenga borde,
        // tanto las que tienen suma como las vacias (calculadas).
        for (int col = COL_ULEX_DISP; col <= LAST_COL; col++) {
            if (totals[col] != null) {
                setNumber(row, col, totals[col], s.totals);
            } else {
                setText(row, col, null, s.totals);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void setText(HSSFRow row, int col, String value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private static void setNumber(HSSFRow row, int col, BigDecimal value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
        if (style != null) cell.setCellStyle(style);
    }

    private static void addToTotals(BigDecimal[] totals, int col, BigDecimal value) {
        if (value == null) return;
        totals[col] = (totals[col] == null) ? value : totals[col].add(value);
    }

    private int columnWidthFor(int col) {
        // POI usa unidades de 1/256 de char-width.
        // Conversion px -> POI: poiUnits ≈ (px - 5) * 256 / 7
        //   25 px ≈ 915,  30 px ≈ 914,  75 px ≈ 2560,  96 px ≈ 3328
        switch (col) {
            case 0:                                                   return 915;   // A — margen, 25 px
            case COL_FECHA:                                           return 2400;  // B — FECHA
            case COL_DIA:                                             return 1200;  // C — DIA
            case COL_ULEX_DISP:                                                     // D
            case COL_CONSUMO:                                                       // E
            case COL_DILUYENTE:                                                     // H
            case COL_BENT_PCT:                                                      // I
            case COL_CAOL_PCT:                                                      // J
            case COL_REPROC_IN:                                                     // K
            case COL_LEY_MP_BENT:                                                   // L
            case COL_LEY_RECALC:    return 3328;                                    // M — 96 px
            case COL_GRUPO_D:                                                       // F
            case COL_GRUPO_N:       return 1100;                                    // G — 30 px
            case COL_LEY_PT:                                                        // N
            case COL_PT_A:                                                          // P
            case COL_PT_B:                                                          // Q
            case COL_KPM_BENT:                                                      // T
            case COL_KPM_MERMA:                                                     // U
            case COL_KPA:                                                           // V
            case COL_MERMA:                                                         // W
            case COL_MERMA_PCT:     return 2560;                                    // X — 75 px
            case COL_OBS:           return 8000;                                    // Y — Observaciones
            default:                return 3600;                                    // O, R, S — defaults
        }
    }

    private static String safeUpper(String s) {
        return s == null ? "" : s.toUpperCase();
    }

    private static String monthName(int m) {
        String[] n = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                      "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        return (m >= 1 && m <= 12) ? n[m - 1] : "";
    }

    private void sendResponse(HSSFWorkbook wb) throws Exception {
        HttpServletResponse resp = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();
        resp.setContentType("application/vnd.ms-excel");
        String fname = "reporte_diario_" + (productionLine != null ? safeFileName(productionLine.getCode()) : "")
                + "_" + year + "_" + month + ".xls";
        resp.addHeader("Content-disposition", "attachment; filename=" + fname);
        ServletOutputStream out = resp.getOutputStream();
        wb.write(out);
        out.flush();
        out.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    private static String safeFileName(String s) {
        return s == null ? "linea" : s.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    // ------------------------------------------------------------------ estilos

    private static class Styles {
        HSSFCellStyle title;
        HSSFCellStyle header;
        HSSFCellStyle body, bodyCenter, bodyPct;
        HSSFCellStyle totals, totalsLabel;
    }

    private Styles buildStyles(HSSFWorkbook wb) {
        Styles s = new Styles();
        HSSFDataFormat fmt = wb.createDataFormat();
        short num2 = fmt.getFormat("#,##0.00");   // todos los numericos a 2 decimales
        short pct  = fmt.getFormat("0.00%");      // % tambien a 2 decimales

        HSSFFont bold = wb.createFont();
        bold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        HSSFFont titleFont = wb.createFont();
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        titleFont.setFontHeightInPoints((short) 14);

        s.title = wb.createCellStyle();
        s.title.setFont(titleFont);
        s.title.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        // Header sin color de fondo, bordes finos, alineado centro, wrap text
        s.header = wb.createCellStyle();
        s.header.setFont(bold);
        s.header.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        s.header.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        s.header.setWrapText(true);
        applyBorders(s.header);

        // Body numerico (2 decimales, alineado derecha)
        s.body = wb.createCellStyle();
        s.body.setDataFormat(num2);
        s.body.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        applyBorders(s.body);

        // Body texto centrado (DIA, GRUPO D/N)
        s.bodyCenter = wb.createCellStyle();
        s.bodyCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        applyBorders(s.bodyCenter);

        // Body porcentaje (2 decimales)
        s.bodyPct = wb.createCellStyle();
        s.bodyPct.setDataFormat(pct);
        s.bodyPct.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        applyBorders(s.bodyPct);

        // Totales: bold, sin color de fondo (solo bordes y bold para distinguir)
        s.totals = wb.createCellStyle();
        s.totals.setDataFormat(num2);
        s.totals.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        s.totals.setFont(bold);
        applyBorders(s.totals);

        s.totalsLabel = wb.createCellStyle();
        s.totalsLabel.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        s.totalsLabel.setFont(bold);
        applyBorders(s.totalsLabel);

        return s;
    }

    private static void applyBorders(HSSFCellStyle st) {
        st.setBorderTop(HSSFCellStyle.BORDER_THIN);
        st.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        st.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        st.setBorderRight(HSSFCellStyle.BORDER_THIN);
    }

    // ------------------------------------------------------------------ getters / setters

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public ProductionLine getProductionLine() { return productionLine; }
    public void setProductionLine(ProductionLine productionLine) { this.productionLine = productionLine; }

    public boolean isShowEmptyDays() { return showEmptyDays; }
    public void setShowEmptyDays(boolean showEmptyDays) { this.showEmptyDays = showEmptyDays; }
}
