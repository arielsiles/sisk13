package com.encens.khipus.exception.fixedassets;

/**
 * This exception is thrown when there are Actual Fixed Assets
 * this exception can be useful for example in closeActualMonth functionality
 * where all the fixed Assets are supposed to be in depreciated state to proceed.
 *
 * @author
 * @version 2.5
 */

public class ThereAreActualFixedAssetException extends Exception {
    public ThereAreActualFixedAssetException() {
    }

    public ThereAreActualFixedAssetException(String message) {
        super(message);
    }

    public ThereAreActualFixedAssetException(Throwable cause) {
        super(cause);
    }
}