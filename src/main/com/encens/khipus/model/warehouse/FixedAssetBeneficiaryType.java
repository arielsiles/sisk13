package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.5
 */
public enum FixedAssetBeneficiaryType {
    PERSON("FixedAssetBeneficiaryType.person"),
    ORGANIZATION("FixedAssetBeneficiaryType.organization");
    private String resourceKey;

    FixedAssetBeneficiaryType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}