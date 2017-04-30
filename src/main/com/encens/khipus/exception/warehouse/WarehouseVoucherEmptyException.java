package com.encens.khipus.exception.warehouse;

/**
 * @author
 */
public class WarehouseVoucherEmptyException extends Exception {
    public WarehouseVoucherEmptyException() {
    }

    public WarehouseVoucherEmptyException(String message) {
        super(message);
    }

    public WarehouseVoucherEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public WarehouseVoucherEmptyException(Throwable cause) {
        super(cause);
    }
}
