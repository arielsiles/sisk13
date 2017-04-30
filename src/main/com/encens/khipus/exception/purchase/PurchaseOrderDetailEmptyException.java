package com.encens.khipus.exception.purchase;

/**
 * PurchaseOrderDetailEmptyException
 *
 * @author
 * @version 2.1
 */
public class PurchaseOrderDetailEmptyException extends Exception {
    public PurchaseOrderDetailEmptyException() {
    }

    public PurchaseOrderDetailEmptyException(String message) {
        super(message);
    }

    public PurchaseOrderDetailEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PurchaseOrderDetailEmptyException(Throwable cause) {
        super(cause);
    }
}
