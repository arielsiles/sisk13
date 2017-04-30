package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.AccountingMovement;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.PayableDocument;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.service.finances.PayableDocumentService;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.2.9
 */
@Name("payableDocumentConverterAction")
@Scope(ScopeType.CONVERSATION)
public class PayableDocumentConverterAction extends GenericAction<PayableDocument> {

    @Logger
    private Log log;
    @In(value = "accountingMovementDataModelForPayableDocumentConverter", required = false)
    private AccountingMovementDataModelForPayableDocumentConverter accountingMovementDataModel;
    @In
    private PayableDocumentService payableDocumentService;

    @Factory(value = "payableDocumentConverter", scope = ScopeType.STATELESS)
    public PayableDocument init() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("PayableDocument.title") + " " + FormatUtils.concatBySeparator("-", getInstance().getDocumentTypeCode(), getInstance().getDocumentNumber());
    }

    @End(ifOutcome = Outcome.SUCCESS)
    public String convertToPayableDocument() {

        AccountingMovement itemForPayableDocument = null;
        List<AccountingMovement> itemsForPayments = new ArrayList<AccountingMovement>();

        boolean existsSelectedForPayableAccount = false;
        for (Map.Entry<AccountingMovement, Boolean> entry : accountingMovementDataModel.getSelectedForPayableAccount().entrySet()) {
            if (entry.getValue()) {
                if (existsSelectedForPayableAccount) {
                    itemForPayableDocument = null;
                } else {
                    existsSelectedForPayableAccount = true;
                    itemForPayableDocument = entry.getKey();
                }
            }
        }

        boolean existsSelectedForPayments = false;
        for (Map.Entry<AccountingMovement, Boolean> entry : accountingMovementDataModel.getSelectedForPayments().entrySet()) {
            if (entry.getValue()) {
                existsSelectedForPayments = true;
                itemsForPayments.add(entry.getKey());
            }
        }
        Boolean redisplay = false;

        if (!existsSelectedForPayableAccount) {
            addNotDefineItemForPayableDocumentMessage();
            redisplay = true;
        } else if (itemForPayableDocument == null) {
            addDefineOnlyOneItemForPayableDocumentMessage();
            redisplay = true;
        }

        if (!existsSelectedForPayments) {
            addNotDefineItemsForPaymentsMessage();
            redisplay = true;
        }

        if (!redisplay && !Collections.disjoint(accountingMovementDataModel.getSelectedForPayableAccount().entrySet(), accountingMovementDataModel.getSelectedForPayments().entrySet())) {
            addCannotSelectBothOptionsMessage();
            redisplay = true;
        }

        if (getInstance().getProvider() == null) {
            addRequiredMessage("PayableDocument.provider");
            redisplay = true;
        }
        if (getInstance().getDocumentType() == null) {
            addRequiredMessage("PayableDocument.documentType");
            redisplay = true;
        }
        if (getInstance().getPayableAccount() == null) {
            addRequiredMessage("PayableDocument.payableAccount");
            redisplay = true;
        }

        if (redisplay) {
            return Outcome.REDISPLAY;
        }

        try {
            setInstance(
                    payableDocumentService.convertToPayableDocument(getInstance(),
                            accountingMovementDataModel.getConverterType(),
                            accountingMovementDataModel.getBusinessUnit(),
                            itemForPayableDocument,
                            itemsForPayments)
            );
            addCreatedMessage();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
        }

        return Outcome.SUCCESS;
    }

    private void addRequiredMessage(String messageKey) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Common.required",
                messages.get(messageKey)
        );
    }

    private void addNotDefineItemForPayableDocumentMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PayableDocumentConverter.error.notDefineItemForPayableDocument");
    }

    private void addDefineOnlyOneItemForPayableDocumentMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PayableDocumentConverter.error.defineOnlyOneItemForPayableDocument");
    }

    private void addNotDefineItemsForPaymentsMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PayableDocumentConverter.error.notDefineItemsForPayments");
    }

    private void addCannotSelectBothOptionsMessage() {
        log.info(MessageUtils.getMessage("PayableDocumentConverter.error.cannotSelectBothOptions"));
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PayableDocumentConverter.error.cannotSelectBothOptions");
    }

    public void assignProvider(Provider provider) {
        getInstance().setProvider(provider);
    }

    public void updateProviderInfo() {
        try {
            getInstance().setProvider(getService().findById(Provider.class, getInstance().getProvider().getId()));
            accountingMovementDataModel.setProvider(getInstance().getProvider());
        } catch (EntryNotFoundException ignored) {
        }
        getInstance().setEntity(getInstance().getProvider().getEntity());
        getInstance().setPayableAccount(getInstance().getProvider().getPayableAccount());
    }

    public void clearProvider() {
        accountingMovementDataModel.setProvider(null);
        getInstance().setProvider(null);
        getInstance().setEntity(null);
    }

    public void assignPayableAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException ignored) {
        }
        getInstance().setPayableAccount(cashAccount);
    }

    public void clearPayableAccount() {
        getInstance().setPayableAccount(null);
    }
}
