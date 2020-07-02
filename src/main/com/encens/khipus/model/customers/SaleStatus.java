package com.encens.khipus.model.customers;

public enum SaleStatus {
    PREPARAR("SaleStatus.preparar"),
    ANULADO("SaleStatus.anulado"),
    CONTABILIZADO("SaleStatus.contabilizado");

    private String resourceKey;

    private SaleStatus(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
