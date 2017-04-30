package com.encens.khipus.framework.service;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import org.hibernate.Session;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Remove;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Provides generic CRUD operations for Entity instances.
 *
 * @author
 * @version 1.0
 */

@Stateless
@Name("genericService")
@AutoCreate
public class GenericServiceBean implements GenericService {


    @In(value = "#{entityManager}")
    private EntityManager em;

    @In(value = "#{listEntityManager}")
    private EntityManager eventEntityManager;

    @Logger
    protected Log log;

    @TransactionAttribute(REQUIRES_NEW)
    public void create(Object entity) throws EntryDuplicatedException {
        try {
            //this is gonna persist the entity if it is not managed and if for some reason the entity was already added
            // to the entity manager (duplicated exceptions), the next time it will persist the entity
            getEntityManager().persist(entity);
            getEntityManager().flush();
        } catch (PersistenceException e) { //TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            log.debug("Persistence error..", e);
            throw new EntryDuplicatedException();
        }
    }


    public <T> T findById(Class<T> clazz, Object id) throws EntryNotFoundException {
        return findById(clazz, id, false);
    }

    public <T> T findById(Class<T> clazz, Object id, Boolean refresh) throws EntryNotFoundException {
        T object = getEntityManager().find(clazz, id);
        if (object != null) {
            if (refresh) {
                getEntityManager().refresh(object);
            }
            return object;
        } else {
            throw new EntryNotFoundException("Entity(" + clazz.getName() + ") not found: " + id);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException {
        try {
            if (!getEntityManager().contains(entity)) {
                getEntityManager().merge(entity);
            }
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) { // TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            throw new EntryDuplicatedException(ee);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void delete(Object entity) throws ConcurrencyException, ReferentialIntegrityException {
        try {
            getEntityManager().remove(entity);
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (EntityNotFoundException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException(e);
        }

    }

    @Remove
    public void destroy() {
    }

    protected EntityManager getEntityManager() {
        return em;
    }

    protected EntityManager getEventEntityManager() {
        return eventEntityManager;
    }

    protected void detach(Object entity) {
        org.hibernate.Session session = (Session) getEntityManager().getDelegate();
        session.evict(entity);
    }
}
