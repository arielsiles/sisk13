package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.IVARate;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 2.26
 */
public class TaxCalculator extends Calculator<CategoryTributaryPayroll> {
    private IVARate ivaRate;

    public TaxCalculator(IVARate ivaRate) {
        this.ivaRate = ivaRate;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setTax(BigDecimalUtil.getPercentage(instance.getUnlikeTaxable(), ivaRate.getRate()));
    }
}
