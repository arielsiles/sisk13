package com.encens.khipus.exception.employees;

import com.encens.khipus.model.employees.DischargeDocumentState;

/**
 * @author
 * @version 2.26
 */
public class DischargeDocumentStateException extends Exception {
    private DischargeDocumentState currentState;

    public DischargeDocumentStateException(DischargeDocumentState state) {
        this.currentState = state;
    }

    public DischargeDocumentState getCurrentState() {
        return currentState;
    }
}
