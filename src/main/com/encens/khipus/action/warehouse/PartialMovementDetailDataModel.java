package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.MovementDetail;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("partialMovementDetailDataModel")
@Scope(ScopeType.PAGE)
public class PartialMovementDetailDataModel extends QueryDataModel<Long, MovementDetail> {
    private static final String[] RESTRICTIONS = {

    };

    public void init() {
        sortProperty = "warehouseVoucher.number, productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select movementDetail from MovementDetail movementDetail " +
                "left join fetch movementDetail.productItem productItem " +
                "left join fetch movementDetail.measureUnit measureUnit " +
                "left join fetch movementDetail.parentMovementDetail parentMovement " +
                "left join fetch movementDetail.inventoryMovement inventoryMovement " +
                "left join fetch inventoryMovement.warehouseVoucher warehouseVoucher " +
                "where movementDetail.parentMovementDetail is not null " +
                "and parentMovement.companyNumber=#{warehouseVoucherUpdateAction.warehouseVoucher.id.companyNumber} " +
                "and parentMovement.transactionNumber=#{warehouseVoucherUpdateAction.warehouseVoucher.id.transactionNumber} " +
                "and parentMovement.state=#{warehouseVoucherUpdateAction.warehouseVoucher.state} ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
