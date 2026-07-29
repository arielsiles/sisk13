package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.xproduction.*;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.xproduction.BaritinaDailyReportService;
import com.encens.khipus.service.xproduction.WarehouseBalanceRow;
import com.encens.khipus.service.xproduction.XProductionBalanceService;
import com.encens.khipus.service.xproduction.XProductionBaritinaService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
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
    private XProductionBalanceService xproductionBalanceService;
    @In
    private ProductItemService productItemService;
    @In
    private FacesMessages facesMessages;

    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    /** Locale de los numeros escritos como texto dentro de OBSERVACIONES (miles '.', decimal ','). */
    private static final Locale SPANISH = new Locale("es");

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
            // El componente es de scope PAGE: los caches deben limpiarse en cada generacion,
            // porque el balance depende de la fecha de corte del periodo solicitado.
            balanceCache.clear();
            itemLabelCache.clear();

            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(year, month - 1, 1, 0, 0, 0);
            Date firstDay = c.getTime();
            int lastDayNum = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            c.add(Calendar.MONTH, 1);
            Date nextMonth = c.getTime();

            // 1. Ordenes: todas hasta nextMonth (para derivar articulos si la linea no los configura)
            List<XProduction> allOrders = baritinaDailyReportService.findProductions(productionLine, new Date(0), nextMonth);

            // 2. Articulos: configuracion de la linea primero (mismo criterio que ULEXITA); la
            //    derivacion desde las ordenes queda solo como respaldo para lineas sin configurar.
            //    Sin esto, un periodo sin ordenes deja los codigos en nulo y TODO el reporte en cero.
            String mpCodArt = productionLine.getCodArtMpPrincipal();
            if (mpCodArt == null) mpCodArt = deriveMpCodArt(allOrders);
            Set<String> productCodArts = new LinkedHashSet<String>();
            if (productionLine.getCodArtPtPrincipal() != null) {
                productCodArts.add(productionLine.getCodArtPtPrincipal());
            } else {
                productCodArts = derivePtCodArts(allOrders);
            }

            // 3. Agregacion por dia (periodo) desde las ordenes
            DayData[] days = new DayData[lastDayNum + 1];
            for (int d = 1; d <= lastDayNum; d++) days[d] = new DayData();
            Map<Long, ProductiveZone> zoneById = new LinkedHashMap<Long, ProductiveZone>();

            for (XProduction p : allOrders) {
                // Ubicar la orden por la fecha del PLAN de produccion (mismo criterio que ULEXITA,
                // el Kardex y XProductionBalanceService), NO por initDate: un turno que arranca
                // pasada la medianoche tiene initDate en el dia calendario siguiente al de su plan.
                Date od = orderDate(p);
                if (od == null) continue;
                if (od.before(firstDay)) continue;
                if (!od.before(nextMonth)) continue;

                BigDecimal usoTn = tn(mpInputQty(p, mpCodArt));
                BigDecimal ptTn = tn(sumPt(p, productCodArts));

                Calendar dc = Calendar.getInstance();
                dc.setTime(od);
                int day = dc.get(Calendar.DAY_OF_MONTH);
                DayData dd = days[day];
                dd.hasProduction = true;
                dd.usoTn = BigDecimalUtil.sum(dd.usoTn, usoTn, 6);
                dd.ptTn = BigDecimalUtil.sum(dd.ptTn, ptTn, 6);
                collectUnconfiguredMarks(dd, p, mpCodArt, productCodArts);

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

            // 4.b Vales de ajuste del periodo (no despachos). No alteran los saldos del reporte;
            //     se anotan en OBSERVACIONES para que el movimiento no quede perdido y se pueda
            //     explicar cualquier diferencia contra "Saldos de Almacen".
            Set<String> adjustCods = new LinkedHashSet<String>();
            if (mpCodArt != null) adjustCods.add(mpCodArt);
            adjustCods.addAll(productCodArts);
            for (Object[] row : baritinaDailyReportService.adjustmentRows(adjustCods, firstDay, nextMonth)) {
                int day = dayOfMonth((Date) row[0]);
                if (day < 1 || day > lastDayNum) continue;
                addAdjustment(days[day], row);
            }

            // 5. Saldos iniciales: ambos se toman del mismo calculo que la pantalla "Saldos de
            //    Almacen" (XProductionBalanceService) al ultimo dia del mes anterior (firstDay - 1),
            //    convertido KG -> TN. El PT no puede derivarse de las ordenes: su saldo tambien
            //    incluye cargas y ajustes por vale, que las ordenes de produccion no ven.
            BigDecimal openMpTn = balanceBeforeTn(mpCodArt, firstDay);
            BigDecimal openProdTn = BigDecimal.ZERO;
            for (String cod : productCodArts) {
                openProdTn = BigDecimalUtil.sum(openProdTn, balanceBeforeTn(cod, firstDay), 6);
            }

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
                setText(row, colObs, composeObs(dd), s.body);

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

    /**
     * Fecha con la que el reporte ubica una orden en el dia: la del PLAN de produccion (mismo
     * criterio que ULEXITA, el Kardex de Articulos y XProductionBalanceService). Si la orden no
     * tiene plan, cae a initDate. Asi un turno que arranca pasada la medianoche queda en el dia
     * de su plan y no en el siguiente.
     */
    private static Date orderDate(XProduction p) {
        if (p.getProductionPlan() != null && p.getProductionPlan().getDate() != null) {
            return p.getProductionPlan().getDate();
        }
        return p.getInitDate();
    }

    /**
     * Saldo en TN de un articulo al ultimo dia del mes anterior (firstDay - 1) segun
     * XProductionBalanceService, o sea con el mismo criterio que la pantalla "Saldos de Almacen"
     * (incluye vales, acopio y produccion). El balance viene en la unidad del articulo (KG) y se
     * convierte a TN. Los balances por almacen se cachean: MP y PT suelen repetir almacen.
     */
    private BigDecimal balanceBeforeTn(String cod, Date firstDay) {
        if (cod == null) return BigDecimal.ZERO;
        ProductItem item = productItemService.findProductItemByCode(cod);
        if (item == null) return BigDecimal.ZERO;
        Calendar c = Calendar.getInstance();
        c.setTime(firstDay);
        c.add(Calendar.DAY_OF_MONTH, -1);
        String key = item.getCompanyNumber() + "|" + item.getWarehouseCode();
        List<WarehouseBalanceRow> balances = balanceCache.get(key);
        if (balances == null) {
            balances = xproductionBalanceService.computeBalances(
                    item.getCompanyNumber(), item.getWarehouseCode(), c.getTime());
            balanceCache.put(key, balances);
        }
        for (WarehouseBalanceRow row : balances) {
            if (cod.equals(row.getProductItemCode())) {
                return tn(row.getBalance());
            }
        }
        return BigDecimal.ZERO;
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

    /**
     * Consumo de materia prima de la orden: solo el articulo MP configurado en la linea, para que
     * la columna USO y el saldo arrastrado correspondan a un unico articulo. Si la linea no tiene
     * MP configurada se cae al comportamiento anterior (insumo marcado por defecto en la formula).
     */
    private BigDecimal mpInputQty(XProduction p, String mpCodArt) {
        BigDecimal q = BigDecimal.ZERO;
        for (XSupply sup : p.getSupplyList()) {
            if (sup.getQuantity() == null) continue;
            boolean isMp = (mpCodArt != null)
                    ? mpCodArt.equals(sup.getProductItemCode())
                    : (sup.hasFormula() && Boolean.TRUE.equals(sup.getFormulationInput().getInputDefault()));
            if (isMp) q = BigDecimalUtil.sum(q, sup.getQuantity(), 6);
        }
        return q;
    }

    /** Produccion de la orden, contando solo los PT configurados en la linea. */
    private BigDecimal sumPt(XProduction p, Set<String> productCodArts) {
        BigDecimal q = BigDecimal.ZERO;
        for (XProductionProduct pr : p.getProductionProductList()) {
            if (pr.getQuantity() == null) continue;
            if (pr.getProductItemCode() != null && productCodArts.contains(pr.getProductItemCode())) {
                q = BigDecimalUtil.sum(q, pr.getQuantity(), 6);
            }
        }
        return q;
    }

    // -------------------------------------------------------------- observaciones

    /** Balances por almacen ya calculados (clave compania|almacen): MP y PT suelen repetirlo. */
    private final Map<String, List<WarehouseBalanceRow>> balanceCache =
            new HashMap<String, List<WarehouseBalanceRow>>();

    /** Etiquetas de articulo ya resueltas, para no repetir consultas al armar los marcadores. */
    private final Map<String, String> itemLabelCache = new HashMap<String, String>();

    /** "2021 BARITINA MOLIDA", o solo el codigo si el articulo no se encuentra. */
    private String itemLabel(String cod) {
        if (cod == null) return "";
        String label = itemLabelCache.get(cod);
        if (label == null) {
            ProductItem item = productItemService.findProductItemByCode(cod);
            label = (item == null || item.getName() == null) ? cod : cod + " " + item.getName();
            itemLabelCache.put(cod, label);
        }
        return label;
    }

    /**
     * Acumula en el dia los articulos que la orden movio pero que NO estan configurados en la
     * linea: no entran en las columnas (romperian el cuadre del saldo, que sigue a un solo
     * articulo) pero deben quedar visibles en OBSERVACIONES.
     */
    private void collectUnconfiguredMarks(DayData dd, XProduction p, String mpCodArt, Set<String> productCodArts) {
        for (XProductionProduct pr : p.getProductionProductList()) {
            String cod = pr.getProductItemCode();
            if (cod == null || productCodArts.contains(cod) || isZero(pr.getQuantity())) continue;
            accumulate(dd.otherPtTn, cod, tn(pr.getQuantity()));
        }
        // Sin MP configurada no hay contra que comparar: se omite para no marcar todo el consumo.
        if (mpCodArt == null) return;
        for (XSupply sup : p.getSupplyList()) {
            String cod = sup.getProductItemCode();
            if (cod == null || mpCodArt.equals(cod) || isZero(sup.getQuantity())) continue;
            accumulate(dd.otherSupplyTn, cod, tn(sup.getQuantity()));
        }
    }

    /** Acumula el ajuste por vale del dia, con signo (entrada +, salida -), agrupado por vale y articulo. */
    private void addAdjustment(DayData dd, Object[] row) {
        BigDecimal qty = (BigDecimal) row[2];
        if (isZero(qty)) return;
        BigDecimal signedTn = MovementDetailType.E.equals(row[1]) ? tn(qty) : tn(qty).negate();
        accumulate(dd.adjustTn, row[4] + "|" + row[3], signedTn);
    }

    private static void accumulate(Map<String, BigDecimal> map, String key, BigDecimal value) {
        BigDecimal prev = map.get(key);
        map.put(key, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, value, 6));
    }

    /** Observacion final del dia: la capturada en la orden mas los marcadores. Null si no hay nada. */
    private String composeObs(DayData dd) {
        StringBuilder sb = new StringBuilder(dd.obs);
        for (Map.Entry<String, BigDecimal> e : dd.adjustTn.entrySet()) {
            String[] parts = e.getKey().split("\\|", 2);
            append(sb, MessageUtils.getMessage("DailyProductionReport.obs.adjustment",
                    parts[0], itemLabel(parts.length > 1 ? parts[1] : null), decimal(e.getValue())));
        }
        for (Map.Entry<String, BigDecimal> e : dd.otherPtTn.entrySet()) {
            append(sb, MessageUtils.getMessage("DailyProductionReport.obs.unconfiguredProduct",
                    itemLabel(e.getKey()), decimal(e.getValue())));
        }
        for (Map.Entry<String, BigDecimal> e : dd.otherSupplyTn.entrySet()) {
            append(sb, MessageUtils.getMessage("DailyProductionReport.obs.unconfiguredSupply",
                    itemLabel(e.getKey()), decimal(e.getValue())));
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static void append(StringBuilder sb, String text) {
        if (sb.length() > 0) sb.append(" | ");
        sb.append(text);
    }

    private static String decimal(BigDecimal value) {
        return FormatUtils.formatNumber(value == null ? BigDecimal.ZERO : value, "#,##0.00", SPANISH);
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
        /** Ajustes por vale del dia: clave "noTrans|codArt" -> TN con signo (entrada +, salida -). */
        Map<String, BigDecimal> adjustTn = new LinkedHashMap<String, BigDecimal>();
        /** PT producidos que no estan configurados en la linea: cod_art -> TN. */
        Map<String, BigDecimal> otherPtTn = new LinkedHashMap<String, BigDecimal>();
        /** Insumos consumidos distintos de la MP configurada: cod_art -> TN. */
        Map<String, BigDecimal> otherSupplyTn = new LinkedHashMap<String, BigDecimal>();
    }

    // -------------------------------------------------------------- getters/setters

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public ProductionLine getProductionLine() { return productionLine; }
    public void setProductionLine(ProductionLine productionLine) { this.productionLine = productionLine; }
}
