package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.ExchangeRate;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

/**
 * GestionPayrollServiceBean
 *
 * @author
 * @version 2.26
 */
@Stateless
@Name("gestionPayrollService")
@AutoCreate
public class GestionPayrollServiceBean extends GenericServiceBean implements GestionPayrollService {

    @In("#{entityManager}")
    private EntityManager em;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @Logger
    private Log log;

    public GestionPayroll getAllGestionPayroll() {
        try {
            Query query = em.createNamedQuery("GestionPayroll.findAll");
            return (GestionPayroll) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public GestionPayroll findGestionPayrollById(Long id) {
        try {
            Query query = em.createNamedQuery("GestionPayroll.findById");
            query.setParameter("id", id);
            return (GestionPayroll) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public GestionPayroll findGestionPayrollById(Long id, EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        try {
            Query query = entityManager.createNamedQuery("GestionPayroll.findById");
            query.setParameter("id", id);
            return (GestionPayroll) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<GestionPayroll> filterGestionPayroll(Date initDate, Date endDate, Sector sector) {
        List<GestionPayroll> gestionPayrollList = new ArrayList<GestionPayroll>();
        try {
            if (sector != null) {
                if (initDate == null) {
                    if (endDate == null) {
                        Query query = em.createNamedQuery("GestionPayroll.findBySector");
                        query.setParameter("sector", sector);
                        gestionPayrollList = query.getResultList();
                        return gestionPayrollList;
                    }
                    Query query = em.createNamedQuery("GestionPayroll.findByEndRangeSector");
                    query.setParameter("endDate", endDate);
                    query.setParameter("sector", sector);
                    gestionPayrollList = query.getResultList();
                    return gestionPayrollList;
                } else {
                    if (endDate == null) {
                        Query query = em.createNamedQuery("GestionPayroll.findByInitRangeSector");
                        query.setParameter("initDate", initDate);
                        query.setParameter("sector", sector);
                        gestionPayrollList = query.getResultList();
                        return gestionPayrollList;
                    }
                    Query query = em.createNamedQuery("GestionPayroll.findByInitEndRangeSector");
                    query.setParameter("initDate", initDate);
                    query.setParameter("endDate", endDate);
                    query.setParameter("sector", sector);
                    gestionPayrollList = query.getResultList();
                    return gestionPayrollList;
                }
            } else {/*NULL sector*/
                if (initDate == null) {
                    if (endDate == null) {
                        Query query = em.createNamedQuery("GestionPayroll.findAll");
                        gestionPayrollList = query.getResultList();
                        return gestionPayrollList;
                    }
                    Query query = em.createNamedQuery("GestionPayroll.findByEndRange");
                    query.setParameter("endDate", endDate);
                    gestionPayrollList = query.getResultList();
                    return gestionPayrollList;
                } else {
                    if (endDate == null) {
                        Query query = em.createNamedQuery("GestionPayroll.findByInitRange");
                        query.setParameter("initDate", initDate);
                        gestionPayrollList = query.getResultList();
                        return gestionPayrollList;
                    }
                    Query query = em.createNamedQuery("GestionPayroll.findByInitEndRange");
                    query.setParameter("initDate", initDate);
                    query.setParameter("endDate", endDate);
                    gestionPayrollList = query.getResultList();
                    return gestionPayrollList;
                }
            }

        } catch (NoResultException e) {
            return gestionPayrollList;
        }
    }

    public List<GestionPayroll> findValidGestionPayrolls(Gestion gestion, Month month) {
        if (gestion != null && month != null) {
            try {
                return em.createNamedQuery("GestionPayroll.findValidGestionPayrolls")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getResultList();
            } catch (Exception e) {
            }
        }
        return new ArrayList<GestionPayroll>();
    }

    public List<ExchangeRate> findExchangeRateFromGestionPayroll(Gestion gestion, Month month) {
        List<ExchangeRate> exchangeRateList = new ArrayList<ExchangeRate>();
        if (gestion != null && month != null) {
            try {
                exchangeRateList = em.createNamedQuery("GestionPayroll.findExchangeRateFromGestionPayroll")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month).getResultList();
                Map<BigDecimal, ExchangeRate> rateMap = new HashMap<BigDecimal, ExchangeRate>();
                for (ExchangeRate exchangeRate : exchangeRateList) {
                    if (!rateMap.containsKey(exchangeRate.getRate())) {
                        rateMap.put(exchangeRate.getRate(), exchangeRate);
                    }
                }

                exchangeRateList = new ArrayList(rateMap.values());
            } catch (Exception e) {
            }
        }
        return exchangeRateList;
    }

    public Long countValidGestionPayrolls(Gestion gestion, Month month) {
        if (gestion != null && month != null) {
            try {
                return (Long) em.createNamedQuery("GestionPayroll.countValidGestionPayrolls")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getSingleResult();
            } catch (Exception e) {
            }
        }
        return (long) 0;
    }

    public List<GestionPayroll> findGestionPayrolls(Gestion gestion, Month month) {
        if (gestion != null && month != null) {
            try {
                return em.createNamedQuery("GestionPayroll.findGestionPayrolls")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month).getResultList();
            } catch (Exception e) {
            }
        }
        return new ArrayList<GestionPayroll>();
    }

    @SuppressWarnings(value = "unchecked")
    public List<GestionPayroll> findGestionPayrollByGestionAndBusinessUnitAndMonth(Gestion gestion, Month month,
                                                                                   BusinessUnit businessUnit,
                                                                                   EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        return entityManager.createNamedQuery("GestionPayroll.findGestionPayrollByGestionAndBusinessUnitAndMonth")
                .setParameter("gestion", gestion)
                .setParameter("month", month)
                .setParameter("businessUnit", businessUnit).getResultList();
    }

    public GestionPayroll findGestionPayrollByGestionAndBusinessUnitAndMonthAndJobCategory(Gestion gestion, Month month,
                                                                                           BusinessUnit businessUnit,
                                                                                           JobCategory jobCategory,
                                                                                           EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        try {
            return (GestionPayroll) entityManager.createNamedQuery("GestionPayroll.findGestionPayrollByGestionAndBusinessUnitAndMonthAndJobCategory")
                    .setParameter("gestion", gestion)
                    .setParameter("month", month)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Counts how many payrolls there are given a set of filters
     *
     * @param gestion               a given gestion
     * @param businessUnit          a given businessUnit
     * @param jobCategory           a given jobCategory
     * @param gestionPayrollType    a given gestionPayrollType
     * @param useEventEntityManager true if the search will be by event
     * @return how many payrolls there are given a set of filters
     */
    public Long countByGestionAndBusinessUnitAndJobCategoryAndType(Gestion gestion,
                                                                   BusinessUnit businessUnit,
                                                                   JobCategory jobCategory,
                                                                   GestionPayrollType gestionPayrollType,
                                                                   boolean useEventEntityManager) {
        EntityManager entityManager;
        if (useEventEntityManager) {
            entityManager = listEm;
        } else {
            entityManager = getEntityManager();
        }

        try {
            return (Long) entityManager.createNamedQuery("GestionPayroll.countByGestionAndBusinessUnitAndJobCategoryAndType")
                    .setParameter("gestion", gestion)
                    .setParameter("gestionPayrollType", gestionPayrollType)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory).getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

    /**
     * Counts how many payrolls there are given a set of filters
     *
     * @param gestion               a given gestion
     * @param businessUnit          a given businessUnit
     * @param jobCategory           a given jobCategory
     * @param gestionPayrollType    a given gestionPayrollType
     * @param idList                a list of id to filter for exclude purpose
     * @param useEventEntityManager true if the search will be by event
     * @return how many payrolls there are given a set of filters
     */
    public Long countByGestionAndBusinessUnitAndJobCategoryAndTypeNotInIdList(Gestion gestion,
                                                                              BusinessUnit businessUnit,
                                                                              JobCategory jobCategory,
                                                                              GestionPayrollType gestionPayrollType,
                                                                              List<Long> idList,
                                                                              boolean useEventEntityManager) {
        EntityManager entityManager = useEventEntityManager ? listEm : getEntityManager();
        try {
            return (Long) entityManager.createNamedQuery("GestionPayroll.countByGestionAndBusinessUnitAndJobCategoryAndTypeNotInIdList")
                    .setParameter("gestion", gestion)
                    .setParameter("gestionPayrollType", gestionPayrollType)
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("idList", idList)
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

    /**
     * Counts how many payrolls there are given a set of filters
     *
     * @param businessUnit          a given businessUnit
     * @param gestion               a given gestion
     * @param month                 a given month
     * @param jobCategory           a given jobCategory
     * @param gestionPayrollType    a given gestionPayrollType
     * @param idList                a list of id to filter for exclude purpose
     * @param useEventEntityManager true if the search will be by event
     * @return how many payrolls there are given a set of filters
     */
    public Long countByBusinessUnitAndGestionAndMonthAndJobCategoryAndTypeAndNotInList(
            BusinessUnit businessUnit,
            Gestion gestion,
            Month month,
            JobCategory jobCategory,
            GestionPayrollType gestionPayrollType,
            List<Long> idList,
            boolean useEventEntityManager) {
        EntityManager entityManager = useEventEntityManager ? listEm : getEntityManager();
        try {
            return (Long) entityManager.createNamedQuery("GestionPayroll.countByBusinessUnitAndGestionAndMonthAndJobCategoryAndTypeAndNotInList")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("gestion", gestion)
                    .setParameter("month", month)
                    .setParameter("gestionPayrollType", gestionPayrollType)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("idList", idList)
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

    /**
     * Counts how many payrolls there are given a set of filters
     *
     * @param businessUnit          a given businessUnit
     * @param gestion               a given gestion
     * @param month                 a given month
     * @param jobCategory           a given jobCategory
     * @param gestionPayrollType    a given gestionPayrollType
     * @param useEventEntityManager true if the search will be by event
     * @return how many payrolls there are given a set of filters
     */
    public Long countByBusinessUnitAndGestionAndMonthAndJobCategoryAndType(
            BusinessUnit businessUnit,
            Gestion gestion,
            Month month,
            JobCategory jobCategory,
            GestionPayrollType gestionPayrollType,
            boolean useEventEntityManager) {
        EntityManager entityManager = useEventEntityManager ? listEm : getEntityManager();
        try {
            return (Long) entityManager.createNamedQuery("GestionPayroll.countByBusinessUnitAndGestionAndMonthAndJobCategoryAndType")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("gestion", gestion)
                    .setParameter("month", month)
                    .setParameter("gestionPayrollType", gestionPayrollType)
                    .setParameter("jobCategory", jobCategory)
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }


    public Long countGestionPayrolls(Gestion gestion, Month month) {
        if (gestion != null && month != null) {
            try {
                return (Long) em.createNamedQuery("GestionPayroll.countGestionPayrolls")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month).getSingleResult();
            } catch (Exception e) {
            }
        }
        return (long) 0;
    }


    public Boolean hasOfficialGeneration(GestionPayroll gestionPayroll) {
        if (gestionPayroll != null) {
            try {
                return ((Long) em.createNamedQuery("GestionPayroll.countByGeneratedPayrollType")
                        .setParameter("gestionPayroll", gestionPayroll)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getSingleResult()).longValue() > 0;
            } catch (Exception e) {
            }
        }
        return false;
    }

    public BigDecimal sumLiquidForBankAccount(Gestion gestion, Month month, Long currencyId) {
        BigDecimal result = BigDecimal.ZERO;
        if (gestion != null && month != null) {
            try {
                BigDecimal sumManagersLiquidForBankAccount = (BigDecimal) em.createNamedQuery("GestionPayroll.sumManagersLiquidForBankAccount")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                        .setParameter("paymentType", PaymentType.PAYMENT_BANK_ACCOUNT)
                        .setParameter("defaultAccount", Boolean.TRUE)
                        .setParameter("currencyId", currencyId).getSingleResult();
                BigDecimal sumProffesorsLiquidForBankAccount = (BigDecimal) em.createNamedQuery("GestionPayroll.sumProffesorsLiquidForBankAccount")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                        .setParameter("paymentType", PaymentType.PAYMENT_BANK_ACCOUNT)
                        .setParameter("defaultAccount", Boolean.TRUE)
                        .setParameter("currencyId", currencyId).getSingleResult();

                sumManagersLiquidForBankAccount = sumManagersLiquidForBankAccount == null ? BigDecimal.ZERO : sumManagersLiquidForBankAccount;
                sumProffesorsLiquidForBankAccount = sumProffesorsLiquidForBankAccount == null ? BigDecimal.ZERO : sumProffesorsLiquidForBankAccount;
                result = BigDecimalUtil.sum(sumManagersLiquidForBankAccount, sumProffesorsLiquidForBankAccount);

            } catch (Exception e) {
            }
        }
        return result;
    }

    public BigDecimal sumLiquidForCheck(Gestion gestion, Month month) {
        BigDecimal result = BigDecimal.ZERO;
        if (gestion != null && month != null) {
            try {
                BigDecimal sumManagersLiquidForCheck = (BigDecimal) em.createNamedQuery("GestionPayroll.sumManagersLiquidForCheck")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                        .setParameter("paymentType", PaymentType.PAYMENT_WITH_CHECK)
                        .setParameter("defaultAccount", Boolean.TRUE).getSingleResult();
                BigDecimal sumProffesorsLiquidForCheck = (BigDecimal) em.createNamedQuery("GestionPayroll.sumProffesorsLiquidForCheck")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                        .setParameter("paymentType", PaymentType.PAYMENT_WITH_CHECK)
                        .setParameter("defaultAccount", Boolean.TRUE).getSingleResult();

                sumManagersLiquidForCheck = sumManagersLiquidForCheck == null ? BigDecimal.ZERO : sumManagersLiquidForCheck;
                sumProffesorsLiquidForCheck = sumProffesorsLiquidForCheck == null ? BigDecimal.ZERO : sumProffesorsLiquidForCheck;
                result = BigDecimalUtil.sum(sumManagersLiquidForCheck, sumProffesorsLiquidForCheck);

            } catch (Exception e) {
            }
        }
        return result;
    }

    public BigDecimal sumLiquidForCheck(Gestion gestion, Month month, Long currencyId) {
        BigDecimal result = BigDecimal.ZERO;
        if (gestion != null && month != null) {
            try {
                List<Object[]> managersLiquidForCheck = em.createNamedQuery("GestionPayroll.findManagersLiquidForCheck")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                        .setParameter("paymentType", PaymentType.PAYMENT_WITH_CHECK)
                        .setParameter("defaultAccount", Boolean.TRUE)
                        .setParameter("currencyId", currencyId).getResultList();

                if (!ValidatorUtil.isEmptyOrNull(managersLiquidForCheck)) {
                    for (Object[] obj : managersLiquidForCheck) {
                        result = BigDecimalUtil.sum(result, (BigDecimal) obj[0]);
                    }
                }

                List<Object[]> proffesorsLiquidForCheck = em.createNamedQuery("GestionPayroll.findProffesorsLiquidForCheck")
                        .setParameter("gestion", gestion)
                        .setParameter("month", month)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                        .setParameter("paymentType", PaymentType.PAYMENT_WITH_CHECK)
                        .setParameter("defaultAccount", Boolean.TRUE)
                        .setParameter("currencyId", currencyId).getResultList();

                if (!ValidatorUtil.isEmptyOrNull(proffesorsLiquidForCheck)) {
                    for (Object[] obj : proffesorsLiquidForCheck) {
                        result = BigDecimalUtil.sum(result, (BigDecimal) obj[0]);
                    }
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    public List<ManagersPayroll> getManagersPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion, Month month) {
        System.out.println("public List<GeneralPayroll> getManagersPayrollGeneration(businessUnit=" + businessUnit + " jobCategory=" + jobCategory + " gestion=" + gestion + " month=" + month + ")");
        if (businessUnit != null && jobCategory != null && gestion != null && month != null
                && jobCategory.getActive() && PayrollGenerationType.GENERATION_BY_SALARY.equals(jobCategory.getPayrollGenerationType())) {
            return em.createNamedQuery("ManagersPayroll.findByPayrollGenerationParameters")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("gestion", gestion)
                    .setParameter("month", month)
                    .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getResultList();
        }
        return new ArrayList<ManagersPayroll>();
    }

    public List<GeneralPayroll> getProfessorsPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion, Month month) {
        System.out.println("public List<GeneralPayroll> getProfessorsPayrollGeneration(businessUnit=" + businessUnit + " jobCategory=" + jobCategory + " gestion=" + gestion + " month=" + month + ")");
        if (businessUnit != null && jobCategory != null && gestion != null && month != null
                && jobCategory.getActive() && PayrollGenerationType.GENERATION_BY_TIME.equals(jobCategory.getPayrollGenerationType())) {
            return em.createNamedQuery("GeneralPayroll.findByPayrollGenerationParameters")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("gestion", gestion)
                    .setParameter("month", month)
                    .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getResultList();
        }
        return new ArrayList<GeneralPayroll>();
    }

    public List<ChristmasPayroll> getChristmasPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion) {
        if (businessUnit != null && jobCategory != null && gestion != null && jobCategory.getActive()) {
            return em.createNamedQuery("ChristmasPayroll.findByPayrollGenerationParameters")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("gestion", gestion)
                    .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getResultList();
        }
        return new ArrayList<ChristmasPayroll>();
    }

    public List<FiscalProfessorPayroll> getFiscalProfessorPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion) {
        if (businessUnit != null && jobCategory != null && gestion != null && jobCategory.getActive()) {
            return em.createNamedQuery("FiscalProfessorPayroll.findByPayrollGenerationParameters")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("gestion", gestion)
                    .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getResultList();
        }
        return new ArrayList<FiscalProfessorPayroll>();
    }

    public GeneratedPayroll findOfficialGeneratedPayroll(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion, GestionPayrollType type, Month month) {
        GeneratedPayroll generatedPayroll = null;
        try {
            if (GestionPayrollType.SALARY.equals(type)) {
                if (PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(jobCategory.getPayrollGenerationType())) {
                    generatedPayroll = (GeneratedPayroll) em.createNamedQuery("FiscalProfessorPayroll.findOfficialGeneratedPayroll")
                            .setParameter("businessUnit", businessUnit)
                            .setParameter("jobCategory", jobCategory)
                            .setParameter("gestion", gestion)
                            .setParameter("month", month)
                            .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getSingleResult();
                } else if (PayrollGenerationType.GENERATION_BY_SALARY.equals(jobCategory.getPayrollGenerationType())) {
                    generatedPayroll = (GeneratedPayroll) em.createNamedQuery("ManagersPayroll.findOfficialGeneratedPayroll")
                            .setParameter("businessUnit", businessUnit)
                            .setParameter("jobCategory", jobCategory)
                            .setParameter("gestion", gestion)
                            .setParameter("month", month)
                            .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getSingleResult();
                } else if (PayrollGenerationType.GENERATION_BY_TIME.equals(jobCategory.getPayrollGenerationType())) {
                    generatedPayroll = (GeneratedPayroll) em.createNamedQuery("GeneralPayroll.findOfficialGeneratedPayroll")
                            .setParameter("businessUnit", businessUnit)
                            .setParameter("jobCategory", jobCategory)
                            .setParameter("gestion", gestion)
                            .setParameter("month", month)
                            .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getSingleResult();
                }
            } else if (GestionPayrollType.CHRISTMAS_BONUS.equals(type)) {
                generatedPayroll = (GeneratedPayroll) em.createNamedQuery("ChristmasPayroll.findOfficialGeneratedPayroll")
                        .setParameter("businessUnit", businessUnit)
                        .setParameter("jobCategory", jobCategory)
                        .setParameter("gestion", gestion)
                        .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getSingleResult();
            }
        } catch (NoResultException e) {
        }
        return generatedPayroll;
    }

    /**
     * @return The GestionPayroll which generationBeginning and officialPayrollDeadline dates catches the actual date
     */
    public GestionPayroll findAvailableGestionPayroll() {
        try {
            Date today = new Date();
            return (GestionPayroll) listEm.createNamedQuery("GestionPayroll.findAvailableGestionPayroll")
                    .setParameter("date", today)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public String getNextGeneratedPayrollName(GestionPayroll gestionPayroll) {
        Long countGeneratedPayrolls = (Long) listEm.createNamedQuery("GestionPayroll.countGeneratedPayrolls")
                .setParameter("gestionPayroll", gestionPayroll)
                .getSingleResult();
        if (countGeneratedPayrolls == null) {
            countGeneratedPayrolls = (long) 0;
        }
        return gestionPayroll.getGestionName() + " " + FormatUtils.beforeFillingWithZeros(String.valueOf(countGeneratedPayrolls + 1), 3);
    }

    public Boolean hasValidGenerationDateRange(GestionPayroll gestionPayroll) {

        if (gestionPayroll == null || gestionPayroll.getId() == null) {
            return false;
        }

        GestionPayroll gestionPayrollDB = listEm.find(GestionPayroll.class, gestionPayroll.getId());
        return gestionPayrollDB != null && (gestionPayrollDB.getGenerationBeginning() == null || gestionPayrollDB.getGenerationBeginning().compareTo(DateUtils.removeTime(new Date())) <= 0) &&
                (gestionPayrollDB.getGenerationDeadline() == null || gestionPayrollDB.getGenerationDeadline().compareTo(DateUtils.removeTime(new Date())) >= 0);

    }

    public Boolean hasValidOfficialPayrollDeadline(GestionPayroll gestionPayroll) {

        if (gestionPayroll == null || gestionPayroll.getId() == null) {
            return false;
        }

        GestionPayroll gestionPayrollDB = listEm.find(GestionPayroll.class, gestionPayroll.getId());
        return gestionPayrollDB != null && (gestionPayrollDB.getOfficialPayrollDeadline() == null || gestionPayrollDB.getOfficialPayrollDeadline().compareTo(DateUtils.removeTime(new Date())) >= 0);

    }
}