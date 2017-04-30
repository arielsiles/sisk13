package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate global tributary payroll report
 *
 * @author
 * @version 3.4
 */
@Name("tributaryPayrollReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','VIEW')}")
public class TributaryPayrollReportAction extends GenericReportAction {

    @In
    private SessionUser sessionUser;

    private PayrollGenerationCycle payrollGenerationCycle;

    public void generateReport(PayrollGenerationCycle payrollGenerationCycle) {
        log.debug("Generate TributaryPayrollReportAction......" + payrollGenerationCycle);

        //set filter properties
        setPayrollGenerationCycle(payrollGenerationCycle);

        //params
        Map params = new HashMap();
        String title = MessageUtils.getMessage("Reports.tributaryPayroll.title", MessageUtils.getMessage(payrollGenerationCycle.getMonth().getResourceKey()), payrollGenerationCycle.getGestion().getYear());

        params.put("tributaryPayrollTitleParam", title);
        params.putAll(getReportParamsInfo(payrollGenerationCycle));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollReport", "/employees/reports/tributaryPayrollReport.jrxml", title, params);
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
                " FROM TributaryPayroll tributaryPayroll " +
                " LEFT JOIN tributaryPayroll.payrollGenerationCycle payrollGenerationCycle";
    }

    @Create
    public void init() {
        restrictions = new String[]{"tributaryPayroll.company=#{currentCompany}",
                "payrollGenerationCycle = #{tributaryPayrollReportAction.payrollGenerationCycle}"};

        sortProperty = "tributaryPayroll.number";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map getReportParamsInfo(PayrollGenerationCycle payrollGenerationCycle) {
        Map paramMap = new HashMap();

        String month = MessageUtils.getMessage(payrollGenerationCycle.getMonth().getResourceKey());
        String smn = MessageUtils.getMessage("Reports.tributaryPayroll.smn")
                + "  " + formatSMNParam(payrollGenerationCycle.getSmnRate().getRate(), 1)
                + "  " + formatSMNParam(payrollGenerationCycle.getSmnRate().getRate(), 2)
                + "  " + formatSMNParam(payrollGenerationCycle.getSmnRate().getRate(), 4);

        String initialUfv = formatUfvParam(payrollGenerationCycle.getInitialUfvExchangeRate(), payrollGenerationCycle.getStartDate());
        String finalUfv = formatUfvParam(payrollGenerationCycle.getFinalUfvExchangeRate(), payrollGenerationCycle.getEndDate());
        String nit = getOrganizationNIT(payrollGenerationCycle.getBusinessUnit());

        paramMap.put("monthParam", month);
        paramMap.put("smnParam", smn);
        paramMap.put("initialUfvParam", initialUfv);
        paramMap.put("finalUfvParam", finalUfv);
        paramMap.put("nitParam", nit);
        paramMap.putAll(getColumnHeaderComposedLabelParam(payrollGenerationCycle));

        return paramMap;
    }

    private Map getColumnHeaderComposedLabelParam(PayrollGenerationCycle payrollGenerationCycle) {
        Map paramMap = new HashMap();
        paramMap.put("taxColumnParam", MessageUtils.getMessage("TributaryPayroll.tax", payrollGenerationCycle.getIvaRate().getRate()));
        paramMap.put("taxForTwoSMNColumnParam", MessageUtils.getMessage("TributaryPayroll.taxForTwoSMN", payrollGenerationCycle.getIvaRate().getRate()));
        return paramMap;
    }

    private String getOrganizationNIT(BusinessUnit businessUnit) {
        String organizationNIT = businessUnit.getOrganization().getIdNumber();
        return (organizationNIT != null) ? organizationNIT : "";
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }

    private String formatSMNParam(BigDecimal smn, int quantity) {
        return FormatUtils.formatNumber(BigDecimalUtil.multiply(smn, BigDecimal.valueOf(quantity)), MessageUtils.getMessage("patterns.decimalNumber"), sessionUser.getLocale());
    }

    private String formatUfvParam(BigDecimal ufv, Date date) {
        return DateUtils.format(date, MessageUtils.getMessage("patterns.date"))
                + "  " + FormatUtils.formatNumber(ufv, MessageUtils.getMessage("patterns.decimal6FNumber"), sessionUser.getLocale());
    }

}
