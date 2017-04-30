package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("warehouseVoucherProductItemDataModel")
@Scope(ScopeType.PAGE)
public class WarehouseVoucherProductItemDataModel extends QueryDataModel<ProductItemPK, ProductItem> {
    private static final String[] RESTRICTIONS = {
            "lower(productItem.id.productItemCode) like concat(lower(#{warehouseVoucherProductItemDataModel.criteria.id.productItemCode}), '%')",
            "lower(productItem.name) like concat('%', concat(lower(#{warehouseVoucherProductItemDataModel.criteria.name}), '%'))",
            "productItem not in (#{partialWarehouseVoucherAction.selectedProductItemList}) "
    };

    @Create
    public void init() {
        sortProperty = "productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select productItem from ProductItem productItem " +
                "where productItem in " +
                "( select movementDetail.productItem from MovementDetail movementDetail " +
                "where movementDetail.sourceId is null " +
                "and movementDetail.parentMovementDetail is null " +
                "and ((movementDetail.residue is null) or (movementDetail.residue > #{partialWarehouseVoucherAction.minimalResidue})) " +
                "and movementDetail.companyNumber=#{warehouseVoucherUpdateAction.warehouseVoucher.id.companyNumber} " +
                "and movementDetail.transactionNumber=#{warehouseVoucherUpdateAction.warehouseVoucher.id.transactionNumber} " +
                "and movementDetail.state=#{warehouseVoucherUpdateAction.warehouseVoucher.state} " +
                ")";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public List<ProductItem> getSelectedProductItems() {
        List ids = super.getSelectedIdList();

        List<ProductItem> result = new ArrayList<ProductItem>();
        for (Object id : ids) {
            result.add(getEntityManager().find(ProductItem.class, id));
        }

        return result;
    }

}
