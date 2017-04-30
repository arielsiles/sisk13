package com.encens.khipus.service.budget;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.budget.BudgetDistribution;
import com.encens.khipus.model.budget.BudgetDistributionDetail;
import com.encens.khipus.model.budget.EntryBudget;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * EntryBudgetService
 *
 * @author
 * @version 2.2
 */
@Local
public interface EntryBudgetService extends GenericService {

    void verifyInBlock(List<Long> selectedListId);

    void approveInBlock(List<Long> selectedListId);

    void freezeInBlock(List<Long> selectedListId);

    @TransactionAttribute(REQUIRES_NEW)
    void create(EntryBudget entryBudget, List<BudgetDistributionDetail> budgetDistributionDetailList, BudgetDistribution budgetDistribution, boolean includeBudgetDistribution) throws EntryDuplicatedException;

    void update(EntryBudget entryBudget, List<BudgetDistributionDetail> budgetDistributionDetailList, BudgetDistribution budgetDistribution, boolean includeBudgetDistribution) throws ConcurrencyException, EntryDuplicatedException, ReferentialIntegrityException;

    void delete(EntryBudget entryBudget) throws ConcurrencyException, ReferentialIntegrityException;
}
