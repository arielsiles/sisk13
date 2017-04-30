package com.encens.khipus.exception.purchase;

/**
 * @author
 * @version 2.2
 */
public class PurchaseOrderNullifiedException extends Exception {
    public PurchaseOrderNullifiedException() {
    }

    public PurchaseOrderNullifiedException(String message) {
        super(message);
    }

    public PurchaseOrderNullifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PurchaseOrderNullifiedException(Throwable cause) {
        super(cause);
    }
}
