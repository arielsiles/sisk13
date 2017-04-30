package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.SMNRate;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public class SalaryNotTaxableTwoSMNCalculator extends Calculator<CategoryTributaryPayroll> {
    private SMNRate smnRate;

    public SalaryNotTaxableTwoSMNCalculator(SMNRate smnRate) {
        this.smnRate = smnRate;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal twoSMN = BigDecimalUtil.multiply(smnRate.getRate(), TWO);
        instance.setSalaryNotTaxableTwoSMN((instance.getNetSalary().compareTo(twoSMN) >= 0) ? twoSMN : instance.getNetSalary());
    }
}
