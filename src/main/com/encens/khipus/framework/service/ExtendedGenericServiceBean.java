package com.encens.khipus.framework.service;


import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.production.ProductionPlanning;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

@Name("extendedGenericService")
@Stateless
@AutoCreate
public class ExtendedGenericServiceBean implements GenericService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Logger
    protected Log log;

    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public <T> T findById(Class<T> clazz, Object id) throws EntryNotFoundException {
        return findById(clazz, id, false);
    }

    @Override
    public <T> T findById(Class<T> clazz, Object id, Boolean refresh) throws EntryNotFoundException {
        T object = getEntityManager().find(clazz, id);
        if (object != null) {
            if (refresh) {
                getEntityManager().refresh(object);
            }
            return object;
        } else {
            throw new EntryNotFoundException("Entity not found: " + id);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(Object entity) throws EntryDuplicatedException {
        try {
            Object args = preCreate(entity);
            processCreate(entity);
            postCreate(entity, args);
            getEntityManager().flush();
        } catch (PersistenceException e) { //TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            log.debug("Persistence error..", e);
            log.info("PersistenceException caught");
            //log.error(e);
            throw new EntryDuplicatedException(e);
        }
    }

    protected void processCreate(Object entity) {
        if (entity instanceof  BaseModel && ((BaseModel)entity).getId() != null) {
            getEntityManager().merge(entity);
        } else {
            getEntityManager().persist(entity);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException {
        try {
            Object args = preUpdate(entity);
            processUpdate(entity);
            postUpdate(entity, args);
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            log.info("OptimisticException caught");
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) { // TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            log.info("PersistenceException caught");
            //log.error("exception", ee);
            throw new EntryDuplicatedException(ee);
        }
    }

    protected void processUpdate(Object entity) {
        if (getEntityManager().contains(entity) == false) {
            getEntityManager().merge(entity);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void delete(Object entity) throws ConcurrencyException, ReferentialIntegrityException {
        try {
            Object args = preDelete(entity);
            processDelete(entity);
            postDelete(entity, args);
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (EntityNotFoundException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException(e);
        }

    }

    @Override
    public void destroy() {
    }

    protected void processDelete(Object entity) {
        getEntityManager().remove(entity);
    }

    // template method pattern
    protected Object preCreate(Object entity) { return null; }
    protected void postCreate(Object entity, Object args) {}
    protected Object preUpdate(Object entity) { return null; }
    protected void postUpdate(Object entity, Object args) {}
    protected Object preDelete(Object entity) { return null; }
    protected void postDelete(Object entity, Object args) {}
}
