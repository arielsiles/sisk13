package com.encens.khipus.model.employees;

/**
 * Enum for GestionPayrollType
 * PAYMENT= monthly payment payroll
 * CHRISTMAS_BONUS= christmasBonus payroll
 *
 * @author
 * @version 3.2
 */
public enum GestionPayrollType {
    SALARY("GestionPayrollType.salary", "GestionPayrollType.singular.salary", "GestionPayrollType.plural.salary"),
    CHRISTMAS_BONUS("GestionPayrollType.christmasBonus", "GestionPayrollType.singular.christmasBonus", "GestionPayrollType.plural.christmasBonus");

    private String resourceKey;
    private String singularResourceKey;
    private String pluralResourceKey;

    GestionPayrollType(String resourceKey, String singularResourceKey, String pluralResourceKey) {
        this.resourceKey = resourceKey;
        this.singularResourceKey = singularResourceKey;
        this.pluralResourceKey = pluralResourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getSingularResourceKey() {
        return singularResourceKey;
    }

    public void setSingularResourceKey(String singularResourceKey) {
        this.singularResourceKey = singularResourceKey;
    }

    public String getPluralResourceKey() {
        return pluralResourceKey;
    }

    public void setPluralResourceKey(String pluralResourceKey) {
        this.pluralResourceKey = pluralResourceKey;
    }
}
