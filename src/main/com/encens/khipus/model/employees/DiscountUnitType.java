package com.encens.khipus.model.employees;

/**
 * @author
 * @version 3.4
 */
public enum DiscountUnitType {
    CURRENCY("DiscountUnitType.currency"),
    PERCENT("DiscountUnitType.percent");

    private String resourceKey;

    DiscountUnitType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
