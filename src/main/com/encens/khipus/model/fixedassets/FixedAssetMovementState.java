package com.encens.khipus.model.fixedassets;

/**
 * Enum for FixedAssetStateState
 *
 * @author
 * @version 2.26
 */
public enum FixedAssetMovementState {
    PEN("FixedAssetMovementState.pendant"),
    APR("FixedAssetMovementState.approved"),
    ANL("FixedAssetMovementState.annulled");

    private String resourceKey;

    FixedAssetMovementState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}