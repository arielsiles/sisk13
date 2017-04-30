package com.encens.khipus.service.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericService;

import javax.ejb.Local;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 2/11/13
 * Time: 18:45
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface ProductionOrderService{

    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException;

}
