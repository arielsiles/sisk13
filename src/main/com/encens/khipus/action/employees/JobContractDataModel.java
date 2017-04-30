package com.encens.khipus.action.employees;

import com.encens.khipus.action.finances.RotatoryFundAction;
import com.encens.khipus.exception.employees.MalformedEntityQueryCompoundConditionException;
import com.encens.khipus.framework.action.EntityQuery;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.JobCategoryService;
import com.encens.khipus.service.employees.SectorService;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.query.EntityQueryCompoundCondition;
import com.encens.khipus.util.query.EntityQuerySingleCondition;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : 1.1.8
 */
@Name("jobContractDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('JOBCONTRACT','VIEW')}")
public class JobContractDataModel extends QueryDataModel<Long, JobContract> {

    @In(required = false)
    private RotatoryFundAction rotatoryFundAction;
    @Logger
    private Log log;
    private Employee employee;
    private static final String[] RESTRICTIONS = {
            "organizationalUnit.businessUnit = #{jobContractDataModel.businessUnit}",
            "jobCategory.sector = #{jobContractDataModel.sector}",
            "jobCategory = #{jobContractDataModel.jobCategory}",
            "employee = #{jobContractDataModel.employee}",
            "employee.idNumber like concat(#{jobContractDataModel.idNumber}, '%')",
            "lower(employee.lastName) like concat('%', concat(lower(#{jobContractDataModel.lastName}), '%'))",
            "lower(employee.maidenName) like concat('%', concat(lower(#{jobContractDataModel.maidenName}), '%'))",
            "lower(employee.firstName) like concat('%', concat(lower(#{jobContractDataModel.firstName}), '%'))",
            "employee.retentionFlag = #{jobContractDataModel.retentionFlag}",
            "jobContract.contract.id = #{contractAction.instance.id}",
            "jobContract.id not in (#{grantedBonusCreateAction.selectedJobContractIdList})"
    };

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

    @In
    private SectorService sectorService;
    @In
    private JobCategoryService jobCategoryService;

    @Create
    public void init() {
        sortProperty = "employee.lastName,employee.maidenName,employee.firstName";
    }

    @Override
    protected void postInitEntityQuery(EntityQuery entityQuery) {
        entityQuery.setEjbql(addConditions(getEjbql()));
    }

    public String addConditions(String ejbql) {
        EntityQueryCompoundCondition entityQueryCompoundCondition = new EntityQueryCompoundCondition();
        String restrictionResult = "";
        try {

            boolean filterByActiveForTaxPayroll = null != rotatoryFundAction;
            if (filterByActiveForTaxPayroll) {
                entityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition(" jobContract.job.jobCategory.payrollGenerationType != #{rotatoryFundAction.excludedPayrollGenerationType} "));
            }

            restrictionResult = entityQueryCompoundCondition.compile();
        } catch (MalformedEntityQueryCompoundConditionException e) {
            log.error("Malformed entity query compound condition exception, condition will not be added", e);
        }
        if (!ValidatorUtil.isBlankOrNull(restrictionResult)) {
            ejbql += " where ";
            ejbql += restrictionResult;
        }
        log.debug("ejbql: " + ejbql);
        return ejbql;
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
