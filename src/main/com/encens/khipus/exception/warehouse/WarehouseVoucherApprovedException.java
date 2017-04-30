package com.encens.khipus.exception.warehouse;

/**
 * @author
 * @version 2.0
 */
/*@ApplicationException(rollback = true)*/
public class WarehouseVoucherApprovedException extends Exception {
    public WarehouseVoucherApprovedException() {
    }

    public WarehouseVoucherApprovedException(String message) {
        super(message);
    }

    public WarehouseVoucherApprovedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WarehouseVoucherApprovedException(Throwable cause) {
        super(cause);
    }
}
