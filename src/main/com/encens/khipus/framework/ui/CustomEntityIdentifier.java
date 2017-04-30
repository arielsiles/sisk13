package com.encens.khipus.framework.ui;

import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.Component;
import org.jboss.seam.framework.EntityIdentifier;

import javax.faces.application.FacesMessage;
import javax.faces.convert.ConverterException;
import javax.persistence.EntityManager;

/**
 * @author
 * @version 2.9
 */
public class CustomEntityIdentifier extends EntityIdentifier {
    public CustomEntityIdentifier(Object entity, EntityManager entityManager) {
        super(entity, entityManager);
    }

    public CustomEntityIdentifier(Class clazz, Object id) {
        super(clazz, id);
    }

    @Override
    public Object find(EntityManager entityManager) {
        if (!existsObjectInDatabase()) {
            throw new ConverterException(new FacesMessage(MessageUtils.getMessage("Common.error.selected_NotFound")));
        }
        return super.find(entityManager);
    }

    @SuppressWarnings(value = "unchecked")
    private Boolean existsObjectInDatabase() {
        EntityManager eventEm = (EntityManager) Component.getInstance("listEntityManager");
        return null != eventEm.find(getClazz(), getId());
    }
}
