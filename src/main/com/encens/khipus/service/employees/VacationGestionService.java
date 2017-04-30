package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleUndefinedYearException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.VacationGestion;
import com.encens.khipus.model.employees.VacationPlanning;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Local
public interface VacationGestionService extends GenericService {

    @TransactionAttribute(REQUIRES_NEW)
    void createVacationGestion(VacationGestion vacationGestion) throws EntryDuplicatedException;

    @TransactionAttribute(REQUIRES_NEW)
    void updateVacationGestion(VacationGestion vacationGestion) throws ConcurrencyException, EntryDuplicatedException;

    @TransactionAttribute(REQUIRES_NEW)
    void deleteVacationGestion(VacationGestion vacationGestion) throws ConcurrencyException, ReferentialIntegrityException;

    void synchronizeGestionVacation(VacationPlanning vacationPlanning, boolean ignoreUndefinedVacationRule) throws EntryDuplicatedException, ConcurrencyException, VacationRuleUndefinedYearException;

    List<VacationGestion> findByVacationPlanningAvailableDaysOff(VacationPlanning vacationPlanning);

    Integer sumVacationDaysByVacationPlanning(VacationPlanning vacationPlanning);

    Integer sumDaysUsedByVacationPlanning(VacationPlanning vacationPlanning);

    Integer sumDaysOffByVacationPlanning(VacationPlanning vacationPlanning);
}
