package com.encens.khipus.exception.finances;

import com.encens.khipus.model.finances.RotatoryFundCollectionState;

/**
 * @author
 * @version 2.23
 */

public class RotatoryFundCollectionStateException extends Exception {
    private RotatoryFundCollectionState actualState;

    public RotatoryFundCollectionStateException(RotatoryFundCollectionState actualState) {
        this.actualState = actualState;
    }

    public RotatoryFundCollectionStateException(String message, RotatoryFundCollectionState actualState) {
        super(message);
        this.actualState = actualState;
    }

    public RotatoryFundCollectionStateException(String message, Throwable cause, RotatoryFundCollectionState actualState) {
        super(message, cause);
        this.actualState = actualState;
    }

    public RotatoryFundCollectionStateException(Throwable cause, RotatoryFundCollectionState actualState) {
        super(cause);
        this.actualState = actualState;
    }

    public RotatoryFundCollectionState getActualState() {
        return actualState;
    }
}