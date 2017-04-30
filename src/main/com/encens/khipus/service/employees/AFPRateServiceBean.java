package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.AFPRate;
import com.encens.khipus.model.employees.AFPRateType;
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
@Name("afpRateService")
@AutoCreate
public class AFPRateServiceBean extends GenericServiceBean implements AFPRateService {

    @In("#{entityManager}")
    private EntityManager em;

    public AFPRate findActive(AFPRateType afpRateType) throws EntryNotFoundException {
        try {
            return (AFPRate) em.createNamedQuery("AFPRate.findByActiveAFPRate")
                    .setParameter("active", true)
                    .setParameter("afpRateType", afpRateType)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void create(Object entity) throws EntryDuplicatedException {
        AFPRate afpRate = (AFPRate) entity;

        AFPRate activeRate;
        try {
            activeRate = findActive(afpRate.getAfpRateType());
            if (activeRate != null) {
                if (afpRate.getActive()) {
                    activeRate.setActive(false);
                    super.update(activeRate);
                }
            } else {
                afpRate.setActive(true);
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
        AFPRate afpRate = (AFPRate) entity;
        if (afpRate.getActive()) {
            AFPRate activeRate;
            try {
                activeRate = findActive(afpRate.getAfpRateType());
                if (activeRate != null && !activeRate.getId().equals(afpRate.getId())) {
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
