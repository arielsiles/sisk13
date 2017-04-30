package com.encens.khipus.service.budget;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.budget.BudgetDistribution;
import com.encens.khipus.model.budget.BudgetDistributionDetail;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

/**
 * BudgetDistributionService
 *
 * @author
 * @version 2.5
 */
@Local
public interface BudgetDistributionService extends GenericService {
    void create(BudgetDistribution budgetDistribution, List<BudgetDistributionDetail> budgetDistributionDetailList) throws EntryDuplicatedException;

    void update(BudgetDistribution budgetDistribution, List<BudgetDistributionDetail> budgetDistributionDetailList) throws EntryDuplicatedException, ConcurrencyException;

    Boolean validateDuplicated(BudgetDistribution budgetDistribution);

    Map<Long, Map<Month, Double>> getGlobalBudgetDistributionDetailsByGestion(Gestion gestion);

    Map<Month, Double> getGlobalBudgetDistributionByGestion(Gestion gestion);
}
