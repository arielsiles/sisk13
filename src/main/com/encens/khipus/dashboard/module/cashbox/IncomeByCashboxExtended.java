package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;
import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeByCashboxExtended implements DashboardObject {
    private Integer code;
    private String month;

    @Sum(fieldResultName = "laPazBs")
    private BigDecimal laPazBs;
    @Sum(fieldResultName = "laPazUsd")
    private BigDecimal laPazUsd;
    @Sum(fieldResultName = "totalLaPazUsd")
    private BigDecimal totalLaPazUsd;

    @Sum(fieldResultName = "santaCruzBs")
    private BigDecimal santaCruzBs;
    @Sum(fieldResultName = "santaCruzUsd")
    private BigDecimal santaCruzUsd;
    @Sum(fieldResultName = "totalSantaCruzUsd")
    private BigDecimal totalSantaCruzUsd;

    @Sum(fieldResultName = "cochabambaBs")
    private BigDecimal cochabambaBs;
    @Sum(fieldResultName = "cochabambaUsd")
    private BigDecimal cochabambaUsd;
    @Sum(fieldResultName = "totalCochabambaUsd")
    private BigDecimal totalCochabambaUsd;

    @Sum(fieldResultName = "oruroBs")
    private BigDecimal oruroBs;
    @Sum(fieldResultName = "oruroUsd")
    private BigDecimal oruroUsd;
    @Sum(fieldResultName = "totalOruroUsd")
    private BigDecimal totalOruroUsd;

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

    public BigDecimal getLaPazBs() {
        return laPazBs;
    }

    public void setLaPazBs(BigDecimal laPazBs) {
        this.laPazBs = laPazBs;
    }

    public BigDecimal getLaPazUsd() {
        return laPazUsd;
    }

    public void setLaPazUsd(BigDecimal laPazUsd) {
        this.laPazUsd = laPazUsd;
    }

    public BigDecimal getTotalLaPazUsd() {
        return totalLaPazUsd;
    }

    public void setTotalLaPazUsd(BigDecimal totalLaPazUsd) {
        this.totalLaPazUsd = totalLaPazUsd;
    }

    public BigDecimal getSantaCruzBs() {
        return santaCruzBs;
    }

    public void setSantaCruzBs(BigDecimal santaCruzBs) {
        this.santaCruzBs = santaCruzBs;
    }

    public BigDecimal getSantaCruzUsd() {
        return santaCruzUsd;
    }

    public void setSantaCruzUsd(BigDecimal santaCruzUsd) {
        this.santaCruzUsd = santaCruzUsd;
    }

    public BigDecimal getTotalSantaCruzUsd() {
        return totalSantaCruzUsd;
    }

    public void setTotalSantaCruzUsd(BigDecimal totalSantaCruzUsd) {
        this.totalSantaCruzUsd = totalSantaCruzUsd;
    }

    public BigDecimal getCochabambaBs() {
        return cochabambaBs;
    }

    public void setCochabambaBs(BigDecimal cochabambaBs) {
        this.cochabambaBs = cochabambaBs;
    }

    public BigDecimal getCochabambaUsd() {
        return cochabambaUsd;
    }

    public void setCochabambaUsd(BigDecimal cochabambaUsd) {
        this.cochabambaUsd = cochabambaUsd;
    }

    public BigDecimal getTotalCochabambaUsd() {
        return totalCochabambaUsd;
    }

    public void setTotalCochabambaUsd(BigDecimal totalCochabambaUsd) {
        this.totalCochabambaUsd = totalCochabambaUsd;
    }

    public BigDecimal getOruroBs() {
        return oruroBs;
    }

    public void setOruroBs(BigDecimal oruroBs) {
        this.oruroBs = oruroBs;
    }

    public BigDecimal getOruroUsd() {
        return oruroUsd;
    }

    public void setOruroUsd(BigDecimal oruroUsd) {
        this.oruroUsd = oruroUsd;
    }

    public BigDecimal getTotalOruroUsd() {
        return totalOruroUsd;
    }

    public void setTotalOruroUsd(BigDecimal totalOruroUsd) {
        this.totalOruroUsd = totalOruroUsd;
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
        totalLaPazUsd = BigDecimalUtil.sum(laPazUsd, BigDecimalUtil.divide(laPazBs, exchangeRate));
        totalSantaCruzUsd = BigDecimalUtil.sum(santaCruzUsd, BigDecimalUtil.divide(santaCruzBs, exchangeRate));
        totalCochabambaUsd = BigDecimalUtil.sum(cochabambaUsd, BigDecimalUtil.divide(cochabambaBs, exchangeRate));
        totalOruroUsd = BigDecimalUtil.sum(oruroUsd, BigDecimalUtil.divide(oruroBs, exchangeRate));
        totalUsd = BigDecimalUtil.sum(usd, BigDecimalUtil.divide(bs, exchangeRate));
    }
}
