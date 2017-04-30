package com.encens.khipus.model.customers;

/**
 * Encens Team
 *
 * @author
 * @version : DiscountRuleState, 09-10-2009 04:19:14 PM
 */
public enum DiscountRuleState {
    ACTIVE("DiscountRuleState.active"),
    INACTIVE("DiscountRuleState.inactive");

    private String resourceKey;

    DiscountRuleState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
