package com.encens.khipus.model.warehouse;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
public enum WarehouseVoucherType {
    C("WarehouseVoucherType.consumption"),
    D("WarehouseVoucherType.devolution"),
    T("WarehouseVoucherType.transfer"),
    M("WarehouseVoucherType.executorUnitTransfer"),
    P("WarehouseVoucherType.productTransfer"),
    B("WarehouseVoucherType.low"),
    W("WarehouseVoucherType.reworkOutput"),

    R("WarehouseVoucherType.reception"),
    E("WarehouseVoucherType.input"),
    S("WarehouseVoucherType.output"),

    RT("WarehouseVoucherType.receptionByTransfer"),
    BV("WarehouseVoucherType.salesOuput");

    private String resourceKey;

    WarehouseVoucherType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static List<WarehouseVoucherType> getInputTypes() {
        return Arrays.asList(WarehouseVoucherType.E, WarehouseVoucherType.R, WarehouseVoucherType.D, WarehouseVoucherType.T, WarehouseVoucherType.RT);
    }

    public static List<WarehouseVoucherType> getOutputTypes() {
        return Arrays.asList(WarehouseVoucherType.C, WarehouseVoucherType.S, WarehouseVoucherType.B, WarehouseVoucherType.W, WarehouseVoucherType.BV);
    }

    public static boolean isSpecialVoucherType(WarehouseVoucherType warehouseVoucherType) {
        return WarehouseVoucherType.E.equals(warehouseVoucherType) ||
                WarehouseVoucherType.S.equals(warehouseVoucherType) ||
                WarehouseVoucherType.M.equals(warehouseVoucherType);
    }
}
