package com.encens.khipus.model.employees;

/**
 * ScheduleEvaluationState
 *
 * @author
 * @version 2.24
 */
public enum ScheduleEvaluationState {
    ENABLED("ScheduleEvaluationState.ENABLED"),
    DISABLED("ScheduleEvaluationState.DISABLED");
    private String resourceKey;

    ScheduleEvaluationState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
