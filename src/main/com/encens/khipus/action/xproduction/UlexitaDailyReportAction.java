package com.encens.khipus.action.xproduction;

import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.XProduction;
import com.encens.khipus.model.xproduction.XProductionUlexita;
import com.encens.khipus.model.xproduction.XSupply;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.xproduction.BaritinaDailyReportService;
import com.encens.khipus.service.xproduction.WarehouseBalanceRow;
import com.encens.khipus.service.xproduction.XProductionBalanceService;
import com.encens.khipus.service.xproduction.XProductionUlexitaCalc;
import com.encens.khipus.service.xproduction.XProductionUlexitaService;
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
 * Reporte mensual diario de produccion ULEXITA.
 *
 * Genera un Excel (.xls) con una fila por DIA del mes (todos los dias,
 * incluidos los sin orden con saldos arrastrados), replicando la planilla
 * manual "REPORTE DIARIO DE PRODUCCION ULEXITA".
 *
 * Los datos de proceso (leyes, Kpa, merma, etc.) son por orden: un dia con
 * varias ordenes genera varias filas. Los flujos de materia prima y producto
 * siguen la misma logica que el reporte BARITINA:
 *  - INGRESO MATERIA PRIMA ULEX: acopio (CollectMaterial) del articulo MP.
 *  - ULEX DISPONIBLE: saldo_anterior + INGRESO (acumulado).
 *  - DESPACHO: despachos (WarehouseVoucherDispatch) de los PT.
 *  - SALDO: saldo_anterior + PT TOTAL BUENO - DESPACHO.
 */
@Name("ulexitaDailyReportAction")
@Scope(ScopeType.PAGE)
public class UlexitaDailyReportAction {

    @Logger
    private Log log;

    @In
    private XProductionUlexitaService xproductionUlexitaService;
    @In
    private BaritinaDailyReportService baritinaDailyReportService;
    @In
    private XProductionBalanceService xproductionBalanceService;
    @In
    private ProductItemService productItemService;
    @In
    private FacesMessages facesMessages;

    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    private Integer year;
    private Integer month;
    private ProductionLine productionLine;

    // Columnas (0-based). A=0 margen. Se agregan INGRESO (tras DIA), DESPACHO y
    // SALDO (tras PT TOTAL BUENO) respecto a la version anterior.
    private static final int COL_FECHA       = 1;   // B
    private static final int COL_DIA         = 2;   // C
    private static final int COL_INGRESO     = 3;   // D  (NUEVA)
    private static final int COL_ULEX_DISP   = 4;   // E  (saldo MP = saldo_ant + INGRESO)
    private static final int COL_CONSUMO     = 5;   // F
    private static final int COL_GRUPO_D     = 6;   // G
    private static final int COL_GRUPO_N     = 7;   // H
    private static final int COL_DILUYENTE   = 8;   // I
    private static final int COL_BENT_PCT    = 9;   // J
    private static final int COL_CAOL_PCT    = 10;  // K
    private static final int COL_REPROC_IN   = 11;  // L
    private static final int COL_LEY_MP_BENT = 12;  // M
    private static final int COL_LEY_RECALC  = 13;  // N
    private static final int COL_LEY_PT      = 14;  // O
    private static final int COL_GRANULADO   = 15;  // P
    private static final int COL_PT_A        = 16;  // Q
    private static final int COL_PT_B        = 17;  // R
    private static final int COL_PT_BUENO    = 18;  // S
    private static final int COL_DESPACHO    = 19;  // T  (NUEVA)
    private static final int COL_SALDO       = 20;  // U  (NUEVA: saldo PT)
    private static final int COL_REPROC_OUT  = 21;  // V
    private static final int COL_KPM_BENT    = 22;  // W
    private static final int COL_KPM_MERMA   = 23;  // X
    private static final int COL_KPA         = 24;  // Y
    private static final int COL_MERMA       = 25;  // Z
    private static final int COL_MERMA_PCT   = 26;  // AA
    private static final int COL_OBS         = 27;  // AB
    private static final int LAST_COL        = 27;

    private static final int HEADER_ROW1     = 3;
    private static final int HEADER_ROW2     = 4;
    private static final int SALDO_ROW       = 5;
    private static final int DATA_START_ROW  = 6;

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

            List<XProduction> allOrders = baritinaDailyReportService.findProductions(productionLine, new Date(0), nextMonth);

            // Articulos: MP principal de la linea (config), PT clasificacion A/B
            String mpCod = productionLine.getCodArtMpPrincipal();
            if (mpCod == null) mpCod = deriveDefaultInputCod(allOrders);
            Set<String> ptCods = new LinkedHashSet<String>();
            if (productionLine.getCodArtPtA() != null) ptCods.add(productionLine.getCodArtPtA());
            if (productionLine.getCodArtPtB() != null) ptCods.add(productionLine.getCodArtPtB());
            if (ptCods.isEmpty()) ptCods = derivePtCods(allOrders);

            // Ordenes del periodo agrupadas por dia + sumas previas para saldos iniciales
            Map<Integer, List<XProduction>> ordersByDay = new HashMap<Integer, List<XProduction>>();
            BigDecimal ptBuenoBeforeTn = BigDecimal.ZERO;
            for (XProduction p : allOrders) {
                if (p.getInitDate() == null) continue;
                if (p.getInitDate().before(firstDay)) {
                    XProductionUlexita u = xproductionUlexitaService.findByProduction(p);
                    XProductionUlexitaCalc calc = new XProductionUlexitaCalc(
                            p, u, p.getProductionLine(), p.getSupplyList(), p.getProductionProductList());
                    ptBuenoBeforeTn = BigDecimalUtil.sum(ptBuenoBeforeTn, nz(calc.getPtTotalBueno()), 6);
                    continue;
                }
                if (!p.getInitDate().before(nextMonth)) continue;
                Calendar dc = Calendar.getInstance();
                dc.setTime(p.getInitDate());
                int day = dc.get(Calendar.DAY_OF_MONTH);
                List<XProduction> list = ordersByDay.get(day);
                if (list == null) { list = new ArrayList<XProduction>(); ordersByDay.put(day, list); }
                list.add(p);
            }

            // Acopio (INGRESO) y Despacho por dia
            Map<Integer, BigDecimal> ingresoByDay = bucketByDay(baritinaDailyReportService.sumAcopioByDay(mpCod, firstDay, nextMonth));
            Map<Integer, BigDecimal> despachoByDay = bucketByDay(baritinaDailyReportService.dispatchRows(ptCods, firstDay, nextMonth));

            // Saldo anterior de ULEX DISPONIBLE: se toma del mismo calculo que la pantalla
            // "Saldos de Almacen" (XProductionBalanceService) para el articulo MP principal,
            // al ultimo dia del mes anterior (firstDay - 1). Asi la columna arranca del mismo
            // saldo que muestra Saldos. El PT (saldoPt) mantiene su calculo previo.
            BigDecimal ulexDisp = ulexBalanceBeforeTn(mpCod, firstDay);
            BigDecimal saldoPt = BigDecimalUtil.subtract(ptBuenoBeforeTn, tn(baritinaDailyReportService.sumDispatchBefore(ptCods, firstDay)), 6);

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("ULEXITA " + month + "-" + year);
            sheet.setDisplayGridlines(false);
            Styles s = buildStyles(wb);
            buildHeader(sheet, s);

            // Fila SALDO ANT.: etiqueta combinada B-C (totalsLabel, sin wrapText)
            // para que el texto no agrande el alto de la fila.
            HSSFRow rs = sheet.createRow(SALDO_ROW);
            setText(rs, COL_FECHA, "SALDO ANT.", s.totalsLabel);
            setText(rs, COL_DIA, null, s.totalsLabel);
            sheet.addMergedRegion(new CellRangeAddress(SALDO_ROW, SALDO_ROW, COL_FECHA, COL_DIA));
            for (int col = COL_INGRESO; col <= LAST_COL; col++) setText(rs, col, null, s.body);
            setNumber(rs, COL_ULEX_DISP, ulexDisp, s.body);
            setNumber(rs, COL_SALDO, saldoPt, s.body);
            setText(rs, COL_OBS, null, s.obsLeft);

            BigDecimal[] totals = new BigDecimal[LAST_COL + 1];
            int rowIdx = DATA_START_ROW;
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM");

            for (int d = 1; d <= lastDayNum; d++) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.clear();
                dayCal.set(year, month - 1, d);
                Date date = dayCal.getTime();

                BigDecimal ingreso = tn(ingresoByDay.get(d));   // acopio KG -> TN
                BigDecimal despacho = tn(despachoByDay.get(d)); // despacho KG -> TN
                // ULEX DISP = saldo_ant + INGRESO - CONSUMO.
                // El INGRESO (acopio) se suma una vez por dia; el CONSUMO se resta por orden.
                ulexDisp = BigDecimalUtil.sum(ulexDisp, ingreso, 6);

                List<XProduction> dayOrders = ordersByDay.get(d);
                if (dayOrders == null || dayOrders.isEmpty()) {
                    // Dia sin orden: fila en blanco con saldos arrastrados
                    saldoPt = BigDecimalUtil.subtract(saldoPt, despacho, 6);
                    HSSFRow row = sheet.createRow(rowIdx++);
                    writeFlowCells(row, s, fmt.format(date), dayLetter(date), ingreso, ulexDisp, despacho, saldoPt, null);
                    blankProcessCells(row, s);
                    addTotals(totals, ingreso, despacho, null, null);
                } else {
                    boolean first = true;
                    for (XProduction p : dayOrders) {
                        XProductionUlexita u = xproductionUlexitaService.findByProduction(p);
                        XProductionUlexitaCalc calc = new XProductionUlexitaCalc(
                                p, u, p.getProductionLine(), p.getSupplyList(), p.getProductionProductList());
                        BigDecimal rowIngreso = first ? ingreso : BigDecimal.ZERO;
                        BigDecimal rowDespacho = first ? despacho : BigDecimal.ZERO;
                        // Restar el CONSUMO de la orden al ULEX DISPONIBLE
                        BigDecimal consumo = (p.isApproved() && u != null && u.getConsumoMpCalcSnap() != null)
                                ? u.getConsumoMpCalcSnap() : calc.getConsumoMpCalc();
                        ulexDisp = BigDecimalUtil.subtract(ulexDisp, nz(consumo), 6);
                        saldoPt = BigDecimalUtil.subtract(BigDecimalUtil.sum(saldoPt, nz(calc.getPtTotalBueno()), 6), rowDespacho, 6);

                        HSSFRow row = sheet.createRow(rowIdx++);
                        writeFlowCells(row, s, fmt.format(date), dayLetter(date), rowIngreso, ulexDisp, rowDespacho, saldoPt, p.getObservation());
                        writeProcessCells(row, s, p, u, calc);
                        addTotals(totals, rowIngreso, rowDespacho, u, calc);
                        first = false;
                    }
                }
            }

            // Fila TOTAL MES
            HSSFRow rt = sheet.createRow(rowIdx);
            setText(rt, COL_FECHA, "TOTAL MES:", s.totalsLabel);
            setText(rt, COL_DIA, null, s.totalsLabel);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, COL_FECHA, COL_DIA));
            for (int col = COL_INGRESO; col <= LAST_COL; col++) {
                if (totals[col] != null) setNumber(rt, col, totals[col], s.totals);
                else setText(rt, col, null, s.totals);
            }
            setNumber(rt, COL_ULEX_DISP, ulexDisp, s.totals); // ultimo saldo
            setNumber(rt, COL_SALDO, saldoPt, s.totals);      // ultimo saldo

            for (int i = 0; i <= LAST_COL; i++) sheet.setColumnWidth(i, columnWidthFor(i));

            sendResponse(wb);
        } catch (Exception e) {
            log.error("Error generando reporte ULEXITA", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DailyProductionReport.error.generic");
        }
    }

    // ------------------------------------------------------------------ cabecera

    private void buildHeader(HSSFSheet sheet, Styles s) {
        HSSFRow r0 = sheet.createRow(0);
        HSSFCell cTitle = r0.createCell(COL_FECHA);
        cTitle.setCellValue("REPORTE DIARIO DE PRODUCCION \"" + safeUpper(productionLine.getName()) + "\"");
        cTitle.setCellStyle(s.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, COL_FECHA, LAST_COL));

        sheet.createRow(1).createCell(COL_FECHA).setCellValue("Periodo: " + monthName(month) + " " + year);

        HSSFRow rh1 = sheet.createRow(HEADER_ROW1);
        HSSFRow rh2 = sheet.createRow(HEADER_ROW2);
        rh1.setHeightInPoints(32f);
        rh2.setHeightInPoints(32f);

        putHeader(sheet, rh1, rh2, COL_FECHA,       "FECHA",                              s.header);
        putHeader(sheet, rh1, rh2, COL_DIA,         "DIA",                                s.header);
        putHeader(sheet, rh1, rh2, COL_INGRESO,     "INGRESO MATERIA PRIMA ULEX (TN)",    s.header);
        putHeader(sheet, rh1, rh2, COL_ULEX_DISP,   "ULEX DISPONIBLE (TN)",               s.header);
        putHeader(sheet, rh1, rh2, COL_CONSUMO,     "CONSUMO MATERIA PRIMA ULEX (TN)",    s.header);
        putHeader(sheet, rh1, rh2, COL_DILUYENTE,   "Diluyente añadido (TN)",             s.header);
        putHeader(sheet, rh1, rh2, COL_BENT_PCT,    "Proporcion bentonita en el diluyente %", s.header);
        putHeader(sheet, rh1, rh2, COL_CAOL_PCT,    "Proporcion caolin en el diluyente %", s.header);
        putHeader(sheet, rh1, rh2, COL_REPROC_IN,   "Consumo Reproceso (TN)",             s.header);
        putHeader(sheet, rh1, rh2, COL_LEY_MP_BENT, "Ley MP con bentonita",               s.header);
        putHeader(sheet, rh1, rh2, COL_LEY_RECALC,  "Ley MP recalculada",                 s.header);
        putHeader(sheet, rh1, rh2, COL_LEY_PT,      "Ley PT",                             s.header);
        putHeader(sheet, rh1, rh2, COL_GRANULADO,   "PRODUCTO GRANULADO (TN)",            s.header);
        putHeader(sheet, rh1, rh2, COL_PT_BUENO,    "PT TOTAL BUENO (TN)",                s.header);
        putHeader(sheet, rh1, rh2, COL_DESPACHO,    "DESPACHO (TN)",                      s.header);
        putHeader(sheet, rh1, rh2, COL_SALDO,       "SALDO (TN)",                         s.header);
        putHeader(sheet, rh1, rh2, COL_REPROC_OUT,  "REPROCESO final (TN)",               s.header);
        putHeader(sheet, rh1, rh2, COL_KPM_BENT,    "Kpm bentonita",                      s.header);
        putHeader(sheet, rh1, rh2, COL_KPM_MERMA,   "Kpm merma",                          s.header);
        putHeader(sheet, rh1, rh2, COL_KPA,         "Kpa",                                s.header);
        putHeader(sheet, rh1, rh2, COL_MERMA,       "MERMA (TN)",                         s.header);
        putHeader(sheet, rh1, rh2, COL_MERMA_PCT,   "% MERMA",                            s.header);
        putHeader(sheet, rh1, rh2, COL_OBS,         "OBSERVACIONES",                      s.header);

        // GRUPO: row1 mergeado D-N, row2 separadas D/N
        setText(rh1, COL_GRUPO_D, "GRUPO", s.header);
        setText(rh1, COL_GRUPO_N, null, s.header);
        sheet.addMergedRegion(new CellRangeAddress(HEADER_ROW1, HEADER_ROW1, COL_GRUPO_D, COL_GRUPO_N));
        setText(rh2, COL_GRUPO_D, "D", s.header);
        setText(rh2, COL_GRUPO_N, "N", s.header);

        // PRODUCTO (TN): row1 mergeado A-B, row2 separadas A/B
        setText(rh1, COL_PT_A, "PRODUCTO (TN)", s.header);
        setText(rh1, COL_PT_B, null, s.header);
        sheet.addMergedRegion(new CellRangeAddress(HEADER_ROW1, HEADER_ROW1, COL_PT_A, COL_PT_B));
        setText(rh2, COL_PT_A, "A", s.header);
        setText(rh2, COL_PT_B, "B", s.header);

        sheet.createFreezePane(0, DATA_START_ROW);
    }

    private static void putHeader(HSSFSheet sheet, HSSFRow rh1, HSSFRow rh2, int col,
                                  String value, HSSFCellStyle style) {
        setText(rh1, col, value, style);
        setText(rh2, col, null, style);
        sheet.addMergedRegion(new CellRangeAddress(HEADER_ROW1, HEADER_ROW2, col, col));
    }

    // ------------------------------------------------------------------ filas

    /** Celdas comunes (flujos de MP/PT, fecha, dia, observaciones). */
    private void writeFlowCells(HSSFRow row, Styles s, String fecha, String dia,
                                BigDecimal ingreso, BigDecimal ulexDisp,
                                BigDecimal despacho, BigDecimal saldoPt, String obs) {
        setText(row, COL_FECHA, fecha, s.body);
        setText(row, COL_DIA, dia, s.bodyCenter);
        if (!isZero(ingreso)) setNumber(row, COL_INGRESO, ingreso, s.body); else setText(row, COL_INGRESO, null, s.body);
        setNumber(row, COL_ULEX_DISP, ulexDisp, s.body);
        if (!isZero(despacho)) setNumber(row, COL_DESPACHO, despacho, s.body); else setText(row, COL_DESPACHO, null, s.body);
        setNumber(row, COL_SALDO, saldoPt, s.body);
        setText(row, COL_OBS, obs, s.obsLeft);
    }

    /** Columnas de proceso (calc por orden). */
    private void writeProcessCells(HSSFRow row, Styles s, XProduction p, XProductionUlexita u, XProductionUlexitaCalc calc) {
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
    }

    /** Columnas de proceso vacias (dia sin orden), con borde. */
    private void blankProcessCells(HSSFRow row, Styles s) {
        for (int col = COL_CONSUMO; col <= COL_PT_BUENO; col++) setText(row, col, null, s.body);
        for (int col = COL_REPROC_OUT; col <= COL_MERMA_PCT; col++) setText(row, col, null, s.body);
    }

    private void addTotals(BigDecimal[] totals, BigDecimal ingreso, BigDecimal despacho,
                           XProductionUlexita u, XProductionUlexitaCalc calc) {
        addToTotals(totals, COL_INGRESO, ingreso);
        addToTotals(totals, COL_DESPACHO, despacho);
        if (calc == null) return;
        addToTotals(totals, COL_CONSUMO,    calc.getConsumoMpCalc());
        addToTotals(totals, COL_DILUYENTE,  calc.getDiluyenteTotal());
        addToTotals(totals, COL_REPROC_IN,  u != null ? u.getConsumoReprocesoTn() : null);
        addToTotals(totals, COL_GRANULADO,  u != null ? u.getProductoGranuladoTn() : null);
        addToTotals(totals, COL_PT_A,       calc.getPtA());
        addToTotals(totals, COL_PT_B,       calc.getPtB());
        addToTotals(totals, COL_PT_BUENO,   calc.getPtTotalBueno());
        addToTotals(totals, COL_REPROC_OUT, u != null ? u.getReprocesoFinalTn() : null);
        addToTotals(totals, COL_MERMA,      calc.getMerma());
    }

    // ------------------------------------------------------------------ derivacion

    private String deriveDefaultInputCod(List<XProduction> orders) {
        for (XProduction p : orders) {
            for (XSupply sup : p.getSupplyList()) {
                if (sup.hasFormula() && Boolean.TRUE.equals(sup.getFormulationInput().getInputDefault())) {
                    return sup.getProductItemCode();
                }
            }
        }
        return null;
    }

    private Set<String> derivePtCods(List<XProduction> orders) {
        Set<String> set = new LinkedHashSet<String>();
        for (XProduction p : orders) {
            for (com.encens.khipus.model.xproduction.XProductionProduct pr : p.getProductionProductList()) {
                if (pr.getProductItemCode() != null) set.add(pr.getProductItemCode());
            }
        }
        return set;
    }

    /**
     * Saldo anterior de ULEX DISPONIBLE en TN: balance del articulo MP principal segun
     * XProductionBalanceService (mismo criterio que la pantalla "Saldos de Almacen") al
     * ultimo dia del mes anterior (firstDay - 1). El balance viene en la unidad del
     * articulo (KG para ULEXITA) y se convierte a TN.
     */
    private BigDecimal ulexBalanceBeforeTn(String mpCod, Date firstDay) {
        if (mpCod == null) return BigDecimal.ZERO;
        ProductItem mp = productItemService.findProductItemByCode(mpCod);
        if (mp == null) return BigDecimal.ZERO;
        Calendar c = Calendar.getInstance();
        c.setTime(firstDay);
        c.add(Calendar.DAY_OF_MONTH, -1);
        Date cutoff = c.getTime();
        List<WarehouseBalanceRow> balances = xproductionBalanceService.computeBalances(
                mp.getCompanyNumber(), mp.getWarehouseCode(), cutoff);
        for (WarehouseBalanceRow row : balances) {
            if (mpCod.equals(row.getProductItemCode())) {
                return tn(row.getBalance());
            }
        }
        return BigDecimal.ZERO;
    }

    // ------------------------------------------------------------------ helpers

    private static BigDecimal tn(BigDecimal kg) {
        if (kg == null) return BigDecimal.ZERO;
        return BigDecimalUtil.divide(kg, THOUSAND, 6);
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static boolean isZero(BigDecimal v) { return v == null || v.compareTo(BigDecimal.ZERO) == 0; }

    private static Map<Integer, BigDecimal> bucketByDay(List<Object[]> rows) {
        Map<Integer, BigDecimal> map = new HashMap<Integer, BigDecimal>();
        for (Object[] r : rows) {
            if (r[0] == null || r[1] == null) continue;
            Calendar cc = Calendar.getInstance();
            cc.setTime((Date) r[0]);
            int day = cc.get(Calendar.DAY_OF_MONTH);
            BigDecimal prev = map.get(day);
            map.put(day, prev == null ? (BigDecimal) r[1] : prev.add((BigDecimal) r[1]));
        }
        return map;
    }

    private static void addToTotals(BigDecimal[] totals, int col, BigDecimal value) {
        if (value == null) return;
        totals[col] = (totals[col] == null) ? value : totals[col].add(value);
    }

    private int columnWidthFor(int col) {
        switch (col) {
            case 0:               return 915;   // A margen
            case COL_FECHA:       return 2400;  // FECHA
            case COL_DIA:         return 1000;  // DIA
            case COL_GRUPO_D:
            case COL_GRUPO_N:     return 1100;  // GRUPO D/N
            case COL_OBS:         return pxWidth(370); // OBSERVACIONES
            case COL_LEY_RECALC:  return pxWidth(82);  // Ley MP recalculada
            case COL_GRANULADO:   return pxWidth(92);  // PRODUCTO GRANULADO (TN)
            case COL_SALDO:       return pxWidth(78);  // SALDO (TN)
            case COL_REPROC_OUT:  return pxWidth(86);  // REPROCESO final (TN)
            case COL_INGRESO:
            case COL_ULEX_DISP:
            case COL_CONSUMO:
            case COL_DESPACHO:    return 3328;
            default:              return 2800;
        }
    }

    /**
     * Convierte pixeles a unidades POI: poiUnits = px * 256 / 7.
     * El ancho mostrado en Excel coincide con el px solicitado (sin el offset
     * de 5px de la formula clasica, que en la practica dejaba las columnas 5px
     * mas angostas que lo pedido).
     */
    private static int pxWidth(int px) {
        return (int) Math.round(px * 256.0 / 7.0);
    }

    private static String safeUpper(String s) { return s == null ? "" : s.toUpperCase(); }

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
        HSSFCellStyle title, header, body, bodyCenter, bodyPct, obsLeft, totals, totalsLabel;
    }

    private Styles buildStyles(HSSFWorkbook wb) {
        Styles s = new Styles();
        HSSFDataFormat fmt = wb.createDataFormat();
        short num2 = fmt.getFormat("#,##0.00");
        short pct = fmt.getFormat("0.00%");

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

        s.body = wb.createCellStyle();
        s.body.setDataFormat(num2);
        s.body.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        applyBorders(s.body);

        s.bodyCenter = wb.createCellStyle();
        s.bodyCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        applyBorders(s.bodyCenter);

        s.bodyPct = wb.createCellStyle();
        s.bodyPct.setDataFormat(pct);
        s.bodyPct.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        applyBorders(s.bodyPct);

        s.obsLeft = wb.createCellStyle();
        s.obsLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        s.obsLeft.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        applyBorders(s.obsLeft);

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
}
