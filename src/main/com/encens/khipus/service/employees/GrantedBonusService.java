package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.GrantedBonusNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GrantedBonus;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.26
 */
@Local
public interface GrantedBonusService extends GenericService {


    GrantedBonus find(Long id, EntityManager entityManager) throws GrantedBonusNotFoundException;

    @TransactionAttribute(REQUIRES_NEW)
    void updatePayrollGenerationCycle(GrantedBonus grantedBonus)
            throws ConcurrencyException, GrantedBonusNotFoundException;

    @TransactionAttribute(REQUIRES_NEW)
    void deletePayrollGenerationCycle(GrantedBonus grantedBonus)
            throws ReferentialIntegrityException,
            GrantedBonusNotFoundException;

    @TransactionAttribute(REQUIRES_NEW)
    void createGrantedBonus(HashMap<JobContract, GrantedBonus> jobContractBonusHashMap, List<JobContract> selectedJobContractList);

    @SuppressWarnings(value = "unchecked")
    Map<Long, List<GrantedBonus>> findByPayrollGenerationCycleAndJobCategory(PayrollGenerationCycle payrollGenerationCycle, JobCategory jobCategory);
}