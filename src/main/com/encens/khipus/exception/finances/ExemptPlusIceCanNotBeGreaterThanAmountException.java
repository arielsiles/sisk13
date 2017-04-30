package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.22
 */

public class ExemptPlusIceCanNotBeGreaterThanAmountException extends Exception {
    public ExemptPlusIceCanNotBeGreaterThanAmountException() {
    }

    public ExemptPlusIceCanNotBeGreaterThanAmountException(String message) {
        super(message);
    }

    public ExemptPlusIceCanNotBeGreaterThanAmountException(Throwable cause) {
        super(cause);
    }
}