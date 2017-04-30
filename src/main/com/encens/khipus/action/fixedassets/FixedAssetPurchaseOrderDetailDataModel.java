package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("fixedAssetPurchaseOrderDetailDataModel")
@Scope(ScopeType.PAGE)
public class FixedAssetPurchaseOrderDetailDataModel extends QueryDataModel<Long, FixedAssetPurchaseOrderDetail> {
    private static final String[] RESTRICTIONS = {
            /* be careful to select only FixedAssetPurchaseOrder of type FIXEDASSET*/
            /* since this is a nested conversation that allows to view both entities in the same form
            * we select the instance which evolves the other*/
            "fixedAssetPurchaseOrderDetail.purchaseOrder = #{fixedAssetPurchaseOrder}",
            "fixedAssetPurchaseOrderDetail.purchaseOrder.orderType = " +
                    "#{enumerationUtil.getEnumValue('com.encens.khipus.model.purchases.PurchaseOrderType', 'FIXEDASSET')}"};

    @Create
    public void init() {
        sortProperty = "fixedAssetPurchaseOrderDetail.detailNumber";
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetPurchaseOrderDetail from FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail" +
                " left join fetch fixedAssetPurchaseOrderDetail.fixedAssetSubGroup fixedAssetSubGroup" +
                " left join fetch fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
