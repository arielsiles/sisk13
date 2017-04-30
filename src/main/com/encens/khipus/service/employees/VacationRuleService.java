package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleOverlapException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.VacationRule;

import javax.ejb.Local;

/**
 * @author
 * @version 3.4
 */
@Local
public interface VacationRuleService extends GenericService {
    void createVacationRule(VacationRule vacationRule) throws EntryDuplicatedException, VacationRuleOverlapException;

    void updateVacationRule(VacationRule vacationRule) throws ConcurrencyException, EntryDuplicatedException, VacationRuleOverlapException;

    void deleteVacationRule(VacationRule vacationRule) throws ConcurrencyException, ReferentialIntegrityException;

    VacationRule findLastVacationRule();

    VacationRule findBySeniorityYear(Integer seniorityYear);
}
