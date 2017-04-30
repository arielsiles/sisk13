package com.encens.khipus.exception.fixedassets;

/**
 * Validates that fixedAssetCode be unique
 * a unique constraint was not used because the attribute can be null.
 * So method validation applied
 *
 * @author
 * @version 2.4.2
 */

public class DuplicatedFixedAssetCodeException extends Exception {
    public DuplicatedFixedAssetCodeException() {
    }

    public DuplicatedFixedAssetCodeException(String message) {
        super(message);
    }

    public DuplicatedFixedAssetCodeException(Throwable cause) {
        super(cause);
    }
}