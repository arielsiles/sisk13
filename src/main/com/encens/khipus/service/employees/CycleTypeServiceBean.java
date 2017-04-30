package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.CycleType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * User: Ariel
 * Date: 24-06-2010
 * Time: 01:02:47 PM
 */
@Stateless
@Name("cycleTypeService")
@AutoCreate
public class CycleTypeServiceBean implements CycleTypeService {

    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public CycleType getCycleType(Integer period) {
        CycleType cycleType = null;
        try {
            cycleType = (CycleType) em.createNamedQuery("CycleType.findByPeriod").
                    setParameter("period", period).
                    getSingleResult();

        } catch (Exception e) {
            log.debug("Not found CycleType...");
        }
        return cycleType;
    }
}
