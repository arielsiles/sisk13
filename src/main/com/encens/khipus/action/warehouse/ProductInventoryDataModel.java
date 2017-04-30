package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.ProductInventory;
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

@Name("productInventoryDataModel")
@Scope(ScopeType.PAGE)
public class ProductInventoryDataModel extends QueryDataModel<Long, ProductInventory> {
    private static final String[] RESTRICTIONS =
            {
                    "lower(productInventory.productItemCode)  like concat('%', concat(lower(#{productInventoryDataModel.criteria.productItemCode}), '%'))",
                    "lower(productInventory.productItem.name) like concat('%', concat(lower(#{productInventoryAction.productItemName}), '%'))"
            };
    //{"lower(warehouseGroup.name) like concat('%', concat(lower(#{groupDataModel.criteria.name}), '%'))"};
    @Create
    public void init() {
        sortProperty = "productInventory.quantity";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select productInventory from ProductInventory productInventory";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public void filterByProductItemCode(String productItemCode) {
        getCriteria().setProductItemCode(productItemCode);
        updateAndSearch();
    }

    public void filterByProductItemName(String productItemName) {
        //getCriteria().setProductItemCode(productItemCode);
        getCriteria().getProductItem().setName(productItemName);
        updateAndSearch();
    }

    public void filterByCostCenterCode(String costCenterCode) {
        getCriteria().setCostCenterCode(costCenterCode);
        updateAndSearch();
    }
}
