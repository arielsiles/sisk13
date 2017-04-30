package com.encens.khipus.action.purchases;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.exception.warehouse.AdvancePaymentAmountException;
import com.encens.khipus.exception.warehouse.AdvancePaymentStateException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentState;
import com.encens.khipus.service.purchases.PurchaseOrderRemakePaymentsService;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 3.2.10
 */
@Name("purchaseOrderRemakePaymentsAction")
@Scope(ScopeType.CONVERSATION)
public class PurchaseOrderRemakePaymentsAction extends GenericAction<PurchaseOrder> {
    @In
    private SessionUser sessionUser;
    @In
    private FacesMessages facesMessages;
    @In
    private PurchaseOrderRemakePaymentsService purchaseOrderRemakePaymentsService;

    public String remakePayments() {

        try {
            List<PurchaseOrderPayment> purchaseOrderPaymentList = purchaseOrderRemakePaymentsService.remakePayments();
            addSuccessOperationMessage(messages.get("PurchaseOrder.remakesPurchaseOrderPayments"));
            addSuccessRemakePaymentMessage(purchaseOrderPaymentList);
        } catch (AdvancePaymentAmountException e) {
            addPayAmountErrorMessage(e.getLimit(), e.getDefaultCurrencySymbol());
            return Outcome.CANCEL;
        } catch (AdvancePaymentStateException e) {
            addStateErrorMessage(e.getActualState());
            return Outcome.CANCEL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.FAIL;
        } catch (PurchaseOrderNullifiedException e) {
            addFailRemakePaymentMessage();
            return Outcome.CANCEL;
        } catch (Exception e) {
            e.printStackTrace();
            addFailRemakePaymentMessage();
            return Outcome.CANCEL;
        }
        return Outcome.SUCCESS;
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

    private void addFailRemakePaymentMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.remakesPurchaseOrderPayments");
    }

    private void addSuccessRemakePaymentMessage(List<PurchaseOrderPayment> purchaseOrderPaymentList) {
        String fragmentMessage = "";
        for (PurchaseOrderPayment purchaseOrderPayment : purchaseOrderPaymentList) {
            String formattedAmount = FormatUtils.toAcronym(
                    FormatUtils.formatNumber(purchaseOrderPayment.getSourceAmount(), messages.get("patterns.decimalNumber"), sessionUser.getLocale()),
                    messages.get(purchaseOrderPayment.getSourceCurrency().getSymbolResourceKey())
            );
            fragmentMessage += MessageUtils.getMessage("PurchaseOrder.remakePaymentFragmentMessage",
                    messages.get(purchaseOrderPayment.getPaymentType().getResourceKey()),
                    formattedAmount,
                    purchaseOrderPayment.getPurchaseOrder().getOrderNumber()
            );
            fragmentMessage += ", ";
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.remakePaymentMainMessage",
                fragmentMessage);
    }

}
