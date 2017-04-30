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
 * Encens S.R.L.
 * Action to execute managers payroll report
 *
 * @author
 * @version $Id: ManagersPayrollReportAction.java  06-ene-2010 16:55:06$
 */
@Name("managersPayrollReportAction")
@Scope(ScopeType.PAGE)
public class ManagersPayrollReportAction extends GenericReportAction {

    @In
    PayrollReportService payrollReportService;
    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate ManagersPayrollReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollReport", "/employees/reports/managersPayrollReport.jrxml", PageFormat.CUSTOM, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.managersPayroll.fileName"), params);
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
                "managersPayroll.totalDiscount," +
                "managersPayroll.discountsOutOfRetention," +
                "managersPayroll.incomeOutOfIva," +
                "managersPayroll.liquid," +
                "managersPayroll.otherIncomes," +
                "employee.id," +
                "employee.paymentType," +
                "managersPayroll.basicIncome," +
                "managersPayroll.absenceMinutesDiscount " +
                " FROM ManagersPayroll managersPayroll" +
                " LEFT JOIN managersPayroll.employee employee";
    }

    @Create
    public void init() {
        restrictions = new String[]{"managersPayroll.company=#{currentCompany}",
                "managersPayroll.generatedPayroll=#{managersPayrollReportAction.generatedPayroll}"};

        sortProperty = "employee.lastName";
    }

    /**
     * get report header info
     *
     * @param generatedPayrollId
     * @return Map
     */
    protected Map<String, Object> getPayrollHeaderInfoMap(Long generatedPayrollId) {
        Map<String, Object> payrollInfoMap = payrollReportService.getGeneratedPayrollInfo(generatedPayrollId);

        //set generated payroll type
        if (payrollInfoMap.get("payrollType") != null) {
            GeneratedPayrollType generatedPayrollType = (GeneratedPayrollType) payrollInfoMap.get("payrollType");
            payrollInfoMap.put("payrollType", messages.get(generatedPayrollType.getResourceKey()));
        }

        //set month message
        if (payrollInfoMap.get("month") != null) {
            Month gestionMonth = (Month) payrollInfoMap.get("month");
            payrollInfoMap.put("month", messages.get(gestionMonth.getResourceKey()));
        }

        return payrollInfoMap;
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }
}
