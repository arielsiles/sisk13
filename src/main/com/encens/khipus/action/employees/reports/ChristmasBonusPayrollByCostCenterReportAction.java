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
 * Action to generate christmas bonus payroll grouped by cost center
 *
 * @author
 * @version 3.2
 */
@Name("christmasBonusPayrollByCostCenterReportAction")
@Scope(ScopeType.PAGE)
public class ChristmasBonusPayrollByCostCenterReportAction extends ChristmasBonusPayrollReportAction {

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate ChristmasBonusPayrollByCostCenterReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollByCostCenterReport", "/employees/reports/christmasBonusPayrollByCostCenterReport.jrxml", PageFormat.CUSTOM, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.managersPayroll.fileName"), params);
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
                "costCenter.code," +
                "costCenter.description" +
                " FROM ChristmasPayroll christmasPayroll" +
                " LEFT JOIN christmasPayroll.employee employee" +
                " LEFT JOIN christmasPayroll.costCenter costCenter";
    }

    @Create
    public void init() {
        restrictions = new String[]{"christmasPayroll.company=#{currentCompany}",
                "christmasPayroll.generatedPayroll=#{christmasBonusPayrollByCostCenterReportAction.generatedPayroll}"};

        sortProperty = "costCenter.code, employee.lastName, employee.maidenName, employee.firstName";
    }
}
