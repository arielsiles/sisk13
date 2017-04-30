package com.encens.khipus.action.purchases;

import com.encens.khipus.action.AppIdentity;
import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseDocumentAmountException;
import com.encens.khipus.exception.purchase.PurchaseDocumentException;
import com.encens.khipus.exception.purchase.PurchaseDocumentNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseDocumentStateException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.purchases.PurchaseDocumentState;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.purchases.PurchaseDocumentService;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.25
 */
@Name("purchaseDocumentAction")
@Scope(ScopeType.CONVERSATION)
public class PurchaseDocumentAction extends GenericAction<PurchaseDocument> {

    private GenericAction<PurchaseOrder> purchaseOrderAction;

    @In(create = true)
    private VoucherUpdateAction voucherUpdateAction;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private PurchaseDocumentService purchaseDocumentService;

    @In
    private SessionUser sessionUser;

    @In
    private AppIdentity identity;

    @Factory(value = "purchaseDocument", scope = ScopeType.STATELESS)
    public PurchaseDocument initializePurchaseOrderDocument() {
        return getInstance();
    }

    public List<CollectionDocumentType> getPurchaseDocumentTypeList() {
        List<CollectionDocumentType> collectionDocumentTypeList = new ArrayList<CollectionDocumentType>();
        if (purchaseOrderAction.getInstance().getDocumentType() != null) {
            collectionDocumentTypeList.add(purchaseOrderAction.getInstance().getDocumentType());
        }

        if (CollectionDocumentType.INVOICE.equals(purchaseOrderAction.getInstance().getDocumentType()) &&
                identity.hasPermission("PURCHASEDOCUMENTADJUSTMENT", "VIEW")) {
            collectionDocumentTypeList.add(CollectionDocumentType.ADJUSTMENT);
        }

        return collectionDocumentTypeList;
    }

    @Factory(value = "purchaseDocumentCurrencies", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] initFinanceCurrencyTypes() {
        return new FinancesCurrencyType[]{FinancesCurrencyType.P};
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("PurchaseDocument.tab.title");
    }

    public Boolean isPurchaseOrderPending() {
        return purchaseOrderAction.getInstance().isPurchaseOrderPending();
    }

    public String addPurchaseDocument() {
        setOp(OP_CREATE);

        //set a null v in the current instance to force a create the new instance.
        setInstance(null);
        putDefaultValues();
        return Outcome.SUCCESS;
    }

    public String addPurchaseDocumentFromAccounting() {
        setOp(OP_CREATE);

        //set a null v in the current instance to force a create the new instance.
        setInstance(null);
        /*putDefaultValues();*/
        return Outcome.SUCCESS;
    }

    @Override
    public String select(PurchaseDocument purchaseDocument) {
        setOp(OP_UPDATE);
        setInstance(purchaseDocument);
        try {
            setInstance(purchaseDocumentService.readDocument(purchaseDocument.getId()));

            return Outcome.SUCCESS;
        } catch (PurchaseDocumentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    public String create() {
        try {
            updateAdjustmentValues();
            purchaseDocumentService.createDocument(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseDocumentException e) {
            addPurchaseDocumentErrorMessages(e.getErrorTypes());

            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentAmountException e) {
            addPurchaseDocumentAmountErrorMessage(e.getLimit());

            return Outcome.REDISPLAY;
        }
    }

    public String createFromAccounting() {
        try {
            /*updateAdjustmentValues();*/
            System.out.println("............>> PurchaseDocument : " + getInstance().toString());

            Voucher voucher = voucherUpdateAction.getInstance();
            getInstance().setVoucher(voucher);
            purchaseDocumentService.createDocumentSimple(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseDocumentException e) {
            addPurchaseDocumentErrorMessages(e.getErrorTypes());

            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentAmountException e) {
            addPurchaseDocumentAmountErrorMessage(e.getLimit());

            return Outcome.REDISPLAY;
        }
    }

    @Override
    public String update() {
        try {
            updateAdjustmentValues();
            purchaseDocumentService.updateDocument(getInstance());
            addUpdatedMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseDocumentStateException e) {
            addPurchaseDocumentStateErrorMessage(e.getCurrentState());

            return Outcome.FAIL;
        } catch (PurchaseDocumentAmountException e) {
            addPurchaseDocumentAmountErrorMessage(e.getLimit());

            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentException e) {
            addPurchaseDocumentErrorMessages(e.getErrorTypes());

            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {

            try {
                setInstance(purchaseDocumentService.readDocument(getInstance().getId()));
            } catch (PurchaseDocumentNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }

            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        }
    }

    public String postApprovedUpdate() {
        updateAdjustmentValues();
        return super.update();
    }

    @Override
    public String delete() {
        throw new UnsupportedOperationException();
    }

    public String approve() {
        try {
            updateAdjustmentValues();
            purchaseDocumentService.approveDocument(getInstance());
            addApproveSuccessfulMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseDocumentStateException e) {
            addPurchaseDocumentStateErrorMessage(e.getCurrentState());

            return Outcome.FAIL;
        } catch (PurchaseDocumentAmountException e) {
            addPurchaseDocumentAmountErrorMessage(e.getLimit());

            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        } catch (DuplicatedFinanceAccountingDocumentException e) {
            addDuplicatedFinanceAccountingDocumentErrorMessage(e);

            return Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();

            return Outcome.FAIL;
        }
    }

    public String approveFromAccounting() {
        try {
            //updateAdjustmentValues();
            purchaseDocumentService.approveDocumentSimple(getInstance());
            addApproveSuccessfulMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseDocumentStateException e) {
            addPurchaseDocumentStateErrorMessage(e.getCurrentState());

            return Outcome.FAIL;
        } catch (PurchaseDocumentAmountException e) {
            addPurchaseDocumentAmountErrorMessage(e.getLimit());

            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        } catch (DuplicatedFinanceAccountingDocumentException e) {
            addDuplicatedFinanceAccountingDocumentErrorMessage(e);

            return Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();

            return Outcome.FAIL;
        }
    }

    public String nullify() {
        try {
            purchaseDocumentService.nullifyDocument(getInstance());

            return Outcome.SUCCESS;
        } catch (PurchaseDocumentNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        } catch (PurchaseDocumentStateException e) {
            addPurchaseDocumentStateErrorMessage(e.getCurrentState());

            return Outcome.FAIL;
        }
    }

    public void assignFinancesEntity(FinancesEntity financesEntity) {
        getInstance().setFinancesEntity(financesEntity);
        if (financesEntity != null) {
            getInstance().setNit(financesEntity.getNitNumber());
        }
    }

    public void clearFinancesEntity() {
        getInstance().setFinancesEntity(null);
    }

    public void updateDocumentType() {
        getInstance().setCurrency(FinancesCurrencyType.P);
        getInstance().setCashAccountAdjustment(null);
    }

    public void updateExchangeRate() {
        BigDecimal exchangeRate = BigDecimal.ONE;
        if (null != getInstance().getCurrency() && FinancesCurrencyType.D.equals(getInstance().getCurrency())) {
            try {
                exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("The last exchange rate cannot be loaded.");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("Not exist the las change rate.");
            }
        }

        getInstance().setExchangeRate(exchangeRate);
    }

    public void setPurchaseOrderAction(GenericAction<PurchaseOrder> purchaseOrderAction) {
        this.purchaseOrderAction = purchaseOrderAction;
    }

    private void putDefaultValues() {
        getInstance().setPurchaseOrder(purchaseOrderAction.getInstance());
        getInstance().setPurchaseOrderId(purchaseOrderAction.getInstance().getId());
        getInstance().setState(PurchaseDocumentState.PENDING);
        getInstance().setIce(BigDecimal.ZERO);
        getInstance().setExempt(BigDecimal.ZERO);
        getInstance().setCurrency(FinancesCurrencyType.P);
        getInstance().setExchangeRate(BigDecimal.ONE);
        getInstance().setType(purchaseOrderAction.getInstance().getDocumentType());
        assignFinancesEntity(purchaseOrderAction.getInstance().getProvider().getEntity());
    }

    private void addPurchaseDocumentErrorMessages(List<PurchaseDocumentException.ErrorType> errorTypes) {
        for (PurchaseDocumentException.ErrorType errorType : errorTypes) {
            if (PurchaseDocumentException.ErrorType.ICE_NEGATIVE_VALUE.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.iceNegative");
            }

            if (PurchaseDocumentException.ErrorType.ICE_GREATER_THAN_AMOUNT.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.iceGreaterThanAmount", formatDecimalNumber(getInstance().getAmount()));
            }

            if (PurchaseDocumentException.ErrorType.EXEMPT_NEGATIVE_VALUE.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.exemptNegative");
            }

            if (PurchaseDocumentException.ErrorType.EXEMPT_GREATER_THAN_AMOUNT.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.exemptGreaterThanAmount", formatDecimalNumber(getInstance().getAmount()));
            }

            if (PurchaseDocumentException.ErrorType.SUM_ICE_EXEMPT_EXCEED_AMOUNT.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.sumIceExemptExceedAmount", formatDecimalNumber(getInstance().getAmount()));
            }

        }
    }

    @Override
    protected void addNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.error.notFound", getInstance().getNumber());
    }

    private void addPurchaseDocumentAmountErrorMessage(BigDecimal limit) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseDocument.error.amountExceedPurchaseOrderAmount", formatDecimalNumber(limit));
    }

    private void addPurchaseDocumentStateErrorMessage(PurchaseDocumentState state) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseDocument.error.stateIsUnEditablePurchase", MessageUtils.getMessage(state.getResourceKey()));
    }

    private void addDuplicatedFinanceAccountingDocumentErrorMessage(DuplicatedFinanceAccountingDocumentException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FinanceAccountDocument.error.duplicatedFinanceAccountingDocument",
                e.getDuplicateId().getEntityCode(),
                e.getDuplicateId().getInvoiceNumber(),
                e.getDuplicateId().getAuthorizationNumber());
    }

    private void addApproveSuccessfulMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseDocument.message.approveSuccessfulMessage", getInstance().getNumber());
    }

    private String formatDecimalNumber(BigDecimal value) {
        if (null != value) {
            return FormatUtils.formatNumber(value,
                    MessageUtils.getMessage("patterns.decimalNumber"),
                    sessionUser.getLocale());
        }

        return "";
    }

    public void assignCashAccountAdjustment(CashAccount cashAccount) {
        getInstance().setCashAccountAdjustment(cashAccount);
        getInstance().setCurrency(cashAccount.getCurrency());
        updateExchangeRate();
    }

    public void clearCashAccountAdjustment() {
        getInstance().setCashAccountAdjustment(null);
        getInstance().setCurrency(null);
    }

    private void updateAdjustmentValues() {
        if (getInstance().isAdjustmentDocument()) {
            getInstance().setNetAmount(getInstance().getAmount());
            if (ValidatorUtil.isBlankOrNull(getInstance().getNumber())) {
                getInstance().setNumber(messages.get("PurchaseDocument.adjustmentNumber"));
            }
        }
    }

    public VoucherUpdateAction getVoucherUpdateAction() {
        return voucherUpdateAction;
    }

    public void setVoucherUpdateAction(VoucherUpdateAction voucherUpdateAction) {
        this.voucherUpdateAction = voucherUpdateAction;
    }
}
