package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class UsedBalanceCalculator extends Calculator<CategoryTributaryPayroll> {
    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal result;
        if (instance.getDependentTotalBalance().compareTo(BigDecimal.ONE) >= 0
                && instance.getPhysicalBalance().compareTo(instance.getDependentTotalBalance()) > 0) {
            result = instance.getDependentTotalBalance();

        } else {
            if (instance.getDependentTotalBalance().compareTo(instance.getPhysicalBalance()) >= 0) {
                result = instance.getPhysicalBalance();
            } else {
                result = BigDecimal.ZERO;
            }
        }
        instance.setUsedBalance(result);
    }
}
