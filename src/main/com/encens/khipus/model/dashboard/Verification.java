package com.encens.khipus.model.dashboard;

/**
 * Enumeration for widget verification types
 *
 * @author
 * @version 2.26
 */
public enum Verification {
    MONTH_END("Verification.monthEnd"),
    ON_LINE("Verification.onLine");

    private String resourceKey;

    Verification(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
