package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate warehouse evolution report
 *
 * @author
 * @version $Id: WarehouseEvolutionReportAction.java  21-abr-2010 12:13:33$
 */
@Name("warehouseEvolutionReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEEVOLUTIONREPORT','VIEW')}")
public class WarehouseEvolutionReportAction extends GenericReportAction {

    private BusinessUnit businessUnit;
    private Warehouse warehouse;
    private ProductItem productItem;
    private Date initDate;
    private Date endDate;

    public void generateReport() {
        log.debug("Generate WarehouseEvolutionReportAction......");
        //add default filters

        Map params = new HashMap();
        params.putAll(getReportParamsInfo());

        super.generateReport("warehouseEvolutionReport", "/warehouse/reports/warehouseEvolutionReport.jrxml", MessageUtils.getMessage("Reports.warehouseEvolution.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "itemGroup.groupCode," +
                "subGroup.subGroupCode," +
                "warehouse.warehouseCode," +
                "productItem.productItemCode," +
                "itemGroup.name," +
                "subGroup.name," +
                "warehouse.name," +
                "productItem.name," +
                "measureUnit.name" +
                " FROM ProductItem productItem" +
                " LEFT JOIN productItem.subGroup subGroup" +
                " LEFT JOIN subGroup.group itemGroup" +
                " LEFT JOIN productItem.inventories inventory" +
                " LEFT JOIN inventory.warehouse warehouse" +
                " LEFT JOIN warehouse.executorUnit executorUnit" +
                " LEFT JOIN productItem.usageMeasureUnit measureUnit" +
                " LEFT JOIN productItem.movementDetailList movementDetail";

    }

    @Create
    public void init() {
        restrictions = new String[]{
                "movementDetail.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.warehouse.WarehouseVoucherState','APR')}",
                "executorUnit=#{warehouseEvolutionReportAction.businessUnit}",
                "warehouse=#{warehouseEvolutionReportAction.warehouse}",
                "productItem=#{warehouseEvolutionReportAction.productItem}",
                "movementDetail.movementDetailDate >= #{warehouseEvolutionReportAction.initDate}",
                "movementDetail.movementDetailDate <= #{warehouseEvolutionReportAction.endDate}"
        };

        sortProperty = "itemGroup.groupCode,subGroup.subGroupCode,warehouse.warehouseCode,productItem.productItemCode";
    }

    /**
     * compose required report params
     *
     * @return Map
     */
    private Map getReportParamsInfo() {
        Map paramMap = new HashMap();
        String dateRangeInfo = "";

        if (initDate != null) {
            dateRangeInfo = dateRangeInfo + MessageUtils.getMessage("Common.dateFrom") + " " + DateUtils.format(initDate, MessageUtils.getMessage("patterns.date")) + " ";
            paramMap.put("initPeriodDateParam", initDate);
        }

        if (endDate != null) {
            dateRangeInfo = dateRangeInfo + MessageUtils.getMessage("Common.dateTo") + " " + DateUtils.format(endDate, MessageUtils.getMessage("patterns.date"));
            paramMap.put("endPeriodDateParam", endDate);
        }

        paramMap.put("dateRangeParam", dateRangeInfo);
        return paramMap;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
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

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
