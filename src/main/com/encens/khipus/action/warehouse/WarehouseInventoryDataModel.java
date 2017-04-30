package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehousePK;
import com.encens.khipus.model.warehouse.WarehouseState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.0
 */

@Name("warehouseInventoryDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('INVENTORY','VIEW')}")
public class WarehouseInventoryDataModel extends QueryDataModel<WarehousePK, Warehouse> {
    private static final String[] RESTRICTIONS = {
            "lower(inventoryWarehouse.name) like concat('%', concat(lower(#{warehouseInventoryDataModel.criteria.name}), '%'))",
            "inventoryWarehouse.state = #{warehouseInventoryDataModel.criteria.state}"};

    @Create
    public void init() {
        sortProperty = "inventoryWarehouse.name";
    }

    @Override
    public String getEjbql() {
        return "select inventoryWarehouse from Warehouse inventoryWarehouse where inventoryWarehouse.id.warehouseCode in (select inventory.warehouseCode from Inventory inventory)";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public void viewOnlyValidWarehouseStates() {
        getCriteria().setState(WarehouseState.VIG);
    }
}
