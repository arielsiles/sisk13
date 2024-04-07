package com.encens.khipus.model.finances;

public enum ExpenseType {
    ADMINISTRATIVE("WarehouseVoucher.expenseType.administrative"),
    PRODUCTION("WarehouseVoucher.expenseType.production");

    private String resourceKey;

    ExpenseType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
