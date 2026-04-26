package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.purchases.PurchaseOrderDetail;
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

@Name("warehousePurchaseOrderDetailDataModel")
@Scope(ScopeType.PAGE)
public class WarehousePurchaseOrderDetailDataModel extends QueryDataModel<Long, PurchaseOrderDetail> {
    private static final String[] RESTRICTIONS = {"warehousePurchaseOrderDetail.purchaseOrder = #{warehousePurchaseOrder}"};

    @Create
    public void init() {
        sortProperty = "warehousePurchaseOrderDetail.detailNumber";
    }

    @Override
    public String getEjbql() {
        // JOIN FETCH eager para productItem y purchaseMeasureUnit: la grilla los
        // accede al renderizar (productItem.fullName, purchaseMeasureUnit.name).
        // Sin fetch eager, los proxies LAZY quedan atados al listEntityManager
        // del request en que se cargaron; si el data model (PAGE-scope) sobrevive
        // al request, en renders posteriores los proxies disparan
        // LazyInitializationException. Cargandolos en el mismo SELECT evitamos
        // el problema sin alterar la semantica de la consulta.
        return "select warehousePurchaseOrderDetail from PurchaseOrderDetail warehousePurchaseOrderDetail" +
                " left join fetch warehousePurchaseOrderDetail.productItem" +
                " left join fetch warehousePurchaseOrderDetail.purchaseMeasureUnit";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
