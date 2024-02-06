package com.encens.khipus.model.xproduction;

/**
 * @author
 * @version 2.0
 */
public enum FormulationState {
    APR("Formulation.state.approve"),
    PEN("Formulation.state.pending"),
    ANL("Formulation.state.cancel");
    private String resourceKey;

    FormulationState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
