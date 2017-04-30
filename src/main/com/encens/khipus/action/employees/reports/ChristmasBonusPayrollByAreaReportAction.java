package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate christmas bonus payroll grouped by area
 *
 * @author
 * @version 3.2
 */
@Name("christmasBonusPayrollByAreaReportAction")
@Scope(ScopeType.PAGE)
public class ChristmasBonusPayrollByAreaReportAction extends ChristmasBonusPayrollReportAction {

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate ChristmasBonusPayrollByAreaReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollByAreaReport", "/employees/reports/christmasBonusPayrollByAreaReport.jrxml", PageFormat.CUSTOM, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.managersPayroll.fileName"), params);
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
                "employee.paymentType," +
                "christmasPayroll.area" +
                " FROM ChristmasPayroll christmasPayroll" +
                " LEFT JOIN christmasPayroll.employee employee";

    }

    @Create
    public void init() {
        restrictions = new String[]{"christmasPayroll.company=#{currentCompany}",
                "christmasPayroll.generatedPayroll=#{christmasBonusPayrollByAreaReportAction.generatedPayroll}"};

        sortProperty = "christmasPayroll.area, employee.lastName, employee.maidenName, employee.firstName";
    }

}
