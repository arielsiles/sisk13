package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class DependentBalanceToNextMonthCalculator extends Calculator<CategoryTributaryPayroll> {
    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal result = BigDecimal.ZERO;
        if (instance.getUsedBalance().compareTo(instance.getDependentTotalBalance()) == -1) {
            result = BigDecimalUtil.subtract(instance.getDependentBalance(), instance.getUsedBalance());
        }
        instance.setDependentBalanceToNextMonth(result);
    }
}
