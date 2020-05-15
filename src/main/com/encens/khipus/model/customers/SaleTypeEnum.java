package com.encens.khipus.model.customers;

public enum SaleTypeEnum {
    CASH("SaleType.cashSale"),
    CREDIT("SaleType.creditSale");

    private String resourceKey;

    SaleTypeEnum(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
