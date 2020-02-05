package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.finances.VoucherServiceBean;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("majorAccountingReportAction")
@Scope(ScopeType.PAGE)
public class MajorAccountingReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

    private CashAccount cashAccount;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;
    @In(create = true)
    VoucherUpdateAction voucherUpdateAction;
    @In
    private VoucherService voucherService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private CashAccountService cashAccountService;


    @Create
    public void init() {
        restrictions = new String[]{
                //"voucherDetail.account=#{majorAccountingReportAction.cashAccount.accountCode}"
        };
        //sortProperty = "date";
    }

    @Override
    protected String getEjbql() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        return  " SELECT " +
                "        voucher.date, " +
                "        voucherDetail.account as account, " +
                "        voucher.documentType, " +
                "        voucher.documentNumber as transactionNumber, " +
                "        voucher.gloss, " +
                "        voucherDetail.debit as debit, " +
                "        voucherDetail.credit as credit " +
                "  FROM  Voucher voucher " +
                "  LEFT  JOIN voucher.voucherDetailList voucherDetail " +
                "  WHERE voucher.date between '"+start+"' and '"+end+"' " +
                "  AND   voucherDetail.account = '"+ cashAccount.getAccountCode() +"'" +
                "  AND   voucher.state <> 'ANL'" +
                "  order by voucher.date";
    }

    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        String cashAccountName = this.cashAccount.getFullName();
        Double balance = voucherAccoutingService.getBalance(startDate, cashAccount.getAccountCode());

        log.debug("Generating products produced report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("documentTitle", messages.get("MajorAccounting.report.title"));
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("startDate",startDate);
        reportParameters.put("endDate",endDate);
        reportParameters.put("cashAccount",cashAccountName);
        reportParameters.put("balance",balance);

        /*setReportFormat(ReportFormat.PDF);*/
        super.generateReport(
                "majorAccountingReport",
                "/accounting/reports/majorAccountingReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("MajorAccounting.report"),
                reportParameters);
    }


    public void generateCSV(){
        final String fileName = "d:/mayor.csv";
        final String NEXT_LINE = "\n";
        String delim = "|";

        String start = DateUtils.format(startDate, "yyyy-MM-dd");
        String end = DateUtils.format(endDate, "yyyy-MM-dd");


        try {
            FileWriter fw = new FileWriter(fileName);

            List<CashAccount> cashAccountList = cashAccountService.findCashAccountList();
            boolean flag = false;
            fw.append("CUENTA").append(delim).append("FECHA").append(delim).append("TIPO").append(delim).append("NO_DOC").append(delim).append("GLOSA").append(delim).append("DEBE").append(delim).append("HABER").append(delim).append("SALDO").append(NEXT_LINE);
            for (CashAccount ca:cashAccountList){

                BigDecimal debit  = BigDecimal.ZERO;
                BigDecimal credit = BigDecimal.ZERO;
                // Double balance = voucherAccoutingService.getBalance(startDate, ca.getAccountCode());
                Double balance = 0.0;

                List<VoucherServiceBean.VoucherTransaction> voucherTransactionList = voucherService.getTransactionMajorAccounting(start, end, ca.getAccountCode());
                for(VoucherServiceBean.VoucherTransaction voucherTransaction:voucherTransactionList){
                    debit = voucherTransaction.getDebit();
                    credit = voucherTransaction.getCredit();
                    balance = balance.doubleValue() + debit.doubleValue() - credit.doubleValue();

                    //if (balance > 0){
                        flag = true;

                        String gloss = voucherTransaction.getGloss().replaceAll("[\n\r]", "");

                        fw.append(ca.getFullName()).append(delim);
                        fw.append(voucherTransaction.getDate()).append(delim);
                        fw.append(voucherTransaction.getDocumentType()).append(delim);
                        fw.append(voucherTransaction.getDocumentNumber()).append(delim);
                        fw.append(gloss).append(delim);
                        fw.append(voucherTransaction.getDebit().toString()).append(delim);
                        fw.append(voucherTransaction.getCredit().toString()).append(delim);
                        fw.append(balance.toString()).append(NEXT_LINE);
                    //}
                }
                fw.append(NEXT_LINE);
                /*if (flag){
                    fw.append(NEXT_LINE);
                    flag = false;
                }*/
            }

            fw.flush();
            fw.close();

        } catch (IOException e) { e.printStackTrace(); }

    }


    private static void crearArchivoCSV(String file, String delim) {
        final String NEXT_LINE = "\n";
        try {
            FileWriter fw = new FileWriter(file);

            fw.append("testing").append(delim);
            fw.append("123").append(NEXT_LINE);

            fw.append("value1");
            fw.append(delim);
            fw.append("312");
            fw.append(NEXT_LINE);

            fw.append("anotherthing,888\n");

            fw.flush();
            fw.close();
        } catch (IOException e) {
            // Error al crear el archivo, por ejemplo, el archivo
            // está actualmente abierto.
            e.printStackTrace();
        }
    }

    public void clearAccount() {
        setCashAccount(null);
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

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }
}
