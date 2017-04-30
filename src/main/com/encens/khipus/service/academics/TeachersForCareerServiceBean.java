package com.encens.khipus.service.academics;

import com.encens.khipus.model.employees.TeachersForCareer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * User: Ariel
 * Date: 25-06-2010
 * Time: 04:42:24 PM
 */

@Stateless
@Name("teachersForCareerService")
@AutoCreate
public class TeachersForCareerServiceBean implements TeachersForCareerService {
    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public TeachersForCareer getCareer(Integer administrativeAcademicUnit, String studyPlan, Integer period, Integer gestion) {
        TeachersForCareer teachersForCareer = null;

        try {
            Query query = em.createNamedQuery("TeachersForCarrer.findCareer");
            query.setParameter("administrativeAcademicUnit", administrativeAcademicUnit);
            query.setParameter("studyPlan", studyPlan);
            query.setParameter("period", period);
            query.setParameter("gestion", gestion);

            teachersForCareer = (TeachersForCareer) query.getSingleResult();
        } catch (Exception e) {
            log.debug("Not found data in view table...." + e);
        }

        return teachersForCareer;
    }
}
