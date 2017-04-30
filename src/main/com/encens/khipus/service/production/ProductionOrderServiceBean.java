package com.encens.khipus.service.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Created by Diego on 05/08/2014.
 */
@Name("productionOrderService")
@Stateless
@AutoCreate
public class ProductionOrderServiceBean extends GenericServiceBean implements ProductionOrderService  {
    @In(value = "#{entityManager}")
    private EntityManager em;

    @TransactionAttribute(REQUIRES_NEW)
    @Override
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
}
