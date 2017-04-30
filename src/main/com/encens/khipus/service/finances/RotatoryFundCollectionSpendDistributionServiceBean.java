package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.RotatoryFundCollection;
import com.encens.khipus.model.finances.RotatoryFundCollectionSpendDistribution;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.22
 */
@Stateless
@Name("rotatoryFundCollectionSpendDistributionService")
@AutoCreate
public class RotatoryFundCollectionSpendDistributionServiceBean extends GenericServiceBean implements RotatoryFundCollectionSpendDistributionService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @In
    private RotatoryFundService rotatoryFundService;
    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;


    @TransactionAttribute(REQUIRES_NEW)
    public void createRotatoryFundCollectionSpendDistribution(RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution)
            throws RotatoryFundCollectionApprovedException,
            RotatoryFundNullifiedException, RotatoryFundLiquidatedException, RotatoryFundCollectionNotFoundException, RotatoryFundApprovedException, RotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredException, RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException {
        RotatoryFundCollection rotatoryFundCollection = rotatoryFundCollectionSpendDistribution.getRotatoryFundCollection();
        RotatoryFundCollection databaseRotatoryFundCollection = listEm.find(RotatoryFundCollection.class, rotatoryFundCollection.getId());
        try {
            if (rotatoryFundCollectionService.canChangeRotatoryFundCollection(rotatoryFundCollection)) {
                Double spendDistributionSum = getAmountRotatoryFundCollectionSpendDistributionSum(rotatoryFundCollection).doubleValue();
                Double total = spendDistributionSum + rotatoryFundCollectionSpendDistribution.getAmount().doubleValue();
                if (total > rotatoryFundCollectionSpendDistribution.getRotatoryFundCollection().getSourceAmount().doubleValue()) {
                    throw new RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException("The rotatoryFundSpendDistributions amount sum exceeds total");
                }
                try {
                    super.create(rotatoryFundCollectionSpendDistribution);
                } catch (EntryDuplicatedException e) {
                    throw new RuntimeException("An Unexpected error has happened ", e);
                }
            }
        } catch (RotatoryFundCollectionNullifiedException e) {
            log.debug("an RotatoryFundCollection can't be annul never");
        }
    }

    public RotatoryFundCollectionSpendDistribution findRotatoryFundCollectionSpendDistribution(Long id) throws EntryNotFoundException {
        findInDataBase(id);
        RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution = getEntityManager().find(RotatoryFundCollectionSpendDistribution.class, id);
        getEntityManager().refresh(rotatoryFundCollectionSpendDistribution);
        return rotatoryFundCollectionSpendDistribution;
    }

    public void updateRotatoryFundCollection(RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution)
            throws RotatoryFundLiquidatedException, RotatoryFundNullifiedException,
            ConcurrencyException, EntryNotFoundException,
            RotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredException, RotatoryFundCollectionNotFoundException, RotatoryFundCollectionNullifiedException, RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException {
        RotatoryFundCollection rotatoryFundCollection = rotatoryFundCollectionSpendDistribution.getRotatoryFundCollection();
        if (rotatoryFundCollectionService.isRotatoryFundCollectionNullified(rotatoryFundCollection)) {
            rotatoryFundCollectionService.findRotatoryFundCollection(rotatoryFundCollectionSpendDistribution.getId());
            throw new RotatoryFundCollectionNullifiedException("The rotatoryFundCollection was already annulled, and cannot be changed");
        }
        Double spendDistributionSum = getRotatoryFundCollectionSpendDistributionAmountSumButCurrent(rotatoryFundCollection, rotatoryFundCollectionSpendDistribution).doubleValue();
        Double total = spendDistributionSum + rotatoryFundCollectionSpendDistribution.getAmount().doubleValue();
        if (total > rotatoryFundCollectionSpendDistribution.getRotatoryFundCollection().getSourceAmount().doubleValue()) {
            throw new RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException("The rotatoryFundSpendDistributions amount sum exceeds total");
        }
        getEntityManager().merge(rotatoryFundCollectionSpendDistribution);
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteRotatoryFundCollection(RotatoryFundCollectionSpendDistribution entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            EntryNotFoundException, RotatoryFundNullifiedException, RotatoryFundCollectionNotFoundException, RotatoryFundCollectionApprovedException, RotatoryFundCollectionNullifiedException {

        findInDataBase(entity.getId());
        RotatoryFundCollection rotatoryFundCollection = entity.getRotatoryFundCollection();
        if (rotatoryFundCollectionService.canChangeRotatoryFundCollection(rotatoryFundCollection)) {
            try {
                super.delete(entity);
            } catch (ConcurrencyException e) {
                throw new EntryNotFoundException(e);
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundCollectionSpendDistribution> findRotatoryFundCollectionSpendDistributions(RotatoryFundCollection rotatoryFundCollection) {
        List<RotatoryFundCollectionSpendDistribution> spendDistributionList = getEntityManager()
                .createNamedQuery("RotatoryFundCollectionSpendDistribution.findByRotatoryFundCollection")
                .setParameter("rotatoryFundCollection", rotatoryFundCollection)
                .getResultList();
        if (null == spendDistributionList) {
            spendDistributionList = new ArrayList<RotatoryFundCollectionSpendDistribution>();
        }
        return spendDistributionList;
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundCollectionSpendDistribution> findDataBaseRotatoryFundCollectionSpendDistributions(RotatoryFundCollection rotatoryFundCollection) {
        List<RotatoryFundCollectionSpendDistribution> spendDistributionList = listEm
                .createNamedQuery("RotatoryFundCollectionSpendDistribution.findByRotatoryFundCollection")
                .setParameter("rotatoryFundCollection", rotatoryFundCollection)
                .getResultList();
        if (null == spendDistributionList) {
            spendDistributionList = new ArrayList<RotatoryFundCollectionSpendDistribution>();
        }
        return spendDistributionList;
    }

    @SuppressWarnings(value = "unchecked")
    public List<RotatoryFundCollectionSpendDistribution> getRotatoryFundCollectionSpendDistributionList(RotatoryFundCollection rotatoryFundCollection) {
        return getEntityManager()
                .createNamedQuery("RotatoryFundCollectionSpendDistribution.findByRotatoryFundCollection")
                .setParameter("rotatoryFundCollection", rotatoryFundCollection).getResultList();
    }

    public BigDecimal getPercentageRotatoryFundCollectionSpendDistributionSum(RotatoryFundCollection rotatoryFundCollection) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundCollectionSpendDistribution.findPercentageSumByRotatoryFundCollection")
                .setParameter("rotatoryFundCollection", rotatoryFundCollection).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public BigDecimal getAmountRotatoryFundCollectionSpendDistributionSum(RotatoryFundCollection rotatoryFundCollection) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundCollectionSpendDistribution.findAmountSumByRotatoryFundCollection")
                .setParameter("rotatoryFundCollection", rotatoryFundCollection).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public BigDecimal getRotatoryFundCollectionSpendDistributionPercentageSumButCurrent(RotatoryFundCollection rotatoryFundCollection, RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundCollectionSpendDistribution.findPercentageSumByRotatoryFundCollectionButCurrent")
                .setParameter("rotatoryFundCollection", rotatoryFundCollection)
                .setParameter("id", rotatoryFundCollectionSpendDistribution.getId()).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public BigDecimal getRotatoryFundCollectionSpendDistributionAmountSumButCurrent(RotatoryFundCollection rotatoryFundCollection, RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("RotatoryFundCollectionSpendDistribution.findAmountSumByRotatoryFundCollectionButCurrent")
                .setParameter("rotatoryFundCollection", rotatoryFundCollection)
                .setParameter("id", rotatoryFundCollectionSpendDistribution.getId()).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    private RotatoryFundCollectionSpendDistribution findInDataBase(Long id) throws EntryNotFoundException {
        RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution = listEm.find(RotatoryFundCollectionSpendDistribution.class, id);
        if (null == rotatoryFundCollectionSpendDistribution) {
            throw new EntryNotFoundException("Cannot find the RotatoryFundCollectionSpendDistribution entity for id=" + id);
        }
        return rotatoryFundCollectionSpendDistribution;
    }
}