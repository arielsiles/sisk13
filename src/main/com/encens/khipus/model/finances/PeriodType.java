package com.encens.khipus.model.finances;

/**
 * @author
 * @version 2.14
 */
public enum PeriodType {
    DAY("PeriodType.day"),
    MONTH("PeriodType.month");
    private String resourceKey;

    PeriodType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}