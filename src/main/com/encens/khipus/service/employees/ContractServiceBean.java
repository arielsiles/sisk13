package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ContractServiceBean
 *
 * @author
 */
@Name("contractService")
@Stateless
@AutoCreate
public class ContractServiceBean implements ContractService {

    @In("#{entityManager}")
    private EntityManager em;

    @In
    private GenericService genericService;


    /**
     * Finds the contract list of employees, the list contains between initDate and enDate for a employee.
     *
     * @param employee
     * @param initDate
     * @param endDate
     */
    public List<Contract> getContractsByEmployeeInDateRange(Employee employee, Date initDate, Date endDate) {
        try {
            return em.createNamedQuery("Contract.findByEmployeeInDateRange")
                    .setParameter("employee", employee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<Contract>(0);
    }

    /**
     * Finds the contract list of employees, the list contains between initDate and enDate for a employee.
     *
     * @param businessUnit
     * @param jobCategory
     * @param employee
     * @param initDate
     * @param endDate
     * @return
     */
    public List<Contract> getContractsForPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Employee employee, Date initDate, Date endDate) {
        try {
            return em.createNamedQuery("Contract.findContractsForPayrollGeneration")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("employee", employee)
                    .setParameter("initDate", initDate)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<Contract>(0);
    }

    public List<Contract> getContractsForPayrollGenerationByLastDayOfMonth(BusinessUnit businessUnit, JobCategory jobCategory, Employee employee, Date initDate, Date endDate) {
        Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(endDate);
        Date lastDayOfMonth = DateUtils.getLastDayOfMonth(endDate);

        Calendar first = DateUtils.toCalendar(firstDayOfMonth);
        first.set(Calendar.HOUR, 0);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        first.set(Calendar.MILLISECOND, 0);
        firstDayOfMonth = first.getTime();

        lastDayOfMonth.setDate(30);
        Calendar last = DateUtils.toCalendar(lastDayOfMonth);
        last.set(Calendar.HOUR, 0);
        last.set(Calendar.MINUTE, 0);
        last.set(Calendar.SECOND, 0);
        last.set(Calendar.MILLISECOND, 0);
        lastDayOfMonth = last.getTime();
        try {
            return em.createNamedQuery("Contract.findContractsForPayrollGenerationByLastDayOfMonth")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("jobCategory", jobCategory)
                    .setParameter("employee", employee)
                    .setParameter("initDate", initDate)
                    .setParameter("lastDayOfMonth", lastDayOfMonth)
                    .setParameter("endDate", endDate)
                    .setParameter("activeForPayrollGeneration", Boolean.TRUE).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<Contract>(0);
    }

    /**
     * Find all contracts related to employee in range date
     * @param employee
     * @param initDate
     * @param endDate
     * @return List
     */
    public List<Contract> getContractsByEmployeeInGestion(Employee employee, Date initDate, Date endDate) {
        List<Contract> contractList;
        contractList = em.createNamedQuery("Contract.findByEmployeeInGestion")
                .setParameter("employee", employee)
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .getResultList();
        return contractList;
    }

    /**
     * Get the career (organizational unit) with more groups and asignatures for this employee in this contract
     * range dates
     * @param employee
     * @param initDate
     * @param endDate
     * @return OrganizationalUnit
     */
    public OrganizationalUnit getMaxOrganizationalUnitProfessorAssigned(Employee employee, Date initDate, Date endDate) {
        OrganizationalUnit organizationalUnit = null;

        List resultList;
        resultList = em.createNamedQuery("Contract.findMaxProfessorCareerAssigned")
                .setParameter("employee", employee)
                .setParameter("initDate", initDate)
                .setParameter("endDate", endDate)
                .getResultList();

        for (int i = 0; i < resultList.size(); i++) {
            Object[] row = (Object[]) resultList.get(i);

            Long organizationalUnitId = (Long) row[0];
            organizationalUnit = findOrganizationalUnit(organizationalUnitId);
            if (organizationalUnit != null) {
                break;
            }
        }
        return organizationalUnit;
    }

    private OrganizationalUnit findOrganizationalUnit(Long organizationalUnitId) {
        OrganizationalUnit organizationalUnit = null;

        if (organizationalUnitId != null) {
            try {
                organizationalUnit = genericService.findById(OrganizationalUnit.class, organizationalUnitId);
            } catch (EntryNotFoundException ignore) {
            }
        }
        return organizationalUnit;
    }

}
