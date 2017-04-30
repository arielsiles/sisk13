package com.encens.khipus.util.employees.payroll.structure;

import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.ReflectionUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author
 * @version 3.4
 */
public class BasicMergeCalculator extends Calculator<Object> {
    private String propertyName;
    private BigDecimal rootValue;
    private BigDecimal childValue;
    private Operator operator;

    public BasicMergeCalculator(String propertyName, Object root, Object child, Operator operator) {
        this.propertyName = propertyName;
        try {
            this.rootValue = BigDecimalUtil.toBigDecimal(ReflectionUtils.get(root, propertyName));
        } catch (Exception ignored) {
        }
        try {
            this.childValue = BigDecimalUtil.toBigDecimal(ReflectionUtils.get(child, propertyName));
        } catch (Exception ignored) {
        }
        this.operator = operator;
    }

    public static BasicMergeCalculator sum(String propertyName, Object root, Object child) {
        return new BasicMergeCalculator(propertyName, root, child, Operator.SUM);
    }

    public static BasicMergeCalculator avg(String propertyName, Object root, Object child) {
        return new BasicMergeCalculator(propertyName, root, child, Operator.AVG);
    }

    public static BasicMergeCalculator max(String propertyName, Object root, Object child) {
        return new BasicMergeCalculator(propertyName, root, child, Operator.MAX);
    }

    public static BasicMergeCalculator min(String propertyName, Object root, Object child) {
        return new BasicMergeCalculator(propertyName, root, child, Operator.MIN);
    }

    @Override
    public void execute(Object instance) {
        try {
            Class valueClass = ReflectionUtils.getField(instance.getClass(), propertyName).getType();
            ReflectionUtils.set(instance, propertyName, calculateValue(valueClass));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Number calculateValue(Class valueClass) {
        BigDecimal value = BigDecimal.ZERO;

        if (Operator.SUM.equals(operator)) {
            value = BigDecimalUtil.sum(rootValue, childValue);
        } else if (Operator.AVG.equals(operator)) {
            value = BigDecimalUtil.avg(rootValue, childValue);
        } else if (Operator.MAX.equals(operator)) {
            value = BigDecimalUtil.max(rootValue, childValue);
        } else if (Operator.MIN.equals(operator)) {
            value = BigDecimalUtil.min(rootValue, childValue);
        }

        if (!BigDecimal.class.equals(valueClass)) {
            if (Integer.class.equals(valueClass)) {
                return value.intValue();
            } else if (Long.class.equals(valueClass)) {
                return value.longValue();
            } else if (Double.class.equals(valueClass)) {
                return value.doubleValue();
            } else if (Float.class.equals(valueClass)) {
                return value.floatValue();
            } else if (BigInteger.class.equals(valueClass)) {
                return value.toBigInteger();
            }
        }

        return value;
    }

    @Override
    public String toString() {
        return "BasicMergeCalculator{" +
                "propertyName='" + propertyName + '\'' +
                ", rootValue=" + rootValue +
                ", childValue=" + childValue +
                ", operator=" + operator +
                '}';
    }
}
