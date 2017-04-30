package com.encens.khipus.util.employees;

import com.encens.khipus.model.finances.FinancesBankAccount;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AccountingRecordFixData
 *
 * @author
 * @version 2.4
 */
public class AccountingRecordFixData implements Serializable {
    private AccountingRecordData accountingRecordData;
    private String sourceBankAccountNumber;
    private FinancesBankAccount sourceBankAccount;
    private BigDecimal maximumAmount;
    private FinancesBankAccount targetBankAccount;

    public AccountingRecordData getAccountingRecordData() {
        return accountingRecordData;
    }

    public void setAccountingRecordData(AccountingRecordData accountingRecordData) {
        this.accountingRecordData = accountingRecordData;
    }

    public String getSourceBankAccountNumber() {
        return sourceBankAccountNumber;
    }

    public void setSourceBankAccountNumber(String sourceBankAccountNumber) {
        this.sourceBankAccountNumber = sourceBankAccountNumber;
    }

    public FinancesBankAccount getSourceBankAccount() {
        return sourceBankAccount;
    }

    public void setSourceBankAccount(FinancesBankAccount sourceBankAccount) {
        this.sourceBankAccount = sourceBankAccount;
    }

    public BigDecimal getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(BigDecimal maximumAmount) {
        this.maximumAmount = maximumAmount;
    }

    public FinancesBankAccount getTargetBankAccount() {
        return targetBankAccount;
    }

    public void setTargetBankAccount(FinancesBankAccount targetBankAccount) {
        this.targetBankAccount = targetBankAccount;
    }

    public void restart() {
        setMaximumAmount(null);
        setTargetBankAccount(null);
    }

    @Override
    public String toString() {
        return "AccountingRecordFixData{" +
                "\n\t accountingRecordData=" + accountingRecordData +
                "\n\t sourceBankAccountCode='" + sourceBankAccountNumber + '\'' +
                "\n\t sourceBankAccount=" + sourceBankAccount +
                "\n\t maximumAmount=" + maximumAmount +
                "\n\t targetBankAccount=" + targetBankAccount +
                '}';
    }
}
