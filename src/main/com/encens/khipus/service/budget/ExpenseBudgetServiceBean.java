package com.encens.khipus.service.budget;

import com.encens.khipus.action.AppIdentity;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.budget.BudgetDistribution;
import com.encens.khipus.model.budget.BudgetDistributionDetail;
import com.encens.khipus.model.budget.BudgetState;
import com.encens.khipus.model.budget.ExpenseBudget;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * ExpenseBudgetServiceBean
 *
 * @author
 * @version 2.0
 */
@Name("expenseBudgetService")
@Stateless
@AutoCreate
public class ExpenseBudgetServiceBean extends GenericServiceBean implements ExpenseBudgetService {

    @In
    private AppIdentity identity;
    @In
    private EntityManager listEntityManager;

    public ExpenseBudget getDBExpenseBudget(ExpenseBudget budgetProgram) throws EntryNotFoundException {
        if (budgetProgram == null || budgetProgram.getId() == null) {
            throw new EntryNotFoundException("Entity not found: the entity is null");
        }
        ExpenseBudget object = listEntityManager.find(ExpenseBudget.class, budgetProgram.getId());
        if (object != null) {
            return object;
        } else {
            throw new EntryNotFoundException("Entity not found: " + budgetProgram.getId());
        }
    }

    public Boolean hasValidEditableState(ExpenseBudget expenseBudget) throws EntryNotFoundException {
        return (expenseBudget != null && ((expenseBudget = getDBExpenseBudget(expenseBudget)).getEditable() || identity.hasPermission("EXPENSEBUDGETSAPPROVAL", "VIEW")));
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void verifyInBlock(List<Long> selectedListId) {
        for (Long expenseBudgetId : selectedListId) {
            ExpenseBudget expenseBudget = getEntityManager().find(ExpenseBudget.class, expenseBudgetId);
            if ((expenseBudget.getEditable() || identity.hasPermission("EXPENSEBUDGETSAPPROVAL", "VIEW")) && (BudgetState.ELABORATED.equals(expenseBudget.getState()) || BudgetState.BLOCKED.equals(expenseBudget.getState()))) {
                expenseBudget.setEditable(true);
                expenseBudget.setState(BudgetState.CHECKED);
                getEntityManager().merge(expenseBudget);
            }
        }
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void approveInBlock(List<Long> selectedListId) {
        for (Long expenseBudgetId : selectedListId) {
            ExpenseBudget expenseBudget = getEntityManager().find(ExpenseBudget.class, expenseBudgetId);
            if (BudgetState.CHECKED.equals(expenseBudget.getState()) || BudgetState.BLOCKED.equals(expenseBudget.getState())) {
                expenseBudget.setEditable(false);
                expenseBudget.setState(BudgetState.APPROVED);
                getEntityManager().merge(expenseBudget);
            }
        }
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void freezeInBlock(List<Long> selectedListId) {
        for (Long expenseBudgetId : selectedListId) {
            ExpenseBudget expenseBudget = getEntityManager().find(ExpenseBudget.class, expenseBudgetId);
            if (!BudgetState.BLOCKED.equals(expenseBudget.getState())) {
                expenseBudget.setEditable(false);
                expenseBudget.setState(BudgetState.BLOCKED);
                getEntityManager().merge(expenseBudget);
            }
        }
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void create(ExpenseBudget expenseBudget, List<BudgetDistributionDetail> budgetDistributionDetailList,
                       BudgetDistribution budgetDistribution, boolean includeBudgetDistribution) throws EntryDuplicatedException {
        try {
            if (includeBudgetDistribution && budgetDistribution != null) {
                budgetDistribution.setBusinessUnit(expenseBudget.getBusinessUnit());
                budgetDistribution.setGestion(expenseBudget.getGestion());
                getEntityManager().persist(budgetDistribution);
                for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
                    budgetDistributionDetail.setBudgetDistribution(budgetDistribution);
                    getEntityManager().persist(budgetDistributionDetail);
                }
                expenseBudget.setBudgetDistribution(budgetDistribution);
            }
            getEntityManager().persist(expenseBudget);
            getEntityManager().flush();
        } catch (PersistenceException e) {
            log.debug("Persistence error..", e);
            throw new EntryDuplicatedException();
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void update(ExpenseBudget expenseBudget, List<BudgetDistributionDetail> budgetDistributionDetailList,
                       BudgetDistribution budgetDistribution, boolean includeBudgetDistribution)
            throws ConcurrencyException, EntryDuplicatedException, ReferentialIntegrityException {
        try {
            if (!includeBudgetDistribution) {
                if (null != expenseBudget.getBudgetDistribution()) {
                    try {
                        for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
                            getEntityManager().remove(budgetDistributionDetail);
                        }
                        getEntityManager().remove(expenseBudget.getBudgetDistribution());
                    } catch (OptimisticLockException e) {
                        throw new ConcurrencyException(e);
                    } catch (EntityNotFoundException e) {
                        log.debug(e, "the element was not found...");
                    } catch (PersistenceException e) {
                        throw new ReferentialIntegrityException(e);
                    }
                    expenseBudget.setBudgetDistribution(null);
                }

            } else {
                if (budgetDistribution != null && budgetDistribution.getId() == null) {
                    // create case
                    budgetDistribution.setBusinessUnit(expenseBudget.getBusinessUnit());
                    budgetDistribution.setGestion(expenseBudget.getGestion());
                    getEntityManager().persist(budgetDistribution);
                    for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
                        budgetDistributionDetail.setBudgetDistribution(budgetDistribution);
                        getEntityManager().persist(budgetDistributionDetail);
                    }
                    expenseBudget.setBudgetDistribution(budgetDistribution);
                } else {
                    // update case
                    if (!getEntityManager().contains(budgetDistribution)) {
                        getEntityManager().merge(budgetDistribution);
                    }
                    for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
                        if (!getEntityManager().contains(budgetDistributionDetail)) {
                            getEntityManager().merge(budgetDistributionDetail);
                        }
                    }
                }
            }

            if (!getEntityManager().contains(expenseBudget)) {
                getEntityManager().merge(expenseBudget);
            }
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            throw new EntryDuplicatedException(ee);
        }
    }

    public void delete(ExpenseBudget expenseBudget) throws ConcurrencyException, ReferentialIntegrityException {
        try {
            if (null != expenseBudget.getBudgetDistribution() && null != expenseBudget.getBudgetDistribution().getId()) {
                for (BudgetDistributionDetail budgetDistributionDetail : expenseBudget.getBudgetDistribution().getBudgetDistributionDetailList()) {
                    getEntityManager().remove(budgetDistributionDetail);
                }
                getEntityManager().remove(expenseBudget.getBudgetDistribution());
            }
            getEntityManager().remove(expenseBudget);
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (EntityNotFoundException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException(e);
        }
    }

    /**
     * Get the accumulated execution since firstDate and before endDate
     *
     * @param expenseBudgetId The expenseBudget id
     * @param startDate       Since this date
     * @param endDate         Up to this date
     * @return the accumulatedExecution
     */
    public BigDecimal getAccumulatedExecutionBetween(String executorUnitCode, String costCenterCode, Date startDate, Date endDate, Long expenseBudgetId) {
        return (BigDecimal) getEntityManager().createNamedQuery("ExpenseBudget.sumAccumulatedExecutionBetween")
                .setParameter("executorUnitCode", executorUnitCode)
                .setParameter("costCenterCode", costCenterCode)
                .setParameter("expenseBudgetIdParam", expenseBudgetId)
                .setParameter("startMovementDateParam", startDate)
                .setParameter("endMovementDateParam", endDate).getSingleResult();
    }

    /**
     * Get the accumulated execution since firstDate, before endDate and executorUnit code, for a specific classifier.
     *
     * @param executorUnitCode the executor unit code
     * @param classifierId     The classifier id
     * @param startDate        Since this date
     * @param endDate          Up to this date
     * @return the accumulatedExecution
     */
    public BigDecimal getAccumulatedExecutionByBusinessUnit(String executorUnitCode, Long classifierId, Date startDate, Date endDate) {
        return (BigDecimal) getEntityManager().createNamedQuery("ExpenseBudget.sumAccumulatedExecutionByBusinessUnit")
                .setParameter("executorUnitCode", executorUnitCode)
                .setParameter("classifierId", classifierId)
                .setParameter("startMovementDate", startDate)
                .setParameter("endMovementDate", endDate).getSingleResult();
    }

    /**
     * Get the accumulated execution since firstDate and before endDate, for a specific classifier.
     *
     * @param classifierId The classifier id
     * @param startDate    Since this date
     * @param endDate      Up to this date
     * @return the accumulatedExecution
     */
    public BigDecimal getAccumulatedExecutionByClassifier(Long classifierId, Date startDate, Date endDate) {
        return (BigDecimal) getEntityManager().createNamedQuery("ExpenseBudget.sumAccumulatedExecutionByClassifier")
                .setParameter("classifierId", classifierId)
                .setParameter("startMovementDate", startDate)
                .setParameter("endMovementDate", endDate).getSingleResult();
    }

    /**
     * Get the expense budget amount that has been registered by BusinessUnit, Classifier and Gestion.
     *
     * @param businessUnitId The businessUnit id
     * @param classifierId   The classifier id
     * @param gestionId      The gestion Id
     * @return the accumulatedExecution
     */
    public BigDecimal getExpenseBudgetAmountByBusinessUnit(Long businessUnitId, Long classifierId, Long gestionId) {
        return (BigDecimal) getEntityManager().createNamedQuery("ExpenseBudget.sumExpenseBudgetByBusinessUnit")
                .setParameter("businessUnitId", businessUnitId)
                .setParameter("classifierId", classifierId)
                .setParameter("gestionId", gestionId)
                .getSingleResult();
    }

    /**
     * Get the expense budget amount that has been registered by Classifier and Gestion.
     *
     * @param classifierId The classifier id
     * @param gestionId    The gestion Id
     * @return the accumulatedExecution
     */
    public BigDecimal getExpenseBudgetAmountByClassifier(Long classifierId, Long gestionId) {
        return (BigDecimal) getEntityManager().createNamedQuery("ExpenseBudget.sumExpenseBudgetByClassifier")
                .setParameter("classifierId", classifierId)
                .setParameter("gestionId", gestionId)
                .getSingleResult();
    }
}
