package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.*;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * PollFormService
 *
 * @author
 * @version 1.1.6
 */
@Local
public interface PollFormService {
    Long calculateCareerTotal(Long personId, Long pollFormId, PollFormGrouppingType pollFormGrouppingType);

    Long calculateSubjectTotal(Long personId, Long pollFormId);

    Long calculateCopyTotal(Long personId, Long pollFormId);

    Long calculatePollCopyByFacultyCareer(Long personId, Long pollFormId, Long facultyId, Long careerId);

    BigDecimal calculateEvaluationValue(Long personId, Long pollFormId, Long sectionId);

    Long countAssertQuestionEvaluationCriteriaValueByPerson(Long personId, Long pollFormId, Long questionId, Long evaluationCriteriaValueId, Long facultyId, Long careerId);

    List<Location> getLocationList();

    List<Faculty> getFacultyList(Location location);

    List<Career> getCareerList(Faculty faculty);

    List<Subject> getSubjectList(Career career);

    String readQuestionContent(Long questionId);

    List<PollForm> getPollFormByCycle(Cycle cycle);

    List<Question> getQuestionListByPollForm(PollForm pollForm);
}
