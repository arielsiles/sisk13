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
import java.math.BigDecimal;
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

    @In
    private VacationMovementService vacationMovementService;

    @In
    private VacationRuleService vacationRuleService;

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

        vacationMovementService.postTaking(vacation);
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
        vacation.setDaysOff(BigDecimal.ZERO);
        update(vacation);

        deleteSpecialDate(vacation);
        if (VacationState.APPROVED.equals(lastState)) {
            vacationMovementService.revertTaking(vacation);
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
            /* El motivo lo escribe el modulo, no se elige a mano: es lo que permite reportar
               por causa y distinguir estos dias de los demas permisos pagados. */
            specialDate.setReason(SpecialDateReason.VACATION);
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
            /* Los dias derivados de un rango son enteros. Los medios dias existen -el informe
               de vacaciones los usa- y se cargan a mano corrigiendo el total. */
            vacation.setTotalDays(BigDecimal.valueOf(
                    DateUtils.daysBetweenWithoutWeekend(vacation.getInitDate(), vacation.getEndDate())));
        } else {
            vacation.setTotalDays(BigDecimal.ZERO);
        }
    }

    private BigDecimal getVacationDaysOff(Vacation vacation) {
        BigDecimal daysOff = BigDecimal.ZERO;
        if (vacation.getVacationGestion() != null) {
            daysOff = vacation.getVacationGestion().getDaysOff().subtract(vacation.getTotalDays());
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

    /**
     * El limite es el saldo del plan, no los dias de una gestion. Antes se comparaba contra la
     * gestion a la que la vacacion estaba atada, asi que una vacacion mas larga que el derecho
     * de un anio se rechazaba aunque el empleado tuviera saldo de sobra: 18 dias no entraban
     * contra una gestion de 15, teniendo 45 disponibles.
     * <p/>
     * El saldo puede quedar por debajo de cero hasta el limite que fije la regla de antiguedad:
     * son las vacaciones tomadas por adelantado. Con el limite en cero no se permiten.
     */
    private void validateExceedVacationDays(Vacation vacation) throws VacationPlanningExceedVacationDaysException {
        VacationPlanning planning = vacation.getVacationGestion().getVacationPlanning();

        BigDecimal available = vacationMovementService.balance(planning);
        /* Una vacacion aprobada que se esta reeditando ya descontó: no se cuenta dos veces. */
        if (VacationState.APPROVED.equals(vacation.getState())) {
            available = available.add(vacation.getTotalDays());
        }

        Integer advanceLimit = resolveAdvanceDaysLimit(planning);
        BigDecimal maximum = available.add(BigDecimal.valueOf(advanceLimit));

        if (vacation.getTotalDays().compareTo(maximum) > 0) {
            VacationPlanningExceedVacationDaysException e =
                    new VacationPlanningExceedVacationDaysException(available, vacation.getTotalDays());
            e.setAdvanceDaysLimit(advanceLimit);
            throw e;
        }
    }

    /**
     * El limite de anticipo sale del tramo de antiguedad que le toca al empleado. Quien todavia
     * no cumplio el primer anio no tiene tramo -y es justamente el caso del anticipo-, asi que
     * ahi se usa el tramo mas bajo del catalogo.
     */
    private Integer resolveAdvanceDaysLimit(VacationPlanning vacationPlanning) {
        VacationRule rule = vacationRuleService.findBySeniorityYear(vacationPlanning.getSeniorityYears());
        if (null == rule) {
            rule = vacationRuleService.findFirstTranche();
        }
        return (null == rule || null == rule.getAdvanceDaysLimit()) ? 0 : rule.getAdvanceDaysLimit();
    }

    public BigDecimal sumApprovedDaysByVacationPlanning(VacationPlanning vacationPlanning) {
        BigDecimal result = (BigDecimal) getEntityManager()
                .createNamedQuery("Vacation.sumTotalDaysByVacationPlanning")
                .setParameter("vacationPlanning", vacationPlanning)
                .setParameter("state", VacationState.APPROVED)
                .getSingleResult();
        return (result != null) ? result : BigDecimal.ZERO;
    }

    public BigDecimal sumTotalDaysByVacationGestion(VacationGestion vacationGestion) {
        BigDecimal result = (BigDecimal) getEntityManager().createNamedQuery("Vacation.sumTotalDaysByVacationGestion")
                .setParameter("vacationGestionId", vacationGestion.getId())
                .setParameter("state", VacationState.APPROVED)
                .getSingleResult();
        return (result != null) ? result : BigDecimal.ZERO;
    }

}