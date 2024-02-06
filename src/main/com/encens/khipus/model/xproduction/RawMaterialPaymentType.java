package com.encens.khipus.model.xproduction;

/**
 * Enumeration of Type of RawMaterialPaymentType
 *
 * @author
 * @version 2.24
 */
public enum RawMaterialPaymentType {

    PAYMENT_CASHBOX("RotatoryFundPaymentType.paymentWithCashBox"),
    PAYMENT_BANK_ACCOUNT("RotatoryFundPaymentType.paymentToBankAccount");

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