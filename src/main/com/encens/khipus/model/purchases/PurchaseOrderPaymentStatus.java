package com.encens.khipus.model.purchases;

/**
 * PurchaseOrderPaymentStatus
 *
 * @author
 * @version 2.23
 */
public enum PurchaseOrderPaymentStatus {
    NO_PAYMENT("PurchaseOrderPaymentStatus.NO_PAYMENT"),
    PARTIAL_PAYMENT("PurchaseOrderPaymentStatus.PARTIAL_PAYMENT"),
    FULLY_PAID("PurchaseOrderPaymentStatus.FULLY_PAID");

    private String resourceKey;

    PurchaseOrderPaymentStatus(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
