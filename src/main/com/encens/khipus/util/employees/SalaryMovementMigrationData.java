package com.encens.khipus.util.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.SalaryMovementType;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 3.4
 */
public class SalaryMovementMigrationData extends AbstractMigrationData {
    private JobCategory jobCategory;
    private BigDecimal exchangeRate;
    private Map<SalaryMovementType, BigDecimal> amountsBySalaryMovementType = new HashMap<SalaryMovementType, BigDecimal>();

    public SalaryMovementMigrationData(BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, FinancesCurrencyType financesCurrencyType, BigDecimal exchangeRate) {
        setBusinessUnitCode(businessUnit.getExecutorUnitCode());
        setCostCenterCode(costCenter.getCode());
        setJobCategory(jobCategory);
        setCurrency(financesCurrencyType);
        setExchangeRate(exchangeRate);
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Map<SalaryMovementType, BigDecimal> getAmountsBySalaryMovementType() {
        return amountsBySalaryMovementType;
    }

    public void setAmountsBySalaryMovementType(Map<SalaryMovementType, BigDecimal> amountsBySalaryMovementType) {
        this.amountsBySalaryMovementType = amountsBySalaryMovementType;
    }

    public CashAccount getNationalCurrencyDebitAccount() {
        return getJobCategory() != null ? getJobCategory().getNationalCurrencyDebitAccount() : null;
    }

    public CashAccount getForeignCurrencyDebitAccount() {
        return getJobCategory() != null ? getJobCategory().getForeignCurrencyDebitAccount() : null;
    }

    public CashAccount getNationalCurrencyCreditAccount() {
        return getJobCategory() != null ? getJobCategory().getNationalCurrencyCreditAccount() : null;
    }

    public CashAccount getForeignCurrencyCreditAccount() {
        return getJobCategory() != null ? getJobCategory().getForeignCurrencyCreditAccount() : null;
    }

    public String getKeyCode() {
        return getBusinessUnitCode() + "_" + getCostCenterCode() + "_" + getJobCategory().getId() + "_" + getCurrency() + "_" + getExchangeRate();
    }

    public void addAmount(SalaryMovementType salaryMovementType, BigDecimal amount) {
        amountsBySalaryMovementType.put(salaryMovementType, BigDecimalUtil.sum(amountsBySalaryMovementType.get(salaryMovementType), amount));
        addAmount(amount);
    }

    @Override
    public String toString() {
        return "CostCenterMigrationData{" +
                "businessUnitCode='" + getBusinessUnitCode() + '\'' +
                ", costCenterCode='" + getCostCenterCode() + '\'' +
                ", jobCategory.id='" + getJobCategory().getId() + '\'' +
                ", jobCategory.fullName='" + getJobCategory().getFullName() + '\'' +
                '}';
    }
}