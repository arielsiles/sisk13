package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Charge;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

/**
 * ChargeServiceBean
 *
 * @author
 * @version 2.17
 */
@Name("chargeService")
@Stateless
@AutoCreate
public class ChargeServiceBean extends GenericServiceBean implements ChargeService {
    public boolean validateName(Charge charge) {
        Long countByName = (Long) getEntityManager().createNamedQuery("Charge.countByName")
                .setParameter("name", charge.getName())
                .getSingleResult();
        return countByName == null || countByName == 0;
    }
}
