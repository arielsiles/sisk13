package com.encens.khipus.model.production;

public enum SupplyType {
    INGREDIENT("SupplyType.ingredient"),
    MATERIAL("SupplyType.material"),
    PRODUCT("SupplyType.product");

    private String resourceKey;

    SupplyType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
