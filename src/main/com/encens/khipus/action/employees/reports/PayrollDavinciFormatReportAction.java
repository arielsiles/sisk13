package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate payroll in DAVINCI format
 *
 * @author
 * @version 3.4
 */
@Name("payrollDavinciFormatReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','VIEW')}")
public class PayrollDavinciFormatReportAction extends GenericReportAction {

    private PayrollGenerationCycle payrollGenerationCycle;

    public void generateReport(PayrollGenerationCycle payrollGenerationCycle) {
        log.debug("Generate PayrollDavinciFormatReportAction......" + payrollGenerationCycle);

        //set filter properties
        setPayrollGenerationCycle(payrollGenerationCycle);

        //params
        Map params = new HashMap();
        String title = MessageUtils.getMessage("Reports.payrollDavinciFormat.title", MessageUtils.getMessage(payrollGenerationCycle.getMonth().getResourceKey()), payrollGenerationCycle.getGestion().getYear());

        params.putAll(getReportParamsInfo(payrollGenerationCycle));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollDavinciReport", "/employees/reports/payrollDavinciFormatReport.jrxml", title, params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "tributaryPayroll.number," +
                "employee.idNumber," +
                "tributaryPayroll.netSalary," +
                "tributaryPayroll.fiscalCredit," +
                "tributaryPayroll.retentionClearance," +
                "tributaryPayroll.dependentBalanceToNextMonth," +
                "tributaryPayroll.name" +
                " FROM TributaryPayroll tributaryPayroll " +
                " LEFT JOIN tributaryPayroll.payrollGenerationCycle payrollGenerationCycle" +
                " LEFT JOIN tributaryPayroll.employee employee";
    }

    @Create
    public void init() {
        restrictions = new String[]{"tributaryPayroll.company=#{currentCompany}",
                "payrollGenerationCycle = #{payrollDavinciFormatReportAction.payrollGenerationCycle}"};

        sortProperty = "tributaryPayroll.number";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map getReportParamsInfo(PayrollGenerationCycle payrollGenerationCycle) {
        Map paramMap = new HashMap();

        String nit = getOrganizationNIT(payrollGenerationCycle.getBusinessUnit());
        String month = String.valueOf(payrollGenerationCycle.getMonth().getValueAsPosition());
        String year = String.valueOf(payrollGenerationCycle.getGestion().getYear());

        paramMap.put("nitParam", nit);
        paramMap.put("monthParam", month);
        paramMap.put("yearParam", year);

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
}
