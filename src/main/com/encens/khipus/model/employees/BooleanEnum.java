package com.encens.khipus.model.employees;

/**
 * Enum for BooleanEnum
 *
 * @author
 * @version 3.4
 */
public enum BooleanEnum {
    TRUE("BooleanEnum.TRUE", Boolean.TRUE),
    FALSE("BooleanEnum.FALSE", Boolean.FALSE);

    private String resourceKey;
    private Boolean value;

    BooleanEnum(String resourceKey, Boolean value) {
        this.resourceKey = resourceKey;
        this.value = value;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
