package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.service.finances.CashAvailableReportService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate cash available report
 *
 * @author
 * @version $Id: CashAvailableReportAction.java  19-nov-2010 19:16:19$
 */
@Name("cashAvailableReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTCASHAVAILABLE','VIEW')}")
public class CashAvailableReportAction extends GenericReportAction {

    @In
    private CashAvailableReportService cashAvailableReportService;
    private Date currentDate;

    public void generateReport() {
        log.debug("Generate CashAvailableReportAction........");

        Map params = new HashMap();
        params.putAll(getReportParams());

        //add sub reports
        addBankAccountCrossTabSubReport(params);
        addExecutorUnitDepositSubReport(params);
        addExecutorUnitCheckReceivableSubReport(params);
        addExecutorUnitCollectionsSubReport(params);

        super.generateReport("cashAvailableReport", "/finances/reports/cashAvailableReport.jrxml", MessageUtils.getMessage("Reports.cashAvailable.title"), params);
    }

    @Override
    protected String getEjbql() {
        return getCrossTabSql();
    }

    @Create
    public void init() {
        restrictions = getCrossTabRestrictions();
        sortProperty = getCrossTabOrder();

        //initialize current date
        currentDate = new Date();
    }

    /**
     * SQL to get all banks with at least one bank account
     *
     * @return String
     */
    private String getCrossTabSql() {
        return "SELECT " +
                "financesBank.id," +
                "financesBank.name," +
                "financesBankAccount.companyNumber," +
                "financesBankAccount.accountNumber," +
                "financesBankAccount.currency" +
                " FROM FinancesBank financesBank" +
                " LEFT JOIN financesBank.financesBankAccountList financesBankAccount" +
                " LEFT JOIN financesBankAccount.cashAccount cashAccount" +
                " LEFT JOIN cashAccount.accountingMovementDetailList accountingMovementDetail" +
                " WHERE financesBankAccount.accountNumber IS NOT NULL";
    }

    private String[] getCrossTabRestrictions() {
        return new String[]{};
    }

    private String getCrossTabOrder() {
        return "financesBank.name";
    }

    /**
     * Report parameters, is there calculate and set all crosstab info as Map parameters
     *
     * @return Map
     */
    private Map getReportParams() {
        Map paramsMap = new HashMap();

        String dateRangeInfo = "";
        if (currentDate != null) {
            dateRangeInfo = MessageUtils.getMessage("Common.dateTo") + " " + DateUtils.format(currentDate, MessageUtils.getMessage("patterns.date"));
        }

        paramsMap.put("dateRangeParam", dateRangeInfo);
        paramsMap.put("crosstabInfoMapParam", cashAvailableReportService.calculateCashAvailableCrossTabInfoData(getCurrentDate()));

        log.debug("Report params:" + paramsMap);
        return paramsMap;
    }

    /**
     * Add bank account crosstab sub report
     *
     * @param mainReportParams
     */
    private void addBankAccountCrossTabSubReport(Map mainReportParams) {
        log.debug("Generating addBankAccountCrossTabSubReport.............................");
        String subReportKey = "BANKACCOUNTCROSSTABSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = getCrossTabSql();
        String[] restrictions = getCrossTabRestrictions();
        String orderBy = getCrossTabOrder();

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/finances/reports/cashAvailableBankAccountCrossTabSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Add executor unit deposits sub report in main report
     *
     * @param mainReportParams
     */
    private void addExecutorUnitDepositSubReport(Map mainReportParams) {
        log.debug("Generating addExecutorUnitSubReport.............................");
        String subReportKey = "EXECUTORUNITDEPOSITSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();
        //add sub reports
        addBankAccountCrossTabSubReport(params);

        String ejbql = "SELECT " +
                "executorUnit.id," +
                "executorUnit.description" +
                " FROM ExecutorUnit executorUnit";

        String[] restrictions = new String[]{};

        String orderBy = "executorUnit.description";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/finances/reports/executorUnitDepositSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Add executor unit checks receivable sub report in main report
     *
     * @param mainReportParams
     */
    private void addExecutorUnitCheckReceivableSubReport(Map mainReportParams) {
        log.debug("Generating addExecutorUnitCheckReceivableSubReport.............................");
        String subReportKey = "EXECUTORUNITCHECKRECEIVABLESUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();
        //add sub reports
        addBankAccountCrossTabSubReport(params);

        String ejbql = "SELECT " +
                "financesExecutorUnit.executorUnitCode," +
                "financesExecutorUnit.description" +
                " FROM FinancesExecutorUnit financesExecutorUnit";

        String[] restrictions = new String[]{};

        String orderBy = "financesExecutorUnit.description";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/finances/reports/executorUnitCheckReceivableSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Add executor unit collections sub report in main report
     *
     * @param mainReportParams
     */
    private void addExecutorUnitCollectionsSubReport(Map mainReportParams) {
        log.debug("Generating addExecutorUnitCollectionsSubReport.............................");
        String subReportKey = "EXECUTORUNITCOLLECTIONSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "executorUnit.id," +
                "executorUnit.description" +
                " FROM ExecutorUnit executorUnit";

        String[] restrictions = new String[]{};

        String orderBy = "executorUnit.description";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/finances/reports/executorUnitCollectionSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }
}
