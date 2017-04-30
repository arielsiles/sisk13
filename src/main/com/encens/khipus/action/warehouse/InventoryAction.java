package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.0
 */
@Name("inventoryAction")
@Scope(ScopeType.CONVERSATION)
public class InventoryAction extends GenericAction<Warehouse> {

    /**
     * Fields that enable the user to filter the product list when desired
     */
    private ProductItem productItem;
    private CostCenter costCenter;

    @In(required = false)
    InventoryDetailDataModel inventoryDetailDataModel;

    @Factory(value = "inventoryWarehouse", scope = ScopeType.STATELESS)
    public Warehouse initInventory() {
        return getInstance();
    }

    @Override
    public String create() {
        return Outcome.REDISPLAY;
    }

    @Override
    public void createAndNew() {
    }

    @Override
    public String update() {
        return Outcome.REDISPLAY;
    }

    @Override
    public String delete() {
        return Outcome.REDISPLAY;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public Warehouse getWarehouse() {
        return getInstance();
    }

    public void assignProductItem(ProductItem productItem) {
        try {
            setProductItem(getService().findById(ProductItem.class, productItem.getId()));
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
    }

    public void clearProductItem() {
        setProductItem(null);
    }

    /**
     * This method is used for quickSearch components
     */

    public void assignProductItem() {
        assignProductItem(productItem);
    }

    /**
     * This method is used for quickSearch components
     */

    public void assignCostCenter() {
        assignCostCenter(costCenter);
    }

    public void assignCostCenter(CostCenter costCenter) {
        try {
            setCostCenter(getService().findById(CostCenter.class, costCenter.getId()));
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public void search() {
        inventoryDetailDataModel.filterByCostCenterCode(null == getCostCenter() ? null : getCostCenter().getCode());
        inventoryDetailDataModel.filterByProductItemCode(null == getProductItem() ? null : productItem.getId().getProductItemCode());
        inventoryDetailDataModel.search();
    }

    public void clear() {
        clearCostCenter();
        clearProductItem();
        inventoryDetailDataModel.filterByCostCenterCode(null == getCostCenter() ? null : getCostCenter().getCode());
        inventoryDetailDataModel.filterByProductItemCode(null == getProductItem() ? null : productItem.getId().getProductItemCode());
        inventoryDetailDataModel.search();
    }
}
