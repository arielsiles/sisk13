package com.encens.khipus.util.query;

/**
 * Enum for EntityQueryConditionOperator
 *
 * @author
 * @version 3.4
 */
public enum EntityQueryConditionOperator implements EntityQueryConditionElement {
    AND("EntityQueryConditionOperator.AND"),
    OR("EntityQueryConditionOperator.OR");

    private String resourceKey;

    EntityQueryConditionOperator(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
