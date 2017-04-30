package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.finances.Job;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.model.finances.Salary;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens S.R.L.
 * Service to management academic planning summary
 *
 * @author
 * @version $Id: AcademicPlanningSummaryServiceBean.java  09-jul-2010 12:29:07$
 */
@Stateless
@Name("academicPlanningSummaryService")
@AutoCreate
public class AcademicPlanningSummaryServiceBean implements AcademicPlanningSummaryService {

    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    /**
     * Find the teacher salary, this is from job of teacher as employee
     * @param organizationalUnit as career
     * @param cycle cycle
     * @param employeeCode teacher code mapped from academic system
     * @return Salary
     */
    public Salary findTeacherSalary(OrganizationalUnit organizationalUnit, Cycle cycle, String employeeCode) {
        log.debug("Executing findTeacherSalary.........");
        Salary salary = null;

        if (employeeCode != null) {
            List<Job> jobList = null;
            try {
                jobList = em.createNamedQuery("Job.findByOrganizationalUnitCycleEmployeeCode")
                    .setParameter("organizationalUnit", organizationalUnit)
                        .setParameter("cycle", cycle)
                        .setParameter("employeeCode", employeeCode)
                        .getResultList();
            } catch (Exception e) {
                jobList = new ArrayList<Job>();
                log.debug("Error in find teacher salary..", e);
            }
            log.debug("Teacher Job list...." + jobList);

            //get salary of first
            if (!jobList.isEmpty()) {
                Job job = jobList.get(0);
                salary = job.getSalary();
            }
        }

        return salary;
    }

}
