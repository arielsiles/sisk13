package com.encens.khipus.model.employees;

/**
 * Enum for DismissalState
 *
 * @author
 * @version 3.4
 */
public enum DismissalState {
    PENDING("DismissalState.PENDING"),
    OPEN("DismissalState.OPEN"),
    CLOSE("DismissalState.CLOSE");
    private String resourceKey;

    DismissalState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
