package com.encens.khipus.model.warehouse;

/**
 * @author
 * @version 2.0
 */
public enum SubGroupState {
    VIG("SubGroup.state.valid"),
    BLO("SubGroup.state.blocked");
    private String resourceKey;

    SubGroupState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
