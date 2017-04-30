package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.SocialWelfareEntity;
import com.encens.khipus.util.employees.TributaryPayrollCalculateResult;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.5
 */
@Name("tributaryPayrollService")
@AutoCreate
@Stateless
public class TributaryPayrollServiceBean extends GenericServiceBean implements TributaryPayrollService {

    public List<TributaryPayrollCalculateResult> sumLaboralPensionFundRetentionGroupingByCostCenter(PayrollGenerationCycle payrollGenerationCycle, SocialWelfareEntity pensionFundOrganization) {
        List<TributaryPayrollCalculateResult> resultList = new ArrayList<TributaryPayrollCalculateResult>();
        try {
            resultList = getEntityManager().createNamedQuery("TributaryPayroll.sumLaboralRetentionAFP")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .setParameter("pensionFundOrganization", pensionFundOrganization)
                    .getResultList();
        } catch (NoResultException ignored) {
        }
        return resultList;
    }

    public List<TributaryPayrollCalculateResult> sumPatronalPensionFundRetentionGroupingByCostCenter(PayrollGenerationCycle payrollGenerationCycle, SocialWelfareEntity pensionFundOrganization) {
        List<TributaryPayrollCalculateResult> resultList = new ArrayList<TributaryPayrollCalculateResult>();
        try {
            resultList = getEntityManager().createNamedQuery("TributaryPayroll.sumPatronalRetentionAFP")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .setParameter("pensionFundOrganization", pensionFundOrganization)
                    .getResultList();
        } catch (NoResultException ignored) {
        }
        return resultList;
    }

    public List<TributaryPayrollCalculateResult> sumSocialSecurityRetentionGroupingByCostCenter(PayrollGenerationCycle payrollGenerationCycle, SocialWelfareEntity socialSecurityOrganization) {
        List<TributaryPayrollCalculateResult> resultList = new ArrayList<TributaryPayrollCalculateResult>();
        try {
            resultList = getEntityManager().createNamedQuery("TributaryPayroll.sumCNS")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .setParameter("socialSecurityOrganization", socialSecurityOrganization)
                    .getResultList();
        } catch (NoResultException ignored) {
        }
        return resultList;
    }

    public List<TributaryPayrollCalculateResult> sumGlobalPensionFundRetentionGroupingBySocialWelfareEntity(PayrollGenerationCycle payrollGenerationCycle) {
        List<TributaryPayrollCalculateResult> resultList = new ArrayList<TributaryPayrollCalculateResult>();
        try {
            resultList = getEntityManager().createNamedQuery("TributaryPayroll.sumGlobalRetentionAFP")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .getResultList();
        } catch (NoResultException ignored) {
        }
        return resultList;
    }

    public List<TributaryPayrollCalculateResult> sumGlobalSocialSecurityRetentionGroupingBySocialWelfareEntity(PayrollGenerationCycle payrollGenerationCycle) {
        List<TributaryPayrollCalculateResult> resultList = new ArrayList<TributaryPayrollCalculateResult>();
        try {
            resultList = getEntityManager().createNamedQuery("TributaryPayroll.sumGlobalRetentionCNS")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .getResultList();
        } catch (NoResultException ignored) {
        }
        return resultList;
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    public Boolean hasUnregisteredPensionFundOrganizations(PayrollGenerationCycle payrollGenerationCycle) {
        Long counter = null;
        try {
            counter = (Long) getEntityManager().createNamedQuery("TributaryPayroll.countUnregisteredPensionFundOrganization")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .getSingleResult();
        } catch (NoResultException ignored) {
        }
        return counter == null || counter.longValue() > 0;
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    public Boolean hasUnregisteredSocialSecurityOrganizations(PayrollGenerationCycle payrollGenerationCycle) {
        Long counter = null;
        try {
            counter = (Long) getEntityManager().createNamedQuery("TributaryPayroll.countUnregisteredSocialSecurityOrganization")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .getSingleResult();
        } catch (NoResultException ignored) {
        }
        return counter == null || counter.longValue() > 0;
    }
}
