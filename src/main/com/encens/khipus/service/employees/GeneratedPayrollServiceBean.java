package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.GestionPayrollOfficialPayrollDeadlineException;
import com.encens.khipus.exception.employees.PayrollSelectItemsEmptyException;
import com.encens.khipus.exception.employees.PayrollSelectItemsHasAccountingRecordException;
import com.encens.khipus.exception.employees.UpdateActivePaymentException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.finances.QuotaService;
import com.encens.khipus.service.finances.RotatoryFundCollectionService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.*;
import com.encens.khipus.util.employees.PayrollGenerationResult;
import com.encens.khipus.util.employees.payroll.fiscal.FiscalPayrollGenerator;
import com.encens.khipus.util.employees.payroll.tributary.TributaryPayrollGenerator;
import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.*;
import javax.transaction.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation of GeneratedPayrollService
 *
 * @author
 * @version 3.4
 */
@Name("generatedPayrollService")
@Stateless
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class GeneratedPayrollServiceBean implements GeneratedPayrollService {

    private static final int DAYS_OF_YEAR = 365;
    private static final int SCALE = 2;

    @Resource
    private UserTransaction userTransaction;

    @PersistenceContext(unitName = "khipus")
    private EntityManager em;

    @In
    private RetentionValidatorService retentionValidatorService;
    @In
    private GestionPayrollService gestionPayrollService;
    @In
    private GenericService genericService;
    @In
    private SpecialDateService specialDateService;
    @In
    private EmployeeService employeeService;
    @In
    private ContractService contractService;
    @In
    private HoraryBandContractService horaryBandContractService;
    @In
    private GeneratedPayrollService generatedPayrollService;
    @In
    private RHMarkService rhMarkService;
    @In
    private SalaryMovementService salaryMovementService;
    @In
    private QuotaService quotaService;
    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;
    @In
    private GestionService gestionService;
    @In
    private ManagersPayrollService managersPayrollService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private PayrollReportService payrollReportService;
    @In
    private TributaryPayrollGeneratorService tributaryPayrollGeneratorService;
    @In
    private InvoicesFormService invoicesFormService;
    @In
    private ExtraHoursWorkedService extraHoursWorkedService;
    @In
    private GrantedBonusService grantedBonusService;
    @In
    private TaxPayrollUtilService taxPayrollUtilService;
    @In
    private PayrollGenerationCycleMergeService payrollGenerationCycleMergeService;
    @In
    private ChristmasPayrollService christmasPayrollService;
    @In
    private DiscountRuleService discountRuleService;
    @In
    private BankAccountService bankAccountService;
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @In
    private DiscountRuleRangeService discountRuleRangeService;

    @Logger
    private Log log;

    @SuppressWarnings({"UnusedAssignment"})
    private GeneratedPayroll load(GeneratedPayroll generatedPayroll) {
        return (GeneratedPayroll) em.createNamedQuery("GeneratedPayroll.load")
                .setParameter("generatedPayrollId", generatedPayroll.getId()).getSingleResult();
    }

    public void update(GeneratedPayroll generatedPayroll)
            throws ConcurrencyException, EntryDuplicatedException,
            GeneratedPayrollHasNegativeAmountException, EmployeeMissingBankAccountException,
            AlreadyExistsAnOfficialGeneratedPayrollException, CannotChangeToOutdatedGeneratedPayrollTypeException,
            CannotChangeFromOutdatedGeneratedPayrollTypeException, CannotChangeFromOfficialToTestGeneratedPayrollTypeException, QuotaInfoOutdatedException, GestionPayrollOfficialPayrollDeadlineException {
        GeneratedPayroll databaseGeneratedPayroll = listEm.find(GeneratedPayroll.class, generatedPayroll.getId());
        GeneratedPayrollType dataBaseGeneratedPayrollType = databaseGeneratedPayroll.getGeneratedPayrollType();

        if ((GeneratedPayrollType.OFFICIAL.equals(dataBaseGeneratedPayrollType) || GeneratedPayrollType.OFFICIAL.equals(generatedPayroll.getGeneratedPayrollType()))
                && generatedPayrollService.hasNegativeAmount(generatedPayroll)) {
            throw new GeneratedPayrollHasNegativeAmountException("There is negative liquid results");
        }

        /*test to official*/
        if (dataBaseGeneratedPayrollType.equals(GeneratedPayrollType.TEST) &&
                GeneratedPayrollType.OFFICIAL.equals(generatedPayroll.getGeneratedPayrollType())) {

            if (!gestionPayrollService.hasValidOfficialPayrollDeadline(databaseGeneratedPayroll.getGestionPayroll())) {
                throw new GestionPayrollOfficialPayrollDeadlineException("The gestion payroll it's out of official payroll deadline");
            }

            // If not all employees have a bank account
            if (!generatedPayrollService.haveBankAccounts(generatedPayroll)) {
                throw new EmployeeMissingBankAccountException("There are employees without bank account assigned");
            }

            long count = generatedPayrollService.countOfficialGeneratedPayrollByGestionPayrollParameters(generatedPayroll.getGestionPayroll());
            if (count > 0) {
                /*return can not be more than one official for the gestion*/
                generatedPayroll.setGeneratedPayrollType(dataBaseGeneratedPayrollType);
                throw new AlreadyExistsAnOfficialGeneratedPayrollException("There is already an official generated payroll");
            }
            GeneratedPayroll generatedPayroll4Operations = load(generatedPayroll);
            payrollGenerationCycleMergeService.merge(generatedPayroll4Operations);
            PayrollGenerationType payrollGenerationType = generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType();
            List<? extends GenericPayroll> genericPayrollList = new ArrayList<GenericPayroll>();
            if (payrollGenerationType.equals(PayrollGenerationType.GENERATION_BY_SALARY)) {
                genericPayrollList = getManagersPayrollList(generatedPayroll4Operations);
            }
            if (payrollGenerationType.equals(PayrollGenerationType.GENERATION_BY_PERIODSALARY)) {
                genericPayrollList = getFiscalProfessorPayrollList(generatedPayroll4Operations);
            }
            if (payrollGenerationType.equals(PayrollGenerationType.GENERATION_BY_TIME)) {
                genericPayrollList = getGeneralPayrollList(generatedPayroll4Operations);
            }
            salaryMovementService.matchGeneratedSalaryMovement(generatedPayroll4Operations, genericPayrollList);
        }
        /* any to outdated*/
        if (!dataBaseGeneratedPayrollType.equals(GeneratedPayrollType.OUTDATED) &&
                GeneratedPayrollType.OUTDATED.equals(generatedPayroll.getGeneratedPayrollType())) {
            throw new CannotChangeToOutdatedGeneratedPayrollTypeException("It is not possible to change from other type to outdated");
        }
        /* outdated to any*/
        if (dataBaseGeneratedPayrollType.equals(GeneratedPayrollType.OUTDATED) &&
                !GeneratedPayrollType.OUTDATED.equals(generatedPayroll.getGeneratedPayrollType())) {
            throw new CannotChangeFromOutdatedGeneratedPayrollTypeException("It is not possible to change from outdated to other type");
        }

        /*official to test*/
        if (dataBaseGeneratedPayrollType.equals(GeneratedPayrollType.OFFICIAL) &&
                GeneratedPayrollType.TEST.equals(generatedPayroll.getGeneratedPayrollType())) {
            throw new CannotChangeFromOfficialToTestGeneratedPayrollTypeException("It is not possible to change from official to test type");
        }

        /* Obtain the collections by GestionPayroll paid by payroll*/
        List<RotatoryFundCollection> databaseRotatoryFundCollectionList =
                rotatoryFundCollectionService.findRotatoryFundCollectionByGestionPayroll(generatedPayroll.getGestionPayroll());
        /*check if quota info are still valid*/
        boolean quotasStillValid = true;
        for (int i = 0; (i < databaseRotatoryFundCollectionList.size() && quotasStillValid); i++) {
            RotatoryFundCollection rotatoryFundCollection = databaseRotatoryFundCollectionList.get(i);
            quotasStillValid = quotaService.isQuotaInfoStillValid(rotatoryFundCollection);
        }
        if (!quotasStillValid) {
            throw new QuotaInfoOutdatedException("the quota info have been changed, so its necessary to recalculate the payrolls");
        }
        try {
            userTransaction.begin();
            em.merge(generatedPayroll);
            em.flush();
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                unexpectedErrorLog(e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                unexpectedErrorLog(e1);
            }
            throw new EntryDuplicatedException(ee);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                unexpectedErrorLog(e1);
            }
            throw new RuntimeException(e);
        }
    }

    protected void unexpectedErrorLog(Exception e) {
        log.error("An unexpected error have happened rolling back", e);
    }

    public List<GeneratedPayroll> findGeneratedPayrollByName(GeneratedPayroll generatedPayroll) {
        try {
            userTransaction.begin();
            Query query = em.createNamedQuery("GeneratedPayroll.findGeneratedPayrollByName");
            query.setParameter("name", generatedPayroll.getName());
            List<GeneratedPayroll> resultList = query.getResultList();
            userTransaction.commit();
            return resultList;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
            return new ArrayList<GeneratedPayroll>();
        }
    }

    public Long countGeneratedPayrollByName(GeneratedPayroll generatedPayroll) {
        Long resultCount = (long) 0;
        try {
            userTransaction.begin();
            Query query = em.createNamedQuery("GeneratedPayroll.countGeneratedPayrollByName");
            query.setParameter("name", generatedPayroll.getName());
            resultCount = (Long) query.getSingleResult();
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return resultCount == null ? 0 : resultCount;
    }

    public List<GeneratedPayroll> findGeneratedPayrollByGestionPayroll(GestionPayroll gestionPayroll, GeneratedPayrollType generatedPayrollType) {
        try {
            userTransaction.begin();
            Query query = em.createNamedQuery("GeneratedPayroll.findGeneratedPayrollByGestionPayroll");
            query.setParameter("gestionPayroll", gestionPayroll);
            query.setParameter("generatedPayrollType", generatedPayrollType);
            List<GeneratedPayroll> resultList = query.getResultList();
            userTransaction.commit();
            return resultList;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return new ArrayList<GeneratedPayroll>();
    }

    public Long countGeneratedPayrollByGestionPayroll(GestionPayroll gestionPayroll, GeneratedPayrollType generatedPayrollType) {
        Long resultCount = (long) 0;
        try {
            userTransaction.begin();
            Query query = em.createNamedQuery("GeneratedPayroll.countGeneratedPayrollByGestionPayroll");
            query.setParameter("gestionPayroll", gestionPayroll);
            query.setParameter("generatedPayrollType", generatedPayrollType);
            resultCount = (Long) query.getSingleResult();
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return resultCount == null ? 0 : resultCount;
    }

    public Long countOfficialGeneratedPayrollByGestionPayrollParameters(GestionPayroll gestionPayroll) {
        Long resultCount = (long) 0;
        try {
            userTransaction.begin();
            resultCount = (Long) em.createNamedQuery("GeneratedPayroll.countOfficialGeneratedPayrollByBusinessUnitAndGestionAndMonthAndJobCategoryAndGestionPayrollTypeAndGeneratedPayrollType")
                    .setParameter("businessUnit", gestionPayroll.getBusinessUnit())
                    .setParameter("gestion", gestionPayroll.getGestion())
                    .setParameter("month", gestionPayroll.getMonth())
                    .setParameter("gestionPayrollType", gestionPayroll.getGestionPayrollType())
                    .setParameter("jobCategory", gestionPayroll.getJobCategory())
                    .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                    .getSingleResult();
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return resultCount == null ? 0 : resultCount;
    }

    public List<GeneratedPayroll> findGeneratedPayrollsByGestionPayroll(GestionPayroll gestionPayroll) {
        try {
            userTransaction.begin();
            Query query = em.createNamedQuery("GeneratedPayroll.findGeneratedPayrollsByGestionPayroll");
            query.setParameter("gestionPayroll", gestionPayroll);
            List<GeneratedPayroll> resultList = query.getResultList();
            userTransaction.commit();
            return resultList;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return new ArrayList<GeneratedPayroll>();
    }

    public List<GeneratedPayroll> findOfficialGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType(GestionPayroll gestionPayroll) {
        return findGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType(gestionPayroll, GeneratedPayrollType.OFFICIAL);
    }

    @SuppressWarnings(value = "unchecked")
    private List<GeneratedPayroll> findGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType(GestionPayroll gestionPayroll, GeneratedPayrollType generatedPayrollType) {
        try {
            userTransaction.begin();
            Query query = em.createNamedQuery("GeneratedPayroll.findGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType");
            query.setParameter("gestionPayroll", gestionPayroll);
            query.setParameter("generatedPayrollType", generatedPayrollType);
            List<GeneratedPayroll> resultList = query.getResultList();
            userTransaction.commit();
            return resultList;
        } catch (NoResultException e) {
            return new ArrayList<GeneratedPayroll>();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                unexpectedErrorLog(e1);
            }
        }
        return new ArrayList<GeneratedPayroll>();
    }

    public Boolean hasGeneratedPayrolls(GestionPayroll gestionPayroll) {
        Long countGeneratedPayrolls = null;
        try {
            userTransaction.begin();
            countGeneratedPayrolls = (Long) em.createNamedQuery("GeneratedPayroll.countGeneratedPayrolls")
                    .setParameter("gestionPayroll", gestionPayroll)
                    .getSingleResult();
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return countGeneratedPayrolls != null && countGeneratedPayrolls > 0;
    }

    public List<GeneratedPayroll> findGeneratedPayrollsByGestion(Gestion gestion) {
        try {
            userTransaction.begin();
            Query query = em.createNamedQuery("GeneratedPayroll.findGeneratedPayrollsByGestion");
            query.setParameter("gestion", gestion);
            List<GeneratedPayroll> resultList = query.getResultList();
            userTransaction.commit();
            return resultList;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return new ArrayList<GeneratedPayroll>();
    }

    public List<GeneratedPayroll> findValidGeneratedPayrollsByGestionAndMount(Gestion gestion, Month month) {
        try {
            userTransaction.begin();
            List<GeneratedPayroll> resultList = em.createNamedQuery("GeneratedPayroll.findGeneratedPayrollsByGestionAndType")
                    .setParameter("gestion", gestion)
                    .setParameter("month", month).setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getResultList();
            userTransaction.commit();
            return resultList;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return new ArrayList<GeneratedPayroll>();
    }

    public Long countValidGeneratedPayrollsByGestionAndMounth(Gestion gestion, Month month) {
        try {
            userTransaction.begin();
            Long countResult = (Long) em.createNamedQuery("GeneratedPayroll.findGeneratedPayrollsByGestionAndType")
                    .setParameter("gestion", gestion)
                    .setParameter("month", month).setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getSingleResult();
            userTransaction.commit();
            return countResult;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return (long) 0;
    }

    @SuppressWarnings({"unchecked"})
    public List<ManagersPayroll> getManagersPayrollList(GeneratedPayroll generatedPayroll) {
        return em.createNamedQuery("ManagersPayroll.findByGeneratedPayroll").setParameter("generatedPayroll", generatedPayroll).getResultList();
    }

    @SuppressWarnings({"unchecked"})
    public List<FiscalProfessorPayroll> getFiscalProfessorPayrollList(GeneratedPayroll generatedPayroll) {
        return em.createNamedQuery("FiscalProfessorPayroll.findByGeneratedPayroll").setParameter("generatedPayroll", generatedPayroll).getResultList();
    }

    @SuppressWarnings({"unchecked"})
    public List<GeneralPayroll> getGeneralPayrollList(GeneratedPayroll generatedPayroll) {
        return em.createNamedQuery("GeneralPayroll.findByGeneratedPayroll").setParameter("generatedPayroll", generatedPayroll).getResultList();
    }

    @SuppressWarnings({"unchecked"})
    public List<ManagersPayroll> loadManagersPayrollList(List<Long> idList) {
        if (!ValidatorUtil.isEmptyOrNull(idList)) {
            return em.createNamedQuery("ManagersPayroll.loadByGeneratedPayrollList").setParameter("idList", idList).getResultList();
        }
        return new ArrayList<ManagersPayroll>(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<GeneralPayroll> loadGeneralPayrollList(List<Long> idList) {
        if (!ValidatorUtil.isEmptyOrNull(idList)) {
            return em.createNamedQuery("GeneralPayroll.loadByGeneratedPayrollList").setParameter("idList", idList).getResultList();
        }
        return new ArrayList<GeneralPayroll>(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<ChristmasPayroll> loadChristmasPayrollList(List<Long> idList) {
        if (!ValidatorUtil.isEmptyOrNull(idList)) {
            return em.createNamedQuery("ChristmasPayroll.loadByGeneratedPayrollList").setParameter("idList", idList).getResultList();
        }
        return new ArrayList<ChristmasPayroll>(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<FiscalProfessorPayroll> loadFiscalProfessorPayrollList(List<Long> idList) {
        if (!ValidatorUtil.isEmptyOrNull(idList)) {
            return em.createNamedQuery("FiscalProfessorPayroll.loadByGeneratedPayrollList").setParameter("idList", idList).getResultList();
        }
        return new ArrayList<FiscalProfessorPayroll>(0);
    }

    /*count worked days*/
    private int getContractDays4Month(Contract contract, GestionPayroll gestionPayroll) {
        int periodDuration = 30;
        Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(gestionPayroll.getEndDate());
        Calendar initDateRange = DateUtils.toDateCalendar(gestionPayroll.getInitDate());
        Calendar endDateRange = DateUtils.toDateCalendar(gestionPayroll.getEndDate());
        Calendar initDateContract = DateUtils.toDateCalendar(contract.getInitDate());
        Calendar endDateContract = DateUtils.toDateCalendar(contract.getEndDate());
        Date currentInitDate = initDateRange.getTime();
        Date currentEndDate = endDateRange.getTime();
        Calendar first = DateUtils.toCalendar(firstDayOfMonth);
        first.set(Calendar.HOUR, 0);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        first.set(Calendar.MILLISECOND, 0);
        firstDayOfMonth = first.getTime();

        Calendar last = DateUtils.toCalendar(gestionPayroll.getEndDate());
        last.set(Calendar.DAY_OF_MONTH, 30);
        last.set(Calendar.HOUR, 0);
        last.set(Calendar.MINUTE, 0);
        last.set(Calendar.SECOND, 0);
        last.set(Calendar.MILLISECOND, 0);
        Date lastDayOfMonth = last.getTime();

        if (contract.getInitDate().compareTo(firstDayOfMonth) >= 0 && contract.getEndDate().compareTo(lastDayOfMonth) <= 0) {
            periodDuration = (int) DateUtils.daysBetween(contract.getInitDate(), contract.getEndDate());
            log.debug(">>>> ALTA Y BAJA DE CONTRATO...");
        } else {
            //High of contract
            if (contract.getInitDate().compareTo(firstDayOfMonth) >= 0 && contract.getInitDate().compareTo(lastDayOfMonth) <= 0) {
                if ((int) DateUtils.daysBetween(contract.getInitDate(), lastDayOfMonth) > 30) {
                    periodDuration = 30;
                    log.debug(">>>> ALTA DE CONTRATO...1");
                } else {
                    periodDuration = (int) DateUtils.daysBetween(contract.getInitDate(), lastDayOfMonth);
                    log.debug(">>>> ALTA DE CONTRATO...2");
                }
            }
            // Low Contract
            if (contract.getEndDate().compareTo(firstDayOfMonth) > 0 && contract.getEndDate().compareTo(lastDayOfMonth) < 0) {
                if ((int) DateUtils.daysBetween(firstDayOfMonth, contract.getEndDate()) > 30 || (int) DateUtils.daysBetween(firstDayOfMonth, lastDayOfMonth) < 30) {
                    periodDuration = 30;
                } else {
                    periodDuration = (int) DateUtils.daysBetween(firstDayOfMonth, contract.getEndDate());
                }
                log.debug(">>>> BAJA DE CONTRATO...");
            }
        }
        if (contract.getInitDate().compareTo(initDateRange.getTime()) >= 0 &&
                contract.getInitDate().compareTo(firstDayOfMonth) < 0 &&
                contract.getEndDate().compareTo(lastDayOfMonth) >= 0) {
            if ((int) DateUtils.daysBetween(firstDayOfMonth, lastDayOfMonth) > 30 || (int) DateUtils.daysBetween(firstDayOfMonth, lastDayOfMonth) < 30) {
                periodDuration = 30;
            }
            log.debug(">>>> ALTA ANTERIOR......");
        } else {
            //NORMAL CONTRACT
            if (contract.getInitDate().compareTo(firstDayOfMonth) < 0 && contract.getEndDate().compareTo(lastDayOfMonth) > 0) {
                if (initDateContract != null && initDateContract.compareTo(initDateRange) > 0) {
                    currentInitDate = initDateContract.getTime();
                }
                if (endDateContract != null && endDateContract.compareTo(endDateRange) < 0) {
                    currentInitDate = endDateContract.getTime();
                }
                if (currentInitDate.compareTo(initDateRange.getTime()) != 0 || currentEndDate.compareTo(endDateRange.getTime()) != 0) {
                    periodDuration = (int) DateUtils.daysBetween(currentInitDate, currentEndDate);
                    periodDuration = periodDuration > 30 ? 30 : periodDuration;
                }
                log.debug(">>>> CONTRATOS NORMALES...");
            }
        }
        return periodDuration;
    }


    /*Generates the PayrollReport for a given GestionPayrroll entry (year-month) for the the payroll id 1
    */

    /*according to the type of gestionPayroll defined by the jobCaterory that draws the kind of employee payroll*/

    public PayrollGenerationResult fillPayroll(GeneratedPayroll generatedPayroll) throws Exception {

        PayrollGenerationResult payrollGenerationResult = PayrollGenerationResult.SUCCESS;
        log.debug("------ Execute fillPayroll method ------");

        Long generatedPayrollAuxId = null;
        try {
            boolean alreadyExistsGP = false;

            if (generatedPayroll.getId() != null) {
                try {
                    if (genericService.findById(GeneratedPayroll.class, generatedPayroll.getId()) != null) {
                        alreadyExistsGP = true;
                    }
                } catch (EntryNotFoundException e) {
                    log.debug("The generated payroll was not found", e);
                }
            }

            if (!alreadyExistsGP) {
                userTransaction.setTransactionTimeout(60);
                userTransaction.begin();
                em.persist(generatedPayroll);
                em.flush();
                userTransaction.commit();
                userTransaction.setTransactionTimeout(0);
            }

            generatedPayrollAuxId = generatedPayroll.getId();

            PayrollGenerationType payrollGenerationType = generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType();
            Boolean isSalaryGenerationType = !PayrollGenerationType.GENERATION_BY_TIME.equals(payrollGenerationType);
            GestionPayroll gestionPayroll = generatedPayroll.getGestionPayroll();

            List<Date> specialDate4BusinessUnit = specialDateService.getSpecialDateRange(gestionPayroll.getBusinessUnit(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate());
            Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit = specialDateService.getSpecialDateTimeRange(gestionPayroll.getBusinessUnit(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate());
            Map<Long, List<Date>> specialDate4OrganizationalUnit = new HashMap<Long, List<Date>>();
            Map<Long, Map<Date, List<TimeInterval>>> specialDateTimeForOrganizationalUnit = new LinkedHashMap<Long, Map<Date, List<TimeInterval>>>();

            //will be taken into account oly for academic fiscal sector
            List<DiscountRule> globalDiscountRuleList = new ArrayList<DiscountRule>();
            List<DiscountRule> businessUnitGlobalDiscountRuleList = new ArrayList<DiscountRule>();
            List<DiscountRule> jobCategoryBusinessUnitDiscountRuleList = new ArrayList<DiscountRule>();
            //map that holds known discount rule to apply given a minute
            Map<Integer, DiscountRuleRange> minuteDiscountRuleRangeMap = new HashMap<Integer, DiscountRuleRange>();

            if (isSalaryGenerationType != null) {
                Integer pageSize = 50;
                Integer firstResult = 0;
                Boolean existsElements = true;

                ExecutionTimeUtil executionTimeUtil = new ExecutionTimeUtil();
                executionTimeUtil.startExecution();

                /* Obtain the collections by GestionPayroll paid by payroll*/
                List<RotatoryFundCollection> databaseRotatoryFundCollectionList = rotatoryFundCollectionService.findRotatoryFundCollectionByGestionPayroll(gestionPayroll);
                List<RotatoryFundCollection> newRotatoryFundCollectionList = new ArrayList<RotatoryFundCollection>();
                /*check if quota info are still valid*/
                boolean quotasStillValid = true;
                /*indicates if the collections need to be recreated*/
                boolean recreateIsNeed = false;
                for (int i = 0; (i < databaseRotatoryFundCollectionList.size() && quotasStillValid); i++) {
                    RotatoryFundCollection rotatoryFundCollection = databaseRotatoryFundCollectionList.get(i);
                    quotasStillValid = quotaService.isQuotaInfoStillValid(rotatoryFundCollection);
                }

                while (existsElements && PayrollGenerationResult.SUCCESS.equals(payrollGenerationResult)) {
                    userTransaction.setTransactionTimeout(120 * pageSize);
                    userTransaction.begin();
                    List<Employee> employeeList = employeeService.getEmployeesForPayrollGenerationByLastDayOfMonth(gestionPayroll.getBusinessUnit(),
                            gestionPayroll.getJobCategory(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate(), firstResult, pageSize + 1);
                    log.debug("Current employeeList size =" + employeeList.size());

                    if (existsElements = employeeList.size() > pageSize) {
                        employeeList = employeeList.subList(0, pageSize);
                    }

                    log.debug("EmployeeList size to proccess =" + employeeList.size());
                    log.debug("Index processing from " + firstResult + " to " + (firstResult + pageSize));

                    if (isSalaryGenerationType) {
                        if (gestionPayroll.getJobCategory().getPayrollGenerationType().equals(PayrollGenerationType.GENERATION_BY_PERIODSALARY)) {
                            List<Long> employeeIdList = (List<Long>) ListUtil.i.getIdList(employeeList);
                            HashMap<Long, List<JobContract>> employeeJobContractMap = (HashMap<Long, List<JobContract>>) horaryBandContractService.getJobContractWithValidHoraryBandContractByEmployeeBusinessUnitJobCategory(
                                    employeeIdList, gestionPayroll.getBusinessUnit(), gestionPayroll.getJobCategory(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate());
                            payrollGenerationResult = fillFiscalProfessorPayroll(
                                    generatedPayroll, employeeList, specialDate4BusinessUnit,
                                    specialDateTime4BusinessUnit, specialDate4OrganizationalUnit,
                                    specialDateTimeForOrganizationalUnit,
                                    globalDiscountRuleList,
                                    businessUnitGlobalDiscountRuleList,
                                    jobCategoryBusinessUnitDiscountRuleList,
                                    minuteDiscountRuleRangeMap,
                                    newRotatoryFundCollectionList,
                                    employeeIdList,
                                    employeeJobContractMap);
                        } else {
                            payrollGenerationResult = fillManagersPayroll(
                                    generatedPayroll, employeeList, specialDate4BusinessUnit,
                                    specialDateTime4BusinessUnit, specialDate4OrganizationalUnit,
                                    specialDateTimeForOrganizationalUnit,
                                    newRotatoryFundCollectionList);
                        }
                    } else {
                        payrollGenerationResult = fillProffesorsPayroll(
                                generatedPayroll, employeeList, specialDate4BusinessUnit,
                                specialDateTime4BusinessUnit, specialDate4OrganizationalUnit,
                                specialDateTimeForOrganizationalUnit,
                                newRotatoryFundCollectionList);
                    }

                    firstResult += pageSize;

                    userTransaction.commit();
                    userTransaction.setTransactionTimeout(0);
                }
                /* check if its necessary to recreate collections*/
                log.debug("quotas valid?" + quotasStillValid);
                /* check if its necessary to recreate collections*/
                if (databaseRotatoryFundCollectionList.size() != newRotatoryFundCollectionList.size() || !quotasStillValid) {
                    recreateIsNeed = true;
                }
                if (quotasStillValid && !recreateIsNeed) {
                    /*compare both lists to reconsider if recreate is needed */
                    for (int i = 0; (i < newRotatoryFundCollectionList.size() && !recreateIsNeed); i++) {
                        RotatoryFundCollection newRotatoryFundCollection = newRotatoryFundCollectionList.get(i);
                        boolean foundNewElement = false;
                        for (RotatoryFundCollection checkRotatoryFundCollection : databaseRotatoryFundCollectionList) {
                            if ((newRotatoryFundCollection.getQuota().getId().longValue() == checkRotatoryFundCollection.getQuota().getId().longValue())
                                    && (newRotatoryFundCollection.getQuotaResidue().doubleValue() == checkRotatoryFundCollection.getQuotaResidue().doubleValue())
                                    && (newRotatoryFundCollection.getSourceAmount().doubleValue() == checkRotatoryFundCollection.getSourceAmount().doubleValue())) {
                                foundNewElement = true;
                            }
                        }
                        recreateIsNeed = !foundNewElement;
                    }
                }
                if (recreateIsNeed) {
                    log.debug("DELETE OLD RotatoryFoundCollections");
                    rotatoryFundCollectionService.deleteRotatoryFundByGestionPayroll(gestionPayroll);
                    log.debug("SET OUTDATED all GeneratedPayrolls but this");
                    setTestToOutdatedGeneratedPayrollButCurrentByGestionPayroll(gestionPayroll, generatedPayroll);
                    log.debug("CREATE NEW RotatoryFoundCollections ACCORDING TO the new list of collections");
                    createRotatoryFunds(newRotatoryFundCollectionList);
                }
                executionTimeUtil.endExecution();
                log.debug("Time executions time = " + executionTimeUtil.timeInSecons() + " seg");

                if (!PayrollGenerationResult.SUCCESS.equals(payrollGenerationResult)) {
                    if (generatedPayrollAuxId != null) {
                        deleteGeneratedPayroll(generatedPayrollAuxId);
                    }
                    userTransaction.setTransactionTimeout(0);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e, "Unexpected exception, rolling back");
            userTransaction.rollback();
            if (generatedPayrollAuxId != null) {
                deleteGeneratedPayroll(generatedPayrollAuxId);
            }
            userTransaction.setTransactionTimeout(0);
            payrollGenerationResult = PayrollGenerationResult.FAIL;
        }

        return payrollGenerationResult;
    }

    /**
     * generate end of year bonus payroll
     *
     * @param generatedPayroll a given GeneralPayroll that contains a set of params
     * @return a custom outcome
     * @throws Exception
     */
    public PayrollGenerationResult fillChristmasPayroll(GeneratedPayroll generatedPayroll) throws Exception {

        PayrollGenerationResult payrollGenerationResult;
        log.debug("------ Execute fillChristmasPayroll method ------");
        boolean basicBased = false;
        try {
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            basicBased = companyConfiguration.getBasicBasedChristmasPayroll();
        } catch (CompanyConfigurationNotFoundException e) {
            log.error("company companyConfiguration was not found", e);
        }
        try {
            GestionPayroll gestionPayroll = generatedPayroll.getGestionPayroll();
            ExecutionTimeUtil executionTimeUtil = new ExecutionTimeUtil();
            executionTimeUtil.startExecution();
            List<Month> lastThreeMonthList = new ArrayList<Month>();
            lastThreeMonthList.add(Month.SEPTEMBER);
            lastThreeMonthList.add(Month.OCTOBER);
            lastThreeMonthList.add(Month.NOVEMBER);
            //get the september, october and november official generated payrolls
            Map<GestionPayroll, GeneratedPayroll> lastThreeMonthsPayrollMap = new LinkedHashMap<GestionPayroll, GeneratedPayroll>();
            Map<Month, GestionPayroll> lastThreeMonthsGestionPayrollMap = new LinkedHashMap<Month, GestionPayroll>();
            for (Month month : lastThreeMonthList) {
                Gestion gestion = gestionService.getGestion(gestionPayroll.getGestion().getYear());
                @SuppressWarnings({"NullableProblems"})
                GestionPayroll lastGestionPayroll = gestionPayrollService.findGestionPayrollByGestionAndBusinessUnitAndMonthAndJobCategory(
                        gestion, month, gestionPayroll.getBusinessUnit(), gestionPayroll.getJobCategory(), null);
                GeneratedPayroll lastGeneratedPayroll = findOfficialGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType(lastGestionPayroll).get(0);
                lastThreeMonthsPayrollMap.put(lastGestionPayroll, lastGeneratedPayroll);
                lastThreeMonthsGestionPayrollMap.put(month, lastGestionPayroll);
            }
            //merge a list of active employees that exists in september, october and november payrolls
            List<Long> employeeIdList = managersPayrollService.findEmployeeIdListByGeneratedPayroll(lastThreeMonthsPayrollMap.get(
                    lastThreeMonthsGestionPayrollMap.get(Month.SEPTEMBER)));
            for (int i = 1; i < lastThreeMonthList.size(); i++) {
                employeeIdList = managersPayrollService.findEmployeeIdListByGeneratedPayrollInEmployeeIdList(
                        lastThreeMonthsPayrollMap.get(lastThreeMonthsGestionPayrollMap.get(lastThreeMonthList.get(i))), employeeIdList);
            }

            int employeeTotalSize = employeeIdList.size();

            //settlement date
            Calendar settlementDate = Calendar.getInstance();
            settlementDate.set(Calendar.YEAR, gestionPayroll.getGestion().getYear());
            settlementDate.set(Calendar.MONTH, settlementDate.getMaximum(Calendar.MONTH));
            settlementDate.set(Calendar.DAY_OF_MONTH, settlementDate.getMaximum(Calendar.DAY_OF_MONTH));

            // to hold all employees ManagersPayroll by month
            Map<Month, Map<Long, ManagersPayroll>> monthManagersPayrollMap = new LinkedHashMap<Month, Map<Long, ManagersPayroll>>();

            userTransaction.setTransactionTimeout(120 * employeeTotalSize);
            userTransaction.begin();

            em.persist(generatedPayroll);
            em.flush();

            for (Month month : lastThreeMonthList) {
                // gets the entries of employee id and managersPayroll for the employeeIdSubList
                List<Object[]> managersPayrollList = managersPayrollService.findByGeneratedPayrollAndEmployeeIdList(
                        lastThreeMonthsPayrollMap.get(lastThreeMonthsGestionPayrollMap.get(month)), employeeIdList);

                LinkedHashMap<Long, ManagersPayroll> managersPayrollHashMap = new LinkedHashMap<Long, ManagersPayroll>();
                for (Object[] objects : managersPayrollList) {
                    Long employeeId = (Long) objects[0];
                    ManagersPayroll managersPayroll = (ManagersPayroll) objects[1];
                    managersPayrollHashMap.put(employeeId, managersPayroll);
                }
                // since this query will be executed by pageSize combine the results if any
                if (monthManagersPayrollMap.containsKey(month)) {
                    monthManagersPayrollMap.get(month).putAll(managersPayrollHashMap);
                } else {
                    monthManagersPayrollMap.put(month, managersPayrollHashMap);
                }
            }

            // process each employee
            for (Long employeeId : employeeIdList) {
                Employee employee = employeeService.getEmployeeById(employeeId);
                // get contract earlier init date from the payrolls
                // get the last payroll basic salary
                Date initContractDate = null;
                BigDecimal salary = null;
                for (Month month : lastThreeMonthList) {
                    ManagersPayroll managersPayroll = monthManagersPayrollMap.get(month).get(employeeId);
                    Date currentInitContractDate = managersPayroll.getContractInitDate();
                    if (null == initContractDate) {
                        initContractDate = currentInitContractDate;
                    } else {
                        if (initContractDate.compareTo(currentInitContractDate) > 0) {
                            initContractDate = currentInitContractDate;
                        }
                    }
                    salary = managersPayroll.getSalary();
                }

                int workedDays = (int) DateUtils.daysBetween(initContractDate, settlementDate.getTime());
                if (workedDays > DAYS_OF_YEAR) {
                    workedDays = DAYS_OF_YEAR;
                }

                ManagersPayroll septemberManagersPayroll = monthManagersPayrollMap.get(Month.SEPTEMBER).get(employeeId);
                ManagersPayroll octoberManagersPayroll = monthManagersPayrollMap.get(Month.OCTOBER).get(employeeId);
                ManagersPayroll novemberManagersPayroll = monthManagersPayrollMap.get(Month.NOVEMBER).get(employeeId);
                BigDecimal septemberTotalIncome;
                BigDecimal octoberTotalIncome;
                BigDecimal novemberTotalIncome;
                if (basicBased) {
                    septemberTotalIncome = BigDecimalUtil.sum(septemberManagersPayroll.getSalary(), septemberManagersPayroll.getOtherIncomes(), septemberManagersPayroll.getIncomeOutOfIva());
                    octoberTotalIncome = BigDecimalUtil.sum(octoberManagersPayroll.getSalary(), octoberManagersPayroll.getOtherIncomes(), octoberManagersPayroll.getIncomeOutOfIva());
                    novemberTotalIncome = BigDecimalUtil.sum(novemberManagersPayroll.getSalary(), novemberManagersPayroll.getOtherIncomes(), novemberManagersPayroll.getIncomeOutOfIva());
                } else {
                    septemberTotalIncome = BigDecimalUtil.sum(septemberManagersPayroll.getTotalIncome(), septemberManagersPayroll.getIncomeOutOfIva());
                    octoberTotalIncome = BigDecimalUtil.sum(octoberManagersPayroll.getTotalIncome(), octoberManagersPayroll.getIncomeOutOfIva());
                    novemberTotalIncome = BigDecimalUtil.sum(novemberManagersPayroll.getTotalIncome(), novemberManagersPayroll.getIncomeOutOfIva());
                }
                BigDecimal averageSalary = BigDecimalUtil.divide(BigDecimalUtil.sum(septemberTotalIncome, octoberTotalIncome, novemberTotalIncome), BigDecimalUtil.toBigDecimal(lastThreeMonthList.size()), SCALE);
                BigDecimal contributableSalary = workedDays >= 365 ? averageSalary : workedDays > 90 ? (BigDecimalUtil.divide(BigDecimalUtil.multiply(averageSalary, BigDecimalUtil.toBigDecimal(workedDays), SCALE), BigDecimalUtil.toBigDecimal(DAYS_OF_YEAR), SCALE)) : BigDecimal.ZERO;
                BankAccount bankAccount = payrollReportService.getEmployeeDefaultBankAccount(employeeId);
                ChristmasPayroll christmasPayroll = christmasPayrollService.buildChristmasPayroll(generatedPayroll, gestionPayroll,
                        employee, initContractDate, salary, workedDays, novemberManagersPayroll,
                        septemberTotalIncome, octoberTotalIncome, novemberTotalIncome, averageSalary,
                        contributableSalary, bankAccount);
                em.persist(christmasPayroll);
                em.flush();
            }
            userTransaction.commit();
            userTransaction.setTransactionTimeout(0);
            payrollGenerationResult = PayrollGenerationResult.SUCCESS;

            executionTimeUtil.endExecution();
            log.debug("Time executions time = " + executionTimeUtil.timeInSecons() + " seg");
        } catch (Exception e) {
            log.error("Payroll generation exception... ", e);
            userTransaction.rollback();
            userTransaction.setTransactionTimeout(0);
            throw new RuntimeException("Payroll generation exception");
        }

        return payrollGenerationResult;
    }


    /**
     * @param generatedPayroll               a GeneralPayroll that groups all ManagersPayroll entries
     * @param employeeList                   a list of employees to process
     * @param specialDate4BusinessUnit       a list of dates where the employee have permission
     * @param specialDateTime4BusinessUnit   a map of list of dates and time intervals where the employee have permission
     * @param specialDate4OrganizationalUnit a map that contains a list of special dates by OrganizationalUnit
     * @param specialDateTimeForOrganizationalUnit
     *                                       a map that contains a list of special dates by time by OrganizationalUnit
     * @param newRotatoryFundCollectionList  a list of Rotatory Fund Collections
     * @return PayrollGenerationResult for Managers
     */
    public PayrollGenerationResult fillManagersPayroll(GeneratedPayroll generatedPayroll, List<Employee> employeeList,
                                                       List<Date> specialDate4BusinessUnit,
                                                       Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                       Map<Long, List<Date>> specialDate4OrganizationalUnit,
                                                       Map<Long, Map<Date, List<TimeInterval>>> specialDateTimeForOrganizationalUnit,
                                                       List<RotatoryFundCollection> newRotatoryFundCollectionList) {

        GestionPayroll gestionPayroll = generatedPayroll.getGestionPayroll();
        // iterates each employee
        int index = 0;
        List<Long> employeeIdList = ListUtil.i.getIdList(employeeList);
        Map<Long, TributaryPayroll> lastMonthTributaryPayrollMap
                = tributaryPayrollGeneratorService.getTributaryPayrollsForLastMonth(generatedPayroll.getPayrollGenerationCycle(), employeeIdList);
        // InvoicesForm map by employee id
        Map<Long, InvoicesForm> invoicesFormMap = invoicesFormService.findInvoicesFormMapByPayrollGenerationCycleAndEmployeeList(
                generatedPayroll.getPayrollGenerationCycle(), employeeIdList);
        // ExtraHoursWorked map by jobContract id
        Map<Long, ExtraHoursWorked> extraHoursWorkedCache = extraHoursWorkedService.findByPayrollGenerationCycleAndJobCategory(
                generatedPayroll.getPayrollGenerationCycle(), generatedPayroll.getGestionPayroll().getJobCategory());
        Map<Long, List<GrantedBonus>> grantedBonusMap = grantedBonusService.findByPayrollGenerationCycleAndJobCategory(
                generatedPayroll.getPayrollGenerationCycle(), generatedPayroll.getGestionPayroll().getJobCategory());
        SeniorityBonus seniorityBonus = taxPayrollUtilService.getActiveSeniorityBonus();
        for (Employee employee : employeeList) {
            index++;
            log.debug("Processing employee = " + employee.getId() + employee.getFullName() + " index=" + index);
            JobContract currentJobContract;
            Double lastIvaResidue = 0.0;
            Double totalSumOfDiscounts = 0.0;

            Double totalSumOfIncomesBeforeIva = 0.0;
            Double totalSumOfIncomesOutOfIva = 0.0;

            // absences and tardiness
            Double totalSumOfDiscountsPerLateness = 0.0;

            // minutes of band absences
            //Integer totalSumOfMinuteBandAbsences = new Integer(0);
            List<Integer> totalSumOfMinuteBandAbsencesList = new ArrayList<Integer>();

            Double perMinuteDiscount = 0.0;

            Double totalWinDiscount = 0.0;
            Double totalAdvanceDiscount = 0.0;
            Double totalLoanDiscount = 0.0;
            Double totalOtherDiscount = 0.0;
            // get the contracts, for the current employee, valid for the last gestionPayroll (year-month) entry
            List<Contract> employeeValidContractsList = contractService.getContractsForPayrollGenerationByLastDayOfMonth(gestionPayroll.getBusinessUnit(), gestionPayroll.getJobCategory(), employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

            if (!employeeValidContractsList.isEmpty()) { //IF THE EMPLOYEE HAS VALID CONTRACTS

                Double mensualTotalSalary;
                Double ivaResidue = 0.0;
                Double totalRCIvaDiscount = 0.0;
                Double totalAfpDiscount = 0.0;
                Double totalOtherIncomes = 0.0;
                Double totalIncomeOutOfIva = 0.0;
                Double proHome = 0.0;
                int workedDays = 30;

                //sum of Lists
                Integer cumulativeMinutesIntheMonth4AllContracts = 0;

                List<Integer> cumulativeMinutesIntheMonthByContractList = new ArrayList();
                List<Integer> cumulativePerformanceMinutesIntheMonthByContractList = new ArrayList();
                List<Integer> cumulativeLatenessMinutesIntheMonthByContractList = new ArrayList();
                List<Double> cumulativeDayAbsencesIntheMonthByContractList = new ArrayList();

                List<Double> contractsPriceList = new ArrayList();

                // AttendanceControl
                Calendar initDate = Calendar.getInstance();
                initDate.setTime(gestionPayroll.getInitDate());

                Calendar endDate = Calendar.getInstance();
                endDate.setTime(gestionPayroll.getEndDate());

                // The current values of HoraryBandContract for payroll generation
                List<HoraryBandContract> hourlyBandContract4EmployeeList = horaryBandContractService.getValidHoraryBandContractsByEmployeeAndBusinessUnitAndJobCategory(employee, gestionPayroll.getBusinessUnit(), gestionPayroll.getJobCategory(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

                if (hourlyBandContract4EmployeeList.isEmpty()) {
                    return PayrollGenerationResult.WITHOUT_BANDS.assignResultData(employee.getIdNumberAndFullName());
                }

                currentJobContract = getJobContractForPayment(hourlyBandContract4EmployeeList);
                if (currentJobContract == null) {
                    Contract employeeContract = em.find(Contract.class, employeeValidContractsList.get(0).getId());
                    currentJobContract = em.find(JobContract.class, employeeContract.getJobContractList().get(0).getId());
                }

                hourlyBandContract4EmployeeList = cleanDuplicate(hourlyBandContract4EmployeeList);

                // The current values of RRMark for payroll generation
                Map<Date, List<Date>> rhMarkTimeDateMap4Employee = rhMarkService.getRHMarkDateTimeMapByDateRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

                // The current values of SpecialDate for payroll generation
                List<Date> specialDate4Employee = specialDateService.getSpecialDateRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());
                Map<Date, List<TimeInterval>> specialDateTime4Employee = specialDateService.getSpecialDateTimeRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());
                //todo: implementar rotaciones en las badas horarias
                // here is the control because, each contract may have different variables of cost
                for (Contract contract : employeeValidContractsList) {

                    List<HoraryBandContract> horaryBandContractList4Contract = filterBandsByContract(contract, hourlyBandContract4EmployeeList);

                    if (!horaryBandContractList4Contract.isEmpty()) {// IF THE CONTRACT HAS VALID TIMES

                        List<Integer> cumulativeMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Integer> cumulativePerformanceMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Integer> cumulativeLatenessMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Double> cumulativeDayAbsencesIntheMonth4ContractList = new ArrayList<Double>(0);

                        // this var is to control the days of the month. It is initially setted to the first day of the month
                        Calendar currentDate = Calendar.getInstance();
                        currentDate.setTime(gestionPayroll.getInitDate());
                        currentDate.set(Calendar.MILLISECOND, 0);
                        /*if the contract does not cover all the month, then compute the fraction of salary
                        corresponding to the period of the contract in days*/
                        int contractDays = getContractDays4Month(contract, gestionPayroll);
                        workedDays = contractDays;

                        log.debug("workedDays: " + workedDays);
                        Job job = em.find(Job.class, currentJobContract.getJob().getId());

                        if (!specialDate4OrganizationalUnit.containsKey(job.getOrganizationalUnit().getId())) {
                            specialDate4OrganizationalUnit.put(job.getOrganizationalUnit().getId(), specialDateService.getSpecialDateRange(job.getOrganizationalUnit(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate()));
                        }
                        if (!specialDateTimeForOrganizationalUnit.containsKey(job.getOrganizationalUnit().getId())) {
                            specialDateTimeForOrganizationalUnit.put(job.getOrganizationalUnit().getId(), specialDateService.getSpecialDateTimeRange(job.getOrganizationalUnit(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate()));
                        }

                        // here saves the contract price normalized according to workable days according to contract.
                        if (job.getSalary().getCurrency().getSymbol().equalsIgnoreCase("$US")) {
                            double salary = job.getSalary().getAmount().doubleValue() * generatedPayroll.getExchangeRate().getSale().doubleValue() / 30 * contractDays;
                            contractsPriceList.add(salary);
                        } else {
                            double salary = job.getSalary().getAmount().doubleValue() / 30 * contractDays;
                            contractsPriceList.add(salary);
                        }
                        HoraryBandContract horaryBandContractEspecial = getHasHorariEspecial(horaryBandContractList4Contract);
                        if(horaryBandContractEspecial != null)
                            executeAttendanceControlManagersRotation(endDate, currentDate, generatedPayroll,
                                    employee, horaryBandContractEspecial,
                                    rhMarkTimeDateMap4Employee,
                                    specialDate4BusinessUnit,
                                    specialDateTime4BusinessUnit,
                                    specialDate4OrganizationalUnit.get(job.getOrganizationalUnit().getId()),
                                    specialDateTimeForOrganizationalUnit.get(job.getOrganizationalUnit().getId()),
                                    specialDate4Employee,
                                    specialDateTime4Employee,
                                    cumulativeMinutesIntheMonth4ContractList,
                                    cumulativePerformanceMinutesIntheMonth4ContractList,
                                    cumulativeLatenessMinutesIntheMonth4ContractList,
                                    cumulativeDayAbsencesIntheMonth4ContractList,
                                    totalSumOfMinuteBandAbsencesList);
                        else
                        executeAttendanceControlManagers(endDate, currentDate, generatedPayroll,
                                employee, horaryBandContractList4Contract,
                                rhMarkTimeDateMap4Employee,
                                specialDate4BusinessUnit,
                                specialDateTime4BusinessUnit,
                                specialDate4OrganizationalUnit.get(job.getOrganizationalUnit().getId()),
                                specialDateTimeForOrganizationalUnit.get(job.getOrganizationalUnit().getId()),
                                specialDate4Employee,
                                specialDateTime4Employee,
                                cumulativeMinutesIntheMonth4ContractList,
                                cumulativePerformanceMinutesIntheMonth4ContractList,
                                cumulativeLatenessMinutesIntheMonth4ContractList,
                                cumulativeDayAbsencesIntheMonth4ContractList,
                                totalSumOfMinuteBandAbsencesList);

                        int bandDuration = 0;
                        int performance = 0;
                        double dayAbsences = 0.0;
                        int tardinessMonth = 0;
                        for (int k = 0; k < cumulativeMinutesIntheMonth4ContractList.size(); k++) {
                            bandDuration += cumulativeMinutesIntheMonth4ContractList.get(k);
                            performance += cumulativePerformanceMinutesIntheMonth4ContractList.get(k);
                            dayAbsences += cumulativeDayAbsencesIntheMonth4ContractList.get(k);
                            tardinessMonth += cumulativeLatenessMinutesIntheMonth4ContractList.get(k);
                        }
                        cumulativeDayAbsencesIntheMonthByContractList.add(dayAbsences);
                        cumulativeLatenessMinutesIntheMonthByContractList.add(tardinessMonth);
                        cumulativeMinutesIntheMonthByContractList.add(bandDuration);
                        cumulativePerformanceMinutesIntheMonthByContractList.add(performance);
                    }
                }
                Double pricePerMinute = 0.0;
                Double dayAbsences = 0.0;
                Integer tardinessTotal = 0;

                for (int i = 0; i < cumulativeMinutesIntheMonthByContractList.size(); i++) {
                    dayAbsences += cumulativeDayAbsencesIntheMonthByContractList.get(i);
                    tardinessTotal += cumulativeLatenessMinutesIntheMonthByContractList.get(i);
                    Integer minutes = cumulativeMinutesIntheMonthByContractList.get(i);
                    Integer performanceMinutes = cumulativePerformanceMinutesIntheMonthByContractList.get(i);
                    Double salary = contractsPriceList.get(i);
                    Double pricePerPeriod = 0.0;
                    if (minutes == 0) {
                        pricePerMinute = 0.0;
                    } else {
                        pricePerMinute = (salary / minutes);
                    }
                    if (performanceMinutes != 0) {
                        cumulativeMinutesIntheMonth4AllContracts += minutes - performanceMinutes;
                    }
                    perMinuteDiscount += ((minutes - performanceMinutes) * pricePerPeriod);
                }

                /* todo get the basic salary does not support may contracts */
                double basicSalary;
                if (currentJobContract.getJob().getSalary().getCurrency().getSymbol().equalsIgnoreCase("$US")) {
                    basicSalary = currentJobContract.getJob().getSalary().getAmount().doubleValue() * generatedPayroll.getExchangeRate().getSale().doubleValue();
                } else {
                    basicSalary = currentJobContract.getJob().getSalary().getAmount().doubleValue();
                }

                log.debug(">>> WORKED DAYS: " + workedDays);
                log.debug(">>> day Absences: " + dayAbsences);
                mensualTotalSalary = basicSalary / 30 * (workedDays - dayAbsences);

                // calculate discounts
                // Salary movement list of employee that has relationship with gestion payroll
                List<SalaryMovement> salaryMovementList = salaryMovementService.findByEmployeeAndGestionPayroll(employee, gestionPayroll);

                Boolean activeForTaxPayrollGeneration = currentJobContract.getContract().getActiveForTaxPayrollGeneration();

                for (SalaryMovement employeeSalaryMovement : salaryMovementList) {
                    double amount;
                    if (employeeSalaryMovement.getCurrency().getSymbol().equalsIgnoreCase("$US")) {
                        amount = employeeSalaryMovement.getAmount().doubleValue() * generatedPayroll.getExchangeRate().getSale().doubleValue();
                    } else {
                        amount = employeeSalaryMovement.getAmount().doubleValue();
                    }
                    // only available for not active for tax payroll generation employees because they could get other incomes via other bonus
                    if (employeeSalaryMovement.getSalaryMovementType().getMovementType().equals(MovementType.OTHER_INCOME) && !activeForTaxPayrollGeneration) {
                        totalOtherIncomes += amount;
                    }
                }
                // round to 2 decimal points
                perMinuteDiscount = BigDecimalUtil.toBigDecimal(perMinuteDiscount).doubleValue();

                // Discount per lateness accumulated in the month
                if (tardinessTotal == 30) {
                    totalSumOfDiscountsPerLateness = basicSalary / 30 / 2;
                }
                if (tardinessTotal > 30 && tardinessTotal <= 60) {
                    totalSumOfDiscountsPerLateness = basicSalary / 30;
                }
                if (tardinessTotal > 60 && tardinessTotal <= 90) {
                    totalSumOfDiscountsPerLateness = basicSalary / 30 * 2;
                }
                if (tardinessTotal > 90) {
                    totalSumOfDiscountsPerLateness = basicSalary / 30 * 3;
                }

                int totalBandAbsenceMinutes = 0;
                for (Integer integer : totalSumOfMinuteBandAbsencesList) {
                    totalBandAbsenceMinutes += integer;
                }

                double absenceDiscount = dayAbsences * basicSalary / 30;

                //Todo this code part must be confirmed by customer
/*
                if ((absenceDiscount + tardinessTotal) > basicSalary * 0.2) {
                    absenceDiscount = basicSalary * 0.2 - tardinessTotal;
                }
*/

                totalSumOfDiscounts += totalSumOfDiscountsPerLateness + totalWinDiscount + totalOtherDiscount;

                CategoryTributaryPayroll categoryTributaryPayroll = null;
                CategoryFiscalPayroll categoryFiscalPayroll = null;

                if (activeForTaxPayrollGeneration) {
                    TributaryPayrollGenerator generator;
                    ExtraHoursWorked extraHoursWorked = extraHoursWorkedCache.get(currentJobContract.getId());
                    List<GrantedBonus> grantedBonuses = grantedBonusMap.get(currentJobContract.getId());
                    BigDecimal lastMonthBalance = BigDecimal.ZERO;
                    TributaryPayroll lastMonthTributaryPayroll
                            = lastMonthTributaryPayrollMap.get(employee.getId());
                    if (null != lastMonthTributaryPayroll && null != lastMonthTributaryPayroll.getDependentBalanceToNextMonth()) {
                        lastMonthBalance = lastMonthTributaryPayroll.getDependentBalanceToNextMonth();
                    }

                    generator = new TributaryPayrollGenerator(employee,
                            currentJobContract,
                            extraHoursWorked,
                            grantedBonuses,
                            seniorityBonus,
                            BigDecimalUtil.toBigDecimal(totalOtherIncomes),
                            workedDays,
                            gestionPayroll.getPayrollGenerationCycle().getEndDate(),
                            generatedPayroll.getPayrollGenerationCycle(),
                            invoicesFormMap.get(employee.getId()),
                            lastMonthBalance,
                            gestionPayroll);
                    categoryTributaryPayroll = generator.generate();
                    categoryTributaryPayroll.setGeneratedPayroll(generatedPayroll);
                    categoryTributaryPayroll.setNumber((long) (index + 1));
                    totalOtherIncomes = categoryTributaryPayroll.getTotalOtherIncomes().doubleValue();
                    totalRCIvaDiscount = categoryTributaryPayroll.getRetentionClearance().doubleValue();
                    totalAfpDiscount = categoryTributaryPayroll.getRetentionAFP().doubleValue();
                } else {
                    if (employee.getRetentionFlag()) {
                        BigDecimal amount = BigDecimalUtil.toBigDecimal((mensualTotalSalary + totalSumOfIncomesBeforeIva));
                        if (retentionValidatorService.applyRetention(employee, gestionPayroll, amount)) {
                            totalRCIvaDiscount = (mensualTotalSalary + totalOtherIncomes) * 0.155;
                        }
                    }
                }

                totalSumOfIncomesBeforeIva += totalOtherIncomes;
                // this discounts are applied directly to liquid
                totalSumOfIncomesOutOfIva += totalIncomeOutOfIva;

                totalSumOfDiscounts += totalRCIvaDiscount + totalAfpDiscount;

                mensualTotalSalary = BigDecimalUtil.toBigDecimal(mensualTotalSalary).doubleValue();


                /*TODO the process of collections have to be changed to take into account many contracts by employee*/
                /*------since here loan and advance discount-------*/
                BigDecimal liquid = BigDecimalUtil.toBigDecimal(mensualTotalSalary + totalSumOfIncomesBeforeIva + totalSumOfIncomesOutOfIva - totalSumOfDiscounts);
                if (liquid.doubleValue() >= 0) {
                    BigDecimal amountToPay = quotaService.sumResidueToCollectByPayrollEmployeeAndJobCategory(employee, gestionPayroll);
                    if (amountToPay.doubleValue() > 0) {
                        log.debug("***********************************************************************");
                        log.debug("empleado:" + employee.getFullName());
                        log.debug("sector:" + gestionPayroll.getJobCategory().getSector().getName());
                        log.debug("pagar: " + amountToPay);
                        List<Quota> quotaList = quotaService.findQuotaToCollectByPayrollEmployeeAndJobCategory(employee, gestionPayroll);
                        Double maxDiscount = liquid.doubleValue();
                        log.debug("max to discount:" + maxDiscount);
                        for (int i = 0; (i < quotaList.size() && maxDiscount >= 0); i++) {
                            Quota quota = quotaList.get(i);
                            double quotaDiscountAmount = quota.getResidue().doubleValue();
                            BigDecimal exchangeRate = BigDecimal.ONE;
                            double discount;
                            /* this assume the currency to be dollar instead of bs*/
                            if (quota.getCurrency() != FinancesCurrencyType.P) {
                                quotaDiscountAmount = quota.getResidue().doubleValue() * generatedPayroll.getExchangeRate().getSale().doubleValue();
                                exchangeRate = generatedPayroll.getExchangeRate().getSale();
                            }
                            /* Discount all if its possible, if not discount only a part*/
                            if (maxDiscount >= quotaDiscountAmount) {
                                discount = quotaDiscountAmount;
                            } else {
                                discount = maxDiscount;
                            }
                            totalSumOfDiscounts += discount;
                            if (quota.getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.ADVANCE)) {
                                totalAdvanceDiscount += discount;
                            }
                            if (quota.getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.LOAN)) {
                                totalLoanDiscount += discount;
                            }
                            if (quota.getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.OTHER_RECEIVABLES)) {
                                if (PayrollColumnType.WIN.equals(quota.getRotatoryFund().getDocumentType().getPayrollColumnType())) {
                                    totalWinDiscount += discount;
                                } else if (PayrollColumnType.OTHER_DISCOUNTS.equals(quota.getRotatoryFund().getDocumentType().getPayrollColumnType())) {
                                    totalOtherDiscount += discount;
                                }
                            }

                            maxDiscount -= discount;
                            RotatoryFundCollection rotatoryFundCollection = rotatoryFundCollectionService.buildRotatoryFundCollection(
                                    gestionPayroll, quota, exchangeRate, discount, FinancesCurrencyType.P);
                            newRotatoryFundCollectionList.add(rotatoryFundCollection);
                        }
                    }
                }
                totalSumOfDiscounts = BigDecimalUtil.toBigDecimal(totalSumOfDiscounts).doubleValue();
                /*----------until here compute of loan and advance discounts---------*/

                ManagersPayroll managersPayroll = new ManagersPayroll();
                managersPayroll.setActiveForTaxPayrollGeneration(activeForTaxPayrollGeneration);
                managersPayroll.setContractInitDate(currentJobContract.getContract().getInitDate());
                managersPayroll.setContractEndDate(currentJobContract.getContract().getEndDate());
                managersPayroll.setGeneratedPayroll(generatedPayroll);
                managersPayroll.setEmployee(employee);
                managersPayroll.setWorkedDays(BigDecimalUtil.toBigDecimal(workedDays - dayAbsences));
                managersPayroll.setSalary(BigDecimalUtil.toBigDecimal(basicSalary));
                managersPayroll.setBasicIncome(BigDecimalUtil.toBigDecimal(mensualTotalSalary));
                managersPayroll.setOtherIncomes(BigDecimalUtil.toBigDecimal(totalSumOfIncomesBeforeIva));
                managersPayroll.setTotalIncome(BigDecimalUtil.toBigDecimal(mensualTotalSalary + totalSumOfIncomesBeforeIva));
                managersPayroll.setTardinessMinutesDiscount(BigDecimalUtil.toBigDecimal(totalSumOfDiscountsPerLateness));
                managersPayroll.setDifference(BigDecimalUtil.toBigDecimal(mensualTotalSalary + totalSumOfIncomesBeforeIva - totalSumOfDiscounts));
                managersPayroll.setIvaRetention(BigDecimalUtil.toBigDecimal(totalRCIvaDiscount));
                managersPayroll.setIncomeOutOfIva(BigDecimalUtil.toBigDecimal(totalSumOfIncomesOutOfIva));
                managersPayroll.setDiscountsOutOfRetention(BigDecimalUtil.toBigDecimal(totalSumOfDiscounts));
                managersPayroll.setLiquid(BigDecimalUtil.toBigDecimal(mensualTotalSalary + totalSumOfIncomesBeforeIva
                        + totalSumOfIncomesOutOfIva - totalSumOfDiscounts));
                managersPayroll.setContractMode(currentJobContract.getContract().getContractMode().getName());
                if (currentJobContract.getJob().getJobCategory() != null) {
                    managersPayroll.setKindOfEmployee(currentJobContract.getJob().getJobCategory().getName());
                }
                managersPayroll.setIvaResidue(BigDecimalUtil.toBigDecimal(ivaResidue));
                managersPayroll.setLastIvaResidue(BigDecimalUtil.toBigDecimal(lastIvaResidue));
                managersPayroll.setLaboralTotal(BigDecimalUtil.toBigDecimal(0));
                managersPayroll.setProHome(BigDecimalUtil.toBigDecimal(proHome));
                managersPayroll.setInsurance(BigDecimalUtil.toBigDecimal(0));
                managersPayroll.setTotalDiscount(BigDecimalUtil.toBigDecimal(totalSumOfDiscounts));
                managersPayroll.setBandAbsenceMinutes(totalBandAbsenceMinutes);
                managersPayroll.setAbsenceMinutesDiscount(BigDecimalUtil.toBigDecimal(absenceDiscount));
                managersPayroll.setTardinessMinutes(cumulativeMinutesIntheMonth4AllContracts);
                managersPayroll.setWinDiscount(BigDecimalUtil.toBigDecimal(totalWinDiscount));
                managersPayroll.setLoanDiscount(BigDecimalUtil.toBigDecimal(totalLoanDiscount));
                managersPayroll.setAdvanceDiscount(BigDecimalUtil.toBigDecimal(totalAdvanceDiscount));
                managersPayroll.setOtherDiscounts(BigDecimalUtil.toBigDecimal(totalOtherDiscount));
                managersPayroll.setAfp(null != categoryTributaryPayroll && categoryTributaryPayroll.getRetentionAFP().compareTo(BigDecimal.ZERO) == 1 ?
                        categoryTributaryPayroll.getRetentionAFP() : BigDecimalUtil.toBigDecimal(0));
                managersPayroll.setRciva(BigDecimalUtil.toBigDecimal(totalRCIvaDiscount));
                managersPayroll.setUnit(currentJobContract.getJob().getOrganizationalUnit().getName());
                OrganizationalLevel areaLevel = findOrganizationalLevelByName("AREA");
                OrganizationalUnit areaOrganizationalUnit = findFather(currentJobContract.getJob().getOrganizationalUnit(),
                        areaLevel);
                if (areaOrganizationalUnit != null) {
                    managersPayroll.setArea(areaOrganizationalUnit.getName());
                }
                managersPayroll.setJob(currentJobContract.getJob().getCharge().getName());
                managersPayroll.setCategory(currentJobContract.getJob().getJobCategory().getAcronym());

                managersPayroll.setBusinessUnit(currentJobContract.getJob().getOrganizationalUnit().getBusinessUnit());
                managersPayroll.setCostCenter(currentJobContract.getJob().getOrganizationalUnit().getCostCenter());
                managersPayroll.setCharge(currentJobContract.getJob().getCharge());
                managersPayroll.setJobCategory(currentJobContract.getJob().getJobCategory());

                if (activeForTaxPayrollGeneration) {
                    FiscalPayrollGenerator fiscalPayrollGenerator;

                    BigDecimal defaultHourDayPayment = null;
                    try {
                        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
                        defaultHourDayPayment = companyConfiguration.getHrsWorkingDay();
                    } catch (CompanyConfigurationNotFoundException e) {
                        log.error("company companyConfiguration was not found", e);
                    }
                    fiscalPayrollGenerator = new FiscalPayrollGenerator(employee,
                            currentJobContract,
                            categoryTributaryPayroll,
                            managersPayroll,
                            gestionPayroll,
                            defaultHourDayPayment,
                            workedDays);
                    categoryFiscalPayroll = fiscalPayrollGenerator.generate();
                    categoryFiscalPayroll.setGeneratedPayroll(generatedPayroll);
                    categoryFiscalPayroll.setNumber((long) (index + 1));
                    // to avoid loose of precision
                    if (Math.abs(managersPayroll.getLiquid().doubleValue() - categoryFiscalPayroll.getLiquidPayment().doubleValue()) <= 0.05) {
                        managersPayroll.setLiquid(categoryFiscalPayroll.getLiquidPayment());
                    }
                }
                em.persist(managersPayroll);
                if (activeForTaxPayrollGeneration) {
                    em.persist(categoryTributaryPayroll);
                    categoryFiscalPayroll.setCategoryTributaryPayroll(categoryTributaryPayroll);
                    em.persist(categoryFiscalPayroll);
                }
            }// end if contractList.size>0
            else {
                return PayrollGenerationResult.WITHOUT_CONTRACTS.assignResultData(employee.getIdNumberAndFullName());
            }
        }

        return PayrollGenerationResult.SUCCESS;
    }

    private HoraryBandContract getHasHorariEspecial(List<HoraryBandContract> horaryBandContractList4Contract) {
        for(HoraryBandContract horaryBandContract:horaryBandContractList4Contract)
        {
            if(horaryBandContract.getTypeHoraryBand() != null)
                return horaryBandContract;
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public PayrollGenerationResult fillProffesorsPayroll(GeneratedPayroll generatedPayroll, List<Employee> employeeList,
                                                         List<Date> specialDate4BusinessUnit,
                                                         Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                         Map<Long, List<Date>> specialDate4OrganizationalUnit,
                                                         Map<Long, Map<Date, List<TimeInterval>>> specialDateTimeForOrganizationalUnit, List<RotatoryFundCollection> newRotatoryFundCollectionList) {
        GestionPayroll gestionPayroll = generatedPayroll.getGestionPayroll();
        // iterates each employee
        int index = 0;

        String out = "\n\nBEGIN fillProffesorsPayroll";

        for (Employee employee : employeeList) {
            index++;
            log.debug("Proccesing employee = " + employee.getId() + " index=" + index);
            JobContract currentJobContract;
            Double totalSumOfDiscounts;
            Double totalSumOfDiscountsBeforeIva;
            Double totalSumOfDiscountsPerLatenessAbsences;
            Double perHourDiscount = 0.0;
            Double perMinuteDiscount = 0.0;
            Double totalRCIvaDiscount = 0.0;
            // get the contracts, for the current employee, valid for the last gestionPayroll (year-month) entry
            List<Contract> employeeValidContractsList = contractService.getContractsForPayrollGeneration(gestionPayroll.getBusinessUnit(), gestionPayroll.getJobCategory(), employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

            out += "\n# Proccesing employee = " + employee.getId() + " index=" + index;
            out += "\n\t* employeeValidContractsList.size = " + employeeValidContractsList.size();

            if (!employeeValidContractsList.isEmpty()) {
                Double totalIncomeOutOfIva = 0.0;
                Double totalOtherDiscount = 0.0;

                Double totalDiscountOutOfIva = 0.0;
                Double totalOtherIncome = 0.0;

                Double mensualTotalSalary = 0.0;
                Integer mensualAbsenceMinutes = 0;
                Double mensualAbsenceDiscount = 0.0;

                //sum of Lists
                Integer cumulativeMinutesIntheMonth4AllContracts = 0;
                List<Integer> cumulativeMinutesIntheMonthByContractList = new ArrayList();
                List<Integer> cumulativePerformanceMinutesIntheMonthByContractList = new ArrayList();
                List<Integer> cumulativeAbsenceMinutesIntheMonthByContractList = new ArrayList();
                List<Double> contractsPriceList = new ArrayList();

                // AttendanceControl
                Calendar initDate = Calendar.getInstance();
                initDate.setTime(gestionPayroll.getInitDate());
                Calendar endDate = Calendar.getInstance();
                endDate.setTime(gestionPayroll.getEndDate());

                // The current values of HoraryBandContract for payroll generation
                List<HoraryBandContract> hourlyBandContract4EmployeeList = horaryBandContractService.getValidHoraryBandContractsByEmployeeAndBusinessUnitAndJobCategory(employee, gestionPayroll.getBusinessUnit(), gestionPayroll.getJobCategory(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

                if (hourlyBandContract4EmployeeList.isEmpty()) {
                    return PayrollGenerationResult.WITHOUT_BANDS.assignResultData(employee.getIdNumberAndFullName());
                }

                currentJobContract = getJobContractForPayment(hourlyBandContract4EmployeeList);
                if (currentJobContract == null) {
                    Contract employeeContract = em.find(Contract.class, employeeValidContractsList.get(0).getId());
                    currentJobContract = em.find(JobContract.class, employeeContract.getJobContractList().get(0).getId());
                }

                hourlyBandContract4EmployeeList = cleanDuplicate(hourlyBandContract4EmployeeList);

                // The current values of RRMark for payroll generation
                Map<Date, List<Date>> rhMarkTimeDateMap4Employee = rhMarkService.getRHMarkDateTimeMapByDateRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

                // The current values of SpecialDate for payroll generation
                List<Date> specialDate4Employee = specialDateService.getSpecialDateRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());
                Map<Date, List<TimeInterval>> specialDateTime4Employee = specialDateService.getSpecialDateTimeRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

                // here is the control because, each contract may have different variables of cost
                for (Contract contract : employeeValidContractsList) {
                    List<HoraryBandContract> horaryBandContractList4Contract = filterBandsByContract(contract, hourlyBandContract4EmployeeList);

                    out += "\n\t* contract = " + contract.getId() + " horaryBandContractList4Contract.isEmpty() = " + horaryBandContractList4Contract.isEmpty();

                    if (!horaryBandContractList4Contract.isEmpty()) {

                        List<Integer> cumulativeMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Integer> cumulativePerformanceMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Integer> cumulativeAbsenceMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Integer> cumulativeLatenessMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);

//                        horaryBandContract = em.find(HoraryBandContract.class, horaryBandContract.getId());
                        //TODO this code part was replaced by currentJobContract usage
                        contract = em.find(Contract.class, contract.getId());
                        Job job = em.find(Job.class, contract.getJobContractList().get(0).getJob().getId());

                        if (!specialDate4OrganizationalUnit.containsKey(job.getOrganizationalUnit().getId())) {
                            specialDate4OrganizationalUnit.put(job.getOrganizationalUnit().getId(), specialDateService.getSpecialDateRange(currentJobContract.getJob().getOrganizationalUnit(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate()));
                        }
                        if (!specialDateTimeForOrganizationalUnit.containsKey(job.getOrganizationalUnit().getId())) {
                            specialDateTimeForOrganizationalUnit.put(job.getOrganizationalUnit().getId(), specialDateService.getSpecialDateTimeRange(job.getOrganizationalUnit(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate()));
                        }

                        if (job.getSalary().getCurrency().getSymbol().equalsIgnoreCase("$US")) {
                            //                        contractsPriceList.add(job.getSalary().getAmount() * generatedPayroll.getExchangeRate().getSale());
                            contractsPriceList.add(job.getSalary().getAmount().doubleValue());// all is in $us
                        } else {
                            contractsPriceList.add(job.getSalary().getAmount().doubleValue());
                        }

                        out += "\n\t* salaryAmount() = " + job.getSalary().getAmount().doubleValue();

                        // this var is to control the days of the month. It is initially setted to the first day of the month
                        Calendar currentDate = Calendar.getInstance();
                        currentDate.setTime(gestionPayroll.getInitDate());
                        currentDate.set(Calendar.MILLISECOND, 0);

                        executeAttendanceControlProffesors(endDate, currentDate,
                                generatedPayroll, employee,
                                horaryBandContractList4Contract,
                                rhMarkTimeDateMap4Employee,
                                specialDate4BusinessUnit,
                                specialDateTime4BusinessUnit,
                                specialDate4OrganizationalUnit.get(job.getOrganizationalUnit().getId()),
                                specialDateTimeForOrganizationalUnit.get(job.getOrganizationalUnit().getId()),
                                specialDate4Employee,
                                specialDateTime4Employee,
                                cumulativeMinutesIntheMonth4ContractList,
                                cumulativePerformanceMinutesIntheMonth4ContractList,
                                cumulativeAbsenceMinutesIntheMonth4ContractList,
                                cumulativeLatenessMinutesIntheMonth4ContractList);

                        int bandDuration = 0;
                        int performance = 0;
                        int absence = 0;
                        for (int k = 0; k < cumulativeMinutesIntheMonth4ContractList.size(); k++) {
                            bandDuration += cumulativeMinutesIntheMonth4ContractList.get(k);
                            performance += cumulativePerformanceMinutesIntheMonth4ContractList.get(k);
                            absence += cumulativeAbsenceMinutesIntheMonth4ContractList.get(k);
                        }
                        cumulativeMinutesIntheMonthByContractList.add(new Integer(bandDuration));
                        cumulativePerformanceMinutesIntheMonthByContractList.add(new Integer(performance));
                        cumulativeAbsenceMinutesIntheMonthByContractList.add(new Integer(absence));
                    }
                }

                out += "\n\t* contractsPriceList = " + contractsPriceList;

                for (int i = 0; i < cumulativeMinutesIntheMonthByContractList.size(); i++) {
                    Integer minutes = cumulativeMinutesIntheMonthByContractList.get(i);
                    Integer performanceMinutes = cumulativePerformanceMinutesIntheMonthByContractList.get(i);
                    Double pricePerPeriod = contractsPriceList.get(i);
                    mensualTotalSalary += (minutes * pricePerPeriod / 45);

                    out += "\n\t* (minutes[" + minutes + "] * pricePerPeriod[" + pricePerPeriod + "] / 45) = " + (minutes * pricePerPeriod / 45);

                    mensualAbsenceDiscount += cumulativeAbsenceMinutesIntheMonthByContractList.get(i) * pricePerPeriod / 45;
                    mensualAbsenceMinutes += cumulativeAbsenceMinutesIntheMonthByContractList.get(i);
                    cumulativeMinutesIntheMonth4AllContracts += minutes - performanceMinutes;
                    perMinuteDiscount += ((minutes - performanceMinutes) * pricePerPeriod / 45);
                }
                out += "\n\t* mensualTotalSalary =" + mensualTotalSalary + "\n";
//                perMinuteDiscount = cumulativeMinutesIntheMonth4AllContracts * (mensualTotalSalary / 30 / 30 / 2);
                // round to 2 decimal points
                perMinuteDiscount = BigDecimalUtil.toBigDecimal(perMinuteDiscount).doubleValue();

                //todo other discounts must be taken into account
                totalSumOfDiscountsPerLatenessAbsences = perHourDiscount + perMinuteDiscount;

                // calculate discounts
                // Salary movement list of employee that has relationship with gestion payroll
                List<SalaryMovement> salaryMovementList = salaryMovementService.findByEmployeeAndGestionPayroll(employee, gestionPayroll);

                for (SalaryMovement employeeSalaryMovement : salaryMovementList) {
                    double amount = 0;
                    if (employeeSalaryMovement.getCurrency().getSymbol().equalsIgnoreCase("$US")) {
                        amount = employeeSalaryMovement.getAmount().doubleValue();
                    } else {
                        if (employeeSalaryMovement.getCurrency().getSymbol().equalsIgnoreCase("BS")) {
                            amount = employeeSalaryMovement.getAmount().doubleValue() / generatedPayroll.getExchangeRate().getSale().doubleValue();
                        }
                    }
                    if (employeeSalaryMovement.getSalaryMovementType().getMovementType().equals(MovementType.OTHER_DISCOUNT)) {
                        totalOtherDiscount += amount;
                    }
                    if (employeeSalaryMovement.getSalaryMovementType().getMovementType().equals(MovementType.OTHER_INCOME)) {
                        totalOtherIncome += amount;
                    }
                    if (employeeSalaryMovement.getSalaryMovementType().getMovementType().equals(MovementType.DISCOUNT_OUT_OF_RETENTION)) {
                        totalDiscountOutOfIva += amount;
                    }
                }
                /* This field is used to determine whether to apply retention over loan and advance,
                 by default do not apply  unless specified in companyConfiguration*/
                boolean applyRetentionToLoanAndAdvance = false;
                try {
                    CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
                    applyRetentionToLoanAndAdvance = companyConfiguration.isRetentionForLoanAndAdvance();
                } catch (CompanyConfigurationNotFoundException e) {
                    log.error("company companyConfiguration was not found", e);
                }

                totalSumOfDiscountsBeforeIva = totalSumOfDiscountsPerLatenessAbsences + totalOtherDiscount + mensualAbsenceDiscount;
                //todo here type of contract
                if (employee.getRetentionFlag()) {
                    BigDecimal amount = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(((mensualTotalSalary + totalOtherIncome) - totalSumOfDiscountsBeforeIva)),
                            generatedPayroll.getExchangeRate().getSale());
                    if (retentionValidatorService.applyRetention(employee, gestionPayroll, amount)) {
                        totalRCIvaDiscount = ((mensualTotalSalary + totalOtherIncome) - totalSumOfDiscountsBeforeIva) * 0.155;
                    }
                }
                totalSumOfDiscounts = totalSumOfDiscountsBeforeIva + totalRCIvaDiscount + totalDiscountOutOfIva;
                totalSumOfDiscounts = BigDecimalUtil.toBigDecimal(totalSumOfDiscounts).doubleValue();

                mensualTotalSalary = BigDecimalUtil.toBigDecimal(mensualTotalSalary).doubleValue();
                mensualAbsenceDiscount = BigDecimalUtil.toBigDecimal(mensualAbsenceDiscount).doubleValue();
                // If the employee have presented invoices so his fiscal credit should be calculated

                totalSumOfDiscounts = BigDecimalUtil.toBigDecimal(totalSumOfDiscounts).doubleValue();
                /*----------until here compute of loan and advance discounts---------*/

                GeneralPayroll generalPayroll = new GeneralPayroll();
                generalPayroll.setGeneratedPayroll(generatedPayroll);
                generalPayroll.setEmployee(employee);
                generalPayroll.setWorkedDays(30);
                generalPayroll.setSalary(BigDecimalUtil.toBigDecimal(mensualTotalSalary));
                generalPayroll.setTotalIncome(BigDecimalUtil.toBigDecimal(mensualTotalSalary));
                generalPayroll.setAbsenceTotalDiscount(BigDecimalUtil.toBigDecimal(totalSumOfDiscountsPerLatenessAbsences));
                generalPayroll.setDifference(BigDecimalUtil.toBigDecimal(mensualTotalSalary + totalOtherIncome - totalSumOfDiscountsBeforeIva));
                generalPayroll.setIvaRetention(BigDecimalUtil.toBigDecimal(totalRCIvaDiscount));
                generalPayroll.setIncomeOutOfIva(BigDecimalUtil.toBigDecimal(totalIncomeOutOfIva));
                generalPayroll.setOtherIncomes(BigDecimalUtil.toBigDecimal(totalOtherIncome));
                generalPayroll.setDiscountsOutOfRetention(BigDecimalUtil.toBigDecimal(totalDiscountOutOfIva));
                generalPayroll.setLiquid((BigDecimalUtil.toBigDecimal((mensualTotalSalary + totalOtherIncome) - totalSumOfDiscounts + totalIncomeOutOfIva)));
                generalPayroll.setContractMode(currentJobContract.getContract().getContractMode().getName());
                if (currentJobContract.getJob().getJobCategory() != null) {
                    generalPayroll.setKindOfEmployee(currentJobContract.getJob().getJobCategory().getName());
                }
                generalPayroll.setOtherDiscounts(BigDecimalUtil.toBigDecimal(totalOtherDiscount));
                generalPayroll.setTotalDiscount(BigDecimalUtil.toBigDecimal(totalSumOfDiscounts));
                generalPayroll.setAbsenceMinutes(cumulativeMinutesIntheMonth4AllContracts);
                generalPayroll.setAbsenceMinutesDiscount(BigDecimalUtil.toBigDecimal(perMinuteDiscount));
                generalPayroll.setAbsenceTotalDiscount(BigDecimalUtil.toBigDecimal(totalSumOfDiscountsPerLatenessAbsences));
                generalPayroll.setTardiness(BigDecimalUtil.toBigDecimal(totalSumOfDiscountsPerLatenessAbsences));
                generalPayroll.setAbsenceminut(mensualAbsenceMinutes);
                generalPayroll.setAbsencediscount(BigDecimalUtil.toBigDecimal(mensualAbsenceDiscount));
                generalPayroll.setTotalperiodearned(BigDecimalUtil.toBigDecimal(mensualTotalSalary));
                generalPayroll.setTotalperiodworked(BigDecimalUtil.toBigDecimal(mensualTotalSalary - mensualAbsenceDiscount));
                generalPayroll.setControlType(BooleanUtils.toInteger(employee.getControlFlag()));

                generalPayroll.setCostCenter(currentJobContract.getJob().getOrganizationalUnit().getCostCenter());
                generalPayroll.setBusinessUnit(currentJobContract.getJob().getOrganizationalUnit().getBusinessUnit());
                generalPayroll.setCharge(currentJobContract.getJob().getCharge());
                generalPayroll.setJobCategory(currentJobContract.getJob().getJobCategory());

                em.persist(generalPayroll);
//                em.flush();
            }// end if contractlist.size>0
            else {
                return PayrollGenerationResult.WITHOUT_CONTRACTS.assignResultData(employee.getIdNumberAndFullName());
            }
        }
//        }
//        closeConnection(connection);
        log.debug(out);
        return PayrollGenerationResult.SUCCESS;
    }

    private void executeAttendanceControlManagersRotation(Calendar endDate, Calendar currentDate, GeneratedPayroll generatedPayroll,
                                                  Employee employee,
                                                  HoraryBandContract validDayHoraryBandContract4Date,
                                                  Map<Date, List<Date>> rhMarkTimeDateMap4Employee,
                                                  List<Date> specialDate4BusinessUnit,
                                                  Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                  List<Date> specialDate4OrganizationalUnit,
                                                  Map<Date, List<TimeInterval>> specialDateTimeForOrganizationalUnit,
                                                  List<Date> specialDate4Employee,
                                                  Map<Date, List<TimeInterval>> specialDateTime4Employee,
                                                  List<Integer> cumulativeMinutesIntheMonth4ContractList,
                                                  List<Integer> cumulativePerformanceMinutesIntheMonth4ContractList,
                                                  List<Integer> cumulativeLatenessMinutesIntheMonth4ContractList,
                                                  List<Double> cumulativeDayAbsencesIntheMonth4ContractList,
                                                  List<Integer> totalSumOfMinuteBandAbsencesList) {
        double perMinuteSalary = 0;

        // iterate all days of the gestion including the last day
        while (currentDate.compareTo(endDate) <= 0) {
            Integer cumulativeMinuteBandAbsencesInADay = 0;
            Integer cumulativeNumberBandAbsencesInADay = 0;
            Double dayAbsences = 0.0;
            Integer minutosRetrasoAcum = 0;
            Integer cumulativeMinuteLatenessInADay4AllContractBands = 0;
            Integer cumulativeBandsDurationInADay4AllContractBands = 0;
            //al parecer controla q sea diferente de lunes
            if (currentDate.get(Calendar.DAY_OF_WEEK) != 1) {
                // all special DATES 4 this month
                List<Date> holydaySpecialDateList = new ArrayList<Date>();
                boolean hasPermission4Today = false;
                if (specialDate4BusinessUnit.contains(currentDate.getTime()) ||
                        specialDate4OrganizationalUnit.contains(currentDate.getTime()) ||
                        specialDate4Employee.contains(currentDate.getTime())) {
                    hasPermission4Today = true;
                }

                List<Date> dateTimeRHMarkList = filterDateTimeRHMarkByDate(rhMarkTimeDateMap4Employee, currentDate.getTime());

                /*// find the valid HoraryBandContract list for a specific Date. Because may exist horary changes
                List<HoraryBandContract> validHoraryBandContract4DateList = horaryBandContractService.findValidHoraryBandContracts4Date(horaryBandContractList4Contract, currentDate);
//                map HoraryBandContract by day for a given date
                Hashtable<Integer, List<HoraryBandContract>> horaryBandContractMapByDay = horaryBandContractService.getHoraryBandContractMapByDay(validHoraryBandContract4DateList);
                // lista de bandas horarias contrato por dia
                List<HoraryBandContract> validDayHoraryBand4DateList = horaryBandContractMapByDay.get(currentDate.get(Calendar.DAY_OF_WEEK));
                // check what bands are valid for this date of the month. Checks all the valid bands for the month.*/

                int bandAbsences = 0;

                for (int k = 0; k < validDayHoraryBandContract4Date.getTypeHoraryBand().getHoraryBands().size(); k++) {
                    HoraryBand horaryBand = validDayHoraryBandContract4Date.getTypeHoraryBand().getHoraryBands().get(k);
                    int minutosAcumaladosRestraso = 0;
                    int minuteBandAbsences = 0;
                    int bandsNumber = validDayHoraryBandContract4Date.getTypeHoraryBand().getHoraryBands().size();

                    // check if the employee marked this date at this band period and retrive his marks as a list
                    List<Date> correctMarks = findInitEndRHMarks(dateTimeRHMarkList,horaryBand,validDayHoraryBandContract4Date.getTolerance(),validDayHoraryBandContract4Date.getLimit(), currentDate);
                    Calendar initBandHourCalendar = DateUtils.toCalendar(horaryBand.getInitHour());
                    Calendar endBandHourCalendar = DateUtils.toCalendar(horaryBand.getEndHour());
                    List<Long> bandDifferenceList = this.getDifferenceInHoursMinutesSecondsBetweenMarks(
                            initBandHourCalendar, endBandHourCalendar);
                    Long bandDifferenceInMinutes = bandDifferenceList.get(0) * 60 + bandDifferenceList.get(1);
                    Integer bandDuration = new Integer(bandDifferenceInMinutes.intValue());
                    cumulativeBandsDurationInADay4AllContractBands += bandDuration;
                    boolean hasPermission4BandInterval = hasPermissionForBandInterval(
                            currentDate,
                            specialDateTime4BusinessUnit,
                            specialDateTimeForOrganizationalUnit,
                            specialDateTime4Employee,
                            horaryBand);
                    log.debug("hasPermission4BandInterval: " + hasPermission4BandInterval);
                    // if it isn't sunday
                    if (!hasPermission4Today) {
                        // if there are no valid quantity of marks for the HoraryBand It is absence.
                        // si es A procede a descontar la banda si no tiene ambas marcas
                        if (employee.getControlFlag() && correctMarks.size() < 2) {
                            //discount HoraryBand duration
                            if (!hasPermission4BandInterval) {
                                cumulativeMinuteBandAbsencesInADay += bandDuration;
                                cumulativeNumberBandAbsencesInADay++;

                                // compute the absences only in the last band
                                bandAbsences++;
                            }
                            if ((k == (bandsNumber - 1)) && (bandAbsences > 0)) {
                                if (bandAbsences < bandsNumber) {
                                    dayAbsences += 0.5;
                                }
                                if (bandAbsences == bandsNumber) {
                                    if (bandsNumber == 1) {
                                        if (bandDuration >= (8 * 60)) {
                                            dayAbsences++;
                                        } else {
                                            dayAbsences += 0.5;
                                        }
                                    } else {
                                        dayAbsences += 1;
                                    }
                                }
                            }
                            if (!hasPermission4BandInterval) {
                                minuteBandAbsences = bandDuration;
                                totalSumOfMinuteBandAbsencesList.add(minuteBandAbsences);
                            }
                        }
                        // check in case of lateness
                        // si es d no descuenta nada
                        if (employee.getControlFlag() && !hasPermission4BandInterval && correctMarks.size() >= 1) {
                            Calendar employeeInitMarkCalendar = DateUtils.toCalendar(correctMarks.get(0));
                            // set year and month in case marTime saves only time mark
                            employeeInitMarkCalendar.set(Calendar.YEAR, correctMarks.get(0).getYear());
                            employeeInitMarkCalendar.set(Calendar.MONTH, correctMarks.get(0).getMonth());

                            // checks if the employee out of the tolerance range at income in order to apply discounts
                            if (!this.isMarkInToleranceRange(validDayHoraryBandContract4Date.getTolerance().getBeforeInit(),
                                    validDayHoraryBandContract4Date.getTolerance().getAfterInit(), initBandHourCalendar, employeeInitMarkCalendar)) {
                                // aplly discount
                                // gets the differences between employees init mark and HoraryBand init time
                                List<Long> differenceList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                        initBandHourCalendar, employeeInitMarkCalendar);
                                Long differenceInHours = differenceList.get(0);
                                Long differenceInMinutes = differenceList.get(1);
                                // sum of hour and minutes in a day to be shown in the payroll
                                Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                                boolean isNegative = false;
                                if (cumulativeDifferenceInMinutes < 0) {
                                    isNegative = true;
                                }
                                minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                                cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);
                                // in case that the tardiness is at left side of init band mark
                                int tolerance = 0;
                                if (isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getBeforeInit();
                                }
                                // in case that the tardiness is at right side of init band mark
                                if (!isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getAfterInit();
                                }
                                if (cumulativeDifferenceInMinutes.intValue() > tolerance) {
                                    cumulativeMinuteLatenessInADay4AllContractBands += cumulativeDifferenceInMinutes.intValue();
                                    minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                                }
                            }

                            // cast to calendar employees end mark
                            Calendar employeeEndMarkCalendar = DateUtils.toCalendar(correctMarks.get(1));
                            // set year and month in case marTime saves only time mark
                            employeeEndMarkCalendar.set(Calendar.YEAR, correctMarks.get(1).getYear());
                            employeeEndMarkCalendar.set(Calendar.MONTH, correctMarks.get(1).getMonth());

                            // discount in outcome
                            if (!this.isMarkInToleranceRange(validDayHoraryBandContract4Date.getTolerance().getBeforeEnd(),
                                    validDayHoraryBandContract4Date.getTolerance().getAfterEnd(), endBandHourCalendar, employeeEndMarkCalendar)) {
                                // aplly discount
                                // gets the differences between employees end mark and HoraryBand end time
                                List<Long> differenceList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                        endBandHourCalendar, employeeEndMarkCalendar);
                                Long differenceInHours = differenceList.get(0);
                                Long differenceInMinutes = differenceList.get(1);
                                Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                                boolean isNegative = false;
                                if (cumulativeDifferenceInMinutes < 0) {
                                    isNegative = true;
                                }
                                cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);

                                int tolerance = 0;
                                if (isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getBeforeEnd();
                                }
                                // in case that the tardiness is at right side of init band mark
                                if (!isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getAfterEnd();
                                }
                                if (cumulativeDifferenceInMinutes.intValue() > tolerance) {
                                    cumulativeMinuteLatenessInADay4AllContractBands += cumulativeDifferenceInMinutes.intValue();
                                    minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                                }
                            }
                        }// end if corect marks control
                        minutosRetrasoAcum += minutosAcumaladosRestraso;
                        registerControlReportManagers(minutosAcumaladosRestraso, correctMarks, dateTimeRHMarkList,
                                generatedPayroll, validDayHoraryBandContract4Date, employee, currentDate,
                                minuteBandAbsences, cumulativeNumberBandAbsencesInADay,
                                perMinuteSalary, bandDuration);
                    }// end if every other day
                    else {
                        registerControlReportManagers(minutosAcumaladosRestraso, correctMarks, dateTimeRHMarkList,
                                generatedPayroll, validDayHoraryBandContract4Date, employee, currentDate,
                                minuteBandAbsences, 0,
                                perMinuteSalary, bandDuration);
                    }
                }// end if holiday control
            } // end if is not sunday
            int bandDuration = cumulativeBandsDurationInADay4AllContractBands;
            int tardiness = cumulativeMinuteLatenessInADay4AllContractBands;
            int performance = bandDuration - tardiness;

            cumulativeDayAbsencesIntheMonth4ContractList.add(dayAbsences);
            cumulativeLatenessMinutesIntheMonth4ContractList.add(tardiness);
            cumulativeMinutesIntheMonth4ContractList.add(bandDuration);
            cumulativePerformanceMinutesIntheMonth4ContractList.add(performance);

            // go ahead one step in the day of the month for the next iteration
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
        }// end for iterate days of gestion
    }

    private void executeAttendanceControlManagers(Calendar endDate, Calendar currentDate, GeneratedPayroll generatedPayroll,
                                                  Employee employee,
                                                  List<HoraryBandContract> horaryBandContractList4Contract,
                                                  Map<Date, List<Date>> rhMarkTimeDateMap4Employee,
                                                  List<Date> specialDate4BusinessUnit,
                                                  Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                  List<Date> specialDate4OrganizationalUnit,
                                                  Map<Date, List<TimeInterval>> specialDateTimeForOrganizationalUnit,
                                                  List<Date> specialDate4Employee,
                                                  Map<Date, List<TimeInterval>> specialDateTime4Employee,
                                                  List<Integer> cumulativeMinutesIntheMonth4ContractList,
                                                  List<Integer> cumulativePerformanceMinutesIntheMonth4ContractList,
                                                  List<Integer> cumulativeLatenessMinutesIntheMonth4ContractList,
                                                  List<Double> cumulativeDayAbsencesIntheMonth4ContractList,
                                                  List<Integer> totalSumOfMinuteBandAbsencesList) {
        double perMinuteSalary = 0;

        // iterate all days of the gestion including the last day
        while (currentDate.compareTo(endDate) <= 0) {
            Integer cumulativeMinuteBandAbsencesInADay = 0;
            Integer cumulativeNumberBandAbsencesInADay = 0;
            Double dayAbsences = 0.0;
            Integer minutosRetrasoAcum = 0;
            Integer cumulativeMinuteLatenessInADay4AllContractBands = 0;
            Integer cumulativeBandsDurationInADay4AllContractBands = 0;
            //al parecer controla q sea diferente de lunes
            if (currentDate.get(Calendar.DAY_OF_WEEK) != 1) {
                // all special DATES 4 this month
                List<Date> holydaySpecialDateList = new ArrayList<Date>();
                boolean hasPermission4Today = false;
                if (specialDate4BusinessUnit.contains(currentDate.getTime()) ||
                        specialDate4OrganizationalUnit.contains(currentDate.getTime()) ||
                        specialDate4Employee.contains(currentDate.getTime())) {
                    hasPermission4Today = true;
                }

                List<Date> dateTimeRHMarkList = filterDateTimeRHMarkByDate(rhMarkTimeDateMap4Employee, currentDate.getTime());

                // find the valid HoraryBandContract list for a specific Date. Because may exist horary changes
                List<HoraryBandContract> validHoraryBandContract4DateList = horaryBandContractService.findValidHoraryBandContracts4Date(horaryBandContractList4Contract, currentDate);
//                map HoraryBandContract by day for a given date
                Hashtable<Integer, List<HoraryBandContract>> horaryBandContractMapByDay = horaryBandContractService.getHoraryBandContractMapByDay(validHoraryBandContract4DateList);
                // lista de bandas horarias contrato por dia
                List<HoraryBandContract> validDayHoraryBand4DateList = horaryBandContractMapByDay.get(currentDate.get(Calendar.DAY_OF_WEEK));
                // check what bands are valid for this date of the month. Checks all the valid bands for the month.

                int bandAbsences = 0;

                for (int k = 0; k < validDayHoraryBand4DateList.size(); k++) {
                    HoraryBandContract validDayHoraryBandContract4Date = validDayHoraryBand4DateList.get(k);
                    int minutosAcumaladosRestraso = 0;
                    int minuteBandAbsences = 0;
                    int bandsNumber = validDayHoraryBand4DateList.size();

                    // check if the employee marked this date at this band period and retrive his marks as a list
                    List<Date> correctMarks = findInitEndRHMarks(dateTimeRHMarkList, validDayHoraryBandContract4Date, currentDate);
                    Calendar initBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract4Date.getHoraryBand().getInitHour());
                    Calendar endBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract4Date.getHoraryBand().getEndHour());
                    List<Long> bandDifferenceList = this.getDifferenceInHoursMinutesSecondsBetweenMarks(
                            initBandHourCalendar, endBandHourCalendar);
                    Long bandDifferenceInMinutes = bandDifferenceList.get(0) * 60 + bandDifferenceList.get(1);
                    Integer bandDuration = new Integer(bandDifferenceInMinutes.intValue());
                    cumulativeBandsDurationInADay4AllContractBands += bandDuration;
                    boolean hasPermission4BandInterval = hasPermissionForBandInterval(
                            currentDate,
                            specialDateTime4BusinessUnit,
                            specialDateTimeForOrganizationalUnit,
                            specialDateTime4Employee,
                            validDayHoraryBandContract4Date);
                    log.debug("hasPermission4BandInterval: " + hasPermission4BandInterval);
                    // if it isn't sunday
                    if (!hasPermission4Today) {
                        // if there are no valid quantity of marks for the HoraryBand It is absence.
                        // si es A procede a descontar la banda si no tiene ambas marcas
                        if (employee.getControlFlag() && correctMarks.size() < 2) {
                            //discount HoraryBand duration
                            if (!hasPermission4BandInterval) {
                                cumulativeMinuteBandAbsencesInADay += bandDuration;
                                cumulativeNumberBandAbsencesInADay++;

                                // compute the absences only in the last band
                                bandAbsences++;
                            }
                            if ((k == (bandsNumber - 1)) && (bandAbsences > 0)) {
                                if (bandAbsences < bandsNumber) {
                                    dayAbsences += 0.5;
                                }
                                if (bandAbsences == bandsNumber) {
                                    if (bandsNumber == 1) {
                                        if (bandDuration >= (8 * 60)) {
                                            dayAbsences++;
                                        } else {
                                            dayAbsences += 0.5;
                                        }
                                    } else {
                                        dayAbsences += 1;
                                    }
                                }
                            }
                            if (!hasPermission4BandInterval) {
                                minuteBandAbsences = bandDuration;
                                totalSumOfMinuteBandAbsencesList.add(minuteBandAbsences);
                            }
                        }
                        // check in case of lateness
                        // si es d no descuenta nada
                        if (employee.getControlFlag() && !hasPermission4BandInterval && correctMarks.size() >= 1) {
                            Calendar employeeInitMarkCalendar = DateUtils.toCalendar(correctMarks.get(0));
                            // set year and month in case marTime saves only time mark
                            employeeInitMarkCalendar.set(Calendar.YEAR, correctMarks.get(0).getYear());
                            employeeInitMarkCalendar.set(Calendar.MONTH, correctMarks.get(0).getMonth());

                            // checks if the employee out of the tolerance range at income in order to apply discounts
                            if (!this.isMarkInToleranceRange(validDayHoraryBandContract4Date.getTolerance().getBeforeInit(),
                                    validDayHoraryBandContract4Date.getTolerance().getAfterInit(), initBandHourCalendar, employeeInitMarkCalendar)) {
                                // aplly discount
                                // gets the differences between employees init mark and HoraryBand init time
                                List<Long> differenceList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                        initBandHourCalendar, employeeInitMarkCalendar);
                                Long differenceInHours = differenceList.get(0);
                                Long differenceInMinutes = differenceList.get(1);
                                // sum of hour and minutes in a day to be shown in the payroll
                                Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                                boolean isNegative = false;
                                if (cumulativeDifferenceInMinutes < 0) {
                                    isNegative = true;
                                }
                                minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                                cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);
                                // in case that the tardiness is at left side of init band mark
                                int tolerance = 0;
                                if (isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getBeforeInit();
                                }
                                // in case that the tardiness is at right side of init band mark
                                if (!isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getAfterInit();
                                }
                                if (cumulativeDifferenceInMinutes.intValue() > tolerance) {
                                    cumulativeMinuteLatenessInADay4AllContractBands += cumulativeDifferenceInMinutes.intValue();
                                    minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                                }
                            }

                            // cast to calendar employees end mark
                            Calendar employeeEndMarkCalendar = DateUtils.toCalendar(correctMarks.get(1));
                            // set year and month in case marTime saves only time mark
                            employeeEndMarkCalendar.set(Calendar.YEAR, correctMarks.get(1).getYear());
                            employeeEndMarkCalendar.set(Calendar.MONTH, correctMarks.get(1).getMonth());

                            // discount in outcome
                            if (!this.isMarkInToleranceRange(validDayHoraryBandContract4Date.getTolerance().getBeforeEnd(),
                                    validDayHoraryBandContract4Date.getTolerance().getAfterEnd(), endBandHourCalendar, employeeEndMarkCalendar)) {
                                // aplly discount
                                // gets the differences between employees end mark and HoraryBand end time
                                List<Long> differenceList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                        endBandHourCalendar, employeeEndMarkCalendar);
                                Long differenceInHours = differenceList.get(0);
                                Long differenceInMinutes = differenceList.get(1);
                                Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                                boolean isNegative = false;
                                if (cumulativeDifferenceInMinutes < 0) {
                                    isNegative = true;
                                }
                                cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);

                                int tolerance = 0;
                                if (isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getBeforeEnd();
                                }
                                // in case that the tardiness is at right side of init band mark
                                if (!isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getAfterEnd();
                                }
                                if (cumulativeDifferenceInMinutes.intValue() > tolerance) {
                                    cumulativeMinuteLatenessInADay4AllContractBands += cumulativeDifferenceInMinutes.intValue();
                                    minutosAcumaladosRestraso = Math.abs(cumulativeDifferenceInMinutes.intValue());
                                }
                            }
                        }// end if corect marks control
                        minutosRetrasoAcum += minutosAcumaladosRestraso;
                        registerControlReportManagers(minutosAcumaladosRestraso, correctMarks, dateTimeRHMarkList,
                                generatedPayroll, validDayHoraryBandContract4Date, employee, currentDate,
                                minuteBandAbsences, cumulativeNumberBandAbsencesInADay,
                                perMinuteSalary, bandDuration);
                    }// end if every other day
                    else {
                        registerControlReportManagers(minutosAcumaladosRestraso, correctMarks, dateTimeRHMarkList,
                                generatedPayroll, validDayHoraryBandContract4Date, employee, currentDate,
                                minuteBandAbsences, 0,
                                perMinuteSalary, bandDuration);
                    }
                }// end if holiday control
            } // end if is not sunday
            int bandDuration = cumulativeBandsDurationInADay4AllContractBands;
            int tardiness = cumulativeMinuteLatenessInADay4AllContractBands;
            int performance = bandDuration - tardiness;

            cumulativeDayAbsencesIntheMonth4ContractList.add(dayAbsences);
            cumulativeLatenessMinutesIntheMonth4ContractList.add(tardiness);
            cumulativeMinutesIntheMonth4ContractList.add(bandDuration);
            cumulativePerformanceMinutesIntheMonth4ContractList.add(performance);

            // go ahead one step in the day of the month for the next iteration
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
        }// end for iterate days of gestion
    }

    public void executeAttendanceControlProffesors(Calendar endDate, Calendar currentDate, GeneratedPayroll generatedPayroll,
                                                   Employee employee,
                                                   List<HoraryBandContract> horaryBandContractList4Contract,
                                                   Map<Date, List<Date>> rhMarkTimeDateMap4Employee,
                                                   List<Date> specialDate4BusinessUnit,
                                                   Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                   List<Date> specialDate4OrganizationalUnit,
                                                   Map<Date, List<TimeInterval>> specialDateTimeForOrganizationalUnit,
                                                   List<Date> specialDate4Employee,
                                                   Map<Date, List<TimeInterval>> specialDateTime4Employee,
                                                   List<Integer> cumulativeMinutesIntheMonth4ContractList,
                                                   List<Integer> cumulativePerformanceMinutesIntheMonth4ContractList,
                                                   List<Integer> cumulativeAbsenceMinutesIntheMonth4ContractList,
                                                   List<Integer> cumulativeLatenessMinutesIntheMonth4ContractList) {

        // iterate all days of the gestion including the last day
        while (currentDate.compareTo(endDate) <= 0) {
            // count how many halfDay discounts the employee has accumulated this date
            // esto acumula atrasos actualmente
            Integer cumulativeMinuteAbsencesInADay4AllContractBands = 0;
            Integer cumulativeAbsence = 0;
//            int daybandAbsence=0;
            Integer cumulativeMinutePerformanceInADay4AllContractBands = 0;
            Integer cumulativeBandsDurationInADay4AllContractBands = 0;

            // if it isn't sunday
            if (currentDate.get(Calendar.DAY_OF_WEEK) != 1) {
                // se esta asignado el permiso a todos los contrato para que no falle, pero hay q corregir
                boolean hasPermission4Today = false;
                if (specialDate4BusinessUnit.contains(currentDate.getTime()) ||
                        specialDate4OrganizationalUnit.contains(currentDate.getTime()) ||
                        specialDate4Employee.contains(currentDate.getTime())) {
                    hasPermission4Today = true;
                }


                List<Date> dateTimeRHMarkList = filterDateTimeRHMarkByDate(rhMarkTimeDateMap4Employee, currentDate.getTime());
                // find the valid HoraryBandContract list for a specific Date. Because may exist horary changes
                List<HoraryBandContract> validHoraryBandContract4DateList = horaryBandContractService.findValidHoraryBandContracts4Date(horaryBandContractList4Contract, currentDate);

//                validHoraryBandContract4DateList=joinConsecutiveBands(validHoraryBandContract4DateList);
                // map for tolerances of HoraryBand
                Hashtable<Integer, List<HoraryBandContract>> horaryBandContractMapByDay = horaryBandContractService.getHoraryBandContractMapByDay(validHoraryBandContract4DateList);
                // lista de bandas horarias contrato por dia
                List<HoraryBandContract> validDayHoraryBand4DateList = horaryBandContractMapByDay.get(currentDate.get(Calendar.DAY_OF_WEEK));
                for (HoraryBandContract validDayHoraryBandContract4Date : validDayHoraryBand4DateList) {
                    boolean hasPermission4BandInterval = hasPermissionForBandInterval(
                            currentDate,
                            specialDateTime4BusinessUnit,
                            specialDateTimeForOrganizationalUnit,
                            specialDateTime4Employee,
                            validDayHoraryBandContract4Date);
                    log.debug("hasPermission4BandInterval: " + hasPermission4BandInterval);

                    int minutosAcumalados = 0;
                    int ausenciabanda = 0;
                    // check if the employee marked this date at this band period and retrive his marks as a list
                    List<Date> correctMarks = findInitEndRHMarks(dateTimeRHMarkList, validDayHoraryBandContract4Date, currentDate);
                    Calendar initBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract4Date.getHoraryBand().getInitHour());
                    Calendar endBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract4Date.getHoraryBand().getEndHour());

                    if (!employee.getControlFlag() || validDayHoraryBandContract4Date.getTimeType().equalsIgnoreCase("HCL") || hasPermission4Today || hasPermission4BandInterval) {
                        //pay for the band duration
                        Integer bandDuration = validDayHoraryBandContract4Date.getHoraryBand().getDuration();
                        cumulativeBandsDurationInADay4AllContractBands += bandDuration;
                    } else {
                        Integer bandDuration = validDayHoraryBandContract4Date.getHoraryBand().getDuration();
                        cumulativeBandsDurationInADay4AllContractBands += bandDuration;
                        // if the list size is less than 2 there are no valid marks for the HoraryBand
                        if (correctMarks.size() < 2) {
                            cumulativeAbsence += bandDuration;
                            ausenciabanda = bandDuration;
                        }

                        if (correctMarks.size() >= 2) {
                            Calendar employeeInitMarkCalendar = DateUtils.toCalendar(correctMarks.get(0));
                            // set year and month in case marTime saves only time mark
                            employeeInitMarkCalendar.set(Calendar.YEAR, correctMarks.get(0).getYear());
                            employeeInitMarkCalendar.set(Calendar.MONTH, correctMarks.get(0).getMonth());
                            // cast to calendar employees end mark
                            Calendar employeeEndMarkCalendar = DateUtils.toCalendar(correctMarks.get(1));
                            // set year and month in case marTime saves only time mark
                            employeeEndMarkCalendar.set(Calendar.YEAR, correctMarks.get(1).getYear());
                            employeeEndMarkCalendar.set(Calendar.MONTH, correctMarks.get(1).getMonth());

                            Long cumulativeDifferenceInMinutesIncomeTime = (long) 0;
                            // checks if the employee out of the tolerance range at income in order to apply discounts
                            if (!this.isMarkInToleranceRange(validDayHoraryBandContract4Date.getTolerance().getBeforeInit(),
                                    validDayHoraryBandContract4Date.getTolerance().getAfterInit(), initBandHourCalendar, employeeInitMarkCalendar)) {

                                // aplly discount
                                // gets the differences between employees init mark and HoraryBand init time
                                List<Long> differenceList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                        initBandHourCalendar, employeeInitMarkCalendar);
                                Long differenceInHours = differenceList.get(0);
                                Long differenceInMinutes = differenceList.get(1);
                                // sum of hour and minutes in a day to be shown in the payroll
                                // , because there is no discount rule per hours
                                Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                                boolean isNegative = false;
                                if (cumulativeDifferenceInMinutes < 0) {
                                    isNegative = true;
                                }
                                // ensure that the discount is no more than band duration
                                if (Math.abs(cumulativeDifferenceInMinutes) > bandDuration) {
                                    cumulativeDifferenceInMinutes = bandDuration.longValue();
                                }
                                minutosAcumalados = Math.abs(cumulativeDifferenceInMinutes.intValue());
                                cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);
                                // in case that the tardiness is at left side of init band mark
                                int tolerance = 0;
                                if (isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getBeforeInit();
                                }
                                // in case that the tardiness is at right side of init band mark
                                if (!isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getAfterInit();
                                }

                                if (cumulativeDifferenceInMinutes.intValue() > tolerance) {
                                    cumulativeMinuteAbsencesInADay4AllContractBands += cumulativeDifferenceInMinutes.intValue() - tolerance;
                                    minutosAcumalados = Math.abs(cumulativeDifferenceInMinutes.intValue()) - tolerance;
                                }
                                cumulativeDifferenceInMinutesIncomeTime = new Long(minutosAcumalados);
                            }
                            if (!this.isMarkInToleranceRange(validDayHoraryBandContract4Date.getTolerance().getBeforeEnd(),
                                    validDayHoraryBandContract4Date.getTolerance().getAfterEnd(), endBandHourCalendar, employeeEndMarkCalendar)) {
                                // apply discount
                                // gets the differences between employees end mark and HoraryBand end time
                                List<Long> differenceList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                        endBandHourCalendar, employeeEndMarkCalendar);
                                Long differenceInHours = differenceList.get(0);
                                Long differenceInMinutes = differenceList.get(1);

                                Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                                boolean isNegative = false;
                                if (cumulativeDifferenceInMinutes < 0) {
                                    isNegative = true;
                                }
                                cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);
                                Long cumulativeDifferenceInMinutesInOutcomeTime = cumulativeDifferenceInMinutesIncomeTime + cumulativeDifferenceInMinutes;

                                int tolerance = 0;
                                if (isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getAfterEnd();
                                }
                                // in case that the tardiness is at right side of init band mark
                                if (!isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getBeforeEnd();
                                }

                                if (cumulativeDifferenceInMinutes.intValue() > tolerance) {
                                    cumulativeDifferenceInMinutes = cumulativeDifferenceInMinutes - tolerance;
                                }
                                // ensure that the discount is no more than band duration
                                if ((cumulativeDifferenceInMinutes + minutosAcumalados) > bandDuration) {
                                    cumulativeDifferenceInMinutes = bandDuration.longValue()
                                            - minutosAcumalados;
                                }
                                cumulativeMinuteAbsencesInADay4AllContractBands += cumulativeDifferenceInMinutes.intValue();
                                minutosAcumalados += cumulativeDifferenceInMinutes;
                            }
                        }// end if corect marks control
                    }// end if holiday control

                    registerControlReportProffesors(minutosAcumalados, ausenciabanda, correctMarks, dateTimeRHMarkList,
                            generatedPayroll, validDayHoraryBandContract4Date, employee, currentDate, 0,
                            (validDayHoraryBandContract4Date.getJobContract().getJob().getSalary().getAmount().doubleValue() / 45),
                            validDayHoraryBandContract4Date.getHoraryBand().getDuration(),
                            false);
                }// end for dayHoraryBand
//                }// end if holiday control
            } // end if is not sunday
            int cumBandDuration = cumulativeBandsDurationInADay4AllContractBands;
            if (!employee.getControlFlag()) {
                cumulativeMinuteAbsencesInADay4AllContractBands = 0;
                cumulativeAbsence = 0;
            }
            int tardiness = cumulativeMinuteAbsencesInADay4AllContractBands;
            int absence = cumulativeAbsence;
            int performance = cumBandDuration - tardiness;
            cumulativeMinutesIntheMonth4ContractList.add(cumBandDuration);
            cumulativePerformanceMinutesIntheMonth4ContractList.add(performance);
            cumulativeAbsenceMinutesIntheMonth4ContractList.add(absence);
            // go ahead one step in the day of the month for the next iteration
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
        }// end for iterate days of month
    }

    private boolean hasPermissionForBandInterval(Calendar currentDate,
                                                 Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                 Map<Date, List<TimeInterval>> specialDateTimeForOrganizationalUnit,
                                                 Map<Date, List<TimeInterval>> specialDateTime4Employee,
                                                 HoraryBand horaryBand) {
        if (specialDateTime4BusinessUnit.containsKey(currentDate.getTime())) {
            for (TimeInterval timeInterval : specialDateTime4BusinessUnit.get(currentDate.getTime())) {

                if (hasTimeIntervalPermissionForBand(horaryBand, timeInterval)) {
                    return true;
                }
            }
        }
        if (specialDateTime4Employee.containsKey(currentDate.getTime())) {
            for (TimeInterval timeInterval : specialDateTime4Employee.get(currentDate.getTime())) {

                if (hasTimeIntervalPermissionForBand(horaryBand, timeInterval)) {
                    return true;
                }
            }
        }
        if (specialDateTimeForOrganizationalUnit.containsKey(currentDate.getTime())) {
            for (TimeInterval timeInterval : specialDateTimeForOrganizationalUnit.get(currentDate.getTime())) {

                if (hasTimeIntervalPermissionForBand(horaryBand, timeInterval)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasPermissionForBandInterval(Calendar currentDate,
                                                 Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                 Map<Date, List<TimeInterval>> specialDateTimeForOrganizationalUnit,
                                                 Map<Date, List<TimeInterval>> specialDateTime4Employee,
                                                 HoraryBandContract validDayHoraryBandContract4Date) {
        if (specialDateTime4BusinessUnit.containsKey(currentDate.getTime())) {
            for (TimeInterval timeInterval : specialDateTime4BusinessUnit.get(currentDate.getTime())) {
                HoraryBand horaryBand = validDayHoraryBandContract4Date.getHoraryBand();
                if (hasTimeIntervalPermissionForBand(horaryBand, timeInterval)) {
                    return true;
                }
            }
        }
        if (specialDateTime4Employee.containsKey(currentDate.getTime())) {
            for (TimeInterval timeInterval : specialDateTime4Employee.get(currentDate.getTime())) {
                HoraryBand horaryBand = validDayHoraryBandContract4Date.getHoraryBand();
                if (hasTimeIntervalPermissionForBand(horaryBand, timeInterval)) {
                    return true;
                }
            }
        }
        if (specialDateTimeForOrganizationalUnit.containsKey(currentDate.getTime())) {
            for (TimeInterval timeInterval : specialDateTimeForOrganizationalUnit.get(currentDate.getTime())) {
                HoraryBand horaryBand = validDayHoraryBandContract4Date.getHoraryBand();
                if (hasTimeIntervalPermissionForBand(horaryBand, timeInterval)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void executeAttendanceControlFiscalProffesors(Calendar endDate, Calendar currentDate, GeneratedPayroll generatedPayroll,
                                                          Employee employee,
                                                          HoraryBandContract horaryBandContractList4Contract,
                                                          Map<Date, List<Date>> rhMarkTimeDateMap4Employee,
                                                          List<Date> specialDate4BusinessUnit,
                                                          Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                          List<Date> specialDate4OrganizationalUnit,
                                                          Map<Date, List<TimeInterval>> specialDateTimeForOrganizationalUnit,
                                                          List<Date> specialDate4Employee,
                                                          Map<Date, List<TimeInterval>> specialDateTime4Employee,
                                                          List<Integer> cumulativeBandDurationMinutesInTheMonth4ContractList,
                                                          List<Integer> cumulativePerformanceMinutesIntheMonth4ContractList,
                                                          List<Integer> cumulativeAbsenceMinutesIntheMonth4ContractList,
                                                          List<Integer> cumulativeLatenessMinutesIntheMonth4ContractList) {

        // iterate all days of the gestion including the last day
        while (currentDate.compareTo(endDate) <= 0) {
            // count how many halfDay discounts the employee has accumulated this date
            // esto acumula atrasos actualmente
            Integer cumulativeAbsence = 0;
            Integer cumulativeMinuteLatenessInADay4AllContractBands = 0;
            Integer cumulativeMinuteAbsencesInADay4AllContractBands = 0;
            Integer cumulativeMinutePerformanceInADay4AllContractBands = 0;
            Integer cumulativeBandsDurationInADay4AllContractBands = 0;

            // if it isn't sunday
            if (currentDate.get(Calendar.DAY_OF_WEEK) != 1) {
                boolean hasPermission4Today = false;
                if (specialDate4BusinessUnit.contains(currentDate.getTime()) ||
                        specialDate4OrganizationalUnit.contains(currentDate.getTime()) ||
                        specialDate4Employee.contains(currentDate.getTime())) {
                    hasPermission4Today = true;
                }

                List<Date> dateTimeRHMarkList = filterDateTimeRHMarkByDate(rhMarkTimeDateMap4Employee, currentDate.getTime());
                // find the valid HoraryBandContract list for a specific Date. Because may exist horary changes
                HoraryBandContract validHoraryBandContract4Date = horaryBandContractService.findValidHoraryBand4Date(horaryBandContractList4Contract, currentDate);

                List<HoraryBandContract> horaryBandContractList = new ArrayList<HoraryBandContract>();
                if (null != validHoraryBandContract4Date) {
                    horaryBandContractList.add(validHoraryBandContract4Date);
                }
                // map for tolerances of HoraryBand
                Hashtable<Integer, List<HoraryBandContract>> horaryBandContractMapByDay = horaryBandContractService.getHoraryBandContractMapByDay(horaryBandContractList);
                // lista de bandas horarias contrato por dia
                List<HoraryBandContract> validDayHoraryBand4DateList = horaryBandContractMapByDay.get(currentDate.get(Calendar.DAY_OF_WEEK));
                log.debug("procesing bands for date and day [size]: " + validDayHoraryBand4DateList.size());
                log.debug("date: " + currentDate.getTime());
                for (HoraryBandContract horaryBandContract : validDayHoraryBand4DateList) {
                    log.debug("day :" + horaryBandContract.getHoraryBand().getInitDay());
                }
                for (HoraryBandContract validDayHoraryBandContract4Date : validDayHoraryBand4DateList) {
                    boolean hasPermission4BandInterval = hasPermissionForBandInterval(
                            currentDate,
                            specialDateTime4BusinessUnit,
                            specialDateTimeForOrganizationalUnit,
                            specialDateTime4Employee,
                            validDayHoraryBandContract4Date);
                    log.debug("hasPermission4BandInterval: " + hasPermission4BandInterval);

                    int minutosAcumalados = 0;
                    int ausenciabanda = 0;
                    // check if the employee marked this date at this band period and retrive his marks as a list
                    List<Date> correctMarks = findInitEndRHMarks(dateTimeRHMarkList, validDayHoraryBandContract4Date, currentDate);
                    Calendar initBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract4Date.getHoraryBand().getInitHour());
                    Calendar endBandHourCalendar = DateUtils.toCalendar(validDayHoraryBandContract4Date.getHoraryBand().getEndHour());

                    Integer bandDuration = validDayHoraryBandContract4Date.getHoraryBand().getDuration();
                    cumulativeBandsDurationInADay4AllContractBands += bandDuration;
                    if (!employee.getControlFlag() || validDayHoraryBandContract4Date.getTimeType().equalsIgnoreCase("HCL") || hasPermission4Today || hasPermission4BandInterval) {
                        //pay for the band duration
                        cumulativeMinutePerformanceInADay4AllContractBands += bandDuration;
                    } else {
                        // if the list size is less than 2 there are no valid marks for the HoraryBand
                        if (correctMarks.size() < 2) {
                            cumulativeAbsence += bandDuration;
                            cumulativeMinuteAbsencesInADay4AllContractBands += bandDuration;
                            ausenciabanda = bandDuration;
                            log.debug("missing band absence");
                        }

                        if (correctMarks.size() >= 2) {
                            log.debug("performing band ok present");
                            Calendar employeeInitMarkCalendar = DateUtils.toCalendar(correctMarks.get(0));
                            // set year and month in case marTime saves only time mark
                            employeeInitMarkCalendar.set(Calendar.YEAR, correctMarks.get(0).getYear());
                            employeeInitMarkCalendar.set(Calendar.MONTH, correctMarks.get(0).getMonth());
                            // cast to calendar employees end mark
                            Calendar employeeEndMarkCalendar = DateUtils.toCalendar(correctMarks.get(1));
                            // set year and month in case marTime saves only time mark
                            employeeEndMarkCalendar.set(Calendar.YEAR, correctMarks.get(1).getYear());
                            employeeEndMarkCalendar.set(Calendar.MONTH, correctMarks.get(1).getMonth());

                            Long cumulativeDifferenceInMinutesIncomeTime = (long) 0;
                            // checks if the employee out of the tolerance range at income in order to apply discounts
                            if (!this.isMarkInToleranceRange(validDayHoraryBandContract4Date.getTolerance().getBeforeInit(),
                                    validDayHoraryBandContract4Date.getTolerance().getAfterInit(), initBandHourCalendar, employeeInitMarkCalendar)) {

                                // apply discount
                                // gets the differences between employees init mark and HoraryBand init time
                                List<Long> differenceList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                        initBandHourCalendar, employeeInitMarkCalendar);
                                Long differenceInHours = differenceList.get(0);
                                Long differenceInMinutes = differenceList.get(1);
                                // sum of hour and minutes in a day to be shown in the payroll
                                // , because there is no discount rule per hours
                                Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                                boolean isNegative = false;
                                if (cumulativeDifferenceInMinutes < 0) {
                                    isNegative = true;
                                    log.debug("differenceList:" + initBandHourCalendar.getTime() + " - " + employeeInitMarkCalendar.getTime());
                                    for (Long val : differenceList) {
                                        log.debug("val:" + val);
                                    }
                                    log.debug("negative difference: " + cumulativeDifferenceInMinutes);
                                }
                                // ensure that the discount is no more than band duration
                                if (Math.abs(cumulativeDifferenceInMinutes) > bandDuration) {
                                    cumulativeDifferenceInMinutes = bandDuration.longValue();
                                }
                                minutosAcumalados = Math.abs(cumulativeDifferenceInMinutes.intValue());
                                cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);
                                // in case that the tardiness is at left side of init band mark
                                int tolerance = 0;
                                if (isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getBeforeInit();
                                    log.debug("tolerance left side income:" + tolerance);
                                }
                                // in case that the tardiness is at right side of init band mark
                                if (!isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getAfterInit();
                                    log.debug("tolerance right side income:" + tolerance);
                                }

                                if (cumulativeDifferenceInMinutes.intValue() >= tolerance) {
//                                    cumulativeMinuteLatenessInADay4AllContractBands += cumulativeDifferenceInMinutes.intValue() - tolerance;
                                    minutosAcumalados = Math.abs(cumulativeDifferenceInMinutes.intValue()) - tolerance;
                                    log.debug("substract tolerance income: " + cumulativeDifferenceInMinutes + " - " + tolerance);
                                }
                                cumulativeDifferenceInMinutesIncomeTime = new Long(minutosAcumalados);
                            }
                            if (!this.isMarkInToleranceRange(validDayHoraryBandContract4Date.getTolerance().getBeforeEnd(),
                                    validDayHoraryBandContract4Date.getTolerance().getAfterEnd(), endBandHourCalendar, employeeEndMarkCalendar)) {
                                // apply discount
                                // gets the differences between employees end mark and HoraryBand end time
                                List<Long> differenceList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
                                        endBandHourCalendar, employeeEndMarkCalendar);
                                Long differenceInHours = differenceList.get(0);
                                Long differenceInMinutes = differenceList.get(1);

                                Long cumulativeDifferenceInMinutes = differenceInHours * 60 + differenceInMinutes;
                                boolean isNegative = false;
                                if (cumulativeDifferenceInMinutes < 0) {
                                    isNegative = true;
                                }
                                cumulativeDifferenceInMinutes = Math.abs(cumulativeDifferenceInMinutes);
                                Long cumulativeDifferenceInMinutesInOutcomeTime = cumulativeDifferenceInMinutesIncomeTime + cumulativeDifferenceInMinutes;

                                int tolerance = 0;
                                if (isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getAfterEnd();
                                }
                                // in case that the tardiness is at right side of init band mark
                                if (!isNegative) {
                                    tolerance = validDayHoraryBandContract4Date.getTolerance().getBeforeEnd();
                                }

                                if (cumulativeDifferenceInMinutes.intValue() >= tolerance) {
                                    log.debug("substract tolerance outcome: " + cumulativeDifferenceInMinutes + " - " + tolerance);
                                    cumulativeDifferenceInMinutes = cumulativeDifferenceInMinutes - tolerance;
                                }
                                // ensure that the discount is no more than band duration
                                if ((cumulativeDifferenceInMinutes + minutosAcumalados) > bandDuration) {
                                    cumulativeDifferenceInMinutes = bandDuration.longValue()
                                            - minutosAcumalados;
                                }
                                minutosAcumalados += cumulativeDifferenceInMinutes;
                            }
                            log.debug("minutosAcumalados: " + minutosAcumalados);
                            cumulativeMinuteLatenessInADay4AllContractBands += minutosAcumalados;
                        }// end if corect marks control
                    }// end if holiday control

                    registerControlReportProffesors(minutosAcumalados, ausenciabanda, correctMarks, dateTimeRHMarkList,
                            generatedPayroll, validDayHoraryBandContract4Date, employee, currentDate, 0,
                            0.0,
                            validDayHoraryBandContract4Date.getHoraryBand().getDuration(),
                            false);
                }// end for dayHoraryBand
//                }// end if holiday control
            } // end if is not sunday
            int cumBandDuration = cumulativeBandsDurationInADay4AllContractBands;
            if (!employee.getControlFlag()) {
                cumulativeMinuteLatenessInADay4AllContractBands = 0;
                cumulativeAbsence = 0;
            }
            int tardiness = cumulativeMinuteLatenessInADay4AllContractBands;
            int absence = cumulativeMinuteAbsencesInADay4AllContractBands;
            int performance = cumulativeMinutePerformanceInADay4AllContractBands;
            cumulativeBandDurationMinutesInTheMonth4ContractList.add(cumBandDuration);
            cumulativePerformanceMinutesIntheMonth4ContractList.add(performance);
            cumulativeAbsenceMinutesIntheMonth4ContractList.add(absence);
            cumulativeLatenessMinutesIntheMonth4ContractList.add(tardiness);
            // go ahead one step in the day of the month for the next iteration
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            log.debug("*************");
            log.debug("tardiness:" + tardiness);
            log.debug("absence:" + absence);
            log.debug("performance:" + performance);
        }// end for iterate days of month
    }

    private boolean hasTimeIntervalPermissionForBand(HoraryBand horaryBand, TimeInterval timeInterval) {
        return (horaryBand.getInitHour().compareTo(timeInterval.getStart()) >= 0 && horaryBand.getInitHour().compareTo(timeInterval.getEnd()) <= 0)
                || (horaryBand.getEndHour().compareTo(timeInterval.getStart()) >= 0 && horaryBand.getEndHour().compareTo(timeInterval.getEnd()) <= 0);
    }

    private List<Date> filterDateTimeRHMarkByDate(Map<Date, List<Date>> rhMarkTimeDateMap4Employee, Date currentDate) {
        if (rhMarkTimeDateMap4Employee.containsKey(currentDate)) {
            return rhMarkTimeDateMap4Employee.get(currentDate);
        }
        return new ArrayList<Date>();
    }

    public List<HoraryBandContract> filterBandsByContract(Contract contract, List<HoraryBandContract> hourlyBandContract4EmployeeList) {
        List<HoraryBandContract> returnList = new ArrayList<HoraryBandContract>();
        for (HoraryBandContract horaryBandContract : hourlyBandContract4EmployeeList) {
            horaryBandContract = em.find(HoraryBandContract.class, horaryBandContract.getId());
            if (horaryBandContract.getJobContract().getContract().getId().longValue() == contract.getId().longValue()) {
                returnList.add(horaryBandContract);
            }
        }
        return returnList;
    }

    public List<HoraryBandContract> cleanDuplicate(List<HoraryBandContract> horaryBandContractList) {
        List<HoraryBandContract> depuratedList = new ArrayList<HoraryBandContract>() {
            @Override
            public boolean contains(Object o) {
                HoraryBandContract horaryBandContract = ((HoraryBandContract) o);
                HoraryBand horaryBand = horaryBandContract.getHoraryBand();
                for (Object obj : toArray()) {
                    HoraryBandContract bandContract = ((HoraryBandContract) obj);
                    HoraryBand band = bandContract.getHoraryBand();

                    if (bandContract.getInitDate().compareTo(horaryBandContract.getInitDate()) == 0 &&
                            bandContract.getEndDate().compareTo(horaryBandContract.getEndDate()) == 0 &&
                            band.getInitDay().equalsIgnoreCase(horaryBand.getInitDay()) &&
                            band.getEndDay().equalsIgnoreCase(horaryBand.getEndDay()) &&
                            band.getInitHour().compareTo(horaryBand.getInitHour()) == 0 &&
                            band.getEndHour().compareTo(horaryBand.getEndHour()) == 0 &&
                            ((band.getDuration() != null && band.getDuration().intValue() == horaryBand.getDuration().intValue()) || (band.getDuration() == null && horaryBand.getDuration() == null)) &&
                            ((band.getEveryOtherDay() != null && band.getEveryOtherDay().intValue() == horaryBand.getEveryOtherDay().intValue()) || (band.getEveryOtherDay() == null && horaryBand.getEveryOtherDay() == null))) {
                        return true;
                    }
                }
                return false;
            }
        };

        for (HoraryBandContract horaryBandContract : horaryBandContractList) {
            if (!depuratedList.contains(horaryBandContract)) {
                depuratedList.add(horaryBandContract);
            }
        }
        return depuratedList;
    }


    public JobContract getJobContractForPayment(List<HoraryBandContract> hourlyBandContract4EmployeeList) {
        Long currentJobContractId = null;
        Long currentHoraryBandMax = null;
        Map<Long, Long> horaryBandTimes = new HashMap<Long, Long>() {
            @Override
            public Long put(Long key, Long value) {
                Long currentValue = get(key);
                return super.put(key, (currentValue != null ? currentValue : 0) + value);
            }
        };

        for (HoraryBandContract horaryBandContract : hourlyBandContract4EmployeeList) {
            Long minutes = DateUtils.differenceBetween(horaryBandContract.getHoraryBand().getInitHour(), horaryBandContract.getHoraryBand().getEndHour(), TimeUnit.MINUTES);
            horaryBandTimes.put(horaryBandContract.getJobContractId(), minutes);
        }

        for (Map.Entry<Long, Long> horaryBandTime : horaryBandTimes.entrySet()) {
            if (null == currentHoraryBandMax || horaryBandTime.getValue() > currentHoraryBandMax) {
                currentJobContractId = horaryBandTime.getKey();
                currentHoraryBandMax = horaryBandTime.getValue();
            }
        }

        return null != currentJobContractId ? em.find(JobContract.class, currentJobContractId) : null;
    }


    public OrganizationalUnit findFather(OrganizationalUnit organizationalUnit, OrganizationalLevel organizationalLevel) {
        if (organizationalUnit != null && organizationalUnit.getOrganizationalUnitRoot() != null) {
            System.out.println("organizationalLevel.getName() --> " + organizationalLevel.getName());
            System.out.println("organizationalUnit.getName() --> " + organizationalUnit.getName());
            System.out.println("organizationalUnit.getOrganizationalUnitRoot().getName() --> " + organizationalUnit.getOrganizationalUnitRoot().getName());
            System.out.println("organizationalUnit.getOrganizationalUnitRoot().getOrganizationalLevel().getName() --> " + organizationalUnit.getOrganizationalUnitRoot().getOrganizationalLevel().getName());

            if (organizationalUnit.getOrganizationalUnitRoot().getOrganizationalLevel().getName().equals(organizationalLevel.getName())) {
                return organizationalUnit.getOrganizationalUnitRoot();
            } else {
                while (organizationalUnit.getOrganizationalUnitRoot() != null) {
                    return findFather(organizationalUnit.getOrganizationalUnitRoot(), organizationalLevel);
                }
            }
        }
        return null;
    }


    /*
    * @param validHoraryBandContract4DateList   list to join consecutive bands
    * */

    private List<HoraryBandContract> joinConsecutiveBands(List<HoraryBandContract> validHoraryBandContract4DateList) {
        List<HoraryBandContract> validHoraryBandContract4DateJoinedList = new ArrayList<HoraryBandContract>();
        /*sort by init hour basicaly*/
        Collections.sort(validHoraryBandContract4DateList);
        for (int j = 0; j < validHoraryBandContract4DateList.size(); j++) {
            HoraryBandContract hbc = validHoraryBandContract4DateList.get(j);
            for (int k = j + 1; k < validHoraryBandContract4DateList.size(); k++) {
                HoraryBandContract hbc2 = validHoraryBandContract4DateList.get(k);
                if (hbc.getHoraryBand().getEndHour() == hbc2.getHoraryBand().getInitHour()) {
                    hbc.getHoraryBand().setEndHour(hbc2.getHoraryBand().getEndHour());
                    validHoraryBandContract4DateList.remove(hbc2);
                    k--;
                }
            }
        }
        return validHoraryBandContract4DateList;
    }

    /*
    * @param validHoraryBandContract4DateList   list to exclude the inclusive bands.
    * */

    private List<HoraryBandContract> showInclusiveBands(List<HoraryBandContract> validHoraryBandContract4DateList) {
        List<HoraryBandContract> validHoraryBandContract4DateJoinedList = new ArrayList<HoraryBandContract>();
        Collections.sort(validHoraryBandContract4DateList);
        for (int j = 0; j < validHoraryBandContract4DateList.size(); j++) {
            HoraryBandContract hbc1 = validHoraryBandContract4DateList.get(j);
            HoraryBand horaryBand1 = hbc1.getHoraryBand();
            for (int k = j + 1; k < validHoraryBandContract4DateList.size(); k++) {
                HoraryBandContract hbc2 = validHoraryBandContract4DateList.get(k);
                HoraryBand horaryBand2 = hbc2.getHoraryBand();
                if ((horaryBand1.getEndHour().before(horaryBand2.getEndHour())) ||
                        (horaryBand1.getEndHour() == horaryBand2.getEndHour())) {
                    /*show the band conflict*/

                    validHoraryBandContract4DateList.remove(hbc2);
                    k--;
                }
            }
        }
        return validHoraryBandContract4DateList;
    }

    private void registerControlReportManagers(int minutesAcumulated, List<Date> correctMarks, List<Date> dateRhMarkList,
                                               GeneratedPayroll generatedPayroll, HoraryBandContract validDayHoraryBandContract4Date, Employee employee,
                                               Calendar controlDayOfMonth,
                                               Integer minuteBandAbsences, Integer cumulativeNumberBandAbsencesInADay,
                                               Double perMinuteSalary, Integer bandDuration) {
        Date initMark = null;
        Date endMark = null;
        if (correctMarks != null) {
            if (correctMarks.size() == 1) {
                initMark = correctMarks.get(0);
            }
            if (correctMarks.size() == 2) {
                initMark = correctMarks.get(0);
                endMark = correctMarks.get(1);
            }
        }
        String marcaciones = "";
        for (Date rhMarkDateTime : dateRhMarkList) {
            marcaciones = marcaciones + " ; " + rhMarkDateTime.toString();
        }
        // drop the first semicolon
        if (marcaciones.length() > 3) {
            marcaciones = marcaciones.substring(3);
        }
        ControlReport controlReport;
        controlReport = new ControlReport();
        controlReport.setGeneratedPayroll(generatedPayroll);
        controlReport.setHoraryBandContract(validDayHoraryBandContract4Date);
        controlReport.setDate(controlDayOfMonth.getTime());
        controlReport.setInitMark(initMark);
        controlReport.setEndMark(endMark);

        controlReport.setMinutesDiscount(minutesAcumulated);

        controlReport.setMarks(marcaciones);
        controlReport.setBandAbsence(minuteBandAbsences);
        controlReport.setNumberBandAbsences(cumulativeNumberBandAbsencesInADay);
//        controlReport.setBandAbsenceDiscount(cumulativeBandAbsencesInADay * perMinuteSalary);
        controlReport.setPerMinuteSalary(BigDecimalUtil.toBigDecimal(perMinuteSalary));
        controlReport.setPerBandSalary(BigDecimalUtil.toBigDecimal(perMinuteSalary * 45));
        // working time
        controlReport.setPerformanceMinutes(bandDuration - minutesAcumulated);
        controlReport.setDiscountAmount(BigDecimalUtil.toBigDecimal(minutesAcumulated * perMinuteSalary));
        controlReport.setPerformanceMinuteAmount(BigDecimalUtil.toBigDecimal((bandDuration - minutesAcumulated) * perMinuteSalary));
        controlReport.setControlType(BooleanUtils.toInteger(employee.getControlFlag()));
        em.persist(controlReport);
//        em.flush();
    }


    private void registerControlReportProffesors(int minutesAcumulated, int bandAbsence, List<Date> correctMarks, List<Date> dateRhMarkList,
                                                 GeneratedPayroll generatedPayroll, HoraryBandContract validDayHoraryBandContract4Date, Employee employee,
                                                 Calendar currentDate, Integer cumulativeNumberBandAbsencesInADay,
                                                 Double perMinuteSalary, Integer bandDuration,
                                                 boolean isDateHolyday) {
        Date initMark = null;
        Date endMark = null;
        if (correctMarks != null) {
            if (correctMarks.size() == 1) {
                initMark = correctMarks.get(0);
            }
            if (correctMarks.size() == 2) {
                initMark = correctMarks.get(0);
                endMark = correctMarks.get(1);
            }
        }
        String marcaciones = "";
        for (Date rhMarkDateTime : dateRhMarkList) {
            marcaciones = marcaciones + " ; " + rhMarkDateTime.toString();
        }
        // drop the first semicolon
        if (marcaciones.length() > 3) {
            marcaciones = marcaciones.substring(3);
        }
        ControlReport controlReport;
        controlReport = new ControlReport();
        controlReport.setGeneratedPayroll(generatedPayroll);
        controlReport.setHoraryBandContract(validDayHoraryBandContract4Date);
        controlReport.setDate(currentDate.getTime());
        controlReport.setInitMark(initMark);
        controlReport.setEndMark(endMark);

        controlReport.setMarks(marcaciones);
        int performance = bandDuration - bandAbsence;
        if (isDateHolyday) {
            minutesAcumulated = 0;
            bandAbsence = 0;
            performance = 0;
        }
        // descuento por banda
        controlReport.setBandAbsence(bandAbsence);
        controlReport.setNumberBandAbsences(cumulativeNumberBandAbsencesInADay);
        // total bandas trabajadas sin retrasos
        controlReport.setPerformanceMinutes(performance);
        // atrasos
        controlReport.setMinutesDiscount(minutesAcumulated);

        if (!PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(generatedPayroll.getGestionPayroll().getJobCategory().getPayrollGenerationType())) {
            controlReport.setBandAbsenceDiscount(BigDecimalUtil.toBigDecimal(bandAbsence * perMinuteSalary));
            controlReport.setPerMinuteSalary(BigDecimalUtil.toBigDecimal(perMinuteSalary));
            controlReport.setPerBandSalary(BigDecimalUtil.toBigDecimal(perMinuteSalary * 45));
            controlReport.setPerformanceMinuteAmount(BigDecimalUtil.toBigDecimal((performance) * perMinuteSalary));
            controlReport.setDiscountAmount(BigDecimalUtil.toBigDecimal((minutesAcumulated) * perMinuteSalary));
        }

        controlReport.setControlType(BooleanUtils.toInteger(employee.getControlFlag()));
        em.persist(controlReport);
//        em.flush();
    }

    /*
    Returns RegisterMarkAction list only if the employee has marked at least one time to in and one time to out his horaryBand.
     the list is a list of two elements, the employee first and last mark for the horaryBand.
   @param dateRhMarkList a list of marks of an employee given an especific date
   @param dayHoraryBand a HoraryBand of a especific day that matches with the day of the date of the dateRhMarkList
    */

    private List<RHMark> findInitEndRHMarksAdminB(List<RHMark> dateRhMarkList, HoraryBandContract dayHoraryBandContract, Calendar dayOfMonthCalendar) {
        // initially the list to be returned is empty.
        List<RHMark> marksList = new ArrayList<RHMark>();
        HoraryBand dayHoraryBand = dayHoraryBandContract.getHoraryBand();
        Tolerance tolerance = dayHoraryBandContract.getTolerance();
        Limit limit = dayHoraryBandContract.getLimit();
        // to catch the first and las ocurrence of marks
        RHMark nearestInitMark = null;
        RHMark nearestEndMark = null;
        // the employee at least have marked two times during the day.
        if (dateRhMarkList.size() >= 1) {
            Calendar initBandMark = Calendar.getInstance();
            Calendar endBandMark = Calendar.getInstance();
            // in order to make a comparison, normalize the year,month and date of the horary band, so that we check only the time not the date
            initBandMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                    dayHoraryBand.getInitHour().getHours(), dayHoraryBand.getInitHour().getMinutes(), dayHoraryBand.getInitHour().getSeconds());
            initBandMark.set(Calendar.MILLISECOND, 0);
            // in order to make a comparison, normalize the year,month and date of the horary band, so that we check only the time not the date
            endBandMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                    dayHoraryBand.getEndHour().getHours(), dayHoraryBand.getEndHour().getMinutes(), dayHoraryBand.getEndHour().getSeconds());
            endBandMark.set(Calendar.MILLISECOND, 0);
            Calendar beforeInitLimit = Calendar.getInstance();
            Calendar afterEndLimit = Calendar.getInstance();
            beforeInitLimit.setTime(initBandMark.getTime());
            beforeInitLimit.add(Calendar.MINUTE, -limit.getBeforeInit());
            afterEndLimit.setTime(endBandMark.getTime());
            afterEndLimit.add(Calendar.MINUTE, limit.getAfterEnd());
            Calendar lastInitEmployeeMark = null;
            Calendar lastEndEmployeeMark = null;


            for (RHMark rhMark : dateRhMarkList) {
                Calendar employeeMark = Calendar.getInstance();
                Date employeeMarkD = new Date((dayOfMonthCalendar.get(Calendar.YEAR) - 1900), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                        rhMark.getMarTime().getHours(), rhMark.getMarTime().getMinutes(), rhMark.getMarTime().getSeconds());


                // to ensure that the time mark has setted year and month. In case only the time is retrived.
                employeeMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                        rhMark.getMarTime().getHours(), rhMark.getMarTime().getMinutes(), rhMark.getMarTime().getSeconds());
                employeeMark.set(Calendar.MILLISECOND, 0);

                employeeMark.setTime(employeeMarkD);
                //for init mark
                if ((employeeMark.compareTo(beforeInitLimit) >= 0) && (employeeMark.compareTo(endBandMark) < 0)) {
                    // override nearest init mark if convenient
                    if (nearestInitMark == null) {
                        nearestInitMark = rhMark;
                        lastInitEmployeeMark = employeeMark;
                    } else {
                        int initBandEmployeeMarkDifference = ((Long) (Math.abs(initBandMark.getTimeInMillis() - employeeMark.getTimeInMillis()))).intValue();
                        int initBandNearestInitMarkDifference = ((Long) Math.abs(initBandMark.getTimeInMillis() - lastInitEmployeeMark.getTimeInMillis())).intValue();
                        if (initBandEmployeeMarkDifference < initBandNearestInitMarkDifference) {
                            // override with the nearest mark to the init HoraryBand
                            nearestInitMark = rhMark;
                            lastInitEmployeeMark = employeeMark;
                        }
                    }
                }
            }
            if (nearestInitMark != null) {
                marksList.add(nearestInitMark);
            }

        }
        return marksList;
    }

    /*
    Returns RegisterMarkAction list only if the employee has marked at least one time to in and one time to out his horaryBand.
     the list is a list of two elements, the employee first and last mark for the horaryBand.
   @param dateRhMarkList a list of marks of an employee given an especific date
   @param dayHoraryBand a HoraryBand of a especific day that matches with the day of the date of the dateRhMarkList
    */

    public List<Date> findInitEndRHMarks(List<Date> dateRhMarkList, HoraryBand dayHoraryBand ,Tolerance tolerance ,Limit limit , Calendar dayOfMonthCalendar) {
        // initially the list to be returned is empty.
        List<Date> marksList = new ArrayList<Date>();
        // to catch the first and las ocurrence of marks
        Date nearestInitMark = null;
        Date nearestEndMark = null;
        // the employee at least have marked two times during the day.
        if (dateRhMarkList.size() >= 2) {
            Calendar initBandMark = Calendar.getInstance();
            Calendar endBandMark = Calendar.getInstance();
            // in order to make a comparison, normalize the year,month and date of the horary band, so that we check only the time not the date
            initBandMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                    dayHoraryBand.getInitHour().getHours(), dayHoraryBand.getInitHour().getMinutes(), dayHoraryBand.getInitHour().getSeconds());
            initBandMark.set(Calendar.MILLISECOND, 0);
            // in order to make a comparison, normalize the year,month and date of the horary band, so that we check only the time not the date
            endBandMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                    dayHoraryBand.getEndHour().getHours(), dayHoraryBand.getEndHour().getMinutes(), dayHoraryBand.getEndHour().getSeconds());
            endBandMark.set(Calendar.MILLISECOND, 0);
            Calendar beforeInitLimit = Calendar.getInstance();
            Calendar afterEndLimit = Calendar.getInstance();
            beforeInitLimit.setTime(initBandMark.getTime());
            beforeInitLimit.add(Calendar.MINUTE, -limit.getBeforeInit());
            afterEndLimit.setTime(endBandMark.getTime());
            afterEndLimit.add(Calendar.MINUTE, limit.getAfterEnd());
            Calendar lastInitEmployeeMark = null;
            Calendar lastEndEmployeeMark = null;


            for (Date rhMarkDateTime : dateRhMarkList) {
                //RegisterMarkAction rhMark = dateRhMarkList.get(i);
                Calendar employeeMark = Calendar.getInstance();
                Date employeeMarkD = new Date((dayOfMonthCalendar.get(Calendar.YEAR) - 1900), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                        rhMarkDateTime.getHours(), rhMarkDateTime.getMinutes(), rhMarkDateTime.getSeconds());


                // to ensure that the time mark has setted year and month. In case only the time is retrived.
                employeeMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                        rhMarkDateTime.getHours(), rhMarkDateTime.getMinutes(), rhMarkDateTime.getSeconds());
                employeeMark.set(Calendar.MILLISECOND, 0);

                employeeMark.setTime(employeeMarkD);
                //for init mark
                if ((employeeMark.compareTo(beforeInitLimit) >= 0) && (employeeMark.compareTo(endBandMark) < 0)) {
                    // override nearest init mark if convenient
                    if (nearestInitMark == null) {
                        nearestInitMark = rhMarkDateTime;
                        lastInitEmployeeMark = employeeMark;
                    } else {
                        int initBandEmployeeMarkDifference = ((Long) (Math.abs(initBandMark.getTimeInMillis() - employeeMark.getTimeInMillis()))).intValue();
                        int initBandNearestInitMarkDifference = ((Long) Math.abs(initBandMark.getTimeInMillis() - lastInitEmployeeMark.getTimeInMillis())).intValue();
                        if (initBandEmployeeMarkDifference < initBandNearestInitMarkDifference) {
                            // override with the nearest mark to the init HoraryBand
                            nearestInitMark = rhMarkDateTime;
                            lastInitEmployeeMark = employeeMark;
                        }
                    }
                }

                // for end mark
                if ((employeeMark.compareTo(initBandMark) > 0) && (employeeMark.compareTo(afterEndLimit) <= 0) && nearestInitMark != rhMarkDateTime) {
                    if (nearestEndMark == null) {
                        nearestEndMark = rhMarkDateTime;
                        lastEndEmployeeMark = employeeMark;
                    } else {
                        int endBandEmployeeMarkDifference = ((Long) (Math.abs(endBandMark.getTimeInMillis() - employeeMark.getTimeInMillis()))).intValue();
                        int endBandNearestEndMarkDifference = ((Long) Math.abs(endBandMark.getTimeInMillis() - lastEndEmployeeMark.getTimeInMillis())).intValue();
                        if (endBandEmployeeMarkDifference < endBandNearestEndMarkDifference) {
                            nearestEndMark = rhMarkDateTime;
                            lastEndEmployeeMark = employeeMark;
                        }
                    }
                }
            }
            if (nearestInitMark != null && nearestEndMark != null) {
                marksList.add(nearestInitMark);
                marksList.add(nearestEndMark);

            }

        }
        return marksList;
    }


    /*
    Returns RegisterMarkAction list only if the employee has marked at least one time to in and one time to out his horaryBand.
     the list is a list of two elements, the employee first and last mark for the horaryBand.
   @param dateRhMarkList a list of marks of an employee given an especific date
   @param dayHoraryBand a HoraryBand of a especific day that matches with the day of the date of the dateRhMarkList
    */

    public List<Date> findInitEndRHMarks(List<Date> dateRhMarkList, HoraryBandContract dayHoraryBandContract, Calendar dayOfMonthCalendar) {
        // initially the list to be returned is empty.
        List<Date> marksList = new ArrayList<Date>();
        HoraryBand dayHoraryBand = dayHoraryBandContract.getHoraryBand();
        Tolerance tolerance = dayHoraryBandContract.getTolerance();
        Limit limit = dayHoraryBandContract.getLimit();
        // to catch the first and las ocurrence of marks
        Date nearestInitMark = null;
        Date nearestEndMark = null;
        // the employee at least have marked two times during the day.
        if (dateRhMarkList.size() >= 2) {
            Calendar initBandMark = Calendar.getInstance();
            Calendar endBandMark = Calendar.getInstance();
            // in order to make a comparison, normalize the year,month and date of the horary band, so that we check only the time not the date
            initBandMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                    dayHoraryBand.getInitHour().getHours(), dayHoraryBand.getInitHour().getMinutes(), dayHoraryBand.getInitHour().getSeconds());
            initBandMark.set(Calendar.MILLISECOND, 0);
            // in order to make a comparison, normalize the year,month and date of the horary band, so that we check only the time not the date
            endBandMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                    dayHoraryBand.getEndHour().getHours(), dayHoraryBand.getEndHour().getMinutes(), dayHoraryBand.getEndHour().getSeconds());
            endBandMark.set(Calendar.MILLISECOND, 0);
            Calendar beforeInitLimit = Calendar.getInstance();
            Calendar afterEndLimit = Calendar.getInstance();
            beforeInitLimit.setTime(initBandMark.getTime());
            beforeInitLimit.add(Calendar.MINUTE, -limit.getBeforeInit());
            afterEndLimit.setTime(endBandMark.getTime());
            afterEndLimit.add(Calendar.MINUTE, limit.getAfterEnd());
            Calendar lastInitEmployeeMark = null;
            Calendar lastEndEmployeeMark = null;


            for (Date rhMarkDateTime : dateRhMarkList) {
                //RegisterMarkAction rhMark = dateRhMarkList.get(i);
                Calendar employeeMark = Calendar.getInstance();
                Date employeeMarkD = new Date((dayOfMonthCalendar.get(Calendar.YEAR) - 1900), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                        rhMarkDateTime.getHours(), rhMarkDateTime.getMinutes(), rhMarkDateTime.getSeconds());


                // to ensure that the time mark has setted year and month. In case only the time is retrived.
                employeeMark.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                        rhMarkDateTime.getHours(), rhMarkDateTime.getMinutes(), rhMarkDateTime.getSeconds());
                employeeMark.set(Calendar.MILLISECOND, 0);

                employeeMark.setTime(employeeMarkD);
                //for init mark
                if ((employeeMark.compareTo(beforeInitLimit) >= 0) && (employeeMark.compareTo(endBandMark) < 0)) {
                    // override nearest init mark if convenient
                    if (nearestInitMark == null) {
                        nearestInitMark = rhMarkDateTime;
                        lastInitEmployeeMark = employeeMark;
                    } else {
                        int initBandEmployeeMarkDifference = ((Long) (Math.abs(initBandMark.getTimeInMillis() - employeeMark.getTimeInMillis()))).intValue();
                        int initBandNearestInitMarkDifference = ((Long) Math.abs(initBandMark.getTimeInMillis() - lastInitEmployeeMark.getTimeInMillis())).intValue();
                        if (initBandEmployeeMarkDifference < initBandNearestInitMarkDifference) {
                            // override with the nearest mark to the init HoraryBand
                            nearestInitMark = rhMarkDateTime;
                            lastInitEmployeeMark = employeeMark;
                        }
                    }
                }

                // for end mark
                if ((employeeMark.compareTo(initBandMark) > 0) && (employeeMark.compareTo(afterEndLimit) <= 0) && nearestInitMark != rhMarkDateTime) {
                    if (nearestEndMark == null) {
                        nearestEndMark = rhMarkDateTime;
                        lastEndEmployeeMark = employeeMark;
                    } else {
                        int endBandEmployeeMarkDifference = ((Long) (Math.abs(endBandMark.getTimeInMillis() - employeeMark.getTimeInMillis()))).intValue();
                        int endBandNearestEndMarkDifference = ((Long) Math.abs(endBandMark.getTimeInMillis() - lastEndEmployeeMark.getTimeInMillis())).intValue();
                        if (endBandEmployeeMarkDifference < endBandNearestEndMarkDifference) {
                            nearestEndMark = rhMarkDateTime;
                            lastEndEmployeeMark = employeeMark;
                        }
                    }
                }
            }
            if (nearestInitMark != null && nearestEndMark != null) {
                marksList.add(nearestInitMark);
                marksList.add(nearestEndMark);

            }

        }
        return marksList;
    }

    /**
     * Finds which marks can be associated to a band
     *
     * @param dateRhMarkList        a given list of marks
     * @param dayHoraryBandContract a given HoraryBandContract
     * @return a list of marks which can be associated to a band
     */

    public List<Date> findAssociatedRHMarks(List<Date> dateRhMarkList, HoraryBandContract dayHoraryBandContract) {
        // initially the list to be returned is empty.
        List<Date> marksList = new ArrayList<Date>();
        HoraryBand dayHoraryBand = dayHoraryBandContract.getHoraryBand();
        Limit limit = dayHoraryBandContract.getLimit();
        // the employee at least have marked two times during the day.
        Calendar initBandMark = Calendar.getInstance();
        Calendar endBandMark = Calendar.getInstance();
        // in order to make a comparison, normalize the year,month and date of the horary band, so that we check only the time not the date
        initBandMark.set(initBandMark.get(Calendar.YEAR), initBandMark.get(Calendar.MONTH), initBandMark.get(Calendar.DAY_OF_MONTH),
                dayHoraryBand.getInitHour().getHours(), dayHoraryBand.getInitHour().getMinutes(), dayHoraryBand.getInitHour().getSeconds());
        initBandMark.set(Calendar.MILLISECOND, 0);
        // in order to make a comparison, normalize the year,month and date of the horary band, so that we check only the time not the date
        endBandMark.set(initBandMark.get(Calendar.YEAR), initBandMark.get(Calendar.MONTH), initBandMark.get(Calendar.DAY_OF_MONTH),
                dayHoraryBand.getEndHour().getHours(), dayHoraryBand.getEndHour().getMinutes(), dayHoraryBand.getEndHour().getSeconds());
        endBandMark.set(Calendar.MILLISECOND, 0);
        Calendar beforeInitLimit = Calendar.getInstance();
        Calendar afterEndLimit = Calendar.getInstance();
        beforeInitLimit.setTime(initBandMark.getTime());
        beforeInitLimit.add(Calendar.MINUTE, -limit.getBeforeInit());
        afterEndLimit.setTime(endBandMark.getTime());
        afterEndLimit.add(Calendar.MINUTE, limit.getAfterEnd());

        for (Date rhMarkDateTime : dateRhMarkList) {
            Calendar employeeMark = Calendar.getInstance();
            Date employeeMarkD = new Date((initBandMark.get(Calendar.YEAR) - 1900), initBandMark.get(Calendar.MONTH), initBandMark.get(Calendar.DAY_OF_MONTH),
                    rhMarkDateTime.getHours(), rhMarkDateTime.getMinutes(), rhMarkDateTime.getSeconds());

            // to ensure that the time mark has setted year and month. In case only the time is retrived.
            employeeMark.set(initBandMark.get(Calendar.YEAR), initBandMark.get(Calendar.MONTH), initBandMark.get(Calendar.DAY_OF_MONTH),
                    rhMarkDateTime.getHours(), rhMarkDateTime.getMinutes(), rhMarkDateTime.getSeconds());
            employeeMark.set(Calendar.MILLISECOND, 0);

            employeeMark.setTime(employeeMarkD);
            //for init mark
            if ((employeeMark.compareTo(beforeInitLimit) >= 0) && (employeeMark.compareTo(afterEndLimit) <= 0)) {
                marksList.add(rhMarkDateTime);
            }
        }
        return marksList;
    }

    private Calendar rhMarkToCalendar(RHMark rhMark, Calendar dayOfMonthCalendar) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(dayOfMonthCalendar.get(Calendar.YEAR), dayOfMonthCalendar.get(Calendar.MONTH), dayOfMonthCalendar.get(Calendar.DAY_OF_MONTH),
                rhMark.getMarTime().getHours(), rhMark.getMarTime().getMinutes(), rhMark.getMarTime().getSeconds());
        return calendar;
    }

    // return a HoraryBand list given a HoraryBandContract list

    private List<HoraryBand> findHoraryBand(List<HoraryBandContract> hourlyBandContractList) {
        List<HoraryBand> horaryBandList = new ArrayList<HoraryBand>();
        for (HoraryBandContract aHourlyBandContractList : hourlyBandContractList) {
            horaryBandList.add(aHourlyBandContractList.getHoraryBand());
        }
        return horaryBandList;
    }

    public boolean discountHalfDay(Long latenessInMillis, Long halfDayDiscountLowerLimit) {
        return latenessInMillis >= halfDayDiscountLowerLimit;
    }

    public void calculateLatenessDiscount(Calendar initBandHour, Calendar endBandHour, Calendar initMarkHour, Calendar endMarkHour) {
        // check for init
        Integer beforeInitTolerance = 5;
        Integer afterInitTolerance = 5;
        //apply any discount
        if (!this.isMarkInToleranceRange(beforeInitTolerance, afterInitTolerance, initBandHour, initMarkHour)) {
            // recover the list of differences in hours, minutes and seconds for init
            List<Long> timeDifferencesList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(initBandHour, initMarkHour);
            // when an employee register his income the discount is taken account only in case of lateness on the other side an absensee
            // in this a case that the employee is late.
            // for difference in init hours
            // for diference in init minutes     8:15 9:10  diff 55min
            // for difference in init seconds
            if (timeDifferencesList.get(0) <= 1 && timeDifferencesList.get(1) <= 1 && timeDifferencesList.get(2) <= 1) {
                //descontar por segundo
                Math.abs(timeDifferencesList.get(2));
            }
        }
        // check for end
        Integer beforeEndTolerance = 5;
        Integer afterEndTolerance = 5;
        //aplly any discount
        if (!this.isMarkInToleranceRange(beforeEndTolerance, afterEndTolerance, endBandHour, endMarkHour)) {
            // recover the list of differences in hours, minutes and seconds for end
            List<Long> timeDifferencesList = this.getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(endBandHour, endMarkHour);
            // when an employee register his out the discount is taken account if the employee leave earlier than he should
            // on the other side an absensee is taken into account.
            // in this case the employee is leaving his job earlier.
            // for difference in end hours
            // for diference in end minutes
            // for difference in end seconds
            //descontar por segundo
            if (timeDifferencesList.get(0) >= 1 && timeDifferencesList.get(1) >= 1 && timeDifferencesList.get(2) >= 1) {
                timeDifferencesList.get(2);
            }
        }
    }

    /*
   Gets the difference betwen markOne and markTwo in hours,minutes, and seconds as a list.
   Notice that when the markOne time is after the markTwo the result is negative, and vice versa
    */

    public List<Long> getDifferenceInHoursMinutesSecondsBetweenMarks(
            Calendar markOne, Calendar markTwo) {
        List<Long> differencesHoursMinutesSecondsList = new ArrayList<Long>();
        Long differencesInMillis;
        differencesInMillis = getDifferenceInMillisBetweenEmployeeMarkAndHourlyBand(markOne, markTwo);

        long time = differencesInMillis / 1000;
        differencesHoursMinutesSecondsList.add((time / 3600));// for hours
        differencesHoursMinutesSecondsList.add((time % 3600) / 60);// for minutes
        differencesHoursMinutesSecondsList.add(time % 60);// for seconds
        return differencesHoursMinutesSecondsList;
    }

    /*
   Gets the difference betwen employee marks and hourly bands in hours,minutes, and seconds as a list.
   Notice that when the employee marked a time after the band mark the result is negative, and
   when the employee marked a time before the band mark the result is positive
    */

    public List<Long> getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(
            Calendar bandHour, Calendar markHour) {
        // normalize band date.
        bandHour.set(markHour.get(Calendar.YEAR), markHour.get(Calendar.MONTH), markHour.get(Calendar.DAY_OF_MONTH));

        List<Long> differencesHoursMinutesSecondsList = new ArrayList<Long>();
        Long differencesInMillis;
        differencesInMillis = getDifferenceInMillisBetweenEmployeeMarkAndHourlyBand(bandHour, markHour);
        log.debug("millis: " + differencesInMillis);
        long time = differencesInMillis / 1000;
        differencesHoursMinutesSecondsList.add((time / 3600));// for hours
        differencesHoursMinutesSecondsList.add((time % 3600) / 60);// for minutes
        differencesHoursMinutesSecondsList.add(time % 60);// for seconds
        return differencesHoursMinutesSecondsList;
    }

    /*
   calculates the difference betwen employee marks and hourly bands in millis.
    */

    public Long getDifferenceInMillisBetweenEmployeeMarkAndHourlyBand(Calendar bandHour, Calendar markHour) {
        return markHour.getTimeInMillis() - bandHour.getTimeInMillis();
    }


    /*
   calculates the difference betwen employee marks and hourly bands in hours,minutes, and seconds.
    */

    public List<Long> getDifferenceInHoursMinutesSecondsBetweenEmployeeMarkAndHourlyBand(Calendar initBandHour, Calendar endBandHour, Calendar initMarkHour, Calendar endMarkHour) {
        List<Long> differencesHoursMinutesSecondsList = new ArrayList<Long>();
        List<Long> differencesInMillisList;
        differencesInMillisList = getDifferenceInMillisBetweenEmployeeMarkAndHourlyBand(initBandHour, endBandHour, initMarkHour, endMarkHour);
        // for init
        Long initDiffInMillis = differencesInMillisList.get(0);
        // for end
        Long endDiffInMillis = differencesInMillisList.get(1);

        long time = initDiffInMillis / 1000;
        differencesHoursMinutesSecondsList.add((time / 3600));// for hours
        differencesHoursMinutesSecondsList.add((time % 3600) / 60);// for minutes
        differencesHoursMinutesSecondsList.add(time % 60);// for seconds
        time = endDiffInMillis / 1000;
        differencesHoursMinutesSecondsList.add((time / 3600));// for hours
        differencesHoursMinutesSecondsList.add((time % 3600) / 60);// for minutes
        differencesHoursMinutesSecondsList.add(time % 60);// for seconds

        return differencesHoursMinutesSecondsList;
    }

    /*
   calculates the difference betwen employee marks and hourly bands in millis.
    */

    public List<Long> getDifferenceInMillisBetweenEmployeeMarkAndHourlyBand(Calendar initBandHour, Calendar endBandHour, Calendar initMarkHour, Calendar endMarkHour) {
        List<Long> differencesList = new ArrayList<Long>();
        Long initDifference = new Long(initMarkHour.getTimeInMillis() - initBandHour.getTimeInMillis());
        Long endDifference = new Long(endBandHour.getTimeInMillis() - endMarkHour.getTimeInMillis());
        differencesList.add(initDifference);
        differencesList.add(endDifference);
        return differencesList;
    }

    /*
   asks if an employee mark falls in the range of tolerance.
       @param before tolerance before
       @param before tolerance after
       @param hourlyBand the calendar date that represets the time of the hourly band
       @param employeeMark the time mark that the employee registered
       @return if the mark falls in the tolerance range.

    */

    public boolean isMarkInToleranceRange(Integer before, Integer after, Calendar hourlyBand, Calendar employeeMark) {
        if (before == null) {
            before = 0;
        }
        if (after == null) {
            after = 0;
        }

        Calendar lowerRange = Calendar.getInstance();
        Calendar upperRange = Calendar.getInstance();
        lowerRange.set(hourlyBand.get(Calendar.YEAR), hourlyBand.get(Calendar.MONTH), hourlyBand.get(Calendar.DAY_OF_MONTH),
                hourlyBand.get(Calendar.HOUR_OF_DAY), hourlyBand.get(Calendar.MINUTE), hourlyBand.get(Calendar.SECOND));
        lowerRange.add(Calendar.MINUTE, before * (-1));

        upperRange.set(hourlyBand.get(Calendar.YEAR), hourlyBand.get(Calendar.MONTH), hourlyBand.get(Calendar.DAY_OF_MONTH),
                hourlyBand.get(Calendar.HOUR_OF_DAY), hourlyBand.get(Calendar.MINUTE), hourlyBand.get(Calendar.SECOND));
        upperRange.add(Calendar.MINUTE, after);
        return ((getHourAndMiniteInMillis(employeeMark) <= getHourAndMiniteInMillis(upperRange))
                && (getHourAndMiniteInMillis(employeeMark) >= getHourAndMiniteInMillis(lowerRange)));
    }

    public int getHourAndMiniteInMillis(Calendar calendar) {
        int timeInMillis = 0;
        timeInMillis += calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        timeInMillis += calendar.get(Calendar.MINUTE) * 60 * 1000;
        return timeInMillis;
    }

    /*
   calculates the difference betwen employee marks and hourly bands,
   but take care because each difference(hour, minute, second) is independent of each other.
    */

    public List<Integer> getDifferenceBetweenEmployeeMarkAndHourlyBand(Calendar initBandHour, Calendar endBandHour, Calendar initMarkHour, Calendar endMarkHour) {
        List<Integer> differencesList = new ArrayList<Integer>();
        Integer initHourDifference;
        Integer endHourDifference;
        Integer initMinuteDifference;
        Integer endMinuteDifference;
        Integer initSecondDifference;
        Integer endSecondDifference;
        initHourDifference = initMarkHour.get(Calendar.HOUR_OF_DAY) - initBandHour.get(Calendar.HOUR_OF_DAY);
        endHourDifference = endMarkHour.get(Calendar.HOUR_OF_DAY) - endBandHour.get(Calendar.HOUR_OF_DAY);
        initMinuteDifference = initMarkHour.get(Calendar.MINUTE) - initBandHour.get(Calendar.MINUTE);
        endMinuteDifference = endMarkHour.get(Calendar.MINUTE) - endBandHour.get(Calendar.MINUTE);
        initSecondDifference = initMarkHour.get(Calendar.SECOND) - initBandHour.get(Calendar.SECOND);
        endSecondDifference = endMarkHour.get(Calendar.SECOND) - endBandHour.get(Calendar.SECOND);
        differencesList.add(initHourDifference);
        differencesList.add(endHourDifference);
        differencesList.add(initMinuteDifference);
        differencesList.add(endMinuteDifference);
        differencesList.add(initSecondDifference);
        differencesList.add(endSecondDifference);
        return differencesList;
    }


    public OrganizationalLevel findOrganizationalLevelByName(String name) {
        try {
            Query query = em.createNamedQuery("OrganizationalLevel.findOrganizationalLevelByName");
            query.setParameter("name", name);
            return (OrganizationalLevel) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void createRotatoryFunds(List<RotatoryFundCollection> newRotatoryFundCollectionList) {
        try {
            userTransaction.begin();
            for (RotatoryFundCollection rotatoryFundCollection : newRotatoryFundCollectionList) {
                rotatoryFundCollection.setCode(rotatoryFundCollectionService.getNextCodeNumber().intValue());
                em.persist(rotatoryFundCollection);
                em.flush();
            }
            userTransaction.commit();
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                unexpectedErrorLog(e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void deleteGeneratedPayroll(Long generatedPayrollId) {
        try {
            userTransaction.begin();
            GeneratedPayroll generatedPayroll = em.find(GeneratedPayroll.class, generatedPayrollId);
            em.remove(generatedPayroll);
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                unexpectedErrorLog(e1);
            }
        }
    }
    /*Changes the state of a set of generated payrolls by gestion payroll
    * it can change from test to outdated but not from official to outdated*/

    @SuppressWarnings(value = "unchecked")
    private void setTestToOutdatedGeneratedPayrollButCurrentByGestionPayroll(GestionPayroll gestionPayroll, GeneratedPayroll generatedPayroll) {
        try {
            userTransaction.begin();
            em.createNamedQuery("GeneratedPayroll.setTestToOutdatedGeneratedPayrollButCurrentByGestionPayroll")
                    .setParameter("gestionPayroll", gestionPayroll)
                    .setParameter("test", GeneratedPayrollType.TEST)
                    .setParameter("outdated", GeneratedPayrollType.OUTDATED)
                    .setParameter("generatedPayrollId", generatedPayroll.getId()).executeUpdate();
            userTransaction.commit();
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                unexpectedErrorLog(e1);
            }
            throw new RuntimeException(e);
        }
    }

    public Boolean hasNegativeAmount(GeneratedPayroll generatedPayroll) {
        Long managersPayrollCount = (Long) em.createNamedQuery("ManagersPayroll.countPayrollWithNegativeAmount")
                .setParameter("generatedPayroll", generatedPayroll).getSingleResult();
        Long proffesorsPayrollCount = (Long) em.createNamedQuery("GeneralPayroll.countPayrollWithNegativeAmount")
                .setParameter("generatedPayroll", generatedPayroll).getSingleResult();
        Long christmasPayrollCount = (Long) em.createNamedQuery("ChristmasPayroll.countPayrollWithNegativeAmount")
                .setParameter("generatedPayroll", generatedPayroll).getSingleResult();
        Long fiscalProfessorPayroll = (Long) em.createNamedQuery("FiscalProfessorPayroll.countPayrollWithNegativeAmount")
                .setParameter("generatedPayroll", generatedPayroll).getSingleResult();
        return managersPayrollCount > 0 || proffesorsPayrollCount > 0 || christmasPayrollCount > 0 || fiscalProfessorPayroll > 0;
    }

    public Boolean haveBankAccounts(GeneratedPayroll generatedPayroll) {
        Long managersPayrollCount = (Long) em.createNamedQuery("ManagersPayroll.employeeWithoutBankAccount")
                .setParameter("generatedPayroll", generatedPayroll)
                .setParameter("paymentType", PaymentType.PAYMENT_BANK_ACCOUNT).getSingleResult();
        Long proffesorsPayrollCount = (Long) em.createNamedQuery("GeneralPayroll.employeeWithoutBankAccount")
                .setParameter("generatedPayroll", generatedPayroll)
                .setParameter("paymentType", PaymentType.PAYMENT_BANK_ACCOUNT).getSingleResult();
        Long christmasPayrollCount = (Long) em.createNamedQuery("ChristmasPayroll.employeeWithoutBankAccount")
                .setParameter("generatedPayroll", generatedPayroll)
                .setParameter("paymentType", PaymentType.PAYMENT_BANK_ACCOUNT).getSingleResult();
        Long fiscalProfessorPayrollCount = (Long) em.createNamedQuery("FiscalProfessorPayroll.employeeWithoutBankAccount")
                .setParameter("generatedPayroll", generatedPayroll)
                .setParameter("paymentType", PaymentType.PAYMENT_BANK_ACCOUNT).getSingleResult();
        return managersPayrollCount == 0 && proffesorsPayrollCount == 0 && christmasPayrollCount == 0 && fiscalProfessorPayrollCount == 0;
    }

    public Boolean validateHasAccountingRecord(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll, List<Long> selectIdList) {
        Long countResult = (Long) em.createNamedQuery(entityClass.getSimpleName() + ".countByAccountingRecord")
                .setParameter("generatedPayroll", generatedPayroll)
                .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                .setParameter("selectIdList", selectIdList)
                .setParameter("BOOLEAN_TRUE", Boolean.TRUE).getSingleResult();
        return countResult == null || countResult == 0;
    }

    public Boolean validateHasAccountingRecordOrHasInactivePayment(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll, List<Long> selectIdList) {
        Long countResult = (Long) em.createNamedQuery(entityClass.getSimpleName() + ".countByAccountingRecordOrInactivePayment")
                .setParameter("generatedPayroll", generatedPayroll)
                .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                .setParameter("selectIdList", selectIdList)
                .setParameter("BOOLEAN_TRUE", Boolean.TRUE)
                .setParameter("BOOLEAN_FALSE", Boolean.FALSE).getSingleResult();
        return countResult == null || countResult == 0;
    }

    public List<Integer> updateActivePaymentToPayrollItems(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll, List<Long> selectIdList)
            throws PayrollSelectItemsEmptyException,
            PayrollSelectItemsHasAccountingRecordException,
            UpdateActivePaymentException {

        if (selectIdList == null) {
            throw new PayrollSelectItemsEmptyException("The select id list is empty");
        }
        if (ValidatorUtil.isEmptyOrNull(selectIdList)) {
            selectIdList.add((long) 0);
        }

        if (!validateHasAccountingRecord(entityClass, generatedPayroll, selectIdList)) {
            throw new PayrollSelectItemsHasAccountingRecordException("Some items has has accounting records");
        }

        List<Integer> successUpdateList = new ArrayList<Integer>();

        try {
            userTransaction.setTransactionTimeout(60 * selectIdList.size());
            userTransaction.begin();
            successUpdateList.add(em.createNamedQuery(entityClass.getSimpleName() + ".updateActivePaymentToSelectItems")
                    .setParameter("generatedPayroll", generatedPayroll)
                    .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                    .setParameter("BOOLEAN_TRUE", Boolean.TRUE)
                    .setParameter("BOOLEAN_FALSE", Boolean.FALSE)
                    .setParameter("selectIdList", selectIdList).executeUpdate());
            successUpdateList.add(em.createNamedQuery(entityClass.getSimpleName() + ".updateInactivePaymentToUnselectItems")
                    .setParameter("generatedPayroll", generatedPayroll)
                    .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                    .setParameter("BOOLEAN_TRUE", Boolean.TRUE)
                    .setParameter("BOOLEAN_FALSE", Boolean.FALSE)
                    .setParameter("selectIdList", selectIdList).executeUpdate());
            em.flush();
            userTransaction.commit();
            userTransaction.setTransactionTimeout(0);
        } catch (SystemException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e);
            }
            throw new UpdateActivePaymentException("Cannot be complete the operation");
        } catch (HeuristicRollbackException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e);
            }
            throw new UpdateActivePaymentException("Cannot be complete the operation");
        } catch (javax.transaction.RollbackException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e);
            }
            throw new UpdateActivePaymentException("Cannot be complete the operation");
        } catch (NotSupportedException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e);
            }
            throw new UpdateActivePaymentException("Cannot be complete the operation");
        } catch (HeuristicMixedException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e);
            }
            throw new UpdateActivePaymentException("Cannot be complete the operation");
        }
        return successUpdateList;
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getSelectIdList(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll) {
        List<Long> selectIdList = em.createNamedQuery(entityClass.getSimpleName() + ".findSelectIdList")
                .setParameter("generatedPayroll", generatedPayroll)
                .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                .setParameter("BOOLEAN_TRUE", Boolean.TRUE)
                .setParameter("BOOLEAN_FALSE", Boolean.FALSE).getResultList();
        return !ValidatorUtil.isEmptyOrNull(selectIdList) ? selectIdList : new ArrayList<Long>();
    }

    @SuppressWarnings({"unchecked"})
    public List<GenericPayroll> getSelectItemList(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll) {
        List<GenericPayroll> selectIdList = em.createNamedQuery(entityClass.getSimpleName() + ".findSelectItemList")
                .setParameter("generatedPayroll", generatedPayroll)
                .setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL)
                .setParameter("BOOLEAN_TRUE", Boolean.TRUE)
                .setParameter("BOOLEAN_FALSE", Boolean.FALSE).getResultList();
        return !ValidatorUtil.isEmptyOrNull(selectIdList) ? selectIdList : new ArrayList<GenericPayroll>();
    }

    public Map<Long, FinancesBankAccount> getFinancesBankAccountMapByPayroll(Class<? extends GenericPayroll> entityClass, GeneratedPayroll generatedPayroll)
            throws CompanyConfigurationNotFoundException {
        Map<Long, FinancesBankAccount> financesBankAccountMap = new HashMap<Long, FinancesBankAccount>();
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        for (GenericPayroll genericPayroll : getSelectItemList(entityClass, generatedPayroll)) {
            Long currencyId = null;
            PaymentType paymentType = employeeService.getEmployeesPaymentType(genericPayroll.getEmployee());
            Currency currency = employeeService.getEmployeesCurrencyByPaymentType(generatedPayroll, genericPayroll.getEmployee(), paymentType);
            if (currency == null) {
                if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                    if (FiscalProfessorPayroll.class.equals(entityClass) || ManagersPayroll.class.equals(entityClass)) {
                        currencyId = Constants.currencyIdBs;
                    } else {
                        currencyId = Constants.currencyIdSus;
                    }
                }
            } else {
                currencyId = currency.getId();
            }

            if (currencyId != null) {
                financesBankAccountMap.put((Long) genericPayroll.getId(),
                        Constants.currencyIdBs.equals(currencyId) ? companyConfiguration.getNationalBankAccountForPayment() : companyConfiguration.getForeignBankAccountForPayment());
            }
        }

        return financesBankAccountMap;
    }

    @SuppressWarnings({"unchecked"})
    public List<Employee> getPayPayrollEmployeeList(Class<? extends GenericPayroll> entityClass, List<Long> idList) {
        return em.createNamedQuery(entityClass.getSimpleName() + ".findEmployeesByIdList")
                .setParameter("idList", idList).getResultList();
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getPayPayrollEmployeeIdList(Class<? extends GenericPayroll> entityClass, List<Long> idList) {
        return em.createNamedQuery(entityClass.getSimpleName() + ".findEmployeeIdList")
                .setParameter("idList", idList).getResultList();
    }

    public int[] calculateGeneratedPayrolls(int year, String month, Integer executorUnitId) {
        String sql = " SELECT TP.TOTALPLAN, PO.PLANOFICIAL," +
                " CASE WHEN TP.TOTALPLAN > 0 THEN ROUND(PO.PLANOFICIAL * 100 / TP.TOTALPLAN, 0) ELSE 0 END PLANOFICIALPORCENT" +
                " FROM" +
                " (SELECT COUNT(*) TOTALPLAN" +
                " FROM GESTIONPLANILLA GP LEFT JOIN GESTION GE ON GP.IDGESTION = GE.IDGESTION" +
                " WHERE" +
                " GE.ANIO = " + year +
                " AND GP.MES = '" + month + "'";
        if (null != executorUnitId) {
            sql += " AND GP.IDUNIDADNEGOCIO = " + executorUnitId;
        }
        sql += " ) TP," +
                " (SELECT COUNT(*) PLANOFICIAL" +
                " FROM GESTIONPLANILLA GP LEFT JOIN GESTION GE ON GP.IDGESTION = GE.IDGESTION" +
                " WHERE" +
                " GE.ANIO = " + year +
                " AND GP.MES = '" + month + "'";
        if (null != executorUnitId) {
            sql += " AND GP.IDUNIDADNEGOCIO = " + executorUnitId;
        }
        sql += " AND EXISTS(SELECT POG.IDPLANILLAGENERADA FROM PLANILLAGENERADA POG WHERE POG.TIPOPLANILLAGEN = 'OFFICIAL' AND POG.IDGESTIONPLANILLA = GP.IDGESTIONPLANILLA)" +
                " ) PO";

        Object[] singleResult = (Object[]) em.createNativeQuery(sql).getSingleResult();

        return new int[]{
                Integer.parseInt(singleResult[0].toString()),
                Integer.parseInt(singleResult[1].toString()),
                Integer.parseInt(singleResult[2].toString())};
    }

    /**
     * @param generatedPayroll               a GeneralPayroll that groups all FiscalProfessorPayroll entries
     * @param employeeList                   a list of employees to process
     * @param specialDate4BusinessUnit       a list of dates where the employee have permission
     * @param specialDateTime4BusinessUnit   a map of list of dates and time intervals where the employee have permission
     * @param specialDate4OrganizationalUnit a map that contains a list of special dates by OrganizationalUnit
     * @param specialDateTimeForOrganizationalUnit
     *                                       a map that contains a list of special dates by time by OrganizationalUnit
     * @param globalDiscountRuleList         discountRule general for all company
     * @param businessUnitGlobalDiscountRuleList
     *                                       discountRule general for a given BusinessUnit
     * @param jobCategoryBusinessUnitDiscountRuleList
     *                                       specific discountRule for a given BusinessUnit and JobCategory
     * @param minuteDiscountRuleRangeMap     a map that holds known minute rule pair
     * @param newRotatoryFundCollectionList  a list of Rotatory Fund Collections
     * @param employeeIdList                 a list of employee id
     * @param employeeJobContractMap         @return PayrollGenerationType
     * @return PayrollGenerationResult
     */
    @SuppressWarnings("unchecked")
    public PayrollGenerationResult fillFiscalProfessorPayroll(GeneratedPayroll generatedPayroll, List<Employee> employeeList,
                                                              List<Date> specialDate4BusinessUnit,
                                                              Map<Date, List<TimeInterval>> specialDateTime4BusinessUnit,
                                                              Map<Long, List<Date>> specialDate4OrganizationalUnit,
                                                              Map<Long, Map<Date, List<TimeInterval>>> specialDateTimeForOrganizationalUnit,
                                                              List<DiscountRule> globalDiscountRuleList,
                                                              List<DiscountRule> businessUnitGlobalDiscountRuleList,
                                                              List<DiscountRule> jobCategoryBusinessUnitDiscountRuleList,
                                                              Map<Integer, DiscountRuleRange> minuteDiscountRuleRangeMap,
                                                              List<RotatoryFundCollection> newRotatoryFundCollectionList,
                                                              List<Long> employeeIdList,
                                                              HashMap<Long, List<JobContract>> employeeJobContractMap) {
        em.flush();
        GestionPayroll gestionPayroll = generatedPayroll.getGestionPayroll();
        // iterates each employee
        int index = 0;
        Map<Long, TributaryPayroll> lastMonthTributaryPayrollMap
                = tributaryPayrollGeneratorService.getTributaryPayrollsForLastMonth(generatedPayroll.getPayrollGenerationCycle(), employeeIdList);
        // InvoicesForm map by employee id
        Map<Long, InvoicesForm> invoicesFormMap = invoicesFormService.findInvoicesFormMapByPayrollGenerationCycleAndEmployeeList(
                generatedPayroll.getPayrollGenerationCycle(), employeeIdList);
        // ExtraHoursWorked map by jobContract id
        Map<Long, ExtraHoursWorked> extraHoursWorkedCache = extraHoursWorkedService.findByPayrollGenerationCycleAndJobCategory(
                generatedPayroll.getPayrollGenerationCycle(), generatedPayroll.getGestionPayroll().getJobCategory());
        Map<Long, List<GrantedBonus>> grantedBonusMap = grantedBonusService.findByPayrollGenerationCycleAndJobCategory(
                generatedPayroll.getPayrollGenerationCycle(), generatedPayroll.getGestionPayroll().getJobCategory());
        SeniorityBonus seniorityBonus = taxPayrollUtilService.getActiveSeniorityBonus();

        //get discount rules in case they've already not been loaded
        if (globalDiscountRuleList.isEmpty() && businessUnitGlobalDiscountRuleList.isEmpty() && jobCategoryBusinessUnitDiscountRuleList.isEmpty()) {
            globalDiscountRuleList = discountRuleService.findGlobalActiveDiscountRuleByGestion(gestionPayroll.getGestion());
            businessUnitGlobalDiscountRuleList = discountRuleService.findBusinessUnitGlobalActiveDiscountRuleByGestion(
                    gestionPayroll.getGestion(), gestionPayroll.getBusinessUnit());
            jobCategoryBusinessUnitDiscountRuleList = discountRuleService.findActiveByGestionAndBusinessUnitAndJobCategory(
                    gestionPayroll.getGestion(), gestionPayroll.getBusinessUnit(), gestionPayroll.getJobCategory());
        }

        for (Employee employee : employeeList) {
            index++;
            log.debug("Processing employee = " + employee.getId() + employee.getFullName() + " index=" + index);
            JobContract currentJobContract;
            Double lastIvaResidue = 0.0;
            Double totalSumOfDiscounts = 0.0;

            Double totalSumOfIncomesBeforeIva = 0.0;
            Double totalSumOfIncomesOutOfIva = 0.0;

            // absences and tardiness
            Double totalSumOfDiscountsPerLateness = 0.0;
            Double totalSumOfDiscountsPerAbsences = 0.0;

            Double perAbsenceMinuteDiscount = 0.0;

            Double totalWinDiscount = 0.0;
            Double totalAdvanceDiscount = 0.0;
            Double totalLoanDiscount = 0.0;
            Double totalOtherDiscount = 0.0;
            // get the contracts, for the current employee, valid for the last gestionPayroll (year-month) entry
            List<JobContract> employeeJobContractList = employeeJobContractMap.get(employee.getId());
            boolean isActiveContract = false;
            for (JobContract jobContract : employeeJobContractList) {
                if (null != jobContract.getCostPivotHoraryBandContract() && jobContract.getCostPivotHoraryBandContract().getActive()) {
                    isActiveContract = true;
                    break;
                }
            }
            log.debug("isActiveContract:" + isActiveContract);
            if (employeeJobContractList.isEmpty() ||
                    !isActiveContract) {
                return PayrollGenerationResult.WITHOUT_BANDS.assignResultData(employee.getIdNumberAndFullName());
            } else { //IF THE EMPLOYEE HAS VALID JOB CONTRACTS

                Double mensualTotalSalary;
                Double ivaResidue = 0.0;
                Double totalRCIvaDiscount = 0.0;
                Double totalAfpDiscount = 0.0;
                Double totalOtherIncomes = 0.0;
                Double totalIncomeOutOfIva = 0.0;
                Double proHome = 0.0;
                int workedDays;

                //sum of Lists
                Integer cumulativeMinutesInTheMonth4Contract = 0;

                // how many minutes should be worked according to the bands (expected)
                List<Integer> cumulativeExpectedMinutesByContractList = new ArrayList();
                // how many minutes has the employee really worked (observed)
                List<Integer> cumulativePerformanceMinutesByContractList = new ArrayList();
                // how many lateness minutes has the employee accumulated (observed)
                List<Integer> cumulativeLatenessMinutesByContractList = new ArrayList();
                // how many missing minutes has the employee accumulated (observed)
                List<Double> cumulativeBandAbsencesByContractList = new ArrayList();

                List<Double> contractsPriceList = new ArrayList();

                // AttendanceControl
                Calendar initDate = Calendar.getInstance();
                initDate.setTime(gestionPayroll.getInitDate());

                Calendar endDate = Calendar.getInstance();
                endDate.setTime(gestionPayroll.getEndDate());

                // The current values of HoraryBandContract for payroll generation
                currentJobContract = employeeJobContractList.get(0);

                // The current values of RRMark for payroll generation
                Map<Date, List<Date>> rhMarkTimeDateMap4Employee = rhMarkService.getRHMarkDateTimeMapByDateRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

                // The current values of SpecialDate for payroll generation
                List<Date> specialDate4Employee = specialDateService.getSpecialDateRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());
                Map<Date, List<TimeInterval>> specialDateTime4Employee = specialDateService.getSpecialDateTimeRange(employee, gestionPayroll.getInitDate(), gestionPayroll.getEndDate());

                /*if the contract does not cover all the month, then compute the fraction of salary
                    corresponding to the period of the contract in days*/
                workedDays = getContractDays4Month(currentJobContract.getContract(), gestionPayroll);
                log.debug("workedDays: " + workedDays);

                // here is the control because, each contract may have different variables of cost
                for (JobContract jobContract : employeeJobContractList) {
                    //assume only one contract
                    log.debug("iterating for jobContract :" + jobContract.getId());
                    log.debug("iterating for jobContract band :" + jobContract.getCostPivotHoraryBandContract().getHoraryBand().getInitDay());
                    if (null != jobContract.getCostPivotHoraryBandContract()) {// IF THE CONTRACT HAS VALID TIMES
                        log.debug("there are valid bands");
                        List<Integer> cumulativeBandDurationMinutesInTheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Integer> cumulativePerformanceMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Integer> cumulativeLatenessMinutesIntheMonth4ContractList = new ArrayList<Integer>(0);
                        List<Integer> cumulativeBandAbsencesInTheMonth4ContractList = new ArrayList<Integer>(0);

                        // this var is to control the days of the month. It is initially setted to the first day of the month
                        Calendar currentDate = Calendar.getInstance();
                        currentDate.setTime(gestionPayroll.getInitDate());
                        currentDate.set(Calendar.MILLISECOND, 0);

                        Job job = em.find(Job.class, currentJobContract.getJob().getId());

                        if (!specialDate4OrganizationalUnit.containsKey(job.getOrganizationalUnit().getId())) {
                            specialDate4OrganizationalUnit.put(job.getOrganizationalUnit().getId(), specialDateService.getSpecialDateRange(job.getOrganizationalUnit(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate()));
                        }
                        if (!specialDateTimeForOrganizationalUnit.containsKey(job.getOrganizationalUnit().getId())) {
                            specialDateTimeForOrganizationalUnit.put(job.getOrganizationalUnit().getId(), specialDateService.getSpecialDateTimeRange(job.getOrganizationalUnit(), gestionPayroll.getInitDate(), gestionPayroll.getEndDate()));
                        }

                        log.debug("jobContract.getOccupationalAmount(): " + jobContract.getOccupationalAmount());
                        log.debug("jobContract.getContract().getOccupationalGlobalAmount(): " + jobContract.getContract().getOccupationalGlobalAmount());
                        log.debug("jobContract.getContract().getOccupationalBasicAmount(): " + jobContract.getContract().getOccupationalBasicAmount());
                        double contractPrice = 0;
                        if (!BigDecimalUtil.isZeroOrNull(jobContract.getOccupationalAmount()) &&
                                !BigDecimalUtil.isZeroOrNull(jobContract.getContract().getOccupationalGlobalAmount()) &&
                                !BigDecimalUtil.isZeroOrNull(jobContract.getContract().getOccupationalBasicAmount())) {
                            contractPrice = jobContract.getOccupationalAmount().doubleValue() / jobContract.getContract().getOccupationalGlobalAmount().doubleValue() * jobContract.getContract().getOccupationalBasicAmount().doubleValue();
                        }
                        contractsPriceList.add(contractPrice);
                        executeAttendanceControlFiscalProffesors(endDate, currentDate, generatedPayroll,
                                employee, jobContract.getCostPivotHoraryBandContract(),
                                rhMarkTimeDateMap4Employee,
                                specialDate4BusinessUnit,
                                specialDateTime4BusinessUnit,
                                specialDate4OrganizationalUnit.get(job.getOrganizationalUnit().getId()),
                                specialDateTimeForOrganizationalUnit.get(job.getOrganizationalUnit().getId()),
                                specialDate4Employee,
                                specialDateTime4Employee,
                                cumulativeBandDurationMinutesInTheMonth4ContractList,
                                cumulativePerformanceMinutesIntheMonth4ContractList,
                                cumulativeBandAbsencesInTheMonth4ContractList,
                                cumulativeLatenessMinutesIntheMonth4ContractList);

                        int bandDuration = 0;
                        int performance = 0;
                        double bandAbsences = 0.0;
                        int tardinessMonth = 0;
                        for (int k = 0; k < cumulativeBandDurationMinutesInTheMonth4ContractList.size(); k++) {
                            bandDuration += cumulativeBandDurationMinutesInTheMonth4ContractList.get(k);
                            performance += cumulativePerformanceMinutesIntheMonth4ContractList.get(k);
                            tardinessMonth += cumulativeLatenessMinutesIntheMonth4ContractList.get(k);
                            bandAbsences += cumulativeBandAbsencesInTheMonth4ContractList.get(k);
                        }
                        cumulativeBandAbsencesByContractList.add(bandAbsences);
                        cumulativeLatenessMinutesByContractList.add(tardinessMonth);
                        cumulativeExpectedMinutesByContractList.add(bandDuration);
                        cumulativePerformanceMinutesByContractList.add(performance);
                    }
                }
                Double pricePerMinute;
                Integer tardinessTotal = 0;
                Integer absenceTotal = 0;
                Integer bandMinutesTotal = 0;

                for (int i = 0; i < cumulativeExpectedMinutesByContractList.size(); i++) {
                    tardinessTotal += cumulativeLatenessMinutesByContractList.get(i);
                    absenceTotal += cumulativeBandAbsencesByContractList.get(i).intValue();
                    bandMinutesTotal += cumulativeExpectedMinutesByContractList.get(i);
                    Integer minutes = cumulativeExpectedMinutesByContractList.get(i);
                    Integer bandAbsenceMinutes = cumulativeBandAbsencesByContractList.get(i).intValue();
                    Integer performanceMinutes = cumulativePerformanceMinutesByContractList.get(i);
                    Double salary = contractsPriceList.get(i);
                    log.debug("precio contrato i:" + salary);
                    if (performanceMinutes != 0) {
                        cumulativeMinutesInTheMonth4Contract += minutes - performanceMinutes;
                    }
                    if (bandMinutesTotal == 0) {
                        pricePerMinute = 0.0;
                    } else {
                        pricePerMinute = cumulativeExpectedMinutesByContractList.get(i) != 0 ? salary / 30 * workedDays / cumulativeExpectedMinutesByContractList.get(i) : 0.0;
                    }
                    perAbsenceMinuteDiscount += (bandAbsenceMinutes * pricePerMinute);
                }

                log.debug("bandMinutesTotal*******************************: " + bandMinutesTotal);
                log.debug("tardinessTotal  *******************************: " + tardinessTotal);
                log.debug("absenceTotal    *******************************: " + absenceTotal);
                log.debug("jobCategoryBusinessUnitDiscountRuleList size:" + jobCategoryBusinessUnitDiscountRuleList.size());

                // RRHH indicates that those should be consider as the same concept
                tardinessTotal += absenceTotal;
                DiscountRuleRange discountRuleRange = findDiscountRuleRange(tardinessTotal,
                        minuteDiscountRuleRangeMap, globalDiscountRuleList, businessUnitGlobalDiscountRuleList, jobCategoryBusinessUnitDiscountRuleList);
                log.debug("discountRuleRange for tardinessTotal :" + tardinessTotal + "-" + (null != discountRuleRange ? discountRuleRange : "is null"));
                double basicSalary;
                basicSalary = null == currentJobContract.getContract().getOccupationalBasicAmount() ? 0.0 : currentJobContract.getContract().getOccupationalBasicAmount().doubleValue();

                log.debug(">>> WORKED DAYS: " + workedDays);
                mensualTotalSalary = basicSalary / 30 * (workedDays);

                // calculate discounts
                // Salary movement list of employee that has relationship with gestion payroll
                List<SalaryMovement> salaryMovementList = salaryMovementService.findByEmployeeAndGestionPayroll(employee, gestionPayroll);

                Boolean activeForTaxPayrollGeneration = currentJobContract.getContract().getActiveForTaxPayrollGeneration();
                for (SalaryMovement employeeSalaryMovement : salaryMovementList) {
                    double amount;
                    if (employeeSalaryMovement.getCurrency().getSymbol().equalsIgnoreCase("$US")) {
                        amount = employeeSalaryMovement.getAmount().doubleValue() * generatedPayroll.getExchangeRate().getSale().doubleValue();
                    } else {
                        amount = employeeSalaryMovement.getAmount().doubleValue();
                    }
                    // only available for not active for tax payroll generation employees because they could get other incomes via other bonus
                    if (employeeSalaryMovement.getSalaryMovementType().getMovementType().equals(MovementType.OTHER_INCOME) && !activeForTaxPayrollGeneration) {
                        totalOtherIncomes += amount;
                    }
                }
                // Discount per lateness accumulated in the month
                totalSumOfDiscountsPerLateness = 0.0;
                if (tardinessTotal >= 0 && null != discountRuleRange) {
                    if (discountRuleRange.getDiscountRule().getDiscountUnitType().equals(DiscountUnitType.CURRENCY)) {
                        BigDecimal amount = discountRuleRange.getDiscountRule().getCurrency().getSymbol().equalsIgnoreCase("$US") ?
                                BigDecimalUtil.multiply(discountRuleRange.getAmount(), generatedPayroll.getExchangeRate().getSale()) :
                                discountRuleRange.getAmount();
                        totalSumOfDiscountsPerLateness = amount.doubleValue();
                    } else {
                        //percentage case
                        BigDecimal amount = discountRuleRange.getAmount();

                        totalSumOfDiscountsPerLateness = mensualTotalSalary * amount.doubleValue() / 100;
                    }
                }
                // if the option should be  based on mensualTotalSalary
                /*if (absenceTotal.compareTo(0) > 0 && bandMinutesTotal.compareTo(0) > 0) {
                    totalSumOfDiscountsPerAbsences = absenceTotal * mensualTotalSalary / bandMinutesTotal;
                }*/
                totalSumOfDiscountsPerAbsences = perAbsenceMinuteDiscount;
                totalSumOfDiscounts += totalSumOfDiscountsPerLateness + totalWinDiscount + totalOtherDiscount;
                log.debug("totalSumOfDiscounts " + totalSumOfDiscounts);
                CategoryTributaryPayroll categoryTributaryPayroll = null;
                CategoryFiscalPayroll categoryFiscalPayroll = null;

                if (activeForTaxPayrollGeneration) {
                    TributaryPayrollGenerator generator;
                    ExtraHoursWorked extraHoursWorked = null;
                    List<GrantedBonus> grantedBonuses = new ArrayList<GrantedBonus>();
                    for (JobContract jobContract : employeeJobContractList) {
                        ExtraHoursWorked jobContractExtraHoursWorked = extraHoursWorkedCache.get(jobContract.getId());
                        if (null != jobContractExtraHoursWorked) {
                            extraHoursWorked = jobContractExtraHoursWorked;
                        }
                        List<GrantedBonus> grantedBonusesForContract = grantedBonusMap.get(jobContract.getId());
                        if (!ValidatorUtil.isEmptyOrNull(grantedBonusesForContract)) {
                            grantedBonuses.addAll(grantedBonusesForContract);
                        }
                    }
                    BigDecimal lastMonthBalance = BigDecimal.ZERO;
                    TributaryPayroll lastMonthTributaryPayroll
                            = lastMonthTributaryPayrollMap.get(employee.getId());
                    if (null != lastMonthTributaryPayroll && null != lastMonthTributaryPayroll.getDependentBalanceToNextMonth()) {
                        lastMonthBalance = lastMonthTributaryPayroll.getDependentBalanceToNextMonth();
                    }

                    generator = new TributaryPayrollGenerator(employee,
                            currentJobContract,
                            extraHoursWorked,
                            grantedBonuses,
                            seniorityBonus,
                            BigDecimalUtil.toBigDecimal(totalOtherIncomes),
                            workedDays,
                            gestionPayroll.getPayrollGenerationCycle().getEndDate(),
                            generatedPayroll.getPayrollGenerationCycle(),
                            invoicesFormMap.get(employee.getId()),
                            lastMonthBalance,
                            gestionPayroll);
                    categoryTributaryPayroll = generator.generate();
                    categoryTributaryPayroll.setGeneratedPayroll(generatedPayroll);
//                    categoryTributaryPayroll.setTotalOtherIncomes(BigDecimalUtil.subtract(categoryTributaryPayroll.getTotalGrained(), categoryTributaryPayroll.getBasicAmount()));
                    //todo improve
                    categoryTributaryPayroll.setNumber((long) (index + 1));
                    totalOtherIncomes = categoryTributaryPayroll.getTotalOtherIncomes().doubleValue();
                    totalRCIvaDiscount = categoryTributaryPayroll.getRetentionClearance().doubleValue();
                    totalAfpDiscount = categoryTributaryPayroll.getRetentionAFP().doubleValue();
                } else {
                    if (employee.getRetentionFlag()) {
                        BigDecimal amount = BigDecimalUtil.toBigDecimal((mensualTotalSalary + totalOtherIncomes));
                        if (retentionValidatorService.applyRetention(employee, gestionPayroll, amount)) {
                            totalRCIvaDiscount = (mensualTotalSalary + totalOtherIncomes) * 0.155;
                        }
                    }
                }

                totalSumOfIncomesBeforeIva += totalOtherIncomes;
                // this discounts are applied directly to liquid
                totalSumOfIncomesOutOfIva += totalIncomeOutOfIva;

                totalSumOfDiscounts += totalRCIvaDiscount + totalAfpDiscount;
                log.debug("totalSumOfDiscounts: " + totalSumOfDiscounts);

                mensualTotalSalary = BigDecimalUtil.toBigDecimal(mensualTotalSalary).doubleValue();
                log.debug("mensualTotalSalary: " + mensualTotalSalary);
                BankAccount bankAccount = employee.getPaymentType().equals(PaymentType.PAYMENT_BANK_ACCOUNT) ? bankAccountService.getDefaultAccount(employee) : null;
                /*TODO the process of collections have to be changed to take into account many contracts by employee*/
                /*------since here loan and advance discount-------*/
                log.debug("basicSalary: " + basicSalary);
                log.debug("totalSumOfIncomesBeforeIva: " + totalSumOfIncomesBeforeIva);
                log.debug("totalSumOfIncomesOutOfIva: " + totalSumOfIncomesOutOfIva);
                log.debug("totalSumOfDiscounts: " + totalSumOfDiscounts);
                BigDecimal liquid = BigDecimalUtil.toBigDecimal(basicSalary + totalSumOfIncomesBeforeIva + totalSumOfIncomesOutOfIva - totalSumOfDiscounts);
                log.debug("liquid:" + liquid);
                if (liquid.doubleValue() >= 0) {
                    BigDecimal amountToPay = quotaService.sumResidueToCollectByPayrollEmployeeAndJobCategory(employee, gestionPayroll);
                    if (amountToPay.doubleValue() > 0) {
                        log.debug("***********************************************************************");
                        log.debug("employee:" + employee.getFullName());
                        log.debug("sector:" + gestionPayroll.getJobCategory().getSector().getName());
                        log.debug("amountToPay: " + amountToPay);
                        List<Quota> quotaList = quotaService.findQuotaToCollectByPayrollEmployeeAndJobCategory(employee, gestionPayroll);
                        Double maxDiscount = liquid.doubleValue();
                        log.debug("max to discount:" + maxDiscount);
                        for (int i = 0; (i < quotaList.size() && maxDiscount >= 0); i++) {
                            Quota quota = quotaList.get(i);
                            double quotaDiscountAmount = quota.getResidue().doubleValue();
                            BigDecimal exchangeRate = BigDecimal.ONE;
                            double discount;
                            /* this assume the currency to be dollar instead of bs*/
                            if (quota.getCurrency() != FinancesCurrencyType.P) {
                                quotaDiscountAmount = quota.getResidue().doubleValue() * generatedPayroll.getExchangeRate().getSale().doubleValue();
                                exchangeRate = generatedPayroll.getExchangeRate().getSale();
                            }
                            /* Discount all if its possible, if not discount only a part*/
                            if (maxDiscount >= quotaDiscountAmount) {
                                discount = quotaDiscountAmount;
                            } else {
                                discount = maxDiscount;
                            }
                            totalSumOfDiscounts += discount;
                            if (quota.getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.ADVANCE)) {
                                totalAdvanceDiscount += discount;
                            }
                            if (quota.getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.LOAN)) {
                                totalLoanDiscount += discount;
                            }
                            if (quota.getRotatoryFund().getDocumentType().getRotatoryFundType().equals(RotatoryFundType.OTHER_RECEIVABLES)) {
                                if (PayrollColumnType.WIN.equals(quota.getRotatoryFund().getDocumentType().getPayrollColumnType())) {
                                    totalWinDiscount += discount;
                                } else if (PayrollColumnType.OTHER_DISCOUNTS.equals(quota.getRotatoryFund().getDocumentType().getPayrollColumnType())) {
                                    totalOtherDiscount += discount;
                                }
                            }

                            maxDiscount -= discount;
                            RotatoryFundCollection rotatoryFundCollection = rotatoryFundCollectionService.buildRotatoryFundCollection(
                                    gestionPayroll, quota, exchangeRate, discount, FinancesCurrencyType.P);
                            newRotatoryFundCollectionList.add(rotatoryFundCollection);
                        }
                    }
                }
                totalSumOfDiscounts = BigDecimalUtil.toBigDecimal(totalSumOfDiscounts).doubleValue();
                /*----------until here compute of loan and advance discounts---------*/
                FiscalProfessorPayroll fiscalProfessorPayroll = new FiscalProfessorPayroll();
                fiscalProfessorPayroll.setActiveForTaxPayrollGeneration(activeForTaxPayrollGeneration);
                fiscalProfessorPayroll.setContractInitDate(currentJobContract.getContract().getInitDate());
                fiscalProfessorPayroll.setContractEndDate(currentJobContract.getContract().getEndDate());
                fiscalProfessorPayroll.setGeneratedPayroll(generatedPayroll);
                fiscalProfessorPayroll.setEmployee(employee);
                fiscalProfessorPayroll.setWorkedDays(BigDecimalUtil.toBigDecimal(workedDays));
                fiscalProfessorPayroll.setBasicSalary(BigDecimalUtil.toBigDecimal(basicSalary));
                fiscalProfessorPayroll.setBasicIncome(BigDecimalUtil.toBigDecimal(mensualTotalSalary));
                fiscalProfessorPayroll.setOtherIncomes(BigDecimalUtil.toBigDecimal(totalSumOfIncomesBeforeIva));
                fiscalProfessorPayroll.setTotalIncome(BigDecimalUtil.toBigDecimal(mensualTotalSalary + totalSumOfIncomesBeforeIva));
                fiscalProfessorPayroll.setTardinessMinutesDiscount(BigDecimalUtil.toBigDecimal(totalSumOfDiscountsPerLateness));
                fiscalProfessorPayroll.setAbsenceMinutesDiscount(BigDecimalUtil.toBigDecimal(totalSumOfDiscountsPerAbsences));
                if (null != bankAccount) {
                    fiscalProfessorPayroll.setAccountNumber(bankAccount.getAccountNumber());
                    fiscalProfessorPayroll.setClientCod(bankAccount.getClientCod());
                    fiscalProfessorPayroll.setCurrency(bankAccount.getCurrency());
                }
                fiscalProfessorPayroll.setDifference(BigDecimalUtil.toBigDecimal(mensualTotalSalary + totalSumOfIncomesBeforeIva - totalSumOfDiscounts));
                fiscalProfessorPayroll.setIvaRetention(BigDecimalUtil.toBigDecimal(totalRCIvaDiscount));
                fiscalProfessorPayroll.setIncomeOutOfIva(BigDecimalUtil.toBigDecimal(totalSumOfIncomesOutOfIva));
                fiscalProfessorPayroll.setDiscountsOutOfRetention(BigDecimalUtil.toBigDecimal(totalSumOfDiscounts));
                fiscalProfessorPayroll.setLiquid(BigDecimalUtil.toBigDecimal(mensualTotalSalary + totalSumOfIncomesBeforeIva
                        + totalSumOfIncomesOutOfIva - totalSumOfDiscounts));
                fiscalProfessorPayroll.setContractMode(currentJobContract.getContract().getContractMode().getName());
                if (currentJobContract.getJob().getJobCategory() != null) {
                    fiscalProfessorPayroll.setKindOfEmployee(currentJobContract.getJob().getJobCategory().getName());
                }
                fiscalProfessorPayroll.setIvaResidue(BigDecimalUtil.toBigDecimal(ivaResidue));
                fiscalProfessorPayroll.setLastIvaResidue(BigDecimalUtil.toBigDecimal(lastIvaResidue));
                fiscalProfessorPayroll.setLaboralTotal(BigDecimalUtil.toBigDecimal(0));
                fiscalProfessorPayroll.setProHome(BigDecimalUtil.toBigDecimal(proHome));
                fiscalProfessorPayroll.setInsurance(BigDecimalUtil.toBigDecimal(0));
                fiscalProfessorPayroll.setTotalDiscount(BigDecimalUtil.toBigDecimal(totalSumOfDiscounts));
                fiscalProfessorPayroll.setBandAbsenceMinutes(absenceTotal);
                fiscalProfessorPayroll.setTardinessMinutes(tardinessTotal);
                fiscalProfessorPayroll.setWinDiscount(BigDecimalUtil.toBigDecimal(totalWinDiscount));
                fiscalProfessorPayroll.setLoanDiscount(BigDecimalUtil.toBigDecimal(totalLoanDiscount));
                fiscalProfessorPayroll.setAdvanceDiscount(BigDecimalUtil.toBigDecimal(totalAdvanceDiscount));
                fiscalProfessorPayroll.setOtherDiscounts(BigDecimalUtil.toBigDecimal(totalOtherDiscount));
                fiscalProfessorPayroll.setAfp(null != categoryTributaryPayroll && categoryTributaryPayroll.getRetentionAFP().compareTo(BigDecimal.ZERO) == 1 ?
                        categoryTributaryPayroll.getRetentionAFP() : BigDecimalUtil.toBigDecimal(0));
                fiscalProfessorPayroll.setRciva(BigDecimalUtil.toBigDecimal(totalRCIvaDiscount));
                fiscalProfessorPayroll.setUnit(currentJobContract.getJob().getOrganizationalUnit().getName());
                fiscalProfessorPayroll.setCostCenter(currentJobContract.getJob().getOrganizationalUnit().getCostCenter());
                OrganizationalLevel areaLevel = findOrganizationalLevelByName("AREA");
                OrganizationalUnit areaOrganizationalUnit = findFather(currentJobContract.getJob().getOrganizationalUnit(),
                        areaLevel);
                if (areaOrganizationalUnit != null) {
                    fiscalProfessorPayroll.setArea(areaOrganizationalUnit.getName());
                }
                fiscalProfessorPayroll.setJob(currentJobContract.getJob().getCharge().getName());
                fiscalProfessorPayroll.setCategory(currentJobContract.getJob().getJobCategory().getAcronym());

                fiscalProfessorPayroll.setBusinessUnit(currentJobContract.getJob().getOrganizationalUnit().getBusinessUnit());
                fiscalProfessorPayroll.setCostCenter(currentJobContract.getJob().getOrganizationalUnit().getCostCenter());
                fiscalProfessorPayroll.setCharge(currentJobContract.getJob().getCharge());
                fiscalProfessorPayroll.setJobCategory(currentJobContract.getJob().getJobCategory());
                fiscalProfessorPayroll.setPaymentType(employee.getPaymentType());

                if (activeForTaxPayrollGeneration) {
                    FiscalPayrollGenerator fiscalPayrollGenerator;

                    BigDecimal defaultHourDayPayment = null;
                    try {
                        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
                        defaultHourDayPayment = companyConfiguration.getHrsWorkingDay();
                    } catch (CompanyConfigurationNotFoundException e) {
                        log.error("company companyConfiguration was not found", e);
                    }
                    fiscalPayrollGenerator = new FiscalPayrollGenerator(employee,
                            currentJobContract,
                            categoryTributaryPayroll,
                            fiscalProfessorPayroll,
                            gestionPayroll,
                            defaultHourDayPayment,
                            workedDays);
                    categoryFiscalPayroll = fiscalPayrollGenerator.generate();
                    categoryFiscalPayroll.setGeneratedPayroll(generatedPayroll);
                    categoryFiscalPayroll.setNumber((long) (index + 1));
                }
                em.persist(fiscalProfessorPayroll);
                if (activeForTaxPayrollGeneration) {
                    em.persist(categoryTributaryPayroll);
                    categoryFiscalPayroll.setCategoryTributaryPayroll(categoryTributaryPayroll);
                    em.persist(categoryFiscalPayroll);
                }
            }
        }

        return PayrollGenerationResult.SUCCESS;
    }

    private DiscountRuleRange findDiscountRuleRange(Integer minutes,
                                                    Map<Integer, DiscountRuleRange> minuteDiscountRuleRangeMap,
                                                    List<DiscountRule> globalDiscountRuleList,
                                                    List<DiscountRule> businessUnitGlobalDiscountRuleList,
                                                    List<DiscountRule> jobCategoryBusinessUnitDiscountRuleList) {
        DiscountRuleRange discountRuleRange;
        if (!minuteDiscountRuleRangeMap.isEmpty() && minuteDiscountRuleRangeMap.containsKey(minutes)) {
            discountRuleRange = minuteDiscountRuleRangeMap.get(minutes);
        } else {
            discountRuleRange = discountRuleRangeService.findDiscountRuleRangeInList(minutes, jobCategoryBusinessUnitDiscountRuleList, minuteDiscountRuleRangeMap);
            if (null == discountRuleRange) {
                discountRuleRange = discountRuleRangeService.findDiscountRuleRangeInList(minutes, businessUnitGlobalDiscountRuleList, minuteDiscountRuleRangeMap);
            }
            if (null == discountRuleRange) {
                discountRuleRange = discountRuleRangeService.findDiscountRuleRangeInList(minutes, globalDiscountRuleList, minuteDiscountRuleRangeMap);
            }
        }
        return discountRuleRange;
    }

}
