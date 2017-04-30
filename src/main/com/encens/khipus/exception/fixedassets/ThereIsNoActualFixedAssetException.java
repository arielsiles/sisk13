package com.encens.khipus.exception.fixedassets;

/**
 * This exception is thrown when there is no Actual Fixed Assets
 * this exception can be useful for example in depreciation functionality
 * where is desired to look for fixed assets in actual state to apply depreciation.
 *
 * @author
 * @version 2.4.3
 */

public class ThereIsNoActualFixedAssetException extends Exception {
    public ThereIsNoActualFixedAssetException() {
    }

    public ThereIsNoActualFixedAssetException(String message) {
        super(message);
    }

    public ThereIsNoActualFixedAssetException(Throwable cause) {
        super(cause);
    }
}