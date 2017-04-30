package com.encens.khipus.model.accounting;

/**
 * Enum for SaleType
 *
 * @author
 * @version
 */
public enum SaleType {

    CASH_SALE("Product.cashSale"),
    CREDIT_SALE("Product.creditSale");

    private String resourceKey;

    SaleType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
