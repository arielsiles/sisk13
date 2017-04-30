package com.encens.khipus.model.employees;

/**
 * Enum for DiscountRuleType
 *
 * @author
 * @version 3.5.2
 */
public enum DiscountRuleType {
    LATENESS("DiscountRuleType.LATENESS"),
    SOLIDARY_AFP("DiscountRuleType.SOLIDARY_AFP");

    private String resourceKey;

    DiscountRuleType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
