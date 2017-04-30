package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;
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
 * Action to generate job contracts employee reports
 *
 * @author
 * @version $Id: JobContractReportAction.java  13-abr-2010 18:50:57$
 */
@Name("jobContractReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('JOBCONTRACTREPORT','VIEW')}")
public class JobContractReportAction extends GenericReportAction {

    private Sector sector;
    private BusinessUnit businessUnit;
    private JobCategory jobCategory;
    private Date initDate;
    private Date endDate;

    public void generateReport() {
        log.debug("Generate JobContractReportAction......");

        Map params = new HashMap();
        params.putAll(readReportHeaderParamsInfo());

        super.generateReport("jobContracReport", "/employees/reports/jobContractReport.jrxml", MessageUtils.getMessage("Reports.jobContract.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "jobContract.job.organizationalUnit.businessUnit.publicity," +
                "jobContract.job.jobCategory.name," +
                "contract.contractMode.name," +
                "jobContract.job.organizationalUnit.name," +
                "employee.idNumber," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "jobContract.job.salary.amount," +
                "jobContract.job.salary.currency.symbol," +
                "employee.retentionFlag," +
                "employee.controlFlag," +
                "contract.initDate," +
                "contract.endDate," +
                "contract.activeForPayrollGeneration," +
                "employee.paymentType," +
                "bankAccount.clientCod," +
                "bankAccount.accountNumber" +
                " FROM JobContract jobContract" +
                //employee
                " LEFT JOIN jobContract.contract contract" +
                " LEFT JOIN contract.employee employee" +
                " LEFT JOIN employee.bankAccountList bankAccount";

    }

    @Create
    public void init() {
        restrictions = new String[]{"jobContract.company=#{currentCompany}",
                "jobContract.job.organizationalUnit.businessUnit=#{jobContractReportAction.businessUnit}",
                "jobContract.job.jobCategory.sector=#{jobContractReportAction.sector}",
                "jobContract.job.jobCategory=#{jobContractReportAction.jobCategory}",
                "contract.initDate >= #{jobContractReportAction.initDate}",
                "contract.endDate <= #{jobContractReportAction.endDate}"};

        sortProperty = "employee.lastName,employee.maidenName,employee.firstName";
    }

    /**
     * Read report header fields an define as params
     *
     * @return Map
     */
    private Map readReportHeaderParamsInfo() {
        Map headerParamMap = new HashMap();
        String filtersInfo = "";

        if (businessUnit != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("JobContract.businessUnit") + ": " + businessUnit.getPublicity() + "\n";
        }

        if (sector != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Job.sector") + ": " + sector.getName() + "\n";
        }

        if (initDate != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Contract.initDate") + ": " + DateUtils.format(initDate, MessageUtils.getMessage("patterns.date")) + "\n";
        }

        if (endDate != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Contract.endDate") + ": " + DateUtils.format(endDate, MessageUtils.getMessage("patterns.date")) + "\n";
        }

        headerParamMap.put("filterInfoParam", filtersInfo);
        return headerParamMap;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
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
