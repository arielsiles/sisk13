package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.SalaryMovementType;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate salary movement report
 *
 * @author
 * @version $Id: SalaryMovementReportAction.java  24-may-2010 16:15:45$
 */
@Name("salaryMovementReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SALARYMOVEMENTREPORT','VIEW')}")
public class SalaryMovementReportAction extends GenericReportAction {

    private BusinessUnit businessUnit;
    private JobCategory jobCategory;
    private SalaryMovementType salaryMovementType;
    private Date initDate;
    private Date endDate;

    public void generateReport() {
        log.debug("Generate SalaryMovementReportAction......");

        Map params = new HashMap();
        params.putAll(readReportHeaderParamsInfo());

        super.generateReport("salaryMovementReport", "/employees/reports/salaryMovementReport.jrxml", PageFormat.LEGAL, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.salaryMovement.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT DISTINCT " +
                "businessUnit.publicity," +
                "employee.idNumber," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "salaryMovement.date," +
                "salaryMovementType.name," +
                "salaryMovement.amount," +
                "currency.symbol" +
                " FROM SalaryMovement salaryMovement" +
                " LEFT JOIN salaryMovement.employee employee" +
                " LEFT JOIN salaryMovement.salaryMovementType salaryMovementType" +
                " LEFT JOIN salaryMovement.currency currency" +
                " LEFT JOIN employee.contractList contract" +
                " LEFT JOIN contract.jobContractList jobContract" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.jobCategory jobCategory" +
                " LEFT JOIN job.organizationalUnit organizationalUnit" +
                " LEFT JOIN organizationalUnit.businessUnit businessUnit";
    }


    @Create
    public void init() {
        restrictions = new String[]{"salaryMovement.company=#{currentCompany}",
                "businessUnit=#{salaryMovementReportAction.businessUnit}",
                "salaryMovementType=#{salaryMovementReportAction.salaryMovementType}",
                "jobCategory=#{salaryMovementReportAction.jobCategory}",
                "salaryMovement.date >= #{salaryMovementReportAction.initDate}",
                "salaryMovement.date <= #{salaryMovementReportAction.endDate}"};

        sortProperty = "businessUnit.publicity,employee.lastName,employee.maidenName,employee.firstName,salaryMovementType.name";
    }

    /**
     * Read report header fields and define as params
     *
     * @return Map
     */
    private Map readReportHeaderParamsInfo() {
        Map headerParamMap = new HashMap();
        String filtersInfo = "";

        if (initDate != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Common.dateFrom") + " " + DateUtils.format(initDate, MessageUtils.getMessage("patterns.date")) + " ";
        }

        if (endDate != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Common.dateTo") + " " + DateUtils.format(endDate, MessageUtils.getMessage("patterns.date"));
        }

        headerParamMap.put("filterInfoParam", filtersInfo);
        return headerParamMap;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public SalaryMovementType getSalaryMovementType() {
        return salaryMovementType;
    }

    public void setSalaryMovementType(SalaryMovementType salaryMovementType) {
        this.salaryMovementType = salaryMovementType;
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
