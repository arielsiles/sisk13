package com.encens.khipus.model.employees;


/**
 * Enumeration for destiny of SpecialDate
 *
 * @author Ariel Siles Encinas
 * @version 1.2.3
 */
public enum SpecialDateTarget {

    BUSINESSUNIT("SpecialDateTarget.businessUnit"),
    ORGANIZATIONALUNIT("SpecialDateTarget.organizationalUnit"),
    EMPLOYEE("SpecialDateTarget.employee");

    private String resourceKey;

    SpecialDateTarget(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}