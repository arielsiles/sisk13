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
import org.apache.poi.hssf.util.HSSFColor;
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
    private static final int COL_FECHA       = 1;
    private static final int COL_DIA         = 2;
    private static final int COL_ULEX_DISP   = 3;
    private static final int COL_CONSUMO     = 4;
    private static final int COL_GRUPO_D     = 5;
    private static final int COL_GRUPO_N     = 6;
    private static final int COL_DILUYENTE   = 7;
    private static final int COL_BENT_PCT    = 8;
    private static final int COL_CAOL_PCT    = 9;
    private static final int COL_REPROC_IN   = 10;
    private static final int COL_LEY_MP_BENT = 11;
    private static final int COL_LEY_RECALC  = 12;
    private static final int COL_LEY_PT      = 13;
    private static final int COL_GRANULADO   = 14;
    private static final int COL_PT_A        = 15;
    private static final int COL_PT_B        = 16;
    private static final int COL_PT_BUENO    = 17;
    private static final int COL_REPROC_OUT  = 21;
    private static final int COL_KPM_BENT    = 22;
    private static final int COL_KPM_MERMA   = 23;
    private static final int COL_KPA         = 24;
    private static final int COL_MERMA       = 25;
    private static final int COL_MERMA_PCT   = 26;
    private static final int COL_OBS         = 27;

    private static final int LAST_COL        = 27;
    private static final int HEADER_ROWS     = 7;
    private static final int DATA_START_ROW  = 7;

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
        // Fila 0: titulo principal
        HSSFRow r0 = sheet.createRow(0);
        HSSFCell c0 = r0.createCell(0);
        c0.setCellValue("REPORTE DIARIO DE PRODUCCION \"" + safeUpper(productionLine.getName()) + "\"");
        c0.setCellStyle(s.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, LAST_COL));

        // Fila 1: periodo
        HSSFRow r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("Periodo: " + monthName(month) + " " + year + "    Empresa: " + cc.getCompanyName());

        // Filas 4-6: cabecera de columnas (3 niveles agrupados)
        // Nivel 1 (fila 4): leyendas de tipo
        HSSFRow r4 = sheet.createRow(4);
        setText(r4, COL_ULEX_DISP, "Saldo materias primas", s.legendOrange);
        setText(r4, COL_CONSUMO, "Calculo", s.legendBlue);
        setText(r4, COL_DILUYENTE, "Insumo", s.legendDark);
        setText(r4, COL_LEY_MP_BENT, "Dato laboratorio", s.legendYellow);
        setText(r4, COL_LEY_PT, "Dato laboratorio", s.legendYellow);
        setText(r4, COL_KPM_BENT, "Calculo", s.legendBlue);

        // Nivel 2 (fila 5): grupos
        HSSFRow r5 = sheet.createRow(5);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, COL_GRUPO_D, COL_GRUPO_N));
        setText(r5, COL_GRUPO_D, "GRUPO", s.headerCenter);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, COL_PT_A, COL_PT_B));
        setText(r5, COL_PT_A, "PRODUCTO (TN)", s.headerCenter);

        // Nivel 3 (fila 6): nombres de columna
        HSSFRow r6 = sheet.createRow(6);
        setText(r6, COL_FECHA,       "FECHA",                          s.headerCenter);
        setText(r6, COL_DIA,         "DIA",                            s.headerCenter);
        setText(r6, COL_ULEX_DISP,   "ULEX DISPONIBLE",                s.headerOrange);
        setText(r6, COL_CONSUMO,     "CONSUMO MATERIA PRIMA ULEX (TN)",s.headerBlue);
        setText(r6, COL_GRUPO_D,     "D",                              s.headerCenter);
        setText(r6, COL_GRUPO_N,     "N",                              s.headerCenter);
        setText(r6, COL_DILUYENTE,   "Diluyente añadido (TN)",         s.headerCenter);
        setText(r6, COL_BENT_PCT,    "Proporcion bentonita %",         s.headerCenter);
        setText(r6, COL_CAOL_PCT,    "Proporcion caolin %",            s.headerBlue);
        setText(r6, COL_REPROC_IN,   "Consumo Reproceso (TN)",         s.headerCenter);
        setText(r6, COL_LEY_MP_BENT, "Ley MP con bentonita",           s.headerYellow);
        setText(r6, COL_LEY_RECALC,  "Ley MP recalculada",             s.headerBlue);
        setText(r6, COL_LEY_PT,      "Ley PT",                         s.headerYellow);
        setText(r6, COL_GRANULADO,   "PRODUCTO GRANULADO (TN)",        s.headerCenter);
        setText(r6, COL_PT_A,        "A",                              s.headerCenter);
        setText(r6, COL_PT_B,        "B",                              s.headerCenter);
        setText(r6, COL_PT_BUENO,    "PT TOTAL BUENO (TN)",            s.headerBlue);
        setText(r6, COL_REPROC_OUT,  "REPROCESO final (TN)",           s.headerCenter);
        setText(r6, COL_KPM_BENT,    "Kpm bentonita",                  s.headerBlue);
        setText(r6, COL_KPM_MERMA,   "Kpm merma",                      s.headerBlue);
        setText(r6, COL_KPA,         "Kpa",                            s.headerBlue);
        setText(r6, COL_MERMA,       "MERMA (TN)",                     s.headerOrange);
        setText(r6, COL_MERMA_PCT,   "% MERMA",                        s.headerBlue);
        setText(r6, COL_OBS,         "OBSERVACIONES",                  s.headerCenter);

        sheet.createFreezePane(0, HEADER_ROWS);
    }

    // ------------------------------------------------------------------ filas de datos

    private void writeDataRow(HSSFSheet sheet, int rowIdx, XProduction p, XProductionUlexita u,
                              XProductionUlexitaCalc calc, Styles s) {
        HSSFRow row = sheet.createRow(rowIdx);
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM");
        setText(row, COL_FECHA, fmt.format(p.getInitDate()), s.body);
        setText(row, COL_DIA, calc.getDia(), s.bodyCenter);

        BigDecimal ulexDisp = (p.isApproved() && u != null) ? u.getUlexDisponibleSnap() : null;
        setNumber(row, COL_ULEX_DISP, ulexDisp, s.bodyOrange);

        BigDecimal consumoCalc = (p.isApproved() && u != null && u.getConsumoMpCalcSnap() != null)
                ? u.getConsumoMpCalcSnap() : calc.getConsumoMpCalc();
        setNumber(row, COL_CONSUMO, consumoCalc, s.bodyBlue);

        setText(row, COL_GRUPO_D, calc.isShiftDay() ? "X" : "", s.bodyCenter);
        setText(row, COL_GRUPO_N, calc.isShiftNight() ? "X" : "", s.bodyCenter);

        setNumber(row, COL_DILUYENTE,   calc.getDiluyenteTotal(),  s.body);
        setNumber(row, COL_BENT_PCT,    calc.getBentonitaPct(),    s.body);
        setNumber(row, COL_CAOL_PCT,    calc.getCaolinPct(),       s.bodyBlue);
        setNumber(row, COL_REPROC_IN,   u != null ? u.getConsumoReprocesoTn() : null, s.body);
        setNumber(row, COL_LEY_MP_BENT, u != null ? u.getLeyMpBentonita() : null,     s.bodyYellow);
        setNumber(row, COL_LEY_RECALC,  calc.getLeyMpRecalc(),     s.bodyBlue);
        setNumber(row, COL_LEY_PT,      u != null ? u.getLeyPt() : null,              s.bodyYellow);
        setNumber(row, COL_GRANULADO,   u != null ? u.getProductoGranuladoTn() : null,s.body);
        setNumber(row, COL_PT_A,        calc.getPtA(),             s.body);
        setNumber(row, COL_PT_B,        calc.getPtB(),             s.body);
        setNumber(row, COL_PT_BUENO,    calc.getPtTotalBueno(),    s.bodyBlue);
        setNumber(row, COL_REPROC_OUT,  u != null ? u.getReprocesoFinalTn() : null,   s.body);
        setNumber(row, COL_KPM_BENT,    calc.getKpmBentonita(),    s.bodyBlue);
        setNumber(row, COL_KPM_MERMA,   calc.getKpmMerma(),        s.bodyBlue);
        setNumber(row, COL_KPA,         calc.getKpa(),             s.bodyBlue);
        setNumber(row, COL_MERMA,       calc.getMerma(),           s.bodyOrange);
        setNumber(row, COL_MERMA_PCT,   calc.getMermaPct(),        s.bodyBluePct);

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
        setText(row, COL_FECHA, "TOTAL MES (TN)", s.totalsLabel);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, COL_FECHA, COL_DIA));
        for (int col = 0; col <= LAST_COL; col++) {
            if (totals[col] != null) {
                setNumber(row, col, totals[col], s.totals);
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
        switch (col) {
            case COL_FECHA: return 2400;
            case COL_DIA: return 1200;
            case COL_GRUPO_D:
            case COL_GRUPO_N: return 1200;
            case COL_OBS: return 8000;
            default: return 3600;
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
        HSSFCellStyle legendOrange, legendBlue, legendYellow, legendDark;
        HSSFCellStyle headerCenter, headerOrange, headerBlue, headerYellow;
        HSSFCellStyle body, bodyCenter;
        HSSFCellStyle bodyOrange, bodyBlue, bodyBluePct, bodyYellow;
        HSSFCellStyle totals, totalsLabel;
    }

    private Styles buildStyles(HSSFWorkbook wb) {
        Styles s = new Styles();
        HSSFDataFormat fmt = wb.createDataFormat();
        short num4 = fmt.getFormat("#,##0.0000");
        short pct = fmt.getFormat("0.0%");

        HSSFFont bold = wb.createFont();
        bold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        HSSFFont titleFont = wb.createFont();
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        titleFont.setFontHeightInPoints((short) 14);

        s.title = wb.createCellStyle();
        s.title.setFont(titleFont);
        s.title.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        s.legendOrange = legendStyle(wb, HSSFColor.LIGHT_ORANGE.index);
        s.legendBlue   = legendStyle(wb, HSSFColor.LIGHT_TURQUOISE.index);
        s.legendYellow = legendStyle(wb, HSSFColor.LIGHT_YELLOW.index);
        s.legendDark   = legendStyle(wb, HSSFColor.GREY_25_PERCENT.index);

        s.headerCenter = headerStyle(wb, bold, HSSFColor.WHITE.index);
        s.headerOrange = headerStyle(wb, bold, HSSFColor.LIGHT_ORANGE.index);
        s.headerBlue   = headerStyle(wb, bold, HSSFColor.LIGHT_TURQUOISE.index);
        s.headerYellow = headerStyle(wb, bold, HSSFColor.LIGHT_YELLOW.index);

        s.body         = bodyStyle(wb, num4, HSSFColor.WHITE.index, false);
        s.bodyCenter   = bodyTextStyle(wb, HSSFColor.WHITE.index, true);
        s.bodyOrange   = bodyStyle(wb, num4, HSSFColor.LIGHT_ORANGE.index, false);
        s.bodyBlue     = bodyStyle(wb, num4, HSSFColor.LIGHT_TURQUOISE.index, false);
        s.bodyBluePct  = bodyStyle(wb, pct,  HSSFColor.LIGHT_TURQUOISE.index, false);
        s.bodyYellow   = bodyStyle(wb, num4, HSSFColor.LIGHT_YELLOW.index, false);

        s.totals = bodyStyle(wb, num4, HSSFColor.GREY_25_PERCENT.index, true);
        s.totals.setFont(bold);
        s.totalsLabel = bodyTextStyle(wb, HSSFColor.GREY_25_PERCENT.index, true);
        s.totalsLabel.setFont(bold);

        return s;
    }

    private static HSSFCellStyle legendStyle(HSSFWorkbook wb, short bg) {
        HSSFCellStyle st = wb.createCellStyle();
        st.setFillForegroundColor(bg);
        st.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        st.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        return st;
    }

    private static HSSFCellStyle headerStyle(HSSFWorkbook wb, HSSFFont bold, short bg) {
        HSSFCellStyle st = wb.createCellStyle();
        st.setFont(bold);
        st.setFillForegroundColor(bg);
        st.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        st.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        st.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        st.setWrapText(true);
        applyBorders(st);
        return st;
    }

    private static HSSFCellStyle bodyStyle(HSSFWorkbook wb, short dataFmt, short bg, boolean bold) {
        HSSFCellStyle st = wb.createCellStyle();
        st.setDataFormat(dataFmt);
        st.setFillForegroundColor(bg);
        st.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        st.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        applyBorders(st);
        return st;
    }

    private static HSSFCellStyle bodyTextStyle(HSSFWorkbook wb, short bg, boolean center) {
        HSSFCellStyle st = wb.createCellStyle();
        st.setFillForegroundColor(bg);
        st.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        st.setAlignment(center ? HSSFCellStyle.ALIGN_CENTER : HSSFCellStyle.ALIGN_LEFT);
        applyBorders(st);
        return st;
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
