package com.encens.khipus.util;

import com.encens.khipus.model.admin.BusinessUnit;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Store the field names that are related with <code>com.encens.khipus.model.admin.BusinessUnit</code> and are annotated by
 * <code>@com.encens.khipus.validator.BusinessUnit.class</code>.
 * <p/>
 * All fields are stored into <code>Map</code> object when the key its the class name and the values are
 * a <code>Set</code> of <code>String</code> objects it contains the field names.
 *
 * @author
 * @version 2.21
 */
public class BusinessUnitFieldStore {
    private Map<String, Set<String>> cache;

    public static BusinessUnitFieldStore i = new BusinessUnitFieldStore();

    private BusinessUnitFieldStore() {
        cache = new HashMap<String, Set<String>>();
    }

    public List<BusinessUnit> getValuesToValidate(Object instance) {

        List<BusinessUnit> result = new ArrayList<BusinessUnit>();

        Class clazz = instance.getClass();

        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Set<String> fields = getBusinessUnitFields(clazz);
            if (null != fields) {
                for (String fieldName : fields) {
                    result.add(getValue(fieldName, instance, clazz));
                }
            }
        }

        return result;
    }

    private Set<String> getBusinessUnitFields(Class clazz) {
        if (null == cache.get(clazz.getName())) {
            putInCache(clazz);
        }

        return cache.get(clazz.getName());
    }

    private BusinessUnit getValue(String fieldName, Object instance, Class clazz) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (BusinessUnit) field.get(instance);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("The field " + fieldName + " not exists in " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("The field " + fieldName + " is inaccessible in " + clazz.getName(), e);
        }
    }

    private void putInCache(Class clazz) {
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Set<String> currentFields = getFields(clazz);
            if (!currentFields.isEmpty()) {
                cache.put(clazz.getName(), currentFields);
            }
        }
    }

    private Set<String> getFields(Class clazz) {
        Set<String> fields = new HashSet<String>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(com.encens.khipus.validator.BusinessUnit.class)) {
                fields.add(field.getName());
            }
        }

        return fields;
    }
}
