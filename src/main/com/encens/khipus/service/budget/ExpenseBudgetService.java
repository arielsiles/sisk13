package com.encens.khipus.service.budget;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.budget.BudgetDistribution;
import com.encens.khipus.model.budget.BudgetDistributionDetail;
import com.encens.khipus.model.budget.ExpenseBudget;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * ExpenseBudgetService
 *
 * @author
 * @version 2.0
 */
@Local
public interface ExpenseBudgetService extends GenericService {
    Boolean hasValidEditableState(ExpenseBudget expenseBudget) throws EntryNotFoundException;

    void verifyInBlock(List<Long> selectedListId);

    void approveInBlock(List<Long> selectedListId);

    void freezeInBlock(List<Long> selectedListId);

    BigDecimal getAccumulatedExecutionBetween(String executorUnitCode, String costCenterCode, Date startDate, Date endDate, Long expenseBudgetId);

    BigDecimal getAccumulatedExecutionByBusinessUnit(String executorUnitCode, Long classifierId, Date startDate, Date endDate);

    BigDecimal getAccumulatedExecutionByClassifier(Long classifierId, Date startDate, Date endDate);

    BigDecimal getExpenseBudgetAmountByBusinessUnit(Long businessUnitId, Long classifierId, Long gestionId);

    BigDecimal getExpenseBudgetAmountByClassifier(Long classifierId, Long gestionId);

    void create(ExpenseBudget expenseBudget, List<BudgetDistributionDetail> budgetDistributionDetailList,
                BudgetDistribution budgetDistribution, boolean includeBudgetDistribution) throws EntryDuplicatedException;

    void update(ExpenseBudget expenseBudget, List<BudgetDistributionDetail> budgetDistributionDetailList,
                BudgetDistribution budgetDistribution, boolean includeBudgetDistribution)
            throws ConcurrencyException, EntryDuplicatedException, ReferentialIntegrityException;

    void delete(ExpenseBudget expenseBudget) throws ConcurrencyException, ReferentialIntegrityException;
}
