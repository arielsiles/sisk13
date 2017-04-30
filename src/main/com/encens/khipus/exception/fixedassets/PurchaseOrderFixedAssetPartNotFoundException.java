package com.encens.khipus.exception.fixedassets;

/**
 * @author
 * @version 3.3
 */
public class PurchaseOrderFixedAssetPartNotFoundException extends Exception {
    public PurchaseOrderFixedAssetPartNotFoundException() {
    }

    public PurchaseOrderFixedAssetPartNotFoundException(String message) {
        super(message);
    }

    public PurchaseOrderFixedAssetPartNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PurchaseOrderFixedAssetPartNotFoundException(Throwable cause) {
        super(cause);
    }
}
