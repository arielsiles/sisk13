package com.encens.khipus.util.purchases;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.service.finances.FinanceAccountingDocumentService;
import com.encens.khipus.service.purchases.PurchaseDocumentService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author
 * @version 2.26
 */
@Name("purchaseOrderValidator")
@Scope(ScopeType.EVENT)
public class PurchaseOrderValidator {

    @In
    private SessionUser sessionUser;

    @In
    private PurchaseDocumentService purchaseDocumentService;

    @In
    private FacesMessages facesMessages;

    @In
    private Map<String, String> messages;

    @In
    private FinanceAccountingDocumentService financeAccountingDocumentService;

    public boolean isValidThePurchaseDocuments(PurchaseOrder purchaseOrder, boolean validateAmounts) {
        if(purchaseOrder.getWithBill() != null)
        {
            if(purchaseOrder.getWithBill().compareTo(Constants.WITH_BILL) ==0 )
                try {
                    if(financeAccountingDocumentService.findByOrderVoucher(purchaseOrder).size() == 0)
                    {
                        addNotPruchaseDocumentErrorMessage(purchaseOrder);
                        return false;
                    }
                } catch (CompanyConfigurationNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    return false;
                }

        }
        if (purchaseOrder.getDocumentType() != null && hasDistinctPurchaseDocumentType(purchaseOrder)) {
            addDistinctPurchaseDocumentTypeErrorMessage(purchaseOrder);
            return false;
        }
        if (validateAmounts) {
            // get the su of the purchaseOrder documents
            BigDecimal totalAmount = getSumPurchaseDocumentAmounts(purchaseOrder);
            boolean isTotalAmountPositive = totalAmount.compareTo(BigDecimal.ZERO) > 0;

            if (containInvoiceNumber(purchaseOrder) && (!isTotalAmountPositive)) {
                // in case there isn't approved purchaseOrder documents
                addPurchaseDocumentNotFoundErrorMessage();
                return false;
            }

            // if there is pending purchaseOrder documents
            if (countPendingPurchaseDocuments(purchaseOrder) > 0) {
                // in case there is pending purchaseOrder documents
                addPurchaseDocumentsPendingErrorMessage();
                return false;
            }

            BigDecimal purchaseOrderAmount = purchaseOrder.getTotalAmount();
            // if there is approved documents
            if (isTotalAmountPositive && (totalAmount.compareTo(purchaseOrderAmount) != 0)) {
                // in case the approved purchaseOrder documents sum is not equal to purchaseOrder amount
                addPurchaseDocumentAmountErrorMessage(purchaseOrderAmount);
                return false;
            }
        }
        return true;
    }

    public void addPurchaseDocumentNotFoundErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseDocumentRequired");
    }

    public void addPurchaseDocumentAmountErrorMessage(BigDecimal purchaseOrderTotalAmount) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseDocumentTotalAmount", formatDecimalNumber(purchaseOrderTotalAmount));
    }

    public void addPurchaseDocumentsPendingErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseDocumentPending");
    }

    public void addNotPruchaseDocumentErrorMessage(PurchaseOrder purchaseOrder) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "PurchaseOrder.error.NotPruchaseDocument",
                purchaseOrder.getOrderNumber());
    }

    public void addDistinctPurchaseDocumentTypeErrorMessage(PurchaseOrder purchaseOrder) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.distinctPurchaseDocumentType",
                purchaseOrder.getOrderNumber(),
                messages.get(purchaseOrder.getDocumentType().getResourceKey())
        );
    }

    private BigDecimal getSumPurchaseDocumentAmounts(PurchaseOrder purchaseOrder) {
        return purchaseDocumentService.sumApprovedPurchaseDocumentAmounts(purchaseOrder);
    }

    private Long countPendingPurchaseDocuments(PurchaseOrder purchaseOrder) {
        return purchaseDocumentService.countPendingPurchaseDocuments(purchaseOrder);
    }

    private Boolean hasDistinctPurchaseDocumentType(PurchaseOrder purchaseOrder) {
        Long counter = purchaseDocumentService.countDistinctByPurchaseOrder(purchaseOrder, purchaseOrder.getDocumentType());
        return counter != null && counter > 0;
    }

    private boolean containInvoiceNumber(PurchaseOrder purchaseOrder) {
        return !ValidatorUtil.isBlankOrNull(purchaseOrder.getInvoiceNumber());
    }

    private String formatDecimalNumber(BigDecimal value) {
        if (null != value) {
            return FormatUtils.formatNumber(value,
                    MessageUtils.getMessage("patterns.decimalNumber"),
                    sessionUser.getLocale());
        }

        return "";
    }
}
