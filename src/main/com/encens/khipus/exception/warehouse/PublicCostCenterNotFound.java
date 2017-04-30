package com.encens.khipus.exception.warehouse;

/**
 * @author
 * @version 2.4
 */
public class PublicCostCenterNotFound extends Exception {
    public PublicCostCenterNotFound() {
    }

    public PublicCostCenterNotFound(String message) {
        super(message);
    }

    public PublicCostCenterNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public PublicCostCenterNotFound(Throwable cause) {
        super(cause);
    }
}
