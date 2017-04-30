package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class BudgetExtendedInstanceFactory implements InstanceFactory<BudgetExtended> {
    public BudgetExtended createInstance(BudgetExtended cachedObject, Object[] row) {
        BudgetExtended instance = new BudgetExtended();
        instance.setCode(row[0].toString());
        instance.setName(row[1].toString());
        instance.setYearBudget((BigDecimal) row[2]);
        instance.setAccruedExecution((BigDecimal) row[3]);
        instance.setMonthBudget((BigDecimal) row[4]);
        instance.setMonthExecution((BigDecimal) row[5]);
        instance.setMonthVarianceExecution((BigDecimal) row[6]);
        instance.setCurrentExecution((BigDecimal) row[7]);
        instance.setYearVarianceExecution((BigDecimal) row[8]);
        instance.setYearPercentageExecution((BigDecimal) row[9]);

        return instance;
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }
}
