package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.SocialWelfareEntity;
import com.encens.khipus.util.employees.TributaryPayrollCalculateResult;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 3.5
 */
@Local
public interface TributaryPayrollService extends GenericService {
    List<TributaryPayrollCalculateResult> sumLaboralPensionFundRetentionGroupingByCostCenter(PayrollGenerationCycle payrollGenerationCycle, SocialWelfareEntity pensionFundOrganization);

    List<TributaryPayrollCalculateResult> sumPatronalPensionFundRetentionGroupingByCostCenter(PayrollGenerationCycle payrollGenerationCycle, SocialWelfareEntity pensionFundOrganization);

    List<TributaryPayrollCalculateResult> sumSocialSecurityRetentionGroupingByCostCenter(PayrollGenerationCycle payrollGenerationCycle, SocialWelfareEntity socialSecurityOrganization);

    List<TributaryPayrollCalculateResult> sumGlobalPensionFundRetentionGroupingBySocialWelfareEntity(PayrollGenerationCycle payrollGenerationCycle);

    List<TributaryPayrollCalculateResult> sumGlobalSocialSecurityRetentionGroupingBySocialWelfareEntity(PayrollGenerationCycle payrollGenerationCycle);

    @SuppressWarnings("UnnecessaryUnboxing")
    Boolean hasUnregisteredPensionFundOrganizations(PayrollGenerationCycle payrollGenerationCycle);

    @SuppressWarnings("UnnecessaryUnboxing")
    Boolean hasUnregisteredSocialSecurityOrganizations(PayrollGenerationCycle payrollGenerationCycle);
}
