package com.encens.khipus.model.finances;

/**
 * @author
 * @version 3.4
 */
public enum PayableDocumentTypeState {
    VIG("PayableDocumentTypeState.VIG"),
    BLO("PayableDocumentTypeState.BLO");

    private String resourceKey;

    PayableDocumentTypeState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
