package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Limit;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 09:07:35 PM
 */
@Stateless
@Name("limitService")
@AutoCreate
public class LimitServiceBean implements LimitService {

    @In("#{entityManager}")
    private EntityManager em;

    public Limit getLimit(Long id) {
        Limit result = null;
        try {
            result = (Limit) em.createNamedQuery("Limit.findLimit").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
        }
        return result;
    }
}
