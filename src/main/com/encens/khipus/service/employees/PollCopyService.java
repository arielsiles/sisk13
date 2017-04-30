package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.employees.EvaluationCriteriaValue;
import com.encens.khipus.model.employees.PollCopy;
import com.encens.khipus.model.employees.PollForm;
import com.encens.khipus.model.employees.Question;
import com.encens.khipus.util.academic.AcademicStructure;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

/**
 * PollCopyService
 *
 * @author
 * @version 2.24
 */
@Local
public interface PollCopyService extends GenericService {
    void createPollCopy(PollCopy pollCopy,
                        Map<Question, EvaluationCriteriaValue> evaluationCriteriaValueResult) throws EntryDuplicatedException;

    void createFormAcademicStructure(PollForm pollForm,
                                     Person evaluator,
                                     Map<AcademicStructure, Map<Question, EvaluationCriteriaValue>> evaluationCriteriaValueResult,
                                     Text comment)
            throws EntryDuplicatedException;

    void createFormAcademicStructure(PollForm pollForm,
                                     Person evaluator,
                                     List<AcademicStructure> academicStructureList,
                                     Map<Question, EvaluationCriteriaValue> evaluationCriteriaValueResult,
                                     Text comment)
            throws EntryDuplicatedException;

    Boolean isEnabledToEvaluate(Person evaluator, PollForm pollForm);
}
