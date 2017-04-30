package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.3
 */
public enum BeneficiaryType {
    PERSON("BeneficiaryType.person"),
    ORGANIZATION("BeneficiaryType.organization");
    private String resourceKey;

    BeneficiaryType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
