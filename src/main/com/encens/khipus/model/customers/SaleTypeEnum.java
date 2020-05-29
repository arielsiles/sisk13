package com.encens.khipus.model.customers;

public enum SaleTypeEnum {
    CASH("VENTADIRECTA", "SaleType.cashSale"),
    CREDIT("SECUENCIAPEDIDO", "SaleType.creditSale");

    private String sequenceName;
    private String resourceKey;

    SaleTypeEnum(String sequenceName, String resourceKey) {
        this.sequenceName = sequenceName;
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }
}
