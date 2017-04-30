package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 3.0
 */
public enum WarehouseDocumentTypeFieldRestriction {
    CONTRA_ACCOUNT_DEFINED_BY_PRODUCT_ITEM("WarehouseDocumentTypeFieldRestriction.CONTRA_ACCOUNT_DEFINED_BY_PRODUCT_ITEM"),
    CONTRA_ACCOUNT_DEFINED_BY_DEFAULT("WarehouseDocumentTypeFieldRestriction.CONTRA_ACCOUNT_DEFINED_BY_DEFAULT"),
    CONTRA_ACCOUNT_DEFINED_BY_USER("WarehouseDocumentTypeFieldRestriction.CONTRA_ACCOUNT_DEFINED_BY_USER");

    private String resourceKey;

    WarehouseDocumentTypeFieldRestriction(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}