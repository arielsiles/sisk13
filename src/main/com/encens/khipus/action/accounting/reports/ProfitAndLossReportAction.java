package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

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

@Name("profitAndLossReportAction")
@Scope(ScopeType.PAGE)
public class ProfitAndLossReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

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

        return ejbql;
    }

    public void generateReport() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        String documentTitle = "ESTADO  DE  GANANCIAS  Y  PERDIDAS";

        Double totalProfits = voucherAccoutingService.getTotalProfits(start, end);
        Double totalLosses  = voucherAccoutingService.getTotalLosses(start, end);
        Double totalResults = totalProfits - totalLosses;

        log.debug("Generating balance sheet report...................");

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("documentTitle", documentTitle);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("totalProfits", totalProfits);
        params.put("totalLosses", totalLosses);
        params.put("totalResults", totalResults);

        addCriteriaProfitSubReport("PROFITSUBREPORT", params);
        addCriteriaLossSubReport("LOSSSUBREPORT", params);


        /*setReportFormat(ReportFormat.PDF);*/
        super.generateReport("profitAndLossReport",
                "/accounting/reports/profitAndLossReport.jrxml",
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
                "/accounting/reports/lossReport.jrxml",
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
                "/accounting/reports/profitReport.jrxml",
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
