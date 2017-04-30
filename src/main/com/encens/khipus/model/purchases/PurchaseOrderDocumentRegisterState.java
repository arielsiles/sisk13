package com.encens.khipus.model.purchases;

/**
 * @author
 * @version 3
 */
public enum PurchaseOrderDocumentRegisterState {
    PENDING("PurchaseOrderDocumentRegisterState.PENDING"),
    COMPLETED("PurchaseOrderDocumentRegisterState.COMPLETED");
    private String resourceKey;

    PurchaseOrderDocumentRegisterState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
