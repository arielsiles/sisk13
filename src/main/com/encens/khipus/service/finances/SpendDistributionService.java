package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.RotatoryFundLiquidatedException;
import com.encens.khipus.exception.finances.RotatoryFundNullifiedException;
import com.encens.khipus.exception.finances.SpendDistributionPercentageSumExceedsOneHundredException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.finances.SpendDistribution;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.math.BigDecimal;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.21
 */
@Local
public interface SpendDistributionService extends GenericService {

    List<SpendDistribution> findSpendDistributions(RotatoryFund rotatoryFund);

    List<SpendDistribution> findDataBaseSpendDistributions(RotatoryFund rotatoryFund);

    BigDecimal getPercentageSpendDistributionSum(RotatoryFund rotatoryFund);

    void createSpendDistribution(SpendDistribution spendDistribution)
            throws RotatoryFundLiquidatedException,
            SpendDistributionPercentageSumExceedsOneHundredException, RotatoryFundNullifiedException;

    SpendDistribution findSpendDistribution(Long id) throws EntryNotFoundException;

    BigDecimal getSpendDistributionPercentageSumButCurrent(RotatoryFund rotatoryFund, SpendDistribution spendDistribution);

    void updateRotatoryFund(SpendDistribution spendDistribution)
            throws RotatoryFundLiquidatedException, RotatoryFundNullifiedException,
            ConcurrencyException, EntryNotFoundException,
            SpendDistributionPercentageSumExceedsOneHundredException;

    @TransactionAttribute(REQUIRES_NEW)
    void deleteRotatoryFund(SpendDistribution entity)
            throws RotatoryFundLiquidatedException,
            ReferentialIntegrityException,
            EntryNotFoundException, RotatoryFundNullifiedException;

    List<SpendDistribution> getSpendDistributionList(RotatoryFund rotatoryFund);

    String findRotatoryFundCashAccountList(RotatoryFund rotatoryFund);

    List<CostCenter> getCostCenterListBySpendDistribution(RotatoryFund rotatoryFund);

    List<CashAccount> getCashAccountListBySpendDistribution(RotatoryFund rotatoryFund);
}