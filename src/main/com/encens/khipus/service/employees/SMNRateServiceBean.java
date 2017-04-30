package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.SMNRate;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("smnRateService")
@AutoCreate
public class SMNRateServiceBean extends GenericServiceBean implements SMNRateService {

    @In("#{entityManager}")
    private EntityManager em;

    public SMNRate findActive() throws EntryNotFoundException {
        try {
            return (SMNRate) em.createNamedQuery("SMNRate.findByActiveSMNRate")
                    .setParameter("active", true)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void create(Object entity) throws EntryDuplicatedException {
        SMNRate smnRate = (SMNRate) entity;

        SMNRate activeRate = null;
        try {
            activeRate = findActive();
            if (activeRate != null) {
                if (smnRate.getActive()) {
                    activeRate.setActive(false);
                    super.update(activeRate);
                }
            } else {
                smnRate.setActive(true);
            }
        } catch (EntryNotFoundException e) {
            e.printStackTrace();
        } catch (ConcurrencyException e) {
            e.printStackTrace();
        }

        super.create(entity);
    }

    @Override
    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException {
        SMNRate smnRate = (SMNRate) entity;
        if (smnRate.getActive()) {
            SMNRate activeRate = null;
            try {
                activeRate = findActive();
                if (activeRate != null && !activeRate.getId().equals(smnRate.getId())) {
                    activeRate.setActive(false);
                    super.update(activeRate);
                }
            } catch (EntryNotFoundException e) {
                e.printStackTrace();
            } catch (ConcurrencyException e) {
                e.printStackTrace();
            }
        }
        super.update(entity);
    }
}
