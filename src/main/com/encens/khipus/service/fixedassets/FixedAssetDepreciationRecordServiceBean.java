package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetDepreciationRecord;
import com.encens.khipus.model.fixedassets.FixedAssetGroupPk;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroupPk;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.CurrencyValuesContainer;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * FixedAssetDepreciationRecordService implementation
 *
 * @author
 * @version 2.21
 */
@Stateless
@AutoCreate
@Name("fixedAssetDepreciationRecordService")
public class FixedAssetDepreciationRecordServiceBean extends GenericServiceBean implements FixedAssetDepreciationRecordService {

    public void createFixedAssetDepreciationRecord(FixedAsset fixedAsset, Date lastDayOfCurrentProcessMonth, BigDecimal lastDayOfMonthUfvExchangeRate) {
        Calendar currentDate = Calendar.getInstance();
        FixedAssetDepreciationRecord fixedAssetDepreciationRecord = new FixedAssetDepreciationRecord();
        fixedAssetDepreciationRecord.setFixedAsset(fixedAsset);
        fixedAssetDepreciationRecord.setCurrency(FinancesCurrencyType.U);
        fixedAssetDepreciationRecord.setAcumulatedDepreciation(fixedAsset.getAcumulatedDepreciation());
        fixedAssetDepreciationRecord.setBsAccumulatedDepreciation(
                BigDecimalUtil.multiply(
                        fixedAsset.getAcumulatedDepreciation(),
                        lastDayOfMonthUfvExchangeRate
                )
        );
        fixedAssetDepreciationRecord.setCostCenterCode(fixedAsset.getCostCenterCode());
        fixedAssetDepreciationRecord.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
        fixedAssetDepreciationRecord.setDepreciation(fixedAsset.getDepreciation());
        fixedAssetDepreciationRecord.setBsDepreciation(
                BigDecimalUtil.multiply(
                        fixedAsset.getDepreciation(),
                        lastDayOfMonthUfvExchangeRate
                )
        );
        fixedAssetDepreciationRecord.setDepreciationRate(fixedAsset.getDepreciationRate());
        fixedAssetDepreciationRecord.setBusinessUnit(fixedAsset.getBusinessUnit());
        fixedAssetDepreciationRecord.setTotalValue(
                BigDecimalUtil.sum(
                        fixedAsset.getUfvOriginalValue(),
                        fixedAsset.getImprovement()
                )
        );
        Calendar lastDay = DateUtils.toDateCalendar(lastDayOfCurrentProcessMonth);
        fixedAssetDepreciationRecord.setProcessDate(lastDay.getTime());
        fixedAssetDepreciationRecord.setDepreciationDate(currentDate.getTime());
        fixedAssetDepreciationRecord.setBsUfvRate(lastDayOfMonthUfvExchangeRate);
        getEntityManager().persist(fixedAssetDepreciationRecord);
        getEntityManager().flush();
    }

    /**
     * Gets the depreciation sum for fixedAssets int he group and in the date range
     *
     * @param fixedAssetGroupId fixedAssetGroup
     * @param dateRange         The date range start
     * @return The sum of depreciation amounts
     */
    public CurrencyValuesContainer getDepreciationAmountForGroupUpTo(FixedAssetGroupPk fixedAssetGroupId, Date dateRange) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetDepreciationRecord.findDepreciationAmountForGroupUpTo");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("dateRange", dateRange);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting depreciation amount for group.");
        } catch (Exception e) {
            log.error("Error when getting depreciation amount for group.. ", e);
        }
        return (res);
    }
    /**
     * Gets the depreciation sum for a group, subgroup and date range
     * @param fixedAssetSubGroupId fixedAssetSubGroupId 
     * @param fixedAssetGroupId fixedAssetGroup
     * @param dateRange         The date range start
     * @return The sum of depreciation amounts
     */
    public CurrencyValuesContainer getDepreciationAmountForGroupAndSubGroupUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                                     FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                                     Date dateRange) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetDepreciationRecord.findDepreciationAmountForGroupAndSubGroupUpTo");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("fixedAssetSubGroupId", fixedAssetSubGroupId);
            query.setParameter("dateRange", dateRange);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting depreciation amount for group.");
        } catch (Exception e) {
            log.error("Error when getting depreciation amount for group.. ", e);
        }
        return (res);
    }

    /**
     * Gets the depreciation sum for fixedAssets in the group, subgroup and date range
     * @param fixedAssetSubGroupId fixedAssetSubGroupId
     * @param fixedAssetGroupId fixedAssetGroup
     * @param fixedAssetId fixedAssetId
     * @param dateRange         The date range start
     * @return The sum of depreciation amounts
     */
    public CurrencyValuesContainer getDepreciationAmountForFixedAssetUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                                     FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                                     Long fixedAssetId,
                                                                     Date dateRange) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetDepreciationRecord.findDepreciationAmountForFixedAssetUpTo");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("fixedAssetSubGroupId", fixedAssetSubGroupId);
            query.setParameter("dateRange", dateRange);
            query.setParameter("fixedAssetId", fixedAssetId);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting depreciation amount for group.");
        } catch (Exception e) {
            log.error("Error when getting depreciation amount for group.. ", e);
        }
        return (res);
    }

    /**
     * Get the sum of the depreciations for a FixedAsset (in Bs)
     * @param fixedAsset The fixedAsset
     * @return The sum
     */
     public Double getBsDepreciationsSum(FixedAsset fixedAsset) {
        Double res=null;
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetDepreciationRecord.getBsDepreciationsSum");
            query.setParameter("fixedAsset", fixedAsset);
            BigDecimal queryResult=(BigDecimal) query.getSingleResult();
            if(queryResult!=null){
                res = queryResult.doubleValue();
            }
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting depreciations SUM.", nrex);
        } catch (Exception e) {
            log.error("Error when getting depreciations SUM. ", e);
        }
        return (res);
    }
}
