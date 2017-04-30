package com.encens.khipus.model;

import com.encens.khipus.util.FieldsStore;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.persistence.EmbeddedId;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * This JPA listener analyses an entity looking for String fields and changes them to uppercase.
 *
 * @author
 * @version 2.17
 */
public class UpperCaseStringListener {

    protected static final LogProvider log = Logging.getLogProvider(UpperCaseStringListener.class);

    /**
     * This method is an entity callback method that is executed when the entity is just going to be persisted
     *
     * @param entity the entity that is going to be persisted or updated
     * @throws NoSuchFieldException     signals that the class doesn't have a field of a specified name
     * @throws IllegalArgumentException indicates that a method has been passed an illegal or inappropriate argument
     * @throws IllegalAccessException   indicates that the class does not have access to the definition of a field
     */
    @PrePersist
    public void setUppercaseOnPrePersist(Object entity) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = entity.getClass();

        if (!FieldsStore.getInstance().existsClass(clazz.getName())) {
            analyzeClass(clazz);
        }

        do {
            Set<String> fieldList = FieldsStore.getInstance().getFields(clazz.getName());
            for (String fieldName : fieldList) {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                if (field.getType() == String.class) {
                    setUppercase(entity, field);
                } else if (field.isAnnotationPresent(EmbeddedId.class)) {
                    setUppercaseOnPrePersist(field.get(entity));
                }

            }
            clazz = clazz.getSuperclass();
        }

        while (clazz != null);
    }

    /**
     * This method is an entity callback method that is executed when the entity is just going to be updated.
     *
     * @param entity the entity that is going to be persisted or updated
     * @throws NoSuchFieldException     signals that the class doesn't have a field of a specified name
     * @throws IllegalArgumentException indicates that a method has been passed an illegal or inappropriate argument
     * @throws IllegalAccessException   indicates that the class does not have access to the definition of a field
     */
    @PreUpdate
    public void setUppercaseOnPreUpdate(Object entity) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = entity.getClass();

        if (!FieldsStore.getInstance().existsClass(clazz.getName())) {
            analyzeClass(clazz);
        }

        do {
            Set<String> fieldList = FieldsStore.getInstance().getFields(clazz.getName());
            for (String fieldName : fieldList) {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                if (field.getType() == String.class) {
                    setUppercase(entity, field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        while (clazz != null);
    }

    /**
     * Changes the value o an object field to upper case
     *
     * @param entity the entity instance that has the field
     * @param field  the field to be updated
     */
    private void setUppercase(Object entity, Field field) {
        try {
            log.debug("Getting the value of the field " + field.getName());

            String value = (String) field.get(entity);
            if (value != null) {
                value = value.toUpperCase();
                field.set(entity, value);
                log.debug("Changing field " + field.getName() + " to Upper case");
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("The field " + field.getName() + " is inaccessible");
        }
    }

    /**
     * Analyses a class using reflection and saves its information (class name, string fields, embedded id fields)
     * in the {@link com.encens.khipus.util.FieldsStore}
     *
     * @param clazz the class to be analyzed
     */
    private void analyzeClass(Class clazz) {
        FieldsStore.getInstance().addClass(clazz.getName());

        log.debug("Analzing String fields for an object of type " + clazz.getName());
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == String.class) {
                FieldsStore.getInstance().addField(clazz.getName(), field.getName());
                log.debug("Adding field " + field.getName() + " to class " + clazz.getName());
            } else if (field.isAnnotationPresent(EmbeddedId.class)) {
                FieldsStore.getInstance().addField(clazz.getName(), field.getName());
                log.debug("Adding embedded field " + field.getName() + " to class " + clazz.getName());

                Class embeddedFieldClass = field.getType();
                if (!FieldsStore.getInstance().existsClass(embeddedFieldClass.getName())) {
                    analyzeClass(embeddedFieldClass);
                }
            }
        }

        /* Recursive call when there is a parent class */
        Class superClass = clazz.getSuperclass();
        if (superClass != null && !FieldsStore.getInstance().existsClass(superClass.getName())) {
            analyzeClass(superClass);
        }
    }
}
