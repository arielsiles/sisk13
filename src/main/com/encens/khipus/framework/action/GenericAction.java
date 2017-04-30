package com.encens.khipus.framework.action;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericService;
import org.jboss.seam.annotations.*;
import org.jboss.seam.core.Manager;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Reflections;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * This is a basic action that allows you to make CRUD over entities using a SFSB facade.
 * All this stuff must be used in a CONVERSATION context only.
 * It currently works with non compound @Id entities
 *
 * @author
 * @version 1.0
 */

@Name("genericAction")
public class GenericAction<T> implements Serializable {
    public static final String OP_CREATE = "create";
    public static final String OP_UPDATE = "update";

    private Class<T> entityClass;

    private T instance;

    private String displayNameProperty = null;

    @In
    protected GenericService genericService;

    @In
    protected FacesMessages facesMessages;

    @In
    protected Map<String, String> messages;

    @Logger
    protected Log log;

    private String op;


    private static final long serialVersionUID = 3905512085984947065L;

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(T instance) {
        try {
            setOp(OP_UPDATE);
            //define the unmanaged instance as current instance
            this.instance = instance;
            //Ensure the instance exists in the database, find it
            setInstance(getService().findById(getEntityClass(), getId(instance)));
            return Outcome.SUCCESS;

        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }


    @End
    public String create() {
        try {
            getService().create(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    public void createAndNew() {
        try {
            getService().create(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }


    @End
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            getService().update(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @End
    public String delete() {
        try {
            getService().delete(getInstance());
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
        }

        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    public String cancel() {
        return Outcome.CANCEL;
    }

    public T getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    public T createInstance() {
        if (getEntityClass() != null) {
            try {
                instance = entityClass.newInstance();
                return instance;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {

            return null;
        }
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    public String getOp() {
        if (op != null) {
            return op;
        } else {
            return OP_CREATE; //by default set the create operation
        }
    }

    public void setOp(String op) {
        this.op = op;
    }

    public boolean isManaged() {
        return OP_UPDATE.equals(getOp());
    }

    /**
     * The refreshInstance method refresh the state of the instance from the database,
     * overwriting changes made to the entity, if any.
     */
    public void refreshInstance() {
        try {
            //Ensure the instance exists in the database, find it
            setInstance(getService().findById(getEntityClass(), getId(instance), true));
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
        }
    }

    /**
     * Recover the ID of the entity instance
     *
     * @param instance the entity instance
     * @return the object representing the identity of the entity
     */
    protected Object getId(Object instance) {
        Object id = null;
        Class clazz = instance.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    try {
                        field.setAccessible(true);
                        id = field.get(instance);
                        break;
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("It isn't possible to get the value for @Id field");
                    }
                } else if (field.isAnnotationPresent(EmbeddedId.class)) {
                    try {
                        field.setAccessible(true);
                        id = field.get(instance);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("It isn't possible to get the value for @EmbeddedId field");
                    }

                }
            }
        }
        return id;
    }


    /**
     * When the entity is being to be updated it performs a version + 1 update in the version field,
     * but, if some exception is thrown by the database (like unique constraint exceptions),
     * then the version remains as version + 1, but it supposse it was rolled backed,
     * so the version number must be restored to version only.
     * This happens with Conversation Scope.
     * TODO: report another EJB3 bug (if it's)
     *
     * @param instance the object instance
     * @return the version value, null if it has not @Version
     */
    protected Object getVersion(Object instance) {
        Object version = null;
        Class clazz = instance.getClass();
        if (clazz.isAnnotationPresent(Entity.class)) {
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Version.class)) {
                        try {
                            field.setAccessible(true);
                            version = field.get(instance);
                        } catch (IllegalAccessException e) {
                            throw new IllegalArgumentException("The object instance must have an @Version field");
                        }
                        break;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("The object instance must be an annotated @Entity class");
        }
        return version;
    }

    /**
     * Defines the current version to an Entity
     *
     * @param instance the entity instance
     * @param value    the version value
     */
    protected void setVersion(Object instance, Long value) {
        Class clazz = instance.getClass();
        if (value != null && clazz.isAnnotationPresent(Entity.class)) {
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Version.class)) {
                        try {
                            field.setAccessible(true);
                            field.set(instance, value);
                        } catch (IllegalAccessException e) {
                            throw new IllegalArgumentException("The object instance must have an @Version field");
                        }
                        break;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("The object instance must be an annotated @Entity class");
        }

    }

    public Class<T> getEntityClass() {
        if (entityClass == null) {
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                entityClass = (Class<T>) paramType.getActualTypeArguments()[0];
            } else {
                throw new IllegalArgumentException("Could not guess entity class by reflection");
            }
        }
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Object getDisplayPropertyValue() {
        Object entity = getInstance();
        if (entity != null && getDisplayNameProperty() != null) {
            Method entityDisplayPropertyGetter = Reflections.getGetterMethod(entity.getClass(), getDisplayNameProperty());
            try {
                Object value = Reflections.invoke(entityDisplayPropertyGetter, entity);
                if (value != null) {
                    return value;
                } else {
                    return getDisplayNameMessage();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error trying to recover the value of the entity for displayNameProperty");
            }
        } else {
            return getDisplayNameMessage();
        }
    }

    public void closeConversation(String outcome) {
        if (null != outcome) {
            log.debug("I closed the nested conversation manually!!!");
            Manager.instance().endConversation(true);
        }
    }

    public void entryNotFoundLog() {
        log.debug("entity was removed by another user");
    }

    public void concurrencyLog() {
        log.debug("Concurrency exception caught, entity was updated by another user");
    }

    public void unexpectedErrorLog(Exception e) {
        log.error("An unexpected error have happened...", e);
    }

    public void entryNotFoundErrorLog(Exception e) {
        log.error("entity was removed by another user...", e);
    }

    public void referentialIntegrityLog() {
        log.debug("entity cannot be deleted because is being referenced");
    }

    protected String getDisplayNameMessage() {
        return messages.get("Common.info.item");
    }

    protected void addNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.error.notFound", getDisplayPropertyValue());
    }

    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.created", getDisplayPropertyValue());
    }

    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.updated", getDisplayPropertyValue());
    }

    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.deleted", getDisplayPropertyValue());
    }

    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Common.message.duplicated", getDisplayPropertyValue());
    }

    protected void addDuplicatedFieldMessage(String fieldResourceKey, Object fieldValue) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Common.message.duplicatedField", messages.get(fieldResourceKey), fieldValue);
    }

    protected void addUpdateConcurrencyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.error.concurrency");
    }

    protected void addDeleteConcurrencyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.message.concurrency.delete", getDisplayPropertyValue());
    }

    protected void addDeleteReferentialIntegrityMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.message.referentialIntegrity.delete", getDisplayPropertyValue());
    }

    protected void addCompanyConfigurationNotFoundErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "CompanyConfiguration.notFound");
    }

    protected void addSuccessOperationMessage(String operationMessage) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.successOperation", operationMessage);
    }

    protected String getDisplayNameProperty() {
        return displayNameProperty;
    }

    protected GenericService getService() {
        return genericService;
    }
}
