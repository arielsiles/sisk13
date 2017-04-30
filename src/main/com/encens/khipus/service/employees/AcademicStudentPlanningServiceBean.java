package com.encens.khipus.service.employees;

import com.encens.khipus.model.academics.AcademicStudentPlanning;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * AcademicStructureServiceBean
 *
 * @author
 * @version 2.24
 */
@Name("academicStudentPlanningService")
@Stateless
@AutoCreate
public class AcademicStudentPlanningServiceBean implements AcademicStudentPlanningService {
    @In(value = "#{entityManager}")
    private EntityManager em;

    @Logger
    protected Log log;

    public List<AcademicStudentPlanning> getPlanning(Integer studentCode, Integer gestion, Integer period) {
        return em.createNamedQuery("AcademicStudentPlanning.findByStudentCodeAndGestionAndPeriod")
                .setParameter("studentCode", studentCode)
                .setParameter("gestion", gestion)
                .setParameter("period", period).getResultList();
    }
}
