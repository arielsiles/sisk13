package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehousePK;
import com.encens.khipus.model.warehouse.WarehouseState;
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
@Name("warehouseSearchDataModel")
@Scope(ScopeType.PAGE)
public class WarehouseSearchDataModel extends QueryDataModel<WarehousePK, Warehouse> {
    private static final String[] RESTRICTIONS = {
            "lower(warehouse.name) like concat('%', concat(lower(#{warehouseSearchDataModel.criteria.name}), '%'))",
            "lower(warehouse.warehouseCode) like concat(lower(#{warehouseSearchDataModel.criteria.warehouseCode}), '%')",
            "warehouse.state = #{warehouseSearchDataModel.criteria.state}",
            "warehouse.executorUnit = #{warehouseSearchDataModel.criteria.executorUnit}"};

    @Create
    public void init() {
        sortProperty = "warehouse.name";
    }

    @Override
    public String getEjbql() {
        return "select warehouse from Warehouse warehouse";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public void viewOnlyValidWarehouseStates() {
        getCriteria().setState(WarehouseState.VIG);
    }

    public void filterByExecutorUnitCode(BusinessUnit executorUnit) {
        getCriteria().setExecutorUnit(executorUnit);
        updateAndSearch();
    }
}
