package com.encens.khipus.util.employees.payroll.structure;

import java.util.LinkedList;

/**
 * @author
 * @version 2.26
 */
public abstract class PayrollGenerator<T> {
    private LinkedList<PayrollColumn<T>> columns = new LinkedList<PayrollColumn<T>>();

    protected void addColumn(PayrollColumn<T> column) {
        columns.add(column);
    }

    public T generate() {
        initializeColumns();
        T element = getInstance();
        for (PayrollColumn<T> column : columns) {
            column.calculate(element);
        }

        return element;
    }

    protected abstract void initializeColumns();

    protected abstract T getInstance();
}
