package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.FixedAssetPayment;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.model.warehouse.FixedAssetBeneficiaryType;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.purchases.GlossGeneratorService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.5.2.2
 */
@Name("fixedAssetPaymentAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetPaymentAction extends GenericAction<FixedAssetPayment> {
    @In
    private GlossGeneratorService glossGeneratorService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;


    @Factory(value = "fixedAssetPayment", scope = ScopeType.STATELESS)
    public FixedAssetPayment initCharge() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "description";
    }

    @Factory(value = "fixedAssetPaymentTypes", scope = ScopeType.STATELESS)
    public PurchaseOrderPaymentType[] initPaymentTypes() {
        return PurchaseOrderPaymentType.values();
    }

    @Factory(value = "fixedAssetBeneficiaryTypes", scope = ScopeType.STATELESS)
    public FixedAssetBeneficiaryType[] initFixedAssetBeneficiaryTypes() {
        return FixedAssetBeneficiaryType.values();
    }

    @Factory(value = "fixedAssetPaymentCurrencies", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] initFinanceCurrencyTypes() {
        return new FinancesCurrencyType[]{FinancesCurrencyType.D, FinancesCurrencyType.P};
    }

    public void clearExchangeRateField() {
        getInstance().setExchangeRate(null);
    }

    public void paymentTypeChanged() {
        getInstance().setFixedAssetBeneficiaryType(FixedAssetBeneficiaryType.PERSON);
        if (isBankPayment() || isCheckPayment()) {
            getInstance().setCashBoxCashAccount(null);
        }
        if (isCashBoxPayment()) {
            getInstance().setBankAccount(null);
        }
    }

    public void cleanBeneficiaryFields() {
        getInstance().setBeneficiaryName(null);
        getInstance().setFixedAssetBeneficiaryType(FixedAssetBeneficiaryType.ORGANIZATION);
    }

    public boolean isEnableBeneficiaryFields() {
        return null != getInstance().getPaymentType()
                && PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(getInstance().getPaymentType());
    }

    public void assignCashBoxCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
        getInstance().setCashBoxCashAccount(cashAccount);
        accountChanged();
    }

    public void clearCashBoxCashAccount() {
        getInstance().setCashBoxCashAccount(null);
    }

    public boolean isEnableExchangeRateField() {
        return useExchangeCurrency();
    }

    public boolean useExchangeCurrency() {
        return null != getInstance().getSourceCurrency()
                && (FinancesCurrencyType.D.equals(getInstance().getSourceCurrency()));
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

    /* In case of a change of bank account or cash account */

    public void accountChanged() {
        FinancesCurrencyType selectedCurrency = null;
        if ((isBankPayment() || isCheckPayment()) && null != getInstance().getBankAccount()) {
            selectedCurrency = getInstance().getBankAccount().getCurrency();
            getInstance().setPayCurrency(selectedCurrency);
        } else if ((isCashBoxPayment()) && null != getInstance().getCashBoxCashAccount()) {
            selectedCurrency = getInstance().getCashBoxCashAccount().getCurrency();
            getInstance().setPayCurrency(selectedCurrency);
        }
        if (null != selectedCurrency && selectedCurrency.equals(FinancesCurrencyType.D)) {
            try {
                getInstance().setExchangeRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("finances currency not found");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("finances exchange rate not found");
            }
        } else {
            getInstance().setExchangeRate(BigDecimal.ONE);
        }
    }

    public void setDefaultDescription(PurchaseOrder purchaseOrder, String moduleGloss, String acronym) {
        String suggestedDescription = glossGeneratorService.generatePurchaseOrderGloss(purchaseOrder,
                moduleGloss,
                acronym);
        getInstance().setDescription(suggestedDescription);
    }
}