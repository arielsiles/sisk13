package com.encens.khipus.model.finances;

/**
 * CostCenterType
 *
 * @author
 * @version 2.5
 */
public enum CostCenterType {
    DIS("CostCenterType.DIS"), NOD("CostCenterType.NOD");

    private String resourceKey;

    CostCenterType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
