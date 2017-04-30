package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class LiquidPaymentCalculator extends Calculator<CategoryFiscalPayroll> {
    @Override
    public void execute(CategoryFiscalPayroll instance) {
        BigDecimal result = BigDecimalUtil.subtract(instance.getTotalGrained(), instance.getTotalDiscount());
        instance.setLiquidPayment(result);
    }
}