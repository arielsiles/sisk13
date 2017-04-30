package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.cashbox.IncomeByInvoiceExtendedAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate income by invoice report
 *
 * @author
 * @version $Id: IncomeByInvoiceReportAction.java  02-ago-2010 19:20:02$
 */
@Name("incomeByInvoiceReportAction")
@Restrict("#{s:hasPermission('REPORTINCOMEBYINVOICE','VIEW')}")
public class IncomeByInvoiceReportAction extends GenericReportAction {

    @In
    private IncomeByInvoiceExtendedAction incomeByInvoiceExtendedAction;

    public void generateReport() {
        log.debug("Generating IncomeByInvoiceReportAction............................");

        Map params = new HashMap();

        //add sub reports
        addIncomeByInvoiceByConceptSubReport(params);
        addIncomeByInvoiceByBranchSubReport(params);
        addIncomeByInvoiceByCategorySubReport(params);

        super.generateSqlReport("incomeByInvoiceReport", "/cashbox/reports/incomeByInvoiceReport.jrxml", MessageUtils.getMessage("Reports.incomeByInvoice.title"), params);
    }

    @Override
    protected String getNativeSql() {
        return null;
    }

    /**
     * income invoice by concept sub report
     *
     * @param mainReportParams
     */
    private void addIncomeByInvoiceByConceptSubReport(Map mainReportParams) {
        log.debug("Generating addIncomeByInvoiceByConceptSubReport.............................");
        String subReportKey = "INCOMEBYCONCEPTSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String nativeSql = incomeByInvoiceExtendedAction.getIncomeByInvoiceByConceptAction().getSql();

        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/cashbox/reports/incomeByInvoiceByConceptSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                nativeSql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * income invoice by branch sub report
     *
     * @param mainReportParams
     */
    private void addIncomeByInvoiceByBranchSubReport(Map mainReportParams) {
        log.debug("Generating addIncomeByInvoiceByBranchSubReport.............................");
        String subReportKey = "INCOMEBYBRANCHSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String nativeSql = incomeByInvoiceExtendedAction.getIncomeByInvoiceByBranchAction().getSql();

        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/cashbox/reports/incomeByInvoiceByBranchSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                nativeSql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }



    /**
     * income invoice by category sub report
     *
     * @param mainReportParams
     */
    private void addIncomeByInvoiceByCategorySubReport(Map mainReportParams) {
        log.debug("Generating addIncomeByInvoiceByCategorySubReport.............................");
        String subReportKey = "INCOMEBYCATEGORYSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String nativeSql = incomeByInvoiceExtendedAction.getIncomeByInvoiceByCategoryAction().getSql();

        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/cashbox/reports/incomeByInvoiceByCategorySubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                nativeSql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }
}
