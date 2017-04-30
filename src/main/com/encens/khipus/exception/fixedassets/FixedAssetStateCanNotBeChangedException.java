package com.encens.khipus.exception.fixedassets;

/**
 * This exception is thrown when it was not possible to change the state of a Fixed Asset.
 *
 * @author
 * @version 2.4.3
 */

public class FixedAssetStateCanNotBeChangedException extends Exception {
    public FixedAssetStateCanNotBeChangedException() {
    }

    public FixedAssetStateCanNotBeChangedException(String message) {
        super(message);
    }

    public FixedAssetStateCanNotBeChangedException(Throwable cause) {
        super(cause);
    }
}