package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.CNSRate;
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
@Name("cnsRateService")
@AutoCreate
public class CNSRateServiceBean extends GenericServiceBean implements CNSRateService {

    @In("#{entityManager}")
    private EntityManager em;

    public CNSRate findActive() throws EntryNotFoundException {
        try {
            return (CNSRate) em.createNamedQuery("CNSRate.findByActiveCNSRate")
                    .setParameter("active", true)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void create(Object entity) throws EntryDuplicatedException {
        CNSRate cnsRate = (CNSRate) entity;

        CNSRate activeRate = null;
        try {
            activeRate = findActive();
            if (activeRate != null) {
                if (cnsRate.getActive()) {
                    activeRate.setActive(false);
                    super.update(activeRate);
                }
            } else {
                cnsRate.setActive(true);
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
        CNSRate cnsRate = (CNSRate) entity;
        if (cnsRate.getActive()) {
            CNSRate activeRate = null;
            try {
                activeRate = findActive();
                if (activeRate != null && !activeRate.getId().equals(cnsRate.getId())) {
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
