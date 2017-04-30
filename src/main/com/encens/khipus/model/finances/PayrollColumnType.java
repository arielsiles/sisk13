package com.encens.khipus.model.finances;

/**
 * Enum for PayrollColumnType
 *
 * @author
 * @version 3.5.2
 */
public enum PayrollColumnType {
    WIN("PayrollColumnType.WIN"),
    OTHER_DISCOUNTS("PayrollColumnType.OTHER_DISCOUNTS");

    private String resourceKey;

    PayrollColumnType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
