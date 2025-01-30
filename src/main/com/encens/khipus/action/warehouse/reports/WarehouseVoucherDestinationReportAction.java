package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("warehouseVoucherDestinationReportAction")
@Scope(ScopeType.PAGE)
public class WarehouseVoucherDestinationReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

    private WarehouseDocumentType documentType;

    private ProductItem productItem;
    private Warehouse warehouse;
    private WarehouseVoucherState state = WarehouseVoucherState.APR;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    @In(create = true)
    KardexProductMovementAction kardexProductMovementAction;


    @Create
    public void init() {
        restrictions = new String[]{
                "warehouseVoucher.date >= #{warehouseVoucherDestinationReportAction.startDate}",
                "warehouseVoucher.date <= #{warehouseVoucherDestinationReportAction.endDate}",
                "warehouseVoucher.state = #{warehouseVoucherDestinationReportAction.state}",
                "warehouseVoucher.warehouseCode = #{warehouseVoucherDestinationReportAction.warehouse.warehouseCode}",
                "warehouseVoucher.documentCode = #{warehouseVoucherDestinationReportAction.documentType.documentCode}"
        };

        sortProperty = "warehouseVoucher.date";
    }


    protected String getEjbql() {

        return  "SELECT " +
                "   warehouseVoucher.date as date, " +
                "   warehouseVoucher.number as number, " +
                "   movementDetail.productItemCode as code, " +
                "   productItem.name as productName, " +
                "   movementDetail.measureCode as med, " +
                "   movementDetail.quantity as quantity, " +
                "   movementDetail.unitCost as unitCost, " +
                "   movementDetail.amount as amount, " +
                "   subGroup.group.name as groupName," +
                "   warehouseVoucher as warehouseVoucher, " +
                "   destination.code as destCode, " +
                "   destination.name as destName " +
                "FROM MovementDetail movementDetail " +
                "   LEFT JOIN movementDetail.inventoryMovement.warehouseVoucher warehouseVoucher " +
                "   LEFT JOIN movementDetail.inventoryMovement.warehouseVoucher.destination destination" +
                "   LEFT JOIN movementDetail.productItem productItem " +
                "   LEFT JOIN productItem.subGroup subGroup";

    }


    public void generateReport() {

        log.debug("generating Item Destination Report................................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        String period = DateUtils.format(startDate, "dd/MM/yyyy") + " - " + DateUtils.format(endDate, "dd/MM/yyyy");

        String reportTitle = "DESTINO DE VALES DE ALMACEN - " + documentType.getName();

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", reportTitle);
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());

        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("warehouse", warehouse.getFullName());
        paramMap.put("period", period);

        super.generateReport(
                "itemDestinationReport",
                "/warehouse/reports/warehouseVoucherDestinationReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("Reports.kardex.movement.warehouseVoucherDestination.titleReport"),
                paramMap);

    }


    public void cleanProductItem() {
        this.productItem = null;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void cleanWarehouseField() {
        warehouse = null;
    }

    public WarehouseDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(WarehouseDocumentType documentType) {
        this.documentType = documentType;
    }

    public WarehouseVoucherState getState() {
        return state;
    }

    public void setState(WarehouseVoucherState state) {
        this.state = state;
    }
}