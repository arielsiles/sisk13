package com.encens.khipus.framework.service;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/7/13
 * Time: 5:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataBaseExecutor {
    DataBaseCommand command;

    public DataBaseExecutor(DataBaseCommand command) {
        this.command = command;
    }

    public void create() throws EntryDuplicatedException {
        try {
            command.execute();
        } catch (PersistenceException e) { //TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            throw new EntryDuplicatedException();
        }
    }

    public void update() throws ConcurrencyException, EntryDuplicatedException {
        try {
            command.execute();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) { // TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            throw new EntryDuplicatedException(ee);
        }
    }

    public void delete() throws ConcurrencyException, ReferentialIntegrityException {
        try {
            command.execute();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (EntityNotFoundException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException(e);
        }
    }
}
