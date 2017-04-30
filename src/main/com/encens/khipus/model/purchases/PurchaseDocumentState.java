package com.encens.khipus.model.purchases;

/**
 * @author
 * @version 2.25
 */
public enum PurchaseDocumentState {
    PENDING("PurchaseDocumentState.pending"),
    APPROVED("PurchaseDocumentState.approved"),
    NULLIFIED("PurchaseDocumentState.nullified");

    private String resourceKey;

    PurchaseDocumentState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
