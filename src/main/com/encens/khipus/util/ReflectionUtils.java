package com.encens.khipus.util;

import org.jboss.seam.util.Reflections;

import java.lang.reflect.Field;

/**
 * ReflectionUtils
 *
 * @author
 * @version 1.2.3
 */
public class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static void set(Object sourceObject, String objectFieldName, String propertyName, String propertyValue) throws Exception {
        Field objectField = getField(sourceObject.getClass(), objectFieldName);
        Object targetValue = Reflections.get(objectField, sourceObject);
        if (targetValue == null) {
            targetValue = objectField.getType().newInstance();
        }
        Reflections.set(getField(targetValue.getClass(), propertyName), targetValue, propertyValue);
        Reflections.set(objectField, sourceObject, targetValue);
    }

    public static Object get(Object sourceObject, String objectFieldName, String propertyName) throws Exception {
        Field objectField = getField(sourceObject.getClass(), objectFieldName);
        Object targetValue = Reflections.get(objectField, sourceObject);
        if (targetValue != null) {
            return Reflections.get(getField(targetValue.getClass(), propertyName), targetValue);
        }
        return null;
    }

    public static void set(Object sourceObject, String propertyName, Object propertyValue) throws Exception {
        Field objectField = getField(sourceObject.getClass(), propertyName);
        Reflections.set(objectField, sourceObject, propertyValue);
    }

    public static Object get(Object sourceObject, String propertyName) throws Exception {
        Field objectField = getField(sourceObject.getClass(), propertyName);
        return Reflections.get(objectField, sourceObject);
    }

    public static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
        Field resultField = Reflections.getField(clazz, fieldName);
        resultField.setAccessible(true);
        return resultField;
    }
}
