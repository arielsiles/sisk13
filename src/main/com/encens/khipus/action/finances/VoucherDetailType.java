package com.encens.khipus.action.finances;

/**
 * Enum for VoucherDetailType
 *
 * @author
 * @version 0.3
 */
public enum VoucherDetailType {
    DEBIT("VoucherDetailType.DEBIT"),
    CREDIT("VoucherDetailType.CREDIT");

    private String resourceKey;

    VoucherDetailType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
