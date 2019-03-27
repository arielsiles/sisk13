package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.
 */
public enum OriginModule {
    OP("OriginModule.product"),
    OC("OriginModule.purchaseOrder"),
    TP("OriginModule.productTransfer");
    private String resourceKey;

    OriginModule(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
