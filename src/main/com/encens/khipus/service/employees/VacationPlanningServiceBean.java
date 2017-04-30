package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleUndefinedYearException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.VacationPlanning;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.joda.time.DateTime;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.NoResultException;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Name("vacationPlanningService")
@Stateless
@AutoCreate
public class VacationPlanningServiceBean extends GenericServiceBean implements VacationPlanningService {

    @In
    private VacationGestionService vacationGestionService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    public VacationPlanning load(VacationPlanning vacationPlanning) throws EntryNotFoundException {
        VacationPlanning result = null;
        try {
            result = (VacationPlanning) getEntityManager()
                    .createNamedQuery("VacationPlanning.load")
                    .setParameter("id", vacationPlanning.getId())
                    .getSingleResult();
        } catch (NoResultException ignored) {
        }
        if (result == null) {
            throw new EntryNotFoundException();
        }
        return result;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void createVacationPlanning(VacationPlanning vacationPlanning) throws EntryDuplicatedException {
        vacationPlanning.setCode(sequenceGeneratorService.nextValue(Constants.VACATIONPLAN_CODE_SEQUENCE));
        create(vacationPlanning);
        try {
            synchronizeVacationDaysIgnoreUndefinedVacationRule(vacationPlanning);
        } catch (ConcurrencyException ignore) {
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateVacationPlanning(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException {
        update(vacationPlanning);
        synchronizeVacationDaysIgnoreUndefinedVacationRule(vacationPlanning);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteVacationPlanning(VacationPlanning vacationPlanning) throws ConcurrencyException, ReferentialIntegrityException {
        delete(vacationPlanning);
    }

    public void synchronizeVacationDays(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException, VacationRuleUndefinedYearException {
        synchronizeVacationDays(vacationPlanning, false);
    }

    public void synchronizeVacationDaysIgnoreUndefinedVacationRule(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException {
        try {
            synchronizeVacationDays(vacationPlanning, true);
        } catch (VacationRuleUndefinedYearException ignore) {
        }
    }

    private void synchronizeVacationDays(VacationPlanning vacationPlanning, boolean ignoreUndefinedVacationRule) throws ConcurrencyException, EntryDuplicatedException, VacationRuleUndefinedYearException {
        DateTime currentDateTime = new DateTime();
        DateTime initDateTime = new DateTime(vacationPlanning.getInitDate());
        int years = DateUtils.yearsBetween(initDateTime, currentDateTime);

        //first synchronize the years
        vacationPlanning.setSeniorityYears(years);

        //synchronize gestions
        vacationGestionService.synchronizeGestionVacation(vacationPlanning, ignoreUndefinedVacationRule);

        Integer vacationDays = vacationGestionService.sumVacationDaysByVacationPlanning(vacationPlanning);
        Integer daysUsed = vacationGestionService.sumDaysUsedByVacationPlanning(vacationPlanning);
        Integer daysOff = vacationGestionService.sumDaysOffByVacationPlanning(vacationPlanning);

        vacationPlanning.setVacationDays(vacationDays);
        vacationPlanning.setDaysOff(daysOff);
        vacationPlanning.setDaysUsed(daysUsed);

        update(vacationPlanning);
    }
}
