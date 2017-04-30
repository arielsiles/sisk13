package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 3.4
 */
public class BasicAmountCalculator extends Calculator<CategoryFiscalPayroll> {

    private CategoryTributaryPayroll categoryTributaryPayroll;

    public BasicAmountCalculator(CategoryTributaryPayroll categoryTributaryPayroll) {
        this.categoryTributaryPayroll = categoryTributaryPayroll;
    }

    @Override
    public void execute(CategoryFiscalPayroll instance) {
        instance.setBasicAmount(categoryTributaryPayroll.getBasicAmount());
    }
}
