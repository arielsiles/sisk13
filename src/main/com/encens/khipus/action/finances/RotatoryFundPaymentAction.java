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
import com.encens.khipus.service.finances.*;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * @author
 * @version 2.24
 */
@Name("rotatoryFundPaymentAction")
@Scope(ScopeType.CONVERSATION)
public class RotatoryFundPaymentAction extends GenericAction<RotatoryFundPayment> {
    @In
    private RotatoryFundService rotatoryFundService;

    @In
    private RotatoryFundPaymentService rotatoryFundPaymentService;

    @In(value = "rotatoryFundAction")
    private RotatoryFundAction rotatoryFundAction;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private SpendDistributionService spendDistributionService;

    @In
    private User currentUser;

    @In
    private RotatoryFundPaymentRemakeService rotatoryFundPaymentRemakeService;

    private RotatoryFundPayment instanceToRemake;

    private String oldDocumentNumber;

    private boolean useOldDocumentNumber = false;

    @Create
    public void init() {
        if (!isManaged()) {
            getInstance().setRegisterEmployee(currentUser);
            getInstance().setCreationDate(new Date());
            getInstance().setPaymentDate(new Date());
            getInstance().setState(RotatoryFundPaymentState.PEN);
            getInstance().setPaymentCurrency(getRotatoryFund().getPayCurrency());
            getInstance().setRotatoryFund(rotatoryFundAction.getInstance());
            getInstance().setBeneficiaryName(getRotatoryFund().getEmployee().getSingleFullName());
            getInstance().setRotatoryFundPaymentType(RotatoryFundPaymentType.PAYMENT_WITH_CHECK);
            getInstance().setBeneficiaryType(BeneficiaryType.PERSON);
            getInstance().setPaymentAmount(
                    BigDecimalUtil.subtract(getRotatoryFund().getAmount()
                            , rotatoryFundPaymentService.getPendantPaymentSum(getRotatoryFund())
                            , rotatoryFundPaymentService.getApprovedPaymentSum(getRotatoryFund())));
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

    @Factory(value = "rotatoryFundPayment", scope = ScopeType.STATELESS)
    public RotatoryFundPayment initRotatoryFundPayment() {
        return getInstance();
    }

    @Factory(value = "manualPaymentTypes", scope = ScopeType.STATELESS)
    public List<RotatoryFundPaymentType> initManualPaymentTypes() {
        RotatoryFundPaymentType[] array = RotatoryFundPaymentType.values();
        return Arrays.asList(array);
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String addRotatoryFundPayment() {
        String validationOutcome = rotatoryFundStateValidation();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        /*if the payments already cover the total*/
        if (BigDecimalUtil.sum(rotatoryFundPaymentService.getPendantPaymentSum(getRotatoryFund()),
                rotatoryFundPaymentService.getApprovedPaymentSum(getRotatoryFund())).compareTo(getRotatoryFund().getAmount()) >= 0) {
            addRotatoryFundAlreadyCoveredError();
            return Outcome.REDISPLAY;
        }


        setInstance(null);
        setOp(OP_CREATE);
        getInstance().setRotatoryFund(rotatoryFundAction.getInstance());
        /*at create time */
        init();

        initializeRemakeFields();
        return Outcome.SUCCESS;
    }

    @Override
    @Restrict("#{s:hasPermission('ROTATORYFUNDPAYMENT','CREATE')}")
    public String create() {
        /*ensures the calculus be completed in case they have not been completed*/
        updateSourceAmount();
        try {
            rotatoryFundPaymentService.createRotatoryFundPayment(getInstance());
            addCreatedMessage();
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (PaymentSumExceedsRotatoryFundAmountException e) {
            addPaymentSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        }
        select(getInstance());
        return Outcome.REDISPLAY;
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('ROTATORYFUNDPAYMENT','VIEW')}")
    public String select(RotatoryFundPayment instance) {
        try {
            setOp(OP_UPDATE);
            /*refresh the instance from database*/
            setInstance(rotatoryFundPaymentService.findRotatoryFundPayment(instance.getId()));
            initializeRemakeFields();
        } catch (RotatoryFundPaymentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('REMAKEROTATORYFUNDPAYMENT','VIEW')}")
    public String selectToRemake(RotatoryFundPayment sourceRotatoryFundPayment) {
        String validationOutcome = rotatoryFundStateValidation();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        try {
            instanceToRemake = rotatoryFundPaymentService.findRotatoryFundPayment(sourceRotatoryFundPayment.getId());
            oldDocumentNumber = rotatoryFundPaymentRemakeService.getOldDocumentNumber(sourceRotatoryFundPayment);
        } catch (RotatoryFundPaymentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }

        setInstance(rotatoryFundPaymentRemakeService.readToRemake(sourceRotatoryFundPayment));
        setOp(OP_CREATE);

        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('REMAKEROTATORYFUNDPAYMENT','VIEW')}")
    public String remake() {
        try {
            updateSourceAmount();
            rotatoryFundPaymentRemakeService.remake(instanceToRemake, getInstance(), useOldDocumentNumber);
            initializeRemakeFields();

            return Outcome.SUCCESS;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentAnnulledException e) {
            addRotatoryFundPaymentAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (PaymentSumExceedsRotatoryFundAmountException e) {
            addPaymentSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDPAYMENT','UPDATE')}")
    public String update() {
        /*ensures the calculus be completed in case they have not been completed*/
        updateSourceAmount();
        try {
            rotatoryFundPaymentService.updateRotatoryFund(getInstance(), null);
            addUpdatedMessage();
        } catch (RotatoryFundPaymentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (PaymentSumExceedsRotatoryFundAmountException e) {
            addPaymentSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                rotatoryFundPaymentService.findRotatoryFundPayment(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (RotatoryFundPaymentNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentAnnulledException e) {
            addRotatoryFundPaymentAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentApprovedException e) {
            addRotatoryFundPaymentApprovedError();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDPAYMENTAPPROVE','VIEW')}")
    public String approveRotatoryFundPayment() {
        /*ensures the calculus be completed in case they have not been completed*/
        updateSourceAmount();
        try {
            getInstance().setApprovedByEmployee(currentUser);
            getInstance().setApprovalDate(new Date());
            rotatoryFundPaymentService.approveRotatoryFundPayment(getInstance());
            addRotatoryFundPaymentApprovedMessage();
            return Outcome.SUCCESS;
        } catch (RotatoryFundPaymentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.REDISPLAY;
        } catch (PaymentSumExceedsRotatoryFundAmountException e) {
            addPaymentSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentAnnulledException e) {
            addRotatoryFundPaymentAnnulledError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                rotatoryFundPaymentService.findRotatoryFundPayment(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (RotatoryFundPaymentNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (RotatoryFundPaymentApprovedException e) {
            addRotatoryFundPaymentApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        }
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDPAYMENTANNUL','VIEW')}")
    public String annulRotatoryFundPayment() {
        try {
            getInstance().setAnnulledByEmployee(currentUser);
            rotatoryFundPaymentService.annulRotatoryFundPayment(getInstance());
            addRotatoryFundPaymentAnnulledMessage();
            return Outcome.SUCCESS;
        } catch (RotatoryFundPaymentAnnulledException e) {
            addRotatoryFundPaymentAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentApprovedException e) {
            addRotatoryFundPaymentApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                rotatoryFundPaymentService.findRotatoryFundPayment(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (RotatoryFundPaymentNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        }
    }


    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDPAYMENT','DELETE')}")
    public String delete() {
        try {
            rotatoryFundPaymentService.deleteRotatoryFund(getInstance());
            addDeletedMessage();
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundApprovedException e) {
            addRotatoryFundApprovedError();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (RotatoryFundPaymentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundPaymentApprovedException e) {
            addRotatoryFundPaymentApprovedError();
            return Outcome.REDISPLAY;
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
        return rotatoryFundPaymentService;
    }

    public void bankAccountFieldChanged() {
        updateExchangeRate();
        updateAmounts();
    }

    public void payCurrencyFieldChanged() {
        updateExchangeRate();
    }

    /* updates all the data taking into account rotatory fund amount */

    public void updateAmounts() {
        if (areCurrenciesEqual()) {
            getInstance().setSourceAmount(getInstance().getPaymentAmount());
        } else if (null != getInstance().getSourceCurrency()
                && null != getInstance().getExchangeRate()
                && null != getInstance().getPaymentAmount()) {
            BigDecimal sourceAmount;
            if (getInstance().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                sourceAmount = BigDecimalUtil.multiply(getInstance().getPaymentAmount(), getInstance().getExchangeRate());
            } else {
                sourceAmount = BigDecimalUtil.divide(getInstance().getPaymentAmount(), getInstance().getExchangeRate());
            }
            getInstance().setSourceAmount(sourceAmount);
        } else {
            getInstance().setSourceAmount(null);
            getInstance().setPaymentAmount(null);
        }


    }

    /* update source amount based on exchange rate and payment amount*/

    public void updateSourceAmount() {
        if (areCurrenciesEqual()) {
            getInstance().setSourceAmount(getInstance().getPaymentAmount());
        } else if (null != getInstance().getSourceCurrency() && null != getInstance().getPaymentCurrency()
                && null != getInstance().getExchangeRate() && null != getInstance().getPaymentAmount()) {
            BigDecimal sourceAmount;
            if (getInstance().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                sourceAmount = BigDecimalUtil.multiply(getInstance().getPaymentAmount(), getInstance().getExchangeRate());
            } else {
                sourceAmount = BigDecimalUtil.divide(getInstance().getPaymentAmount(), getInstance().getExchangeRate());
            }
            getInstance().setSourceAmount(sourceAmount);
        } else {
            getInstance().setSourceAmount(null);
        }
    }

    public boolean areCurrenciesEqual() {
        /* both fields are not empty */
        return null != getInstance().getSourceCurrency()
                && getInstance().getSourceCurrency().equals(getInstance().getPaymentCurrency());
    }

    public void updateExchangeRate() {
        if (getInstance().getSourceCurrency() != null &&
                getInstance().getSourceCurrency().equals(getInstance().getPaymentCurrency()) &&
                getInstance().getPaymentCurrency().equals(FinancesCurrencyType.P)
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

    public void paymentTypeChanged() {
        if (getRotatoryFund().getEmployee() != null) {
            getInstance().setBeneficiaryName(getRotatoryFund().getEmployee().getSingleFullName());
        }
        getInstance().setBeneficiaryType(BeneficiaryType.PERSON);
        if (isPaymentBankAccount() || isPaymentWithCheck()) {
            getInstance().setCashBoxCashAccount(null);
            getInstance().setCashAccountAdjustment(null);
        }
        if (isPaymentCashBox() || isPaymentCashAccountAdjustment()) {
            getInstance().setBankAccount(null);
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

        updateExchangeRate();
        updateAmounts();
    }

    public void clearCashAccountAdjustment() {
        getInstance().setCashAccountAdjustment(null);
    }

    public boolean isEnableExchangeRateField() {
        /* both fields are not empty */
        FinancesCurrencyType paymentAccountCurrency = null;
        if (null != getInstance().getPaymentCurrency()) {
            if (null != getInstance().getBankAccount()) {
                paymentAccountCurrency = getInstance().getBankAccount().getCurrency();
            }
            if (null != getInstance().getCashBoxCashAccount()) {
                paymentAccountCurrency = getInstance().getCashBoxCashAccount().getCurrency();
            }
            if (null != getInstance().getCashAccountAdjustment()) {
                paymentAccountCurrency = getInstance().getCashAccountAdjustment().getCurrency();
            }
            if (!FinancesCurrencyType.P.equals(getInstance().getPaymentCurrency())
                    || (null != paymentAccountCurrency && !FinancesCurrencyType.P.equals(paymentAccountCurrency))) {
                return true;
            }
        }
        return false;
    }

    public boolean isRotatoryFundPaymentPending() {
        return !isManaged() || (null != getInstance().getState() && RotatoryFundPaymentState.PEN.equals(getInstance().getState()));
    }

    public boolean isRotatoryFundPaymentAnnulled() {
        return null != getInstance().getState() && RotatoryFundPaymentState.ANL.equals(getInstance().getState());
    }

    public boolean isEnableRotatoryFundPaymentType() {
        return !isManaged();
    }

    public boolean isEnableBankAccount() {
        return isRotatoryFundPaymentPending()
                && (isBankPayment()
                || isCheckPayment());
    }

    public boolean isCheckPayment() {
        return getInstance().getRotatoryFundPaymentType() != null &&
                getInstance().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_WITH_CHECK);
    }

    public boolean isBankPayment() {
        return getInstance().getRotatoryFundPaymentType() != null &&
                getInstance().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT);
    }

    public boolean isEnablePayAmount() {
        return isRotatoryFundPaymentPending();
    }

    public boolean isEnablePaymentDate() {
        return isRotatoryFundPaymentPending();
    }

    public boolean isEnableSourceCurrency() {
        return isRotatoryFundPaymentPending() && getInstance().getRotatoryFundPaymentType() != null
                && !isBankPayment() && !isCheckPayment();
    }

    public boolean isEnableExchangeRate() {
        return isRotatoryFundPaymentPending();
    }

    public boolean isEnableComputeButton() {
        return isEnableExchangeRateField();
    }

    public boolean isEnableCheckFields() {
        return null != getInstance().getRotatoryFundPaymentType()
                && (RotatoryFundPaymentType.PAYMENT_WITH_CHECK.equals(getInstance().getRotatoryFundPaymentType())
                || RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT.equals(getInstance().getRotatoryFundPaymentType()));
    }

    public boolean isEnableBeneficiaryTypeField() {
        return isEnableCheckFields()
                && getRotatoryFund().getDocumentType() != null && (getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.RECEIVABLE_FUND));
    }

    public boolean isRotatoryFundLiquidated() {
        return getInstance().getRotatoryFund() != null && getInstance().getRotatoryFund().getState() != null && (getInstance().getRotatoryFund().getState().equals(RotatoryFundState.LIQ));
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFundAction.getInstance();
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

    public boolean isPaymentWithCheck() {
        return null != getInstance().getRotatoryFundPaymentType() && getInstance().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_WITH_CHECK);
    }

    public boolean isPaymentBankAccount() {
        return null != getInstance().getRotatoryFundPaymentType() && getInstance().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT);
    }

    public boolean isPaymentCashBox() {
        return null != getInstance().getRotatoryFundPaymentType() && getInstance().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASHBOX);
    }

    public boolean isPaymentCashAccountAdjustment() {
        return null != getInstance().getRotatoryFundPaymentType() && getInstance().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASH_ACCOUNT_ADJ);
    }

    public Boolean checkIsEnabledToRemake(RotatoryFundPayment payment) {
        return null != payment && rotatoryFundPaymentRemakeService.isEnabledToRemake(payment);
    }

    public boolean isEnabledRemakeOptions() {
        return null != instanceToRemake;
    }

    public boolean isEnabledDocumentNumberOption() {
        return null != oldDocumentNumber;
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

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundPayment.message.created");
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundPayment.message.deleted");
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundPayment.message.updated");
    }

    private void addRotatoryFundAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundAlreadyAnnulled", getRotatoryFund().getCode());
    }

    private void addRotatoryFundPaymentAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundPaymentAlreadyAnnulled");
    }

    private void addRotatoryFundPaymentApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundPaymentAlreadyApproved");
    }

    private void addRotatoryFundPaymentAnnulledMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundPayment.message.rotatoryFundPaymentAnnulled");
    }

    private void addRotatoryFundPaymentApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundPayment.message.rotatoryFundPaymentApproved");
    }


    private void addRotatoryFundAlreadyCoveredError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundAlreadyCovered");
    }

    private void addRotatoryFundApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundAlreadyApproved", getRotatoryFund().getCode());
    }

    private void addRotatoryFundLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundAlreadyLiquidated", getRotatoryFund().getCode());
    }

    private void addPaymentSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.paymentSumExceedsRotatoryFundAmount", getRotatoryFund().getAmount());
    }

    private String rotatoryFundStateValidation() {
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

        return Outcome.SUCCESS;
    }

    private void initializeRemakeFields() {
        instanceToRemake = null;
        oldDocumentNumber = null;
    }

}