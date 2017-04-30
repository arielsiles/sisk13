package com.encens.khipus.util.employees.payroll.structure;

/**
 * @author
 * @version 2.26
 */
public class PayrollColumn<T> {
    private Calculator<T> calculator;

    public PayrollColumn(Calculator<T> calculator) {
        this.calculator = calculator;
    }

    public void calculate(T instance) {
        calculator.execute(instance);
    }

    public static <T> PayrollColumn<T> getInstance(Calculator<T> calculator) {
        return new PayrollColumn<T>(calculator);
    }
}

