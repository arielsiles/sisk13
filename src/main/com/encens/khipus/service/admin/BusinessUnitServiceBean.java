package com.encens.khipus.service.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.contacts.Organization;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * BusinessUnit service implementation class
 *
 * @author
 * @version 2.26
 */

@Stateless
@Name("businessUnitService")
@AutoCreate
public class BusinessUnitServiceBean extends GenericServiceBean implements BusinessUnitService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public BusinessUnit findById(Long id) {
        return em.find(BusinessUnit.class, id);
    }

    public BusinessUnit findByUser(User user) {
        try {
            return (BusinessUnit) em.createQuery("select u.employee.businessUnit from User u where u =:user")
                    .setParameter("user", user).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void delete(BusinessUnit businessUnit) throws EntryNotFoundException, ConcurrencyException, ReferentialIntegrityException {
        Organization organization = findById(Organization.class, businessUnit.getOrganization().getId());
        super.delete(businessUnit);
        super.delete(organization);
    }

    /**
     * This method
     *
     * @param executorUnitCode The business Unit executor unit code
     * @return The entity found
     */
    public BusinessUnit findBusinessUnitByExecutorUnitCode(String executorUnitCode) {
        BusinessUnit businessUnit = (BusinessUnit) em.createNamedQuery("BusinessUnit.findByExecutorUnitCode")
                .setParameter("executorUnitCode", executorUnitCode)
                .getSingleResult();
        return businessUnit;
    }

    @SuppressWarnings(value = "unchecked")
    public List<BusinessUnit> findAll(EntityManager entityManager) {
        if (null == entityManager) {
            entityManager = getEntityManager();
        }
        return entityManager.createNamedQuery("BusinessUnit.findAll")
                .getResultList();
    }
}
