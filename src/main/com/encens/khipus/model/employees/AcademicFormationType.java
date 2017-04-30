package com.encens.khipus.model.employees;

/**
 * Encens Team
 *
 * @author
 * @version : AcademicFormationType, 26-11-2009 07:56:47 PM
 */
public enum AcademicFormationType {

    UNDERGRADUATE("AcademicFormationType.undergraduate"),
    SPECIALTY("AcademicFormationType.specialty"),
    DIPLOMAED("AcademicFormationType.diplomaed"),
    MASTER("AcademicFormationType.Masters"),
    PHD("AcademicFormationType.PhD"),
    OTHER("AcademicFormationType.other");

    private String resourceKey;

    AcademicFormationType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
