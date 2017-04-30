package com.encens.khipus.util;

import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * This class contains values by currency
 * @author
 * @version 2.7
 */
public class CurrencyValuesContainer {
    private BigDecimal ufvValue;
    private BigDecimal bsValue;


    public CurrencyValuesContainer() {
    }

    public CurrencyValuesContainer(BigDecimal ufvValue, BigDecimal bsValue) {
        this.ufvValue = ufvValue;
        this.bsValue = bsValue;
    }

    public BigDecimal getUfvValue() {
        return ufvValue;
    }

    public void setUfvValue(BigDecimal ufvValue) {
        this.ufvValue = ufvValue;
    }

    public BigDecimal getBsValue() {
        return bsValue;
    }

    public void setBsValue(BigDecimal bsValue) {
        this.bsValue = bsValue;
    }

}
