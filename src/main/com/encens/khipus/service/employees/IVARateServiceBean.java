package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.IVARate;
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
@Name("ivaRateService")
@AutoCreate
public class IVARateServiceBean extends GenericServiceBean implements IVARateService {

    @In("#{entityManager}")
    private EntityManager em;

    public IVARate findActive() throws EntryNotFoundException {
        try {
            return (IVARate) em.createNamedQuery("IVARate.findByActiveIVARATE")
                    .setParameter("active", true)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void create(Object entity) throws EntryDuplicatedException {
        IVARate ivaRate = (IVARate) entity;

        IVARate activeRate = null;
        try {
            activeRate = findActive();
            if (activeRate != null) {
                if (ivaRate.getActive()) {
                    activeRate.setActive(false);
                    super.update(activeRate);
                }
            } else {
                ivaRate.setActive(true);
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
        IVARate ivaRate = (IVARate) entity;
        if (ivaRate.getActive()) {
            IVARate activeRate = null;
            try {
                activeRate = findActive();
                if (activeRate != null && !activeRate.getId().equals(ivaRate.getId())) {
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
