package com.encens.khipus.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Singleton class to store reflected class information.
 * It contains a map of classes and for each a set of fields.
 *
 * @author
 * @version 2.17
 */
public class FieldsStore {

    private static FieldsStore INSTANCE = null;
    private Map<String, Set<String>> reflectedClassMap;

    /**
     * Private constructor
     */
    private FieldsStore() {
        reflectedClassMap = new HashMap<String, Set<String>>();
    }

    /**
     * Synchronized creator to avoid multi-thread issues
     */
    private static synchronized void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FieldsStore();
        }
    }

    /**
     * Returns the singleton instance
     *
     * @return the singleton instance
     */
    public static FieldsStore getInstance() {
        if (INSTANCE == null) {
            createInstance();
        }
        return INSTANCE;
    }

    /**
     * Checks whether a class is already registered
     *
     * @param className class name to be checked
     * @return true if exists, otherwise false
     */
    public boolean existsClass(String className) {
        return reflectedClassMap.containsKey(className);
    }

    /**
     * Registers a new class
     *
     * @param className class name to be registered
     */
    public void addClass(String className) {
        if (!existsClass(className)) {
            reflectedClassMap.put(className, new HashSet<String>());
        }
    }

    /**
     * Registers a field for a class
     *
     * @param className class name
     * @param fieldName field name to be registered to the class
     */
    public void addField(String className, String fieldName) {
        if (!existsClass(className)) {
            addClass(className);
        }

        getFields(className).add(fieldName);
    }

    /**
     * Returns a collection of fields given a class name
     *
     * @param className
     * @return if the class is registered it return the collection of fields, otherwise null
     */
    public Set<String> getFields(String className) {
        if (existsClass(className)) {
            return reflectedClassMap.get(className);
        } else {
            return null;
        }
    }
}
