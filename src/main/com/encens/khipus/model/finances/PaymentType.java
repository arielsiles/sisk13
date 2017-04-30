package com.encens.khipus.model.finances;

/**
 * Enumeration of Type of Payment
 *
 * @author
 * @version 1.1.10
 */
public enum PaymentType {

    PAYMENT_BANK_ACCOUNT(1, "PaymentType.paymentToBankAccount"),
    PAYMENT_WITH_CHECK(2, "PaymentType.paymentWithCheck");

    private int code;

    private String resourceKey;

    PaymentType(int code, String resourceKey) {
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
