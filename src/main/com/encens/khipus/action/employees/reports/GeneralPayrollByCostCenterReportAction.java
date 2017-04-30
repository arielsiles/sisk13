package com.encens.khipus.action.employees.reports;

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
 * Action to generate teacher payroll grouped by cost center
 *
 * @author
 * @version $Id: GeneralPayrollByCostCenterReportAction.java  02-dic-2010 17:06:41$
 */
@Name("generalPayrollByCostCenterReportAction")
@Scope(ScopeType.PAGE)
public class GeneralPayrollByCostCenterReportAction extends GeneralPayrollReportAction {

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate GeneralPayrollByCostCenterReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollByCostCenterReport", "/employees/reports/generalPayrollByCostCenterReport.jrxml", MessageUtils.getMessage("Reports.generalPayroll.fileName"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "employee.idNumber," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "generalPayroll.salary," +
                "generalPayroll.totalperiodworked," +
                "generalPayroll.absencediscount," +
                "generalPayroll.tardiness," +
                "generalPayroll.otherDiscounts," +
                "generalPayroll.difference," +
                "generalPayroll.ivaRetention," +
                "generalPayroll.incomeOutOfIva," +
                "generalPayroll.liquid," +
                "generalPayroll.totalDiscount," +
                "generalPayroll.absenceminut," +
                "employee.id," +
                "employee.paymentType," +
                "generalPayroll.otherIncomes," +
                "generalPayroll.discountsOutOfRetention," +
                "costCenter.code," +
                "costCenter.description" +
                " FROM GeneralPayroll generalPayroll" +
                " LEFT JOIN GeneralPayroll.employee employee" +
                " LEFT JOIN GeneralPayroll.costCenter costCenter";
    }

    @Create
    public void init() {
        restrictions = new String[]{"generalPayroll.company=#{currentCompany}",
                "generalPayroll.generatedPayroll=#{generalPayrollByCostCenterReportAction.generatedPayroll}"};

        sortProperty = "costCenter.code, employee.lastName, employee.maidenName, employee.firstName";
    }
}
