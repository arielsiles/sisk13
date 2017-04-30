package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.RotatoryFundLiquidatedException;
import com.encens.khipus.exception.finances.RotatoryFundNullifiedException;
import com.encens.khipus.exception.finances.SpendDistributionPercentageSumExceedsOneHundredException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.finances.SpendDistribution;
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
 * @version 2.21
 */
@Stateless
@Name("spendDistributionService")
@AutoCreate
public class SpendDistributionServiceBean extends GenericServiceBean implements SpendDistributionService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @In
    private RotatoryFundService rotatoryFundService;

    @TransactionAttribute(REQUIRES_NEW)
    public void createSpendDistribution(SpendDistribution spendDistribution)
            throws RotatoryFundLiquidatedException,
            RotatoryFundNullifiedException,
            SpendDistributionPercentageSumExceedsOneHundredException {
        RotatoryFund rotatoryFund = spendDistribution.getRotatoryFund();
        if (rotatoryFundService.isRotatoryFundLiquidated(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(spendDistribution.getId());
            throw new RotatoryFundLiquidatedException("The rotatoryFund was already liquidated, and cannot be changed");
        }
        if (rotatoryFundService.isRotatoryFundNullified(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(spendDistribution.getId());
            throw new RotatoryFundNullifiedException("The rotatoryFund was already annulled, and cannot be changed");
        }

        try {
            super.create(spendDistribution);
        } catch (EntryDuplicatedException e) {
            throw new RuntimeException("An Unexpected error has happened ", e);
        }

    }

    public SpendDistribution findSpendDistribution(Long id) throws EntryNotFoundException {
        findInDataBase(id);
        SpendDistribution spendDistribution = getEntityManager().find(SpendDistribution.class, id);
        getEntityManager().refresh(spendDistribution);
        return spendDistribution;
    }

    public void updateRotatoryFund(SpendDistribution spendDistribution)
            throws RotatoryFundLiquidatedException, RotatoryFundNullifiedException,
            ConcurrencyException, EntryNotFoundException,
            SpendDistributionPercentageSumExceedsOneHundredException {

        RotatoryFund rotatoryFund = spendDistribution.getRotatoryFund();
        if (rotatoryFundService.isRotatoryFundLiquidated(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(spendDistribution.getId());
            throw new RotatoryFundLiquidatedException("The rotatoryFund was already liquidated, and cannot be changed");
        }
        if (rotatoryFundService.isRotatoryFundNullified(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(spendDistribution.getId());
            throw new RotatoryFundNullifiedException("The rotatoryFund was already annulled, and cannot be changed");
        }
/*
        Double spendDistributionSum = getSpendDistributionPercentageSumButCurrent(rotatoryFund, spendDistribution).doubleValue() + spendDistribution.getPercentage().doubleValue();
        if (spendDistributionSum > BigDecimalUtil.ONE_HUNDRED.doubleValue()) {
            throw new SpendDistributionPercentageSumExceedsOneHundredException("The spendDistributions percentage sum exceeds one hundred percent");
        }
*/
        getEntityManager().merge(spendDistribution);
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteRotatoryFund(SpendDistribution entity)
            throws RotatoryFundLiquidatedException,
            ReferentialIntegrityException,
            EntryNotFoundException,
            RotatoryFundNullifiedException {

        findInDataBase(entity.getId());
        RotatoryFund rotatoryFund = entity.getRotatoryFund();
        if (rotatoryFundService.isRotatoryFundLiquidated(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(entity.getId());
            throw new RotatoryFundLiquidatedException("The rotatoryFund was already liquidated, and cannot be changed");
        }
        if (rotatoryFundService.isRotatoryFundNullified(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(entity.getId());
            throw new RotatoryFundNullifiedException("The rotatoryFund was already annulled, and cannot be changed");
        }
        try {
            super.delete(entity);
        } catch (ConcurrencyException e) {
            throw new EntryNotFoundException(e);
        }

    }

    @SuppressWarnings(value = "unchecked")
    public List<SpendDistribution> findSpendDistributions(RotatoryFund rotatoryFund) {
        List<SpendDistribution> spendDistributionList = getEntityManager()
                .createNamedQuery("SpendDistribution.findByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .getResultList();
        if (null == spendDistributionList) {
            spendDistributionList = new ArrayList<SpendDistribution>();
        }
        return spendDistributionList;
    }

    @SuppressWarnings(value = "unchecked")
    public List<SpendDistribution> findDataBaseSpendDistributions(RotatoryFund rotatoryFund) {
        List<SpendDistribution> spendDistributionList = listEm
                .createNamedQuery("SpendDistribution.findByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .getResultList();
        if (null == spendDistributionList) {
            spendDistributionList = new ArrayList<SpendDistribution>();
        }
        return spendDistributionList;
    }

    @SuppressWarnings(value = "unchecked")
    public List<SpendDistribution> getSpendDistributionList(RotatoryFund rotatoryFund) {
        return getEntityManager()
                .createNamedQuery("SpendDistribution.findByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<CostCenter> getCostCenterListBySpendDistribution(RotatoryFund rotatoryFund) {
        return getEntityManager()
                .createNamedQuery("SpendDistribution.findCostCenterByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<CashAccount> getCashAccountListBySpendDistribution(RotatoryFund rotatoryFund) {
        return getEntityManager()
                .createNamedQuery("SpendDistribution.findCashAccountByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getResultList();
    }


    public BigDecimal getPercentageSpendDistributionSum(RotatoryFund rotatoryFund) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("SpendDistribution.findPercentageSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public BigDecimal getSpendDistributionPercentageSumButCurrent(RotatoryFund rotatoryFund, SpendDistribution spendDistribution) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("SpendDistribution.findPercentageSumByRotatoryFundButCurrent")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("id", spendDistribution.getId()).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    private SpendDistribution findInDataBase(Long id) throws EntryNotFoundException {
        SpendDistribution spendDistribution = listEm.find(SpendDistribution.class, id);
        if (null == spendDistribution) {
            throw new EntryNotFoundException("Cannot find the SpendDistribution entity for id=" + id);
        }
        return spendDistribution;
    }

    @SuppressWarnings(value = "unchecked")
    public String findRotatoryFundCashAccountList(RotatoryFund rotatoryFund) {
        List<SpendDistribution> spendDistributionList = new ArrayList<SpendDistribution>();
        List<SpendDistribution> resultList = listEm.createNamedQuery("SpendDistribution.findByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .getResultList();
        if (resultList != null) {
            spendDistributionList = resultList;
        }
        String accounts = "'";
        for (SpendDistribution spendDistribution : spendDistributionList) {
            accounts += spendDistribution.getCashAccount().getAccountCode() + "',";
        }
        /*delete last comma*/
        if (accounts.length() > 1) {
            accounts = accounts.substring(0, accounts.length() - 1);
        }
        return accounts;
    }

}