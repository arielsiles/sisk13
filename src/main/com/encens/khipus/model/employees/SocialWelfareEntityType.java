package com.encens.khipus.model.employees;

/**
 * @author
 * @version 3.5
 */
public enum SocialWelfareEntityType {
    PENSION_FUND("SocialWelfareEntityType.PENSION_FUND"),
    SOCIAL_SECURITY("SocialWelfareEntityType.SOCIAL_SECURITY");
    private String resourceKey;

    private SocialWelfareEntityType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
