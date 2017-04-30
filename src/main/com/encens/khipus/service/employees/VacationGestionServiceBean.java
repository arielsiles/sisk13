package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleUndefinedYearException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.VacationGestion;
import com.encens.khipus.model.employees.VacationPlanning;
import com.encens.khipus.model.employees.VacationRule;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.joda.time.DateTime;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Name("vacationGestionService")
@Stateless
@AutoCreate
public class VacationGestionServiceBean extends GenericServiceBean implements VacationGestionService {

    @In
    private VacationService vacationService;

    @In
    private VacationRuleService vacationRuleService;

    @TransactionAttribute(REQUIRES_NEW)
    public void createVacationGestion(VacationGestion vacationGestion) throws EntryDuplicatedException {
        create(vacationGestion);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateVacationGestion(VacationGestion vacationGestion) throws ConcurrencyException, EntryDuplicatedException {
        update(vacationGestion);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deleteVacationGestion(VacationGestion vacationGestion) throws ConcurrencyException, ReferentialIntegrityException {
        delete(vacationGestion);
    }


    public void synchronizeGestionVacation(VacationPlanning vacationPlanning, boolean ignoreUndefinedVacationRule) throws EntryDuplicatedException, ConcurrencyException, VacationRuleUndefinedYearException {
        DateTime initDateTime = new DateTime(vacationPlanning.getInitDate());
        int gestion = initDateTime.getYear();
        int years = vacationPlanning.getSeniorityYears();

        int seniorityYears = 1;
        while (seniorityYears <= years) {
            VacationGestion vacationGestion = null;
            try {
                vacationGestion = (VacationGestion) getEventEntityManager().createNamedQuery("VacationGestion.findByVacationPlanningGestion")
                        .setParameter("vacationPlanning", vacationPlanning)
                        .setParameter("gestion", gestion)
                        .getSingleResult();
            } catch (Exception e) {
                log.debug("not found gestion... " + gestion + "," + e);
            }


            if (vacationGestion != null) {
                //update vacation days
                Integer daysUsed = vacationService.sumTotalDaysByVacationGestion(vacationGestion);
                int daysOff = vacationGestion.getVacationDays() - daysUsed;

                vacationGestion.setDaysUsed(daysUsed);
                vacationGestion.setDaysOff(daysOff);
                updateVacationGestion(vacationGestion);
            } else {
                VacationRule vacationRule = vacationRuleService.findBySeniorityYear(seniorityYears);
                if (vacationRule != null) {
                    int days = vacationRule.getVacationDays();

                    vacationGestion = new VacationGestion();
                    vacationGestion.setVacationDays(days);
                    vacationGestion.setDaysUsed(0);
                    vacationGestion.setDaysOff(days);
                    vacationGestion.setGestion(gestion);
                    vacationGestion.setVacationPlanning(vacationPlanning);

                    createVacationGestion(vacationGestion);
                } else {
                    if (!ignoreUndefinedVacationRule) {
                        throw new VacationRuleUndefinedYearException(seniorityYears);
                    }
                }
            }

            gestion++;
            seniorityYears++;
        }
    }

    public List<VacationGestion> findByVacationPlanningAvailableDaysOff(VacationPlanning vacationPlanning) {
        List<VacationGestion> resultList = new ArrayList<VacationGestion>();
        try {
            resultList = (List<VacationGestion>) getEntityManager().createNamedQuery("VacationGestion.findByVacationPlanningAvailableDaysOff")
                    .setParameter("vacationPlanningId", vacationPlanning.getId())
                    .getResultList();
        } catch (NoResultException ignored) {
        }
        return resultList;
    }

    public Integer sumVacationDaysByVacationPlanning(VacationPlanning vacationPlanning) {
        Long result = (Long) getEntityManager().createNamedQuery("VacationGestion.sumVacationDaysByVacationPlanning")
                .setParameter("vacationPlanningId", vacationPlanning.getId())
                .getSingleResult();
        return (result != null) ? result.intValue() : 0;
    }

    public Integer sumDaysUsedByVacationPlanning(VacationPlanning vacationPlanning) {
        Long result = (Long) getEntityManager().createNamedQuery("VacationGestion.sumDaysUsedByVacationPlanning")
                .setParameter("vacationPlanningId", vacationPlanning.getId())
                .getSingleResult();
        return (result != null) ? result.intValue() : 0;
    }

    public Integer sumDaysOffByVacationPlanning(VacationPlanning vacationPlanning) {
        Long result = (Long) getEntityManager().createNamedQuery("VacationGestion.sumDaysOffByVacationPlanning")
                .setParameter("vacationPlanningId", vacationPlanning.getId())
                .getSingleResult();
        return (result != null) ? result.intValue() : 0;
    }

}

