package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class DebtExtendedAttribute {
    private Integer realCounter = 0;
    private Integer paymentCounter = 0;
    private Integer debtCounter = 0;

    private BigDecimal realBs = BigDecimal.ZERO;
    private BigDecimal realUsd = BigDecimal.ZERO;
    private BigDecimal paymentBs = BigDecimal.ZERO;
    private BigDecimal paymentUsd = BigDecimal.ZERO;
    private BigDecimal debtBs = BigDecimal.ZERO;
    private BigDecimal debtUsd = BigDecimal.ZERO;

    public Integer getRealCounter() {
        return realCounter;
    }

    public void setRealCounter(Integer realCounter) {
        this.realCounter = realCounter;
    }

    public Integer getPaymentCounter() {
        return paymentCounter;
    }

    public void setPaymentCounter(Integer paymentCounter) {
        this.paymentCounter = paymentCounter;
    }

    public Integer getDebtCounter() {
        return debtCounter;
    }

    public void setDebtCounter(Integer debtCounter) {
        this.debtCounter = debtCounter;
    }

    public BigDecimal getRealBs() {
        return realBs;
    }

    public void setRealBs(BigDecimal realBs) {
        this.realBs = realBs;
    }

    public BigDecimal getRealUsd() {
        return realUsd;
    }

    public void setRealUsd(BigDecimal realUsd) {
        this.realUsd = realUsd;
    }

    public BigDecimal getPaymentBs() {
        return paymentBs;
    }

    public void setPaymentBs(BigDecimal paymentBs) {
        this.paymentBs = paymentBs;
    }

    public BigDecimal getPaymentUsd() {
        return paymentUsd;
    }

    public void setPaymentUsd(BigDecimal paymentUsd) {
        this.paymentUsd = paymentUsd;
    }

    public BigDecimal getDebtBs() {
        return debtBs;
    }

    public void setDebtBs(BigDecimal debtBs) {
        this.debtBs = debtBs;
    }

    public BigDecimal getDebtUsd() {
        return debtUsd;
    }

    public void setDebtUsd(BigDecimal debtUsd) {
        this.debtUsd = debtUsd;
    }

    public void addValues(DebtExtendedAttribute attribute) {
        if (null != attribute.getRealCounter()) {
            this.realCounter = this.realCounter + attribute.getRealCounter();
        }

        if (null != attribute.getPaymentCounter()) {
            this.paymentCounter = this.paymentCounter + attribute.getPaymentCounter();
        }

        if (null != attribute.getDebtCounter()) {
            this.debtCounter = this.debtCounter + attribute.getDebtCounter();
        }

        if (null != attribute.getRealBs()) {
            this.realBs = BigDecimalUtil.sum(this.realBs, attribute.getRealBs());
        }

        if (null != attribute.getRealUsd()) {
            this.realUsd = BigDecimalUtil.sum(this.realUsd, attribute.getRealUsd());
        }

        if (null != attribute.getPaymentBs()) {
            this.paymentBs = BigDecimalUtil.sum(this.paymentBs, attribute.getPaymentBs());
        }

        if (null != attribute.getPaymentUsd()) {
            this.paymentUsd = BigDecimalUtil.sum(this.paymentUsd, attribute.getPaymentUsd());
        }

        if (null != attribute.getDebtBs()) {
            this.debtBs = BigDecimalUtil.sum(this.debtBs, attribute.getDebtBs());
        }

        if (null != attribute.getDebtUsd()) {
            this.debtUsd = BigDecimalUtil.sum(this.debtUsd, attribute.getDebtUsd());
        }
    }
}
