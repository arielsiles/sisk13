package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.Periodo;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialPayRoll;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.service.production.ProductiveZoneService;
import com.encens.khipus.service.production.RawMaterialPayRollService;
import com.encens.khipus.service.production.RawMaterialPayRollServiceBean;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.MoneyUtil;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.lang.time.FastDateFormat;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Encens S.R.L.
 * Action to generate payroll summary report by payment method and currency
 *
 * @author
 * @version $Id: SummaryPayrollByPaymentMethodReportAction.java  22-ene-2010 11:38:12$
 */
@Name("rawMaterialPayRollReportAction")
@Scope(ScopeType.CONVERSATION)
//@Restrict("#{s:hasPermission('RAWMATERIALPAYSUMMARY','VIEW')}")
public class RawMaterialPayRollReportAction extends GenericReportAction {
    @In
    RawMaterialPayRollService rawMaterialPayRollService;

    @In
    ProductiveZoneService productiveZoneService;

    private String summaryReportTitle;
    private String gestionTitle;

    private Gestion gestion;
    private Month month;
    private Periodo periodo;
    private ProductiveZone zone;
    private MetaProduct metaProduct;
    private double unitPrice;
    private double mountCollection;
    private double totalCollectionXUnitPrice;
    private double differences;
    private double weightReal;
    private double totalWeightRealXUnitPrice;
    private double yogurt;
    private double veterinari;
    private double credit;
    private double recipient;
    private RawMaterialPayRollServiceBean.SummaryTotal summaryTotal;
    private RawMaterialPayRollServiceBean.Discounts discounts;
    private Date startDate;
    private Date endDate;
    private Calendar dateIni;
    private Calendar dateEnd;

    private List<GestionPayroll> gestionPayrollList;


    private GeneratedPayrollType generatedPayrollType = GeneratedPayrollType.OFFICIAL;


    public void generateReport() throws ParseException {
        log.debug("Generate RotatoryFundReportAction........");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        dateIni = Calendar.getInstance();
        dateEnd = Calendar.getInstance();
        dateIni.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
        dateEnd.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
        sdf.setCalendar(dateIni);
        sdf.setCalendar(dateEnd);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Map params = new HashMap();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        dateIni.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
        dateEnd.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
        startDate = dateFormat.parse(dateFormat.format(dateIni.getTime()));
        endDate = dateFormat.parse(dateFormat.format(dateEnd.getTime()));

        if (zone == null) {
            generarTodosGAB(params, df);
        } else {

            generarGAB(params, df);
        }

    }

    private void generarGAB(Map params, DateFormat df) throws ParseException {
        RawMaterialPayRoll rawMaterialPayRoll;
        TypedReportData typedReportData;
        TypedReportData mostrar = new TypedReportData();
        JasperPrint jasperPrint1 = new JasperPrint();
        JasperPrint jasperPrint2;
        /*  DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

     Date startDate = dateFormat.parse(dateFormat.format(dateIni.getTime()));
     Date endDate = dateFormat.parse(dateFormat.format(dateEnd.getTime()));*/


        rawMaterialPayRoll = rawMaterialPayRollService.getTotalsRawMaterialPayRoll(startDate, endDate, zone, metaProduct);

        params.put("reportTitle", messages.get("Report.titleGeneral"));
        params.put("periodo", (periodo.getResourceKey().toString() == "Periodo.first") ? "1RA QUINCENA" : "2DA QUINCENA" + " " + getMes(month));
        params.put("startDate", df.format(dateIni.getTime()));
        params.put("endDate", df.format(dateEnd.getTime()));
        params.put("nombre_gab", "GAB: " + zone.getNumber() + " - " + zone.getName());


        params.put("totalCollectedByGAB", rawMaterialPayRoll.getTotalCollectedByGAB());
        params.put("totalMountCollectdByGAB", rawMaterialPayRoll.getTotalMountCollectdByGAB());
        params.put("totalRetentionGAB", rawMaterialPayRoll.getTotalRetentionGAB());
        params.put("totalCreditByGAB", rawMaterialPayRoll.getTotalCreditByGAB());
        params.put("totalVeterinaryByGAB", rawMaterialPayRoll.getTotalVeterinaryByGAB());
        params.put("totalAlcoholByGAB", rawMaterialPayRoll.getTotalAlcoholByGAB());
        params.put("totalConcentratedByGAB", rawMaterialPayRoll.getTotalConcentratedByGAB());
        params.put("totalYogourdByGAB", rawMaterialPayRoll.getTotalYogourdByGAB());
        params.put("totalRecipByGAB", rawMaterialPayRoll.getTotalRecipByGAB());
        params.put("totalDiscountByGAB", rawMaterialPayRoll.getTotalDiscountByGAB());
        params.put("totalCommission", rawMaterialPayRoll.getTotalCommission());
        params.put("totalAdjustmentByGAB", rawMaterialPayRoll.getTotalAdjustmentByGAB());
        params.put("totalReserveDicount", rawMaterialPayRoll.getTotalReserveDicount());
        params.put("totalDiscountGA", rawMaterialPayRoll.getTotalGA());
        params.put("totalOtherIncomeByGAB", rawMaterialPayRoll.getTotalOtherIncomeByGAB());
        params.put("totalLiquidByGAB", rawMaterialPayRoll.getTotalLiquidByGAB());
        params.put("dateStart", "Fecha Inicio - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateIni));
        params.put("dateEnd", "Fecha Fin - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateEnd));
        params.put("unitPrice", String.format("%.2f", rawMaterialPayRoll.getUnitPrice()));
        //params.put("unitPrice","3.00");

        Double liquidPayable = rawMaterialPayRoll.getTotalLiquidByGAB();
        MoneyUtil moneyUtil = new MoneyUtil();
        params.put("literally_money", moneyUtil.Convertir(liquidPayable.toString(), true));
        typedReportData = super.getReport("rotatoryFundReport"
                , "/production/reports/rawMaterialPayRollReport.jrxml"
                , MessageUtils.getMessage("Report.rawMaterialPayRollReportAction")
                , params);

        jasperPrint1 = typedReportData.getJasperPrint();

        jasperPrint2 = getbyGABReportTypedReportData().getJasperPrint();
        List pages = jasperPrint2.getPages();
        for (Object jrPrintPage : pages) {
            jasperPrint1.addPage((JRPrintPage) jrPrintPage);
        }


        try {
            typedReportData.setJasperPrint(jasperPrint1);
            GenerationReportData generationReportData = new GenerationReportData(typedReportData);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void generarTodosGAB(Map params, DateFormat df) throws ParseException {
        JasperPrint jasperPrint1 = new JasperPrint();
        JasperPrint jasperPrint2, collectedBayGAB;
        RawMaterialPayRoll rawMaterialPayRoll;
        TypedReportData typedReportData;
        TypedReportData mostrar = new TypedReportData();
        boolean tomarPrimero = true;
        List<ProductiveZone> productiveZones = productiveZoneService.findAll();
        /* DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

     Date startDate = dateFormat.parse(dateFormat.format(dateIni.getTime()));
     Date endDate = dateFormat.parse(dateFormat.format(dateEnd.getTime()));*/

        for (ProductiveZone productiveZone : productiveZones) {
            zone = productiveZone;
            rawMaterialPayRoll = rawMaterialPayRollService.getTotalsRawMaterialPayRoll(startDate, endDate, zone, metaProduct);

            System.out.println("=====> PERIODO: " + periodo.getResourceKey().toString());
            params.put("reportTitle", messages.get("Report.titleGeneral"));
            params.put("periodo", (periodo.getResourceKey().toString() == "Periodo.first") ? "1RA QUINCENA" : "2DA QUINCENA" + " " + getMes(month));
            params.put("startDate", df.format(dateIni.getTime()));
            params.put("endDate", df.format(dateEnd.getTime()));
            params.put("nombre_gab", "GAB: " + zone.getNumber() + " - " + zone.getName());


            params.put("totalCollectedByGAB", rawMaterialPayRoll.getTotalCollectedByGAB());
            params.put("totalMountCollectdByGAB", rawMaterialPayRoll.getTotalMountCollectdByGAB());
            params.put("totalRetentionGAB", rawMaterialPayRoll.getTotalRetentionGAB());
            params.put("totalCreditByGAB", rawMaterialPayRoll.getTotalCreditByGAB());
            params.put("totalVeterinaryByGAB", rawMaterialPayRoll.getTotalVeterinaryByGAB());
            params.put("totalAlcoholByGAB", rawMaterialPayRoll.getTotalAlcoholByGAB());
            params.put("totalConcentratedByGAB", rawMaterialPayRoll.getTotalConcentratedByGAB());
            params.put("totalYogourdByGAB", rawMaterialPayRoll.getTotalYogourdByGAB());
            params.put("totalRecipByGAB", rawMaterialPayRoll.getTotalRecipByGAB());
            params.put("totalDiscountByGAB", rawMaterialPayRoll.getTotalDiscountByGAB());
            params.put("totalCommission", rawMaterialPayRoll.getTotalCommission());
            params.put("totalAdjustmentByGAB", rawMaterialPayRoll.getTotalAdjustmentByGAB());
            params.put("totalReserveDicount", rawMaterialPayRoll.getTotalReserveDicount());
            params.put("totalOtherIncomeByGAB", rawMaterialPayRoll.getTotalOtherIncomeByGAB());
            params.put("totalLiquidByGAB", rawMaterialPayRoll.getTotalLiquidByGAB());
            params.put("dateStart", "Fecha Inicio - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateIni));
            params.put("dateEnd", "Fecha Fin - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateEnd));

            Double liquidPayable = rawMaterialPayRoll.getTotalLiquidByGAB();
            MoneyUtil moneyUtil = new MoneyUtil();
            params.put("literally_money", moneyUtil.Convertir(liquidPayable.toString(), true));

            typedReportData = super.getReport("RawMaterialPayRollReport", "/production/reports/rawMaterialPayRollReport.jrxml", MessageUtils.getMessage("Report.rawMaterialPayRollReportAction"), params);

            if (tomarPrimero) {
                jasperPrint1 = typedReportData.getJasperPrint();
                collectedBayGAB = getbyGABReportTypedReportData().getJasperPrint();
                List pages = collectedBayGAB.getPages();
                for (Object jrPrintPage : pages) {
                    jasperPrint1.addPage((JRPrintPage) jrPrintPage);
                }
                mostrar = typedReportData;
            } else {
                jasperPrint2 = typedReportData.getJasperPrint();
                collectedBayGAB = getbyGABReportTypedReportData().getJasperPrint();
                List pagesCollecteds = collectedBayGAB.getPages();

                for (Object jrPrintPage : pagesCollecteds) {
                    jasperPrint2.addPage((JRPrintPage) jrPrintPage);
                }

                List pages = jasperPrint2.getPages();
                for (Object jrPrintPage : pages) {
                    jasperPrint1.addPage((JRPrintPage) jrPrintPage);
                }
            }
            tomarPrimero = false;
        }

        try {
            mostrar.setJasperPrint(jasperPrint1);
            GenerationReportData generationReportData = new GenerationReportData(mostrar);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
        zone = null;
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                " rawMaterialPayRecord.id, " +
                " rawMaterialProducer.firstName, " +
                " RawMaterialPayRecord.totalAmount, " +
                " rawMaterialPayRoll.unitPrice, " +
                " RawMaterialPayRecord.totalPayCollected, " +
                " rawMaterialProducerDiscount.withholdingTax, " +
                " rawMaterialProducerDiscount.alcohol, " +
                " rawMaterialProducerDiscount.concentrated, " +
                " rawMaterialProducerDiscount.credit, " +
                " rawMaterialProducerDiscount.veterinary, " +
                " rawMaterialProducerDiscount.yogurt, " +
                " rawMaterialProducerDiscount.cans, " +
                " rawMaterialProducerDiscount.otherDiscount, " +
                " rawMaterialPayRecord.productiveZoneAdjustment, " +
                " rawMaterialProducerDiscount.otherIncoming, " +
                " rawMaterialPayRecord.liquidPayable, " +
                " rawMaterialProducer.lastName, " +
                " rawMaterialProducer.maidenName, " +
                " productiveZone.name, " +
                " rawMaterialPayRecord.discountReserve, " +
                " rawMaterialPayRecord.discountGA " +
                " FROM RawMaterialPayRoll rawMaterialPayRoll " +
                " inner join RawMaterialPayRoll.rawMaterialPayRecordList rawMaterialPayRecord " +
                " inner join rawMaterialPayRecord.rawMaterialProducerDiscount rawMaterialProducerDiscount " +
                " inner join rawMaterialProducerDiscount.rawMaterialProducer rawMaterialProducer " +
                " inner join RawMaterialPayRoll.productiveZone productiveZone ";

    }

    private String getMes(Month month) {
        String result = "";
        if (Month.JANUARY == month)
            result = "Enero";
        if (Month.FEBRUARY == month)
            result = "Febrero";
        if (Month.MARCH == month)
            result = "Marzo";
        if (Month.APRIL == month)
            result = "Abril";
        if (Month.MAY == month)
            result = "Mayo";
        if (Month.JUNE == month)
            result = "Junio";
        if (Month.JULY == month)
            result = "Julio";
        if (Month.AUGUST == month)
            result = "Agosto";
        if (Month.SEPTEMBER == month)
            result = "Septiembre";
        if (Month.OCTOBER == month)
            result = "Octubre";
        if (Month.NOVEMBER == month)
            result = "Noviembre";
        if (Month.DECEMBER == month)
            result = "Diciembre";

        return result;
    }

    @Create
    public void init() {

        this.month = Month.getMonth(new Date());
        Calendar end = Calendar.getInstance();
        end.setTime(new Date());
        if(end.get(Calendar.DAY_OF_MONTH) > 15)
            this.periodo = Periodo.SECONDPERIODO;
        else
            this.periodo = Periodo.FIRSTPERIODO;

        restrictions = new String[]{"rawMaterialPayRoll.productiveZone = #{rawMaterialPayRollReportAction.zone}",
                "rawMaterialPayRoll.metaProduct = #{rawMaterialPayRollReportAction.metaProduct}",
                "rawMaterialPayRoll.startDate = #{rawMaterialPayRollReportAction.startDate}",
                "rawMaterialPayRecord.totalAmount <> #{0.0}",
                "rawMaterialPayRoll.endDate = #{rawMaterialPayRollReportAction.endDate}"
        };

        sortProperty = "rawMaterialProducer.firstName";
    }

    private void getTotal(RawMaterialPayRoll rawMaterialPayRoll) {
        //rawMaterialPayRoll.
    }

    public RawMaterialPayRollService getRawMaterialPayRollService() {
        return rawMaterialPayRollService;
    }

    public void setRawMaterialPayRollService(RawMaterialPayRollService rawMaterialPayRollService) {
        this.rawMaterialPayRollService = rawMaterialPayRollService;
    }

    public String getSummaryReportTitle() {
        return summaryReportTitle;
    }

    public void setSummaryReportTitle(String summaryReportTitle) {
        this.summaryReportTitle = summaryReportTitle;
    }

    public String getGestionTitle() {
        return gestionTitle;
    }

    public void setGestionTitle(String gestionTitle) {
        this.gestionTitle = gestionTitle;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public List<GestionPayroll> getGestionPayrollList() {
        return gestionPayrollList;
    }

    public void setGestionPayrollList(List<GestionPayroll> gestionPayrollList) {
        this.gestionPayrollList = gestionPayrollList;
    }

    public void cleanGestionList() {
        setGestionPayrollList(null);
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Periodo periodo) {
        this.periodo = periodo;
    }

    public ProductiveZone getZone() {
        return zone;
    }

    public void setZone(ProductiveZone zone) {
        this.zone = zone;
    }

    @Factory(value = "monthEnumPayRoll")
    public Month[] getMonthEnum() {
        return Month.values();
    }

    @Factory(value = "periodosPayRoll", scope = ScopeType.STATELESS)
    public Periodo[] getPeriodos() {
        return Periodo.values();
    }

    public GeneratedPayrollType getGeneratedPayrollType() {
        return generatedPayrollType;
    }

    public void setGeneratedPayrollType(GeneratedPayrollType generatedPayrollType) {
        this.generatedPayrollType = generatedPayrollType;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public Calendar getDateIni() {
        return dateIni;
    }

    public void setDateIni(Calendar dateIni) {
        this.dateIni = dateIni;
    }

    public Calendar getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Calendar dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getFullNameOfProductiveZone() {
        return (zone == null ? "" : zone.getFullName());
    }

    public void setFullNameOfProductiveZone(String fullName) {

    }

    protected GenericService getService() {
        return rawMaterialPayRollService;
    }

    public void selectProductiveZone(ProductiveZone productiveZone) {
        try {
            productiveZone = getService().findById(ProductiveZone.class, productiveZone.getId());
            setZone(productiveZone);
        } catch (Exception ex) {
            log.error("Caught Error", ex);
        }
    }

    public TypedReportData getbyGABReportTypedReportData() {
        log.debug("Generate RotatoryFundReportAction........");
        String subReportKey = "RAWMATERIALCOLLECTEDBYGABREPORT";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        dateIni = Calendar.getInstance();
        dateEnd = Calendar.getInstance();
        dateIni.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
        dateEnd.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
        sdf.setCalendar(dateIni);
        sdf.setCalendar(dateEnd);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Map params = new HashMap();

        //params.put("reportTitle",messages.get("Report.titleGeneral"));
        params.put("title", messages.get("Report.titleGeneral"));
        params.put("header", messages.get("Report.header.collectedByGAB"));
        params.put("periodo", (periodo.getResourceKey().toString() == "Periodo.first") ? "1RA QUINCENA" : "2DA QUINCENA" + " " + getMes(month));
        params.put("startDate", df.format(dateIni.getTime()));
        params.put("endDate", df.format(dateEnd.getTime()));
        params.put("nombre_gab", "GAB: " + zone.getNumber() + " - " + zone.getName());

        boolean isFirst = (periodo.getResourceKey().toString() == "Periodo.first") ? true : false;

        System.out.println("--------> Periodo: " + periodo.getResourceKey().toString() + " - " + periodo.getInitDay() + " - " +periodo.getEndDay());
        System.out.println("--------> isfirst: " + isFirst);

        String fileReport = (isFirst) ? "rawMaterialCollectedByGAB1daReport.jrxml" : "rawMaterialCollectedByGAB2daReport.jrxml";
        System.out.println("--------> fileReport: " + fileReport);

        int cont = periodo.getInitDay();
        for (int i = periodo.getInitDay(); i <= (isFirst?15:31); i++) {

            params.put("DAY" + cont, "D" + i);
            cont++;
        }

        if (cont <= 31 && isFirst) {
            for (int i = cont; i <= 31; i++) {
                params.put("DAY" + cont, "D" + i);
                cont++;
            }
        }

        params.put("dateStart", "Fecha Inicio - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateIni));
        params.put("dateEnd", "Fecha Fin - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateEnd));

        return super.getReport(
                subReportKey,
                "/production/reports/" + fileReport,
                //getSqlCollected(),
                getSqlOld(),
                params,
                "rotatoryFundReport");

    }

    protected String getSqlNew() {

        int initDay = periodo.getInitDay();
        int endDay = periodo.getEndDay(month.getValue() + 1, gestion.getYear());
        int month_act = (month.getValue()) + 1;
        int cont = 1;
        String sql = "select am.cantidad , pe.nombres||' '||pe.apellidopaterno ||' '||pe.apellidomaterno as productor \n";
        for (int i = initDay; i <= endDay; i++) {
            sql += "       , (case sa.fecha \n" +
                    "             when to_date('" + i + "/" + month_act + "/" + gestion.getYear() + "','dd/mm/yyyy') \n" +
                    "             then am.cantidad\n" +
                    "             end) as D" + cont + "\n";
            cont++;
        }
        sql += "  from sesionacopio sa\n" +
                "                              inner join acopiomateriaprima am\n" +
                "                              on am.idsesionacopio = sa.idsesionacopio\n" +
                "                              inner join persona pe \n" +
                "                              on am.IDPRODUCTORMATERIAPRIMA=pe.idpersona \n" +
                "                              inner join zonaproductiva zp\n" +
                "                              on zp.idzonaproductiva = sa.idzonaproductiva\n" +
                "                              where sa.fecha between to_date('" + initDay + "/" + month_act + "/" + gestion.getYear() + "','dd/mm/yyyy') and to_date('" + endDay + "/" + month_act + "/" + gestion.getYear() + "','dd/mm/yyyy') \n" +
                "                              AND zp.idzonaproductiva = " + zone.getId().toString() + "\n" +
                "                              order by sa.fecha asc\n";

        return sql;
    }

    private String getSqlCollected(){

        int initDay = periodo.getInitDay();
        int endDay = periodo.getEndDay(month.getValue() + 1, gestion.getYear());
        int cont = 1;
        int cantDays = 1;
        String sql = "";
        int month_act = (month.getValue()) + 1;
        sql += "select productor\n" ;

        for (int i = initDay; i <= endDay; i++) {
            //sql += ((cont == 1) ? "" : " , ")+"max(decode(fecha,to_date('" + i + "/" + month_act + "/" + gestion.getYear() + "','dd/mm/yyyy'),cantidad,0)) AS D"+cont+"\n";
            sql += ", max(decode(fecha,to_date('" + i + "/" + month_act + "/" + gestion.getYear() + "','dd/mm/yyyy'),cantidad,0)) AS D"+cont+"\n";
            cont++;
            cantDays ++;
        }
        cont = 1;
        if (periodo.getResourceKey().toString().compareTo("Periodo.first") == 0) {
            sql += ", 0 AS D16 \n";
        }
        else{
            for(int i= cantDays ; i<=17;i++){
                sql += ", 0 as D"+i+"\n";
            }
        }
        for (int i = initDay; i <= endDay; i++) {
            sql += ((cont == 1) ? "," : " + ")+"max(cantidad)\n";
            cont++;
        }

        sql += "       TOTAL\n" +
                "from (\n" +
                "select pe.nombres||' '||pe.apellidopaterno||' '||pe.apellidomaterno as productor ,am.cantidad as cantidad ,sa.fecha as fecha from acopiomateriaprima am\n" +
                "inner join sesionacopio sa\n" +
                "on am.idsesionacopio =sa.idsesionacopio\n" +
                "inner join persona pe\n" +
                "on pe.idpersona = am.idproductormateriaprima\n" +
                "where sa.fecha between to_date('" + initDay + "/" + month_act + "/" + gestion.getYear() + "') and to_date('" + endDay + "/" + month_act + "/" + gestion.getYear() + "','dd/mm/yyyy')\n" +
                "and sa.idzonaproductiva = "+ zone.getId().toString() + "\n" +
                "and am.cantidad <> 0\n" +
                "order by sa.fecha\n" +
                ")\n" +
                "group by productor\n" +
                "order by productor asc";
        return sql;
    }

    /*private String getSqlOld() {
        int initDay = periodo.getInitDay();
        int endDay = periodo.getEndDay(month.getValue() + 1, gestion.getYear());
        int cont = 1;
        String sql = " select \n" +
                " A" + cont + ".productor as productor \n";
        String total = "";
        boolean wasCollected = true;

        for (int i = initDay; i <= endDay; i++) {
            sql += "      , A" + cont + ".CANTIDAD AS D" + cont + "\n";
            total += ((i == initDay) ? "," : "+") + " A" + cont + ".CANTIDAD";
            cont++;
        }
        if (cont < 16 && (periodo.getResourceKey().toString() == "Periodo.first") ? false : true) {
            for (int i = cont; i <= 16; i++) {
                sql += "      , 0.0 AS D" + cont + "\n";
                cont++;
            }
        }
        sql += total + " AS TOTAL \n";
        sql += " from \n";
        cont = 1;
        int month_act = (month.getValue()) + 1;

        for (int i = initDay; i <= endDay; i++) {
            Calendar date_aux = Calendar.getInstance();
            date_aux.set(gestion.getYear(), month.getValue(), i);
            wasCollected = rawMaterialPayRollService.verifDayColected(date_aux, zone);
            if (wasCollected)
                sql += ((cont == 1) ? "" : " , ") + "   (select am.cantidad , pe.nombres||' '||pe.apellidopaterno ||' '||pe.apellidomaterno as productor, am.IDPRODUCTORMATERIAPRIMA\n" +
                        "  from sesionacopio sa\n" +
                        "                              inner join acopiomateriaprima am\n" +
                        "                              on am.idsesionacopio = sa.idsesionacopio\n" +
                        "                              inner join persona pe \n" +
                        "                              on am.IDPRODUCTORMATERIAPRIMA=pe.idpersona \n" +
                        "                              inner join zonaproductiva zp\n" +
                        "                              on zp.idzonaproductiva = sa.idzonaproductiva\n" +
                        "                              where sa.fecha = STR_TO_DATE('" + i + "," + month_act + "," + gestion.getYear() + "','%d,%m,%Y') \n" +
                        "                              AND zp.idzonaproductiva = " + zone.getId().toString() + "\n" +
                        "                              ORDER BY pe.nombres\n" +
                        "      ) A" + cont + ((i != endDay) ? "\n" : "\n");
            else
                sql += ((cont == 1) ? "" : " , ") + "    (select 0.0 AS CANTIDAD , pe.nombres||' '||pe.apellidopaterno ||' '||pe.apellidomaterno as productor, PM.IDPRODUCTORMATERIAPRIMA\n" +
                        "  from PRODUCTORMATERIAPRIMA PM                             \n" +
                        "                              inner join persona pe \n" +
                        "                              on PM.IDPRODUCTORMATERIAPRIMA=pe.idpersona                               \n" +
                        "                              WHERE PM.idzonaproductiva = " + zone.getId().toString() + "\n" +
                        "                              ORDER BY pe.nombres" +
                        "      ) A" + cont + ((i != endDay) ? "\n" : "\n");

            cont++;
        }
        cont = 2;

        for (int i = initDay; i < endDay; i++) {
            sql += ((cont == 2) ? " WHERE A" : " and A") + 1 + ".IDPRODUCTORMATERIAPRIMA = A" + cont + ".IDPRODUCTORMATERIAPRIMA\n";
            cont++;
        }

        return sql;
    }*/
    private String getSqlOld() {

        int initDay = periodo.getInitDay();
        int endDay = periodo.getEndDay(month.getValue() + 1, gestion.getYear());
        String sql = "SELECT \tp.productor AS productor, \n";
        int cont = 1;
        for(int d=initDay;d<=endDay;d++ )
        {
            sql +=" IFNULL(d"+cont+".cantidad, 0) D"+cont+",\n";
            cont ++;
        }
        if (periodo.getResourceKey().toString().equals("Periodo.first")) {
            sql +=" 0.0 D16,\n";
        }
        if (periodo.getResourceKey().toString().equals("Periodo.second") && endDay == 28){
            sql +=" 0.0 D14,\n";
            sql +=" 0.0 D15,\n";
            sql +=" 0.0 D16,\n";
        }
        if (periodo.getResourceKey().toString().equals("Periodo.second") && endDay == 29){
            sql +=" 0.0 D15,\n";
            sql +=" 0.0 D16,\n";
        }
        if (periodo.getResourceKey().toString().equals("Periodo.second") && endDay == 30) {
            sql +=" 0.0 D16,\n";
        }
        cont = 1;
        //sql += " IFNULL(d"+cont+".cantidad, 0) ";
        for(int d=initDay+1;d<=endDay+1;d++)
        {
            sql += " +IFNULL(d"+cont+".cantidad, 0) ";
            cont++;
        }
        sql +=" TOTAL \n" + " FROM\n" +
                "(\tSELECT pm.`idzonaproductiva`, pm.`idproductormateriaprima`, CONCAT(pe.`apellidopaterno`,' ',pe.`apellidomaterno`,' ',pe.`nombres`) AS productor, 0 AS cantidad\n" +
                "\tFROM productormateriaprima pm                             \n" +
                "\tLEFT JOIN persona pe ON pm.`idproductormateriaprima` = pe.idpersona\n" +
                ")p\n";
        int month_act = (month.getValue()) + 1;
        cont = 1;
        for(int d=initDay;d<=endDay;d++)
        {
            sql +="LEFT JOIN \n" +
                    "\t(\tSELECT sa.`fecha`, sa.`idzonaproductiva`, am.`idproductormateriaprima`, am.`cantidad`\n" +
                    "\t\tFROM acopiomateriaprima am\n" +
                    "\t\t\tLEFT JOIN sesionacopio sa ON am.`idsesionacopio` \t  = sa.`idsesionacopio`\n" +
                    "\t\t\tLEFT JOIN persona pe      ON am.`idproductormateriaprima` = pe.`idpersona`\n" +
                    "\t\tWHERE sa.`fecha` = '"+gestion.getYear()+"-"+month_act+"-"+d+"'\n" +
                    "\t) d"+cont+" \tON p.idproductormateriaprima = d"+cont+".idproductormateriaprima\n";
            cont++;
        }
        sql +=" WHERE p.idzonaproductiva = " + zone.getId().toString();

        return sql;
    }

    /*private String getSqlOldFeb29() {

        int initDay = periodo.getInitDay();
        int endDay = periodo.getEndDay(month.getValue() + 1, gestion.getYear());
        String sql = "SELECT \tp.productor AS productor, \n";
        int cont = 1;
        for(int d=initDay;d<=endDay;d++ )
        {
            sql +=" IFNULL(d"+cont+".cantidad, 0) D"+cont+",\n";
            cont ++;
        }
        if (periodo.getResourceKey().toString().equals("Periodo.first")) {
            sql +=" 0.0 D16,\n";
        }

        if (periodo.getResourceKey().toString().equals("Periodo.second") && endDay == 28){
            sql +=" 0.0 D14,\n";
            sql +=" 0.0 D15,\n";
            sql +=" 0.0 D16,\n";
        }

        if (periodo.getResourceKey().toString().equals("Periodo.second") && endDay == 29){
            sql +=" 0.0 D15,\n";
            sql +=" 0.0 D16,\n";
        }

        if (periodo.getResourceKey().toString().equals("Periodo.second") && endDay == 30){
            sql +=" 0.0 D16,\n";
        }

        cont = 1;

        //sql += " IFNULL(d"+cont+".cantidad, 0) ";
        for(int d=initDay+1;d<=endDay+1;d++)
        {
            sql += " +IFNULL(d"+cont+".cantidad, 0) ";
            cont++;
        }
        sql +=" TOTAL \n" + " FROM\n" +
                "(\tSELECT pm.`idzonaproductiva`, pm.`idproductormateriaprima`, CONCAT(pe.`apellidopaterno`,' ',pe.`apellidomaterno`,' ',pe.`nombres`) AS productor, 0 AS cantidad\n" +
                "\tFROM productormateriaprima pm                             \n" +
                "\tLEFT JOIN persona pe ON pm.`idproductormateriaprima` = pe.idpersona\n" +
                ")p\n";
        int month_act = (month.getValue()) + 1;
        cont = 1;
        for(int d=initDay;d<=endDay;d++)
        {
            sql +="LEFT JOIN \n" +
                    "\t(\tSELECT sa.`fecha`, sa.`idzonaproductiva`, am.`idproductormateriaprima`, am.`cantidad`\n" +
                    "\t\tFROM acopiomateriaprima am\n" +
                    "\t\t\tLEFT JOIN sesionacopio sa ON am.`idsesionacopio` \t  = sa.`idsesionacopio`\n" +
                    "\t\t\tLEFT JOIN persona pe      ON am.`idproductormateriaprima` = pe.`idpersona`\n" +
                    "\t\tWHERE sa.`fecha` = '"+gestion.getYear()+"-"+month_act+"-"+d+"'\n" +
                    "\t) d"+cont+" \tON p.idproductormateriaprima = d"+cont+".idproductormateriaprima\n";
            cont++;
        }
        sql +=" WHERE p.idzonaproductiva = " + zone.getId().toString();

        return sql;
    }*/

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}