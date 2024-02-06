package com.encens.khipus.model.xproduction;

/**
 * @author
 * @version 2.0
 */
public enum ProductionPlanState {
    PEN("ProductionPlan.state.PEN"),
    APR("ProductionPlan.state.APR"),
    FIN("ProductionPlan.state.FIN"),
    SUS("ProductionPlan.state.SUS");

    private String resourceKey;

    ProductionPlanState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
