package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("quotaService")
@AutoCreate
public class QuotaServiceBean extends GenericServiceBean implements QuotaService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private RotatoryFundService rotatoryFundService;

    @TransactionAttribute(REQUIRES_NEW)
    public void createQuota(Quota quota)
            throws RotatoryFundApprovedException, RotatoryFundLiquidatedException,
            QuotaSumExceedsRotatoryFundAmountException, ExpirationDateBeforeStartDateException, RotatoryFundNullifiedException {
        RotatoryFund rotatoryFund = quota.getRotatoryFund();
        if (quota.getExpirationDate().compareTo(rotatoryFund.getStartDate()) < 0) {
            throw new ExpirationDateBeforeStartDateException("The expiration date of the quota can't be before rotatory fund start date");
        }

        RotatoryFund databaseRotatoryFund = listEm.find(RotatoryFund.class, rotatoryFund.getId());
        if (rotatoryFundService.canChangeRotatoryFund(rotatoryFund)) {
            BigDecimal quotaSum = allValidQuotaSum(rotatoryFund);
            BigDecimal total = BigDecimalUtil.sum(quotaSum, quota.getAmount());
            if (total.compareTo(rotatoryFund.getAmount()) > 0) {
                throw new QuotaSumExceedsRotatoryFundAmountException("The quotas amount sum exceeds the rotatory fund amount");
            }
            try {
                quota.setResidue(quota.getAmount());
                quota.setState(QuotaState.PEN);
                super.create(quota);
                rotatoryFundService.computeRotatoryFundStatistics(rotatoryFund);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("An Unexpected error has happened ", e);
            }
        }
    }

    public BigDecimal allValidQuotaSum(RotatoryFund rotatoryFund) {
        return BigDecimalUtil.sum(getQuotaSumByState(rotatoryFund, QuotaState.PEN)
                , getQuotaSumByState(rotatoryFund, QuotaState.APR)
                , getQuotaSumByState(rotatoryFund, QuotaState.LIQ)
                , getQuotaSumByState(rotatoryFund, QuotaState.PLI));
    }

    public Quota findQuota(Long id) throws QuotaNotFoundException {
        findInDataBase(id);
        Quota quota = getEntityManager().find(Quota.class, id);
        getEntityManager().refresh(quota);
        return quota;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateRotatoryFund(Quota quota)
            throws RotatoryFundLiquidatedException,
            ConcurrencyException, QuotaNotFoundException, QuotaSumExceedsRotatoryFundAmountException, ResidueCannotBeLessThanZeroException {
        RotatoryFund rotatoryFund = quota.getRotatoryFund();
        if (rotatoryFundService.isRotatoryFundLiquidated(rotatoryFund)) {
            rotatoryFundService.findRotatoryFund(quota.getId());
            getEntityManager().refresh(quota);
            throw new RotatoryFundLiquidatedException("The rotatoryFund was already liquidated, and cannot be changed");
        }
        Double quotaSum = getQuotaSumButCurrent(rotatoryFund, quota).doubleValue() + quota.getAmount().doubleValue();
        if (quotaSum > rotatoryFund.getAmount().doubleValue()) {
            getEntityManager().refresh(quota);
            throw new QuotaSumExceedsRotatoryFundAmountException("The quotas amount sum exceeds the rotatory fund amount");
        }
        Quota databaseQuota = listEm.find(Quota.class, quota.getId());
        if (databaseQuota.getAmount().compareTo(quota.getAmount()) != 0) {
            quota.setResidue(
                    BigDecimalUtil.sum(quota.getResidue(),
                            BigDecimalUtil.subtract(quota.getAmount(), databaseQuota.getAmount())
                    )
            );
        }
        if (quota.getResidue().doubleValue() < 0) {
            getEntityManager().refresh(quota);
            throw new ResidueCannotBeLessThanZeroException("The residue can not be less than zero");
        }
        if (!getEntityManager().contains(quota)) {
            getEntityManager().merge(quota);
        }
//        getEntityManager().flush();
        rotatoryFundService.computeRotatoryFundStatistics(quota.getRotatoryFund());
        //update rotatoryFund number of quotas info only if it is approved
        if (rotatoryFundService.isRotatoryFundApproved(rotatoryFund)) {
            rotatoryFundService.computeRotatoryFundStatistics(rotatoryFund);
            if (!getEntityManager().contains(quota)) {
                getEntityManager().merge(rotatoryFund);
            }
        }
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteRotatoryFund(Quota entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            QuotaNotFoundException {

        findInDataBase(entity.getId());
        RotatoryFund rotatoryFund = entity.getRotatoryFund();
        try {
            if (rotatoryFundService.canChangeRotatoryFund(rotatoryFund)) {
                try {
                    super.delete(entity);
                    rotatoryFundService.computeRotatoryFundStatistics(rotatoryFund);
                } catch (ConcurrencyException e) {
                    throw new QuotaNotFoundException(e);
                }
            }
        } catch (RotatoryFundNullifiedException e) {
            log.debug("an rotatoryFund can't be annul never");
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<Quota> getQuotaList(RotatoryFund rotatoryFund) {
        return getEntityManager()
                .createNamedQuery("Quota.findByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<Quota> getEventQuotaList(RotatoryFund rotatoryFund) {
        return listEm
                .createNamedQuery("Quota.findByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<Quota> getAllApprovedQuotaList(RotatoryFund rotatoryFund) {
        List<Quota> quotaList = new ArrayList<Quota>();
        quotaList.addAll(getQuotaListByState(rotatoryFund, QuotaState.LIQ));
        quotaList.addAll(getQuotaListByState(rotatoryFund, QuotaState.PLI));
        quotaList.addAll(getQuotaListByState(rotatoryFund, QuotaState.APR));
        return quotaList;
    }

    @SuppressWarnings(value = "unchecked")
    public List<Quota> getQuotaListByState(RotatoryFund rotatoryFund, QuotaState state) {
        return getEntityManager()
                .createNamedQuery("Quota.findByRotatoryFundByState")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", state).getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public void approvePendantQuotaList(RotatoryFund rotatoryFund) {
        getEntityManager()
                .createNamedQuery("Quota.approvePendantQuotaList")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", QuotaState.APR)
                .setParameter("quotaState", QuotaState.PEN).executeUpdate();
    }

    public Date getMaxQuotaDate(RotatoryFund rotatoryFund) {
        return (Date) getEntityManager()
                .createNamedQuery("Quota.findMaxDateByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("quotaState", QuotaState.ANL).getSingleResult();
    }

    public Date getMinQuotaDate(RotatoryFund rotatoryFund) {
        return (Date) getEntityManager()
                .createNamedQuery("Quota.findMinDateByRotatoryFund")
                .setParameter("quotaState", QuotaState.ANL)
                .setParameter("rotatoryFund", rotatoryFund).getSingleResult();
    }

    public BigDecimal getQuotaSumByState(RotatoryFund rotatoryFund, QuotaState quotaState) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("Quota.findSumByRotatoryFundByState")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("state", quotaState).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public BigDecimal getQuotaResidueSum(RotatoryFund rotatoryFund) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("Quota.findResidueSumByRotatoryFund")
                .setParameter("rotatoryFund", rotatoryFund).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    public boolean checkCurrency(RotatoryFund rotatoryFund) {
        int queryResult = ((Long) getEntityManager()
                .createNamedQuery("Quota.checkCurrency")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("currency", rotatoryFund.getPayCurrency()).getSingleResult()).intValue();
        return queryResult == 0;
    }

    public BigDecimal getQuotaSumButCurrent(RotatoryFund rotatoryFund, Quota quota) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) getEntityManager()
                .createNamedQuery("Quota.findSumByRotatoryFundButCurrent")
                .setParameter("rotatoryFund", rotatoryFund)
                .setParameter("id", quota.getId()).getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

    private Quota findInDataBase(Long id) throws QuotaNotFoundException {
        Quota quota = listEm.find(Quota.class, id);
        if (null == quota) {
            throw new QuotaNotFoundException("Cannot find the Quota entity for id=" + id);
        }

        return quota;
    }

    public boolean isQuotaInfoStillValid(RotatoryFundCollection rotatoryFundCollection) {
        Long id = rotatoryFundCollection.getQuota().getId();
        BigDecimal residue = rotatoryFundCollection.getQuotaResidue();
        Quota quota = null;
        try {
            quota = (Quota) listEm.createNamedQuery("Quota.isQuotaInfoStillValid")
                    .setParameter("id", id)
                    .setParameter("residue", residue)
                    .getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
        return quota != null;
    }

    @SuppressWarnings(value = "unchecked")
    public List<Quota> findQuotaToCollectByPayrollEmployeeAndJobCategory(Employee employee, GestionPayroll gestionPayroll) {
        /* Since the payroll is generated for a given month it is necessary set the gestionPayrollEndDate to the last day of month*/
        Date lastDayOfMonth = DateUtils.lastDayOfMonth(gestionPayroll.getMonth().getValue(), gestionPayroll.getGestion().getYear());
        List<Quota> quotaList = new ArrayList<Quota>();
        List<Quota> resultList = listEm.createNamedQuery("Quota.findQuotaToCollectByPayrollEmployeeAndJobCategory")
                .setParameter("gestionPayrollEndDate", lastDayOfMonth)
                .setParameter("rotatoryFunState", RotatoryFundState.APR)
                .setParameter("employee", employee)
                .setParameter("jobCategory", gestionPayroll.getJobCategory())
                .setParameter("quotaApprovedSate", QuotaState.APR)
                .setParameter("quotaPartiallyLiquidatedSate", QuotaState.PLI)
                .setParameter("discountByPayroll", true)
                .getResultList();
        if (resultList != null) {
            quotaList = resultList;
        }
        return quotaList;
    }

    @SuppressWarnings(value = "unchecked")
    public BigDecimal sumResidueToCollectByPayrollEmployeeAndJobCategory(Employee employee, GestionPayroll gestionPayroll) {
        /* Since the payroll is generated for a given month it is necessary set the gestionPayrollEndDate to the last day of month*/
        Date lastDayOfMonth = DateUtils.lastDayOfMonth(gestionPayroll.getMonth().getValue(), gestionPayroll.getGestion().getYear());
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal queryResult = (BigDecimal) listEm.createNamedQuery("Quota.sumResidueToCollectByPayrollEmployeeAndJobCategory")
                .setParameter("gestionPayrollEndDate", lastDayOfMonth)
                .setParameter("rotatoryFunState", RotatoryFundState.APR)
                .setParameter("employee", employee)
                .setParameter("jobCategory", gestionPayroll.getJobCategory())
                .setParameter("quotaApprovedSate", QuotaState.APR)
                .setParameter("quotaPartiallyLiquidatedSate", QuotaState.PLI)
                .setParameter("discountByPayroll", true)
                .getSingleResult();
        if (queryResult != null) {
            result = queryResult;
        }
        return result;
    }

}