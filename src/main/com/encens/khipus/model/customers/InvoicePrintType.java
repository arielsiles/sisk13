package com.encens.khipus.model.customers;

/**
 * @author Ariel Siles Encinas
 * @version 1.2.9
 */

public enum InvoicePrintType {

    INVOICE_COPY("PrintInvoiceType.copy"),
    INVOICE_ORIGINAL("PrintInvoiceType.original");

    private String resourceKey;

    InvoicePrintType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
