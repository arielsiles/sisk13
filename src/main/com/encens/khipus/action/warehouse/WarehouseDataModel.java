package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehousePK;
import com.encens.khipus.util.ListEntityManagerName;
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
@Name("warehouseDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSE','VIEW')}")
public class WarehouseDataModel extends QueryDataModel<WarehousePK, Warehouse> {

    private static final String[] RESTRICTIONS = {
            "lower(warehouse.name) like concat('%', concat(lower(#{warehouseDataModel.criteria.name}), '%'))",
            "lower(warehouse.warehouseCode) like concat(lower(#{warehouseDataModel.criteria.warehouseCode}), '%')",
            "warehouse.state = #{warehouseDataModel.criteria.state}"};

    @Create
    public void init() {
        setEntityManagerName(ListEntityManagerName.BUSINESS_UNIT_LIST.getName());
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
}
