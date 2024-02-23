package com.encens.khipus.model.purchases;

/**
 * Enumeration of Type of PurchaseOrderPaymentType
 *
 * @author
 * @version 2.24
 */
public enum PayConditionsType {

    CASH("PurchaseOrder.payConditions.cash"),
    CREDIT("PurchaseOrder.payConditions.credit");

    private String resourceKey;

    PayConditionsType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}