package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.query.QueryBuilder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Service implementation of RotatoryFundService
 *
 * @author
 * @version 3.5.2.2
 */

@Stateless
@Name("rotatoryFundService")
@AutoCreate
public class RotatoryFundServiceBean extends GenericServiceBean implements RotatoryFundService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In(required = false)
    private User currentUser;
    @In(value = "quotaService")
    private QuotaService quotaService;
    @In
    private SpendDistributionService spendDistributionService;
    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;
    @In
    private RotatoryFundPaymentService rotatoryFundPaymentService;

    public RotatoryFund findById(Long rotatoryFundId) throws EntryNotFoundException {
        RotatoryFund rotatoryFund = (RotatoryFund) getEntityManager().createNamedQuery("RotatoryFund.findById")
                .setParameter("rotatoryFundId", rotatoryFundId).getSingleResult();

        if (rotatoryFund == null) {
            throw new EntryNotFoundException("RotatoryFund not found: " + rotatoryFundId);
        }

        return rotatoryFund;
    }


    @TransactionAttribute(REQUIRES_NEW)
    public void create(RotatoryFund rotatoryFund, List<Quota> quotaList, RotatoryFundPayment rotatoryFundPayment) throws EntryDuplicatedException {
        try {
            rotatoryFund.setCode(getNextCodeNumber().intValue());
            rotatoryFund.setPayableResidue(rotatoryFund.getAmount());
            rotatoryFund.setReceivableResidue(BigDecimal.ZERO);
            clearUnusedData(rotatoryFund);
            getEntityManager().persist(rotatoryFund);
            if (rotatoryFundPayment != null) {
                clearUnusedPaymentData(rotatoryFundPayment);
                rotatoryFundPayment.setCode(rotatoryFundPaymentService.getNextCodeNumber().intValue());
                rotatoryFundPayment.setRotatoryFund(rotatoryFund);
                getEntityManager().persist(rotatoryFundPayment);
            }
            /* update and persist quota list */
            for (Quota quota : quotaList) {
                quota.setResidue(quota.getAmount());
                quota.setDiscountByPayroll(rotatoryFund.getDiscountByPayroll());
                quota.setCurrency(rotatoryFund.getPayCurrency());
                quota.setExchangeRate(rotatoryFund.getExchangeRate());
                getEntityManager().persist(quota);
            }
            getEntityManager().flush();
        } catch (PersistenceException e) {
            log.debug("Persistence error..", e);
            throw new EntryDuplicatedException();
        }
    }

    private void clearUnusedData(RotatoryFund rotatoryFund) {
        if (rotatoryFund.getDocumentType().getRotatoryFundType().equals(RotatoryFundType.ADVANCE)
                || rotatoryFund.getDocumentType().getRotatoryFundType().equals(RotatoryFundType.LOAN)
                || rotatoryFund.getDocumentType().getRotatoryFundType().equals(RotatoryFundType.OTHER_RECEIVABLES)
                || rotatoryFund.getDocumentType().getRotatoryFundType().equals(RotatoryFundType.PARTNER_WITHDRAWAL)) {
            rotatoryFund.setProvider(null);
        }
    }

    private void clearUnusedPaymentData(RotatoryFundPayment rotatoryFundPayment) {
        if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT)
                || rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_WITH_CHECK)) {
            rotatoryFundPayment.setCashBoxCashAccount(null);
            rotatoryFundPayment.setSourceCurrency(rotatoryFundPayment.getBankAccount().getCurrency());
        }
        if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASHBOX)) {
            rotatoryFundPayment.setBankAccount(null);
            rotatoryFundPayment.setSourceCurrency(rotatoryFundPayment.getCashBoxCashAccount().getCurrency());
        }
        if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASH_ACCOUNT_ADJ)) {
            rotatoryFundPayment.setBankAccount(null);
            rotatoryFundPayment.setSourceCurrency(rotatoryFundPayment.getCashAccountAdjustment().getCurrency());
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void approveRotatoryFundAndPayments(RotatoryFund rotatoryFund)
            throws RotatoryFundApprovedException,
            RotatoryFundLiquidatedException,
            QuotaEmptyException,
            SpendDistributionEmptyException, SpendDistributionSumIsNotOneHundredException,
            ConcurrencyException,
            RotatoryFundNullifiedException, QuotaSumIsLessThanRotatoryFundAmountException,
            CompanyConfigurationNotFoundException, CurrencyDoNotMatchException, EntryDuplicatedException, PendantPaymentsSumExceedsRotatoryFundAmountException, RotatoryFundPaymentAnnulledException, PaymentSumExceedsRotatoryFundAmountException, RotatoryFundPaymentNotFoundException, RotatoryFundPaymentApprovedException, QuotaSumExceedsRotatoryFundAmountException, RotatoryFundPaymentSumIsLessThanRotatoryFundAmountException, RotatoryFundPaymentSumExceedsRotatoryFundAmountException, RotatoryFundPaymentCurrencyDoNotMatchException, ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException {

        RotatoryFund dataBaseRotatoryFund = findInDataBase(rotatoryFund.getId());
        if (BigDecimalUtil.sum(rotatoryFundPaymentService.getPendantPaymentSum(rotatoryFund),
                rotatoryFundPaymentService.getApprovedPaymentSum(rotatoryFund)).compareTo(rotatoryFund.getAmount()) > 0) {
            throw new PendantPaymentsSumExceedsRotatoryFundAmountException("The payments sum exceeds the rotatory fund amount");
        }

        if (canChangeRotatoryFund(dataBaseRotatoryFund)) {
            updateRotatoryFund(rotatoryFund);
            if (isRotatoryFundEmpty(rotatoryFund)) {
                throw new QuotaEmptyException("The quota cannot be empty");
            }
            if (quotaService.allValidQuotaSum(rotatoryFund).compareTo(rotatoryFund.getAmount()) < 0) {
                throw new QuotaSumIsLessThanRotatoryFundAmountException("The quota amount sum is less than the rotatory fund amount");
            }
            if (quotaService.allValidQuotaSum(rotatoryFund).compareTo(rotatoryFund.getAmount()) > 0) {
                throw new QuotaSumExceedsRotatoryFundAmountException("The quota amount sum is greater than the rotatory fund amount");
            }
            if (!quotaService.checkCurrency(rotatoryFund)) {
                throw new CurrencyDoNotMatchException("The rotatory fund currency do not match with the quotas currencies");
            }
            /* payment checks it can be less but not more than*/
            if (rotatoryFundPaymentService.allValidPaymentSum(rotatoryFund).compareTo(rotatoryFund.getAmount()) > 0) {
                throw new RotatoryFundPaymentSumExceedsRotatoryFundAmountException("The payment amount sum is greater than the rotatory fund amount");
            }
            if (!rotatoryFundPaymentService.checkCurrency(rotatoryFund)) {
                throw new RotatoryFundPaymentCurrencyDoNotMatchException("The rotatory fund currency do not match with the payments currencies");
            }
            rotatoryFund.setState(RotatoryFundState.APR);
            rotatoryFund.setApprovedByEmployee(currentUser);
//            getEntityManager().merge(rotatoryFund);
            if (!getEntityManager().contains(rotatoryFund)) {
                getEntityManager().merge(rotatoryFund);
            }
            getEntityManager().flush();

            quotaService.approvePendantQuotaList(rotatoryFund);
            getEntityManager().flush();

            /* only payments in pendant state*/
            List<RotatoryFundPayment> pendantRotatoryFundPaymentList = rotatoryFundPaymentService.getRotatoryFundPaymentListByState(rotatoryFund, RotatoryFundPaymentState.PEN);
            for (RotatoryFundPayment rotatoryFundPayment : pendantRotatoryFundPaymentList) {
                rotatoryFundPayment.setState(RotatoryFundPaymentState.APR);
                rotatoryFundPayment.setApprovedByEmployee(currentUser);
                rotatoryFundPayment.setApprovalDate(new Date());
                rotatoryFundPaymentService.updateRotatoryFund(rotatoryFundPayment, null);
                computeRotatoryFundStatistics(rotatoryFundPayment.getRotatoryFund());

            }
            if (!getEntityManager().contains(rotatoryFund)) {
                getEntityManager().merge(rotatoryFund);
            }
            getEntityManager().flush();
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateRotatoryFund(RotatoryFund rotatoryFund) throws ConcurrencyException, EntryDuplicatedException,
            ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException {
        clearUnusedData(rotatoryFund);
        RotatoryFund databaseRotatoryFund = findInDataBase(rotatoryFund.getId());
        List<Quota> quotaList = quotaService.getQuotaList(rotatoryFund);
        List<RotatoryFundPayment> rotatoryFundPaymentList = rotatoryFundPaymentService.getRotatoryFundPaymentList(rotatoryFund);
        /*if the update operation includes a change in this attribute so the corresponding attribute in its quotas have to be updated too*/
        if (databaseRotatoryFund.getDiscountByPayroll() != rotatoryFund.getDiscountByPayroll()) {
            for (Quota quota : quotaList) {
                quota.setDiscountByPayroll(rotatoryFund.getDiscountByPayroll());
                getEntityManager().merge(quota);
                getEntityManager().flush();
            }
        }
        /* if the currency have been moved update this too*/
        if (databaseRotatoryFund.getPayCurrency() != rotatoryFund.getPayCurrency()) {
            for (Quota quota : quotaList) {
                quota.setCurrency(rotatoryFund.getPayCurrency());
                getEntityManager().merge(quota);
                getEntityManager().flush();
            }
            for (RotatoryFundPayment rotatoryFundPayment : rotatoryFundPaymentList) {
                rotatoryFundPayment.setPaymentCurrency(rotatoryFund.getPayCurrency());
                getEntityManager().merge(rotatoryFundPayment);
                getEntityManager().flush();
            }
        }

        /*if the first quota date have been moved, so the quota dates will be moved too*/
        if (databaseRotatoryFund.getStartDate().compareTo(rotatoryFund.getStartDate()) != 0) {
            Long dayDifference = DateUtils.daysBetween(databaseRotatoryFund.getStartDate(), rotatoryFund.getStartDate(), false);
            for (Quota quota : quotaList) {
                Calendar calendar = DateUtils.toCalendar(quota.getExpirationDate());
                calendar.add(Calendar.DATE, dayDifference.intValue());
                quota.setExpirationDate(calendar.getTime());
                getEntityManager().merge(quota);
                getEntityManager().flush();
            }
        }

        computeRotatoryFundStatistics(rotatoryFund);

        /* in case of a special edit*/
        if (databaseRotatoryFund.getState().equals(RotatoryFundState.APR) && rotatoryFund.getState().equals(RotatoryFundState.APR)) {
            if (rotatoryFund.getAmount().compareTo(rotatoryFundPaymentService.getApprovedPaymentSum(rotatoryFund)) < 0) {
                throw new ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException("The approved rotatory fund amount can't be less than approved payments sum");
            } else {

                /*quotas which state is different that ANL*/
                List<Quota> approvedQuotaList = quotaService.getAllApprovedQuotaList(rotatoryFund);

                /* shrink or increase size or quotas*/
                BigDecimal reason = BigDecimalUtil.divide(rotatoryFund.getAmount(), databaseRotatoryFund.getAmount(), 10);
                BigDecimal newQuotaSum = BigDecimal.ZERO;

                for (int i = 0; i < approvedQuotaList.size(); i++) {
                    Quota quota = approvedQuotaList.get(i);
                    BigDecimal newAmount = BigDecimalUtil.multiply(quota.getAmount(), reason);
                    if (i < approvedQuotaList.size() - 1) {
                        quota.setAmount(newAmount);
                        newQuotaSum = BigDecimalUtil.sum(newQuotaSum, newAmount);
                    } else {
                        newAmount = BigDecimalUtil.subtract(rotatoryFund.getAmount(), newQuotaSum);
                        quota.setAmount(newAmount);
                    }
                    getEntityManager().merge(quota);
                    getEntityManager().flush();
                    getEntityManager().refresh(quota);
                }
                getEntityManager().flush();


                /*update the quota info and recompute statistics*/
                BigDecimal total = rotatoryFundCollectionService.getCollectionSum(rotatoryFund);
                double subTotal = total.doubleValue();

                approvedQuotaList = quotaService.getAllApprovedQuotaList(rotatoryFund);

                int index = 0;
                while (approvedQuotaList.size() > index && subTotal >= 0) {
                    Quota quota = approvedQuotaList.get(index);
                    if (subTotal >= quota.getAmount().doubleValue()) {
                        if (!quota.getState().equals(QuotaState.LIQ)) {
                            quota.setState(QuotaState.LIQ);
                            quota.setResidue(BigDecimal.ZERO);
                            getEntityManager().merge(quota);
                            getEntityManager().flush();
                        }
                        subTotal = subTotal - quota.getAmount().doubleValue();
                    } else if (subTotal > 0) {
                        quota.setState(QuotaState.PLI);
                        quota.setResidue(BigDecimalUtil.subtract(quota.getAmount(), BigDecimalUtil.toBigDecimal(subTotal)));
                        getEntityManager().merge(quota);
                        getEntityManager().flush();
                        subTotal = 0.0;
                    } else if (subTotal == 0) {
                        quota.setState(QuotaState.APR);
                        quota.setResidue(quota.getAmount());
                        getEntityManager().merge(quota);
                        getEntityManager().flush();
                    }
                    index++;
                }
                computeRotatoryFundStatistics(rotatoryFund);
            }
        }
        getEntityManager().merge(rotatoryFund);
        getEntityManager().flush();
    }

    public void computeRotatoryFundStatistics(RotatoryFund rotatoryFund) {
        Date maxDate;
        Date minDate;
        List<Quota> quotaList = quotaService.getQuotaList(rotatoryFund);
        rotatoryFund.setPayableResidue(getPayableResidueByRotatoryFund(rotatoryFund));
        rotatoryFund.setReceivableResidue(getReceivableResidueByRotatoryFund(rotatoryFund));
        int number = 0;
        if (quotaList != null) {
            number = quotaList.size();
        }
        maxDate = quotaService.getMaxQuotaDate(rotatoryFund);
        minDate = quotaService.getMinQuotaDate(rotatoryFund);
        if ((maxDate != null && !maxDate.equals(rotatoryFund.getExpirationDate()))
                || !rotatoryFund.getPaymentsNumber().equals(number)
                || (minDate != null && !minDate.equals(rotatoryFund.getStartDate()))) {
            rotatoryFund.setPaymentsNumber(number);
            rotatoryFund.setExpirationDate(maxDate);
            rotatoryFund.setStartDate(minDate);
            getEntityManager().merge(rotatoryFund);
        }
    }

    public void updateLiquidatedState(RotatoryFund rotatoryFund) {
        if (BigDecimalUtil.isZeroOrNull(rotatoryFund.getPayableResidue()) &&
                BigDecimalUtil.isZeroOrNull(rotatoryFund.getReceivableResidue())) {
            rotatoryFund.setState(RotatoryFundState.LIQ);
            getEntityManager().merge(rotatoryFund);
            getEntityManager().flush();
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void annulRotatoryFund(RotatoryFund entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ConcurrencyException,
            RotatoryFundNullifiedException, ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException {
        RotatoryFund dbRotatoryFund = findRotatoryFund(entity.getId());
        if (canChangeRotatoryFund(dbRotatoryFund)) {
            entity.setState(RotatoryFundState.ANL);
            try {
                update(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("Unexpected error have happen when annulling rotatoryFund", e);
            }
        }
    }

    public CashAccount matchCashAccount(RotatoryFund rotatoryFund) throws CompanyConfigurationNotFoundException {
        try {
            return findById(CashAccount.class, rotatoryFund.getCashAccount().getId());
        } catch (EntryNotFoundException e) {
            throw new CompanyConfigurationNotFoundException(e);
        }
    }

    public RotatoryFund findInDataBase(Long id) {
        RotatoryFund rotatoryFund = listEm.find(RotatoryFund.class, id);
        if (null == rotatoryFund) {
            throw new RuntimeException("Cannot find the PurchaseOrder entity for id=" + id);
        }
        return rotatoryFund;
    }

    public RotatoryFund findRotatoryFund(Long id) {
        findInDataBase(id);
        RotatoryFund rotatoryFund = getEntityManager().find(RotatoryFund.class, id);
        getEntityManager().refresh(rotatoryFund);
        return rotatoryFund;
    }

    public boolean canChangeRotatoryFund(RotatoryFund entity)
            throws RotatoryFundApprovedException,
            RotatoryFundLiquidatedException, RotatoryFundNullifiedException {

        if (isRotatoryFundApproved(entity)) {
            findRotatoryFund(entity.getId());
            throw new RotatoryFundApprovedException("The rotatoryFund was already approved, and cannot be changed");
        }

        if (isRotatoryFundLiquidated(entity)) {
            findRotatoryFund(entity.getId());
            throw new RotatoryFundLiquidatedException("The rotatoryFund was already liquidated, and cannot be changed");
        }

        if (isRotatoryFundNullified(entity)) {
            findRotatoryFund(entity.getId());
            throw new RotatoryFundNullifiedException("The rotatoryFund was already nullified, and cannot be changed");
        }
        return RotatoryFundState.PEN.equals(entity.getState());
    }

    public boolean canCollectRotatoryFund(RotatoryFund rotatoryFund)
            throws RotatoryFundLiquidatedException, RotatoryFundNullifiedException {

        if (isRotatoryFundLiquidated(rotatoryFund)) {
            findRotatoryFund(rotatoryFund.getId());
            throw new RotatoryFundLiquidatedException("The rotatoryFund was already liquidated, and cannot be changed");
        }

        if (isRotatoryFundNullified(rotatoryFund)) {
            findRotatoryFund(rotatoryFund.getId());
            throw new RotatoryFundNullifiedException("The rotatoryFund was already nullified, and cannot be changed");
        }
        return true;
    }

    public Long getNextCodeNumber() {
        Integer codeNumber = ((Integer) getEntityManager().createNamedQuery("RotatoryFund.maxNumber").getSingleResult());
        if (null == codeNumber) {
            codeNumber = 1;
        } else {
            codeNumber++;
        }
        return codeNumber.longValue();
    }

    public BigDecimal sumRotatoryFundByEmployeeByTypeByCurrencyByState(Employee employee, RotatoryFundType rotatoryFundType, FinancesCurrencyType currencyType, RotatoryFundState rotatoryFundState) {
        return (BigDecimal) listEm.createNamedQuery("RotatoryFund.sumByEmployeeByTypeByCurrencyByState")
                .setParameter("employee", employee)
                .setParameter("rotatoryFundType", rotatoryFundType)
                .setParameter("payCurrency", currencyType)
                .setParameter("state", rotatoryFundState).getSingleResult();
    }
    /* this calculates the payable residue */

    public BigDecimal getPayableResidueByRotatoryFund(RotatoryFund rotatoryFund) {
        BigDecimal approvedPaymentSum = (BigDecimal) listEm.createNamedQuery("RotatoryFundPayment.findSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).
                        setParameter("state", RotatoryFundPaymentState.APR).getSingleResult();
        if (approvedPaymentSum == null) {
            approvedPaymentSum = BigDecimal.ZERO;
        }
        return BigDecimalUtil.subtract(rotatoryFund.getAmount(), approvedPaymentSum);
    }

    /* this calculates the receivable residue*/

    public BigDecimal getReceivableResidueByRotatoryFund(RotatoryFund rotatoryFund) {
        BigDecimal collectionSum = (BigDecimal) listEm.createNamedQuery("RotatoryFundCollection.findSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).
                        setParameter("state", RotatoryFundPaymentState.APR).getSingleResult();
        if (collectionSum == null) {
            collectionSum = BigDecimal.ZERO;
        }
        return BigDecimalUtil.subtract(rotatoryFund.getAmount(), collectionSum);
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFund> findRotatoryFundsByEmployeeByTypeByCurrencyByState(Employee employee, RotatoryFundType rotatoryFundType, FinancesCurrencyType currencyType, RotatoryFundState rotatoryFundState) {
        return listEm.createNamedQuery("RotatoryFund.findByEmployeeByTypeByCurrencyByState")
                .setParameter("employee", employee)
                .setParameter("rotatoryFundType", rotatoryFundType)
                .setParameter("payCurrency", currencyType)
                .setParameter("state", rotatoryFundState).getResultList();
    }

    protected Boolean isRotatoryFundEmpty(RotatoryFund rotatoryFund) {
        List<Quota> quotaList = quotaService.getQuotaList(rotatoryFund);
        return quotaList == null || quotaList.isEmpty();
    }

    protected Boolean isRotatoryFundCollectionDistributionEmpty(RotatoryFund rotatoryFund) {
        List<SpendDistribution> spendDistributionList = spendDistributionService.getSpendDistributionList(rotatoryFund);
        return spendDistributionList == null || spendDistributionList.isEmpty();
    }

    public Boolean isRotatoryFundApproved(RotatoryFund instance) {
        return isRotatoryFundState(instance, RotatoryFundState.APR);
    }

    public Boolean isRotatoryFundLiquidated(RotatoryFund instance) {
        return isRotatoryFundState(instance, RotatoryFundState.LIQ);
    }

    public Boolean isRotatoryFundNullified(RotatoryFund instance) {
        return isRotatoryFundState(instance, RotatoryFundState.ANL);
    }

    protected Boolean isRotatoryFundState(RotatoryFund instance, RotatoryFundState state) {
        RotatoryFund rotatoryFund = findInDataBase(instance.getId());
        return null != rotatoryFund.getState() && state.equals(rotatoryFund.getState());
    }

    public BigDecimal getPaymentAmountBeforeMovementDate(Long rotatoryFundId, Date movementDate) {
        BigDecimal sumPaymentAmount = null;

        try {
            sumPaymentAmount = (BigDecimal) getEntityManager().createNamedQuery("RotatoryFundPayment.sumPaymentAmountByRotatoryFundAndMovementDate")
                    .setParameter("rotatoryFundId", rotatoryFundId)
                    .setParameter("movementDate", movementDate)
                    .setParameter("state", RotatoryFundPaymentState.APR).getSingleResult();
        } catch (NoResultException ignored) {
        }

        return sumPaymentAmount;
    }

    public BigDecimal getCollectionAmountBeforeMovementDate(Long rotatoryFundId, Date movementDate) {

        BigDecimal sumCollectionAmount = null;
        try {
            sumCollectionAmount = (BigDecimal) getEntityManager().createNamedQuery("RotatoryFundCollection.sumCollectionAmountByRotatoryFundAndMovementDate")
                    .setParameter("rotatoryFundId", rotatoryFundId)
                    .setParameter("movementDate", movementDate)
                    .setParameter("state", RotatoryFundPaymentState.APR).getSingleResult();
        } catch (NoResultException ignored) {
        }
        return sumCollectionAmount;
    }

    public BigDecimal getMovementAmountBeforeMovementDate(Long rotatoryFundId, Date movementDate) {
        BigDecimal movementAmount = null;
        try {
            movementAmount = (BigDecimal) getEntityManager().createNamedQuery("RotatoryFundMovement.sumAmountByRotatoryFundAndMovementDate")
                    .setParameter("rotatoryFundId", rotatoryFundId)
                    .setParameter("movementDate", movementDate)
                    .setParameter("state", RotatoryFundMovementState.APR).getSingleResult();
        } catch (NoResultException ignored) {
            ignored.fillInStackTrace();
        }
        return movementAmount;
    }

    public BigDecimal calculateResidueAmount(Long businessUnitId,
                                             Long employeeId,
                                             Long documentTypeId,
                                             Long rotatoryFundId,
                                             Integer rotatoryFundCode,
                                             Date movementStartDate,
                                             Date movementEndDate,
                                             FinancesCurrency financesCurrency,
                                             String companyNumber,
                                             String cashAccountCode) {
        try {
            return (BigDecimal) QueryBuilder.createQuery(
                    "select sum(rotatoryFundMovement.paymentAmount-rotatoryFundMovement.collectionAmount)" +
                            " FROM RotatoryFund rotatoryFund" +
                            " LEFT JOIN rotatoryFund.documentType documentType" +
                            " LEFT JOIN rotatoryFund.cashAccount cashAccount" +
                            " LEFT JOIN cashAccount.financesCurrency financesCurrency" +
                            " LEFT JOIN rotatoryFund.businessUnit businessUnit" +
                            " LEFT JOIN rotatoryFund.employee employee" +
                            " LEFT JOIN rotatoryFund.rotatoryFundMovementList rotatoryFundMovement")
                    .addRestriction("(rotatoryFund.state=:approvedState or rotatoryFund.state=:annulledState)", QueryBuilder.param("approvedState", RotatoryFundState.APR), QueryBuilder.param("annulledState", RotatoryFundState.LIQ))
                    .addRestriction("rotatoryFundMovement.state=:rotatoryFundMovementState", QueryBuilder.param("rotatoryFundMovementState", RotatoryFundMovementState.APR))
                    .addRestriction("businessUnit.id=:businessUnitId", QueryBuilder.param("businessUnitId", businessUnitId))
                    .addRestriction("rotatoryFund.id=:rotatoryFundId", QueryBuilder.param("rotatoryFundId", rotatoryFundId))
                    .addRestriction("rotatoryFund.code=:rotatoryFundCode", QueryBuilder.param("rotatoryFundCode", rotatoryFundCode))
                    .addRestriction("rotatoryFundMovement.date >=:movementStartDate", QueryBuilder.param("movementStartDate", movementStartDate))
                    .addRestriction("rotatoryFundMovement.date <=:movementEndDate", QueryBuilder.param("movementEndDate", movementEndDate))
                    .addRestriction("financesCurrency=:financesCurrency", QueryBuilder.param("financesCurrency", financesCurrency))
                    .addRestriction("employee.id=:employeeId", QueryBuilder.param("employeeId", employeeId))
                    .addRestriction("documentType.id=:documentTypeId", QueryBuilder.param("documentTypeId", documentTypeId))
                    .addRestriction("(cashAccount.companyNumber=:companyNumber and cashAccount.accountCode=:cashAccountCode)", QueryBuilder.param("companyNumber", companyNumber), QueryBuilder.param("cashAccountCode", cashAccountCode))
                    .getSingleResult(getEventEntityManager());
        } catch (NoResultException ignored) {
        }
        return BigDecimal.ZERO;
    }

}