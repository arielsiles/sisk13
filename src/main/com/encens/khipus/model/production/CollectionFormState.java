package com.encens.khipus.model.production;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
public enum CollectionFormState {
    APR("CollectionForm.state.ok"),
    PEN("CollectionForm.state.pendant");

    private String resourceKey;

    CollectionFormState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
