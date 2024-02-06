package com.encens.khipus.model.xproduction;

/**
 * @author
 * @version 2.0
 */
public enum RawMaterialDiscountType {

    RETENTION("RawMaterialDiscountType.retention"),
    TAXES("RawMaterialDiscountType.taxes"),
    OTHERS("RawMaterialDiscountType.otherDiscounts");

    private String resourceKey;

    RawMaterialDiscountType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
