package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleUndefinedYearException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.VacationPlanning;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.employees.VacationPlanningBulkResult;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.joda.time.DateTime;

import javax.ejb.Stateless;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    /* En su propia transaccion: si esto falla no puede arrastrar el alta del contrato, que ya
       esta guardada. Sin REQUIRES_NEW compartiria la transaccion del llamador y un error aca
       la marcaria para rollback, que es justo lo que no puede pasar. */
    @TransactionAttribute(REQUIRES_NEW)
    public boolean createIfAbsent(JobContract jobContract) {
        if (null == jobContract || null == jobContract.getContract()) {
            return false;
        }

        /* Sin reglas cargadas la empresa no usa el modulo: no se le llena la base de planes. */
        Long rules = (Long) getEntityManager()
                .createQuery("select count(vr) from VacationRule vr").getSingleResult();
        if (null == rules || rules == 0) {
            return false;
        }

        Long planned = (Long) getEntityManager().createQuery(
                "select count(vp) from VacationPlanning vp"
                        + " where vp.jobContract.contract.employee = :employee")
                .setParameter("employee", jobContract.getContract().getEmployee())
                .getSingleResult();
        if (null != planned && planned > 0) {
            return false;
        }

        Date initDate = jobContract.getContract().getInitDate();
        if (null == initDate || !isPlausible(initDate)) {
            log.warn("No se crea el plan de vacacion: la fecha de inicio del contrato no sirve (#0)",
                    initDate);
            return false;
        }

        VacationPlanning planning = new VacationPlanning();
        planning.setJobContract(jobContract);
        planning.setInitDate(initDate);
        planning.setSeniorityYears(0);
        planning.setVacationDays(BigDecimal.ZERO);
        planning.setDaysUsed(BigDecimal.ZERO);
        planning.setDaysOff(BigDecimal.ZERO);
        try {
            createVacationPlanning(planning);
            return true;
        } catch (EntryDuplicatedException e) {
            log.warn("No se pudo crear el plan de vacacion del contrato de puesto", e);
            return false;
        }
    }

    /**
     * Un plan por contrato de puesto de empleado activo que todavia no tenga uno. La fecha de
     * arranque de la antiguedad sale del contrato: `empleado.fechaingreso` no se usa porque en
     * la practica viene vacia.
     */
    @SuppressWarnings({"unchecked"})
    public VacationPlanningBulkResult analyzeBulkGeneration() {
        VacationPlanningBulkResult result = new VacationPlanningBulkResult();

        List<JobContract> jobContractList = getEntityManager().createQuery(
                "select jc from JobContract jc"
                        + " where jc.contract.employee.retireDate is null"
                        + " order by jc.contract.employee.lastName asc").getResultList();

        for (JobContract jobContract : jobContractList) {
            String name = jobContract.getContract().getEmployee().getFullName();

            Long planned = (Long) getEntityManager().createQuery(
                    "select count(vp) from VacationPlanning vp where vp.jobContract = :jobContract")
                    .setParameter("jobContract", jobContract)
                    .getSingleResult();
            if (null != planned && planned > 0) {
                result.addAlreadyPlanned();
                continue;
            }

            Date initDate = jobContract.getContract().getInitDate();
            if (null == initDate) {
                result.addExcluded(name, "el contrato no tiene fecha de inicio");
                continue;
            }
            if (!isPlausible(initDate)) {
                result.addExcluded(name, "la fecha de inicio del contrato no es valida: " + initDate);
                continue;
            }

            VacationPlanning planning = new VacationPlanning();
            planning.setJobContract(jobContract);
            planning.setInitDate(initDate);
            planning.setSeniorityYears(0);
            planning.setVacationDays(BigDecimal.ZERO);
            planning.setDaysUsed(BigDecimal.ZERO);
            planning.setDaysOff(BigDecimal.ZERO);
            result.addNewPlanning(planning);
        }
        return result;
    }

    public int confirmBulkGeneration(VacationPlanningBulkResult result) throws Exception {
        int created = 0;
        for (VacationPlanning planning : result.getNewPlannings()) {
            createVacationPlanning(planning);
            created++;
        }
        return created;
    }

    /**
     * Una fecha de contrato anterior a 1950 o futura es un error de carga, no una antiguedad.
     * Sin este filtro un '0024-08-19' devengaria dos mil gestiones.
     */
    private boolean isPlausible(Date initDate) {
        Calendar limit = Calendar.getInstance();
        limit.set(1950, Calendar.JANUARY, 1);
        return initDate.after(limit.getTime()) && !initDate.after(new Date());
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

        BigDecimal vacationDays = vacationGestionService.sumVacationDaysByVacationPlanning(vacationPlanning);
        BigDecimal daysUsed = vacationGestionService.sumDaysUsedByVacationPlanning(vacationPlanning);
        BigDecimal daysOff = vacationGestionService.sumDaysOffByVacationPlanning(vacationPlanning);

        vacationPlanning.setVacationDays(vacationDays);
        vacationPlanning.setDaysOff(daysOff);
        vacationPlanning.setDaysUsed(daysUsed);

        update(vacationPlanning);
    }
}
