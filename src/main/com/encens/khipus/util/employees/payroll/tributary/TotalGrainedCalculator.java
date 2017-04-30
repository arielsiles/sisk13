package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class TotalGrainedCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final int DAY_CONSTANT = 30;
    private Integer workedDays;

    public TotalGrainedCalculator(Integer workedDays) {
        this.workedDays = workedDays;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal value = BigDecimalUtil.toBigDecimal((null == instance.getBasicAmount() ? 0.0 : instance.getBasicAmount().doubleValue()) / DAY_CONSTANT * workedDays);
        //formula TOTAL GANADO= TOTAL GANADO + BONO DE ANTIGÜEDAD+ HORAS EXTRAS + BONOS DE PRODUCCION + BONOS DOMINICALES + OTROS BONOS + OTROS INGRESOS(MOVIMIENTOS A SALARIO)
        value = BigDecimalUtil.sum(value, instance.getTotalOtherIncomes());
        instance.setTotalGrained(value);
    }
}
