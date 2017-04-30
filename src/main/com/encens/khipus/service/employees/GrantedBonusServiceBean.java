package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.GrantedBonusNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.GrantedBonus;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("grantedBonusService")
@AutoCreate
public class GrantedBonusServiceBean extends GenericServiceBean implements GrantedBonusService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @TransactionAttribute(REQUIRES_NEW)
    public void createGrantedBonus(HashMap<JobContract, GrantedBonus> jobContractBonusHashMap, List<JobContract> selectedJobContractList) {
        try {
            for (GrantedBonus grantedBonus : jobContractBonusHashMap.values()) {
                super.create(grantedBonus);
            }
        } catch (EntryDuplicatedException e) {
            throw new RuntimeException("An Unexpected error has happened ", e);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updatePayrollGenerationCycle(GrantedBonus grantedBonus)
            throws ConcurrencyException, GrantedBonusNotFoundException {
        if (!getEntityManager().contains(grantedBonus)) {
            getEntityManager().merge(grantedBonus);
        }
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void deletePayrollGenerationCycle(GrantedBonus grantedBonus)
            throws ReferentialIntegrityException,
            GrantedBonusNotFoundException {

        find(grantedBonus.getId(), listEm);
        try {
            super.delete(grantedBonus);
        } catch (ConcurrencyException e) {
            throw new GrantedBonusNotFoundException(e);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<GrantedBonus> getGrantedBonusList(PayrollGenerationCycle payrollGenerationCycle, EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        return entityManager
                .createNamedQuery("GrantedBonus.findByPayrollGenerationCycle")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle).getResultList();
    }

    public GrantedBonus find(Long id, EntityManager entityManager) throws GrantedBonusNotFoundException {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        GrantedBonus grantedBonus = entityManager.find(GrantedBonus.class, id);
        if (null == grantedBonus) {
            throw new GrantedBonusNotFoundException("Cannot find the GrantedBonus entity for id=" + id);
        }
        return grantedBonus;
    }

    @SuppressWarnings(value = "unchecked")
    public Map<Long, List<GrantedBonus>> findByPayrollGenerationCycleAndJobCategory(PayrollGenerationCycle payrollGenerationCycle, JobCategory jobCategory) {
        Map<Long, List<GrantedBonus>> marResult = new HashMap<Long, List<GrantedBonus>>();
        try {
            List<Object[]> result = getEntityManager()
                    .createNamedQuery("GrantedBonus.findByPayrollGenerationCycleAndJobCategory")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .setParameter("jobCategory", jobCategory)
                    .getResultList();
            for (Object[] objects : result) {
                Long jobContractId = (Long) objects[0];
                if (marResult.containsKey(jobContractId)) {
                    marResult.get(jobContractId).add((GrantedBonus) objects[1]);
                } else {
                    List<GrantedBonus> grantedBonusList = new ArrayList<GrantedBonus>();
                    grantedBonusList.add((GrantedBonus) objects[1]);
                    marResult.put(jobContractId, grantedBonusList);
                }
            }
            return marResult;
        } catch (NoResultException e) {
            return marResult;
        }
    }
}