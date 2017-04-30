package com.encens.khipus.exception.purchase;

/**
 * @author
 * @version 2.2
 */
public class PurchaseOrderFinalizedException extends Exception {
    public PurchaseOrderFinalizedException() {
    }

    public PurchaseOrderFinalizedException(String message) {
        super(message);
    }

    public PurchaseOrderFinalizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PurchaseOrderFinalizedException(Throwable cause) {
        super(cause);
    }
}
