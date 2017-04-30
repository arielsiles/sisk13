package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.purchases.PurchaseOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.3
 */
@Name("purchaseOrderFixedAssetPartDataModel")
@Scope(ScopeType.PAGE)
public class PurchaseOrderFixedAssetPartDataModel extends QueryDataModel<Long, PurchaseOrderFixedAssetPart> {

    private PurchaseOrder purchaseOrder;

    private static final String[] RESTRICTIONS = {
            "purchaseOrder = #{purchaseOrderFixedAssetPartDataModel.purchaseOrder}"
    };

    @Create
    public void init() {
        sortProperty = "fixedAsset.barCode, purchaseOrderFixedAssetPart.description";
    }

    @Override
    public String getEjbql() {
        return "select purchaseOrderFixedAssetPart from PurchaseOrderFixedAssetPart purchaseOrderFixedAssetPart" +
                " left join fetch purchaseOrderFixedAssetPart.measureUnit measureUnit" +
                " left join fetch purchaseOrderFixedAssetPart.fixedAsset fixedAsset" +
                " left join fetch purchaseOrderFixedAssetPart.purchaseOrder purchaseOrder";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public void searchByPurchaseOrder(PurchaseOrder purchaseOrder) {
        setPurchaseOrder(purchaseOrder);
        updateAndSearch();
    }
}
