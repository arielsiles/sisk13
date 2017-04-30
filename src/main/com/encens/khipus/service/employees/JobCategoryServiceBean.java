package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Sector;
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
 * @version : JobCategoryServiceBean, 02-12-2009 07:49:12 PM
 */
@Stateless
@Name("jobCategoryService")
@AutoCreate
public class JobCategoryServiceBean implements JobCategoryService {

    @In("#{entityManager}")
    private EntityManager em;

    public List<JobCategory> getJobCategoriesBySector(Sector sector) {
        if (sector != null && sector.getId() != null) {
            try {
                return em.createNamedQuery("JobCategory.findBySector").setParameter("sector", sector).getResultList();
            } catch (Exception e) {
            }
        }
        return new ArrayList<JobCategory>(0);
    }

    public Long countJobCategoriesBySector(Sector sector) {
        if (sector != null) {
            try {
                return new Long(String.valueOf(em.createNamedQuery("JobCategory.countBySector").setParameter("sector", sector).getSingleResult()));
            } catch (Exception e) {
            }
        }
        return new Long(0);
    }

    public Long countActiveJobCategory(Boolean active) {
        try {
            return new Long(String.valueOf(em.createNamedQuery("JobCategory.countActiveJobCategory")
                    .setParameter("active", active).getSingleResult()));
        } catch (Exception e) {
        }
        return new Long(0);
    }

    public List<JobCategory> getActiveJobCategories(Boolean active) {
        try {
            return em.createNamedQuery("JobCategory.findActiveJobCategory")
                    .setParameter("active", active).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<JobCategory>();
    }

    public JobCategory getJobCategoryById(Long id) {
        JobCategory result = null;
        try {
            result = (JobCategory) em.createNamedQuery("JobCategory.findJobCategory").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
        }
        return result;
    }

}
