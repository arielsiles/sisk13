package com.encens.khipus.model.fixedassets;

/**
 * FixedAssetMaintenanceRequestState
 *
 * @author
 * @version 2.25
 */
public enum FixedAssetMaintenanceRequestState {
    PENDING("FixedAssetMaintenanceRequestState.pending"),
    APPROVED("FixedAssetMaintenanceRequestState.approved"),
    REJECTED("FixedAssetMaintenanceRequestState.rejected"),
    FINISHED("FixedAssetMaintenanceRequestState.finished");
    private String resourceKey;

    FixedAssetMaintenanceRequestState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
