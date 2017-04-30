package com.encens.khipus.util.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;

/**
 * BankAccountPaymentTypeMigrationData
 *
 * @author
 * @version 2.0
 */
public class BankAccountPaymentTypeMigrationData extends AbstractMigrationData {
    private Boolean salaryPayroll;
    private JobCategory jobCategory;
    private String bankAccountCode;


    public BankAccountPaymentTypeMigrationData(Long payrollGenerationId, BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, String bankAccountCode, FinancesCurrencyType financesCurrencyType, Boolean salaryPayroll) {
        setPayrollGenerationId(payrollGenerationId);
        setBusinessUnitCode(businessUnit.getExecutorUnitCode());
        setCostCenterCode(costCenter.getCode());
        setJobCategory(jobCategory);
        setBankAccountCode(bankAccountCode);
        setCurrency(financesCurrencyType);
        setSalaryPayroll(salaryPayroll);
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
        return getBusinessUnitCode() + "_" + getCostCenterCode() + "_" + getBankAccountCode() + "_" + getJobCategory().getNationalCurrencyDebitAccountCode() + "_" + getJobCategory().getForeignCurrencyDebitAccountCode();
    }

    @Override
    public String toString() {
        return "BankAccountPaymentTypeMigrationData{" +
                "businessUnitCode='" + getBusinessUnitCode() + '\'' +
                ", costCenterCode='" + getCostCenterCode() + '\'' +
                ", bankAccountCode='" + bankAccountCode + '\'' +
                ", amount=" + getAmount() +
                ", nationalCurrencyDebitAccountCode='" + getJobCategory().getNationalCurrencyDebitAccountCode() + '\'' +
                ", foreignCurrencyDebitAccountCode='" + getJobCategory().getForeignCurrencyDebitAccountCode() + '\'' +
                '}';
    }
}
