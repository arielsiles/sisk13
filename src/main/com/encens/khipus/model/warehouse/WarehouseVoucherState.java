package com.encens.khipus.model.warehouse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
public enum WarehouseVoucherState {
    APR("WarehouseVoucher.state.ok"),
    PEN("WarehouseVoucher.state.pendant"),
    PAR("WarehouseVoucher.state.partial"),
    ANL("WarehouseVoucher.state.cancel");
    private String resourceKey;

    WarehouseVoucherState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static List<WarehouseVoucherState> getValidStates() {
        List<WarehouseVoucherState> validStates = new ArrayList<WarehouseVoucherState>();
        validStates.add(APR);
        validStates.add(PAR);
        return validStates;
    }
}
