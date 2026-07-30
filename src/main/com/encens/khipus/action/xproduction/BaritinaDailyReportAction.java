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

    private static final int TITLE_ROW   = 0;
    private static final int PERIOD_ROW  = 1;
    private static final int HEADER_ROW  = 3;
    private static final int SALDO_ROW   = 4;
    private static final int DATA_START  = 5;

    /**
     * Indices de columna (0-based) del reporte. No son fijos: la columna "USO OTRAS LINEAS"
     * solo existe cuando la materia prima se comparte con otra linea, y las columnas de zona
     * productiva son tantas como zonas haya en el periodo. Todo lo posterior se corre.
     */
    private static class Cols {
        int margin = 0;   // A
        int fecha = 1;    // B
        int dia = 2;      // C
        int ingreso = 3;  // D
        int uso = 4;      // E
        int usoOtras;     // solo si la MP es compartida; -1 si no aplica
        int saldoMp, pt, saldoPt, despacho, zoneStart, suma, turnos, obs, last;

        Cols(boolean showUsoOtras, int zoneCount) {
            usoOtras  = showUsoOtras ? uso + 1 : -1;
            saldoMp   = uso + (showUsoOtras ? 2 : 1);
            pt        = saldoMp + 1;
            saldoPt   = pt + 1;
            despacho  = saldoPt + 1;
            zoneStart = despacho + 1;
            suma      = zoneStart + zoneCount;
            turnos    = suma + 1;
            obs       = turnos + 1;
            last      = obs;
        }
    }

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
            itemCache.clear();

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

            // 4.c Consumo de la MP por OTRAS lineas. Cuando dos lineas comparten la materia prima
            //     el saldo del articulo es uno solo, asi que hay que descontar tambien lo ajeno o
            //     el reporte se separa de "Saldos de Almacen". La columna se muestra si la MP esta
            //     compartida por configuracion o si de hecho hubo consumo ajeno en el periodo.
            boolean hasUsoOtras = false;
            for (Object[] row : baritinaDailyReportService.supplyRowsOtherLines(
                    mpCodArt, productionLine, firstDay, nextMonth)) {
                Date od = (row[0] != null) ? (Date) row[0] : (Date) row[1];   // plan, o initDate
                if (od == null || row[2] == null) continue;
                int day = dayOfMonth(od);
                if (day < 1 || day > lastDayNum) continue;
                days[day].usoOtrasTn = BigDecimalUtil.sum(days[day].usoOtrasTn, tn((BigDecimal) row[2]), 6);
                hasUsoOtras = true;
            }
            boolean showUsoOtras = hasUsoOtras
                    || baritinaDailyReportService.isSharedMaterial(mpCodArt, productionLine);

            // 5. Saldos iniciales: ambos se toman del mismo calculo que la pantalla "Saldos de
            //    Almacen" (XProductionBalanceService) al ultimo dia del mes anterior (firstDay - 1),
            //    convertido KG -> TN. El PT no puede derivarse de las ordenes: su saldo tambien
            //    incluye cargas y ajustes por vale, que las ordenes de produccion no ven.
            BigDecimal openMpTn = balanceBeforeTn(mpCodArt, firstDay);
            BigDecimal openProdTn = BigDecimal.ZERO;
            for (String cod : productCodArts) {
                openProdTn = BigDecimalUtil.sum(openProdTn, balanceBeforeTn(cod, firstDay), 6);
            }

            // 5.b Nombres de los articulos para los titulos de columna (sin literales de producto).
            String mpName = itemName(mpCodArt);
            String ptName = productCodArts.isEmpty() ? "" : itemName(productCodArts.iterator().next());

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
            Cols c2 = new Cols(showUsoOtras, zones.size());

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(MessageUtils.getMessage(
                    "DailyProductionReport.baritina.sheet", mpName, month, year));
            sheet.setDisplayGridlines(false);
            Styles s = buildStyles(wb);

            buildHeader(sheet, s, c2, zones, mpName, ptName);

            // Fila SALDO ANT.: saldos iniciales (al fin del periodo anterior) de MP y producto
            HSSFRow rs = sheet.createRow(SALDO_ROW);
            setText(rs, c2.fecha, MessageUtils.getMessage("DailyProductionReport.baritina.row.openingBalance"), s.headerLeft);
            for (int col = c2.dia; col <= c2.last; col++) setText(rs, col, null, s.body);
            setNumber(rs, c2.saldoMp, openMpTn, s.num);
            setNumber(rs, c2.saldoPt, openProdTn, s.num);

            // Filas de datos por dia
            BigDecimal saldoMp = openMpTn;
            BigDecimal saldoProd = openProdTn;
            BigDecimal totIngreso = BigDecimal.ZERO, totUso = BigDecimal.ZERO, totUsoOtras = BigDecimal.ZERO,
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

                // El saldo de MP es el del ARTICULO, no el de la linea: si otra linea consume la
                // misma materia prima tambien lo baja. Sin ese termino el saldo del reporte se
                // separaria del de "Saldos de Almacen".
                saldoMp = BigDecimalUtil.subtract(
                        BigDecimalUtil.subtract(BigDecimalUtil.sum(saldoMp, dd.ingresoTn, 6), dd.usoTn, 6),
                        dd.usoOtrasTn, 6);
                saldoProd = BigDecimalUtil.subtract(BigDecimalUtil.sum(saldoProd, dd.ptTn, 6), dd.despachoTn, 6);

                HSSFRow row = sheet.createRow(rowIdx++);
                setText(row, c2.fecha, fmt.format(date), s.body);
                setText(row, c2.dia, dayLetter(date), s.center);
                setNumber(row, c2.ingreso, dd.ingresoTn, s.num);
                setNumber(row, c2.saldoMp, saldoMp, s.num);
                if (dd.hasProduction) setNumber(row, c2.uso, dd.usoTn, s.num); else setText(row, c2.uso, null, s.num);
                if (c2.usoOtras >= 0) {
                    if (isZero(dd.usoOtrasTn)) setText(row, c2.usoOtras, null, s.num);
                    else setNumber(row, c2.usoOtras, dd.usoOtrasTn, s.num);
                }
                setNumber(row, c2.pt, dd.ptTn, s.num);
                setNumber(row, c2.saldoPt, saldoProd, s.num);
                if (isZero(dd.despachoTn)) setText(row, c2.despacho, null, s.num); else setNumber(row, c2.despacho, dd.despachoTn, s.num);

                BigDecimal suma = BigDecimal.ZERO;
                for (int zi = 0; zi < zones.size(); zi++) {
                    int col = c2.zoneStart + zi;
                    BigDecimal zoneTn = dd.zoneTn.get(zones.get(zi).getId());
                    if (dd.hasProduction && zoneTn != null && !isZero(dd.usoTn)) {
                        BigDecimal pct = BigDecimalUtil.divide(BigDecimalUtil.multiply(zoneTn, ONE_HUNDRED, 6), dd.usoTn, 4);
                        setNumber(row, col, pct, s.pct);
                        suma = BigDecimalUtil.sum(suma, pct, 4);
                    } else {
                        setText(row, col, null, s.pct);
                    }
                }
                if (dd.hasProduction) setNumber(row, c2.suma, suma, s.numCenter); else setText(row, c2.suma, null, s.numCenter);
                if (dd.hasProduction && dd.turnos > 0) setNumber(row, c2.turnos, new BigDecimal(dd.turnos), s.center);
                else setText(row, c2.turnos, null, s.center);
                setText(row, c2.obs, composeObs(dd), s.body);

                totIngreso = BigDecimalUtil.sum(totIngreso, dd.ingresoTn, 6);
                totUso = BigDecimalUtil.sum(totUso, dd.usoTn, 6);
                totUsoOtras = BigDecimalUtil.sum(totUsoOtras, dd.usoOtrasTn, 6);
                totPt = BigDecimalUtil.sum(totPt, dd.ptTn, 6);
                totDespacho = BigDecimalUtil.sum(totDespacho, dd.despachoTn, 6);
            }

            // Fila TOTAL
            HSSFRow rt = sheet.createRow(rowIdx);
            setText(rt, c2.fecha, MessageUtils.getMessage("DailyProductionReport.baritina.row.total"), s.totalsLabel);
            setText(rt, c2.dia, null, s.totalsLabel);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, c2.fecha, c2.dia));
            for (int col = c2.ingreso; col <= c2.last; col++) setText(rt, col, null, s.totals);
            setNumber(rt, c2.ingreso, totIngreso, s.totals);
            setNumber(rt, c2.saldoMp, saldoMp, s.totals);
            setNumber(rt, c2.uso, totUso, s.totals);
            if (c2.usoOtras >= 0) setNumber(rt, c2.usoOtras, totUsoOtras, s.totals);
            setNumber(rt, c2.pt, totPt, s.totals);
            setNumber(rt, c2.saldoPt, saldoProd, s.totals);
            setNumber(rt, c2.despacho, totDespacho, s.totals);

            // Anchos: todas las columnas de datos (INGRESO..TURNOS, incluidas las de zona) a 112 px.
            sheet.setColumnWidth(c2.margin, 915);
            sheet.setColumnWidth(c2.fecha, 2400);
            sheet.setColumnWidth(c2.dia, 1100);
            for (int col = c2.ingreso; col <= c2.turnos; col++) sheet.setColumnWidth(col, pxWidth(112));
            sheet.setColumnWidth(c2.obs, 8000);

            sendResponse(wb);
        } catch (Exception e) {
            log.error("Error generando reporte BARITINA", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DailyProductionReport.error.generic");
        }
    }

    // -------------------------------------------------------------- cabecera

    /**
     * Cabecera del reporte. Los titulos de columna se arman con el NOMBRE del articulo configurado
     * en la linea ({@code mpName} / {@code ptName}) y no con literales, para que el mismo template
     * sirva a cualquier linea de este tipo (molienda, chancado, otra materia prima).
     */
    private void buildHeader(HSSFSheet sheet, Styles s, Cols c, List<ProductiveZone> zones,
                             String mpName, String ptName) {
        HSSFRow r0 = sheet.createRow(TITLE_ROW);
        HSSFCell t = r0.createCell(c.fecha);
        t.setCellValue(MessageUtils.getMessage("DailyProductionReport.baritina.title",
                safe(productionLine.getName()).toUpperCase()));
        t.setCellStyle(s.title);
        sheet.addMergedRegion(new CellRangeAddress(TITLE_ROW, TITLE_ROW, c.fecha, c.last));

        // El anio va como texto: si se pasa como numero, la interpolacion lo formatea con el
        // separador de miles del locale y sale "2.026".
        sheet.createRow(PERIOD_ROW).createCell(c.fecha).setCellValue(
                MessageUtils.getMessage("DailyProductionReport.baritina.period",
                        monthName(month), String.valueOf(year)));

        HSSFRow h = sheet.createRow(HEADER_ROW);
        h.setHeightInPoints(40f);
        setText(h, c.fecha,    MessageUtils.getMessage("DailyProductionReport.baritina.col.date"), s.header);
        setText(h, c.dia,      MessageUtils.getMessage("DailyProductionReport.baritina.col.day"), s.header);
        setText(h, c.ingreso,  MessageUtils.getMessage("DailyProductionReport.baritina.col.mpInput", mpName), s.header);
        setText(h, c.saldoMp,  MessageUtils.getMessage("DailyProductionReport.baritina.col.mpBalance", mpName), s.header);
        setText(h, c.uso,      MessageUtils.getMessage("DailyProductionReport.baritina.col.mpUsage", mpName), s.header);
        if (c.usoOtras >= 0) {
            setText(h, c.usoOtras,
                    MessageUtils.getMessage("DailyProductionReport.baritina.col.mpUsageOtherLines", mpName), s.header);
        }
        setText(h, c.pt,       MessageUtils.getMessage("DailyProductionReport.baritina.col.pt", ptName), s.header);
        setText(h, c.saldoPt,  MessageUtils.getMessage("DailyProductionReport.baritina.col.ptBalance", ptName), s.header);
        setText(h, c.despacho, MessageUtils.getMessage("DailyProductionReport.baritina.col.dispatch", ptName), s.header);
        for (int zi = 0; zi < zones.size(); zi++) {
            setText(h, c.zoneStart + zi,
                    MessageUtils.getMessage("DailyProductionReport.baritina.col.zone", safe(zones.get(zi).getName())), s.header);
        }
        setText(h, c.suma,   MessageUtils.getMessage("DailyProductionReport.baritina.col.pctSum"), s.header);
        setText(h, c.turnos, MessageUtils.getMessage("DailyProductionReport.baritina.col.shifts"), s.header);
        setText(h, c.obs,    MessageUtils.getMessage("DailyProductionReport.baritina.col.observations"), s.header);

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
        ProductItem item = item(cod);
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

    /** Articulos ya resueltos, para no repetir consultas al armar cabeceras y marcadores. */
    private final Map<String, ProductItem> itemCache = new HashMap<String, ProductItem>();

    private ProductItem item(String cod) {
        if (cod == null) return null;
        if (!itemCache.containsKey(cod)) {
            itemCache.put(cod, productItemService.findProductItemByCode(cod));
        }
        return itemCache.get(cod);
    }

    /** Nombre del articulo para los titulos de columna ("BARITINA"); vacio si no se encuentra. */
    private String itemName(String cod) {
        ProductItem it = item(cod);
        return (it == null || it.getName() == null) ? "" : it.getName();
    }

    /** "2021 BARITINA MOLIDA", o solo el codigo si el articulo no se encuentra. */
    private String itemLabel(String cod) {
        if (cod == null) return "";
        ProductItem it = item(cod);
        return (it == null || it.getName() == null) ? cod : cod + " " + it.getName();
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
        ProductItem mp = item(mpCodArt);
        if (mp == null) return;
        for (XSupply sup : p.getSupplyList()) {
            String cod = sup.getProductItemCode();
            if (cod == null || mpCodArt.equals(cod) || isZero(sup.getQuantity())) continue;
            // Solo se marcan las materias primas: se comparan contra el almacen de la MP para no
            // ensuciar las observaciones con envases, agua, aglutinantes y demas consumibles, que
            // no forman parte de la historia de la columna USO.
            if (!sameWarehouse(mp, item(cod))) continue;
            accumulate(dd.otherSupplyTn, cod, tn(sup.getQuantity()));
        }
    }

    private static boolean sameWarehouse(ProductItem a, ProductItem b) {
        return a != null && b != null && a.getWarehouseCode() != null
                && a.getWarehouseCode().equals(b.getWarehouseCode());
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

    /**
     * Convierte pixeles a unidades POI: poiUnits = px * 256 / 7.
     * El ancho mostrado en Excel coincide con el px solicitado (sin el offset de 5px de la
     * formula clasica, que en la practica dejaba las columnas 5px mas angostas que lo pedido).
     * Mismo criterio que UlexitaDailyReportAction.
     */
    private static int pxWidth(int px) {
        return (int) Math.round(px * 256.0 / 7.0);
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
        // Fuente de los encabezados (fila de titulos y etiqueta SALDO ANT.): mas chica para que
        // los titulos largos entren en el ancho de columna sin depender del ajuste de texto.
        HSSFFont headerFont = wb.createFont();
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setFontHeightInPoints((short) 9);

        s.title = wb.createCellStyle();
        s.title.setFont(titleFont);
        s.title.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        s.header = wb.createCellStyle();
        s.header.setFont(headerFont);
        s.header.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        s.header.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        s.header.setWrapText(true);
        applyBorders(s.header);

        s.headerLeft = wb.createCellStyle();
        s.headerLeft.setFont(headerFont);
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
        /** Consumo de la misma materia prima por ordenes de otras lineas. */
        BigDecimal usoOtrasTn = BigDecimal.ZERO;
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
