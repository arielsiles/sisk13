package com.encens.khipus.model.budget;

/**
 * This enum contains the classifier types (burden and
 *
 * @author
 * @version 2.0
 */
public enum ClassifierType {
    BURDEN(1, "ClassifierType.burden"),
    ACCOUNTING_ITEM(2, "ClassifierType.accountingItem");
    private int code;
    private String resourceKey;

    ClassifierType(int code, String resourceKey) {
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
