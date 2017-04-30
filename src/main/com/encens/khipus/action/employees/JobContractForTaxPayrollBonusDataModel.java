package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.JobCategoryService;
import com.encens.khipus.service.employees.JobContractForPayrollService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;

import java.util.Date;
import java.util.List;


/**
 * DataModel for JobContract to manage taxPayroll bonus
 *
 * @author
 * @version 2.26
 */

@Name("jobContractForTaxPayrollBonusDataModel")
@Scope(ScopeType.PAGE)
public class JobContractForTaxPayrollBonusDataModel extends QueryDataModel<Long, JobContract> {

    @In
    private JobContractForPayrollService jobContractForPayrollService;

    @In
    private JobCategoryService jobCategoryService;
    @In
    private PayrollGenerationCycleAction payrollGenerationCycleAction;
    @In(required = false)
    private GrantedBonusCreateAction grantedBonusCreateAction;
    @In(required = false)
    private GrantedBonusAction grantedBonusAction;

    @Logger
    protected Log log;

    private JobContract criteria;
    private Date startDate;
    private Date endDate;

    private String idNumber;
    private String firstName;
    private String maidenName;
    private String lastName;
    private BusinessUnit businessUnit;
    private PayrollGenerationType payrollGenerationType;
    private JobCategory jobCategory;
    private Sector sector;
    private Boolean retentionFlag;
    private Employee employee;

    private Boolean enableBusinessUnitFilter = true;
    private Boolean enableJobCategoryFilter = true;

    private List<Long> selectedJobContractIdList;
    private Bonus bonus;

    @Create
    public void init() {
        sortProperty = "employee.lastName,employee.maidenName,employee.firstName";
    }

    @Override
    public Long getCount() {
        return jobContractForPayrollService.getCount(sortProperty, sortAsc,
                idNumber, firstName, maidenName, lastName,
                startDate, endDate, businessUnit, payrollGenerationType,
                selectedJobContractIdList, bonus);
    }

    @Override
    public List<JobContract> getList(Integer firstRow, Integer maxResults) {
        return jobContractForPayrollService.getList(firstRow, maxResults, sortProperty, sortAsc,
                idNumber, firstName, maidenName, lastName,
                startDate, endDate, businessUnit, payrollGenerationType,
                selectedJobContractIdList, bonus);
    }

    @Override
    public JobContract getCriteria() {
        return criteria;
    }

    @Override
    public void setCriteria(JobContract criteria) {
        this.criteria = criteria;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public List<JobCategory> getJobCategoryList() {
        return jobCategoryService.getJobCategoriesBySector(getSector());
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

    public List<Long> getSelectedJobContractIdList() {
        return selectedJobContractIdList;
    }

    public void setSelectedJobContractIdList(List<Long> selectedJobContractIdList) {
        this.selectedJobContractIdList = selectedJobContractIdList;
    }

    public Bonus getBonus() {
        return bonus;
    }

    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
    }

    public PayrollGenerationType getPayrollGenerationType() {
        return payrollGenerationType;
    }

    public void setPayrollGenerationType(PayrollGenerationType payrollGenerationType) {
        this.payrollGenerationType = payrollGenerationType;
    }

    @Override
    public void clear() {
        idNumber = null;
        firstName = null;
        lastName = null;
        maidenName = null;
        updateAndSearch();
    }

    public void loadParameters() {
        startDate = payrollGenerationCycleAction.getInstance().getStartDate();
        endDate = payrollGenerationCycleAction.getInstance().getEndDate();
        selectedJobContractIdList = grantedBonusCreateAction.getSelectedJobContractIdList();
        bonus = (null != grantedBonusCreateAction) ? grantedBonusCreateAction.getBonus() : grantedBonusAction.getInstance().getBonus();
        businessUnit = payrollGenerationCycleAction.getInstance().getBusinessUnit();
        payrollGenerationType = PayrollGenerationType.GENERATION_BY_TIME;
        updateAndSearch();
        log.debug("loadParameters");
        log.debug("startDate " + startDate);
        log.debug("endDate " + endDate);
        log.debug("selectedJobContractIdList " + selectedJobContractIdList);
        log.debug("bonus " + bonus);
        log.debug("businessUnit " + businessUnit);
        log.debug("payrollGenerationType " + payrollGenerationType);
    }
}
