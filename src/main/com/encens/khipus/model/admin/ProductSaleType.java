package com.encens.khipus.model.admin;

/**
 * Enumeration for Permission types
 *
 * @author:
 */

public enum ProductSaleType {

    DAIRY_PRODUCT("Product.dairyProduct"),
    VETERINARY_PRODUCT("Product.veterinaryProduct");

    private String resourceKey;

    ProductSaleType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
