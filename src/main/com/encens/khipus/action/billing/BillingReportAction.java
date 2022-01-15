package com.encens.khipus.action.billing;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.employees.ManagersPayrollService;
import com.encens.khipus.service.employees.ManagersPayrollSummaryService;
import com.encens.khipus.service.employees.PayrollReportService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: PayrollBankReportAction.java  28-ene-2010 15:55:38$
 */
@Name("billingReportAction")
@Scope(ScopeType.PAGE)
public class BillingReportAction extends GenericReportAction {
    private String ejbql;
    private GeneratedPayroll generatedPayroll;
    private PaymentType paymentTypeBankAccount = PaymentType.PAYMENT_BANK_ACCOUNT;

    @In
    private ManagersPayrollService managersPayrollService;
    @In
    private PayrollReportService payrollReportService;
    @In
    private ManagersPayrollSummaryService managersPayrollSummaryService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private SaleService saleService;

    public void generateReport(CustomerOrder order) throws CompanyConfigurationNotFoundException {
        log.debug("payrollEmployeeWithUnisueldo executing.................................................");
        CustomerOrder customerOrder = saleService.findSaleById(order.getId());
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal sumLiquid = BigDecimal.TEN;

        String str30 = "..............................";
               str30 = "                              ";
        String str12 = "000000000000";
        String str05 = "00000";

        String line1 = "";
        Date date = new Date();
        String strDate      = DateUtils.format(date, "ddMMyyyy");
        String month        = "DICIEMBRE";
        String year         = "2021";
        String description  = MessageUtils.getMessage("ManagersPayroll.paymentUnisueldo") + " " + month + " " + year;
        description         = convertLeft(description, str30);
        String strQuantity  = "1025";

        //line1 = customerOrder.getClient().getFullName();
        line1 = "";
        String line2 = "";
        String strTotalLiquid = convert(sumLiquid.toString(), str12);
        String strPagoCta     = companyConfiguration.getForeignBankAccountForPayment().getAccountNumber();
        line2 = strPagoCta + strTotalLiquid;

        Collection<CollectionData> beanCollection = new ArrayList();

        //beanCollection.add(new CollectionData(line1));
        //beanCollection.add(new CollectionData(line2));

        String decodeInvoice = getDecodeInvoice(customerOrder.getMovement().getFactura());
        Scanner scan = new Scanner(decodeInvoice);
        System.out.println("--------- SCAN ----------");
        while(scan.hasNextLine()){
            String line = scan.nextLine();
            System.out.println("SCAN: " + line);
            CollectionData data = new CollectionData(line);
            beanCollection.add(data);
        }


        Map<String, Object> params = new HashMap<String, Object>();
        //params.put("line1", line1);
        //params.put("line2", line2);

        try{
            File jasper = new File(JSFUtil.getRealPath("/customers/reports/billingFileReport.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), params, new JRBeanCollectionDataSource(beanCollection));

            //exportarPDF(jasperPrint);
            //exportarExcel(jasperPrint);
            exportarOther(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }

        //super.generateReport("payrollBankReport", "/employees/reports/payrollBankUnisueldoReport.jrxml", PageFormat.LETTER, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.payrollBankReport"), params);
    }

    public void exportarPDF(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=unisueldo.pdf");
        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void exportarOther(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=FACTURA.xml");
        ServletOutputStream stream = response.getOutputStream();

        JasperExportManager.exportReportToXmlStream(jasperPrint, stream);

        /*JRTextExporter exporter = new JRTextExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);*/


        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
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


    public String getDecodeInvoice(String encodedInput){
        // decode the encoded data
        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(encodedInput));
        System.out.println("Decoded Data: " + decoded);
        return decoded;
    }

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    @Override
    protected String getEjbql() {
        return "";
    }


    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    public PaymentType getPaymentTypeBankAccount() {
        return paymentTypeBankAccount;
    }

    public void setPaymentTypeBankAccount(PaymentType paymentTypeBankAccount) {
        this.paymentTypeBankAccount = paymentTypeBankAccount;
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
