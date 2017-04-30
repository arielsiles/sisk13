package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Bonus;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.JobContractForPayrollService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Name("jobContractForPayrollDataModel")
@Scope(ScopeType.PAGE)
public class JobContractForPayrollDataModel extends QueryDataModel<Long, JobContract> {

    @In
    private JobContractForPayrollService jobContractForPayrollService;

    private JobContract criteria;
    private Date startDate;
    private Date endDate;

    private String idNumber;
    private String firstName;
    private String maidenName;
    private String lastName;
    private BusinessUnit businessUnit;
    private PayrollGenerationType payrollGenerationType;

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

    public PayrollGenerationType getPayrollGenerationType() {
        return payrollGenerationType;
    }

    public void setPayrollGenerationType(PayrollGenerationType payrollGenerationType) {
        this.payrollGenerationType = payrollGenerationType;
    }

    @Override
    public void clear() {
        setIdNumber(null);
        setFirstName(null);
        setMaidenName(null);
        setLastName(null);

        super.clear();
    }
}
