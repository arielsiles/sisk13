package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationAnnulledStateException;
import com.encens.khipus.exception.employees.VacationOverlapException;
import com.encens.khipus.exception.employees.VacationPendingStateException;
import com.encens.khipus.exception.employees.VacationPlanningExceedVacationDaysException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Vacation;
import com.encens.khipus.model.employees.VacationGestion;
import com.encens.khipus.model.employees.VacationState;

import javax.ejb.Local;

/**
 * @author
 * @version 3.4
 */
@Local
public interface VacationService extends GenericService {

    void createVacation(Vacation vacation) throws EntryDuplicatedException, VacationOverlapException, VacationPlanningExceedVacationDaysException;

    void updateVacation(Vacation vacation) throws ConcurrencyException, EntryDuplicatedException, VacationPendingStateException, VacationOverlapException, VacationPlanningExceedVacationDaysException;

    void deleteVacation(Vacation vacation) throws ConcurrencyException, ReferentialIntegrityException, VacationPendingStateException;

    void approveVacation(Vacation vacation) throws ConcurrencyException, VacationPendingStateException, EntryDuplicatedException, VacationOverlapException, VacationPlanningExceedVacationDaysException;

    void annulVacation(Vacation vacation) throws ConcurrencyException, EntryDuplicatedException, VacationAnnulledStateException, ReferentialIntegrityException;

    void calculateValues(Vacation vacation);

    Boolean hasCurrentState(Vacation vacation, VacationState vacationState);

    Integer sumTotalDaysByVacationGestion(VacationGestion vacationGestion);
}
