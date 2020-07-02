package com.encens.khipus.model.customers;

/**
 * Enumeration for the state of Bank
 *
 * @author:
 */

public enum SubsidyEnun {
    SPYL("CustomerOrder.subsidy.prenatalLactation", "S. PRENATAL Y L."),
    SUNI("CustomerOrder.subsidy.universal", "S. UNIVERSAL");

    private String resourceKey;
    private String subsidyType;

    SubsidyEnun(String resourceKey, String subsidyType) {
        this.resourceKey = resourceKey;
        this.subsidyType = subsidyType;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getSubsidyType() {
        return subsidyType;
    }

    public void setSubsidyType(String subsidyType) {
        this.subsidyType = subsidyType;
    }
}
