package com.encens.khipus.model.fixedassets;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum for FixedAssetStateState
 *
 * @author
 * @version 2.0
 */
public enum FixedAssetState {
    PEN("FixedAssetState.state.pending"),
    VIG("FixedAssetState.state.valid"),
    BAJ("FixedAssetState.state.cancel"),
    DEP("FixedAssetState.state.depreciated"),
    TDP("FixedAssetState.state.completelyDepreciated");

    private String resourceKey;

    FixedAssetState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static List<FixedAssetState> getValidState() {
        List<FixedAssetState> validStateList = new ArrayList<FixedAssetState>();
        validStateList.add(VIG);
        validStateList.add(BAJ);
        validStateList.add(DEP);
        validStateList.add(TDP);
        return validStateList;
    }

    public static List<FixedAssetState> getMovementState() {
        List<FixedAssetState> validStateList = new ArrayList<FixedAssetState>();
        validStateList.add(VIG);
        validStateList.add(DEP);
        validStateList.add(TDP);
        return validStateList;
    }
}
