package com.encens.khipus.util.employees;

import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * AbstractMigrationData
 *
 * @author
 * @version 2.0
 */
public abstract class AbstractMigrationData {
    private Long payrollGenerationId;
    private String businessUnitCode;
    private String costCenterCode;
    private BigDecimal amount = BigDecimal.ZERO;
    private FinancesCurrencyType currency;

    public abstract String getKeyCode();


    public Long getPayrollGenerationId() {
        return payrollGenerationId;
    }

    public void setPayrollGenerationId(Long payrollGenerationId) {
        this.payrollGenerationId = payrollGenerationId;
    }

    public String getBusinessUnitCode() {
        return businessUnitCode;
    }

    public void setBusinessUnitCode(String businessUnitCode) {
        this.businessUnitCode = businessUnitCode;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public void addAmount(BigDecimal amount) {
        setAmount(BigDecimalUtil.sum(getAmount(), amount));
    }

    public void addAmount(int scale, BigDecimal amount) {
        setAmount(BigDecimalUtil.sum(scale, getAmount(), amount));
    }
}
