package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.FinalEvaluationForm;
import com.encens.khipus.model.employees.FinalEvaluationFormType;

import javax.ejb.Local;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: FinalEvaluationFormService.java  16-jun-2010 12:36:36$
 */
@Local
public interface FinalEvaluationFormService extends GenericService {
    FinalEvaluationForm getFinalEvaluationFormByCycleAndType(Cycle cycle, FinalEvaluationFormType finalEvaluationFormType);

    Boolean isDuplicatedByCycleAndType(FinalEvaluationForm finalEvaluationForm);
}
