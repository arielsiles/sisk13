package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("profitAndLossFinancialN3ReportAction")
@Scope(ScopeType.PAGE)
public class ProfitAndLossFinancialN3ReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

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


    @Create
    public void init() {
        restrictions = new String[]{};
    }

    @Override
    protected String getEjbql() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        String ejbql = "";

        return ejbql;
    }

    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        Double totalProfits = voucherAccoutingService.getTotalProfits(start, end);
        Double totalLosses  = voucherAccoutingService.getTotalLosses(start, end);
        Double totalResults = totalProfits - totalLosses;

        log.debug("Generating balance sheet report...................");

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("documentTitle", messages.get("ProfitAndLoss.report.title"));
        params.put("companyName", companyConfiguration.getCompanyName());
        params.put("systemName", companyConfiguration.getSystemName());
        params.put("locationName", companyConfiguration.getLocationName());

        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("totalProfits", totalProfits);
        params.put("totalLosses", totalLosses);
        params.put("totalResults", totalResults);

        addCriteriaProfitSubReport("PROFITSUBREPORT", params);
        //addCriteriaLossSubReport("LOSSSUBREPORT", params);

        Double account41  = voucherAccoutingService.calculateLossesNiv2(start, end, "4100000000").doubleValue();
        Double account42  = voucherAccoutingService.calculateLossesNiv2(start, end, "4200000000").doubleValue();
        Double account43  = voucherAccoutingService.calculateLossesNiv2(start, end, "4300000000").doubleValue();
        Double account44  = voucherAccoutingService.calculateLossesNiv2(start, end, "4400000000").doubleValue();
        Double account45  = voucherAccoutingService.calculateLossesNiv2(start, end, "4500000000").doubleValue();
        Double account46  = voucherAccoutingService.calculateLossesNiv2(start, end, "4600000000").doubleValue();
        Double account47  = voucherAccoutingService.calculateLossesNiv2(start, end, "4700000000").doubleValue();

        params.put("account41", account41);
        params.put("account42", account42);
        params.put("account43", account43);
        params.put("account44", account44);
        params.put("account45", account45);
        params.put("account46", account46);
        params.put("account47", account47);

        Double account442 = voucherAccoutingService.calculateLossesNiv3(start, end, "4420000000").doubleValue();
        Double account443 = voucherAccoutingService.calculateLossesNiv3(start, end, "4430000000").doubleValue();

        Double account445 = voucherAccoutingService.calculateLossesNiv3(start, end, "4450000000").doubleValue();
        Double account446 = voucherAccoutingService.calculateLossesNiv3(start, end, "4460000000").doubleValue();
        Double account447 = voucherAccoutingService.calculateLossesNiv3(start, end, "4470000000").doubleValue();

        params.put("account442", account442);
        params.put("account443", account443);

        params.put("account445", account445);
        params.put("account446", account446);
        params.put("account447", account447);

        /*setReportFormat(ReportFormat.PDF);*/
        super.generateReport("profitAndLossReport",
                "/accounting/reports/profitAndLossFinancialN3Report.jrxml",
                PageFormat.LETTER, PageOrientation.PORTRAIT, messages.get("ProfitAndLoss.report"), params);

    }

    /**
     * Add evaluation sub report in main report
     *
     * @param subReportKey     key of sub report
     * @param mainReportParams main report params
     */
    private void addCriteriaLossSubReport(String subReportKey, Map mainReportParams) {
        log.debug("Generating addCriteriaAssetSubReport.............................");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql =  " SELECT " +
                        " rootCashAccount.accountCode as accountCode, " +
                        " rootCashAccount.description as description, " +
                        " SUM(voucherDetail.debit) AS debit, " +
                        " SUM(voucherDetail.credit) AS credit" +
                        " FROM VoucherDetail voucherDetail " +
                        " LEFT JOIN voucherDetail.voucher voucher " +
                        " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                        " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                        " WHERE cashAccount.accountType = 'E' " +
                        " AND voucher.state <> 'ANL' " +
                        " AND voucher.date between '"+start+"' and '"+end+"' " +
                        " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        String[] restrictions = new String[]{};
        String orderBy = "cashAccount.accountCode";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/accounting/reports/lossFinancialReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Add evaluation sub report in main report
     *
     * @param subReportKey     key of sub report
     * @param mainReportParams main report params
     */
    private void addCriteriaProfitSubReport(String subReportKey, Map mainReportParams) {
        log.debug("Generating addCriteriaAssetSubReport.............................");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql =  " SELECT " +
                " rootCashAccount.accountCode as accountCode, " +
                " rootCashAccount.description as description, " +
                " SUM(voucherDetail.debit) AS debit, " +
                " SUM(voucherDetail.credit) AS credit" +
                " FROM VoucherDetail voucherDetail " +
                " LEFT  JOIN voucherDetail.voucher voucher " +
                " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                " WHERE cashAccount.accountType = 'I' " +
                " AND voucher.state <> 'ANL' " +
                " AND voucher.date between '"+start+"' and '"+end+"' " +
                " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        String[] restrictions = new String[]{};
        String orderBy = "cashAccount.accountCode";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/accounting/reports/profitFinancialReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
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
