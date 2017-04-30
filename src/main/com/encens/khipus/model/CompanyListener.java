package com.encens.khipus.model;

import com.encens.khipus.model.admin.Company;
import org.jboss.seam.Component;
import org.jboss.seam.util.Reflections;

import javax.persistence.PrePersist;
import java.lang.reflect.Field;

/**
 * This JPA Listener sets the current company into the entity if it is not defined jus before trying to save the,
 * entity.
 *
 * @author
 */

public class CompanyListener {


    /**
     * This method is an entity callback method that is executed when the lister is applied to the entity
     * and whent the entity is just going to be persisted.
     *
     * @param entity the entity instance to set the current company to
     */
    @PrePersist
    public void setCurrentCompany(Object entity) {
        setCompany(entity, (Company) Component.getInstance("currentCompany"));

    }

    /**
     * Check for a company field, if it's not found then it throws an exception cause a bad configuration of the
     * listener. Otherwise if the field is found, it reads it, and if it's null, it set with the currentCompany, and
     * if it's not null, then nothing is done.
     *
     * @param entity         the entity to set the current company to
     * @param currentCompany the current logged company
     */
    public void setCompany(Object entity, Company currentCompany) {
        boolean fieldFound = false;
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.getType().equals(Company.class)) {
                fieldFound = true;
                try {
                    field.setAccessible(true);
                    if (Reflections.get(field, entity) == null) {
                        if (currentCompany != null) {
                            Reflections.set(field, entity, currentCompany);
                        } else {
                            throw new IllegalArgumentException("The company instance that is trying to be set to the entity is  NULL");
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Error trying to read/update the company field of the entity: " + entity, e);
                }
                break;
            }
        }
        if (!fieldFound) {
            throw new IllegalArgumentException("You have added the CompanyListener to an entity that " +
                    "does not have a mapped company attribute");
        }

    }


}
