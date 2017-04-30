package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleUndefinedYearException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.VacationPlanning;

import javax.ejb.Local;

/**
 * @author
 * @version 3.4
 */
@Local
public interface VacationPlanningService extends GenericService {

    VacationPlanning load(VacationPlanning vacationPlanning) throws EntryNotFoundException;

    void createVacationPlanning(VacationPlanning vacationPlanning) throws EntryDuplicatedException;

    void updateVacationPlanning(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException;

    void deleteVacationPlanning(VacationPlanning vacationPlanning) throws ConcurrencyException, ReferentialIntegrityException;

    void synchronizeVacationDays(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException, VacationRuleUndefinedYearException;

    void synchronizeVacationDaysIgnoreUndefinedVacationRule(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException;
}
