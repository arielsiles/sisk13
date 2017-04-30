package com.encens.khipus.model.fixedassets;

/**
 * @author
 * @version 3.0
 */
public enum FixedAssetMaintenanceReceiptType {
    SUCCESS("FixedAssetMaintenanceReceiptType.SUCCESS"),
    UNSUCCESS("FixedAssetMaintenanceReceiptType.UNSUCCESS");
    private String resourceKey;

    FixedAssetMaintenanceReceiptType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
