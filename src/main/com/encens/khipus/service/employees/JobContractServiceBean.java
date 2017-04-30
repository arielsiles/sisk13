package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.Job;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : JobContractServiceBean, 03-12-2009 04:24:28 PM
 */
@Stateless
@Name("jobContractService")
@AutoCreate
public class JobContractServiceBean extends GenericServiceBean implements JobContractService {

    @In("#{entityManager}")
    private EntityManager em;

    @Override
    public void delete(Object entity) throws ConcurrencyException, ReferentialIntegrityException {
        Long contractId = ((JobContract) entity).getContract().getId();
        Long jobId = ((JobContract) entity).getJob().getId();
        super.delete(entity);
        try {
            super.delete(findById(Contract.class, contractId));
        } catch (Exception e) {
        }
        try {
            super.delete(findById(Job.class, jobId));
        } catch (Exception e) {
        }
    }

    public JobContract getJobContractByEmployeeAndOrgUnitAndDatesOfContract(Employee employee, String career, Date initDate, Date endDate) {
        JobContract result = null;
        try {
            result = (JobContract) em.createNamedQuery("JobContract.findJobContractByEmployeeAndOrgUnitAndDatesOfContract")
                    .setParameter("employee", employee)
                    .setParameter("career", career)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .getSingleResult();
        } catch (Exception e) {
        }

        return result;
    }

    public List<JobContract> getJobContractList(Employee emloyee) {

        ArrayList result = new ArrayList<JobContract>();

        try {
            result = (ArrayList<JobContract>) em.createNamedQuery("JobContract.findJobContractByEmployee")
                    .setParameter("employee", emloyee)
                    .getResultList();
        } catch (Exception e) {
        }

        return result;
    }

    @Override
    public JobContract getJobContract(Employee emloyee) {

        List<JobContract> result = new ArrayList<JobContract>();

        try {
            result = (ArrayList<JobContract>) em.createNamedQuery("JobContract.findJobContractByEmployee")
                    .setParameter("employee", emloyee)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }

        return result.get(0);
    }

    public List<JobContract> getJobContractByEmployeeAndOrgUnit(Employee emloyee, OrganizationalUnit organizationalUnit) {

        ArrayList result = new ArrayList<JobContract>();

        try {
            result = (ArrayList<JobContract>) em.createNamedQuery("JobContract.findJobContractByEmployeeAndOrgUnit")
                    .setParameter("employee", emloyee)
                    .setParameter("organizationalUnit", organizationalUnit)
                    .getResultList();
        } catch (Exception e) {
        }

        return result;
    }

    public JobContract lastJobContractByEmployee(Employee employee) {
        Long lastJobContractId = (Long) em.createNamedQuery("JobContract.findLastJobContractByEmployee")
                .setParameter("employee", employee).getSingleResult();
        if (lastJobContractId != null) {
            return em.find(JobContract.class, lastJobContractId);
        }
        return null;
    }

    public JobContract find(JobContract jobContract, EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        Query query = entityManager.createNamedQuery("JobContract.findJobContract");
        query.setParameter("id", jobContract.getId());
        return (JobContract) query.getSingleResult();
    }

    public JobContract load(JobContract jobContract) {
        return load(jobContract, null);
    }

    public JobContract load(JobContract jobContract, EntityManager entityManager) {
        JobContract result = null;
        List<Long> idList = new ArrayList<Long>(1);
        idList.add(jobContract.getId());
        List<JobContract> jobContractList = load(idList, entityManager);
        if (!ValidatorUtil.isEmptyOrNull(jobContractList)) {
            result = jobContractList.get(0);
        }
        return result;
    }

    public List<JobContract> load(List<Long> idList) {
        return load(idList, null);
    }

    @SuppressWarnings({"unchecked"})
    public List<JobContract> load(List<Long> idList, EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        List<JobContract> result = null;
        try {
            if (!ValidatorUtil.isEmptyOrNull(idList)) {
                result = entityManager.createNamedQuery("JobContract.loadJobContracts")
                        .setParameter("idList", idList)
                        .getResultList();
            }
        } catch (NoResultException ignored) {
        }
        return result;
    }

    @SuppressWarnings({"unchecked"})
    public List<JobContract> findActiveByEmployeeAndDateRange(Employee emloyee, Date initDate, Date endDate, PayrollGenerationType payrollGenerationType) {
        ArrayList result = new ArrayList<JobContract>();
        try {
            result = (ArrayList<JobContract>) em.createNamedQuery("JobContract.findActiveByEmployeeAndDateRange")
                    .setParameter("employee", emloyee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("payrollGenerationType", payrollGenerationType)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE)
                    .getResultList();
            return result;
        } catch (NoResultException e) {
            return result;
        }
    }

    public Long countActiveByEmployeeAndDateRangeAndGenerationType(Employee emloyee, Date initDate, Date endDate, PayrollGenerationType payrollGenerationType) {
        try {
            return (Long) em.createNamedQuery("JobContract.findActiveByEmployeeAndDateRangeAndGenerationType")
                    .setParameter("employee", emloyee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("payrollGenerationType", payrollGenerationType)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE)
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

}
