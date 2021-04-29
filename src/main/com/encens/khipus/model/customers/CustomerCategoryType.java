package com.encens.khipus.model.customers;

import java.util.ArrayList;
import java.util.List;

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

    public List<CustomerCategoryType> fixedCategoryTypes(){
        List<CustomerCategoryType> resulTypes = new ArrayList<CustomerCategoryType>(0);
        resulTypes.add(CustomerCategoryType.FACTORY);
        resulTypes.add(CustomerCategoryType.STORE);
        resulTypes.add(CustomerCategoryType.CONSUMER);

        return resulTypes;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
