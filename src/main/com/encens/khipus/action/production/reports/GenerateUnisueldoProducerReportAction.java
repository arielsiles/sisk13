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
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.BoletaPagoProductor;
import com.encens.khipus.service.production.ProductiveZoneService;
import com.encens.khipus.service.production.RawMaterialPayRollService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id:
 */
@Name("generateUnisueldoProducerReportAction")
@Scope(ScopeType.CONVERSATION)
public class GenerateUnisueldoProducerReportAction extends GenericReportAction {
    @In
    RawMaterialPayRollService rawMaterialPayRollService;

    @In
    ProductiveZoneService productiveZoneService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    private String summaryReportTitle;
    private String gestionTitle;

    private Gestion gestion;
    private Month month;
    private Periodo periodo;
    private ProductiveZone zone;
    private MetaProduct metaProduct;
    private Date startDate;
    private Date endDate;
    private Calendar dateIni;
    private Calendar dateEnd;
    private RawMaterialProducer rawMaterialProducer;

    private List<GestionPayroll> gestionPayrollList;


    private GeneratedPayrollType generatedPayrollType = GeneratedPayrollType.OFFICIAL;


    public void generateReport() throws ParseException, CompanyConfigurationNotFoundException {
        log.debug("Generate RotatoryFundReportAction........");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        dateIni = Calendar.getInstance();
        dateEnd = Calendar.getInstance();
        dateIni.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
        dateEnd.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
        sdf.setCalendar(dateIni);
        sdf.setCalendar(dateEnd);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        dateIni.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
        dateEnd.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
        startDate = dateFormat.parse(dateFormat.format(dateIni.getTime()));
        endDate = dateFormat.parse(dateFormat.format(dateEnd.getTime()));

        List<BoletaPagoProductor> boletaPagoProductors = rawMaterialPayRollService.findBoletaDePago(startDate, endDate, rawMaterialProducer, zone, metaProduct);

        BigDecimal totalLiquid = BigDecimal.ZERO;
        Integer number = 0;
        for (BoletaPagoProductor boleta : boletaPagoProductors){
            if (boleta.getNumerocuenta() != null) {
                totalLiquid = BigDecimalUtil.sum(totalLiquid, BigDecimalUtil.toBigDecimal(boleta.getLiquidoPagable()), 2);
                number++;
            }
        }

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        String str30 = "                              ";
        String str12 = "000000000000";
        String str05 = "00000";

        String line1 = "";
        String strDate      = DateUtils.format(new Date(), "ddMMyyyy");
        String description  = "Pago por entrega de leche";
        description         = convertLeft(description, str30);
        String strQuantity  = convert(number.toString(), str05);

        line1 = description + strQuantity + strDate;

        String line2 = "";
        String strTotalLiquid = convert(totalLiquid.toString(), str12);
        String strPagoCta     = companyConfiguration.getForeignBankAccountForPayment().getAccountNumber();
        line2 = strPagoCta + strTotalLiquid;

        Collection<GenerateUnisueldoProducerReportAction.CollectionData> beanCollection = new ArrayList();

        beanCollection.add(new GenerateUnisueldoProducerReportAction.CollectionData(line1));
        beanCollection.add(new GenerateUnisueldoProducerReportAction.CollectionData(line2));

        for (BoletaPagoProductor boleta : boletaPagoProductors){

            if (boleta.getNumerocuenta() != null){
                totalLiquid = BigDecimalUtil.sum(totalLiquid, BigDecimalUtil.toBigDecimal(boleta.getLiquidoPagable()), 2);
                String strLiquid = convert(boleta.getLiquidoPagable().toString(), str12);
                String register = boleta.getNumerocuenta() + strLiquid + "1";

                GenerateUnisueldoProducerReportAction.CollectionData data = new GenerateUnisueldoProducerReportAction.CollectionData(register);
                beanCollection.add(data);
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("line1", line1);
        params.put("line2", line2);

        try{
            File jasper = new File(JSFUtil.getRealPath("/employees/reports/payrollBankUnisueldoReport.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), params, new JRBeanCollectionDataSource(beanCollection));
            exportarExcel(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String convert(String number, String strBuffer){
        StringBuffer result = new StringBuffer(strBuffer);
        result.replace(result.length() - number.length(), result.length(), number);
        return result.toString();
    }

    public String convertLeft(String number, String strBuffer){
        StringBuffer result = new StringBuffer(strBuffer);
        result.replace(0, result.length() - (result.length() - number.length()), number);
        return result.toString();
    }

    public void exportarExcel(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=unisueldo.xlsx");
        ServletOutputStream stream = response.getOutputStream();
        //JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
        //stream.flush();
        //stream.close();
        //FacesContext.getCurrentInstance().responseComplete();


        JRXlsxExporter exporter = new JRXlsxExporter();
        //JRTextExporter exporter = new JRTextExporter();
        //exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, 43);
        //exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, 10);
        exporter.setParameter(JRTextExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRTextExporterParameter.OUTPUT_STREAM, stream);
        //exporter.setParameter(JRXlsExporterParameter.OUTPUT_FILE_NAME, "unisueldo.xls");
        exporter.exportReport();
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }



    @Override
    protected String getEjbql() {
        return "";
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

    public void selectRawMaterialProducer(RawMaterialProducer producer) {
        try {
            producer = getService().findById(RawMaterialProducer.class, producer.getId());
            setRawMaterialProducer(producer);
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

    public RawMaterialProducer getRawMaterialProducer() {
        return rawMaterialProducer;
    }

    public void setRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        this.rawMaterialProducer = rawMaterialProducer;
    }

    public void clearRawMaterialProducer() {
        rawMaterialProducer = null;
    }

    public class CollectionData{

        private String register;

        public CollectionData(String register){
            this.setRegister(register);
        }

        public String getRegister() {
            return register;
        }

        public void setRegister(String register) {
            this.register = register;
        }
    }

}