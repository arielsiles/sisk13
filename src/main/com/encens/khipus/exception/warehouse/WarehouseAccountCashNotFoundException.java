package com.encens.khipus.exception.warehouse;

/**
 * CompanyConfigurationNotFoundException
 *
 * @author
 * @version 2.3
 */
public class WarehouseAccountCashNotFoundException extends Exception {
    public WarehouseAccountCashNotFoundException() {
    }

    public WarehouseAccountCashNotFoundException(String message) {
        super(message);
    }

    public WarehouseAccountCashNotFoundException(Throwable cause) {
        super(cause);
    }
}