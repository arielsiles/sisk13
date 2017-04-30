package com.encens.khipus.model;

import com.encens.khipus.util.Constants;
import org.jboss.seam.util.Reflections;

import javax.persistence.EmbeddedId;
import javax.persistence.PrePersist;
import java.lang.reflect.Field;

/**
 * CompanyNumberListener
 *
 * @author
 * @version 2.0
 */
public class CompanyNumberListener {
    /**
     * This method is an entity callback method that is executed when the lister is applied to the entity
     * and whent the entity is just going to be persisted.
     *
     * @param entity the entity instance to set the current company number to
     */
    @PrePersist
    public void setCurrentCompany(Object entity) {
        setCompany(entity, Constants.defaultCompanyNumber);

    }

    /**
     * Check for a companyNumber field, if it's not found then it throws an exception cause a bad configuration of the
     * listener. Otherwise if the field is found, it reads it, and if it's null, it set with the current company number,
     * and if it's not null, then nothing is done.
     *
     * @param entity        the entity to set the current company number to
     * @param companyNumber the current company number
     */
    public void setCompany(Object entity, String companyNumber) {

        boolean fieldFound = false;
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(EmbeddedId.class)) {
                try {
                    field.setAccessible(true);
                    Object embeddablePK = Reflections.get(field, entity);
                    if (embeddablePK == null) {
                        throw new IllegalArgumentException("The attribute " + field.getName() + "[" + field.getClass().getName() + "] cannot be null");
                    }
                    for (Field embeddablePKField : embeddablePK.getClass().getDeclaredFields()) {
                        if (Constants.defaultCompanyNumberAttributeName.equals(embeddablePKField.getName())) {
                            fieldFound = true;
                            setPKValue(embeddablePKField, embeddablePK, companyNumber);
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Error trying to read/update the companyNumber field of the entity: " + entity, e);
                }
                break;
            } else if (Constants.defaultCompanyNumberAttributeName.equals(field.getName())) {
                fieldFound = true;
                setPKValue(field, entity, companyNumber);
            }
        }
        if (!fieldFound) {
            throw new IllegalArgumentException("You have added the CompanyNumberListener to an entity that " +
                    "does not have a mapped companyNumber attribute");
        }
    }

    private void setPKValue(Field field, Object entity, Object value) {
        try {
            field.setAccessible(true);
            if (Reflections.get(field, entity) == null) {
                Reflections.set(field, entity, value);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error trying to read/update the companyNumber field of the entity: " + entity, e);
        }
    }

}
