package com.encens.khipus.model.admin;

/**
 * UserRoleGroupingType
 *
 * @author
 * @version 2.26
 */
public enum UserRoleGroupingType {
    GROUPING_BY_USER("UserRoleGroupingType.byUser"),
    GROUPING_BY_ROLE("UserRoleGroupingType.byRole");
    private String resourceKey;

    UserRoleGroupingType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
