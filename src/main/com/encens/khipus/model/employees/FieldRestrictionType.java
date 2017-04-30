package com.encens.khipus.model.employees;

/**
 * Encens Team
 *
 * @author
 * @version : FieldRestrictionType, 04-12-2009 04:12:34 PM
 */
public enum FieldRestrictionType {
    NOTVISIBLE("FieldRestrictionType.notVisible"),
    VISIBLENOTREQUIRED("FieldRestrictionType.visibleAndNotRequired"),
    VISIBLEREQUIRED("FieldRestrictionType.visibleAndRequired");
    private String resourceKey;

    FieldRestrictionType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
