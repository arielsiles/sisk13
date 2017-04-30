package com.encens.khipus.model.employees;

/**
 * @author
 */
public enum GeneratedPayrollType {

    TEST("GeneratedPayrollType.test"),
    OUTDATED("GeneratedPayrollType.outdated"),
    OFFICIAL("GeneratedPayrollType.official"),;

    private String resourceKey;

    GeneratedPayrollType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}