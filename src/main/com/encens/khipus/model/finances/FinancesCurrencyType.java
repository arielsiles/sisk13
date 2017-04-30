package com.encens.khipus.model.finances;

/**
 * FinancesCurrencyType
 *
 * @author
 * @version 2.0
 */
public enum FinancesCurrencyType {
    P("FinancesCurrencyType.P", "FinancesCurrencyType.symbol.P"),
    D("FinancesCurrencyType.D", "FinancesCurrencyType.symbol.D"),
    U("FinancesCurrencyType.U", "FinancesCurrencyType.symbol.U");
    private String resourceKey;
    private String symbolResourceKey;

    FinancesCurrencyType(String resourceKey, String symbolResourceKey) {
        this.resourceKey = resourceKey;
        this.symbolResourceKey = symbolResourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getSymbolResourceKey() {
        return symbolResourceKey;
    }

    public void setSymbolResourceKey(String symbolResourceKey) {
        this.symbolResourceKey = symbolResourceKey;
    }
}
