package com.encens.khipus.exception.warehouse;

/**
 * WarehouseDocumentTypeNotFoundException
 *
 * @author
 * @version 2.1
 */
public class WarehouseDocumentTypeNotFoundException extends Exception {
    public WarehouseDocumentTypeNotFoundException() {
    }

    public WarehouseDocumentTypeNotFoundException(String message) {
        super(message);
    }

    public WarehouseDocumentTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WarehouseDocumentTypeNotFoundException(Throwable cause) {
        super(cause);
    }
}
