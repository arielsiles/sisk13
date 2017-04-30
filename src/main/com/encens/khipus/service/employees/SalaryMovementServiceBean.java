package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.*;

/**
 * SalaryMovementServiceBean
 *
 * @author
 */
@Name("salaryMovementService")
@Stateless
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class SalaryMovementServiceBean extends GenericServiceBean implements SalaryMovementService {
    @Resource
    private UserTransaction userTransaction;

    @PersistenceContext(unitName = "khipus")
    private EntityManager em;
    @In
    private SalaryMovementTypeService salaryMovementTypeService;
    @In
    private CurrencyService currencyService;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @SuppressWarnings({"unchecked"})
    public List<SalaryMovement> findByEmployeeAndInitDateEndDate(Employee employee, Date initDate, Date endDate) {
        return getEntityManager().createNamedQuery("SalaryMovement.findByEmployeeAndInitDateEndDate")
                .setParameter("employee", employee)
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @SuppressWarnings({"unchecked"})
    public List<SalaryMovement> findByEmployeeAndGestionPayroll(Employee employee, GestionPayroll gestionPayroll) {
        return getEntityManager().createNamedQuery("SalaryMovement.findByEmployeeAndGestionPayroll")
                .setParameter("employee", employee)
                .setParameter("gestionPayroll", gestionPayroll)
                .getResultList();
    }

    @SuppressWarnings({"unchecked"})
    public Map<Long, List<SalaryMovement>> findByPayrollGenerationIdList(Class<? extends GenericPayroll> genericPayrollClass, List<Long> payrollGenerationIdList, GestionPayroll gestionPayroll) {
        Map<Long, List<SalaryMovement>> result = new HashMap<Long, List<SalaryMovement>>();
        if (!ChristmasPayroll.class.equals(genericPayrollClass)) {
            List<SalaryMovement> salaryMovementList = new ArrayList<SalaryMovement>(0);
            if (ManagersPayroll.class.equals(genericPayrollClass)) {
                salaryMovementList = getEntityManager().createNamedQuery("SalaryMovement.findByManagersPayrollIdList")
                        .setParameter("payrollGenerationIdList", payrollGenerationIdList)
                        .setParameter("movementTypeList", MovementType.discountTypeGeneratedByPayrollGeneration())
                        .setParameter("gestionPayroll", gestionPayroll)
                        .getResultList();
            } else if (GeneralPayroll.class.equals(genericPayrollClass)) {
                salaryMovementList = getEntityManager().createNamedQuery("SalaryMovement.findByGeneralPayrollIdList")
                        .setParameter("payrollGenerationIdList", payrollGenerationIdList)
                        .setParameter("movementTypeList", MovementType.discountTypeGeneratedByPayrollGeneration())
                        .setParameter("gestionPayroll", gestionPayroll)
                        .getResultList();
            } else if (FiscalProfessorPayroll.class.equals(genericPayrollClass)) {
                salaryMovementList = getEntityManager().createNamedQuery("SalaryMovement.findByFiscalProfessorPayrollIdList")
                        .setParameter("payrollGenerationIdList", payrollGenerationIdList)
                        .setParameter("movementTypeList", MovementType.discountTypeGeneratedByPayrollGeneration())
                        .setParameter("gestionPayroll", gestionPayroll)
                        .getResultList();
            }

            for (SalaryMovement salaryMovement : salaryMovementList) {
                List<SalaryMovement> movementList = result.get(salaryMovement.getEmployee().getId());
                if (movementList == null) {
                    movementList = new ArrayList<SalaryMovement>();
                    movementList.add(salaryMovement);
                    result.put(salaryMovement.getEmployee().getId(), movementList);
                } else {
                    movementList.add(salaryMovement);
                }
            }
        }
        return result;
    }

    public SalaryMovement load(SalaryMovement salaryMovement) throws EntryNotFoundException {
        SalaryMovement result = null;
        try {
            result = (SalaryMovement) getEntityManager().createNamedQuery("SalaryMovement.loadSalaryMovement")
                    .setParameter("id", salaryMovement.getId()).getSingleResult();
        } catch (NoResultException ignored) {

        }
        if (result == null) {
            throw new EntryNotFoundException();
        }
        return result;
    }

    public void matchGeneratedSalaryMovement(GeneratedPayroll generatedPayroll, List<? extends GenericPayroll> genericPayrollList) throws ConcurrencyException, EntryDuplicatedException {
        Map<MovementType, SalaryMovementType> defaultSalaryMovementTypeMap = new HashMap<MovementType, SalaryMovementType>();
        Currency baseCurrency = null;
        try {
            userTransaction.setTransactionTimeout(genericPayrollList.size() * 60);
            userTransaction.begin();
            for (GenericPayroll genericPayroll : genericPayrollList) {
                if (baseCurrency == null) {
                    if (genericPayroll instanceof GeneralPayroll) {
                        baseCurrency = currencyService.getCurrencyById(Constants.currencyIdSus);
                    } else {
                        baseCurrency = currencyService.findBaseCurrency();
                    }
                }
                createOrUpdate(generatedPayroll, genericPayroll.getEmployee(), defaultSalaryMovementTypeMap, MovementType.TARDINESS_MINUTES, baseCurrency, genericPayroll.getTardinessMinutesDiscount());
                createOrUpdate(generatedPayroll, genericPayroll.getEmployee(), defaultSalaryMovementTypeMap, MovementType.LOAN, baseCurrency, genericPayroll.getLoanDiscount());
                createOrUpdate(generatedPayroll, genericPayroll.getEmployee(), defaultSalaryMovementTypeMap, MovementType.ADVANCE_PAYMENT, baseCurrency, genericPayroll.getAdvanceDiscount());
                createOrUpdate(generatedPayroll, genericPayroll.getEmployee(), defaultSalaryMovementTypeMap, MovementType.AFP, baseCurrency, genericPayroll.getAfp());
                createOrUpdate(generatedPayroll, genericPayroll.getEmployee(), defaultSalaryMovementTypeMap, MovementType.RCIVA, baseCurrency, genericPayroll.getRciva());
                createOrUpdate(generatedPayroll, genericPayroll.getEmployee(), defaultSalaryMovementTypeMap, MovementType.WIN, baseCurrency, genericPayroll.getWinDiscount());
                createOrUpdate(generatedPayroll, genericPayroll.getEmployee(), defaultSalaryMovementTypeMap, MovementType.OTHER_DISCOUNT, baseCurrency, genericPayroll.getOtherDiscounts());
                createOrUpdate(generatedPayroll, genericPayroll.getEmployee(), defaultSalaryMovementTypeMap, MovementType.DISCOUNT_OUT_OF_RETENTION, baseCurrency, genericPayroll.getDiscountsOutOfRetention());
            }
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            e.printStackTrace();
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            e.printStackTrace();
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException(e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void createOrUpdate(GeneratedPayroll generatedPayroll, Employee employee, Map<MovementType, SalaryMovementType> defaultSalaryMovementTypeMap, MovementType movementType, Currency currency, BigDecimal amount) throws ConcurrencyException, EntryDuplicatedException {
        if (BigDecimalUtil.isPositive(amount)) {
            List<SalaryMovement> salaryMovementList = getEntityManager().createNamedQuery("SalaryMovement.findSalaryMovementByMovementTypeAndEmployeeAndGestionPayroll")
                    .setParameter("gestionPayroll", generatedPayroll.getGestionPayroll())
                    .setParameter("employee", employee)
                    .setParameter("movementType", movementType)
                    .getResultList();
            SalaryMovement salaryMovement = null;
            if (!ValidatorUtil.isEmptyOrNull(salaryMovementList)) {
                if (salaryMovementList.size() == 1) {
                    salaryMovement = salaryMovementList.get(0);
                    salaryMovement.setAmount(amount);
                    salaryMovement.setCurrency(currency);
                } else {
                    // delete all if exists more that 1 element
                    deleteUnusedMovements(generatedPayroll.getGestionPayroll(), employee, movementType);
                }
            }
            if (salaryMovement != null) {
                update(salaryMovement);
            } else {
                String defaultDescription = generateDefaultDescription(generatedPayroll, movementType);
                salaryMovement = new SalaryMovement(generatedPayroll.getGestionPayroll().getEndDate(), defaultDescription, amount, generatedPayroll.getGestionPayroll(), currency, getDefaultMovementType(defaultSalaryMovementTypeMap, movementType), employee);
                create(salaryMovement);
            }
        } else {
            deleteUnusedMovements(generatedPayroll.getGestionPayroll(), employee, movementType);
        }
    }

    private void deleteUnusedMovements(GestionPayroll gestionPayroll, Employee employee, MovementType movementType) {
        getEntityManager().createNamedQuery("SalaryMovement.deleteSalaryMovementByMovementTypeAndEmployeeAndGestionPayroll")
                .setParameter("gestionPayroll", gestionPayroll)
                .setParameter("employee", employee)
                .setParameter("movementType", movementType).executeUpdate();
        getEntityManager().flush();
    }

    private SalaryMovementType getDefaultMovementType(Map<MovementType, SalaryMovementType> defaultSalaryMovementTypeMap, MovementType movementType) {
        SalaryMovementType salaryMovementType = defaultSalaryMovementTypeMap.get(movementType);
        if (salaryMovementType == null) {
            salaryMovementType = salaryMovementTypeService.findDefaultByMovementType(movementType);
            defaultSalaryMovementTypeMap.put(movementType, salaryMovementType);
        }
        return salaryMovementType;
    }

    public String generateDefaultDescription(GeneratedPayroll generatedPayroll, MovementType movementType) {
        String defaultDescription = MessageUtils.getMessage("SalaryMovement.defaultDescription", generatedPayroll.getName(), MessageUtils.getMessage(movementType.getResourceKey()));
        if (defaultDescription.length() > 200) {
            defaultDescription = defaultDescription.substring(0, 199);
        }
        return defaultDescription;
    }
}
