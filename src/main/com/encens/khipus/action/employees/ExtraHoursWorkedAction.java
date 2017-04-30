package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.ExtraHoursWorked;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.ExtraHoursWorkedService;
import com.encens.khipus.service.employees.JobContractService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

/**
 * Extra hours worked action class
 *
 * @author
 * @version 2.26
 */
@Name("extraHoursWorkedAction")
@Scope(ScopeType.CONVERSATION)
public class ExtraHoursWorkedAction extends GenericAction<ExtraHoursWorked> {

    @In
    private ExtraHoursWorkedService extraHoursWorkedService;
    @In
    private PayrollGenerationCycleAction payrollGenerationCycleAction;
    @In
    private JobContractService jobContractService;
    @In
    private PayrollGenerationCycleValidatorAction payrollGenerationCycleValidatorAction;
    @In(required = false, scope = ScopeType.EVENT)
    @Out(required = false, scope = ScopeType.EVENT)
    private Boolean extraHoursWorkedReadOnly;

    @Factory(value = "extraHoursWorked")
    public ExtraHoursWorked initInvoicesForm() {
        getInstance().setPayrollGenerationCycle(payrollGenerationCycleAction.getInstance());
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("ExtraHoursWorked.title");
    }

    @Override
    protected GenericService getService() {
        return extraHoursWorkedService;
    }

    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String newInstance() {
        setOp(OP_CREATE);
        getInstance();
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String select(ExtraHoursWorked instance) {
        setOp(OP_UPDATE);
        setInstance(instance);
        assignJobContract(instance.getJobContract());
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String create() {

        JobContract jobContract = getInstance().getJobContract();
        if (extraHoursWorkedService.exists(payrollGenerationCycleAction.getInstance(), jobContract)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ExtraHoursWorked.message.duplicated",
                    jobContract.getContract().getEmployee().getFullName(),
                    jobContract.getJob().getJobCategory().getFullName()
            );
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        if (payrollGenerationCycleValidatorAction.hasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract()) ||
                payrollGenerationCycleValidatorAction.isNotGenerationBySalary(getInstance().getJobContract())) {
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
        return super.create();
    }

    @Override
    @End(beforeRedirect = true)
    public String update() {
        if (payrollGenerationCycleValidatorAction.hasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract()) ||
                payrollGenerationCycleValidatorAction.isNotGenerationBySalary(getInstance().getJobContract())) {
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
        return super.update();
    }

    @Override
    @End(beforeRedirect = true)
    public String delete() {
        if (payrollGenerationCycleValidatorAction.hasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract())) {
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
        return super.delete();
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public void assignJobContract(JobContract jobContract) {
        getInstance().setJobContract(jobContractService.load(jobContract));
    }

    public void clearJobContract() {
        //noinspection NullableProblems
        getInstance().setJobContract(null);
    }

    public Boolean getReadOnly() {
        if (extraHoursWorkedReadOnly == null) {
            extraHoursWorkedReadOnly = isManaged() && payrollGenerationCycleValidatorAction.getHasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract());
        }
        return extraHoursWorkedReadOnly;
    }
}
