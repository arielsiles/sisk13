package com.encens.khipus.model.warehouse;

/**
 * @author
 */
public enum DocumentTypeState {
    VIG("WarehouseDocumentType.state.valid"),
    BLO("WarehouseDocumentType.state.blocked");
    private String resourceKey;

    DocumentTypeState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
