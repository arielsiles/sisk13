package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.InventoryDetail;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("inventoryDetailDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('INVENTORY','VIEW')}")
public class InventoryDetailDataModel extends QueryDataModel<Long, InventoryDetail> {
    private static final String[] RESTRICTIONS =
            {
                    "inventoryDetail.warehouseCode = #{inventoryWarehouse.id.warehouseCode}",
                    "inventoryDetail.productItemCode = #{inventoryDetailDataModel.criteria.productItemCode} ",
                    "inventoryDetail.costCenterCode = #{inventoryDetailDataModel.criteria.costCenterCode} "
            };

    @Create
    public void init() {
        sortProperty = "inventoryDetail.quantity";
    }

    @Override
    public String getEjbql() {
        return "select inventoryDetail from InventoryDetail inventoryDetail";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public void filterByProductItemCode(String productItemCode) {
        getCriteria().setProductItemCode(productItemCode);
        updateAndSearch();
    }

    public void filterByCostCenterCode(String costCenterCode) {
        getCriteria().setCostCenterCode(costCenterCode);
        updateAndSearch();
    }
}
