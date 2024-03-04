package com.encens.khipus.model.xproduction;

public enum XProcessState {

    PEN("XProcessState.state.pending"),
    APR("XProcessState.state.approve"),
    ANL("XProcessState.state.cancel");
    private String resourceKey;

    XProcessState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
