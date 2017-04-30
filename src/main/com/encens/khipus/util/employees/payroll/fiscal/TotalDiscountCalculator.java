package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class TotalDiscountCalculator extends Calculator<CategoryFiscalPayroll> {

    @Override
    public void execute(CategoryFiscalPayroll instance) {
        BigDecimal result = BigDecimalUtil.sum(instance.getRetentionAFP(),
                instance.getRetentionClearance(),
                instance.getOtherDiscount());
        instance.setTotalDiscount(result);
    }
}
