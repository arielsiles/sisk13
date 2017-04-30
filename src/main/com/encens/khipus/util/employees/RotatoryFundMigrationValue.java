package com.encens.khipus.util.employees;

import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public class RotatoryFundMigrationValue extends AbstractMigrationData {
    private CashAccount cashAccount;
    private BigDecimal exchangeAmount;

    public RotatoryFundMigrationValue(String businessUnitCode, String costCenterCode, CashAccount cashAccount, FinancesCurrencyType currency, BigDecimal exchangeAmount) {
        setBusinessUnitCode(businessUnitCode);
        setCostCenterCode(costCenterCode);
        setCurrency(currency);
        this.cashAccount = cashAccount;
        this.exchangeAmount = exchangeAmount;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public BigDecimal getExchangeAmount() {
        return exchangeAmount;
    }

    @Override
    public String getKeyCode() {
        return getBusinessUnitCode() + "_" + getCostCenterCode() + "_" + cashAccount.getAccountCode() + "_" + getCurrency() + "_" + exchangeAmount;
    }
}
