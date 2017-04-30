package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;
import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeByInvoiceByCategory implements DashboardObject {
    private Integer code;

    private String month;

    @Sum(fieldResultName = "requiredServiceBs")
    private BigDecimal requiredServiceBs;
    @Sum(fieldResultName = "requiredServiceUsd")
    private BigDecimal requiredServiceUsd;
    @Sum(fieldResultName = "totalRequiredServiceUsd")
    private BigDecimal totalRequiredServiceUsd;

    @Sum(fieldResultName = "optionalServiceBs")
    private BigDecimal optionalServiceBs;
    @Sum(fieldResultName = "optionalServiceUsd")
    private BigDecimal optionalServiceUsd;
    @Sum(fieldResultName = "totalOptionalServiceUsd")
    private BigDecimal totalOptionalServiceUsd;

    @Sum(fieldResultName = "soldProductBs")
    private BigDecimal soldProductBs;
    @Sum(fieldResultName = "soldProductUsd")
    private BigDecimal soldProductUsd;
    @Sum(fieldResultName = "totalSoldProductUsd")
    private BigDecimal totalSoldProductUsd;

    @Sum(fieldResultName = "rentalBs")
    private BigDecimal rentalBs;
    @Sum(fieldResultName = "rentalUsd")
    private BigDecimal rentalUsd;
    @Sum(fieldResultName = "totalRentalUsd")
    private BigDecimal totalRentalUsd;

    @Sum(fieldResultName = "totalBs")
    private BigDecimal totalBs;
    @Sum(fieldResultName = "totalUsd")
    private BigDecimal totalUsd;
    @Sum(fieldResultName = "mainTotalUsd")
    private BigDecimal mainTotalUsd;

    @Sum(fieldResultName = "finalTotalUsd")
    private BigDecimal finalTotalUsd;

    private BigDecimal exchangeRate;

    public Object getIdentifier() {
        return code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getRequiredServiceBs() {
        return requiredServiceBs;
    }

    public void setRequiredServiceBs(BigDecimal requiredServiceBs) {
        this.requiredServiceBs = requiredServiceBs;
    }

    public BigDecimal getRequiredServiceUsd() {
        return requiredServiceUsd;
    }

    public void setRequiredServiceUsd(BigDecimal requiredServiceUsd) {
        this.requiredServiceUsd = requiredServiceUsd;
    }

    public BigDecimal getOptionalServiceBs() {
        return optionalServiceBs;
    }

    public void setOptionalServiceBs(BigDecimal optionalServiceBs) {
        this.optionalServiceBs = optionalServiceBs;
    }

    public BigDecimal getOptionalServiceUsd() {
        return optionalServiceUsd;
    }

    public void setOptionalServiceUsd(BigDecimal optionalServiceUsd) {
        this.optionalServiceUsd = optionalServiceUsd;
    }

    public BigDecimal getSoldProductBs() {
        return soldProductBs;
    }

    public void setSoldProductBs(BigDecimal soldProductBs) {
        this.soldProductBs = soldProductBs;
    }

    public BigDecimal getSoldProductUsd() {
        return soldProductUsd;
    }

    public void setSoldProductUsd(BigDecimal soldProductUsd) {
        this.soldProductUsd = soldProductUsd;
    }

    public BigDecimal getRentalBs() {
        return rentalBs;
    }

    public void setRentalBs(BigDecimal rentalBs) {
        this.rentalBs = rentalBs;
    }

    public BigDecimal getRentalUsd() {
        return rentalUsd;
    }

    public void setRentalUsd(BigDecimal rentalUsd) {
        this.rentalUsd = rentalUsd;
    }

    public BigDecimal getTotalBs() {
        return totalBs;
    }

    public void setTotalBs(BigDecimal totalBs) {
        this.totalBs = totalBs;
    }

    public BigDecimal getTotalUsd() {
        return totalUsd;
    }

    public void setTotalUsd(BigDecimal totalUsd) {
        this.totalUsd = totalUsd;
    }

    public BigDecimal getFinalTotalUsd() {
        return finalTotalUsd;
    }

    public void setFinalTotalUsd(BigDecimal finalTotalUsd) {
        this.finalTotalUsd = finalTotalUsd;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setTotalValues() {
        totalRequiredServiceUsd = BigDecimalUtil.sum(requiredServiceUsd, BigDecimalUtil.divide(requiredServiceBs, exchangeRate));
        totalOptionalServiceUsd = BigDecimalUtil.sum(optionalServiceUsd, BigDecimalUtil.divide(optionalServiceBs, exchangeRate));
        totalSoldProductUsd = BigDecimalUtil.sum(soldProductUsd, BigDecimalUtil.divide(soldProductBs, exchangeRate));
        totalRentalUsd = BigDecimalUtil.sum(rentalUsd, BigDecimalUtil.divide(rentalBs, exchangeRate));
        mainTotalUsd = BigDecimalUtil.sum(totalUsd, BigDecimalUtil.divide(totalBs, exchangeRate));
    }
}
