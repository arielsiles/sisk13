package com.encens.khipus.util.finances;

/**
 * @author
 * @version 3.2.9
 */
public enum PayableDocumentSourceType {
    HHRR("PayableDocumentSourceType.HHRR", "PayableDocumentSourceType.acronym.HHRR"),
    PURCHASE_ORDER("PayableDocumentSourceType.PURCHASE", "PayableDocumentSourceType.acronym.PURCHASE");
    private String resourceKey;
    private String acronymResourceKey;

    private PayableDocumentSourceType(String resourceKey, String acronymResourceKey) {
        this.resourceKey = resourceKey;
        this.acronymResourceKey = acronymResourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getAcronymResourceKey() {
        return acronymResourceKey;
    }

    public void setAcronymResourceKey(String acronymResourceKey) {
        this.acronymResourceKey = acronymResourceKey;
    }
}
