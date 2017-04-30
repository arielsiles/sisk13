package com.encens.khipus.util.fixedassets;

/**
 * @author
 * @version 2.29
 */
public enum FixedAssetTransferenceInventoryReportOrder {
    MOVEMENT_DATE("FixedAssetTransferenceInventoryReportOrder.MOVEMENT_DATE", "fixedAssetMovement.movementDate, fixedAssetMovement.id, fixedAsset.barCode"),
    FIXEDASSET("FixedAssetTransferenceInventoryReportOrder.FIXEDASSET", "fixedAsset.barCode, fixedAssetMovement.movementDate, fixedAssetMovement.id");
    private String resourceKey;
    private String order;

    FixedAssetTransferenceInventoryReportOrder(String resourceKey, String order) {
        this.resourceKey = resourceKey;
        this.order = order;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public String getOrder() {
        return order;
    }
}
