package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.4
 */
public enum SoldProductState {
    PENDING("SoldProduct.state.pending"),
    NULLIFIED("SoldProduct.state.nullified"),
    DELIVERED("SoldProduct.state.delivered");

    String resourceKey;

    SoldProductState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
