package com.encens.khipus.model.employees;

/**
 * Encens Team
 *
 * @author
 * @version : PollFormGroupType, 07-12-2009 11:52:24 AM
 */
public enum PollFormGrouppingType {
    FACULTY("PollFormGrouppingType.faculty"),
    CAREER("PollFormGrouppingType.career"),
    SUBJECT("PollFormGrouppingType.subject");
    private String resourceKey;

    PollFormGrouppingType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
