package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.VoucherDetail;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Name("voucherDetailDataModel")
@Scope(ScopeType.PAGE)
public class VoucherDetailDataModel extends QueryDataModel<Long, VoucherDetail> {

    private static final String[] RESTRICTIONS = {};

    @Create
    public void init() {
        sortProperty = "voucher.transactionNumber";
    }

    @Override
    public String getEjbql() {
        return "select voucherDetail " +
                "from VoucherDetail voucherDetail ";
    }

/*
    @Override
    public String getEjbql() {
        return "select movementDetail " +
                "from MovementDetail movementDetail " +
                "left join fetch movementDetail.productItem productItem " +
                "left join fetch movementDetail.measureUnit measureUnit " +
                "where movementDetail.companyNumber=#{warehouseVoucherUpdateAction.warehouseVoucher.id.companyNumber} " +
                "and movementDetail.transactionNumber=#{warehouseVoucherUpdateAction.warehouseVoucher.id.transactionNumber} " +
                "and movementDetail.state=#{warehouseVoucherUpdateAction.warehouseVoucher.state} " +
                "and movementDetail.sourceId is null";
    }
*/

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
