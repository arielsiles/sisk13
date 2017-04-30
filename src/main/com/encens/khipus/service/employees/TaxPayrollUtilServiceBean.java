package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.JobContract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */

@Stateless
@Name("taxPayrollUtilService")
@AutoCreate
public class TaxPayrollUtilServiceBean extends GenericServiceBean implements TaxPayrollUtilService {

    @In
    private GestionPayrollService gestionPayrollService;

    public AFPRate getActiveAfpRate(AFPRateType afpRateType) {
        try {
            return (AFPRate) getEntityManager()
                    .createNamedQuery("AFPRate.findByActiveAFPRate")
                    .setParameter("active", true)
                    .setParameter("afpRateType", afpRateType)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public DiscountRule findActiveNationalSolidaryAfpDiscountRule() {
        try {
            return (DiscountRule) getEntityManager()
                    .createNamedQuery("DiscountRule.findActiveNationalSolidaryAfpDiscountRule")
                    .setParameter("active", true)
                    .setParameter("discountRuleType", DiscountRuleType.SOLIDARY_AFP)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public CNSRate getActiveCnsRate() {
        try {
            return (CNSRate) getEntityManager()
                    .createNamedQuery("CNSRate.findByActiveCNSRate")
                    .setParameter("active", true)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public IVARate getActiveIvaRate() {
        try {
            return (IVARate) getEntityManager()
                    .createNamedQuery("IVARate.findByActiveIVARATE")
                    .setParameter("active", true)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public SMNRate getActiveSmnRate() {
        try {
            return (SMNRate) getEntityManager()
                    .createNamedQuery("SMNRate.findByActiveSMNRate")
                    .setParameter("active", true)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public SeniorityBonus getActiveSeniorityBonus() {
        try {
            return (SeniorityBonus) getEntityManager()
                    .createNamedQuery("SeniorityBonus.findActiveBonus")
                    .setParameter("active", true)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<ExtraHoursWorked> getExtraHoursWorkedList(PayrollGenerationCycle payrollGenerationCycle) {
        List<ExtraHoursWorked> result = new ArrayList<ExtraHoursWorked>();

        try {
            result = getEntityManager().createNamedQuery("ExtraHoursWorked.findByPayrollGenerationCycle_II")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .getResultList();
        } catch (Exception e) {
            //
        }

        return result;
    }

    @SuppressWarnings(value = "unchecked")
    public List<JobContract> getActiveJobContracts(List<Long> identifiers) {
        List<JobContract> result = new ArrayList<JobContract>();
        try {
            result = getEntityManager().createNamedQuery("JobContract.findJobContractByIdentifiers")
                    .setParameter("identifiers", identifiers)
                    .getResultList();
        } catch (Exception e) {
            //
        }
        return result;
    }

    @SuppressWarnings(value = "unchecked")
    public List<JobContract> getActiveJobContracts(ConfigurationTaxPayroll configuration) {
        List<JobContract> result = new ArrayList<JobContract>();
        try {
            result = getEntityManager().createNamedQuery("JobContract.findJobContractToGenerateTaxPayroll")
                    .setParameter("activeForTaxPayrollGeneration", true)
                    .setParameter("startDate", configuration.getStartDate())
                    .setParameter("endDate", configuration.getEndDate())
                    .setParameter("businessUnit", configuration.getBusinessUnit())
                    .getResultList();
        } catch (Exception e) {
            //
        }
        return result;
    }

    public GeneratedPayroll getTaxPayrollGeneratedInstance(PayrollGenerationCycle payrollGenerationCycle) {
        GeneratedPayroll instance = new GeneratedPayroll();
        instance.setPayrollGenerationCycle(payrollGenerationCycle);
//        instance.setEvaluationState(TaxPayrollEvaluationState.TEST);
        instance.setGenerationDate(new Date());

        return instance;
    }

    public Boolean existTaxPayrollGenerated(ConfigurationTaxPayroll configuration) {
        try {
            Long counter = (Long) getEntityManager().createNamedQuery("TaxPayrollGenerated.countByConfigurationTaxPayroll")
                    .setParameter("configurationTaxPayroll", configuration).getSingleResult();
            return counter != 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public Boolean existsInvoiceForms(PayrollGenerationCycle payrollGenerationCycle) {
        try {
            Long counter = (Long) getEntityManager().createNamedQuery("InvoicesForm.countByPayrollGenerationCycle")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle).getSingleResult();
            return counter != 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public TaxPayrollGenerated getOfficialTaxPayrollGenerated(PayrollGenerationCycle payrollGenerationCycle,
                                                              TaxPayrollGeneratedType type, EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        try {
            return (TaxPayrollGenerated) entityManager
                    .createNamedQuery("TaxPayrollGenerated.findByTypeAndEvaluationState")
                    .setParameter("configurationTaxPayroll", payrollGenerationCycle)
                    .setParameter("evaluationState", TaxPayrollEvaluationState.OFFICIAL)
                    .setParameter("type", type)
                    .getSingleResult();
        } catch (NoResultException e) {
            //
        }
        return null;
    }

    public Boolean existOfficialTaxPayrollGenerated(PayrollGenerationCycle payrollGenerationCycle,
                                                    TaxPayrollGeneratedType type, EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        return null != getOfficialTaxPayrollGenerated(payrollGenerationCycle, type, entityManager);
    }

    @SuppressWarnings(value = "unchecked")
    public List<ManagersPayroll> getManagersPayrolls(ConfigurationTaxPayroll configurationTaxPayroll) {

        List<ManagersPayroll> result = new ArrayList<ManagersPayroll>();

        List<GeneratedPayroll> officialGeneratedPayrolls = getOfficialGeneratedPayrolls(configurationTaxPayroll);
        if (!officialGeneratedPayrolls.isEmpty()) {
            try {
                result = getEntityManager().createNamedQuery("ManagersPayroll.findByGeneratedPayrollList")
                        .setParameter("generatedPayrolls", officialGeneratedPayrolls)
                        .getResultList();
            } catch (Exception e) {
                //
            }
        }

        return result;
    }

    public String getManagersPayrollCacheKey(Employee employee, JobCategory jobCategory) {
        return employee.getId() + "_" + jobCategory.getId();
    }

    private List<GeneratedPayroll> getOfficialGeneratedPayrolls(ConfigurationTaxPayroll configurationTaxPayroll) {
        List<GeneratedPayroll> result = new ArrayList<GeneratedPayroll>();
        if (null != configurationTaxPayroll.getAdministrativePayrolls()) {
            for (AdministrativeGestionPayroll element : configurationTaxPayroll.getAdministrativePayrolls()) {
                GestionPayroll gestionPayroll = element.getAdministrativeGestionPayroll();

                GeneratedPayroll generatedPayroll = gestionPayrollService.findOfficialGeneratedPayroll(gestionPayroll.getBusinessUnit(),
                        gestionPayroll.getJobCategory(),
                        gestionPayroll.getGestion(),
                        GestionPayrollType.SALARY,
                        gestionPayroll.getMonth());
                if (null != generatedPayroll) {
                    result.add(generatedPayroll);
                }
            }
        }

        return result;
    }
}
