package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.0
 */
public enum WarehouseType {

    INPUTS("WarehouseType.productionInputs"),
    DAIRY("WarehouseType.dairyProducts"),
    MATERIAL("WarehouseType.productionMaterial"),
    SERVICE("WarehouseType.materialService"),
    VETERINARY("WarehouseType.veterinaryProducts"),
    AUXILIARY("WarehouseType.auxiliaryWarehouse"),
    REPLACEMENT("WarehouseType.replacementWarehouse"),
    AGENCY("WarehouseType.dairyAgency");

    private String resourceKey;

    WarehouseType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
