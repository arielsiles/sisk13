package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.KindOfSalary;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 09:07:35 PM
 */
@Stateless
@Name("kindOfSalaryService")
@AutoCreate
public class KindOfSalaryServiceBean implements KindOfSalaryService {

    @In("#{entityManager}")
    private EntityManager em;

    public KindOfSalary getKindOfSalaryById(Long id) {
        KindOfSalary result = null;
        try {
            result = (KindOfSalary) em.createNamedQuery("KindOfSalary.findKindOfSalary").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
        }
        return result;
    }

    public List<KindOfSalary> findBySector(Sector sector) {
        try {
            return em.createNamedQuery("KindOfSalary.findBySector").setParameter("sector", sector).getResultList();
        } catch (Exception e) {
            return new ArrayList<KindOfSalary>();
        }
    }
}