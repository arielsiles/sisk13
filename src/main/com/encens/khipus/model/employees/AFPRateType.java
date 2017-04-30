package com.encens.khipus.model.employees;

/**
 * @author
 * @version 2.26
 */
public enum AFPRateType {
    LABOR_CONTRIBUTION("AFPRateType.laborContribution"),
    PATRONAL_CONTRIBUTION_PROFESSIONAL_RISKS("AFPRateType.patronalContributionProfessionalRisks"),
    PATRONAL_CONTRIBUTION_PRO_HOUSING("AFPRateType.patronalContributionProHousing"),
    PATRONAL_CONTRIBUTION_SOLIDARY("AFPRateType.solidaryPatronalContribution");

    private String resourceKey;

    AFPRateType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
