package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Charge;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
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
 * Action to generate warehouse Kardex report
 *
 * @author
 * @version $Id: KardexReportAction.java  15-abr-2010 19:00:22$
 */
@Name("kardexReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('KARDEXREPORT','VIEW')}")
public class KardexReportAction extends GenericReportAction {

    private WarehouseVoucherState warehouseVoucherState;
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private ProductItem productItem;
    private String petitionerIdNumber;
    private Warehouse warehouse;
    private Charge charge;
    private Date initDate;
    private Date endDate;

    public void generateReport() {
        log.debug("Generate KardexReportAction......");
        //add default filters
        setWarehouseVoucherState(WarehouseVoucherState.APR);

        Map params = new HashMap();
        params.putAll(getReportParamsInfo());

        super.generateReport("kardexReport", "/warehouse/reports/kardexReport.jrxml", MessageUtils.getMessage("Reports.kardex.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "movementDetail.productItemCode," +
                "productItem.name," +
                "warehouse.warehouseCode," +
                "warehouse.name," +
                "movementDetail.movementDetailDate," +
                "businessUnit.executorUnitCode," +
                "businessUnit.publicity," +
                "movementDetail.quantity," +
                "movementDetail.amount," +
                "movementDetail.unitPurchasePrice," +
                "movementDetail.purchasePrice," +
                "movementDetail.state," +
                "movementDetail.movementType," +
                "costCenter.code," +
                "costCenter.description," +
                "warehouseVoucher.number," +
                "documentType.documentCode," +
                "documentType.name," +
                "measureUnit.name," +
                "inventoryMovement.description," +
                "petitioner.lastName," +
                "petitioner.maidenName," +
                "petitioner.firstName," +
                "petitionerCharge.name," +
                "movementDetail.warning" +
                " FROM MovementDetail movementDetail" +
                " LEFT JOIN movementDetail.productItem productItem" +
                " LEFT JOIN movementDetail.warehouse warehouse" +
                " LEFT JOIN movementDetail.executorUnit businessUnit" +
                " LEFT JOIN movementDetail.costCenter costCenter" +
                " LEFT JOIN movementDetail.measureUnit measureUnit" +
                " LEFT JOIN movementDetail.inventoryMovement inventoryMovement" +
                " LEFT JOIN inventoryMovement.warehouseVoucher warehouseVoucher" +
                " LEFT JOIN warehouseVoucher.documentType documentType" +
                " LEFT JOIN warehouseVoucher.petitionerJobContract petitionerJobContract" +
                " LEFT JOIN petitionerJobContract.contract contract" +
                " LEFT JOIN contract.employee petitioner" +
                " LEFT JOIN petitionerJobContract.job job" +
                " LEFT JOIN job.charge petitionerCharge";

    }

    @Create
    public void init() {
        restrictions = new String[]{
                "productItem=#{kardexReportAction.productItem}",
                "businessUnit=#{kardexReportAction.businessUnit}",
                "costCenter=#{kardexReportAction.costCenter}",
                "petitioner.idNumber=#{kardexReportAction.petitionerIdNumber}",
                "petitionerCharge=#{kardexReportAction.charge}",
                "warehouse=#{kardexReportAction.warehouse}",
                "movementDetail.state=#{kardexReportAction.warehouseVoucherState}",
                "movementDetail.movementDetailDate >= #{kardexReportAction.initDate}",
                "movementDetail.movementDetailDate <= #{kardexReportAction.endDate}"
        };
        sortProperty = "productItem.name,movementDetail.movementDetailDate,movementDetail.id";
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
            //add init date param to calculate initial quantity values
            paramMap.put("initPeriodDateParam", initDate);
        }

        if (endDate != null) {
            dateRangeInfo = dateRangeInfo + MessageUtils.getMessage("Common.dateTo") + " " + DateUtils.format(endDate, MessageUtils.getMessage("patterns.date"));
        }

        paramMap.put("dateRangeParam", dateRangeInfo);
        return paramMap;
    }

    public WarehouseVoucherState getWarehouseVoucherState() {
        return warehouseVoucherState;
    }

    public void setWarehouseVoucherState(WarehouseVoucherState warehouseVoucherState) {
        this.warehouseVoucherState = warehouseVoucherState;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void assignCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        costCenter = null;
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

    public String getPetitionerIdNumber() {
        return petitionerIdNumber;
    }

    public void setPetitionerIdNumber(String petitionerIdNumber) {
        this.petitionerIdNumber = petitionerIdNumber;
    }

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public void clearCharge() {
        setCharge(null);
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
