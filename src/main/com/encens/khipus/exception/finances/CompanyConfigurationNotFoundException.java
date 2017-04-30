package com.encens.khipus.exception.finances;

/**
 * CompanyConfigurationNotFoundException
 *
 * @author
 * @version 2.3
 */
public class CompanyConfigurationNotFoundException extends Exception {
    public CompanyConfigurationNotFoundException() {
    }

    public CompanyConfigurationNotFoundException(String message) {
        super(message);
    }

    public CompanyConfigurationNotFoundException(Throwable cause) {
        super(cause);
    }
}