package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.purchases.PurchaseOrderPaymentState;

/**
 * @author
 * @version 2.24
 */

public class AdvancePaymentStateException extends Exception {
    private PurchaseOrderPaymentState actualState;

    public AdvancePaymentStateException(PurchaseOrderPaymentState actualState) {
        this.actualState = actualState;
    }

    public AdvancePaymentStateException(String message, PurchaseOrderPaymentState actualState) {
        super(message);
        this.actualState = actualState;
    }

    public AdvancePaymentStateException(String message, Throwable cause, PurchaseOrderPaymentState actualState) {
        super(message, cause);
        this.actualState = actualState;
    }

    public AdvancePaymentStateException(Throwable cause, PurchaseOrderPaymentState actualState) {
        super(cause);
        this.actualState = actualState;
    }

    public PurchaseOrderPaymentState getActualState() {
        return actualState;
    }
}
