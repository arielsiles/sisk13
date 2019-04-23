package com.encens.khipus.model.production;

/**
 * @author
 * @version 2.0
 */
public enum ProductionState {
    PEN("Production.state.PEN"),
    EJE("Production.state.EJE"),
    APR("Production.state.APR"),
    FIN("Production.state.FIN"),
    CONTA("Production.state.CONTA"),
    ANL("Production.state.ANL");

    private String resourceKey;

    ProductionState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
