package com.encens.khipus.model.finances;

/**
 * @author
 * @version 2.40
 */
public enum RotatoryFundDocumentTypeFieldRestriction {
    CASH_ACCOUNT_DEFINED_BY_DEFAULT("RotatoryFundDocumentTypeFieldRestriction.CASH_ACCOUNT_DEFINED_BY_DEFAULT"),
    CASH_ACCOUNT_DEFINED_BY_USER("RotatoryFundDocumentTypeFieldRestriction.CASH_ACCOUNT_DEFINED_BY_USER");

    private String resourceKey;

    RotatoryFundDocumentTypeFieldRestriction(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
