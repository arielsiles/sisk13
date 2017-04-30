package com.encens.khipus.model.finances;

/**
 * @author
 * @version 2.14
 */
public enum RotatoryFundType {
    RECEIVABLE_FUND(1, "RotatoryFundType.receivableFund"),
    PARTNER_WITHDRAWAL(2, "RotatoryFundType.partnerWithdrawal"),
    LOAN(3, "RotatoryFundType.loan"),
    ADVANCE(4, "RotatoryFundType.advance"),
    OTHER_RECEIVABLES(5, "RotatoryFundType.otherReceivables");

    private String resourceKey;
    private int code;

    RotatoryFundType(int code, String resourceKey) {
        this.code = code;
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}