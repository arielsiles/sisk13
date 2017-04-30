package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.totalizer.Totalizer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.7
 */
public class DebtExtendedTotalizer implements Totalizer<DebtExtended> {
    private Map<String, DebtExtendedAttribute> totals = new HashMap<String, DebtExtendedAttribute>();

    private List<Field> fields = new ArrayList<Field>();

    public void calculate(DebtExtended instance) {
        if (null != instance.getCurrentCareer()) {
            processFields(instance.getCurrentCareer());
            for (Field field : fields) {
                DebtExtendedAttribute currentValue = totals.get(field.getName());

                if (null == currentValue) {
                    currentValue = new DebtExtendedAttribute();
                }

                try {
                    field.setAccessible(true);

                    Object value = field.get(instance.getCurrentCareer());
                    if (null != value) {
                        currentValue.addValues((DebtExtendedAttribute) value);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("It isn't possible to get the value for " + field.getName());
                }

                totals.put(field.getName(), currentValue);
            }
        }
    }

    public void initialize() {
        totals.clear();
    }

    public Map<String, DebtExtendedAttribute> getTotals() {
        return totals;
    }

    private void processFields(Career instance) {
        if (fields.isEmpty()) {
            Class clazz = instance.getClass();

            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getType().equals(DebtExtendedAttribute.class)) {
                        fields.add(field);
                    }
                }
            }
        }
    }
}
