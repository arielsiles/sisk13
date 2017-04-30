package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.EvaluationCriteria;
import com.encens.khipus.model.employees.PollForm;

import javax.ejb.Local;
import java.util.List;

/**
 * EvaluationCriteriaService bussines interface
 *
 * @author
 * @version 1.0.18
 */
@Local
public interface EvaluationCriteriaService {
    List<EvaluationCriteria> getDistinctsOnQuestionsByPollForm(PollForm pollForm);
}
