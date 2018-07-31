package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.action.warehouse.reports.KardexProductMovementAction;
import com.encens.khipus.action.warehouse.reports.ProductInventoryReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.ManagersPayroll;
import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.PaymentType;
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
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
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
import java.util.*;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: PayrollBankReportAction.java  28-ene-2010 15:55:38$
 */
@Name("payrollBankUnisueldoReportAction")
@Scope(ScopeType.PAGE)
public class PayrollBankUnisueldoReportAction extends GenericReportAction {
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

    public void generateReport(GeneratedPayroll generatedPayroll) throws CompanyConfigurationNotFoundException {
        log.debug("payrollEmployeeWithUnisueldo executing.................................................");
        setGeneratedPayroll(getEntityManager().find(GeneratedPayroll.class, generatedPayroll.getId()));
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<ManagersPayroll> managersPayrollList = managersPayrollService.getManagersPayrollToUnisueldo(generatedPayroll);
        BigDecimal sumLiquid = managersPayrollSummaryService.sumLiquidByPaymentType(generatedPayroll.getId(), PaymentType.PAYMENT_BANK_ACCOUNT);

        String str30 = "..............................";
        String str12 = "000000000000";
        String str05 = "00000";

        String line1 = "";
        Date date = new Date();
        String strDate      = DateUtils.format(date, "ddMMyyyy");
        String month        = generatedPayroll.getGestionPayroll().getMonth().getMonthLiteral();
        String year         = DateUtils.getCurrentYear(generatedPayroll.getGestionPayroll().getInitDate()).toString();
        String description  = MessageUtils.getMessage("ManagersPayroll.paymentUnisueldo") + " " + month + " " + year;
        description         = convertLeft(description, str30);
        String strQuantity  = convert(Integer.toString(managersPayrollList.size()), str05);

        line1 = description + strQuantity + strDate;

        String line2 = "";
        String strTotalLiquid = convert(sumLiquid.toString(), str12);
        String strPagoCta     = companyConfiguration.getForeignBankAccountForPayment().getAccountNumber();
        line2 = strPagoCta + strTotalLiquid;

        Collection<CollectionData> beanCollection = new ArrayList();

        beanCollection.add(new CollectionData(line1));
        beanCollection.add(new CollectionData(line2));

        for (ManagersPayroll mp : managersPayrollList){
            BankAccount bankAccount = payrollReportService.getEmployeeDefaultBankAccount(mp.getEmployee().getId());
            String strLiquid = convert(mp.getLiquid().toString(), str12);
            String register = bankAccount.getAccountNumber() + strLiquid + bankAccount.getCurrency().getCurrencyCode();

            CollectionData data = new CollectionData(register);
            beanCollection.add(data);

            //System.out.println("===> " + bankAccount.getAccountNumber()+strLiquid+bankAccount.getCurrency().getCurrencyCode() + " - " + mp.getEmployee().getFullName() + " - " + mp.getLiquid());
        }


        //this.setReportFormat(ReportFormat.XLS);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("line1", line1);
        params.put("line2", line2);

        try{
            File jasper = new File(JSFUtil.getRealPath("/employees/reports/payrollBankUnisueldoReport.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), params, new JRBeanCollectionDataSource(beanCollection));
            //exportarPDF(jasperPrint);
            exportarExcel(jasperPrint);
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
