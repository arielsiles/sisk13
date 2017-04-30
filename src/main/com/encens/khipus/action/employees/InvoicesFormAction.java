package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.InvoicesForm;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.InvoicesFormService;
import com.encens.khipus.service.employees.JobContractService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Invoices form action class
 *
 * @author
 * @version 2.26
 */
@Name("invoicesFormAction")
@Scope(ScopeType.CONVERSATION)
public class InvoicesFormAction extends GenericAction<InvoicesForm> {

    @In
    private InvoicesFormService invoicesFormService;
    @In
    private PayrollGenerationCycleAction payrollGenerationCycleAction;
    @In
    private JobContractService jobContractService;
    @In
    private PayrollGenerationCycleValidatorAction payrollGenerationCycleValidatorAction;

    @In(required = false, scope = ScopeType.EVENT)
    @Out(required = false, scope = ScopeType.EVENT)
    private Boolean invoicesFormReadOnly;

    @Factory(value = "invoicesForm")
    public InvoicesForm initInvoicesForm() {
        getInstance().setPayrollGenerationCycle(payrollGenerationCycleAction.getInstance());
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("InvoicesForm.title");
    }

    @Override
    protected GenericService getService() {
        return invoicesFormService;
    }

    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String newInstance() {
        setOp(OP_CREATE);
        getInstance();
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String select(InvoicesForm instance) {
        setOp(OP_UPDATE);
        setInstance(instance);
        assignJobContract(instance.getJobContract());
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String create() {

        List<JobContract> jobContracts = new ArrayList<JobContract>();
        jobContracts.add(getInstance().getJobContract());
        if (invoicesFormService.exists(payrollGenerationCycleAction.getInstance(), jobContracts)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "InvoicesForm.message.duplicated");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        if (payrollGenerationCycleValidatorAction.hasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract()) ||
                payrollGenerationCycleValidatorAction.isNotGenerationBySalary(getInstance().getJobContract())) {
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
        getInstance().setEmployee(getInstance().getJobContract().getContract().getEmployee());
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
        getInstance().setJobContract(null);
    }

    public Boolean getReadOnly() {
        if (invoicesFormReadOnly == null) {
            invoicesFormReadOnly = isManaged() && payrollGenerationCycleValidatorAction.getHasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract());
        }
        return invoicesFormReadOnly;
    }
}
