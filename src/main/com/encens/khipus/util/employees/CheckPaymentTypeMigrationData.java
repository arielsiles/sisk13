package com.encens.khipus.util.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;

/**
 * CheckPaymentTypeMigrationData
 *
 * @author
 * @version 2.0
 */
public class CheckPaymentTypeMigrationData extends AbstractMigrationData {
    private Boolean salaryPayroll;
    private JobCategory jobCategory;
    private Long employeeId;
    private String employeeName;
    private String bankAccountCode;


    public CheckPaymentTypeMigrationData(Long payrollGenerationId, BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, Employee employee, String bankAccountCode, FinancesCurrencyType financesCurrencyType, Boolean salaryPayroll) {
        setPayrollGenerationId(payrollGenerationId);
        setBusinessUnitCode(businessUnit.getExecutorUnitCode());
        setCostCenterCode(costCenter.getCode());
        setEmployeeId(employee.getId());
        setEmployeeName(employee.getFullName());
        setJobCategory(jobCategory);
        setBankAccountCode(bankAccountCode);
        setCurrency(financesCurrencyType);
        setSalaryPayroll(salaryPayroll);
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Boolean getSalaryPayroll() {
        return salaryPayroll;
    }

    public void setSalaryPayroll(Boolean salaryPayroll) {
        this.salaryPayroll = salaryPayroll;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getBankAccountCode() {
        return bankAccountCode;
    }

    public void setBankAccountCode(String bankAccountCode) {
        this.bankAccountCode = bankAccountCode;
    }

    public String getNationalCurrencyDebitAccountCode() {
        return getJobCategory() != null ? getSalaryPayroll() ? getJobCategory().getNationalCurrencyCreditAccountCode() : getJobCategory().getNationalCurrencyChristmasProvisionAccountCode() : null;
    }

    public String getForeignCurrencyDebitAccountCode() {
        return getJobCategory() != null ? getSalaryPayroll() ? getJobCategory().getForeignCurrencyCreditAccountCode() : getJobCategory().getForeignCurrencyChristmasProvisionAccountCode() : null;
    }

    public String getKeyCode() {
        return getBusinessUnitCode() + "_" + getCostCenterCode() + "_" + getEmployeeId() + "_" + getNationalCurrencyDebitAccountCode() + "_" + getForeignCurrencyDebitAccountCode() + "_" + getBankAccountCode();
    }

    @Override
    public String toString() {
        return "CheckPaymentTypeMigrationData{" +
                "businessUnitCode='" + getBusinessUnitCode() + '\'' +
                ", costCenterCode='" + getCostCenterCode() + '\'' +
                ", employeeId=" + employeeId +
                ", employeeName='" + employeeName + '\'' +
                ", amount=" + getAmount() +
                '}';
    }
}
