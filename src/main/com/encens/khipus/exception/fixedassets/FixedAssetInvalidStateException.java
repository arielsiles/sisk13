package com.encens.khipus.exception.fixedassets;

import com.encens.khipus.model.fixedassets.FixedAsset;

import java.util.List;

/**
 * Thrown when the state of the FixedAsset do not match with the desired state
 * this exception is used only for pre validation purpose
 *
 * @author
 * @version 2.25
 */

public class FixedAssetInvalidStateException extends Exception {
    List<FixedAsset> invalidStateFixedAssetList;

    public FixedAssetInvalidStateException() {
    }

    public FixedAssetInvalidStateException(String message) {
        super(message);
    }

    public FixedAssetInvalidStateException(Throwable cause) {
        super(cause);
    }

    public FixedAssetInvalidStateException(List<FixedAsset> invalidStateFixedAssetList) {
        this.invalidStateFixedAssetList = invalidStateFixedAssetList;
    }

    public List<FixedAsset> getInvalidStateFixedAssetList() {
        return invalidStateFixedAssetList;
    }

    public void setInvalidStateFixedAssetList(List<FixedAsset> invalidStateFixedAssetList) {
        this.invalidStateFixedAssetList = invalidStateFixedAssetList;
    }
}