package com.encens.khipus.service.employees;

import com.encens.khipus.exception.employees.JobContractException;
import com.encens.khipus.exception.employees.TaxPayrollException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.26
 */

@Stateless
@Name("tributaryPayrollGeneratorService")
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class TributaryPayrollGeneratorServiceBean extends GenericServiceBean implements TributaryPayrollGeneratorService {

    @In
    private TaxPayrollUtilService taxPayrollUtilService;

    @In
    private PayrollGenerationCycleService payrollGenerationCycleService;
    @Resource
    private UserTransaction userTransaction;

    @PersistenceContext(unitName = "khipus")
    private EntityManager khipusEm;

    public TaxPayrollGenerated getOfficialTributaryPayroll(ConfigurationTaxPayroll configuration) {
        TaxPayrollGenerated result = null;
        try {
            result = (TaxPayrollGenerated) getEntityManager().
                    createNamedQuery("TaxPayrollGenerated.findByTypeAndEvaluationState")
                    .setParameter("configurationTaxPayroll", configuration)
                    .setParameter("type", TaxPayrollGeneratedType.TRIBUTARY)
                    .setParameter("evaluationState", TaxPayrollEvaluationState.OFFICIAL)
                    .getSingleResult();
        } catch (NoResultException e) {
            //
        }
        return result;
    }

    public void validateGestionPayrollEmployees(ConfigurationTaxPayroll configuration) throws TaxPayrollException {
        List<ManagersPayroll> managersPayrolls = taxPayrollUtilService.getManagersPayrolls(configuration);

        List<String> managersKeys = new ArrayList<String>();
        for (ManagersPayroll managersPayroll : managersPayrolls) {
            String key = taxPayrollUtilService.getManagersPayrollCacheKey(managersPayroll.getEmployee(),
                    managersPayroll.getJobCategory());
            managersKeys.add(key);
        }

        List<Employee> unregisteredEmployees = new ArrayList<Employee>();
        List<JobContract> jobContracts = taxPayrollUtilService.getActiveJobContracts(configuration);
        for (JobContract jobContract : jobContracts) {
            String key = taxPayrollUtilService.getManagersPayrollCacheKey(jobContract.getContract().getEmployee(),
                    jobContract.getJob().getJobCategory());
            if (!managersKeys.contains(key)) {
                unregisteredEmployees.add(jobContract.getContract().getEmployee());
            }
        }

        if (!unregisteredEmployees.isEmpty()) {
            throw new JobContractException(unregisteredEmployees);
        }
    }


    @SuppressWarnings(value = "unchecked")
    public Map<Long, TributaryPayroll> getTributaryPayrollsForLastMonth(PayrollGenerationCycle payrollGenerationCycle, List<Long> employeeIdList) {
        Map<Long, TributaryPayroll> cache = new HashMap<Long, TributaryPayroll>();
        PayrollGenerationCycle lastPayrollGenerationCycle = payrollGenerationCycleService.getLastPayrollGenerationCycle(payrollGenerationCycle);
        if (null != lastPayrollGenerationCycle) {
            List<TributaryPayroll> tributaryPayrollList = findByPayrollGenerationCycleAndEmployeeList(lastPayrollGenerationCycle, employeeIdList);
            for (TributaryPayroll tributaryPayroll : tributaryPayrollList) {
                cache.put(tributaryPayroll.getEmployee().getId(), tributaryPayroll);
            }
        }
        return cache;
    }

    @SuppressWarnings(value = "unchecked")
    private List<TributaryPayroll> findByPayrollGenerationCycleAndEmployeeList(PayrollGenerationCycle payrollGenerationCycle,
                                                                               List<Long> employeeList) {
        if (!ValidatorUtil.isEmptyOrNull(employeeList)) {
            try {
                return getEntityManager()
                        .createNamedQuery("TributaryPayroll.findByPayrollGenerationCycleAndEmployeeList")
                        .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                        .setParameter("employeeIdList", employeeList)
                        .getResultList();
            } catch (NoResultException ignored) {
            }
        }
        return new ArrayList<TributaryPayroll>();
    }

    @Override
    protected EntityManager getEntityManager() {
        return khipusEm;
    }
}
