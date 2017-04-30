package com.encens.khipus.service.budget;

import com.encens.khipus.action.AppIdentity;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.budget.BudgetDistribution;
import com.encens.khipus.model.budget.BudgetDistributionDetail;
import com.encens.khipus.model.budget.BudgetState;
import com.encens.khipus.model.budget.EntryBudget;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * EntryBudgetServiceBean
 *
 * @author
 * @version 2.2
 */
@Name("entryBudgetService")
@Stateless
@AutoCreate
public class EntryBudgetServiceBean extends GenericServiceBean implements EntryBudgetService {
    @In
    private AppIdentity identity;

    @TransactionAttribute(REQUIRES_NEW)
    public void verifyInBlock(List<Long> selectedListId) {
        for (Long expenseBudgetId : selectedListId) {
            EntryBudget entryBudget = getEntityManager().find(EntryBudget.class, expenseBudgetId);
            if ((entryBudget.getEditable() || identity.hasPermission("ENTRYBUDGETSAPPROVAL", "VIEW")) && (BudgetState.ELABORATED.equals(entryBudget.getState()) || BudgetState.BLOCKED.equals(entryBudget.getState()))) {
                entryBudget.setEditable(true);
                entryBudget.setState(BudgetState.CHECKED);
                getEntityManager().merge(entryBudget);
            }
        }
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void approveInBlock(List<Long> selectedListId) {
        for (Long expenseBudgetId : selectedListId) {
            EntryBudget entryBudget = getEntityManager().find(EntryBudget.class, expenseBudgetId);
            if (BudgetState.CHECKED.equals(entryBudget.getState()) || BudgetState.BLOCKED.equals(entryBudget.getState())) {
                entryBudget.setEditable(false);
                entryBudget.setState(BudgetState.APPROVED);
                getEntityManager().merge(entryBudget);
            }
        }
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void freezeInBlock(List<Long> selectedListId) {
        for (Long expenseBudgetId : selectedListId) {
            EntryBudget entryBudget = getEntityManager().find(EntryBudget.class, expenseBudgetId);
            if (!BudgetState.BLOCKED.equals(entryBudget.getState())) {
                entryBudget.setEditable(false);
                entryBudget.setState(BudgetState.BLOCKED);
                getEntityManager().merge(entryBudget);
            }
        }
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void create(EntryBudget entryBudget, List<BudgetDistributionDetail> budgetDistributionDetailList,
                       BudgetDistribution budgetDistribution, boolean includeBudgetDistribution) throws EntryDuplicatedException {
        try {
            if (includeBudgetDistribution && budgetDistribution != null) {
                budgetDistribution.setBusinessUnit(entryBudget.getBusinessUnit());
                budgetDistribution.setGestion(entryBudget.getGestion());
                getEntityManager().persist(budgetDistribution);
                for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
                    budgetDistributionDetail.setBudgetDistribution(budgetDistribution);
                    getEntityManager().persist(budgetDistributionDetail);
                }
                entryBudget.setBudgetDistribution(budgetDistribution);
            }
            getEntityManager().persist(entryBudget);
            getEntityManager().flush();
        } catch (PersistenceException e) {
            log.debug("Persistence error..", e);
            throw new EntryDuplicatedException();
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void update(EntryBudget entryBudget, List<BudgetDistributionDetail> budgetDistributionDetailList,
                       BudgetDistribution budgetDistribution, boolean includeBudgetDistribution)
            throws ConcurrencyException, EntryDuplicatedException, ReferentialIntegrityException {
        try {
            if (!includeBudgetDistribution) {
                if (null != entryBudget.getBudgetDistribution()) {
                    try {
                        for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
                            getEntityManager().remove(budgetDistributionDetail);
                        }
                        getEntityManager().remove(entryBudget.getBudgetDistribution());
                    } catch (OptimisticLockException e) {
                        throw new ConcurrencyException(e);
                    } catch (EntityNotFoundException e) {
                        log.debug(e, "the element was not found...");
                    } catch (PersistenceException e) {
                        throw new ReferentialIntegrityException(e);
                    }
                    entryBudget.setBudgetDistribution(null);
                }
            } else {
                if (budgetDistribution != null && budgetDistribution.getId() == null) {
                    // create case
                    budgetDistribution.setBusinessUnit(entryBudget.getBusinessUnit());
                    budgetDistribution.setGestion(entryBudget.getGestion());
                    getEntityManager().persist(budgetDistribution);
                    for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
                        budgetDistributionDetail.setBudgetDistribution(budgetDistribution);
                        getEntityManager().persist(budgetDistributionDetail);
                    }
                    entryBudget.setBudgetDistribution(budgetDistribution);
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

            if (!getEntityManager().contains(entryBudget)) {
                getEntityManager().merge(entryBudget);
            }
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            throw new EntryDuplicatedException(ee);
        }
    }

    public void delete(EntryBudget entryBudget) throws ConcurrencyException, ReferentialIntegrityException {
        try {
            if (null != entryBudget.getBudgetDistribution() && null != entryBudget.getBudgetDistribution().getId()) {
                for (BudgetDistributionDetail budgetDistributionDetail : entryBudget.getBudgetDistribution().getBudgetDistributionDetailList()) {
                    getEntityManager().remove(budgetDistributionDetail);
                }
                getEntityManager().remove(entryBudget.getBudgetDistribution());
            }
            getEntityManager().remove(entryBudget);
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (EntityNotFoundException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException(e);
        }
    }
}
