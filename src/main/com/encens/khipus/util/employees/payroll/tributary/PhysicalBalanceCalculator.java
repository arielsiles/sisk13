package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class PhysicalBalanceCalculator extends Calculator<CategoryTributaryPayroll> {
    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal compareResult = BigDecimalUtil.subtract(instance.getTax(), instance.getTaxForTwoSMN(), instance.getFiscalCredit());
        instance.setPhysicalBalance(compareResult.compareTo(BigDecimal.ONE) >= 0 ? compareResult : BigDecimal.ZERO);
    }
}
