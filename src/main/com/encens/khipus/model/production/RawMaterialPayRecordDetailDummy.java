package com.encens.khipus.model.production;


import java.util.Date;

public class RawMaterialPayRecordDetailDummy {
    private Date date;
    private Double collectedAmount;
    private Double productiveZoneDelta;
    private Double productiveZoneAdjustment;
    private Long totalProducers;
    private Double unitPrice;
    private Double withholding;
    private Double earned;
    private Double grandTotal;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getCollectedAmount() {
        return collectedAmount;
    }

    public void setCollectedAmount(Double collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public Double getProductiveZoneAdjustment() {
        return productiveZoneAdjustment;
    }

    public void setProductiveZoneAdjustment(Double productiveZoneAdjustment) {
        this.productiveZoneAdjustment = productiveZoneAdjustment;
    }

    public Long getTotalProducers() {
        return totalProducers;
    }

    public void setTotalProducers(Long totalProducers) {
        this.totalProducers = totalProducers;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getWithholding() {
        return withholding;
    }

    public void setWithholding(Double withholding) {
        this.withholding = withholding;
    }

    public Double getEarned() {
        return earned;
    }

    public void setEarned(Double earned) {
        this.earned = earned;
    }

    public Double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Double getProductiveZoneDelta() {
        return productiveZoneDelta;
    }

    public void setProductiveZoneDelta(Double productiveZoneDelta) {
        this.productiveZoneDelta = productiveZoneDelta;
    }
}
