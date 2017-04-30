package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the 101 form record report action
 *
 * @author
 * @version 2.26
 */
@Name("formRecordReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FORMRECORDREPORT','VIEW')}")
public class FormRecordReportAction extends GenericReportAction {
    private String executorUnitCode;
    private CostCenter costCenter;
    private Employee employee;
    private Date initDate;
    private Date endDate;

    @Create
    public void init() {
        restrictions = new String[]{
                "employee=#{formRecordReportAction.employee}",
                "costCenter = #{formRecordReportAction.costCenter}",
                "lower(businessUnit.executorUnitCode) like concat(lower(#{formRecordReportAction.executorUnitCode}),'%')",
                "invoicesForm.presentationDate>=#{formRecordReportAction.initDate}",
                "invoicesForm.presentationDate<=#{formRecordReportAction.endDate}"};

        sortProperty = "employee.lastName, employee.maidenName, employee.firstName, employee.id";
    }

    protected String getEjbql() {
        return "SELECT " +
                "    invoicesForm.presentationDate, " +
                "    employee, " +
                "    invoicesForm.fiscalCredit " +
                "FROM InvoicesForm invoicesForm " +
                "     LEFT JOIN invoicesForm.jobContract jobContract " +
                "    LEFT JOIN jobContract.job job " +
                "    LEFT JOIN job.organizationalUnit organizationalUnit " +
                "    LEFT JOIN jobContract.contract contract " +
                "    LEFT JOIN contract.employee employee " +
                "    LEFT JOIN organizationalUnit.businessUnit businessUnit " +
                "    LEFT JOIN organizationalUnit.costCenter costCenter ";
    }

    public void generateReport() {
        log.debug("generating 101FormRecordReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "101FormRecordReport",
                "/employees/reports/formRecordReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("taxPayrollGeneration.reports.101FormRecordReport"),
                reportParameters);
    }

    public void assignCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String clearEmployee() {
        setEmployee(null);
        return null;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
