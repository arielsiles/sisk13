package com.encens.khipus.framework.service;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Territoriotrabajo;
import com.encens.khipus.model.production.RawMaterialProducer;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Base entity operations service
 *
 * @author
 * @version 1.0
 */
@Local
public interface GenericService {
    void create(Object entity) throws EntryDuplicatedException;

    <T> T findById(Class<T> clazz, Object id) throws EntryNotFoundException;

    <T> T findById(Class<T> clazz, Object id, Boolean refresh) throws EntryNotFoundException;

    void update(Object entity) throws EntryDuplicatedException, ConcurrencyException;

    void delete(Object entity) throws ConcurrencyException, ReferentialIntegrityException;

    void destroy();


}
