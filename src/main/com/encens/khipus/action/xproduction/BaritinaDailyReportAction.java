package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.xproduction.*;
import com.encens.khipus.service.xproduction.BaritinaDailyReportService;
import com.encens.khipus.service.xproduction.XProductionBaritinaService;
import com.encens.khipus.util.BigDecimalUtil;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.jboss.seam.ScopeType;
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
import java.util.*;

/**
 * Reporte mensual diario de produccion BARITINA.
 *
 * Genera un Excel (.xls) con una fila por DIA del mes (incluidos dias sin
 * produccion, con saldos arrastrados), replicando la planilla manual
 * "REPORTE DIARIO DE PRODUCCION BARITINA".
 *
 * Fuentes:
 *  - INGRESO MP: acopio (CollectMaterial) del articulo MP de la linea.
 *  - USO MP / PT / zonas / turnos / observaciones: ordenes BARITINA del dia.
 *  - DESPACHO: despachos (WarehouseVoucherDispatch) del articulo PT.
 * Los articulos MP y PT se derivan de las ordenes (insumo por defecto y PT).
 * Las columnas de zonas son dinamicas: solo las zonas usadas en el periodo.
 */
@Name("baritinaDailyReportAction")
@Scope(ScopeType.PAGE)
public class BaritinaDailyReportAction {

    @Logger
    private Log log;

    @In
    private BaritinaDailyReportService baritinaDailyReportService;
    @In
    private XProductionBaritinaService xproductionBaritinaService;
    @In
    private FacesMessages facesMessages;

    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    private Integer year;
    private Integer month;
    private ProductionLine productionLine;

    // Columnas fijas (0-based). Las de zona y las 3 finales se calculan en runtime.
    private static final int COL_MARGIN     = 0; // A
    private static final int COL_FECHA      = 1; // B
    private static final int COL_DIA        = 2; // C
    private static final int COL_INGRESO    = 3; // D
    private static final int COL_USO        = 4; // E
    private static final int COL_SALDO_MP   = 5; // F
    private static final int COL_PT         = 6; // G
    private static final int COL_SALDO_BAR  = 7; // H
    private static final int COL_DESPACHO   = 8; // I
    private static final int COL_ZONE_START = 9; // J ...

    private static final int TITLE_ROW   = 0;
    private static final int PERIOD_ROW  = 1;
    private static final int HEADER_ROW  = 3;
    private static final int SALDO_ROW   = 4;
    private static final int DATA_START  = 5;

    public void generateReport() {
        if (productionLine == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DailyProductionReport.error.lineRequired");
            return;
        }
        if (year == null || month == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DailyProductionReport.error.periodRequired");
            return;
        }
        try {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(year, month - 1, 1, 0, 0, 0);
            Date firstDay = c.getTime();
            int lastDayNum = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            c.add(Calendar.MONTH, 1);
            Date nextMonth = c.getTime();

            // 1. Ordenes: todas hasta nextMonth (para derivar articulos y saldo inicial)
            List<XProduction> allOrders = baritinaDailyReportService.findProductions(productionLine, new Date(0), nextMonth);

            // 2. Articulos derivados de las ordenes
            String mpCodArt = deriveMpCodArt(allOrders);
            Set<String> productCodArts = derivePtCodArts(allOrders);

            // 3. Agregacion por dia (periodo) desde las ordenes
            DayData[] days = new DayData[lastDayNum + 1];
            for (int d = 1; d <= lastDayNum; d++) days[d] = new DayData();
            Map<Long, ProductiveZone> zoneById = new LinkedHashMap<Long, ProductiveZone>();

            BigDecimal beforeUsoTn = BigDecimal.ZERO;
            BigDecimal beforePtTn = BigDecimal.ZERO;

            for (XProduction p : allOrders) {
                BigDecimal usoTn = tn(defaultInputQty(p));
                BigDecimal ptTn = tn(sumPt(p));
                if (p.getInitDate() == null) continue;
                if (p.getInitDate().before(firstDay)) {
                    beforeUsoTn = BigDecimalUtil.sum(beforeUsoTn, usoTn, 6);
                    beforePtTn = BigDecimalUtil.sum(beforePtTn, ptTn, 6);
                    continue;
                }
                if (!p.getInitDate().before(nextMonth)) continue;

                Calendar dc = Calendar.getInstance();
                dc.setTime(p.getInitDate());
                int day = dc.get(Calendar.DAY_OF_MONTH);
                DayData dd = days[day];
                dd.hasProduction = true;
                dd.usoTn = BigDecimalUtil.sum(dd.usoTn, usoTn, 6);
                dd.ptTn = BigDecimalUtil.sum(dd.ptTn, ptTn, 6);

                XProductionBaritina header = xproductionBaritinaService.findByProduction(p);
                if (header != null) {
                    if (header.getTurnos() != null) dd.turnos += header.getTurnos();
                    if (header.getObservacion() != null && header.getObservacion().trim().length() > 0) {
                        dd.obs = (dd.obs.length() == 0) ? header.getObservacion() : dd.obs + " | " + header.getObservacion();
                    }
                }
                for (XProductionBaritinaZona z : xproductionBaritinaService.findZonasByProduction(p)) {
                    if (z.getProductiveZone() == null) continue;
                    Long zid = z.getProductiveZone().getId();
                    zoneById.put(zid, z.getProductiveZone());
                    BigDecimal prev = dd.zoneTn.get(zid);
                    dd.zoneTn.put(zid, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO,
                            z.getCantidadTn() != null ? z.getCantidadTn() : BigDecimal.ZERO, 6));
                }
            }

            // 4. Acopio por dia + despacho por dia
            for (Object[] row : baritinaDailyReportService.sumAcopioByDay(mpCodArt, firstDay, nextMonth)) {
                int day = dayOfMonth((Date) row[0]);
                if (day >= 1 && day <= lastDayNum && row[1] != null) {
                    days[day].ingresoTn = BigDecimalUtil.sum(days[day].ingresoTn, tn((BigDecimal) row[1]), 6);
                }
            }
            for (Object[] row : baritinaDailyReportService.dispatchRows(productCodArts, firstDay, nextMonth)) {
                int day = dayOfMonth((Date) row[0]);
                if (day >= 1 && day <= lastDayNum && row[1] != null) {
                    days[day].despachoTn = BigDecimalUtil.sum(days[day].despachoTn, tn((BigDecimal) row[1]), 6);
                }
            }

            // 5. Saldos iniciales (computados de lo anterior al mes)
            BigDecimal openMpTn = BigDecimalUtil.subtract(
                    tn(baritinaDailyReportService.sumAcopioBefore(mpCodArt, firstDay)), beforeUsoTn, 6);
            BigDecimal openProdTn = BigDecimalUtil.subtract(
                    beforePtTn, tn(baritinaDailyReportService.sumDispatchBefore(productCodArts, firstDay)), 6);

            // 6. Zonas ordenadas (columnas dinamicas)
            List<ProductiveZone> zones = new ArrayList<ProductiveZone>(zoneById.values());
            Collections.sort(zones, new Comparator<ProductiveZone>() {
                public int compare(ProductiveZone a, ProductiveZone b) {
                    String an = a.getNumber() != null ? a.getNumber() : "";
                    String bn = b.getNumber() != null ? b.getNumber() : "";
                    int r = an.compareTo(bn);
                    if (r != 0) return r;
                    return safe(a.getName()).compareTo(safe(b.getName()));
                }
            });

            // 7. Construir Excel
            int colSuma   = COL_ZONE_START + zones.size();
            int colTurnos = colSuma + 1;
            int colObs    = colTurnos + 1;
            int lastCol   = colObs;

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("BARITINA " + month + "-" + year);
            sheet.setDisplayGridlines(false);
            Styles s = buildStyles(wb);

            buildHeader(sheet, s, zones, colSuma, colTurnos, colObs, lastCol);

            // Fila SALDO ANT.: saldos iniciales (al fin del periodo anterior) de MP y producto
            HSSFRow rs = sheet.createRow(SALDO_ROW);
            setText(rs, COL_FECHA, "SALDO ANT.", s.headerLeft);
            for (int col = COL_DIA; col <= lastCol; col++) setText(rs, col, null, s.body);
            setNumber(rs, COL_SALDO_MP, openMpTn, s.num);
            setNumber(rs, COL_SALDO_BAR, openProdTn, s.num);

            // Filas de datos por dia
            BigDecimal saldoMp = openMpTn;
            BigDecimal saldoProd = openProdTn;
            BigDecimal totIngreso = BigDecimal.ZERO, totUso = BigDecimal.ZERO,
                       totPt = BigDecimal.ZERO, totDespacho = BigDecimal.ZERO;

            int rowIdx = DATA_START;
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM");
            for (int d = 1; d <= lastDayNum; d++) {
                DayData dd = days[d];
                // Se muestran TODOS los dias del mes (incluidos los sin produccion).
                Calendar dayCal = Calendar.getInstance();
                dayCal.clear();
                dayCal.set(year, month - 1, d);
                Date date = dayCal.getTime();

                saldoMp = BigDecimalUtil.subtract(BigDecimalUtil.sum(saldoMp, dd.ingresoTn, 6), dd.usoTn, 6);
                saldoProd = BigDecimalUtil.subtract(BigDecimalUtil.sum(saldoProd, dd.ptTn, 6), dd.despachoTn, 6);

                HSSFRow row = sheet.createRow(rowIdx++);
                setText(row, COL_FECHA, fmt.format(date), s.body);
                setText(row, COL_DIA, dayLetter(date), s.center);
                setNumber(row, COL_INGRESO, dd.ingresoTn, s.num);
                setNumber(row, COL_SALDO_MP, saldoMp, s.num);
                if (dd.hasProduction) setNumber(row, COL_USO, dd.usoTn, s.num); else setText(row, COL_USO, null, s.num);
                setNumber(row, COL_PT, dd.ptTn, s.num);
                setNumber(row, COL_SALDO_BAR, saldoProd, s.num);
                if (isZero(dd.despachoTn)) setText(row, COL_DESPACHO, null, s.num); else setNumber(row, COL_DESPACHO, dd.despachoTn, s.num);

                BigDecimal suma = BigDecimal.ZERO;
                for (int zi = 0; zi < zones.size(); zi++) {
                    int col = COL_ZONE_START + zi;
                    BigDecimal zoneTn = dd.zoneTn.get(zones.get(zi).getId());
                    if (dd.hasProduction && zoneTn != null && !isZero(dd.usoTn)) {
                        BigDecimal pct = BigDecimalUtil.divide(BigDecimalUtil.multiply(zoneTn, ONE_HUNDRED, 6), dd.usoTn, 4);
                        setNumber(row, col, pct, s.pct);
                        suma = BigDecimalUtil.sum(suma, pct, 4);
                    } else {
                        setText(row, col, null, s.pct);
                    }
                }
                if (dd.hasProduction) setNumber(row, colSuma, suma, s.numCenter); else setText(row, colSuma, null, s.numCenter);
                if (dd.hasProduction && dd.turnos > 0) setNumber(row, colTurnos, new BigDecimal(dd.turnos), s.center);
                else setText(row, colTurnos, null, s.center);
                setText(row, colObs, dd.obs.length() > 0 ? dd.obs : null, s.body);

                totIngreso = BigDecimalUtil.sum(totIngreso, dd.ingresoTn, 6);
                totUso = BigDecimalUtil.sum(totUso, dd.usoTn, 6);
                totPt = BigDecimalUtil.sum(totPt, dd.ptTn, 6);
                totDespacho = BigDecimalUtil.sum(totDespacho, dd.despachoTn, 6);
            }

            // Fila TOTAL
            HSSFRow rt = sheet.createRow(rowIdx);
            setText(rt, COL_FECHA, "TOTAL:", s.totalsLabel);
            setText(rt, COL_DIA, null, s.totalsLabel);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, COL_FECHA, COL_DIA));
            for (int col = COL_INGRESO; col <= lastCol; col++) setText(rt, col, null, s.totals);
            setNumber(rt, COL_INGRESO, totIngreso, s.totals);
            setNumber(rt, COL_SALDO_MP, saldoMp, s.totals);
            setNumber(rt, COL_USO, totUso, s.totals);
            setNumber(rt, COL_PT, totPt, s.totals);
            setNumber(rt, COL_SALDO_BAR, saldoProd, s.totals);
            setNumber(rt, COL_DESPACHO, totDespacho, s.totals);

            // Anchos: D,E,F = 15.00 ; G = 116px ; H..N (SALDO BARITINA..TURNOS) = 12.50
            sheet.setColumnWidth(COL_MARGIN, 915);
            sheet.setColumnWidth(COL_FECHA, 2400);
            sheet.setColumnWidth(COL_DIA, 1100);
            for (int col = COL_INGRESO; col <= COL_SALDO_MP; col++) sheet.setColumnWidth(col, excelWidth(15.0));
            sheet.setColumnWidth(COL_PT, pxWidth(116));
            for (int col = COL_SALDO_BAR; col <= colTurnos; col++) sheet.setColumnWidth(col, excelWidth(12.5));
            sheet.setColumnWidth(colObs, 8000);

            sendResponse(wb);
        } catch (Exception e) {
            log.error("Error generando reporte BARITINA", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DailyProductionReport.error.generic");
        }
    }

    // -------------------------------------------------------------- cabecera

    private void buildHeader(HSSFSheet sheet, Styles s, List<ProductiveZone> zones,
                             int colSuma, int colTurnos, int colObs, int lastCol) {
        HSSFRow r0 = sheet.createRow(TITLE_ROW);
        HSSFCell t = r0.createCell(COL_FECHA);
        t.setCellValue("REPORTE DIARIO DE PRODUCCION \"" + safe(productionLine.getName()).toUpperCase() + "\"");
        t.setCellStyle(s.title);
        sheet.addMergedRegion(new CellRangeAddress(TITLE_ROW, TITLE_ROW, COL_FECHA, lastCol));

        sheet.createRow(PERIOD_ROW).createCell(COL_FECHA).setCellValue("Periodo: " + monthName(month) + " " + year);

        HSSFRow h = sheet.createRow(HEADER_ROW);
        h.setHeightInPoints(40f);
        setText(h, COL_FECHA,     "FECHA", s.header);
        setText(h, COL_DIA,       "DIA", s.header);
        setText(h, COL_INGRESO,   "INGRESO MATERIA PRIMA BARITINA (TN)", s.header);
        setText(h, COL_SALDO_MP,  "SALDO MATERIA PRIMA BARITINA (TN)", s.header);
        setText(h, COL_USO,       "USO MATERIA PRIMA BARITINA (TN)", s.header);
        setText(h, COL_PT,        "BARITINA PRODUCTO TERMINADO (TN)", s.header);
        setText(h, COL_SALDO_BAR, "SALDO BARITINA (TN)", s.header);
        setText(h, COL_DESPACHO,  "DESPACHO BARITINA (TN)", s.header);
        for (int zi = 0; zi < zones.size(); zi++) {
            setText(h, COL_ZONE_START + zi, safe(zones.get(zi).getName()) + " %", s.header);
        }
        setText(h, colSuma,   "SUMA DE PROPORCIONES", s.header);
        setText(h, colTurnos, "TURNOS PRODUCIDOS", s.header);
        setText(h, colObs,    "OBSERVACIONES", s.header);

        sheet.createFreezePane(0, DATA_START);
    }

    // -------------------------------------------------------------- derivacion

    /** cod_art del insumo por defecto (MP principal) de la primera orden que lo tenga. */
    private String deriveMpCodArt(List<XProduction> orders) {
        for (XProduction p : orders) {
            for (XSupply sup : p.getSupplyList()) {
                if (sup.hasFormula() && Boolean.TRUE.equals(sup.getFormulationInput().getInputDefault())) {
                    return sup.getProductItemCode();
                }
            }
        }
        return null;
    }

    /** cod_art distintos de los productos terminados de las ordenes. */
    private Set<String> derivePtCodArts(List<XProduction> orders) {
        Set<String> set = new LinkedHashSet<String>();
        for (XProduction p : orders) {
            for (XProductionProduct pr : p.getProductionProductList()) {
                if (pr.getProductItemCode() != null) set.add(pr.getProductItemCode());
            }
        }
        return set;
    }

    private BigDecimal defaultInputQty(XProduction p) {
        BigDecimal q = BigDecimal.ZERO;
        for (XSupply sup : p.getSupplyList()) {
            if (sup.hasFormula() && Boolean.TRUE.equals(sup.getFormulationInput().getInputDefault())
                    && sup.getQuantity() != null) {
                q = BigDecimalUtil.sum(q, sup.getQuantity(), 6);
            }
        }
        return q;
    }

    private BigDecimal sumPt(XProduction p) {
        BigDecimal q = BigDecimal.ZERO;
        for (XProductionProduct pr : p.getProductionProductList()) {
            if (pr.getQuantity() != null) q = BigDecimalUtil.sum(q, pr.getQuantity(), 6);
        }
        return q;
    }

    // -------------------------------------------------------------- helpers

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private static BigDecimal tn(BigDecimal kg) {
        if (kg == null) return BigDecimal.ZERO;
        return BigDecimalUtil.divide(kg, THOUSAND, 6);
    }

    private static boolean isZero(BigDecimal v) {
        return v == null || v.compareTo(BigDecimal.ZERO) == 0;
    }

    private static int dayOfMonth(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    private static String dayLetter(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:    return "L";
            case Calendar.TUESDAY:   return "M";
            case Calendar.WEDNESDAY: return "M";
            case Calendar.THURSDAY:  return "J";
            case Calendar.FRIDAY:    return "V";
            case Calendar.SATURDAY:  return "S";
            case Calendar.SUNDAY:    return "D";
            default: return "";
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    /** Convierte un ancho en caracteres de Excel a unidades POI (1/256 de char + padding de 5px). */
    private static int excelWidth(double chars) {
        return (int) Math.round((chars + 5.0 / 7.0) * 256);
    }

    /** Convierte pixeles a unidades POI: poiUnits = (px - 5) * 256 / 7. */
    private static int pxWidth(int px) {
        return (int) Math.round((px - 5) * 256.0 / 7.0);
    }

    private static String monthName(int m) {
        String[] n = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                      "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        return (m >= 1 && m <= 12) ? n[m - 1] : "";
    }

    private static void setText(HSSFRow row, int col, String value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private static void setNumber(HSSFRow row, int col, BigDecimal value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value.doubleValue());
        if (style != null) cell.setCellStyle(style);
    }

    private void sendResponse(HSSFWorkbook wb) throws Exception {
        HttpServletResponse resp = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();
        resp.setContentType("application/vnd.ms-excel");
        String fname = "reporte_diario_baritina_" + year + "_" + month + ".xls";
        resp.addHeader("Content-disposition", "attachment; filename=" + fname);
        ServletOutputStream out = resp.getOutputStream();
        wb.write(out);
        out.flush();
        out.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    // -------------------------------------------------------------- estilos

    private static class Styles {
        HSSFCellStyle title, header, headerLeft, body, center, num, numCenter, pct, totals, totalsLabel;
    }

    private Styles buildStyles(HSSFWorkbook wb) {
        Styles s = new Styles();
        HSSFDataFormat fmt = wb.createDataFormat();
        short num2 = fmt.getFormat("#,##0.00");
        // Porcentaje a 2 decimales con el signo % literal (sin multiplicar x100):
        // el valor almacenado ya es 70.50 -> se muestra "70.50%".
        short pctFmt = fmt.getFormat("0.00\"%\"");

        HSSFFont bold = wb.createFont();
        bold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFFont titleFont = wb.createFont();
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        titleFont.setFontHeightInPoints((short) 14);

        s.title = wb.createCellStyle();
        s.title.setFont(titleFont);
        s.title.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        s.header = wb.createCellStyle();
        s.header.setFont(bold);
        s.header.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        s.header.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        s.header.setWrapText(true);
        applyBorders(s.header);

        s.headerLeft = wb.createCellStyle();
        s.headerLeft.setFont(bold);
        s.headerLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        applyBorders(s.headerLeft);

        s.body = wb.createCellStyle();
        s.body.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        applyBorders(s.body);

        s.center = wb.createCellStyle();
        s.center.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        applyBorders(s.center);

        s.num = wb.createCellStyle();
        s.num.setDataFormat(num2);
        s.num.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        applyBorders(s.num);

        s.numCenter = wb.createCellStyle();
        s.numCenter.setDataFormat(num2);
        s.numCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        applyBorders(s.numCenter);

        s.pct = wb.createCellStyle();
        s.pct.setDataFormat(pctFmt);
        s.pct.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        applyBorders(s.pct);

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

    /** Acumulador por dia. */
    private static class DayData {
        boolean hasProduction = false;
        BigDecimal ingresoTn = BigDecimal.ZERO;
        BigDecimal usoTn = BigDecimal.ZERO;
        BigDecimal ptTn = BigDecimal.ZERO;
        BigDecimal despachoTn = BigDecimal.ZERO;
        int turnos = 0;
        String obs = "";
        Map<Long, BigDecimal> zoneTn = new HashMap<Long, BigDecimal>();
    }

    // -------------------------------------------------------------- getters/setters

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public ProductionLine getProductionLine() { return productionLine; }
    public void setProductionLine(ProductionLine productionLine) { this.productionLine = productionLine; }
}
