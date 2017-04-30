package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.InvoicesForm;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("invoicesFormService")
@AutoCreate
public class InvoicesFormServiceBean extends GenericServiceBean implements InvoicesFormService {

    @In("#{entityManager}")
    private EntityManager em;

    public boolean exists(PayrollGenerationCycle payrollGenerationCycle, List<JobContract> jobContracts) {
        try {
            InvoicesForm invoicesForm = (InvoicesForm) em.createNamedQuery("InvoicesForm.findByPayrollGenerationCycle")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .setParameter("jobContracts", jobContracts)
                    .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public void delete(Object entity) throws ConcurrencyException, ReferentialIntegrityException {
        InvoicesForm invoicesForm = (InvoicesForm) entity;

        try {
            super.delete(findById(InvoicesForm.class, invoicesForm.getId()));
        } catch (EntryNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Map<Long, InvoicesForm> findInvoicesFormMapByPayrollGenerationCycleAndEmployeeList(PayrollGenerationCycle payrollGenerationCycle, List<Long> employeeIdList) {
        Map<Long, InvoicesForm> resultMap = new HashMap<Long, InvoicesForm>();
        if (!ValidatorUtil.isEmptyOrNull(employeeIdList)) {
            try {
                List<Object[]> result = getEntityManager()
                        .createNamedQuery("InvoicesForm.findInvoicesFormMapByPayrollGenerationCycleAndEmployeeList")
                        .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                        .setParameter("employeeIdList", employeeIdList)
                        .getResultList();
                for (Object[] objects : result) {
                    resultMap.put((Long) objects[0], (InvoicesForm) objects[1]);
                }
            } catch (NoResultException ignored) {
            }
        }
        return resultMap;
    }

}
