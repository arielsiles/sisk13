package com.encens.khipus.service.academics;

import com.encens.khipus.model.academics.Horary;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * User: Ariel
 * Date: 23-06-2010
 */
@Stateless
@Name("horaryService")
@AutoCreate
public class HoraryServiceBean implements HoraryService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public Horary getHoraryById(Long horaryId, Integer gestion, Integer period) {
        try {
            Query query = em.createNamedQuery("Horary.findById");
            query.setParameter("horaryId", horaryId);
            query.setParameter("gestion", gestion);
            query.setParameter("period", period);
            return (Horary) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            System.out.println("Error in query ");
            e.printStackTrace();
            return null;
        }
    }

    public void getHorarys() {

        //Query query = em.createNamedQuery("Horary.findById");
        //query.setParameter("horaryId", new Long(41610));

        //Horary h = (Horary) query.getSingleResult();
        //return (Horary) query.getSingleResult();
    }
}



