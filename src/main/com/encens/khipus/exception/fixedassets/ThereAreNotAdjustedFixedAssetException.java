package com.encens.khipus.exception.fixedassets;

/**
 * This exception is thrown when there are Totally Depreciated Fixed Assets without been adjusted
 * this exception can be useful for example in closeActualMonth functionality
 * where all the fixed Assets are supposed to be adjusted due to the exchange rate difference to proceed.
 *
 * @author
 * @version 2.5
 */

public class ThereAreNotAdjustedFixedAssetException extends Exception {
    public ThereAreNotAdjustedFixedAssetException() {
    }

    public ThereAreNotAdjustedFixedAssetException(String message) {
        super(message);
    }

    public ThereAreNotAdjustedFixedAssetException(Throwable cause) {
        super(cause);
    }
}