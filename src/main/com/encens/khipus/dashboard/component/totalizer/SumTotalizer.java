package com.encens.khipus.dashboard.component.totalizer;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.util.BigDecimalUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.7
 */
public class SumTotalizer<T extends DashboardObject> implements Totalizer<T> {
    private Map<String, Number> totals = new HashMap<String, Number>();

    private List<Field> fields = new ArrayList<Field>();

    public void calculate(T instance) {
        processFields(instance);

        for (Field field : fields) {
            Sum sumAnnotation = field.getAnnotation(Sum.class);

            Number actualValue = totals.get(sumAnnotation.fieldResultName());

            try {
                field.setAccessible(true);

                Object value = field.get(instance);
                if (null != value) {
                    if (value instanceof BigDecimal) {
                        if (null == actualValue) {
                            actualValue = (BigDecimal) value;
                        } else {
                            actualValue = BigDecimalUtil.sum((BigDecimal) actualValue, (BigDecimal) value);
                        }
                    }

                    if (value instanceof Long) {
                        if (null == actualValue) {
                            actualValue = (Number) value;
                        } else {
                            actualValue = (Long) actualValue + (Long) value;
                        }
                    }

                    if (value instanceof Integer) {
                        if (null == actualValue) {
                            actualValue = (Number) value;
                        } else {
                            actualValue = (Integer) actualValue + (Integer) value;
                        }
                    }

                    if (value instanceof Double) {
                        if (null == actualValue) {
                            actualValue = (Number) value;
                        } else {
                            actualValue = (Double) actualValue + (Double) value;
                        }
                    }

                    totals.put(sumAnnotation.fieldResultName(), actualValue);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("It isn't possible to get the value for @Sum field");
            }
        }

    }


    public void initialize() {
        totals.clear();
    }

    public Map<String, Number> getTotals() {
        return totals;
    }

    private void processFields(T instance) {
        if (fields.isEmpty()) {
            Class clazz = instance.getClass();

            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Sum.class)) {
                        fields.add(field);
                    }
                }
            }
        }
    }
}
