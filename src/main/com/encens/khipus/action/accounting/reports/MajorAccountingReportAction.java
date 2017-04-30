package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

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

    @In(create = true)
    VoucherUpdateAction voucherUpdateAction;
    @In
    private VoucherService voucherService;
    @In
    private VoucherAccoutingService voucherAccoutingService;


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

        String documentTitle = "MAYOR CONTABLE";
        String cashAccountName = this.cashAccount.getFullName();

        Double balance = voucherAccoutingService.getBalance(startDate, cashAccount.getAccountCode());

        log.debug("Generating products produced report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("documentTitle", documentTitle);
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
