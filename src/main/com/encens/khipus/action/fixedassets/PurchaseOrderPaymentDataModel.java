package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.24
 */
@Name("fixedAssetPurchaseOrderPaymentDataModel")
@Scope(ScopeType.PAGE)
public class PurchaseOrderPaymentDataModel extends QueryDataModel<Long, PurchaseOrderPayment> {
    private static final String[] RESTRICTIONS = {
            "purchaseOrderPayment.purchaseOrder = #{fixedAssetPurchaseOrder}"
    };

    @Create
    public void init() {
        sortProperty = "purchaseOrderPayment.creationDate";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select purchaseOrderPayment from PurchaseOrderPayment purchaseOrderPayment";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
