package com.encens.khipus.model.finances;

/**
 * Enumeration of Kind of Payment either Advance or Liquidation payment
 *
 * @author
 * @version 2.24
 */
public enum PurchaseOrderPaymentKind {

    ADVANCE_PAYMENT(1, "PurchaseOrderPaymentKind.advancePayment"),
    LIQUIDATION_PAYMENT(2, "PurchaseOrderPaymentKind.liquidationPayment");

    private int code;

    private String resourceKey;

    PurchaseOrderPaymentKind(int code, String resourceKey) {
        this.code = code;
        this.resourceKey = resourceKey;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}