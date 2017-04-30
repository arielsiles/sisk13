package com.encens.khipus.model.finances;

/**
 * Enumeration of Type of CollectionDocumentType
 *
 * @author
 * @version 2.23
 */
public enum CollectionDocumentType {

    INVOICE(1, "CollectionDocumentType.invoice"),
    RECEIPT(2, "CollectionDocumentType.receipt"),
    ADJUSTMENT(3, "CollectionDocumentType.adjustment");
    private int code;

    private String resourceKey;

    CollectionDocumentType(int code, String resourceKey) {
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

    public static CollectionDocumentType[] valuesForMovements() {
        return new CollectionDocumentType[]{INVOICE, RECEIPT};
    }
}