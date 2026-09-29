package com.encens.khipus.model.employees;

/**
 * @author
 * @version 6.1.0
 */
public enum MarkImportBatchState {

    CONFIRMED("MarkImportBatchState.confirmed"),
    ANNULLED("MarkImportBatchState.annulled");

    private String resourceKey;

    MarkImportBatchState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
