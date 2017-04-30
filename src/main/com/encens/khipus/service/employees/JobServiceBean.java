package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.Job;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.model.finances.Salary;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : JobService, 03-12-2009 10:57:36 AM
 */
@Stateless
@Name("jobService")
@AutoCreate
public class JobServiceBean implements JobService {
    @In("#{entityManager}")
    private EntityManager em;

    public List<Job> getJobsByOrgUnitCategorySectorAndSalary(OrganizationalUnit organizationalUnit, JobCategory jobCategory, Sector sector, Salary salary) {

        if (organizationalUnit != null && jobCategory != null && sector != null && salary != null &&
                organizationalUnit.getId() != null && jobCategory.getId() != null && sector.getId() != null && salary.getId() != null) {
            try {
                return em.createNamedQuery("Job.findByOrgUnitCategorySectorAndSalary")
                        .setParameter("organizationalUnit", organizationalUnit)
                        .setParameter("sector", sector)
                        .setParameter("jobCategory", jobCategory)
                        .setParameter("salary", salary).getResultList();
            } catch (Exception e) {

            }
        }

        return new ArrayList<Job>(0);
    }
}
