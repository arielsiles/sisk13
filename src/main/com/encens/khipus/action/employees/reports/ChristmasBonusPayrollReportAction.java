package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.service.employees.PayrollReportService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate christmas bonus payroll
 *
 * @author
 * @version 3.2
 */
@Name("christmasBonusPayrollReportAction")
@Scope(ScopeType.PAGE)
public class ChristmasBonusPayrollReportAction extends GenericReportAction {

    @In
    PayrollReportService payrollReportService;
    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate ChristmasBonusPayrollReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollReport", "/employees/reports/christmasBonusPayrollReport.jrxml", PageFormat.CUSTOM, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.christmasBonusPayroll.fileName"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "employee.idNumber," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "christmasPayroll.contractInitDate," +
                "christmasPayroll.workedDays," +
                "christmasPayroll.salary," +
                "christmasPayroll.septemberTotalIncome," +
                "christmasPayroll.octoberTotalIncome," +
                "christmasPayroll.novemberTotalIncome," +
                "christmasPayroll.averageSalary," +
                "christmasPayroll.contributableSalary," +
                "christmasPayroll.liquid," +
                "christmasPayroll.bankAccount," +
                "christmasPayroll.bankAccountCurrency," +
                "christmasPayroll.clientCode," +
                "employee.id," +
                "employee.paymentType" +
                " FROM ChristmasPayroll christmasPayroll" +
                " LEFT JOIN christmasPayroll.employee employee";
    }

    @Create
    public void init() {
        restrictions = new String[]{"christmasPayroll.company=#{currentCompany}",
                "christmasPayroll.generatedPayroll=#{christmasBonusPayrollReportAction.generatedPayroll}"};

        sortProperty = "employee.lastName, employee.maidenName, employee.firstName";
    }

    /**
     * get report header info
     *
     * @param generatedPayrollId
     * @return Map
     */
    protected Map<String, Object> getPayrollHeaderInfoMap(Long generatedPayrollId) {
        Map<String, Object> params = new HashMap<String, Object>();

        Map<String, Object> payrollInfoMap = payrollReportService.getGeneratedPayrollInfo(generatedPayrollId);

        //set generated payroll type
        if (payrollInfoMap.get("payrollType") != null) {
            GeneratedPayrollType generatedPayrollType = (GeneratedPayrollType) payrollInfoMap.get("payrollType");
            params.put("payrollType", messages.get(generatedPayrollType.getResourceKey()));
        }

        //set month message
        if (payrollInfoMap.get("month") != null) {
            Month gestionMonth = (Month) payrollInfoMap.get("month");
            params.put("month", messages.get(gestionMonth.getResourceKey()));
        }

        if (payrollInfoMap.get("year") != null) {
            params.put("year", payrollInfoMap.get("year"));
        }

        if (payrollInfoMap.get("generationDate") != null) {
            params.put("generationDate", payrollInfoMap.get("generationDate"));
        }

        return params;
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }
}
