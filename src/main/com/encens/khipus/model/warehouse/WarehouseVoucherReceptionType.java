package com.encens.khipus.model.warehouse;

/**
 * Enum for WarehouseVoucherReceptionType
 *
 * @author
 * @version 3.0
 */
public enum WarehouseVoucherReceptionType {
    RP("WarehouseVoucherReceptionType.RP"),
    RT("WarehouseVoucherReceptionType.RT");

    private String resourceKey;

    WarehouseVoucherReceptionType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
