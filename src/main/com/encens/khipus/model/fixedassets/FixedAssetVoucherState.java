package com.encens.khipus.model.fixedassets;

/**
 * Enum for FixedAssetVoucherState
 *
 * @author
 * @version 2.24
 */
public enum FixedAssetVoucherState {
    PEN("FixedAssetVoucherState.PEN"),
    APR("FixedAssetVoucherState.APR"),
    ANL("FixedAssetVoucherState.ANL");

    private String resourceKey;

    FixedAssetVoucherState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}