package com.encens.khipus.model.employees;

/**
 * Enum for MarkStateType
 * PENDING when the mark has been associated to an HoraryBand but not have been processed yet
 * LATE when the mark has been processed and have lateness
 * ON_TIME when the mark has been processed and haven't lateness
 *
 * @author
 * @version 3.0
 */
public enum MarkStateType {
    PENDING("MarkStateType.PENDING"),
    LATE("MarkStateType.LATE"),
    ON_TIME("MarkStateType.ON_TIME");

    private String resourceKey;

    MarkStateType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
