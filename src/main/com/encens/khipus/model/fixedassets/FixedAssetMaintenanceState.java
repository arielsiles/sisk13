package com.encens.khipus.model.fixedassets;

/**
 * FixedAssetMaintenanceState
 *
 * @author
 * @version 2.25
 */
public enum FixedAssetMaintenanceState {
    IN_PROGRESS("FixedAssetMaintenanceState.inProgress"),
    COMPLETED("FixedAssetMaintenanceState.completed");
    private String resourceKey;

    FixedAssetMaintenanceState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
