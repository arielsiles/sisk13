package com.encens.khipus.util.employees.payroll.structure;

import java.util.LinkedList;

/**
 * @author
 * @version 3.4
 */
public abstract class MergeProcessor<T> {

    private LinkedList<Calculator<Object>> calculatorList = new LinkedList<Calculator<Object>>();

    protected void addCalculator(Calculator<Object> calculator) {
        calculatorList.add(calculator);
    }

    protected abstract void initialize();

    protected abstract T getInstance();

    public T merge() {
        initialize();
        T element = getInstance();
        for (Calculator<Object> calculator : calculatorList) {
            calculator.execute(element);
        }
        return element;
    }
}
