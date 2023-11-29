package com.encens.khipus.model.production;

/**
 * @author
 * @version 2.24
 */
public enum RawMaterialPaymentState {
    APPROVED("PurchaseOrderPaymentType.approved"),
    PENDING("PurchaseOrderPaymentType.pending"),
    NULLIFIED("PurchaseOrderPaymentType.nullified");
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
