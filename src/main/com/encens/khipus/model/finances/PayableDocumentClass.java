package com.encens.khipus.model.finances;

/**
 * @author
 * @version 3.4
 */
public enum PayableDocumentClass {
    FAC("PayableDocumentClass.FAC"),
    PI("PayableDocumentClass.PI"),
    PA("PayableDocumentClass.PA"),
    OTR("PayableDocumentClass.OTR");
    private String resourceKey;

    private PayableDocumentClass(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
