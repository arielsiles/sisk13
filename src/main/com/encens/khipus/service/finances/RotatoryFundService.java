package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.*;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Encens S.R.L.
 * This class implements the RotatoryFund service local interface
 *
 * @author
 * @version 2.26
 */
@Local
public interface RotatoryFundService extends GenericService {

    RotatoryFund findById(Long rotatoryFundId) throws EntryNotFoundException;

    Boolean isRotatoryFundApproved(RotatoryFund instance);

    Boolean isRotatoryFundLiquidated(RotatoryFund instance);

    Boolean isRotatoryFundNullified(RotatoryFund instance);

    RotatoryFund findRotatoryFund(Long id);

    boolean canChangeRotatoryFund(RotatoryFund entity)
            throws RotatoryFundApprovedException,
            RotatoryFundLiquidatedException, RotatoryFundNullifiedException;

    void computeRotatoryFundStatistics(RotatoryFund rotatoryFund);

    @TransactionAttribute(REQUIRES_NEW)
    void annulRotatoryFund(RotatoryFund entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ConcurrencyException,
            RotatoryFundNullifiedException, ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException;

    boolean canCollectRotatoryFund(RotatoryFund rotatoryFund)
            throws RotatoryFundLiquidatedException, RotatoryFundNullifiedException;

    CashAccount matchCashAccount(RotatoryFund rotatoryFund) throws CompanyConfigurationNotFoundException;

    BigDecimal sumRotatoryFundByEmployeeByTypeByCurrencyByState(Employee employee, RotatoryFundType rotatoryFundType, FinancesCurrencyType currencyType, RotatoryFundState rotatoryFundState);

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFund> findRotatoryFundsByEmployeeByTypeByCurrencyByState(Employee employee, RotatoryFundType rotatoryFundType, FinancesCurrencyType currencyType, RotatoryFundState rotatoryFundState);

    BigDecimal getPayableResidueByRotatoryFund(RotatoryFund rotatoryFund);

    BigDecimal getReceivableResidueByRotatoryFund(RotatoryFund rotatoryFund);

    @TransactionAttribute(REQUIRES_NEW)
    void approveRotatoryFundAndPayments(RotatoryFund rotatoryFund)
            throws RotatoryFundApprovedException,
            RotatoryFundLiquidatedException,
            QuotaEmptyException,
            SpendDistributionEmptyException, SpendDistributionSumIsNotOneHundredException,
            ConcurrencyException,
            RotatoryFundNullifiedException, QuotaSumIsLessThanRotatoryFundAmountException,
            CompanyConfigurationNotFoundException, CurrencyDoNotMatchException, EntryDuplicatedException, PendantPaymentsSumExceedsRotatoryFundAmountException, RotatoryFundPaymentAnnulledException, PaymentSumExceedsRotatoryFundAmountException, RotatoryFundPaymentNotFoundException, RotatoryFundPaymentApprovedException, QuotaSumExceedsRotatoryFundAmountException, RotatoryFundPaymentSumIsLessThanRotatoryFundAmountException, RotatoryFundPaymentSumExceedsRotatoryFundAmountException, RotatoryFundPaymentCurrencyDoNotMatchException, ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException;

    @TransactionAttribute(REQUIRES_NEW)
    void updateRotatoryFund(RotatoryFund rotatoryFund) throws ConcurrencyException, EntryDuplicatedException,
            ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException;

    RotatoryFund findInDataBase(Long id);

    @TransactionAttribute(REQUIRES_NEW)
    void create(RotatoryFund rotatoryFund, List<Quota> quotaList, RotatoryFundPayment rotatoryFundPayment) throws EntryDuplicatedException;

    void updateLiquidatedState(RotatoryFund rotatoryFund);

    BigDecimal getPaymentAmountBeforeMovementDate(Long rotatoryFundId, Date movementDate);

    BigDecimal getCollectionAmountBeforeMovementDate(Long rotatoryFundId, Date movementDate);

    BigDecimal getMovementAmountBeforeMovementDate(Long rotatoryFundId, Date movementDate);

    BigDecimal calculateResidueAmount(Long businessUnitId,
                                      Long employeeId,
                                      Long documentTypeId,
                                      Long rotatoryFundId,
                                      Integer rotatoryFundCode,
                                      Date movementStartDate,
                                      Date movementEndDate,
                                      FinancesCurrency financesCurrency,
                                      String companyNumber,
                                      String cashAccountCode);
}