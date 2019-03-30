package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.
 */
public enum VoucherOperation {
    OP("VoucherOperation.productOrder"),
    OC("VoucherOperation.purchaseOrder"),
    BA("VoucherOperation.productLow"),
    DE("VoucherOperation.productReturn"),
    TP("VoucherOperation.productTransfer");
    private String resourceKey;

    VoucherOperation(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
