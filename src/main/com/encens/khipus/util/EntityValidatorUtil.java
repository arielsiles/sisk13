package com.encens.khipus.util;

import org.jboss.seam.util.Reflections;

import java.lang.reflect.Field;

/**
 * This class is an util to support entity validations
 *
 * @author
 * @version 2.26
 */
public final class EntityValidatorUtil {
    private EntityValidatorUtil() {
    }
    /* This function verifies if the field's value is not null
* @param o The object from which contains the field
* @param fieldName the field of the object o to verify whether it is null
* @return true if the value of the field is not null
* @return false if the value of the field is null or the field have not been found*/

    public static boolean isNotNull(Object o, String fieldName) {
        Field field = Reflections.getField(o.getClass(), fieldName);
        field.setAccessible(true);
        Object result = null;
        try {
            result = Reflections.get(field, o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null != result;
    }

}
