package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Bonus;
import com.encens.khipus.model.employees.GrantedBonus;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.GrantedBonusService;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author
 * @version 3.4
 */
@Name("grantedBonusCreateAction")
@Scope(ScopeType.CONVERSATION)
public class GrantedBonusCreateAction extends GenericAction<GrantedBonus> {

    @In
    private GrantedBonusService grantedBonusService;
    @In(required = false, value = "payrollGenerationCycleAction")
    private PayrollGenerationCycleAction payrollGenerationCycleAction;
    @In(required = false)
    private GrantedBonusDataModel grantedBonusDataModel;
    /* indicates which kind of bonus is been added*/
    private Bonus bonus;
    /* they are used in transference action when it is required a list of selected fixed asset to apply the transfer operation*/
    private List<JobContract> selectedJobContractList;

    private List<Long> selectedJobContractIdList;
    /* map to manage the amount of each bonus*/
    private HashMap<JobContract, GrantedBonus> jobContractBonusHashMap;
    @In
    private JobContractService jobContractService;
    @In
    private PayrollGenerationCycleValidatorAction payrollGenerationCycleValidatorAction;

    @Create
    public void atCreateTime() {
        if (!isManaged()) {
            selectedJobContractList = new ArrayList<JobContract>();
            selectedJobContractIdList = new ArrayList<Long>();
            jobContractBonusHashMap = new HashMap<JobContract, GrantedBonus>();
        }
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    public String addGrantedBonus() {
        /* to create the new GrantedBonus instance*/
        setInstance(null);
        setOp(OP_CREATE);
        getInstance().setPayrollGenerationCycle(payrollGenerationCycleAction.getInstance());
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('GRANTEDBONUS','CREATE')}")
    public String create() {
        if (payrollGenerationCycleValidatorAction.hasOfficialPayroll(payrollGenerationCycleAction.getInstance(), selectedJobContractList)
                || payrollGenerationCycleValidatorAction.isNotGenerationBySalary(selectedJobContractList)) {
            return Outcome.FAIL;
        }
        grantedBonusService.createGrantedBonus(getJobContractBonusHashMap(), selectedJobContractList);
        addCreatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("GrantedBonus.title");
    }

    @Override
    protected GenericService getService() {
        return grantedBonusService;
    }

    public void bonusChanged() {
        selectedJobContractList.clear();
        selectedJobContractIdList.clear();
        jobContractBonusHashMap.clear();
        if (null != grantedBonusDataModel) {
            grantedBonusDataModel.clear();
        }
    }

    public void addJobContractList(List<Long> jobContractIdList) {
        if (!ValidatorUtil.isEmptyOrNull(jobContractIdList)) {
            List<JobContract> jobContractList = jobContractService.load(jobContractIdList);
            for (JobContract jobContract : jobContractList) {
                if (!getSelectedJobContractList().contains(jobContract)) {
                    getSelectedJobContractList().add(jobContract);
                    getSelectedJobContractIdList().add(jobContract.getId());
                    GrantedBonus grantedBonus = new GrantedBonus();
                    grantedBonus.setAmount(getBonus().getAmount());
                    grantedBonus.setBonus(getBonus());
                    grantedBonus.setPayrollGenerationCycle(getPayrollGenerationCycle());
                    grantedBonus.setJobContract(jobContract);
                    jobContractBonusHashMap.put(jobContract, grantedBonus);
                }
            }
        }
    }

    public void removeInstance(JobContract jobContractItem) {
        JobContract jobContract;
        try {
            jobContract = getService().findById(JobContract.class, jobContractItem.getId());
            if (getSelectedJobContractList().contains(jobContract)) {
                getSelectedJobContractList().remove(jobContract);
                getSelectedJobContractIdList().remove(jobContract.getId());
                jobContractBonusHashMap.remove(jobContract);
            }
        } catch (EntryNotFoundException e) {
            log.error(e, "Entry not found");
        }
    }

    /* getters and setters */

    public List<JobContract> getSelectedJobContractList() {
        if (!isManaged() && null == selectedJobContractList) {
            selectedJobContractList = new ArrayList<JobContract>();
        }
        return selectedJobContractList;
    }

    public void setSelectedJobContractList(List<JobContract> selectedJobContractList) {
        this.selectedJobContractList = selectedJobContractList;
    }

    public List<Long> getSelectedJobContractIdList() {
        if (selectedJobContractIdList.isEmpty()) {
            selectedJobContractIdList.add((long) 0);
        }
        return selectedJobContractIdList;
    }

    public void setSelectedJobContractIdList(List<Long> selectedJobContractIdList) {
        this.selectedJobContractIdList = selectedJobContractIdList;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycleAction.getInstance();
    }

    public HashMap<JobContract, GrantedBonus> getJobContractBonusHashMap() {
        return jobContractBonusHashMap;
    }

    public void setJobContractBonusHashMap(HashMap<JobContract, GrantedBonus> jobContractBonusHashMap) {
        this.jobContractBonusHashMap = jobContractBonusHashMap;
    }

    public Bonus getBonus() {
        return bonus;
    }

    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
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

    protected void addAlreadyExistOfficialTributaryTaxPayrollGeneratedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "GrantedBonus.error.alreadyExistOfficialTributaryTaxPayrollGeneratedMessage");
    }

}