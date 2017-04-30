package com.encens.khipus.model.employees;

/**
 * Enum for PostulantType
 *
 * @author
 * @version 3.4
 */
public enum PostulantType {
    PROFESSOR("PostulantType.PROFESSOR"),
    MANAGER("PostulantType.MANAGER");

    private String resourceKey;

    PostulantType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
