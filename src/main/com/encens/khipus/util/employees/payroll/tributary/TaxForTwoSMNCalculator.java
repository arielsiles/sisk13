package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.IVARate;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class TaxForTwoSMNCalculator extends Calculator<CategoryTributaryPayroll> {
    private IVARate ivaRate;

    public TaxForTwoSMNCalculator(IVARate ivaRate) {
        this.ivaRate = ivaRate;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal compareResult = BigDecimalUtil.getPercentage(instance.getSalaryNotTaxableTwoSMN(), ivaRate.getRate(), 0);
        instance.setTaxForTwoSMN(instance.getTax().compareTo(compareResult) > 0 ? compareResult : instance.getTax());
    }
}
