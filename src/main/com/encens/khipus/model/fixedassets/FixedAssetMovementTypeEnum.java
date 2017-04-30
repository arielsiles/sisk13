package com.encens.khipus.model.fixedassets;

/**
 * Enum for FixedAssetStateState
 *
 * @author
 * @version 2.0.1
 */
public enum FixedAssetMovementTypeEnum {
    ALT("FixedAssetMovementTypeEnum.registration"),
    BAJ("FixedAssetMovementTypeEnum.cancel"),
    MEJ("FixedAssetMovementTypeEnum.positiveImprovement"),
    TRA("FixedAssetMovementTypeEnum.transference");

    private String resourceKey;

    FixedAssetMovementTypeEnum(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}