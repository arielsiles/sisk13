package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.24
 */
@Stateless
@Name("rotatoryFundPaymentService")
@AutoCreate
public class RotatoryFundPaymentServiceBean extends GenericServiceBean implements RotatoryFundPaymentService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private RotatoryFundService rotatoryFundService;
    @In
    private RotatoryFundAccountEntryService rotatoryFundAccountEntryService;

    @In
    private User currentUser;

    @TransactionAttribute(REQUIRES_NEW)
    public void createRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment)
            throws RotatoryFundLiquidatedException,
            PaymentSumExceedsRotatoryFundAmountException,
            RotatoryFundNullifiedException,
            CompanyConfigurationNotFoundException {

        clearUnusedPaymentData(rotatoryFundPayment);
        rotatoryFundPayment.setCode(getNextCodeNumber().intValue());
        RotatoryFund rotatoryFund = rotatoryFundPayment.getRotatoryFund();
        rotatoryFundPayment.setCreationDate(new Date());
        RotatoryFund databaseRotatoryFund = listEm.find(RotatoryFund.class, rotatoryFund.getId());

        if (rotatoryFundService.canCollectRotatoryFund(rotatoryFund)) {
            BigDecimal paymentSum = BigDecimalUtil.sum(getPendantPaymentSum(rotatoryFund),
                    getApprovedPaymentSum(rotatoryFund),
                    rotatoryFundPayment.getPaymentAmount());
            if (paymentSum.compareTo(rotatoryFund.getAmount()) > 0) {
                throw new PaymentSumExceedsRotatoryFundAmountException("The payments amount sum exceeds the rotatory fund amount");
            }
            try {
                super.create(rotatoryFundPayment);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("An Unexpected error has happened ", e);
            }
        }
    }

    public void persistRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment)
            throws RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException {
        if (rotatoryFundService.canCollectRotatoryFund(rotatoryFundPayment.getRotatoryFund())) {
            clearUnusedPaymentData(rotatoryFundPayment);
            //noinspection NullableProblems
            rotatoryFundPayment.setReversionCause(null);
            rotatoryFundPayment.setCode(getNextCodeNumber().intValue());
            rotatoryFundPayment.setCreationDate(new Date());

            getEntityManager().persist(rotatoryFundPayment);
        }
    }

    private void clearUnusedPaymentData(RotatoryFundPayment rotatoryFundPayment) {
        if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT)
                || rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_WITH_CHECK)) {
            rotatoryFundPayment.setCashBoxCashAccount(null);
            rotatoryFundPayment.setCashAccountAdjustment(null);
            rotatoryFundPayment.setSourceCurrency(rotatoryFundPayment.getBankAccount().getCurrency());
        }
        if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASHBOX)) {
            rotatoryFundPayment.setBankAccount(null);
            rotatoryFundPayment.setSourceCurrency(rotatoryFundPayment.getCashBoxCashAccount().getCurrency());
            rotatoryFundPayment.setCheckDestination(null);
        }
        if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASH_ACCOUNT_ADJ)) {
            rotatoryFundPayment.setBankAccount(null);
            rotatoryFundPayment.setSourceCurrency(rotatoryFundPayment.getCashAccountAdjustment().getCurrency());
            rotatoryFundPayment.setCheckDestination(null);
        }
    }

    public RotatoryFundPayment findRotatoryFundPayment(Long id) throws RotatoryFundPaymentNotFoundException {
        findInDataBase(id);
        RotatoryFundPayment rotatoryFundPayment = getEntityManager().find(RotatoryFundPayment.class, id);
        getEntityManager().refresh(rotatoryFundPayment);
        return rotatoryFundPayment;
    }

    public RotatoryFundPayment findDatabaseRotatoryFundPayment(Long id) throws RotatoryFundPaymentNotFoundException {
        RotatoryFundPayment rotatoryFundPayment = listEm.find(RotatoryFundPayment.class, id);
        if (rotatoryFundPayment == null) {
            throw new RotatoryFundPaymentNotFoundException("Cannot find the RotatoryFundPayment entity for id=" + id);
        }
        return rotatoryFundPayment;
    }

    public void updateRotatoryFund(RotatoryFundPayment rotatoryFundPayment, FinancesCurrencyType defaultCurrency)
            throws RotatoryFundLiquidatedException,
            ConcurrencyException, RotatoryFundPaymentNotFoundException,
            PaymentSumExceedsRotatoryFundAmountException, RotatoryFundNullifiedException,
            RotatoryFundPaymentAnnulledException, RotatoryFundPaymentApprovedException, CompanyConfigurationNotFoundException {

        clearUnusedPaymentData(rotatoryFundPayment);
        RotatoryFund rotatoryFund = rotatoryFundPayment.getRotatoryFund();
        if (rotatoryFundService.isRotatoryFundLiquidated(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(rotatoryFundPayment.getId());
            throw new RotatoryFundLiquidatedException("The rotatoryFund was already liquidated, and cannot be changed");
        }

        /* The update operation is allowed only in Pendant database State */
        if (canUpdateRotatoryFundPayment(rotatoryFundPayment, rotatoryFund)) {
            BigDecimal paymentSum = getPaymentSumButCurrentByState(rotatoryFund, rotatoryFundPayment, RotatoryFundPaymentState.APR);
            BigDecimal total = BigDecimalUtil.sum(paymentSum, rotatoryFundPayment.getPaymentAmount());
            if (total.compareTo(rotatoryFund.getAmount()) > 0) {
                if (findDatabaseRotatoryFundPayment(rotatoryFundPayment.getId()).getState().equals(RotatoryFundPaymentState.PEN)) {
                    rotatoryFundPayment.setState(RotatoryFundPaymentState.PEN);
                }
                throw new PaymentSumExceedsRotatoryFundAmountException("The payments amount sum exceeds the rotatory fund amount");
            }
            if (!getEntityManager().contains(rotatoryFundPayment)) {
                getEntityManager().merge(rotatoryFundPayment);
            }
            if (rotatoryFundPayment.getState().equals(RotatoryFundPaymentState.APR)) {
                /* uncomment this to fire data to accounting system */
                CashAccount cashAccount = rotatoryFundService.matchCashAccount(rotatoryFund);
                fireAccountingEntry(rotatoryFundPayment, cashAccount);
                if (!getEntityManager().contains(rotatoryFund)) {
                    getEntityManager().merge(rotatoryFund);
                }
            }
            getEntityManager().flush();
        }
    }

    private void fireAccountingEntry(RotatoryFundPayment rotatoryFundPayment, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException {
        if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT)
                || rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_WITH_CHECK)) {
            rotatoryFundAccountEntryService.createRotatoryFundPaymentAccountVsBankAccountEntry(rotatoryFundPayment, cashAccount);
        } else if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASHBOX)) {
            rotatoryFundAccountEntryService.createRotatoryFundPaymentAccountVsCashBoxEntry(rotatoryFundPayment, cashAccount);
        } else if (rotatoryFundPayment.getRotatoryFundPaymentType().equals(RotatoryFundPaymentType.PAYMENT_CASH_ACCOUNT_ADJ)) {
            rotatoryFundAccountEntryService.createRotatoryFundPaymentAccountVsCashAccountAdjustmentEntry(rotatoryFundPayment, cashAccount);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void annulRotatoryFundPayment(RotatoryFundPayment entity)
            throws RotatoryFundPaymentNotFoundException, RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException, RotatoryFundPaymentAnnulledException,
            RotatoryFundPaymentApprovedException, ConcurrencyException {
        RotatoryFundPayment dbRotatoryFundPayment = findDatabaseRotatoryFundPayment(entity.getId());
        if (canUpdateRotatoryFundPayment(dbRotatoryFundPayment, dbRotatoryFundPayment.getRotatoryFund())) {
            entity.setState(RotatoryFundPaymentState.ANL);
            try {
                super.update(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("An Unexpected error has happened ", e);
            }
        }
    }

    public void annulRotatoryFundPaymentToRemake(RotatoryFundPayment payment)
            throws RotatoryFundPaymentNotFoundException,
            RotatoryFundLiquidatedException,
            RotatoryFundPaymentAnnulledException,
            RotatoryFundNullifiedException {
        RotatoryFundPayment databasePayment = findDatabaseRotatoryFundPayment(payment.getId());
        if (databasePayment.isNullified()) {
            throw new RotatoryFundPaymentAnnulledException();
        }

        if (databasePayment.getRotatoryFund().isLiquidated()) {
            throw new RotatoryFundLiquidatedException();
        }

        if (databasePayment.getRotatoryFund().isNullified()) {
            throw new RotatoryFundNullifiedException();
        }

        payment.setState(RotatoryFundPaymentState.ANL);

        getEntityManager().merge(payment);
    }

    private boolean canUpdateRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment, RotatoryFund rotatoryFund)
            throws RotatoryFundLiquidatedException, RotatoryFundNullifiedException,
            RotatoryFundPaymentApprovedException, RotatoryFundPaymentAnnulledException, RotatoryFundPaymentNotFoundException {
        RotatoryFundPayment databaseRotatoryFundPayment = findDatabaseRotatoryFundPayment(rotatoryFundPayment.getId());
        if (rotatoryFundService.canCollectRotatoryFund(rotatoryFund) && databaseRotatoryFundPayment.getState().equals(RotatoryFundPaymentState.APR)) {
            throw new RotatoryFundPaymentApprovedException("The rotatory fund payment was already approved, and can not be changed");
        }
        if (rotatoryFundService.canCollectRotatoryFund(rotatoryFund) && rotatoryFundPayment.getState().equals(RotatoryFundPaymentState.ANL)) {
            throw new RotatoryFundPaymentAnnulledException("The rotatory fund payment was already annulled, and can not be changed");
        }
        return true;
    }


    @TransactionAttribute(REQUIRES_NEW)
    public void approveRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment)
            throws RotatoryFundNullifiedException,
            ConcurrencyException, RotatoryFundLiquidatedException, PaymentSumExceedsRotatoryFundAmountException,
            RotatoryFundPaymentNotFoundException, RotatoryFundPaymentAnnulledException,
            RotatoryFundPaymentApprovedException, CompanyConfigurationNotFoundException {
        rotatoryFundPayment.setState(RotatoryFundPaymentState.APR);
        rotatoryFundPayment.setApprovedByEmployee(currentUser);
        updateRotatoryFund(rotatoryFundPayment, null);
        rotatoryFundService.computeRotatoryFundStatistics(rotatoryFundPayment.getRotatoryFund());
        rotatoryFundService.updateLiquidatedState(rotatoryFundPayment.getRotatoryFund());
    }

    public void deleteRotatoryFund(RotatoryFundPayment entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            RotatoryFundPaymentNotFoundException, RotatoryFundPaymentApprovedException {

        RotatoryFundPayment databaseRotatoryFundPayment = findInDataBase(entity.getId());
        if (!entity.getState().equals(RotatoryFundPaymentState.APR) && !databaseRotatoryFundPayment.getState().equals(RotatoryFundPaymentState.APR)) {
            try {
                super.delete(entity);
            } catch (ConcurrencyException e) {
                throw new RotatoryFundPaymentNotFoundException(e);
            }
        } else {
            throw new RotatoryFundPaymentApprovedException("the rotatory fund was approved by someone else");
        }
    }

    public BigDecimal allValidPaymentSum(RotatoryFund rotatoryFund) {
        return BigDecimalUtil.sum(getPaymentSumByState(rotatoryFund, RotatoryFundPaymentState.PEN)
                , getPaymentSumByState(rotatoryFund, RotatoryFundPaymentState.APR));
    }

    public BigDecimal getPaymentSumByState(RotatoryFund rotatoryFund, RotatoryFundPaymentState state) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundPayment.findSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", state).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public boolean checkCurrency(RotatoryFund rotatoryFund) {
        int queryResult = ((Long) getEntityManager()
                .createNamedQuery("RotatoryFundPayment.checkCurrency")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("currency", rotatoryFund.getPayCurrency()).getSingleResult()).intValue();
        return queryResult == 0;
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundPayment> getRotatoryFundPaymentList(RotatoryFund rotatoryFund) {
        return getEntityManager()
                .createNamedQuery("RotatoryFundPayment.findByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundPayment> getRotatoryFundPaymentListByState(RotatoryFund rotatoryFund, RotatoryFundPaymentState rotatoryFundPaymentState) {
        return getEntityManager()
                .createNamedQuery("RotatoryFundPayment.findByRotatoryFundByState")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("rotatoryFundPaymentState", rotatoryFundPaymentState).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundPayment> getEventRotatoryFundPaymentListByState(RotatoryFund rotatoryFund, RotatoryFundPaymentState rotatoryFundPaymentState) {
        return listEm
                .createNamedQuery("RotatoryFundPayment.findByRotatoryFundByState")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("rotatoryFundPaymentState", rotatoryFundPaymentState).getResultList();
    }

    public Date getMaxRotatoryFundPaymentDate(RotatoryFund rotatoryFund) {
        return (Date) getEntityManager()
                .createNamedQuery("RotatoryFundPayment.findMaxDateByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getSingleResult();
    }

    public BigDecimal getApprovedPaymentSum(RotatoryFund rotatoryFund) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundPayment.findSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", RotatoryFundPaymentState.APR).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public BigDecimal getPendantPaymentSum(RotatoryFund rotatoryFund) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundPayment.findSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", RotatoryFundPaymentState.PEN).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public BigDecimal getPaymentSumButCurrentByState(RotatoryFund rotatoryFund, RotatoryFundPayment rotatoryFundPayment,
                                                     RotatoryFundPaymentState rotatoryFundPaymentState) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundPayment.findSumByRotatoryFundButCurrent")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("id", rotatoryFundPayment.getId())
                .setParameter("state", rotatoryFundPaymentState).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    private RotatoryFundPayment findInDataBase(Long id) throws RotatoryFundPaymentNotFoundException {
        RotatoryFundPayment rotatoryFundPayment = listEm.find(RotatoryFundPayment.class, id);
        if (null == rotatoryFundPayment) {
            throw new RotatoryFundPaymentNotFoundException("Cannot find the RotatoryFundPayment entity for id=" + id);
        }

        return rotatoryFundPayment;
    }

    @SuppressWarnings(value = "unchecked")
    public void annulPendantRotatoryFundPayments(RotatoryFund rotatoryFund) {
        getEntityManager()
                .createNamedQuery("RotatoryFundPayment.annulPendantRotatoryFundPayments")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("databaseState", RotatoryFundPaymentState.PEN)
                .setParameter("state", RotatoryFundPaymentState.ANL).executeUpdate();
    }

    public Boolean isRotatoryFundPaymentApproved(RotatoryFundPayment instance) {
        return isRotatoryFundPaymentState(instance, RotatoryFundPaymentState.APR);
    }

    public Boolean isRotatoryFundPaymentNullified(RotatoryFundPayment instance) {
        return isRotatoryFundPaymentState(instance, RotatoryFundPaymentState.ANL);
    }

    protected Boolean isRotatoryFundPaymentState(RotatoryFundPayment instance, RotatoryFundPaymentState state) {
        RotatoryFundPayment rotatoryFundPayment;
        try {
            rotatoryFundPayment = findInDataBase(instance.getId());
        } catch (RotatoryFundPaymentNotFoundException e) {
            return false;
        }
        return null != rotatoryFundPayment.getState() && state.equals(rotatoryFundPayment.getState());
    }

    public boolean canChangeRotatoryFundPayment(RotatoryFundPayment entity)
            throws RotatoryFundApprovedException,
            RotatoryFundLiquidatedException, RotatoryFundNullifiedException, RotatoryFundPaymentNotFoundException, RotatoryFundPaymentApprovedException, RotatoryFundPaymentAnnulledException {

        if (isRotatoryFundPaymentApproved(entity)) {
            findRotatoryFundPayment(entity.getId());
            throw new RotatoryFundPaymentApprovedException("The rotatoryFundPayment was already approved, and cannot be changed");
        }
        if (isRotatoryFundPaymentNullified(entity)) {
            findRotatoryFundPayment(entity.getId());
            throw new RotatoryFundPaymentAnnulledException("The rotatoryFundPayment was already nullified, and cannot be changed");
        }
        return RotatoryFundPaymentState.PEN.equals(entity.getState());
    }

    public Long getNextCodeNumber() {
        Integer codeNumber = ((Integer) listEm.createNamedQuery("RotatoryFundPayment.maxNumber").getSingleResult());
        if (null == codeNumber) {
            codeNumber = 1;
        } else {
            codeNumber++;
        }
        return codeNumber.longValue();
    }
}