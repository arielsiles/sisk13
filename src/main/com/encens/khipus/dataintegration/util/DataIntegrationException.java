package com.encens.khipus.dataintegration.util;

/**
 * @author
 */
public class DataIntegrationException extends Exception {
    public DataIntegrationException() {
    }

    public DataIntegrationException(String message) {
        super(message);
    }

    public DataIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataIntegrationException(Throwable cause) {
        super(cause);
    }
}
