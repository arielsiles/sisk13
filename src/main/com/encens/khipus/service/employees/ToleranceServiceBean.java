package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Tolerance;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 08:52:24 PM
 */

@Stateless
@Name("toleranceService")
@AutoCreate
public class ToleranceServiceBean implements ToleranceService {
    @In("#{entityManager}")
    private EntityManager em;

    public Tolerance getTolerance(Long id) {
        Tolerance result = null;
        try {
            result = (Tolerance) em.createNamedQuery("Tolerance.findTolerance").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
        }
        return result;
    }
}
