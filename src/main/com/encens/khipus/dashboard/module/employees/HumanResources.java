package com.encens.khipus.dashboard.module.employees;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.6
 */
public class HumanResources implements DashboardObject {
    private Integer monthNumber;

    private String monthName;

    private Integer year;

    @Sum(fieldResultName = "localCurrencyTotalAmount")
    private BigDecimal localCurrencyAmount;

    @Sum(fieldResultName = "exchangeCurrencyTotalAmount")
    private BigDecimal exchangeCurrencyAmount;

    public Object getIdentifier() {
        return monthNumber;
    }

    public void setMonthNumber(Integer monthNumber) {
        this.monthNumber = monthNumber;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getLocalCurrencyAmount() {
        return localCurrencyAmount;
    }

    public void setLocalCurrencyAmount(BigDecimal localCurrencyAmount) {
        this.localCurrencyAmount = localCurrencyAmount;
    }

    public BigDecimal getExchangeCurrencyAmount() {
        return exchangeCurrencyAmount;
    }

    public void setExchangeCurrencyAmount(BigDecimal exchangeCurrencyAmount) {
        this.exchangeCurrencyAmount = exchangeCurrencyAmount;
    }


}
