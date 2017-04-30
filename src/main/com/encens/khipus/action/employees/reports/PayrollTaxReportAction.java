package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.ConfigurationTaxPayroll;
import com.encens.khipus.model.employees.TaxPayrollGenerated;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate tributary payroll report
 *
 * @author
 * @version $Id: PayrollTaxReportAction.java  03-dic-2010 17:01:39$
 */
@Name("payrollTaxReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('APPROVETAXPAYROLL','VIEW')}")
public class PayrollTaxReportAction extends GenericReportAction {

    @In
    private SessionUser sessionUser;
    @In
    private GenericService genericService;

    private TaxPayrollGenerated taxPayrollGenerated;

    public void generateReport(TaxPayrollGenerated taxPayrollGenerated) {
        log.debug("Generate PayrollTaxReportAction......" + taxPayrollGenerated);
        try {
            taxPayrollGenerated = genericService.findById(TaxPayrollGenerated.class, taxPayrollGenerated.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Not found taxPayrollGenerated....", e);
        }

        //set filter properties
        setTaxPayrollGenerated(taxPayrollGenerated);

        //params
        Map params = new HashMap();
        params.putAll(getPayrollHeaderInfoMap(taxPayrollGenerated.getConfigurationTaxPayroll()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollReport", "/employees/reports/payrollTaxReport.jrxml", MessageUtils.getMessage("Reports.payrollTax.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "tributaryPayroll.number," +
                "tributaryPayroll.code," +
                "tributaryPayroll.name," +
                "tributaryPayroll.totalGrained," +
                "tributaryPayroll.retentionAFP," +
                "tributaryPayroll.netSalary," +
                "tributaryPayroll.salaryNotTaxableTwoSMN," +
                "tributaryPayroll.unlikeTaxable," +
                "tributaryPayroll.tax," +
                "tributaryPayroll.fiscalCredit," +
                "tributaryPayroll.taxForTwoSMN," +
                "tributaryPayroll.physicalBalance," +
                "tributaryPayroll.dependentBalance," +
                "tributaryPayroll.lastMonthBalance," +
                "tributaryPayroll.maintenanceOfValue," +
                "tributaryPayroll.lastBalanceUpdated," +
                "tributaryPayroll.dependentTotalBalance," +
                "tributaryPayroll.usedBalance," +
                "tributaryPayroll.retentionClearance," +
                "tributaryPayroll.dependentBalanceToNextMonth" +
                " FROM TributaryPayroll tributaryPayroll";
    }

    @Create
    public void init() {
        restrictions = new String[]{"tributaryPayroll.company=#{currentCompany}",
                "tributaryPayroll.taxPayrollGenerated = #{payrollTaxReportAction.taxPayrollGenerated}"};

        sortProperty = "tributaryPayroll.number";
    }

    /**
     * get report header info
     *
     * @param configurationTaxPayroll
     * @return Map
     */
    protected Map<String, Object> getPayrollHeaderInfoMap(ConfigurationTaxPayroll configurationTaxPayroll) {
        Map<String, Object> payrollInfoMap = new HashMap<String, Object>();

        if (configurationTaxPayroll != null) {
            String dateRange = MessageUtils.getMessage("Common.dateFrom") + " " + DateUtils.format(configurationTaxPayroll.getStartDate(), MessageUtils.getMessage("patterns.date")) +
                    " " + MessageUtils.getMessage("Common.dateTo") + " " + DateUtils.format(configurationTaxPayroll.getEndDate(), MessageUtils.getMessage("patterns.date"));

            String ufvRange = FormatUtils.formatNumber(configurationTaxPayroll.getInitialUfvExchangeRate(), MessageUtils.getMessage("patterns.decimal11FNumber"), sessionUser.getLocale()) +
                    " - " + FormatUtils.formatNumber(configurationTaxPayroll.getFinalUfvExchangeRate(), MessageUtils.getMessage("patterns.decimal11FNumber"), sessionUser.getLocale());

            String nationalMinimunSalary = FormatUtils.formatNumber(configurationTaxPayroll.getSmnRate().getRate(), MessageUtils.getMessage("patterns.decimalNumber"), sessionUser.getLocale());

            payrollInfoMap.put("dateRangeParam", dateRange);
            payrollInfoMap.put("ufvRangeParam", ufvRange);
            payrollInfoMap.put("smnParam", nationalMinimunSalary);
        }
        return payrollInfoMap;
    }

    public TaxPayrollGenerated getTaxPayrollGenerated() {
        return taxPayrollGenerated;
    }

    public void setTaxPayrollGenerated(TaxPayrollGenerated taxPayrollGenerated) {
        this.taxPayrollGenerated = taxPayrollGenerated;
    }
}
