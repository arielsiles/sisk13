package com.encens.khipus.exception.fixedassets;

/**
 * This exception is thrown when the unit price of a FixedAssetPurchaseOrderDetail is equal or less than one ufv.
 *
 * @author
 * @version 2.5
 */

public class UnitPriceEqualOrLessThanOneUfvException extends Exception {
    public UnitPriceEqualOrLessThanOneUfvException() {
    }

    public UnitPriceEqualOrLessThanOneUfvException(String message) {
        super(message);
    }

    public UnitPriceEqualOrLessThanOneUfvException(Throwable cause) {
        super(cause);
    }
}