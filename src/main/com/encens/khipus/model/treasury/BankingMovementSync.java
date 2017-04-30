package com.encens.khipus.model.treasury;

import com.encens.khipus.model.finances.FinancesBankAccount;

/**
 * BankingMovementSync
 *
 * @author
 * @version 2.9
 */
public class BankingMovementSync {
    private FinancesBankAccount bankAccount;
    private String columnSeparator = "|";
    private Integer accountNumberPosition;
    private Integer documentTypePosition;
    private Integer documentNumberPosition;
    private Integer datePosition;
    private String datePattern = "dd/MM/yyyy";
    private Integer amountPosition;
    private Integer magnitude = 0;
    private Integer glossPosition;


    public FinancesBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(FinancesBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getColumnSeparator() {
        return columnSeparator;
    }

    public void setColumnSeparator(String columnSeparator) {
        this.columnSeparator = columnSeparator;
    }

    public Integer getAccountNumberPosition() {
        return accountNumberPosition;
    }

    public void setAccountNumberPosition(Integer accountNumberPosition) {
        this.accountNumberPosition = accountNumberPosition;
    }

    public Integer getDocumentTypePosition() {
        return documentTypePosition;
    }

    public void setDocumentTypePosition(Integer documentTypePosition) {
        this.documentTypePosition = documentTypePosition;
    }

    public Integer getDocumentNumberPosition() {
        return documentNumberPosition;
    }

    public void setDocumentNumberPosition(Integer documentNumberPosition) {
        this.documentNumberPosition = documentNumberPosition;
    }

    public Integer getDatePosition() {
        return datePosition;
    }

    public void setDatePosition(Integer datePosition) {
        this.datePosition = datePosition;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public Integer getAmountPosition() {
        return amountPosition;
    }

    public void setAmountPosition(Integer amountPosition) {
        this.amountPosition = amountPosition;
    }

    public Integer getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Integer magnitude) {
        this.magnitude = magnitude;
    }

    public Integer getGlossPosition() {
        return glossPosition;
    }

    public void setGlossPosition(Integer glossPosition) {
        this.glossPosition = glossPosition;
    }
}
