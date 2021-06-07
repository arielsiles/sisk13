package com.encens.khipus.model.customers;

/**
 * Encens Team
 *
 * @author
 * @version : EntityType, 07-10-2009 11:42:22 AM
 */
public enum CustomerOrderTypeEnum {
    NORMAL("CustomerOrderType.type.normal"),
    TASTING("CustomerOrderType.type.tasting"),
    REFRESHMENT("CustomerOrderType.type.refreshment"),
    REPLACEMENT("CustomerOrderType.type.replacement"),
    MILK("CustomerOrderType.type.milk"),
    VETERINARY("CustomerOrderType.type.veterinary"),
    SPECIAL("CustomerOrderType.type.special"),
    TRANSFER("CustomerOrderType.type.transfer");

    private String resourceKey;

    CustomerOrderTypeEnum(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
