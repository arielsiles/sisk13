package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.22
 */

public class IceCanNotBeGreaterThanAmountException extends Exception {
    public IceCanNotBeGreaterThanAmountException() {
    }

    public IceCanNotBeGreaterThanAmountException(String message) {
        super(message);
    }

    public IceCanNotBeGreaterThanAmountException(Throwable cause) {
        super(cause);
    }
}