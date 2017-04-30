package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate voucher document
 *
 * @author
 * @version 3.0
 */
@Name("voucherDocumentReportAction")
@Scope(ScopeType.PAGE)
public class VoucherDocumentReportAction extends GenericReportAction {

    @In
    User currentUser;
    @In
    private SessionUser sessionUser;
    @In
    protected WarehouseService warehouseService;
    @In
    private MovementDetailService movementDetailService;

    private WarehouseVoucher warehouseVoucher;

    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','VIEW')}")
    public void generateReport(WarehouseVoucher warehouseVoucher) {
        log.debug("Generate VoucherDocumentReportAction......" + warehouseVoucher);

        setWarehouseVoucher(getEntityManager().find(WarehouseVoucher.class, warehouseVoucher.getId()));
        String templatePath = "/warehouse/reports/voucherCommonDocumentReport.jrxml";
        String fileName = getWarehouseVoucher().getDocumentType() != null ? getWarehouseVoucher().getDocumentType().getName() : "voucherDocument";

        Map params = new HashMap();
        if (getWarehouseVoucher().isExecutorUnitTransfer()) {
            templatePath = "/warehouse/reports/voucherExecutorUnitTransferenceDocReport.jrxml";
            params.putAll(getExecutorUintTransferenceDocumentParamsInfo(getWarehouseVoucher()));
        } else if (getWarehouseVoucher().isTransfer()) {
            templatePath = "/warehouse/reports/voucherTransferenceDocReport.jrxml";
            params.putAll(getTransferenceDocumentParamsInfo(getWarehouseVoucher()));
        } else if (getWarehouseVoucher().isReception()) {
            templatePath = "/warehouse/reports/voucherReceptionDocReport.jrxml";
            params.putAll(getReceptionDocumentParamsInfo(getWarehouseVoucher()));
        } else {
            params.putAll(getCommonDocumentParamsInfo(getWarehouseVoucher()));
        }

        setReportFormat(ReportFormat.PDF);
        //add sub report
        addVoucherMovementDetailSubReport(params);
        super.generateReport("voucherDocument", templatePath, PageFormat.LETTER, PageOrientation.PORTRAIT, fileName, params);
    }

    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','VIEW')}")
    public void generateReport(MovementDetail movementDetail) {
        try {
            movementDetail = warehouseService.findById(MovementDetail.class, movementDetail.getId());
            WarehouseVoucher warehouseVoucher = warehouseService.findWarehouseVoucher(new WarehouseVoucherPK(movementDetail.getCompanyNumber(), movementDetail.getInventoryMovement().getId().getTransactionNumber()));
            generateReport(warehouseVoucher);
        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("WarehouseVoucher not found", e);
        } catch (EntryNotFoundException e) {
            log.debug("MovementDetail not found", e);
        }
    }

    @Override
    protected String getEjbql() {
        return "";
    }

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    /**
     * add movement detail sub report in main report
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
                "movementDetail.measureUnit.name," +
                "movementDetail.amount," +
                "movementDetail.warning" +
                " FROM MovementDetail movementDetail" +
                " WHERE movementDetail.sourceId is null";

        String[] restrictions = new String[]{
                "movementDetail.companyNumber=#{voucherDocumentReportAction.warehouseVoucher.id.companyNumber}",
                "movementDetail.transactionNumber=#{voucherDocumentReportAction.warehouseVoucher.id.transactionNumber}",
                "movementDetail.state=#{voucherDocumentReportAction.warehouseVoucher.state}"};

        String orderBy = "movementDetail.productItem.name";

        //generate the sub report
        String subReportKey = "MOVEMENTDETAILSUBREPORT";
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/warehouse/reports/movementDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * get common params
     *
     * @return Map
     */
    private Map<String, Object> getCommonParams(WarehouseVoucher warehouseVoucher) {
        InventoryMovement inventoryMovement = findInventoryMovement(warehouseVoucher);

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userLoginParam", currentUser.getEmployee().getFullName());

        paramMap.put("invoiceNumberParam", warehouseVoucher.getPurchaseOrder() != null ? paramAsString(warehouseVoucher.getPurchaseOrder().getInvoiceNumber()) : "");
        paramMap.put("documentTypeParam", warehouseVoucher.getDocumentType() != null ? warehouseVoucher.getDocumentType().getName() : "");
        paramMap.put("stateParam", warehouseVoucher.getState() != null ? MessageUtils.getMessage(warehouseVoucher.getState().getResourceKey()) : "");
        paramMap.put("voucherDateParam", warehouseVoucher.getDate());
        paramMap.put("descriptionParam", inventoryMovement != null ? paramAsString(inventoryMovement.getDescription()) : "");
        paramMap.put("numberParam", paramAsString(warehouseVoucher.getNumber()));
        paramMap.putAll(getVoucherTotalsParams(warehouseVoucher));

        return paramMap;
    }

    private Map<String, Object> getVoucherTotalsParams(WarehouseVoucher warehouseVoucher) {
        BigDecimal totalNet = movementDetailService.sumWarehouseVoucherMovementDetailAmount(warehouseVoucher.getId().getCompanyNumber(), warehouseVoucher.getState(), warehouseVoucher.getId().getTransactionNumber());

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("totalNetParam", formatDecimalNumber(totalNet));
        paramMap.put("discountParam", formatDecimalNumber(BigDecimal.ZERO) + " (" + formatDecimalNumber(BigDecimal.ZERO) + "%)");
        paramMap.put("totalParam", formatDecimalNumber(totalNet));
        return paramMap;
    }

    private String formatDecimalNumber(BigDecimal bigDecimal) {
        return (bigDecimal != null) ? FormatUtils.formatNumber(bigDecimal, MessageUtils.getMessage("patterns.decimalNumber"), sessionUser.getLocale()) : "";
    }


    /**
     * set voucher data info as params
     *
     * @param warehouseVoucher
     * @return
     */
    private Map<String, Object> getCommonDocumentParamsInfo(WarehouseVoucher warehouseVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.putAll(getCommonParams(warehouseVoucher));
        paramMap.put("executorUnitParam", warehouseVoucher.getExecutorUnit() != null ? warehouseVoucher.getExecutorUnit().getFullName() : "");
        paramMap.put("warehouseParam", warehouseVoucher.getWarehouse() != null ? warehouseVoucher.getWarehouse().getFullName() : "");
        paramMap.put("costCenterParam", warehouseVoucher.getCostCenter() != null ? warehouseVoucher.getCostCenter().getFullName() : "");
        paramMap.put("responsibleParam", warehouseVoucher.getResponsible() != null ? warehouseVoucher.getResponsible().getFullName() : "");

        //only to output or consumption voucher types
        paramMap.put("isOutputVoucherParam", String.valueOf(warehouseVoucher.isOutput() || warehouseVoucher.isConsumption()));
        paramMap.put("petitionerParam", warehouseVoucher.getPetitionerJobContract() != null ? warehouseVoucher.getPetitionerJobContract().getContract().getEmployee().getFullName() : "");
        paramMap.put("petitionerChargeParam", warehouseVoucher.getPetitionerJobContract() != null ? warehouseVoucher.getPetitionerJobContract().getJob().getCharge().getName() : "");
        paramMap.put("petitionerAreaParam", warehouseVoucher.getPetitionerJobContract() != null ? warehouseVoucher.getPetitionerJobContract().getJob().getOrganizationalUnit().getFullName() : "");

        return paramMap;
    }

    /**
     * set voucher data info as params
     *
     * @param warehouseVoucher
     * @return
     */
    private Map<String, Object> getReceptionDocumentParamsInfo(WarehouseVoucher warehouseVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.putAll(getCommonParams(warehouseVoucher));
        paramMap.put("executorUnitParam", warehouseVoucher.getExecutorUnit() != null ? warehouseVoucher.getExecutorUnit().getFullName() : "");
        paramMap.put("warehouseParam", warehouseVoucher.getWarehouse() != null ? warehouseVoucher.getWarehouse().getFullName() : "");
        paramMap.put("costCenterParam", warehouseVoucher.getCostCenter() != null ? warehouseVoucher.getCostCenter().getFullName() : "");
        paramMap.put("responsibleParam", warehouseVoucher.getResponsible() != null ? warehouseVoucher.getResponsible().getFullName() : "");

        //only when voucher is created from purchase order
        PurchaseOrder purchaseOrder = warehouseVoucher.getPurchaseOrder();
        boolean hasPurchaseOrder = purchaseOrder != null;
        paramMap.put("isWithPurchaseOrderParam", String.valueOf(hasPurchaseOrder));
        paramMap.put("orderNumberParam", hasPurchaseOrder ? paramAsString(purchaseOrder.getOrderNumber()) : "");
        paramMap.put("providerNameParam", hasPurchaseOrder && purchaseOrder.getProvider() != null ? paramAsString(purchaseOrder.getProvider().getFullName()) : "");

        return paramMap;
    }

    /**
     * set voucher data info as params
     *
     * @param warehouseVoucher
     * @return
     */
    private Map<String, Object> getTransferenceDocumentParamsInfo(WarehouseVoucher warehouseVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.putAll(getCommonParams(warehouseVoucher));
        paramMap.put("executorUnitParam", warehouseVoucher.getExecutorUnit() != null ? warehouseVoucher.getExecutorUnit().getFullName() : "");
        paramMap.put("targetWarehouseParam", warehouseVoucher.getTargetWarehouse() != null ? warehouseVoucher.getTargetWarehouse().getFullName() : "");
        paramMap.put("costCenterParam", warehouseVoucher.getCostCenter() != null ? warehouseVoucher.getCostCenter().getFullName() : "");
        paramMap.put("targetResponsibleParam", warehouseVoucher.getTargetResponsible() != null ? warehouseVoucher.getTargetResponsible().getFullName() : "");
        paramMap.put("sourceWarehouseParam", warehouseVoucher.getWarehouse() != null ? warehouseVoucher.getWarehouse().getFullName() : "");
        paramMap.put("sourceResponsibleParam", warehouseVoucher.getResponsible() != null ? warehouseVoucher.getResponsible().getFullName() : "");

        return paramMap;
    }

    /**
     * set voucher data info as params
     *
     * @param warehouseVoucher
     * @return
     */
    private Map<String, Object> getExecutorUintTransferenceDocumentParamsInfo(WarehouseVoucher warehouseVoucher) {
        Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.putAll(getCommonParams(warehouseVoucher));
        paramMap.put("sourceExecutorUnitParam", warehouseVoucher.getExecutorUnit() != null ? warehouseVoucher.getExecutorUnit().getFullName() : "");
        paramMap.put("targetExecutorUnitParam", warehouseVoucher.getTargetExecutorUnit() != null ? warehouseVoucher.getTargetExecutorUnit().getFullName() : "");
        paramMap.put("sourceCostCenterParam", warehouseVoucher.getCostCenter() != null ? warehouseVoucher.getCostCenter().getFullName() : "");
        paramMap.put("targetCostCenterParam", warehouseVoucher.getTargetCostCenter() != null ? warehouseVoucher.getTargetCostCenter().getFullName() : "");
        paramMap.put("sourceWarehouseParam", warehouseVoucher.getWarehouse() != null ? warehouseVoucher.getWarehouse().getFullName() : "");
        paramMap.put("targetWarehouseParam", warehouseVoucher.getTargetWarehouse() != null ? warehouseVoucher.getTargetWarehouse().getFullName() : "");
        paramMap.put("sourceResponsibleParam", warehouseVoucher.getResponsible() != null ? warehouseVoucher.getResponsible().getFullName() : "");
        paramMap.put("targetResponsibleParam", warehouseVoucher.getTargetResponsible() != null ? warehouseVoucher.getTargetResponsible().getFullName() : "");

        return paramMap;
    }

    private String paramAsString(Object value) {
        return value != null ? value.toString() : "";
    }

    private InventoryMovement findInventoryMovement(WarehouseVoucher warehouseVoucher) {
        InventoryMovementPK inventoryMovementPK = new InventoryMovementPK(warehouseVoucher.getId().getCompanyNumber(),
                warehouseVoucher.getId().getTransactionNumber(),
                warehouseVoucher.getState().name());

        return warehouseService.findInventoryMovement(inventoryMovementPK);
    }

    public WarehouseVoucher getWarehouseVoucher() {
        return warehouseVoucher;
    }

    public void setWarehouseVoucher(WarehouseVoucher warehouseVoucher) {
        this.warehouseVoucher = warehouseVoucher;
    }
}
