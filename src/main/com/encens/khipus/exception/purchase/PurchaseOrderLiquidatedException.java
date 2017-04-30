package com.encens.khipus.exception.purchase;

/**
 * PurchaseOrderLiquidatedException
 *
 * @author
 * @version 2.23
 */
public class PurchaseOrderLiquidatedException extends Exception {

    public PurchaseOrderLiquidatedException() {
    }

    public PurchaseOrderLiquidatedException(String message) {
        super(message);
    }

    public PurchaseOrderLiquidatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PurchaseOrderLiquidatedException(Throwable cause) {
        super(cause);
    }
}
