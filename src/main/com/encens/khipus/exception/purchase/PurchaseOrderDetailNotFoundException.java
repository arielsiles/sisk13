package com.encens.khipus.exception.purchase;

/**
 * @author
 * @version 2.2
 */
public class PurchaseOrderDetailNotFoundException extends Exception {
    public PurchaseOrderDetailNotFoundException() {
    }

    public PurchaseOrderDetailNotFoundException(String message) {
        super(message);
    }

    public PurchaseOrderDetailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PurchaseOrderDetailNotFoundException(Throwable cause) {
        super(cause);
    }
}
