package com.encens.khipus.model.production;

/**
 * Enumeration of Type of PurchaseOrderPaymentType
 *
 * @author
 * @version 2.24
 */
public enum RawMaterialPaymentType {

    PAYMENT_CASHBOX("RotatoryFundPaymentType.paymentWithCashBox"),
    PAYMENT_BANK_ACCOUNT("RotatoryFundPaymentType.paymentToBankAccount"),
    PAYMENT_WITH_CHECK("RotatoryFundPaymentType.paymentWithCheck");

    private String resourceKey;

    RawMaterialPaymentType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}