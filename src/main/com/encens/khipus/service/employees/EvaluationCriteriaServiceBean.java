package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.EvaluationCriteria;
import com.encens.khipus.model.employees.PollForm;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * EvaluationCriteriaServiceBean service bean
 *
 * @author
 * @version 1.0.18
 */
@Stateless
@Name("evaluationCriteriaService")
@AutoCreate
public class EvaluationCriteriaServiceBean implements EvaluationCriteriaService {
    @In("#{entityManager}")
    private EntityManager em;

    public List<EvaluationCriteria> getDistinctsOnQuestionsByPollForm(PollForm pollForm) {
        if (pollForm != null) {
            try {
                return em.createNamedQuery("EvaluationCriteria.findDistinctsOnQuestionsByPollForm").setParameter("pollForm", pollForm).getResultList();
            } catch (Exception e) {
            }
        }
        return new ArrayList<EvaluationCriteria>();
    }
}
