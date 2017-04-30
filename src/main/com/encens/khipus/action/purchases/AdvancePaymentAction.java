package com.encens.khipus.action.purchases;

import com.encens.khipus.action.warehouse.WarehousePurchaseOrderAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.finances.RotatoryFundReceivableResidueException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.AdvancePaymentAmountException;
import com.encens.khipus.exception.warehouse.AdvancePaymentStateException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.PurchaseOrderPaymentKind;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.purchases.*;
import com.encens.khipus.model.warehouse.BeneficiaryType;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.finances.PaymentRemakeHelperService;
import com.encens.khipus.service.purchases.GlossGeneratorService;
import com.encens.khipus.service.purchases.PurchaseOrderService;
import com.encens.khipus.service.warehouse.AdvancePaymentRemakeService;
import com.encens.khipus.service.warehouse.AdvancePaymentService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.24
 */
@Name("advancePaymentAction")
@Scope(ScopeType.CONVERSATION)
public class AdvancePaymentAction extends GenericAction<PurchaseOrderPayment> {

    private PurchaseOrderService purchaseOrderService;

    private GenericAction<PurchaseOrder> purchaseOrderAction;

    private PurchaseOrderPayment instanceToRemake;

    private String oldDocumentNumber;

    private boolean useOldDocumentNumber = false;

    @In
    private AdvancePaymentService advancePaymentService;

    @In
    private GlossGeneratorService glossGeneratorService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private AdvancePaymentRemakeService advancePaymentRemakeService;

    @In
    private PaymentRemakeHelperService paymentRemakeHelperService;

    @In
    private User currentUser;

    @Factory(value = "advancePayment", scope = ScopeType.STATELESS)
    public PurchaseOrderPayment initAdvancePayment() {
        return getInstance();
    }

    @Create
    public void initialize() {
        if (!isManaged()) {
            getInstance().setPurchaseOrderPaymentKind(PurchaseOrderPaymentKind.ADVANCE_PAYMENT);
            getInstance().setState(PurchaseOrderPaymentState.PENDING);
            getInstance().setPaymentType(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK);

            try {
                getInstance().setExchangeRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("finances currency not found");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("finances exchange rate not found");
            }

        }
    }

    @Factory(value = "payCurrencies", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] initFinanceCurrencyTypes() {
        return new FinancesCurrencyType[]{FinancesCurrencyType.D, FinancesCurrencyType.P};
    }

    @Factory(value = "beneficiaryTypes", scope = ScopeType.STATELESS)
    public BeneficiaryType[] initBeneficiaryTypes() {
        return BeneficiaryType.values();
    }

    public void paymentTypeChanged() {
        if (getPurchaseOrder().getProvider() != null) {
            getInstance().setBeneficiaryName(getPurchaseOrder().getProvider().getEntity().getAcronym());
        }
        getInstance().setBeneficiaryType(BeneficiaryType.PERSON);
        if (isBankPayment() || isCheckPayment()) {
            getInstance().setCashBoxCashAccount(null);
        }
        if (isCashBoxPayment()) {
            getInstance().setBankAccount(null);
        }
    }

    public String addAdvancePayment() {
        if (purchaseOrderService.isPurchaseOrderLiquidated(getPurchaseOrder())) {
            purchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addWarehousePurchaseOrderLiquidatedError();
            return WarehousePurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

        setInstance(null);
        setOp(OP_CREATE);
        initialize();
        getInstance().setPurchaseOrder(getPurchaseOrder());
        getInstance().setBeneficiaryName(getPurchaseOrder().getProvider().getEntity().getAcronym());
        getInstance().setBeneficiaryType(BeneficiaryType.PERSON);
        getInstance().setPayCurrency(FinancesCurrencyType.P);
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @Override
    public String cancel() {
        return super.cancel();
    }

    @Override
    public String select(PurchaseOrderPayment instance) {
        return super.select(instance);
    }

    public String selectToRemake(PurchaseOrderPayment instance) {
        setOp(OP_CREATE);
        try {
            instanceToRemake = genericService.findById(PurchaseOrderPayment.class, instance.getId());
            oldDocumentNumber = paymentRemakeHelperService.getOldDocumentNumber(instanceToRemake.getCompanyNumber(),
                    instanceToRemake.getTransactionNumber());
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }

        setInstance(advancePaymentRemakeService.readToRemake(instanceToRemake));
        return Outcome.SUCCESS;
    }

    public String remake() {
        try {
            advancePaymentRemakeService.remake(instanceToRemake, getInstance(), useOldDocumentNumber);

            return Outcome.SUCCESS;
        } catch (PurchaseOrderNullifiedException e) {
            addWarehousePurchaseOrderNullifiedError();
            return Outcome.FAIL;
        } catch (AdvancePaymentStateException e) {
            addStateErrorMessage(e.getActualState());
            return Outcome.CANCEL;
        } catch (AdvancePaymentAmountException e) {
            addPayAmountErrorMessage(e.getLimit(), e.getDefaultCurrencySymbol());
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    public String create() {
        try {
            advancePaymentService.createAdvancePayment(getInstance());
        } catch (PurchaseOrderLiquidatedException e) {
            addWarehousePurchaseOrderLiquidatedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (PurchaseOrderNullifiedException e) {
            addWarehousePurchaseOrderNullifiedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (AdvancePaymentAmountException e) {
            addPayAmountErrorMessage(e.getLimit(), e.getDefaultCurrencySymbol());
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        select(getInstance());
        return com.encens.khipus.framework.action.Outcome.REDISPLAY;
    }

    @Override
    public String update() {
        try {
            advancePaymentService.updateAdvancePayment(getInstance());
        } catch (PurchaseOrderNullifiedException e) {
            addWarehousePurchaseOrderNullifiedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (PurchaseOrderLiquidatedException e) {
            addWarehousePurchaseOrderLiquidatedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (AdvancePaymentAmountException e) {
            addPayAmountErrorMessage(e.getLimit(), e.getDefaultCurrencySymbol());
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return com.encens.khipus.framework.action.Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (AdvancePaymentStateException e) {
            addStateErrorMessage(e.getActualState());
            return com.encens.khipus.framework.action.Outcome.CANCEL;
        } catch (RotatoryFundReceivableResidueException e) {
            addRotatoryFundReceivableResidueError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }

        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    public String approve() {
        try {
            advancePaymentService.approveAdvancePayment(getInstance());
        } catch (AdvancePaymentStateException e) {
            addStateErrorMessage(e.getActualState());
            return com.encens.khipus.framework.action.Outcome.CANCEL;
        } catch (PurchaseOrderLiquidatedException e) {
            addWarehousePurchaseOrderLiquidatedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (PurchaseOrderNullifiedException e) {
            addWarehousePurchaseOrderNullifiedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (RotatoryFundConcurrencyException e) {
            addRotatoryFundConcurrencyMessage();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (CollectionSumExceedsRotatoryFundAmountException e) {
            addCollectionSumExceedsRotatoryFundAmountError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (RotatoryFundReceivableResidueException e) {
            addRotatoryFundReceivableResidueError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }

        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    public String nullify() {
        try {
            advancePaymentService.nullifyAdvancePayment(getInstance());
        } catch (AdvancePaymentStateException e) {
            addStateErrorMessage(e.getActualState());
            return com.encens.khipus.framework.action.Outcome.CANCEL;
        } catch (PurchaseOrderLiquidatedException e) {
            addWarehousePurchaseOrderLiquidatedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        } catch (PurchaseOrderNullifiedException e) {
            addWarehousePurchaseOrderNullifiedError();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }

        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    private PurchaseOrder getPurchaseOrder() {
        return purchaseOrderAction.getInstance();
    }

    private void addWarehousePurchaseOrderFinalizedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderPayment.error.purchaseOrderAlreadyFinalized",
                getPurchaseOrder().getOrderNumber());
    }

    public void addWarehousePurchaseOrderLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderAlreadyLiquidated", getPurchaseOrder().getOrderNumber());
    }

    private void addWarehousePurchaseOrderNullifiedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderPayment.error.purchaseOrderAlreadyNullified",
                getPurchaseOrder().getOrderNumber());
    }

    public boolean isEnableBeneficiaryFields() {
        return isCheckPayment() || isBankPayment() || isCashBoxPayment();
    }

    public void assignCashBoxCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            //
        }
        getInstance().setCashBoxCashAccount(cashAccount);
        accountChanged();
    }

    public void clearCashBoxCashAccount() {
        getInstance().setCashBoxCashAccount(null);
    }

    public void assignRotatoryFund(RotatoryFund rotatoryFund) {
        try {
            rotatoryFund = getService().findById(RotatoryFund.class, rotatoryFund.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
        getInstance().setRotatoryFund(rotatoryFund);
        accountChanged();
    }

    public void clearRotatoryFund() {
        getInstance().setRotatoryFund(null);
    }


    public Boolean checkIsEnabledToRemake(PurchaseOrderPayment payment) {
        return null != payment && advancePaymentRemakeService.isEnabledToRemake(payment);
    }

    public boolean isEnableExchangeRateField() {
        return getInstance().useExchangeCurrency();
    }

    public boolean isEnableBankAccount() {
        return (isBankPayment()
                || isCheckPayment());
    }

    public boolean isCheckPayment() {
        return getInstance().getPaymentType() != null &&
                getInstance().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK);
    }

    public boolean isBankPayment() {
        return getInstance().getPaymentType() != null &&
                getInstance().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT);
    }

    public boolean isCashBoxPayment() {
        return null != getInstance().getPaymentType()
                && getInstance().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_CASHBOX);
    }

    public boolean isRotatoryFundPayment() {
        return null != getInstance().getPaymentType()
                && getInstance().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND);
    }

    public boolean isApproved() {
        return null != getInstance().getState() && PurchaseOrderPaymentState.APPROVED.equals(getInstance().getState());
    }

    public boolean isNullified() {
        return null != getInstance().getState() && PurchaseOrderPaymentState.NULLIFIED.equals(getInstance().getState());
    }

    public boolean isPending() {
        return null != getInstance().getState() && PurchaseOrderPaymentState.PENDING.equals(getInstance().getState());
    }

    public boolean isRemake() {
        return null != instanceToRemake;
    }

    /* In case of a change of bank account or cash account */

    public void accountChanged() {
        if (!isPurchaseOrderLiquidated()) {
            FinancesCurrencyType selectedCurrency;
            if ((isBankPayment() || isCheckPayment()) && null != getInstance().getBankAccount()) {
                selectedCurrency = getInstance().getBankAccount().getCurrency();
                getInstance().setPayCurrency(selectedCurrency);
            } else if ((isCashBoxPayment()) && null != getInstance().getCashBoxCashAccount()) {
                selectedCurrency = getInstance().getCashBoxCashAccount().getCurrency();
                getInstance().setPayCurrency(selectedCurrency);
            } else if (isRotatoryFundPayment() && null != getInstance().getRotatoryFund()) {
                selectedCurrency = getInstance().getRotatoryFund().getPayCurrency();
                getInstance().setPayCurrency(selectedCurrency);
            }
        }
    }

    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public void setPurchaseOrderAction(GenericAction<PurchaseOrder> purchaseOrderAction) {
        this.purchaseOrderAction = purchaseOrderAction;
    }

    public void setDefaultDescription(PurchaseOrder purchaseOrder) {
        String moduleGloss = null;
        String acronym = null;
        if (purchaseOrder.getOrderType().equals(PurchaseOrderType.WAREHOUSE)) {
            moduleGloss = MessageUtils.getMessage("WarehousePurchaseOrder.warehouses");
            acronym = MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym");
        } else {
            moduleGloss = MessageUtils.getMessage("FixedAssetPurchaseOrder.fixedAssets");
            acronym = MessageUtils.getMessage("FixedAssetPurchaseOrder.orderNumberAcronym");
        }
        String suggestedDescription = glossGeneratorService.generatePurchaseOrderGloss(purchaseOrder,
                moduleGloss,
                acronym);
        getInstance().setDescription(suggestedDescription);
    }

    private void addPayAmountErrorMessage(BigDecimal limit, String defaultCurrencySymbol) {
        if (null == limit || BigDecimal.ZERO.compareTo(limit) == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PurchaseOrderPayment.error.unableCreate");
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PurchaseOrderPayment.error.payAmount", limit, MessageUtils.getMessage(defaultCurrencySymbol));
        }
    }

    private void addStateErrorMessage(PurchaseOrderPaymentState actualState) {
        if (PurchaseOrderPaymentState.APPROVED.equals(actualState)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PurchaseOrderPayment.error.concurrencyApproved");
        }
        if (PurchaseOrderPaymentState.NULLIFIED.equals(actualState)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PurchaseOrderPayment.error.concurrencyNullified");
        }
    }

    public boolean isPurchaseOrderLiquidated() {
        return purchaseOrderService.isPurchaseOrderLiquidated(getPurchaseOrder());
    }


    public PurchaseOrderPayment getInstanceToRemake() {
        return instanceToRemake;
    }

    public void setInstanceToRemake(PurchaseOrderPayment instanceToRemake) {
        this.instanceToRemake = instanceToRemake;
    }

    public String getOldDocumentNumber() {
        return oldDocumentNumber;
    }

    public void setOldDocumentNumber(String oldDocumentNumber) {
        this.oldDocumentNumber = oldDocumentNumber;
    }

    public boolean isUseOldDocumentNumber() {
        return useOldDocumentNumber;
    }

    public void setUseOldDocumentNumber(boolean useOldDocumentNumber) {
        this.useOldDocumentNumber = useOldDocumentNumber;
    }

    public void addRotatoryFundConcurrencyMessage() {
        if (getInstance().getRotatoryFund() != null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "RotatoryFundCollection.error.concurrency", getInstance().getRotatoryFund().getCode());
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PurchaseOrder.error.concurrency", getPurchaseOrder().getOrderNumber());
        }
    }

    public void addRotatoryFundLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundAlreadyLiquidated", getInstance().getRotatoryFund().getCode());
    }

    public void addRotatoryFundReceivableResidueError() {
        try {
            getInstance().setRotatoryFund(getService().findById(RotatoryFund.class, getInstance().getRotatoryFund().getId(), true));
        } catch (EntryNotFoundException e) {
        }
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PurchaseOrderPayment.error.rotatoryFundReceivableResidue"
                , getInstance().getSourceAmount()
                , getInstance().getRotatoryFund().getReceivableResidue()
                , getInstance().getRotatoryFund().getFullName());
    }

    public void addCollectionSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.collectionSumExceedsRotatoryFundAmount", getInstance().getRotatoryFund().getAmount());
    }

    public void addRotatoryFundAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundAlreadyAnnulled", getInstance().getRotatoryFund().getCode());
    }
}
