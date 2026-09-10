package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.service.employees.ContractScheduleService;
import com.encens.khipus.service.employees.GroupScheduleService;
import com.encens.khipus.service.employees.ScheduleExceptionService;
import com.encens.khipus.service.employees.ScheduleLockService;
import com.encens.khipus.service.employees.WorkShiftService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * CRUD del catalogo de turnos.
 *
 * @author
 * @version 6.1.0
 */
@Name("workShiftAction")
@Scope(ScopeType.CONVERSATION)
public class WorkShiftAction extends GenericAction<WorkShift> {

    @In
    private WorkShiftService workShiftService;

    @In
    private GroupScheduleService groupScheduleService;

    @In
    private ContractScheduleService contractScheduleService;

    @In
    private ScheduleExceptionService scheduleExceptionService;

    @In
    private ScheduleLockService scheduleLockService;

    @Factory(value = "workShift")
    @Restrict("#{s:hasPermission('WORKSHIFT','VIEW')}")
    public WorkShift initWorkShift() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WORKSHIFT','VIEW')}")
    public String select(WorkShift instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(WorkShift.class, instance.getId()));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('WORKSHIFT','CREATE')}")
    public String create() {
        return validate() ? super.create() : Outcome.REDISPLAY;
    }

    /**
     * Un turno que ya se uso en un periodo pagado no se edita.
     * <p/>
     * Cambiarle las horas cambiaria hacia atras el calculo de faltas y atrasos de una planilla
     * que alguien ya reviso y firmo, y sin dejar rastro. Lo correcto es desactivarlo y crear
     * uno nuevo con las horas nuevas: asi la historia queda como fue.
     */
    public boolean isLocked() {
        return isManaged() && scheduleLockService.isWorkShiftLocked(getInstance());
    }

    /** Desactivar si se puede aunque este bloqueado: es como se retira un turno viejo. */
    @Restrict("#{s:hasPermission('WORKSHIFT','UPDATE')}")
    public String deactivate() {
        getInstance().setActive(Boolean.FALSE);
        return super.update();
    }

    @Override
    @Restrict("#{s:hasPermission('WORKSHIFT','UPDATE')}")
    public String update() {
        if (isLocked()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkShift.error.locked", getInstance().getName());
            return Outcome.REDISPLAY;
        }
        return validate() ? super.update() : Outcome.REDISPLAY;
    }

    @Override
    @Restrict("#{s:hasPermission('WORKSHIFT','DELETE')}")
    public String delete() {
        /* Un turno usado en el cronograma no se borra: al hacerlo se perderia con que jornada
           se evaluaron esos dias. Se desactiva, y deja de ofrecerse en la paleta. */
        Long used = groupScheduleService.countByWorkShift(getInstance())
                + contractScheduleService.countByWorkShift(getInstance())
                + scheduleExceptionService.countByWorkShift(getInstance());
        if (used > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkShift.error.inUse", getInstance().getName(), used);
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    private boolean validate() {
        WorkShift instance = getInstance();
        Long duplicated = workShiftService.countByName(instance.getName(),
                isManaged() ? instance.getId() : null);
        if (null != duplicated && duplicated > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.message.duplicated", instance.getName());
            return false;
        }
        /* Un turno de cero minutos no es un turno. Como el cruce de medianoche se deduce de que
           el fin no sea posterior al inicio, dos horas iguales darian 24 horas en vez de cero,
           que es peor que rechazarlo. */
        if (instance.getDurationMinutes() == 24 * 60) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkShift.error.sameHours");
            return false;
        }
        return true;
    }

    @Override
    protected GenericService getService() {
        return workShiftService;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
