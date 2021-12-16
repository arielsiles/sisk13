package com.encens.khipus.model.customers;

public enum SaleStatus {
    PENDIENTE("SaleStatus.pendiente", "CustomerOrder.state.PEN"),
    PREPARAR("SaleStatus.preparar", "CustomerOrder.state.PRE"),
    ANULADO("SaleStatus.anulado", "CustomerOrder.state.ANL"),
    CONTABILIZADO("SaleStatus.contabilizado", "CustomerOrder.state.CONTA");

    private String resourceKey;
    private String status;

    SaleStatus(String resourceKey, String status) {
        this.resourceKey = resourceKey;
        this.status = status;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
