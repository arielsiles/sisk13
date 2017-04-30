package com.encens.khipus.model.finances;

/**
 * Enumeration of Type of RotatoryFundPaymentType
 *
 * @author
 * @version 2.24
 */
public enum RotatoryFundPaymentType {

    PAYMENT_BANK_ACCOUNT(1, "RotatoryFundPaymentType.paymentToBankAccount"),
    PAYMENT_WITH_CHECK(2, "RotatoryFundPaymentType.paymentWithCheck"),
    PAYMENT_CASHBOX(3, "RotatoryFundPaymentType.paymentWithCashBox"),
    PAYMENT_CASH_ACCOUNT_ADJ(4, "RotatoryFundPaymentType.paymentCashAccountAdjustment");

    private int code;

    private String resourceKey;

    RotatoryFundPaymentType(int code, String resourceKey) {
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