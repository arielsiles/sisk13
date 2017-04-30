package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.employees.DuplicatedScheduleEvaluationCycleException;
import com.encens.khipus.exception.employees.DuplicatedScheduleEvaluationNameException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Date;

/**
 * ScheduleEvaluationServiceBean
 *
 * @author
 * @version 2.24
 */
@Name("scheduleEvaluationService")
@Stateless
@AutoCreate
public class ScheduleEvaluationServiceBean extends GenericServiceBean implements ScheduleEvaluationService {

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    public void createScheduleEvaluation(ScheduleEvaluation scheduleEvaluation)
            throws DuplicatedScheduleEvaluationNameException,
            DuplicatedScheduleEvaluationCycleException,
            EntryDuplicatedException {
        if (!validateDuplicateScheduleEvaluationName(scheduleEvaluation)) {
            throw new DuplicatedScheduleEvaluationNameException();
        }
        if (!validateDuplicateScheduleEvaluationCycle(scheduleEvaluation)) {
            throw new DuplicatedScheduleEvaluationCycleException();
        }

        for (ScheduleEvaluationForm scheduleEvaluationForm : scheduleEvaluation.getScheduleEvaluationFormList()) {
            scheduleEvaluationForm.setScheduleEvaluation(scheduleEvaluation);
        }
        create(scheduleEvaluation);
    }

    public Boolean validateDuplicateScheduleEvaluationName(ScheduleEvaluation scheduleEvaluation) {
        Long countByName = (Long) ((scheduleEvaluation.getId() == null) ?
                listEm.createNamedQuery("ScheduleEvaluation.countByName")
                        .setParameter("name", scheduleEvaluation.getName())
                        .getSingleResult()
                :
                listEm.createNamedQuery("ScheduleEvaluation.countByNameAndScheduleEvaluation")
                        .setParameter("name", scheduleEvaluation.getName())
                        .setParameter("scheduleEvaluationId", scheduleEvaluation.getId())
                        .getSingleResult());
        return countByName == null || countByName == 0;
    }

    public Boolean validateDuplicateScheduleEvaluationCycle(ScheduleEvaluation scheduleEvaluation) {
        Long countByCycle = (Long) ((scheduleEvaluation.getId() == null)
                ?
                listEm.createNamedQuery("ScheduleEvaluation.countByCycle")
                        .setParameter("cycleId", scheduleEvaluation.getCycle().getId())
                        .getSingleResult()
                :
                listEm.createNamedQuery("ScheduleEvaluation.countByCycleAndScheduleEvaluation")
                        .setParameter("cycleId", scheduleEvaluation.getCycle().getId())
                        .setParameter("scheduleEvaluationId", scheduleEvaluation.getId())
                        .getSingleResult());
        return countByCycle == null || countByCycle == 0;
    }

    public PollForm findPollFormByTypeGestionAndPeriod(PollFormType type, Integer gestion, Integer period, Date dateTime) {
        PollForm pollForm = null;
        try {
            pollForm = (PollForm) getEntityManager().createNamedQuery("ScheduleEvaluationForm.findPollFormByTypeGestionAndPeriod")
                    .setParameter("type", type)
                    .setParameter("year", gestion)
                    .setParameter("period", period)
                    .setParameter("state", ScheduleEvaluationState.ENABLED)
                    .setParameter("dateTime", dateTime).getSingleResult();
            if (pollForm != null) {
                getEntityManager().merge(pollForm);
            }
        } catch (NoResultException noResultException) {
        }
        return pollForm;
    }

}
