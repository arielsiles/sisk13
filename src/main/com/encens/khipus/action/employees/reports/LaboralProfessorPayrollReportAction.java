package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
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
 * Action to to execute laboral professor payroll report
 *
 * @author
 * @version 3.4
 */
@Name("laboralProfessorPayrollReportAction")
@Scope(ScopeType.PAGE)
public class LaboralProfessorPayrollReportAction extends GenericReportAction {

    @In
    PayrollReportService payrollReportService;
    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate LaboralProfessorPayrollReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("laboralPayrollReport", "/employees/reports/laboralProfessorPayrollReport.jrxml", MessageUtils.getMessage("Reports.laboralProfessorPayroll.fileName"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "employee.id," +
                "employee.idNumber," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "fiscalProfessorPayroll.contractInitDate," +
                "fiscalProfessorPayroll.area," +
                "fiscalProfessorPayroll.unit," +
                "fiscalProfessorPayroll.job," +
                "fiscalProfessorPayroll.workedDays," +
                "fiscalProfessorPayroll.basicSalary," +
                "fiscalProfessorPayroll.basicIncome," +
                "fiscalProfessorPayroll.otherIncomes," +
                "fiscalProfessorPayroll.totalIncome," +
                "fiscalProfessorPayroll.tardinessMinutes," +
                "fiscalProfessorPayroll.tardinessMinutesDiscount," +
                "fiscalProfessorPayroll.absenceMinutesDiscount," +
                "fiscalProfessorPayroll.loanDiscount," +
                "fiscalProfessorPayroll.winDiscount," +
                "fiscalProfessorPayroll.advanceDiscount," +
                "fiscalProfessorPayroll.otherDiscounts," +
                "fiscalProfessorPayroll.afp," +
                "fiscalProfessorPayroll.rciva," +
                "fiscalProfessorPayroll.totalDiscount," +
                "fiscalProfessorPayroll.liquid," +
                "fiscalProfessorPayroll.paymentType," +
                "fiscalProfessorPayroll.clientCod," +
                "fiscalProfessorPayroll.accountNumber," +
                "currency.symbol" +
                " FROM FiscalProfessorPayroll fiscalProfessorPayroll" +
                " LEFT JOIN fiscalProfessorPayroll.employee employee" +
                " LEFT JOIN fiscalProfessorPayroll.currency currency";
    }

    @Create
    public void init() {
        restrictions = new String[]{"fiscalProfessorPayroll.company=#{currentCompany}",
                "fiscalProfessorPayroll.generatedPayroll = #{laboralProfessorPayrollReportAction.generatedPayroll}"};

        sortProperty = "employee.lastName, employee.maidenName, employee.firstName";
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
