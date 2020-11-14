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

@Name("balanceSheetFinancialReportAction")
@Scope(ScopeType.PAGE)
public class BalanceSheetFinancialReportAction extends GenericReportAction {

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

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start  = df.format(startDate);
        String end    = df.format(endDate);

        /** Estado de Ganancias y Perdidas **/
        Double totalProfits = voucherAccoutingService.getTotalProfits(start, end);
        Double totalLosses  = voucherAccoutingService.getTotalLosses(start, end);
        Double totalResults = totalProfits - totalLosses;

        /** Estado de Situacion Patrimonial **/
        Double totalAssets      = voucherAccoutingService.getTotalAssets(start, end);
        Double totalLiabilities = voucherAccoutingService.getTotalLiabilities(start, end);
        Double totalCapital     = voucherAccoutingService.getTotalCapital(start, end);

        Double totalLiabilitiesCapital    = totalLiabilities + totalCapital;

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("documentTitle", messages.get("BalanceSheet.report.title"));
        params.put("companyName", companyConfiguration.getCompanyName());
        params.put("systemName", companyConfiguration.getSystemName());
        params.put("locationName", companyConfiguration.getLocationName());
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("totalAssets", totalAssets);
        params.put("totalLiabilities", totalLiabilities);
        params.put("totalCapital", totalCapital);
        params.put("totalLiabilitiesCapital", totalLiabilitiesCapital);


        /** PATRIMONIO **/

        Double capital_social       = voucherAccoutingService.totalCapital(start, end, "3100000000").doubleValue();
        Double capital_aportes      = voucherAccoutingService.totalCapital(start, end, "3200000000").doubleValue();
        Double capital_ajustes      = voucherAccoutingService.totalCapital(start, end, "3300000000").doubleValue();
        Double capital_reservas     = voucherAccoutingService.totalCapital(start, end, "3400000000").doubleValue();
        Double capital_acumulados   = voucherAccoutingService.totalCapital(start, end, "3500000000").doubleValue();

        Double perdidasExcedentes       = voucherAccoutingService.perdidasExcedentesPeriodo(start, end).doubleValue(); // Perdidas/Excedentes Anterior
        Double totalPerdidasExcedentes  = perdidasExcedentes + totalResults;

        params.put("capital_social",     capital_social);
        params.put("capital_aportes",    capital_aportes);
        params.put("capital_ajustes",    capital_ajustes );
        params.put("capital_reservas",   capital_reservas);
        params.put("capital_acumulados", capital_acumulados);

        params.put("totalResults", totalResults); // Del Estado de Ganancias y Perdidas del periodo

        params.put("perdidasExcedentes", perdidasExcedentes);
        params.put("totalPerdidasExcedentes", totalPerdidasExcedentes);



        Double totalPatrimonio = capital_social + capital_aportes + capital_ajustes + capital_reservas + capital_acumulados + totalPerdidasExcedentes;

        Double totalLiabilitiesCapitalRes = totalLiabilities + totalPatrimonio;

        params.put("totalPatrimonio", totalPatrimonio);
        params.put("totalLiabilitiesCapitalRes", totalLiabilitiesCapitalRes);

        addCriteriaAssetSubReport("BALANCESHEETSUBREPORT", params);
        addCriteriaLiabilitiesSubReport("BALANCELIABILITIESSUBREPORT", params);
        //addCriteriaCapitalSubReport("BALANCECAPITALSUBREPORT", params);

        /*setReportFormat(ReportFormat.PDF);*/
        super.generateReport("balanceSheetReport",
                "/accounting/reports/balanceSheetFinancialReport.jrxml",
                PageFormat.LETTER, PageOrientation.PORTRAIT, messages.get("BalanceSheet.report"), params);

    }

    /**
     * Add evaluation sub report in main report
     *
     * @param subReportKey     key of sub report
     * @param mainReportParams main report params
     */
    private void addCriteriaAssetSubReport(String subReportKey, Map mainReportParams) {
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
                        " WHERE cashAccount.accountType = 'A' " +
                        " AND voucher.state <> 'ANL' " +
                        " AND voucher.date between '"+start+"' and '"+end+"' " +
                        " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        String[] restrictions = new String[]{};
        String orderBy = "cashAccount.accountCode";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/accounting/reports/balanceSheetAssetFinancialReport.jrxml",
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
    private void addCriteriaLiabilitiesSubReport(String subReportKey, Map mainReportParams) {
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
                " WHERE cashAccount.accountType = 'P' " +
                " AND voucher.state <> 'ANL' " +
                " AND voucher.date between '"+start+"' and '"+end+"' " +
                " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        String[] restrictions = new String[]{};
        String orderBy = "cashAccount.accountCode";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/accounting/reports/balanceSheetLiabilitiesFinancialReport.jrxml",
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
    private void addCriteriaCapitalSubReport(String subReportKey, Map mainReportParams) {
        log.debug("Generating addCriteriaAssetSubReport.............................");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql =
                " SELECT " +
                " rootCashAccount.accountCode as accountCode, " +
                " rootCashAccount.description as description, " +
                " SUM(voucherDetail.debit) AS debit, " +
                " SUM(voucherDetail.credit) AS credit" +
                " FROM VoucherDetail voucherDetail " +
                " LEFT  JOIN voucherDetail.voucher voucher " +
                " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                " WHERE cashAccount.accountType = 'C' " +
                " AND voucher.state <> 'ANL' " +
                " AND voucher.date between '"+start+"' and '"+end+"' " +
                " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        String[] restrictions = new String[]{};
        String orderBy = "cashAccount.accountCode";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/accounting/reports/balanceSheetCapitalReport.jrxml",
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
