package com.encens.khipus.model.employees;

/**
 * @author Ariel Siles
 */

public enum SpecialDateType {

    PAID("SpecialDateType.paid"),
    UNPAID("SpecialDateType.unpaid");

    private String resourceKey;


    SpecialDateType(String resourceKey) {
        this.setResourceKey(resourceKey);
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
