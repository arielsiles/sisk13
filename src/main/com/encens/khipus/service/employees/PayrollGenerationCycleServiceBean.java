package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.ExchangeRate;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Stateless
@Name("payrollGenerationCycleService")
@AutoCreate
public class PayrollGenerationCycleServiceBean extends GenericServiceBean implements PayrollGenerationCycleService {

    @In(create = true)
    private EntityQuery activeJobCategoryQuery;

    @SuppressWarnings({"unchecked"})
    @TransactionAttribute(value = REQUIRES_NEW)
    public void createPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle, boolean includeActiveJobCategories)
            throws EntryDuplicatedException {
        payrollGenerationCycle.setCreationDate(new Date());
        payrollGenerationCycle.getExchangeRate().setSale(payrollGenerationCycle.getExchangeRate().getRate());
        payrollGenerationCycle.getExchangeRate().setPurchase(payrollGenerationCycle.getExchangeRate().getRate());
        try {
            getEntityManager().persist(payrollGenerationCycle);
            if (includeActiveJobCategories) {
                List<JobCategory> activeJobCategoryList = activeJobCategoryQuery.getResultList();
                for (JobCategory jobCategory : activeJobCategoryList) {
                    GestionPayroll gestionPayroll = new GestionPayroll();
                    gestionPayroll.setJobCategory(jobCategory);
                    ExchangeRate exchangeRate = new ExchangeRate();
                    applyTemplate(gestionPayroll, exchangeRate, payrollGenerationCycle);
                    if (exchangeRate.getDate() == null) {
                        exchangeRate.setDate((Calendar.getInstance().getTime()));
                    }
                    getEntityManager().persist(exchangeRate);
                    gestionPayroll.setPayrollGenerationCycle(payrollGenerationCycle);
                    gestionPayroll.setExchangeRate(exchangeRate);
                    getEntityManager().persist(gestionPayroll);
                }
            }
            getEntityManager().flush();
        } catch (PersistenceException e) {
            log.debug("Persistence error..", e);
            throw new EntryDuplicatedException();
        }
    }

    @TransactionAttribute(value = REQUIRES_NEW)
    public void updatePayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle)
            throws ConcurrencyException, EntryDuplicatedException {
        try {
            if (!getEntityManager().contains(payrollGenerationCycle)) {
                getEntityManager().merge(payrollGenerationCycle);
            }
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            throw new EntryDuplicatedException(ee);
        }
    }


    public void applyTemplate(GestionPayroll gestionPayroll, ExchangeRate exchangeRate, PayrollGenerationCycle payrollGenerationCycle) {
        gestionPayroll.setGestionPayrollType(GestionPayrollType.SALARY);
        gestionPayroll.setBusinessUnit(payrollGenerationCycle.getBusinessUnit());
        gestionPayroll.setInitDate(payrollGenerationCycle.getGenerationInitDate());
        gestionPayroll.setEndDate(payrollGenerationCycle.getGenerationEndDate());
        gestionPayroll.setGenerationBeginning(payrollGenerationCycle.getGenerationBeginning());
        gestionPayroll.setGenerationDeadline(payrollGenerationCycle.getGenerationDeadline());
        gestionPayroll.setOfficialPayrollDeadline(payrollGenerationCycle.getOfficialPayrollDeadline());

        exchangeRate.setDate(payrollGenerationCycle.getExchangeRate().getDate());
        exchangeRate.setPurchase(payrollGenerationCycle.getExchangeRate().getPurchase());
        exchangeRate.setRate(payrollGenerationCycle.getExchangeRate().getRate());
        exchangeRate.setSale(payrollGenerationCycle.getExchangeRate().getSale());

        gestionPayroll.setGestion(payrollGenerationCycle.getGestion());
        gestionPayroll.setMonth(payrollGenerationCycle.getMonth());
        gestionPayroll.setGestionPayrollType(GestionPayrollType.SALARY);
        String gestionName;
        if (null != gestionPayroll.getJobCategory()) {
            gestionName = FormatUtils.concatDashSeparated(payrollGenerationCycle.getName(),
                    gestionPayroll.getJobCategory().getFullName(),
                    DateUtils.format(gestionPayroll.getInitDate(), MessageUtils.getMessage("patterns.date")),
                    DateUtils.format(gestionPayroll.getEndDate(), MessageUtils.getMessage("patterns.date"))
            );
        } else {
            gestionName = FormatUtils.concatDashSeparated(payrollGenerationCycle.getName(),
                    DateUtils.format(gestionPayroll.getInitDate(), MessageUtils.getMessage("patterns.date")),
                    DateUtils.format(gestionPayroll.getEndDate(), MessageUtils.getMessage("patterns.date"))
            );
        }
        gestionPayroll.setGestionName(gestionName);
    }

    public Long countByName(String name) {
        try {
            return (Long) getEventEntityManager().createNamedQuery("PayrollGenerationCycle.countByName")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

    public Long countByNameButThis(String name, Long id) {
        try {
            return (Long) getEventEntityManager().createNamedQuery("PayrollGenerationCycle.countByNameButThis")
                    .setParameter("name", name)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

    public Long countByBusinessUnitAndGestionAndMonth(PayrollGenerationCycle payrollGenerationCycle) {
        try {
            return (Long) getEventEntityManager().createNamedQuery("PayrollGenerationCycle.countByBusinessUnitAndGestionAndMonth")
                    .setParameter("gestion", payrollGenerationCycle.getGestion())
                    .setParameter("month", payrollGenerationCycle.getMonth())
                    .setParameter("businessUnit", payrollGenerationCycle.getBusinessUnit())
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

    public Long countByBusinessUnitAndGestionAndMonthButThis(PayrollGenerationCycle payrollGenerationCycle) {
        try {
            return (Long) getEventEntityManager().createNamedQuery("PayrollGenerationCycle.countByBusinessUnitAndGestionAndMonthButThis")
                    .setParameter("gestion", payrollGenerationCycle.getGestion())
                    .setParameter("month", payrollGenerationCycle.getMonth())
                    .setParameter("businessUnit", payrollGenerationCycle.getBusinessUnit())
                    .setParameter("id", payrollGenerationCycle.getId())
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

    public PayrollGenerationCycle read(PayrollGenerationCycle payrollGenerationCycle) {
        try {
            payrollGenerationCycle = (PayrollGenerationCycle) getEntityManager()
                    .createNamedQuery("PayrollGenerationCycle.loadPayrollGenerationCycle")
                    .setParameter("id", payrollGenerationCycle.getId()).getSingleResult();
        } catch (NoResultException ignored) {
        }
        return payrollGenerationCycle;
    }

    public PayrollGenerationCycle getLastPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        Calendar dateTime = DateUtils.toDateCalendar(payrollGenerationCycle.getStartDate());
        dateTime.add(Calendar.MONTH, -1);
        try {
            return (PayrollGenerationCycle) getEntityManager()
                    .createNamedQuery("PayrollGenerationCycle.findByStartDate")
                    .setParameter("businessUnit", payrollGenerationCycle.getBusinessUnit())
                    .setParameter("startDate", dateTime.getTime())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Boolean isReadOnly(PayrollGenerationCycle payrollGenerationCycle) {
        Long countResult = (Long) getEventEntityManager().createNamedQuery("GestionPayroll.countGeneratedPayrollByPayrollGenerationCycle")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                .getSingleResult();
        return countResult > 0;

    }

    public Boolean hasOfficialPayroll(PayrollGenerationCycle payrollGenerationCycle, BusinessUnit businessUnit, JobCategory jobCategory) {
        Long countResult = (Long) getEventEntityManager().createNamedQuery("GestionPayroll.countGeneratedPayrollByTypeBusinessUnitAndJobCategory")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                .setParameter("businessUnit", businessUnit)
                .setParameter("jobCategory", jobCategory)
                .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                .getSingleResult();
        return countResult > 0;
    }

    public Boolean hasAllPayrollsAsOfficial(PayrollGenerationCycle payrollGenerationCycle) {
        Long countByPayrollGenerationCycle = (Long) getEventEntityManager().createNamedQuery("GestionPayroll.countByPayrollGenerationCycle")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                .getSingleResult();
        Long countGeneratedPayrollByTypeAndPayrollGenerationCycle = (Long) getEventEntityManager().createNamedQuery("GestionPayroll.countGeneratedPayrollByTypeAndPayrollGenerationCycle")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                .getSingleResult();
        return countByPayrollGenerationCycle.compareTo(countGeneratedPayrollByTypeAndPayrollGenerationCycle) == 0;
    }

    public Boolean hasTributaryPayroll(PayrollGenerationCycle payrollGenerationCycle) {
        Long countResult;
        try {
            countResult = (Long) getEventEntityManager().createNamedQuery("TributaryPayroll.countByPayrollGenerationCycle")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .getSingleResult();
        } catch (Exception e) {
            countResult = 0l;
        }
        return countResult > 0;
    }

    public Boolean hasFiscalPayroll(PayrollGenerationCycle payrollGenerationCycle) {
        Long countResult;
        try {
            countResult = (Long) getEventEntityManager().createNamedQuery("FiscalPayroll.countByPayrollGenerationCycle")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .getSingleResult();
        } catch (Exception e) {
            countResult = 0l;
        }
        return countResult > 0;
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    public Boolean hasAllPayrollsAsOfficialByGenerationType(PayrollGenerationCycle payrollGenerationCycle, List<PayrollGenerationType> payrollGenerationTypeList) {
        Long countByPayrollGenerationCycle = (Long) getEventEntityManager()
                .createNamedQuery("GestionPayroll.countByPayrollGenerationCycleByGenerationType")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                .setParameter("payrollGenerationTypeList", payrollGenerationTypeList)
                .getSingleResult();
        Long countGeneratedPayrollByTypeAndPayrollGenerationCycle = (Long) getEventEntityManager()
                .createNamedQuery("GestionPayroll.countGeneratedPayrollByGenerationType")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                .setParameter("payrollGenerationTypeList", payrollGenerationTypeList)
                .getSingleResult();
        return countByPayrollGenerationCycle.longValue() > 0 && countGeneratedPayrollByTypeAndPayrollGenerationCycle.longValue() > 0 && countByPayrollGenerationCycle.compareTo(countGeneratedPayrollByTypeAndPayrollGenerationCycle) == 0;
    }

    public Boolean hasPayrollGenerationInvestmentRegistration(PayrollGenerationCycle payrollGenerationCycle) {
        Long countResult;
        try {
            countResult = (Long) getEventEntityManager().createNamedQuery("PayrollGenerationInvestmentRegistration.countByPayrollGenerationCycle")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .getSingleResult();
        } catch (Exception e) {
            countResult = 0l;
        }
        return countResult > 0;
    }
}
