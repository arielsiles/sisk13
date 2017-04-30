package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.employees.SpecialDateTarget;
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
 * Action to generate special date report
 *
 * @author
 * @version $Id: SpecialDateReportAction.java  08-abr-2010 18:46:00$
 */
@Name("specialDateReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SPECIALDATEREPORT','VIEW')}")
public class SpecialDateReportAction extends GenericReportAction {

    private Sector sector;
    private BusinessUnit businessUnit;
    private JobCategory jobCategory;
    private SpecialDateTarget specialDateTarget;
    private Date initDate;
    private Date endDate;

    public void generateReport() {
        log.debug("Generate SpecialDateReportAction......");

        Map params = new HashMap();
        params.putAll(readReportHeaderParamsInfo());

        super.generateReport("specialDateReport", "/employees/reports/specialDateReport.jrxml", MessageUtils.getMessage("Reports.specialDate.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT DISTINCT " +
                "specialDate.specialDateTarget," +
                "jobCategoryEmployee.name," +
                "employee.idNumber," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "specialDate.initPeriod," +
                "specialDate.endPeriod," +
                "specialDate.title" +
                " FROM SpecialDate specialDate" +
                //employee
                " LEFT JOIN specialDate.employee employee" +
                " LEFT JOIN employee.contractList contract" +
                " LEFT JOIN contract.jobContractList jobContract" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.jobCategory jobCategoryEmployee" +
                " LEFT JOIN jobCategoryEmployee.sector sectorEmployee" +
                " LEFT JOIN job.organizationalUnit organizationalUnitEmployee" +
                " LEFT JOIN organizationalUnitEmployee.businessUnit businessUnitEmployee" +
                //organization
                " LEFT JOIN specialDate.organizationalUnit organizationalUnit" +
                " LEFT JOIN organizationalUnit.sector sectorOrg" +
                " LEFT JOIN organizationalUnit.businessUnit businessUnitOrg" +
                //bussines unit
                " LEFT JOIN specialDate.businessUnit businessUnit" +
                writeSharedConditionsWithOrConector();

    }

    @Create
    public void init() {
        restrictions = new String[]{"specialDate.company=#{currentCompany}",
                "jobCategoryEmployee=#{specialDateReportAction.jobCategory}",
                "specialDate.specialDateTarget=#{specialDateReportAction.specialDateTarget}",
                "specialDate.initPeriod >= #{specialDateReportAction.initDate}",
                "specialDate.endPeriod <= #{specialDateReportAction.endDate}"};

        sortProperty = "specialDate.title";
    }

    /**
     * Write sahred conditions by IU filters
     *
     * @return String
     */
    private String writeSharedConditionsWithOrConector() {
        String conditions = "";

        //bussines unit
        if (businessUnit != null) {
            conditions = conditions + "(businessUnitEmployee.id = " + businessUnit.getId() + " OR businessUnitOrg.id = " + businessUnit.getId() + " OR businessUnit.id = " + businessUnit.getId() + ")";
        }

        //sector
        if (sector != null) {
            if (conditions.length() > 0) {
                conditions = conditions + " AND ";
            }
            conditions = conditions + "(sectorEmployee.id = " + sector.getId() + " OR sectorOrg.id = " + sector.getId() + ")";
        }

        //add the where clausule, only if has conditions
        if (conditions.length() > 0) {
            conditions = " WHERE " + conditions;
        }

        return conditions;
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
            filtersInfo = filtersInfo + MessageUtils.getMessage("SpecialDate.report.sede") + ": " + businessUnit.getPublicity() + "\n";
        }

        if (initDate != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("SpecialDate.initPeriod") + ": " + DateUtils.format(initDate, MessageUtils.getMessage("patterns.date")) + "\n";
        }

        if (endDate != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("SpecialDate.endPeriod") + ": " + DateUtils.format(endDate, MessageUtils.getMessage("patterns.date")) + "\n";
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

    public SpecialDateTarget getSpecialDateTarget() {
        return specialDateTarget;
    }

    public void setSpecialDateTarget(SpecialDateTarget specialDateTarget) {
        this.specialDateTarget = specialDateTarget;
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
