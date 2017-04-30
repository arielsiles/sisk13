package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.model.warehouse.BeneficiaryType;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.purchases.GlossGeneratorService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.24
 */

@Name("liquidationPaymentAction")
@Scope(ScopeType.CONVERSATION)
public class LiquidationPaymentAction {
    private PurchaseOrderPayment liquidationPayment = new PurchaseOrderPayment();

    private List<PurchaseOrder> selectedPurchaseOrdersWithCheck = new ArrayList<PurchaseOrder>();

    @In
    private GlossGeneratorService glossGeneratorService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private GenericService genericService;

    @In
    protected FacesMessages facesMessages;

    @Logger
    protected Log log;

    private PurchaseOrder purchaseOrder;
    // this list stores the PurchaseOrderDetails that when choise payment in check
    private List<PurchaseOrder> purchaseOrdersWithCheck = new ArrayList<PurchaseOrder>();
    private boolean hasAccountCorrency = true;

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public PurchaseOrderPayment getLiquidationPayment() {
        return liquidationPayment;
    }

    public void clearExchangeRateField() {
        getLiquidationPayment().setExchangeRate(null);
    }

    public void changeBeneficiaryFields(PurchaseOrder order) {
        if (null != order && isEnableBeneficiaryFields()) {
            getLiquidationPayment().setBeneficiaryName(order.getProvider().getEntity().getAcronym());
            getLiquidationPayment().setBeneficiaryType(BeneficiaryType.PERSON);
        } else {
            getLiquidationPayment().setBeneficiaryName(null);
            getLiquidationPayment().setBeneficiaryType(BeneficiaryType.ORGANIZATION);
        }
    }

    public void cleanBeneficiaryFields() {
        getLiquidationPayment().setBeneficiaryName(null);
        getLiquidationPayment().setBeneficiaryType(BeneficiaryType.ORGANIZATION);
    }

    public boolean isEnableBeneficiaryFields() {
        return (isCheckPayment() || isBankPayment() || isCashBoxPayment());
    }

    public void assignCashBoxCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = genericService.findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
        getLiquidationPayment().setCashBoxCashAccount(cashAccount);
        accountChanged();
    }

    public void clearCashBoxCashAccount() {
        getLiquidationPayment().setCashBoxCashAccount(null);
    }

    public void assignRotatoryFund(RotatoryFund rotatoryFund) {
        try {
            rotatoryFund = genericService.findById(RotatoryFund.class, rotatoryFund.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
        getLiquidationPayment().setRotatoryFund(rotatoryFund);
        accountChanged();
    }

    public void clearRotatoryFund() {
        getLiquidationPayment().setRotatoryFund(null);
    }

    public boolean isEnableExchangeRateField() {
        return useExchangeCurrency();
    }

    public boolean useExchangeCurrency() {
        return null != getLiquidationPayment().getSourceCurrency()
                && (FinancesCurrencyType.D.equals(getLiquidationPayment().getSourceCurrency()));
    }

    public boolean isEnableBankAccount() {
        return (isBankPayment()
                || isCheckPayment());
    }

    public boolean isCheckPayment() {
        return getLiquidationPayment().getPaymentType() != null &&
                getLiquidationPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK);
    }

    public boolean isBankPayment() {
        return getLiquidationPayment().getPaymentType() != null &&
                getLiquidationPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT);
    }

    public boolean isCashBoxPayment() {
        return null != getLiquidationPayment().getPaymentType()
                && getLiquidationPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_CASHBOX);
    }

    public boolean isRotatoryFundPayment() {
        return null != getLiquidationPayment().getPaymentType()
                && getLiquidationPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND);
    }

    /* In case of a change of bank account or cash account */

    public void accountChanged() {
        FinancesCurrencyType selectedCurrency = null;
        if ((isBankPayment() || isCheckPayment()) && null != getLiquidationPayment().getBankAccount()) {
            selectedCurrency = getLiquidationPayment().getBankAccount().getCurrency();
            getLiquidationPayment().setPayCurrency(selectedCurrency);
        } else if ((isCashBoxPayment()) && null != getLiquidationPayment().getCashBoxCashAccount()) {
            selectedCurrency = getLiquidationPayment().getCashBoxCashAccount().getCurrency();
            getLiquidationPayment().setPayCurrency(selectedCurrency);
        } else if (isRotatoryFundPayment() && null != getLiquidationPayment().getRotatoryFund()) {
            selectedCurrency = getLiquidationPayment().getRotatoryFund().getPayCurrency();
            getLiquidationPayment().setPayCurrency(selectedCurrency);
        }
        if (null != selectedCurrency && selectedCurrency.equals(FinancesCurrencyType.D)) {
            try {
                getLiquidationPayment().setExchangeRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("finances currency not found");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("finances exchange rate not found");
            }
        } else {
            getLiquidationPayment().setExchangeRate(BigDecimal.ONE);
        }


        if(selectedCurrency != null)
            hasAccountCorrency = false;
        else
            hasAccountCorrency = true;
    }

    public void setDefaultDescription(PurchaseOrder purchaseOrder, String module, String acronym) {
        String suggestedDescription = glossGeneratorService.generatePurchaseOrderGloss(purchaseOrder,
                module,
                acronym);
        getLiquidationPayment().setDescription(suggestedDescription);
    }

    public void setLiquidationPayment(PurchaseOrderPayment liquidationPayment) {
        this.liquidationPayment = liquidationPayment;
    }

    public void computePayment(BigDecimal payAmount) {
        payAmount = BigDecimalUtil.roundBigDecimal(payAmount);
        getLiquidationPayment().setPayAmount(payAmount);
        BigDecimal sourceAmount = payAmount;
        if (getLiquidationPayment().getSourceCurrency().equals(FinancesCurrencyType.D)) {
            sourceAmount = BigDecimalUtil.divide(sourceAmount, getLiquidationPayment().getExchangeRate());
        }
        getLiquidationPayment().setSourceAmount(sourceAmount);
        if (isRotatoryFundPayment() && sourceAmount.compareTo(getLiquidationPayment().getRotatoryFund().getReceivableResidue()) > 0) {
            addRotatoryFundReceivableResidueError(sourceAmount);
        }
    }

    public boolean checkPayment(BigDecimal payAmount) {
        if (BigDecimalUtil.isZeroOrNull(payAmount)) {
            return true;
        }

        if (getLiquidationPayment().getPayAmount() == null ||
                getLiquidationPayment().getSourceAmount() == null) {
            return false;
        }

        BigDecimal bankAmount = payAmount;
        if (getLiquidationPayment().getSourceCurrency().equals(FinancesCurrencyType.D)) {
            bankAmount = BigDecimalUtil.divide(bankAmount, getLiquidationPayment().getExchangeRate());
        }

        if (isRotatoryFundPayment() && bankAmount.compareTo(getLiquidationPayment().getRotatoryFund().getReceivableResidue()) > 0) {
            addRotatoryFundReceivableResidueError(bankAmount);
            return false;
        }

        return payAmount.compareTo(getLiquidationPayment().getPayAmount()) == 0 &&
                bankAmount.compareTo(getLiquidationPayment().getSourceAmount()) == 0;
    }

    public void paymentTypeChanged() {
        if (isBankPayment() || isCheckPayment()) {
            getLiquidationPayment().setCashBoxCashAccount(null);
            getLiquidationPayment().setBeneficiaryName(getPurchaseOrder().getProvider().getEntity().getAcronym());
            getLiquidationPayment().setBeneficiaryType(BeneficiaryType.PERSON);
        } else if (isCashBoxPayment()) {
            getLiquidationPayment().setBeneficiaryName(getPurchaseOrder().getProvider().getEntity().getAcronym());
            getLiquidationPayment().setBeneficiaryType(BeneficiaryType.PERSON);
            getLiquidationPayment().setBankAccount(null);
        }
        if (isRotatoryFundPayment()) {
            getLiquidationPayment().setRotatoryFund(null);
        }
        getLiquidationPayment().setPayAmount(null);
        getLiquidationPayment().setSourceAmount(null);
    }

    public void removePurchaseOrder(BigDecimal payAmount) {
        computePayment(payAmount);
    }

    public void entryNotFoundErrorLog(Exception e) {
        log.error("entity was removed by another user...", e);
    }

    public void addRotatoryFundConcurrencyMessage() {
        if (getLiquidationPayment().getRotatoryFund() != null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "RotatoryFundCollection.error.concurrency", getLiquidationPayment().getRotatoryFund().getCode());
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PurchaseOrder.error.concurrency", getPurchaseOrder().getOrderNumber());
        }
    }

    public void addRotatoryFundLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundAlreadyLiquidated", getLiquidationPayment().getRotatoryFund().getCode());
    }

    public void addCollectionSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.collectionSumExceedsRotatoryFundAmount", getLiquidationPayment().getRotatoryFund().getAmount());
    }

    public void addRotatoryFundReceivableResidueError(BigDecimal sourceCurrency) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PurchaseOrderPayment.error.rotatoryFundReceivableResidue"
                , sourceCurrency
                , getLiquidationPayment().getRotatoryFund().getReceivableResidue()
                , getLiquidationPayment().getRotatoryFund().getFullName());
    }

    public void addRotatoryFundAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundAlreadyAnnulled", getLiquidationPayment().getRotatoryFund().getCode());
    }

    public List<PurchaseOrder> getPurchaseOrdersWithCheck() {
        return purchaseOrdersWithCheck;
    }

    public void setPurchaseOrdersWithCheck(List<PurchaseOrder> purchaseOrdersWithCheck) {
        this.purchaseOrdersWithCheck = purchaseOrdersWithCheck;
    }

    public List<PurchaseOrder> getSelectedPurchaseOrdersWithCheck() {
        return selectedPurchaseOrdersWithCheck;
    }

    public void setSelectedPurchaseOrdersWithCheck(List<PurchaseOrder> selectedPurchaseOrdersWithCheck) {
        this.selectedPurchaseOrdersWithCheck = selectedPurchaseOrdersWithCheck;
    }

    public boolean isHasAccountCorrency() {
        return hasAccountCorrency;
    }

    public void setHasAccountCorrency(boolean hasAccountCorrency) {
        this.hasAccountCorrency = hasAccountCorrency;
    }
}
