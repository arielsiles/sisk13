package com.encens.khipus.model.fixedassets;

/**
 * Enum for FixedAssetStateState
 *
 * @author
 * @version 2.3
 */
public enum FixedAssetMovementTypeState {
    VIG("FixedAssetMovementTypeState.actual"),
    BLO("FixedAssetMovementTypeState.blocked");

    private String resourceKey;

    FixedAssetMovementTypeState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}