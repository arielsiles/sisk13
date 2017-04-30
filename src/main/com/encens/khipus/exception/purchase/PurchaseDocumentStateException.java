package com.encens.khipus.exception.purchase;

import com.encens.khipus.model.purchases.PurchaseDocumentState;

/**
 * @author
 * @version 2.25
 */
public class PurchaseDocumentStateException extends Exception {
    private PurchaseDocumentState currentState;

    public PurchaseDocumentStateException(PurchaseDocumentState currentState) {
        this.currentState = currentState;
    }

    public PurchaseDocumentState getCurrentState() {
        return currentState;
    }
}
