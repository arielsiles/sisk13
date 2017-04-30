package com.encens.khipus.model.employees;

/**
 * Enum for MarkStateType
 * PENDING when the employee can still register more valid marks for the band
 * MISSING when there weren't two valid marks (income and outcome)
 * LATE when there were two valid marks but they register lateness
 * ON_TIME when there were two valid marks but they don't register lateness
 *
 * @author
 * @version 3.0
 */
public enum HoraryBandStateType {
    PENDING("HoraryBandStateType.PENDING"),
    MISSING("HoraryBandStateType.MISSING"),
    LATE("HoraryBandStateType.LATE"),
    ON_TIME("HoraryBandStateType.ON_TIME");
    private String resourceKey;

    HoraryBandStateType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
