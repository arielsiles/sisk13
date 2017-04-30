package com.encens.khipus.model.finances;

/**
 * CostCenterState
 *
 * @author
 * @version 2.5
 */
public enum CostCenterState {
    VIG("CostCenterState.VIG"), BLO("CostCenterState.BLO");

    private String resourceKey;

    CostCenterState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
