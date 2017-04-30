package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.JobCategoryService;
import com.encens.khipus.service.employees.SectorService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for JobContract
 *
 * @author
 * @version 2.26
 */

@Name("selectedJobContractBlockDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('JOBCONTRACT','VIEW')}")
public class SelectedJobContractBlockDataModel extends QueryDataModel<Long, JobContract> {

    @In
    private SectorService sectorService;
    @In
    private JobCategoryService jobCategoryService;

    private Employee employee;
    private String idNumber;
    private String firstName;
    private String maidenName;
    private String lastName;
    private BusinessUnit businessUnit;
    private JobCategory jobCategory;
    private Sector sector;
    private Boolean retentionFlag;

    private Boolean enableBusinessUnitFilter = true;
    private Boolean enableJobCategoryFilter = true;

    private static final String[] RESTRICTIONS =
            {
                    "jobContract.id in (#{grantedBonusCreateAction.selectedJobContractIdList})"
            };

    @Create
    public void init() {
        sortProperty = "employee.lastName,employee.maidenName,employee.firstName";
    }

    @Override
    public String getEjbql() {
        return "select jobContract " +
                " from JobContract jobContract" +
                " left join fetch jobContract.contract contract" +
                " left join fetch contract.employee employee" +
                " left join fetch jobContract.job job" +
                " left join fetch job.charge charge" +
                " left join fetch job.jobCategory jobCategory" +
                " left join fetch job.organizationalUnit organizationalUnit";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public void refreshJobCategory() {
        setJobCategory(null);
    }

    public List<JobCategory> getJobCategoryList() {
        return jobCategoryService.getJobCategoriesBySector(getSector());
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public Boolean getRetentionFlag() {
        return retentionFlag;
    }

    public void setRetentionFlag(Boolean retentionFlag) {
        this.retentionFlag = retentionFlag;
    }

    public Boolean getEnableBusinessUnitFilter() {
        return enableBusinessUnitFilter;
    }

    public void setEnableBusinessUnitFilter(Boolean enableBusinessUnitFilter) {
        this.enableBusinessUnitFilter = enableBusinessUnitFilter;
    }

    public Boolean getEnableJobCategoryFilter() {
        return enableJobCategoryFilter;
    }

    public void setEnableJobCategoryFilter(Boolean enableJobCategoryFilter) {
        this.enableJobCategoryFilter = enableJobCategoryFilter;
    }

    @Override
    public void clear() {
        setIdNumber(null);
        setFirstName(null);
        setMaidenName(null);
        setLastName(null);
        if (enableBusinessUnitFilter) {
            setBusinessUnit(null);
        }
        if (enableJobCategoryFilter) {
            setJobCategory(null);
        }
        setSector(null);
        super.clear();
    }

}