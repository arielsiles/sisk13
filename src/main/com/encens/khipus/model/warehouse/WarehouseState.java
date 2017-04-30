package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.0
 */
public enum WarehouseState {
    VIG("Warehouse.state.valid"),
    BLO("Warehouse.state.blocked");
    private String resourceKey;

    WarehouseState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
