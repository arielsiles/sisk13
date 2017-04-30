package com.encens.khipus.action.finances;

import com.encens.khipus.action.AppIdentity;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.warehouse.BeneficiaryType;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.service.finances.*;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * RotatoryFund action class
 *
 * @author
 * @version 3.5.2.2
 */
@Name("rotatoryFundAction")
@Scope(ScopeType.CONVERSATION)
public class RotatoryFundAction extends GenericAction<RotatoryFund> {

    private static final String OTHER_RECEIVABLES = "OTHERRECEIVABLES";
    @Enumerated(EnumType.STRING)
    private PeriodType periodType = PeriodType.MONTH;

    private Integer interval = 1;

    @In
    private User currentUser;

    @In
    private RotatoryFundService rotatoryFundService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private QuotaService quotaService;

    @In
    private RotatoryFundPaymentService rotatoryFundPaymentService;

    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;

    @In
    private JobContractService jobContractService;

    /* This is used only at create time */
    private RotatoryFundPayment rotatoryFundPayment;

    private boolean includePayment;

    /* Var to control whereas the payment amount will be redistributed or not*/
    private boolean redistributePayment;

    /* to verify the currency type of a contract*/
    private Long currencyIdBs = Constants.currencyIdBs;

    /*to filter all employees except DTH category*/
    private PayrollGenerationType excludedPayrollGenerationType = PayrollGenerationType.GENERATION_BY_TIME;

    @In(value = "org.jboss.seam.security.identity")
    private AppIdentity appIdentity;

    /* this list is used at create time of this form to manage the quota creation*/
    @In(create = true)
    private RotatoryFundCreateQuotaListAction rotatoryFundCreateQuotaListAction;
    /*this var is used only at create time to be able to hold info about the old and new start date in events ajax*/
    private Date startDate;

    /**
     * determines if the functionality bust be shorten to a limited version of Other Receivables
     */
    private boolean limitedToOtherReceivables;

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(RotatoryFund instance) {
        try {
            setOp(OP_UPDATE);
            //Ensure the instance exists in the database, find it
            setInstance(rotatoryFundService.findById(instance.getId()));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Create
    public void init() {
        limitedToOtherReceivables = appIdentity.hasPermission(OTHER_RECEIVABLES, Constants.VIEW_PERMISSION);
        if (!isManaged()) {
            getInstance().setDate(new Date());
            getInstance().setStartDate(new Date());
            startDate = getInstance().getStartDate();
            getInstance().setState(RotatoryFundState.PEN);
            getInstance().setRegisterEmployee(currentUser);
            getInstance().setPaymentsNumber(1);
            includePayment = true;
            redistributePayment = false;
            rotatoryFundPayment = new RotatoryFundPayment();
            rotatoryFundPayment.setCreationDate(new Date());
            rotatoryFundPayment.setPaymentDate(new Date());
            rotatoryFundPayment.setState(RotatoryFundPaymentState.PEN);
            rotatoryFundPayment.setRotatoryFundPaymentType(RotatoryFundPaymentType.PAYMENT_WITH_CHECK);
            rotatoryFundPayment.setBeneficiaryType(BeneficiaryType.PERSON);
            rotatoryFundPayment.setRegisterEmployee(currentUser);
            if (limitedToOtherReceivables) {
                rotatoryFundDocumentTypeChanged();
                rotatoryFundPayment.setRotatoryFundPaymentType(RotatoryFundPaymentType.PAYMENT_CASH_ACCOUNT_ADJ);
                paymentTypeChanged();
            }
            try {
                getInstance().setExchangeRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
                getRotatoryFundPayment().setExchangeRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("finances currency not found");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("finances exchange rate not found");
            }
        }
    }

    @Factory(value = "rotatoryFund", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('ROTATORYFUND','VIEW') or s:hasPermission('OTHERRECEIVABLES','VIEW')}")
    public RotatoryFund initRotatoryFund() {
        return getInstance();
    }

    @Factory(value = "periodTypes", scope = ScopeType.STATELESS)
    public PeriodType[] initPeriodTypes() {
        return PeriodType.values();
    }

    @Factory(value = "rotatoryFundPaymentTypes", scope = ScopeType.STATELESS)
    public RotatoryFundPaymentType[] initRotatoryFundPaymentTypes() {
        if (limitedToOtherReceivables) {
            return new RotatoryFundPaymentType[]{RotatoryFundPaymentType.PAYMENT_CASH_ACCOUNT_ADJ};
        }
        return RotatoryFundPaymentType.values();
    }

    @Factory(value = "beneficiaryTypeList", scope = ScopeType.STATELESS)
    public BeneficiaryType[] initBeneficiaryTypes() {
        return BeneficiaryType.values();
    }

    @Factory(value = "rotatoryFundStateList", scope = ScopeType.STATELESS)
    public RotatoryFundState[] initRotatoryFundStates() {
        return RotatoryFundState.values();
    }

    @Override
    public String getDisplayNameProperty() {
        return "description";
    }

    @Factory(value = "basicFinancesCurrencies", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] initFinanceCurrencyTypes() {
        return new FinancesCurrencyType[]{FinancesCurrencyType.D, FinancesCurrencyType.P};
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String newRotatoryFund() {
        /* to create the new RotatoryFund instance*/
        setInstance(null);
        setOp(OP_CREATE);
        /*at create time */
        init();
        return Outcome.SUCCESS;
    }

    @Override
    @Restrict("#{s:hasPermission('ROTATORYFUND','CREATE')}")
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        updateExchangeRate();
        /* in case that discount by payroll is been taken into account */
        if (checkContractDate() == null) {
            addDateOutOfContractRange();
            return Outcome.REDISPLAY;
        }
        if (includePayment && getRotatoryFundPayment().getPaymentAmount().compareTo(getInstance().getAmount()) > 0) {
            addPaymentSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        }
        BigDecimal quotaSum = BigDecimal.ZERO;
        for (Quota quota : rotatoryFundCreateQuotaListAction.getQuotaList()) {
            quotaSum = BigDecimalUtil.sum(quotaSum, quota.getAmount());
        }
        int comparisonResult = quotaSum.compareTo(getInstance().getAmount());
        if (comparisonResult != 0) {
            if (comparisonResult > 0) {
                addQuotaSumExceedsRotatoryFundAmountError();
            } else {
                addQuotaSumIsLessThanRotatoryFundAmountMessage();
            }
            return Outcome.REDISPLAY;
        }

        /*ensures the calculus be completed in case they have not been completed*/
        updateSourceAmount();
        try {
            rotatoryFundService.create(getInstance(), rotatoryFundCreateQuotaListAction.getQuotaList(), includePayment ? rotatoryFundPayment : null);
            addCreatedMessage();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
        super.select(getInstance());
        return Outcome.SUCCESS;
    }

    @Override
    @Restrict("#{s:hasPermission('ROTATORYFUND','CREATE')}")
    public void createAndNew() {
        if (!validate()) {
            return;
        }
        updateExchangeRate();
        /* in case that discount by payroll is been taken into account */
        if (!testContractDate()) {
            addDateOutOfContractRange();
            return;
        }
        if (getRotatoryFundPayment().getPaymentAmount().compareTo(getInstance().getAmount()) > 0) {
            addPaymentSumExceedsRotatoryFundAmountError();
            return;
        }

        BigDecimal quotaSum = BigDecimal.ZERO;
        for (Quota quota : rotatoryFundCreateQuotaListAction.getQuotaList()) {
            quotaSum = BigDecimalUtil.sum(quotaSum, quota.getAmount());
        }
        int comparisonResult = quotaSum.compareTo(getInstance().getAmount());
        if (comparisonResult != 0) {
            if (comparisonResult > 0) {
                addQuotaSumExceedsRotatoryFundAmountError();
            } else {
                addQuotaSumIsLessThanRotatoryFundAmountMessage();
            }
            return;
        }

        updateSourceAmount();
        try {
            rotatoryFundService.create(getInstance(), rotatoryFundCreateQuotaListAction.getQuotaList(), includePayment ? rotatoryFundPayment : null);
            addCreatedMessage();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return;
        }
        /* to create the new RotatoryFund instance*/
        setInstance(null);
        setOp(OP_CREATE);
        /*at create time */
        init();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ROTATORYFUND','UPDATE')}")
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        Long currentVersion = (Long) getVersion(getInstance());
        updateExchangeRate();
        /* in case that discount by payroll is been taken into account */
        if (checkContractDate() == null) {
            addDateOutOfContractRange();
            return Outcome.REDISPLAY;
        }

        /* check if there is pendant payments or collections in case of a special edit of global amount */
        RotatoryFund databaseRotatoryFund = rotatoryFundService.findInDataBase(getInstance().getId());
        if (databaseRotatoryFund.getState().equals(RotatoryFundState.APR) && getInstance().getState().equals(RotatoryFundState.APR)) {
            List<RotatoryFundPayment> eventRotatoryFundPaymentList = rotatoryFundPaymentService.getEventRotatoryFundPaymentListByState(getInstance(), RotatoryFundPaymentState.PEN);
            List<RotatoryFundCollection> eventRotatoryFundCollectionList = rotatoryFundCollectionService.getEventRotatoryFundCollectionListByState(getInstance(), RotatoryFundCollectionState.PEN);
            if (null != eventRotatoryFundPaymentList && eventRotatoryFundPaymentList.size() > 0) {
                addRotatoryFundContainsPendantRotatoryFundPaymentsError();
                return Outcome.REDISPLAY;
            }
            if (null != eventRotatoryFundCollectionList && eventRotatoryFundCollectionList.size() > 0) {
                addRotatoryFundContainsPendantRotatoryFundCollectionsError();
                return Outcome.REDISPLAY;
            }
        }

        try {
            rotatoryFundService.updateRotatoryFund(getInstance());
            addUpdatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            setVersion(getInstance(), currentVersion);
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException e) {
            addApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsError();
            return Outcome.REDISPLAY;
        }
    }

    @Restrict("#{s:hasPermission('APPROVEROTATORYFUND','VIEW') and s:hasPermission('ROTATORYFUNDPAYMENTAPPROVE','VIEW')}")
    @End
    public String approveRotatoryFundAndPayments() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        Long currentVersion = (Long) getVersion(getInstance());
        updateExchangeRate();
        /* in case that discount by payroll is been taken into account */
        if (checkContractDate() == null) {
            addDateOutOfContractRange();
            return Outcome.REDISPLAY;
        }
        try {
            rotatoryFundService.approveRotatoryFundAndPayments(getInstance());
            addRotatoryFundApprovedMessage();
            return Outcome.SUCCESS;
        } catch (RotatoryFundApprovedException e) {
            addRotatoryFundApprovedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (QuotaEmptyException e) {
            addRotatoryFundQuotaEmptyMessage();
            return Outcome.REDISPLAY;
        } catch (CurrencyDoNotMatchException e) {
            addCurrencyDoNotMatchMessage();
            return Outcome.REDISPLAY;
        } catch (QuotaSumIsLessThanRotatoryFundAmountException e) {
            addQuotaSumIsLessThanRotatoryFundAmountMessage();
            return Outcome.REDISPLAY;
        } catch (SpendDistributionEmptyException e) {
            addRotatoryFundSpendDistributionEmptyMessage();
            return Outcome.REDISPLAY;
        } catch (SpendDistributionSumIsNotOneHundredException e) {
            addSpendDistributionSumIsNotOneHundredMessage();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            setInstance(rotatoryFundService.findRotatoryFund(getInstance().getId()));
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            setVersion(getInstance(), currentVersion);
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (PendantPaymentsSumExceedsRotatoryFundAmountException e) {
            addPendantPaymentsSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentAnnulledException e) {
            addRotatoryFundPaymentAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentApprovedException e) {
            addRotatoryFundPaymentApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (PaymentSumExceedsRotatoryFundAmountException e) {
            addPaymentSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (QuotaSumExceedsRotatoryFundAmountException e) {
            addQuotaSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentSumExceedsRotatoryFundAmountException e) {
            addRotatoryFundPaymentSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentSumIsLessThanRotatoryFundAmountException e) {
            addRotatoryFundPaymentSumIsLessThanRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundPaymentCurrencyDoNotMatchException e) {
            addRotatoryFundPaymentCurrencyDoNotMatchError();
            return Outcome.REDISPLAY;
        } catch (ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException e) {
            addApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsError();
            return Outcome.REDISPLAY;
        }
    }

    public boolean isArePendantPaymentsOrCollections() {
        /* check if there is pendant payments or collections*/
        RotatoryFund databaseRotatoryFund = rotatoryFundService.findInDataBase(getInstance().getId());
        if (databaseRotatoryFund.getState().equals(RotatoryFundState.APR) && getInstance().getState().equals(RotatoryFundState.APR)) {
            List<RotatoryFundPayment> eventRotatoryFundPaymentList = rotatoryFundPaymentService.getEventRotatoryFundPaymentListByState(getInstance(), RotatoryFundPaymentState.PEN);
            List<RotatoryFundCollection> eventRotatoryFundCollectionList = rotatoryFundCollectionService.getEventRotatoryFundCollectionListByState(getInstance(), RotatoryFundCollectionState.PEN);
            if ((null != eventRotatoryFundPaymentList && eventRotatoryFundPaymentList.size() > 0)
                    || (null != eventRotatoryFundCollectionList && eventRotatoryFundCollectionList.size() > 0)) {
                return true;
            }
        }
        return false;
    }


    public String checkContractDate() {
        if (getInstance().getDiscountByPayroll() != null &&
                getInstance().getDiscountByPayroll() && !(getInstance().getJobContract().getContract().getInitDate().compareTo(getInstance().getStartDate()) <= 0
                && ((getInstance().getJobContract().getContract().getEndDate() == null)
                || (getInstance().getJobContract().getContract().getEndDate() != null &&
                getInstance().getJobContract().getContract().getEndDate().compareTo(getInstance().getStartDate()) >= 0)))) {
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    public boolean testContractDate() {
        return !(getInstance().getDiscountByPayroll() != null &&
                getInstance().getDiscountByPayroll() && !(getInstance().getJobContract().getContract().getInitDate().compareTo(getInstance().getStartDate()) <= 0
                && ((getInstance().getJobContract().getContract().getEndDate() == null)
                || (getInstance().getJobContract().getContract().getEndDate() != null &&
                getInstance().getJobContract().getContract().getEndDate().compareTo(getInstance().getStartDate()) >= 0))));
    }

    @End
    public String annulRotatoryFund() {
        try {
            rotatoryFundService.annulRotatoryFund(getInstance());
            addRotatoryFundAnnulledMessage();
            return Outcome.SUCCESS;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundApprovedException e) {
            addRotatoryFundApprovedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException e) {
            addApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsError();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public void updateCurrentInstance() {
        setInstance(rotatoryFundService.findRotatoryFund(getInstance().getId()));
    }

    public void compute() {
        getInstance().setReceivableResidue(getInstance().getAmount());
        if (areCurrenciesEqual()) {
            getRotatoryFundPayment().setSourceAmount(getInstance().getAmount());
            getRotatoryFundPayment().setPaymentCurrency(getInstance().getPayCurrency());
            getRotatoryFundPayment().setPaymentAmount(getInstance().getAmount());
        } else if (null != getRotatoryFundPayment().getSourceCurrency() && null != getInstance().getPayCurrency() && null != getRotatoryFundPayment().getExchangeRate()) {
            BigDecimal sourceAmount;
            BigDecimal paymentAmount;
            if (getRotatoryFundPayment().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                sourceAmount = BigDecimalUtil.toBigDecimal(getInstance().getAmount().doubleValue() * getRotatoryFundPayment().getExchangeRate().doubleValue());
                paymentAmount = BigDecimalUtil.multiply(sourceAmount, getRotatoryFundPayment().getExchangeRate());
            } else {
                sourceAmount = BigDecimalUtil.toBigDecimal(getInstance().getAmount().doubleValue() / getRotatoryFundPayment().getExchangeRate().doubleValue());
                paymentAmount = BigDecimalUtil.divide(sourceAmount, getRotatoryFundPayment().getExchangeRate());
            }
            getRotatoryFundPayment().setSourceAmount(sourceAmount);
            getRotatoryFundPayment().setPaymentAmount(paymentAmount);
        }
    }

    public void assignEmployee(Employee employee) {
        getInstance().setEmployee(employee);
    }

    public void updateExchangeRate() {
        /* apply only over the rotatoryFund */
        if (getInstance().getPayCurrency() != null
                && getInstance().getPayCurrency().equals(FinancesCurrencyType.P)) {
            getInstance().setExchangeRate(BigDecimal.ONE);
        }

        /* apply over both the rotatoryFund and thew rotatoryFundPayment */
        if (getRotatoryFundPayment().getSourceCurrency() != null &&
                getRotatoryFundPayment().getSourceCurrency().equals(getInstance().getPayCurrency()) &&
                getInstance().getPayCurrency().equals(FinancesCurrencyType.P)
                ) {
            getInstance().setExchangeRate(BigDecimal.ONE);
            getRotatoryFundPayment().setExchangeRate(BigDecimal.ONE);
        }
    }

    public void amountChanged() {
        resetQuotaList();
        updateAmounts();
    }

    public void resetQuotaList() {
        /*only at create process*/
        if (!isManaged() && isGenerateQuotaFieldsNotEmpty()) {
            rotatoryFundCreateQuotaListAction.reset();
        }
    }
    /* updates all the data taking into account rotatory fund amount */

    public void updateAmounts() {
        BigDecimal payableResidue = isManaged() ? rotatoryFundService.getPayableResidueByRotatoryFund(getInstance()) : getInstance().getAmount();
        getInstance().setPayableResidue(payableResidue);
        BigDecimal receivableResidue = isManaged() ? rotatoryFundService.getReceivableResidueByRotatoryFund(getInstance()) : BigDecimal.ZERO;
        getInstance().setReceivableResidue(receivableResidue);
        if (!isManaged()) {
            getRotatoryFundPayment().setPaymentCurrency(getInstance().getPayCurrency());
            if (areCurrenciesEqual()) {
                getRotatoryFundPayment().setPaymentAmount(getInstance().getAmount());
                getRotatoryFundPayment().setSourceAmount(getInstance().getAmount());
            } else if (null != getRotatoryFundPayment().getSourceCurrency()
                    && null != getInstance().getPayCurrency()
                    && null != getRotatoryFundPayment().getExchangeRate()
                    && null != getInstance().getAmount()) {
                BigDecimal sourceAmount;
                if (getRotatoryFundPayment().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                    sourceAmount = BigDecimalUtil.multiply(getInstance().getAmount(), getRotatoryFundPayment().getExchangeRate());
                } else {
                    sourceAmount = BigDecimalUtil.divide(getInstance().getAmount(), getRotatoryFundPayment().getExchangeRate());
                }
                getRotatoryFundPayment().setSourceAmount(sourceAmount);
                getRotatoryFundPayment().setPaymentAmount(getInstance().getAmount());
            } else {
                getRotatoryFundPayment().setSourceAmount(null);
                getRotatoryFundPayment().setPaymentAmount(null);
            }
        }
    }

    /* update source amount based on exchange rate and payment amount*/

    public void updateSourceAmount() {
        BigDecimal payableResidue = isManaged() ? rotatoryFundService.getPayableResidueByRotatoryFund(getInstance()) : getInstance().getAmount();
        getInstance().setPayableResidue(payableResidue);
        BigDecimal receivableResidue = isManaged() ? rotatoryFundService.getReceivableResidueByRotatoryFund(getInstance()) : BigDecimal.ZERO;
        getInstance().setReceivableResidue(receivableResidue);

        if (areCurrenciesEqual()) {
            getRotatoryFundPayment().setPaymentCurrency(getInstance().getPayCurrency());
            getRotatoryFundPayment().setSourceAmount(getRotatoryFundPayment().getPaymentAmount());
        } else if (null != getRotatoryFundPayment().getSourceCurrency() && null != getInstance().getPayCurrency()
                && null != getRotatoryFundPayment().getExchangeRate() && null != getRotatoryFundPayment().getPaymentAmount()) {
            BigDecimal sourceAmount;
            if (getRotatoryFundPayment().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                sourceAmount = BigDecimalUtil.multiply(getRotatoryFundPayment().getPaymentAmount(), getRotatoryFundPayment().getExchangeRate());
            } else {
                sourceAmount = BigDecimalUtil.divide(getRotatoryFundPayment().getPaymentAmount(), getRotatoryFundPayment().getExchangeRate());
            }
            getRotatoryFundPayment().setSourceAmount(sourceAmount);
        } else {
            getRotatoryFundPayment().setSourceAmount(null);
        }
    }

    /* Updates the payment description making a copy of what is been typed
    * this method must be called only at RotatoryFund creation */

    public void descriptionChanged() {
        getRotatoryFundPayment().setDescription(getInstance().getDescription());
    }

    public void paymentTypeChanged() {
        if (getInstance().getEmployee() != null) {
            getRotatoryFundPayment().setBeneficiaryName(getInstance().getEmployee().getSingleFullName());
        }
        getRotatoryFundPayment().setBeneficiaryType(BeneficiaryType.PERSON);
        if (isPaymentBankAccount() || isPaymentWithCheck()) {
            getRotatoryFundPayment().setCashBoxCashAccount(null);
            getRotatoryFundPayment().setCashAccountAdjustment(null);
            if (isPaymentBankAccount()) {
                getRotatoryFundPayment().setCheckDestination(null);
            }
        }
        if (isPaymentCashBox() || isPaymentCashAccountAdjustment()) {
            getRotatoryFundPayment().setBankAccount(null);
            getRotatoryFundPayment().setCheckDestination(null);
        }
    }

    public void rotatoryFundDocumentTypeChanged() {
        if (getInstance().getDocumentType() != null) {
            if (getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.PARTNER_WITHDRAWAL) ||
                    getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.RECEIVABLE_FUND)) {
                clearDiscountByPayrollField();
            }
            if (getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.ADVANCE) ||
                    getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.LOAN) ||
                    getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.OTHER_RECEIVABLES)) {
                getInstance().setDiscountByPayroll(true);
            }
            /*reset default values for partner withdrawal type (one quota)*/
            if (getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.PARTNER_WITHDRAWAL)) {
                getInstance().setPaymentsNumber(1);
                rotatoryFundCreateQuotaListAction.reset();
            }
            getInstance().setJobContract(null);
        }

        getInstance().setCashAccount(null);
        updateCashAccountValue();
        getInstance().setBusinessUnit(null);
        getInstance().setCostCenter(null);
    }

    public void updateCashAccountValue() {
        if (getInstance().getDocumentType() != null && RotatoryFundDocumentTypeFieldRestriction.CASH_ACCOUNT_DEFINED_BY_DEFAULT.equals(getInstance().getDocumentType().getFieldRestriction())) {
            if (FinancesCurrencyType.P.equals(getInstance().getPayCurrency())) {
                getInstance().setCashAccount(getInstance().getDocumentType().getNationalCashAccount());
            } else if (FinancesCurrencyType.D.equals(getInstance().getPayCurrency())) {
                getInstance().setCashAccount(getInstance().getDocumentType().getForeignCashAccount());
            }
        }
        if (isOtherReceivables() && RotatoryFundDocumentTypeFieldRestriction.CASH_ACCOUNT_DEFINED_BY_DEFAULT.equals(getInstance().getDocumentType().getFieldRestriction())) {
            if (FinancesCurrencyType.P.equals(getInstance().getPayCurrency())) {
                getRotatoryFundPayment().setCashAccountAdjustment(getInstance().getDocumentType().getAdjustmentNationalCashAccount());
            } else if (FinancesCurrencyType.D.equals(getInstance().getPayCurrency())) {
                getRotatoryFundPayment().setCashAccountAdjustment(getInstance().getDocumentType().getAdjustmentForeignCashAccount());
            }
        }

    }

    public Boolean isCashAccountDefinedByDefault() {
        return getInstance().getDocumentType() != null && RotatoryFundDocumentTypeFieldRestriction.CASH_ACCOUNT_DEFINED_BY_DEFAULT.equals(getInstance().getDocumentType().getFieldRestriction());
    }

    public void clearDiscountByPayrollField() {
        getInstance().setDiscountByPayroll(null);
    }

    public boolean isEnableExchangeRateField() {
        return (null != getInstance().getPayCurrency()) &&
                (!FinancesCurrencyType.P.equals(getInstance().getPayCurrency()));
    }

    public boolean isEnablePaymentExchangeRateField() {
        /* both fields are not empty */
        FinancesCurrencyType paymentAccountCurrency = null;
        if (null != getInstance().getPayCurrency()) {
            if (null != getRotatoryFundPayment().getBankAccount()) {
                paymentAccountCurrency = getRotatoryFundPayment().getBankAccount().getCurrency();
            }
            if (null != getRotatoryFundPayment().getCashBoxCashAccount()) {
                paymentAccountCurrency = getRotatoryFundPayment().getCashBoxCashAccount().getCurrency();
            }
            if (null != getRotatoryFundPayment().getCashAccountAdjustment()) {
                paymentAccountCurrency = getRotatoryFundPayment().getCashAccountAdjustment().getCurrency();
            }
            if (!FinancesCurrencyType.P.equals(getInstance().getPayCurrency())
                    || (null != paymentAccountCurrency && !FinancesCurrencyType.P.equals(paymentAccountCurrency))) {
                return true;
            }
        }
        return false;
    }

    public boolean areCurrenciesEqual() {
        /* both fields are not empty */
        return null != getRotatoryFundPayment().getSourceCurrency() && null != getInstance().getPayCurrency() && getRotatoryFundPayment().getSourceCurrency() == getInstance().getPayCurrency();
    }

    public boolean isEnableCheckFields() {
        return null != getRotatoryFundPayment().getRotatoryFundPaymentType()
                && (RotatoryFundPaymentType.PAYMENT_WITH_CHECK.equals(getRotatoryFundPayment().getRotatoryFundPaymentType())
                || RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT.equals(getRotatoryFundPayment().getRotatoryFundPaymentType()));
    }

    public boolean isEnableBeneficiaryTypeField() {
        return isEnableCheckFields()
                && getInstance().getDocumentType() != null && (getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.RECEIVABLE_FUND));
    }

    public boolean isEnableBankAccountInfo() {
        return null != getRotatoryFundPayment().getRotatoryFundPaymentType()
                && (RotatoryFundPaymentType.PAYMENT_WITH_CHECK.equals(getRotatoryFundPayment().getRotatoryFundPaymentType())
                || RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT.equals(getRotatoryFundPayment().getRotatoryFundPaymentType()));
    }

    public boolean isEnableComputeButton() {
        return (useEnableComputeButton() && !areCurrenciesEqual());
    }

    private boolean useEnableComputeButton() {
        return isEnableExchangeRateField();
    }

    public boolean isEnableDiscountByPayrollField() {
        return (getInstance().getDocumentType() != null &&
                (getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.ADVANCE)
                        || getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.LOAN)
                        || getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.OTHER_RECEIVABLES)));
    }

    public boolean isEnableApprovedByEmployeeField() {
        return isManaged()
                && getInstance().getState().equals(RotatoryFundState.APR);
    }

    public void assignJobContract(JobContract jobContract) {
        getInstance().setJobContract(jobContract);
        loadJobContractValues();
    }

    public void loadJobContractValues() {
        if (getInstance().getJobContract() != null) {
            getInstance().setJobContract(jobContractService.load(getInstance().getJobContract()));
            getInstance().setEmployee(getInstance().getJobContract().getContract().getEmployee());
            if (!isManaged() && isEnableCheckFields()) {
                getRotatoryFundPayment().setBeneficiaryName(getInstance().getEmployee().getSingleFullName());
                getRotatoryFundPayment().setCheckDestination(getInstance().getJobContract().getJob().getOrganizationalUnit().getBusinessUnit());
            }
            getInstance().setBusinessUnit(getInstance().getJobContract().getJob().getOrganizationalUnit().getBusinessUnit());
            getInstance().setCostCenter(getInstance().getJobContract().getJob().getOrganizationalUnit().getCostCenter());

            /*set and refresh values of jobContract pay currency*/
            getInstance().setPayCurrency(getInstance().getJobContract().getJob().getSalary().getCurrency().getCurrencyCode().equalsIgnoreCase(currencyIdBs.toString()) ? FinancesCurrencyType.P : FinancesCurrencyType.D);
            payCurrencyFieldChanged();

            showDebtsOfEmployee(getInstance().getJobContract());
        }
    }

    public void showDebtsOfEmployee(JobContract jobContractRetrieved) {
        /*List of debts to show when the employee contract is selected*/
        for (RotatoryFundType rotatoryFundType : RotatoryFundType.values()) {
            BigDecimal nationalSum = rotatoryFundService.sumRotatoryFundByEmployeeByTypeByCurrencyByState(jobContractRetrieved.getContract().getEmployee(), rotatoryFundType, FinancesCurrencyType.P, RotatoryFundState.APR);
            BigDecimal foreignSum = rotatoryFundService.sumRotatoryFundByEmployeeByTypeByCurrencyByState(jobContractRetrieved.getContract().getEmployee(), rotatoryFundType, FinancesCurrencyType.D, RotatoryFundState.APR);

            if (nationalSum != null && nationalSum.compareTo(BigDecimal.ZERO) > 0) {
                List<RotatoryFund> nationalList = rotatoryFundService.findRotatoryFundsByEmployeeByTypeByCurrencyByState(jobContractRetrieved.getContract().getEmployee(), rotatoryFundType, FinancesCurrencyType.P, RotatoryFundState.APR);
                String detail = "    (";
                for (RotatoryFund rotatoryFund : nationalList) {
                    detail = detail + "código: " + rotatoryFund.getCode() + " residuo: " + rotatoryFund.getReceivableResidue() + " " + messages.get(rotatoryFund.getPayCurrency().getSymbolResourceKey()) + ", ";
                }
                detail = detail.substring(0, detail.length() - 2);
                detail += ")";
                addNationalSumMessage(rotatoryFundType, nationalSum, detail);
            }
            if (foreignSum != null && foreignSum.compareTo(BigDecimal.ZERO) > 0) {
                List<RotatoryFund> foreignList = rotatoryFundService.findRotatoryFundsByEmployeeByTypeByCurrencyByState(jobContractRetrieved.getContract().getEmployee(), rotatoryFundType, FinancesCurrencyType.D, RotatoryFundState.APR);
                String detail = "    (";
                for (RotatoryFund rotatoryFund : foreignList) {
                    detail = detail + "código: " + rotatoryFund.getCode() + " residuo: " + rotatoryFund.getReceivableResidue() + " " + messages.get(rotatoryFund.getPayCurrency().getSymbolResourceKey()) + ", ";
                }
                detail = detail.substring(0, detail.length() - 2);
                detail += ")";
                addForeignSumMessage(rotatoryFundType, foreignSum, detail);
            }
        }
    }

    public void clearJobContract() {
        getInstance().setJobContract(null);
        getInstance().setEmployee(null);
        getInstance().setBusinessUnit(null);
        getInstance().setCostCenter(null);
        getRotatoryFundPayment().setCheckDestination(null);
    }

    public void discountByPayrollChanged() {
        clearEmployeeAndContract();
    }

    public void clearEmployeeAndContract() {
        getInstance().setJobContract(null);
        getInstance().setEmployee(null);
        getInstance().setBusinessUnit(null);
        getInstance().setCostCenter(null);
    }

    public void clearEmployee() {
        getInstance().setEmployee(null);
    }

    public void assignRegisterEmployee(User user) {
        getInstance().setRegisterEmployee(user);
    }

    public void clearRegisterEmployee() {
        getInstance().setRegisterEmployee(null);
    }

    public void assignApprovedByEmployee(User user) {
        getInstance().setApprovedByEmployee(user);
    }

    public void clearApprovedByEmployee() {
        getInstance().setApprovedByEmployee(null);
    }

    public void bankAccountFieldChanged() {
        updateAmounts();
    }

    public void payCurrencyFieldChanged() {
        updateAmounts();
        updateCashAccountValue();
    }

    public boolean isGenerateQuotaFieldsNotEmpty() {
        return null != getInstance().getAmount() &&
                null != getInstance().getStartDate() &&
                null != getInstance().getPaymentsNumber() &&
                null != getInterval() &&
                null != getPeriodType();
    }

    public boolean isEnableContractInfo() {
        return getInstance().getJobContract() != null;
    }

    public boolean isEnableUpdateInfo() {
        return isRotatoryFundPending() && isEnableContractInfo() && (isReceivableFund() || isPartnerWithdrawal());
    }

    public boolean isEnableSpendDistributionTab() {
        return (isReceivableFund() || isPartnerWithdrawal());
    }

    public boolean isReceivableFund() {
        return getInstance().getDocumentType() != null && getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.RECEIVABLE_FUND);
    }

    public boolean isLoan() {
        return getInstance().getDocumentType() != null && getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.LOAN);
    }

    public boolean isOtherReceivables() {
        return getInstance().getDocumentType() != null && getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.OTHER_RECEIVABLES);
    }

    public boolean isAdvance() {
        return getInstance().getDocumentType() != null && getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.ADVANCE);
    }

    public boolean isPartnerWithdrawal() {
        return getInstance().getDocumentType() != null && getInstance().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.PARTNER_WITHDRAWAL);
    }

    public boolean isEnableExpirationDate() {
        List<Quota> quotaList = quotaService.getQuotaList(getInstance());
        return quotaList != null && (!quotaList.isEmpty());
    }

    public boolean isRotatoryFundApproved() {
        return isManaged() && null != getInstance().getState() && RotatoryFundState.APR.equals(getInstance().getState());
    }

    public boolean isRotatoryFundPending() {
        return !isManaged() || (null != getInstance().getState() && RotatoryFundState.PEN.equals(getInstance().getState()));
    }

    public boolean isRotatoryFundLiquidated() {
        return isManaged() && null != getInstance().getState() && RotatoryFundState.LIQ.equals(getInstance().getState());
    }

    public boolean isRotatoryFundNullified() {
        return isManaged() && null != getInstance().getState() && RotatoryFundState.ANL.equals(getInstance().getState());
    }

    protected void addDateOutOfContractRange() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.dateOutOfContractRange");
    }

    public void addRotatoryFundApprovedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.rotatoryFundAlreadyApproved");
    }

    public void addRotatoryFundLiquidatedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.rotatoryFundAlreadyLiquidated");
    }

    private void addRotatoryFundQuotaEmptyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.rotatoryFundQuotaEmpty");
    }

    private void addRotatoryFundSpendDistributionEmptyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.rotatoryFundSpendDistributionEmpty");
    }

    private void addCurrencyDoNotMatchMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.currencyDoNotMatch");
    }

    private void addQuotaSumIsLessThanRotatoryFundAmountMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.quotaSumIsLessThanRotatoryFundAmount", getInstance().getAmount());
    }

    private void addSpendDistributionSumIsNotOneHundredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.spendDistributionSumIsNotOneHundred");
    }

    private void addRotatoryFundApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFund.info.approveMessage", getInstance().getCode());
    }

    public void addRotatoryFundAnnulledMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFund.info.rotatoryFundAnnulled", getInstance().getCode());
    }

    protected void addRotatoryFundAnnulledErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFund.error.rotatoryFundAlreadyAnnulled");
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Long getCurrencyIdBs() {
        return currencyIdBs;
    }

    public void setCurrencyIdBs(Long currencyIdBs) {
        this.currencyIdBs = currencyIdBs;
    }

    public void assignCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setCashAccount(cashAccount);
    }

    public void assignCashBoxCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getRotatoryFundPayment().setCashBoxCashAccount(cashAccount);
        updateAmounts();
    }

    public void assignCashAccountAdjustment(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getRotatoryFundPayment().setCashAccountAdjustment(cashAccount);
        updateAmounts();
    }

    public void clearCashAccount() {
        getInstance().setCashAccount(null);
    }

    public void clearCashBoxCashAccount() {
        getRotatoryFundPayment().setCashBoxCashAccount(null);
    }

    public void clearCashAccountAdjustment() {
        getRotatoryFundPayment().setCashAccountAdjustment(null);
    }

    public void assignProvider(Provider provider) {
        getInstance().setProvider(provider);
    }

    public void clearProvider() {
        getInstance().setProvider(null);
    }

    public RotatoryFundPayment getRotatoryFundPayment() {
        return rotatoryFundPayment;
    }

    public void setRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment) {
        this.rotatoryFundPayment = rotatoryFundPayment;
    }

    public boolean isIncludePayment() {
        return includePayment;
    }

    public void setIncludePayment(boolean includePayment) {
        this.includePayment = includePayment;
    }

    public void changeIncludePayment(ValueChangeEvent event) {
        boolean flag = ((Boolean) event.getNewValue());
        setIncludePayment(flag);
    }

    public void payCollapse() {
        includePayment = false;
    }

    public void payExpand() {
        includePayment = true;
    }

    public boolean isRedistributePayment() {
        return redistributePayment;
    }

    public void setRedistributePayment(boolean redistributePayment) {
        this.redistributePayment = redistributePayment;
    }

    public void changeRedistributePayment(ValueChangeEvent event) {
        boolean flag = ((Boolean) event.getNewValue());
        setRedistributePayment(flag);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /*used in view level*/

    public String getSourceAmountLabel() {
        if (null != getRotatoryFundPayment().getRotatoryFundPaymentType()) {
            if (null != getRotatoryFundPayment().getBankAccount()
                    && getRotatoryFundPayment().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_WITH_CHECK)) {
                return messages.get("RotatoryFundPayment.sourceCheckAmount");
            } else if (null != getRotatoryFundPayment().getBankAccount()
                    && getRotatoryFundPayment().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT)) {
                return messages.get("RotatoryFundPayment.sourceBankAmount");
            } else {
                return messages.get("RotatoryFundPayment.sourceCashBoxAmount");
            }
        }
        return null;
    }

    public boolean isPaymentWithCheck() {
        return null != getRotatoryFundPayment().getRotatoryFundPaymentType() && getRotatoryFundPayment().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_WITH_CHECK);
    }

    public boolean isPaymentBankAccount() {
        return null != getRotatoryFundPayment().getRotatoryFundPaymentType() && getRotatoryFundPayment().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT);
    }

    public boolean isPaymentCashBox() {
        return null != getRotatoryFundPayment().getRotatoryFundPaymentType() && getRotatoryFundPayment().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASHBOX);
    }

    public boolean isPaymentCashAccountAdjustment() {
        return null != getRotatoryFundPayment().getRotatoryFundPaymentType() && getRotatoryFundPayment().getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASH_ACCOUNT_ADJ);
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        getInstance().setCostCenter(null);
    }

    public Boolean validate() {
        Boolean valid = true;

        if (getInstance().getCashAccount() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "Common.required", messages.get("RotatoryFund.cashAccount"));
            valid = false;
        }

        if (getInstance().getCashAccount() != null && !getInstance().getCashAccount().getCurrency().equals(getInstance().getPayCurrency())) {
            addCashAccountDistinctPayCurrencyError();
            valid = false;
        }
        return valid;
    }

    public boolean isLimitedToOtherReceivables() {
        return limitedToOtherReceivables;
    }

    public void setLimitedToOtherReceivables(boolean limitedToOtherReceivables) {
        this.limitedToOtherReceivables = limitedToOtherReceivables;
    }

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFund.message.created", getInstance().getCode());
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFund.message.deleted", getInstance().getCode());
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFund.message.updated", getInstance().getCode());
    }

    @Override
    public void addNotFoundMessage() {
        super.addNotFoundMessage();
    }

    public PayrollGenerationType getExcludedPayrollGenerationType() {
        return excludedPayrollGenerationType;
    }

    public void setExcludedPayrollGenerationType(PayrollGenerationType excludedPayrollGenerationType) {
        this.excludedPayrollGenerationType = excludedPayrollGenerationType;
    }

    protected void addNationalSumMessage(RotatoryFundType rotatoryFundType, BigDecimal sum, String detailList) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFund.info.nationalSum", getInstance().getJobContract().getContract().getEmployee().getFullName(), messages.get(rotatoryFundType.getResourceKey()), sum.toString(), detailList);
    }

    protected void addForeignSumMessage(RotatoryFundType rotatoryFundType, BigDecimal sum, String detailList) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFund.info.foreignSum", getInstance().getJobContract().getContract().getEmployee().getFullName(), messages.get(rotatoryFundType.getResourceKey()), sum.toString(), detailList);
    }

    private void addPaymentSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.paymentSumExceedsRotatoryFundAmount", getInstance().getAmount());
    }

    private void addPendantPaymentsSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.pendantPaymentsSumExceedsRotatoryFundAmount", getInstance().getAmount());
    }

    private void addRotatoryFundPaymentAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundPaymentAlreadyAnnulled");
    }

    private void addRotatoryFundPaymentApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundPaymentAlreadyApproved");
    }

    private void addQuotaSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Quota.error.quotaSumExceedsRotatoryFundAmount", getInstance().getAmount());
    }

    private void addRotatoryFundPaymentSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundPaymentSumExceedsRotatoryFundAmount", getInstance().getAmount());
    }

    private void addRotatoryFundContainsPendantRotatoryFundPaymentsError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.RotatoryFundContainsPendantRotatoryFundPayments");
    }

    private void addRotatoryFundContainsPendantRotatoryFundCollectionsError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.RotatoryFundContainsPendantRotatoryFundCollections");
    }

    private void addRotatoryFundPaymentSumIsLessThanRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundPaymentSumIsLessThanRotatoryFundAmount");
    }

    private void addApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFund.error.approvedRotatoryFundAmountCanNotBeLessThanApprovedPayments", getInstance().getAmount(), rotatoryFundPaymentService.getApprovedPaymentSum(getInstance()));
    }

    private void addRotatoryFundPaymentCurrencyDoNotMatchError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.rotatoryFundPaymentCurrencyDoNotMatch");
    }

    private void addCashAccountDistinctPayCurrencyError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundPayment.error.cashAccountDistinctPayCurrency",
                getInstance().getCashAccount().getFullNameAndCurrency(),
                FormatUtils.toAcronym(messages.get(getInstance().getPayCurrency().getResourceKey()), messages.get(getInstance().getPayCurrency().getSymbolResourceKey()))
        );
    }

    public boolean getShowCashAccountSelect() {
        return !(!isManaged() && limitedToOtherReceivables) && null != getInstance().getDocumentType() && ((!isCashAccountDefinedByDefault() && isRotatoryFundPending()) || (isCashAccountDefinedByDefault() && null == getInstance().getCashAccount()));
    }

    public boolean getShowCashAccountAdjustmentSelect() {
        return !limitedToOtherReceivables && isRotatoryFundPending();
//        return !(!isManaged() && limitedToOtherReceivables) && null != getInstance().getDocumentType() && ((!isCashAccountDefinedByDefault() && isRotatoryFundPending()) || (isCashAccountDefinedByDefault() && null == getInstance().getCashAccount()));
    }
}