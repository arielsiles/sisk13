package com.encens.khipus.model.employees;

/**
 * Enum for DismissalRuleType
 *
 * @author
 * @version 3.4
 */
public enum DismissalRuleType {
    MONTHLY("DismissalRuleType.MONTHLY", "DismissalRuleType.unit.MONTHLY");

    private String resourceKey;
    private String unitResourceKey;

    DismissalRuleType(String resourceKey, String unitResourceKey) {
        this.resourceKey = resourceKey;
        this.unitResourceKey = unitResourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getUnitResourceKey() {
        return unitResourceKey;
    }

    public void setUnitResourceKey(String unitResourceKey) {
        this.unitResourceKey = unitResourceKey;
    }
}
