package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class DependentBalanceCalculator extends Calculator<CategoryTributaryPayroll> {
    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal compareResult = BigDecimalUtil.subtract(BigDecimalUtil.sum(instance.getTaxForTwoSMN(), instance.getFiscalCredit()), instance.getTax());
        instance.setDependentBalance(compareResult.compareTo(BigDecimal.ONE) >= 0 ? compareResult : BigDecimal.ZERO);
    }
}
