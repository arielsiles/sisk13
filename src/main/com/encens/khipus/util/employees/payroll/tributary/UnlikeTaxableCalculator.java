package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.SMNRate;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 2.26
 */
public class UnlikeTaxableCalculator extends Calculator<CategoryTributaryPayroll> {

    private SMNRate smnRate;

    public UnlikeTaxableCalculator(SMNRate smnRate) {
        this.smnRate = smnRate;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setUnlikeTaxable(BigDecimalUtil.subtract(instance.getNetSalary(), instance.getSalaryNotTaxableTwoSMN()));
    }
}
