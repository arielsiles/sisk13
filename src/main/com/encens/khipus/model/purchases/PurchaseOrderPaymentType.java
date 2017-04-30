package com.encens.khipus.model.purchases;

/**
 * Enumeration of Type of PurchaseOrderPaymentType
 *
 * @author
 * @version 2.24
 */
public enum PurchaseOrderPaymentType {

    PAYMENT_BANK_ACCOUNT(1, "RotatoryFundPaymentType.paymentToBankAccount"),
    PAYMENT_WITH_CHECK(2, "RotatoryFundPaymentType.paymentWithCheck"),
    PAYMENT_CASHBOX(3, "RotatoryFundPaymentType.paymentWithCashBox"),
    PAYMENT_ROTATORY_FUND(4, "RotatoryFundPaymentType.paymentWithRotatoryFund");

    private int code;

    private String resourceKey;

    PurchaseOrderPaymentType(int code, String resourceKey) {
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