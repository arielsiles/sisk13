package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.finances.RotatoryFundPayment;
import com.encens.khipus.model.finances.RotatoryFundPaymentState;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.math.BigDecimal;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.23
 */
@Local
public interface RotatoryFundPaymentService extends GenericService {

    BigDecimal getApprovedPaymentSum(RotatoryFund rotatoryFund);

    @TransactionAttribute(REQUIRES_NEW)
    void createRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment)
            throws RotatoryFundLiquidatedException, PaymentSumExceedsRotatoryFundAmountException,
            RotatoryFundNullifiedException, CompanyConfigurationNotFoundException;

    void persistRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment)
            throws RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException;

    void updateRotatoryFund(RotatoryFundPayment rotatoryFundPayment, FinancesCurrencyType defaultCurrency)
            throws RotatoryFundLiquidatedException,
            ConcurrencyException, RotatoryFundPaymentNotFoundException,
            PaymentSumExceedsRotatoryFundAmountException, RotatoryFundNullifiedException,
            RotatoryFundPaymentAnnulledException, RotatoryFundPaymentApprovedException, CompanyConfigurationNotFoundException;

    @TransactionAttribute(REQUIRES_NEW)
    void approveRotatoryFundPayment(RotatoryFundPayment rotatoryFundPayment)
            throws RotatoryFundNullifiedException,
            ConcurrencyException, RotatoryFundLiquidatedException, PaymentSumExceedsRotatoryFundAmountException,
            RotatoryFundPaymentNotFoundException, RotatoryFundPaymentAnnulledException,
            RotatoryFundPaymentApprovedException, CompanyConfigurationNotFoundException;

    @TransactionAttribute(REQUIRES_NEW)
    void annulRotatoryFundPayment(RotatoryFundPayment entity)
            throws RotatoryFundPaymentNotFoundException, RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException, RotatoryFundPaymentAnnulledException,
            RotatoryFundPaymentApprovedException, ConcurrencyException;

    void annulRotatoryFundPaymentToRemake(RotatoryFundPayment payment)
            throws RotatoryFundPaymentNotFoundException,
            RotatoryFundLiquidatedException,
            RotatoryFundPaymentAnnulledException,
            RotatoryFundNullifiedException;

    void deleteRotatoryFund(RotatoryFundPayment entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            RotatoryFundPaymentNotFoundException, RotatoryFundPaymentApprovedException;

    BigDecimal getPendantPaymentSum(RotatoryFund rotatoryFund);

    RotatoryFundPayment findRotatoryFundPayment(Long id) throws RotatoryFundPaymentNotFoundException;

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFundPayment> getRotatoryFundPaymentList(RotatoryFund rotatoryFund);

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFundPayment> getRotatoryFundPaymentListByState(RotatoryFund rotatoryFund, RotatoryFundPaymentState rotatoryFundPaymentState);

    Long getNextCodeNumber();

    BigDecimal allValidPaymentSum(RotatoryFund rotatoryFund);

    boolean checkCurrency(RotatoryFund rotatoryFund);

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFundPayment> getEventRotatoryFundPaymentListByState(RotatoryFund rotatoryFund, RotatoryFundPaymentState rotatoryFundPaymentState);
}