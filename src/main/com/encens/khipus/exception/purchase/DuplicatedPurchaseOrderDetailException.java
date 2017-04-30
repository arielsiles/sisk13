package com.encens.khipus.exception.purchase;

/**
 * DuplicatedPurchaseOrderDetailException
 *
 * @author
 * @version 2.22
 */
public class DuplicatedPurchaseOrderDetailException extends Exception {
    public DuplicatedPurchaseOrderDetailException() {
    }

    public DuplicatedPurchaseOrderDetailException(String message) {
        super(message);
    }

    public DuplicatedPurchaseOrderDetailException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedPurchaseOrderDetailException(Throwable cause) {
        super(cause);
    }
}
