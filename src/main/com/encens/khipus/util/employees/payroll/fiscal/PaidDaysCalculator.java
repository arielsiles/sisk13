package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 3.4
 */
public class PaidDaysCalculator extends Calculator<CategoryFiscalPayroll> {
    private int workedDays;

    public PaidDaysCalculator(int workedDays) {
        this.workedDays = workedDays;
    }

    @Override
    public void execute(CategoryFiscalPayroll instance) {
        instance.setPaidDays(BigDecimalUtil.toBigDecimal(workedDays));
    }
}
