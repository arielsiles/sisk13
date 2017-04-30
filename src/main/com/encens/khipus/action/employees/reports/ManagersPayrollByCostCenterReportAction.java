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
 * Encens S.R.L.
 * Action to generate managers payroll grouped by cost center
 *
 * @author
 * @version $Id: ManagersPayrollByCostCenterReportAction.java  02-dic-2010 14:27:29$
 */
@Name("managersPayrollByCostCenterReportAction")
@Scope(ScopeType.PAGE)
public class ManagersPayrollByCostCenterReportAction extends ManagersPayrollReportAction {

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate managersPayrollByCostCenterReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollByCostCenterReport", "/employees/reports/managersPayrollByCostCenterReport.jrxml", PageFormat.CUSTOM, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.managersPayroll.fileName"), params);
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
                "employee.paymentType," +
                "costCenter.code," +
                "costCenter.description" +
                " FROM ManagersPayroll managersPayroll" +
                " LEFT JOIN managersPayroll.employee employee" +
                " LEFT JOIN managersPayroll.costCenter costCenter";
    }

    @Create
    public void init() {
        restrictions = new String[]{"managersPayroll.company=#{currentCompany}",
                "managersPayroll.generatedPayroll=#{managersPayrollByCostCenterReportAction.generatedPayroll}"};

        sortProperty = "costCenter.code, employee.lastName, employee.maidenName, employee.firstName";
    }
}
