package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationAnnulledStateException;
import com.encens.khipus.exception.employees.VacationOverlapException;
import com.encens.khipus.exception.employees.VacationPendingStateException;
import com.encens.khipus.exception.employees.VacationPlanningExceedVacationDaysException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.NoResultException;
import java.util.Arrays;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Name("vacationService")
@Stateless
@AutoCreate
public class VacationServiceBean extends GenericServiceBean implements VacationService {

    @In
    private VacationPlanningService vacationPlanningService;

    @TransactionAttribute(REQUIRES_NEW)
    public void createVacation(Vacation vacation) throws EntryDuplicatedException, VacationOverlapException, VacationPlanningExceedVacationDaysException {
        validateOverlap(vacation, VacationState.PENDING, VacationState.APPROVED);
        validateExceedVacationDays(vacation);
        vacation.setState(VacationState.PENDING);
        create(vacation);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateVacation(Vacation vacation) throws ConcurrencyException, EntryDuplicatedException, VacationPendingStateException, VacationOverlapException, VacationPlanningExceedVacationDaysException {
        validateOverlap(vacation, VacationState.PENDING, VacationState.APPROVED);
        validateExceedVacationDays(vacation);
        if (!hasCurrentState(vacation, VacationState.PENDING)) {
            throw new VacationPendingStateException();
        }

        update(vacation);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteVacation(Vacation vacation) throws ConcurrencyException, ReferentialIntegrityException, VacationPendingStateException {
        if (!hasCurrentState(vacation, VacationState.PENDING)) {
            throw new VacationPendingStateException();
        }
        delete(vacation);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void approveVacation(Vacation vacation) throws ConcurrencyException, VacationPendingStateException, EntryDuplicatedException, VacationOverlapException, VacationPlanningExceedVacationDaysException {
        validateOverlap(vacation, VacationState.APPROVED);
        validateExceedVacationDays(vacation);
        if (!hasCurrentState(vacation, VacationState.PENDING)) {
            throw new VacationPendingStateException();
        }

        vacation.setState(VacationState.APPROVED);
        vacation.setDaysOff(getVacationDaysOff(vacation));
        update(vacation);

        createSpecialDate(vacation);
        vacationPlanningService.synchronizeVacationDaysIgnoreUndefinedVacationRule(vacation.getVacationGestion().getVacationPlanning());
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void annulVacation(Vacation vacation) throws ConcurrencyException, EntryDuplicatedException, VacationAnnulledStateException, ReferentialIntegrityException {
        VacationState lastState = getCurrentState(vacation);
        if (VacationState.ANNULLED.equals(lastState)) {
            throw new VacationAnnulledStateException();
        }

        vacation.setState(VacationState.ANNULLED);
        vacation.setDaysOff(0);
        update(vacation);

        deleteSpecialDate(vacation);
        if (VacationState.APPROVED.equals(lastState)) {
            vacationPlanningService.synchronizeVacationDaysIgnoreUndefinedVacationRule(vacation.getVacationGestion().getVacationPlanning());
        }
    }

    private void createSpecialDate(Vacation vacation) throws EntryDuplicatedException {
        if (vacation.getUseForPayrollGeneration()) {
            SpecialDate specialDate = new SpecialDate();
            specialDate.setTitle(vacation.getDescription());
            specialDate.setInitPeriod(vacation.getInitDate());
            specialDate.setEndPeriod(vacation.getEndDate());
            specialDate.setAllDay(true);
            specialDate.setEmployee(vacation.getVacationGestion().getVacationPlanning().getJobContract().getContract().getEmployee());
            specialDate.setCredit(SpecialDateType.PAID);
            specialDate.setRolType(SpecialDateRol.FECHA);
            specialDate.setSpecialDateTarget(SpecialDateTarget.EMPLOYEE);
            specialDate.setVacation(vacation);
            create(specialDate);
        }
    }

    private void deleteSpecialDate(Vacation vacation) throws ConcurrencyException, ReferentialIntegrityException {
        if (vacation.getUseForPayrollGeneration() && vacation.getSpecialDate() != null) {
            delete(vacation.getSpecialDate());
        }
    }


    public void calculateValues(Vacation vacation) {
        if (vacation.getInitDate() != null && vacation.getEndDate() != null &&
                vacation.getInitDate().compareTo(vacation.getEndDate()) <= 0) {
            //todo put here the integration with holidays if necessary
            vacation.setTotalDays((int) DateUtils.daysBetweenWithoutWeekend(vacation.getInitDate(), vacation.getEndDate()));
        } else {
            vacation.setTotalDays(0);
        }
    }

    private Integer getVacationDaysOff(Vacation vacation) {
        Integer daysOff = 0;
        if (vacation.getVacationGestion() != null) {
            daysOff = vacation.getVacationGestion().getDaysOff() - vacation.getTotalDays();
        }
        return daysOff;
    }

    public Boolean hasCurrentState(Vacation vacation, VacationState state) {
        return state.equals(getCurrentState(vacation));
    }

    private VacationState getCurrentState(Vacation vacation) {
        VacationState state = null;
        try {
            Vacation dbValue = getEventEntityManager().find(Vacation.class, vacation.getId());
            if (dbValue != null) {
                state = dbValue.getState();
            }
        } catch (NoResultException ignored) {
        }
        return state;
    }

    private void validateOverlap(Vacation vacation, VacationState... states) throws VacationOverlapException {
        if (!ValidatorUtil.isEmptyOrNull(states)) {
            List<VacationState> stateList = Arrays.asList(states);
            Long vacationId = vacation.getId() != null ? vacation.getId() : -1l;
            Long countOverlap = (Long) getEventEntityManager().createNamedQuery("Vacation.countOverlap")
                    .setParameter("vacationPlanningId", vacation.getVacationGestion().getVacationPlanning().getId())
                    .setParameter("vacationId", vacationId)
                    .setParameter("stateList", stateList)
                    .setParameter("initDate", vacation.getInitDate())
                    .setParameter("endDate", vacation.getEndDate())
                    .getSingleResult();
            if (countOverlap != null && countOverlap > 0) {
                throw new VacationOverlapException(countOverlap);
            }
        }
    }

    private void validateExceedVacationDays(Vacation vacation) throws VacationPlanningExceedVacationDaysException {
        VacationGestion vacationGestion = vacation.getVacationGestion();
        getEntityManager().refresh(vacationGestion);

        if (vacation.getTotalDays() > vacationGestion.getDaysOff()) {
            throw new VacationPlanningExceedVacationDaysException(vacationGestion.getDaysOff(), vacation.getTotalDays());
        }
    }

    public Integer sumTotalDaysByVacationGestion(VacationGestion vacationGestion) {
        Long result = (Long) getEntityManager().createNamedQuery("Vacation.sumTotalDaysByVacationGestion")
                .setParameter("vacationGestionId", vacationGestion.getId())
                .setParameter("state", VacationState.APPROVED)
                .getSingleResult();
        return (result != null) ? result.intValue() : 0;
    }

}