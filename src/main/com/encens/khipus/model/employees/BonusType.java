package com.encens.khipus.model.employees;

/**
 * @author
 * @version 2.26
 */
public enum BonusType {
    SUNDAYS_BONUS("BonusType.sundaysBonus"),
    SENIORITY_BONUS("BonusType.seniorityBonus"),
    PRODUCTION_BONUS("BonusType.productionBonus"),
    NIGHTWORK_BONUS("BonusType.nightWork"),
    TRANSRETURN_BONUS("BonusType.transportationReturn"),
    REFRESHMENT_BONUS("BonusType.refreshment"),
    REGULAR_BONUS("BonusType.regularBonus");

    private String resourceKey;

    BonusType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
