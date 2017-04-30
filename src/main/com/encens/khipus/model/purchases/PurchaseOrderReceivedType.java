package com.encens.khipus.model.purchases;

/**
 * PurchaseOrderReceivedType, the values description are:
 * [RP] recibido parcialmente,[RT] recibido totalmente.
 *
 * @author
 * @version 2.1
 */
public enum PurchaseOrderReceivedType {
    RP("PurchaseOrderReceivedType.RP"),
    RT("PurchaseOrderReceivedType.RT");

    private String resourceKey;

    PurchaseOrderReceivedType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
