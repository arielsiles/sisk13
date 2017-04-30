package com.encens.khipus.model.finances;

/**
 * Enumeration of Type of RotatoryFundCollection
 *
 * @author
 * @version 2.24
 */
public enum RotatoryFundCollectionType {

    COLLECTION_BANK_ACCOUNT(1, "RotatoryFundCollectionType.collectionToBankAccount"),
    COLLECTION_CASHBOX(2, "RotatoryFundCollectionType.collectionWithCashBox"),
    COLLECTION_WITH_DOCUMENT(3, "RotatoryFundCollectionType.collectionWithDocument"),
    COLLECTION_BY_PAYROLL(4, "RotatoryFundCollectionType.collectionByPayroll"),
    COLLECTION_BY_PURCHASE_ORDER(5, "RotatoryFundCollectionType.collectionByPurchaseOrder"),
    COLLECTION_CASH_ACCOUNT_ADJ(6, "RotatoryFundCollectionType.collectionCashAccountAdjustment"),
    COLLECTION_DEPOSIT_ADJ(7, "RotatoryFundCollectionType.collectionDepositAdjustment");
    private int code;

    private String resourceKey;

    RotatoryFundCollectionType(int code, String resourceKey) {
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