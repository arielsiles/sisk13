package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class LastMonthBalanceCalculator extends Calculator<CategoryTributaryPayroll> {
    private BigDecimal lastMonthBalance;

    public LastMonthBalanceCalculator(BigDecimal lastMonthBalance) {
        this.lastMonthBalance = lastMonthBalance;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setLastMonthBalance(lastMonthBalance);
    }
}
