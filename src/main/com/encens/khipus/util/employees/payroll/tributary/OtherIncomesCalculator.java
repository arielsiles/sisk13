package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public class OtherIncomesCalculator extends Calculator<CategoryTributaryPayroll> {
    private BigDecimal otherIncomes;

    public OtherIncomesCalculator(BigDecimal otherIncomes) {
        this.otherIncomes = otherIncomes;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setOtherIncomes(BigDecimal.ZERO);
        if (!BigDecimalUtil.isZeroOrNull(otherIncomes)) {
            instance.setOtherIncomes(otherIncomes);
        }
        instance.setTotalOtherIncomes(BigDecimalUtil.sum(instance.getOtherIncomes(), instance.getSeniorityBonus(), instance.getExtraHourCost(), instance.getProductionBonus(), instance.getSundayBonus(), instance.getOtherBonus(), instance.getOtherIncomes()));
    }
}
