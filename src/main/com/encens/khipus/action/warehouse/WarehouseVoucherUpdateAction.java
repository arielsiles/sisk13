package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.fixedassets.LiquidationPaymentAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.warehouse.ApprovalWarehouseVoucherService;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.WarehouseAccountEntryService;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.purchases.PurchaseOrderValidator;
import com.encens.khipus.util.query.QueryUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.0
 */
@Name("warehouseVoucherUpdateAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
/*@BusinessUnitRestrict*/
public class WarehouseVoucherUpdateAction extends WarehouseVoucherGeneralAction {

    public static final String FINALIZED_OUTCOME = "Finalized";
    public static final String LIQUIDATED_OUTCOME = "Liquidated";
    public static String APPROVED_OUTCOME = "Approved";
    @In
    private ApprovalWarehouseVoucherService approvalWarehouseVoucherService;

    @In
    private VoucherService voucherService;

    @In
    private MovementDetailService movementDetailService;

    @In(value = "warehousePurchaseOrderService")
    private WarehousePurchaseOrderService warehousePurchaseOrderService;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In(create = true)
    private PurchaseOrderValidator purchaseOrderValidator;

    @In(create = true, value = "liquidationPaymentAction")
    private LiquidationPaymentAction liquidationPaymentAction;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;

    @Override
    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}", postValidation = true)
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','VIEW')}")
    public String select(WarehouseVoucher instance) {
        setOp(OP_UPDATE);
        try {
            readWarehouseVoucher(instance.getId());
        } catch (WarehouseVoucherNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }

        return Outcome.SUCCESS;
    }

    public void putWarehouseVoucher(WarehouseVoucherPK pk) {
        setOp(OP_UPDATE);
        try {
            readWarehouseVoucher(pk);
        } catch (WarehouseVoucherNotFoundException e) {
            //this exception never happens because this method is executed immediately after of create operation.
        }
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','UPDATE')}")
    public String update() {
        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        resetValidateQuantityMappings();

        try {
            for (MovementDetail movementDetail : inventoryMovement.getMovementDetailList()) {
                buildValidateQuantityMappings(movementDetail);
            }
            warehouseService.updateWarehouseVoucher(warehouseVoucher, inventoryMovement,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
        } catch (ConcurrencyException e) {
            try {
                readWarehouseVoucher(warehouseVoucher.getId());
            } catch (WarehouseVoucherNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }

            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (WarehouseVoucherApprovedException e) {
            addWarehouseVoucherApprovedMessage();
            return APPROVED_OUTCOME;
        } catch (WarehouseVoucherNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.FAIL;
        }

        addUpdatedMessage();
        showMovementDetailWarningMessages();
        return Outcome.SUCCESS;
    }

    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHERAPPROVAL','VIEW')}")
    public String approve() {
        resetValidateQuantityMappings();
        Voucher voucher = new Voucher();
        try {
            //primeramente ejecuta las operacion y verifica que no haya errores

            /** O.C. & Pago con Factura **/
            if(     warehouseVoucher.hasPurchaseOrder() &&
                   /*(warehouseVoucher.getPurchaseOrder().getWithBill().equals(Constants.WITH_BILL) || warehouseVoucher.getPurchaseOrder().getWithBill().equals(Constants.WITHOUT_BILL)) &&*/
                    (warehouseVoucher.getPurchaseOrder().getPayConditions().getName().equals(Constants.CONDITION_CREDIT) ||
                     warehouseVoucher.getPurchaseOrder().getPayConditions().getName().equals(Constants.CONDITION_CASH) ) ) {
                //System.out.println("=======> O.C. con Factura ");
                voucher = warehouseAccountEntryService.createEntryAccountForPurchaseOrder(warehouseVoucher); /** Testing **/

                voucherAccoutingService.updatePurchaseDocumentIfExist(voucher);

                approvalWarehouseVoucherService.approveWarehouseVoucherForPurchaseOrder(warehouseVoucher.getId(), getGlossMessage(),
                                                                                        movementDetailUnderMinimalStockMap,
                                                                                        movementDetailOverMaximumStockMap,
                                                                                        movementDetailWithoutWarnings);

                if (warehouseVoucher.getPurchaseOrder().getPayConditions().getName().equals(Constants.CONDITION_CASH))
                    warehousePurchaseOrderService.liquidateCashPurchaseOrder(warehouseVoucher.getPurchaseOrder());
            }

            if (!warehouseVoucher.hasPurchaseOrder()) {

                for (MovementDetail movementDetail : inventoryMovement.getMovementDetailList()) {
                    buildValidateQuantityMappings(movementDetail);
                }

                approvalWarehouseVoucherService.approveWarehouseVoucher(warehouseVoucher.getId(), getGlossMessage(),
                        movementDetailUnderMinimalStockMap,
                        movementDetailOverMaximumStockMap,
                        movementDetailWithoutWarnings);
            }

            addWarehouseVoucherApproveMessage();
            showMovementDetailWarningMessages();

            //luego hace persistente los cambios si es que no hubo ningun error en ambos el vale y la orden de compra

        } /*catch (WarehouseDocumentTypeNotFoundException e) {
            addWarehouseDocumentTypeErrorMessage();
            voucherService.deleteVoucher(voucher);
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderDetailEmptyException e) {
            addPurchaseOrderEmptyMessage(warehouseVoucher.getPurchaseOrder());
            voucherService.deleteVoucher(voucher);
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage(warehouseVoucher.getPurchaseOrder());
            voucherService.deleteVoucher(voucher);
            return LIQUIDATED_OUTCOME;
        } catch (AdvancePaymentPendingException e) {
            addAdvancePaymentPendingErrorMessage(warehouseVoucher.getPurchaseOrder());
            voucherService.deleteVoucher(voucher);
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            liquidationPaymentAction.addRotatoryFundAnnulledError();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (RotatoryFundLiquidatedException e) {
            liquidationPaymentAction.addRotatoryFundLiquidatedError();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (CollectionSumExceedsRotatoryFundAmountException e) {
            liquidationPaymentAction.addCollectionSumExceedsRotatoryFundAmountError();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (RotatoryFundConcurrencyException e) {
            liquidationPaymentAction.addRotatoryFundConcurrencyMessage();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        }*/ catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            voucherService.deleteVoucher(voucher);
            return Outcome.REDISPLAY;
        } catch (WarehouseVoucherApprovedException e) {
            addWarehouseVoucherApprovedMessage();
            voucherService.deleteVoucher(voucher);
            return APPROVED_OUTCOME;
        } catch (WarehouseVoucherEmptyException e) {
            addWarehouseVoucherEmptyException();
            voucherService.deleteVoucher(voucher);
            return Outcome.REDISPLAY;
        } catch (WarehouseVoucherNotFoundException e) {
            addNotFoundMessage();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (ProductItemAmountException e) {
            addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (InventoryProductItemNotFoundException e) {
            addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            voucherService.deleteVoucher(voucher);
            return Outcome.FAIL;
        } catch (WarehouseAccountCashNotFoundException e) {
            addWarehouseAccountCashNotFoundMessage();
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHERAPPROVAL','VIEW')}")
    public String approveFromCollection() {
        resetValidateQuantityMappings();
        try {
            for (MovementDetail movementDetail : inventoryMovement.getMovementDetailList()) {
                buildValidateQuantityMappings(movementDetail);
            }
            approvalWarehouseVoucherService.approveWarehouseVoucherFromCollection(warehouseVoucher.getId(), getGlossMessage(),
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            addWarehouseVoucherApproveMessage();
            showMovementDetailWarningMessages();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return Outcome.REDISPLAY;
        } catch (WarehouseVoucherApprovedException e) {
            addWarehouseVoucherApprovedMessage();
            return APPROVED_OUTCOME;
        } catch (WarehouseVoucherEmptyException e) {
            addWarehouseVoucherEmptyException();
            return Outcome.REDISPLAY;
        } catch (WarehouseVoucherNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (ProductItemAmountException e) {
            addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            return Outcome.FAIL;
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return Outcome.FAIL;
        } catch (InventoryProductItemNotFoundException e) {
            addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            return Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.FAIL;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.FAIL;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.FAIL;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return Outcome.FAIL;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.FAIL;
        } catch (WarehouseAccountCashNotFoundException e) {
            addWarehouseAccountCashNotFoundMessage();
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    @Override
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','DELETE')}")
    public String delete() {
        try {
            warehouseService.deleteWarehouseVoucher(warehouseVoucher.getId());
            addDeletedMessage();
        } catch (WarehouseVoucherNotFoundException e) {
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (WarehouseVoucherApprovedException e) {
            addWarehouseVoucherApprovedMessage();
            return APPROVED_OUTCOME;
        }

        return Outcome.CANCEL;
    }


    public void readWarehouseVoucher(WarehouseVoucherPK id) throws WarehouseVoucherNotFoundException {
        setWarehouseVoucher(warehouseService.findWarehouseVoucher(id));

        InventoryMovementPK inventoryMovementPK =
                new InventoryMovementPK(warehouseVoucher.getId().getCompanyNumber(),
                        warehouseVoucher.getId().getTransactionNumber(),
                        warehouseVoucher.getState().name());

        setInventoryMovement(warehouseService.findInventoryMovement(inventoryMovementPK));
    }

    private String[] getGlossMessage() {
        String gloss[] = new String[2];
        String dateString = DateUtils.format(warehouseVoucher.getDate(), MessageUtils.getMessage("patterns.date"));
        String productCodes = QueryUtils.toQueryParameter(movementDetailService.findDetailProductCodeByVoucher(warehouseVoucher));
        String documentName = warehouseVoucher.getDocumentType().getName();
        String sourceWarehouseName = warehouseVoucher.getWarehouse().getName();
        String movementDescription = getInventoryMovement().getDescription();

        if (warehouseVoucher.isExecutorUnitTransfer()) {
            String targetWarehouseName = warehouseVoucher.getWarehouse().getName();
            gloss[0] = MessageUtils.getMessage("WarehouseVoucher.message.outTransferenceGloss", documentName, sourceWarehouseName, targetWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
            gloss[1] = MessageUtils.getMessage("WarehouseVoucher.message.inTransferenceGloss", documentName, sourceWarehouseName, targetWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
        } else {
            String voucherTypeName = messages.get(warehouseVoucher.getDocumentType().getWarehouseVoucherType().getResourceKey());
            gloss[0] = MessageUtils.getMessage("WarehouseVoucher.message.gloss", voucherTypeName, documentName, sourceWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
        }

        return gloss;

    }

    @Override
    protected void addNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "WarehouseVoucher.error.notFound");
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "WarehouseVoucher.message.updated");
    }

    public boolean isApproved() {
        return WarehouseVoucherState.APR.equals(warehouseVoucher.getState());
    }

    public boolean isPending() {
        return WarehouseVoucherState.PEN.equals(warehouseVoucher.getState());
    }

    public boolean isPartial() {
        return WarehouseVoucherState.PAR.equals(warehouseVoucher.getState());
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "WarehouseVoucher.message.deleted");
    }

    @Override
    protected void addDeleteConcurrencyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "WarehouseVoucher.concurrency.delete");
    }

    @Override
    protected void addDeleteReferentialIntegrityMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "WarehouseVoucher.referentialIntegrity.delete");
    }

    protected void addNotEnoughAmountMessage(ProductItem productItem,
                                             BigDecimal availableAmount) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.approve.error.notEnoughAmount",
                productItem.getName(),
                availableAmount);
    }

    public void addWarehouseVoucherApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.error.approved");
    }


    protected void addWarehouseVoucherEmptyException() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.error.empty");
    }

    private void addWarehouseVoucherApproveMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "WarehouseVoucher.message.approved");
    }


    protected void addInventoryUnitaryBalanceErrorMessage(BigDecimal availableUnitaryBalance,
                                                          ProductItem productItem) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "warehouseVoucher.approve.error.notEnoughUnitaryBalance",
                productItem.getName(),
                availableUnitaryBalance);
    }

    protected void addInventoryProductItemNotFoundErrorMessage(String executorUnitCode,
                                                               ProductItem productItem,
                                                               Warehouse warehouse) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "warehouseVoucher.approve.error.productItemNotFound",
                productItem.getName(),
                warehouse.getName(),
                executorUnitCode);
    }

    protected void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.financesExchangeRateNotFound");
    }

    private String validateInputFields() {
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

        return validationOutcome;
    }

    public boolean isEmptyWarehouseVoucher() {
        return warehouseService.isEmptyWarehouseVoucher(warehouseVoucher.getId());
    }

    public boolean isEnableContractInfo() {
        return warehouseVoucher.getPetitionerJobContract() != null;
    }

    public boolean isShowAddPartialWarehouseVoucherButton() {
        return (isPending() || isPartial()) && isReception()
                && (warehouseVoucher.getWarehouseVoucherReceptionType() == null
                || warehouseVoucher.getWarehouseVoucherReceptionType().equals(WarehouseVoucherReceptionType.RP));
    }

    /*messages*/
    public void addWarehouseVoucherStateChangedErrorMessage(WarehouseVoucherStateException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.error.annulled");
    }

    private void addReComputePaymentRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PurchaseOrder.error.reComputePaymentRequired");
    }

    private void addPurchaseOrderEmptyMessage(PurchaseOrder purchaseOrder) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderDetailEmpty", purchaseOrder.getOrderNumber());
    }

    public void addPurchaseOrderLiquidatedErrorMessage(PurchaseOrder purchaseOrder) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderNotLiquidated", purchaseOrder.getOrderNumber());
    }

    private void addAdvancePaymentPendingErrorMessage(PurchaseOrder purchaseOrder) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderPaymentPending", purchaseOrder.getOrderNumber());
    }
}
