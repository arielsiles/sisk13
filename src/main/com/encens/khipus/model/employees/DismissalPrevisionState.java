package com.encens.khipus.model.employees;

/**
 * @author
 * @version 4
 */
public enum DismissalPrevisionState {
    PENDING("DismissalDetailState.PENDING"),
    APPROVED("DismissalDetailState.APPROVED"),
    ANNULLED("DismissalDetailState.ANNULLED");
    private String resourceKey;

    DismissalPrevisionState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
