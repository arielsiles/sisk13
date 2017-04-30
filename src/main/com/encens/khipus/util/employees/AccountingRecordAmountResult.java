package com.encens.khipus.util.employees;

import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * AccountingRecordAmountResult
 *
 * @author
 * @version 2.0
 */
public class AccountingRecordAmountResult {
    private BigDecimal nationalAmountForBank = BigDecimal.ZERO;
    private BigDecimal foreignAmountForBank = BigDecimal.ZERO;
    private BigDecimal nationalAmountForCheck = BigDecimal.ZERO;
    private BigDecimal foreignAmountForCheck = BigDecimal.ZERO;

    public BigDecimal getNationalAmountForBank() {
        return nationalAmountForBank;
    }

    public void setNationalAmountForBank(BigDecimal nationalAmountForBank) {
        this.nationalAmountForBank = nationalAmountForBank;
    }

    public void addNationalAmountForBank(BigDecimal amount) {
        setNationalAmountForBank(BigDecimalUtil.sum(getNationalAmountForBank(), amount));
    }

    public BigDecimal getForeignAmountForBank() {
        return foreignAmountForBank;
    }

    public void setForeignAmountForBank(BigDecimal foreignAmountForBank) {
        this.foreignAmountForBank = foreignAmountForBank;
    }

    public void addForeignAmountForBank(BigDecimal amount) {
        setForeignAmountForBank(BigDecimalUtil.sum(getForeignAmountForBank(), amount));
    }

    public BigDecimal getNationalAmountForCheck() {
        return nationalAmountForCheck;
    }

    public void setNationalAmountForCheck(BigDecimal nationalAmountForCheck) {
        this.nationalAmountForCheck = nationalAmountForCheck;
    }

    public void addNationalAmountForCheck(BigDecimal amount) {
        setNationalAmountForCheck(BigDecimalUtil.sum(getNationalAmountForCheck(), amount));
    }

    public BigDecimal getForeignAmountForCheck() {
        return foreignAmountForCheck;
    }

    public void setForeignAmountForCheck(BigDecimal foreignAmountForCheck) {
        this.foreignAmountForCheck = foreignAmountForCheck;
    }

    public void addForeignAmountForCheck(BigDecimal amount) {
        setForeignAmountForCheck(BigDecimalUtil.sum(getForeignAmountForCheck(), amount));
    }

    public AccountingRecordAmountResult reset() {
        setNationalAmountForBank(BigDecimal.ZERO);
        setForeignAmountForBank(BigDecimal.ZERO);
        setNationalAmountForCheck(BigDecimal.ZERO);
        setForeignAmountForCheck(BigDecimal.ZERO);
        return this;
    }
}
