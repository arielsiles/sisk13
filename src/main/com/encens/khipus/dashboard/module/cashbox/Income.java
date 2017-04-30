package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class Income implements DashboardObject {
    private Integer month;
    private Integer year;
    private String monthName;

    @Sum(fieldResultName = "totalBsAmount")
    private BigDecimal bsAmount;

    @Sum(fieldResultName = "totalUsdAmount")
    private BigDecimal usdAmount;

    @Sum(fieldResultName = "mainTotalAmount")
    private BigDecimal totalAmount;

    private BigDecimal exchangeRate;


    public Object getIdentifier() {
        return month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public BigDecimal getBsAmount() {
        return bsAmount;
    }

    public void setBsAmount(BigDecimal bsAmount) {
        this.bsAmount = bsAmount;
    }

    public BigDecimal getUsdAmount() {
        return usdAmount;
    }

    public void setUsdAmount(BigDecimal usdAmount) {
        this.usdAmount = usdAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
