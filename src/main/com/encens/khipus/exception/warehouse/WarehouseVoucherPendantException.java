package com.encens.khipus.exception.warehouse;

import javax.ejb.ApplicationException;

/**
 * @author
 * @version 2.0
 */
@ApplicationException(rollback = true)
public class WarehouseVoucherPendantException extends Exception {
    public WarehouseVoucherPendantException() {
    }

    public WarehouseVoucherPendantException(String message) {
        super(message);
    }

    public WarehouseVoucherPendantException(String message, Throwable cause) {
        super(message, cause);
    }

    public WarehouseVoucherPendantException(Throwable cause) {
        super(cause);
    }
}
