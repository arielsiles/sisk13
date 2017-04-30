package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate managers payroll grouped by area
 *
 * @author
 * @version 3.1
 */
@Name("managersPayrollByAreaReportAction")
public class ManagersPayrollByAreaReportAction extends ManagersPayrollReportAction {

    public void generateReport(GeneratedPayroll generatedPayroll) {
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollByAreaReport", "/employees/reports/managersPayrollByAreaReport.jrxml", PageFormat.CUSTOM, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.managersPayroll.fileName"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "employee.idNumber," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "managersPayroll.contractInitDate," +
                "managersPayroll.area," +
                "managersPayroll.unit," +
                "managersPayroll.job," +
                "managersPayroll.workedDays," +
                "managersPayroll.salary," +
                "managersPayroll.totalIncome," +
                "managersPayroll.tardinessMinutesDiscount," +
                "managersPayroll.loanDiscount," +
                "managersPayroll.winDiscount," +
                "managersPayroll.advanceDiscount," +
                "managersPayroll.otherDiscounts," +
                "managersPayroll.afp," +
                "managersPayroll.rciva," +
                "managersPayroll.difference," +
                "managersPayroll.ivaRetention," +
                "managersPayroll.totalDiscount," +
                "managersPayroll.discountsOutOfRetention," +
                "managersPayroll.incomeOutOfIva," +
                "managersPayroll.liquid," +
                "managersPayroll.otherIncomes," +
                "employee.id," +
                "employee.paymentType, " +
                "costCenter.code," +
                "costCenter.description " +
                " FROM ManagersPayroll managersPayroll" +
                " LEFT JOIN managersPayroll.employee employee " +
                " LEFT JOIN managersPayroll.costCenter costCenter";
    }

    @Create
    public void init() {
        restrictions = new String[]{"managersPayroll.company=#{currentCompany}",
                "managersPayroll.generatedPayroll=#{managersPayrollByAreaReportAction.generatedPayroll}"};

        sortProperty = "managersPayroll.area, employee.lastName, employee.maidenName, employee.firstName";
    }

}
