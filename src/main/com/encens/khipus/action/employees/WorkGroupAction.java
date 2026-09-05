package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.service.employees.ContractService;
import com.encens.khipus.service.employees.WorkGroupService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.Date;
import java.util.List;

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

    /* Alta de un integrante: se elige la persona y desde cuando. */
    private Employee newMember;
    private Date newMemberStartDate = new Date();

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
     * Saca a alguien del grupo cerrando su pertenencia, no borrandola: los dias anteriores se
     * siguieron evaluando contra este grupo y esa historia no se toca.
     */
    @Restrict("#{s:hasPermission('WORKGROUP','UPDATE')}")
    public void closeMember(WorkGroupMembership membership) {
        try {
            membership.setEndDate(new Date());
            workGroupService.update(membership);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "WorkGroup.info.memberClosed");
        } catch (Exception e) {
            log.error("No se pudo cerrar la pertenencia al grupo", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WorkGroup.error.closeMemberFailed");
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
