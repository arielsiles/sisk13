package com.encens.khipus.model.purchases;

/**
 * @author
 * @version 2.24
 */
public enum PurchaseOrderPaymentState {
    APPROVED("PurchaseOrderPaymentType.approved"),
    PENDING("PurchaseOrderPaymentType.pending"),
    NULLIFIED("PurchaseOrderPaymentType.nullified");
    private String resourceKey;

    PurchaseOrderPaymentState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
