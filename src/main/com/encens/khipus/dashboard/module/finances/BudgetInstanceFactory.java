package com.encens.khipus.dashboard.module.finances;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class BudgetInstanceFactory implements InstanceFactory<Budget> {
    private String monthName;

    public Budget createInstance(Budget cachedObject, Object[] row) {
        Budget budget = new Budget();
        budget.setYearlyBudget((BigDecimal) row[0]);
        budget.setAccruedExecution((BigDecimal) row[1]);
        budget.setMonthlyExecution((BigDecimal) row[2]);
        budget.setMonthlyBudget((BigDecimal) row[3]);
        budget.setMonthName(monthName);
        return budget;
    }

    public Object getIdentifierValue(Object[] row) {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }
}
