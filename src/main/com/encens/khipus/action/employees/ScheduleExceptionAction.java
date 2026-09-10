package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.ScheduleException;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.service.employees.ContractService;
import com.encens.khipus.service.employees.ScheduleExceptionService;
import com.encens.khipus.service.employees.ScheduleLockService;
import com.encens.khipus.service.employees.WorkShiftService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.List;

/**
 * Alta y baja de excepciones de horario.
 * <p/>
 * La excepcion se guarda contra el contrato, pero se carga eligiendo a la persona: quien la
 * registra piensa en gente, no en numeros de contrato. El contrato se resuelve por la fecha de
 * la excepcion, que es la unica lectura correcta cuando alguien tuvo mas de un contrato.
 *
 * @author
 * @version 6.1.0
 */
@Name("scheduleExceptionAction")
@Scope(ScopeType.CONVERSATION)
public class ScheduleExceptionAction extends GenericAction<ScheduleException> {

    @In
    private ScheduleExceptionService scheduleExceptionService;

    @In
    private ContractService contractService;

    @In
    private WorkShiftService workShiftService;

    @In
    private ScheduleLockService scheduleLockService;

    private Employee employee;

    @Factory(value = "scheduleException")
    @Restrict("#{s:hasPermission('SCHEDULEEXCEPTION','VIEW')}")
    public ScheduleException initScheduleException() {
        return getInstance();
    }

    public List<WorkShift> getShifts() {
        return workShiftService.findActiveList();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('SCHEDULEEXCEPTION','VIEW')}")
    public String select(ScheduleException instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(ScheduleException.class, instance.getId()));
            employee = getInstance().getContract().getEmployee();
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SCHEDULEEXCEPTION','CREATE')}")
    public String create() {
        return validate() ? super.create() : Outcome.REDISPLAY;
    }

    @Override
    @Restrict("#{s:hasPermission('SCHEDULEEXCEPTION','UPDATE')}")
    public String update() {
        return validate() ? super.update() : Outcome.REDISPLAY;
    }

    @Override
    @Restrict("#{s:hasPermission('SCHEDULEEXCEPTION','DELETE')}")
    public String delete() {
        if (scheduleLockService.isClosed(getInstance().getDate())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Schedule.error.closedPeriod", scheduleLockService.lastClosedDate());
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    /**
     * Resuelve el contrato por la fecha y rechaza el duplicado. Sin la validacion del duplicado
     * quedarian dos excepciones para el mismo dia y el resolutor tomaria cualquiera de las dos.
     */
    private boolean validate() {
        ScheduleException instance = getInstance();
        if (null == employee) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ScheduleException.error.employeeRequired");
            return false;
        }
        if (null == instance.getDate()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ScheduleException.error.dateRequired");
            return false;
        }
        /* Un dia ya pagado no se toca: la excepcion cambiaria hacia atras el calculo. */
        if (scheduleLockService.isClosed(instance.getDate())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Schedule.error.closedPeriod", scheduleLockService.lastClosedDate());
            return false;
        }

        List<Contract> contracts = contractService.getContractsByEmployeeInDateRange(
                employee, instance.getDate(), instance.getDate());
        if (contracts.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ScheduleException.error.withoutContract", employee.getFullName());
            return false;
        }
        instance.setContract(contracts.get(0));

        ScheduleException duplicated = scheduleExceptionService.find(
                instance.getContract(), instance.getDate());
        if (null != duplicated && !duplicated.getId().equals(instance.getId())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ScheduleException.error.duplicated", employee.getFullName());
            return false;
        }
        return true;
    }

    public void assignEmployee(Employee selected) {
        this.employee = selected;
    }

    public void clearEmployee() {
        this.employee = null;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    protected GenericService getService() {
        return scheduleExceptionService;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "reason";
    }
}
