package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.employees.DuplicatedScheduleEvaluationCycleException;
import com.encens.khipus.exception.employees.DuplicatedScheduleEvaluationNameException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.PollForm;
import com.encens.khipus.model.employees.PollFormType;
import com.encens.khipus.model.employees.ScheduleEvaluation;

import javax.ejb.Local;
import java.util.Date;

/**
 * ScheduleEvaluationService
 *
 * @author
 * @version 2.24
 */
@Local
public interface ScheduleEvaluationService extends GenericService {

    void createScheduleEvaluation(ScheduleEvaluation scheduleEvaluation)
            throws DuplicatedScheduleEvaluationNameException,
            DuplicatedScheduleEvaluationCycleException,
            EntryDuplicatedException;

    Boolean validateDuplicateScheduleEvaluationName(ScheduleEvaluation scheduleEvaluation);

    Boolean validateDuplicateScheduleEvaluationCycle(ScheduleEvaluation scheduleEvaluation);

    PollForm findPollFormByTypeGestionAndPeriod(PollFormType type, Integer gestion, Integer period, Date dateTime);
}
