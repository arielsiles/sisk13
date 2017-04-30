package com.encens.khipus.util.employees.payroll.structure;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public abstract class Calculator<T> {
    protected static final BigDecimal TWO = new BigDecimal("2");

    public abstract void execute(T instance);
}

