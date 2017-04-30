package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Bonus;
import com.encens.khipus.model.employees.BonusType;
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
@Name("bonusService")
@AutoCreate
public class BonusServiceBean extends GenericServiceBean implements BonusService {

    @In("#{entityManager}")
    private EntityManager em;


    public Bonus load(Bonus bonus) throws EntryNotFoundException {
        Bonus result = (Bonus) em.createNamedQuery("Bonus.load")
                .setParameter("id", bonus.getId())
                .getSingleResult();
        if (result == null) {
            throw new EntryNotFoundException("Bonus not found: " + bonus.getId());
        }
        return result;
    }

    public Bonus findActive(BonusType bonusType) throws EntryNotFoundException {
        try {
            return (Bonus) em.createNamedQuery("Bonus.findByActiveAndBonusType")
                    .setParameter("active", true)
                    .setParameter("bonusType", bonusType)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void create(Object entity) throws EntryDuplicatedException {
        Bonus bonus = (Bonus) entity;

        if (bonus.getBonusType() == BonusType.SENIORITY_BONUS) {
            Bonus activeBonus;
            try {
                activeBonus = findActive(BonusType.SENIORITY_BONUS);
                if (activeBonus != null) {
                    if (bonus.getActive()) {
                        activeBonus.setActive(false);
                        super.update(activeBonus);
                    }
                } else {
                    bonus.setActive(true);
                }
            } catch (EntryNotFoundException e) {
                e.printStackTrace();
            } catch (ConcurrencyException e) {
                e.printStackTrace();
            }
        }

        super.create(entity);
    }

    @Override
    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException {
        Bonus bonus = (Bonus) entity;

        if (bonus.getBonusType() == BonusType.SENIORITY_BONUS && bonus.getActive()) {
            Bonus activeBonus;
            try {
                activeBonus = findActive(BonusType.SENIORITY_BONUS);
                if (activeBonus != null && !activeBonus.getId().equals(bonus.getId())) {
                    activeBonus.setActive(false);
                    super.update(activeBonus);
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
