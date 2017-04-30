package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.0
 */
@Name("warehouseVoucherAction")
@Scope(ScopeType.CONVERSATION)
public class WarehouseVoucherAction extends GenericAction<WarehouseVoucher> {

    @Factory(value = "warehouseVoucher", scope = ScopeType.STATELESS)
    public WarehouseVoucher initWarehouseVoucher() {
        return getInstance();
    }

    @Factory(value = "warehouseVoucherStates", scope = ScopeType.STATELESS)
    public WarehouseVoucherState[] getWarehouseVoucherStates() {
        return WarehouseVoucherState.values();
    }
}
