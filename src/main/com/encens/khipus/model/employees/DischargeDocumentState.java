package com.encens.khipus.model.employees;

/**
 * @author
 * @version 2.26
 */
public enum DischargeDocumentState {
    PENDING("DischargeDocumentState.pending"),
    APPROVED("DischargeDocumentState.approved"),
    NULLIFIED("DischargeDocumentState.nullified");

    private String resourceKey;

    DischargeDocumentState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
