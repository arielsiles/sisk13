package com.encens.khipus.model.production;

/**
 * Enum for FixedAssetStateState
 *
 * @author
 * @version 2.0.1
 */
public enum SalaryMovementProducerTypeEnum {
    TACH("SalaryMovementProducer.tachos"),
    CONC("SalaryMovementProducer.concentrated"),
    LACT("SalaryMovementProducer.dairyProducts"),
    VETE("SalaryMovementProducer.veterinary"),
    OEGR("SalaryMovementProducer.otherDiscount"),
    OING("SalaryMovementProducer.otherIncoming"),
    CBAN("SalaryMovementProducer.bankCommission"),
    CRED("SalaryMovementProducer.credit");

    private String resourceKey;

    SalaryMovementProducerTypeEnum(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}