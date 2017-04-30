package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.CostCenterPk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for CostCenter
 *
 * @author
 * @version 2.26.4
 */

@Name("inventoryCostCenterDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('INVENTORY','VIEW')}")
public class InventoryCostCenterDataModel extends QueryDataModel<CostCenterPk, CostCenter> {

    private static final String[] RESTRICTIONS = {
            "lower(costCenter.code) like concat(lower(#{inventoryCostCenterDataModel.criteria.code}), '%')",
            "lower(costCenter.description) like concat('%', concat(lower(#{inventoryCostCenterDataModel.criteria.description}), '%'))"};

    @Create
    public void init() {
        sortProperty = "costCenter.groupCode,costCenter.code";
    }

    @Override
    public String getEjbql() {
        return "select costCenter from CostCenter costCenter " +
                "where costCenter.code in (select inventoryDetail.costCenterCode from InventoryDetail inventoryDetail where inventoryDetail.warehouseCode=  #{inventoryAction.warehouse.warehouseCode}) ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}

