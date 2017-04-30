package com.encens.khipus.model.employees;

/**
 * @author
 * @version 3.4
 */
public enum VacationState {
    PENDING("VacationState.PENDING"),
    APPROVED("VacationState.APPROVED"),
    ANNULLED("VacationState.ANNULLED");

    private String resourceKey;

    VacationState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
