package com.encens.khipus.model.customers;

/**
 * CustomerCategoryType
 *
 * @author
 * @version
 */
public enum CustomerCategoryType {
    FACTORY("CustomerCategoryType.factory"),
    STORE("CustomerCategoryType.store"),
    CONSUMER("CustomerCategoryType.consumer"),

    FACTORY_LIST("CustomerCategoryType.fix.factory"),
    AGENCY_LIST("CustomerCategoryType.fix.agency"),
    VET_LIST("CustomerCategoryType.fix.vet");

    private String resourceKey;

    CustomerCategoryType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
