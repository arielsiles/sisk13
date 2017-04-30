package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.warehouse.WarehouseVoucherState;

public class WarehouseVoucherStateException extends Exception {
    WarehouseVoucherState newWarehouseVoucherState;

    public WarehouseVoucherStateException(WarehouseVoucherState newWarehouseVoucherState) {
        this.newWarehouseVoucherState = newWarehouseVoucherState;
    }

    public WarehouseVoucherState getNewWarehouseVoucherState() {
        return newWarehouseVoucherState;
    }

    public void setNewWarehouseVoucherState(WarehouseVoucherState newWarehouseVoucherState) {
        this.newWarehouseVoucherState = newWarehouseVoucherState;
    }
}