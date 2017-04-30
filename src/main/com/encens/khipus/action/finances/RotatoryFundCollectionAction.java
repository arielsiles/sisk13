package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.warehouse.BeneficiaryType;
import com.encens.khipus.service.admin.BusinessUnitService;
import com.encens.khipus.service.finances.*;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * @author
 * @version 2.26
 */
@Name("rotatoryFundCollectionAction")
@Scope(ScopeType.CONVERSATION)
public class RotatoryFundCollectionAction extends GenericAction<RotatoryFundCollection> {
    @In
    private RotatoryFundService rotatoryFundService;

    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;

    @In(value = "rotatoryFundAction")
    private RotatoryFundAction rotatoryFundAction;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private SpendDistributionService spendDistributionService;

    /* In case there is a collectionDocument associated to the collection*/
    private CollectionDocument collectionDocument;

    private List<RotatoryFundCollectionSpendDistribution> spendDistributionList = new ArrayList<RotatoryFundCollectionSpendDistribution>();

    @In
    private RotatoryFundPaymentService rotatoryFundPaymentService;

    @In
    private BusinessUnitService businessUnitService;

    @In
    private AccountingTemplateService accountingTemplateService;

    @In
    private FinanceDocumentService financeDocumentService;

    @In
    private User currentUser;

    @In
    private CompanyConfigurationService companyConfigurationService;

    private int currentIndex;

    @Create
    public void init() {
        if (!isManaged()) {
            collectionDocument = new CollectionDocument();
            collectionDocument.setTransactionNumber("");
            collectionDocument.setExempt(BigDecimal.ZERO);
            collectionDocument.setIce(BigDecimal.ZERO);
            getInstance().setRegisterEmployee(currentUser);
            getInstance().setCreationDate(new Date());
            getInstance().setCollectionDate(new Date());
            getInstance().setState(RotatoryFundCollectionState.PEN);
            getInstance().setCollectionCurrency(getRotatoryFund().getPayCurrency());
            getInstance().setSourceCurrency(getRotatoryFund().getPayCurrency());
            getInstance().setRotatoryFund(rotatoryFundAction.getInstance());
            getInstance().setCollectionAmount(rotatoryFundService.getReceivableResidueByRotatoryFund(getRotatoryFund()));
            getInstance().setDescription(getRotatoryFund().getDescription());
            try {
                getInstance().setExchangeRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("finances currency not found");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("finances exchange rate not found");
            }
        }
    }

    @Factory(value = "rotatoryFundCollection", scope = ScopeType.STATELESS)
    public RotatoryFundCollection initRotatoryFundCollection() {
        return getInstance();
    }

    @Factory(value = "manualCollectionTypes", scope = ScopeType.STATELESS)
    public List<RotatoryFundCollectionType> initManualCollectionTypes() {
        List<RotatoryFundCollectionType> manualList = new ArrayList<RotatoryFundCollectionType>();
        manualList.add(RotatoryFundCollectionType.COLLECTION_BANK_ACCOUNT);
        if (getInstance().getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.PARTNER_WITHDRAWAL)
                || getInstance().getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.RECEIVABLE_FUND)) {
            manualList.add(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT);
        }
        manualList.add(RotatoryFundCollectionType.COLLECTION_CASH_ACCOUNT_ADJ);
        manualList.add(RotatoryFundCollectionType.COLLECTION_DEPOSIT_ADJ);
        return manualList;
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String addRotatoryFundCollection() {
        if (rotatoryFundService.isRotatoryFundNullified(getRotatoryFund())) {
            /* in order to refresh the instance since the database*/
            rotatoryFundService.findRotatoryFund(getRotatoryFund().getId());
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        }
        if (rotatoryFundService.isRotatoryFundLiquidated(getRotatoryFund())) {
            /* in order to refresh the instance since the database*/
            rotatoryFundService.findRotatoryFund(getRotatoryFund().getId());
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        }
        /*if the collections already cover the total*/
        if (rotatoryFundCollectionService.getCollectionSum(getRotatoryFund()).compareTo(rotatoryFundPaymentService.getApprovedPaymentSum(getRotatoryFund())) >= 0) {
            addRotatoryFundPaymentsAlreadyCoveredError();
            return Outcome.REDISPLAY;
        }
        /* to create the new RotatoryFundCollection instance*/
        setInstance(null);
        setOp(OP_CREATE);
        getInstance().setRotatoryFund(rotatoryFundAction.getInstance());
        /*at create time */
        init();
        return Outcome.SUCCESS;
    }

    public void checkCollectionDocumentDate() {
        if (collectionDocument != null &&
                DateUtils.toCalendar(collectionDocument.getDate()).get(Calendar.MONTH) != Calendar.getInstance().get(Calendar.MONTH)) {
            addCollectionDocumentOutOfCurrentMonthMessage();
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTION','CREATE')}")
    public String create() {
        /* validates that if the collection involves an invoice then the paycurrency is national currency */
        if (isDocumentCollection() && collectionDocument != null &&
                collectionDocument.getCollectionDocumentType() != null &&
                collectionDocument.getCollectionDocumentType()
                        .equals(CollectionDocumentType.INVOICE) && !getInstance().getSourceCurrency().equals(FinancesCurrencyType.P)) {
            addWrongInvoiceCurrencyError();
            return Outcome.REDISPLAY;
        }

        if (isDocumentCollection() && !validateSpendDistributionValues()) {
            return Outcome.REDISPLAY;
        }

        /*ensures the calculus be completed in case they have not been completed*/
        updateSourceAmount();
        try {
            rotatoryFundCollectionService.createRotatoryFundCollection(getInstance(), collectionDocument, getSpendDistributionList());
            addCreatedMessage();
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (CollectionSumExceedsRotatoryFundAmountException e) {
            addCollectionSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (ExemptCanNotBeGreaterThanAmountException e) {
            addExemptCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        } catch (ExemptPlusIceCanNotBeGreaterThanAmountException e) {
            addExemptPlusIceCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        } catch (IceCanNotBeGreaterThanAmountException e) {
            addIceCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        }
        select(getInstance());
        return Outcome.REDISPLAY;
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTION','VIEW')}")
    public String select(RotatoryFundCollection instance) {
        try {
            setOp(OP_UPDATE);
            /*refresh the instance from database*/
            setInstance(rotatoryFundCollectionService.findRotatoryFundCollection(instance.getId()));
            if (getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT)) {
                setCollectionDocument(getInstance().getCollectionDocument());
            } else {
                collectionDocument = new CollectionDocument();
                collectionDocument.setRotatoryFundCollection(getInstance());
                collectionDocument.setExempt(BigDecimal.ZERO);
                collectionDocument.setIce(BigDecimal.ZERO);
            }
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTION','UPDATE')}")
    public String update() {
        /* validates that if the collection involves an invoice then the paycurrency is national currency */
        if (isDocumentCollection() && collectionDocument != null &&
                collectionDocument.getCollectionDocumentType() != null &&
                collectionDocument.getCollectionDocumentType()
                        .equals(CollectionDocumentType.INVOICE) && !getInstance().getSourceCurrency().equals(FinancesCurrencyType.P)) {
            addWrongInvoiceCurrencyError();
            return Outcome.REDISPLAY;
        }
        /*ensures the calculus be completed in case they have not been completed*/
        updateSourceAmount();
        try {
            rotatoryFundCollectionService.updateRotatoryFund(getInstance(), collectionDocument, null, true, null);
            addUpdatedMessage();
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (CollectionSumExceedsRotatoryFundAmountException e) {
            addCollectionSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                rotatoryFundCollectionService.findRotatoryFundCollection(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (RotatoryFundCollectionNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (ExemptCanNotBeGreaterThanAmountException e) {
            addExemptCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        } catch (ExemptPlusIceCanNotBeGreaterThanAmountException e) {
            addExemptPlusIceCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        } catch (IceCanNotBeGreaterThanAmountException e) {
            addIceCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionNullifiedException e) {
            addRotatoryFundCollectionAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionApprovedException e) {
            addRotatoryFundCollectionApprovedError();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTIONAPPROVE','VIEW')}")
    public String approveRotatoryFundCollection() {
        /* validates that if the collection involves an invoice then the collectionCurrency is national currency */
        if (isDocumentCollection() && collectionDocument != null &&
                collectionDocument.getCollectionDocumentType() != null &&
                collectionDocument.getCollectionDocumentType()
                        .equals(CollectionDocumentType.INVOICE) && !getInstance().getSourceCurrency().equals(FinancesCurrencyType.P)) {
            addWrongInvoiceCurrencyError();
            return Outcome.REDISPLAY;
        }
        /*ensures the calculus be completed in case they have not been completed*/
        updateSourceAmount();
        try {
            getInstance().setApprovedByEmployee(currentUser);
            getInstance().setApprovalDate(new Date());
            rotatoryFundCollectionService.approveRotatoryFundCollection(getInstance(), collectionDocument, true);
            addRotatoryFundCollectionApprovedMessage();
            return Outcome.SUCCESS;
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
            return Outcome.REDISPLAY;
        } catch (ExemptCanNotBeGreaterThanAmountException e) {
            addExemptCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        } catch (CollectionSumExceedsRotatoryFundAmountException e) {
            addCollectionSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionNullifiedException e) {
            addRotatoryFundCollectionAnnulledError();
            return Outcome.REDISPLAY;
        } catch (IceCanNotBeGreaterThanAmountException e) {
            addIceCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                rotatoryFundCollectionService.findRotatoryFundCollection(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (RotatoryFundCollectionNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (ExemptPlusIceCanNotBeGreaterThanAmountException e) {
            addExemptPlusIceCanNotBeGreaterThanAmountError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionApprovedException e) {
            addRotatoryFundCollectionApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionSpendDistributionSumIsNotTotalException e) {
            addRotatoryFundCollectionSpendDistributionSumIsNotTotalError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionSpendDistributionEmptyException e) {
            addRotatoryFundCollectionSpendDistributionEmptyError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionStateException e) {
            addRotatoryFundCollectionStateError(e);
            return Outcome.REDISPLAY;
        } catch (DuplicatedFinanceAccountingDocumentException e) {
            addDuplicatedFinanceAccountingDocumentError(e);
            return Outcome.REDISPLAY;
        }
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTIONANNUL','VIEW')}")
    public String annulRotatoryFundCollection() {
        try {
            getInstance().setAnnulledByEmployee(currentUser);
            rotatoryFundCollectionService.annulRotatoryFundCollection(getInstance());
            addRotatoryFundCollectionAnnulledMessage();
            return Outcome.SUCCESS;
        } catch (RotatoryFundCollectionNullifiedException e) {
            addRotatoryFundCollectionAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionApprovedException e) {
            addRotatoryFundCollectionApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                rotatoryFundCollectionService.findRotatoryFundCollection(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (RotatoryFundCollectionNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        }
    }


    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTION','DELETE')}")
    public String delete() {
        try {
            rotatoryFundCollectionService.deleteRotatoryFund(getInstance());
            addDeletedMessage();
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundApprovedException e) {
            addRotatoryFundApprovedError();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    protected GenericService getService() {
        return rotatoryFundCollectionService;
    }

    public void bankAccountFieldChanged() {
        updateExchangeRate();
        updateAmounts();
    }

    public void payCurrencyFieldChanged() {
        updateExchangeRate();
        updateSourceAmount();
    }

    public boolean areCurrenciesEqual() {
        /* both fields are not empty */
        return null != getInstance().getSourceCurrency() && null != getInstance().getCollectionCurrency() && getInstance().getSourceCurrency() == getInstance().getCollectionCurrency();
    }

    public void updateExchangeRate() {
        if (getInstance().getSourceCurrency() != null &&
                getInstance().getSourceCurrency().equals(getInstance().getCollectionCurrency()) &&
                getInstance().getCollectionCurrency().equals(FinancesCurrencyType.P)
                ) {
            getInstance().setExchangeRate(BigDecimal.ONE);
        } else {
            try {
                getInstance().setExchangeRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("finances currency not found");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("finances exchange rate not found");
            }
        }
    }

    public void updateAmounts() {
        if (areCurrenciesEqual()) {
            getInstance().setSourceAmount(getInstance().getCollectionAmount());
        } else if (null != getInstance().getSourceCurrency()
                && null != getInstance().getExchangeRate()
                && null != getInstance().getCollectionAmount()) {
            BigDecimal sourceAmount;
            if (getInstance().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                sourceAmount = BigDecimalUtil.multiply(getInstance().getCollectionAmount(), getInstance().getExchangeRate());
            } else {
                sourceAmount = BigDecimalUtil.divide(getInstance().getCollectionAmount(), getInstance().getExchangeRate());
            }
            getInstance().setSourceAmount(sourceAmount);
        } else {
            getInstance().setSourceAmount(null);
            getInstance().setCollectionAmount(null);
        }
    }

    public void updateSourceAmount() {
        if (areCurrenciesEqual()) {
            getInstance().setSourceAmount(getInstance().getCollectionAmount());
        } else if (null != getInstance().getSourceCurrency() && null != getInstance().getCollectionCurrency()
                && null != getInstance().getExchangeRate() && null != getInstance().getCollectionAmount()) {
            BigDecimal sourceAmount;
            if (getInstance().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                sourceAmount = BigDecimalUtil.multiply(getInstance().getCollectionAmount(), getInstance().getExchangeRate());
            } else {
                sourceAmount = BigDecimalUtil.divide(getInstance().getCollectionAmount(), getInstance().getExchangeRate());
            }
            getInstance().setSourceAmount(sourceAmount);
        } else {
            getInstance().setSourceAmount(null);
        }
    }

    public void collectionTypeChanged() {
        if (isCheckCollection()) {
            getInstance().setBeneficiaryType(BeneficiaryType.ORGANIZATION);
        } else {
            getInstance().setBeneficiaryType(null);
        }
        if (isBankCollection() || isCheckCollection()) {
            getInstance().setCashBoxCashAccount(null);
        }
        if (isCheckCollection()) {
            getInstance().setBankAccount(null);
        }
        if (isDocumentCollection()) {
            getInstance().setBankAccount(null);
            getInstance().setCashBoxCashAccount(null);
            getSpendDistributionList().clear();
            for (int i = 0; i < 5; i++) {
                addSpendDistribution();
            }
        } else {
            getSpendDistributionList().clear();
        }
        getInstance().setSourceCurrency(getRotatoryFund().getPayCurrency());
        getInstance().setCollectionCurrency(getRotatoryFund().getPayCurrency());
        getInstance().setReceiver(null);
        getInstance().setObservation(null);
        getInstance().setBankAccountNumber(null);
        getInstance().setCashAccountAdjustment(null);
        getInstance().setDepositAdjustment(null);
        updateSourceAmount();
    }

    public void collectionDocumentTypeFieldChanged() {
        if (collectionDocument != null) {
            cleanInvoiceInfo();
            if (collectionDocument.getCollectionDocumentType().equals(CollectionDocumentType.INVOICE)) {
                collectionDocument.setExempt(BigDecimal.ZERO);
                collectionDocument.setIce(BigDecimal.ZERO);
            }
        }
    }

    public void assignCashBoxCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setCashBoxCashAccount(cashAccount);
        updateExchangeRate();
        updateAmounts();
    }

    public void clearCashBoxCashAccount() {
        getInstance().setCashBoxCashAccount(null);
    }

    public void assignCashAccountAdjustment(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setCashAccountAdjustment(cashAccount);
        getInstance().setSourceCurrency(cashAccount.getCurrency());
        updateExchangeRate();
        updateAmounts();
    }

    public void clearCashAccountAdjustment() {
        getInstance().setCashAccountAdjustment(null);
    }

    public void assignDepositAdjustment(FinanceDocument depositAdjustment) {
        try {
            depositAdjustment = getService().findById(FinanceDocument.class, depositAdjustment.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }

        getInstance().setDepositAdjustment(depositAdjustment);
        getInstance().setSourceCurrency(depositAdjustment.getCurrency());

        if (depositAdjustment.getCurrency().equals(getInstance().getCollectionCurrency())) {
            getInstance().setCollectionAmount(depositAdjustment.getAmount());
        } else {
            if (FinancesCurrencyType.P.equals(getInstance().getCollectionCurrency())) {
                BigDecimal sumCreditDetailNationalAmount = financeDocumentService.sumDetail(
                        getInstance().getDepositAdjustment().getTransactionNumber(),
                        getInstance().getDepositAdjustment().getAccountingMovement(),
                        FinanceMovementType.C);
                getInstance().setCollectionAmount(sumCreditDetailNationalAmount);
            } else {
                try {
                    getInstance().setExchangeRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
                    getInstance().setCollectionAmount(BigDecimalUtil.divide(depositAdjustment.getAmount(), getInstance().getExchangeRate(), 6));
                } catch (FinancesCurrencyNotFoundException ignored) {
                } catch (FinancesExchangeRateNotFoundException ignored) {
                }
            }
        }

        updateExchangeRate();
        updateAmounts();
    }

    public void clearDepositAdjustment() {
        getInstance().setDepositAdjustment(null);
        getInstance().setSourceCurrency(null);
        getInstance().setCollectionAmount(null);
        updateExchangeRate();
        updateAmounts();
    }

    public void cleanInvoiceInfo() {
        collectionDocument.setAuthorizationNumber(null);
        collectionDocument.setControlCode(null);
        collectionDocument.setExempt(null);
        collectionDocument.setIce(null);
        collectionDocument.setIva(null);
        collectionDocument.setNit(null);
        collectionDocument.setFinancesEntity(null);
    }

    public boolean isEnableExchangeRateField() {
        /* both fields are not empty */
        FinancesCurrencyType collectionAccountCurrency = null;
        if (null != getInstance().getCollectionCurrency()) {
            if (null != getInstance().getBankAccount()) {
                collectionAccountCurrency = getInstance().getBankAccount().getCurrency();
            }
            if (null != getInstance().getCashBoxCashAccount()) {
                collectionAccountCurrency = getInstance().getCashBoxCashAccount().getCurrency();
            }
            if (null != getInstance().getCashAccountAdjustment()) {
                collectionAccountCurrency = getInstance().getCashAccountAdjustment().getCurrency();
            }
            if (!FinancesCurrencyType.P.equals(getInstance().getCollectionCurrency())
                    || (null != collectionAccountCurrency && !FinancesCurrencyType.P.equals(collectionAccountCurrency))) {
                return true;
            }
        }
        return false;
    }

    public boolean isRotatoryFundCollectionPending() {
        return !isManaged() || (null != getInstance().getState() && RotatoryFundCollectionState.PEN.equals(getInstance().getState()));
    }

    public boolean isEnableRotatoryFundCollectionType() {
        return !isManaged()
                && getInstance().getGestionPayroll() == null;
    }

    public boolean isEnableBankAccount() {
        return isRotatoryFundCollectionPending()
                && getInstance().getGestionPayroll() == null
                && (isBankCollection()
                || isCheckCollection());
    }

    public boolean isCheckCollection() {
        return false;
    }

    public boolean isCashBoxCollection() {
        return null != getInstance().getRotatoryFundCollectionType() && getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_CASHBOX);
    }

    public boolean isBankCollection() {
        return getInstance().getRotatoryFundCollectionType() != null &&
                getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_BANK_ACCOUNT);
    }

    public boolean isDocumentCollection() {
        return getInstance().getRotatoryFundCollectionType() != null &&
                getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT);
    }

    public boolean isPuchaseOrderCollection() {
        return getInstance().getRotatoryFundCollectionType() != null &&
                getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_BY_PURCHASE_ORDER);
    }

    public boolean isCashAccountAdjustmentCollection() {
        return getInstance().getRotatoryFundCollectionType() != null &&
                getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_CASH_ACCOUNT_ADJ);
    }

    public boolean isDepositAdjustmentCollection() {
        return getInstance().getRotatoryFundCollectionType() != null &&
                getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_DEPOSIT_ADJ);
    }

    public boolean isEnablePayAmount() {
        return isRotatoryFundCollectionPending()
                && getInstance().getGestionPayroll() == null
                && !isDepositAdjustmentCollection();
    }

    public boolean isEnableCollectionDate() {
        return isRotatoryFundCollectionPending()
                && getInstance().getGestionPayroll() == null;
    }

    public boolean isEnableSourceCurrency() {
        return isRotatoryFundCollectionPending() && getInstance().getRotatoryFundCollectionType() != null
                && getInstance().getGestionPayroll() == null && !isBankCollection() && !isCheckCollection();
    }

    public boolean isEnableExchangeRate() {
        return isRotatoryFundCollectionPending()
                && getInstance().getGestionPayroll() == null;
    }

    public boolean isEnableComputeButton() {
        return isEnableExchangeRateField();
    }

    public boolean isEnableCheckFields() {
        return false;
        /*null != getInstance().getRotatoryFundCollectionType()
       && (RotatoryFundCollectionType.COLLECTION_WITH_CHECK.equals(getInstance().getRotatoryFundCollectionType()));*/
    }

    public boolean isRotatoryFundLiquidated() {
        return getInstance().getRotatoryFund() != null && getInstance().getRotatoryFund().getState() != null && (getInstance().getRotatoryFund().getState().equals(RotatoryFundState.LIQ));
    }

    public boolean isInvoiceDocumentType() {
        return collectionDocument != null && collectionDocument.getCollectionDocumentType() != null && collectionDocument.getCollectionDocumentType().equals(CollectionDocumentType.INVOICE);
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFundAction.getInstance();
    }

    public CollectionDocument getCollectionDocument() {
        return collectionDocument;
    }

    public void setCollectionDocument(CollectionDocument collectionDocument) {
        this.collectionDocument = collectionDocument;
    }

    public boolean isEnableSpendDistributionTab() {
        return (rotatoryFundAction.isReceivableFund() || rotatoryFundAction.isPartnerWithdrawal()) && getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT);
    }

    public String getAccountCodeList() {
        List<SpendDistribution> spendDistributionList = spendDistributionService.getSpendDistributionList(getInstance().getRotatoryFund());
        String spendDistributions = "";
        for (SpendDistribution spendDistribution : spendDistributionList) {
            spendDistributions += spendDistribution.getId() + ",";

        }
        /*delete last comma*/
        if (spendDistributions.length() > 0) {
            spendDistributions = spendDistributions.substring(0, spendDistributions.length() - 1);
        }
        if (spendDistributions.length() == 0) {
            spendDistributions = null;
        }
        return spendDistributions;
    }

    public boolean isCollectionWithDocument() {
        return getInstance().getRotatoryFundCollectionType() != null && getInstance().getRotatoryFundCollectionType().equals(RotatoryFundCollectionType.COLLECTION_WITH_DOCUMENT);
    }

    public void clearFinancesEntity() {
        collectionDocument.setFinancesEntity(null);
        collectionDocument.setName(null);
        collectionDocument.setNit(null);
    }

    public void assignFinancesEntity(FinancesEntity financesEntity) {
        collectionDocument.setFinancesEntity(financesEntity);
        postSetFinancesEntity();
    }

    public void setFinancesEntity(Provider provider) {
        if (provider != null && provider.getEntity() != null) {
            collectionDocument.setFinancesEntity(provider.getEntity());
            postSetFinancesEntity();
        }
    }

    public void postSetFinancesEntity() {
        if (null != collectionDocument.getFinancesEntity()) {
            collectionDocument.setNit(collectionDocument.getFinancesEntity().getNitNumber());
            collectionDocument.setName(collectionDocument.getFinancesEntity().getAcronym());
        }
    }

    public List<RotatoryFundCollectionSpendDistribution> getSpendDistributionList() {
        return spendDistributionList;
    }

    public void setSpendDistributionList(List<RotatoryFundCollectionSpendDistribution> spendDistributionList) {
        this.spendDistributionList = spendDistributionList;
    }

    public void addSpendDistribution() {
        RotatoryFundCollectionSpendDistribution spendDistribution = new RotatoryFundCollectionSpendDistribution();
        spendDistribution.setBusinessUnit(getRotatoryFund().getBusinessUnit());
        getSpendDistributionList().add(spendDistribution);
    }

    public void removeSpendDistribution(int index) {
        getSpendDistributionList().remove(index);
    }

    public List<CostCenter> getCostCenterList() {
        return spendDistributionService.getCostCenterListBySpendDistribution(rotatoryFundAction.getInstance());
    }

    public List<CashAccount> getCashAccountList() {
        List<CashAccount> cashAccountResultList = new ArrayList<CashAccount>();
        List<CashAccount> cashAccountList = spendDistributionService.getCashAccountListBySpendDistribution(rotatoryFundAction.getInstance());
        for (CashAccount cashAccount : cashAccountList) {
            if (getInstance().getSourceCurrency().equals(cashAccount.getCurrency())) {
                cashAccountResultList.add(cashAccount);
            }
        }
        return cashAccountResultList;
    }

    public boolean isEnableCostCenterList() {
        return !ValidatorUtil.isEmptyOrNull(getCostCenterList());
    }

    public boolean isEnableCashAccountList() {
        return !ValidatorUtil.isEmptyOrNull(spendDistributionService.getCashAccountListBySpendDistribution(rotatoryFundAction.getInstance()));
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public void assignSpendDistributionCostCenter(CostCenter costCenter) {
        getSpendDistributionList().get(getCurrentIndex()).setCostCenter(costCenter);
    }

    public void clearSpendDistributionCostCenter(int index) {
        getSpendDistributionList().get(index).setCostCenter(null);
    }

    public void assignSpendDistributionCashAccount(CashAccount cashAccount) {
        getSpendDistributionList().get(getCurrentIndex()).setCashAccount(cashAccount);
    }

    public void clearSpendDistributionCashAccount(int index) {
        getSpendDistributionList().get(index).setCashAccount(null);
    }

    public Boolean validateSpendDistributionValues() {
        Boolean valid;
        if (valid = !ValidatorUtil.isEmptyOrNull(getSpendDistributionList())) {
            Boolean hasValues = false;
            Boolean hasRequiredFields = true;
            Boolean hasValidCurrencies = true;
            BigDecimal sumResult = BigDecimal.ZERO;
            for (int i = 0; i < getSpendDistributionList().size() && hasRequiredFields && hasValidCurrencies; i++) {
                RotatoryFundCollectionSpendDistribution spendDistribution = getSpendDistributionList().get(i);
                if (spendDistribution.hasValues()) {
                    hasValues = true;
                    if ((hasRequiredFields = spendDistribution.isValid()) &&
                            (hasValidCurrencies = spendDistribution.getCashAccount().getCurrency().equals(getInstance().getSourceCurrency()))) {
                        sumResult = BigDecimalUtil.sum(sumResult, spendDistribution.getAmount());
                    }
                }
            }

            if (!hasValues) {
                valid = false;
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "RotatoryFundCollection.error.spendDistributionEmpty");
            } else if (!hasRequiredFields) {
                valid = false;
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "RotatoryFundCollection.error.spendDistributionRequiredValues");
            } else if (!hasValidCurrencies) {
                valid = false;
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "RotatoryFundCollection.error.spendDistributionCashAccountCurrency",
                        FormatUtils.toAcronym(
                                MessageUtils.getMessage(getInstance().getSourceCurrency().getResourceKey()),
                                MessageUtils.getMessage(getInstance().getSourceCurrency().getSymbolResourceKey())
                        )
                );
            } else if (getInstance().getSourceAmount() == null) {
                valid = false;
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Common.required",
                        getSourceAmountLabel());
            } else if (getInstance().getSourceAmount().compareTo(sumResult) != 0) {
                valid = false;
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "RotatoryFundCollection.error.spendDistributionSumAmount",
                        getInstance().getSourceAmount());
            }
        }
        return valid;
    }

    public String getSourceAmountLabel() {
        return messages.get(isCheckCollection() ? "RotatoryFundCollection.sourceCheckAmount" :
                isBankCollection() ? "RotatoryFundCollection.sourceBankAmount" :
                        isCashBoxCollection() ? "RotatoryFundCollection.sourceCashBoxAmount" :
                                isPuchaseOrderCollection() ? "RotatoryFundCollection.sourcePurchaseOrderAmount" :
                                        "RotatoryFund.sourceAmount");
    }

    public void appliedAccountingTemplate(AccountingTemplate accountingTemplate) {

        accountingTemplate = accountingTemplateService.readFromDataBase(accountingTemplate);

        List<AccountingTemplateDetail> templateDetailList = accountingTemplate.getAccountingTemplateDetailList();
        Boolean isValidList;

        if (isValidList = !ValidatorUtil.isEmptyOrNull(templateDetailList)) {
            List<RotatoryFundCollectionSpendDistribution> spendDistributionTempList = new ArrayList<RotatoryFundCollectionSpendDistribution>();
            RotatoryFundCollectionSpendDistribution maxSpendDistribution = null;
            Integer maxSpendDistributionIndex = null;
            BigDecimal sumAmount = BigDecimal.ZERO;
            Boolean hasCreditValues = false;
            Boolean hasValidCurrencies = true;
            isValidList = false;
            for (int i = 0; i < templateDetailList.size() && !hasCreditValues && hasValidCurrencies; i++) {
                AccountingTemplateDetail templateDetail = templateDetailList.get(i);

                if (templateDetail.getCredit() != null) {
                    hasCreditValues = true;
                    continue;
                }

                if (templateDetail.getDebit() != null) {
                    isValidList = true;
                    RotatoryFundCollectionSpendDistribution spendDistribution = new RotatoryFundCollectionSpendDistribution();
                    if (templateDetail.getExecutorUnit() != null) {
                        spendDistribution.setBusinessUnit(businessUnitService.findBusinessUnitByExecutorUnitCode(templateDetail.getExecutorUnit().getExecutorUnitCode()));
                    }
                    if (templateDetail.getCostCenter() != null) {
                        spendDistribution.setCostCenter(templateDetail.getCostCenter());
                    }
                    if (templateDetail.getCashAccount() != null) {
                        if (!templateDetail.getCashAccount().getCurrency().equals(getInstance().getSourceCurrency())) {
                            hasValidCurrencies = false;
                            continue;
                        }
                        spendDistribution.setCashAccount(templateDetail.getCashAccount());
                    }

                    spendDistribution.setAmount(
                            BigDecimalUtil.divide(getInstance().getSourceAmount().multiply(templateDetail.getDebit()),
                                    BigDecimalUtil.ONE_HUNDRED));

                    sumAmount = BigDecimalUtil.sum(sumAmount, spendDistribution.getAmount());

                    spendDistributionTempList.add(spendDistribution);

                    if (maxSpendDistribution == null || spendDistribution.getAmount().compareTo(maxSpendDistribution.getAmount()) >= 0) {
                        maxSpendDistribution = spendDistribution;
                        maxSpendDistributionIndex = i;
                    }
                }
            }

            if (hasCreditValues) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "RotatoryFundCollection.error.accountingTemplateCreditValues");
            } else if (!hasValidCurrencies) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "RotatoryFundCollection.error.accountingTemplateCashAccountCurrency",
                        FormatUtils.toAcronym(
                                MessageUtils.getMessage(getInstance().getSourceCurrency().getResourceKey()),
                                MessageUtils.getMessage(getInstance().getSourceCurrency().getSymbolResourceKey())
                        )
                );
            } else if (isValidList) {

                BigDecimal balance = BigDecimalUtil.subtract(sumAmount, getInstance().getSourceAmount());
                if (!BigDecimalUtil.isZeroOrNull(balance)) {
                    maxSpendDistribution.setAmount(
                            BigDecimalUtil.isPositive(balance) ?
                                    BigDecimalUtil.subtract(maxSpendDistribution.getAmount(), balance) :
                                    BigDecimalUtil.sum(maxSpendDistribution.getAmount(), balance.abs())

                    );
                    spendDistributionTempList.set(maxSpendDistributionIndex, maxSpendDistribution);
                }


                setSpendDistributionList(spendDistributionTempList);
            }
        }

        if (!isValidList) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "RotatoryFundCollection.error.accountingTemplateEmpty");
        } else if (getInstance().getSourceCurrency() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "RotatoryFundCollection.error.accountingTemplateSourceCurrency");

        }
    }

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollection.message.created");
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollection.message.deleted");
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollection.message.updated");
    }

    private void addRotatoryFundAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundAlreadyAnnulled", getRotatoryFund().getCode());
    }

    private void addRotatoryFundCollectionSpendDistributionSumIsNotTotalError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundCollectionSpendDistributionSumIsNotTotal");
    }

    private void addRotatoryFundCollectionSpendDistributionEmptyError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundCollectionSpendDistributionEmpty");
    }

    private void addRotatoryFundCollectionStateError(RotatoryFundCollectionStateException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundCollectionSpendDistributionEmpty", e.getActualState().name());
    }

    private void addDuplicatedFinanceAccountingDocumentError(DuplicatedFinanceAccountingDocumentException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.duplicatedFinanceAccountingDocument",
                e.getDuplicateId().getEntityCode(),
                e.getDuplicateId().getInvoiceNumber(),
                e.getDuplicateId().getAuthorizationNumber());
    }

    private void addRotatoryFundCollectionAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundCollectionAlreadyAnnulled");
    }

    private void addRotatoryFundCollectionApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundCollectionAlreadyApproved");
    }

    private void addRotatoryFundCollectionAnnulledMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollection.message.rotatoryFundCollectionAnnulled");
    }

    private void addRotatoryFundCollectionApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollection.message.rotatoryFundCollectionApproved");
    }

    private void addRotatoryFundPaymentsAlreadyCoveredError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundPaymentsAlreadyCovered");
    }

    private void addRotatoryFundApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundAlreadyApproved");
    }

    private void addRotatoryFundLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.rotatoryFundAlreadyLiquidated");
    }

    private void addCollectionSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.collectionSumExceedsRotatoryFundAmount", getRotatoryFund().getAmount());
    }

    private void addWrongInvoiceCurrencyError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.wrongInvoiceCurrency");
    }

    private void addExemptCanNotBeGreaterThanAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.exemptCanNotBeGreaterThanAmount", getRotatoryFund().getAmount());
    }

    private void addIceCanNotBeGreaterThanAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.iceCanNotBeGreaterThanAmount", getRotatoryFund().getAmount());
    }

    private void addExemptPlusIceCanNotBeGreaterThanAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollection.error.exemptPlusIceCanNotBeGreaterThanAmount", getRotatoryFund().getAmount());
    }

    protected void addCollectionDocumentOutOfCurrentMonthMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollection.info.collectionDocumentOutOfCurrentMonth");
    }

    public List<CashAccount> getDepositInTransitAccountList() {
        List<CashAccount> result = new ArrayList<CashAccount>();
        CashAccount depositInTransitNationalCurrencyAccount = getDepositInTransitNationalCurrencyAccount();
        CashAccount depositInTransitForeignCurrencyAccount = getDepositInTransitForeignCurrencyAccount();
        if (null != depositInTransitNationalCurrencyAccount) {
            result.add(depositInTransitNationalCurrencyAccount);
        }
        if (null != depositInTransitForeignCurrencyAccount) {
            result.add(depositInTransitForeignCurrencyAccount);
        }
        return !ValidatorUtil.isEmptyOrNull(result) ? result : null;
    }

    public CashAccount getDepositInTransitNationalCurrencyAccount() {
        CashAccount depositInTransitNationalCurrencyAccount;
        try {
            depositInTransitNationalCurrencyAccount = companyConfigurationService.findCompanyConfiguration().getDepositInTransitNationalCurrencyAccount();
            return isDepositAdjustmentCollection() && null != depositInTransitNationalCurrencyAccount ? depositInTransitNationalCurrencyAccount : null;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return null;
        }
    }

    public CashAccount getDepositInTransitForeignCurrencyAccount() {
        CashAccount depositInTransitForeignCurrencyAccount;
        try {
            depositInTransitForeignCurrencyAccount = companyConfigurationService.findCompanyConfiguration().getDepositInTransitForeignCurrencyAccount();
            return isDepositAdjustmentCollection() && null != depositInTransitForeignCurrencyAccount ? depositInTransitForeignCurrencyAccount : null;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return null;
        }
    }
}