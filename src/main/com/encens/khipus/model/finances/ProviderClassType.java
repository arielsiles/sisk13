package com.encens.khipus.model.finances;

/**
 * @author
 * @version 3.2
 */
public enum ProviderClassType {
    PEX("ProviderClassType.PEX"),
    PIN("ProviderClassType.PIN"),
    FF("ProviderClassType.FF"),
    CC("ProviderClassType.CC"),
    INT("ProviderClassType.INT");
    private String resourceKey;

    ProviderClassType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
