package com.encens.khipus.util.employees.payroll.fiscal;

import com.encens.khipus.model.employees.CategoryFiscalPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public class OtherDiscountsCalculator extends Calculator<CategoryFiscalPayroll> {

    @Override
    public void execute(CategoryFiscalPayroll instance) {
        BigDecimal result = BigDecimalUtil.sum(
                instance.getAbsenceMinutesDiscount(),
                instance.getTardinessMinutesDiscount(),
                instance.getLoanDiscount(),
                instance.getAdvanceDiscount(),
                instance.getWinDiscount(),
                instance.getOtherSalaryMovementDiscount()
        );
        instance.setOtherDiscount(result);
    }
}
