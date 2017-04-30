package com.encens.khipus.model.employees;

/**
 * Encens Team
 *
 * @author
 * @version : WorkExperienceType, 26-11-2009 08:08:52 PM
 */
public enum ExperienceType {
    LABORAL("ExperienceType.laboral"),
    PROFESSOR("ExperienceType.professor");

    private String resourceKey;

    ExperienceType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
