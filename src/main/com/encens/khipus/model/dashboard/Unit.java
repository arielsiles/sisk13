package com.encens.khipus.model.dashboard;

/**
 * Enumeration for widget units
 *
 * @author
 * @version 2.26
 */
public enum Unit {
    DAYS("Unit.days"),
    UNITS("Unit.units"),
    PERCENT("Unit.percent");

    private String resourceKey;

    Unit(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
