package com.encens.khipus.dashboard.util;

/**
 * @author
 * @version 3.3
 */
public enum SemaphoreState {
    RED("Common.color.red"),
    YELLOW("Common.color.yellow"),
    GREEN("Common.color.green"),;

    private String resourceKey;

    SemaphoreState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
