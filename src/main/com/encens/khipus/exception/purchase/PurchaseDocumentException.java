package com.encens.khipus.exception.purchase;

import java.util.List;

/**
 * @author
 * @version 2.25
 */
public class PurchaseDocumentException extends Exception {
    public static enum ErrorType {
        ICE_NEGATIVE_VALUE,
        ICE_GREATER_THAN_AMOUNT,
        EXEMPT_NEGATIVE_VALUE,
        EXEMPT_GREATER_THAN_AMOUNT,
        SUM_ICE_EXEMPT_EXCEED_AMOUNT
    }

    private List<ErrorType> errorTypes;

    public PurchaseDocumentException(List<ErrorType> errorTypes) {
        this.errorTypes = errorTypes;
    }

    public List<ErrorType> getErrorTypes() {
        return errorTypes;
    }
}
