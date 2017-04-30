package com.encens.khipus.model.fixedassets;

/**
 * @author
 * @version 3.2
 */
public enum PurchaseOrderCauseType {
    FIXEDASSET_PURCHASE("PurchaseOrderCauseType.FIXEDASSET_PURCHASE"),
    FIXEDASSET_PARTS_PURCHASE("PurchaseOrderCauseType.FIXEDASSET_PARTS_PURCHASE");
    private String resourceKey;

    PurchaseOrderCauseType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
