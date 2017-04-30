package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.0
 */
public enum ProductItemState {
    VIG("ProductItem.state.valid"),
    BLO("ProductItem.state.blocked");

    private String resourceKey;

    ProductItemState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
