package com.encens.khipus.util;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Name("enumerationUtil")
@Scope(ScopeType.EVENT)
public class EnumerationUtil {

    public <T extends Enum<T>> T getEnumValue(String className, String value) {
        Class<T> clazz;

        try {
            clazz = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Illegal class name.", e);
        }

        return Enum.valueOf(clazz, value);
    }

    /* @param className the name of the class which represents the enumeration including its classPath 
    * @param values the values which are desired to retrieve
    * @return a list of Enum elements corresponding to the values if they are found*/

    public <T extends Enum<T>> List<T> getEnumValuesByName(String className, String... values) {
        Class<T> clazz;
        List<T> enumValues = new ArrayList<T>();
        try {
            clazz = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Illegal class name.", e);
        }
        for (String value : values) {
            enumValues.add(Enum.valueOf(clazz, value));
        }
        return enumValues;
    }

    public <T extends Enum> List<T> getEnumValues(String className) {
        List<T> enumValues;
        try {
            Class<T> enumClass = (Class<T>) Class.forName(className);
            enumValues = Arrays.asList((T[]) enumClass.getDeclaredMethod("values").invoke(null));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Illegal class name.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Illegal method name.", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Illegal method name.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access.", e);
        }
        return enumValues;
    }
}
