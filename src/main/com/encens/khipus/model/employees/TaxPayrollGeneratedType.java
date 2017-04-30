package com.encens.khipus.model.employees;

/**
 * @author
 * @version 2.26
 */
public enum TaxPayrollGeneratedType {
    TRIBUTARY("TaxPayrollGeneratedType.tributary"),
    FISCAL("TaxPayrollGeneratedType.fiscal");

    private String resourceKey;

    TaxPayrollGeneratedType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
