package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.PayrollGenerationCycleService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 1.0
 */
@Name("payrollGenerationCycleValidatorAction")
@Scope(ScopeType.PAGE)
@AutoCreate
public class PayrollGenerationCycleValidatorAction {
    @Logger
    protected Log log;
    @In
    protected Map<String, String> messages;
    @In
    protected FacesMessages facesMessages;
    @In
    protected PayrollGenerationCycleService payrollGenerationCycleService;

    public Boolean hasOfficialPayroll(PayrollGenerationCycle payrollGenerationCycle, List<JobContract> jobContractList) {
        Boolean hasOfficialPayroll = false;
        List<String> uniqueMessages = new ArrayList<String>();
        for (JobContract jobContract : jobContractList) {
            Boolean validation = getHasOfficialPayroll(payrollGenerationCycle, jobContract);
            if (validation) {
                String key = jobContract.getJob().getOrganizationalUnit().getBusinessUnit().getId() + Constants.HYPHEN_SEPARATOR + jobContract.getJob().getJobCategory().getId();
                if (!uniqueMessages.contains(key)) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "PayrollGenerationCycle.error.hasOfficial",
                            jobContract.getJob().getOrganizationalUnit().getBusinessUnit().getFullName(),
                            jobContract.getJob().getJobCategory().getFullName()
                    );
                    uniqueMessages.add(key);
                }
            }
            if (!hasOfficialPayroll && validation) {
                hasOfficialPayroll = validation;
            }
        }
        return hasOfficialPayroll;
    }

    public Boolean hasOfficialPayroll(PayrollGenerationCycle payrollGenerationCycle, JobContract jobContract) {
        Boolean hasOfficialPayroll = getHasOfficialPayroll(payrollGenerationCycle, jobContract);
        if (hasOfficialPayroll) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PayrollGenerationCycle.error.hasOfficial",
                    jobContract.getJob().getOrganizationalUnit().getBusinessUnit().getFullName(),
                    jobContract.getJob().getJobCategory().getFullName()
            );
        }
        return hasOfficialPayroll;
    }

    public Boolean getHasOfficialPayroll(PayrollGenerationCycle payrollGenerationCycle, JobContract jobContract) {
        return payrollGenerationCycleService.hasOfficialPayroll(payrollGenerationCycle, jobContract.getJob().getOrganizationalUnit().getBusinessUnit(), jobContract.getJob().getJobCategory());
    }

    public Boolean isNotGenerationBySalary(JobContract jobContract) {
        Boolean valid = PayrollGenerationType.GENERATION_BY_SALARY.equals(jobContract.getJob().getJobCategory().getPayrollGenerationType())
                || PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(jobContract.getJob().getJobCategory().getPayrollGenerationType());
        if (!valid) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "PayrollGenerationCycle.error.isNotGenerationBySalary",
                    jobContract.getContract().getEmployee().getFirstName(),
                    messages.get(jobContract.getJob().getJobCategory().getPayrollGenerationType().getResourceKey()),
                    messages.get(PayrollGenerationType.GENERATION_BY_SALARY.getResourceKey()),
                    messages.get(PayrollGenerationType.GENERATION_BY_PERIODSALARY.getResourceKey())
            );
        }
        return !valid;
    }

    public Boolean isNotGenerationBySalary(List<JobContract> jobContractList) {
        Boolean isNotGenerationBySalary = false;
        for (JobContract jobContract : jobContractList) {
            if (isNotGenerationBySalary(jobContract) && !isNotGenerationBySalary) {
                isNotGenerationBySalary = true;
            }
        }
        return isNotGenerationBySalary;
    }
}
