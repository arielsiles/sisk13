package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.GeneratedPayroll;
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
 * Action to generate category tributary payroll report
 *
 * @author
 * @version 3.4
 */
@Name("categoryTributaryPayrollReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GENERATEDPAYROLL','VIEW')}")
public class CategoryTributaryPayrollReportAction extends GenericReportAction {

    @In
    private SessionUser sessionUser;
    @In
    private GenericService genericService;

    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate CategoryTributaryPayrollReportAction......" + generatedPayroll);
        try {
            generatedPayroll = genericService.findById(GeneratedPayroll.class, generatedPayroll.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Not found GeneratedPayroll....", e);
        }

        PayrollGenerationCycle payrollGenerationCycle = generatedPayroll.getPayrollGenerationCycle();

        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        //params
        Map params = new HashMap();
        String title = MessageUtils.getMessage("Reports.categoryTributaryPayroll.title", MessageUtils.getMessage(payrollGenerationCycle.getMonth().getResourceKey()), payrollGenerationCycle.getGestion().getYear());

        params.put("tributaryPayrollTitleParam", title);
        params.putAll(getReportParamsInfo(payrollGenerationCycle));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollReport", "/employees/reports/categoryTributaryPayrollReport.jrxml", title, params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "categoryTributaryPayroll.number," +
                "categoryTributaryPayroll.code," +
                "categoryTributaryPayroll.name," +
                "categoryTributaryPayroll.totalGrained," +
                "categoryTributaryPayroll.retentionAFP," +
                "categoryTributaryPayroll.netSalary," +
                "categoryTributaryPayroll.salaryNotTaxableTwoSMN," +
                "categoryTributaryPayroll.unlikeTaxable," +
                "categoryTributaryPayroll.tax," +
                "categoryTributaryPayroll.fiscalCredit," +
                "categoryTributaryPayroll.taxForTwoSMN," +
                "categoryTributaryPayroll.physicalBalance," +
                "categoryTributaryPayroll.dependentBalance," +
                "categoryTributaryPayroll.lastMonthBalance," +
                "categoryTributaryPayroll.maintenanceOfValue," +
                "categoryTributaryPayroll.lastBalanceUpdated," +
                "categoryTributaryPayroll.dependentTotalBalance," +
                "categoryTributaryPayroll.usedBalance," +
                "categoryTributaryPayroll.retentionClearance," +
                "categoryTributaryPayroll.dependentBalanceToNextMonth" +
                " FROM CategoryTributaryPayroll categoryTributaryPayroll " +
                " LEFT JOIN categoryTributaryPayroll.generatedPayroll generatedPayroll ";
    }

    @Create
    public void init() {
        restrictions = new String[]{"categoryTributaryPayroll.company = #{currentCompany}",
                "generatedPayroll = #{categoryTributaryPayrollReportAction.generatedPayroll}"};

        sortProperty = "categoryTributaryPayroll.number";
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

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    private String formatSMNParam(BigDecimal smn, int quantity) {
        return FormatUtils.formatNumber(BigDecimalUtil.multiply(smn, BigDecimal.valueOf(quantity)), MessageUtils.getMessage("patterns.decimalNumber"), sessionUser.getLocale());
    }

    private String formatUfvParam(BigDecimal ufv, Date date) {
        return DateUtils.format(date, MessageUtils.getMessage("patterns.date"))
                + "  " + FormatUtils.formatNumber(ufv, MessageUtils.getMessage("patterns.decimal6FNumber"), sessionUser.getLocale());
    }

}
