package com.encens.khipus.model.warehouse;

/**
 * @author Ariel Siles Encinas
 * @version 1.2.9
 */

public enum ProductDeliveryType {

    CASH_ORDER("ProductDeliveryType.cashOrder"),
    CASH_SALE("ProductDeliveryType.cashSale");

    private String resourceKey;

    ProductDeliveryType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
