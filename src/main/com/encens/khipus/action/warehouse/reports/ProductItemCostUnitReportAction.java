package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.warehouse.WarehouseSearchDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate product item cost unit report
 *
 * @author
 * @version $Id: ProductItemCostUnitReportAction.java  10-mar-2010 12:18:41$
 */
@Name("productItemCostUnitReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCTITEMCOSTUNITREPORT','VIEW')}")
public class ProductItemCostUnitReportAction extends GenericReportAction {

    @In(required = false)
    private WarehouseSearchDataModel warehouseSearchDataModel;
    private ProductItemState productItemState;
    private BusinessUnit executorUnit;
    private Warehouse warehouse;
    private ProductItem productItem;
    private Group group;
    private SubGroup subGroup;

    public void generateReport() {
        log.debug("Generate report in ProductItemCostUnitReportAction...");
        Map params = new HashMap();

        String reportPath = "/warehouse/reports/productItemCostUnitReportNeto.jrxml";

        if (warehouse.getWarehouseCode().equals("2"))
            reportPath = "/warehouse/reports/productItemCostUnitReport.jrxml";

        super.generateReport("productItemReport", reportPath, PageFormat.LETTER, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.productItemUnitCost.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "productItem.id.productItemCode," +
                "productItem.name," +
                "productItem.investmentAmount," +
                "productItem.unitCost, " +
                "productItem.cu, " +
                "subGroup, " +
                "group1 " +    //Group is a reserved word
                " FROM Inventory inventory " +
                "LEFT JOIN inventory.productItem productItem " +
                "LEFT JOIN inventory.warehouse warehouse " +
                "LEFT JOIN warehouse.executorUnit executorUnit " +
                "LEFT JOIN productItem.subGroup subGroup " +
                "LEFT JOIN subGroup.group group1";
    }

    @Create
    public void init() {
        restrictions = new String[]{"productItem.state = #{productItemCostUnitReportAction.productItemState}",
                "warehouse = #{productItemCostUnitReportAction.warehouse}",
                "executorUnit = #{productItemCostUnitReportAction.executorUnit}",
                "productItem = #{productItemCostUnitReportAction.productItem}",
                "group1 = #{productItemCostUnitReportAction.group}",
                "subGroup = #{productItemCostUnitReportAction.subGroup}"
        };
        //sortProperty = "productItem.groupCode, productItem.subGroupCode, productItem.name";
        sortProperty = "productItem.groupCode, productItem.name";

    }

    public void cleanMainOptions() {
        setWarehouse(null);
    }

    public boolean isExecutorUnitFieldSelected() {
        return null != this.executorUnit;
    }

    public boolean isGroupFieldSelected() {
        return null != this.group;
    }

    public void cleanWarehouseField() {
        this.warehouse = null;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void cleanProductItemField() {
        this.productItem = null;
    }

    public void assignWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public ProductItemState getProductItemState() {
        return productItemState;
    }

    public void setProductItemState(ProductItemState productItemState) {
        this.productItemState = productItemState;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        if (null != group) {
            this.group = getEntityManager().find(Group.class, group.getId());
            subGroup = null;
        } else {
            this.group = null;
        }
    }

    public void assignGroup(Group group) {
        setGroup(group);
    }

    public void cleanGroupField() {
        setGroup(null);
        subGroup = null;
    }

    public String getGroupInfo() {
        String res = null;
        if (group != null) {
            res = group.getFullName();
        }
        return (res);
    }

    public SubGroup getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(SubGroup subGroup) {
        if (subGroup != null) {
            this.subGroup = getEntityManager().find(SubGroup.class, subGroup.getId());
            this.group = getEntityManager().find(Group.class, this.subGroup.getGroup().getId());
        } else {
            this.subGroup = null;
        }
    }

    public void cleanSubGroup() {
        subGroup = null;
    }

}
