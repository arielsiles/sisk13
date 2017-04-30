package com.encens.khipus.util.employees;

import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.BigDecimalUtil;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AccountingRecordData
 *
 * @author
 * @version 2.2
 */
public class AccountingRecordData implements Serializable {
    public static enum Property {
        AMOUNT, EXCHANGE_RATE
    }

    ;
    private AccountingRecordDataProvider dataProvider;
    private BigDecimal amount;
    private FinancesCurrencyType currency;
    private BigDecimal equivalentAmount;
    private FinancesCurrencyType equivalentCurrency;
    private FinancesCurrencyType currencyBankAccount;
    private BigDecimal exchangeRate;

    public AccountingRecordData() {
    }

    public AccountingRecordData(AccountingRecordDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getEquivalentAmount() {
        return equivalentAmount;
    }

    public void setEquivalentAmount(BigDecimal equivalentAmount) {
        this.equivalentAmount = equivalentAmount;
    }

    public FinancesCurrencyType getCurrencyBankAccount() {
        return currencyBankAccount;
    }

    public void setCurrencyBankAccount(FinancesCurrencyType currencyBankAccount) {
        this.currencyBankAccount = currencyBankAccount;
        setDefaultCurrency(currencyBankAccount);
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
        setEquivalentCurrency(FinancesCurrencyType.P.equals(currency) ? FinancesCurrencyType.D : FinancesCurrencyType.D.equals(currency) ? FinancesCurrencyType.P : null);
    }

    public FinancesCurrencyType getEquivalentCurrency() {
        return equivalentCurrency;
    }

    public void setEquivalentCurrency(FinancesCurrencyType equivalentCurrency) {
        this.equivalentCurrency = equivalentCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
        calculeEquivalentAmount();
    }

    public AccountingRecordDataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(AccountingRecordDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void calculeEquivalentAmount() {
        if (exchangeRate != null && getAmount() != null) {
            if (isNationalCurrencyType()) {
                setEquivalentAmount(BigDecimalUtil.divide(getAmount(), exchangeRate));
            } else if (isForeignCurrencyType()) {
                setEquivalentAmount(BigDecimalUtil.multiply(getAmount(), exchangeRate));
            } else {
                setEquivalentAmount(null);
            }
        } else {
            setEquivalentAmount(null);
        }
        if (dataProvider != null) {
            dataProvider.calculeTotalAmounts();
        }
    }

    public void setDefaultCurrency(FinancesCurrencyType currency) {
        if (getCurrency() == null) {
            setCurrency(currency);
        }
    }

    public void setDefaultNationalCurrencyType() {
        setCurrency(FinancesCurrencyType.P);
    }

    public void setDefaultForeignCurrencyType() {
        setCurrency(FinancesCurrencyType.D);
    }

    public Boolean isNationalCurrencyType() {
        return FinancesCurrencyType.P.equals(getCurrency());
    }

    public Boolean isForeignCurrencyType() {
        return FinancesCurrencyType.D.equals(getCurrency());
    }

    public Boolean isEmpty() {
        return BigDecimalUtil.isZeroOrNull(getAmount()) || BigDecimalUtil.isZeroOrNull(getEquivalentAmount()) || getCurrency() == null || getEquivalentCurrency() == null || getCurrencyBankAccount() == null;
    }


    @Override
    public String toString() {
        return "AccountingRecordData{" +
                "amount=" + amount +
                ", currency=" + currency +
                ", equivalentAmount=" + equivalentAmount +
                ", equivalentCurrency=" + equivalentCurrency +
                ", currencyBankAccount=" + currencyBankAccount +
                ", exchangeRate=" + exchangeRate +
                '}';
    }
}
