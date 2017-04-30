package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.17
 */

public class ResidueCannotBeLessThanZeroException extends Exception {
    public ResidueCannotBeLessThanZeroException() {
    }

    public ResidueCannotBeLessThanZeroException(String message) {
        super(message);
    }

    public ResidueCannotBeLessThanZeroException(Throwable cause) {
        super(cause);
    }
}