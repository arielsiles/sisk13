package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.ContractDuration;
import com.encens.khipus.model.employees.ContractMode;
import com.encens.khipus.model.employees.ContractMovement;
import com.encens.khipus.model.employees.ContractMovementType;
import com.encens.khipus.model.employees.ContractState;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.MainContractResult;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("contractConditionService")
@AutoCreate
public class ContractConditionServiceBean extends GenericServiceBean implements ContractConditionService {

    /* Se resuelve por nombre porque el id no esta garantizado entre bases de clientes. */
    private static final String INACTIVE_STATE = ContractState.INACTIVE;

    @In(required = false)
    private User currentUser;

    @In
    private VacationPlanningService vacationPlanningService;

    public ContractMovement changeCondition(Contract contract, ContractMode newMode,
                                            ContractDuration newDuration, Date newEndDate,
                                            Date from, String reason) throws Exception {
        /* Se relee dentro de la transaccion: la accion vive en la sesion y el contrato que
           trae esta desprendido. Trabajar sobre el administrado evita que el movimiento
           apunte a una instancia distinta del mismo registro. */
        Contract managed = getEntityManager().find(Contract.class, contract.getId());

        ContractMovement movement = new ContractMovement();
        movement.setContract(managed);
        movement.setType(ContractMovementType.CONDITION_CHANGE);
        movement.setDate(startOfDay(from));
        movement.setReason(reason);
        movement.setUser(currentUser);
        movement.setPreviousContractMode(managed.getContractMode());
        movement.setPreviousDuration(managed.getDuration());
        movement.setPreviousEndDate(managed.getEndDate());

        if (null != newMode) {
            managed.setContractMode(newMode);
        }
        managed.setDuration(newDuration);
        /* Un indefinido no lleva fecha de fin. Dejarla puesta la volveria a convertir en un
           vencimiento que nadie renueva, que es el problema que este plan viene a sacar. */
        managed.setEndDate(ContractDuration.FIXED_TERM.equals(newDuration)
                ? startOfDay(newEndDate) : null);

        movement.setNewContractMode(managed.getContractMode());
        movement.setNewDuration(managed.getDuration());
        movement.setNewEndDate(managed.getEndDate());

        update(managed);
        create(movement);
        return movement;
    }

    public ContractMovement terminate(Contract contract, Date departureDate, String reason)
            throws Exception {
        Date departure = startOfDay(departureDate);
        Contract managed = getEntityManager().find(Contract.class, contract.getId());

        ContractMovement movement = new ContractMovement();
        movement.setContract(managed);
        movement.setType(ContractMovementType.TERMINATION);
        movement.setDate(departure);
        movement.setReason(reason);
        movement.setUser(currentUser);
        movement.setPreviousContractMode(managed.getContractMode());
        movement.setNewContractMode(managed.getContractMode());
        movement.setPreviousDuration(managed.getDuration());
        movement.setNewDuration(managed.getDuration());
        movement.setPreviousEndDate(managed.getEndDate());
        movement.setNewEndDate(departure);

        managed.setEndDate(departure);
        /* No se toca activeForPayrollGeneration: apagarlo aca le haria perder la ultima
           planilla, que se genera el mes siguiente. El rango de fechas ya la resuelve. */
        ContractState inactive = findInactiveState();
        if (null != inactive) {
            managed.setContractState(inactive);
        }
        update(managed);

        /* La fecha de salida NO se escribe aca. Se deriva, y de todos los contratos: si a la
           persona le queda otro abierto no se fue, y si esta baja corrige una anterior hay que
           pisar la fecha vieja en vez de respetarla por estar puesta. */
        refreshRetireDate(managed);

        /* El plan de vacaciones de ese contrato se cierra en el mismo acto y queda al dia a la
           fecha de salida. Si no, sigue devengando anios de servicio de una relacion laboral
           que ya termino. Aislado a proposito: la baja no puede quedar bloqueada porque el
           modulo de vacaciones falle. */
        try {
            vacationPlanningService.closePlansOfContract(managed.getId(), departure);
        } catch (Exception e) {
            log.error("No se pudo cerrar el plan de vacacion del contrato #0", e, managed.getId());
        }

        create(movement);
        return movement;
    }

    public void refreshRetireDate(Contract contract) {
        if (null == contract) {
            return;
        }
        /* `find` resuelve contra el contexto de persistencia, asi que encuentra tambien al
           contrato que se acaba de dar de alta. La consulta de mas abajo va a la base y ese
           contrato puede no estar visible todavia -el alta corre en su propia transaccion-, por
           eso se lo pasa aparte: el contrato que dispara el recalculo forma parte de los
           contratos de la persona por definicion, se lo devuelva la consulta o no. */
        Contract managed = getEntityManager().find(Contract.class, contract.getId());
        if (null == managed) {
            /* Se borro: no hay contrato que sumar, solo hay que recalcular sin el. */
            refreshRetireDate(contract.getEmployee(), null);
            return;
        }
        refreshRetireDate(managed.getEmployee(), managed);
    }

    public void refreshRetireDate(Employee employee) {
        refreshRetireDate(employee, null);
    }

    @SuppressWarnings({"unchecked"})
    private void refreshRetireDate(Employee employee, Contract trigger) {
        if (null == employee || null == employee.getId()) {
            return;
        }
        Employee managed = getEntityManager().find(Employee.class, employee.getId());
        if (null == managed) {
            return;
        }

        /* La lista y la cuenta en Java, no una consulta con agregados y un case: la version con
           `sum(case when ...)` sobre el estado devolvia cero contratos abiertos y la persona
           quedaba dada por ida teniendo contrato vigente. Con dos o tres contratos por persona
           no hay nada que optimizar y esto se puede leer. */
        List<Contract> contracts = getEntityManager().createQuery(
                "select c from Contract c left join fetch c.contractState"
                        + " where c.employee.id = :employeeId")
                .setParameter("employeeId", managed.getId())
                .getResultList();

        /* El contrato que disparo el recalculo, si la consulta no lo trajo. */
        if (null != trigger && !containsId(contracts, trigger.getId())) {
            contracts.add(trigger);
        }

        /* Sin contratos no hay nada que concluir: la persona esta cargada pero no contratada. */
        if (contracts.isEmpty()) {
            return;
        }

        boolean anyOpen = false;
        Date lastEnd = null;
        for (Contract each : contracts) {
            ContractState state = each.getContractState();
            if (null == state || !state.isInactive()) {
                anyOpen = true;
            }
            if (null != each.getEndDate() && (null == lastEnd || each.getEndDate().after(lastEnd))) {
                lastEnd = each.getEndDate();
            }
        }

        Date retireDate = anyOpen ? null : lastEnd;
        /* Con los ids: cuando el resultado sorprende, lo primero que hay que saber es que
           contratos vio, no cuantos. */
        StringBuilder seen = new StringBuilder();
        for (Contract each : contracts) {
            seen.append(seen.length() == 0 ? "" : ",").append(each.getId());
        }
        log.info("Fecha de salida del empleado #0: contratos=[#1] abiertos=#2 -> #3",
                managed.getId(), seen, anyOpen, retireDate);

        /* Se escribe con un update directo y no dejandolo al control de cambios. Esta pantalla
           corre con flush manual: un cambio en memoria que nadie manda a grabar se descarta, y
           eso ya costo dos vueltas de pruebas. */
        getEntityManager().createQuery(
                "update Employee e set e.retireDate = :retireDate where e.id = :employeeId")
                .setParameter("retireDate", retireDate)
                .setParameter("employeeId", managed.getId())
                .executeUpdate();

        /* Y sobre las dos copias en memoria, para que ningun merge posterior devuelva el valor
           viejo a la base. */
        managed.setRetireDate(retireDate);
        employee.setRetireDate(retireDate);
    }

    private static boolean containsId(List<Contract> contracts, Long id) {
        for (Contract each : contracts) {
            if (null != each.getId() && each.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------- contrato principal

    public MainContractResult applyMainContract(Contract contract) throws Exception {
        if (null == contract || null == contract.getId()) {
            return MainContractResult.SECONDARY;
        }
        Contract managed = getEntityManager().find(Contract.class, contract.getId());
        if (null == managed) {
            return MainContractResult.SECONDARY;
        }

        List<Contract> others = findOpenContracts(managed.getEmployee(), managed);

        if (!managed.isMain()) {
            /* No se designa a nadie por su cuenta. Que una persona se quede sin contrato
               principal es valido; lo que no puede pasar es que ocurra sin que se vea. */
            for (Contract other : others) {
                if (other.isMain()) {
                    return MainContractResult.SECONDARY;
                }
            }
            return MainContractResult.WITHOUT_MAIN;
        }

        boolean moved = false;
        for (Contract other : others) {
            if (other.isMain()) {
                other.setMainContract(Boolean.FALSE);
                update(other);
                moved = true;
            }
        }
        return moved ? MainContractResult.MOVED : MainContractResult.DESIGNATED;
    }

    public Contract findMainContract(Employee employee) {
        for (Contract contract : findOpenContracts(employee, null)) {
            if (contract.isMain()) {
                return contract;
            }
        }
        return null;
    }

    public boolean lacksMainContract(Employee employee) {
        List<Contract> open = findOpenContracts(employee, null);
        if (open.isEmpty()) {
            return false;
        }
        for (Contract contract : open) {
            if (contract.isMain()) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public List<Contract> findOpenContracts(Employee employee, Contract excluded) {
        if (null == employee || null == employee.getId()) {
            return new ArrayList<Contract>();
        }
        List<Contract> resultList = getEntityManager().createQuery(
                "select c from Contract c"
                        + " where c.employee.id = :employeeId"
                        + " and upper(c.contractState.name) <> :inactive"
                        + " order by c.initDate")
                .setParameter("employeeId", employee.getId())
                .setParameter("inactive", INACTIVE_STATE)
                .getResultList();

        List<Contract> open = new ArrayList<Contract>();
        for (Contract contract : resultList) {
            if (null == excluded || !contract.getId().equals(excluded.getId())) {
                open.add(contract);
            }
        }
        return open;
    }

    @SuppressWarnings({"unchecked"})
    public List<ContractMovement> findMovements(Contract contract) {
        if (null == contract || null == contract.getId()) {
            return new ArrayList<ContractMovement>();
        }
        List<ContractMovement> resultList = getEntityManager()
                .createNamedQuery("ContractMovement.findByContract")
                .setParameter("contract", contract)
                .getResultList();
        return null == resultList ? new ArrayList<ContractMovement>() : resultList;
    }

    @SuppressWarnings({"unchecked"})
    public List<Contract> findExpiring(int days) {
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, days);
        List<Contract> resultList = getEntityManager().createQuery(
                "select c from Contract c"
                        + " where c.duration = :duration"
                        + " and c.endDate is not null"
                        + " and c.endDate <= :limit"
                        + " and c.activeForPayrollGeneration = :active"
                        + " order by c.endDate asc")
                .setParameter("duration", ContractDuration.FIXED_TERM)
                .setParameter("limit", startOfDay(limit.getTime()))
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        return null == resultList ? new ArrayList<Contract>() : resultList;
    }

    @SuppressWarnings({"unchecked"})
    private ContractState findInactiveState() {
        List<ContractState> states = getEntityManager()
                .createQuery("select o from ContractState o where upper(o.name) = :name")
                .setParameter("name", INACTIVE_STATE)
                .getResultList();
        return states.isEmpty() ? null : states.get(0);
    }

    private static Date startOfDay(Date date) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
