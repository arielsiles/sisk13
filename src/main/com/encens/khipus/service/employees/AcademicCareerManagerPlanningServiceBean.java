package com.encens.khipus.service.employees;

import com.encens.khipus.model.academics.AcademicCareerManagerPlanning;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * AcademicCareerManagerPlanningServiceBean
 *
 * @author
 * @version 2.24
 */
@Name("academicCareerManagerPlanningService")
@Stateless
@AutoCreate
public class AcademicCareerManagerPlanningServiceBean implements AcademicCareerManagerPlanningService {
    @In(value = "#{entityManager}")
    private EntityManager em;


    @SuppressWarnings(value = "unchecked")
    public List<AcademicCareerManagerPlanning> getAcademicCareerManagerPlanning(
            String careerId,
            Integer facultyId,
            Integer seatId,
            Integer gestion,
            Integer period) {
        return em.createNamedQuery("AcademicCareerManagerPlanning.findByCareerGestiondAndPeriod")
                .setParameter("careerId", careerId)
                .setParameter("facultyId", facultyId)
                .setParameter("seatId", seatId)
                .setParameter("gestion", gestion)
                .setParameter("period", period).getResultList();
    }
    
    
    public List<AcademicCareerManagerPlanning> getPlanning(Integer employeeCode,
                                                           Integer gestion,
                                                           Integer period) {
        return em.createNamedQuery("AcademicCareerManagerPlanning.findByCodeGestiondAndPeriod")
                .setParameter("employeeCode", employeeCode)
                .setParameter("gestion", gestion)
                .setParameter("period", period)
                .getResultList();
    }
}
