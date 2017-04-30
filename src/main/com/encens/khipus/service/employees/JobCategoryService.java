package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;

import javax.ejb.Local;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : JobCategoryService, 02-12-2009 07:49:32 PM
 */
@Local
public interface JobCategoryService {

    List<JobCategory> getJobCategoriesBySector(Sector sector);

    Long countJobCategoriesBySector(Sector sector);

    Long countActiveJobCategory(Boolean active);

    List<JobCategory> getActiveJobCategories(Boolean active);

    JobCategory getJobCategoryById(Long id);
}
