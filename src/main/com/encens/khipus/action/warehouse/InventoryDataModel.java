package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.InventoryPK;
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
@Name("inventoryDataModel")
@Scope(ScopeType.PAGE)
public class InventoryDataModel extends QueryDataModel<InventoryPK, Inventory> {
    private static final String[] RESTRICTIONS =
            {"inventory.warehouseCode = #{inventoryWarehouse.id.warehouseCode}"};

    @Create
    public void init() {
        sortProperty = "inventory.unitaryBalance";
    }

    @Override
    public String getEjbql() {
        return "select inventory from Inventory inventory";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
