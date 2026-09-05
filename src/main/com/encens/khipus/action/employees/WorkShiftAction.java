package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.WorkShift;
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

    @Override
    @Restrict("#{s:hasPermission('WORKSHIFT','UPDATE')}")
    public String update() {
        return validate() ? super.update() : Outcome.REDISPLAY;
    }

    @Override
    @Restrict("#{s:hasPermission('WORKSHIFT','DELETE')}")
    public String delete() {
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
