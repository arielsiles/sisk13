package com.encens.khipus.util.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;

import java.math.BigDecimal;

/**
 * CostCenterMigrationData
 *
 * @author
 * @version 1.4
 */
public class CostCenterMigrationData extends AbstractMigrationData {
    public enum CostCenterMigrationDataType {
        PAYROLL_PROVISION, CHRISTMAS_PROVISION, COMPENSATION_PREVISION
    }

    private CostCenterMigrationDataType migrationDataType;
    private JobCategory jobCategory;
    private BigDecimal exchangeRate;

    public CostCenterMigrationData(Long payrollGenerationId, BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, BigDecimal exchangeRate, CostCenterMigrationDataType migrationDataType) {
        setPayrollGenerationId(payrollGenerationId);
        setBusinessUnitCode(businessUnit.getExecutorUnitCode());
        setCostCenterCode(costCenter.getCode());
        setJobCategory(jobCategory);
        setExchangeRate(exchangeRate);
        setMigrationDataType(migrationDataType);
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

    public CostCenterMigrationDataType getMigrationDataType() {
        return migrationDataType;
    }

    public void setMigrationDataType(CostCenterMigrationDataType migrationDataType) {
        this.migrationDataType = migrationDataType;
    }

    public CashAccount getNationalCurrencyDebitAccount() {
        if (getJobCategory() != null) {
            if (CostCenterMigrationDataType.PAYROLL_PROVISION.equals(getMigrationDataType())) {
                return getJobCategory().getNationalCurrencyDebitAccount();
            } else if (CostCenterMigrationDataType.CHRISTMAS_PROVISION.equals(getMigrationDataType())) {
                return getJobCategory().getNationalCurrencyChristmasExpendAccount();
            } else if (CostCenterMigrationDataType.COMPENSATION_PREVISION.equals(getMigrationDataType())) {
                return getJobCategory().getNationalCurrencyCompensationExpendAccount();
            }
        }
        return null;
    }

    public CashAccount getForeignCurrencyDebitAccount() {
        if (getJobCategory() != null) {
            if (CostCenterMigrationDataType.PAYROLL_PROVISION.equals(getMigrationDataType())) {
                return getJobCategory().getForeignCurrencyDebitAccount();
            } else if (CostCenterMigrationDataType.CHRISTMAS_PROVISION.equals(getMigrationDataType())) {
                return getJobCategory().getNationalCurrencyChristmasExpendAccount();
            } else if (CostCenterMigrationDataType.COMPENSATION_PREVISION.equals(getMigrationDataType())) {
                return getJobCategory().getNationalCurrencyCompensationExpendAccount();
            }
        }
        return null;
    }

    public CashAccount getNationalCurrencyCreditAccount() {
        if (getJobCategory() != null) {
            if (CostCenterMigrationDataType.PAYROLL_PROVISION.equals(getMigrationDataType())) {
                return getJobCategory().getNationalCurrencyCreditAccount();
            } else if (CostCenterMigrationDataType.CHRISTMAS_PROVISION.equals(getMigrationDataType())) {
                return getJobCategory().getNationalCurrencyChristmasProvisionAccount();
            } else if (CostCenterMigrationDataType.COMPENSATION_PREVISION.equals(getMigrationDataType())) {
                return getJobCategory().getNationalCurrencyCompensationPrevisionAccount();
            }
        }
        return null;
    }

    public CashAccount getForeignCurrencyCreditAccount() {
        if (getJobCategory() != null) {
            if (CostCenterMigrationDataType.PAYROLL_PROVISION.equals(getMigrationDataType())) {
                return getJobCategory().getForeignCurrencyCreditAccount();
            } else if (CostCenterMigrationDataType.CHRISTMAS_PROVISION.equals(getMigrationDataType())) {
                return getJobCategory().getForeignCurrencyChristmasProvisionAccount();
            } else if (CostCenterMigrationDataType.COMPENSATION_PREVISION.equals(getMigrationDataType())) {
                return getJobCategory().getForeignCurrencyCompensationPrevisionAccount();
            }
        }
        return null;
    }

    public String getKeyCode() {
        return getBusinessUnitCode() + "_" + getCostCenterCode() + "_" + getJobCategory().getId() + "_" + getExchangeRate();
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
