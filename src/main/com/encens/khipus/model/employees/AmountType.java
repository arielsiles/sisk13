package com.encens.khipus.model.employees;

/**
 * Enum for AmountType
 *
 * @author
 * @version 3.4
 */
public enum AmountType {
    FIXED("AmountType.FIXED"),
    PERCENT("AmountType.PERCENT");

    private String resourceKey;

    AmountType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
