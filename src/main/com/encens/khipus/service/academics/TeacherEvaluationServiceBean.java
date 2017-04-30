package com.encens.khipus.service.academics;

import com.encens.khipus.model.employees.TeacherEvaluation;
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
 * Date: 22-06-2010
 * Time: 06:55:58 PM
 */

@Stateless
@Name("teacherEvaluationService")
@AutoCreate
public class TeacherEvaluationServiceBean implements TeacherEvaluationService {
    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public TeacherEvaluation getTeacherEvaluation(Long employeeCode, Integer period, Integer gestion) {
        log.debug("Executing getTeacherEvaluation.............");

        TeacherEvaluation teacherEvaluation = null;
        try {
            Query query = em.createNamedQuery("TeacherEvaluation.findTeacherEvaluation");
            query.setParameter("employeeCode", employeeCode);
            query.setParameter("period", period);
            query.setParameter("gestion", gestion);

            teacherEvaluation = (TeacherEvaluation) query.getSingleResult();
        } catch (Exception e) {
            log.debug("Not found view value with...." + employeeCode + "-" + period + "-" + gestion + "-" + e);
        }

        return teacherEvaluation;
    }
}
