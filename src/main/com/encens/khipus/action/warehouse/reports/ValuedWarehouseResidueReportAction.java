package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("valuedWarehouseResidueReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('VALUEDWAREHOUSERESIDUEREPORT','VIEW')}")
public class ValuedWarehouseResidueReportAction extends GenericReportAction {
    private BusinessUnit businessUnit;
    private ProductItem productItem;
    private Warehouse warehouse;
    private BigDecimal initUnitaryBalance;
    private BigDecimal endUnitaryBalance;

    @Create
    public void init() {
        restrictions = new String[]{
                "executorUnit=#{valuedWarehouseResidueReportAction.businessUnit}",
                "warehouse=#{valuedWarehouseResidueReportAction.warehouse}",
                "productItem=#{valuedWarehouseResidueReportAction.productItem}",
                "inventory.unitaryBalance >= #{valuedWarehouseResidueReportAction.initUnitaryBalance}",
                "inventory.unitaryBalance <= #{valuedWarehouseResidueReportAction.endUnitaryBalance}"
        };
        sortProperty = "warehouse.id.warehouseCode, warehouse.id.companyNumber, productItem.productItemCode";
    }

    @Override
    protected String getEjbql() {
        return "SELECT warehouse.id, " +
                "      warehouse.name , " +
                "      productItem.id, " +
                "      productItem.productItemCode, " +
                "      productItem.name, " +
                "      productItem.usageMeasureUnit, " +
                "      productItem.unitCost, " +
                "      inventory.unitaryBalance " +
                "FROM  ProductItem productItem " +
                "      JOIN productItem.inventories inventory " +
                "      LEFT JOIN inventory.warehouse warehouse " +
                "      LEFT JOIN warehouse.executorUnit executorUnit";

    }

    public void generateReport() {
        log.debug("Generating valued Warehouse Residue Report report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "valuedWarehouseResidueReport",
                "/warehouse/reports/valuedWarehouseResidueReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("ValuedWarehouseResidueReport.report.page.title"),
                reportParameters);
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void cleanProductItem() {
        this.productItem = null;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void clearWarehouse() {
        setWarehouse(null);
    }

    public void assignWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public BigDecimal getInitUnitaryBalance() {
        return initUnitaryBalance;
    }

    public void setInitUnitaryBalance(BigDecimal initUnitaryBalance) {
        this.initUnitaryBalance = initUnitaryBalance;
    }

    public BigDecimal getEndUnitaryBalance() {
        return endUnitaryBalance;
    }

    public void setEndUnitaryBalance(BigDecimal endUnitaryBalance) {
        this.endUnitaryBalance = endUnitaryBalance;
    }
}
