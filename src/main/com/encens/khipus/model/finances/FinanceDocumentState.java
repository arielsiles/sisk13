package com.encens.khipus.model.finances;

/**
 * @author
 */
public enum FinanceDocumentState {
    APR("FinanceDocumentState.APR"),
    ANL("FinanceDocumentState.ANL"),
    CCI("FinanceDocumentState.CCI"),
    DES("FinanceDocumentState.DES");

    private String resourceKey;

    FinanceDocumentState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
