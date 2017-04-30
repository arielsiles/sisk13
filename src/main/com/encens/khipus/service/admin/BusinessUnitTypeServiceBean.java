package com.encens.khipus.service.admin;

import com.encens.khipus.model.admin.BusinessUnitType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * @author
 *         BusinessUnitTypeService implementation class
 * @version 1.2.3
 */
@Stateless(name = "BusinessUnitTypeServiceBean")
@Name("businessUnitTypeService")
@AutoCreate
public class BusinessUnitTypeServiceBean implements BusinessUnitTypeService {
    @In("#{entityManager}")
    private EntityManager em;

    public Long countMainBusinessUnitType() {
        try {
            return (Long) em.createNamedQuery("BusinessUnitType.countMainBusinessUnitType").setParameter("mainValue", Boolean.TRUE).getSingleResult();
        } catch (NoResultException e) {
            return new Long(0);
        }
    }

    public BusinessUnitType findBusinessUnitType(Long id) {
        try {
            return ((BusinessUnitType) em.createNamedQuery("BusinessUnitType.isMainBusinessUnitType").setParameter("id", id).getSingleResult());
        } catch (NoResultException e) {
            return null;
        }
    }
}
