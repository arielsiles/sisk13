package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.DiscountAmountException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderDetail;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderDetailService;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("warehousePurchaseOrderDetailAction")
@Scope(ScopeType.CONVERSATION)
@BusinessUnitRestrict
public class WarehousePurchaseOrderDetailAction extends GenericAction<PurchaseOrderDetail> {

    private List<String> productItemMeasureCodes = new ArrayList<String>();

    private String productItemCompanyNumber;

    @In
    private WarehousePurchaseOrderService warehousePurchaseOrderService;

    @In
    private WarehousePurchaseOrderDetailService warehousePurchaseOrderDetailService;

    @In(value = "warehousePurchaseOrderAction")
    private WarehousePurchaseOrderAction warehousePurchaseOrderAction;

    private Provide provide;

    @Factory(value = "warehousePurchaseOrderDetail", scope = ScopeType.STATELESS)
    public PurchaseOrderDetail initWarehousePurchaseOrderDetail() {
        return getInstance();
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDERDETAIL','CREATE')}")
    public String addWarehousePurchaseOrderDetail() {
        warehousePurchaseOrderAction.enablePurchaseOrderDetailTab();
        if (warehousePurchaseOrderService.isPurchaseOrderApproved(getPurchaseOrder())) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderApprovedError();
            return WarehousePurchaseOrderAction.APPROVED_OUTCOME;
        }

        if (warehousePurchaseOrderService.isPurchaseOrderFinalized(getPurchaseOrder())) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderFinalizedError();
            return WarehousePurchaseOrderAction.FINALIZED_OUTCOME;
        }

        if (warehousePurchaseOrderService.isPurchaseOrderLiquidated(getPurchaseOrder())) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderLiquidatedError();
            return WarehousePurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

        setInstance(null);
        setOp(OP_CREATE);
        getInstance().setPurchaseOrder(warehousePurchaseOrderAction.getInstance());
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String create() {
        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        warehousePurchaseOrderAction.resetValidateQuantityMappings();
        warehousePurchaseOrderAction.buildValidateQuantityMappings(getInstance());
        try {
            warehousePurchaseOrderDetailService.createPurchaseOrderDetail(getInstance(), getInstance().getUnitCost(),
                    warehousePurchaseOrderAction.getPurchaseOrderDetailUnderMinimalStockMap(),
                    warehousePurchaseOrderAction.getPurchaseOrderDetailOverMaximumStockMap(),
                    warehousePurchaseOrderAction.getPurchaseOrderDetailWithoutWarnings());
            addCreatedMessage();
            warehousePurchaseOrderAction.showPurchaseOrderDetailWarningMessages();
        } catch (PurchaseOrderApprovedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderApprovedError();
            return WarehousePurchaseOrderAction.APPROVED_OUTCOME;
        } catch (PurchaseOrderFinalizedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderFinalizedError();
            return WarehousePurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (PurchaseOrderNullifiedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderNullifiedError();
            return WarehousePurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            warehousePurchaseOrderAction.updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (DiscountAmountException e) {
            warehousePurchaseOrderAction.addDiscountAmountErrorMessage(e.getLimit());
            warehousePurchaseOrderAction.resetTotalAmount();
            return Outcome.REDISPLAY;
        } catch (DuplicatedPurchaseOrderDetailException e) {
            addDuplicatedDetailMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderFinalizedError();
            return WarehousePurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

        return Outcome.SUCCESS;
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public void createAndNew() {
        if (Outcome.SUCCESS.equals(validateInputFields())) {
            warehousePurchaseOrderAction.resetValidateQuantityMappings();
            warehousePurchaseOrderAction.buildValidateQuantityMappings(getInstance());
            try {
                warehousePurchaseOrderDetailService.createPurchaseOrderDetail(getInstance(), getInstance().getUnitCost(),
                        warehousePurchaseOrderAction.getPurchaseOrderDetailUnderMinimalStockMap(),
                        warehousePurchaseOrderAction.getPurchaseOrderDetailOverMaximumStockMap(),
                        warehousePurchaseOrderAction.getPurchaseOrderDetailWithoutWarnings());
                try {
                    getService().create(getInstance());
                    addCreatedMessage();
                    warehousePurchaseOrderAction.showPurchaseOrderDetailWarningMessages();
                    createInstance();
                } catch (EntryDuplicatedException e) {
                    addDuplicatedMessage();
                }
                getInstance().setPurchaseOrder(warehousePurchaseOrderAction.getInstance());
            } catch (PurchaseOrderApprovedException e) {
                warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
                addWarehousePurchaseOrderApprovedError();
            } catch (PurchaseOrderFinalizedException e) {
                warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
                addWarehousePurchaseOrderFinalizedError();
            } catch (PurchaseOrderNullifiedException e) {
                warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
                addWarehousePurchaseOrderNullifiedError();
            } catch (ConcurrencyException e) {
                warehousePurchaseOrderAction.updateCurrentInstance();
                addUpdateConcurrencyMessage();
            } catch (DiscountAmountException e) {
                warehousePurchaseOrderAction.addDiscountAmountErrorMessage(e.getLimit());
                warehousePurchaseOrderAction.resetTotalAmount();
            } catch (DuplicatedPurchaseOrderDetailException e) {
                addDuplicatedDetailMessage();
            } catch (PurchaseOrderLiquidatedException e) {
                warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
                addWarehousePurchaseOrderFinalizedError();
            }
        }
    }

    @Override
    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String cancel() {
        return super.cancel();
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String select(PurchaseOrderDetail instance) {
        try {
            warehousePurchaseOrderAction.enablePurchaseOrderDetailTab();
            setOp(OP_UPDATE);

            setInstance(warehousePurchaseOrderDetailService.findPurchaseOrderDetail(instance.getId()));
            productItemCompanyNumber = getInstance().getProductItem().getId().getCompanyNumber();
            productItemMeasureCodes.add(getInstance().getProductItem().getUsageMeasureCode());
            if (null != getInstance().getProductItem().getGroupMeasureUnit()) {
                productItemMeasureCodes.add(getInstance().getProductItem().getGroupMeasureCode());
            }

            provide = warehousePurchaseOrderDetailService.findProvideElement(getInstance().getProductItem(), getPurchaseOrder().getProvider());
            if (null == provide) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "WarehousePurchaseOrderDetail.info.productItem",
                        getInstance().getProductItem().getName());
            }
        } catch (PurchaseOrderDetailNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }

        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String update() {
        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        warehousePurchaseOrderAction.resetValidateQuantityMappings();
        warehousePurchaseOrderAction.buildValidateQuantityMappings(getInstance());
        try {
            warehousePurchaseOrderDetailService.updatePurchaseOrderDetail(getInstance(),
                    warehousePurchaseOrderAction.getPurchaseOrderDetailUnderMinimalStockMap(),
                    warehousePurchaseOrderAction.getPurchaseOrderDetailOverMaximumStockMap(),
                    warehousePurchaseOrderAction.getPurchaseOrderDetailWithoutWarnings());
            addUpdatedMessage();
            warehousePurchaseOrderAction.showPurchaseOrderDetailWarningMessages();
        } catch (PurchaseOrderFinalizedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderFinalizedError();
            return WarehousePurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            try {
                warehousePurchaseOrderDetailService.findPurchaseOrderDetail(getInstance().getId());
                warehousePurchaseOrderAction.updateCurrentInstance();
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (PurchaseOrderDetailNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (PurchaseOrderDetailNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (PurchaseOrderDetailTotalAmountException e) {
            addTotalAmountErrorMessage(e.getLowerLimit());
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderApprovedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderApprovedError();
            return WarehousePurchaseOrderAction.APPROVED_OUTCOME;
        } catch (PurchaseOrderNullifiedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            warehousePurchaseOrderAction.addPurchaseOrderNullifiedErrorMessage();
            return WarehousePurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (DiscountAmountException e) {
            warehousePurchaseOrderAction.addDiscountAmountErrorMessage(e.getLimit());
            warehousePurchaseOrderAction.resetTotalAmount();
            return Outcome.REDISPLAY;
        } catch (DuplicatedPurchaseOrderDetailException e) {
            addDuplicatedDetailMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderFinalizedError();
            return WarehousePurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    public String delete() {
        try {
            warehousePurchaseOrderDetailService.deletePurchaseOrderDetail(getInstance());
            addDeletedMessage();
        } catch (PurchaseOrderFinalizedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderFinalizedError();
            return WarehousePurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (PurchaseOrderApprovedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderApprovedError();
            return WarehousePurchaseOrderAction.APPROVED_OUTCOME;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (PurchaseOrderDetailNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (PurchaseOrderNullifiedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderNullifiedError();
            return WarehousePurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            warehousePurchaseOrderAction.updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (DiscountAmountException e) {
            warehousePurchaseOrderAction.addDiscountAmountErrorMessage(e.getLimit());
            warehousePurchaseOrderAction.resetTotalAmount();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderFinalizedError();
            return WarehousePurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

        return Outcome.SUCCESS;
    }

    public void assignProductItem(ProductItem productItem) {
        productItemCompanyNumber = productItem.getId().getCompanyNumber();
        provide = warehousePurchaseOrderDetailService.findProvideElement(productItem, getPurchaseOrder().getProvider());
        getInstance().setProductItem(productItem);
        getInstance().setPurchaseMeasureUnit(provide.getGroupMeasureUnit());
        getInstance().setUnitCost(provide.getGroupAmount());
        getInstance().setTotalAmount(getInstance().getRequestedQuantity() != null ? BigDecimalUtil.multiply(getInstance().getRequestedQuantity(), getInstance().getUnitCost(), 6) : BigDecimal.ZERO);
    }

    public void updateProperties() {
        ProductItem productItem = getInstance().getProductItem();

        productItemCompanyNumber = productItem.getId().getCompanyNumber();
        provide = warehousePurchaseOrderDetailService.findProvideElement(productItem, getPurchaseOrder().getProvider());

        getInstance().setPurchaseMeasureUnit(provide.getGroupMeasureUnit());
        getInstance().setUnitCost(provide.getGroupAmount());
        getInstance().setTotalAmount(getInstance().getRequestedQuantity() != null ? BigDecimalUtil.multiply(getInstance().getRequestedQuantity(), getInstance().getUnitCost(), 6) : BigDecimal.ZERO);
    }

    public void clearProductItem() {
        productItemCompanyNumber = null;
        provide = null;

        getInstance().setProductItem(null);
        getInstance().setPurchaseMeasureUnit(null);
        getInstance().setUnitCost(null);
        getInstance().setTotalAmount(null);
    }

    @Override
    protected GenericService getService() {
        return warehousePurchaseOrderDetailService;
    }

    private void addWarehousePurchaseOrderApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderDetail.error.purchaseOrderAlreadyApproved",
                getPurchaseOrder().getOrderNumber());
    }

    private void addWarehousePurchaseOrderFinalizedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderDetail.error.purchaseOrderAlreadyFinalized",
                warehousePurchaseOrderAction.getInstance().getOrderNumber());
    }


    private void addWarehousePurchaseOrderNullifiedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderDetail.error.purchaseOrderAlreadyNullified",
                warehousePurchaseOrderAction.getInstance().getOrderNumber());
    }

    private void addWarehousePurchaseOrderLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderDetail.error.purchaseOrderAlreadyFinalized",
                warehousePurchaseOrderAction.getInstance().getOrderNumber());
    }

    private void addTotalAmountErrorMessage(BigDecimal lowerLimit) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehousePurchaseOrderDetail.error.totalAmount",
                MessageUtils.getMessage("WarehousePurchaseOrderDetail.providerUnitPrice"),
                MessageUtils.getMessage("WarehousePurchaseOrderDetail.receivedQuantity"),
                lowerLimit);
    }

    private void addDuplicatedDetailMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehousePurchaseOrderDetail.error.duplicated",
                getInstance().getProductItem().getFullName(),
                getInstance().getPurchaseOrder().getOrderNumber());
    }

    private PurchaseOrder getPurchaseOrder() {
        return warehousePurchaseOrderAction.getInstance();
    }

    private String validateInputFields() {
        String outcome = Outcome.SUCCESS;

        if (null == getInstance().getProductItem()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("WarehousePurchaseOrderDetail.productItem"));
            outcome = Outcome.REDISPLAY;
        }

        BigDecimal requestedQuantity = getInstance().getRequestedQuantity();
        if (null != requestedQuantity && BigDecimal.ZERO.compareTo(requestedQuantity) >= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.error.greaterThan",
                    MessageUtils.getMessage("WarehousePurchaseOrderDetail.requestedQuantity"), BigDecimal.ZERO);
            outcome = Outcome.REDISPLAY;
        }

        if (null == getInstance().getUnitCost()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WarehousePurchaseOrderDetail.error.unitCost", getInstance().getPurchaseMeasureUnit().getName());
            outcome = Outcome.REDISPLAY;
        }

        if (Outcome.SUCCESS.equals(outcome)) {
            BigDecimal totalAmount = BigDecimalUtil.multiply(getInstance().getRequestedQuantity(), getInstance().getUnitCost(), 6);
            if (totalAmount.compareTo(getInstance().getTotalAmount()) != 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehousePurchaseOrderDetail.error.calculateTotalAmount");
                outcome = Outcome.REDISPLAY;
            }
        }

        return outcome;
    }

    @Override
    protected String getDisplayNameMessage() {
        if (null != getInstance().getProductItem()) {
            return getInstance().getProductItem().getName();
        }

        return super.getDisplayNameMessage();
    }

    public List<String> getProductItemMeasureCodes() {
        return productItemMeasureCodes;
    }

    public String getProductItemCompanyNumber() {
        return productItemCompanyNumber;
    }

    public void calculateUnitCost() {
        BigDecimal unitCost = BigDecimal.ZERO;
        if (null != getInstance().getRequestedQuantity() && null != getInstance().getTotalAmount()) {
            unitCost = BigDecimalUtil.divide(getInstance().getTotalAmount(), getInstance().getRequestedQuantity(), 6);
        }
        getInstance().setUnitCost(unitCost);
    }

    public void calculateTotalAmount() {
        BigDecimal totalAmount = null;
        if (null != getInstance().getRequestedQuantity() && null != getInstance().getUnitCost()) {
            totalAmount = BigDecimalUtil.multiply(getInstance().getRequestedQuantity(), getInstance().getUnitCost(), 6);
        }
        getInstance().setTotalAmount(totalAmount);
    }

    public boolean isShowCalculateLink() {
        return null != getInstance().getProductItem()
                && null != getInstance().getPurchaseMeasureUnit()
                && null != getInstance().getUnitCost();
    }
}
