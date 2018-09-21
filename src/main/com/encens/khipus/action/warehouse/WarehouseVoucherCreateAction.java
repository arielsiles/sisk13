package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.warehouse.InventoryException;
import com.encens.khipus.exception.warehouse.MonthProcessValidException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherPendantException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.finances.MeasureUnitPk;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.warehouse.ApprovalWarehouseVoucherService;
import com.encens.khipus.service.warehouse.MonthProcessService;
import com.encens.khipus.service.warehouse.WarehouseCatalogService;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("warehouseVoucherCreateAction")
@Scope(ScopeType.CONVERSATION)
public class WarehouseVoucherCreateAction extends WarehouseVoucherGeneralAction {

    private MovementDetailUIController uiController = null;

    private List<MovementDetail> movementDetails = new ArrayList<MovementDetail>();

    private List<ProductItemPK> selectedProductItems = new ArrayList<ProductItemPK>();

    @In
    private WarehouseCatalogService warehouseCatalogService;

    @In(value = "warehouseVoucherUpdateAction")
    private WarehouseVoucherUpdateAction warehouseVoucherUpdateAction;

    @In(value = "monthProcessService")
    private MonthProcessService monthProcessService;

    @In
    private ApprovalWarehouseVoucherService approvalWarehouseVoucherService;

    private ProductItem productItemFrom;
    private ProductItem productItemTo;
    private BigDecimal quantity;
    private String description;


    @Override
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','CREATE')}")
    public String create() {
        String validationsOutcome = validations();
        if (!Outcome.SUCCESS.equals(validationsOutcome)) {
            return validationsOutcome;
        }
        // reset movement detail quantity mappings to manage warnings and warning column content
        resetValidateQuantityMappings();
        try {
            warehouseService.saveWarehouseVoucher(warehouseVoucher, inventoryMovement, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);

            warehouseVoucherUpdateAction.putWarehouseVoucher(warehouseVoucher.getId());
            showMovementDetailWarningMessages();
            return Outcome.SUCCESS;
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.FAIL;
        }
    }

    @End
    public String createTransfer(){


        /** Transfer from **/
        WarehouseVoucher warehouseVoucherFrom = new WarehouseVoucher();
        warehouseVoucherFrom.setCostCenter(warehouseVoucher.getCostCenter());
        warehouseVoucherFrom.setExecutorUnit(warehouseVoucher.getExecutorUnit());
        warehouseVoucherFrom.setDate(new Date());
        warehouseVoucherFrom.setDocumentType(warehouseService.findWarehouseDocumentType(WarehouseVoucherType.C));
        warehouseVoucherFrom.setState(WarehouseVoucherState.PEN);
        warehouseVoucherFrom.setWarehouse(warehouseVoucher.getWarehouse());


        InventoryMovement inventoryMovementFrom = new InventoryMovement();
        inventoryMovementFrom.setCreationDate(new Date());
        inventoryMovementFrom.setDescription(description);
        inventoryMovementFrom.setMovementDate(new Date());

        MovementDetail movementDetail = new MovementDetail();
        movementDetail.setMovementDetailDate(new Date());
        movementDetail.setProductItem(productItemFrom);
        movementDetail.setProductItemCode(productItemFrom.getProductItemCode());
        movementDetail.setQuantity(quantity);
        movementDetail.setMeasureCode(productItemFrom.getUsageMeasureCode());
        movementDetail.setWarehouse(warehouseVoucher.getWarehouse());
        movementDetail.setUnitCost(productItemFrom.getUnitCost());
        movementDetail.setAmount(BigDecimalUtil.multiply(quantity, productItemFrom.getUnitCost(), 6));
        movementDetail.setUnitPurchasePrice(productItemFrom.getCu());
        movementDetail.setPurchasePrice(BigDecimalUtil.multiply(quantity, productItemFrom.getCu(), 6));
        movementDetail.setMovementType(MovementDetailType.S);
        movementDetail.setCashAccount(productItemFrom.getCashAccount());


        /** Transfer to **/
        WarehouseVoucher warehouseVoucherTo = new WarehouseVoucher();
        warehouseVoucherTo.setCostCenter(warehouseVoucher.getCostCenter());
        warehouseVoucherTo.setExecutorUnit(warehouseVoucher.getExecutorUnit());
        warehouseVoucherTo.setDate(new Date());
        warehouseVoucherTo.setDocumentType(warehouseService.findWarehouseDocumentType(WarehouseVoucherType.R));
        warehouseVoucherTo.setState(WarehouseVoucherState.PEN);
        warehouseVoucherTo.setWarehouse(warehouseVoucher.getWarehouse());

        InventoryMovement inventoryMovementTo = new InventoryMovement();
        inventoryMovementTo.setCreationDate(new Date());
        inventoryMovementTo.setDescription(description);
        inventoryMovementTo.setMovementDate(new Date());


        MovementDetail movementDetail2 = new MovementDetail();
        movementDetail2.setMovementDetailDate(new Date());
        movementDetail2.setProductItem(productItemTo);
        movementDetail2.setProductItemCode(productItemTo.getProductItemCode());
        movementDetail2.setQuantity(quantity);
        movementDetail2.setMeasureCode(productItemTo.getUsageMeasureCode());
        movementDetail2.setWarehouse(warehouseVoucher.getWarehouse());
        movementDetail2.setUnitCost(productItemTo.getUnitCost());
        movementDetail2.setAmount(BigDecimalUtil.multiply(quantity, productItemTo.getUnitCost(), 6));
        movementDetail2.setUnitPurchasePrice(productItemTo.getCu());
        movementDetail2.setPurchasePrice(BigDecimalUtil.multiply(quantity, productItemTo.getCu(), 6));
        movementDetail2.setMovementType(MovementDetailType.E);
        movementDetail2.setCashAccount(productItemTo.getCashAccount());

        List<MovementDetail> movementDetailsTo = new ArrayList<MovementDetail>();

        movementDetailsTo.add(movementDetail2);

        /** End Transfer to **/
        movementDetails.add(movementDetail);

        try {

            warehouseService.saveWarehouseVoucher(warehouseVoucherFrom, inventoryMovementFrom, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);

            warehouseVoucherUpdateAction.putWarehouseVoucher(warehouseVoucherFrom.getId());


            warehouseService.saveWarehouseVoucher(warehouseVoucherTo, inventoryMovementTo, movementDetailsTo,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);

            warehouseVoucherUpdateAction.putWarehouseVoucher(warehouseVoucherTo.getId());


            try {

                approvalWarehouseVoucherService.approveWarehouseVoucherTransferProduct(warehouseVoucherFrom.getId(), description,
                        movementDetailUnderMinimalStockMap,
                        movementDetailOverMaximumStockMap,
                        movementDetailWithoutWarnings);

                approvalWarehouseVoucherService.approveWarehouseVoucherTransferProduct(warehouseVoucherTo.getId(), description,
                        movementDetailUnderMinimalStockMap,
                        movementDetailOverMaximumStockMap,
                        movementDetailWithoutWarnings);

            } catch (Exception e){
                e.printStackTrace();
                return Outcome.FAIL;
            }

            showMovementDetailWarningMessages();
            return Outcome.SUCCESS;
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.FAIL;
        }



    }


    /*public void createWarehouseForInputOfInputsAndMateriales(List<ProductionOrder> productionOrders) {
        String validationsOutcome = validations();
        if (!Outcome.SUCCESS.equals(validationsOutcome)) {
            return validationsOutcome;
        }
        // reset movement detail quantity mappings to manage warnings and warning column content
        resetValidateQuantityMappings();
        try {
            warehouseService.saveWarehouseVoucher(warehouseVoucher, inventoryMovement, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);

            warehouseVoucherUpdateAction.putWarehouseVoucher(warehouseVoucher.getId());
            showMovementDetailWarningMessages();
            return Outcome.SUCCESS;
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.FAIL;
        }
    }*/

    public String closeCurrentMonthProcess() {
        try {
            monthProcessService.closeCurrentProcessMonth();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "Warehouse.message.closeCurrentMonth");

            return Outcome.SUCCESS;
        } catch (WarehouseVoucherPendantException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Warehouse.error.closeCurrentMonth.pendantVoucher");
            return Outcome.FAIL;
        } catch (MonthProcessValidException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Warehouse.error.closeCurrentMonth.valid");
            return Outcome.FAIL;
        }
    }

    public void addProductItemNotFoundMessage(String productItemName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductItem.error.notFound", productItemName);
    }

    @Override
    public WarehouseVoucher getWarehouseVoucher() {
        warehouseVoucher.setState(WarehouseVoucherState.PEN);
        warehouseVoucher.setDate(monthProcessService.getMothProcessDate(new Date()));
        return warehouseVoucher;
    }

    @Override
    public void clearWarehouse() {
        cleanMovementDetailFields();
        super.clearWarehouse();
    }

    @Override
    public void assignWarehouse(Warehouse warehouse) {
        cleanMovementDetailFields();
        super.assignWarehouse(warehouse);
    }

    @Override
    public void assignResponsible() {
        cleanMovementDetailFields();
        super.assignResponsible();
    }

    public void clearContraAccount() {
        warehouseVoucher.setContraAccount(null);
    }

    @Override
    public List<String> getElementsToReReder() {
        List<String> list = new ArrayList<String>();
        list.add("warehouseField");
        list.add("movementDetailListPanel");

        if (warehouseVoucher.isExecutorUnitTransfer()) {
            list.add("executorUnitCodeField");
        } else {
            list.add("executorUnitCodeField");
            list.add("targetWarehousePanel");
        }
        return list;
    }

    public void addProductItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            if (selectedProductItems.contains(productItem.getId())) {
                continue;
            }

            MeasureUnit measureUnit = getMeasureUnit(productItem);
            if (null == measureUnit) {
                continue;
            }

            selectedProductItems.add(productItem.getId());

            MovementDetail detail = new MovementDetail();
            detail.setProductItem(productItem);
            detail.setMeasureUnit(measureUnit);
            detail.setWarehouse(warehouseVoucher.getWarehouse());
            updateProductItemAccount(detail);

            if (isShownUnitCostField()) {
                detail.setUnitCost(productItem.getUnitCost());
            }

            movementDetails.add(detail);
        }
    }




    public boolean isShownAmountField() {
        return null != uiController && uiController.isShownAmountField();
    }

    public boolean isEnabledAmountField() {
        return null != uiController && uiController.isEnabledAmountField();
    }

    public boolean isShownUnitCostField() {
        return null != uiController && uiController.isShownUnitCostField();
    }

    public boolean isEnabledUnitCostField() {
        return null != uiController && uiController.isEnabledUnitCostField();
    }

    public void calculateTotalAmount() {
        if (null != movementDetail.getProductItem() && null != movementDetail.getQuantity()) {
            BigDecimal totalAmount = BigDecimalUtil.multiply(
                    movementDetail.getProductItem().getUnitCost(), movementDetail.getQuantity(), 6);
            movementDetail.setAmount(totalAmount);
        }
    }

    public void updateTotalAmount(MovementDetail movementDetail) {
        if (null != movementDetail.getQuantity() && null != movementDetail.getUnitCost()) {
            BigDecimal totalAmount = BigDecimalUtil.multiply(
                    movementDetail.getUnitCost(), movementDetail.getQuantity(), 6);
            movementDetail.setAmount(totalAmount);
        }
    }

    public void updateUnitCost(MovementDetail movementDetail) {
        if (null != movementDetail.getQuantity() && null != movementDetail.getAmount()) {
            movementDetail.setUnitCost(
                    BigDecimalUtil.divide(movementDetail.getAmount(), movementDetail.getQuantity(), 6)
            );
        }
    }

    public void cleanMainFields() {
        uiController = new MovementDetailUIController(warehouseVoucher);

        warehouseVoucher.setExecutorUnit(null);
        clearCostCenter();
        clearWarehouse();
        cleanResponsible();

        warehouseVoucher.setTargetExecutorUnit(null);
        clearTargetCostCenter();
        clearTargetWarehouse();
        updateDefaultDescription();

        cleanMovementDetailFields();
        clearContraAccount();
    }

    public boolean isEnableContractInfo() {
        return warehouseVoucher.getPetitionerJobContract() != null;
    }

    public void updateDefaultDescription() {
        if (warehouseVoucher.getDocumentType() != null) {
            inventoryMovement.setDescription(warehouseVoucher.getDocumentType().getDefaultDescription());
        } else {
            inventoryMovement.setDescription(null);
        }
    }

    public void removeMovementDetail(MovementDetail instance) {
        selectedProductItems.remove(instance.getProductItem().getId());
        movementDetails.remove(instance);
    }

    public List<MovementDetail> getMovementDetails() {
        return movementDetails;
    }

    public void setMovementDetails(List<MovementDetail> movementDetails) {
        this.movementDetails = movementDetails;
    }

    public Integer getRows() {
        if (null != movementDetails && !movementDetails.isEmpty()) {
            return movementDetails.size() + 1;
        }

        return 1;
    }

    private void cleanMovementDetailFields() {
        movementDetail = new MovementDetail();
        movementDetails = new ArrayList<MovementDetail>();
        selectedProductItems = new ArrayList<ProductItemPK>();
    }

    private void updateProductItemAccount(MovementDetail movementDetail) {
        if (null == movementDetail.getProductItem()) {
            movementDetail.setProductItemAccount(null);
        } else {
            movementDetail.setProductItemAccount(movementDetail.getProductItem().getProductItemAccount());
        }
    }

    private void validateOutputMovementDetail(MovementDetail movementDetail) throws InventoryException {
        if (null != movementDetail && (warehouseVoucher.isConsumption()
                || warehouseVoucher.isOutput()
                || warehouseVoucher.isTransfer()
                || warehouseVoucher.isExecutorUnitTransfer())) {
            approvalWarehouseVoucherService.validateOutputMovementDetail(warehouseVoucher,
                    warehouseVoucher.getWarehouse(), movementDetail, false);
        }
    }


    private String validations() {
        String validationOutcome = Outcome.SUCCESS;

        if (warehouseVoucher.isExecutorUnitTransfer()) {
            validationOutcome = validateExecutorUnitTransferenceVoucher();
        }

        if (warehouseVoucher.isTransfer()) {
            validationOutcome = validateTransferenceVoucher();
        }

        if (warehouseVoucher.isDevolution() || warehouseVoucher.isInput() || warehouseVoucher.isReception()) {
            validationOutcome = validateReceptionVoucher();
        }

        if (warehouseVoucher.isConsumption() || warehouseVoucher.isOutput()) {
            validationOutcome = validateConsumptionVoucher();
        }

        if (warehouseVoucher.isOutput()) {
            validationOutcome = validateOutputVoucher();
        }

        if (movementDetails.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehouseVoucher.error.movementDetailsRequired");

            validationOutcome = Outcome.REDISPLAY;
        } else {
            for (MovementDetail movementDetail : movementDetails) {
                try {
                    validateOutputMovementDetail(movementDetail);
                } catch (InventoryException e) {
                    validationOutcome = Outcome.REDISPLAY;
                    addInventoryMessages(e.getInventoryMessages());
                }
            }
        }

        return validationOutcome;
    }

    private MeasureUnit getMeasureUnit(ProductItem productItem) {
        MeasureUnitPk measureUnitPk = new MeasureUnitPk(productItem.getId().getCompanyNumber(),
                productItem.getUsageMeasureCode());

        return warehouseCatalogService.findWarehouseCatalog(MeasureUnit.class, measureUnitPk);
    }

    public ProductItem getProductItemFrom() {
        return productItemFrom;
    }

    public void setProductItemFrom(ProductItem productItemFrom) {
        this.productItemFrom = productItemFrom;
    }

    public ProductItem getProductItemTo() {
        return productItemTo;
    }

    public void setProductItemTo(ProductItem productItemTo) {
        this.productItemTo = productItemTo;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
