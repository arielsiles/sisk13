package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.GrantedBonusNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GrantedBonus;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.GrantedBonusService;
import com.encens.khipus.service.employees.JobContractService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;


/**
 * @author
 * @version 3.4
 */
@Name("grantedBonusAction")
@Scope(ScopeType.CONVERSATION)
public class GrantedBonusAction extends GenericAction<GrantedBonus> {

    @In
    private GrantedBonusService grantedBonusService;
    @In(required = false, value = "payrollGenerationCycleAction")
    private PayrollGenerationCycleAction payrollGenerationCycleAction;
    @In
    private JobContractService jobContractService;
    @In
    private PayrollGenerationCycleValidatorAction payrollGenerationCycleValidatorAction;
    @In(required = false, scope = ScopeType.EVENT)
    @Out(required = false, scope = ScopeType.EVENT)
    private Boolean grantedBonusReadOnly;

    @Create
    public void atCreateTime() {
        if (!isManaged()) {
            getInstance().setPayrollGenerationCycle(getPayrollGenerationCycle());
        }
    }

    @Factory(value = "grantedBonus", scope = ScopeType.STATELESS)
    public GrantedBonus initGrantedBonus() {
        return getInstance();
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('GRANTEDBONUS','VIEW')}")
    public String select(GrantedBonus instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(instance);
            /*refresh the instance from database*/
            setInstance(getService().findById(getEntityClass(), getId(instance)));
            assignJobContract(instance.getJobContract());
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('GRANTEDBONUS','UPDATE')}")
    public String update() {
        try {
            if (payrollGenerationCycleValidatorAction.hasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract()) ||
                    payrollGenerationCycleValidatorAction.isNotGenerationBySalary(getInstance().getJobContract())) {
                return com.encens.khipus.framework.action.Outcome.FAIL;
            }
            grantedBonusService.updatePayrollGenerationCycle(getInstance());
            addUpdatedMessage();
        } catch (GrantedBonusNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                getService().findById(GrantedBonus.class, getInstance().getId(), true);
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('GRANTEDBONUS','DELETE')}")
    public String delete() {
        try {
            if (payrollGenerationCycleValidatorAction.hasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract())) {
                return com.encens.khipus.framework.action.Outcome.FAIL;
            }
            grantedBonusService.deletePayrollGenerationCycle(getInstance());
            addDeletedMessage();
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (GrantedBonusNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("GrantedBonus.grantedBonus");
    }

    @Override
    protected GenericService getService() {
        return grantedBonusService;
    }

    public void bonusChanged() {
    }

    public void assignJobContract(JobContract jobContract) {
        getInstance().setJobContract(jobContractService.load(jobContract));
    }

    public void clearJobContract() {
        getInstance().setJobContract(null);
    }

    /* getters and setters */

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycleAction.getInstance();
    }

    public Boolean getReadOnly() {
        if (grantedBonusReadOnly == null) {
            grantedBonusReadOnly = isManaged() && payrollGenerationCycleValidatorAction.getHasOfficialPayroll(payrollGenerationCycleAction.getInstance(), getInstance().getJobContract());
        }
        return grantedBonusReadOnly;
    }

/* messages */

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "GrantedBonus.message.created");
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "GrantedBonus.message.deleted");
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "GrantedBonus.message.updated");
    }

    protected void addOnUpdateAlreadyExistOfficialTributaryTaxPayrollGeneratedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "GrantedBonus.error.onUpdateAlreadyExistOfficialTributaryTaxPayrollGeneratedMessage");
    }

    protected void addOnDeleteAlreadyExistOfficialTributaryTaxPayrollGeneratedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "GrantedBonus.error.onDeleteAlreadyExistOfficialTributaryTaxPayrollGeneratedMessage");
    }
}