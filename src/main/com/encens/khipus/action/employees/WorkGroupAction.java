package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.action.employees.dto.GroupCandidate;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.model.employees.WorkGroupMembershipType;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.service.employees.ContractService;
import com.encens.khipus.service.employees.ScheduleLockService;
import com.encens.khipus.service.employees.WorkGroupService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD de grupos de trabajo y de quien los integra.
 *
 * @author
 * @version 6.1.0
 */
@Name("workGroupAction")
@Scope(ScopeType.CONVERSATION)
public class WorkGroupAction extends GenericAction<WorkGroup> {

    @In
    private WorkGroupService workGroupService;

    @In
    private ContractService contractService;

    @In
    private ScheduleLockService scheduleLockService;

    /* Alta de un integrante: se elige la persona y desde cuando.
       Las fechas arrancan VACIAS a proposito. Con la de hoy puesta por defecto, nadie la
       cambia y la pertenencia queda con una fecha que no es la real; y la fecha es lo que
       decide contra que cronograma se evalua cada dia. Obligar a elegirla es mas seguro
       que adivinar. */
    private Employee newMember;
    private Date newMemberStartDate;

    /* Asignacion masiva: su propia fecha y la lista de quienes ese dia estaban libres.
       La fecha es aparte de la del alta de a uno a proposito: son dos formularios distintos
       -uno en la pagina y otro en el modal- y compartir el campo dejaba a la vista dos fechas
       que decian cosas distintas. */
    private Date bulkStartDate;
    private List<GroupCandidate> candidates;

    /* Se movio a alguien en el ultimo intento. El modal se cierra solo cuando fue asi: si no
       habia nadie tildado tiene que quedar abierto para poder tildar. */
    private boolean bulkApplied = false;

    /* Prestamo: alguien de otro grupo viene por unos dias.
       Se elige de una lista acotada a quienes pertenecen a OTRO grupo esa fecha: prestar a
       alguien que no esta en ningun grupo no significa nada, y el buscador general de
       empleados ofrecia a los 245. */
    private Long loanContractId;
    private String loanMemberName;
    /* Mes en el que se abre el calendario de "Hasta". Sin esto se abre en el mes actual, y
       elegir el dia 23 para un prestamo de julio devolvia el 23 de septiembre. */
    private Date loanCalendarMonth;
    private String loanFilter;
    private boolean loanApplied = false;
    private Date loanStartDate;
    private Date loanEndDate;
    private String loanReason;

    /* Cierre de una pertenencia, con la fecha que corresponda y no la de hoy. */
    private WorkGroupMembership closingMembership;
    private Date closingDate;

    /* Hasta cuando esta cerrado por una planilla oficial. Se consulta una vez: no cambia
       mientras alguien edita un grupo, y preguntarlo por fila serian decenas de consultas. */
    private Date closedUntil;
    private boolean closedUntilLoaded = false;

    @Factory(value = "workGroup")
    @Restrict("#{s:hasPermission('WORKGROUP','VIEW')}")
    public WorkGroup initWorkGroup() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WORKGROUP','VIEW')}")
    public String select(WorkGroup instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(WorkGroup.class, instance.getId()));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('WORKGROUP','CREATE')}")
    public String create() {
        return validate() ? super.create() : Outcome.REDISPLAY;
    }

    @Override
    @Restrict("#{s:hasPermission('WORKGROUP','UPDATE')}")
    public String update() {
        return validate() ? super.update() : Outcome.REDISPLAY;
    }

    @Override
    @Restrict("#{s:hasPermission('WORKGROUP','DELETE')}")
    public String delete() {
        if (!workGroupService.findMemberships(getInstance()).isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.hasMembers", getInstance().getName());
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    private boolean validate() {
        Long duplicated = workGroupService.countByName(getInstance().getName(),
                isManaged() ? getInstance().getId() : null);
        if (null != duplicated && duplicated > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.message.duplicated", getInstance().getName());
            return false;
        }
        return true;
    }

    public List<WorkGroupMembership> getMemberships() {
        return workGroupService.findMemberships(getInstance());
    }

    /**
     * Suma una persona al grupo. Se rechaza si ese dia ya pertenecia a otro grupo: nadie puede
     * estar en dos a la vez, o no habria forma de saber contra que cronograma evaluarlo.
     */
    @Restrict("#{s:hasPermission('WORKGROUP','UPDATE')}")
    public void addMember() {
        if (null == newMember) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.memberRequired");
            return;
        }
        if (null == newMemberStartDate) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.startDateRequired");
            return;
        }
        Contract contract = currentContractOf(newMember);
        if (null == contract) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.withoutContract", newMember.getFullName());
            return;
        }

        WorkGroupMembership membership = new WorkGroupMembership();
        membership.setWorkGroup(getInstance());
        membership.setContract(contract);
        membership.setStartDate(newMemberStartDate);
        membership.setType(WorkGroupMembershipType.BASE);

        WorkGroupMembership overlapping = workGroupService.findOverlapping(membership);
        if (null != overlapping) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.alreadyInGroup", newMember.getFullName(),
                    overlapping.getWorkGroup().getName());
            return;
        }

        try {
            workGroupService.create(membership);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WorkGroup.info.memberAdded", newMember.getFullName());
            newMember = null;
        } catch (Exception e) {
            log.error("No se pudo agregar el integrante al grupo", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.addMemberFailed");
        }
    }

    /**
     * Una pertenencia se puede BORRAR mientras no cubra dias ya pagados.
     * <p/>
     * Cerrar sirve para quien de verdad estuvo en el grupo y se fue. No sirve para deshacer un
     * alta equivocada: cerrarla el mismo dia deja una pertenencia de un dia que despues estorba
     * -se pisa con la pertenencia correcta- y no habia forma de sacarla. Eso era un callejon
     * sin salida.
     */
    public boolean isRemovable(WorkGroupMembership membership) {
        if (null == membership || null == membership.getStartDate()) {
            return false;
        }
        if (!closedUntilLoaded) {
            closedUntil = scheduleLockService.lastClosedDate();
            closedUntilLoaded = true;
        }
        return null == closedUntil || membership.getStartDate().after(closedUntil);
    }

    /** Borra una pertenencia que no alcanza a ningun periodo cerrado. Es deshacer, no dar de baja. */
    @Restrict("#{s:hasPermission('WORKGROUP','UPDATE')}")
    public void removeMember(WorkGroupMembership membership) {
        if (!isRemovable(membership)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.notRemovable");
            return;
        }
        try {
            workGroupService.delete(membership);
            candidates = null;
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WorkGroup.info.memberRemoved");
        } catch (Exception e) {
            log.error("No se pudo quitar la pertenencia al grupo", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.removeMemberFailed");
        }
    }

    /**
     * Saca a alguien del grupo cerrando su pertenencia, no borrandola: los dias anteriores se
     * siguieron evaluando contra este grupo y esa historia no se toca.
     */
    /** Prepara el cierre. La fecha se elige: sacar a alguien hoy casi nunca es lo real. */
    public void prepareClose(WorkGroupMembership membership) {
        this.closingMembership = membership;
        this.closingDate = null;
    }

    @Restrict("#{s:hasPermission('WORKGROUP','UPDATE')}")
    public void closeMember() {
        if (null == closingMembership) {
            return;
        }
        if (null == closingDate) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.closeDateRequired");
            return;
        }
        if (scheduleLockService.isClosed(closingDate)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Schedule.error.closedPeriod", scheduleLockService.lastClosedDate());
            return;
        }
        try {
            workGroupService.closeMembership(closingMembership, closingDate);
            candidates = null;
            closingMembership = null;
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WorkGroup.info.memberClosed");
        } catch (Exception e) {
            log.error("No se pudo cerrar la pertenencia al grupo", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.closeMemberFailed");
        }
    }

    // -------------------------------------------------------------- prestamo

    /**
     * Presta a alguien de otro grupo por unos dias. La pertenencia base no se toca: vencido el
     * plazo la persona vuelve a su grupo sola, sin que nadie tenga que acordarse.
     */
    @Restrict("#{s:hasPermission('WORKGROUP','UPDATE')}")
    public void addLoan() {
        loanApplied = false;
        if (null == loanStartDate || null == loanEndDate) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.loanDatesRequired");
            return;
        }
        if (null == loanContractId) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.memberRequired");
            return;
        }
        if (loanEndDate.before(loanStartDate)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.loanBadRange");
            return;
        }
        if (null == loanReason || loanReason.trim().length() == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.loanReasonRequired");
            return;
        }
        if (scheduleLockService.isClosed(loanStartDate)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Schedule.error.closedPeriod", scheduleLockService.lastClosedDate());
            return;
        }
        try {
            Contract contract = workGroupService.findById(Contract.class, loanContractId);

            WorkGroupMembership loan = new WorkGroupMembership();
            loan.setWorkGroup(getInstance());
            loan.setContract(contract);
            loan.setStartDate(loanStartDate);
            loan.setEndDate(loanEndDate);
            loan.setType(WorkGroupMembershipType.LOAN);
            loan.setReason(loanReason);

            /* Solo se compara contra otros prestamos: pisar a la pertenencia base es el punto. */
            WorkGroupMembership overlapping = workGroupService.findOverlapping(loan);
            if (null != overlapping) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WorkGroup.error.alreadyLoaned",
                        contract.getEmployee().getFullName(),
                        overlapping.getWorkGroup().getName());
                return;
            }
            workGroupService.create(loan);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WorkGroup.info.loanAdded", contract.getEmployee().getFullName());
            loanApplied = true;
            clearLoanMember();
            loanReason = null;
        } catch (Exception e) {
            log.error("No se pudo registrar el prestamo al grupo", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.addMemberFailed");
        }
    }

    /**
     * Los que se pueden prestar: quienes pertenecen a OTRO grupo en la fecha del prestamo.
     * <p/>
     * Se busca en un modal y no en una lista desplegable: con cien personas un combo no se
     * puede usar. Y se acota a los de otros grupos porque prestar a alguien que no esta en
     * ninguno no significa nada.
     */
    public List<GroupCandidate> getLoanCandidateList() {
        List<GroupCandidate> result = new ArrayList<GroupCandidate>();
        if (null == loanStartDate) {
            return result;
        }
        String filter = null == loanFilter ? "" : loanFilter.trim().toLowerCase();
        for (WorkGroupMembership membership : workGroupService.findBaseMembershipsAt(loanStartDate)) {
            if (membership.getWorkGroup().getId().equals(getInstance().getId())) {
                continue;
            }
            String name = membership.getContract().getEmployee().getFullName();
            if (filter.length() > 0 && (null == name || !name.toLowerCase().contains(filter))) {
                continue;
            }
            result.add(new GroupCandidate(membership.getContract(),
                    membership.getWorkGroup().getName()));
        }
        return result;
    }

    public void selectLoanCandidate(GroupCandidate candidate) {
        this.loanContractId = candidate.getContract().getId();
        this.loanMemberName = candidate.getContract().getEmployee().getFullName();
    }

    public void clearLoanMember() {
        this.loanContractId = null;
        this.loanMemberName = null;
    }

    /** Se limpia al cambiar la fecha: la persona elegida puede ya no ser candidata. */
    public void refreshLoanCandidates() {
        clearLoanMember();
        this.loanFilter = null;
        /* El calendario de "Hasta" pasa a abrirse en el mes del "Desde". */
        this.loanCalendarMonth = loanStartDate;
    }

    public Date getLoanCalendarMonth() {
        return null != loanCalendarMonth ? loanCalendarMonth : loanStartDate;
    }

    public void setLoanCalendarMonth(Date loanCalendarMonth) {
        this.loanCalendarMonth = loanCalendarMonth;
    }

    public String getLoanMemberName() {
        return loanMemberName;
    }

    public String getLoanFilter() {
        return loanFilter;
    }

    public void setLoanFilter(String loanFilter) {
        this.loanFilter = loanFilter;
    }

    public boolean isLoanDateMissing() {
        return null == loanStartDate;
    }

    public boolean isLoanApplied() {
        return loanApplied;
    }



    /**
     * Los candidatos a entrar al grupo: contratos vigentes en la fecha elegida que ese dia no
     * pertenecian a ningun grupo. Se filtran antes y no despues porque ofrecer a alguien que va
     * a ser rechazado por solapamiento hace perder el tiempo dos veces.
     */
    public List<GroupCandidate> getCandidates() {
        if (null == bulkStartDate) {
            return new ArrayList<GroupCandidate>();
        }
        if (null == candidates) {
            candidates = new ArrayList<GroupCandidate>();
            Map<Long, String> groupByContract = new HashMap<Long, String>();
            for (WorkGroupMembership membership
                    : workGroupService.findBaseMembershipsAt(bulkStartDate)) {
                groupByContract.put(membership.getContract().getId(),
                        membership.getWorkGroup().getName());
            }
            for (Contract contract : workGroupService.findContractsForMove(bulkStartDate)) {
                String current = groupByContract.get(contract.getId());
                /* Los que ya estan en ESTE grupo no se ofrecen: no hay nada que hacer. */
                if (null != current && current.equals(getInstance().getName())) {
                    continue;
                }
                candidates.add(new GroupCandidate(contract, current));
            }
        }
        return candidates;
    }

    /** Todavia no se eligio la fecha: la lista no se puede armar. */
    public boolean isBulkDateMissing() {
        return null == bulkStartDate;
    }

    public boolean isBulkApplied() {
        return bulkApplied;
    }

    public int getCandidateCount() {
        return getCandidates().size();
    }

    /**
     * Rearma la lista. Se llama SOLO desde el calendario del modal, nunca desde un setter.
     * <p/>
     * Hacerlo en el setter fue un error: la fecha se actualiza antes que la tabla en el mismo
     * envio, asi que al tildar y aceptar la lista se reconstruia a mitad de camino y los tildes
     * se perdian -"no habia nadie tildado" con las casillas marcadas en pantalla-.
     */
    public void refreshCandidates() {
        candidates = null;
    }

    /**
     * Suma de una vez a todos los tildados. Cada uno se agrega por separado y se sigue con el
     * resto si alguno falla: que una fila con un problema puntual no tire abajo las otras
     * cuarenta.
     */
    /**
     * Agrega o mueve a los tildados. Quien venia de otro grupo queda cerrado ahi el dia
     * anterior: una reorganizacion pasa de treinta clics a uno, y cada movimiento queda con
     * su fecha, asi que la historia se reconstruye sola.
     */
    @Restrict("#{s:hasPermission('WORKGROUP','UPDATE')}")
    public void addSelectedMembers() {
        bulkApplied = false;
        if (null == bulkStartDate) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.startDateRequired");
            return;
        }
        if (scheduleLockService.isClosed(bulkStartDate)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Schedule.error.closedPeriod", scheduleLockService.lastClosedDate());
            return;
        }
        List<Contract> selected = new ArrayList<Contract>();
        for (GroupCandidate candidate : getCandidates()) {
            if (candidate.isSelected()) {
                selected.add(candidate.getContract());
            }
        }
        if (selected.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "WorkGroup.warn.noneSelected");
            return;
        }
        int moved = workGroupService.moveMembers(selected, getInstance(), bulkStartDate);
        candidates = null;
        bulkApplied = moved > 0;
        if (moved > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WorkGroup.info.bulkAdded", moved);
        }
        if (moved < selected.size()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.bulkFailed", selected.size() - moved);
        }
    }

    private Contract currentContractOf(Employee employee) {
        List<Contract> contracts = contractService.getContractsByEmployeeInDateRange(
                employee, newMemberStartDate, newMemberStartDate);
        return contracts.isEmpty() ? null : contracts.get(0);
    }

    public void assignNewMember(Employee employee) {
        this.newMember = employee;
    }

    public void clearNewMember() {
        this.newMember = null;
    }

    public Employee getNewMember() {
        return newMember;
    }

    public void setNewMember(Employee newMember) {
        this.newMember = newMember;
    }

    public Long getLoanContractId() {
        return loanContractId;
    }

    public void setLoanContractId(Long loanContractId) {
        this.loanContractId = loanContractId;
    }

    public Date getLoanStartDate() {
        return loanStartDate;
    }

    public void setLoanStartDate(Date loanStartDate) {
        this.loanStartDate = loanStartDate;
    }

    public Date getLoanEndDate() {
        return loanEndDate;
    }

    public void setLoanEndDate(Date loanEndDate) {
        this.loanEndDate = loanEndDate;
    }

    public String getLoanReason() {
        return loanReason;
    }

    public void setLoanReason(String loanReason) {
        this.loanReason = loanReason;
    }

    public WorkGroupMembership getClosingMembership() {
        return closingMembership;
    }

    public Date getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(Date closingDate) {
        this.closingDate = closingDate;
    }

    public Date getBulkStartDate() {
        return bulkStartDate;
    }

    public void setBulkStartDate(Date bulkStartDate) {
        this.bulkStartDate = bulkStartDate;
    }

    public Date getNewMemberStartDate() {
        return newMemberStartDate;
    }

    public void setNewMemberStartDate(Date newMemberStartDate) {
        this.newMemberStartDate = newMemberStartDate;
    }

    @Override
    protected GenericService getService() {
        return workGroupService;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
