package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.FinishedGoodsWarehouse;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("finishedGoodsWarehouseDataModel")
@Scope(ScopeType.PAGE)
public class FinishedGoodsWarehouseDataModel extends QueryDataModel<Long, FinishedGoodsWarehouse> {
    private static final String[] RESTRICTIONS = {
            "lower(warehouse.name) like concat(#{finishedGoodsWarehouseDataModel.criteria.name}, '%')",
            "lower(warehouse.code) like concat(#{finishedGoodsWarehouseDataModel.criteria.code}, '%')"
    };

    @Create
    public void init() {
        sortProperty = "warehouse.name";
    }

    @Override
    public String getEjbql() {
        return "select warehouse " +
                "from FinishedGoodsWarehouse warehouse ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
