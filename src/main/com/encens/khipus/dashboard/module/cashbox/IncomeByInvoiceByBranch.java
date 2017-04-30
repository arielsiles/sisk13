package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;
import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeByInvoiceByBranch implements DashboardObject {
    private Integer code;
    private String month;

    @Sum(fieldResultName = "uniquePaymentBs")
    private BigDecimal uniquePaymentBs;
    @Sum(fieldResultName = "uniquePaymentUsd")
    private BigDecimal uniquePaymentUsd;
    @Sum(fieldResultName = "totalUniquePaymentUsd")
    private BigDecimal totalUniquePaymentUsd;

    @Sum(fieldResultName = "halfYearPaymentBs")
    private BigDecimal halfYearPaymentBs;
    @Sum(fieldResultName = "halfYearPaymentUsd")
    private BigDecimal halfYearPaymentUsd;
    @Sum(fieldResultName = "totalHalfYearPaymentUsd")
    private BigDecimal totalHalfYearPaymentUsd;

    @Sum(fieldResultName = "variableIncomeBs")
    private BigDecimal variableIncomeBs;
    @Sum(fieldResultName = "variableIncomeUsd")
    private BigDecimal variableIncomeUsd;
    @Sum(fieldResultName = "totalVariableIncomeUsd")
    private BigDecimal totalVariableIncomeUsd;

    @Sum(fieldResultName = "feeAmountBs")
    private BigDecimal feeAmountBs;
    @Sum(fieldResultName = "feeAmountUsd")
    private BigDecimal feeAmountUsd;
    @Sum(fieldResultName = "totalFeeAmountUsd")
    private BigDecimal totalFeeAmountUsd;

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

    @Sum(fieldResultName = "reserveBs")
    private BigDecimal reserveBs;
    @Sum(fieldResultName = "reserveUsd")
    private BigDecimal reserveUsd;
    @Sum(fieldResultName = "totalReserveUsd")
    private BigDecimal totalReserveUsd;

    @Sum(fieldResultName = "bs")
    private BigDecimal bs;
    @Sum(fieldResultName = "usd")
    private BigDecimal usd;
    @Sum(fieldResultName = "totalUsd")
    private BigDecimal totalUsd;

    @Sum(fieldResultName = "mainTotalUsd")
    private BigDecimal mainTotalUsd;

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

    public BigDecimal getUniquePaymentBs() {
        return uniquePaymentBs;
    }

    public void setUniquePaymentBs(BigDecimal uniquePaymentBs) {
        this.uniquePaymentBs = uniquePaymentBs;
    }

    public BigDecimal getUniquePaymentUsd() {
        return uniquePaymentUsd;
    }

    public void setUniquePaymentUsd(BigDecimal uniquePaymentUsd) {
        this.uniquePaymentUsd = uniquePaymentUsd;
    }

    public BigDecimal getTotalUniquePaymentUsd() {
        return totalUniquePaymentUsd;
    }

    public void setTotalUniquePaymentUsd(BigDecimal totalUniquePaymentUsd) {
        this.totalUniquePaymentUsd = totalUniquePaymentUsd;
    }

    public BigDecimal getHalfYearPaymentBs() {
        return halfYearPaymentBs;
    }

    public void setHalfYearPaymentBs(BigDecimal halfYearPaymentBs) {
        this.halfYearPaymentBs = halfYearPaymentBs;
    }

    public BigDecimal getHalfYearPaymentUsd() {
        return halfYearPaymentUsd;
    }

    public void setHalfYearPaymentUsd(BigDecimal halfYearPaymentUsd) {
        this.halfYearPaymentUsd = halfYearPaymentUsd;
    }

    public BigDecimal getTotalHalfYearPaymentUsd() {
        return totalHalfYearPaymentUsd;
    }

    public void setTotalHalfYearPaymentUsd(BigDecimal totalHalfYearPaymentUsd) {
        this.totalHalfYearPaymentUsd = totalHalfYearPaymentUsd;
    }

    public BigDecimal getVariableIncomeBs() {
        return variableIncomeBs;
    }

    public void setVariableIncomeBs(BigDecimal variableIncomeBs) {
        this.variableIncomeBs = variableIncomeBs;
    }

    public BigDecimal getVariableIncomeUsd() {
        return variableIncomeUsd;
    }

    public void setVariableIncomeUsd(BigDecimal variableIncomeUsd) {
        this.variableIncomeUsd = variableIncomeUsd;
    }

    public BigDecimal getTotalVariableIncomeUsd() {
        return totalVariableIncomeUsd;
    }

    public void setTotalVariableIncomeUsd(BigDecimal totalVariableIncomeUsd) {
        this.totalVariableIncomeUsd = totalVariableIncomeUsd;
    }

    public BigDecimal getFeeAmountBs() {
        return feeAmountBs;
    }

    public void setFeeAmountBs(BigDecimal feeAmountBs) {
        this.feeAmountBs = feeAmountBs;
    }

    public BigDecimal getFeeAmountUsd() {
        return feeAmountUsd;
    }

    public void setFeeAmountUsd(BigDecimal feeAmountUsd) {
        this.feeAmountUsd = feeAmountUsd;
    }

    public BigDecimal getTotalFeeAmountUsd() {
        return totalFeeAmountUsd;
    }

    public void setTotalFeeAmountUsd(BigDecimal totalFeeAmountUsd) {
        this.totalFeeAmountUsd = totalFeeAmountUsd;
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

    public BigDecimal getTotalSoldProductUsd() {
        return totalSoldProductUsd;
    }

    public void setTotalSoldProductUsd(BigDecimal totalSoldProductUsd) {
        this.totalSoldProductUsd = totalSoldProductUsd;
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

    public BigDecimal getTotalRentalUsd() {
        return totalRentalUsd;
    }

    public void setTotalRentalUsd(BigDecimal totalRentalUsd) {
        this.totalRentalUsd = totalRentalUsd;
    }

    public BigDecimal getReserveBs() {
        return reserveBs;
    }

    public void setReserveBs(BigDecimal reserveBs) {
        this.reserveBs = reserveBs;
    }

    public BigDecimal getReserveUsd() {
        return reserveUsd;
    }

    public void setReserveUsd(BigDecimal reserveUsd) {
        this.reserveUsd = reserveUsd;
    }

    public BigDecimal getTotalReserveUsd() {
        return totalReserveUsd;
    }

    public void setTotalReserveUsd(BigDecimal totalReserveUsd) {
        this.totalReserveUsd = totalReserveUsd;
    }

    public BigDecimal getBs() {
        return bs;
    }

    public void setBs(BigDecimal bs) {
        this.bs = bs;
    }

    public BigDecimal getUsd() {
        return usd;
    }

    public void setUsd(BigDecimal usd) {
        this.usd = usd;
    }

    public BigDecimal getTotalUsd() {
        return totalUsd;
    }

    public void setTotalUsd(BigDecimal totalUsd) {
        this.totalUsd = totalUsd;
    }

    public BigDecimal getMainTotalUsd() {
        return mainTotalUsd;
    }

    public void setMainTotalUsd(BigDecimal mainTotalUsd) {
        this.mainTotalUsd = mainTotalUsd;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setTotalValues() {
        totalUniquePaymentUsd = BigDecimalUtil.sum(uniquePaymentUsd, BigDecimalUtil.divide(uniquePaymentBs, exchangeRate));
        totalHalfYearPaymentUsd = BigDecimalUtil.sum(halfYearPaymentUsd, BigDecimalUtil.divide(halfYearPaymentBs, exchangeRate));
        totalVariableIncomeUsd = BigDecimalUtil.sum(variableIncomeUsd, BigDecimalUtil.divide(variableIncomeBs, exchangeRate));
        totalFeeAmountUsd = BigDecimalUtil.sum(feeAmountUsd, BigDecimalUtil.divide(feeAmountBs, exchangeRate));
        totalSoldProductUsd = BigDecimalUtil.sum(soldProductUsd, BigDecimalUtil.divide(soldProductBs, exchangeRate));
        totalRentalUsd = BigDecimalUtil.sum(rentalUsd, BigDecimalUtil.divide(rentalBs, exchangeRate));
        totalReserveUsd = BigDecimalUtil.sum(reserveUsd, BigDecimalUtil.divide(reserveBs, exchangeRate));
        totalUsd = BigDecimalUtil.sum(usd, BigDecimalUtil.divide(bs, exchangeRate));
    }
}
