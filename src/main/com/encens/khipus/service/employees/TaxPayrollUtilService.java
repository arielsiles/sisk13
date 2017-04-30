package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.JobContract;

import javax.ejb.Local;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Local
public interface TaxPayrollUtilService extends GenericService {
    AFPRate getActiveAfpRate(AFPRateType afpRateType);

    CNSRate getActiveCnsRate();

    IVARate getActiveIvaRate();

    SMNRate getActiveSmnRate();

    SeniorityBonus getActiveSeniorityBonus();

    List<JobContract> getActiveJobContracts(List<Long> identifiers);

    List<JobContract> getActiveJobContracts(ConfigurationTaxPayroll configuration);

    List<ExtraHoursWorked> getExtraHoursWorkedList(PayrollGenerationCycle payrollGenerationCycle);

    GeneratedPayroll getTaxPayrollGeneratedInstance(PayrollGenerationCycle payrollGenerationCycle);

    TaxPayrollGenerated getOfficialTaxPayrollGenerated(PayrollGenerationCycle payrollGenerationCycle,
                                                       TaxPayrollGeneratedType type, EntityManager entityManager);

    Boolean existOfficialTaxPayrollGenerated(PayrollGenerationCycle payrollGenerationCycle,
                                             TaxPayrollGeneratedType type, EntityManager entityManager);

    Boolean existTaxPayrollGenerated(ConfigurationTaxPayroll configuration);

    Boolean existsInvoiceForms(PayrollGenerationCycle payrollGenerationCycle);

    List<ManagersPayroll> getManagersPayrolls(ConfigurationTaxPayroll configurationTaxPayroll);

    String getManagersPayrollCacheKey(Employee employee, JobCategory jobCategory);

    DiscountRule findActiveNationalSolidaryAfpDiscountRule();
}
