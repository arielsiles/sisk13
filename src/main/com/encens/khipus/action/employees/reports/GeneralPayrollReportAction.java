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
 * Encens S.R.L.
 * Action to generate general payroll report
 *
 * @author
 * @version $Id: GeneralPayrollReportAction.java  28-dic-2009 15:43:02$
 */
@Name("generalPayrollReportAction")
@Scope(ScopeType.PAGE)
public class GeneralPayrollReportAction extends GenericReportAction {

    @In
    PayrollReportService payrollReportService;
    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate GeneralPayrollReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(getPayrollHeaderInfoMap(generatedPayroll.getId()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollReport", "/employees/reports/generalPayrollReport.jrxml", MessageUtils.getMessage("Reports.generalPayroll.fileName"), params);
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
                "generalPayroll.discountsOutOfRetention" +
                " FROM GeneralPayroll generalPayroll" +
                " LEFT JOIN GeneralPayroll.employee employee";
    }

    @Create
    public void init() {
        restrictions = new String[]{"generalPayroll.company=#{currentCompany}",
                "generalPayroll.generatedPayroll=#{generalPayrollReportAction.generatedPayroll}"};

        sortProperty = "employee.lastName";
    }

    /**
     * get info to the report header
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
