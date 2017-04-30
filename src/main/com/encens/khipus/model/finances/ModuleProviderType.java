package com.encens.khipus.model.finances;

/**
 * @author
 * @version 1.0
 */
public enum ModuleProviderType {
    WAREHOUSE("ModuleProviderType.warehouse"),
    FIXEDASSET("ModuleProviderType.fixedAsset");

    private String resourceKey;

    ModuleProviderType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
