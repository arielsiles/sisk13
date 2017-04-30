package com.encens.khipus.model.employees;

/**
 * Enum for
 *
 * @author
 * @version 3.4
 */
public enum DismissalDetailState {
    PENDING("DismissalDetailState.PENDING"),
    APPROVED("DismissalDetailState.APPROVED"),
    ANNULLED("DismissalDetailState.ANNULLED");
    private String resourceKey;

    DismissalDetailState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
