package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 2.26
 */
public class RetentionClearanceCalculator extends Calculator<CategoryTributaryPayroll> {
    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setRetentionClearance(BigDecimalUtil.subtract(instance.getPhysicalBalance(), instance.getUsedBalance()));
    }
}
