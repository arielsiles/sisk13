package com.encens.khipus.model.fixedassets;

/**
 * FixedAssetMaintenanceRequestType
 *
 * @author
 * @version 2.25
 */
public enum FixedAssetMaintenanceRequestType {
    PREVENTIVE("FixedAssetMaintenanceRequestType.preventive"),
    CORRECTIVE("FixedAssetMaintenanceRequestType.corrective");
    private String resourceKey;

    FixedAssetMaintenanceRequestType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
