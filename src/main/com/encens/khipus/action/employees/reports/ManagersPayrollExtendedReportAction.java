package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.GeneratedPayroll;
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
 * Action to gererate managers payroll extended report
 *
 * @author
 * @version $Id: ManagersPayrollExtendedReportAction.java  03-mar-2010 12:28:58$
 */
@Name("managersPayrollExtendedReportAction")
@Scope(ScopeType.PAGE)
public class ManagersPayrollExtendedReportAction extends GenericReportAction {

    @In
    PayrollReportService payrollReportService;
    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate ManagersPayrollExtendedReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollExtendedReport", "/employees/reports/managersPayrollExtendedReport.jrxml", MessageUtils.getMessage("Reports.extendedManagersPayroll.fileName"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "managersPayroll.employee.idNumber," +
                "managersPayroll.employee.lastName," +
                "managersPayroll.employee.maidenName," +
                "managersPayroll.employee.firstName," +
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
                "managersPayroll.contractMode," +
                "managersPayroll.generatedPayroll.gestionPayroll.businessUnit.publicity," +
                "managersPayroll.employee.id," +
                "managersPayroll.employee.paymentType" +
                " FROM ManagersPayroll managersPayroll";
    }

    @Create
    public void init() {
        restrictions = new String[]{"managersPayroll.company=#{currentCompany}",
                "managersPayroll.generatedPayroll=#{managersPayrollExtendedReportAction.generatedPayroll}"};

        sortProperty = "managersPayroll.employee.lastName";
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }
}
