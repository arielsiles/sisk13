package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.util.DateUtils;
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

@Name("kardexProviderReportAction")
@Scope(ScopeType.PAGE)
public class KardexProviderReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

    private CashAccount cashAccount;
    private Provider provider;

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

        String ejbql = "";

        if (provider != null) {

            ejbql = " SELECT " +
                    "        voucher.date, " +
                    "        voucherDetail.account as account, " +
                    "        voucher.documentType, " +
                    "        voucher.documentNumber as transactionNumber, " +
                    "        voucher.gloss, " +
                    "        voucherDetail.debit as debit, " +
                    "        voucherDetail.credit as credit " +
                    "  FROM  Voucher voucher " +
                    "  LEFT  JOIN voucher.voucherDetailList voucherDetail " +
                    "  WHERE voucher.date between '" + start + "' and '" + end + "' " +
                    "  AND   voucherDetail.account = '" + cashAccount.getAccountCode() + "'" +
                    "  AND   voucherDetail.providerCode = '" + provider.getProviderCode() + "' " +
                    "  AND   voucher.state <> 'ANL' " +
                    "  order by voucher.date";
        }else {
            if (cashAccount != null) {
                ejbql = " SELECT " +
                        "        voucherDetail.providerCode, " +
                        "        entity.acronym, " +
                        "        SUM(voucherDetail.debit) as totalDebit, " +
                        "        SUM(voucherDetail.credit) as totalCredit " +
                        "  FROM  Voucher voucher " +
                        "  JOIN voucher.voucherDetailList voucherDetail " +
                        "  JOIN voucherDetail.provider.entity entity " +
                        "  WHERE voucher.date between '" + start + "' and '" + end + "' " +
                        "  AND   voucherDetail.account = '" + cashAccount.getAccountCode() + "'" +
                        "  AND   voucher.state <> 'ANL' " +
                        "  group by voucherDetail.providerCode, entity.acronym " +
                        "  order by entity.acronym";
            }else{
                /*ejbql = " SELECT " +
                        " cashAccount.accountCode AS accountCode," +
                        " cashAccount.description AS description," +
                        " entity.id AS providerCode, " +
                        " entity.acronym AS acronym," +
                        " SUM(voucherDetail.debit) as debit, " +
                        " SUM(voucherDetail.credit) as credit " +
                        " FROM Voucher voucher" +
                        " LEFT JOIN voucher.voucherDetailList voucherDetail" +
                        " LEFT JOIN voucherDetail.provider.entity entity" +
                        " LEFT JOIN voucherDetail.cashAccount cashAccount " +
                        " WHERE voucher.date between '" + start + "' and '" + end + "' " +  <<--------- Modify
                        " AND   voucher.state <> 'ANL' " +
                        " AND   voucherDetail.providerCode IS NOT NULL " +
                        " GROUP BY cashAccount.accountCode, cashAccount.description, entity.id, entity.acronym " +
                        " ORDER BY cashAccount.accountCode, cashAccount.description, entity.acronym ";*/
                ejbql = " SELECT " +
                        " cashAccount.accountCode AS accountCode," +
                        " cashAccount.description AS description," +
                        " entity.id AS providerCode, " +
                        " entity.acronym AS acronym," +
                        " SUM(voucherDetail.debit) as debit, " +
                        " SUM(voucherDetail.credit) as credit " +
                        " FROM Voucher voucher" +
                        " LEFT JOIN voucher.voucherDetailList voucherDetail" +
                        " LEFT JOIN voucherDetail.provider.entity entity" +
                        " LEFT JOIN voucherDetail.cashAccount cashAccount " +
                        " WHERE voucher.date <= '" + end + "' " +
                        " AND   voucher.state <> 'ANL' " +
                        " AND   voucherDetail.providerCode IS NOT NULL " +
                        " GROUP BY cashAccount.accountCode, cashAccount.description, entity.id, entity.acronym " +
                        " ORDER BY cashAccount.accountCode, cashAccount.description, entity.acronym ";
            }
        }
        return ejbql;
    }

    public void generateReport() {

        if(provider != null) {
            String documentTitle = "KARDEX - PROVEEDORES";
            String cashAccountName = this.cashAccount.getFullName();
            String providerName = provider.getEntity().getAcronym();

            Double balance = voucherAccoutingService.getBalanceProvider(startDate, cashAccount.getAccountCode(), provider.getProviderCode());

            log.debug("Generating provider kardex report...................");

            HashMap<String, Object> reportParameters = new HashMap<String, Object>();
            reportParameters.put("documentTitle", documentTitle);
            reportParameters.put("startDate", startDate);
            reportParameters.put("endDate", endDate);
            reportParameters.put("cashAccount", cashAccountName);
            reportParameters.put("balance", balance);
            reportParameters.put("provider", providerName);

            /*setReportFormat(ReportFormat.PDF);*/
            super.generateReport(
                    "majorAccountingReport",
                    "/accounting/reports/kardexProviderReport.jrxml",
                    PageFormat.LETTER,
                    PageOrientation.PORTRAIT,
                    messages.get("menu.finances.accounting.kardexProvider"),
                    reportParameters);
        }else{
            if (cashAccount != null) {
                String documentTitle = "ESTADO AUXILIAR ANALITICO";
                String cashAccountName = this.cashAccount.getFullName();

                log.debug("Generating summary provider kardex report...................");

                HashMap<String, Object> reportParameters = new HashMap<String, Object>();
                reportParameters.put("documentTitle", documentTitle);
                reportParameters.put("startDate", startDate);
                reportParameters.put("endDate", endDate);
                reportParameters.put("cashAccount", cashAccountName);
                reportParameters.put("cashAccountCode", cashAccount.getAccountCode());

                /*setReportFormat(ReportFormat.PDF);*/
                super.generateReport(
                        "summaryProviderKardexReport",
                        "/accounting/reports/summaryProviderKardexReport.jrxml",
                        PageFormat.LETTER,
                        PageOrientation.PORTRAIT,
                        messages.get("menu.finances.accounting.summaryProviderKardex"),
                        reportParameters);
            }else{

                log.debug("Generating analytical auxiliary state report...................");
                HashMap<String, Object> reportParameters = new HashMap<String, Object>();
                reportParameters.put("startDate", startDate);
                reportParameters.put("endDate", endDate);

                super.generateReport(
                        "summaryProviderKardexReport",
                        "/accounting/reports/analyticalAuxiliaryStateReport.jrxml",
                        PageFormat.LETTER,
                        PageOrientation.PORTRAIT,
                        messages.get("Reports.accounting.analyticalAuxiliaryState.title"),
                        reportParameters);

            }
        }
    }

    public void clearAccount() {
        setCashAccount(null);
    }

    public void clearProvider() {
        setProvider(null);
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

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }
}
