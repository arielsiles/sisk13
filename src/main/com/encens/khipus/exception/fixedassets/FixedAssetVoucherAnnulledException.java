package com.encens.khipus.exception.fixedassets;

/**
 * @author
 * @version 2.24
 */

public class FixedAssetVoucherAnnulledException extends Exception {
    public FixedAssetVoucherAnnulledException() {
    }

    public FixedAssetVoucherAnnulledException(String message) {
        super(message);
    }

    public FixedAssetVoucherAnnulledException(Throwable cause) {
        super(cause);
    }
}