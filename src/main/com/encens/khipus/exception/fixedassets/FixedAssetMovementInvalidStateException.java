package com.encens.khipus.exception.fixedassets;

import com.encens.khipus.model.fixedassets.FixedAssetMovement;

import java.util.List;

/**
 * Thrown when the state of the FixedAssetMovement state do not match with the desired state
 * this exception is used only for pre validation purpose
 *
 * @author
 * @version 2.25
 */

public class FixedAssetMovementInvalidStateException extends Exception {
    List<FixedAssetMovement> invalidStateFixedAssetMovementList;

    public FixedAssetMovementInvalidStateException() {
    }

    public FixedAssetMovementInvalidStateException(String message) {
        super(message);
    }

    public FixedAssetMovementInvalidStateException(Throwable cause) {
        super(cause);
    }

    public FixedAssetMovementInvalidStateException(List<FixedAssetMovement> invalidStateFixedAssetMovementList) {
        this.invalidStateFixedAssetMovementList = invalidStateFixedAssetMovementList;
    }

    public List<FixedAssetMovement> getInvalidStateFixedAssetMovementList() {
        return invalidStateFixedAssetMovementList;
    }

    public void setInvalidStateFixedAssetMovementList(List<FixedAssetMovement> invalidStateFixedAssetMovementList) {
        this.invalidStateFixedAssetMovementList = invalidStateFixedAssetMovementList;
    }
}