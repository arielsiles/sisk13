package com.encens.khipus.exception.purchase;

/**
 * @author
 * @version 2.2
 */
public class PurchaseOrderApprovedException extends Exception {
    public PurchaseOrderApprovedException() {
    }

    public PurchaseOrderApprovedException(String message) {
        super(message);
    }

    public PurchaseOrderApprovedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PurchaseOrderApprovedException(Throwable cause) {
        super(cause);
    }
}
