package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.Job;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.model.finances.Salary;

import javax.ejb.Local;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : JobService, 03-12-2009 10:58:00 AM
 */
@Local
public interface JobService {
    List<Job> getJobsByOrgUnitCategorySectorAndSalary(OrganizationalUnit organizationalUnit, JobCategory jobCategory, Sector sector, Salary salary);
}
