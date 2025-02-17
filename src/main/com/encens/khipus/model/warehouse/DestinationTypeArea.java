package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.3
 */
public enum DestinationTypeArea {
    PRODUCTION("WarehouseVoucher.destinationArea.production"),
    MAINTENANCE("WarehouseVoucher.destinationArea.maintenance"),
    OTHER("WarehouseVoucher.destinationArea.other");
    private String resourceKey;

    DestinationTypeArea(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
