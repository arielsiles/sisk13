package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Gestion;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * User: Ariel
 * Date: 24-06-2010
 * Time: 12:57:28 PM
 */

@Stateless
@Name("gestionService")
@AutoCreate
public class GestionServiceBean implements GestionService {

    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public Gestion getGestion(Integer year) {
        Gestion gestion = null;
        try {
            gestion = (Gestion) em.createNamedQuery("Gestion.findByYear").
                    setParameter("year", year).
                    getSingleResult();

        } catch (Exception e) {
            log.debug("Not found Gestion...");
        }
        return gestion;
    }

    public Gestion getLastGestion() {
        Gestion gestion = null;
        try {
           List<Gestion> gestions = (List<Gestion>) em.createNamedQuery("Gestion.findLast").
                    getResultList();
            gestion = gestions.get(0);
        } catch (Exception e) {
            log.debug("Not found Gestion...");
        }
        return gestion;
    }
}
