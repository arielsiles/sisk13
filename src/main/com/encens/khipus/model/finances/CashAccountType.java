package com.encens.khipus.model.finances;

/**
 * FinancesCurrencyType
 *
 * @author
 * @version 2.0
 */
public enum CashAccountType {
    A("CashAccountType.A"),
    P("CashAccountType.P"),
    C("CashAccountType.C"),
    E("CashAccountType.E"),
    I("CashAccountType.I"),
    OD("CashAccountType.OD"),
    OA("CashAccountType.OA");
    private String resourceKey;

    CashAccountType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
