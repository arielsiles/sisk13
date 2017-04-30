package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.ExtraHoursWorked;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.JobContract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("extraHoursWorkedService")
@AutoCreate
public class ExtraHoursWorkedServiceBean extends GenericServiceBean implements ExtraHoursWorkedService {

    public boolean exists(PayrollGenerationCycle payrollGenerationCycle, JobContract jobContract) {
        try {
            getEntityManager().createNamedQuery("ExtraHoursWorked.findByPayrollGenerationCycleAndJobCategoryAndEmployee")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .setParameter("employee", jobContract.getContract().getEmployee())
                    .setParameter("jobCategory", jobContract.getJob().getJobCategory())
                    .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public void delete(Object entity) throws ConcurrencyException, ReferentialIntegrityException {
        ExtraHoursWorked extraHoursWorked = (ExtraHoursWorked) entity;

        try {
            super.delete(findById(ExtraHoursWorked.class, extraHoursWorked.getId()));
        } catch (EntryNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings(value = "unchecked")
    public Map<Long, ExtraHoursWorked> findByPayrollGenerationCycleAndJobCategory(PayrollGenerationCycle payrollGenerationCycle, JobCategory jobCategory) {
        HashMap<Long, ExtraHoursWorked> extraHoursWorkedMap = new HashMap<Long, ExtraHoursWorked>();
        try {
            List<Object[]> result = getEntityManager().createNamedQuery("ExtraHoursWorked.findByPayrollGenerationCycleAndJobCategory")
                    .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                    .setParameter("jobCategory", jobCategory)
                    .getResultList();
            for (Object[] objects : result) {
                extraHoursWorkedMap.put((Long) objects[0], (ExtraHoursWorked) objects[1]);
            }
            return extraHoursWorkedMap;
        } catch (NoResultException e) {
            return new HashMap<Long, ExtraHoursWorked>();
        }
    }

}
