package com.encens.khipus.model.employees;

/**
 * FinalEvaluationFormType
 *
 * @author
 * @version 2.7
 */
public enum FinalEvaluationFormType {
    CAREER_BOSS("FinalEvaluationFormType.careerBoss"),
    PROFESSOR("FinalEvaluationFormType.professor");
    private String resourceKey;

    FinalEvaluationFormType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
