package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.SavingType;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.service.customers.AccountService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("savingKardexReportAction")
@Scope(ScopeType.PAGE)
public class SavingKardexReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;
    @In
    private AccountService accountService;

    private Date startDate;
    private Date endDate;
    private Account savingAccount;
    private SavingType savingTypeDPF = SavingType.DPF;

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    protected String getEjbql() {

        String condition = " voucherDetail.debit, voucherDetail.credit, ";
        if (savingAccount.getCurrency().equals(FinancesCurrencyType.D) || savingAccount.getCurrency().equals(FinancesCurrencyType.M))
            condition = " voucherDetail.debitMe as debit, voucherDetail.creditMe as credit, ";

        String ejbql = " SELECT " +
                " voucher.date, " +
                condition +
                " voucher.gloss, " +
                " voucher.documentType || '-' || voucher.documentNumber as doc " +
                " FROM VoucherDetail voucherDetail" +
                " JOIN voucherDetail.voucher voucher" +
                " WHERE voucherDetail.partnerAccount = #{savingKardexReportAction.savingAccount}" +
                " AND voucher.state <> 'ANL'" +
                " AND voucher.date BETWEEN #{savingKardexReportAction.startDate} AND #{savingKardexReportAction.endDate} " +
                " AND voucherDetail.partnerAccount is not null" +
                " ORDER BY voucher.date";

        return ejbql;
    }


    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        BigDecimal balance = accountService.calculateAccountBalance(savingAccount, DateUtils.getDate(Constants.YEAR, Constants.MONTH, Constants.DAY), DateUtils.addDay(startDate, -1));
        //" partnerAccount.accountType.name || ' - ' || partnerAccount.currency AS accountTypeCurrency, " +
        String fullAccountTypeName = savingAccount.getAccountType().getName() + " (" + messages.get(savingAccount.getCurrency().getSymbolResourceKey()) + ")";

        String documentTitle = messages.get("Reports.account.savingKardex.title");
        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("startDate", startDate);
        reportParameters.put("endDate", endDate);
        reportParameters.put("balance", balance.doubleValue());
        reportParameters.put("fullAccountName", savingAccount.getFullAccountName());
        reportParameters.put("fullAccountTypeName", fullAccountTypeName);

        super.generateReport(
                "savingKardexReport",
                "/customers/reports/savingKardexReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Account.report.savingStatusReport.title"),
                reportParameters);

    }

    public void clearSavingAccount(){
        setSavingAccount(null);
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SavingType getSavingTypeDPF() {
        return savingTypeDPF;
    }

    public void setSavingTypeDPF(SavingType savingTypeDPF) {
        this.savingTypeDPF = savingTypeDPF;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Account getSavingAccount() {
        return savingAccount;
    }

    public void setSavingAccount(Account savingAccount) {
        this.savingAccount = savingAccount;
    }
}