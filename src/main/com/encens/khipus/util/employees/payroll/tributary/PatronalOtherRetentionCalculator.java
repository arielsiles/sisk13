package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CNSRate;
import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 3.4
 */
public class PatronalOtherRetentionCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final int TWO_DECIMAL_SCALE = 2;
    private CNSRate cnsRate;

    public PatronalOtherRetentionCalculator(CNSRate cnsRate) {
        this.cnsRate = cnsRate;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setCns(BigDecimalUtil.getPercentage(instance.getTotalGrained(),
                cnsRate.getRate(), TWO_DECIMAL_SCALE));
    }

}
