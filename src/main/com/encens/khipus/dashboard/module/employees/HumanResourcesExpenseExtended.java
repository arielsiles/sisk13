package com.encens.khipus.dashboard.module.employees;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class HumanResourcesExpenseExtended implements DashboardObject {
    private String code;
    private String description;

    @Sum(fieldResultName = "januaryBs")
    private BigDecimal januaryBs;
    @Sum(fieldResultName = "januaryUsd")
    private BigDecimal januaryUsd;
    @Sum(fieldResultName = "totalJanuaryUsd")
    private BigDecimal totalJanuaryUsd;

    @Sum(fieldResultName = "februaryBs")
    private BigDecimal februaryBs;
    @Sum(fieldResultName = "februaryUsd")
    private BigDecimal februaryUsd;
    @Sum(fieldResultName = "totalFebruaryUsd")
    private BigDecimal totalFebruaryUsd;

    @Sum(fieldResultName = "marchBs")
    private BigDecimal marchBs;
    @Sum(fieldResultName = "marchUsd")
    private BigDecimal marchUsd;
    @Sum(fieldResultName = "totalMarchUsd")
    private BigDecimal totalMarchUsd;

    @Sum(fieldResultName = "aprilBs")
    private BigDecimal aprilBs;
    @Sum(fieldResultName = "aprilUsd")
    private BigDecimal aprilUsd;
    @Sum(fieldResultName = "totalAprilUsd")
    private BigDecimal totalAprilUsd;

    @Sum(fieldResultName = "mayBs")
    private BigDecimal mayBs;
    @Sum(fieldResultName = "mayUsd")
    private BigDecimal mayUsd;
    @Sum(fieldResultName = "totalMayUsd")
    private BigDecimal totalMayUsd;

    @Sum(fieldResultName = "juneBs")
    private BigDecimal juneBs;
    @Sum(fieldResultName = "juneUsd")
    private BigDecimal juneUsd;
    @Sum(fieldResultName = "totalJuneUsd")
    private BigDecimal totalJuneUsd;

    @Sum(fieldResultName = "julyBs")
    private BigDecimal julyBs;
    @Sum(fieldResultName = "julyUsd")
    private BigDecimal julyUsd;
    @Sum(fieldResultName = "totalJulyUsd")
    private BigDecimal totalJulyUsd;

    @Sum(fieldResultName = "augustBs")
    private BigDecimal augustBs;
    @Sum(fieldResultName = "augustUsd")
    private BigDecimal augustUsd;
    @Sum(fieldResultName = "totalAugustUsd")
    private BigDecimal totalAugustUsd;

    @Sum(fieldResultName = "septemberBs")
    private BigDecimal septemberBs;
    @Sum(fieldResultName = "septemberUsd")
    private BigDecimal septemberUsd;
    @Sum(fieldResultName = "totalSeptemberUsd")
    private BigDecimal totalSeptemberUsd;

    @Sum(fieldResultName = "octoberBs")
    private BigDecimal octoberBs;
    @Sum(fieldResultName = "octoberUsd")
    private BigDecimal octoberUsd;
    @Sum(fieldResultName = "totalOctoberUsd")
    private BigDecimal totalOctoberUsd;

    @Sum(fieldResultName = "novemberBs")
    private BigDecimal novemberBs;
    @Sum(fieldResultName = "novemberUsd")
    private BigDecimal novemberUsd;
    @Sum(fieldResultName = "totalNovemberUsd")
    private BigDecimal totalNovemberUsd;

    @Sum(fieldResultName = "decemberBs")
    private BigDecimal decemberBs;
    @Sum(fieldResultName = "decemberUsd")
    private BigDecimal decemberUsd;
    @Sum(fieldResultName = "totalDecemberUsd")
    private BigDecimal totalDecemberUsd;

    @Sum(fieldResultName = "bs")
    private BigDecimal bs;
    @Sum(fieldResultName = "usd")
    private BigDecimal usd;
    @Sum(fieldResultName = "totalUsd")
    private BigDecimal totalUsd;

    @Sum(fieldResultName = "mainTotal")
    private BigDecimal mainTotal;

    public Object getIdentifier() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getJanuaryBs() {
        return januaryBs;
    }

    public void setJanuaryBs(BigDecimal januaryBs) {
        this.januaryBs = januaryBs;
    }

    public BigDecimal getJanuaryUsd() {
        return januaryUsd;
    }

    public void setJanuaryUsd(BigDecimal januaryUsd) {
        this.januaryUsd = januaryUsd;
    }

    public BigDecimal getTotalJanuaryUsd() {
        return totalJanuaryUsd;
    }

    public void setTotalJanuaryUsd(BigDecimal totalJanuaryUsd) {
        this.totalJanuaryUsd = totalJanuaryUsd;
    }

    public BigDecimal getFebruaryBs() {
        return februaryBs;
    }

    public void setFebruaryBs(BigDecimal februaryBs) {
        this.februaryBs = februaryBs;
    }

    public BigDecimal getFebruaryUsd() {
        return februaryUsd;
    }

    public void setFebruaryUsd(BigDecimal februaryUsd) {
        this.februaryUsd = februaryUsd;
    }

    public BigDecimal getTotalFebruaryUsd() {
        return totalFebruaryUsd;
    }

    public void setTotalFebruaryUsd(BigDecimal totalFebruaryUsd) {
        this.totalFebruaryUsd = totalFebruaryUsd;
    }

    public BigDecimal getMarchBs() {
        return marchBs;
    }

    public void setMarchBs(BigDecimal marchBs) {
        this.marchBs = marchBs;
    }

    public BigDecimal getMarchUsd() {
        return marchUsd;
    }

    public void setMarchUsd(BigDecimal marchUsd) {
        this.marchUsd = marchUsd;
    }

    public BigDecimal getTotalMarchUsd() {
        return totalMarchUsd;
    }

    public void setTotalMarchUsd(BigDecimal totalMarchUsd) {
        this.totalMarchUsd = totalMarchUsd;
    }

    public BigDecimal getAprilBs() {
        return aprilBs;
    }

    public void setAprilBs(BigDecimal aprilBs) {
        this.aprilBs = aprilBs;
    }

    public BigDecimal getAprilUsd() {
        return aprilUsd;
    }

    public void setAprilUsd(BigDecimal aprilUsd) {
        this.aprilUsd = aprilUsd;
    }

    public BigDecimal getTotalAprilUsd() {
        return totalAprilUsd;
    }

    public void setTotalAprilUsd(BigDecimal totalAprilUsd) {
        this.totalAprilUsd = totalAprilUsd;
    }

    public BigDecimal getMayBs() {
        return mayBs;
    }

    public void setMayBs(BigDecimal mayBs) {
        this.mayBs = mayBs;
    }

    public BigDecimal getMayUsd() {
        return mayUsd;
    }

    public void setMayUsd(BigDecimal mayUsd) {
        this.mayUsd = mayUsd;
    }

    public BigDecimal getTotalMayUsd() {
        return totalMayUsd;
    }

    public void setTotalMayUsd(BigDecimal totalMayUsd) {
        this.totalMayUsd = totalMayUsd;
    }

    public BigDecimal getJuneBs() {
        return juneBs;
    }

    public void setJuneBs(BigDecimal juneBs) {
        this.juneBs = juneBs;
    }

    public BigDecimal getJuneUsd() {
        return juneUsd;
    }

    public void setJuneUsd(BigDecimal juneUsd) {
        this.juneUsd = juneUsd;
    }

    public BigDecimal getTotalJuneUsd() {
        return totalJuneUsd;
    }

    public void setTotalJuneUsd(BigDecimal totalJuneUsd) {
        this.totalJuneUsd = totalJuneUsd;
    }

    public BigDecimal getJulyBs() {
        return julyBs;
    }

    public void setJulyBs(BigDecimal julyBs) {
        this.julyBs = julyBs;
    }

    public BigDecimal getJulyUsd() {
        return julyUsd;
    }

    public void setJulyUsd(BigDecimal julyUsd) {
        this.julyUsd = julyUsd;
    }

    public BigDecimal getTotalJulyUsd() {
        return totalJulyUsd;
    }

    public void setTotalJulyUsd(BigDecimal totalJulyUsd) {
        this.totalJulyUsd = totalJulyUsd;
    }

    public BigDecimal getAugustBs() {
        return augustBs;
    }

    public void setAugustBs(BigDecimal augustBs) {
        this.augustBs = augustBs;
    }

    public BigDecimal getAugustUsd() {
        return augustUsd;
    }

    public void setAugustUsd(BigDecimal augustUsd) {
        this.augustUsd = augustUsd;
    }

    public BigDecimal getTotalAugustUsd() {
        return totalAugustUsd;
    }

    public void setTotalAugustUsd(BigDecimal totalAugustUsd) {
        this.totalAugustUsd = totalAugustUsd;
    }

    public BigDecimal getSeptemberBs() {
        return septemberBs;
    }

    public void setSeptemberBs(BigDecimal septemberBs) {
        this.septemberBs = septemberBs;
    }

    public BigDecimal getSeptemberUsd() {
        return septemberUsd;
    }

    public void setSeptemberUsd(BigDecimal septemberUsd) {
        this.septemberUsd = septemberUsd;
    }

    public BigDecimal getTotalSeptemberUsd() {
        return totalSeptemberUsd;
    }

    public void setTotalSeptemberUsd(BigDecimal totalSeptemberUsd) {
        this.totalSeptemberUsd = totalSeptemberUsd;
    }

    public BigDecimal getOctoberBs() {
        return octoberBs;
    }

    public void setOctoberBs(BigDecimal octoberBs) {
        this.octoberBs = octoberBs;
    }

    public BigDecimal getOctoberUsd() {
        return octoberUsd;
    }

    public void setOctoberUsd(BigDecimal octoberUsd) {
        this.octoberUsd = octoberUsd;
    }

    public BigDecimal getTotalOctoberUsd() {
        return totalOctoberUsd;
    }

    public void setTotalOctoberUsd(BigDecimal totalOctoberUsd) {
        this.totalOctoberUsd = totalOctoberUsd;
    }

    public BigDecimal getNovemberBs() {
        return novemberBs;
    }

    public void setNovemberBs(BigDecimal novemberBs) {
        this.novemberBs = novemberBs;
    }

    public BigDecimal getNovemberUsd() {
        return novemberUsd;
    }

    public void setNovemberUsd(BigDecimal novemberUsd) {
        this.novemberUsd = novemberUsd;
    }

    public BigDecimal getTotalNovemberUsd() {
        return totalNovemberUsd;
    }

    public void setTotalNovemberUsd(BigDecimal totalNovemberUsd) {
        this.totalNovemberUsd = totalNovemberUsd;
    }

    public BigDecimal getDecemberBs() {
        return decemberBs;
    }

    public void setDecemberBs(BigDecimal decemberBs) {
        this.decemberBs = decemberBs;
    }

    public BigDecimal getDecemberUsd() {
        return decemberUsd;
    }

    public void setDecemberUsd(BigDecimal decemberUsd) {
        this.decemberUsd = decemberUsd;
    }

    public BigDecimal getTotalDecemberUsd() {
        return totalDecemberUsd;
    }

    public void setTotalDecemberUsd(BigDecimal totalDecemberUsd) {
        this.totalDecemberUsd = totalDecemberUsd;
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

    public BigDecimal getMainTotal() {
        return mainTotal;
    }

    public void setMainTotal(BigDecimal mainTotal) {
        this.mainTotal = mainTotal;
    }
}
