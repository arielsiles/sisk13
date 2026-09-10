package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleUndefinedYearException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.ContractState;
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

    /**
     * Por aca pasan los tres caminos que crean un plan: el alta de contrato, la generacion
     * masiva y el boton Nuevo. Por eso la regla que no puede romperse nunca vive aca y no en
     * cada uno: <b>una persona no puede tener dos planes abiertos a la vez</b>.
     * <p/>
     * El saldo de vacaciones se deriva sumando los movimientos de un plan. Con dos planes
     * abiertos el saldo se parte en dos y ninguno de los dos dice la verdad.
     */
    @TransactionAttribute(REQUIRES_NEW)
    public void createVacationPlanning(VacationPlanning vacationPlanning) throws EntryDuplicatedException {
        if (hasOpenPlan(vacationPlanning)) {
            throw new EntryDuplicatedException();
        }
        vacationPlanning.setCode(sequenceGeneratorService.nextValue(Constants.VACATIONPLAN_CODE_SEQUENCE));
        create(vacationPlanning);
        try {
            synchronizeVacationDaysIgnoreUndefinedVacationRule(vacationPlanning);
        } catch (ConcurrencyException ignore) {
        }
    }

    /** true si esa persona ya tiene un plan abierto, sin contar al que se esta creando. */
    private boolean hasOpenPlan(VacationPlanning planning) {
        if (null == planning || null == planning.getJobContract()
                || null == planning.getJobContract().getContract()) {
            return false;
        }
        Long open = (Long) getEntityManager().createQuery(
                "select count(vp) from VacationPlanning vp"
                        + " where vp.jobContract.contract.employee = :employee"
                        + " and vp.closeDate is null")
                .setParameter("employee", planning.getJobContract().getContract().getEmployee())
                .getSingleResult();
        return null != open && open > 0;
    }

    /* En su propia transaccion: si esto falla no puede arrastrar el alta del contrato, que ya
       esta guardada. Sin REQUIRES_NEW compartiria la transaccion del llamador y un error aca
       la marcaria para rollback, que es justo lo que no puede pasar. */
    @TransactionAttribute(REQUIRES_NEW)
    public boolean createIfAbsent(JobContract jobContract) {
        if (null == jobContract || null == jobContract.getContract()) {
            return false;
        }

        /* Un contrato cerrado no genera plan. No deberia pasar dando de alta, pero la regla vive
           aca y no en quien llama, para que no dependa de por donde se entre. */
        ContractState state = jobContract.getContract().getContractState();
        if (null != state && state.isInactive()) {
            return false;
        }

        /* Solo el contrato PRINCIPAL genera vacaciones. Un secundario es trabajo eventual
           acotado y no arrastra el derecho. Esta condicion estaba solo en la generacion masiva:
           faltando aca, dar de alta un contrato adicional le creaba un plan igual. */
        if (!jobContract.getContract().isMain()) {
            return false;
        }

        /* Sin reglas cargadas la empresa no usa el modulo: no se le llena la base de planes. */
        Long rules = (Long) getEntityManager()
                .createQuery("select count(vr) from VacationRule vr").getSingleResult();
        if (null == rules || rules == 0) {
            return false;
        }

        /* Solo los planes ABIERTOS. Con "ningun plan" a secas, un reingreso no recibia el
           suyo: la persona volvia y seguia viendo el saldo del periodo anterior. Mirando solo
           los abiertos las dos situaciones quedan bien y siguen siendo distinguibles: un cambio
           de puesto tiene su plan abierto y no recibe otro, y un reingreso no tiene ninguno. */
        Long planned = (Long) getEntityManager().createQuery(
                "select count(vp) from VacationPlanning vp"
                        + " where vp.jobContract.contract.employee = :employee"
                        + " and vp.closeDate is null")
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
     * Un plan por contrato de puesto de empleado activo que todavia no tenga uno.
     * <p/>
     * Solo del <b>contrato principal</b>: una persona puede tener varios contratos a la vez, y
     * los secundarios son trabajo eventual acotado que no genera vacaciones. Sin esa condicion
     * alguien con dos contratos tendria dos planes y acumularia el doble.
     * <p/>
     * La fecha de arranque de la antiguedad sale del contrato: `empleado.fechaingreso` no se usa
     * porque en la practica viene vacia.
     */
    @SuppressWarnings({"unchecked"})
    public VacationPlanningBulkResult analyzeBulkGeneration() {
        VacationPlanningBulkResult result = new VacationPlanningBulkResult();

        /* El contrato tiene que estar VIGENTE, no solo ser el principal. Un contrato cerrado
           conserva la marca de principal -lo fue en su momento- y si su dueno reingreso ya no
           tiene fecha de salida, asi que cumplia las otras dos condiciones y se colaba: se le
           generaba un plan a un contrato terminado, que nacia abierto y empezaba a devengar
           anios que nunca correspondieron.
           El principal va como parametro y no como literal `= true` porque la columna esta
           mapeada con un tipo propio -un entero que representa el booleano- y HQL no sabe
           renderizar un literal booleano contra un tipo asi. */
        List<JobContract> jobContractList = getEntityManager().createQuery(
                "select jc from JobContract jc"
                        + " where jc.contract.employee.retireDate is null"
                        + " and jc.contract.mainContract = :main"
                        + " and upper(jc.contract.contractState.name) <> :inactive"
                        + " order by jc.contract.employee.lastName asc")
                .setParameter("main", Boolean.TRUE)
                .setParameter("inactive", ContractState.INACTIVE)
                .getResultList();

        for (JobContract jobContract : jobContractList) {
            String name = jobContract.getContract().getEmployee().getFullName();

            /* Por PERSONA y no por puesto, igual que el alta de contrato. Mirando el puesto,
               a alguien con un plan abierto en otro puesto se le creaba un segundo plan y su
               saldo quedaba partido en dos. */
            Long planned = (Long) getEntityManager().createQuery(
                    "select count(vp) from VacationPlanning vp"
                            + " where vp.jobContract.contract.employee = :employee"
                            + " and vp.closeDate is null")
                    .setParameter("employee", jobContract.getContract().getEmployee())
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

    /**
     * Cierra los planes de un contrato al ultimo dia de trabajo.
     * <p/>
     * Primero escribe la fecha de cierre y RECIEN DESPUES sincroniza: asi el devengo cuenta
     * contra esa fecha y el plan queda cerrado y al dia en el mismo acto. Al reves quedaria
     * cerrado con el saldo de otro dia, que es peor que no cerrarlo.
     * <p/>
     * El saldo que sobra no se paga ni se borra: queda en el plan cerrado, pendiente de
     * liquidacion. Pagarlo es finiquito y eso es otro tema.
     */
    @SuppressWarnings({"unchecked"})
    public int closePlansOfContract(Long contractId, Date closeDate) {
        if (null == contractId || null == closeDate) {
            return 0;
        }
        List<VacationPlanning> plannings = getEntityManager().createQuery(
                "select vp from VacationPlanning vp"
                        + " where vp.jobContract.contract.id = :contractId"
                        + " and vp.closeDate is null")
                .setParameter("contractId", contractId)
                .getResultList();

        int closed = 0;
        for (VacationPlanning planning : plannings) {
            try {
                planning.setCloseDate(closeDate);
                synchronizeVacationDaysIgnoreUndefinedVacationRule(planning);
                closed++;
            } catch (Exception e) {
                log.error("No se pudo cerrar el plan de vacacion #0", e, planning.getId());
            }
        }
        return closed;
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
        /* Hasta la fecha de cierre, no hasta hoy. Es el unico lugar que cuenta los anios de
           servicio, asi que con esto un plan cerrado deja de crecer para siempre y sin que haya
           que correr ningun proceso: el mismo calculo da el resultado correcto siempre. */
        DateTime currentDateTime = new DateTime(vacationPlanning.getAccrualLimit());
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
