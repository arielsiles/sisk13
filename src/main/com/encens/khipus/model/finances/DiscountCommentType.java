package com.encens.khipus.model.finances;

/**
 * Enum for DiscountCommentType
 *
 * @author
 * @version 3.0
 */
public enum DiscountCommentType {
    FIXED_ASSET_VOUCHER("DiscountCommentType.FIXED_ASSET_VOUCHER"),
    WAREHOUSE_PURCHASE_ORDER("DiscountCommentType.WAREHOUSE_PURCHASE_ORDER"),
    FIXED_ASSET_PURCHASE_ORDER("DiscountCommentType.FIXED_ASSET_PURCHASE_ORDER"),
    ROTATORY_FUND("DiscountCommentType.ROTATORY_FUND");

    private String resourceKey;

    DiscountCommentType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}