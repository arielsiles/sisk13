package com.encens.khipus.util.employees;

import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.4
 */
public class RotatoryFundMigrationData {
    private String transactionNumber;
    private Map<String, RotatoryFundMigrationValue> debit = new HashMap<String, RotatoryFundMigrationValue>();
    private Map<String, RotatoryFundMigrationValue> credit = new HashMap<String, RotatoryFundMigrationValue>();

    public RotatoryFundMigrationData(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void addDebit(String businessUnitCode, String costCenterCode, CashAccount cashAccount,
                         BigDecimal amount, FinancesCurrencyType currency, BigDecimal exchangeAmount) {
        addAmount(debit, businessUnitCode, costCenterCode, cashAccount, amount, currency, exchangeAmount);
    }

    public void addCredit(String businessUnitCode, String costCenterCode, CashAccount cashAccount,
                          BigDecimal amount, FinancesCurrencyType currency, BigDecimal exchangeAmount) {
        addAmount(credit, businessUnitCode, costCenterCode, cashAccount, amount, currency, exchangeAmount);
    }

    public List<RotatoryFundMigrationValue> getDebitValues() {
        return new ArrayList<RotatoryFundMigrationValue>(debit.values());
    }

    public List<RotatoryFundMigrationValue> getCreditValues() {
        return new ArrayList<RotatoryFundMigrationValue>(credit.values());
    }

    public Boolean isEmpty() {
        return debit.isEmpty() && credit.isEmpty();
    }

    private void addAmount(Map<String, RotatoryFundMigrationValue> map, String businessUnitCode, String costCenterCode, CashAccount cashAccount,
                           BigDecimal amount, FinancesCurrencyType currency, BigDecimal exchangeAmount) {
        RotatoryFundMigrationValue value = new RotatoryFundMigrationValue(businessUnitCode, costCenterCode, cashAccount, currency, exchangeAmount);
        if (!map.containsKey(value.getKeyCode())) {
            value.setAmount(amount);
            map.put(value.getKeyCode(), value);
        } else {
            map.get(value.getKeyCode()).addAmount(amount);
        }
    }
}
