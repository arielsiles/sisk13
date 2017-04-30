package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.ExtraHoursWorked;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class ExtraHourCalculator extends Calculator<CategoryTributaryPayroll> {
    private ExtraHoursWorked extraHoursWorked;

    public ExtraHourCalculator(ExtraHoursWorked extraHoursWorked) {
        this.extraHoursWorked = extraHoursWorked;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setExtraHour(BigDecimal.ZERO);
        instance.setExtraHourCost(BigDecimal.ZERO);
        if (null != extraHoursWorked) {
            instance.setExtraHour(extraHoursWorked.getExtraHours());
            instance.setExtraHourCost(extraHoursWorked.getTotalPaid());
        }
    }
}
