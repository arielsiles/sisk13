package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.finances.Quota;
import com.encens.khipus.model.finances.QuotaState;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.finances.RotatoryFundCollection;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Local
public interface QuotaService extends GenericService {
    @SuppressWarnings(value = "unchecked")
    List<Quota> getQuotaList(RotatoryFund rotatoryFund);

    void createQuota(Quota quota)
            throws RotatoryFundApprovedException, RotatoryFundLiquidatedException,
            QuotaSumExceedsRotatoryFundAmountException, ExpirationDateBeforeStartDateException, RotatoryFundNullifiedException;

    Quota findQuota(Long id) throws QuotaNotFoundException;

    void updateRotatoryFund(Quota quota)
            throws RotatoryFundLiquidatedException,
            ConcurrencyException, QuotaNotFoundException, QuotaSumExceedsRotatoryFundAmountException, ResidueCannotBeLessThanZeroException;

    void deleteRotatoryFund(Quota entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            QuotaNotFoundException;

    Date getMaxQuotaDate(RotatoryFund rotatoryFund);


    BigDecimal getQuotaSumButCurrent(RotatoryFund rotatoryFund, Quota quota);

    @SuppressWarnings(value = "unchecked")
    void approvePendantQuotaList(RotatoryFund rotatoryFund);

    @SuppressWarnings(value = "unchecked")
    boolean isQuotaInfoStillValid(RotatoryFundCollection rotatoryFundCollection);

    boolean checkCurrency(RotatoryFund rotatoryFund);

    BigDecimal getQuotaResidueSum(RotatoryFund rotatoryFund);

    @SuppressWarnings(value = "unchecked")
    BigDecimal sumResidueToCollectByPayrollEmployeeAndJobCategory(Employee employee, GestionPayroll gestionPayroll);

    @SuppressWarnings(value = "unchecked")
    List<Quota> findQuotaToCollectByPayrollEmployeeAndJobCategory(Employee employee, GestionPayroll gestionPayroll);

    Date getMinQuotaDate(RotatoryFund rotatoryFund);

    BigDecimal getQuotaSumByState(RotatoryFund rotatoryFund, QuotaState quotaState);

    BigDecimal allValidQuotaSum(RotatoryFund rotatoryFund);

    @SuppressWarnings(value = "unchecked")
    List<Quota> getQuotaListByState(RotatoryFund rotatoryFund, QuotaState state);

    @SuppressWarnings(value = "unchecked")
    List<Quota> getAllApprovedQuotaList(RotatoryFund rotatoryFund);

    @SuppressWarnings(value = "unchecked")
    List<Quota> getEventQuotaList(RotatoryFund rotatoryFund);
}