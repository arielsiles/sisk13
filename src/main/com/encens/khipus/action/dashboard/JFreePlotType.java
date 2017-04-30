package com.encens.khipus.action.dashboard;

/**
 * Enum for JFreePlotType
 *
 * @author
 * @version 3.2
 */
public enum JFreePlotType {
    PIE("JFreePlotType.PIE"),
    METER("JFreePlotType.METER");

    private String resourceKey;

    JFreePlotType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
