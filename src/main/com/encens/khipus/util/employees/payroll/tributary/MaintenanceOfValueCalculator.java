package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class MaintenanceOfValueCalculator extends Calculator<CategoryTributaryPayroll> {
    private static final int SCALE = 6;
    private BigDecimal initialUfvExchangeRate;
    private BigDecimal finalUfvExchangeRate;

    public MaintenanceOfValueCalculator(BigDecimal initialUfvExchangeRate, BigDecimal finalUfvExchangeRate) {
        this.initialUfvExchangeRate = initialUfvExchangeRate;
        this.finalUfvExchangeRate = finalUfvExchangeRate;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        BigDecimal value = BigDecimal.ZERO;
        if (null != instance.getLastMonthBalance() && instance.getLastMonthBalance().compareTo(BigDecimal.ZERO) == 1) {
            value = BigDecimalUtil.subtract(BigDecimalUtil.divide(BigDecimalUtil.multiply(instance.getLastMonthBalance(), finalUfvExchangeRate, SCALE),
                    initialUfvExchangeRate, SCALE),
                    instance.getLastMonthBalance());
        }
        instance.setMaintenanceOfValue(value);
    }
}
