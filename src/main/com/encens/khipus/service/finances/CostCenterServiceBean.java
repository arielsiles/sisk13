package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.CostCenter;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * Encens S.R.L.
 * This class implements the costCenter service
 *
 * @author
 * @version 2.0.2
 */

@Stateless
@Name("costCenterService")
@AutoCreate
public class CostCenterServiceBean implements CostCenterService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    /**
     * This method finds a CostCenter by code
     *
     * @param code The business Unit executor unit code
     * @return The entity found
     */
    public CostCenter findCostCenterByCode(String code) {
        CostCenter costCenter = (CostCenter) em.createNamedQuery("CostCenter.findByCode")
                .setParameter("code", code)
                .getSingleResult();
        return costCenter;
    }
}
