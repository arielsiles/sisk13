package com.encens.khipus.action.warehouse;

import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseVoucher;

/**
 * @author
 * @version 2.5
 */
public class MovementDetailUIController {
    private WarehouseVoucher voucher;

    public MovementDetailUIController(WarehouseVoucher voucher) {
        this.voucher = voucher;
    }

    public boolean isShownAmountField(ProductItem productItem) {
        return (voucher.isReception()
                || voucher.isInput()
                || voucher.isOutput()
                || voucher.isExecutorUnitTransfer()
                || voucher.isDevolution()
                || voucher.isConsumption()) && null != productItem && productItem.getControlValued();
    }

    public boolean isShownAmountField() {
        return (voucher.isReception()
                || voucher.isInput()
                || voucher.isOutput()
                || voucher.isExecutorUnitTransfer()
                || voucher.isDevolution()
                || voucher.isConsumption());
    }

    public boolean isEnabledAmountField() {
        return (voucher.isReception()
                || voucher.isInput()
                || voucher.isOutput()
                || voucher.isDevolution());
    }

    public boolean isShownUnitCostField() {
        return voucher.isConsumption()
                || voucher.isReception()
                || voucher.isExecutorUnitTransfer()
                || voucher.isDevolution()
                || voucher.isInput()
                || voucher.isOutput();
    }

    public boolean isEnabledUnitCostField() {
        return voucher.isReception()
                || voucher.isInput()
                || voucher.isOutput()
                || voucher.isDevolution();
    }
}
