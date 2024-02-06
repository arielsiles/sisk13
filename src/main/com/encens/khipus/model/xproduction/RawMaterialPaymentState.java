package com.encens.khipus.model.xproduction;

/**
 * @author
 * @version 2.24
 */
public enum RawMaterialPaymentState {
    APPROVED("RawMaterialPaymentState.approved"),
    PENDING("RawMaterialPaymentState.pending"),
    PAY("RawMaterialPaymentState.toPay"),
    NULLIFIED("RawMaterialPaymentState.nullified"),
    LIQUIDATE("RawMaterialPaymentState.liquidate");

    private String resourceKey;

    RawMaterialPaymentState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
