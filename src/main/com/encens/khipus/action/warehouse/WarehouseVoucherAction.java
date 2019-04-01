package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.warehouse.WarehouseAccountEntryService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Name("warehouseVoucherAction")
@Scope(ScopeType.CONVERSATION)
public class WarehouseVoucherAction extends GenericAction<WarehouseVoucher> {

    private Date startDate;
    private Date endDate;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;
    @In
    private VoucherAccoutingService voucherAccoutingService;

    @Factory(value = "warehouseVoucher", scope = ScopeType.STATELESS)
    public WarehouseVoucher initWarehouseVoucher() {
        return getInstance();
    }

    @Factory(value = "warehouseVoucherStates", scope = ScopeType.STATELESS)
    public WarehouseVoucherState[] getWarehouseVoucherStates() {
        return WarehouseVoucherState.values();
    }



    public void processVouchersWithoutAccounting(){

        List<WarehouseVoucher> warehouseVoucherList = warehouseAccountEntryService.getVouchersWithoutAccounting(startDate, endDate);

        for (WarehouseVoucher warehouseVoucher : warehouseVoucherList){

            if (warehouseVoucher.getOperation().equals(VoucherOperation.TP)){

            }

            if (warehouseVoucher.getOperation().equals(VoucherOperation.BA)){

            }

            if (warehouseVoucher.getOperation().equals(VoucherOperation.DE)){

            }

        }

    }

    public void createAccountingForVoucherTP(WarehouseVoucher warehouseVoucher, Date startDate, Date endDate){

        HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);
        MovementDetail movementDetail =  warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList().get(0);

        Voucher voucher = new Voucher();

        if (movementDetail.getMovementType().equals(MovementDetailType.S)){
            VoucherDetail voucherDetail = new VoucherDetail();
        }

        if (movementDetail.getMovementType().equals(MovementDetailType.E)){

        }

    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
