package com.encens.khipus.service.employees;

import com.encens.khipus.model.academics.AcademicCareerManagerPlanning;
import com.encens.khipus.model.academics.AcademicEmployeePlanning;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.24
 */

@Stateless
@Name("academicEmployeePlanningService")
@AutoCreate
public class AcademicEmployeePlanningServiceBean implements AcademicEmployeePlanningService {
    @In(value = "#{entityManager}")
    private EntityManager em;

    @SuppressWarnings(value = "unchecked")
    public List<AcademicEmployeePlanning> getPlanning(Integer employeeCode,
                                                      Integer gestion,
                                                      Integer period) {
        return em.createNamedQuery("AcademicEmployeePlanning.findByCodeGestiondAndPeriod")
                .setParameter("employeeCode", employeeCode)
                .setParameter("gestion", gestion)
                .setParameter("period", period)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<AcademicEmployeePlanning> getPlanningWithinSubject(Integer employeeCode,
                                                                   Integer gestion,
                                                                   Integer period) {
        return em.createNamedQuery("AcademicEmployeePlanning.findWithinSubjectByCodeAndGestionAndPeriod")
                .setParameter("employeeCode", employeeCode)
                .setParameter("gestion", gestion)
                .setParameter("period", period)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<AcademicEmployeePlanning> getPlanningByAcademicCareerManagerPlanning(List<AcademicCareerManagerPlanning> academicCareerManagerPlanningList) {

        List<AcademicEmployeePlanning> academicEmployeePlanningList = new ArrayList<AcademicEmployeePlanning>();

        for (AcademicCareerManagerPlanning academicCareerManagerPlanning : academicCareerManagerPlanningList) {
            academicEmployeePlanningList.addAll(
                    em.createNamedQuery("AcademicEmployeePlanning.findByCareerGestiondAndPeriod")
                            .setParameter("careerId", academicCareerManagerPlanning.getCareerId())
                            .setParameter("facultyId", academicCareerManagerPlanning.getFacultyId())
                            .setParameter("seatId", academicCareerManagerPlanning.getSeatId())
                            .setParameter("gestion", academicCareerManagerPlanning.getGestion())
                            .setParameter("period", academicCareerManagerPlanning.getPeriod())
                            .getResultList());
        }

        return academicEmployeePlanningList;
    }
}
