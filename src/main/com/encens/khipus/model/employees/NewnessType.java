package com.encens.khipus.model.employees;

/**
 * Enum for NewnessType
 *
 * @author
 * @version 3.4
 */
public enum NewnessType {
    I("NewnessType.ingress"),
    R("NewnessType.retirement");

    private String resourceKey;

    NewnessType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
