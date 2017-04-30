package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.
 */
public enum MovementDetailType {
    S("MovementDetail.type.output"),
    E("MovementDetail.type.input");
    private String resourceKey;

    MovementDetailType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
