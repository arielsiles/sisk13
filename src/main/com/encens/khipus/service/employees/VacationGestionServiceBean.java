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

import java.math.BigDecimal;
import javax.ejb.TransactionAttribute;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private VacationMovementService vacationMovementService;

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
        createMissingGestions(vacationPlanning, ignoreUndefinedVacationRule);
        distributeConsumption(vacationPlanning);
    }

    /**
     * Una gestion por anio de servicio cumplido, con los dias que fija la regla de antiguedad.
     * Los dias de una gestion no caducan: se acumulan a las siguientes.
     */
    private void createMissingGestions(VacationPlanning vacationPlanning, boolean ignoreUndefinedVacationRule)
            throws EntryDuplicatedException, ConcurrencyException, VacationRuleUndefinedYearException {
        DateTime initDateTime = new DateTime(vacationPlanning.getInitDate());
        int gestion = initDateTime.getYear();
        int years = vacationPlanning.getSeniorityYears();

        /* Con saldo inicial declarado el sistema no devenga nada anterior a esa fecha: el
           numero declarado ya lo contiene. Se compara la fecha del devengo -el aniversario que
           cierra ese anio de servicio- contra el corte, no los anios. */
        Date openingDate = vacationPlanning.getOpeningDate();

        int seniorityYears = 1;
        while (seniorityYears <= years) {
            if (null != openingDate
                    && !VacationMovementServiceBean.accrualDate(vacationPlanning, gestion).after(openingDate)) {
                gestion++;
                seniorityYears++;
                continue;
            }
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
                /* La gestion ya existe, pero su devengo puede faltar: por un fallo a mitad de
                   camino, o porque la gestion es anterior al libro. postAccrual es idempotente,
                   asi que llamarlo siempre repone lo que falte y no duplica lo que ya esta.
                   Sin esto una gestion sin devengo queda rota para siempre: el proceso la
                   detecta en cada corrida y nunca la arregla. */
                vacationMovementService.postAccrual(vacationGestion);
            } else {
                VacationRule vacationRule = vacationRuleService.findBySeniorityYear(seniorityYears);
                if (vacationRule != null) {
                    BigDecimal days = BigDecimal.valueOf(vacationRule.getVacationDays());

                    vacationGestion = new VacationGestion();
                    vacationGestion.setVacationDays(days);
                    vacationGestion.setDaysUsed(BigDecimal.ZERO);
                    vacationGestion.setDaysOff(days);
                    vacationGestion.setGestion(gestion);
                    vacationGestion.setVacationPlanning(vacationPlanning);

                    createVacationGestion(vacationGestion);
                    /* Los dias devengados entran al libro de saldo. Es idempotente: volver a
                       sincronizar no los duplica. */
                    vacationMovementService.postAccrual(vacationGestion);
                } else if (!ignoreUndefinedVacationRule) {
                    throw new VacationRuleUndefinedYearException(seniorityYears);
                }
            }

            gestion++;
            seniorityYears++;
        }
    }

    /**
     * Reparte lo consumido entre las gestiones, de la mas antigua a la mas nueva.
     * <p/>
     * Antes cada vacacion se descontaba de la gestion a la que estaba atada, asi que una
     * vacacion mas larga que el derecho de un anio no entraba en ninguna. Se consume contra el
     * saldo y se reparte: 18 dias sobre gestiones de 15 dejan la primera en 15 usados y la
     * segunda en 3, que es como lo lleva el informe de RRHH.
     * <p/>
     * El saldo inicial se consume primero por ser el mas antiguo. Si al final sobra consumo
     * -vacaciones anticipadas- queda en la ultima gestion y sus dias libres van en negativo,
     * que es justamente lo que hay que ver.
     */
    @SuppressWarnings({"unchecked"})
    private void distributeConsumption(VacationPlanning vacationPlanning) throws ConcurrencyException, EntryDuplicatedException {
        List<VacationGestion> gestionList = getEntityManager()
                .createNamedQuery("VacationGestion.findByVacationPlanningOrdered")
                .setParameter("vacationPlanning", vacationPlanning)
                .getResultList();
        if (gestionList.isEmpty()) {
            return;
        }

        BigDecimal pending = vacationService.sumApprovedDaysByVacationPlanning(vacationPlanning)
                .subtract(vacationMovementService.openingBalance(vacationPlanning));
        if (pending.compareTo(BigDecimal.ZERO) < 0) {
            pending = BigDecimal.ZERO;
        }

        for (int i = 0; i < gestionList.size(); i++) {
            VacationGestion gestion = gestionList.get(i);
            BigDecimal used;
            if (i == gestionList.size() - 1) {
                used = pending;
            } else {
                used = pending.min(gestion.getVacationDays());
            }
            pending = pending.subtract(used);

            gestion.setDaysUsed(used);
            gestion.setDaysOff(gestion.getVacationDays().subtract(used));
            updateVacationGestion(gestion);
        }
    }

    @SuppressWarnings({"unchecked"})
    public VacationGestion findOldestForConsumption(VacationPlanning vacationPlanning) {
        List<VacationGestion> withDays = findByVacationPlanningAvailableDaysOff(vacationPlanning);
        if (!withDays.isEmpty()) {
            return withDays.get(0);
        }
        List<VacationGestion> all = getEntityManager()
                .createNamedQuery("VacationGestion.findByVacationPlanningOrdered")
                .setParameter("vacationPlanning", vacationPlanning)
                .getResultList();
        return all.isEmpty() ? null : all.get(0);
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

    public BigDecimal sumVacationDaysByVacationPlanning(VacationPlanning vacationPlanning) {
        BigDecimal result = (BigDecimal) getEntityManager().createNamedQuery("VacationGestion.sumVacationDaysByVacationPlanning")
                .setParameter("vacationPlanningId", vacationPlanning.getId())
                .getSingleResult();
        return (result != null) ? result : BigDecimal.ZERO;
    }

    public BigDecimal sumDaysUsedByVacationPlanning(VacationPlanning vacationPlanning) {
        BigDecimal result = (BigDecimal) getEntityManager().createNamedQuery("VacationGestion.sumDaysUsedByVacationPlanning")
                .setParameter("vacationPlanningId", vacationPlanning.getId())
                .getSingleResult();
        return (result != null) ? result : BigDecimal.ZERO;
    }

    public BigDecimal sumDaysOffByVacationPlanning(VacationPlanning vacationPlanning) {
        BigDecimal result = (BigDecimal) getEntityManager().createNamedQuery("VacationGestion.sumDaysOffByVacationPlanning")
                .setParameter("vacationPlanningId", vacationPlanning.getId())
                .getSingleResult();
        return (result != null) ? result : BigDecimal.ZERO;
    }

}

