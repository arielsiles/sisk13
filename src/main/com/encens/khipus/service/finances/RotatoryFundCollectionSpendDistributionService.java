package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.RotatoryFundCollection;
import com.encens.khipus.model.finances.RotatoryFundCollectionSpendDistribution;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.math.BigDecimal;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.22
 */
@Local
public interface RotatoryFundCollectionSpendDistributionService extends GenericService {
    @TransactionAttribute(REQUIRES_NEW)
    void createRotatoryFundCollectionSpendDistribution(RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution)
            throws RotatoryFundCollectionApprovedException,
            RotatoryFundNullifiedException, RotatoryFundLiquidatedException, RotatoryFundCollectionNotFoundException, RotatoryFundApprovedException, RotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredException, RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException;

    BigDecimal getPercentageRotatoryFundCollectionSpendDistributionSum(RotatoryFundCollection rotatoryFundCollection);

    RotatoryFundCollectionSpendDistribution findRotatoryFundCollectionSpendDistribution(Long id) throws EntryNotFoundException;

    void updateRotatoryFundCollection(RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution)
            throws RotatoryFundLiquidatedException, RotatoryFundNullifiedException,
            ConcurrencyException, EntryNotFoundException,
            RotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredException, RotatoryFundCollectionNotFoundException, RotatoryFundCollectionNullifiedException, RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException;

    @TransactionAttribute(REQUIRES_NEW)
    void deleteRotatoryFundCollection(RotatoryFundCollectionSpendDistribution entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            EntryNotFoundException, RotatoryFundNullifiedException, RotatoryFundCollectionNotFoundException, RotatoryFundCollectionApprovedException, RotatoryFundCollectionNullifiedException;

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFundCollectionSpendDistribution> findRotatoryFundCollectionSpendDistributions(RotatoryFundCollection rotatoryFundCollection);

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFundCollectionSpendDistribution> findDataBaseRotatoryFundCollectionSpendDistributions(RotatoryFundCollection rotatoryFundCollection);

    @SuppressWarnings(value = "unchecked")
    List<RotatoryFundCollectionSpendDistribution> getRotatoryFundCollectionSpendDistributionList(RotatoryFundCollection rotatoryFundCollection);

    BigDecimal getRotatoryFundCollectionSpendDistributionPercentageSumButCurrent(RotatoryFundCollection rotatoryFundCollection, RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution);

    BigDecimal getRotatoryFundCollectionSpendDistributionAmountSumButCurrent(RotatoryFundCollection rotatoryFundCollection, RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution);

    BigDecimal getAmountRotatoryFundCollectionSpendDistributionSum(RotatoryFundCollection rotatoryFundCollection);
}