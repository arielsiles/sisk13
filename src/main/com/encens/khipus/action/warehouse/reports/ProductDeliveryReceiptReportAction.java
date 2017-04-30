package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.warehouse.InventoryMovement;
import com.encens.khipus.model.warehouse.InventoryMovementPK;
import com.encens.khipus.model.warehouse.ProductDelivery;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate product delivery receipt report
 *
 * @author
 * @version $Id: ProductDeliveryReceiptReportAction.java  23-sep-2010 18:25:14$
 */
@Name("productDeliveryReceiptReportAction")
@Scope(ScopeType.PAGE)
public class ProductDeliveryReceiptReportAction extends GenericReportAction {

    @In
    private User currentUser;
    @In
    private WarehouseService warehouseService;


    private ProductDelivery productDelivery;
    private WarehouseVoucher warehouseVoucher;

    @Restrict("#{s:hasPermission('PRODUCTDELIVERY','VIEW')}")
    public void generateReport(ProductDelivery productDelivery) {
        log.debug("Generate ProductDeliveryReceiptReportAction......");

        setReportFormat(ReportFormat.PDF);

        Map params = new HashMap();
        //set restrictions filters
        setProductDelivery(productDelivery);
        if (productDelivery.getWarehouseVoucher() != null) {
            setWarehouseVoucher(productDelivery.getWarehouseVoucher());

            //add detail sub report
            addVoucherMovementDetailSubReport(params);
        }
        params.putAll(getReportParams(getWarehouseVoucher()));
        super.generateReport("productDeliveryReceiptReport", "/warehouse/reports/productDeliveryReceiptReport.jrxml", PageFormat.LETTER, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.productDeliveryReceipt.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "productDelivery.id," +
                "productDelivery.invoiceNumber," +
                "soldProduct.firstName," +
                "soldProduct.secondName," +
                "soldProduct.names," +
                "soldProduct.personalIdentification," +
                "branch.description," +
                "warehouseVoucher.date," +
                "warehouseVoucher.number," +
                "warehouseVoucher.state," +
                "executorUnit.executorUnitCode," +
                "organization.name," +
                "costCenter.code," +
                "costCenter.description," +
                "warehouse.warehouseCode," +
                "warehouse.name," +
                "responsible.lastName," +
                "responsible.maidenName," +
                "responsible.firstName" +
                " FROM ProductDelivery productDelivery" +
                " LEFT JOIN productDelivery.soldProductList soldProduct" +
                " LEFT JOIN soldProduct.branch branch" +
                " LEFT JOIN productDelivery.warehouseVoucher warehouseVoucher" +
                " LEFT JOIN warehouseVoucher.executorUnit executorUnit" +
                " LEFT JOIN executorUnit.organization organization" +
                " LEFT JOIN warehouseVoucher.costCenter costCenter" +
                " LEFT JOIN warehouseVoucher.warehouse warehouse" +
                " LEFT JOIN warehouseVoucher.responsible responsible";
    }

    @Create
    public void init() {
        restrictions = new String[]{"productDelivery = #{productDeliveryReceiptReportAction.productDelivery}"};
        sortProperty = "productDelivery.id";
    }


    /**
     * get report params
     *
     * @return Map
     */
    private Map<String, Object> getReportParams(WarehouseVoucher warehouseVoucher) {
        InventoryMovement inventoryMovement = findInventoryMovement(warehouseVoucher);

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userLoginParam", currentUser.getEmployee().getFullName());
        paramMap.put("descriptionParam", inventoryMovement != null ? paramAsString(inventoryMovement.getDescription()) : "");
        paramMap.put("hasWarehouseVoucherParam", String.valueOf(warehouseVoucher != null));

        return paramMap;
    }

    /**
     * add voucher movement detail sub report in main report
     *
     * @param mainReportParams
     */
    private void addVoucherMovementDetailSubReport(Map mainReportParams) {
        log.debug("Generating addVoucherMovementDetailSubReport.............................");

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "movementDetail.productItem.productItemCode," +
                "movementDetail.productItem.name," +
                "movementDetail.quantity," +
                "movementDetail.measureUnit.name" +
                " FROM MovementDetail movementDetail" +
                " WHERE movementDetail.sourceId is null";

        String[] restrictions = new String[]{
                "movementDetail.companyNumber=#{productDeliveryReceiptReportAction.warehouseVoucher.id.companyNumber}",
                "movementDetail.transactionNumber=#{productDeliveryReceiptReportAction.warehouseVoucher.id.transactionNumber}",
                "movementDetail.state=#{productDeliveryReceiptReportAction.warehouseVoucher.state}"};

        String orderBy = "movementDetail.productItem.name";

        //generate the sub report
        String subReportKey = "PRODDELIVERYMOVDETAILSUBREPORT";
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/warehouse/reports/productDeliveryMovementDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }


    private String paramAsString(Object value) {
        return value != null ? value.toString() : "";
    }

    private InventoryMovement findInventoryMovement(WarehouseVoucher warehouseVoucher) {
        InventoryMovement inventoryMovement = null;
        if (warehouseVoucher != null) {
            InventoryMovementPK inventoryMovementPK = new InventoryMovementPK(warehouseVoucher.getId().getCompanyNumber(),
                    warehouseVoucher.getId().getTransactionNumber(),
                    warehouseVoucher.getState().name());
            inventoryMovement = warehouseService.findInventoryMovement(inventoryMovementPK);
        }
        return inventoryMovement;
    }

    public ProductDelivery getProductDelivery() {
        return productDelivery;
    }

    public void setProductDelivery(ProductDelivery productDelivery) {
        this.productDelivery = productDelivery;
    }

    public WarehouseVoucher getWarehouseVoucher() {
        return warehouseVoucher;
    }

    public void setWarehouseVoucher(WarehouseVoucher warehouseVoucher) {
        this.warehouseVoucher = warehouseVoucher;
    }
}
