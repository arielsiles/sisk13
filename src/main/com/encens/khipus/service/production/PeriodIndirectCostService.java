package com.encens.khipus.service.production;

import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.IndirectCosts;
import com.encens.khipus.model.production.IndirectCostsConfig;
import com.encens.khipus.model.production.PeriodIndirectCost;

import javax.ejb.Local;
import java.util.List;

/**
 * Created by Diego on 29/08/2014.
 */
@Local
public interface PeriodIndirectCostService {
    public PeriodIndirectCost findLastPeriodIndirectCost();

    PeriodIndirectCost findPeriodIndirect(Month month, Gestion gestion);

    boolean findPeriodIndirect(PeriodIndirectCost periodIndirectCost);

    PeriodIndirectCost findLastPeriodIndirectCostUsed();

    public List<IndirectCostsConfig> findPredefinedIndirectCost();

    boolean findPeriodIndirectCostUsed(PeriodIndirectCost periodIndirectCost);
}
