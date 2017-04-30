package com.encens.khipus.model.purchases;

/**
 * @author
 * @version 2.2
 */
public enum PurchaseOrderType {
    WAREHOUSE("PurchaseOrderType.warehouse", "PurchaseOrderType.warehouse.acronym", "WarehousePurchaseOrder.warehouses"),
    FIXEDASSET("PurchaseOrderType.fixedAsset", "PurchaseOrderType.fixedAsset.acronym", "FixedAssetPurchaseOrder.fixedAssets");
    private String resourceKey;
    private String acronym;
    private String module;

    private PurchaseOrderType(String resourceKey, String acronym, String module) {
        this.resourceKey = resourceKey;
        this.acronym = acronym;
        this.module = module;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
