package com.encens.khipus.action.warehouse;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.accounting.VoucherCreateAction;
import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.accounting.reports.VoucherReportAction;
import com.encens.khipus.action.fixedassets.LiquidationPaymentAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.AdvancePaymentPendingException;
import com.encens.khipus.exception.warehouse.DiscountAmountException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.WarehouseDocumentTypeNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.service.purchases.PurchaseOrderService;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.purchases.PurchaseOrderValidator;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.0
 */

//@BusinessUnitRestrict
@Name("warehousePurchaseOrderAction")
@Scope(ScopeType.CONVERSATION)
public class WarehousePurchaseOrderAction extends GenericAction<PurchaseOrder> {

    private static final Integer SCALE = 6;

    @In(create = true, value = "warehousePurchaseOrderDetailListCreateAction")
    private WarehousePurchaseOrderDetailListCreateAction detailListCreateAction;

    @In(create = true, value = "liquidationPaymentAction")
    private LiquidationPaymentAction liquidationPaymentAction;

    @In
    private PurchaseOrderService purchaseOrderService;

    @In
    private User currentUser;

    @In(create = true)
    private PurchaseOrderValidator purchaseOrderValidator;

    @In
    private JobContractService jobContractService;

    @In
    private SessionUser sessionUser;

    @In
    protected Map<String, String> messages;

    @In
    private InventoryService inventoryService;

    @In(create = true)
    private VoucherUpdateAction voucherUpdateAction;

    @In(create = true)
    private VoucherCreateAction voucherCreateAction;

    @In(create = true)
    private VoucherReportAction voucherReportAction;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    private String activeTabName = "warehousePurchaseOrderTab";

    public static final String APPROVED_OUTCOME = "Approved";
    public static final String FINALIZED_OUTCOME = "Finalized";
    public static final String LIQUIDATED_OUTCOME = "Liquidated";

    private Boolean showBillConditions =false;

    private Boolean billConditions = true;

    // this map stores the PurchaseOrderDetails that are under the minimal stock and the unitaryBalance of the Inventory
    private Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap = new HashMap<PurchaseOrderDetail, BigDecimal>();
    // this map stores the PurchaseOrderDetails that are over the maximum stock and the unitaryBalance of the Inventory
    private Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap = new HashMap<PurchaseOrderDetail, BigDecimal>();
    // this list stores the PurchaseOrderDetails that should not show warnings
    private List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings = new ArrayList<PurchaseOrderDetail>();

    @In(value = "warehousePurchaseOrderService")
    private WarehousePurchaseOrderService service;

    @Factory(value = "warehousePurchaseOrder", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDER','VIEW')}")
    public PurchaseOrder initPurchaseOrder() {
        getInstance().setOrderType(PurchaseOrderType.WAREHOUSE);
        return getInstance();
    }

    @Factory(value = "purchaseOrderStateEnum")
    public PurchaseOrderState[] initPurchaseOrderStateEnum() {
        return PurchaseOrderState.values();
    }

    public void updateCurrentInstance() {
        setInstance(service.findPurchaseOrder(getInstance().getId()));
    }

    public void resetTotalAmount() {
        getInstance().setTotalAmount(BigDecimal.ZERO);
    }

    public void calculatePercentAmountByTotalAmount() {
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountPercent = BigDecimal.ZERO;
        if (!BigDecimalUtil.isZeroOrNull(getInstance().getSubTotalAmount())) {
            discountAmount = BigDecimalUtil.subtract(getInstance().getSubTotalAmount(), getInstance().getTotalAmount());
            if (!BigDecimalUtil.isZeroOrNull(discountAmount)) {
                discountPercent = BigDecimalUtil.divide(BigDecimalUtil.multiply(discountAmount, BigDecimalUtil.ONE_HUNDRED), getInstance().getSubTotalAmount(), 4);
            }
        }
        getInstance().setDiscountAmount(discountAmount);
        getInstance().setDiscountPercent(discountPercent);
    }

    public void calculateTotalAmountByPercentAmount() {
        BigDecimal discountPercentage = getInstance().getDiscountPercent();
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (BigDecimalUtil.isPositive(discountPercentage) && BigDecimalUtil.isPositive(getInstance().getSubTotalAmount())) {
            discountAmount = BigDecimalUtil.multiply(
                    getInstance().getSubTotalAmount(),
                    BigDecimalUtil.divide(discountPercentage, BigDecimalUtil.ONE_HUNDRED, 7));
        }
        BigDecimal totalAmount = BigDecimalUtil.subtract(getInstance().getSubTotalAmount(), discountAmount);
        getInstance().setTotalAmount(totalAmount);
        getInstance().setDiscountPercent(discountPercentage);
        getInstance().setDiscountAmount(discountAmount);
    }

    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}", postValidation = true)
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Override
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDER','VIEW')}")
    public String select(PurchaseOrder instance) {
        String outcome = super.select(instance);
        String ordersNumbers = MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym")+" ";

        for(PurchaseOrder purchaseOrder: liquidationPaymentAction.getPurchaseOrdersWithCheck())
        {
            ordersNumbers += purchaseOrder.getOrderNumber();
            ordersNumbers += "; ";
        }
        if (instance.isPurchaseOrderFinalized()) {
            liquidationPaymentAction.setDefaultDescription(instance,
                    MessageUtils.getMessage("WarehousePurchaseOrder.warehouses"),
                    ordersNumbers);
            liquidationPaymentAction.setPurchaseOrder(getInstance());
        }
        return outcome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDER','CREATE')}")
    public String create() {
        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        resetValidateQuantityMappings();
        for (PurchaseOrderDetail purchaseOrderDetail : detailListCreateAction.getInstances()) {
            buildValidateQuantityMappings(purchaseOrderDetail);
        }
        try {
            /** Crea tomando en cuenta Proveedor, History
            service.create(getInstance(), detailListCreateAction.getInstances(),
                    purchaseOrderDetailUnderMinimalStockMap,
                    purchaseOrderDetailOverMaximumStockMap,
                    purchaseOrderDetailWithoutWarnings);*/
            service.createBasic(getInstance(), detailListCreateAction.getInstances(),
                    purchaseOrderDetailUnderMinimalStockMap,
                    purchaseOrderDetailOverMaximumStockMap,
                    purchaseOrderDetailWithoutWarnings);
            addCreatedMessage();
            super.select(getInstance());
            showPurchaseOrderDetailWarningMessages();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (DuplicatedPurchaseOrderDetailException e) {
            return Outcome.REDISPLAY;
        }
    }

    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @Override
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDER','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), false)) {
            return Outcome.REDISPLAY;
        }

        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        resetValidateQuantityMappings();
        for (PurchaseOrderDetail purchaseOrderDetail : getInstance().getPurchaseOrderDetailList()) {
            buildValidateQuantityMappings(purchaseOrderDetail);
        }
        try {
            service.updateWarehousePurchaseOrder(getInstance(),
                    purchaseOrderDetailUnderMinimalStockMap,
                    purchaseOrderDetailOverMaximumStockMap,
                    purchaseOrderDetailWithoutWarnings);
            addUpdatedMessage();
            showPurchaseOrderDetailWarningMessages();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderApprovedException e) {
            addPurchaseOrderApprovedErrorMessage();
            return APPROVED_OUTCOME;
        } catch (PurchaseOrderFinalizedException e) {
            addPurchaseOrderFinalizedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            try {
                setInstance(getService().findById(PurchaseOrder.class, getInstance().getId(), true));
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (PurchaseOrderNullifiedException e) {
            addPurchaseOrderNullifiedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (DiscountAmountException e) {
            addDiscountAmountErrorMessage(e.getLimit());
            resetTotalAmount();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage();
            return LIQUIDATED_OUTCOME;
        } catch (DuplicatedPurchaseOrderDetailException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderDetailNotFoundException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        }
    }

    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('SPECIALUPDATEPURCHASEORDER','VIEW')}")
    public String specialUpdate() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), true)) {
            return Outcome.REDISPLAY;
        }

        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        try {
            service.specialUpdatePurchaseOrder(getInstance());
            addUpdatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDERANNUL','VIEW')}")
    public String nullifyWarehousePurchaseOrder() {
        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        try {
            service.nullifyPurchaseOrder(getInstance());
            addPurchaseOrderNullifiedMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderFinalizedException e) {
            addPurchaseOrderFinalizedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderNullifiedException e) {
            addPurchaseOrderNullifiedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage();
            return LIQUIDATED_OUTCOME;
        }
    }

    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDERAPPROVE','VIEW')}")
    public String approveWarehousePurchaseOrder() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), true)) {
            return Outcome.REDISPLAY;
        }

        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        resetValidateQuantityMappings();
        for (PurchaseOrderDetail purchaseOrderDetail : getInstance().getPurchaseOrderDetailList()) {
            buildValidateQuantityMappings(purchaseOrderDetail);
        }
        try {

                service.approveWarehousePurchaseOrder(getInstance(),
                        purchaseOrderDetailUnderMinimalStockMap,
                        purchaseOrderDetailOverMaximumStockMap,
                        purchaseOrderDetailWithoutWarnings);

            addPurchaseOrderApprovedMessage();
            showPurchaseOrderDetailWarningMessages();
            return Outcome.SUCCESS;
        } catch (CompanyConfigurationNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderApprovedException e) {
            addPurchaseOrderApprovedErrorMessage();
            return APPROVED_OUTCOME;
        } catch (PurchaseOrderFinalizedException e) {
            addPurchaseOrderFinalizedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (PurchaseOrderDetailEmptyException e) {
            addPurchaseOrderEmptyMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderNullifiedException e) {
            addPurchaseOrderNullifiedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (DiscountAmountException e) {
            addDiscountAmountErrorMessage(e.getLimit());
            resetTotalAmount();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage();
            return LIQUIDATED_OUTCOME;
        }
    }

    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDERFINALIZE','VIEW')}")
    public String finalizeWarehousePurchaseOrder() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), true)) {
            return Outcome.REDISPLAY;
        }

        try {
            service.finalizePurchaseOrder(getInstance());
            select(getInstance());
            addPurchaseOrderFinalizedMessage();
            return Outcome.SUCCESS;
        } catch (WarehouseDocumentTypeNotFoundException e) {
            addWarehouseDocumentTypeErrorMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderDetailEmptyException e) {
            addPurchaseOrderEmptyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderFinalizedException e) {
            addPurchaseOrderFinalizedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.FAIL;
        }
    }

    @BusinessUnitRestriction(value = "#{warehousePurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('WAREHOUSEPURCHASEORDERLIQUIDATE','VIEW')}")
    public String liquidateWarehousePurchaseOrder() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), true)) {
            return Outcome.REDISPLAY;
        }

        if (false) {
            addReComputePaymentRequiredMessage();
            return Outcome.REDISPLAY;
        }

        try {

            /*if(liquidationPaymentAction.isCheckPayment()){*/

            if(liquidationPaymentAction.isCheckPayment()){
                service.onlyLiquidatePurchaseOrder(liquidationPaymentAction.getPurchaseOrdersWithCheck(),getInstance());
                addPurchaseOrderWithCheckLiquidatedMessage(liquidationPaymentAction.getPurchaseOrdersWithCheck());//personalizar este mensaje con todas la ordenes de produccion
            }
            else{
                if (liquidationPaymentAction.isCashBoxPayment()){
                    service.liquidatePurchaseOrderWithCashBox(liquidationPaymentAction.getPurchaseOrdersWithCheck(), getInstance());
                    addPurchaseOrderWithCheckLiquidatedMessage(liquidationPaymentAction.getPurchaseOrdersWithCheck());
                }else {
                    service.onlyLiquidatePurchaseOrder(getInstance(), getLiquidationPayment());
                    addPurchaseOrderLiquidatedMessage();
                }
            }
            //service.liquidatePurchaseOrder(getInstance());
            return Outcome.SUCCESS;
        } catch (WarehouseDocumentTypeNotFoundException e) {
            addWarehouseDocumentTypeErrorMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderDetailEmptyException e) {
            addPurchaseOrderEmptyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage();
            return LIQUIDATED_OUTCOME;
        } catch (AdvancePaymentPendingException e) {
            addAdvancePaymentPendingErrorMessage();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundConcurrencyException e) {
            liquidationPaymentAction.addRotatoryFundConcurrencyMessage();
            return Outcome.FAIL;
        } catch (CollectionSumExceedsRotatoryFundAmountException e) {
            liquidationPaymentAction.addCollectionSumExceedsRotatoryFundAmountError();
            return Outcome.FAIL;
        } catch (RotatoryFundLiquidatedException e) {
            liquidationPaymentAction.addRotatoryFundLiquidatedError();
            return Outcome.FAIL;
        } catch (RotatoryFundNullifiedException e) {
            liquidationPaymentAction.addRotatoryFundAnnulledError();
            return Outcome.FAIL;
        }
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public PurchaseOrderPayment getLiquidationPayment() {
        return hasBalanceAmount() ? liquidationPaymentAction.getLiquidationPayment() : null;
    }

    public void assignWarehouse(Warehouse warehouse) {
        getInstance().setWarehouse(warehouse);
    }

    public void clearWarehouse() {
        getInstance().setWarehouse(null);
    }

    public void changeExecutorUnit() {
        getInstance().setWarehouse(null);
        getInstance().setReceptionPlace(null);
    }

    public String getCostCenterFullName() {
        return getInstance().getCostCenter() != null ? getInstance().getCostCenter().getFullName() : null;
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        getInstance().setCostCenter(null);
    }

    public void assignProvider(Provider provider) {
        detailListCreateAction.initializeAction();
        getInstance().setProvider(provider);
    }

    public void clearProvider() {
        detailListCreateAction.initializeAction();
        getInstance().setProvider(null);
    }

    public String getResponsibleFullName() {
        if (getInstance().getResponsible() == null) {
            getInstance().setResponsible(currentUser.getEmployee());
        }
        return getInstance().getResponsible().getFullName();
    }

    public boolean isPurchaseOrderApproved() {
        return isManaged() && getInstance().isPurchaseOrderApproved();
    }

    public boolean isPurchaseOrderPending() {
        return !isManaged() || getInstance().isPurchaseOrderPending();
    }

    public boolean isPurchaseOrderFinalized() {
        return isManaged() && getInstance().isPurchaseOrderFinalized();
    }

    public boolean isPurchaseOrderLiquidated() {
        return isManaged() && getInstance().isPurchaseOrderLiquidated();
    }

    public boolean isPurchaseOrderNullified() {
        return isManaged() && getInstance().isNullified();
    }

    public Boolean hasBalanceAmount() {
        return getCurrentBalanceAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getCurrentBalanceAmount() {
        if(liquidationPaymentAction.getPurchaseOrdersWithCheck().isEmpty())
        return purchaseOrderService.currentBalanceAmount(getInstance());
        else{
            Double total = purchaseOrderService.currentBalanceAmount(getInstance()).doubleValue();
            for(PurchaseOrder purchaseOrder: liquidationPaymentAction.getPurchaseOrdersWithCheck())
            {
                total += purchaseOrderService.currentBalanceAmount(purchaseOrder).doubleValue();
            }

            return new BigDecimal(total);
        }
    }

    public void addPurchaseOrder(List<PurchaseOrder> purchaseOrders) {
        String gloss = " - CON ORDENES DE COMPRA: ";
        for (PurchaseOrder purchaseOrder : purchaseOrders) {
            if (liquidationPaymentAction.getSelectedPurchaseOrdersWithCheck().contains(purchaseOrder)) {
                continue;
            }

            liquidationPaymentAction.getSelectedPurchaseOrdersWithCheck().add(purchaseOrder);
            liquidationPaymentAction.getPurchaseOrdersWithCheck().add(purchaseOrder);
            gloss += purchaseOrder.getOrderNumber()+"; ";
        }

        String ordersNumbers = MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym")+"; ";

        for(PurchaseOrder purchaseOrder: liquidationPaymentAction.getPurchaseOrdersWithCheck())
        {
            ordersNumbers += purchaseOrder.getOrderNumber();
            ordersNumbers += "; ";
        }

            liquidationPaymentAction.setDefaultDescription(getInstance(),
                    MessageUtils.getMessage("WarehousePurchaseOrder.warehouses"),
                    ordersNumbers);

        getInstance().setGloss(getInstance().getGloss()+gloss);
        liquidationPaymentAction.computePayment(getCurrentBalanceAmount());
    }

    public BigDecimal removePurchaseOrderAndGetCurrentBalanceAmount(PurchaseOrder instance){
        if(!liquidationPaymentAction.getPurchaseOrdersWithCheck().isEmpty()){
            liquidationPaymentAction.getPurchaseOrdersWithCheck().remove(instance);
            liquidationPaymentAction.getSelectedPurchaseOrdersWithCheck().remove(instance);
        }
        return getCurrentBalanceAmount();
    }

    public boolean checkPayment() {
        if (getLiquidationPayment() != null) {
            return liquidationPaymentAction.checkPayment(getCurrentBalanceAmount());
        }
        return true;
    }

    public boolean isExecutorUnitSelected() {
        return null != getInstance().getExecutorUnit();
    }

    public boolean isEnableContractInfo() {
        return getInstance().getPetitionerJobContract() != null;
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.financesExchangeRateNotFound");
    }

    public void addPurchaseOrderApprovedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderAlreadyApproved", getInstance().getOrderNumber());
    }

    public void addPurchaseOrderFinalizedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderAlreadyFinalized", getInstance().getOrderNumber());
    }

    public void addPurchaseOrderLiquidatedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderAlreadyLiquidated", getInstance().getOrderNumber());
    }

    public void addPurchaseOrderNullifiedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderAlreadyNullified", getInstance().getOrderNumber());
    }

    private void addReComputePaymentRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PurchaseOrder.error.reComputePaymentRequired");
    }

    private void addPurchaseOrderEmptyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderDetailEmpty", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.approveMessage", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderNullifiedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.nullifiedMessage", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderFinalizedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.finalizeMessage", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderLiquidatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.liquidateMessage", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderWithCheckLiquidatedMessage(List<PurchaseOrder> purchaseOrdersWithCheck) {

        String ordersNumbers = getInstance().getOrderNumber()+", ";

        for(PurchaseOrder purchaseOrder: purchaseOrdersWithCheck)
        {
            ordersNumbers += purchaseOrder.getOrderNumber();
            ordersNumbers += ", ";
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.liquidateMessageWithCheck", ordersNumbers);
    }

    private void addWarehouseDocumentTypeErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.error.warehouseDocumentTypeNotFound");
    }

    private void addAdvancePaymentPendingErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderPaymentPending", getInstance().getOrderNumber());
    }

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.message.created", getInstance().getOrderNumber());
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.message.updated", getInstance().getOrderNumber());
    }

    public void addDiscountAmountErrorMessage(BigDecimal limit) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehousePurchaseOrder.approve.error.discountAmount", limit);
    }

    @Override
    protected GenericService getService() {
        return service;
    }

    public PurchaseOrder getPurchaseOrder() {
        return getInstance();
    }

    private String validateInputFields() {
        String outcome = Outcome.SUCCESS;
        if (null == getInstance().getProvider()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("WarehousePurchaseOrder.provider"));
            outcome = Outcome.REDISPLAY;
        }

        if (null == getInstance().getResponsible()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("WarehousePurchaseOrder.responsible"));
            outcome = Outcome.REDISPLAY;
        }

        if (null == getInstance().getWarehouse()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("WarehousePurchaseOrder.warehouse"));
            outcome = Outcome.REDISPLAY;
        }

        if (null == getInstance().getCostCenterCode() || "".equals(getInstance().getCostCenterCode().trim())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("WarehousePurchaseOrder.costCenter"));
            outcome = Outcome.REDISPLAY;
        }

        if (null == getInstance().getPetitionerJobContract()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("WarehousePurchaseOrder.petitioner"));
            outcome = Outcome.REDISPLAY;
        }

        return outcome;
    }

    public void buildValidateQuantityMappings(PurchaseOrderDetail purchaseOrderDetail) {
        BigDecimal requiredQuantity = purchaseOrderDetail.getRequestedQuantity();
        if (null != requiredQuantity) {
            BigDecimal equivalentQuantity = purchaseOrderDetail.getProductItem().getEquivalentQuantity();
            if (null != equivalentQuantity && equivalentQuantity.compareTo(BigDecimal.ONE) != 0) {
                requiredQuantity = BigDecimalUtil.multiply(requiredQuantity, equivalentQuantity, SCALE);
            }
            ProductItem productItem = purchaseOrderDetail.getProductItem();
            Warehouse warehouse = purchaseOrderDetail.getPurchaseOrder().getWarehouse();
            BigDecimal minimalStock = productItem.getMinimalStock();
            BigDecimal maximumStock = productItem.getMaximumStock();
            BigDecimal unitaryBalance = inventoryService.findUnitaryBalanceByProductItemAndArticle(warehouse.getId(), productItem.getId());
            BigDecimal totalQuantity = BigDecimalUtil.sum(requiredQuantity, unitaryBalance, SCALE);
            // by default does not show warning until is verified
            boolean showWarning = false;

            if (null != minimalStock) {
                // minimalStock is not null
                int minimalComparison = totalQuantity.compareTo(minimalStock);
                if (minimalComparison < 0) {
                    // if under minimalStock
                    this.purchaseOrderDetailUnderMinimalStockMap.put(purchaseOrderDetail, unitaryBalance);
                    showWarning = true;
                }
            }
            if (null != maximumStock) {
                // maximumStock is not null
                int maximumComparison = totalQuantity.compareTo(maximumStock);
                if (maximumComparison > 0) {
                    // if over maximumStock
                    this.purchaseOrderDetailOverMaximumStockMap.put(purchaseOrderDetail, unitaryBalance);
                    showWarning = true;
                }
            }
            if (!showWarning) {
                purchaseOrderDetailWithoutWarnings.add(purchaseOrderDetail);
            }
        }
    }

    /**
     * Shows the warnings attribute according to the Maps and List mappings
     */
    public void showPurchaseOrderDetailWarningMessages() {
        for (Map.Entry<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailBigDecimalEntry : purchaseOrderDetailUnderMinimalStockMap.entrySet()) {
            PurchaseOrderDetail purchaseOrderDetail = purchaseOrderDetailBigDecimalEntry.getKey();
            BigDecimal requiredQuantity = purchaseOrderDetail.getRequestedQuantity();
            // if under minimal Stock
            BigDecimal equivalentQuantity = purchaseOrderDetail.getProductItem().getEquivalentQuantity();
            if (null != equivalentQuantity && equivalentQuantity.compareTo(BigDecimal.ONE) != 0) {
                requiredQuantity = BigDecimalUtil.multiply(requiredQuantity, equivalentQuantity, SCALE);
            }
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "WarehousePurchaseOrderDetail.warning.underMinimalStock",
                    FormatUtils.formatNumber(requiredQuantity, messages.get("patterns.decimalNumber"), sessionUser.getLocale()),
                    FormatUtils.formatNumber(purchaseOrderDetailBigDecimalEntry.getValue(), messages.get("patterns.decimalNumber"), sessionUser.getLocale()),
                    purchaseOrderDetail.getProductItem().getFullName(),
                    purchaseOrderDetail.getProductItem().getUsageMeasureUnit().getMeasureUnitCode());
        }
        for (Map.Entry<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailBigDecimalEntry : purchaseOrderDetailOverMaximumStockMap.entrySet()) {
            PurchaseOrderDetail purchaseOrderDetail = purchaseOrderDetailBigDecimalEntry.getKey();
            BigDecimal requiredQuantity = purchaseOrderDetail.getRequestedQuantity();
            BigDecimal equivalentQuantity = purchaseOrderDetail.getProductItem().getEquivalentQuantity();
            if (null != equivalentQuantity && equivalentQuantity.compareTo(BigDecimal.ONE) != 0) {
                requiredQuantity = BigDecimalUtil.multiply(requiredQuantity, equivalentQuantity, SCALE);
            }
            // if over maximumStock
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "WarehousePurchaseOrderDetail.warning.overMaximumStock",
                    FormatUtils.formatNumber(requiredQuantity, messages.get("patterns.decimalNumber"), sessionUser.getLocale()),
                    FormatUtils.formatNumber(purchaseOrderDetailBigDecimalEntry.getValue(), messages.get("patterns.decimalNumber"), sessionUser.getLocale()),
                    purchaseOrderDetail.getProductItem().getFullName(),
                    purchaseOrderDetail.getProductItem().getUsageMeasureUnit().getMeasureUnitCode());
        }
    }


    /**
     * Cleans the purchaseOrderDetail validation mappings
     */
    public void resetValidateQuantityMappings() {
        purchaseOrderDetailUnderMinimalStockMap = new HashMap<PurchaseOrderDetail, BigDecimal>();
        purchaseOrderDetailOverMaximumStockMap = new HashMap<PurchaseOrderDetail, BigDecimal>();
        purchaseOrderDetailWithoutWarnings = new ArrayList<PurchaseOrderDetail>();
    }

    public boolean isWarehousePurchaseOrderEmpty() {
        return !isManaged() || !service.containPurchaseOrderDetails(getInstance());

    }

    public String getActiveTabName() {
        return activeTabName;
    }

    public void setActiveTabName(String activeTabName) {
        this.activeTabName = activeTabName;
    }

    public void enablePurchaseOrderDetailTab() {
        setActiveTabName("warehousePurchaseOrderTab");
    }

    public void enablePurchaseOrderPaymentTab() {
        setActiveTabName("purchaseOrderPaymentTab");
    }

    public void assignPetitionerJobContract(JobContract jobContract) {
        getInstance().setPetitionerJobContract(jobContract);
        loadPetitionerJobContractValues();
    }

    public void loadPetitionerJobContractValues() {
        if (getInstance().getPetitionerJobContract() != null) {
            getInstance().setPetitionerJobContract(jobContractService.load(getInstance().getPetitionerJobContract()));
            getInstance().setExecutorUnit(getInstance().getPetitionerJobContract().getJob().getOrganizationalUnit().getBusinessUnit());
            getInstance().setCostCenter(getInstance().getPetitionerJobContract().getJob().getOrganizationalUnit().getCostCenter());
        }
    }

    public void unUsedMethod() {
        log.debug("-----------------------------");
    }

    public void clearPetitionerJobContract() {
        getInstance().setPetitionerJobContract(null);
        getInstance().setExecutorUnit(null);
        getInstance().setCostCenter(null);
    }

    public Map<PurchaseOrderDetail, BigDecimal> getPurchaseOrderDetailUnderMinimalStockMap() {
        return purchaseOrderDetailUnderMinimalStockMap;
    }

    public Map<PurchaseOrderDetail, BigDecimal> getPurchaseOrderDetailOverMaximumStockMap() {
        return purchaseOrderDetailOverMaximumStockMap;
    }

    public List<PurchaseOrderDetail> getPurchaseOrderDetailWithoutWarnings() {
        return purchaseOrderDetailWithoutWarnings;
    }

    @Override
    public void addNotFoundMessage() {
        super.addNotFoundMessage();
    }

    public void addProductItemNotFoundMessage(String productItemName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductItem.error.notFound", productItemName);
    }

    public void verifyCondicionWill()
    {
        if(getInstance().getDocumentType() == CollectionDocumentType.INVOICE)
        {
            showBillConditions = true;
            getInstance().setWithBill("CONFACTURA");
        }else{
            showBillConditions = false;
            getInstance().setWithBill("SINFACTURA");
        }

    }

    public Boolean getShowBillConditions() {
        return showBillConditions;
    }

    public void setShowBillConditions(Boolean showBillConditions) {
        this.showBillConditions = showBillConditions;
    }

    public void setWithWill()
    {
        if(this.billConditions)
        {
            getInstance().setWithBill(Constants.WITH_BILL);
        }else{
            getInstance().setWithBill(Constants.WITHOUT_BILL);
        }
    }

    public boolean isCashPayment(){
        return getInstance().getPayConditions().getName().equals(Constants.CONDITION_CASH);
    }

    public Boolean getBillConditions() {
        return billConditions;
    }

    public void setBillConditions(Boolean billConditions) {
        this.billConditions = billConditions;
    }

    public  void generateVoucherReport(PurchaseOrderPayment purchaseOrderPayment){

        Voucher voucher = voucherAccoutingService.getVoucher(purchaseOrderPayment.getIdtmpenc());
        List<VoucherDetail> voucherDetails = voucherAccoutingService.getVoucherDetailList(voucher);

        voucherCreateAction.setVoucher(voucher);
        voucherCreateAction.setVoucherDetails(voucherDetails);
        voucherCreateAction.generateReport(voucher);

    }

}
