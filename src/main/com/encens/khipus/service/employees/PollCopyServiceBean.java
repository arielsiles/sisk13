package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.academics.AcademicStructureService;
import com.encens.khipus.util.TextUtil;
import com.encens.khipus.util.academic.AcademicStructure;
import com.encens.khipus.util.employees.PollFormUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PollCopyServiceBean
 *
 * @author
 * @version 2.24
 */
@Name("pollCopyService")
@Stateless
@AutoCreate
public class PollCopyServiceBean extends GenericServiceBean implements PollCopyService {

    @In
    private AcademicStructureService academicStructureService;

    @In
    private ScheduleEvaluationService scheduleEvaluationService;


    public void createPollCopy(PollCopy pollCopy,
                               Map<Question, EvaluationCriteriaValue> evaluationCriteriaValueResult) throws EntryDuplicatedException {
        pollCopy.setPollPunctuationList(new ArrayList<PollPunctuation>());
        for (Map.Entry<Question, EvaluationCriteriaValue> valueEntry : evaluationCriteriaValueResult.entrySet()) {
            pollCopy.getPollPunctuationList().add(new PollPunctuation(pollCopy, valueEntry.getKey(), valueEntry.getValue()));
        }
        super.create(pollCopy);
    }

    public void createFormAcademicStructure(PollForm pollForm,
                                            Person evaluator,
                                            Map<AcademicStructure, Map<Question, EvaluationCriteriaValue>> evaluationCriteriaValueResult,
                                            Text comment)
            throws EntryDuplicatedException {
        for (Map.Entry<AcademicStructure, Map<Question, EvaluationCriteriaValue>> valueEntry : evaluationCriteriaValueResult.entrySet()) {
            AcademicStructure academicStructure = valueEntry.getKey();
            PollCopy pollCopy = newPollCopy(pollForm, evaluator, academicStructure, comment);
            createPollCopy(pollCopy, valueEntry.getValue());
        }
    }

    public void createFormAcademicStructure(PollForm pollForm,
                                            Person evaluator,
                                            List<AcademicStructure> academicStructureList,
                                            Map<Question, EvaluationCriteriaValue> evaluationCriteriaValueResult,
                                            Text comment)
            throws EntryDuplicatedException {
        for (AcademicStructure academicStructure : academicStructureList) {
            PollCopy pollCopy = newPollCopy(pollForm, evaluator, academicStructure, comment);
            createPollCopy(pollCopy, evaluationCriteriaValueResult);
        }
    }

    private PollCopy newPollCopy(PollForm pollForm,
                                 Person evaluator,
                                 AcademicStructure academicStructure,
                                 Text comment) {
        PollCopy pollCopy = new PollCopy();

        pollCopy.setRevisionNumber(getRevisionNumber(pollForm));
        pollCopy.setPollForm(pollForm);
        pollCopy.setEvaluator(evaluator);
        pollCopy.setPerson(academicStructure.getEmployee());

        if (!TextUtil.isEmpty(comment)) {
            pollCopy.setComment(comment);
        }

        if (PollFormUtil.isVisible(pollForm.getCycleRestriction())) {
            pollCopy.setCycle(pollForm.getCycle());
        }

        if (PollFormUtil.isVisible(pollForm.getAcademicPeriodRestriction()) && academicStructure.getSubject() != null) {
            pollCopy.setAcademicPeriod(
                    academicStructureService.synchronizeAcademicPeriod(academicStructure.getCareer().getReferenceId(),
                            academicStructure.getSubject().getReferenceId(),
                            pollForm.getCycle().getGestion().getYear(),
                            pollForm.getCycle().getCycleType().getPeriod())
            );
        }

        PollFormUtil.insertGrouppingValues(pollForm,
                pollCopy,
                academicStructure.getFaculty(),
                academicStructure.getCareer(),
                academicStructure.getSubject());

        return pollCopy;
    }

    public Boolean isEnabledToEvaluate(Person evaluator, PollForm pollForm) {
        Long result = (Long) getEntityManager()
                .createNamedQuery("PollCopy.countByEvaluatorAndPollForm")
                .setParameter("pollForm", pollForm)
                .setParameter("evaluator", evaluator)
                .getSingleResult();

        return null != result && result == 0;
    }

    private Integer getRevisionNumber(PollForm pollForm) {
        Integer result = (Integer) getEntityManager().createNamedQuery("PollCopy.maxRevisionNumber")
                .setParameter("pollForm", pollForm)
                .getSingleResult();

        if (null == result) {
            result = 1;
        }

        return result;
    }
}
