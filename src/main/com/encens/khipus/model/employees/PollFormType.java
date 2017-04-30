package com.encens.khipus.model.employees;

/**
 * PollFormType
 *
 * @author
 * @version 2.24
 */
public enum PollFormType {
    STUDENT_POLLFORM("PollFormType.STUDENT_POLLFORM"),
    TEACHER_POLLFORM("PollFormType.TEACHER_POLLFORM"),
    CAREERMANAGER_POLLFORM("PollFormType.CAREERMANAGER_POLLFORM"),
    AUTOEVALUATION_POLLFORM("PollFormType.AUTOEVALUATION_POLLFORM");
    private String resourceKey;

    PollFormType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
