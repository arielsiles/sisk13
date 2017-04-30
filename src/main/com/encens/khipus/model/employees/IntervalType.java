package com.encens.khipus.model.employees;

/**
 * Enum for IntervalType
 *
 * @author
 * @version 3.5.2
 */
public enum IntervalType {
    OVERLAP("IntervalType.OVERLAP"),
    CONTIGUOUS("IntervalType.CONTIGUOUS");


    private String resourceKey;

    IntervalType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
