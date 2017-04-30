package com.encens.khipus.exception.warehouse;

/**
 * @author
 * @version 2.0
 */

public class WarehouseVoucherNotFoundException extends Exception {
    public WarehouseVoucherNotFoundException() {
    }

    public WarehouseVoucherNotFoundException(String message) {
        super(message);
    }

    public WarehouseVoucherNotFoundException(Throwable cause) {
        super(cause);
    }
}
