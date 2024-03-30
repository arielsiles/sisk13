package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.Periodo;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialPayRoll;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.ProductiveZoneService;
import com.encens.khipus.service.production.RawMaterialPayRollService;
import com.encens.khipus.service.production.RawMaterialPayRollServiceBean;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.lang.time.FastDateFormat;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

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
@Name("rawMaterialGeneralPayRollReportAction")
@Scope(ScopeType.CONVERSATION)
//@Restrict("#{s:hasPermission('RAWMATERIALPAYSUMMARY','VIEW')}")
public class RawMaterialGeneralPayRollReportAction extends GenericReportAction {
    @In
    RawMaterialPayRollService rawMaterialPayRollService;

    @In
    ProductiveZoneService productiveZoneService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

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

        generarTodosGAB(params, df);

    }

    private void generarTodosGAB(Map params, DateFormat df) throws ParseException {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        JasperPrint jasperPrint1 = new JasperPrint();
        TypedReportData typedReportData;
        TypedReportData mostrar = new TypedReportData();

        String title = "PLANILLA DE PAGO A PRODUCTORES";

        System.out.println("=====> PERIODO: " + periodo.getResourceKey().toString());
        params.put("reportTitle", title);
        params.put("companyName", companyConfiguration.getCompanyName());
        params.put("systemName", companyConfiguration.getSystemName());
        params.put("locationName", companyConfiguration.getLocationName());
        params.put("periodo", (periodo.getResourceKey().toString() == "Periodo.first") ? "1RA QUINCENA" : "2DA QUINCENA" + " " + getMes(month));
        params.put("startDate", df.format(dateIni.getTime()));
        params.put("endDate", df.format(dateEnd.getTime()));

        params.put("dateStart", "Fecha Inicio - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateIni));
        params.put("dateEnd", "Fecha Fin - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateEnd));


        typedReportData = super.getReport("RawMaterialPayRollReport",
                "/production/reports/rawMaterialGeneralPayRollReport.jrxml",
                MessageUtils.getMessage("Report.rawMaterialPayRollReportAction"), params);

        jasperPrint1 = typedReportData.getJasperPrint();

        mostrar = typedReportData;

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
                " productiveZone.name as gab," +
                " rawMaterialProducer.idNumber," +
                " rawMaterialProducer.firstName || ' ' || rawMaterialProducer.lastName || ' ' || rawMaterialProducer.maidenName as name," +
                " rawMaterialPayRecord.totalAmount as quantity," +
                " rawMaterialPayRecord.totalPayCollected as totalPay," +
                " rawMaterialPayRecord.liquidPayable as liquidPay" +
                " FROM RawMaterialPayRecord rawMaterialPayRecord " +
                " JOIN rawMaterialPayRecord.rawMaterialProducerDiscount rawMaterialProducerDiscount" +
                " JOIN rawMaterialProducerDiscount.rawMaterialProducer rawMaterialProducer" +
                " JOIN rawMaterialPayRecord.rawMaterialPayRoll rawMaterialPayRoll" +
                " JOIN rawMaterialPayRoll.productiveZone productiveZone" +
                "";

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

        restrictions = new String[]{
                "rawMaterialPayRoll.metaProduct = #{rawMaterialGeneralPayRollReportAction.metaProduct}",
                "rawMaterialPayRoll.startDate = #{rawMaterialGeneralPayRollReportAction.startDate}",
                "rawMaterialPayRecord.totalAmount <> #{0.0}",
                "rawMaterialPayRoll.endDate = #{rawMaterialGeneralPayRollReportAction.endDate}"
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

    /*@Factory(value = "monthEnumPayRoll")
    public Month[] getMonthEnum() {
        return Month.values();
    }*/

    /*@Factory(value = "periodosPayRoll", scope = ScopeType.STATELESS)
    public Periodo[] getPeriodos() {
        return Periodo.values();
    }*/

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