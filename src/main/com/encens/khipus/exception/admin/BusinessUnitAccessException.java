package com.encens.khipus.exception.admin;

/**
 * The implementations must thrown <code>BusinessUnitAccessException</code> when attempting make a crud operations
 * on entities that contain a restricted <code>BusinessUnit</code> relationships.
 *
 * @author
 * @version 2.22
 */

public class BusinessUnitAccessException extends RuntimeException {
    private String businessUnitName;

    public BusinessUnitAccessException(String businessUnitName) {
        this.businessUnitName = businessUnitName;
    }

    public BusinessUnitAccessException(String message, String businessUnitName) {
        super(message);
        this.businessUnitName = businessUnitName;
    }

    public BusinessUnitAccessException(String message, Throwable cause, String businessUnitName) {
        super(message, cause);
        this.businessUnitName = businessUnitName;
    }

    public BusinessUnitAccessException(Throwable cause, String businessUnitName) {
        super(cause);
        this.businessUnitName = businessUnitName;
    }

    public String getBusinessUnitName() {
        return businessUnitName;
    }
}
