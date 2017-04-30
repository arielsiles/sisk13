package com.encens.khipus.model.fixedassets;

/**
 * @author
 * @version 2.26
 */
public enum FixedAssetPaymentState {
    APPROVED("FixedAssetPaymentType.approved"),
    PENDING("FixedAssetPaymentType.pending"),
    NULLIFIED("FixedAssetPaymentType.nullified");
    private String resourceKey;

    FixedAssetPaymentState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}